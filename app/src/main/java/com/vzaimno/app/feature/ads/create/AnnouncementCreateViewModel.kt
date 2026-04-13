package com.vzaimno.app.feature.ads.create

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vzaimno.app.R
import com.vzaimno.app.core.network.ApiResult
import com.vzaimno.app.core.network.UploadFilePayload
import com.vzaimno.app.data.repository.AnnouncementRepository
import com.vzaimno.app.data.repository.SessionManager
import com.vzaimno.app.feature.ads.AdsAnnouncementCoordinator
import com.vzaimno.app.feature.ads.AdsDestination
import com.vzaimno.app.feature.ads.AdsFilterBucket
import com.vzaimno.app.feature.ads.adsBucket
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AnnouncementCreateEvent {
    data class Submitted(
        val focusFilter: AdsFilterBucket,
        val message: String?,
    ) : AnnouncementCreateEvent
}

@HiltViewModel
class AnnouncementCreateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val announcementRepository: AnnouncementRepository,
    private val sessionManager: SessionManager,
    private val adsAnnouncementCoordinator: AdsAnnouncementCoordinator,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val prefillAnnouncementId: String? =
        savedStateHandle.get<String?>(AdsDestination.prefillAnnouncementIdArgument)

    private val _uiState = MutableStateFlow(
        AnnouncementCreateUiState(
            prefillSourceAnnouncementId = prefillAnnouncementId,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AnnouncementCreateEvent>()
    val events: SharedFlow<AnnouncementCreateEvent> = _events.asSharedFlow()

    private var hasLoadedPrefill = false

    init {
        loadPrefillIfNeeded()
    }

    fun onMainGroupSelected(mainGroup: AnnouncementMainGroup) {
        updateDraft { draft ->
            draft.withMainGroup(mainGroup)
        }
    }

    fun onActionTypeSelected(actionType: com.vzaimno.app.core.model.AnnouncementStructuredData.ActionType) {
        updateDraft { draft ->
            draft.withActionType(actionType)
        }
    }

    fun onTitleChanged(value: String) {
        updateDraft { draft -> draft.copy(title = value) }
    }

    fun onItemTypeSelected(itemType: AnnouncementCreateItemType?) {
        updateDraft { draft -> draft.copy(itemType = itemType) }
    }

    fun onPurchaseTypeSelected(purchaseType: AnnouncementCreatePurchaseType?) {
        updateDraft { draft -> draft.copy(purchaseType = purchaseType) }
    }

    fun onHelpTypeSelected(helpType: AnnouncementCreateHelpType?) {
        updateDraft { draft -> draft.copy(helpType = helpType) }
    }

    fun onSourceKindSelected(kind: com.vzaimno.app.core.model.AnnouncementStructuredData.SourceKind?) {
        updateDraft { draft -> draft.copy(sourceKind = kind) }
    }

    fun onDestinationKindSelected(kind: com.vzaimno.app.core.model.AnnouncementStructuredData.DestinationKind?) {
        updateDraft { draft -> draft.copy(destinationKind = kind) }
    }

    fun onUrgencySelected(urgency: com.vzaimno.app.core.model.AnnouncementStructuredData.Urgency) {
        updateDraft { draft -> draft.copy(urgency = urgency) }
    }

    fun onSourceAddressChanged(value: String) {
        updateDraft { draft ->
            draft.copy(
                source = draft.source.copy(
                    address = value,
                    placeId = null,
                    point = null,
                ),
            )
        }
    }

    fun onDestinationAddressChanged(value: String) {
        updateDraft { draft ->
            draft.copy(
                destination = draft.destination.copy(
                    address = value,
                    placeId = null,
                    point = null,
                ),
            )
        }
    }

    fun onBudgetModeSelected(mode: AnnouncementBudgetMode) {
        updateDraft { draft ->
            val updatedBudget = when (mode) {
                AnnouncementBudgetMode.Fixed -> draft.budget.copy(
                    mode = mode,
                    amount = draft.budget.amount.ifBlank {
                        draft.budget.max.ifBlank { draft.budget.min }
                    },
                    min = "",
                    max = "",
                )

                AnnouncementBudgetMode.Range -> draft.budget.copy(
                    mode = mode,
                    amount = "",
                    min = draft.budget.min.ifBlank { draft.budget.amount },
                    max = draft.budget.max.ifBlank { draft.budget.amount },
                )
            }
            draft.copy(budget = updatedBudget)
        }
    }

    fun onBudgetAmountChanged(value: String) {
        updateDraft { draft ->
            draft.copy(budget = draft.budget.copy(amount = value))
        }
    }

    fun onBudgetMinChanged(value: String) {
        updateDraft { draft ->
            draft.copy(budget = draft.budget.copy(min = value))
        }
    }

    fun onBudgetMaxChanged(value: String) {
        updateDraft { draft ->
            draft.copy(budget = draft.budget.copy(max = value))
        }
    }

    fun onTaskBriefChanged(value: String) {
        updateDraft { draft -> draft.copy(taskBrief = value) }
    }

    fun onNotesChanged(value: String) {
        updateDraft { draft -> draft.copy(notes = value) }
    }

    fun onContactNameChanged(value: String) {
        updateDraft { draft ->
            draft.copy(contacts = draft.contacts.copy(name = value))
        }
    }

    fun onContactPhoneChanged(value: String) {
        updateDraft { draft ->
            draft.copy(contacts = draft.contacts.copy(phone = value))
        }
    }

    fun onContactMethodSelected(method: AnnouncementContactMethod) {
        updateDraft { draft ->
            draft.copy(contacts = draft.contacts.copy(method = method))
        }
    }

    fun onAudienceSelected(audience: AnnouncementAudience) {
        updateDraft { draft ->
            draft.copy(contacts = draft.contacts.copy(audience = audience))
        }
    }

    fun onEstimatedTaskMinutesChanged(value: String) {
        updateDraft { draft ->
            draft.copy(
                attributes = draft.attributes.copy(estimatedTaskMinutes = value),
            )
        }
    }

    fun onWaitingMinutesChanged(value: String) {
        updateDraft { draft ->
            draft.copy(
                attributes = draft.attributes.copy(waitingMinutes = value),
            )
        }
    }

    fun onFloorChanged(value: String) {
        updateDraft { draft ->
            draft.copy(
                attributes = draft.attributes.copy(floor = value),
            )
        }
    }

    fun onWeightCategorySelected(category: com.vzaimno.app.core.model.AnnouncementStructuredData.WeightCategory?) {
        updateDraft { draft ->
            draft.copy(
                attributes = draft.attributes.copy(weightCategory = category),
            )
        }
    }

    fun onSizeCategorySelected(category: com.vzaimno.app.core.model.AnnouncementStructuredData.SizeCategory?) {
        updateDraft { draft ->
            draft.copy(
                attributes = draft.attributes.copy(sizeCategory = category),
            )
        }
    }

    fun onAttributeToggleChanged(
        toggle: AnnouncementAttributeToggle,
        enabled: Boolean,
    ) {
        updateDraft { draft ->
            draft.copy(
                attributes = draft.attributes.withToggle(toggle, enabled),
            ).normalizedForCurrentAction()
        }
    }

    fun onMediaPicked(uris: List<Uri>) {
        if (uris.isEmpty()) return

        updateDraft { draft ->
            val newMedia = uris
                .map { uri ->
                    AnnouncementSelectedMedia(
                        id = uri.toString(),
                        uriString = uri.toString(),
                        fileName = queryDisplayName(uri),
                        mimeType = context.contentResolver.getType(uri),
                    )
                }
                .filterNot { selected ->
                    draft.media.any { it.id == selected.id }
                }

            draft.copy(
                media = (draft.media + newMedia).take(MAX_MEDIA_ITEMS),
            )
        }
    }

    fun removeMedia(mediaId: String) {
        updateDraft { draft ->
            draft.copy(
                media = draft.media.filterNot { it.id == mediaId },
            )
        }
    }

    fun toggleSummaryExpanded() {
        _uiState.update { state ->
            state.copy(isSummaryExpanded = !state.isSummaryExpanded)
        }
    }

    fun clearInlineMessage() {
        _uiState.update { state ->
            state.copy(inlineMessage = null)
        }
    }

    fun submit() {
        if (_uiState.value.isBusy) return

        val draft = _uiState.value.draft
        val validation = draft.validate(context)
        if (validation.hasErrors) {
            _uiState.update { state ->
                state.copy(
                    fieldErrors = validation,
                    inlineMessage = validation.firstMessage(),
                )
            }
            return
        }

        val submissionPlan = draft.submissionPlan()
        val localId = "local-${UUID.randomUUID()}"
        val optimisticAnnouncement = draft.toOptimisticAnnouncement(
            localId = localId,
            userId = sessionManager.activeSession.value.user?.id ?: "local",
            requestStatus = submissionPlan.requestStatus,
        )

        adsAnnouncementCoordinator.upsertOptimistic(optimisticAnnouncement)
        _uiState.update { state ->
            state.copy(
                isSubmitting = true,
                fieldErrors = AnnouncementCreateFieldErrors(),
                inlineMessage = null,
            )
        }

        viewModelScope.launch {
            when (val createResult = announcementRepository.createAnnouncement(draft.toRepositoryDraft(submissionPlan.requestStatus))) {
                is ApiResult.Success -> {
                    var createdAnnouncement = createResult.value
                    adsAnnouncementCoordinator.replaceOptimistic(localId, createdAnnouncement)

                    var postSubmitMessage = successMessageFor(createdAnnouncement.adsBucket())
                    if (draft.media.isNotEmpty()) {
                        val mediaPayloads = buildUploadPayloads(draft.media)
                        if (mediaPayloads.isEmpty()) {
                            postSubmitMessage = context.getString(R.string.ads_create_warning_media_not_uploaded)
                        } else {
                            when (
                                val uploadResult = announcementRepository.uploadAnnouncementMedia(
                                    announcementId = createdAnnouncement.id,
                                    files = mediaPayloads,
                                )
                            ) {
                                is ApiResult.Success -> {
                                    createdAnnouncement = uploadResult.value
                                    adsAnnouncementCoordinator.replaceOptimistic(
                                        localId = createResult.value.id,
                                        announcement = createdAnnouncement,
                                    )
                                    postSubmitMessage = successMessageFor(createdAnnouncement.adsBucket())
                                }

                                is ApiResult.Failure -> {
                                    postSubmitMessage = context.getString(
                                        R.string.ads_create_warning_media_with_reason,
                                        uploadResult.error.message,
                                    )
                                }
                            }
                        }
                    }

                    _uiState.update { state ->
                        state.copy(isSubmitting = false)
                    }
                    _events.emit(
                        AnnouncementCreateEvent.Submitted(
                            focusFilter = createdAnnouncement.adsBucket(),
                            message = postSubmitMessage,
                        ),
                    )
                }

                is ApiResult.Failure -> {
                    adsAnnouncementCoordinator.removeOptimistic(localId)
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            inlineMessage = createResult.error.message,
                        )
                    }
                }
            }
        }
    }

    private fun loadPrefillIfNeeded() {
        if (prefillAnnouncementId.isNullOrBlank() || hasLoadedPrefill) return
        hasLoadedPrefill = true

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isPrefillLoading = true,
                    inlineMessage = null,
                )
            }

            when (val result = announcementRepository.fetchAnnouncement(prefillAnnouncementId)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            draft = result.value.toCreateFormDraft(),
                            fieldErrors = AnnouncementCreateFieldErrors(),
                            isPrefillLoading = false,
                            inlineMessage = if (result.value.hasReusablePrefillMedia()) {
                                context.getString(R.string.ads_create_prefill_media_hint)
                            } else {
                                null
                            },
                            prefillSourceTitle = result.value.title,
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isPrefillLoading = false,
                            inlineMessage = result.error.message,
                        )
                    }
                }
            }
        }
    }

    private fun updateDraft(
        transform: (AnnouncementCreateFormDraft) -> AnnouncementCreateFormDraft,
    ) {
        _uiState.update { state ->
            state.copy(
                draft = transform(state.draft),
                fieldErrors = AnnouncementCreateFieldErrors(),
                inlineMessage = null,
            )
        }
    }

    private fun buildUploadPayloads(media: List<AnnouncementSelectedMedia>): List<UploadFilePayload> = buildList {
        media.forEachIndexed { index, selectedMedia ->
            val uri = Uri.parse(selectedMedia.uriString)
            val bytes = runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.readBytes()
                }
            }.getOrNull() ?: return@forEachIndexed

            add(
                UploadFilePayload(
                    bytes = bytes,
                    fileName = selectedMedia.fileName ?: "announcement-media-${index + 1}.jpg",
                    mimeType = selectedMedia.mimeType ?: "image/jpeg",
                ),
            )
        }
    }

    private fun queryDisplayName(uri: Uri): String? = runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }
    }.getOrNull()

    private fun successMessageFor(filter: AdsFilterBucket): String = when (filter) {
        AdsFilterBucket.Active -> context.getString(R.string.ads_create_success_active)
        AdsFilterBucket.Actions -> context.getString(R.string.ads_create_success_review)
        AdsFilterBucket.Archive -> context.getString(R.string.ads_create_success_active)
    }

    private companion object {
        const val MAX_MEDIA_ITEMS = 8
    }
}
