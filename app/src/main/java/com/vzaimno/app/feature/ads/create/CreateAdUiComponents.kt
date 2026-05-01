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
import androidx.compose.material.icons.outlined.Inventory2
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vzaimno.app.core.designsystem.theme.Background
import com.vzaimno.app.core.designsystem.theme.ChipBackground
import com.vzaimno.app.core.designsystem.theme.Outline
import com.vzaimno.app.core.designsystem.theme.OutlineVariant
import com.vzaimno.app.core.designsystem.theme.Primary
import com.vzaimno.app.core.designsystem.theme.PrimaryBright
import com.vzaimno.app.core.designsystem.theme.PrimaryDark
import com.vzaimno.app.core.designsystem.theme.Surface
import com.vzaimno.app.core.designsystem.theme.SurfaceAlt
import com.vzaimno.app.core.designsystem.theme.SurfaceVariant
import com.vzaimno.app.core.designsystem.theme.TextPrimary
import com.vzaimno.app.core.designsystem.theme.TextSecondary

val SectionCardRadius = 26.dp
val InnerCardRadius = 20.dp
val ButtonRadius = 30.dp
val SmallChipRadius = 16.dp
val LargeChipRadius = 22.dp
val GalleryImageRadius = 18.dp

val ScreenHorizontalPadding = 20.dp
val SectionVerticalGap = 16.dp
val InnerCardPadding = 18.dp

@Immutable
internal data class CreateAdPalette(
    val isDark: Boolean,
    val screenBackground: Color,
    val topBarBackground: Color,
    val sectionSurface: Color,
    val sectionSurfaceAlt: Color,
    val surfaceMuted: Color,
    val border: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentStrong: Color,
    val selectedContainer: Color,
    val selectedContent: Color,
    val selectedIconContainer: Color,
    val selectedIconContent: Color,
    val inputSurface: Color,
    val inputBorder: Color,
    val noticeSurface: Color,
    val noticeContent: Color,
    val warningSurface: Color,
    val warningContent: Color,
    val ctaContainer: Color,
    val ctaContent: Color,
    val previewHeroStart: Color,
    val previewHeroEnd: Color,
    val previewFooter: Color,
    val previewChip: Color,
    val previewChipContent: Color,
    val shadow: Color,
)

@Composable
internal fun rememberCreateAdPalette(): CreateAdPalette {
    val scheme = MaterialTheme.colorScheme
    val isDarkTheme = scheme.background.luminance() < 0.5f
    return if (isDarkTheme) {
        CreateAdPalette(
            isDark = true,
            screenBackground = Background,
            topBarBackground = Background,
            sectionSurface = Surface,
            sectionSurfaceAlt = SurfaceAlt,
            surfaceMuted = ChipBackground,
            border = OutlineVariant,
            borderStrong = Outline,
            textPrimary = TextPrimary,
            textSecondary = TextSecondary,
            accent = Primary,
            accentSoft = OutlineVariant,
            accentStrong = PrimaryBright,
            selectedContainer = PrimaryDark,
            selectedContent = TextPrimary,
            selectedIconContainer = PrimaryBright,
            selectedIconContent = Background,
            inputSurface = SurfaceAlt,
            inputBorder = Outline,
            noticeSurface = SurfaceVariant,
            noticeContent = TextPrimary,
            warningSurface = Color(0xFF3A2B21),
            warningContent = Color(0xFFFFDABD),
            ctaContainer = PrimaryBright,
            ctaContent = Background,
            previewHeroStart = SurfaceVariant,
            previewHeroEnd = Surface,
            previewFooter = SurfaceAlt,
            previewChip = ChipBackground,
            previewChipContent = TextSecondary,
            shadow = Color.Black.copy(alpha = 0.28f),
        )
    } else {
        CreateAdPalette(
            isDark = false,
            screenBackground = scheme.background,
            topBarBackground = scheme.background,
            sectionSurface = scheme.surface,
            sectionSurfaceAlt = Color.White,
            surfaceMuted = scheme.surfaceVariant.copy(alpha = 0.58f),
            border = scheme.outlineVariant,
            borderStrong = scheme.outline,
            textPrimary = scheme.onSurface,
            textSecondary = scheme.onSurfaceVariant,
            accent = scheme.primary,
            accentSoft = scheme.primaryContainer,
            accentStrong = scheme.primary,
            selectedContainer = scheme.primaryContainer,
            selectedContent = scheme.primary,
            selectedIconContainer = scheme.primary,
            selectedIconContent = scheme.onPrimary,
            inputSurface = Color.White,
            inputBorder = scheme.outlineVariant,
            noticeSurface = scheme.secondaryContainer,
            noticeContent = scheme.onSecondaryContainer,
            warningSurface = scheme.secondaryContainer,
            warningContent = scheme.onSecondaryContainer,
            ctaContainer = scheme.primary,
            ctaContent = scheme.onPrimary,
            previewHeroStart = scheme.primaryContainer,
            previewHeroEnd = scheme.surfaceVariant,
            previewFooter = scheme.secondaryContainer,
            previewChip = scheme.primary,
            previewChipContent = scheme.onPrimary,
            shadow = Color.Black.copy(alpha = 0.08f),
        )
    }
}

@Composable
fun CreateAdSectionCard(
    title: String,
    subtitle: String? = null,
    moderationMark: DraftModerationMark? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = rememberCreateAdPalette()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SectionCardRadius),
        colors = CardDefaults.cardColors(containerColor = palette.sectionSurface),
        border = BorderStroke(1.dp, palette.border),
        elevation = CardDefaults.cardElevation(defaultElevation = if (palette.isDark) 0.dp else 1.dp),
    ) {
        Column(modifier = Modifier.padding(InnerCardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (moderationMark != null) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Модерация",
                        tint = palette.warningContent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

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
    val palette = rememberCreateAdPalette()
    val containerColor = when {
        selected -> palette.selectedContainer
        compact -> palette.sectionSurfaceAlt
        else -> palette.sectionSurfaceAlt
    }
    val borderColor = if (selected) Color.Transparent else palette.border
    val titleColor = if (selected) palette.selectedContent else palette.textPrimary
    val subtitleColor = if (selected) palette.selectedContent.copy(alpha = 0.78f) else palette.textSecondary
    val shape = RoundedCornerShape(if (compact) SmallChipRadius else LargeChipRadius)

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (selected && !palette.isDark && !compact) 1.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 10.dp else 14.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                if (compact) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) palette.selectedContent else palette.textSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) palette.selectedIconContainer else palette.surfaceMuted,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) palette.selectedIconContent else palette.textSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.width(if (compact) 8.dp else 10.dp))
            }

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = text,
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.Medium,
                    color = titleColor,
                    maxLines = if (compact) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null && !compact) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun CreateAdToggleTile(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberCreateAdPalette()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) palette.selectedContainer.copy(alpha = if (palette.isDark) 0.82f else 0.78f) else Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) palette.selectedContent else palette.textSecondary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = if (selected) palette.selectedContent else palette.textPrimary,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) palette.selectedContent.copy(alpha = 0.75f) else palette.textSecondary,
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                modifier = Modifier.size(26.dp),
                shape = CircleShape,
                color = if (selected) palette.selectedIconContainer else Color.Transparent,
                border = if (selected) null else BorderStroke(1.5.dp, palette.borderStrong),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = palette.selectedIconContent,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateAdInfoTag(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val palette = rememberCreateAdPalette()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = palette.surfaceMuted,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary,
            )
        }
    }
}

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
    leadingIcon: ImageVector = Icons.Outlined.Inventory2,
) {
    val palette = rememberCreateAdPalette()
    val heading = buildString {
        append(actionText.ifBlank { title })
        if (objectText.isNotBlank() && objectText != "Без деталей") {
            append(" · ")
            append(objectText)
        }
    }.ifBlank { title }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (palette.isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(SectionCardRadius),
                ambientColor = palette.shadow,
                spotColor = palette.shadow,
            ),
        color = palette.sectionSurface,
        shape = RoundedCornerShape(SectionCardRadius),
        border = BorderStroke(1.dp, palette.border),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.92f,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surfaceMuted),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = palette.textSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.textPrimary,
                    maxLines = 1,
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
                        tint = palette.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = palette.border)
                    Spacer(Modifier.height(10.dp))
                    CreateAdSummaryRow(label = "Маршрут", value = routeSummary, palette = palette)
                    CreateAdSummaryRow(label = "Когда", value = timeSummary, palette = palette)
                    CreateAdSummaryRow(label = "Цена", value = priceSummary, palette = palette)
                }
            }
        }
    }
}

@Composable
private fun CreateAdSummaryRow(
    label: String,
    value: String,
    palette: CreateAdPalette,
) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
            modifier = Modifier.width(58.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CreateAdSummaryCard(
    title: String,
    tags: List<String>,
    routeSummary: String,
    timeSummary: String,
    budgetSummary: String,
    modifier: Modifier = Modifier,
) {
    val palette = rememberCreateAdPalette()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InnerCardRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (palette.isDark) 0.dp else 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(palette.previewHeroStart, palette.sectionSurfaceAlt),
                    ),
                )
                .padding(16.dp),
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.textPrimary,
                )
                if (tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRowTags(tags = tags, palette = palette)
                }
                Spacer(Modifier.height(10.dp))
                SummaryDetailRow("Маршрут", routeSummary, palette = palette)
                SummaryDetailRow("Когда", timeSummary, palette = palette)
                SummaryDetailRow("Цена", budgetSummary, palette = palette)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowTags(
    tags: List<String>,
    palette: CreateAdPalette,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = palette.surfaceMuted,
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SummaryDetailRow(
    label: String,
    value: String,
    palette: CreateAdPalette,
) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = palette.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CreateAdRecommendedPriceView(
    priceText: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val palette = rememberCreateAdPalette()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InnerCardRadius),
        color = if (palette.isDark) palette.accentSoft else palette.previewHeroEnd,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Рекомендуемая цена",
                style = MaterialTheme.typography.labelMedium,
                color = if (palette.isDark) palette.selectedContent.copy(alpha = 0.78f) else palette.selectedContent,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = priceText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (palette.isDark) palette.selectedContent else palette.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (palette.isDark) palette.selectedContent.copy(alpha = 0.72f) else palette.textSecondary,
            )
        }
    }
}

@Composable
fun CreateAdReadinessCard(
    issues: List<String>,
    modifier: Modifier = Modifier,
) {
    val palette = rememberCreateAdPalette()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InnerCardRadius),
        color = palette.noticeSurface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Чтобы отправить объявление, осталось:",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = palette.noticeContent,
            )
            Spacer(Modifier.height(8.dp))
            issues.take(6).forEach { issue ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = "•  ",
                        color = palette.noticeContent,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = issue,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.noticeContent,
                    )
                }
            }
        }
    }
}

@Composable
fun CreateAdTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    moderationMark: DraftModerationMark? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val palette = rememberCreateAdPalette()

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = palette.textSecondary,
                modifier = Modifier.weight(1f),
            )
            if (moderationMark != null) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = palette.warningContent,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            leadingIcon = leadingIcon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = palette.textSecondary,
                    )
                }
            },
            trailingIcon = trailingIcon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = palette.accent,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary,
                focusedBorderColor = palette.accent,
                unfocusedBorderColor = palette.inputBorder,
                focusedContainerColor = palette.inputSurface,
                unfocusedContainerColor = palette.inputSurface,
                focusedPlaceholderColor = palette.textSecondary,
                unfocusedPlaceholderColor = palette.textSecondary,
                focusedLeadingIconColor = palette.textSecondary,
                unfocusedLeadingIconColor = palette.textSecondary,
                focusedTrailingIconColor = palette.accent,
                unfocusedTrailingIconColor = palette.accent,
                cursorColor = palette.accent,
            ),
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
    val palette = rememberCreateAdPalette()

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = palette.textSecondary,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { newValue -> onValueChange(newValue.filter { it.isDigit() }) },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = if (trailingUnit.isNotBlank()) {
                {
                    Text(
                        text = trailingUnit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary,
                focusedBorderColor = palette.accent,
                unfocusedBorderColor = palette.inputBorder,
                focusedContainerColor = palette.inputSurface,
                unfocusedContainerColor = palette.inputSurface,
                focusedPlaceholderColor = palette.textSecondary,
                unfocusedPlaceholderColor = palette.textSecondary,
                cursorColor = palette.accent,
            ),
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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

@Composable
fun CreateAdTextArea(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
) {
    val palette = rememberCreateAdPalette()

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = palette.textSecondary,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp),
            maxLines = 8,
            shape = RoundedCornerShape(18.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary,
                focusedBorderColor = palette.accent,
                unfocusedBorderColor = palette.inputBorder,
                focusedContainerColor = palette.inputSurface,
                unfocusedContainerColor = palette.inputSurface,
                focusedPlaceholderColor = palette.textSecondary,
                unfocusedPlaceholderColor = palette.textSecondary,
                cursorColor = palette.accent,
            ),
        )
    }
}

@Composable
fun CreateAdToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberCreateAdPalette()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = if (palette.isDark) palette.sectionSurface else Color.White,
                checkedTrackColor = palette.accentSoft,
                checkedBorderColor = palette.accentSoft,
                uncheckedThumbColor = Color(0xFFE9ECEA),
                uncheckedTrackColor = palette.surfaceMuted,
                uncheckedBorderColor = palette.borderStrong,
            ),
        )
    }
}

@Composable
fun CreateAdBottomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val palette = rememberCreateAdPalette()

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(ButtonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.ctaContainer,
            contentColor = palette.ctaContent,
            disabledContainerColor = palette.ctaContainer.copy(alpha = 0.45f),
            disabledContentColor = palette.ctaContent.copy(alpha = 0.7f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (palette.isDark) 0.dp else 2.dp),
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = palette.ctaContent,
                strokeWidth = 2.dp,
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }
}
