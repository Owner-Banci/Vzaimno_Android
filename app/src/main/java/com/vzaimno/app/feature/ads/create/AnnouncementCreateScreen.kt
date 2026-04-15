package com.vzaimno.app.feature.ads.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.feature.ads.AdsFilterBucket
import com.vzaimno.app.feature.shell.navigation.HideShellBottomBarEffect
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AnnouncementCreateRoute(
    onBack: () -> Unit,
    onSubmitted: (AdsFilterBucket, String?) -> Unit,
    viewModel: AnnouncementCreateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3),
        onResult = viewModel::onMediaPicked,
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AnnouncementCreateEvent.Submitted -> onSubmitted(event.focusFilter, event.message)
            }
        }
    }

    AnnouncementCreateScreen(
        state = state,
        onBack = onBack,
        onActionTypeSelected = viewModel::onActionTypeSelected,
        onItemTypeSelected = viewModel::onItemTypeSelected,
        onPurchaseTypeSelected = viewModel::onPurchaseTypeSelected,
        onHelpTypeSelected = viewModel::onHelpTypeSelected,
        onTaskBriefChanged = viewModel::onTaskBriefChanged,
        onSourceKindSelected = viewModel::onSourceKindSelected,
        onSourceAddressChanged = viewModel::onSourceAddressChanged,
        onDestinationKindSelected = viewModel::onDestinationKindSelected,
        onDestinationAddressChanged = viewModel::onDestinationAddressChanged,
        onUrgencySelected = viewModel::onUrgencySelected,
        onStartDateChanged = viewModel::onStartDateChanged,
        onHasEndTimeChanged = viewModel::onHasEndTimeChanged,
        onEndDateChanged = viewModel::onEndDateChanged,
        onConditionChanged = viewModel::onConditionChanged,
        onEstimatedTaskMinutesChanged = viewModel::onEstimatedTaskMinutesChanged,
        onWaitingMinutesChanged = viewModel::onWaitingMinutesChanged,
        onFloorChanged = viewModel::onFloorChanged,
        onWeightCategorySelected = viewModel::onWeightCategorySelected,
        onSizeCategorySelected = viewModel::onSizeCategorySelected,
        onCargoLengthChanged = viewModel::onCargoLengthChanged,
        onCargoWidthChanged = viewModel::onCargoWidthChanged,
        onCargoHeightChanged = viewModel::onCargoHeightChanged,
        onBudgetMinChanged = viewModel::onBudgetMinChanged,
        onBudgetMaxChanged = viewModel::onBudgetMaxChanged,
        onContactNameChanged = viewModel::onContactNameChanged,
        onContactPhoneChanged = viewModel::onContactPhoneChanged,
        onContactMethodSelected = viewModel::onContactMethodSelected,
        onAudienceSelected = viewModel::onAudienceSelected,
        onNotesChanged = viewModel::onNotesChanged,
        onPickMedia = {
            mediaPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onRemoveMedia = viewModel::removeMedia,
        onToggleSummary = viewModel::toggleSummaryExpanded,
        onToggleExactDimensions = viewModel::toggleExactDimensions,
        onDismissMessage = viewModel::clearInlineMessage,
        onSubmit = viewModel::submit,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AnnouncementCreateScreen(
    state: AnnouncementCreateUiState,
    onBack: () -> Unit,
    onActionTypeSelected: (AnnouncementStructuredData.ActionType) -> Unit,
    onItemTypeSelected: (AnnouncementCreateItemType?) -> Unit,
    onPurchaseTypeSelected: (AnnouncementCreatePurchaseType?) -> Unit,
    onHelpTypeSelected: (AnnouncementCreateHelpType?) -> Unit,
    onTaskBriefChanged: (String) -> Unit,
    onSourceKindSelected: (AnnouncementStructuredData.SourceKind?) -> Unit,
    onSourceAddressChanged: (String) -> Unit,
    onDestinationKindSelected: (AnnouncementStructuredData.DestinationKind?) -> Unit,
    onDestinationAddressChanged: (String) -> Unit,
    onUrgencySelected: (AnnouncementStructuredData.Urgency) -> Unit,
    onStartDateChanged: (String) -> Unit,
    onHasEndTimeChanged: (Boolean) -> Unit,
    onEndDateChanged: (String) -> Unit,
    onConditionChanged: (AnnouncementConditionOption, Boolean) -> Unit,
    onEstimatedTaskMinutesChanged: (String) -> Unit,
    onWaitingMinutesChanged: (String) -> Unit,
    onFloorChanged: (String) -> Unit,
    onWeightCategorySelected: (AnnouncementStructuredData.WeightCategory?) -> Unit,
    onSizeCategorySelected: (AnnouncementStructuredData.SizeCategory?) -> Unit,
    onCargoLengthChanged: (String) -> Unit,
    onCargoWidthChanged: (String) -> Unit,
    onCargoHeightChanged: (String) -> Unit,
    onBudgetMinChanged: (String) -> Unit,
    onBudgetMaxChanged: (String) -> Unit,
    onContactNameChanged: (String) -> Unit,
    onContactPhoneChanged: (String) -> Unit,
    onContactMethodSelected: (AnnouncementContactMethod) -> Unit,
    onAudienceSelected: (AnnouncementAudience) -> Unit,
    onNotesChanged: (String) -> Unit,
    onPickMedia: () -> Unit,
    onRemoveMedia: (String) -> Unit,
    onToggleSummary: () -> Unit,
    onToggleExactDimensions: () -> Unit,
    onDismissMessage: () -> Unit,
    onSubmit: () -> Unit,
) {
    HideShellBottomBarEffect(reason = "ads_create")

    val draft = state.draft
    val exactDimensionsExpanded = state.showsExactDimensions || draft.hasExactDimensions

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MilkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Новое объявление",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                actions = {
                    IconButton(onClick = onBack, enabled = !state.isBusy) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Закрыть",
                            tint = TurquoiseAccent,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MilkBackground,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .background(MilkBackground.copy(alpha = 0.96f))
                    .padding(horizontal = ScreenHorizontalPadding, vertical = 6.dp),
            ) {
                CreateAdStickyMiniSummary(
                    title = draft.resolvedTitle,
                    actionText = draft.actionType?.title ?: "Черновик",
                    objectText = draft.objectSummary.ifBlank { "Без деталей" },
                    routeSummary = draft.routeSummary,
                    timeSummary = draft.timeSummary,
                    priceSummary = draft.budgetSummary,
                    isExpanded = state.isSummaryExpanded,
                    onToggle = onToggleSummary,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .imePadding(),
                contentPadding = PaddingValues(
                    start = ScreenHorizontalPadding,
                    top = 8.dp,
                    end = ScreenHorizontalPadding,
                    bottom = 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(SectionVerticalGap),
            ) {
                if (state.isCreateAgain && state.prefillSourceTitle != null) {
                    item {
                        CreateInfoBanner(
                            title = "Черновик на основе прошлого объявления",
                            text = "Мы перенесли данные из «${state.prefillSourceTitle}» и отметили поля, которые требуют корректировки после модерации.",
                        )
                    }
                }

                if (!state.inlineMessage.isNullOrBlank()) {
                    item {
                        CreateWarningBanner(
                            text = state.inlineMessage,
                            onDismiss = onDismissMessage,
                        )
                    }
                }

                if (state.isPrefillLoading) {
                    item {
                        CreateInfoBanner(
                            title = "Подгружаем данные объявления",
                            text = "Сейчас перенесём сценарий, адреса, бюджет и пометки модерации.",
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Соберите объявление\nиз готовых блоков",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                        )
                        Text(
                            text = "Сначала выберите сценарий, потом уточните детали. Категория, заголовок и итоговый payload соберутся автоматически.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                        )
                    }
                }

                item {
                    CreateAdSectionCard(
                        title = "Что нужно сделать",
                        subtitle = "Это главный выбор. Он определяет набор полей, итоговый заголовок и backend group.",
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            AnnouncementStructuredData.ActionType.entries.forEach { action ->
                                CreateAdChoiceChip(
                                    text = action.title,
                                    subtitle = actionSubtitle(action),
                                    icon = actionIcon(action),
                                    selected = draft.actionType == action,
                                    onClick = { onActionTypeSelected(action) },
                                    modifier = Modifier.fillMaxWidth(0.48f),
                                )
                            }
                        }
                    }
                }

                if (!draft.showsStructuredSections) {
                    item {
                        CreateAdSectionCard(
                            title = "С чего начать",
                            subtitle = "Выберите верхний сценарий. После этого экран покажет только подходящие блоки и уберёт всё лишнее.",
                        ) {
                            Text(
                                text = "Минимум ручного текста: в основном чипы, переключатели, адреса и несколько коротких числовых значений.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = draft.showsStructuredSections,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(SectionVerticalGap)) {
                            CreateAdSectionCard(
                                title = draft.objectSectionTitle,
                                subtitle = draft.objectSectionSubtitle,
                            ) {
                                ObjectSectionContent(
                                    draft = draft,
                                    onItemTypeSelected = onItemTypeSelected,
                                    onPurchaseTypeSelected = onPurchaseTypeSelected,
                                    onHelpTypeSelected = onHelpTypeSelected,
                                    onTaskBriefChanged = onTaskBriefChanged,
                                    onConditionChanged = onConditionChanged,
                                )
                            }

                            CreateAdSectionCard(
                                title = draft.sourceSectionTitle,
                                subtitle = sourceSectionSubtitle(draft.actionType),
                                moderationMark = draft.moderationMarks[draft.sourceAddressModerationKey],
                            ) {
                                CompactChoiceFlow(
                                    items = draft.availableSourceKinds,
                                    selected = draft.sourceKind,
                                    title = { sourceTitle(draft.actionType, it) },
                                    icon = { sourceIcon(it) },
                                    onSelected = onSourceKindSelected,
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                CreateAdTextField(
                                    label = draft.sourceFieldLabel,
                                    value = draft.source.address,
                                    onValueChange = onSourceAddressChanged,
                                    placeholder = draft.sourceAddressPlaceholder,
                                    moderationMark = draft.moderationMarks[draft.sourceAddressModerationKey],
                                )
                                ModerationHelp(draft.moderationMarks[draft.sourceAddressModerationKey])
                            }

                            if (draft.showsDestinationSection) {
                                CreateAdSectionCard(
                                    title = draft.destinationSectionTitle,
                                    subtitle = destinationSectionSubtitle(draft.actionType),
                                    moderationMark = draft.moderationMarks[draft.destinationAddressModerationKey],
                                ) {
                                    CompactChoiceFlow(
                                        items = draft.availableDestinationKinds,
                                        selected = draft.destinationKind,
                                        title = { destinationTitle(draft.actionType, it) },
                                        icon = { destinationIcon(it) },
                                        onSelected = onDestinationKindSelected,
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    CreateAdTextField(
                                        label = draft.destinationFieldLabel,
                                        value = draft.destination.address,
                                        onValueChange = onDestinationAddressChanged,
                                        placeholder = draft.destinationAddressPlaceholder,
                                        moderationMark = draft.moderationMarks[draft.destinationAddressModerationKey],
                                    )
                                    ModerationHelp(draft.moderationMarks[draft.destinationAddressModerationKey])
                                }
                            }

                            CreateAdSectionCard(
                                title = "Когда",
                                subtitle = "Сначала выберите срочность, потом при необходимости уточните крайнее время и ожидание.",
                            ) {
                                CompactChoiceFlow(
                                    items = AnnouncementStructuredData.Urgency.entries.toList(),
                                    selected = draft.urgency,
                                    title = { it.title },
                                    icon = { urgencyIcon(it) },
                                    onSelected = onUrgencySelected,
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                if (draft.urgency == AnnouncementStructuredData.Urgency.Scheduled) {
                                    DateTimeField(
                                        label = "Начало",
                                        value = formatStoredDateTime(draft.startDate) ?: draft.startDate,
                                        placeholder = "Выберите дату и время",
                                        onValueChange = onStartDateChanged,
                                    )
                                } else {
                                    CreateAdInfoTag(text = draft.timeSummary, icon = urgencyIcon(draft.urgency))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Если нужен точный слот, переключите на «Ко времени».",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                CreateAdToggleRow(
                                    title = "Указать крайнее время",
                                    checked = draft.hasEndTime,
                                    onCheckedChange = onHasEndTimeChanged,
                                )

                                AnimatedVisibility(
                                    visible = draft.hasEndTime,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically(),
                                ) {
                                    Column(modifier = Modifier.padding(top = 12.dp)) {
                                        DateTimeField(
                                            label = "Крайнее время",
                                            value = formatStoredDateTime(draft.endDate) ?: draft.endDate,
                                            placeholder = "Выберите дату и время",
                                            onValueChange = onEndDateChanged,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                CreateAdValueField(
                                    label = "Сколько займёт задача",
                                    value = draft.attributes.estimatedTaskMinutes,
                                    onValueChange = onEstimatedTaskMinutesChanged,
                                    trailingUnit = "мин",
                                    placeholder = "30",
                                )

                                AnimatedVisibility(
                                    visible = draft.attributes.waitOnSite,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically(),
                                ) {
                                    Column(modifier = Modifier.padding(top = 12.dp)) {
                                        CreateAdValueField(
                                            label = "Сколько можно ждать",
                                            value = draft.attributes.waitingMinutes,
                                            onValueChange = onWaitingMinutesChanged,
                                            trailingUnit = "мин",
                                            placeholder = "10",
                                        )
                                    }
                                }
                            }

                            if (draft.availableConditionOptions.isNotEmpty()) {
                                CreateAdSectionCard(
                                    title = "Условия",
                                    subtitle = "Выберите только те параметры, которые реально влияют на сценарий и цену.",
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        draft.availableConditionOptions.forEach { option ->
                                            CreateAdToggleTile(
                                                title = option.title,
                                                subtitle = option.subtitle,
                                                icon = conditionIcon(option),
                                                selected = draft.attributes.valueFor(option),
                                                onClick = {
                                                    onConditionChanged(
                                                        option,
                                                        !draft.attributes.valueFor(option),
                                                    )
                                                },
                                            )
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = draft.attributes.requiresLiftToFloor,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically(),
                                    ) {
                                        Column(modifier = Modifier.padding(top = 14.dp)) {
                                            CreateAdValueField(
                                                label = "Этаж",
                                                value = draft.attributes.floor,
                                                onValueChange = onFloorChanged,
                                                placeholder = "Например: 5",
                                            )
                                        }
                                    }
                                }
                            }

                            if (draft.showsCargoSection) {
                                CreateAdSectionCard(
                                    title = "Габариты и вес",
                                    subtitle = "Сначала выберите примерную категорию, затем при необходимости раскройте точные размеры.",
                                ) {
                                    Text(
                                        text = "Вес",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextSecondary,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    CompactChoiceFlow(
                                        items = AnnouncementStructuredData.WeightCategory.entries.toList(),
                                        selected = draft.attributes.weightCategory,
                                        title = { it.title },
                                        icon = { null },
                                        onSelected = onWeightCategorySelected,
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Размер",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextSecondary,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    CompactChoiceFlow(
                                        items = AnnouncementStructuredData.SizeCategory.entries.toList(),
                                        selected = draft.attributes.sizeCategory,
                                        title = { it.title },
                                        icon = { null },
                                        onSelected = onSizeCategorySelected,
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(InnerCardRadius))
                                            .clickable(onClick = onToggleExactDimensions)
                                            .animateContentSize(),
                                        shape = RoundedCornerShape(InnerCardRadius),
                                        color = CardBackground.copy(alpha = 0.72f),
                                        border = BorderStroke(1.dp, TurquoiseAccent.copy(alpha = 0.14f)),
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Указать точные габариты",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                                    color = TextPrimary,
                                                    modifier = Modifier.weight(1f),
                                                )
                                                Icon(
                                                    imageVector = if (exactDimensionsExpanded) {
                                                        Icons.Outlined.Close
                                                    } else {
                                                        Icons.Outlined.DragHandle
                                                    },
                                                    contentDescription = null,
                                                    tint = TurquoiseAccent,
                                                )
                                            }

                                            AnimatedVisibility(
                                                visible = exactDimensionsExpanded,
                                                enter = fadeIn() + expandVertically(),
                                                exit = fadeOut() + shrinkVertically(),
                                            ) {
                                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                        CreateAdValueField(
                                                            label = "Длина",
                                                            value = draft.attributes.cargoLength,
                                                            onValueChange = onCargoLengthChanged,
                                                            trailingUnit = "см",
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                        CreateAdValueField(
                                                            label = "Ширина",
                                                            value = draft.attributes.cargoWidth,
                                                            onValueChange = onCargoWidthChanged,
                                                            trailingUnit = "см",
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                        CreateAdValueField(
                                                            label = "Высота",
                                                            value = draft.attributes.cargoHeight,
                                                            onValueChange = onCargoHeightChanged,
                                                            trailingUnit = "см",
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            CreateAdSectionCard(
                                title = "Цена",
                                subtitle = "Сначала посмотрите рекомендацию, потом задайте свой диапазон, если хотите отклониться.",
                            ) {
                                CreateAdRecommendedPriceView(
                                    priceText = draft.recommendedPriceRange.text,
                                    subtitle = "Оценка по сценарию, срочности, условиям и габаритам.",
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                CreateAdBudgetRangeField(
                                    minValue = draft.budget.min,
                                    maxValue = draft.budget.max,
                                    onMinChange = onBudgetMinChanged,
                                    onMaxChange = onBudgetMaxChanged,
                                    minPlaceholder = draft.recommendedPriceRange.minPlaceholder,
                                    maxPlaceholder = draft.recommendedPriceRange.maxPlaceholder,
                                )
                            }

                            CreateAdSectionCard(
                                title = "Контакты",
                                subtitle = "Исполнитель увидит ваши контакты и предпочтительный способ связи.",
                            ) {
                                CreateAdTextField(
                                    label = "Имя",
                                    value = draft.contacts.name,
                                    onValueChange = onContactNameChanged,
                                    placeholder = "Как к вам обращаться",
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                CreateAdTextField(
                                    label = "Телефон",
                                    value = draft.contacts.phone,
                                    onValueChange = onContactPhoneChanged,
                                    placeholder = "+7 999 123-45-67",
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Способ связи",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextSecondary,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CompactChoiceFlow(
                                    items = AnnouncementContactMethod.entries.toList(),
                                    selected = draft.contacts.method,
                                    title = { it.title },
                                    icon = { null },
                                    onSelected = onContactMethodSelected,
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Для кого",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextSecondary,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CompactChoiceFlow(
                                    items = AnnouncementAudience.entries.toList(),
                                    selected = draft.contacts.audience,
                                    title = { it.title },
                                    icon = { null },
                                    onSelected = onAudienceSelected,
                                )
                            }

                            CreateAdSectionCard(
                                title = "Описание и фото",
                                subtitle = "Короткий title собирается автоматически из действия и типа задачи. Подробное описание собирается ниже и остаётся редактируемым.",
                                moderationMark = draft.moderationMarks["title"] ?: draft.moderationMarks["notes"] ?: draft.moderationMarks["media"],
                            ) {
                                Text(
                                    text = "Title",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = draft.generatedTitle,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Собирается из двух главных смысловых параметров.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                                ModerationHelp(draft.moderationMarks["title"])

                                Spacer(modifier = Modifier.height(18.dp))
                                CreateAdTextArea(
                                    label = "Описание",
                                    value = draft.notes,
                                    onValueChange = onNotesChanged,
                                    placeholder = draft.notesPlaceholder,
                                )
                                ModerationHelp(draft.moderationMarks["notes"])
                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = onPickMedia,
                                    enabled = !state.isBusy && draft.media.size < 3,
                                    shape = RoundedCornerShape(ButtonRadius),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AddAPhoto,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (draft.media.isEmpty()) {
                                            "Добавить фото (до 3)"
                                        } else {
                                            "Изменить фото"
                                        },
                                    )
                                }
                                ModerationHelp(draft.moderationMarks["media"])

                                if (draft.media.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(draft.media, key = { it.id }) { media ->
                                            MediaPreviewItem(
                                                media = media,
                                                onRemove = { onRemoveMedia(media.id) },
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Фото проверяются на сервере. Если спорно — объявление уйдёт в черновики.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }
                            }

                            CreateAdSectionCard(
                                title = "Итог объявления",
                                subtitle = "Сначала проверьте итоговую карточку. Кнопка отправки появляется только здесь, в самом конце формы.",
                            ) {
                                FinalSummaryCard(draft = draft)

                                if (draft.selectedConditionTitles.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        draft.selectedConditionTitles.forEach { title ->
                                            CreateAdInfoTag(text = title)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                AnimatedVisibility(
                                    visible = draft.isReadyForSubmit,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically(),
                                ) {
                                    CreateAdBottomButton(
                                        text = "Отправить на проверку",
                                        onClick = onSubmit,
                                        enabled = !state.isBusy,
                                        isLoading = state.isSubmitting,
                                    )
                                }

                                AnimatedVisibility(
                                    visible = !draft.isReadyForSubmit,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically(),
                                ) {
                                    CreateAdReadinessCard(issues = draft.submitReadinessIssues)
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
private fun ObjectSectionContent(
    draft: AnnouncementCreateFormDraft,
    onItemTypeSelected: (AnnouncementCreateItemType?) -> Unit,
    onPurchaseTypeSelected: (AnnouncementCreatePurchaseType?) -> Unit,
    onHelpTypeSelected: (AnnouncementCreateHelpType?) -> Unit,
    onTaskBriefChanged: (String) -> Unit,
    onConditionChanged: (AnnouncementConditionOption, Boolean) -> Unit,
) {
    when (draft.actionType) {
        AnnouncementStructuredData.ActionType.Pickup,
        AnnouncementStructuredData.ActionType.Carry,
        -> CompactChoiceFlow(
            items = draft.availableGenericItemTypes,
            selected = draft.itemType,
            title = { it.title },
            icon = { itemIcon(it) },
            onSelected = onItemTypeSelected,
        )

        AnnouncementStructuredData.ActionType.Buy -> CompactChoiceFlow(
            items = AnnouncementCreatePurchaseType.entries.toList(),
            selected = draft.purchaseType,
            title = { it.title },
            icon = { purchaseIcon(it) },
            onSelected = onPurchaseTypeSelected,
        )

        AnnouncementStructuredData.ActionType.Ride -> {
            CreateAdInfoTag(text = "1 пассажир", icon = Icons.Outlined.Person)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Для поездки оставляем короткий сценарий без лишних параметров.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            CompactChoiceFlow(
                items = listOf(false, true),
                selected = draft.attributes.needsTrunk,
                title = { if (it) "Нужен багажник" else "Без багажа" },
                icon = { if (it) Icons.Outlined.LocalShipping else Icons.Outlined.Person },
                onSelected = { needsTrunk ->
                    onConditionChanged(AnnouncementConditionOption.NeedsTrunk, needsTrunk)
                },
            )
        }

        AnnouncementStructuredData.ActionType.ProHelp -> {
            CompactChoiceFlow(
                items = AnnouncementCreateHelpType.entries.toList(),
                selected = draft.helpType,
                title = { it.title },
                icon = { helpIcon(it) },
                onSelected = onHelpTypeSelected,
            )
            Spacer(modifier = Modifier.height(14.dp))
            CreateAdTextField(
                label = draft.taskBriefLabel,
                value = draft.taskBrief,
                onValueChange = onTaskBriefChanged,
                placeholder = draft.taskBriefPlaceholder,
            )
        }

        AnnouncementStructuredData.ActionType.Other -> {
            CreateAdTextField(
                label = draft.taskBriefLabel,
                value = draft.taskBrief,
                onValueChange = onTaskBriefChanged,
                placeholder = draft.taskBriefPlaceholder,
            )
        }

        null -> Unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> CompactChoiceFlow(
    items: List<T>,
    selected: T?,
    title: (T) -> String,
    icon: (T) -> ImageVector?,
    onSelected: (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { item ->
            CreateAdChoiceChip(
                text = title(item),
                icon = icon(item),
                compact = true,
                selected = selected == item,
                onClick = { onSelected(item) },
            )
        }
    }
}

@Composable
private fun FinalSummaryCard(draft: AnnouncementCreateFormDraft) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InnerCardRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            TurquoiseAccent.copy(alpha = 0.16f),
                            CardBackground.copy(alpha = 0.92f),
                        ),
                    ),
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryLine("Сценарий", draft.actionType?.title ?: "Черновик")
            SummaryLine("Категория", draft.mainGroup.title)
            SummaryLine("Кратко", draft.objectSummary)
            SummaryLine("Маршрут", draft.routeSummary)
            SummaryLine("Когда", draft.timeSummary)
            SummaryLine("Цена", draft.budgetSummary)
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(76.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MediaPreviewItem(
    media: AnnouncementSelectedMedia,
    onRemove: () -> Unit,
) {
    Box(modifier = Modifier.size(86.dp)) {
        AsyncImage(
            model = Uri.parse(media.uriString),
            contentDescription = "Фото объявления",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(GalleryImageRadius)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(22.dp)
                .clickable(onClick = onRemove),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.55f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Удалить фото",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun CreateInfoBanner(title: String, text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InnerCardRadius),
        color = CardBackground.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, TurquoiseAccent.copy(alpha = 0.18f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun CreateWarningBanner(text: String, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InnerCardRadius),
        color = Color(0xFFFFF3E0),
        border = BorderStroke(1.dp, Color(0xFFE8A16C).copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = Color(0xFFE8A16C),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Скрыть сообщение",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ModerationHelp(mark: DraftModerationMark?) {
    if (mark == null) return
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = mark.details,
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFFE08D49),
    )
}

@Composable
private fun DateTimeField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    val context = LocalContext.current
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    showDateTimePicker(
                        initial = parsePickerInstant(value),
                        context = context,
                        onSelected = onValueChange,
                    )
                },
            shape = RoundedCornerShape(12.dp),
            color = CardBackground,
            border = BorderStroke(1.dp, TurquoiseAccent.copy(alpha = 0.18f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = TurquoiseAccent,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = value.ifBlank { placeholder },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isBlank()) TextSecondary else TextPrimary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun showDateTimePicker(
    context: android.content.Context,
    initial: Instant,
    onSelected: (String) -> Unit,
) {
    val zoneId = ZoneId.systemDefault()
    val initialDate = ZonedDateTime.ofInstant(initial, zoneId)
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val picked = ZonedDateTime.of(
                        year,
                        month + 1,
                        dayOfMonth,
                        hourOfDay,
                        minute,
                        0,
                        0,
                        zoneId,
                    ).toInstant()
                    onSelected(picked.toString())
                },
                initialDate.hour,
                initialDate.minute,
                true,
            ).show()
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth,
    ).show()
}

private fun parsePickerInstant(displayValue: String): Instant =
    runCatching { Instant.parse(displayValue) }.getOrElse { Instant.now().plusSeconds(60 * 60) }

private fun actionSubtitle(action: AnnouncementStructuredData.ActionType): String = when (action) {
    AnnouncementStructuredData.ActionType.Pickup -> "Забрать и отвезти"
    AnnouncementStructuredData.ActionType.Buy -> "Купить и привезти"
    AnnouncementStructuredData.ActionType.Carry -> "Донести, поднять, перенести"
    AnnouncementStructuredData.ActionType.Ride -> "Подвезти пассажира"
    AnnouncementStructuredData.ActionType.ProHelp -> "Быстрая помощь специалиста"
    AnnouncementStructuredData.ActionType.Other -> "Нестандартное поручение"
}

private fun sourceSectionSubtitle(action: AnnouncementStructuredData.ActionType?): String = when (action) {
    AnnouncementStructuredData.ActionType.Pickup -> "Выберите точку забора и укажите адрес."
    AnnouncementStructuredData.ActionType.Buy -> "Отметьте тип точки покупки и место, где удобнее купить."
    AnnouncementStructuredData.ActionType.Carry -> "Укажите стартовую точку и адрес, откуда нужно начать перенос."
    AnnouncementStructuredData.ActionType.Ride -> "Подача водителя или попутчика начинается отсюда."
    AnnouncementStructuredData.ActionType.ProHelp -> "По этому адресу исполнителю нужно приехать для помощи."
    AnnouncementStructuredData.ActionType.Other -> "Укажите основную точку, где начинается задача."
    null -> ""
}

private fun destinationSectionSubtitle(action: AnnouncementStructuredData.ActionType?): String = when (action) {
    AnnouncementStructuredData.ActionType.Pickup,
    AnnouncementStructuredData.ActionType.Buy,
    -> "Точка назначения обязательна для доставки."

    AnnouncementStructuredData.ActionType.Carry -> "Можно оставить пустым, если нужен только подъём или спуск на месте."
    AnnouncementStructuredData.ActionType.Ride -> "Укажите, куда подвезти пассажира."
    else -> ""
}

private fun sourceTitle(
    action: AnnouncementStructuredData.ActionType?,
    kind: AnnouncementStructuredData.SourceKind,
): String = when {
    action == AnnouncementStructuredData.ActionType.Buy && kind == AnnouncementStructuredData.SourceKind.Venue -> "В заведении"
    action == AnnouncementStructuredData.ActionType.Buy && kind == AnnouncementStructuredData.SourceKind.Address -> "В конкретном месте"
    action == AnnouncementStructuredData.ActionType.Buy && kind == AnnouncementStructuredData.SourceKind.Other -> "Где угодно рядом"
    action == AnnouncementStructuredData.ActionType.ProHelp && kind == AnnouncementStructuredData.SourceKind.Address -> "По адресу"
    action == AnnouncementStructuredData.ActionType.ProHelp && kind == AnnouncementStructuredData.SourceKind.Office -> "В офисе"
    action == AnnouncementStructuredData.ActionType.ProHelp && kind == AnnouncementStructuredData.SourceKind.Venue -> "В заведении"
    else -> kind.title
}

private fun destinationTitle(
    action: AnnouncementStructuredData.ActionType?,
    kind: AnnouncementStructuredData.DestinationKind,
): String = when {
    action == AnnouncementStructuredData.ActionType.Ride && kind == AnnouncementStructuredData.DestinationKind.Metro -> "К метро"
    else -> kind.title
}

private fun actionIcon(action: AnnouncementStructuredData.ActionType): ImageVector = when (action) {
    AnnouncementStructuredData.ActionType.Pickup -> Icons.Outlined.Inventory2
    AnnouncementStructuredData.ActionType.Buy -> Icons.Outlined.ShoppingCart
    AnnouncementStructuredData.ActionType.Carry -> Icons.Outlined.OpenWith
    AnnouncementStructuredData.ActionType.Ride -> Icons.Outlined.DirectionsCar
    AnnouncementStructuredData.ActionType.ProHelp -> Icons.Outlined.Build
    AnnouncementStructuredData.ActionType.Other -> Icons.Outlined.AutoAwesome
}

private fun itemIcon(itemType: AnnouncementCreateItemType): ImageVector = when (itemType) {
    AnnouncementCreateItemType.Groceries -> Icons.Outlined.ShoppingCart
    AnnouncementCreateItemType.Documents -> Icons.Outlined.Inventory2
    AnnouncementCreateItemType.Electronics -> Icons.Outlined.Build
    AnnouncementCreateItemType.FragileItem -> Icons.Outlined.Warning
    AnnouncementCreateItemType.Bags -> Icons.Outlined.Checkroom
    AnnouncementCreateItemType.BulkyItem -> Icons.Outlined.LocalShipping
    AnnouncementCreateItemType.Other -> Icons.Outlined.AutoAwesome
}

private fun purchaseIcon(type: AnnouncementCreatePurchaseType): ImageVector = when (type) {
    AnnouncementCreatePurchaseType.Groceries -> Icons.Outlined.ShoppingCart
    AnnouncementCreatePurchaseType.Medicine -> Icons.Outlined.Medication
    AnnouncementCreatePurchaseType.Clothing -> Icons.Outlined.Checkroom
    AnnouncementCreatePurchaseType.Electronics -> Icons.Outlined.Build
    AnnouncementCreatePurchaseType.HomeGoods -> Icons.Outlined.Apartment
    AnnouncementCreatePurchaseType.Other -> Icons.Outlined.AutoAwesome
}

private fun helpIcon(type: AnnouncementCreateHelpType): ImageVector = when (type) {
    AnnouncementCreateHelpType.Consultation -> Icons.Outlined.Phone
    AnnouncementCreateHelpType.SetupDevice -> Icons.Outlined.Build
    AnnouncementCreateHelpType.InstallOrConnect -> Icons.Outlined.DragHandle
    AnnouncementCreateHelpType.MinorRepair -> Icons.Outlined.Build
    AnnouncementCreateHelpType.Diagnose -> Icons.Outlined.Warning
    AnnouncementCreateHelpType.Other -> Icons.Outlined.AutoAwesome
}

private fun sourceIcon(kind: AnnouncementStructuredData.SourceKind): ImageVector = when (kind) {
    AnnouncementStructuredData.SourceKind.Person -> Icons.Outlined.Person
    AnnouncementStructuredData.SourceKind.PickupPoint -> Icons.Outlined.Inventory2
    AnnouncementStructuredData.SourceKind.Venue -> Icons.Outlined.Storefront
    AnnouncementStructuredData.SourceKind.Address -> Icons.Outlined.LocationOn
    AnnouncementStructuredData.SourceKind.Office -> Icons.Outlined.Business
    AnnouncementStructuredData.SourceKind.Other -> Icons.Outlined.AutoAwesome
}

private fun destinationIcon(kind: AnnouncementStructuredData.DestinationKind): ImageVector = when (kind) {
    AnnouncementStructuredData.DestinationKind.Person -> Icons.Outlined.Person
    AnnouncementStructuredData.DestinationKind.Address -> Icons.Outlined.LocationOn
    AnnouncementStructuredData.DestinationKind.Office -> Icons.Outlined.Business
    AnnouncementStructuredData.DestinationKind.Entrance -> Icons.Outlined.Apartment
    AnnouncementStructuredData.DestinationKind.Metro -> Icons.Outlined.Train
    AnnouncementStructuredData.DestinationKind.Other -> Icons.Outlined.AutoAwesome
}

private fun urgencyIcon(urgency: AnnouncementStructuredData.Urgency): ImageVector = when (urgency) {
    AnnouncementStructuredData.Urgency.Now -> Icons.Outlined.Bolt
    AnnouncementStructuredData.Urgency.Today -> Icons.Outlined.WbSunny
    AnnouncementStructuredData.Urgency.Scheduled -> Icons.Outlined.CalendarMonth
    AnnouncementStructuredData.Urgency.Flexible -> Icons.Outlined.Schedule
}

private fun conditionIcon(option: AnnouncementConditionOption): ImageVector = when (option) {
    AnnouncementConditionOption.RequiresVehicle -> Icons.Outlined.DirectionsCar
    AnnouncementConditionOption.NeedsTrunk -> Icons.Outlined.LocalShipping
    AnnouncementConditionOption.RequiresCarefulHandling -> Icons.Outlined.Warning
    AnnouncementConditionOption.RequiresLiftToFloor -> Icons.Outlined.OpenWith
    AnnouncementConditionOption.HasElevator -> Icons.Outlined.Elevator
    AnnouncementConditionOption.NeedsLoader -> Icons.Outlined.Person
    AnnouncementConditionOption.WaitOnSite -> Icons.Outlined.HourglassBottom
    AnnouncementConditionOption.CallBeforeArrival -> Icons.Outlined.Phone
    AnnouncementConditionOption.RequiresConfirmationCode -> Icons.Outlined.Password
    AnnouncementConditionOption.Contactless -> Icons.Outlined.Person
    AnnouncementConditionOption.RequiresReceipt -> Icons.Outlined.ReceiptLong
    AnnouncementConditionOption.PhotoReportRequired -> Icons.Outlined.CameraAlt
}
