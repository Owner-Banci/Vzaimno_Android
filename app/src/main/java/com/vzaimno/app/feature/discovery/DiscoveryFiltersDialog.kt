package com.vzaimno.app.feature.discovery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.AnnouncementStructuredData

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DiscoveryFiltersDialog(
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
    onSetOnlyOnRoute: (Boolean) -> Unit,
    onActivateRoute: (com.vzaimno.app.core.model.GeoPoint, com.vzaimno.app.core.model.GeoPoint, String, String) -> Unit,
    onBuildRouteFromDraft: () -> Unit,
    onDeactivateRoute: () -> Unit,
    onUpdateRouteRadius: (Int) -> Unit,
    onUpdateRouteDraftStartAddress: (String) -> Unit,
    onUpdateRouteDraftEndAddress: (String) -> Unit,
    onSelectRouteAnnouncement: (String) -> Unit,
    onAcceptRouteAnnouncement: (String) -> Unit,
    onRemoveAcceptedRouteAnnouncement: (String) -> Unit,
    onOpenAnnouncementDetails: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = MaterialTheme.spacing.xLarge,
                            top = MaterialTheme.spacing.xLarge,
                            end = MaterialTheme.spacing.xLarge,
                            bottom = MaterialTheme.spacing.large,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onReset) {
                        Text(text = "Сбросить")
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Фильтры",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Маршрут, расстояние по пути и параметры задач",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(text = "Закрыть")
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = MaterialTheme.spacing.xLarge,
                        top = 0.dp,
                        end = MaterialTheme.spacing.xLarge,
                        bottom = MaterialTheme.spacing.xLarge,
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    item {
                        DiscoverySectionCard(
                            title = "Маршрут пользователя",
                            subtitle = "Задайте путь, чтобы карта и список опирались на реальный маршрут, а не на demo-точки.",
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                            ) {
                                DiscoveryFilterField(
                                    label = "Откуда",
                                    placeholder = "Введите стартовый адрес",
                                    value = routeState.draftStartAddress,
                                    onValueChange = onUpdateRouteDraftStartAddress,
                                )
                                DiscoveryFilterField(
                                    label = "Куда",
                                    placeholder = "Введите адрес прибытия",
                                    value = routeState.draftEndAddress,
                                    onValueChange = onUpdateRouteDraftEndAddress,
                                )

                                if (routeState.hasRoute) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        DiscoveryInfoPill(
                                            icon = Icons.Outlined.Map,
                                            text = buildRouteMetaText(routeState),
                                        )
                                        if (routeState.nonAcceptedMatchCount > 0) {
                                            DiscoveryInfoPill(
                                                icon = Icons.Outlined.Route,
                                                text = "${routeState.nonAcceptedMatchCount} по пути",
                                            )
                                        }
                                    }
                                }

                                DiscoveryToggleTile(
                                    title = "Показывать только задачи по пути",
                                    subtitle = "Оставить на карте и в списке только объявления внутри выбранного радиуса от маршрута.",
                                    checked = draft.onlyOnRoute,
                                    onCheckedChange = onSetOnlyOnRoute,
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = onBuildRouteFromDraft,
                                        enabled = !routeState.isBuilding,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2BB7A7),
                                        ),
                                    ) {
                                        if (routeState.isBuilding) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White,
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.NearMe,
                                                contentDescription = null,
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(text = if (routeState.isBuilding) "Строим..." else "Построить маршрут")
                                    }

                                    if (routeState.hasRoute || routeState.hasDraftAddresses) {
                                        OutlinedButton(
                                            onClick = onDeactivateRoute,
                                            modifier = Modifier.height(52.dp),
                                        ) {
                                            Text(text = "Сбросить")
                                        }
                                    }
                                }

                                if (routeState.statusMessage.isNotBlank()) {
                                    Text(
                                        text = routeState.statusMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    item {
                        DiscoverySectionCard(
                            title = "Насколько рядом с маршрутом",
                            subtitle = "Чем меньше радиус, тем ближе задачи к реальной траектории.",
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            ) {
                                listOf(300, 500, 1000).forEach { radius ->
                                    DiscoveryChoiceChip(
                                        title = "до $radius м",
                                        selected = routeState.radiusMeters == radius,
                                        onClick = { onUpdateRouteRadius(radius) },
                                    )
                                }
                            }
                        }
                    }

                    if (routeState.hasRoute) {
                        item {
                            DiscoverySectionCard(
                                title = "Задания по пути",
                                subtitle = "Сначала принятые в маршрут, ниже оставшиеся ветки.",
                            ) {
                                if (routeState.matchedAnnouncements.isEmpty() && !routeState.isBuilding) {
                                    Text(
                                        text = "Пока нет задач внутри выбранного радиуса маршрута.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                                    ) {
                                        val matchedById = routeState.matchedAnnouncements.associateBy { it.item.announcementId }
                                        routeState.acceptedAnnouncementIds.forEachIndexed { index, announcementId ->
                                            val matched = matchedById[announcementId] ?: return@forEachIndexed
                                            RouteTaskRouteCard(
                                                matched = matched,
                                                isAccepted = true,
                                                isSelected = routeState.selectedAnnouncementId == announcementId,
                                                waypointLabel = listOf("C", "D").getOrElse(index) { "C${index + 1}" },
                                                onSelect = { onSelectRouteAnnouncement(announcementId) },
                                                onAcceptOrRemove = { onRemoveAcceptedRouteAnnouncement(announcementId) },
                                                onOpenDetails = { onOpenAnnouncementDetails(announcementId) },
                                                canAccept = false,
                                            )
                                        }

                                        routeState.previewBranches
                                            .filter { preview ->
                                                !routeState.acceptedAnnouncementIds.contains(preview.item.announcementId)
                                            }
                                            .forEach { preview ->
                                                val matched = matchedById[preview.item.announcementId] ?: return@forEach
                                                RouteTaskRouteCard(
                                                    matched = matched,
                                                    isAccepted = false,
                                                    isSelected = routeState.selectedAnnouncementId == preview.item.announcementId,
                                                    waypointLabel = null,
                                                    onSelect = { onSelectRouteAnnouncement(preview.item.announcementId) },
                                                    onAcceptOrRemove = {
                                                        onAcceptRouteAnnouncement(preview.item.announcementId)
                                                    },
                                                    onOpenDetails = {
                                                        onOpenAnnouncementDetails(preview.item.announcementId)
                                                    },
                                                    canAccept = routeState.acceptedAnnouncementIds.size < 2,
                                                )
                                            }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        DiscoverySectionCard(
                            title = "Категория",
                            subtitle = "Быстрый отсев между доставкой и помощью.",
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            ) {
                                DiscoveryChoiceChip(
                                    title = "Доставка",
                                    selected = draft.categories.contains(DiscoveryCategoryFilter.Delivery),
                                    onClick = { onToggleCategory(DiscoveryCategoryFilter.Delivery) },
                                )
                                DiscoveryChoiceChip(
                                    title = "Помощь",
                                    selected = draft.categories.contains(DiscoveryCategoryFilter.Help),
                                    onClick = { onToggleCategory(DiscoveryCategoryFilter.Help) },
                                )
                            }
                        }
                    }

                    item {
                        DiscoverySectionCard(
                            title = "Срочность",
                            subtitle = "Можно выбрать один или несколько сценариев.",
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            ) {
                                discoveryUrgencyFilters.forEach { urgency ->
                                    DiscoveryChoiceChip(
                                        title = urgency.filterLabel(),
                                        selected = draft.urgencies.contains(urgency),
                                        onClick = { onToggleUrgency(urgency) },
                                    )
                                }
                            }
                        }
                    }

                    item {
                        DiscoverySectionCard(
                            title = "Бюджет",
                            subtitle = "Диапазон стоимости для карты и списка.",
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
                                DiscoveryFilterField(
                                    label = "От",
                                    placeholder = "0",
                                    value = draft.budgetMinText,
                                    onValueChange = onBudgetMinChange,
                                    modifier = Modifier.weight(1f),
                                )
                                DiscoveryFilterField(
                                    label = "До",
                                    placeholder = "5000",
                                    value = draft.budgetMaxText,
                                    onValueChange = onBudgetMaxChange,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    item {
                        DiscoverySectionCard(
                            title = "Условия выполнения",
                            subtitle = "Оставьте только подходящие по формату задачи.",
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            ) {
                                DiscoveryToggleTile(
                                    title = "Только с фото",
                                    subtitle = "Оставить объявления, где уже есть медиа.",
                                    checked = draft.withPhotoOnly,
                                    onCheckedChange = onSetWithPhotoOnly,
                                )
                                DiscoveryToggleTile(
                                    title = "Нужна машина",
                                    subtitle = "Оставить только задачи с требованием транспорта.",
                                    checked = draft.requiresVehicleOnly,
                                    onCheckedChange = onSetRequiresVehicleOnly,
                                )
                                DiscoveryToggleTile(
                                    title = "Нужен грузчик",
                                    subtitle = "Оставить задачи, где нужен дополнительный помощник.",
                                    checked = draft.needsLoaderOnly,
                                    onCheckedChange = onSetNeedsLoaderOnly,
                                )
                                DiscoveryToggleTile(
                                    title = "Бесконтактно",
                                    subtitle = "Показывать только бесконтактные сценарии.",
                                    checked = draft.contactlessOnly,
                                    onCheckedChange = onSetContactlessOnly,
                                )
                            }
                        }
                    }
                }

                Surface(
                    tonalElevation = 2.dp,
                    shadowElevation = 8.dp,
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.xLarge),
                        onClick = onApply,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2BB7A7),
                        ),
                    ) {
                        Text(
                            text = "Применить фильтры",
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverySectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            content()
        }
    }
}

@Composable
private fun DiscoveryFilterField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder)
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
        )
    }
}

@Composable
private fun DiscoveryChoiceChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Color(0xFF2BB7A7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DiscoveryToggleTile(
    title: String,
    subtitle: String,
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun DiscoveryInfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RouteTaskRouteCard(
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
        isAccepted -> Color(0xFF2BB673)
        isSelected -> Color(0xFFF08A63)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val backgroundColor = when {
        isAccepted -> Color(0xFF2BB673).copy(alpha = 0.08f)
        isSelected -> Color(0xFFF08A63).copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isAccepted && waypointLabel != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2BB673)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = waypointLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                } else if (!isAccepted) {
                    Icon(
                        imageVector = Icons.Outlined.Route,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFFF08A63) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = matched.item.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                matched.item.budgetText?.let { budget ->
                    Text(
                        text = budget,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2BB7A7),
                    )
                }
            }

            Text(
                text = "До маршрута ${matched.distanceToRouteMeters.toInt()} м",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (matched.item.addressText.isNotBlank()) {
                Text(
                    text = matched.item.addressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onOpenDetails) {
                    Text(text = "Подробнее")
                }
                if (isAccepted) {
                    OutlinedButton(onClick = onAcceptOrRemove) {
                        Text(text = "Убрать")
                    }
                } else if (canAccept) {
                    Button(
                        onClick = onAcceptOrRemove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2BB7A7),
                        ),
                    ) {
                        Text(
                            text = "В маршрут",
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

private fun buildRouteMetaText(routeState: RouteState): String {
    val route = routeState.currentRoute ?: return "Маршрут ещё не построен"
    return "${discoveryFormatDistance(route.distanceMeters)} • ${discoveryFormatDuration(route.durationSeconds)}"
}

private fun discoveryFormatDistance(distanceMeters: Int): String {
    return if (distanceMeters >= 1000) {
        String.format("%.1f км", distanceMeters / 1000.0)
    } else {
        "$distanceMeters м"
    }
}

private fun discoveryFormatDuration(durationSeconds: Int): String {
    val minutes = (durationSeconds / 60.0).toInt().coerceAtLeast(1)
    return if (minutes >= 60) {
        val hours = minutes / 60
        val rest = minutes % 60
        if (rest == 0) "$hours ч" else "$hours ч $rest мин"
    } else {
        "$minutes мин"
    }
}

private val discoveryUrgencyFilters = listOf(
    AnnouncementStructuredData.Urgency.Now,
    AnnouncementStructuredData.Urgency.Today,
    AnnouncementStructuredData.Urgency.Scheduled,
    AnnouncementStructuredData.Urgency.Flexible,
)

@Composable
private fun AnnouncementStructuredData.Urgency.filterLabel(): String = when (this) {
    AnnouncementStructuredData.Urgency.Now -> "Сейчас"
    AnnouncementStructuredData.Urgency.Today -> "Сегодня"
    AnnouncementStructuredData.Urgency.Scheduled -> "По времени"
    AnnouncementStructuredData.Urgency.Flexible -> "Гибко"
}
