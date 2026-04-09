package com.vzaimno.app.feature.ads.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
        onMainGroupSelected = viewModel::onMainGroupSelected,
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
        onBudgetModeSelected = viewModel::onBudgetModeSelected,
        onBudgetAmountChanged = viewModel::onBudgetAmountChanged,
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
        onSubmit = viewModel::submit,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementCreateScreen(
    state: AnnouncementCreateUiState,
    onBack: () -> Unit,
    onMainGroupSelected: (AnnouncementMainGroup) -> Unit,
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
    onBudgetModeSelected: (AnnouncementBudgetMode) -> Unit,
    onBudgetAmountChanged: (String) -> Unit,
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
    onSubmit: () -> Unit,
) {
    HideShellBottomBarEffect(reason = "ads_create")

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (state.isCreateAgain) {
                                R.string.ads_create_again_title
                            } else {
                                R.string.ads_create_title
                            },
                        ),
                    )
                },
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
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = MaterialTheme.spacing.xLarge)
                        .padding(top = MaterialTheme.spacing.medium, bottom = MaterialTheme.spacing.large)
                        .height(56.dp),
                    enabled = !state.isBusy,
                    onClick = onSubmit,
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(text = stringResource(R.string.ads_create_submit))
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                    )
                    .imePadding(),
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.xLarge,
                    top = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.xLarge,
                    bottom = 148.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            ) {
                item {
                    CreateHeroCard(
                        state = state,
                    )
                }

                if (!state.inlineMessage.isNullOrBlank()) {
                    item {
                        CreateInlineMessageCard(
                            message = state.inlineMessage,
                            onDismiss = onDismissMessage,
                        )
                    }
                }

                if (state.isPrefillLoading) {
                    item {
                        CreateLoadingCard(
                            message = stringResource(R.string.ads_create_prefill_loading),
                        )
                    }
                }

                item {
                    CreateSectionCard(
                        title = stringResource(R.string.ads_create_section_main),
                    ) {
                        CreateSegmentedButtons(
                            values = AnnouncementMainGroup.entries,
                            selectedValue = state.draft.mainGroup,
                            label = { item -> stringResource(item.titleRes) },
                            onSelected = onMainGroupSelected,
                        )

                        SelectionChipGroup(
                            title = stringResource(R.string.ads_create_action_label),
                            values = state.draft.availableActionTypes,
                            selectedValue = state.draft.actionType,
                            label = { item -> item.title },
                            onSelected = { actionType ->
                                onActionTypeSelected(actionType)
                            },
                        )

                        CreateTextField(
                            value = state.draft.title,
                            onValueChange = onTitleChanged,
                            label = stringResource(R.string.ads_field_title),
                            placeholder = stringResource(R.string.ads_create_title_placeholder),
                            error = state.fieldErrors.title,
                        )
                    }
                }

                item {
                    CreateSectionCard(
                        title = stringResource(R.string.ads_create_section_object),
                    ) {
                        when (state.draft.actionType) {
                            AnnouncementStructuredData.ActionType.Pickup,
                            AnnouncementStructuredData.ActionType.Carry,
                            -> SelectionChipGroup(
                                title = stringResource(R.string.ads_field_item_type),
                                values = state.draft.availableItemTypes,
                                selectedValue = state.draft.itemType,
                                label = { item -> stringResource(item.titleRes) },
                                onSelected = onItemTypeSelected,
                            )

                            AnnouncementStructuredData.ActionType.Buy -> SelectionChipGroup(
                                title = stringResource(R.string.ads_field_purchase_type),
                                values = AnnouncementCreatePurchaseType.entries,
                                selectedValue = state.draft.purchaseType,
                                label = { item -> stringResource(item.titleRes) },
                                onSelected = onPurchaseTypeSelected,
                            )

                            AnnouncementStructuredData.ActionType.ProHelp -> SelectionChipGroup(
                                title = stringResource(R.string.ads_field_help_type),
                                values = AnnouncementCreateHelpType.entries,
                                selectedValue = state.draft.helpType,
                                label = { item -> stringResource(item.titleRes) },
                                onSelected = onHelpTypeSelected,
                            )

                            else -> Unit
                        }

                        if (state.draft.showsTaskBriefField) {
                            CreateTextField(
                                value = state.draft.taskBrief,
                                onValueChange = onTaskBriefChanged,
                                label = stringResource(R.string.ads_field_task_brief),
                                placeholder = stringResource(taskBriefPlaceholderRes(state.draft.actionType)),
                                error = state.fieldErrors.taskBrief,
                            )
                        }

                        CreateTextField(
                            value = state.draft.notes,
                            onValueChange = onNotesChanged,
                            label = stringResource(R.string.ads_field_description),
                            placeholder = stringResource(R.string.ads_create_notes_placeholder),
                            minLines = 4,
                            maxLines = 6,
                        )
                    }
                }

                item {
                    CreateSectionCard(
                        title = stringResource(R.string.ads_create_section_route),
                        icon = Icons.Outlined.Route,
                    ) {
                        SelectionChipGroup(
                            title = stringResource(R.string.ads_field_source_kind),
                            values = state.draft.availableSourceKinds,
                            selectedValue = state.draft.sourceKind,
                            label = { item -> item.title },
                            onSelected = onSourceKindSelected,
                        )

                        CreateTextField(
                            value = state.draft.source.address,
                            onValueChange = onSourceAddressChanged,
                            label = stringResource(R.string.ads_field_source_address),
                            placeholder = stringResource(sourceAddressPlaceholderRes(state.draft.actionType)),
                            error = state.fieldErrors.sourceAddress,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.MyLocation,
                                    contentDescription = null,
                                )
                            },
                        )

                        if (state.draft.showsDestinationSection) {
                            SelectionChipGroup(
                                title = stringResource(R.string.ads_field_destination_kind),
                                values = state.draft.availableDestinationKinds,
                                selectedValue = state.draft.destinationKind,
                                label = { item -> item.title },
                                onSelected = onDestinationKindSelected,
                            )

                            CreateTextField(
                                value = state.draft.destination.address,
                                onValueChange = onDestinationAddressChanged,
                                label = stringResource(R.string.ads_field_destination_address),
                                placeholder = stringResource(destinationAddressPlaceholderRes(state.draft.actionType)),
                                error = state.fieldErrors.destinationAddress,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Route,
                                        contentDescription = null,
                                    )
                                },
                            )
                        }

                        SelectionChipGroup(
                            title = stringResource(R.string.ads_field_urgency),
                            values = AnnouncementStructuredData.Urgency.entries,
                            selectedValue = state.draft.urgency,
                            label = { item -> item.title },
                            onSelected = onUrgencySelected,
                        )
                    }
                }

                item {
                    CreateSectionCard(
                        title = stringResource(R.string.ads_create_section_budget),
                        icon = Icons.Outlined.LocalOffer,
                    ) {
                        CreateSegmentedButtons(
                            values = AnnouncementBudgetMode.entries,
                            selectedValue = state.draft.budget.mode,
                            label = { item -> stringResource(item.titleRes) },
                            onSelected = onBudgetModeSelected,
                        )

                        if (state.draft.budget.mode == AnnouncementBudgetMode.Fixed) {
                            CreateTextField(
                                value = state.draft.budget.amount,
                                onValueChange = onBudgetAmountChanged,
                                label = stringResource(R.string.ads_create_budget_amount_label),
                                placeholder = stringResource(R.string.ads_create_budget_amount_placeholder),
                                keyboardType = KeyboardType.Number,
                                error = state.fieldErrors.budgetAmount,
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                            ) {
                                CreateTextField(
                                    modifier = Modifier.weight(1f),
                                    value = state.draft.budget.min,
                                    onValueChange = onBudgetMinChanged,
                                    label = stringResource(R.string.ads_create_budget_min_label),
                                    placeholder = stringResource(R.string.ads_create_budget_min_placeholder),
                                    keyboardType = KeyboardType.Number,
                                    error = state.fieldErrors.budgetMin,
                                )
                                CreateTextField(
                                    modifier = Modifier.weight(1f),
                                    value = state.draft.budget.max,
                                    onValueChange = onBudgetMaxChanged,
                                    label = stringResource(R.string.ads_create_budget_max_label),
                                    placeholder = stringResource(R.string.ads_create_budget_max_placeholder),
                                    keyboardType = KeyboardType.Number,
                                    error = state.fieldErrors.budgetMax,
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        ) {
                            CreateTextField(
                                modifier = Modifier.weight(1f),
                                value = state.draft.attributes.estimatedTaskMinutes,
                                onValueChange = onEstimatedTaskMinutesChanged,
                                label = stringResource(R.string.ads_field_duration),
                                placeholder = stringResource(R.string.ads_create_duration_placeholder),
                                keyboardType = KeyboardType.Number,
                                error = state.fieldErrors.estimatedTaskMinutes,
                            )

                            if (state.draft.attributes.waitOnSite) {
                                CreateTextField(
                                    modifier = Modifier.weight(1f),
                                    value = state.draft.attributes.waitingMinutes,
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

                item {
                    CreateSectionCard(
                        title = stringResource(R.string.ads_create_section_attributes),
                        icon = Icons.Outlined.Tune,
                    ) {
                        if (state.draft.actionType in setOf(
                                AnnouncementStructuredData.ActionType.Pickup,
                                AnnouncementStructuredData.ActionType.Buy,
                                AnnouncementStructuredData.ActionType.Carry,
                            )
                        ) {
                            SelectionChipGroup(
                                title = stringResource(R.string.ads_field_weight),
                                values = AnnouncementStructuredData.WeightCategory.entries,
                                selectedValue = state.draft.attributes.weightCategory,
                                label = { item -> item.title },
                                onSelected = onWeightCategorySelected,
                            )

                            SelectionChipGroup(
                                title = stringResource(R.string.ads_field_size),
                                values = AnnouncementStructuredData.SizeCategory.entries,
                                selectedValue = state.draft.attributes.sizeCategory,
                                label = { item -> item.title },
                                onSelected = onSizeCategorySelected,
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        ) {
                            state.draft.availableAttributeToggles.forEach { toggle ->
                                AttributeToggleRow(
                                    title = stringResource(toggle.titleRes),
                                    checked = state.draft.attributes.valueFor(toggle),
                                    onCheckedChange = { enabled ->
                                        onAttributeToggleChanged(toggle, enabled)
                                    },
                                )
                            }
                        }

                        if (state.draft.attributes.requiresLiftToFloor) {
                            CreateTextField(
                                value = state.draft.attributes.floor,
                                onValueChange = onFloorChanged,
                                label = stringResource(R.string.ads_create_floor_label),
                                placeholder = stringResource(R.string.ads_create_floor_placeholder),
                                keyboardType = KeyboardType.Number,
                                error = state.fieldErrors.floor,
                            )
                        }
                    }
                }

                item {
                    CreateSectionCard(
                        title = stringResource(R.string.ads_create_section_contacts),
                        icon = Icons.Outlined.Phone,
                    ) {
                        CreateTextField(
                            value = state.draft.contacts.name,
                            onValueChange = onContactNameChanged,
                            label = stringResource(R.string.ads_create_contact_name_label),
                            placeholder = stringResource(R.string.ads_create_contact_name_placeholder),
                        )

                        CreateTextField(
                            value = state.draft.contacts.phone,
                            onValueChange = onContactPhoneChanged,
                            label = stringResource(R.string.ads_create_contact_phone_label),
                            placeholder = stringResource(R.string.ads_create_contact_phone_placeholder),
                            keyboardType = KeyboardType.Phone,
                            error = state.fieldErrors.contactPhone,
                        )

                        SelectionChipGroup(
                            title = stringResource(R.string.ads_create_contact_method_label),
                            values = AnnouncementContactMethod.entries,
                            selectedValue = state.draft.contacts.method,
                            label = { item -> stringResource(item.titleRes) },
                            onSelected = onContactMethodSelected,
                        )

                        SelectionChipGroup(
                            title = stringResource(R.string.ads_create_audience_label),
                            values = AnnouncementAudience.entries,
                            selectedValue = state.draft.contacts.audience,
                            label = { item -> stringResource(item.titleRes) },
                            onSelected = onAudienceSelected,
                        )
                    }
                }

                item {
                    CreateSectionCard(
                        title = stringResource(R.string.ads_media_section_title),
                        icon = Icons.Outlined.AddPhotoAlternate,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.ads_create_media_caption),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(R.string.ads_create_media_limit, 8),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Button(
                                enabled = !state.isBusy,
                                onClick = onPickMedia,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = null,
                                )
                                Text(
                                    modifier = Modifier.padding(start = 8.dp),
                                    text = stringResource(R.string.ads_create_media_button),
                                )
                            }
                        }

                        if (state.draft.media.isNotEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(vertical = MaterialTheme.spacing.small),
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                            ) {
                                items(
                                    items = state.draft.media,
                                    key = { media -> media.id },
                                ) { media ->
                                    MediaPreviewCard(
                                        media = media,
                                        onRemove = { onRemoveMedia(media.id) },
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
private fun CreateHeroCard(
    state: AnnouncementCreateUiState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                        ),
                    ),
                )
                .padding(MaterialTheme.spacing.xLarge),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            ) {
                Text(
                    text = stringResource(R.string.shell_brand_caption),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Text(
                    text = stringResource(
                        if (state.isCreateAgain) {
                            R.string.ads_create_again_hero_title
                        } else {
                            R.string.ads_create_hero_title
                        },
                    ),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = if (!state.prefillSourceTitle.isNullOrBlank()) {
                        stringResource(R.string.ads_create_again_hero_body_with_title, state.prefillSourceTitle)
                    } else {
                        stringResource(
                            if (state.isCreateAgain) {
                                R.string.ads_create_again_hero_body
                            } else {
                                R.string.ads_create_hero_body
                            },
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.92f),
                )
            }
        }
    }
}

@Composable
private fun CreateLoadingCard(
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CreateInlineMessageCard(
    message: String?,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = message.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.ads_inline_error_dismiss))
            }
        }
    }
}

@Composable
private fun CreateSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            content = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                content()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> CreateSegmentedButtons(
    values: List<T>,
    selectedValue: T,
    label: @Composable (T) -> Unit,
    onSelected: (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        values.forEachIndexed { index, value ->
            SegmentedButton(
                selected = selectedValue == value,
                onClick = { onSelected(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = values.size),
            ) {
                label(value)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> SelectionChipGroup(
    title: String,
    values: List<T>,
    selectedValue: T?,
    label: @Composable (T) -> Unit,
    onSelected: (T) -> Unit,
) {
    if (values.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            values.forEach { value ->
                FilterChip(
                    selected = value == selectedValue,
                    onClick = { onSelected(value) },
                    label = {
                        label(value)
                    },
                )
            }
        }
    }
}

@Composable
private fun CreateTextField(
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
        placeholder = { Text(text = placeholder) },
        leadingIcon = leadingIcon,
        isError = error != null,
        supportingText = error?.let {
            {
                Text(text = it)
            }
        },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = keyboardType,
            imeAction = ImeAction.Next,
        ),
        singleLine = maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun AttributeToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onCheckedChange(!checked) },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun MediaPreviewCard(
    media: AnnouncementSelectedMedia,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier.size(width = 140.dp, height = 112.dp),
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            model = media.uriString,
            contentDescription = stringResource(R.string.ads_image_preview_description),
            contentScale = ContentScale.Crop,
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.48f),
        ) {
            IconButton(
                modifier = Modifier.size(32.dp),
                onClick = onRemove,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.ads_action_cancel),
                    tint = Color.White,
                )
            }
        }
    }
}

private fun taskBriefPlaceholderRes(
    actionType: AnnouncementStructuredData.ActionType?,
): Int = when (actionType) {
    AnnouncementStructuredData.ActionType.ProHelp -> R.string.ads_create_task_brief_placeholder_help
    AnnouncementStructuredData.ActionType.Other -> R.string.ads_create_task_brief_placeholder_other
    else -> R.string.ads_create_task_brief_placeholder_generic
}

private fun sourceAddressPlaceholderRes(
    actionType: AnnouncementStructuredData.ActionType?,
): Int = when (actionType) {
    AnnouncementStructuredData.ActionType.Buy -> R.string.ads_create_source_placeholder_buy
    AnnouncementStructuredData.ActionType.Ride -> R.string.ads_create_source_placeholder_ride
    AnnouncementStructuredData.ActionType.ProHelp -> R.string.ads_create_source_placeholder_help
    else -> R.string.ads_create_source_placeholder_generic
}

private fun destinationAddressPlaceholderRes(
    actionType: AnnouncementStructuredData.ActionType?,
): Int = when (actionType) {
    AnnouncementStructuredData.ActionType.Ride -> R.string.ads_create_destination_placeholder_ride
    AnnouncementStructuredData.ActionType.Carry -> R.string.ads_create_destination_placeholder_carry
    else -> R.string.ads_create_destination_placeholder_generic
}
