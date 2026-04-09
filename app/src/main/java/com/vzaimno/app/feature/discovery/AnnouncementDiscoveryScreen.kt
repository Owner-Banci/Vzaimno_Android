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
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.26f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {
            DiscoveryHeader(
                modifier = Modifier.padding(
                    start = MaterialTheme.spacing.xLarge,
                    top = MaterialTheme.spacing.xxLarge,
                    end = MaterialTheme.spacing.xLarge,
                ),
            )
            SearchAndModeRow(
                modifier = Modifier.padding(
                    start = MaterialTheme.spacing.xLarge,
                    top = MaterialTheme.spacing.large,
                    end = MaterialTheme.spacing.xLarge,
                ),
                state = state,
                onSearchQueryChange = onSearchQueryChange,
                onToggleContentMode = onToggleContentMode,
            )
            QuickFiltersRow(
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                filters = state.filters,
                onToggleQuickCategory = onToggleQuickCategory,
                onToggleQuickAction = onToggleQuickAction,
            )
            DiscoverySummaryRow(
                modifier = Modifier.padding(
                    start = MaterialTheme.spacing.xLarge,
                    top = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.xLarge,
                ),
                state = state,
                onOpenFilters = onOpenFilters,
                onClearAllFilters = onClearAllFilters,
            )

            state.contentMessage?.let { message ->
                InlineMessageCard(
                    modifier = Modifier.padding(
                        start = MaterialTheme.spacing.xLarge,
                        top = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.xLarge,
                    ),
                    message = message,
                    onDismiss = onDismissContentMessage,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = MaterialTheme.spacing.large),
            ) {
                when {
                    state.isInitialLoading && state.announcements.isEmpty() -> {
                        CenterStatusCard(
                            title = stringResource(R.string.discovery_loading_title),
                            message = stringResource(R.string.discovery_loading_message),
                            showProgress = true,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = MaterialTheme.spacing.xLarge),
                        )
                    }

                    state.loadErrorMessage != null && state.announcements.isEmpty() -> {
                        CenterStatusCard(
                            title = stringResource(R.string.discovery_error_title),
                            message = state.loadErrorMessage,
                            showProgress = false,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = MaterialTheme.spacing.xLarge),
                            primaryAction = {
                                Button(onClick = onRetry) {
                                    Text(text = stringResource(R.string.discovery_retry))
                                }
                            },
                        )
                    }

                    state.contentMode == DiscoveryContentMode.Map -> {
                        DiscoveryMapContent(
                            state = state,
                            onRetry = onRetry,
                            onRefresh = onRefresh,
                            onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                            onOpenFilters = onOpenFilters,
                            onMapFocusConsumed = onMapFocusConsumed,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    else -> {
                        DiscoveryListContent(
                            state = state,
                            onRefresh = onRefresh,
                            onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                            onShowAnnouncementOnMap = onShowAnnouncementOnMap,
                            onOpenFilters = onOpenFilters,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
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

@Composable
private fun DiscoveryHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        Text(
            text = stringResource(R.string.shell_brand_caption),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.discovery_title),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.discovery_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchAndModeRow(
    state: DiscoveryUiState,
    onSearchQueryChange: (String) -> Unit,
    onToggleContentMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(text = stringResource(R.string.discovery_search_placeholder))
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                )
            },
            trailingIcon = if (state.searchQuery.isNotBlank()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.discovery_clear_search),
                        )
                    }
                }
            } else {
                null
            },
            shape = RoundedCornerShape(24.dp),
        )

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
        ) {
            IconButton(
                modifier = Modifier.size(56.dp),
                onClick = onToggleContentMode,
            ) {
                Icon(
                    imageVector = if (state.contentMode == DiscoveryContentMode.Map) {
                        Icons.AutoMirrored.Outlined.List
                    } else {
                        Icons.Outlined.Map
                    },
                    contentDescription = if (state.contentMode == DiscoveryContentMode.Map) {
                        stringResource(R.string.discovery_switch_to_list)
                    } else {
                        stringResource(R.string.discovery_switch_to_map)
                    },
                )
            }
        }
    }
}

@Composable
private fun QuickFiltersRow(
    filters: DiscoveryFilterState,
    onToggleQuickCategory: (DiscoveryCategoryFilter) -> Unit,
    onToggleQuickAction: (AnnouncementStructuredData.ActionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.xLarge),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        item {
            FilterChip(
                selected = filters.categories.contains(DiscoveryCategoryFilter.Delivery),
                onClick = { onToggleQuickCategory(DiscoveryCategoryFilter.Delivery) },
                label = {
                    Text(text = stringResource(R.string.discovery_category_delivery))
                },
            )
        }
        item {
            FilterChip(
                selected = filters.categories.contains(DiscoveryCategoryFilter.Help),
                onClick = { onToggleQuickCategory(DiscoveryCategoryFilter.Help) },
                label = {
                    Text(text = stringResource(R.string.discovery_category_help))
                },
            )
        }

        quickActionTypes.forEach { action ->
            item {
                FilterChip(
                    selected = filters.actions.contains(action),
                    onClick = { onToggleQuickAction(action) },
                    label = {
                        Text(text = action.uiLabel())
                    },
                )
            }
        }
    }
}

@Composable
private fun DiscoverySummaryRow(
    state: DiscoveryUiState,
    onOpenFilters: () -> Unit,
    onClearAllFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
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
                        text = stringResource(
                            R.string.discovery_results_summary,
                            state.announcements.size,
                            state.totalAnnouncementCount,
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(
                            R.string.discovery_map_points_summary,
                            state.mapAnnouncements.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedButton(
                    onClick = onOpenFilters,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.filters.activeCount > 0) {
                            stringResource(R.string.discovery_filters_with_count, state.filters.activeCount)
                        } else {
                            stringResource(R.string.discovery_filters_button)
                        },
                    )
                }
            }

            if (state.hasActiveSearchOrFilters) {
                TextButton(
                    onClick = onClearAllFilters,
                ) {
                    Text(text = stringResource(R.string.discovery_clear_filters))
                }
            }
        }
    }
}

@Composable
private fun InlineMessageCard(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
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
private fun DiscoveryMapContent(
    state: DiscoveryUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
    onOpenFilters: () -> Unit,
    onMapFocusConsumed: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(horizontal = MaterialTheme.spacing.xLarge),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isMapConfigured) {
                MapCanvas(
                    state = state,
                    onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                    onMapFocusConsumed = onMapFocusConsumed,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                CenterStatusCard(
                    title = stringResource(R.string.discovery_map_unavailable_title),
                    message = stringResource(R.string.discovery_map_unavailable_message),
                    showProgress = false,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(MaterialTheme.spacing.xLarge),
                )
            }

            if (state.mapAnnouncements.isEmpty() && state.loadErrorMessage == null && !state.isInitialLoading) {
                CenterStatusCard(
                    title = stringResource(R.string.discovery_empty_title),
                    message = if (state.announcements.isEmpty()) {
                        stringResource(R.string.discovery_empty_message)
                    } else {
                        stringResource(R.string.discovery_empty_map_message)
                    },
                    showProgress = false,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(MaterialTheme.spacing.xLarge),
                )
            }

            if (state.loadErrorMessage != null && state.announcements.isNotEmpty()) {
                CenterStatusCard(
                    title = stringResource(R.string.discovery_error_title),
                    message = state.loadErrorMessage,
                    showProgress = false,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(MaterialTheme.spacing.xLarge),
                    primaryAction = {
                        Button(onClick = onRetry) {
                            Text(text = stringResource(R.string.discovery_retry))
                        }
                    },
                )
            }

            DiscoveryFloatingActions(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        horizontal = MaterialTheme.spacing.large,
                        vertical = MaterialTheme.spacing.large,
                    ),
                onRefresh = onRefresh,
                onOpenFilters = onOpenFilters,
            )
        }
    }
}

@Composable
private fun MapCanvas(
    state: DiscoveryUiState,
    onOpenAnnouncementDetails: (String) -> Unit,
    onMapFocusConsumed: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(55.751244, 37.618423),
            10f,
        )
    }
    var isMapLoaded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val cameraPadding = with(density) { 64.dp.roundToPx() }

    LaunchedEffect(isMapLoaded, state.mapViewport) {
        if (!isMapLoaded) return@LaunchedEffect
        cameraPositionState.animate(
            update = state.mapViewport.toCameraUpdate(cameraPadding),
            durationMs = 700,
        )
    }

    LaunchedEffect(isMapLoaded, state.mapFocusRequest?.token) {
        val request = state.mapFocusRequest ?: return@LaunchedEffect
        if (!isMapLoaded) return@LaunchedEffect

        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(
                LatLng(request.point.latitude, request.point.longitude),
                request.zoom,
            ),
            durationMs = 650,
        )
        onMapFocusConsumed(request.token)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            compassEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
        ),
        properties = MapProperties(
            isMyLocationEnabled = false,
        ),
        onMapLoaded = {
            isMapLoaded = true
        },
    ) {
        state.mapAnnouncements.forEach { item ->
            val point = item.point ?: return@forEach
            Marker(
                state = MarkerState(
                    position = LatLng(point.latitude, point.longitude),
                ),
                title = item.announcement.title,
                snippet = item.subtitle,
                icon = BitmapDescriptorFactory.defaultMarker(
                    if (state.detailsState.announcementId == item.announcement.id) {
                        BitmapDescriptorFactory.HUE_AZURE
                    } else {
                        BitmapDescriptorFactory.HUE_RED
                    },
                ),
                onClick = {
                    onOpenAnnouncementDetails(item.announcement.id)
                    true
                },
            )
        }
    }
}

@Composable
private fun DiscoveryFloatingActions(
    onRefresh: () -> Unit,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.End,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
        ) {
            IconButton(
                modifier = Modifier.size(52.dp),
                onClick = onRefresh,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.discovery_refresh),
                )
            }
        }

        Button(
            onClick = onOpenFilters,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterAlt,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.discovery_filters_button))
        }
    }
}

@Composable
private fun DiscoveryListContent(
    state: DiscoveryUiState,
    onRefresh: () -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
    onShowAnnouncementOnMap: (String) -> Unit,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = MaterialTheme.spacing.xLarge,
                top = 0.dp,
                end = MaterialTheme.spacing.xLarge,
                bottom = 160.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            if (state.announcements.isEmpty()) {
                item {
                    CenterStatusCard(
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

        DiscoveryFloatingActions(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.large,
                ),
            onRefresh = onRefresh,
            onOpenFilters = onOpenFilters,
        )
    }
}

@Composable
private fun AnnouncementListCard(
    item: DiscoveryAnnouncementItemUi,
    apiBaseUrl: String,
    onOpenDetails: () -> Unit,
    onShowOnMap: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.Top,
            ) {
                AnnouncementImage(
                    announcement = item.announcement,
                    apiBaseUrl = apiBaseUrl,
                    modifier = Modifier.size(width = 96.dp, height = 96.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    ) {
                        DiscoveryInfoChip(
                            label = item.announcement.discoveryCategoryLabel(),
                            leadingIcon = Icons.Outlined.NearMe,
                        )
                        DiscoveryInfoChip(
                            label = item.responseGate.availabilityLabel(),
                            leadingIcon = when (item.responseGate) {
                                DiscoveryResponseGate.Available -> Icons.Outlined.CheckCircle
                                else -> Icons.Outlined.Inventory2
                            },
                        )
                    }
                    Text(
                        text = item.announcement.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item.budgetText?.let { budgetText ->
                DiscoveryInfoChip(
                    label = budgetText,
                    leadingIcon = Icons.Outlined.Inventory2,
                )
            }

            AddressSummary(
                sourceAddress = item.sourceAddress,
                destinationAddress = item.destinationAddress,
            )

            if (!item.announcement.hasAttachedMedia) {
                DiscoveryInfoChip(
                    label = stringResource(R.string.discovery_no_photo),
                    leadingIcon = Icons.Outlined.ImageNotSupported,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onOpenDetails,
                ) {
                    Text(text = stringResource(R.string.discovery_open_details))
                }

                if (item.point != null) {
                    OutlinedButton(onClick = onShowOnMap) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.discovery_show_on_map))
                    }
                }
            }
        }
    }
}

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = MaterialTheme.spacing.xLarge,
                    top = 0.dp,
                    end = MaterialTheme.spacing.xLarge,
                    bottom = MaterialTheme.spacing.xLarge,
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
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
                        text = stringResource(R.string.discovery_filters_title),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.discovery_filters_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(
                    onClick = onReset,
                ) {
                    Text(text = stringResource(R.string.discovery_reset_filters))
                }
            }

            FilterSection(
                title = stringResource(R.string.discovery_filter_category_title),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    FilterChip(
                        selected = draft.categories.contains(DiscoveryCategoryFilter.Delivery),
                        onClick = { onToggleCategory(DiscoveryCategoryFilter.Delivery) },
                        label = {
                            Text(text = stringResource(R.string.discovery_category_delivery))
                        },
                    )
                    FilterChip(
                        selected = draft.categories.contains(DiscoveryCategoryFilter.Help),
                        onClick = { onToggleCategory(DiscoveryCategoryFilter.Help) },
                        label = {
                            Text(text = stringResource(R.string.discovery_category_help))
                        },
                    )
                }
            }

            FilterSection(
                title = stringResource(R.string.discovery_filter_urgency_title),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    urgencyFilters.forEach { urgency ->
                        FilterChip(
                            selected = draft.urgencies.contains(urgency),
                            onClick = { onToggleUrgency(urgency) },
                            label = {
                                Text(text = urgency.uiLabel())
                            },
                        )
                    }
                }
            }

            FilterSection(
                title = stringResource(R.string.discovery_filter_budget_title),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.budgetMinText,
                        onValueChange = onBudgetMinChange,
                        label = {
                            Text(text = stringResource(R.string.discovery_filter_budget_min))
                        },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.budgetMaxText,
                        onValueChange = onBudgetMaxChange,
                        label = {
                            Text(text = stringResource(R.string.discovery_filter_budget_max))
                        },
                        singleLine = true,
                    )
                }
            }

            FilterSection(
                title = stringResource(R.string.discovery_filter_preferences_title),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
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
            ) {
                Text(text = stringResource(R.string.discovery_apply_filters))
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
                        .padding(horizontal = MaterialTheme.spacing.xLarge),
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
                    start = MaterialTheme.spacing.xLarge,
                    top = MaterialTheme.spacing.large,
                    end = MaterialTheme.spacing.xLarge,
                    bottom = MaterialTheme.spacing.large,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            ) {
                if (announcement.hasAttachedMedia) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        ) {
                            items(
                                items = announcement.imageUrls(apiBaseUrl),
                                key = { imageUrl -> imageUrl },
                            ) { imageUrl ->
                                AsyncImage(
                                    modifier = Modifier
                                        .size(width = 220.dp, height = 160.dp)
                                        .clip(RoundedCornerShape(24.dp)),
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
                        Column(
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        ) {
                            Text(
                                text = announcement.discoveryCategoryLabel(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = announcement.title,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (item.subtitle.isNotBlank()) {
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            ) {
                                item.budgetText?.let { budgetText ->
                                    DiscoveryInfoChip(
                                        label = budgetText,
                                        leadingIcon = Icons.Outlined.Inventory2,
                                    )
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
                    DetailsSectionCard(
                        title = stringResource(R.string.discovery_details_addresses),
                    ) {
                        AddressSummary(
                            sourceAddress = announcement.primarySourceAddress,
                            destinationAddress = announcement.primaryDestinationAddress,
                            expanded = true,
                        )
                    }
                }

                announcement.detailsDescriptionText?.let { description ->
                    item {
                        DetailsSectionCard(
                            title = stringResource(R.string.discovery_details_description),
                        ) {
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
                        DetailsSectionCard(
                            title = stringResource(R.string.discovery_details_contacts),
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                            ) {
                                rows.forEach { row ->
                                    DetailValueRow(
                                        label = row.first,
                                        value = row.second,
                                    )
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
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.99f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
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
                        label = {
                            Text(text = stringResource(R.string.discovery_offer_message_label))
                        },
                        placeholder = {
                            Text(text = stringResource(R.string.discovery_offer_message_placeholder))
                        },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    ) {
                        announcementQuickOfferPrice(item.announcement)?.let { quickOfferPriceText ->
                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !detailsState.isSubmitting,
                                onClick = onSubmitQuickOffer,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = quickOfferPriceText)
                            }
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = !detailsState.isSubmitting,
                            onClick = onOpenCustomPriceDialog,
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
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
        title = {
            Text(text = stringResource(R.string.discovery_offer_custom_title))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = priceInput,
                    onValueChange = onPriceChange,
                    label = {
                        Text(text = stringResource(R.string.discovery_offer_custom_field))
                    },
                    supportingText = error?.let {
                        {
                            Text(text = stringResource(R.string.discovery_offer_custom_error))
                        }
                    },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = onSubmit,
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
private fun CenterStatusCard(
    title: String,
    message: String,
    showProgress: Boolean,
    modifier: Modifier = Modifier,
    primaryAction: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.xLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            if (showProgress) {
                CircularProgressIndicator()
            } else {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
            modifier = modifier.clip(RoundedCornerShape(24.dp)),
            model = previewUrl,
            contentDescription = stringResource(R.string.ads_image_preview_description),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCameraBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.discovery_no_photo),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(if (expanded) MaterialTheme.spacing.medium else 6.dp),
    ) {
        lines.forEach { line ->
            Text(
                text = line,
                style = if (expanded) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
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
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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

@Composable
private fun DetailsSectionCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
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
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

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
        stringResource(
            R.string.discovery_offer_quick_price,
            quickOfferPrice,
        )
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

private fun DiscoveryMapViewport.toCameraUpdate(padding: Int) = when (this) {
    is DiscoveryMapViewport.FocusPoint -> {
        CameraUpdateFactory.newLatLngZoom(
            LatLng(point.latitude, point.longitude),
            zoom,
        )
    }

    is DiscoveryMapViewport.Bounds -> {
        CameraUpdateFactory.newLatLngBounds(
            LatLngBounds(
                LatLng(southWest.latitude, southWest.longitude),
                LatLng(northEast.latitude, northEast.longitude),
            ),
            padding,
        )
    }

    is DiscoveryMapViewport.Fallback -> {
        CameraUpdateFactory.newLatLngZoom(
            LatLng(point.latitude, point.longitude),
            zoom,
        )
    }
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
