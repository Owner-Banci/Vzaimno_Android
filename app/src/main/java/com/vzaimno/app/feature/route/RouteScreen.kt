package com.vzaimno.app.feature.route

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.AddRoad
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RemoveRoad
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.vzaimno.app.core.designsystem.theme.spacing
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

private val ShellBottomBarContentPadding = 148.dp

@Composable
fun RouteHomeRoute(
    onOpenAnnouncementDetails: (String) -> Unit,
    viewModel: RouteViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.startForegroundRefresh()
            try {
                awaitCancellation()
            } finally {
                viewModel.stopForegroundRefresh()
            }
        }
    }

    RouteScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onDismissInlineMessage = viewModel::clearInlineMessage,
        onSwitchRole = viewModel::switchRole,
        onSelectTask = viewModel::selectTask,
        onAdvanceTaskStage = viewModel::advanceTaskStage,
        onAcceptPreviewTask = viewModel::acceptPreviewTask,
        onRemoveAcceptedTask = viewModel::removeAcceptedTask,
        onRouteRadiusSelected = viewModel::updatePerformerRouteRadius,
        onOpenAnnouncementDetails = onOpenAnnouncementDetails,
        onMapZoomIn = viewModel::zoomMapIn,
        onMapZoomOut = viewModel::zoomMapOut,
        onMapCenterOnUser = viewModel::focusMapOnUserStub,
        onOpenExternalMaps = { navigation ->
            if (!openRouteInMaps(context, navigation)) {
                scope.launch {
                    snackbarHostState.showSnackbar("Не нашли приложение, которое может открыть маршрут.")
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RouteScreen(
    state: RouteUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onDismissInlineMessage: () -> Unit,
    onSwitchRole: (RouteRole) -> Unit,
    onSelectTask: (String) -> Unit,
    onAdvanceTaskStage: (String) -> Unit,
    onAcceptPreviewTask: (String) -> Unit,
    onRemoveAcceptedTask: (String) -> Unit,
    onRouteRadiusSelected: (Int) -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
    onMapZoomIn: () -> Unit,
    onMapZoomOut: () -> Unit,
    onMapCenterOnUser: () -> Unit,
    onOpenExternalMaps: (RouteExternalNavigationUi) -> Unit,
) {
    val activeRole = state.roleSelection.selectedRole
    val performerState = state.performer
    val customerState = state.customer
    val activeHasInitialContent = when (activeRole) {
        RouteRole.Performer -> performerState.status == RouteRoleContentStatus.Content
        RouteRole.Customer -> customerState.status == RouteRoleContentStatus.Content
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.loading.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
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
                    bottom = ShellBottomBarContentPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            ) {
                item {
                    RouteRoleSwitcher(
                        state = state.roleSelection,
                        onSwitchRole = onSwitchRole,
                    )
                }

                if (!state.loading.inlineMessage.isNullOrBlank()) {
                    item {
                        RouteInlineMessageCard(
                            message = state.loading.inlineMessage.orEmpty(),
                            onDismiss = onDismissInlineMessage,
                        )
                    }
                }

                item {
                    RouteMapCard(
                        state = state.map,
                        onMarkerSelected = onSelectTask,
                        onRetry = onRetry,
                        onZoomIn = onMapZoomIn,
                        onZoomOut = onMapZoomOut,
                        onCenterOnUser = onMapCenterOnUser,
                        onOpenExternalMaps = onOpenExternalMaps,
                    )
                }

                val activeMetrics = when (activeRole) {
                    RouteRole.Performer -> performerState.metrics
                    RouteRole.Customer -> customerState.metrics
                }
                if (activeMetrics.isNotEmpty()) {
                    item {
                        RouteMetricsRow(metrics = activeMetrics)
                    }
                }

                if (activeRole == RouteRole.Performer &&
                    performerState.status == RouteRoleContentStatus.Content
                ) {
                    item {
                        RouteRadiusSelector(
                            radiusMeters = performerState.radiusMeters,
                            optionsMeters = performerState.radiusOptionsMeters,
                            enabled = !state.actions.rebuildingRoute,
                            onSelected = onRouteRadiusSelected,
                        )
                    }
                }

                if (activeRole == RouteRole.Performer) {
                    performerState.nextStep?.let { nextStep ->
                        item {
                            RouteNextStepCard(
                                nextStep = nextStep,
                                isBusy = state.actions.updatingStageTaskId == nextStep.taskId,
                                onAction = {
                                    nextStep.taskId?.let(onAdvanceTaskStage)
                                },
                            )
                        }
                    }
                }

                if (state.actions.rebuildingRoute || state.actions.updatingStageTaskId != null) {
                    item {
                        RouteProcessCard(
                            title = if (state.actions.rebuildingRoute) {
                                "Перестраиваем маршрут"
                            } else {
                                "Обновляем этап"
                            },
                            message = if (state.actions.rebuildingRoute) {
                                "Подтягиваем новую схему маршрута и пересчитываем задачи по пути."
                            } else {
                                "Синхронизируем выполнение с сервером и обновляем карточки задач."
                            },
                        )
                    }
                }

                if (state.loading.isInitialLoading && !activeHasInitialContent) {
                    item {
                        RouteCenterStatusCard(
                            title = "Собираем маршрут",
                            message = "Подтягиваем контекст, карту и разрешённые задачи для текущей роли.",
                            showProgress = true,
                            actionLabel = null,
                            onAction = null,
                        )
                    }
                } else {
                    when (activeRole) {
                        RouteRole.Performer -> performerContent(
                            performer = performerState,
                            actions = state.actions,
                            onRetry = onRetry,
                            onSelectTask = onSelectTask,
                            onAdvanceTaskStage = onAdvanceTaskStage,
                            onAcceptPreviewTask = onAcceptPreviewTask,
                            onRemoveAcceptedTask = onRemoveAcceptedTask,
                            onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                        )

                        RouteRole.Customer -> customerContent(
                            customer = customerState,
                            actions = state.actions,
                            onRetry = onRetry,
                            onSelectTask = onSelectTask,
                            onOpenAnnouncementDetails = onOpenAnnouncementDetails,
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.performerContent(
    performer: PerformerRouteUiState,
    actions: RouteExecutionActionUiState,
    onRetry: () -> Unit,
    onSelectTask: (String) -> Unit,
    onAdvanceTaskStage: (String) -> Unit,
    onAcceptPreviewTask: (String) -> Unit,
    onRemoveAcceptedTask: (String) -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
) {
    when (performer.status) {
        RouteRoleContentStatus.Loading -> Unit
        RouteRoleContentStatus.Empty -> {
            item {
                RouteCenterStatusCard(
                    title = performer.emptyTitle,
                    message = performer.emptyMessage,
                    showProgress = false,
                    actionLabel = "Обновить",
                    onAction = onRetry,
                )
            }
        }

        RouteRoleContentStatus.Content -> {
            if (performer.activeTasks.isNotEmpty()) {
                item {
                    RouteSectionHeader(
                        title = "Сейчас в маршруте",
                        subtitle = "Главное задание, добавленные точки по пути и следующий доступный шаг.",
                        count = performer.activeTasks.size,
                    )
                }
                items(
                    items = performer.activeTasks,
                    key = RouteTaskCardUi::id,
                ) { task ->
                    RouteTaskCard(
                        task = task,
                        isBusy = actions.updatingStageTaskId == task.id,
                        onSelect = { onSelectTask(task.id) },
                        onOpenDetails = { onOpenAnnouncementDetails(task.announcementId) },
                        onAdvanceStage = {
                            if (task.canAdvanceStage) {
                                onAdvanceTaskStage(task.id)
                            }
                        },
                        onAcceptToRoute = null,
                        onRemoveFromRoute = {
                            if (task.canRemoveFromRoute) {
                                onRemoveAcceptedTask(task.id)
                            }
                        },
                    )
                }
            }

            if (performer.previewTasks.isNotEmpty()) {
                item {
                    RouteSectionHeader(
                        title = "По пути рядом",
                        subtitle = if (actions.acceptedExtraCount >= actions.acceptedExtraLimit) {
                            "Лимит дополнительных задач заполнен. Можно убрать одну из принятых и выбрать другую."
                        } else {
                            "Можно принять до двух дополнительных задач и сразу перестроить маршрут."
                        },
                        count = performer.previewTasks.size,
                    )
                }
                items(
                    items = performer.previewTasks,
                    key = RouteTaskCardUi::id,
                ) { task ->
                    RouteTaskCard(
                        task = task,
                        isBusy = false,
                        onSelect = { onSelectTask(task.id) },
                        onOpenDetails = { onOpenAnnouncementDetails(task.announcementId) },
                        onAdvanceStage = null,
                        onAcceptToRoute = {
                            if (task.canAcceptToRoute) {
                                onAcceptPreviewTask(task.id)
                            }
                        },
                        onRemoveFromRoute = null,
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.customerContent(
    customer: CustomerRouteUiState,
    actions: RouteExecutionActionUiState,
    onRetry: () -> Unit,
    onSelectTask: (String) -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
) {
    when (customer.status) {
        RouteRoleContentStatus.Loading -> Unit
        RouteRoleContentStatus.Empty -> {
            item {
                RouteCenterStatusCard(
                    title = customer.emptyTitle,
                    message = customer.emptyMessage,
                    showProgress = false,
                    actionLabel = "Обновить",
                    onAction = onRetry,
                )
            }
        }

        RouteRoleContentStatus.Content -> {
            item {
                RouteSectionHeader(
                    title = "Мои задачи",
                    subtitle = "Следите за маршрутом и этапами выполнения своих заказов.",
                    count = customer.tasks.size,
                )
            }
            items(
                items = customer.tasks,
                key = RouteTaskCardUi::id,
            ) { task ->
                RouteTaskCard(
                    task = task,
                    isBusy = actions.updatingStageTaskId == task.id,
                    onSelect = { onSelectTask(task.id) },
                    onOpenDetails = { onOpenAnnouncementDetails(task.announcementId) },
                    onAdvanceStage = null,
                    onAcceptToRoute = null,
                    onRemoveFromRoute = null,
                )
            }
        }
    }
}

@Composable
private fun RouteHeader(summary: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Маршрут",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RouteRoleSwitcher(
    state: RouteRoleSelectionUiState,
    onSwitchRole: (RouteRole) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.86f)),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.options.forEach { option ->
                val selected = option.role == state.selectedRole
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSwitchRole(option.role) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        Color.Transparent
                    },
                    border = if (selected) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.34f))
                    } else {
                        null
                    },
                    shadowElevation = if (selected) 1.dp else 0.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteMapCard(
    state: RouteMapUiState,
    onMarkerSelected: (String) -> Unit,
    onRetry: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCenterOnUser: () -> Unit,
    onOpenExternalMaps: (RouteExternalNavigationUi) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(312.dp),
        ) {
            if (state.isConfigured) {
                RouteYandexMap(
                    state = state,
                    onMarkerSelected = onMarkerSelected,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            state.externalNavigation?.let { navigation ->
                FilledTonalButton(
                    onClick = { onOpenExternalMaps(navigation) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(MaterialTheme.spacing.large),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                    )
                    Text(
                        text = "Открыть в картах",
                        modifier = Modifier.padding(start = MaterialTheme.spacing.small),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = MaterialTheme.spacing.large),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                RouteMapFloatingButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Увеличить карту",
                    onClick = onZoomIn,
                )
                RouteMapFloatingButton(
                    icon = Icons.Filled.Remove,
                    contentDescription = "Уменьшить карту",
                    onClick = onZoomOut,
                )
                RouteMapFloatingButton(
                    icon = Icons.Filled.MyLocation,
                    contentDescription = "Показать текущее положение",
                    onClick = onCenterOnUser,
                )
            }

            state.note?.let { note ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(MaterialTheme.spacing.large),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.68f),
                ) {
                    Text(
                        text = note,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.small,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                }
            }

            if (state.isLoading) {
                RouteMapOverlay(
                    title = "Обновляем карту",
                    message = "Подтягиваем контекст маршрута и актуальные точки задач.",
                    actionLabel = null,
                    onAction = null,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else if (state.overlayTitle != null && state.overlayMessage != null) {
                RouteMapOverlay(
                    title = state.overlayTitle,
                    message = state.overlayMessage,
                    actionLabel = "Обновить",
                    onAction = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun RouteMapFloatingButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun RouteMapOverlay(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(MaterialTheme.spacing.xLarge),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                OutlinedButton(onClick = onAction) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

@Composable
private fun RouteMetricsRow(metrics: List<RouteMetricUi>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        items(metrics, key = RouteMetricUi::id) { metric ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small,
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = metric.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = metric.value,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteRadiusSelector(
    radiusMeters: Int,
    optionsMeters: List<Int>,
    enabled: Boolean,
    onSelected: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.AltRoute,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Задачи по пути",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Показывать задания на расстоянии от маршрута",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                optionsMeters.forEach { option ->
                    val selected = option == radiusMeters
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(enabled = enabled) { onSelected(option) },
                        shape = RoundedCornerShape(999.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        },
                    ) {
                        Text(
                            text = "до ${formatRadiusOption(option)}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun formatRadiusOption(radiusMeters: Int): String = if (radiusMeters >= 1000) {
    "${radiusMeters / 1000} км"
} else {
    "$radiusMeters м"
}

@Composable
private fun RouteNextStepCard(
    nextStep: RouteNextStepUi,
    isBusy: Boolean,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
                        .padding(MaterialTheme.spacing.medium),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = nextStep.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = nextStep.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    )
                }
            }

            if (nextStep.actionLabel != null && nextStep.taskId != null) {
                FilledTonalButton(
                    onClick = onAction,
                    enabled = !isBusy,
                ) {
                    Text(text = nextStep.actionLabel)
                }
            }
        }
    }
}

@Composable
private fun RouteProcessCard(
    title: String,
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun RouteInlineMessageCard(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            TextButton(onClick = onDismiss) {
                Text(text = "Скрыть")
            }
        }
    }
}

@Composable
private fun RouteSectionHeader(
    title: String,
    subtitle: String,
    count: Int,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = 2.dp,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RouteTaskCard(
    task: RouteTaskCardUi,
    isBusy: Boolean,
    onSelect: () -> Unit,
    onOpenDetails: () -> Unit,
    onAdvanceStage: (() -> Unit)?,
    onAcceptToRoute: (() -> Unit)?,
    onRemoveFromRoute: (() -> Unit)?,
) {
    val accentColor = when (task.kind) {
        RouteTaskKind.Primary -> Color(0xFF2BB7A7)
        RouteTaskKind.AcceptedExtra -> Color(0xFF2BB673)
        RouteTaskKind.Preview -> Color(0xFFF08A63)
        RouteTaskKind.CustomerObserved -> Color(0xFF2BB7A7)
    }
    var detailsExpanded by rememberSaveable(task.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (task.isSelected) {
                accentColor.copy(alpha = 0.48f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.86f)
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RouteChip(
                            label = task.kindLabel,
                            tone = when (task.kind) {
                                RouteTaskKind.Primary -> RouteChipTone.Accent
                                RouteTaskKind.AcceptedExtra -> RouteChipTone.Positive
                                RouteTaskKind.Preview -> RouteChipTone.Warning
                                RouteTaskKind.CustomerObserved -> RouteChipTone.Accent
                            },
                        )
                        task.statusChip?.let { chip ->
                            RouteChip(
                                label = chip.label,
                                tone = chip.tone,
                            )
                        }
                    }

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = task.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        task.priceText?.let { price ->
                            Text(
                                text = price,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = accentColor,
                            )
                        }
                        task.detourText?.let { detour ->
                            Text(
                                text = detour,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (task.isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircleOutline,
                            contentDescription = null,
                            tint = accentColor,
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = { detailsExpanded = !detailsExpanded },
                    ) {
                        Icon(
                            imageVector = if (detailsExpanded) {
                                Icons.Outlined.KeyboardArrowUp
                            } else {
                                Icons.Outlined.KeyboardArrowDown
                            },
                            contentDescription = if (detailsExpanded) {
                                "Свернуть описание"
                            } else {
                                "Показать описание"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (task.stageChips.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    task.stageChips.forEach { chip ->
                        RouteStageChip(
                            chip = chip,
                            isActionable = task.canAdvanceStage &&
                                onAdvanceStage != null &&
                                chip.stage == task.nextStage,
                            onClick = {
                                if (task.canAdvanceStage && chip.stage == task.nextStage) {
                                    onAdvanceStage?.invoke()
                                }
                            },
                        )
                    }
                }
            } else if (!task.previewSummary.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                ) {
                    Text(
                        text = task.previewSummary,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.medium,
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AnimatedVisibility(
                visible = detailsExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 8 }),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    Text(
                        text = task.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (task.addressText != null || task.priceText != null || task.detourText != null || task.previewSummary != null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            task.addressText?.let { value ->
                                RouteDetailLine(label = "Адрес", value = value)
                            }
                            task.priceText?.let { value ->
                                RouteDetailLine(label = "Оплата", value = value)
                            }
                            task.detourText?.let { value ->
                                RouteDetailLine(label = "Отклонение", value = value)
                            }
                            task.previewSummary?.let { value ->
                                RouteDetailLine(label = "Подсказка", value = value)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onOpenDetails,
                            enabled = task.canOpenDetails,
                        ) {
                            Text(text = "Открыть карточку")
                        }
                        when {
                            task.canAdvanceStage && onAdvanceStage != null && task.nextStageLabel != null -> {
                                FilledTonalButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = onAdvanceStage,
                                    enabled = !isBusy,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = accentColor.copy(alpha = 0.18f),
                                    ),
                                ) {
                                    Text(
                                        text = task.nextStageLabel,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }

                            task.canAcceptToRoute && onAcceptToRoute != null -> {
                                FilledTonalButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = onAcceptToRoute,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color(0xFF2BB7A7),
                                        contentColor = Color.White,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AddRoad,
                                        contentDescription = null,
                                    )
                                    Text(
                                        text = "В маршрут",
                                        modifier = Modifier.padding(start = 6.dp),
                                    )
                                }
                            }

                            task.canRemoveFromRoute && onRemoveFromRoute != null -> {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = onRemoveFromRoute,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.RemoveRoad,
                                        contentDescription = null,
                                    )
                                    Text(
                                        text = "Убрать",
                                        modifier = Modifier.padding(start = 6.dp),
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
private fun RouteChip(
    label: String,
    tone: RouteChipTone,
) {
    val background = when (tone) {
        RouteChipTone.Accent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        RouteChipTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        RouteChipTone.Positive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        RouteChipTone.Warning -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
        RouteChipTone.Danger -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
    }
    val foreground = when (tone) {
        RouteChipTone.Accent -> MaterialTheme.colorScheme.primary
        RouteChipTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        RouteChipTone.Positive -> MaterialTheme.colorScheme.primary
        RouteChipTone.Warning -> MaterialTheme.colorScheme.secondary
        RouteChipTone.Danger -> MaterialTheme.colorScheme.error
    }

    Surface(
        shape = CircleShape,
        color = background,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = 6.dp,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = foreground,
        )
    }
}

@Composable
private fun RouteStageChip(
    chip: RouteStageChipUi,
    isActionable: Boolean,
    onClick: () -> Unit,
) {
    val background = when (chip.state) {
        RouteStageProgressState.Done -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        RouteStageProgressState.Current -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        RouteStageProgressState.Pending -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    }
    val foreground = when (chip.state) {
        RouteStageProgressState.Done -> Color(0xFF126A38)
        RouteStageProgressState.Current -> Color(0xFF14665E)
        RouteStageProgressState.Pending -> if (isActionable) Color(0xFF2BB7A7) else MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(enabled = isActionable, onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = background,
        border = if (isActionable) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2BB7A7).copy(alpha = 0.24f))
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = 10.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        when (chip.state) {
                            RouteStageProgressState.Done,
                            RouteStageProgressState.Current,
                            -> Color(0xFF2BB7A7)

                            RouteStageProgressState.Pending -> if (isActionable) {
                                Color(0xFF2BB7A7).copy(alpha = 0.18f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (chip.state) {
                        RouteStageProgressState.Done -> "✓"
                        RouteStageProgressState.Current -> "•"
                        RouteStageProgressState.Pending -> (chip.stage.ordinal + 1).toString()
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = when (chip.state) {
                        RouteStageProgressState.Done,
                        RouteStageProgressState.Current,
                        -> Color.White

                        RouteStageProgressState.Pending -> if (isActionable) Color(0xFF2BB7A7) else MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Text(
                text = chip.label,
                style = MaterialTheme.typography.labelMedium,
                color = foreground,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RouteDetailLine(
    label: String,
    value: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RouteCenterStatusCard(
    title: String,
    message: String,
    showProgress: Boolean,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        modifier = Modifier.padding(12.dp),
                        imageVector = Icons.AutoMirrored.Outlined.AltRoute,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (!showProgress && actionLabel != null && onAction != null) {
                OutlinedButton(
                    onClick = onAction,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}
