package com.vzaimno.app.feature.ads

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vzaimno.app.R
import com.vzaimno.app.core.common.humanizeRawValue
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementOffer
import com.vzaimno.app.core.model.ModerationSeverity
import com.vzaimno.app.core.model.detailsDescriptionText
import com.vzaimno.app.core.model.formattedBudgetText
import com.vzaimno.app.core.model.hasAttachedMedia
import com.vzaimno.app.core.model.imageUrls
import com.vzaimno.app.core.model.maxModerationSeverity
import com.vzaimno.app.core.model.moderationSeverityForField
import com.vzaimno.app.core.model.offersCount
import com.vzaimno.app.core.model.primaryDestinationAddress
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.structuredData
import com.vzaimno.app.core.model.visibleModerationReasons
import java.time.Instant
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AnnouncementDetailsRoute(
    onBack: () -> Unit,
    onRequestParentRefresh: () -> Unit,
    onCreateAgain: (Announcement) -> Unit,
    onOpenChatThread: (String) -> Unit,
    viewModel: AnnouncementDetailsViewModel = hiltViewModel(),
    offersViewModel: AnnouncementOffersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val offersState by offersViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val offersSection = state.announcement?.ownerOffersSectionPresentation()

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    LaunchedEffect(state.announcement?.id, offersSection?.isVisible) {
        if (offersSection?.isVisible == true) {
            offersViewModel.loadIfNeeded()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AnnouncementDetailsEvent.RefreshParent -> onRequestParentRefresh()
                AnnouncementDetailsEvent.RefreshParentAndClose -> {
                    onRequestParentRefresh()
                    onBack()
                }
            }
        }
    }

    LaunchedEffect(offersViewModel) {
        offersViewModel.events.collectLatest { event ->
            when (event) {
                is AnnouncementOffersEvent.OfferAccepted -> {
                    viewModel.refresh()
                    onRequestParentRefresh()
                    onOpenChatThread(event.threadId)
                }

                AnnouncementOffersEvent.OfferRejected -> {
                    viewModel.refresh()
                    onRequestParentRefresh()
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.ads_offer_reject_success),
                    )
                }
            }
        }
    }

    AnnouncementDetailsScreen(
        state = state,
        offersState = offersState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onRefresh = {
            viewModel.refresh()
            if (offersSection?.isVisible == true) {
                offersViewModel.refresh()
            }
        },
        onRetryOffers = offersViewModel::retry,
        onArchive = viewModel::archive,
        onDelete = viewModel::delete,
        onAppeal = viewModel::appeal,
        onCreateAgain = onCreateAgain,
        onDismissInlineMessage = viewModel::clearContentMessage,
        onDismissOffersInlineMessage = offersViewModel::clearContentMessage,
        onAcceptOffer = offersViewModel::acceptOffer,
        onRejectOffer = offersViewModel::rejectOffer,
    )
}

private data class PendingDetailAction(
    val type: AnnouncementMutationType,
    val title: String,
)

private data class PendingOfferDecision(
    val offerId: String,
    val performerName: String,
    val action: OfferDecisionAction,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AnnouncementDetailsScreen(
    state: AnnouncementDetailsUiState,
    offersState: AnnouncementOffersUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onRetryOffers: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onAppeal: () -> Unit,
    onCreateAgain: (Announcement) -> Unit,
    onDismissInlineMessage: () -> Unit,
    onDismissOffersInlineMessage: () -> Unit,
    onAcceptOffer: (String) -> Unit,
    onRejectOffer: (String) -> Unit,
) {
    var pendingAction by remember { mutableStateOf<PendingDetailAction?>(null) }
    var pendingOfferDecision by remember { mutableStateOf<PendingOfferDecision?>(null) }
    var soonDialogMessageRes by remember { mutableStateOf<Int?>(null) }
    val offerPerformerFallback = stringResource(R.string.ads_offer_performer_fallback)

    pendingAction?.let { action ->
        val titleRes = if (action.type == AnnouncementMutationType.Archive) {
            R.string.ads_archive_dialog_title
        } else {
            R.string.ads_delete_dialog_title
        }
        val bodyRes = if (action.type == AnnouncementMutationType.Archive) {
            R.string.ads_archive_dialog_message
        } else {
            R.string.ads_delete_dialog_message
        }
        val confirmRes = if (action.type == AnnouncementMutationType.Archive) {
            R.string.ads_archive_confirm
        } else {
            R.string.ads_delete_confirm
        }

        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = {
                Text(text = stringResource(titleRes))
            },
            text = {
                Text(text = stringResource(bodyRes, action.title))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (action.type == AnnouncementMutationType.Archive) {
                            onArchive()
                        } else {
                            onDelete()
                        }
                        pendingAction = null
                    },
                ) {
                    Text(text = stringResource(confirmRes))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text(text = stringResource(R.string.ads_action_cancel))
                }
            },
        )
    }

    pendingOfferDecision?.let { decision ->
        val titleRes = if (decision.action == OfferDecisionAction.Accept) {
            R.string.ads_offer_accept_dialog_title
        } else {
            R.string.ads_offer_reject_dialog_title
        }
        val bodyRes = if (decision.action == OfferDecisionAction.Accept) {
            R.string.ads_offer_accept_dialog_message
        } else {
            R.string.ads_offer_reject_dialog_message
        }
        val confirmRes = if (decision.action == OfferDecisionAction.Accept) {
            R.string.ads_offer_accept_action
        } else {
            R.string.ads_offer_reject_action
        }

        AlertDialog(
            onDismissRequest = { pendingOfferDecision = null },
            title = {
                Text(text = stringResource(titleRes))
            },
            text = {
                Text(text = stringResource(bodyRes, decision.performerName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (decision.action == OfferDecisionAction.Accept) {
                            onAcceptOffer(decision.offerId)
                        } else {
                            onRejectOffer(decision.offerId)
                        }
                        pendingOfferDecision = null
                    },
                ) {
                    Text(text = stringResource(confirmRes))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingOfferDecision = null }) {
                    Text(text = stringResource(R.string.ads_action_cancel))
                }
            },
        )
    }

    soonDialogMessageRes?.let { messageRes ->
        AlertDialog(
            onDismissRequest = { soonDialogMessageRes = null },
            title = {
                Text(text = stringResource(R.string.ads_entry_soon))
            },
            text = {
                Text(text = stringResource(messageRes))
            },
            confirmButton = {
                TextButton(onClick = { soonDialogMessageRes = null }) {
                    Text(text = stringResource(R.string.ads_action_ok))
                }
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.ads_details_title))
                },
                navigationIcon = {
                    IconButton(
                        enabled = state.mutationState == null,
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.ads_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        when {
            state.isInitialLoading && state.announcement == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.loadErrorMessage != null && state.announcement == null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                        ),
                    contentPadding = PaddingValues(
                        start = MaterialTheme.spacing.xLarge,
                        top = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.xLarge,
                        bottom = MaterialTheme.spacing.xxxLarge,
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    item {
                        DetailsMessageCard(
                            title = stringResource(R.string.ads_details_error_title),
                            message = state.loadErrorMessage,
                            actionLabel = stringResource(R.string.root_retry),
                            onAction = onRetry,
                            tone = DetailsMessageTone.Error,
                        )
                    }
                }
            }

            else -> {
                PullToRefreshBox(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                ) {
                    val announcement = state.announcement

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                                ),
                            ),
                        contentPadding = PaddingValues(
                            start = MaterialTheme.spacing.xLarge,
                            top = MaterialTheme.spacing.medium,
                            end = MaterialTheme.spacing.xLarge,
                            bottom = MaterialTheme.spacing.xxxLarge,
                        ),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                    ) {
                        if (!state.contentMessage.isNullOrBlank()) {
                            item {
                                DetailsMessageCard(
                                    title = stringResource(R.string.ads_inline_error_title),
                                    message = state.contentMessage,
                                    actionLabel = stringResource(R.string.ads_inline_error_dismiss),
                                    onAction = onDismissInlineMessage,
                                    tone = DetailsMessageTone.Warning,
                                )
                            }
                        }

                        if (announcement != null) {
                            val images = announcement.imageUrls(state.apiBaseUrl)
                            val offersSection = announcement.ownerOffersSectionPresentation()

                            if (images.isNotEmpty()) {
                                item {
                                    AnnouncementMediaGallery(
                                        imageUrls = images,
                                    )
                                }
                            }

                            item {
                                AnnouncementHeaderCard(
                                    announcement = announcement,
                                )
                            }

                            item {
                                AnnouncementStatusSection(
                                    announcement = announcement,
                                )
                            }

                            if (offersSection != null) {
                                item {
                                    AnnouncementOffersSection(
                                        announcement = announcement,
                                        section = offersSection,
                                        offersState = offersState,
                                        onRetry = onRetryOffers,
                                        onDismissInlineMessage = onDismissOffersInlineMessage,
                                        onAccept = { offer ->
                                            pendingOfferDecision = PendingOfferDecision(
                                                offerId = offer.id,
                                                performerName = offer.performerName(
                                                    fallback = offerPerformerFallback,
                                                ),
                                                action = OfferDecisionAction.Accept,
                                            )
                                        },
                                        onReject = { offer ->
                                            pendingOfferDecision = PendingOfferDecision(
                                                offerId = offer.id,
                                                performerName = offer.performerName(
                                                    fallback = offerPerformerFallback,
                                                ),
                                                action = OfferDecisionAction.Reject,
                                            )
                                        },
                                    )
                                }
                            }

                            item {
                                AnnouncementStructuredDataSection(
                                    announcement = announcement,
                                )
                            }

                            if (!announcement.primarySourceAddress.isNullOrBlank() || !announcement.primaryDestinationAddress.isNullOrBlank()) {
                                item {
                                    AnnouncementAddressesSection(
                                        announcement = announcement,
                                    )
                                }
                            }

                            item {
                                AnnouncementAttributesSection(
                                    announcement = announcement,
                                )
                            }

                            item {
                                AnnouncementMediaSection(
                                    announcement = announcement,
                                    apiBaseUrl = state.apiBaseUrl,
                                )
                            }

                            item {
                                AnnouncementActionsSection(
                                    announcement = announcement,
                                    mutationType = state.mutationState?.type,
                                    onArchive = {
                                        pendingAction = PendingDetailAction(
                                            type = AnnouncementMutationType.Archive,
                                            title = announcement.title,
                                        )
                                    },
                                    onDelete = {
                                        pendingAction = PendingDetailAction(
                                            type = AnnouncementMutationType.Delete,
                                            title = announcement.title,
                                        )
                                    },
                                    onAppeal = onAppeal,
                                    onReopen = {
                                        soonDialogMessageRes = R.string.ads_reopen_entry_message
                                    },
                                    onCreateAgain = {
                                        onCreateAgain(announcement)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementMediaGallery(
    imageUrls: List<String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large),
                text = stringResource(R.string.ads_gallery_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                items(
                    items = imageUrls,
                    key = { url -> url },
                ) { imageUrl ->
                    AsyncImage(
                        modifier = Modifier
                            .size(width = 220.dp, height = 168.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                        model = imageUrl,
                        contentDescription = stringResource(R.string.ads_image_preview_description),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnouncementHeaderCard(
    announcement: Announcement,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Text(
                text = announcementCategoryLabel(announcement),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                announcement.formattedBudgetText?.let { budget ->
                    DetailsInfoChip(
                        label = budget,
                        icon = Icons.Outlined.Inventory2,
                    )
                }

                announcement.createdAtOrEpochFallback()?.let { createdAt ->
                    DetailsInfoChip(
                        label = createdAt.asDetailTimeLabel(),
                        icon = Icons.Outlined.Schedule,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnouncementStatusSection(
    announcement: Announcement,
) {
    val status = announcement.statusPresentation()
    val decisionSummary = announcementDecisionSummaryText(announcement)

    DetailsSectionCard(
        title = stringResource(R.string.ads_status_section_title),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DetailsStatusBadge(presentation = status)

                if (announcement.shouldShowOffersCount()) {
                    Text(
                        text = stringResource(R.string.ads_offers_count, announcement.offersCount),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                text = stringResource(status.descriptionRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            decisionSummary?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (announcement.visibleModerationReasons.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    announcement.visibleModerationReasons.forEach { reason ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = moderationSeverityColor(reason.severity),
                                modifier = Modifier.padding(top = 2.dp),
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = announcementFieldLabel(reason.field),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                if (reason.details.isNotBlank()) {
                                    Text(
                                        text = reason.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementStructuredDataSection(
    announcement: Announcement,
) {
    val structured = announcement.structuredData
    val rows = buildList<Pair<String, String>> {
        add(stringResource(R.string.ads_field_category) to announcementCategoryLabel(announcement))
        structured.urgency?.title?.let { add(stringResource(R.string.ads_field_urgency) to it) }
        structured.helpType?.let { add(stringResource(R.string.ads_field_help_type) to humanizeRawValue(it)) }
        structured.purchaseType?.let { add(stringResource(R.string.ads_field_purchase_type) to humanizeRawValue(it)) }
        structured.itemType?.let { add(stringResource(R.string.ads_field_item_type) to humanizeRawValue(it)) }
        structured.sourceKind?.title?.let { add(stringResource(R.string.ads_field_source_kind) to it) }
        structured.destinationKind?.title?.let { add(stringResource(R.string.ads_field_destination_kind) to it) }
        structured.weightCategory?.title?.let { add(stringResource(R.string.ads_field_weight) to it) }
        structured.sizeCategory?.title?.let { add(stringResource(R.string.ads_field_size) to it) }
        structured.estimatedTaskMinutes?.let {
            add(stringResource(R.string.ads_field_duration) to stringResource(R.string.ads_minutes_format, it))
        }
        structured.waitingMinutes?.let {
            add(stringResource(R.string.ads_field_waiting) to stringResource(R.string.ads_minutes_format, it))
        }
        announcement.formattedBudgetText?.let { add(stringResource(R.string.ads_field_budget) to it) }
        structured.taskBrief?.takeIf(String::isNotBlank)?.let {
            add(stringResource(R.string.ads_field_task_brief) to it)
        }
        announcement.detailsDescriptionText?.takeIf(String::isNotBlank)?.let {
            add(stringResource(R.string.ads_field_description) to it)
        }
    }

    if (rows.isEmpty()) return

    DetailsSectionCard(
        title = stringResource(R.string.ads_data_section_title),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    ) {}
                }
                DetailsFieldRow(
                    label = label,
                    value = value,
                    severity = when (label) {
                        stringResource(R.string.ads_field_title) -> announcement.moderationSeverityForField("title")
                        stringResource(R.string.ads_field_description) -> announcement.moderationSeverityForField("notes")
                        else -> ModerationSeverity.None
                    },
                )
            }
        }
    }
}

@Composable
private fun AnnouncementAddressesSection(
    announcement: Announcement,
) {
    DetailsSectionCard(
        title = stringResource(R.string.ads_addresses_section_title),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            announcement.primarySourceAddress?.takeIf(String::isNotBlank)?.let { source ->
                DetailsFieldRow(
                    label = stringResource(R.string.ads_field_source_address),
                    value = source,
                    severity = announcement.moderationSeverityForField("pickup_address"),
                )
            }
            announcement.primaryDestinationAddress?.takeIf(String::isNotBlank)?.let { destination ->
                DetailsFieldRow(
                    label = stringResource(R.string.ads_field_destination_address),
                    value = destination,
                    severity = announcement.moderationSeverityForField("dropoff_address"),
                )
            }
        }
    }
}

@Composable
private fun AnnouncementAttributesSection(
    announcement: Announcement,
) {
    val structured = announcement.structuredData
    val chips = buildList {
        if (structured.requiresVehicle) add(stringResource(R.string.ads_attribute_vehicle))
        if (structured.needsTrunk) add(stringResource(R.string.ads_attribute_trunk))
        if (structured.requiresCarefulHandling) add(stringResource(R.string.ads_attribute_careful))
        if (structured.needsLoader) add(stringResource(R.string.ads_attribute_loader))
        if (structured.requiresLiftToFloor) add(stringResource(R.string.ads_attribute_lift))
        if (structured.hasElevator) add(stringResource(R.string.ads_attribute_elevator))
        if (structured.waitOnSite) add(stringResource(R.string.ads_attribute_waiting))
        if (structured.contactless) add(stringResource(R.string.ads_attribute_contactless))
        if (structured.requiresReceipt) add(stringResource(R.string.ads_attribute_receipt))
        if (structured.requiresConfirmationCode) add(stringResource(R.string.ads_attribute_code))
        if (structured.callBeforeArrival) add(stringResource(R.string.ads_attribute_call_before))
        if (structured.photoReportRequired) add(stringResource(R.string.ads_attribute_photo_report))
    }

    if (chips.isEmpty()) return

    DetailsSectionCard(
        title = stringResource(R.string.ads_attributes_section_title),
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            items(
                items = chips,
                key = { chip -> chip },
            ) { chip ->
                DetailsInfoChip(
                    label = chip,
                    icon = Icons.Outlined.LocalShipping,
                )
            }
        }
    }
}

@Composable
private fun AnnouncementMediaSection(
    announcement: Announcement,
    apiBaseUrl: String,
) {
    DetailsSectionCard(
        title = stringResource(R.string.ads_media_section_title),
    ) {
        val imageUrls = announcement.imageUrls(apiBaseUrl)
        if (imageUrls.isEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ads_media_missing_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.ads_media_missing_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                Text(
                    text = stringResource(R.string.ads_media_attached_count, imageUrls.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    items(
                        items = imageUrls,
                        key = { url -> url },
                    ) { imageUrl ->
                        AsyncImage(
                            modifier = Modifier
                                .size(width = 160.dp, height = 120.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            model = imageUrl,
                            contentDescription = stringResource(R.string.ads_image_preview_description),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnnouncementOffersSection(
    announcement: Announcement,
    section: OwnerOffersSectionPresentation,
    offersState: AnnouncementOffersUiState,
    onRetry: () -> Unit,
    onDismissInlineMessage: () -> Unit,
    onAccept: (AnnouncementOffer) -> Unit,
    onReject: (AnnouncementOffer) -> Unit,
) {
    DetailsSectionCard(
        title = stringResource(R.string.ads_offers_section_title),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.ads_offers_owner_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val displayedCount = offersState.offers.size.takeIf { it > 0 } ?: announcement.offersCount
                if (displayedCount > 0) {
                    Text(
                        text = stringResource(R.string.ads_offers_count, displayedCount),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        if (!offersState.contentMessage.isNullOrBlank()) {
            DetailsMessageCard(
                title = stringResource(R.string.ads_inline_error_title),
                message = offersState.contentMessage,
                actionLabel = stringResource(R.string.ads_inline_error_dismiss),
                onAction = onDismissInlineMessage,
                tone = DetailsMessageTone.Warning,
            )
        }

        when {
            offersState.isInitialLoading && offersState.offers.isEmpty() -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = stringResource(R.string.ads_offers_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            offersState.loadErrorMessage != null && offersState.offers.isEmpty() -> {
                DetailsMessageCard(
                    title = stringResource(R.string.ads_offers_error_title),
                    message = offersState.loadErrorMessage,
                    actionLabel = stringResource(R.string.root_retry),
                    onAction = onRetry,
                    tone = DetailsMessageTone.Error,
                )
            }

            offersState.offers.isEmpty() -> {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                ) {
                    Column(
                        modifier = Modifier.padding(MaterialTheme.spacing.large),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    ) {
                        Text(
                            text = stringResource(
                                if (section.acceptsNewOffers) {
                                    R.string.ads_offers_empty_title
                                } else {
                                    R.string.ads_offers_closed_empty_title
                                },
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(
                                if (section.acceptsNewOffers) {
                                    R.string.ads_offers_empty_message
                                } else {
                                    R.string.ads_offers_closed_empty_message
                                },
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            else -> {
                if (offersState.isRefreshing) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = stringResource(R.string.ads_offers_refreshing),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    offersState.offers.forEach { offer ->
                        OfferResponseCard(
                            announcement = announcement,
                            offer = offer,
                            processingAction = offersState.processingActionFor(offer.id),
                            onAccept = { onAccept(offer) },
                            onReject = { onReject(offer) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfferResponseCard(
    announcement: Announcement,
    offer: AnnouncementOffer,
    processingAction: OfferDecisionAction?,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    val summary = offer.summaryPresentation()
    val performerFallback = stringResource(R.string.ads_offer_performer_fallback)
    val priceText = offer.formattedPriceText()
    val status = offer.statusBadgePresentation()
    val ratingText = offer.ratingText()
    val completedCountText = offer.completedCountText()
    val secondaryHintRes = offer.secondaryHintText()

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f)),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.Top,
            ) {
                OfferAvatar(
                    avatarUrl = offer.performer?.avatarUrl,
                    initials = offer.performerInitials(
                        fallback = stringResource(R.string.ads_offer_avatar_fallback),
                    ),
                    performerName = offer.performerName(fallback = performerFallback),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = offer.performerName(fallback = performerFallback),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    offer.performerContextText()?.let { context ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = context,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    Text(
                        text = priceText ?: stringResource(R.string.ads_offer_price_missing),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (priceText == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                    ToneBadge(
                        label = stringResource(status.labelRes),
                        tone = status.tone,
                    )
                }
            }

            Text(
                text = summary.message ?: summary.fallbackRes?.let { stringResource(it) }.orEmpty(),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            secondaryHintRes?.let { hintRes ->
                Text(
                    text = stringResource(hintRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                DetailsInfoChip(
                    label = stringResource(offer.pricingModeLabelRes()),
                    icon = Icons.Outlined.Inventory2,
                )
                DetailsInfoChip(
                    label = offer.createdAtLabel(),
                    icon = Icons.Outlined.Schedule,
                )
                ratingText?.let { rating ->
                    DetailsInfoChip(
                        label = stringResource(R.string.ads_offer_rating_chip, rating),
                        icon = Icons.Rounded.Star,
                    )
                }
                completedCountText?.let { completed ->
                    DetailsInfoChip(
                        label = stringResource(R.string.ads_offer_completed_chip, completed),
                        icon = Icons.Outlined.TaskAlt,
                    )
                }
            }

            if (offer.canAcceptFromOwner(announcement) || offer.canRejectFromOwner(announcement)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = processingAction == null,
                        onClick = onReject,
                    ) {
                        if (processingAction == OfferDecisionAction.Reject) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(text = stringResource(R.string.ads_offer_reject_action))
                        }
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = processingAction == null,
                        onClick = onAccept,
                    ) {
                        if (processingAction == OfferDecisionAction.Accept) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            Text(text = stringResource(R.string.ads_offer_accept_action))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferAvatar(
    avatarUrl: String?,
    initials: String,
    performerName: String,
) {
    if (avatarUrl != null) {
        AsyncImage(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
            model = avatarUrl,
            contentDescription = stringResource(R.string.ads_offer_avatar_description, performerName),
            contentScale = ContentScale.Crop,
        )
    } else {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.28f),
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun AnnouncementActionsSection(
    announcement: Announcement,
    mutationType: AnnouncementMutationType?,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onAppeal: () -> Unit,
    onReopen: () -> Unit,
    onCreateAgain: () -> Unit,
) {
    DetailsSectionCard(
        title = stringResource(R.string.ads_actions_section_title),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            if (announcement.shouldShowAppealAction()) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = mutationType == null,
                    onClick = onAppeal,
                ) {
                    if (mutationType == AnnouncementMutationType.Appeal) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.SupportAgent,
                            contentDescription = null,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(R.string.ads_appeal_action),
                    )
                }
            }

            if (announcement.canArchiveFromAds()) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = mutationType == null,
                    onClick = onArchive,
                ) {
                    if (mutationType == AnnouncementMutationType.Archive) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Archive,
                            contentDescription = null,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(R.string.ads_archive_action),
                    )
                }
            }

            if (announcement.shouldShowReopenEntry()) {
                DetailsEntryPointCard(
                    title = stringResource(R.string.ads_reopen_entry_title),
                    body = stringResource(R.string.ads_reopen_entry_caption),
                    buttonLabel = stringResource(R.string.ads_reopen_entry_button),
                    onClick = onReopen,
                )
            }

            if (announcement.shouldShowCreateAgainEntry()) {
                DetailsEntryPointCard(
                    title = stringResource(R.string.ads_create_again_entry_title),
                    body = stringResource(R.string.ads_create_again_entry_caption),
                    buttonLabel = stringResource(R.string.ads_create_again_entry_button),
                    onClick = onCreateAgain,
                )
            }

            if (announcement.canDeleteFromAds()) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = mutationType == null,
                    onClick = onDelete,
                ) {
                    if (mutationType == AnnouncementMutationType.Delete) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(R.string.ads_delete_action),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsEntryPointCard(
    title: String,
    body: String,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                DetailsInfoChip(label = stringResource(R.string.ads_entry_soon))
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClick,
            ) {
                Text(text = buttonLabel)
            }
        }
    }
}

@Composable
private fun DetailsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.84f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            content = content,
        )
    }
}

@Composable
private fun DetailsFieldRow(
    label: String,
    value: String,
    severity: ModerationSeverity,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.Top,
    ) {
        if (severity != ModerationSeverity.None) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = moderationSeverityColor(severity),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DetailsStatusBadge(
    presentation: AnnouncementStatusPresentation,
) {
    ToneBadge(
        label = stringResource(presentation.labelRes),
        tone = presentation.tone,
    )
}

@Composable
private fun ToneBadge(
    label: String,
    tone: AnnouncementStatusTone,
) {
    val containerColor = tone.containerColor(
        accentColor = MaterialTheme.colorScheme.primary,
        infoColor = MaterialTheme.colorScheme.secondary,
        positiveColor = Color(0xFF5E9C76),
        warningColor = Color(0xFFD2A047),
        dangerColor = Color(0xFFC96861),
        neutralColor = MaterialTheme.colorScheme.outline,
    )

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, containerColor.copy(alpha = 0.72f)),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = containerColor,
        )
    }
}

@Composable
private fun DetailsInfoChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class DetailsMessageTone {
    Warning,
    Error,
}

@Composable
private fun DetailsMessageCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    tone: DetailsMessageTone,
) {
    val containerColor = when (tone) {
        DetailsMessageTone.Warning -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.58f)
        DetailsMessageTone.Error -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.82f)
    }
    val iconTint = when (tone) {
        DetailsMessageTone.Warning -> MaterialTheme.colorScheme.secondary
        DetailsMessageTone.Error -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = iconTint,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

@Composable
private fun moderationSeverityColor(severity: ModerationSeverity): Color = when (severity) {
    ModerationSeverity.None -> MaterialTheme.colorScheme.outline
    ModerationSeverity.Warning -> Color(0xFFD2A047)
    ModerationSeverity.Danger -> Color(0xFFC96861)
}

private fun Instant.asDetailTimeLabel(): String = DateUtils.getRelativeTimeSpanString(
    toEpochMilli(),
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE,
).toString()
