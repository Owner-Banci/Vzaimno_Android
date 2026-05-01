package com.vzaimno.app.feature.discovery

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingCart
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
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vzaimno.app.R
import com.vzaimno.app.core.common.doubleOrNullCompat
import com.vzaimno.app.core.common.taskBoolValue
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.core.model.GeoPoint
import com.vzaimno.app.core.model.detailChipLabels
import com.vzaimno.app.core.model.detailsDescriptionText
import com.vzaimno.app.core.model.formattedBudgetText
import com.vzaimno.app.core.model.hasAttachedMedia
import com.vzaimno.app.core.model.imageUrls
import com.vzaimno.app.core.model.primaryDestinationAddress
import com.vzaimno.app.core.model.primarySourceAddress
import com.vzaimno.app.core.model.quickOfferPrice
import com.vzaimno.app.core.model.structuredData
import com.vzaimno.app.core.map.YandexMapKitLifecycle
import com.vzaimno.app.core.map.createMovableYandexMapView
import com.vzaimno.app.core.model.taskStringValue
import java.time.Instant
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

private val ShellBottomBarContentPadding = 148.dp

@Composable
fun AnnouncementDiscoveryRoute(
    onOpenCreate: () -> Unit = {},
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
        onSetFilterDraftOnlyOnRoute = viewModel::updateFilterDraftOnlyOnRoute,
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
        onOpenCreate = onOpenCreate,
        onActivateRoute = viewModel::activateRoute,
        onBuildRouteFromDraft = viewModel::buildRouteFromDraft,
        onDeactivateRoute = viewModel::deactivateRoute,
        onUpdateRouteRadius = viewModel::updateRouteRadius,
        onUpdateRouteDraftStartAddress = viewModel::updateRouteDraftStartAddress,
        onUpdateRouteDraftEndAddress = viewModel::updateRouteDraftEndAddress,
        onSelectRouteAnnouncement = viewModel::selectRouteAnnouncement,
        onClearRouteSelection = viewModel::clearRouteSelection,
        onAcceptRouteAnnouncement = viewModel::acceptRouteAnnouncement,
        onRemoveAcceptedRouteAnnouncement = viewModel::removeAcceptedRouteAnnouncement,
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
    onSetFilterDraftOnlyOnRoute: (Boolean) -> Unit,
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
    onOpenCreate: () -> Unit = {},
    onActivateRoute: (GeoPoint, GeoPoint, String, String) -> Unit = { _, _, _, _ -> },
    onBuildRouteFromDraft: () -> Unit = {},
    onDeactivateRoute: () -> Unit = {},
    onUpdateRouteRadius: (Int) -> Unit = {},
    onUpdateRouteDraftStartAddress: (String) -> Unit = {},
    onUpdateRouteDraftEndAddress: (String) -> Unit = {},
    onSelectRouteAnnouncement: (String) -> Unit = {},
    onClearRouteSelection: () -> Unit = {},
    onAcceptRouteAnnouncement: (String) -> Unit = {},
    onRemoveAcceptedRouteAnnouncement: (String) -> Unit = {},
) {
    var nextMapCameraCommandId by remember { mutableStateOf(0L) }
    var mapCameraCommand by remember { mutableStateOf<DiscoveryMapCameraCommand?>(null) }

    fun issueMapCameraCommand(kind: DiscoveryMapCameraCommandKind) {
        nextMapCameraCommandId += 1
        mapCameraCommand = DiscoveryMapCameraCommand(
            id = nextMapCameraCommandId,
            kind = kind,
        )
    }

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
                        cameraCommand = mapCameraCommand,
                        onCameraCommandHandled = { commandId ->
                            if (mapCameraCommand?.id == commandId) {
                                mapCameraCommand = null
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    )
                }

                // Top overlay: search bar + list button
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
                                onClick = onToggleContentMode,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.List,
                                    contentDescription = stringResource(R.string.discovery_switch_to_list),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
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
                        onClick = { issueMapCameraCommand(DiscoveryMapCameraCommandKind.ZoomIn) },
                    )
                    MapActionButton(
                        icon = Icons.Filled.Remove,
                        contentDescription = stringResource(R.string.discovery_zoom_out),
                        onClick = { issueMapCameraCommand(DiscoveryMapCameraCommandKind.ZoomOut) },
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
            DiscoveryFiltersDialog(
                draft = state.filterDraft,
                routeState = state.routeState,
                routeAnnouncements = state.mapAnnouncements,
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
                onSetOnlyOnRoute = onSetFilterDraftOnlyOnRoute,
                onActivateRoute = onActivateRoute,
                onBuildRouteFromDraft = onBuildRouteFromDraft,
                onDeactivateRoute = onDeactivateRoute,
                onUpdateRouteRadius = onUpdateRouteRadius,
                onUpdateRouteDraftStartAddress = onUpdateRouteDraftStartAddress,
                onUpdateRouteDraftEndAddress = onUpdateRouteDraftEndAddress,
                onSelectRouteAnnouncement = onSelectRouteAnnouncement,
                onAcceptRouteAnnouncement = onAcceptRouteAnnouncement,
                onRemoveAcceptedRouteAnnouncement = onRemoveAcceptedRouteAnnouncement,
                onOpenAnnouncementDetails = onOpenAnnouncementDetails,
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
            CustomOfferPriceSheet(
                announcement = detailsState.item?.announcement,
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

/**
 * MapView wrapper that fixes the GL surface sizing issue when embedded in Compose AndroidView.
 *
 * Key fix: wrap MapView in a FrameLayout and delay onStart() until after the first layout pass.
 * This ensures the GL rendering surface initializes at the correct size, preventing the
 * white grid pattern that appeared in the top ~40% of the map.
 *
 * Also supports drawing route polylines, preview branches, and markers for "по пути" feature.
 */
private data class DiscoveryMapCameraCommand(
    val id: Long,
    val kind: DiscoveryMapCameraCommandKind,
)

private enum class DiscoveryMapCameraCommandKind {
    ZoomIn,
    ZoomOut,
}

private enum class DiscoveryMarkerVisualStyle {
    DefaultPill,
    RouteMatch,
    AcceptedWaypoint,
    Start,
    Finish,
    Selected,
    RegularDot,
}

private enum class DiscoveryMarkerCategoryIcon {
    Pickup,
    Buy,
    Carry,
    Ride,
    ProHelp,
    Other,
}

@Composable
private fun YandexMapCanvas(
    state: DiscoveryUiState,
    onOpenAnnouncementDetails: (String) -> Unit,
    onMapFocusConsumed: (Long) -> Unit,
    cameraCommand: DiscoveryMapCameraCommand?,
    onCameraCommandHandled: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Holder for map collections (mirrors googlemapstest architecture)
    val mapCollections = remember { MapCollectionsHolder() }
    val markerIconCache = remember { mutableMapOf<String, ImageProvider>() }

    val containerAndMapView = remember {
        val mv = createMovableYandexMapView(context)
        val container = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            addView(
                mv,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                ),
            )
        }
        Pair(container, mv)
    }
    val container = containerAndMapView.first
    val mapView = containerAndMapView.second

    var isMapReady by remember { mutableStateOf(false) }
    var isMapStarted by remember { mutableStateOf(false) }
    var mapContainerSize by remember { mutableStateOf(IntSize.Zero) }
    var mapCameraZoom by remember { mutableStateOf(10f) }

    // Manage lifecycle: start MapKit only after the GL surface has a real size.
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        var disposed = false
        var cameraListenerAttached = false
        val cameraRefreshListener = CameraListener { _, cameraPosition, _, finished ->
            if (finished) {
                mapCameraZoom = cameraPosition.zoom
                YandexMapKitLifecycle.refreshSurface(mapView, resyncCamera = false)
            }
        }

        fun initializeMapObjectsIfNeeded() {
            if (mapCollections.markerCollection != null) return

            val map = mapView.mapWindow.map
            val rootCollection = map.mapObjects
            mapCollections.routeCollection = rootCollection.addCollection()
            mapCollections.previewBranchCollection = rootCollection.addCollection()
            mapCollections.selectedBranchCollection = rootCollection.addCollection()
            mapCollections.markerCollection = rootCollection.addCollection()

            map.move(
                CameraPosition(
                    Point(55.751244, 37.618423),
                    10f,
                    0f,
                    0f,
                ),
            )
        }

        fun attachCameraRefreshListenerIfNeeded() {
            if (cameraListenerAttached) return
            mapView.mapWindow.map.addCameraListener(cameraRefreshListener)
            cameraListenerAttached = true
        }

        fun refreshMapSurface() {
            YandexMapKitLifecycle.refreshSurface(mapView)
        }

        fun startMapIfPossible(): Boolean {
            if (disposed) return false
            if (isMapStarted) return true
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return false
            if (mapView.width <= 0 || mapView.height <= 0) return false

            YandexMapKitLifecycle.start(mapView)
            isMapStarted = true
            initializeMapObjectsIfNeeded()
            attachCameraRefreshListenerIfNeeded()
            isMapReady = true
            mapCameraZoom = mapView.mapWindow.map.cameraPosition.zoom
            refreshMapSurface()
            return true
        }

        lateinit var layoutListener: ViewTreeObserver.OnGlobalLayoutListener
        layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (startMapIfPossible()) {
                    mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        }
        mapView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (startMapIfPossible()) {
                        mapView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    if (isMapStarted) {
                        isMapReady = false
                        YandexMapKitLifecycle.stop(mapView)
                        isMapStarted = false
                    }
                }
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        mapView.post {
            if (startMapIfPossible()) {
                mapView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            }
        }
        onDispose {
            disposed = true
            mapView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            lifecycle.removeObserver(observer)
            if (cameraListenerAttached) {
                runCatching { mapView.mapWindow.map.removeCameraListener(cameraRefreshListener) }
                cameraListenerAttached = false
            }
            if (isMapStarted) {
                isMapReady = false
                YandexMapKitLifecycle.stop(mapView)
                isMapStarted = false
            }
        }
    }

    // Update markers when announcements change
    LaunchedEffect(
        state.mapAnnouncements,
        state.routeState.currentRoute,
        state.routeState.matchedAnnouncements,
        state.routeState.acceptedAnnouncementIds,
        state.routeState.selectedAnnouncementId,
        state.detailsState.announcementId,
        mapCameraZoom,
        isMapReady,
    ) {
        if (!isMapReady) return@LaunchedEffect
        val markerCollection = mapCollections.markerCollection ?: return@LaunchedEffect
        markerCollection.clear()
        mapCollections.markerTapListeners.clear()

        val routeActive = state.routeState.hasRoute
        val matchedById = state.routeState.matchedAnnouncements.associateBy { it.item.announcementId }
        val matchedIds = matchedById.keys

        if (routeActive) {
            state.routeState.currentRoute?.points?.firstOrNull()?.let { startPoint ->
                addDiscoveryMarker(
                    collection = markerCollection,
                    iconCache = markerIconCache,
                    tapListeners = mapCollections.markerTapListeners,
                    point = startPoint,
                    markerId = "route-start",
                    visualStyle = DiscoveryMarkerVisualStyle.Start,
                    text = "A",
                    zIndex = 40f,
                    onTap = null,
                )
            }
            state.routeState.currentRoute?.points?.lastOrNull()?.let { endPoint ->
                addDiscoveryMarker(
                    collection = markerCollection,
                    iconCache = markerIconCache,
                    tapListeners = mapCollections.markerTapListeners,
                    point = endPoint,
                    markerId = "route-finish",
                    visualStyle = DiscoveryMarkerVisualStyle.Finish,
                    text = "B",
                    zIndex = 40f,
                    onTap = null,
                )
            }

            state.routeState.acceptedAnnouncementIds.forEachIndexed { index, announcementId ->
                val waypoint = matchedById[announcementId]?.item?.point ?: return@forEachIndexed
                val label = listOf("C", "D").getOrElse(index) { "C${index + 1}" }
                addDiscoveryMarker(
                    collection = markerCollection,
                    iconCache = markerIconCache,
                    tapListeners = mapCollections.markerTapListeners,
                    point = waypoint,
                    markerId = announcementId,
                    visualStyle = DiscoveryMarkerVisualStyle.AcceptedWaypoint,
                    text = label,
                    zIndex = 36f,
                    onTap = { onOpenAnnouncementDetails(announcementId) },
                )
            }
        }

        if (!routeActive) {
            val clusters = buildDiscoveryMarkerClusters(
                items = state.mapAnnouncements,
                zoom = mapCameraZoom,
            )
            clusters.forEach { cluster ->
                if (cluster.items.size > 1) {
                    addDiscoveryClusterMarker(
                        collection = markerCollection,
                        iconCache = markerIconCache,
                        tapListeners = mapCollections.markerTapListeners,
                        point = cluster.point,
                        count = cluster.items.size,
                        onTap = {
                            val map = mapView.mapWindow.map
                            val current = map.cameraPosition
                            val zoomStep = when {
                                cluster.items.size >= 100 -> 2.4f
                                cluster.items.size >= 20 -> 2.0f
                                else -> 1.45f
                            }
                            map.move(
                                CameraPosition(
                                    Point(cluster.point.latitude, cluster.point.longitude),
                                    (current.zoom + zoomStep).coerceIn(3f, 20f),
                                    current.azimuth,
                                    current.tilt,
                                ),
                                Animation(Animation.Type.SMOOTH, 0.35f),
                                null,
                            )
                        },
                    )
                } else {
                    val item = cluster.items.first()
                    val markerText = item.budgetText?.take(14)
                        ?: item.announcement.title.take(14).ifBlank { "Задача" }
                    addDiscoveryMarker(
                        collection = markerCollection,
                        iconCache = markerIconCache,
                        tapListeners = mapCollections.markerTapListeners,
                        point = cluster.point,
                        markerId = item.announcement.id,
                        visualStyle = if (item.announcement.id == state.detailsState.announcementId) {
                            DiscoveryMarkerVisualStyle.Selected
                        } else {
                            DiscoveryMarkerVisualStyle.DefaultPill
                        },
                        text = markerText,
                        categoryIcon = item.announcement.discoveryMarkerCategoryIcon(),
                        zIndex = if (item.announcement.id == state.detailsState.announcementId) 34f else 16f,
                        onTap = { onOpenAnnouncementDetails(item.announcement.id) },
                    )
                }
            }
            return@LaunchedEffect
        }

        state.mapAnnouncements.forEach { item ->
            val geoPoint = item.point ?: return@forEach
            if (routeActive && state.routeState.acceptedAnnouncementIds.contains(item.announcement.id)) {
                return@forEach
            }

            val visualStyle = when {
                item.announcement.id == state.routeState.selectedAnnouncementId ||
                    item.announcement.id == state.detailsState.announcementId ->
                    DiscoveryMarkerVisualStyle.Selected

                routeActive && matchedIds.contains(item.announcement.id) ->
                    DiscoveryMarkerVisualStyle.RouteMatch

                routeActive -> DiscoveryMarkerVisualStyle.RegularDot

                else -> DiscoveryMarkerVisualStyle.DefaultPill
            }
            val markerText = when (visualStyle) {
                DiscoveryMarkerVisualStyle.RegularDot -> ""
                else -> item.budgetText?.take(14)
                    ?: item.announcement.title.take(14).ifBlank { "Задача" }
            }
            addDiscoveryMarker(
                collection = markerCollection,
                iconCache = markerIconCache,
                tapListeners = mapCollections.markerTapListeners,
                point = geoPoint,
                markerId = item.announcement.id,
                visualStyle = visualStyle,
                text = markerText,
                categoryIcon = item.announcement.discoveryMarkerCategoryIcon(),
                zIndex = when (visualStyle) {
                    DiscoveryMarkerVisualStyle.Selected -> 34f
                    DiscoveryMarkerVisualStyle.AcceptedWaypoint -> 36f
                    DiscoveryMarkerVisualStyle.Start,
                    DiscoveryMarkerVisualStyle.Finish,
                    -> 40f

                    DiscoveryMarkerVisualStyle.RouteMatch -> 24f
                    DiscoveryMarkerVisualStyle.DefaultPill -> 16f
                    DiscoveryMarkerVisualStyle.RegularDot -> 12f
                },
                onTap = { onOpenAnnouncementDetails(item.announcement.id) },
            )
        }
    }

    // Draw route polyline
    LaunchedEffect(state.routeState.currentRoute, isMapReady) {
        if (!isMapReady) return@LaunchedEffect
        val routeCollection = mapCollections.routeCollection ?: return@LaunchedEffect
        routeCollection.clear()
        val route = state.routeState.currentRoute ?: return@LaunchedEffect
        if (route.points.size < 2) return@LaunchedEffect

        val polyline = routeCollection.addPolyline(
            Polyline(route.points.map { Point(it.latitude, it.longitude) }),
        )
        polyline.setStrokeColor(0xFF2BB7A7.toInt())
        polyline.outlineColor = 0xFFA6E3DB.toInt()
        polyline.outlineWidth = 2f
        polyline.strokeWidth = 6f
        polyline.zIndex = 5f
    }

    // Draw preview branches (dashed lines to nearby announcements)
    LaunchedEffect(
        state.routeState.previewBranches,
        state.routeState.selectedAnnouncementId,
        state.routeState.acceptedAnnouncementIds,
        isMapReady,
    ) {
        if (!isMapReady) return@LaunchedEffect
        val previewCollection = mapCollections.previewBranchCollection ?: return@LaunchedEffect
        previewCollection.clear()

        state.routeState.previewBranches
            .filter {
                it.item.announcementId != state.routeState.selectedAnnouncementId &&
                    !state.routeState.acceptedAnnouncementIds.contains(it.item.announcementId)
            }
            .forEach { preview ->
                val polyline = previewCollection.addPolyline(
                    Polyline(preview.directBranchPoints.map { Point(it.latitude, it.longitude) }),
                )
                polyline.setStrokeColor(0xFFF7B290.toInt())
                polyline.outlineColor = 0x00000000
                polyline.strokeWidth = 4f
                polyline.dashLength = 16f
                polyline.gapLength = 12f
                polyline.zIndex = 2f
            }
    }

    // Draw selected branch (solid orange line)
    LaunchedEffect(state.routeState.selectedBranchPoints, isMapReady) {
        if (!isMapReady) return@LaunchedEffect
        val selectedCollection = mapCollections.selectedBranchCollection ?: return@LaunchedEffect
        selectedCollection.clear()
        val points = state.routeState.selectedBranchPoints
        if (points.size < 2) return@LaunchedEffect

        val polyline = selectedCollection.addPolyline(
            Polyline(points.map { Point(it.latitude, it.longitude) }),
        )
        polyline.setStrokeColor(0xFFF08A63.toInt())
        polyline.outlineColor = 0xFFFFFFFF.toInt()
        polyline.outlineWidth = 2f
        polyline.strokeWidth = 5f
        polyline.zIndex = 4f
    }

    // Handle viewport changes
    LaunchedEffect(state.mapViewport, state.routeState.isActive, isMapReady) {
        if (!isMapReady) return@LaunchedEffect
        if (state.routeState.hasRoute) return@LaunchedEffect
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

    LaunchedEffect(
        state.routeState.currentRoute,
        state.routeState.selectedBranchPoints,
        state.routeState.selectedAnnouncementId,
        state.routeState.acceptedAnnouncementIds,
        isMapReady,
    ) {
        if (!isMapReady) return@LaunchedEffect
        if (!state.routeState.hasRoute) return@LaunchedEffect
        fitDiscoveryRouteContent(
            mapView = mapView,
            routeState = state.routeState,
        )
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

    LaunchedEffect(cameraCommand?.id, isMapReady) {
        val command = cameraCommand ?: return@LaunchedEffect
        if (!isMapReady) return@LaunchedEffect

        val map = mapView.mapWindow.map
        val current = map.cameraPosition
        val targetZoom = when (command.kind) {
            DiscoveryMapCameraCommandKind.ZoomIn -> current.zoom + 0.9f
            DiscoveryMapCameraCommandKind.ZoomOut -> current.zoom - 0.9f
        }.coerceIn(3f, 20f)

        map.move(
            CameraPosition(
                current.target,
                targetZoom,
                current.azimuth,
                current.tilt,
            ),
            Animation(Animation.Type.SMOOTH, 0.3f),
            null,
        )
        onCameraCommandHandled(command.id)
    }

    LaunchedEffect(isMapReady, mapContainerSize) {
        if (!isMapReady || mapContainerSize == IntSize.Zero) return@LaunchedEffect
        YandexMapKitLifecycle.refreshSurface(mapView)
    }

    AndroidView(
        factory = { container },
        modifier = modifier.onSizeChanged { size ->
            if (size.width > 0 && size.height > 0 && size != mapContainerSize) {
                mapContainerSize = size
            }
        },
        update = { view ->
            YandexMapKitLifecycle.refreshSurface(mapView)
            view.invalidate()
        },
    )
}

/** Holds references to separated map object collections for layered rendering. */
private class MapCollectionsHolder {
    var routeCollection: MapObjectCollection? = null
    var previewBranchCollection: MapObjectCollection? = null
    var selectedBranchCollection: MapObjectCollection? = null
    var markerCollection: MapObjectCollection? = null
    val markerTapListeners: MutableList<MapObjectTapListener> = mutableListOf()
}

private data class DiscoveryMarkerCluster(
    val point: GeoPoint,
    val items: List<DiscoveryAnnouncementItemUi>,
)

private fun buildDiscoveryMarkerClusters(
    items: List<DiscoveryAnnouncementItemUi>,
    zoom: Float,
): List<DiscoveryMarkerCluster> {
    val mapItems = items.filter { it.point != null }
    if (mapItems.size <= 1 || zoom >= 16.7f) {
        return mapItems.mapNotNull { item ->
            item.point?.let { point -> DiscoveryMarkerCluster(point = point, items = listOf(item)) }
        }
    }

    val density = android.content.res.Resources.getSystem().displayMetrics.density
    val cellSizePx = when {
        zoom < 9f -> 260f * density
        zoom < 11f -> 170f * density
        zoom < 13f -> 126f * density
        zoom < 15f -> 104f * density
        else -> 82f * density
    }

    return mapItems
        .groupBy { item ->
            val point = item.point ?: return@groupBy "empty"
            val projected = point.projectToWorldPixels(zoom)
            "${floor(projected.first / cellSizePx).toInt()}|${floor(projected.second / cellSizePx).toInt()}"
        }
        .values
        .map { clusteredItems ->
            val points = clusteredItems.mapNotNull(DiscoveryAnnouncementItemUi::point)
            DiscoveryMarkerCluster(
                point = GeoPoint(
                    latitude = points.sumOf(GeoPoint::latitude) / points.size,
                    longitude = points.sumOf(GeoPoint::longitude) / points.size,
                ),
                items = clusteredItems,
            )
        }
}

private fun GeoPoint.projectToWorldPixels(zoom: Float): Pair<Double, Double> {
    val sinLatitude = sin(latitude.coerceIn(-85.05112878, 85.05112878) * PI / 180.0)
    val scale = 256.0 * 2.0.pow(zoom.toDouble())
    val x = (longitude + 180.0) / 360.0 * scale
    val y = (0.5 - ln((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * PI)) * scale
    return x to y
}

private fun addDiscoveryMarker(
    collection: MapObjectCollection,
    iconCache: MutableMap<String, ImageProvider>,
    tapListeners: MutableList<MapObjectTapListener>,
    point: GeoPoint,
    markerId: String,
    visualStyle: DiscoveryMarkerVisualStyle,
    text: String,
    categoryIcon: DiscoveryMarkerCategoryIcon? = null,
    zIndex: Float,
    onTap: (() -> Unit)?,
) {
    val cacheKey = "${visualStyle.name}|$text|${categoryIcon?.name.orEmpty()}"
    val placemark = collection.addPlacemark(
        Point(point.latitude, point.longitude),
        iconCache.getOrPut(cacheKey) {
            ImageProvider.fromBitmap(
                createDiscoveryMarkerBitmap(
                    text = text,
                    style = visualStyle,
                    categoryIcon = categoryIcon,
                ),
            )
        },
    )
    placemark.zIndex = zIndex
    placemark.userData = markerId
    if (onTap != null) {
        val listener = MapObjectTapListener { _, _ ->
            onTap()
            true
        }
        tapListeners.add(listener)
        placemark.addTapListener(listener)
    }
}

private fun addDiscoveryClusterMarker(
    collection: MapObjectCollection,
    iconCache: MutableMap<String, ImageProvider>,
    tapListeners: MutableList<MapObjectTapListener>,
    point: GeoPoint,
    count: Int,
    onTap: () -> Unit,
) {
    val cacheKey = "cluster|$count"
    val placemark = collection.addPlacemark(
        Point(point.latitude, point.longitude),
        iconCache.getOrPut(cacheKey) {
            ImageProvider.fromBitmap(createDiscoveryClusterBitmap(count))
        },
    )
    placemark.zIndex = 28f
    val listener = MapObjectTapListener { _, _ ->
        onTap()
        true
    }
    tapListeners.add(listener)
    placemark.addTapListener(listener)
}

private fun Announcement.discoveryMarkerCategoryIcon(): DiscoveryMarkerCategoryIcon {
    structuredData.actionType?.let { action ->
        return when (action) {
            AnnouncementStructuredData.ActionType.Pickup -> DiscoveryMarkerCategoryIcon.Pickup
            AnnouncementStructuredData.ActionType.Buy -> DiscoveryMarkerCategoryIcon.Buy
            AnnouncementStructuredData.ActionType.Carry -> DiscoveryMarkerCategoryIcon.Carry
            AnnouncementStructuredData.ActionType.Ride -> DiscoveryMarkerCategoryIcon.Ride
            AnnouncementStructuredData.ActionType.ProHelp -> DiscoveryMarkerCategoryIcon.ProHelp
            AnnouncementStructuredData.ActionType.Other -> DiscoveryMarkerCategoryIcon.Other
        }
    }

    return when (structuredData.resolvedCategory) {
        AnnouncementStructuredData.ResolvedCategory.PickupPoint,
        AnnouncementStructuredData.ResolvedCategory.Handoff,
        -> DiscoveryMarkerCategoryIcon.Pickup

        AnnouncementStructuredData.ResolvedCategory.Buy -> DiscoveryMarkerCategoryIcon.Buy
        AnnouncementStructuredData.ResolvedCategory.Carry -> DiscoveryMarkerCategoryIcon.Carry
        AnnouncementStructuredData.ResolvedCategory.Ride -> DiscoveryMarkerCategoryIcon.Ride
        AnnouncementStructuredData.ResolvedCategory.ProHelp -> DiscoveryMarkerCategoryIcon.ProHelp
        AnnouncementStructuredData.ResolvedCategory.Other,
        null,
        -> when (category.trim().lowercase()) {
            "delivery" -> DiscoveryMarkerCategoryIcon.Pickup
            "help", "errands" -> DiscoveryMarkerCategoryIcon.ProHelp
            else -> DiscoveryMarkerCategoryIcon.Other
        }
    }
}

private fun createDiscoveryMarkerBitmap(
    text: String,
    style: DiscoveryMarkerVisualStyle,
    categoryIcon: DiscoveryMarkerCategoryIcon? = null,
): android.graphics.Bitmap {
    if (style == DiscoveryMarkerVisualStyle.RegularDot) {
        return createDiscoveryDotMarkerBitmap()
    }

    val density = android.content.res.Resources.getSystem().displayMetrics.density
    val displayText = text.trim().ifBlank {
        when (style) {
            DiscoveryMarkerVisualStyle.Start -> "A"
            DiscoveryMarkerVisualStyle.Finish -> "B"
            DiscoveryMarkerVisualStyle.AcceptedWaypoint -> "C"
            else -> "Задача"
        }
    }
    val isRoundEndpoint = style == DiscoveryMarkerVisualStyle.Start ||
        style == DiscoveryMarkerVisualStyle.Finish ||
        style == DiscoveryMarkerVisualStyle.AcceptedWaypoint
    val textSize = if (isRoundEndpoint) 13f * density else 12f * density
    val horizontalPadding = if (isRoundEndpoint) 12f * density else 14f * density
    val verticalPadding = if (isRoundEndpoint) 12f * density else 9f * density
    val cornerRadius = if (isRoundEndpoint) 999f else 18f * density

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = when (style) {
            DiscoveryMarkerVisualStyle.DefaultPill -> 0xFF1C1C1E.toInt()
            DiscoveryMarkerVisualStyle.RouteMatch,
            DiscoveryMarkerVisualStyle.AcceptedWaypoint,
            DiscoveryMarkerVisualStyle.Start,
            DiscoveryMarkerVisualStyle.Finish,
            DiscoveryMarkerVisualStyle.Selected,
            DiscoveryMarkerVisualStyle.RegularDot,
            -> 0xFFFFFFFF.toInt()
        }
    }

    val textWidth = textPaint.measureText(displayText)
    val textHeight = textPaint.fontMetrics.let { it.descent - it.ascent }
    val minEndpointSize = (44 * density).toInt()
    val shouldDrawCategoryIcon = categoryIcon != null && !isRoundEndpoint
    val iconSize = if (shouldDrawCategoryIcon) 18f * density else 0f
    val iconGap = if (shouldDrawCategoryIcon) 7f * density else 0f
    val contentWidth = textWidth + iconGap + iconSize
    val bitmapWidth = if (isRoundEndpoint) {
        maxOf(
            minEndpointSize,
            (textWidth + horizontalPadding * 2).toInt(),
        )
    } else {
        (contentWidth + horizontalPadding * 2).toInt().coerceAtLeast((88 * density).toInt())
    }
    val bitmapHeight = if (isRoundEndpoint) {
        maxOf(
            minEndpointSize,
            (textHeight + verticalPadding * 2).toInt(),
        )
    } else {
        (textHeight + verticalPadding * 2).toInt().coerceAtLeast((38 * density).toInt())
    }

    val bitmap = android.graphics.Bitmap.createBitmap(
        bitmapWidth,
        bitmapHeight,
        android.graphics.Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(bitmap)

    val fillColor = when (style) {
        DiscoveryMarkerVisualStyle.DefaultPill -> 0xFFFFFFFF.toInt()
        DiscoveryMarkerVisualStyle.RouteMatch -> 0xFF2BB7A7.toInt()
        DiscoveryMarkerVisualStyle.AcceptedWaypoint -> 0xFF2BB673.toInt()
        DiscoveryMarkerVisualStyle.Start -> 0xFF1B82E2.toInt()
        DiscoveryMarkerVisualStyle.Finish -> 0xFFE75757.toInt()
        DiscoveryMarkerVisualStyle.Selected -> 0xFFF2994A.toInt()
        DiscoveryMarkerVisualStyle.RegularDot -> 0xFF7A878D.toInt()
    }
    val strokeColor = when (style) {
        DiscoveryMarkerVisualStyle.DefaultPill -> 0x1F2BB7A7
        DiscoveryMarkerVisualStyle.RouteMatch -> 0xFF2BB7A7.toInt()
        DiscoveryMarkerVisualStyle.AcceptedWaypoint,
        DiscoveryMarkerVisualStyle.Start,
        DiscoveryMarkerVisualStyle.Finish,
        DiscoveryMarkerVisualStyle.Selected -> 0xFFF2994A.toInt()
        DiscoveryMarkerVisualStyle.RegularDot -> 0xFFFFFFFF.toInt()
    }

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor
        this.style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }

    val rect = RectF(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)

    val contentLeft = (bitmapWidth - contentWidth) / 2f
    val textX = if (shouldDrawCategoryIcon) {
        contentLeft + iconSize + iconGap
    } else {
        contentLeft
    }
    val textY = bitmapHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
    if (categoryIcon != null && shouldDrawCategoryIcon) {
        val iconColor = when (style) {
            DiscoveryMarkerVisualStyle.DefaultPill -> 0xFF6B7280.toInt()
            else -> 0xFFFFFFFF.toInt()
        }
        drawDiscoveryCategoryIcon(
            canvas = canvas,
            icon = categoryIcon,
            centerX = contentLeft + iconSize / 2f,
            centerY = bitmapHeight / 2f,
            size = iconSize,
            color = iconColor,
        )
    }
    canvas.drawText(displayText, textX, textY, textPaint)

    return bitmap
}

private fun createDiscoveryClusterBitmap(count: Int): android.graphics.Bitmap {
    val density = android.content.res.Resources.getSystem().displayMetrics.density
    val sizeDp = when {
        count >= 1_000 -> 78f
        count >= 100 -> 68f
        count >= 10 -> 58f
        else -> 50f
    }
    val size = (sizeDp * density).toInt().coerceAtLeast(50)
    val bitmap = android.graphics.Bitmap.createBitmap(
        size,
        size,
        android.graphics.Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(bitmap)
    val radius = size / 2f
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF7F3E9.toInt()
    }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 3.2f * density
    }
    canvas.drawCircle(radius, radius, radius - stroke.strokeWidth / 2f, fill)
    canvas.drawCircle(radius, radius, radius - stroke.strokeWidth / 2f, stroke)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF111111.toInt()
        textAlign = Paint.Align.CENTER
        textSize = when {
            count >= 1_000 -> 18f * density
            count >= 100 -> 17f * density
            else -> 16f * density
        }
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val label = count.toString()
    val textY = radius - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(label, radius, textY, textPaint)
    return bitmap
}

private fun drawDiscoveryCategoryIcon(
    canvas: Canvas,
    icon: DiscoveryMarkerCategoryIcon,
    centerX: Float,
    centerY: Float,
    size: Float,
    color: Int,
) {
    val imageVector = icon.imageVector()
    val viewportWidth = imageVector.viewportWidth.takeIf { it > 0f } ?: 24f
    val viewportHeight = imageVector.viewportHeight.takeIf { it > 0f } ?: 24f
    val scale = size / maxOf(viewportWidth, viewportHeight)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    canvas.save()
    canvas.translate(centerX - viewportWidth * scale / 2f, centerY - viewportHeight * scale / 2f)
    canvas.scale(scale, scale)
    drawImageVectorGroup(
        canvas = canvas,
        group = imageVector.root,
        parser = PathParser(),
        paint = paint,
    )
    canvas.restore()
}

private fun DiscoveryMarkerCategoryIcon.imageVector(): ImageVector = when (this) {
    DiscoveryMarkerCategoryIcon.Pickup -> Icons.Outlined.Inventory2
    DiscoveryMarkerCategoryIcon.Buy -> Icons.Outlined.ShoppingCart
    DiscoveryMarkerCategoryIcon.Carry -> Icons.Outlined.OpenWith
    DiscoveryMarkerCategoryIcon.Ride -> Icons.Outlined.DirectionsCar
    DiscoveryMarkerCategoryIcon.ProHelp -> Icons.Outlined.Build
    DiscoveryMarkerCategoryIcon.Other -> Icons.Outlined.AutoAwesome
}

private fun drawImageVectorGroup(
    canvas: Canvas,
    group: VectorGroup,
    parser: PathParser,
    paint: Paint,
) {
    canvas.save()
    canvas.translate(group.translationX + group.pivotX, group.translationY + group.pivotY)
    canvas.rotate(group.rotation)
    canvas.scale(group.scaleX, group.scaleY)
    canvas.translate(-group.pivotX, -group.pivotY)

    for (node in group) {
        when (node) {
            is VectorGroup -> drawImageVectorGroup(canvas, node, parser, paint)
            is VectorPath -> {
                val originalAlpha = paint.alpha
                paint.alpha = (255f * node.fillAlpha).toInt().coerceIn(0, 255)
                parser.clear()
                val path = parser
                    .addPathNodes(node.pathData)
                    .toPath()
                    .asAndroidPath()
                canvas.drawPath(path, paint)
                paint.alpha = originalAlpha
            }
        }
    }
    canvas.restore()
}

private fun createDiscoveryDotMarkerBitmap(): android.graphics.Bitmap {
    val density = android.content.res.Resources.getSystem().displayMetrics.density
    val size = (28f * density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(
        size,
        size,
        android.graphics.Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(bitmap)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF7A878D.toInt()
    }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
    }
    val radius = size / 2f
    canvas.drawCircle(radius, radius, radius - 2.5f * density, fill)
    canvas.drawCircle(radius, radius, radius - 2.5f * density, stroke)
    return bitmap
}

private fun fitDiscoveryRouteContent(
    mapView: MapView,
    routeState: RouteState,
) {
    val points = buildList {
        addAll(routeState.currentRoute?.points.orEmpty())
        addAll(routeState.selectedBranchPoints)
    }.distinctBy { point -> "${point.latitude}|${point.longitude}" }

    if (points.isEmpty()) return

    if (points.size == 1) {
        mapView.mapWindow.map.move(
            CameraPosition(
                Point(points.first().latitude, points.first().longitude),
                14f,
                0f,
                0f,
            ),
            Animation(Animation.Type.SMOOTH, 0.5f),
            null,
        )
        return
    }

    val minLat = points.minOf { it.latitude }
    val maxLat = points.maxOf { it.latitude }
    val minLon = points.minOf { it.longitude }
    val maxLon = points.maxOf { it.longitude }
    val center = Point(
        (minLat + maxLat) / 2.0,
        (minLon + maxLon) / 2.0,
    )
    val span = maxOf(maxLat - minLat, maxLon - minLon)
    val zoom = when {
        span < 0.004 -> 15.6f
        span < 0.008 -> 14.8f
        span < 0.015 -> 14.0f
        span < 0.03 -> 13.2f
        span < 0.06 -> 12.3f
        else -> 11.4f
    }

    mapView.mapWindow.map.move(
        CameraPosition(center, zoom, 0f, 0f),
        Animation(Animation.Type.SMOOTH, 0.55f),
        null,
    )
}

// -- Route "По пути" UI --

/**
 * Row inside the bottom floating card that lets user activate route mode.
 * Uses the first two announcements with geo points as A→B for demo purposes.
 */
@Composable
private fun RouteActivationRow(
    announcements: List<DiscoveryAnnouncementItemUi>,
    onActivateRoute: (GeoPoint, GeoPoint, String, String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Use first two map announcements as start/end for route demo
                val pointA = announcements.getOrNull(0)
                val pointB = announcements.getOrNull(1)
                if (pointA?.point != null && pointB?.point != null) {
                    onActivateRoute(
                        pointA.point,
                        pointB.point,
                        pointA.sourceAddress ?: "Точка A",
                        pointB.sourceAddress ?: "Точка B",
                    )
                }
            }
            .padding(MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF2196F3).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.NearMe,
                contentDescription = null,
                tint = Color(0xFF2196F3),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.discovery_route_along),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.discovery_route_along_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Surface(
            shape = CircleShape,
            color = Color(0xFF2196F3),
        ) {
            Icon(
                modifier = Modifier.padding(MaterialTheme.spacing.medium),
                imageVector = Icons.Outlined.NearMe,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

/**
 * Bottom panel shown when route is active. Shows route info, radius selector,
 * and matched announcements list.
 */
@Composable
private fun RouteBottomPanel(
    routeState: RouteState,
    onDeactivateRoute: () -> Unit,
    onUpdateRadius: (Int) -> Unit,
    onSelectAnnouncement: (String) -> Unit,
    onAcceptAnnouncement: (String) -> Unit,
    onRemoveAccepted: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val radiusOptions = listOf(100, 300, 500, 1000)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp),
        ) {
            // Header: route info + close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.spacing.large,
                        end = MaterialTheme.spacing.medium,
                        top = MaterialTheme.spacing.large,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.discovery_route_along),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (routeState.statusMessage.isNotBlank()) {
                        Text(
                            text = routeState.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val route = routeState.currentRoute
                    if (route != null) {
                        Text(
                            text = "${formatDistance(route.distanceMeters)} • ${formatDuration(route.durationSeconds)} • По пути: ${routeState.nonAcceptedMatchCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (routeState.isBuilding) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = MaterialTheme.spacing.small),
                        strokeWidth = 2.dp,
                    )
                }

                TextButton(onClick = onDeactivateRoute) {
                    Text(
                        text = stringResource(R.string.discovery_route_close),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // Radius selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.small),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                Text(
                    text = stringResource(R.string.discovery_route_radius),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                radiusOptions.forEach { radius ->
                    val selected = routeState.radiusMeters == radius
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onUpdateRadius(radius) },
                        shape = RoundedCornerShape(999.dp),
                        color = if (selected) Color(0xFF2196F3) else MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = "${radius}м",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            HorizontalDivider()

            // Matched announcements list
            if (routeState.matchedAnnouncements.isEmpty() && !routeState.isBuilding) {
                Text(
                    text = stringResource(R.string.discovery_route_no_tasks),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.large),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = MaterialTheme.spacing.large,
                        vertical = MaterialTheme.spacing.small,
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    // Accepted announcements first
                    val orderedAccepted = routeState.acceptedAnnouncementIds
                    orderedAccepted.forEachIndexed { index, announcementId ->
                        val matched = routeState.matchedAnnouncements.firstOrNull { it.item.announcementId == announcementId }
                        if (matched != null) {
                            item(key = "accepted_$announcementId") {
                                RouteMatchedAnnouncementCard(
                                    matched = matched,
                                    isAccepted = true,
                                    isSelected = routeState.selectedAnnouncementId == announcementId,
                                    waypointLabel = listOf("C", "D").getOrElse(index) { "C${index + 1}" },
                                    onSelect = { onSelectAnnouncement(announcementId) },
                                    onAcceptOrRemove = { onRemoveAccepted(announcementId) },
                                    onOpenDetails = { onOpenDetails(announcementId) },
                                    canAccept = false,
                                )
                            }
                        }
                    }

                    // Non-accepted matched announcements
                    val nonAccepted = routeState.previewBranches.filter {
                        !routeState.acceptedAnnouncementIds.contains(it.item.announcementId)
                    }
                    items(
                        items = nonAccepted,
                        key = { "branch_${it.item.announcementId}" },
                    ) { preview ->
                        val matched = routeState.matchedAnnouncements.firstOrNull {
                            it.item.announcementId == preview.item.announcementId
                        }
                        if (matched != null) {
                            RouteMatchedAnnouncementCard(
                                matched = matched,
                                isAccepted = false,
                                isSelected = routeState.selectedAnnouncementId == preview.item.announcementId,
                                waypointLabel = null,
                                onSelect = { onSelectAnnouncement(preview.item.announcementId) },
                                onAcceptOrRemove = { onAcceptAnnouncement(preview.item.announcementId) },
                                onOpenDetails = { onOpenDetails(preview.item.announcementId) },
                                canAccept = routeState.acceptedAnnouncementIds.size < 2,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteMatchedAnnouncementCard(
    matched: MatchedRouteAnnouncement,
    isAccepted: Boolean,
    isSelected: Boolean,
    waypointLabel: String?,
    onSelect: () -> Unit,
    onAcceptOrRemove: () -> Unit,
    onOpenDetails: () -> Unit,
    canAccept: Boolean,
) {
    val borderColor = when {
        isAccepted -> Color(0xFF4CAF50)
        isSelected -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val bgColor = when {
        isAccepted -> Color(0xFF4CAF50).copy(alpha = 0.08f)
        isSelected -> Color(0xFFFF9800).copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isAccepted && waypointLabel != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = waypointLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                )
                            }
                        }
                    }
                    Text(
                        text = matched.item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                matched.item.budgetText?.let { price ->
                    Text(
                        text = price,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            Text(
                text = "${matched.distanceToRouteMeters.toInt()} м от маршрута",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (matched.item.addressText.isNotBlank()) {
                Text(
                    text = matched.item.addressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onOpenDetails) {
                    Text(
                        text = "Подробнее",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                if (isAccepted) {
                    OutlinedButton(onClick = onAcceptOrRemove) {
                        Text(
                            text = stringResource(R.string.discovery_route_remove),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                } else if (canAccept) {
                    Button(
                        onClick = onAcceptOrRemove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.discovery_route_accept),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

private fun formatDistance(meters: Int): String {
    return if (meters >= 1000) {
        "${meters / 1000} км"
    } else {
        "$meters м"
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    return if (minutes >= 60) {
        "${minutes / 60} ч ${minutes % 60} мин"
    } else {
        "$minutes мин"
    }
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
                            bottom = ShellBottomBarContentPadding,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnnouncementListCard(
    item: DiscoveryAnnouncementItemUi,
    onOpenDetails: () -> Unit,
    onShowOnMap: () -> Unit,
) {
    val chipLabels = item.announcement.detailChipLabels()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onShowOnMap),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.84f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                item.previewImageUrl?.let { imageUrl ->
                    AnnouncementImage(
                        imageUrl = imageUrl,
                        modifier = Modifier.size(76.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.announcement.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        chipLabels.forEach { label ->
                            val isBudget = item.budgetText != null && label == item.budgetText
                            DiscoveryInfoChip(
                                label = label,
                                leadingIcon = if (isBudget) Icons.Outlined.Inventory2 else null,
                            )
                        }
                    }
                }
            }

            if (item.point != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onOpenDetails) {
                        Text(
                            text = "Подробнее",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    TextButton(onClick = onShowOnMap) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            modifier = Modifier.padding(start = 4.dp),
                            text = stringResource(R.string.discovery_show_on_map),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                        )
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
    routeState: RouteState,
    routeAnnouncements: List<DiscoveryAnnouncementItemUi>,
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
    onActivateRoute: (GeoPoint, GeoPoint, String, String) -> Unit,
    onDeactivateRoute: () -> Unit,
    onUpdateRouteRadius: (Int) -> Unit,
    onSelectRouteAnnouncement: (String) -> Unit,
    onAcceptRouteAnnouncement: (String) -> Unit,
    onRemoveAcceptedRouteAnnouncement: (String) -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
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

            FilterSection(title = stringResource(R.string.discovery_filters_and_route)) {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                    if (routeState.isActive) {
                        RouteBottomPanel(
                            routeState = routeState,
                            onDeactivateRoute = onDeactivateRoute,
                            onUpdateRadius = onUpdateRouteRadius,
                            onSelectAnnouncement = onSelectRouteAnnouncement,
                            onAcceptAnnouncement = onAcceptRouteAnnouncement,
                            onRemoveAccepted = onRemoveAcceptedRouteAnnouncement,
                            onOpenDetails = onOpenAnnouncementDetails,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        RouteActivationRow(
                            announcements = routeAnnouncements,
                            onActivateRoute = onActivateRoute,
                        )
                    }
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
                        PublicAnnouncementAddressSection(announcement = announcement)
                    }
                }

                item {
                    DetailsSectionCard(title = "Время") {
                        PublicAnnouncementTimeSection(announcement = announcement)
                    }
                }

                item {
                    DetailsSectionCard(title = "Бюджет") {
                        DetailValueRow(
                            label = "Сумма",
                            value = announcement.formattedBudgetText ?: "—",
                        )
                    }
                }

                item {
                    DetailsSectionCard(title = "Детали") {
                        PublicAnnouncementDetailsSection(
                            announcement = announcement,
                            imageCount = announcement.imageUrls(apiBaseUrl).size,
                        )
                    }
                }

                item {
                    DetailsSectionCard(title = "Контакты") {
                        val rows = publicContactSummaryRows(announcement)
                        if (rows.isEmpty()) {
                            DetailValueRow(label = "Контакты", value = "—")
                        } else {
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

            }

            DiscoveryResponseComposer(
                detailsState = detailsState,
                item = item,
                onOfferMessageChange = onOfferMessageChange,
                onOpenCustomPriceDialog = onOpenCustomPriceDialog,
                onSubmitQuickOffer = onSubmitQuickOffer,
                onDismiss = onDismiss,
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
    onDismiss: () -> Unit,
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
                        onClick = onDismiss,
                    )
                }

                item.responseGate == DiscoveryResponseGate.OwnAnnouncement -> {
                    ResponseStatusBanner(
                        title = "Это ваше объявление",
                        subtitle = "Отклики доступны только для других пользователей.",
                    )
                }

                item.responseGate == DiscoveryResponseGate.Unavailable -> {
                    ResponseStatusBanner(
                        title = "Отклики закрыты",
                        subtitle = "Задание уже принято или больше не доступно для новых исполнителей.",
                    )
                }

                item.responseGate == DiscoveryResponseGate.RequiresAuth -> {
                    ResponseStatusBanner(
                        title = "Нужна авторизация",
                        subtitle = "Войдите в аккаунт, чтобы откликнуться на объявление.",
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
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 62.dp),
                                enabled = !detailsState.isSubmitting,
                                onClick = onSubmitQuickOffer,
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                ),
                            ) {
                                if (detailsState.isSubmittingQuickOffer) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                    )
                                } else {
                                    Icon(imageVector = Icons.AutoMirrored.Outlined.Send, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Быстрый отклик",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                    Text(
                                        text = quickOfferPriceText,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 62.dp),
                            enabled = !detailsState.isSubmitting,
                            onClick = onOpenCustomPriceDialog,
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Своя цена")
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
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomOfferPriceSheet(
    announcement: Announcement?,
    priceInput: String,
    error: DiscoveryCustomPriceError?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onPriceChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val currentPrice = priceInput.filter(Char::isDigit).toIntOrNull() ?: 0
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = MaterialTheme.spacing.xLarge, vertical = MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Text(
                text = "Своя цена",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            announcement?.formattedBudgetText?.let { budgetText ->
                Text(
                    text = "Бюджет объявления: $budgetText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = {
                        val next = (currentPrice - 50).coerceAtLeast(0)
                        onPriceChange(next.takeIf { it > 0 }?.toString().orEmpty())
                    },
                    enabled = !isSubmitting && currentPrice > 0,
                ) {
                    Text(text = "−50")
                }

                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = priceInput,
                    onValueChange = onPriceChange,
                    label = { Text(text = "Сумма") },
                    supportingText = error?.let {
                        { Text(text = stringResource(R.string.discovery_offer_custom_error)) }
                    },
                    singleLine = true,
                )

                OutlinedButton(
                    onClick = {
                        val next = currentPrice + 50
                        onPriceChange(next.toString())
                    },
                    enabled = !isSubmitting,
                ) {
                    Text(text = "+50")
                }
            }

            Text(
                text = "Шаг изменения цены — 50 ₽.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting && currentPrice > 0,
                onClick = onSubmit,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = "Отправить отклик")
                }
            }
        }
    }
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
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
        model = imageUrl,
        contentDescription = stringResource(R.string.ads_image_preview_description),
        contentScale = ContentScale.Crop,
    )
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
private fun PublicAnnouncementAddressSection(announcement: Announcement) {
    val sourceAddress = announcement.primarySourceAddress
        ?: announcement.taskStringValue(
            paths = listOf(listOf("task", "route", "source", "address")),
            legacyKeys = listOf("address", "pickup_address", "source_address"),
        )
    val destinationAddress = announcement.primaryDestinationAddress
        ?: announcement.taskStringValue(
            paths = listOf(listOf("task", "route", "destination", "address")),
            legacyKeys = listOf("destination_address", "dropoff_address"),
        )

    val isDelivery = announcement.category.trim().lowercase() == "delivery"
    val hasDestination = !destinationAddress.isNullOrBlank()

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
        when {
            isDelivery -> {
                DetailValueRow(label = "Откуда", value = sourceAddress ?: "—")
                DetailValueRow(label = "Куда", value = destinationAddress ?: "—")
            }

            hasDestination -> {
                DetailValueRow(label = "Где", value = sourceAddress ?: "—")
                DetailValueRow(label = "Куда", value = destinationAddress ?: "—")
            }

            else -> {
                DetailValueRow(label = "Где", value = sourceAddress ?: "—")
            }
        }
    }
}

@Composable
private fun PublicAnnouncementTimeSection(announcement: Announcement) {
    val startAt = announcement.taskStringValue(
        paths = listOf(listOf("task", "route", "start_at")),
        legacyKeys = listOf("start_at"),
    )
    val hasEndTime = announcement.data.taskBoolValue(
        paths = listOf(listOf("task", "route", "has_end_time")),
        legacyKeys = listOf("has_end_time"),
    ) ?: false
    val endAt = announcement.taskStringValue(
        paths = listOf(listOf("task", "route", "end_at")),
        legacyKeys = listOf("end_at"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
        DetailValueRow(label = "Начало", value = formatPublicDateTime(startAt))
        if (hasEndTime) {
            DetailValueRow(label = "Окончание", value = formatPublicDateTime(endAt))
        }
    }
}

@Composable
private fun PublicAnnouncementDetailsSection(
    announcement: Announcement,
    imageCount: Int,
) {
    val structured = announcement.structuredData
    val isDelivery = announcement.category.trim().lowercase() == "delivery"
    val description = announcement.detailsDescriptionText ?: "—"
    val dimensions = buildList {
        announcement.data["cargo_length"]?.doubleOrNullCompat()?.let { add("Д ${it.toInt()} см") }
        announcement.data["cargo_width"]?.doubleOrNullCompat()?.let { add("Ш ${it.toInt()} см") }
        announcement.data["cargo_height"]?.doubleOrNullCompat()?.let { add("В ${it.toInt()} см") }
    }.takeIf { it.isNotEmpty() }?.joinToString(" • ")
    val floor = announcement.data["floor"]?.doubleOrNullCompat()?.toInt()

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
        DetailValueRow(label = "Описание", value = description)

        if (isDelivery) {
            dimensions?.let { DetailValueRow(label = "Габариты", value = it) }
            if (structured.requiresLiftToFloor && floor != null) {
                DetailValueRow(label = "Подъём", value = "$floor этаж")
            }
            DetailValueRow(label = "Лифт", value = if (structured.hasElevator) "Есть" else "Нет")
            DetailValueRow(label = "Грузчик", value = if (structured.needsLoader) "Нужен" else "Не нужен")
        }

        DetailValueRow(
            label = "Фото",
            value = if (imageCount > 0) "Прикреплено: $imageCount" else "Не прикреплено",
        )
    }
}

private fun publicContactSummaryRows(announcement: Announcement): List<Pair<String, String>> {
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

    return buildList<Pair<String, String>> {
        if (!name.isNullOrBlank()) add("Имя" to name)
        if (!phone.isNullOrBlank()) add("Телефон" to phone)
        if (!method.isNullOrBlank()) add("Связь" to publicContactMethodLabel(method))
    }
}

private fun publicContactMethodLabel(rawValue: String): String = when (rawValue.trim().lowercase()) {
    "calls_and_messages" -> "Звонки и сообщения"
    "messages_only" -> "Только сообщения"
    "calls_only" -> "Только звонки"
    else -> rawValue
}

private fun formatPublicDateTime(rawValue: String?): String =
    rawValue
        ?.let { runCatching { Instant.parse(it) }.getOrNull() }
        ?.atZone(ZoneId.systemDefault())
        ?.format(java.time.format.DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM, java.time.format.FormatStyle.SHORT))
        ?: "—"

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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
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
