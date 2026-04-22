package com.vzaimno.app.feature.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.DisputeSettlementOption
import com.vzaimno.app.core.model.DisputeState
import kotlinx.coroutines.delay

// region Dispute panel

@Composable
internal fun DisputePanel(
    state: DisputeUiState,
    onOpenDispute: () -> Unit,
    onRespondAsCounterparty: () -> Unit,
    onOptionSelected: (String) -> Unit,
    canOpenDispute: Boolean,
) {
    val dispute = state.activeDispute
    if (dispute == null && !canOpenDispute && !state.shouldShowThinkingState) return

    // Tick every 60 seconds to refresh the countdown text.
    var nowTick by remember { mutableStateOf(0L) }
    LaunchedEffect(dispute?.counterpartyDeadlineAtEpochSeconds) {
        while (dispute?.counterpartyDeadlineAtEpochSeconds != null) {
            delay(60_000L)
            nowTick++
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Разрешение спора",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (state.shouldShowThinkingState) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = "Модель анализирует",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (dispute != null) {
                Text(
                    text = disputeSummaryText(dispute),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (dispute.isWaitingCounterparty) {
                    // Consume nowTick so Compose treats this branch as dependent on it.
                    @Suppress("UNUSED_EXPRESSION") nowTick
                    disputeDeadlineText(dispute.counterpartyDeadlineAtEpochSeconds)?.let { deadline ->
                        Text(
                            text = "До конца окна ответа: $deadline",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }

                if (state.canRespondAsCounterparty) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onRespondAsCounterparty,
                    ) {
                        Text(text = "Ответить на спор")
                    }
                }

                if (state.canAnswerClarifications ||
                    (dispute.isWaitingClarificationAnswers && dispute.questions.isNotEmpty())
                ) {
                    DisputeClarificationBlock(
                        state = state,
                        dispute = dispute,
                    )
                }

                if ((dispute.isWaitingRound1Votes || dispute.isWaitingRound2Votes) &&
                    dispute.options.isNotEmpty()
                ) {
                    DisputeOptionsBlock(
                        state = state,
                        dispute = dispute,
                        onOptionSelected = onOptionSelected,
                    )
                }

                if (dispute.lastModelError != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    ) {
                        Text(
                            modifier = Modifier.padding(MaterialTheme.spacing.medium),
                            text = dispute.lastModelError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            } else if (canOpenDispute) {
                Text(
                    text = "Если по выполнению заказа возник конфликт, " +
                        "можно открыть спор и запустить автоматическое урегулирование через модель.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenDispute,
                ) {
                    Text(text = "Открыть спор")
                }
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun DisputeClarificationBlock(
    state: DisputeUiState,
    dispute: DisputeState,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Вопросы модели",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            dispute.questions.take(5).forEach { question ->
                Text(
                    text = "• ${question.text}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            if (state.canAnswerClarifications) {
                Text(
                    text = "Первое ваше сообщение после этих вопросов будет зафиксировано как официальный ответ.",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun DisputeOptionsBlock(
    state: DisputeUiState,
    dispute: DisputeState,
    onOptionSelected: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = if (dispute.activeRound == 1) {
                "Варианты решения (раунд 1)"
            } else {
                "Варианты решения (раунд 2)"
            },
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Нажмите на вариант, чтобы посмотреть детали и подтвердить выбор.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            dispute.options.forEach { option ->
                DisputeOptionChip(
                    title = compactDisputeOptionTitle(
                        option = option,
                        requestedCompensationRub = dispute.initiatorTerms.requestedCompensationRub,
                    ),
                    isSelected = dispute.myVoteOptionId == option.id,
                    isDisabled = !state.canVoteInCurrentRound || state.isSubmitting,
                    onClick = { onOptionSelected(option.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisputeOptionChip(
    title: String,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = { if (!isDisabled) onClick() },
        enabled = !isDisabled,
        label = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            null
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    )
}

// endregion

// region Bottom sheets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OpenDisputeBottomSheet(
    state: DisputeUiState,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCompensationChanged: (String) -> Unit,
    onResolutionSelected: (DisputeResolutionKind) -> Unit,
    onSubmit: () -> Unit,
) {
    val form = state.openForm
    val canSubmit = form.problemDescription.trim().isNotEmpty() && !state.isSubmitting

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.xLarge)
                .padding(bottom = MaterialTheme.spacing.xxLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            Text(
                text = "Открыть спор",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Опишите, что произошло. Модель поможет обеим сторонам договориться о справедливом решении.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.problemTitle,
                onValueChange = onTitleChanged,
                label = { Text(text = "Тема спора") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.problemDescription,
                onValueChange = onDescriptionChanged,
                label = { Text(text = "Что произошло") },
                minLines = 4,
                maxLines = 6,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.requestedCompensationText,
                onValueChange = onCompensationChanged,
                label = { Text(text = "Сумма компенсации, ₽") },
                placeholder = { Text(text = "Например 1500") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            ResolutionKindPicker(
                title = "Желаемое решение",
                selected = form.selectedResolution,
                onSelected = onResolutionSelected,
            )

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canSubmit,
                onClick = onSubmit,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = "Отправить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CounterpartyDisputeBottomSheet(
    state: DisputeUiState,
    onDismiss: () -> Unit,
    onAcceptModeSelected: (Boolean) -> Unit,
    onResponseChanged: (String) -> Unit,
    onRefundPercentChanged: (String) -> Unit,
    onResolutionSelected: (DisputeResolutionKind) -> Unit,
    onAccept: () -> Unit,
    onSubmitCounter: () -> Unit,
) {
    val form = state.counterpartyForm
    val dispute = state.activeDispute
    val initiator = dispute?.initiatorTerms
    val acceptMode = form.isAcceptMode
    val refundPercent = form.acceptableRefundPercentText.toIntOrNull() ?: 50
    val canSubmit = if (acceptMode) {
        !state.isSubmitting
    } else {
        form.responseDescription.trim().isNotEmpty() && !state.isSubmitting
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.xLarge)
                .padding(bottom = MaterialTheme.spacing.xxLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            Text(
                text = "Ответ на спор",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (initiator != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                ) {
                    Column(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Условия первой стороны",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        KeyValueRow(label = "Сумма", value = "${formatRub(initiator.requestedCompensationRub)} ₽")
                        KeyValueRow(
                            label = "Решение",
                            value = DisputeResolutionKind.fromRaw(initiator.desiredResolution).title,
                        )
                        if (initiator.problemTitle.isNotEmpty()) {
                            KeyValueRow(label = "Тема", value = initiator.problemTitle)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                SegmentedButtonItem(
                    modifier = Modifier.weight(1f),
                    title = "Согласиться",
                    selected = acceptMode,
                    onClick = { onAcceptModeSelected(true) },
                )
                SegmentedButtonItem(
                    modifier = Modifier.weight(1f),
                    title = "Не согласиться",
                    selected = !acceptMode,
                    onClick = { onAcceptModeSelected(false) },
                )
            }

            if (!acceptMode) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.responseDescription,
                    onValueChange = onResponseChanged,
                    label = { Text(text = "Ваша версия") },
                    minLines = 4,
                    maxLines = 6,
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "На какой возврат согласны",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Slider(
                        value = refundPercent.coerceIn(0, 100).toFloat(),
                        onValueChange = { raw ->
                            val snapped = ((raw / 5f).toInt() * 5).coerceIn(0, 100)
                            onRefundPercentChanged(snapped.toString())
                        },
                        valueRange = 0f..100f,
                        steps = 19,
                    )
                    Text(
                        text = "${refundPercent.coerceIn(0, 100)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                ResolutionKindPicker(
                    title = "Предпочитаемое решение",
                    selected = form.selectedResolution,
                    onSelected = onResolutionSelected,
                )
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canSubmit,
                onClick = { if (acceptMode) onAccept() else onSubmitCounter() },
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = if (acceptMode) "Согласиться" else "Отправить")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DisputeOptionDetailBottomSheet(
    state: DisputeUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val dispute = state.activeDispute ?: return
    val optionId = state.optionDetail.optionId ?: return
    val option = dispute.options.firstOrNull { it.id == optionId } ?: return

    val effectiveCompensationRub = disputeOptionCompensationRub(
        option = option,
        requestedCompensationRub = dispute.initiatorTerms.requestedCompensationRub,
    )
    val shortTitle = if (effectiveCompensationRub != null) {
        "${formatRub(effectiveCompensationRub)} ₽"
    } else {
        shortResolutionKindTitle(option.resolutionKind)
    }
    val isSelected = dispute.myVoteOptionId == option.id
    val viewerRole = dispute.viewerPartyRole?.trim()?.lowercase()
    val roleReason = when (viewerRole) {
        "performer" -> option.performerAction.ifBlank { option.description }
        else -> option.customerAction.ifBlank { option.description }
    }.ifBlank {
        "Модель считает это условие реалистичным для быстрого урегулирования."
    }
    val canConfirm = !state.isSubmitting && state.canVoteInCurrentRound

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.xLarge)
                .padding(bottom = MaterialTheme.spacing.xxLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Вариант",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = shortTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Детали",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    effectiveCompensationRub?.let { amount ->
                        KeyValueRow(label = "Итоговая сумма", value = "${formatRub(amount)} ₽")
                    }
                    option.refundPercent?.let { percent ->
                        KeyValueRow(label = "Процент", value = "$percent%")
                    }
                    KeyValueRow(
                        label = "Формат",
                        value = shortResolutionKindTitle(option.resolutionKind),
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Почему это может быть выгодно вам",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = roleReason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            option.description.takeIf { it.isNotBlank() }?.let { description ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Комментарий модели",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canConfirm,
                onClick = onConfirm,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = if (isSelected) "Выбрано" else "Выбрать")
                }
            }
        }
    }
}

// endregion

// region Helpers

@Composable
private fun ResolutionKindPicker(
    title: String,
    selected: DisputeResolutionKind,
    onSelected: (DisputeResolutionKind) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            DisputeResolutionKind.values().forEach { kind ->
                ResolutionChip(
                    title = kind.title,
                    selected = kind == selected,
                    onClick = { onSelected(kind) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResolutionChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
        },
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
private fun SegmentedButtonItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
        )
    }
}

@Composable
private fun KeyValueRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.small),
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = LocalContentColor.current,
        )
    }
}

// endregion
