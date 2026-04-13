package com.vzaimno.app.feature.ads.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.AnnouncementStructuredData
import com.vzaimno.app.feature.ads.AdsFilterBucket
import com.vzaimno.app.feature.shell.navigation.HideShellBottomBarEffect
import kotlinx.coroutines.flow.collectLatest

private val MilkBackground = Color(0xFFF5F2ED)
private val TurquoiseAccent = Color(0xFF2BA8A4)
private val TurquoiseLight = Color(0xFFD6F5F3)
private val TurquoiseBorder = Color(0xFF5ECFCB)
private val CardWhite = Color(0xFFFFFFFF)
private val SectionCardShape = RoundedCornerShape(18.dp)

@Composable
fun AnnouncementCreateRoute(
    onBack: () -> Unit,
    onSubmitted: (AdsFilterBucket, String?) -> Unit,
    viewModel: AnnouncementCreateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8),
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
        onTitleChanged = viewModel::onTitleChanged,
        onItemTypeSelected = viewModel::onItemTypeSelected,
        onPurchaseTypeSelected = viewModel::onPurchaseTypeSelected,
        onHelpTypeSelected = viewModel::onHelpTypeSelected,
        onSourceKindSelected = viewModel::onSourceKindSelected,
        onDestinationKindSelected = viewModel::onDestinationKindSelected,
        onUrgencySelected = viewModel::onUrgencySelected,
        onSourceAddressChanged = viewModel::onSourceAddressChanged,
        onDestinationAddressChanged = viewModel::onDestinationAddressChanged,
        onBudgetMinChanged = viewModel::onBudgetMinChanged,
        onBudgetMaxChanged = viewModel::onBudgetMaxChanged,
        onTaskBriefChanged = viewModel::onTaskBriefChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onContactNameChanged = viewModel::onContactNameChanged,
        onContactPhoneChanged = viewModel::onContactPhoneChanged,
        onContactMethodSelected = viewModel::onContactMethodSelected,
        onAudienceSelected = viewModel::onAudienceSelected,
        onEstimatedTaskMinutesChanged = viewModel::onEstimatedTaskMinutesChanged,
        onWaitingMinutesChanged = viewModel::onWaitingMinutesChanged,
        onFloorChanged = viewModel::onFloorChanged,
        onWeightCategorySelected = viewModel::onWeightCategorySelected,
        onSizeCategorySelected = viewModel::onSizeCategorySelected,
        onAttributeToggleChanged = viewModel::onAttributeToggleChanged,
        onPickMedia = {
            mediaPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onRemoveMedia = viewModel::removeMedia,
        onDismissMessage = viewModel::clearInlineMessage,
        onToggleSummary = viewModel::toggleSummaryExpanded,
        onSubmit = viewModel::submit,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementCreateScreen(
    state: AnnouncementCreateUiState,
    onBack: () -> Unit,
    onActionTypeSelected: (AnnouncementStructuredData.ActionType) -> Unit,
    onTitleChanged: (String) -> Unit,
    onItemTypeSelected: (AnnouncementCreateItemType?) -> Unit,
    onPurchaseTypeSelected: (AnnouncementCreatePurchaseType?) -> Unit,
    onHelpTypeSelected: (AnnouncementCreateHelpType?) -> Unit,
    onSourceKindSelected: (AnnouncementStructuredData.SourceKind?) -> Unit,
    onDestinationKindSelected: (AnnouncementStructuredData.DestinationKind?) -> Unit,
    onUrgencySelected: (AnnouncementStructuredData.Urgency) -> Unit,
    onSourceAddressChanged: (String) -> Unit,
    onDestinationAddressChanged: (String) -> Unit,
    onBudgetMinChanged: (String) -> Unit,
    onBudgetMaxChanged: (String) -> Unit,
    onTaskBriefChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onContactNameChanged: (String) -> Unit,
    onContactPhoneChanged: (String) -> Unit,
    onContactMethodSelected: (AnnouncementContactMethod) -> Unit,
    onAudienceSelected: (AnnouncementAudience) -> Unit,
    onEstimatedTaskMinutesChanged: (String) -> Unit,
    onWaitingMinutesChanged: (String) -> Unit,
    onFloorChanged: (String) -> Unit,
    onWeightCategorySelected: (AnnouncementStructuredData.WeightCategory?) -> Unit,
    onSizeCategorySelected: (AnnouncementStructuredData.SizeCategory?) -> Unit,
    onAttributeToggleChanged: (AnnouncementAttributeToggle, Boolean) -> Unit,
    onPickMedia: () -> Unit,
    onRemoveMedia: (String) -> Unit,
    onDismissMessage: () -> Unit,
    onToggleSummary: () -> Unit,
    onSubmit: () -> Unit,
) {
    HideShellBottomBarEffect(reason = "ads_create")

    val draft = state.draft

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MilkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Sticky mini summary is part of the top bar area
            Column {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            enabled = !state.isSubmitting,
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
                // Pinned sticky summary - always visible
                StickyMiniSummary(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    draft = draft,
                    isExpanded = state.isSummaryExpanded,
                    onToggle = onToggleSummary,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                )
                .imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Inline message
            if (!state.inlineMessage.isNullOrBlank()) {
                item {
                    CreateInlineMessageCard(
                        message = state.inlineMessage,
                        onDismiss = onDismissMessage,
                    )
                }
            }

            // Prefill loading
            if (state.isPrefillLoading) {
                item {
                    CreateLoadingCard(
                        message = stringResource(R.string.ads_create_prefill_loading),
                    )
                }
            }

            // Header
            item {
                Column(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ads_create_flow_header),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.ads_create_flow_subheader),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 1. Action/Scenario Selection
            item {
                FlowSectionCard(
                    title = stringResource(R.string.ads_create_flow_action_section),
                    subtitle = stringResource(R.string.ads_create_flow_action_hint),
                ) {
                    ActionTypeGrid(
                        selectedAction = draft.actionType,
                        onActionSelected = onActionTypeSelected,
                    )
                }
            }

            // Structured sections
            if (draft.actionType != null) {
                // 2. Object Section
                item {
                    FlowSectionCard(
                        title = stringResource(R.string.ads_create_flow_object_section),
                    ) {
                        when (draft.actionType) {
                            AnnouncementStructuredData.ActionType.Pickup,
                            AnnouncementStructuredData.ActionType.Carry,
                            -> FlowChipGroup(
                                values = draft.availableItemTypes,
                                selectedValue = draft.itemType,
                                label = { stringResource(it.titleRes) },
                                onSelected = onItemTypeSelected,
                            )

                            AnnouncementStructuredData.ActionType.Buy -> FlowChipGroup(
                                values = AnnouncementCreatePurchaseType.entries,
                                selectedValue = draft.purchaseType,
                                label = { stringResource(it.titleRes) },
                                onSelected = onPurchaseTypeSelected,
                            )

                            AnnouncementStructuredData.ActionType.ProHelp -> FlowChipGroup(
                                values = AnnouncementCreateHelpType.entries,
                                selectedValue = draft.helpType,
                                label = { stringResource(it.titleRes) },
                                onSelected = onHelpTypeSelected,
                            )

                            else -> Unit
                        }

                        if (draft.showsTaskBriefField) {
                            FlowTextField(
                                value = draft.taskBrief,
                                onValueChange = onTaskBriefChanged,
                                label = stringResource(R.string.ads_field_task_brief),
                                placeholder = stringResource(taskBriefPlaceholderRes(draft.actionType)),
                                error = state.fieldErrors.taskBrief,
                            )
                        }
                    }
                }

                // 3. Source Section
                item {
                    FlowSectionCard(
                        title = stringResource(R.string.ads_create_flow_source_section),
                    ) {
                        FlowChipGroup(
                            values = draft.availableSourceKinds,
                            selectedValue = draft.sourceKind,
                            label = { it.title },
                            onSelected = onSourceKindSelected,
                        )
                        FlowTextField(
                            value = draft.source.address,
                            onValueChange = onSourceAddressChanged,
                            label = stringResource(R.string.ads_field_source_address),
                            placeholder = stringResource(sourceAddressPlaceholderRes(draft.actionType)),
                            error = state.fieldErrors.sourceAddress,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.MyLocation,
                                    contentDescription = null,
                                    tint = TurquoiseAccent,
                                )
                            },
                        )
                    }
                }

                // 4. Destination Section
                if (draft.showsDestinationSection) {
                    item {
                        FlowSectionCard(
                            title = stringResource(R.string.ads_create_flow_destination_section),
                        ) {
                            FlowChipGroup(
                                values = draft.availableDestinationKinds,
                                selectedValue = draft.destinationKind,
                                label = { it.title },
                                onSelected = onDestinationKindSelected,
                            )
                            FlowTextField(
                                value = draft.destination.address,
                                onValueChange = onDestinationAddressChanged,
                                label = stringResource(R.string.ads_field_destination_address),
                                placeholder = stringResource(destinationAddressPlaceholderRes(draft.actionType)),
                                error = state.fieldErrors.destinationAddress,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Route,
                                        contentDescription = null,
                                        tint = TurquoiseAccent,
                                    )
                                },
                            )
                        }
                    }
                }

                // 5. When Section
                item {
                    FlowSectionCard(
                        title = stringResource(R.string.ads_create_flow_when_section),
                    ) {
                        FlowChipGroup(
                            values = AnnouncementStructuredData.Urgency.entries,
                            selectedValue = draft.urgency,
                            label = { it.title },
                            onSelected = onUrgencySelected,
                        )
                    }
                }

                // 6. Conditions Section
                if (draft.availableAttributeToggles.isNotEmpty()) {
                    item {
                        FlowSectionCard(
                            title = stringResource(R.string.ads_create_flow_conditions_section),
                            subtitle = stringResource(R.string.ads_create_flow_conditions_hint),
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                draft.availableAttributeToggles.forEach { toggle ->
                                    ConditionToggleRow(
                                        title = stringResource(toggle.titleRes),
                                        checked = draft.attributes.valueFor(toggle),
                                        onCheckedChange = { enabled ->
                                            onAttributeToggleChanged(toggle, enabled)
                                        },
                                    )
                                }
                            }
                            if (draft.attributes.requiresLiftToFloor) {
                                FlowTextField(
                                    value = draft.attributes.floor,
                                    onValueChange = onFloorChanged,
                                    label = stringResource(R.string.ads_create_floor_label),
                                    placeholder = stringResource(R.string.ads_create_floor_placeholder),
                                    keyboardType = KeyboardType.Number,
                                    error = state.fieldErrors.floor,
                                )
                            }
                            if (draft.attributes.waitOnSite) {
                                FlowTextField(
                                    value = draft.attributes.waitingMinutes,
                                    onValueChange = onWaitingMinutesChanged,
                                    label = stringResource(R.string.ads_field_waiting),
                                    placeholder = stringResource(R.string.ads_create_waiting_placeholder),
                                    keyboardType = KeyboardType.Number,
                                    error = state.fieldErrors.waitingMinutes,
                                )
                            }
                        }
                    }
                }

                // 7. Cargo Section
                if (draft.actionType in setOf(
                        AnnouncementStructuredData.ActionType.Pickup,
                        AnnouncementStructuredData.ActionType.Buy,
                        AnnouncementStructuredData.ActionType.Carry,
                    )
                ) {
                    item {
                        FlowSectionCard(
                            title = stringResource(R.string.ads_create_flow_cargo_section),
                        ) {
                            FlowChipGroup(
                                title = stringResource(R.string.ads_field_weight),
                                values = AnnouncementStructuredData.WeightCategory.entries,
                                selectedValue = draft.attributes.weightCategory,
                                label = { it.title },
                                onSelected = onWeightCategorySelected,
                            )
                            FlowChipGroup(
                                title = stringResource(R.string.ads_field_size),
                                values = AnnouncementStructuredData.SizeCategory.entries,
                                selectedValue = draft.attributes.sizeCategory,
                                label = { it.title },
                                onSelected = onSizeCategorySelected,
                            )
                        }
                    }
                }

                // 8. Price Section - RANGE ONLY, no Fixed
                item {
                    val recommendedPrice = draft.recommendedPriceRange()
                    FlowSectionCard(
                        title = stringResource(R.string.ads_create_flow_price_section),
                        subtitle = stringResource(R.string.ads_create_flow_price_hint),
                    ) {
                        // Recommended price
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = TurquoiseLight,
                            border = BorderStroke(1.dp, TurquoiseBorder.copy(alpha = 0.3f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.ads_create_flow_recommended_price),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TurquoiseAccent,
                                )
                                Text(
                                    text = recommendedPrice.text,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text = stringResource(R.string.ads_create_flow_price_disclaimer),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // Range only: От / До
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            FlowTextField(
                                modifier = Modifier.weight(1f),
                                value = draft.budget.min,
                                onValueChange = onBudgetMinChanged,
                                label = stringResource(R.string.ads_create_budget_min_label),
                                placeholder = "${recommendedPrice.min}",
                                keyboardType = KeyboardType.Number,
                                error = state.fieldErrors.budgetMin,
                            )
                            FlowTextField(
                                modifier = Modifier.weight(1f),
                                value = draft.budget.max,
                                onValueChange = onBudgetMaxChanged,
                                label = stringResource(R.string.ads_create_budget_max_label),
                                placeholder = "${recommendedPrice.max}",
                                keyboardType = KeyboardType.Number,
                                error = state.fieldErrors.budgetMax,
                            )
                        }
                    }
                }

                // 9. Contact Section
                item {
                    FlowSectionCard(
                        title = stringResource(R.string.ads_create_flow_contact_section),
                    ) {
                        FlowTextField(
                            value = draft.contacts.name,
                            onValueChange = onContactNameChanged,
                            label = stringResource(R.string.ads_create_contact_name_label),
                            placeholder = stringResource(R.string.ads_create_contact_name_placeholder),
                        )
                        FlowTextField(
                            value = draft.contacts.phone,
                            onValueChange = onContactPhoneChanged,
                            label = stringResource(R.string.ads_create_contact_phone_label),
                            placeholder = stringResource(R.string.ads_create_contact_phone_placeholder),
                            keyboardType = KeyboardType.Phone,
                            error = state.fieldErrors.contactPhone,
                        )
                        FlowChipGroup(
                            values = AnnouncementContactMethod.entries,
                            selectedValue = draft.contacts.method,
                            label = { stringResource(it.titleRes) },
                            onSelected = onContactMethodSelected,
                        )
                        FlowChipGroup(
                            values = AnnouncementAudience.entries,
                            selectedValue = draft.contacts.audience,
                            label = { stringResource(it.titleRes) },
                            onSelected = onAudienceSelected,
                        )
                    }
                }

                // 10. Additional (Title + Description + Media)
                item {
                    FlowSectionCard(
                        title = stringResource(R.string.ads_create_flow_additional_section),
                    ) {
                        // Title is auto-filled, user can optionally edit
                        FlowTextField(
                            value = draft.title,
                            onValueChange = onTitleChanged,
                            label = stringResource(R.string.ads_field_title),
                            placeholder = draft.generatedTitle(),
                        )
                        // Description
                        FlowTextField(
                            value = draft.notes,
                            onValueChange = onNotesChanged,
                            label = stringResource(R.string.ads_field_description),
                            placeholder = stringResource(R.string.ads_create_notes_placeholder),
                            minLines = 3,
                            maxLines = 5,
                        )
                        // Media
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.ads_create_media_caption),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                enabled = !state.isBusy,
                                onClick = onPickMedia,
                                colors = ButtonDefaults.buttonColors(containerColor = TurquoiseAccent),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.ads_create_media_button),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                        if (draft.media.isNotEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(items = draft.media, key = { it.id }) { media ->
                                    MediaPreviewCard(
                                        media = media,
                                        onRemove = { onRemoveMedia(media.id) },
                                    )
                                }
                            }
                        }
                    }
                }

                // 11. Final Summary Card
                item { FinalSummaryCard(draft = draft) }

                // 12. Readiness or Submit
                item {
                    val issues = draft.readinessIssues()
                    if (issues.isNotEmpty()) {
                        ReadinessCard(issues = issues)
                    } else {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !state.isBusy,
                            onClick = onSubmit,
                            colors = ButtonDefaults.buttonColors(containerColor = TurquoiseAccent),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.ads_create_flow_submit),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            }
                        }
                    }
                }
            } else {
                // No scenario selected
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = SectionCardShape,
                        color = CardWhite.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, TurquoiseBorder.copy(alpha = 0.15f)),
                    ) {
                        Text(
                            text = stringResource(R.string.ads_create_flow_no_scenario),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                }
            }
        }
    }
}

// ── Sticky Mini Summary ──────────────────────────────────────────────

@Composable
private fun StickyMiniSummary(
    draft: AnnouncementCreateFormDraft,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        color = CardWhite.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, TurquoiseBorder.copy(alpha = 0.25f)),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = draft.generatedTitle(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (isExpanded) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = TurquoiseAccent,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniChip(text = draft.actionSummaryText())
                MiniChip(text = draft.objectSummaryText())
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniSummaryRow(
                        label = stringResource(R.string.ads_create_flow_mini_route),
                        value = draft.routeSummaryText(),
                    )
                    MiniSummaryRow(
                        label = stringResource(R.string.ads_create_flow_mini_when),
                        value = draft.whenSummaryText(),
                    )
                    MiniSummaryRow(
                        label = stringResource(R.string.ads_create_flow_mini_price),
                        value = draft.priceSummaryText(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniChip(text: String) {
    Surface(shape = RoundedCornerShape(10.dp), color = TurquoiseLight) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = TurquoiseAccent,
        )
    }
}

@Composable
private fun MiniSummaryRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Action Type Grid ─────────────────────────────────────────────────

@Composable
private fun ActionTypeGrid(
    selectedAction: AnnouncementStructuredData.ActionType?,
    onActionSelected: (AnnouncementStructuredData.ActionType) -> Unit,
) {
    val actions = listOf(
        Triple(AnnouncementStructuredData.ActionType.Pickup, R.string.ads_create_flow_action_pickup, R.string.ads_create_flow_action_pickup_sub),
        Triple(AnnouncementStructuredData.ActionType.Buy, R.string.ads_create_flow_action_buy, R.string.ads_create_flow_action_buy_sub),
        Triple(AnnouncementStructuredData.ActionType.Carry, R.string.ads_create_flow_action_carry, R.string.ads_create_flow_action_carry_sub),
        Triple(AnnouncementStructuredData.ActionType.Ride, R.string.ads_create_flow_action_ride, R.string.ads_create_flow_action_ride_sub),
        Triple(AnnouncementStructuredData.ActionType.ProHelp, R.string.ads_create_flow_action_pro_help, R.string.ads_create_flow_action_pro_help_sub),
        Triple(AnnouncementStructuredData.ActionType.Other, R.string.ads_create_flow_action_other, R.string.ads_create_flow_action_other_sub),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        actions.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { (actionType, titleRes, subtitleRes) ->
                    val isSelected = selectedAction == actionType
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 80.dp)
                            .clickable { onActionSelected(actionType) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) TurquoiseLight else CardWhite,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) TurquoiseAccent else MaterialTheme.colorScheme.outlineVariant,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(titleRes),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) TurquoiseAccent else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(subtitleRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ── Final Summary Card ───────────────────────────────────────────────

@Composable
private fun FinalSummaryCard(draft: AnnouncementCreateFormDraft) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SectionCardShape,
        color = CardWhite,
        border = BorderStroke(1.dp, TurquoiseBorder.copy(alpha = 0.25f)),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.ads_create_flow_summary_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = draft.generatedTitle(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            // Tags
            val tags = draft.activeConditionTags()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MiniChip(text = draft.actionSummaryText())
                MiniChip(text = draft.objectSummaryText())
                MiniChip(text = draft.whenSummaryText())
                tags.take(4).forEach { tag -> MiniChip(text = tag) }
            }
            // Summary rows
            SummaryRow(stringResource(R.string.ads_create_flow_summary_scenario), draft.actionSummaryText())
            SummaryRow(stringResource(R.string.ads_create_flow_summary_object), draft.objectSummaryText())
            SummaryRow(stringResource(R.string.ads_create_flow_summary_route), draft.routeSummaryText())
            SummaryRow(stringResource(R.string.ads_create_flow_summary_when), draft.whenSummaryText())
            SummaryRow(stringResource(R.string.ads_create_flow_summary_price), draft.priceSummaryText())
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(100.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
    }
}

// ── Readiness Card ───────────────────────────────────────────────────

@Composable
private fun ReadinessCard(issues: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SectionCardShape,
        color = CardWhite,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.ads_create_flow_readiness_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            issues.take(6).forEach { issue ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Outlined.Error, contentDescription = null, tint = TurquoiseAccent, modifier = Modifier.size(18.dp))
                    Text(text = issue, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

// ── Section Card ─────────────────────────────────────────────────────

@Composable
private fun FlowSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SectionCardShape,
        color = CardWhite.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, TurquoiseBorder.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}

// ── Chip Group ───────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FlowChipGroup(
    values: List<T>,
    selectedValue: T?,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    title: String? = null,
) {
    if (values.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { value ->
                val isSelected = value == selectedValue
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelected(value) },
                    label = { Text(text = label(value), style = MaterialTheme.typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TurquoiseLight,
                        selectedLabelColor = TurquoiseAccent,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) TurquoiseAccent else MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = TurquoiseAccent,
                        enabled = true,
                        selected = isSelected,
                    ),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }
    }
}

// ── Condition Toggle ─────────────────────────────────────────────────

@Composable
private fun ConditionToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) },
        color = Color.Transparent,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(modifier = Modifier.weight(1f), text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = if (checked) TurquoiseAccent else Color.Transparent,
                border = BorderStroke(if (checked) 0.dp else 1.5.dp, if (checked) Color.Transparent else MaterialTheme.colorScheme.outlineVariant),
            ) {
                if (checked) {
                    Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp).padding(2.dp))
                }
            }
        }
    }
}

// ── Text Field ───────────────────────────────────────────────────────

@Composable
private fun FlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        leadingIcon = leadingIcon,
        isError = error != null,
        supportingText = error?.let { { Text(text = it) } },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = keyboardType,
            imeAction = ImeAction.Next,
        ),
        singleLine = maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(14.dp),
    )
}

// ── Misc helpers ─────────────────────────────────────────────────────

@Composable
private fun CreateLoadingCard(message: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = SectionCardShape, color = CardWhite.copy(alpha = 0.96f)) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = TurquoiseAccent)
            Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun CreateInlineMessageCard(message: String?, onDismiss: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = SectionCardShape, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(modifier = Modifier.weight(1f), text = message.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.ads_inline_error_dismiss)) }
        }
    }
}

@Composable
private fun MediaPreviewCard(media: AnnouncementSelectedMedia, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(width = 120.dp, height = 96.dp)) {
        AsyncImage(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)), model = media.uriString, contentDescription = stringResource(R.string.ads_image_preview_description), contentScale = ContentScale.Crop)
        Surface(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp), shape = CircleShape, color = Color.Black.copy(alpha = 0.48f)) {
            IconButton(modifier = Modifier.size(28.dp), onClick = onRemove) {
                Icon(imageVector = Icons.Outlined.Close, contentDescription = stringResource(R.string.ads_action_cancel), tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun taskBriefPlaceholderRes(actionType: AnnouncementStructuredData.ActionType?): Int = when (actionType) {
    AnnouncementStructuredData.ActionType.ProHelp -> R.string.ads_create_task_brief_placeholder_help
    AnnouncementStructuredData.ActionType.Other -> R.string.ads_create_task_brief_placeholder_other
    else -> R.string.ads_create_task_brief_placeholder_generic
}

private fun sourceAddressPlaceholderRes(actionType: AnnouncementStructuredData.ActionType?): Int = when (actionType) {
    AnnouncementStructuredData.ActionType.Buy -> R.string.ads_create_source_placeholder_buy
    AnnouncementStructuredData.ActionType.Ride -> R.string.ads_create_source_placeholder_ride
    AnnouncementStructuredData.ActionType.ProHelp -> R.string.ads_create_source_placeholder_help
    else -> R.string.ads_create_source_placeholder_generic
}

private fun destinationAddressPlaceholderRes(actionType: AnnouncementStructuredData.ActionType?): Int = when (actionType) {
    AnnouncementStructuredData.ActionType.Ride -> R.string.ads_create_destination_placeholder_ride
    AnnouncementStructuredData.ActionType.Carry -> R.string.ads_create_destination_placeholder_carry
    else -> R.string.ads_create_destination_placeholder_generic
}
