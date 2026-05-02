package com.vzaimno.app.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.vzaimno.app.core.common.IoDispatcher
import com.vzaimno.app.core.common.trimmedOrNull
import com.vzaimno.app.core.config.AppConfig
import com.vzaimno.app.core.model.AcceptedOfferResult
import com.vzaimno.app.core.model.AccessToken
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementOffer
import com.vzaimno.app.core.model.CreateAnnouncementDraft
import com.vzaimno.app.core.model.DisputeState
import com.vzaimno.app.core.model.EditableProfileFields
import com.vzaimno.app.core.model.LoginCredentials
import com.vzaimno.app.core.model.OfferPricingMode
import com.vzaimno.app.core.model.RegisterCredentials
import com.vzaimno.app.core.model.ReportReasonOption
import com.vzaimno.app.core.model.ReviewEligibility
import com.vzaimno.app.core.model.ReviewRole
import com.vzaimno.app.core.model.RouteBuildRequest
import com.vzaimno.app.core.model.RouteContext
import com.vzaimno.app.core.model.RouteDetails
import com.vzaimno.app.core.model.SessionUser
import com.vzaimno.app.core.model.UserProfile
import com.vzaimno.app.core.model.UserProfileReview
import com.vzaimno.app.core.model.UserReviewFeed
import com.vzaimno.app.core.network.ApiErrorMapper
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.core.network.UploadFilePayload
import com.vzaimno.app.core.network.safeApiCall
import com.vzaimno.app.core.network.toMultipartPart
import com.vzaimno.app.data.mappers.toDomain
import com.vzaimno.app.data.mappers.toDto
import com.vzaimno.app.data.mappers.toEditableFields
import com.vzaimno.app.data.remote.AnnouncementApi
import com.vzaimno.app.data.remote.AuthApi
import com.vzaimno.app.data.remote.ChatApi
import com.vzaimno.app.data.remote.DeviceApi
import com.vzaimno.app.data.remote.ProfileApi
import com.vzaimno.app.data.remote.RouteApi
import com.vzaimno.app.data.remote.dto.AppealRequestDto
import com.vzaimno.app.data.remote.dto.CounterpartyDisputeResponseRequestDto
import com.vzaimno.app.data.remote.dto.CreateOfferRequestDto
import com.vzaimno.app.data.remote.dto.DeviceRegistrationRequestDto
import com.vzaimno.app.data.remote.dto.DisputeStateDto
import com.vzaimno.app.data.remote.dto.EmptyBodyDto
import com.vzaimno.app.data.remote.dto.ExecutionStageUpdateRequestDto
import com.vzaimno.app.data.remote.dto.OpenDisputeRequestDto
import com.vzaimno.app.data.remote.dto.ReportSubmissionRequestDto
import com.vzaimno.app.data.remote.dto.SelectDisputeOptionRequestDto
import com.vzaimno.app.data.remote.dto.SendChatMessageRequestDto
import com.vzaimno.app.data.remote.dto.SubmitReviewRequestDto
import com.vzaimno.app.data.remote.dto.UnregisterDeviceRequestDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

interface AuthRepository {
    suspend fun register(credentials: RegisterCredentials): ApiResult<AccessToken>
    suspend fun login(credentials: LoginCredentials): ApiResult<AccessToken>
    suspend fun fetchMe(): ApiResult<SessionUser>
}

interface ProfileRepository {
    suspend fun fetchMeProfile(): ApiResult<UserProfile>
    suspend fun updateMyProfile(fields: EditableProfileFields): ApiResult<EditableProfileFields>
    suspend fun fetchMyReviews(limit: Int, offset: Int): ApiResult<List<UserProfileReview>>
    suspend fun fetchMyReviewsFeed(
        limit: Int,
        offset: Int,
        role: ReviewRole,
    ): ApiResult<UserReviewFeed>

    suspend fun fetchReviewEligibility(announcementId: String): ApiResult<ReviewEligibility>
    suspend fun submitReview(announcementId: String, stars: Int, text: String): ApiResult<Unit>
}

interface DeviceRepository {
    suspend fun registerCurrentDevice(): ApiResult<Unit>
    suspend fun unregisterCurrentDevice(): ApiResult<Unit>
}

interface AnnouncementRepository {
    suspend fun createAnnouncement(draft: CreateAnnouncementDraft): ApiResult<Announcement>
    suspend fun fetchMyAnnouncements(): ApiResult<List<Announcement>>
    suspend fun fetchPublicAnnouncements(): ApiResult<List<Announcement>>
    suspend fun fetchAnnouncement(announcementId: String): ApiResult<Announcement>
    suspend fun uploadAnnouncementMedia(
        announcementId: String,
        files: List<UploadFilePayload>,
    ): ApiResult<Announcement>

    suspend fun archiveAnnouncement(announcementId: String): ApiResult<Announcement>
    suspend fun deleteAnnouncement(announcementId: String): ApiResult<Boolean>
    suspend fun appealAnnouncement(announcementId: String, reason: String?): ApiResult<Announcement>
    suspend fun createOffer(
        announcementId: String,
        message: String?,
        proposedPrice: Int?,
        pricingMode: OfferPricingMode,
        agreedPrice: Int?,
        minimumPriceAccepted: Boolean,
    ): ApiResult<AnnouncementOffer>

    suspend fun fetchOffers(announcementId: String): ApiResult<List<AnnouncementOffer>>
    suspend fun acceptOffer(announcementId: String, offerId: String): ApiResult<AcceptedOfferResult>
    suspend fun rejectOffer(announcementId: String, offerId: String): ApiResult<Unit>
    suspend fun updateExecutionStage(announcementId: String, stage: String): ApiResult<Announcement>
}

interface ChatRepository {
    suspend fun fetchThreads(): ApiResult<List<com.vzaimno.app.core.model.ChatThreadPreview>>
    suspend fun fetchMessages(
        threadId: String,
        limit: Int,
        before: Instant? = null,
    ): ApiResult<List<com.vzaimno.app.core.model.ChatMessage>>

    suspend fun sendMessage(threadId: String, text: String): ApiResult<com.vzaimno.app.core.model.ChatMessage>
    suspend fun sendImageMessage(
        threadId: String,
        text: String?,
        file: UploadFilePayload,
    ): ApiResult<com.vzaimno.app.core.model.ChatMessage>

    suspend fun ensureSupportThread(): ApiResult<String>
    suspend fun fetchSupportMessages(
        threadId: String,
        limit: Int,
        before: Instant? = null,
    ): ApiResult<List<com.vzaimno.app.core.model.ChatMessage>>

    suspend fun sendSupportMessage(
        threadId: String,
        text: String,
    ): ApiResult<com.vzaimno.app.core.model.ChatMessage>

    suspend fun fetchReportReasonOptions(): ApiResult<List<ReportReasonOption>>
    suspend fun submitReport(
        targetType: String,
        targetId: String,
        reasonCode: String,
        reasonText: String?,
    ): ApiResult<Unit>

    suspend fun fetchActiveDispute(threadId: String): ApiResult<DisputeState?>
    suspend fun openDispute(
        threadId: String,
        problemTitle: String,
        problemDescription: String,
        requestedCompensationRub: Int,
        desiredResolution: String,
    ): ApiResult<DisputeState>

    suspend fun acceptCounterpartyDispute(
        threadId: String,
        disputeId: String,
    ): ApiResult<DisputeState>

    suspend fun respondCounterpartyDispute(
        threadId: String,
        disputeId: String,
        responseDescription: String,
        acceptableRefundPercent: Int,
        desiredResolution: String,
    ): ApiResult<DisputeState>

    suspend fun selectDisputeOption(
        threadId: String,
        disputeId: String,
        optionId: String,
    ): ApiResult<DisputeState>
}

interface RouteRepository {
    suspend fun fetchAnnouncementRoute(announcementId: String): ApiResult<RouteDetails>
    suspend fun fetchRouteContext(announcementId: String): ApiResult<RouteContext>
    suspend fun fetchMyCurrentRoute(): ApiResult<RouteDetails>
    suspend fun fetchMyCurrentRouteContext(): ApiResult<RouteContext>
    suspend fun buildRoute(request: RouteBuildRequest): ApiResult<RouteDetails>
}

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val apiErrorMapper: ApiErrorMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository {

    override suspend fun register(credentials: RegisterCredentials): ApiResult<AccessToken> = repositoryCall {
        authApi.register(credentials.toDto()).toDomain()
    }

    override suspend fun login(credentials: LoginCredentials): ApiResult<AccessToken> = repositoryCall {
        authApi.login(credentials.toDto()).toDomain()
    }

    override suspend fun fetchMe(): ApiResult<SessionUser> = repositoryCall {
        authApi.me().toDomain()
    }

    private suspend fun <T> repositoryCall(block: suspend () -> T): ApiResult<T> =
        withContext(ioDispatcher) {
            safeApiCall(apiErrorMapper, block)
        }
}

@Singleton
class DefaultProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val sessionManager: SessionManager,
    private val apiErrorMapper: ApiErrorMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProfileRepository {

    override suspend fun fetchMeProfile(): ApiResult<UserProfile> = repositoryCall {
        profileApi.getMyProfile().toDomain()
    }

    override suspend fun updateMyProfile(fields: EditableProfileFields): ApiResult<EditableProfileFields> =
        repositoryCall {
            profileApi.updateMyProfile(fields.toDto()).toEditableFields()
        }

    override suspend fun fetchMyReviews(
        limit: Int,
        offset: Int,
    ): ApiResult<List<UserProfileReview>> = repositoryCall {
        profileApi.getMyReviews(limit = limit, offset = offset, role = null)
            .items
            .map { it.toDomain() }
    }

    override suspend fun fetchMyReviewsFeed(
        limit: Int,
        offset: Int,
        role: ReviewRole,
    ): ApiResult<UserReviewFeed> = repositoryCall {
        profileApi.getMyReviews(limit = limit, offset = offset, role = role.rawValue)
            .toDomain(fallbackRole = role)
    }

    override suspend fun fetchReviewEligibility(announcementId: String): ApiResult<ReviewEligibility> =
        repositoryCall {
            profileApi.getAnnouncementReviewContext(announcementId).toDomain()
        }

    override suspend fun submitReview(
        announcementId: String,
        stars: Int,
        text: String,
    ): ApiResult<Unit> = repositoryCall {
        profileApi.submitAnnouncementReview(
            announcementId = announcementId,
            request = SubmitReviewRequestDto(
                stars = stars,
                text = text.trimmedOrNull(),
            ),
        )
        Unit
    }

    private suspend fun <T> repositoryCall(block: suspend () -> T): ApiResult<T> =
        sessionAwareApiCall(ioDispatcher, apiErrorMapper, sessionManager, block)
}

@Singleton
class DefaultDeviceRepository @Inject constructor(
    private val deviceApi: DeviceApi,
    private val sessionManager: SessionManager,
    private val apiErrorMapper: ApiErrorMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context,
) : DeviceRepository {

    override suspend fun registerCurrentDevice(): ApiResult<Unit> = repositoryCall {
        deviceApi.registerDevice(
            DeviceRegistrationRequestDto(
                deviceId = currentDeviceId(context),
                platform = "android",
                pushToken = null,
                locale = Locale.getDefault().toLanguageTag(),
                timezone = TimeZone.getDefault().id,
                deviceName = buildDeviceName(),
            ),
        )
        Unit
    }

    override suspend fun unregisterCurrentDevice(): ApiResult<Unit> = repositoryCall {
        deviceApi.deleteCurrentDevice(
            UnregisterDeviceRequestDto(
                deviceId = currentDeviceId(context),
                pushToken = null,
            ),
        )
        Unit
    }

    private suspend fun <T> repositoryCall(block: suspend () -> T): ApiResult<T> =
        sessionAwareApiCall(ioDispatcher, apiErrorMapper, sessionManager, block)
}

@Singleton
class DefaultAnnouncementRepository @Inject constructor(
    private val announcementApi: AnnouncementApi,
    private val appConfig: AppConfig,
    private val sessionManager: SessionManager,
    private val apiErrorMapper: ApiErrorMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AnnouncementRepository {

    override suspend fun createAnnouncement(draft: CreateAnnouncementDraft): ApiResult<Announcement> =
        repositoryCall {
            announcementApi.createAnnouncement(draft.toDto()).toDomain()
        }

    override suspend fun fetchMyAnnouncements(): ApiResult<List<Announcement>> = repositoryCall {
        announcementApi.getMyAnnouncements().map { it.toDomain() }
    }

    override suspend fun fetchPublicAnnouncements(): ApiResult<List<Announcement>> = repositoryCall {
        announcementApi.getPublicAnnouncements().map { it.toDomain() }
    }

    override suspend fun fetchAnnouncement(announcementId: String): ApiResult<Announcement> = repositoryCall {
        announcementApi.getAnnouncement(announcementId).toDomain()
    }

    override suspend fun uploadAnnouncementMedia(
        announcementId: String,
        files: List<UploadFilePayload>,
    ): ApiResult<Announcement> = repositoryCall {
        announcementApi.uploadAnnouncementMedia(
            announcementId = announcementId,
            files = files.map { it.toMultipartPart() },
        ).toDomain()
    }

    override suspend fun archiveAnnouncement(announcementId: String): ApiResult<Announcement> = repositoryCall {
        announcementApi.archiveAnnouncement(announcementId, EmptyBodyDto).toDomain()
    }

    override suspend fun deleteAnnouncement(announcementId: String): ApiResult<Boolean> = repositoryCall {
        announcementApi.deleteAnnouncement(announcementId).ok
    }

    override suspend fun appealAnnouncement(
        announcementId: String,
        reason: String?,
    ): ApiResult<Announcement> = repositoryCall {
        announcementApi.appealAnnouncement(
            announcementId = announcementId,
            request = AppealRequestDto(reason = reason.trimmedOrNull()),
        ).toDomain()
    }

    override suspend fun createOffer(
        announcementId: String,
        message: String?,
        proposedPrice: Int?,
        pricingMode: OfferPricingMode,
        agreedPrice: Int?,
        minimumPriceAccepted: Boolean,
    ): ApiResult<AnnouncementOffer> = repositoryCall {
        announcementApi.submitOffer(
            announcementId = announcementId,
            request = CreateOfferRequestDto(
                message = message.trimmedOrNull(),
                proposedPrice = proposedPrice,
                pricingMode = pricingMode.rawValue,
                agreedPrice = agreedPrice,
                minimumPriceAccepted = minimumPriceAccepted,
            ),
        ).toDomain(appConfig.normalizedApiBaseUrl)
    }

    override suspend fun fetchOffers(announcementId: String): ApiResult<List<AnnouncementOffer>> = repositoryCall {
        announcementApi.getAnnouncementOffers(announcementId)
            .map { it.toDomain(appConfig.normalizedApiBaseUrl) }
    }

    override suspend fun acceptOffer(
        announcementId: String,
        offerId: String,
    ): ApiResult<AcceptedOfferResult> = repositoryCall {
        announcementApi.acceptOffer(
            announcementId = announcementId,
            offerId = offerId,
            body = EmptyBodyDto,
        ).toDomain(appConfig.normalizedApiBaseUrl)
    }

    override suspend fun rejectOffer(announcementId: String, offerId: String): ApiResult<Unit> = repositoryCall {
        announcementApi.rejectOffer(
            announcementId = announcementId,
            offerId = offerId,
            body = EmptyBodyDto,
        )
        Unit
    }

    override suspend fun updateExecutionStage(
        announcementId: String,
        stage: String,
    ): ApiResult<Announcement> = repositoryCall {
        announcementApi.updateExecutionStage(
            announcementId = announcementId,
            request = ExecutionStageUpdateRequestDto(stage = stage),
        ).toDomain()
    }

    private suspend fun <T> repositoryCall(block: suspend () -> T): ApiResult<T> =
        sessionAwareApiCall(ioDispatcher, apiErrorMapper, sessionManager, block)
}

@Singleton
class DefaultChatRepository @Inject constructor(
    private val chatApi: ChatApi,
    private val appConfig: AppConfig,
    private val sessionManager: SessionManager,
    private val apiErrorMapper: ApiErrorMapper,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ChatRepository {

    override suspend fun fetchThreads(): ApiResult<List<com.vzaimno.app.core.model.ChatThreadPreview>> =
        repositoryCall {
            chatApi.getChats().map { it.toDomain(appConfig.normalizedApiBaseUrl) }
        }

    override suspend fun fetchMessages(
        threadId: String,
        limit: Int,
        before: Instant?,
    ): ApiResult<List<com.vzaimno.app.core.model.ChatMessage>> = repositoryCall {
        chatApi.getMessages(
            threadId = threadId,
            limit = limit,
            before = before?.let(DateTimeFormatter.ISO_INSTANT::format),
        ).map { it.toDomain(appConfig.normalizedApiBaseUrl) }
    }

    override suspend fun sendMessage(
        threadId: String,
        text: String,
    ): ApiResult<com.vzaimno.app.core.model.ChatMessage> = repositoryCall {
        chatApi.sendMessage(
            threadId = threadId,
            request = SendChatMessageRequestDto(text = text),
        ).toDomain(appConfig.normalizedApiBaseUrl)
    }

    override suspend fun sendImageMessage(
        threadId: String,
        text: String?,
        file: UploadFilePayload,
    ): ApiResult<com.vzaimno.app.core.model.ChatMessage> = repositoryCall {
        chatApi.sendImageMessage(
            threadId = threadId,
            file = file.toMultipartPart(fieldName = "file"),
            text = text
                ?.trimmedOrNull()
                ?.toRequestBody("text/plain".toMediaTypeOrNull()),
        ).toDomain(appConfig.normalizedApiBaseUrl)
    }

    override suspend fun ensureSupportThread(): ApiResult<String> = repositoryCall {
        chatApi.ensureSupportThread().threadId
    }

    override suspend fun fetchSupportMessages(
        threadId: String,
        limit: Int,
        before: Instant?,
    ): ApiResult<List<com.vzaimno.app.core.model.ChatMessage>> = repositoryCall {
        chatApi.getSupportMessages(
            threadId = threadId,
            limit = limit,
            before = before?.let(DateTimeFormatter.ISO_INSTANT::format),
        ).map { it.toDomain(appConfig.normalizedApiBaseUrl) }
    }

    override suspend fun sendSupportMessage(
        threadId: String,
        text: String,
    ): ApiResult<com.vzaimno.app.core.model.ChatMessage> = repositoryCall {
        chatApi.sendSupportMessage(
            threadId = threadId,
            request = SendChatMessageRequestDto(text = text),
        ).toDomain(appConfig.normalizedApiBaseUrl)
    }

    override suspend fun fetchReportReasonOptions(): ApiResult<List<ReportReasonOption>> = repositoryCall {
        chatApi.getReportReasonCodes().map { it.toDomain() }
    }

    override suspend fun submitReport(
        targetType: String,
        targetId: String,
        reasonCode: String,
        reasonText: String?,
    ): ApiResult<Unit> = repositoryCall {
        chatApi.submitReport(
            ReportSubmissionRequestDto(
                targetType = targetType,
                targetId = targetId,
                reasonCode = reasonCode,
                reasonText = reasonText.trimmedOrNull(),
            ),
        )
        Unit
    }

    override suspend fun fetchActiveDispute(threadId: String): ApiResult<DisputeState?> {
        val result = repositoryCall {
            val response = chatApi.getActiveDispute(threadId)
            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val body = response.body()?.string()?.trim().orEmpty()
            if (body.isBlank() || body == "null") {
                null
            } else {
                json.decodeFromString<DisputeStateDto>(body).toDomain()
            }
        }
        // Backward-compatible fallback: older backends respond 404 when no active dispute exists.
        return when {
            result is ApiResult.Failure && result.error.statusCode == 404 ->
                ApiResult.Success(null)

            else -> result
        }
    }

    override suspend fun openDispute(
        threadId: String,
        problemTitle: String,
        problemDescription: String,
        requestedCompensationRub: Int,
        desiredResolution: String,
    ): ApiResult<DisputeState> = repositoryCall {
        chatApi.openDispute(
            threadId = threadId,
            request = OpenDisputeRequestDto(
                problemTitle = problemTitle,
                problemDescription = problemDescription,
                requestedCompensationRub = requestedCompensationRub.coerceAtLeast(0),
                desiredResolution = desiredResolution,
            ),
        ).toDomain()
    }

    override suspend fun acceptCounterpartyDispute(
        threadId: String,
        disputeId: String,
    ): ApiResult<DisputeState> = repositoryCall {
        chatApi.acceptCounterpartyDispute(
            threadId = threadId,
            disputeId = disputeId,
            body = EmptyBodyDto,
        ).toDomain()
    }

    override suspend fun respondCounterpartyDispute(
        threadId: String,
        disputeId: String,
        responseDescription: String,
        acceptableRefundPercent: Int,
        desiredResolution: String,
    ): ApiResult<DisputeState> = repositoryCall {
        chatApi.respondCounterpartyDispute(
            threadId = threadId,
            disputeId = disputeId,
            request = CounterpartyDisputeResponseRequestDto(
                responseDescription = responseDescription,
                acceptableRefundPercent = acceptableRefundPercent.coerceIn(0, 100),
                desiredResolution = desiredResolution,
            ),
        ).toDomain()
    }

    override suspend fun selectDisputeOption(
        threadId: String,
        disputeId: String,
        optionId: String,
    ): ApiResult<DisputeState> = repositoryCall {
        chatApi.selectDisputeOption(
            threadId = threadId,
            disputeId = disputeId,
            request = SelectDisputeOptionRequestDto(optionId = optionId),
        ).toDomain()
    }

    private suspend fun <T> repositoryCall(block: suspend () -> T): ApiResult<T> =
        sessionAwareApiCall(ioDispatcher, apiErrorMapper, sessionManager, block)
}

@Singleton
class DefaultRouteRepository @Inject constructor(
    private val routeApi: RouteApi,
    private val appConfig: AppConfig,
    private val sessionManager: SessionManager,
    private val apiErrorMapper: ApiErrorMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RouteRepository {

    override suspend fun fetchAnnouncementRoute(announcementId: String): ApiResult<RouteDetails> = repositoryCall {
        routeApi.getAnnouncementRoute(announcementId).toDomain(appConfig.normalizedApiBaseUrl)
    }

    override suspend fun fetchRouteContext(announcementId: String): ApiResult<RouteContext> = repositoryCall {
        routeApi.getAnnouncementRouteContext(announcementId).toDomain()
    }

    override suspend fun fetchMyCurrentRoute(): ApiResult<RouteDetails> = repositoryCall {
        routeApi.getMyCurrentRoute().toDomain(appConfig.normalizedApiBaseUrl)
    }

    override suspend fun fetchMyCurrentRouteContext(): ApiResult<RouteContext> = repositoryCall {
        routeApi.getMyCurrentRouteContext().toDomain()
    }

    override suspend fun buildRoute(request: RouteBuildRequest): ApiResult<RouteDetails> = repositoryCall {
        routeApi.buildRoute(request.toDto()).toDomain(appConfig.normalizedApiBaseUrl)
    }

    private suspend fun <T> repositoryCall(block: suspend () -> T): ApiResult<T> =
        sessionAwareApiCall(ioDispatcher, apiErrorMapper, sessionManager, block)
}

private suspend fun <T> sessionAwareApiCall(
    ioDispatcher: CoroutineDispatcher,
    apiErrorMapper: ApiErrorMapper,
    sessionManager: SessionManager,
    block: suspend () -> T,
): ApiResult<T> = withContext(ioDispatcher) {
    val result = safeApiCall(apiErrorMapper, block)
    if (result is ApiResult.Failure && result.error.invalidatesSession) {
        sessionManager.clearSession()
    }
    result
}

private fun currentDeviceId(context: Context): String = Settings.Secure.getString(
    context.contentResolver,
    Settings.Secure.ANDROID_ID,
) ?: "android-device"

private fun buildDeviceName(): String {
    val manufacturer = Build.MANUFACTURER.trimmedOrNull()
    val model = Build.MODEL.trimmedOrNull()
    return listOfNotNull(manufacturer, model).joinToString(separator = " ").ifBlank { "Android Device" }
}
