package com.vzaimno.app.feature.shell.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AltRoute
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TurnRight
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
fun RouteShellScreen() {
    ShellTopLevelScreen(
        titleRes = R.string.shell_route_title,
        subtitleRes = R.string.shell_route_subtitle,
    ) {
        item {
            ShellHeroCard(
                icon = Icons.Outlined.AltRoute,
                title = stringResource(R.string.shell_route_hero_title),
                body = stringResource(R.string.shell_route_hero_body),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    ShellMetricRow(
                        label = stringResource(R.string.shell_route_metric_window),
                        value = stringResource(R.string.shell_route_metric_window_value),
                    )
                    ShellMetricRow(
                        label = stringResource(R.string.shell_route_metric_mode),
                        value = stringResource(R.string.shell_route_metric_mode_value),
                    )
                }
            }
        }

        item {
            ShellSectionCard(
                title = stringResource(R.string.shell_route_section_title),
                eyebrow = stringResource(R.string.shell_section_ready_now),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    ShellFeatureRow(
                        icon = Icons.Outlined.Schedule,
                        title = stringResource(R.string.shell_route_feature_timeline_title),
                        subtitle = stringResource(R.string.shell_route_feature_timeline_body),
                    )
                    ShellDivider()
                    ShellFeatureRow(
                        icon = Icons.Outlined.TurnRight,
                        title = stringResource(R.string.shell_route_feature_guidance_title),
                        subtitle = stringResource(R.string.shell_route_feature_guidance_body),
                    )
                }
            }
        }
    }
}
