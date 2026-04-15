package com.vzaimno.app.feature.ads.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Design tokens ───────────────────────────────────────────────────────

val TurquoiseAccent = Color(0xFF3CC8C4)
val MilkBackground = Color(0xFFF7F3E9)
val CardBackground = Color.White
val PeachBadge = Color(0xFFFFC9A6)
val TextPrimary = Color.Black.copy(alpha = 0.90f)
val TextSecondary = Color.Black.copy(alpha = 0.60f)
val ShadowColor = Color.Black.copy(alpha = 0.08f)

val SectionCardRadius = 18.dp
val InnerCardRadius = 16.dp
val ButtonRadius = 16.dp
val SmallChipRadius = 14.dp
val LargeChipRadius = 18.dp
val GalleryImageRadius = 14.dp

val ScreenHorizontalPadding = 20.dp
val SectionVerticalGap = 16.dp
val InnerCardPadding = 20.dp

// ════════════════════════════════════════════════════════════════════════
//  SECTION CARD — white card with turquoise border
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdSectionCard(
    title: String,
    subtitle: String? = null,
    moderationMark: DraftModerationMark? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SectionCardRadius),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground.copy(alpha = 0.55f),
        ),
        border = BorderStroke(1.dp, TurquoiseAccent.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(InnerCardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (moderationMark != null) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Модерация",
                        tint = Color(0xFFE8A16C),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  CHOICE CHIP — large action chips (2-column grid)
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdChoiceChip(
    text: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val bgColor = if (selected) TurquoiseAccent else CardBackground.copy(alpha = 0.72f)
    val textColor = if (selected) Color.White else TextPrimary
    val borderColor = if (selected) TurquoiseAccent else TurquoiseAccent.copy(alpha = 0.25f)
    val shape = RoundedCornerShape(if (compact) SmallChipRadius else LargeChipRadius)

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 8.dp else 12.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(if (compact) 16.dp else 20.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = text,
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null && !compact) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  TOGGLE TILE — condition tiles with check circle
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdToggleTile(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (selected) TurquoiseAccent.copy(alpha = 0.10f) else Color.Transparent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) TurquoiseAccent else TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) TurquoiseAccent else Color.Transparent,
                        CircleShape,
                    )
                    .then(
                        if (!selected) Modifier.background(
                            Color.Transparent, CircleShape,
                        ) else Modifier
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, TextSecondary.copy(alpha = 0.3f)),
                    ) {}
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  INFO TAG — small pill label
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdInfoTag(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = CardBackground.copy(alpha = 0.80f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  STICKY MINI SUMMARY
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdStickyMiniSummary(
    title: String,
    actionText: String,
    objectText: String,
    routeSummary: String,
    timeSummary: String,
    priceSummary: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(SectionCardRadius), ambientColor = ShadowColor, spotColor = ShadowColor),
            color = CardBackground.copy(alpha = 0.94f),
        shape = RoundedCornerShape(SectionCardRadius),
        border = BorderStroke(1.dp, TurquoiseAccent.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = if (isExpanded) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                        tint = TurquoiseAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CreateAdMiniTag(text = actionText)
                CreateAdMiniTag(text = objectText)
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    CreateAdSummaryRow(label = "Маршрут", value = routeSummary)
                    CreateAdSummaryRow(label = "Когда", value = timeSummary)
                    CreateAdSummaryRow(label = "Цена", value = priceSummary)
                }
            }
        }
    }
}

@Composable
private fun CreateAdMiniTag(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = TurquoiseAccent.copy(alpha = 0.2f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TurquoiseAccent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun CreateAdSummaryRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ════════════════════════════════════════════════════════════════════════
//  SUMMARY CARD — gradient accent card for final section
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdSummaryCard(
    title: String,
    tags: List<String>,
    routeSummary: String,
    timeSummary: String,
    budgetSummary: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InnerCardRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            TurquoiseAccent.copy(alpha = 0.18f),
                            CardBackground.copy(alpha = 0.84f),
                        ),
                    ),
                )
                .padding(16.dp),
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                if (tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRowTags(tags)
                }
                Spacer(Modifier.height(10.dp))
                SummaryDetailRow("Маршрут", routeSummary)
                SummaryDetailRow("Когда", timeSummary)
                SummaryDetailRow("Цена", budgetSummary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowTags(tags: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = TurquoiseAccent.copy(alpha = 0.12f),
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = TurquoiseAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun SummaryDetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ════════════════════════════════════════════════════════════════════════
//  RECOMMENDED PRICE VIEW
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdRecommendedPriceView(
    priceText: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InnerCardRadius),
        color = TurquoiseAccent.copy(alpha = 0.10f),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Рекомендуемая цена",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = priceText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  READINESS CARD — bullet list of issues
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdReadinessCard(
    issues: List<String>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InnerCardRadius),
        color = Color(0xFFFFF3E0),
        border = BorderStroke(1.dp, Color(0xFFE8A16C).copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Чтобы отправить объявление, осталось:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(8.dp))
            issues.take(6).forEach { issue ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(text = "•  ", color = Color(0xFFE8A16C), style = MaterialTheme.typography.bodySmall)
                    Text(text = issue, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
//  TEXT FIELD / VALUE FIELD / BUDGET RANGE
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    moderationMark: DraftModerationMark? = null,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.weight(1f),
            )
            if (moderationMark != null) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE8A16C),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TurquoiseAccent,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
fun CreateAdValueField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    trailingUnit: String = "",
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { newVal -> onValueChange(newVal.filter { it.isDigit() }) },
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = if (trailingUnit.isNotBlank()) {
                { Text(trailingUnit, color = TextSecondary) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TurquoiseAccent,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
fun CreateAdBudgetRangeField(
    minValue: String,
    maxValue: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    minPlaceholder: String = "",
    maxPlaceholder: String = "",
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CreateAdValueField(
            label = "От",
            value = minValue,
            onValueChange = onMinChange,
            trailingUnit = "₽",
            placeholder = minPlaceholder,
            modifier = Modifier.weight(1f),
        )
        CreateAdValueField(
            label = "До",
            value = maxValue,
            onValueChange = onMaxChange,
            trailingUnit = "₽",
            placeholder = maxPlaceholder,
            modifier = Modifier.weight(1f),
        )
    }
}

// ════════════════════════════════════════════════════════════════════════
//  TEXT AREA
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdTextArea(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp),
            maxLines = 8,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TurquoiseAccent,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

// ════════════════════════════════════════════════════════════════════════
//  TOGGLE ROW (simple switch)
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

// ════════════════════════════════════════════════════════════════════════
//  BOTTOM BUTTON — primary CTA
// ════════════════════════════════════════════════════════════════════════

@Composable
fun CreateAdBottomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(ButtonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = TurquoiseAccent,
            contentColor = Color.White,
            disabledContainerColor = TurquoiseAccent.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.6f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }
}
