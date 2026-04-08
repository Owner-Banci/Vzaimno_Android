package com.vzaimno.app.feature.shell.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vzaimno.app.R
import com.vzaimno.app.feature.shell.components.ShellDivider
import com.vzaimno.app.feature.shell.components.ShellFeatureRow
import com.vzaimno.app.feature.shell.components.ShellHeroCard
import com.vzaimno.app.feature.shell.components.ShellMetricRow
import com.vzaimno.app.feature.shell.components.ShellSectionCard
import com.vzaimno.app.feature.shell.components.ShellTopLevelScreen
import com.vzaimno.app.core.designsystem.theme.spacing
import androidx.compose.material3.MaterialTheme

@Composable
fun MapShellScreen() {
    ShellTopLevelScreen(
        titleRes = R.string.shell_map_title,
        subtitleRes = R.string.shell_map_subtitle,
    ) {
        item {
            ShellHeroCard(
                icon = Icons.Outlined.Explore,
                title = stringResource(R.string.shell_map_hero_title),
                body = stringResource(R.string.shell_map_hero_body),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    ShellMetricRow(
                        label = stringResource(R.string.shell_map_metric_zone),
                        value = stringResource(R.string.shell_map_metric_zone_value),
                    )
                    ShellMetricRow(
                        label = stringResource(R.string.shell_map_metric_priority),
                        value = stringResource(R.string.shell_map_metric_priority_value),
                    )
                }
            }
        }

        item {
            ShellSectionCard(
                title = stringResource(R.string.shell_map_section_title),
                eyebrow = stringResource(R.string.shell_section_next_stage),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    ShellFeatureRow(
                        icon = Icons.Outlined.PinDrop,
                        title = stringResource(R.string.shell_map_feature_markers_title),
                        subtitle = stringResource(R.string.shell_map_feature_markers_body),
                    )
                    ShellDivider()
                    ShellFeatureRow(
                        icon = Icons.Outlined.FilterAlt,
                        title = stringResource(R.string.shell_map_feature_filters_title),
                        subtitle = stringResource(R.string.shell_map_feature_filters_body),
                    )
                }
            }
        }
    }
}
