package com.vzaimno.app.feature.shell.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.session.SessionAccessLevel
import com.vzaimno.app.core.session.SessionState
import com.vzaimno.app.feature.shell.components.ShellDivider
import com.vzaimno.app.feature.shell.components.ShellFeatureRow
import com.vzaimno.app.feature.shell.components.ShellHeroCard
import com.vzaimno.app.feature.shell.components.ShellMetricRow
import com.vzaimno.app.feature.shell.components.ShellSectionCard
import com.vzaimno.app.feature.shell.components.ShellTopLevelScreen

@Composable
fun ProfileShellScreen(
    sessionState: SessionState.Authenticated,
    isOnline: Boolean,
    isLoggingOut: Boolean,
    onRetryRestore: () -> Unit,
    onLogout: () -> Unit,
) {
    ShellTopLevelScreen(
        titleRes = R.string.shell_profile_title,
        subtitleRes = R.string.shell_profile_subtitle,
    ) {
        item {
            ShellHeroCard(
                icon = Icons.Outlined.AccountCircle,
                title = sessionState.user?.email ?: stringResource(R.string.shell_profile_email_placeholder),
                body = stringResource(R.string.shell_profile_hero_body),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    ShellMetricRow(
                        label = stringResource(R.string.shell_profile_metric_access),
                        value = stringResource(
                            if (sessionState.accessLevel == SessionAccessLevel.Verified) {
                                R.string.shell_profile_access_verified
                            } else {
                                R.string.shell_profile_access_cached
                            },
                        ),
                    )
                    ShellMetricRow(
                        label = stringResource(R.string.shell_profile_metric_network),
                        value = stringResource(
                            if (isOnline) {
                                R.string.shell_profile_network_online
                            } else {
                                R.string.shell_profile_network_offline
                            },
                        ),
                    )
                }
            }
        }

        item {
            ShellSectionCard(
                title = stringResource(R.string.shell_profile_section_title),
                eyebrow = stringResource(R.string.shell_section_ready_now),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    ShellFeatureRow(
                        icon = Icons.Outlined.Security,
                        title = stringResource(R.string.shell_profile_feature_session_title),
                        subtitle = sessionState.statusMessage
                            ?: stringResource(R.string.shell_profile_feature_session_body),
                    )
                    ShellDivider()
                    ShellFeatureRow(
                        icon = Icons.Outlined.Wifi,
                        title = stringResource(R.string.shell_profile_feature_connectivity_title),
                        subtitle = stringResource(
                            if (isOnline) {
                                R.string.shell_profile_feature_connectivity_online
                            } else {
                                R.string.shell_profile_feature_connectivity_offline
                            },
                        ),
                    )

                    if (sessionState.accessLevel == SessionAccessLevel.Cached) {
                        OutlinedButton(
                            onClick = onRetryRestore,
                        ) {
                            androidx.compose.material3.Text(text = stringResource(R.string.root_retry))
                        }
                    }

                    Button(
                        enabled = !isLoggingOut,
                        onClick = onLogout,
                    ) {
                        androidx.compose.material3.Text(
                            text = stringResource(
                                if (isLoggingOut) {
                                    R.string.shell_profile_logout_loading
                                } else {
                                    R.string.shell_profile_logout
                                },
                            ),
                        )
                    }
                }
            }
        }
    }
}
