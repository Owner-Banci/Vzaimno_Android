package com.vzaimno.app.feature.shell.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.feature.shell.components.ShellDivider
import com.vzaimno.app.feature.shell.components.ShellFeatureRow
import com.vzaimno.app.feature.shell.components.ShellHeroCard
import com.vzaimno.app.feature.shell.components.ShellSectionCard
import com.vzaimno.app.feature.shell.components.ShellTopLevelScreen

@Composable
fun ChatsShellScreen(
    onOpenPreview: () -> Unit,
) {
    ShellTopLevelScreen(
        titleRes = R.string.shell_chats_title,
        subtitleRes = R.string.shell_chats_subtitle,
    ) {
        item {
            ShellHeroCard(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = stringResource(R.string.shell_chats_hero_title),
                body = stringResource(R.string.shell_chats_hero_body),
            )
        }

        item {
            ShellSectionCard(
                title = stringResource(R.string.shell_chats_section_title),
                eyebrow = stringResource(R.string.shell_section_ready_now),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    ShellFeatureRow(
                        icon = Icons.Outlined.MarkChatUnread,
                        title = stringResource(R.string.shell_chats_feature_updates_title),
                        subtitle = stringResource(R.string.shell_chats_feature_updates_body),
                    )
                    ShellDivider()
                    ShellFeatureRow(
                        icon = Icons.Outlined.SupportAgent,
                        title = stringResource(R.string.shell_chats_feature_support_title),
                        subtitle = stringResource(R.string.shell_chats_feature_support_body),
                    )
                    Button(
                        onClick = onOpenPreview,
                    ) {
                        androidx.compose.material3.Text(text = stringResource(R.string.shell_chats_open_preview))
                    }
                }
            }
        }
    }
}
