package com.vzaimno.app.core.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: (@Composable (() -> Unit))? = null,
    placeholder: (@Composable (() -> Unit))? = null,
    leadingIcon: (@Composable (() -> Unit))? = null,
    isError: Boolean = false,
    supportingText: (@Composable (() -> Unit))? = null,
    minLines: Int = 3,
    collapsedMaxLines: Int = 4,
    expandedMinLines: Int = 8,
    expandedMaxLines: Int = 14,
    shape: Shape = RoundedCornerShape(18.dp),
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: TextFieldColors? = null,
) {
    var expanded by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val resolvedColors = colors ?: OutlinedTextFieldDefaults.colors()
    val minVisibleLines = if (expanded) maxOf(minLines, expandedMinLines) else minLines
    val maxVisibleLines = if (expanded) {
        maxOf(minVisibleLines, expandedMaxLines)
    } else {
        maxOf(minLines, collapsedMaxLines)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            isError = isError,
            minLines = minVisibleLines,
            maxLines = maxVisibleLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            supportingText = supportingText,
            shape = shape,
            textStyle = textStyle,
            colors = resolvedColors,
        )
        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .size(28.dp),
            enabled = enabled,
            onClick = { expanded = !expanded },
        ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = if (expanded) Icons.Outlined.CloseFullscreen else Icons.Outlined.OpenInFull,
                    contentDescription = if (expanded) "Свернуть поле" else "Развернуть поле",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}
