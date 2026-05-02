package com.vzaimno.app.data.remote

import com.vzaimno.app.data.remote.dto.AcceptOfferResponseDto
import com.vzaimno.app.data.remote.dto.AnnouncementDto
import com.vzaimno.app.data.remote.dto.AnnouncementOfferDto
import com.vzaimno.app.data.remote.dto.AppealRequestDto
import com.vzaimno.app.data.remote.dto.ChatMessageDto
import com.vzaimno.app.data.remote.dto.ChatThreadPreviewDto
import com.vzaimno.app.data.remote.dto.CounterpartyDisputeResponseRequestDto
import com.vzaimno.app.data.remote.dto.DisputeStateDto
import com.vzaimno.app.data.remote.dto.OpenDisputeRequestDto
import com.vzaimno.app.data.remote.dto.SelectDisputeOptionRequestDto
import com.vzaimno.app.data.remote.dto.CreateAnnouncementRequestDto
import com.vzaimno.app.data.remote.dto.CreateOfferRequestDto
import com.vzaimno.app.data.remote.dto.DeleteOkResponseDto
import com.vzaimno.app.data.remote.dto.DeviceRegistrationRequestDto
import com.vzaimno.app.data.remote.dto.EmptyBodyDto
import com.vzaimno.app.data.remote.dto.ExecutionStageUpdateRequestDto
import com.vzaimno.app.data.remote.dto.LoginRequestDto
import com.vzaimno.app.data.remote.dto.MeProfileResponseDto
import com.vzaimno.app.data.remote.dto.MeResponseDto
import com.vzaimno.app.data.remote.dto.MediaModerationResponseDto
import com.vzaimno.app.data.remote.dto.MyReviewsResponseDto
import com.vzaimno.app.data.remote.dto.OperationStatusResponseDto
import com.vzaimno.app.data.remote.dto.RegisterRequestDto
import com.vzaimno.app.data.remote.dto.ReportDto
import com.vzaimno.app.data.remote.dto.ReportReasonOptionDto
import com.vzaimno.app.data.remote.dto.ReportSubmissionRequestDto
import com.vzaimno.app.data.remote.dto.ReviewEligibilityDto
import com.vzaimno.app.data.remote.dto.ReviewsFeedResponseDto
import com.vzaimno.app.data.remote.dto.RouteContextDto
import com.vzaimno.app.data.remote.dto.RouteBuildRequestDto
import com.vzaimno.app.data.remote.dto.RouteDetailsDto
import com.vzaimno.app.data.remote.dto.SendChatMessageRequestDto
import com.vzaimno.app.data.remote.dto.SubmitReviewRequestDto
import com.vzaimno.app.data.remote.dto.SupportThreadDto
import com.vzaimno.app.data.remote.dto.TokenResponseDto
import com.vzaimno.app.data.remote.dto.UnregisterDeviceRequestDto
import com.vzaimno.app.data.remote.dto.UpdateMyProfileRequestDto
import com.vzaimno.app.data.remote.dto.UserProfileSectionDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): TokenResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): TokenResponseDto

    @GET("me")
    suspend fun me(): MeResponseDto
}

interface ProfileApi {
    @GET("users/me")
    suspend fun getMyProfile(): MeProfileResponseDto

    @PATCH("users/me/profile")
    suspend fun updateMyProfile(@Body request: UpdateMyProfileRequestDto): UserProfileSectionDto

    @GET("users/me/reviews")
    suspend fun getMyReviews(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("role") role: String? = null,
    ): ReviewsFeedResponseDto

    @GET("announcements/{announcementId}/review-context")
    suspend fun getAnnouncementReviewContext(
        @Path("announcementId") announcementId: String,
    ): ReviewEligibilityDto

    @POST("announcements/{announcementId}/review")
    suspend fun submitAnnouncementReview(
        @Path("announcementId") announcementId: String,
        @Body request: SubmitReviewRequestDto,
    ): OperationStatusResponseDto
}

interface DeviceApi {
    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegistrationRequestDto): OperationStatusResponseDto

    @HTTP(method = "DELETE", path = "devices/me", hasBody = true)
    suspend fun deleteCurrentDevice(@Body request: UnregisterDeviceRequestDto): OperationStatusResponseDto
}

interface AnnouncementApi {
    @POST("announcements")
    suspend fun createAnnouncement(@Body request: CreateAnnouncementRequestDto): AnnouncementDto

    @GET("announcements/me")
    suspend fun getMyAnnouncements(): List<AnnouncementDto>

    @GET("announcements/public")
    suspend fun getPublicAnnouncements(): List<AnnouncementDto>

    @GET("announcements/{announcementId}")
    suspend fun getAnnouncement(@Path("announcementId") announcementId: String): AnnouncementDto

    @Multipart
    @POST("announcements/{announcementId}/media")
    suspend fun uploadAnnouncementMedia(
        @Path("announcementId") announcementId: String,
        @Part files: List<MultipartBody.Part>,
    ): AnnouncementDto

    @PATCH("announcements/{announcementId}/archive")
    suspend fun archiveAnnouncement(
        @Path("announcementId") announcementId: String,
        @Body body: EmptyBodyDto,
    ): AnnouncementDto

    @DELETE("announcements/{announcementId}")
    suspend fun deleteAnnouncement(@Path("announcementId") announcementId: String): DeleteOkResponseDto

    @POST("announcements/{announcementId}/appeal")
    suspend fun appealAnnouncement(
        @Path("announcementId") announcementId: String,
        @Body request: AppealRequestDto,
    ): AnnouncementDto

    @POST("announcements/{announcementId}/offers")
    suspend fun submitOffer(
        @Path("announcementId") announcementId: String,
        @Body request: CreateOfferRequestDto,
    ): AnnouncementOfferDto

    @GET("announcements/{announcementId}/offers")
    suspend fun getAnnouncementOffers(
        @Path("announcementId") announcementId: String,
    ): List<AnnouncementOfferDto>

    @POST("announcements/{announcementId}/offers/{offerId}/accept")
    suspend fun acceptOffer(
        @Path("announcementId") announcementId: String,
        @Path("offerId") offerId: String,
        @Body body: EmptyBodyDto,
    ): AcceptOfferResponseDto

    @POST("announcements/{announcementId}/offers/{offerId}/reject")
    suspend fun rejectOffer(
        @Path("announcementId") announcementId: String,
        @Path("offerId") offerId: String,
        @Body body: EmptyBodyDto,
    ): OperationStatusResponseDto

    @POST("announcements/{announcementId}/execution-stage")
    suspend fun updateExecutionStage(
        @Path("announcementId") announcementId: String,
        @Body request: ExecutionStageUpdateRequestDto,
    ): AnnouncementDto
}

interface ChatApi {
    @GET("chats")
    suspend fun getChats(): List<ChatThreadPreviewDto>

    @GET("chats/{threadId}/messages")
    suspend fun getMessages(
        @Path("threadId") threadId: String,
        @Query("limit") limit: Int,
        @Query("before") before: String? = null,
    ): List<ChatMessageDto>

    @POST("chats/{threadId}/messages")
    suspend fun sendMessage(
        @Path("threadId") threadId: String,
        @Body request: SendChatMessageRequestDto,
    ): ChatMessageDto

    @Multipart
    @POST("chats/{threadId}/messages/media")
    suspend fun sendImageMessage(
        @Path("threadId") threadId: String,
        @Part file: MultipartBody.Part,
        @Part("text") text: okhttp3.RequestBody?,
    ): ChatMessageDto

    @GET("support/thread")
    suspend fun ensureSupportThread(): SupportThreadDto

    @GET("support/thread/{threadId}/messages")
    suspend fun getSupportMessages(
        @Path("threadId") threadId: String,
        @Query("limit") limit: Int,
        @Query("before") before: String? = null,
    ): List<ChatMessageDto>

    @POST("support/thread/{threadId}/messages")
    suspend fun sendSupportMessage(
        @Path("threadId") threadId: String,
        @Body request: SendChatMessageRequestDto,
    ): ChatMessageDto

    @GET("chats/{threadId}/disputes/active")
    suspend fun getActiveDispute(
        @Path("threadId") threadId: String,
    ): Response<ResponseBody>

    @POST("chats/{threadId}/disputes/open")
    suspend fun openDispute(
        @Path("threadId") threadId: String,
        @Body request: OpenDisputeRequestDto,
    ): DisputeStateDto

    @POST("chats/{threadId}/disputes/{disputeId}/counterparty/accept")
    suspend fun acceptCounterpartyDispute(
        @Path("threadId") threadId: String,
        @Path("disputeId") disputeId: String,
        @Body body: EmptyBodyDto,
    ): DisputeStateDto

    @POST("chats/{threadId}/disputes/{disputeId}/counterparty/respond")
    suspend fun respondCounterpartyDispute(
        @Path("threadId") threadId: String,
        @Path("disputeId") disputeId: String,
        @Body request: CounterpartyDisputeResponseRequestDto,
    ): DisputeStateDto

    @POST("chats/{threadId}/disputes/{disputeId}/options/select")
    suspend fun selectDisputeOption(
        @Path("threadId") threadId: String,
        @Path("disputeId") disputeId: String,
        @Body request: SelectDisputeOptionRequestDto,
    ): DisputeStateDto

    @GET("reports/reason-codes")
    suspend fun getReportReasonCodes(): List<ReportReasonOptionDto>

    @POST("reports")
    suspend fun submitReport(@Body request: ReportSubmissionRequestDto): ReportDto
}

interface RouteApi {
    @GET("announcements/{announcementId}/route")
    suspend fun getAnnouncementRoute(
        @Path("announcementId") announcementId: String,
    ): RouteDetailsDto

    @GET("announcements/{announcementId}/route/context")
    suspend fun getAnnouncementRouteContext(
        @Path("announcementId") announcementId: String,
    ): RouteContextDto

    @GET("routes/me/current")
    suspend fun getMyCurrentRoute(): RouteDetailsDto

    @GET("routes/me/current/context")
    suspend fun getMyCurrentRouteContext(): RouteContextDto

    @POST("route/build")
    suspend fun buildRoute(@Body request: RouteBuildRequestDto): RouteDetailsDto
}
