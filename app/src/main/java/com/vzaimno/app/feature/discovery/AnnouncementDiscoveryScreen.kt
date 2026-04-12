package com.vzaimno.app.feature.discovery

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.PhotoCameraBack
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.detailsDescriptionText
import com.vzaimno.app.core.model.formattedBudgetText
import com.vzaimno.app.core.model.hasAttachedMedia
import com.vzaimno.app.core.model.imageUrls
import com.vzaimno.app.core.model.primaryDestinationAddress
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.quickOfferPrice
import com.vzaimno.app.core.model.taskStringValue
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView

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
            state.contentMode == DiscoveryContentMode.Map -> {
                // Full-screen map with overlays
                if (state.isMapConfigured) {
                    YandexMapCanvas(
                        state = state,
                        onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                        onMapFocusConsumed = onMapFocusConsumed,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    )
                }

                // Top overlay: Search bar + filter button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                            ),
                        )
                        .padding(
                            start = MaterialTheme.spacing.large,
                            top = MaterialTheme.spacing.medium,
                            end = MaterialTheme.spacing.large,
                        ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    // Search bar row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.discovery_search_placeholder),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                ),
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                        ) {
                            IconButton(
                                modifier = Modifier.size(56.dp),
                                onClick = onOpenFilters,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = stringResource(R.string.discovery_filters_button),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Quick filter chips
                    QuickActionChipsRow(
                        filters = state.filters,
                        onToggleQuickAction = onToggleQuickAction,
                    )
                }

                // Right-side map action buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.End),
                        )
                        .padding(end = MaterialTheme.spacing.large),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    MapActionButton(
                        icon = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.discovery_zoom_in),
                        onClick = { /* Zoom handled by map */ },
                    )
                    MapActionButton(
                        icon = Icons.Filled.Remove,
                        contentDescription = stringResource(R.string.discovery_zoom_out),
                        onClick = { /* Zoom handled by map */ },
                    )
                    MapActionButton(
                        icon = Icons.Filled.MyLocation,
                        contentDescription = stringResource(R.string.discovery_my_location),
                        onClick = onRefresh,
                    )
                    MapActionButton(
                        icon = Icons.Outlined.PanTool,
                        contentDescription = stringResource(R.string.discovery_pan_mode),
                        onClick = { },
                    )
                }

                // Bottom floating "Filters & Route" card
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                        )
                        .padding(
                            start = MaterialTheme.spacing.large,
                            end = MaterialTheme.spacing.large,
                            bottom = MaterialTheme.spacing.large,
                        )
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenFilters)
                            .padding(MaterialTheme.spacing.large),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.discovery_filters_and_route),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.discovery_filters_and_route_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary,
                        ) {
                            Icon(
                                modifier = Modifier.padding(MaterialTheme.spacing.medium),
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }
                    }
                }

                // Empty state overlay
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
                            .padding(horizontal = MaterialTheme.spacing.xLarge),
                    )
                }

                // Loading overlay
                if (state.isInitialLoading && state.announcements.isEmpty()) {
                    CenterStatusCard(
                        title = stringResource(R.string.discovery_loading_title),
                        message = stringResource(R.string.discovery_loading_message),
                        showProgress = true,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = MaterialTheme.spacing.xLarge),
                    )
                }

                // Error overlay
                if (state.loadErrorMessage != null && state.announcements.isEmpty()) {
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

                // Content message
                state.contentMessage?.let { message ->
                    InlineMessageCard(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
                            )
                            .padding(top = 160.dp)
                            .padding(horizontal = MaterialTheme.spacing.xLarge),
                        message = message,
                        onDismiss = onDismissContentMessage,
                    )
                }
            }

            else -> {
                // List mode
                DiscoveryListContent(
                    state = state,
                    onRefresh = onRefresh,
                    onRetry = onRetry,
                    onSearchQueryChange = onSearchQueryChange,
                    onToggleContentMode = onToggleContentMode,
                    onToggleQuickAction = onToggleQuickAction,
                    onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                    onShowAnnouncementOnMap = onShowAnnouncementOnMap,
                    onOpenFilters = onOpenFilters,
                    onDismissContentMessage = onDismissContentMessage,
                )
            }
        }

        // List/Map mode toggle FAB (only on map mode)
        if (state.contentMode == DiscoveryContentMode.Map) {
            // Mode toggle is in the top bar on list mode
        }

        // Filters bottom sheet
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

        // Details bottom sheet
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

// -- Quick action filter chips matching reference (Забрать, Купить, Перенести) --

@Composable
private fun QuickActionChipsRow(
    filters: DiscoveryFilterState,
    onToggleQuickAction: (AnnouncementStructuredData.ActionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        quickActionTypes.forEach { action ->
            val selected = filters.actions.contains(action)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onToggleQuickAction(action) },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shadowElevation = if (selected) 0.dp else 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = action.chipIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = action.uiLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun MapActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        IconButton(
            modifier = Modifier.fillMaxSize(),
            onClick = onClick,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// -- Yandex MapKit integration --

@Composable
private fun YandexMapCanvas(
    state: DiscoveryUiState,
    onOpenAnnouncementDetails: (String) -> Unit,
    onMapFocusConsumed: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
    }

    var isMapReady by remember { mutableStateOf(false) }

    // Manage lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }
                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Update markers when announcements change
    LaunchedEffect(state.mapAnnouncements, isMapReady) {
        if (!isMapReady) return@LaunchedEffect
        val map = mapView.mapWindow.map
        val mapObjects = map.mapObjects
        mapObjects.clear()

        state.mapAnnouncements.forEach { item ->
            val geoPoint = item.point ?: return@forEach
            val placemark = mapObjects.addPlacemark(
                Point(geoPoint.latitude, geoPoint.longitude),
            )
            placemark.addTapListener(MapObjectTapListener { _, _ ->
                onOpenAnnouncementDetails(item.announcement.id)
                true
            })
        }
    }

    // Handle viewport changes
    LaunchedEffect(state.mapViewport, isMapReady) {
        if (!isMapReady) return@LaunchedEffect
        val map = mapView.mapWindow.map
        val cameraPosition = when (val viewport = state.mapViewport) {
            is DiscoveryMapViewport.FocusPoint -> CameraPosition(
                Point(viewport.point.latitude, viewport.point.longitude),
                viewport.zoom,
                0f, 0f,
            )
            is DiscoveryMapViewport.Bounds -> CameraPosition(
                Point(
                    (viewport.southWest.latitude + viewport.northEast.latitude) / 2,
                    (viewport.southWest.longitude + viewport.northEast.longitude) / 2,
                ),
                12f, 0f, 0f,
            )
            is DiscoveryMapViewport.Fallback -> CameraPosition(
                Point(viewport.point.latitude, viewport.point.longitude),
                viewport.zoom,
                0f, 0f,
            )
        }
        map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 0.7f), null)
    }

    // Handle focus requests
    LaunchedEffect(state.mapFocusRequest?.token, isMapReady) {
        val request = state.mapFocusRequest ?: return@LaunchedEffect
        if (!isMapReady) return@LaunchedEffect
        val map = mapView.mapWindow.map
        map.move(
            CameraPosition(
                Point(request.point.latitude, request.point.longitude),
                request.zoom,
                0f, 0f,
            ),
            Animation(Animation.Type.SMOOTH, 0.65f),
            null,
        )
        onMapFocusConsumed(request.token)
    }

    AndroidView(
        factory = {
            mapView.also {
                val map = it.mapWindow.map
                map.move(
                    CameraPosition(
                        Point(55.751244, 37.618423),
                        10f, 0f, 0f,
                    ),
                )
                isMapReady = true
            }
        },
        modifier = modifier,
    )
}

// -- List mode content --

@Composable
private fun DiscoveryListContent(
    state: DiscoveryUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleContentMode: () -> Unit,
    onToggleQuickAction: (AnnouncementStructuredData.ActionType) -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
    onShowAnnouncementOnMap: (String) -> Unit,
    onOpenFilters: () -> Unit,
    onDismissContentMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            ),
    ) {
        // Search bar + mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.spacing.large,
                    top = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.large,
                ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                    ),
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
            ) {
                IconButton(
                    modifier = Modifier.size(56.dp),
                    onClick = onToggleContentMode,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = stringResource(R.string.discovery_switch_to_map),
                    )
                }
            }
        }

        // Quick filter chips
        QuickActionChipsRow(
            modifier = Modifier.padding(
                start = MaterialTheme.spacing.large,
                top = MaterialTheme.spacing.medium,
                end = MaterialTheme.spacing.large,
            ),
            filters = state.filters,
            onToggleQuickAction = onToggleQuickAction,
        )

        // Content message
        state.contentMessage?.let { message ->
            InlineMessageCard(
                modifier = Modifier.padding(
                    start = MaterialTheme.spacing.large,
                    top = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.large,
                ),
                message = message,
                onDismiss = onDismissContentMessage,
            )
        }

        // List content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = MaterialTheme.spacing.medium),
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

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = MaterialTheme.spacing.large,
                            end = MaterialTheme.spacing.large,
                            bottom = MaterialTheme.spacing.xxxLarge,
                        ),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
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
                }
            }
        }
    }
}

// -- Shared composables --

@Composable
private fun InlineMessageCard(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                    modifier = Modifier.size(width = 84.dp, height = 84.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    Text(
                        text = item.announcement.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    item.budgetText?.let { budgetText ->
                        DiscoveryInfoChip(
                            label = budgetText,
                            leadingIcon = Icons.Outlined.Inventory2,
                        )
                    }
                }
            }

            if (item.point != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onShowOnMap,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                TextButton(onClick = onReset) {
                    Text(text = stringResource(R.string.discovery_reset_filters))
                }
            }

            FilterSection(title = stringResource(R.string.discovery_filter_category_title)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    FilterChip(
                        selected = draft.categories.contains(DiscoveryCategoryFilter.Delivery),
                        onClick = { onToggleCategory(DiscoveryCategoryFilter.Delivery) },
                        label = { Text(text = stringResource(R.string.discovery_category_delivery)) },
                    )
                    FilterChip(
                        selected = draft.categories.contains(DiscoveryCategoryFilter.Help),
                        onClick = { onToggleCategory(DiscoveryCategoryFilter.Help) },
                        label = { Text(text = stringResource(R.string.discovery_category_help)) },
                    )
                }
            }

            FilterSection(title = stringResource(R.string.discovery_filter_urgency_title)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    urgencyFilters.forEach { urgency ->
                        FilterChip(
                            selected = draft.urgencies.contains(urgency),
                            onClick = { onToggleUrgency(urgency) },
                            label = { Text(text = urgency.uiLabel()) },
                        )
                    }
                }
            }

            FilterSection(title = stringResource(R.string.discovery_filter_budget_title)) {
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.budgetMinText,
                        onValueChange = onBudgetMinChange,
                        label = { Text(text = stringResource(R.string.discovery_filter_budget_min)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = draft.budgetMaxText,
                        onValueChange = onBudgetMaxChange,
                        label = { Text(text = stringResource(R.string.discovery_filter_budget_max)) },
                        singleLine = true,
                    )
                }
            }

            FilterSection(title = stringResource(R.string.discovery_filter_preferences_title)) {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
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
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.discovery_apply_filters),
                    modifier = Modifier.padding(vertical = 4.dp),
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

    ModalBottomSheet(onDismissRequest = onDismiss) {
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
                                key = { it },
                            ) { imageUrl ->
                                AsyncImage(
                                    modifier = Modifier
                                        .size(width = 220.dp, height = 160.dp)
                                        .clip(RoundedCornerShape(18.dp)),
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
                        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
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
                            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
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
                            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
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
                        label = { Text(text = stringResource(R.string.discovery_offer_message_label)) },
                        placeholder = { Text(text = stringResource(R.string.discovery_offer_message_placeholder)) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(18.dp),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                        announcementQuickOfferPrice(item.announcement)?.let { quickOfferPriceText ->
                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !detailsState.isSubmitting,
                                onClick = onSubmitQuickOffer,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                ),
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Outlined.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = quickOfferPriceText)
                            }
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = !detailsState.isSubmitting,
                            onClick = onOpenCustomPriceDialog,
                            shape = RoundedCornerShape(18.dp),
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
private fun ResponseStatusBanner(title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
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
        title = { Text(text = stringResource(R.string.discovery_offer_custom_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = priceInput,
                    onValueChange = onPriceChange,
                    label = { Text(text = stringResource(R.string.discovery_offer_custom_field)) },
                    supportingText = error?.let {
                        { Text(text = stringResource(R.string.discovery_offer_custom_error)) }
                    },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(enabled = !isSubmitting, onClick = onSubmit) {
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
            modifier = modifier.clip(RoundedCornerShape(14.dp)),
            model = previewUrl,
            contentDescription = stringResource(R.string.ads_image_preview_description),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCameraBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.discovery_no_photo),
                    style = MaterialTheme.typography.labelSmall,
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
        if (!sourceAddress.isNullOrBlank()) add(stringResource(R.string.discovery_address_from, sourceAddress))
        if (!destinationAddress.isNullOrBlank()) add(stringResource(R.string.discovery_address_to, destinationAddress))
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
        shape = RoundedCornerShape(18.dp),
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
private fun DetailValueRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
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
        shape = RoundedCornerShape(18.dp),
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
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

// -- Label helpers --

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

private fun AnnouncementStructuredData.ActionType.chipIcon(): androidx.compose.ui.graphics.vector.ImageVector = when (this) {
    AnnouncementStructuredData.ActionType.Pickup -> Icons.Outlined.Inventory2
    AnnouncementStructuredData.ActionType.Buy -> Icons.Outlined.Inventory2
    AnnouncementStructuredData.ActionType.Carry -> Icons.Outlined.NearMe
    AnnouncementStructuredData.ActionType.Ride -> Icons.Outlined.NearMe
    AnnouncementStructuredData.ActionType.ProHelp -> Icons.Outlined.CheckCircle
    AnnouncementStructuredData.ActionType.Other -> Icons.Outlined.Place
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
