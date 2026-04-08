package com.vzaimno.app.feature.shell.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.feature.shell.components.ShellDivider
import com.vzaimno.app.feature.shell.components.ShellFeatureRow
import com.vzaimno.app.feature.shell.components.ShellHeroCard
import com.vzaimno.app.feature.shell.components.ShellMetricRow
import com.vzaimno.app.feature.shell.components.ShellSectionCard
import com.vzaimno.app.feature.shell.components.ShellTopLevelScreen

@Composable
fun AnnouncementsShellScreen() {
    ShellTopLevelScreen(
        titleRes = R.string.shell_ads_title,
        subtitleRes = R.string.shell_ads_subtitle,
    ) {
        item {
            ShellHeroCard(
                icon = Icons.Outlined.Inventory2,
                title = stringResource(R.string.shell_ads_hero_title),
                body = stringResource(R.string.shell_ads_hero_body),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    ShellMetricRow(
                        label = stringResource(R.string.shell_ads_metric_scope),
                        value = stringResource(R.string.shell_ads_metric_scope_value),
                    )
                    ShellMetricRow(
                        label = stringResource(R.string.shell_ads_metric_focus),
                        value = stringResource(R.string.shell_ads_metric_focus_value),
                    )
                }
            }
        }

        item {
            ShellSectionCard(
                title = stringResource(R.string.shell_ads_section_title),
                eyebrow = stringResource(R.string.shell_section_next_stage),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    ShellFeatureRow(
                        icon = Icons.Outlined.Tune,
                        title = stringResource(R.string.shell_ads_feature_filters_title),
                        subtitle = stringResource(R.string.shell_ads_feature_filters_body),
                    )
                    ShellDivider()
                    ShellFeatureRow(
                        icon = Icons.Outlined.AddCircleOutline,
                        title = stringResource(R.string.shell_ads_feature_create_title),
                        subtitle = stringResource(R.string.shell_ads_feature_create_body),
                    )
                }
            }
        }
    }
}
