package com.vzaimno.app.feature.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.PhotoCameraBack
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.detailsDescriptionText
import com.vzaimno.app.core.model.formattedBudgetText
import com.vzaimno.app.core.model.hasAttachedMedia
import com.vzaimno.app.core.model.imageUrls
import com.vzaimno.app.core.model.primaryDestinationAddress
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.quickOfferPrice
import com.vzaimno.app.core.model.taskStringValue
import kotlinx.coroutines.delay

@Composable
fun AnnouncementDiscoveryRoute(
    viewModel: AnnouncementDiscoveryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    AnnouncementDiscoveryScreen(
        state = state,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onDismissContentMessage = viewModel::clearContentMessage,
        onToggleContentMode = viewModel::toggleContentMode,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onToggleQuickCategory = viewModel::toggleQuickCategory,
        onToggleQuickAction = viewModel::toggleQuickAction,
        onOpenFilters = viewModel::openFilters,
        onDismissFilters = viewModel::dismissFilters,
        onResetFilterDraft = viewModel::resetFilterDraft,
        onApplyFilterDraft = viewModel::applyFilterDraft,
        onUpdateFilterDraftBudgetMin = viewModel::updateFilterDraftBudgetMin,
        onUpdateFilterDraftBudgetMax = viewModel::updateFilterDraftBudgetMax,
        onToggleFilterDraftCategory = viewModel::toggleFilterDraftCategory,
        onToggleFilterDraftUrgency = viewModel::toggleFilterDraftUrgency,
        onSetFilterDraftWithPhotoOnly = viewModel::setFilterDraftWithPhotoOnly,
        onSetFilterDraftRequiresVehicleOnly = viewModel::setFilterDraftRequiresVehicleOnly,
        onSetFilterDraftNeedsLoaderOnly = viewModel::setFilterDraftNeedsLoaderOnly,
        onSetFilterDraftContactlessOnly = viewModel::setFilterDraftContactlessOnly,
        onClearAllFilters = viewModel::clearAllFilters,
        onOpenAnnouncementDetails = viewModel::openAnnouncementDetails,
        onShowAnnouncementOnMap = viewModel::showAnnouncementOnMap,
        onDismissDetails = viewModel::dismissDetails,
        onOfferMessageChange = viewModel::updateOfferMessage,
        onOpenCustomPriceDialog = viewModel::openCustomPriceDialog,
        onDismissCustomPriceDialog = viewModel::dismissCustomPriceDialog,
        onCustomPriceChange = viewModel::updateCustomPriceInput,
        onSubmitQuickOffer = viewModel::submitQuickOffer,
        onSubmitCustomOffer = viewModel::submitCustomOffer,
        onMapFocusConsumed = viewModel::consumeMapFocus,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementDiscoveryScreen(
    state: DiscoveryUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onDismissContentMessage: () -> Unit,
    onToggleContentMode: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleQuickCategory: (DiscoveryCategoryFilter) -> Unit,
    onToggleQuickAction: (AnnouncementStructuredData.ActionType) -> Unit,
    onOpenFilters: () -> Unit,
    onDismissFilters: () -> Unit,
    onResetFilterDraft: () -> Unit,
    onApplyFilterDraft: () -> Unit,
    onUpdateFilterDraftBudgetMin: (String) -> Unit,
    onUpdateFilterDraftBudgetMax: (String) -> Unit,
    onToggleFilterDraftCategory: (DiscoveryCategoryFilter) -> Unit,
    onToggleFilterDraftUrgency: (AnnouncementStructuredData.Urgency) -> Unit,
    onSetFilterDraftWithPhotoOnly: (Boolean) -> Unit,
    onSetFilterDraftRequiresVehicleOnly: (Boolean) -> Unit,
    onSetFilterDraftNeedsLoaderOnly: (Boolean) -> Unit,
    onSetFilterDraftContactlessOnly: (Boolean) -> Unit,
    onClearAllFilters: () -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
    onShowAnnouncementOnMap: (String) -> Unit,
    onDismissDetails: () -> Unit,
    onOfferMessageChange: (String) -> Unit,
    onOpenCustomPriceDialog: () -> Unit,
    onDismissCustomPriceDialog: () -> Unit,
    onCustomPriceChange: (String) -> Unit,
    onSubmitQuickOffer: () -> Unit,
    onSubmitCustomOffer: () -> Unit,
    onMapFocusConsumed: (Long) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            state.isInitialLoading && state.announcements.isEmpty() -> {
                DiscoveryCenterStatus(
                    title = stringResource(R.string.discovery_loading_title),
                    message = stringResource(R.string.discovery_loading_message),
                    showProgress = true,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.loadErrorMessage != null && state.announcements.isEmpty() -> {
                DiscoveryCenterStatus(
                    title = stringResource(R.string.discovery_error_title),
                    message = state.loadErrorMessage,
                    showProgress = false,
                    modifier = Modifier.align(Alignment.Center),
                    primaryAction = {
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Text(text = stringResource(R.string.discovery_retry))
                        }
                    },
                )
            }

            state.contentMode == DiscoveryContentMode.Map -> {
                DiscoveryMapLayout(
                    state = state,
                    onRetry = onRetry,
                    onRefresh = onRefresh,
                    onSearchQueryChange = onSearchQueryChange,
                    onToggleContentMode = onToggleContentMode,
                    onToggleQuickCategory = onToggleQuickCategory,
                    onToggleQuickAction = onToggleQuickAction,
                    onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                    onOpenFilters = onOpenFilters,
                    onMapFocusConsumed = onMapFocusConsumed,
                    onDismissContentMessage = onDismissContentMessage,
                )
            }

            else -> {
                DiscoveryListLayout(
                    state = state,
                    onRefresh = onRefresh,
                    onSearchQueryChange = onSearchQueryChange,
                    onToggleContentMode = onToggleContentMode,
                    onToggleQuickCategory = onToggleQuickCategory,
                    onToggleQuickAction = onToggleQuickAction,
                    onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                    onShowAnnouncementOnMap = onShowAnnouncementOnMap,
                    onOpenFilters = onOpenFilters,
                    onClearAllFilters = onClearAllFilters,
                    onDismissContentMessage = onDismissContentMessage,
                )
            }
        }

        if (state.isFiltersSheetVisible) {
            FiltersBottomSheet(
                draft = state.filterDraft,
                onDismiss = onDismissFilters,
                onReset = onResetFilterDraft,
                onApply = onApplyFilterDraft,
                onBudgetMinChange = onUpdateFilterDraftBudgetMin,
                onBudgetMaxChange = onUpdateFilterDraftBudgetMax,
                onToggleCategory = onToggleFilterDraftCategory,
                onToggleUrgency = onToggleFilterDraftUrgency,
                onSetWithPhotoOnly = onSetFilterDraftWithPhotoOnly,
                onSetRequiresVehicleOnly = onSetFilterDraftRequiresVehicleOnly,
                onSetNeedsLoaderOnly = onSetFilterDraftNeedsLoaderOnly,
                onSetContactlessOnly = onSetFilterDraftContactlessOnly,
            )
        }

        val detailsState = state.detailsState
        if (detailsState.isVisible && detailsState.item != null) {
            AnnouncementDetailsBottomSheet(
                apiBaseUrl = state.apiBaseUrl,
                detailsState = detailsState,
                onDismiss = onDismissDetails,
                onOfferMessageChange = onOfferMessageChange,
                onOpenCustomPriceDialog = onOpenCustomPriceDialog,
                onSubmitQuickOffer = onSubmitQuickOffer,
            )
        }

        if (detailsState.isCustomPriceDialogVisible) {
            CustomOfferPriceDialog(
                priceInput = detailsState.customPriceInput,
                error = detailsState.customPriceError,
                isSubmitting = detailsState.isSubmittingCustomOffer,
                onDismiss = onDismissCustomPriceDialog,
                onPriceChange = onCustomPriceChange,
                onSubmit = onSubmitCustomOffer,
            )
        }
    }
}

// ── Map-first layout ───────────────────────────────────────────────

@Composable
private fun DiscoveryMapLayout(
    state: DiscoveryUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleContentMode: () -> Unit,
    onToggleQuickCategory: (DiscoveryCategoryFilter) -> Unit,
    onToggleQuickAction: (AnnouncementStructuredData.ActionType) -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
    onOpenFilters: () -> Unit,
    onMapFocusConsumed: (Long) -> Unit,
    onDismissContentMessage: () -> Unit,
) {
    var isMapLoaded by remember(state.isMapConfigured) { mutableStateOf(false) }
    var isMapLoadTimedOut by remember(state.isMapConfigured) { mutableStateOf(false) }

    LaunchedEffect(state.isMapConfigured) {
        if (!state.isMapConfigured) {
            isMapLoaded = false
            isMapLoadTimedOut = false
            return@LaunchedEffect
        }
        isMapLoaded = false
        isMapLoadTimedOut = false
        delay(6_000)
        if (!isMapLoaded) {
            isMapLoadTimedOut = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen map canvas
        if (state.isMapConfigured) {
            MapCanvas(
                state = state,
                onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                onMapFocusConsumed = onMapFocusConsumed,
                onMapLoaded = {
                    isMapLoaded = true
                    isMapLoadTimedOut = false
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Map status overlays
        if (!state.isMapConfigured) {
            DiscoveryCenterStatus(
                title = stringResource(R.string.discovery_map_unavailable_title),
                message = stringResource(R.string.discovery_map_unavailable_message),
                showProgress = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
            )
        }
        if (state.isMapConfigured && !isMapLoaded && !isMapLoadTimedOut) {
            DiscoveryCenterStatus(
                title = stringResource(R.string.discovery_map_loading_title),
                message = stringResource(R.string.discovery_map_loading_message),
                showProgress = true,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
            )
        }
        if (state.isMapConfigured && isMapLoadTimedOut) {
            DiscoveryCenterStatus(
                title = stringResource(R.string.discovery_map_runtime_unavailable_title),
                message = stringResource(R.string.discovery_map_runtime_unavailable_message),
                showProgress = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                primaryAction = {
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text(text = stringResource(R.string.discovery_retry))
                    }
                },
            )
        }
        if (
            state.mapAnnouncements.isEmpty() &&
            state.loadErrorMessage == null &&
            !state.isInitialLoading &&
            (!state.isMapConfigured || isMapLoaded || isMapLoadTimedOut)
        ) {
            DiscoveryCenterStatus(
                title = stringResource(R.string.discovery_empty_title),
                message = if (state.announcements.isEmpty()) {
                    stringResource(R.string.discovery_empty_message)
                } else {
                    stringResource(R.string.discovery_empty_map_message)
                },
                showProgress = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
            )
        }
        if (state.loadErrorMessage != null && state.announcements.isNotEmpty()) {
            DiscoveryCenterStatus(
                title = stringResource(R.string.discovery_error_title),
                message = state.loadErrorMessage,
                showProgress = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                primaryAction = {
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Text(text = stringResource(R.string.discovery_retry))
                    }
                },
            )
        }

        // Top overlay: search + chips
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                )
                .padding(top = 12.dp),
        ) {
            MapSearchBar(
                modifier = Modifier.padding(horizontal = 16.dp),
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                onToggleContentMode = onToggleContentMode,
            )
            Spacer(modifier = Modifier.height(10.dp))
            MapQuickChips(
                filters = state.filters,
                onToggleQuickCategory = onToggleQuickCategory,
                onToggleQuickAction = onToggleQuickAction,
            )
            state.contentMessage?.let { message ->
                InlineMessageCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    message = message,
                    onDismiss = onDismissContentMessage,
                )
            }
        }

        // Right-side map action buttons
        MapActionButtons(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
        )

        // Bottom floating card
        FiltersAndRouteCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            onOpenFilters = onOpenFilters,
            resultCount = state.announcements.size,
            filterCount = state.filters.activeCount,
        )
    }
}

// ── Map overlay components ─────────────────────────────────────────

@Composable
private fun MapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleContentMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.discovery_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon = if (query.isNotBlank()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.discovery_clear_search),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                } else {
                    null
                },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            )
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
        ) {
            IconButton(
                modifier = Modifier.size(52.dp),
                onClick = onToggleContentMode,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.List,
                    contentDescription = stringResource(R.string.discovery_switch_to_list),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun MapQuickChips(
    filters: DiscoveryFilterState,
    onToggleQuickCategory: (DiscoveryCategoryFilter) -> Unit,
    onToggleQuickAction: (AnnouncementStructuredData.ActionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            OneUiChip(
                selected = filters.categories.contains(DiscoveryCategoryFilter.Delivery),
                onClick = { onToggleQuickCategory(DiscoveryCategoryFilter.Delivery) },
                label = stringResource(R.string.discovery_category_delivery),
                leadingIcon = Icons.Outlined.NearMe,
            )
        }
        item {
            OneUiChip(
                selected = filters.categories.contains(DiscoveryCategoryFilter.Help),
                onClick = { onToggleQuickCategory(DiscoveryCategoryFilter.Help) },
                label = stringResource(R.string.discovery_category_help),
            )
        }
        quickActionTypes.forEach { action ->
            item {
                OneUiChip(
                    selected = filters.actions.contains(action),
                    onClick = { onToggleQuickAction(action) },
                    label = action.uiLabel(),
                )
            }
        }
    }
}

@Composable
private fun OneUiChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        shadowElevation = if (selected) 0.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun MapActionButton(
    icon: @Composable () -> Unit,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        IconButton(
            modifier = Modifier.fillMaxSize(),
            onClick = onClick,
        ) {
            icon()
        }
    }
}

@Composable
private fun MapActionButtons(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MapActionButton(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            },
            contentDescription = stringResource(R.string.discovery_zoom_in),
            onClick = { /* Zoom handled by MapKit gestures */ },
        )
        MapActionButton(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            },
            contentDescription = stringResource(R.string.discovery_zoom_out),
            onClick = { /* Zoom handled by MapKit gestures */ },
        )
        MapActionButton(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            },
            contentDescription = stringResource(R.string.discovery_my_location),
            onClick = { /* Location permission + focus */ },
        )
    }
}

@Composable
private fun FiltersAndRouteCard(
    onOpenFilters: () -> Unit,
    resultCount: Int,
    filterCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onOpenFilters)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    modifier = Modifier.padding(10.dp),
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.discovery_filters_and_route),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (filterCount > 0) {
                        stringResource(R.string.discovery_filters_with_count, filterCount)
                    } else {
                        stringResource(R.string.discovery_filters_and_route_subtitle)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    modifier = Modifier.padding(8.dp).size(18.dp),
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

// ── List layout ────────────────────────────────────────────────────

@Composable
private fun DiscoveryListLayout(
    state: DiscoveryUiState,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleContentMode: () -> Unit,
    onToggleQuickCategory: (DiscoveryCategoryFilter) -> Unit,
    onToggleQuickAction: (AnnouncementStructuredData.ActionType) -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
    onShowAnnouncementOnMap: (String) -> Unit,
    onOpenFilters: () -> Unit,
    onClearAllFilters: () -> Unit,
    onDismissContentMessage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            ),
    ) {
        // Search bar with map toggle
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.discovery_search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = if (state.searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = stringResource(R.string.discovery_clear_search),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    } else {
                        null
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
            ) {
                IconButton(
                    modifier = Modifier.size(52.dp),
                    onClick = onToggleContentMode,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = stringResource(R.string.discovery_switch_to_map),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // Quick chips
        Spacer(modifier = Modifier.height(10.dp))
        MapQuickChips(
            filters = state.filters,
            onToggleQuickCategory = onToggleQuickCategory,
            onToggleQuickAction = onToggleQuickAction,
        )

        // Summary + filter bar
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(
                    R.string.discovery_results_summary,
                    state.announcements.size,
                    state.totalAnnouncementCount,
                ),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Surface(
                onClick = onOpenFilters,
                shape = RoundedCornerShape(20.dp),
                color = if (state.filters.activeCount > 0) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (state.filters.activeCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Text(
                        text = if (state.filters.activeCount > 0) {
                            stringResource(R.string.discovery_filters_with_count, state.filters.activeCount)
                        } else {
                            stringResource(R.string.discovery_filters_button)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (state.filters.activeCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }

        state.contentMessage?.let { message ->
            InlineMessageCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                message = message,
                onDismiss = onDismissContentMessage,
            )
        }

        // List content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 100.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.announcements.isEmpty()) {
                item {
                    DiscoveryCenterStatus(
                        title = stringResource(R.string.discovery_empty_title),
                        message = stringResource(R.string.discovery_empty_message),
                        showProgress = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                items(
                    items = state.announcements,
                    key = { item -> item.announcement.id },
                ) { item ->
                    AnnouncementListCard(
                        item = item,
                        onOpenDetails = { onOpenAnnouncementDetails(item.announcement.id) },
                        onShowOnMap = { onShowAnnouncementOnMap(item.announcement.id) },
                        apiBaseUrl = state.apiBaseUrl,
                    )
                }
            }
        }
    }
}

// ── Shared components ──────────────────────────────────────────────

@Composable
private fun InlineMessageCard(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.discovery_message_dismiss))
            }
        }
    }
}

@Composable
private fun AnnouncementListCard(
    item: DiscoveryAnnouncementItemUi,
    apiBaseUrl: String,
    onOpenDetails: () -> Unit,
    onShowOnMap: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onOpenDetails),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                AnnouncementImage(
                    announcement = item.announcement,
                    apiBaseUrl = apiBaseUrl,
                    modifier = Modifier.size(80.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.announcement.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        DiscoveryInfoChip(label = item.announcement.discoveryCategoryLabel())
                        item.budgetText?.let { budgetText ->
                            DiscoveryInfoChip(label = budgetText)
                        }
                    }
                }
            }

            AddressSummary(
                sourceAddress = item.sourceAddress,
                destinationAddress = item.destinationAddress,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onOpenDetails,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.discovery_open_details),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                if (item.point != null) {
                    OutlinedButton(
                        onClick = onShowOnMap,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.discovery_show_on_map),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryCenterStatus(
    title: String,
    message: String,
    showProgress: Boolean,
    modifier: Modifier = Modifier,
    primaryAction: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        modifier = Modifier.padding(14.dp),
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            primaryAction?.invoke()
        }
    }
}

@Composable
private fun AnnouncementImage(
    announcement: Announcement,
    apiBaseUrl: String,
    modifier: Modifier = Modifier,
) {
    val previewUrl = announcement.imageUrls(apiBaseUrl).firstOrNull()
    if (previewUrl != null) {
        AsyncImage(
            modifier = modifier.clip(RoundedCornerShape(18.dp)),
            model = previewUrl,
            contentDescription = stringResource(R.string.ads_image_preview_description),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoCameraBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun AddressSummary(
    sourceAddress: String?,
    destinationAddress: String?,
    expanded: Boolean = false,
) {
    val lines = buildList {
        if (!sourceAddress.isNullOrBlank()) {
            add(stringResource(R.string.discovery_address_from, sourceAddress))
        }
        if (!destinationAddress.isNullOrBlank()) {
            add(stringResource(R.string.discovery_address_to, destinationAddress))
        }
    }

    if (lines.isEmpty()) {
        Text(
            text = stringResource(R.string.discovery_address_missing),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(if (expanded) 10.dp else 4.dp),
    ) {
        lines.forEach { line ->
            Text(
                text = line,
                style = if (expanded) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DiscoveryInfoChip(
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Bottom sheets ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FiltersBottomSheet(
    draft: DiscoveryFilterState,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onBudgetMinChange: (String) -> Unit,
    onBudgetMaxChange: (String) -> Unit,
    onToggleCategory: (DiscoveryCategoryFilter) -> Unit,
    onToggleUrgency: (AnnouncementStructuredData.Urgency) -> Unit,
    onSetWithPhotoOnly: (Boolean) -> Unit,
    onSetRequiresVehicleOnly: (Boolean) -> Unit,
    onSetNeedsLoaderOnly: (Boolean) -> Unit,
    onSetContactlessOnly: (Boolean) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 20.dp,
                    top = 0.dp,
                    end = 20.dp,
                    bottom = 20.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.discovery_filters_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.discovery_filters_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onReset) {
                    Text(text = stringResource(R.string.discovery_reset_filters))
                }
            }

            FilterSection(title = stringResource(R.string.discovery_filter_category_title)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = draft.categories.contains(DiscoveryCategoryFilter.Delivery),
                        onClick = { onToggleCategory(DiscoveryCategoryFilter.Delivery) },
                        label = { Text(text = stringResource(R.string.discovery_category_delivery)) },
                        shape = RoundedCornerShape(16.dp),
                    )
                    FilterChip(
                        selected = draft.categories.contains(DiscoveryCategoryFilter.Help),
                        onClick = { onToggleCategory(DiscoveryCategoryFilter.Help) },
                        label = { Text(text = stringResource(R.string.discovery_category_help)) },
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }

            FilterSection(title = stringResource(R.string.discovery_filter_urgency_title)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    urgencyFilters.forEach { urgency ->
                        FilterChip(
                            selected = draft.urgencies.contains(urgency),
                            onClick = { onToggleUrgency(urgency) },
                            label = { Text(text = urgency.uiLabel()) },
                            shape = RoundedCornerShape(16.dp),
                        )
                    }
                }
            }

            FilterSection(title = stringResource(R.string.discovery_filter_budget_title)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.budgetMinText,
                        onValueChange = onBudgetMinChange,
                        label = { Text(text = stringResource(R.string.discovery_filter_budget_min)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.budgetMaxText,
                        onValueChange = onBudgetMaxChange,
                        label = { Text(text = stringResource(R.string.discovery_filter_budget_max)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }

            FilterSection(title = stringResource(R.string.discovery_filter_preferences_title)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterToggleRow(
                        title = stringResource(R.string.discovery_filter_with_photo),
                        checked = draft.withPhotoOnly,
                        onCheckedChange = onSetWithPhotoOnly,
                    )
                    FilterToggleRow(
                        title = stringResource(R.string.discovery_filter_requires_vehicle),
                        checked = draft.requiresVehicleOnly,
                        onCheckedChange = onSetRequiresVehicleOnly,
                    )
                    FilterToggleRow(
                        title = stringResource(R.string.discovery_filter_needs_loader),
                        checked = draft.needsLoaderOnly,
                        onCheckedChange = onSetNeedsLoaderOnly,
                    )
                    FilterToggleRow(
                        title = stringResource(R.string.discovery_filter_contactless),
                        checked = draft.contactlessOnly,
                        onCheckedChange = onSetContactlessOnly,
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onApply,
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.discovery_apply_filters),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementDetailsBottomSheet(
    apiBaseUrl: String,
    detailsState: DiscoveryDetailsUiState,
    onDismiss: () -> Unit,
    onOfferMessageChange: (String) -> Unit,
    onOpenCustomPriceDialog: () -> Unit,
    onSubmitQuickOffer: () -> Unit,
) {
    val item = detailsState.item ?: return
    val announcement = item.announcement

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            if (detailsState.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 16.dp,
                    end = 20.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (announcement.hasAttachedMedia) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = announcement.imageUrls(apiBaseUrl),
                                key = { imageUrl -> imageUrl },
                            ) { imageUrl ->
                                AsyncImage(
                                    modifier = Modifier
                                        .size(width = 200.dp, height = 150.dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                    model = imageUrl,
                                    contentDescription = stringResource(R.string.ads_image_preview_description),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }

                item {
                    DetailsSectionCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = announcement.discoveryCategoryLabel(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = announcement.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (item.subtitle.isNotBlank()) {
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item.budgetText?.let { budgetText ->
                                    DiscoveryInfoChip(label = budgetText, leadingIcon = Icons.Outlined.Inventory2)
                                }
                                DiscoveryInfoChip(
                                    label = item.responseGate.availabilityLabel(),
                                    leadingIcon = Icons.Outlined.CheckCircle,
                                )
                            }
                        }
                    }
                }

                item {
                    DetailsSectionCard(title = stringResource(R.string.discovery_details_addresses)) {
                        AddressSummary(
                            sourceAddress = announcement.primarySourceAddress,
                            destinationAddress = announcement.primaryDestinationAddress,
                            expanded = true,
                        )
                    }
                }

                announcement.detailsDescriptionText?.let { description ->
                    item {
                        DetailsSectionCard(title = stringResource(R.string.discovery_details_description)) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                item {
                    val rows = contactSummaryRows(announcement)
                    if (rows.isNotEmpty()) {
                        DetailsSectionCard(title = stringResource(R.string.discovery_details_contacts)) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                rows.forEach { row ->
                                    DetailValueRow(label = row.first, value = row.second)
                                }
                            }
                        }
                    }
                }

                detailsState.loadErrorMessage?.let { loadError ->
                    item {
                        DetailsSectionCard {
                            Text(
                                text = loadError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                detailsState.actionErrorMessage?.let { errorMessage ->
                    item {
                        DetailsSectionCard {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                if (detailsState.offerSuccessState == DiscoveryOfferSuccessState.Submitted) {
                    item {
                        DetailsSectionCard {
                            ResponseStatusBanner(
                                title = stringResource(R.string.discovery_offer_success_title),
                                subtitle = stringResource(R.string.discovery_offer_success_message),
                            )
                        }
                    }
                }
            }

            DiscoveryResponseComposer(
                detailsState = detailsState,
                item = item,
                onOfferMessageChange = onOfferMessageChange,
                onOpenCustomPriceDialog = onOpenCustomPriceDialog,
                onSubmitQuickOffer = onSubmitQuickOffer,
            )
        }
    }
}

@Composable
private fun DiscoveryResponseComposer(
    detailsState: DiscoveryDetailsUiState,
    item: DiscoveryAnnouncementItemUi,
    onOfferMessageChange: (String) -> Unit,
    onOpenCustomPriceDialog: () -> Unit,
    onSubmitQuickOffer: () -> Unit,
) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                detailsState.offerSuccessState == DiscoveryOfferSuccessState.Submitted -> {
                    ResponseStatusBanner(
                        title = stringResource(R.string.discovery_offer_success_title),
                        subtitle = stringResource(R.string.discovery_offer_success_message),
                    )
                }

                item.responseGate != DiscoveryResponseGate.Available -> {
                    val gateCopy = item.responseGate.gateCopy()
                    ResponseStatusBanner(
                        title = stringResource(gateCopy.first),
                        subtitle = stringResource(gateCopy.second),
                    )
                }

                else -> {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = detailsState.offerMessage,
                        onValueChange = onOfferMessageChange,
                        label = { Text(text = stringResource(R.string.discovery_offer_message_label)) },
                        placeholder = { Text(text = stringResource(R.string.discovery_offer_message_placeholder)) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        announcementQuickOfferPrice(item.announcement)?.let { quickOfferPriceText ->
                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !detailsState.isSubmitting,
                                onClick = onSubmitQuickOffer,
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = quickOfferPriceText)
                            }
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = !detailsState.isSubmitting,
                            onClick = onOpenCustomPriceDialog,
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Text(text = stringResource(R.string.discovery_offer_custom_price))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResponseStatusBanner(
    title: String,
    subtitle: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CustomOfferPriceDialog(
    priceInput: String,
    error: DiscoveryCustomPriceError?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onPriceChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(text = stringResource(R.string.discovery_offer_custom_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = priceInput,
                    onValueChange = onPriceChange,
                    label = { Text(text = stringResource(R.string.discovery_offer_custom_field)) },
                    supportingText = error?.let {
                        { Text(text = stringResource(R.string.discovery_offer_custom_error)) }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = onSubmit,
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(text = stringResource(R.string.discovery_offer_send))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.discovery_offer_cancel))
            }
        },
    )
}

@Composable
private fun DetailsSectionCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun DetailValueRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}

@Composable
private fun FilterToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

// ── Extensions ─────────────────────────────────────────────────────

@Composable
private fun Announcement.discoveryCategoryLabel(): String = when (category.trim().lowercase()) {
    DiscoveryCategoryFilter.Delivery.rawValue -> stringResource(R.string.ads_category_delivery)
    DiscoveryCategoryFilter.Help.rawValue -> stringResource(R.string.ads_category_help)
    else -> stringResource(R.string.discovery_category_generic)
}

@Composable
private fun AnnouncementStructuredData.ActionType.uiLabel(): String = when (this) {
    AnnouncementStructuredData.ActionType.Pickup -> stringResource(R.string.discovery_action_pickup)
    AnnouncementStructuredData.ActionType.Buy -> stringResource(R.string.discovery_action_buy)
    AnnouncementStructuredData.ActionType.Carry -> stringResource(R.string.discovery_action_carry)
    AnnouncementStructuredData.ActionType.Ride -> stringResource(R.string.discovery_action_ride)
    AnnouncementStructuredData.ActionType.ProHelp -> stringResource(R.string.discovery_action_pro_help)
    AnnouncementStructuredData.ActionType.Other -> stringResource(R.string.discovery_action_other)
}

@Composable
private fun AnnouncementStructuredData.Urgency.uiLabel(): String = when (this) {
    AnnouncementStructuredData.Urgency.Now -> stringResource(R.string.discovery_urgency_now)
    AnnouncementStructuredData.Urgency.Today -> stringResource(R.string.discovery_urgency_today)
    AnnouncementStructuredData.Urgency.Scheduled -> stringResource(R.string.discovery_urgency_scheduled)
    AnnouncementStructuredData.Urgency.Flexible -> stringResource(R.string.discovery_urgency_flexible)
}

@Composable
private fun DiscoveryResponseGate.availabilityLabel(): String = when (this) {
    DiscoveryResponseGate.Available -> stringResource(R.string.discovery_gate_available_label)
    DiscoveryResponseGate.OwnAnnouncement -> stringResource(R.string.discovery_gate_own_label)
    DiscoveryResponseGate.RequiresAuth -> stringResource(R.string.discovery_gate_auth_label)
    DiscoveryResponseGate.Unavailable -> stringResource(R.string.discovery_gate_unavailable_label)
    DiscoveryResponseGate.AlreadyResponded -> stringResource(R.string.discovery_gate_responded_label)
}

private fun DiscoveryResponseGate.gateCopy(): Pair<Int, Int> = when (this) {
    DiscoveryResponseGate.Available -> R.string.discovery_gate_available_label to R.string.discovery_gate_available_message
    DiscoveryResponseGate.OwnAnnouncement -> R.string.discovery_gate_own_title to R.string.discovery_gate_own_message
    DiscoveryResponseGate.RequiresAuth -> R.string.discovery_gate_auth_title to R.string.discovery_gate_auth_message
    DiscoveryResponseGate.Unavailable -> R.string.discovery_gate_unavailable_title to R.string.discovery_gate_unavailable_message
    DiscoveryResponseGate.AlreadyResponded -> R.string.discovery_gate_responded_title to R.string.discovery_gate_responded_message
}

@Composable
private fun announcementQuickOfferPrice(announcement: Announcement): String? =
    announcement.quickOfferPrice?.let { quickOfferPrice ->
        stringResource(R.string.discovery_offer_quick_price, quickOfferPrice)
    }

@Composable
private fun contactSummaryRows(announcement: Announcement): List<Pair<String, String>> {
    val name = announcement.taskStringValue(
        paths = listOf(listOf("task", "contacts", "name")),
        legacyKeys = listOf("contact_name"),
    )
    val phone = announcement.taskStringValue(
        paths = listOf(listOf("task", "contacts", "phone")),
        legacyKeys = listOf("contact_phone"),
    )
    val method = announcement.taskStringValue(
        paths = listOf(listOf("task", "contacts", "method")),
        legacyKeys = listOf("contact_method"),
    )

    return buildList {
        if (!name.isNullOrBlank()) {
            add(stringResource(R.string.discovery_contact_name) to name)
        }
        method?.let {
            add(stringResource(R.string.discovery_contact_method) to contactMethodLabel(it))
        }
        if (!phone.isNullOrBlank()) {
            add(
                stringResource(R.string.discovery_contact_phone) to
                    stringResource(R.string.discovery_contact_phone_provided),
            )
        }
    }
}

@Composable
private fun contactMethodLabel(rawValue: String): String = when (rawValue.trim().lowercase()) {
    "calls_and_messages" -> stringResource(R.string.discovery_contact_method_calls_and_messages)
    "messages_only" -> stringResource(R.string.discovery_contact_method_messages_only)
    "calls_only" -> stringResource(R.string.discovery_contact_method_calls_only)
    else -> stringResource(R.string.discovery_contact_method_unknown)
}

private val quickActionTypes = listOf(
    AnnouncementStructuredData.ActionType.Pickup,
    AnnouncementStructuredData.ActionType.Buy,
    AnnouncementStructuredData.ActionType.Carry,
    AnnouncementStructuredData.ActionType.Ride,
    AnnouncementStructuredData.ActionType.ProHelp,
)

private val urgencyFilters = listOf(
    AnnouncementStructuredData.Urgency.Now,
    AnnouncementStructuredData.Urgency.Today,
    AnnouncementStructuredData.Urgency.Scheduled,
    AnnouncementStructuredData.Urgency.Flexible,
)
