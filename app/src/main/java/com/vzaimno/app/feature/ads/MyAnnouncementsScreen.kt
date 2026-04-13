package com.vzaimno.app.feature.ads

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PhotoCameraBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.formattedBudgetText
import com.vzaimno.app.core.model.hasAttachedMedia
import com.vzaimno.app.core.model.offersCount
import com.vzaimno.app.core.model.previewImageUrl
import java.time.Instant

@Composable
fun MyAnnouncementsRoute(
    onOpenDetails: (String) -> Unit,
    onOpenCreate: () -> Unit,
    refreshSignal: Boolean,
    postCreateFilter: AdsFilterBucket?,
    postCreateMessage: String?,
    onPostCreateHandled: () -> Unit,
    onRefreshSignalHandled: () -> Unit,
    viewModel: MyAnnouncementsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    LaunchedEffect(refreshSignal) {
        if (refreshSignal) {
            viewModel.refresh()
            onRefreshSignalHandled()
        }
    }

    LaunchedEffect(postCreateFilter, postCreateMessage) {
        if (postCreateFilter != null || !postCreateMessage.isNullOrBlank()) {
            viewModel.applyPostCreateResult(
                filter = postCreateFilter,
                message = postCreateMessage,
            )
            onPostCreateHandled()
        }
    }

    MyAnnouncementsScreen(
        state = state,
        onOpenDetails = onOpenDetails,
        onOpenCreate = onOpenCreate,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onFilterSelected = viewModel::selectFilter,
        onArchive = viewModel::archive,
        onDelete = viewModel::delete,
        onDismissInlineMessage = viewModel::clearContentMessage,
    )
}

private data class PendingListAction(
    val announcementId: String,
    val announcementTitle: String,
    val type: AnnouncementMutationType,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyAnnouncementsScreen(
    state: MyAnnouncementsUiState,
    onOpenDetails: (String) -> Unit,
    onOpenCreate: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onFilterSelected: (AdsFilterBucket) -> Unit,
    onArchive: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismissInlineMessage: () -> Unit,
) {
    var pendingAction by remember { mutableStateOf<PendingListAction?>(null) }
    pendingAction?.let { action ->
        val titleRes = if (action.type == AnnouncementMutationType.Archive) {
            R.string.ads_archive_dialog_title
        } else {
            R.string.ads_delete_dialog_title
        }
        val messageRes = if (action.type == AnnouncementMutationType.Archive) {
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
                Text(
                    text = stringResource(messageRes, action.announcementTitle),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (action.type == AnnouncementMutationType.Archive) {
                            onArchive(action.announcementId)
                        } else {
                            onDelete(action.announcementId)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp),
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                        ),
                    ),
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.xLarge,
                    top = MaterialTheme.spacing.xxLarge,
                    end = MaterialTheme.spacing.xLarge,
                    bottom = MaterialTheme.spacing.xxxLarge,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.ads_screen_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                if (state.screenState != AdsScreenState.Error) {
                    item {
                        AdsSummaryCard(summary = state.summary)
                    }

                    item {
                        AdsFilterRow(
                            selectedFilter = state.selectedFilter,
                            onFilterSelected = onFilterSelected,
                        )
                    }
                }

                if (!state.contentMessage.isNullOrBlank()) {
                    item {
                        AnnouncementMessageCard(
                            title = stringResource(R.string.ads_inline_error_title),
                            message = state.contentMessage,
                            actionLabel = stringResource(R.string.ads_inline_error_dismiss),
                            onAction = onDismissInlineMessage,
                            tone = AnnouncementMessageTone.Warning,
                        )
                    }
                }

                when (state.screenState) {
                    AdsScreenState.Loading -> {
                        items(count = 3) { index ->
                            AnnouncementLoadingCard(index = index)
                        }
                    }

                    AdsScreenState.Error -> {
                        item {
                            AnnouncementMessageCard(
                                title = stringResource(R.string.ads_error_title),
                                message = state.loadErrorMessage.orEmpty(),
                                actionLabel = stringResource(R.string.root_retry),
                                onAction = onRetry,
                                tone = AnnouncementMessageTone.Error,
                            )
                        }
                    }

                    AdsScreenState.Empty -> {
                        item {
                            AnnouncementsEmptyStateCard(
                                title = stringResource(R.string.ads_empty_title),
                                message = stringResource(R.string.ads_empty_message),
                            )
                        }
                    }

                    AdsScreenState.Content -> {
                        if (state.filteredAnnouncements.isEmpty()) {
                            item {
                                AnnouncementsEmptyStateCard(
                                    title = stringResource(
                                        when (state.selectedFilter) {
                                            AdsFilterBucket.Active -> R.string.ads_filter_empty_active_title
                                            AdsFilterBucket.Actions -> R.string.ads_filter_empty_actions_title
                                            AdsFilterBucket.Archive -> R.string.ads_filter_empty_archive_title
                                        },
                                    ),
                                    message = stringResource(
                                        when (state.selectedFilter) {
                                            AdsFilterBucket.Active -> R.string.ads_filter_empty_active_message
                                            AdsFilterBucket.Actions -> R.string.ads_filter_empty_actions_message
                                            AdsFilterBucket.Archive -> R.string.ads_filter_empty_archive_message
                                        },
                                    ),
                                )
                            }
                        } else {
                            items(
                                items = state.filteredAnnouncements,
                                key = { announcement -> announcement.id },
                            ) { announcement ->
                                AnnouncementListCard(
                                    announcement = announcement,
                                    apiBaseUrl = state.apiBaseUrl,
                                    isMutating = state.mutationState?.announcementId == announcement.id,
                                    mutationType = state.mutationState?.type,
                                    onClick = { onOpenDetails(announcement.id) },
                                    onArchive = {
                                        pendingAction = PendingListAction(
                                            announcementId = announcement.id,
                                            announcementTitle = announcement.title,
                                            type = AnnouncementMutationType.Archive,
                                        )
                                    },
                                    onDelete = {
                                        pendingAction = PendingListAction(
                                            announcementId = announcement.id,
                                            announcementTitle = announcement.title,
                                            type = AnnouncementMutationType.Delete,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pinned bottom button
        if (state.screenState != AdsScreenState.Loading) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.97f),
                shadowElevation = 8.dp,
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.xLarge, vertical = 12.dp),
                    onClick = onOpenCreate,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(R.string.ads_create_entry_button),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdsSummaryCard(summary: AdsSummaryUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.ads_summary_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AdsStatTile(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.ads_filter_active),
                    value = summary.activeCount,
                    accent = true,
                )
                AdsStatTile(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.ads_filter_actions),
                    value = summary.actionsCount,
                )
                AdsStatTile(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.ads_filter_archive),
                    value = summary.archiveCount,
                )
            }
        }
    }
}

@Composable
private fun AdsStatTile(
    title: String,
    value: Int,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (accent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (accent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AdsFilterRow(
    selectedFilter: AdsFilterBucket,
    onFilterSelected: (AdsFilterBucket) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        AdsFilterBucket.entries.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = AdsFilterBucket.entries.size,
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.surface,
                    activeContentColor = MaterialTheme.colorScheme.onSurface,
                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(
                    text = stringResource(
                        when (filter) {
                            AdsFilterBucket.Active -> R.string.ads_filter_active
                            AdsFilterBucket.Actions -> R.string.ads_filter_actions
                            AdsFilterBucket.Archive -> R.string.ads_filter_archive
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun AnnouncementListCard(
    announcement: Announcement,
    apiBaseUrl: String,
    isMutating: Boolean,
    mutationType: AnnouncementMutationType?,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    val status = announcement.statusPresentation()
    val decisionSummary = announcementDecisionSummaryText(announcement)
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = !isMutating, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AnnouncementPreview(
                modifier = Modifier.size(96.dp),
                announcement = announcement,
                apiBaseUrl = apiBaseUrl,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = announcement.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = announcementCategoryLabel(announcement),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AnnouncementStatusBadge(presentation = status)

                        Box {
                            IconButton(
                                enabled = !isMutating,
                                onClick = { menuExpanded = true },
                            ) {
                                if (isMutating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.MoreVert,
                                        contentDescription = stringResource(R.string.ads_item_menu),
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                            ) {
                                if (announcement.canArchiveFromAds()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = stringResource(R.string.ads_archive_action))
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Archive,
                                                contentDescription = null,
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onArchive()
                                        },
                                    )
                                }

                                if (announcement.canDeleteFromAds()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = stringResource(R.string.ads_delete_action))
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteOutline,
                                                contentDescription = null,
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onDelete()
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    announcement.formattedBudgetText?.let { budgetText ->
                        AnnouncementInfoChip(
                            label = budgetText,
                            leadingIcon = Icons.Outlined.Inventory2,
                        )
                    }
                    if (!announcement.hasAttachedMedia) {
                        AnnouncementInfoChip(
                            label = stringResource(R.string.ads_no_photo_chip),
                            leadingIcon = Icons.Outlined.ImageNotSupported,
                        )
                    }
                }

                decisionSummary?.let { summary ->
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    announcement.createdAtOrEpochFallback()?.let { createdAt ->
                        Text(
                            text = createdAt.asRelativeTimeLabel(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (announcement.shouldShowOffersCount()) {
                        Text(
                            text = stringResource(R.string.ads_offers_count, announcement.offersCount),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (mutationType == AnnouncementMutationType.Delete && isMutating) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                    )
                    Text(
                        text = stringResource(R.string.ads_delete_in_progress),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnouncementPreview(
    announcement: Announcement,
    apiBaseUrl: String,
    modifier: Modifier = Modifier,
) {
    val imageUrl = announcement.previewImageUrl(apiBaseUrl)

    if (imageUrl != null) {
        AsyncImage(
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            model = imageUrl,
            contentDescription = stringResource(R.string.ads_image_preview_description),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCameraBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.ads_no_photo_chip),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AnnouncementStatusBadge(
    presentation: AnnouncementStatusPresentation,
) {
    val containerColor = presentation.tone.containerColor(
        accentColor = MaterialTheme.colorScheme.primary,
        infoColor = MaterialTheme.colorScheme.secondary,
        positiveColor = Color(0xFF5E9C76),
        warningColor = Color(0xFFD2A047),
        dangerColor = Color(0xFFC96861),
        neutralColor = MaterialTheme.colorScheme.outline,
    )

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            text = stringResource(presentation.labelRes),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
        )
    }
}

@Composable
private fun AnnouncementInfoChip(
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let { icon ->
                Icon(
                    imageVector = icon,
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

private enum class AnnouncementMessageTone {
    Default,
    Warning,
    Error,
}

@Composable
private fun AnnouncementMessageCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    tone: AnnouncementMessageTone = AnnouncementMessageTone.Default,
) {
    val containerColor = when (tone) {
        AnnouncementMessageTone.Default -> MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        AnnouncementMessageTone.Warning -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        AnnouncementMessageTone.Error -> Color(0xFFF7E5E3)
    }
    val iconTint = when (tone) {
        AnnouncementMessageTone.Default -> MaterialTheme.colorScheme.primary
        AnnouncementMessageTone.Warning -> MaterialTheme.colorScheme.secondary
        AnnouncementMessageTone.Error -> Color(0xFFBA5C54)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
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
                TextButton(
                    onClick = onAction,
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

@Composable
private fun AnnouncementsEmptyStateCard(
    title: String,
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.xLarge, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AnnouncementLoadingCard(index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (index % 2 == 0) 0.92f else 0.8f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)),
                )
            }
        }
    }
}

@Composable
private fun CreateAnnouncementEntryCard(
    onClick: () -> Unit,
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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ads_create_entry_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.ads_create_entry_caption),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AnnouncementInfoChip(label = stringResource(R.string.ads_entry_soon))
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClick,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddCircleOutline,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.ads_create_entry_button),
                )
            }
        }
    }
}

private fun Instant.asRelativeTimeLabel(): String = DateUtils.getRelativeTimeSpanString(
    toEpochMilli(),
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE,
).toString()
