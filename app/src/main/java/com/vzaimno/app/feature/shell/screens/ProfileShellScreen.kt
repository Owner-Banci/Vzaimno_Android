package com.vzaimno.app.feature.shell.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.ReviewRole
import com.vzaimno.app.core.session.SessionAccessLevel
import com.vzaimno.app.core.session.SessionState
import com.vzaimno.app.feature.profile.ProfileHomeViewModel
import com.vzaimno.app.feature.profile.ProfileHeroCard
import com.vzaimno.app.feature.profile.ProfileInfoRow
import com.vzaimno.app.feature.profile.ProfileMessageCard
import com.vzaimno.app.feature.profile.ProfileReviewsPreviewUiState
import com.vzaimno.app.feature.profile.ProfileSecondaryBadge
import com.vzaimno.app.feature.profile.ProfileSectionCard
import com.vzaimno.app.feature.profile.ProfileSkeletonLine
import com.vzaimno.app.feature.profile.ProfileTextActionRow
import com.vzaimno.app.feature.profile.ProfileDivider
import com.vzaimno.app.feature.profile.ProfileReviewCard
import com.vzaimno.app.feature.profile.ProfileHomeUiState

@Composable
fun ProfileShellScreen(
    sessionState: SessionState.Authenticated,
    isOnline: Boolean,
    onRetryRestore: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenReviews: (ReviewRole) -> Unit,
    refreshSignal: Boolean,
    onRefreshSignalHandled: () -> Unit,
    viewModel: ProfileHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    LaunchedEffect(refreshSignal) {
        if (refreshSignal) {
            viewModel.refreshAfterEdit()
            onRefreshSignalHandled()
        }
    }

    ProfileHomeScreen(
        state = state,
        sessionState = sessionState,
        isOnline = isOnline,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onRetryRestore = onRetryRestore,
        onOpenEdit = onOpenEdit,
        onOpenReviews = onOpenReviews,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileHomeScreen(
    state: ProfileHomeUiState,
    sessionState: SessionState.Authenticated,
    isOnline: Boolean,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onRetryRestore: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenReviews: (ReviewRole) -> Unit,
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
        ) {
            when {
                state.isInitialLoading && state.profile == null -> {
                    ProfileHomeLoadingState()
                }

                state.loadErrorMessage != null && state.profile == null -> {
                    ProfileHomeErrorState(
                        message = state.loadErrorMessage,
                        onRetry = onRetry,
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                                ),
                            ),
                        contentPadding = PaddingValues(
                            start = MaterialTheme.spacing.xLarge,
                            top = MaterialTheme.spacing.xxLarge,
                            end = MaterialTheme.spacing.xLarge,
                            bottom = MaterialTheme.spacing.xxxLarge,
                        ),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.profile_screen_title),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }

                        if (!state.contentMessage.isNullOrBlank()) {
                            item {
                                ProfileMessageCard(
                                    title = stringResource(R.string.profile_inline_error_title),
                                    message = state.contentMessage,
                                    actionLabel = stringResource(R.string.root_retry),
                                    onAction = onRetry,
                                )
                            }
                        }

                        state.profile?.let { profile ->
                            item {
                                ProfileHeroCard(
                                    profile = profile,
                                    onEdit = onOpenEdit,
                                    onOpenReviews = {
                                        onOpenReviews(ReviewRole.Performer)
                                    },
                                )
                            }

                            item {
                                ProfileSectionCard(
                                    title = stringResource(R.string.profile_settings_section_title),
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                                    ) {
                                        ProfileInfoRow(
                                            icon = Icons.Outlined.DarkMode,
                                            title = stringResource(R.string.profile_settings_dark_theme),
                                            subtitle = stringResource(R.string.profile_settings_dark_theme_subtitle),
                                            trailing = {
                                                ProfileSecondaryBadge(labelRes = R.string.profile_secondary_soon)
                                            },
                                        )
                                        ProfileDivider()
                                        ProfileInfoRow(
                                            icon = Icons.Outlined.Notifications,
                                            title = stringResource(R.string.profile_settings_notifications),
                                            subtitle = stringResource(R.string.profile_settings_notifications_subtitle),
                                            trailing = {
                                                ProfileSecondaryBadge(labelRes = R.string.profile_secondary_soon)
                                            },
                                        )
                                        ProfileDivider()
                                        ProfileInfoRow(
                                            icon = Icons.Outlined.Payments,
                                            title = stringResource(R.string.profile_settings_payments),
                                            subtitle = stringResource(R.string.profile_settings_payments_subtitle),
                                            trailing = {
                                                ProfileSecondaryBadge(labelRes = R.string.profile_secondary_soon)
                                            },
                                        )
                                    }
                                }
                            }

                            item {
                                ProfileSectionCard(
                                    title = stringResource(R.string.profile_account_section_title),
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                                    ) {
                                        ProfileTextActionRow(
                                            title = stringResource(R.string.profile_account_edit_title),
                                            subtitle = stringResource(R.string.profile_account_edit_subtitle),
                                            onClick = onOpenEdit,
                                        )
                                        ProfileDivider()
                                        ProfileInfoRow(
                                            icon = Icons.Outlined.CheckCircle,
                                            title = stringResource(R.string.profile_account_session_title),
                                            subtitle = stringResource(
                                                if (sessionState.accessLevel == SessionAccessLevel.Verified) {
                                                    R.string.profile_session_verified
                                                } else {
                                                    R.string.profile_session_cached
                                                },
                                            ),
                                            trailing = {
                                                if (sessionState.accessLevel == SessionAccessLevel.Cached) {
                                                    TextButton(onClick = onRetryRestore) {
                                                        Text(text = stringResource(R.string.root_retry))
                                                    }
                                                }
                                            },
                                        )
                                        ProfileDivider()
                                        ProfileInfoRow(
                                            icon = Icons.Outlined.Wifi,
                                            title = stringResource(R.string.profile_account_network_title),
                                            subtitle = stringResource(
                                                if (isOnline) {
                                                    R.string.profile_network_online
                                                } else {
                                                    R.string.profile_network_offline
                                                },
                                            ),
                                        )
                                        ProfileDivider()
                                        ProfileInfoRow(
                                            icon = Icons.Outlined.LocationOn,
                                            title = profile.city.ifBlank {
                                                stringResource(R.string.profile_location_missing_title)
                                            },
                                            subtitle = profile.preferredAddress.ifBlank {
                                                stringResource(R.string.profile_location_missing_subtitle)
                                            },
                                        )
                                    }
                                }
                            }

                            item {
                                ProfileSectionCard(
                                    title = stringResource(R.string.profile_support_section_title),
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                                    ) {
                                        ProfileInfoRow(
                                            icon = Icons.AutoMirrored.Outlined.HelpOutline,
                                            title = stringResource(R.string.profile_support_help_title),
                                            subtitle = stringResource(R.string.profile_support_help_subtitle),
                                            trailing = {
                                                ProfileSecondaryBadge(labelRes = R.string.profile_secondary_soon)
                                            },
                                        )
                                        ProfileDivider()
                                        ProfileInfoRow(
                                            icon = Icons.Outlined.Policy,
                                            title = stringResource(R.string.profile_support_rules_title),
                                            subtitle = stringResource(R.string.profile_support_rules_subtitle),
                                            trailing = {
                                                ProfileSecondaryBadge(labelRes = R.string.profile_secondary_soon)
                                            },
                                        )
                                    }
                                }
                            }

                            item {
                                ProfileSectionCard(
                                    title = stringResource(R.string.profile_reviews_section_title),
                                    action = {
                                        TextButton(
                                            onClick = { onOpenReviews(ReviewRole.Performer) },
                                        ) {
                                            Text(text = stringResource(R.string.profile_reviews_action_all))
                                        }
                                    },
                                ) {
                                    when (val previewState = state.previewState) {
                                        ProfileReviewsPreviewUiState.Loading -> {
                                            ProfilePreviewLoadingState()
                                        }

                                        ProfileReviewsPreviewUiState.Empty -> {
                                            ProfileMessageCard(
                                                title = stringResource(R.string.profile_reviews_empty_title),
                                                message = stringResource(R.string.profile_reviews_empty_performer),
                                                actionLabel = stringResource(R.string.profile_reviews_action_all),
                                                onAction = { onOpenReviews(ReviewRole.Performer) },
                                            )
                                        }

                                        is ProfileReviewsPreviewUiState.Error -> {
                                            ProfileMessageCard(
                                                title = stringResource(R.string.profile_reviews_error_title),
                                                message = previewState.message,
                                                actionLabel = stringResource(R.string.root_retry),
                                                onAction = onRetry,
                                            )
                                        }

                                        is ProfileReviewsPreviewUiState.Content -> {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                                            ) {
                                                Surface(
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(16.dp),
                                                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.profile_reviews_preview_label),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                                                            verticalAlignment = Alignment.Bottom,
                                                        ) {
                                                            Text(
                                                                text = previewState.summary.average.toUiRating(),
                                                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                            )
                                                            Text(
                                                                text = stringResource(
                                                                    R.string.profile_reviews_summary_count,
                                                                    previewState.summary.count,
                                                                ),
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            )
                                                        }
                                                    }
                                                }

                                                previewState.reviews.forEach { review ->
                                                    ProfileReviewCard(review = review)
                                                }
                                            }
                                        }
                                    }
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
private fun ProfileHomeLoadingState() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            ),
        contentPadding = PaddingValues(
            start = MaterialTheme.spacing.xLarge,
            top = MaterialTheme.spacing.xxLarge,
            end = MaterialTheme.spacing.xLarge,
            bottom = MaterialTheme.spacing.xxxLarge,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                ProfileSkeletonLine(
                    modifier = Modifier.fillMaxWidth(0.22f),
                    height = 14,
                )
                ProfileSkeletonLine(
                    modifier = Modifier.fillMaxWidth(0.46f),
                    height = 36,
                )
                ProfileSkeletonLine(
                    modifier = Modifier.fillMaxWidth(0.88f),
                    height = 18,
                )
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.66f),
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                )
                                .size(64.dp),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        ) {
                            ProfileSkeletonLine(
                                modifier = Modifier.fillMaxWidth(0.56f),
                                height = 24,
                            )
                            ProfileSkeletonLine(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                height = 18,
                            )
                        }
                    }
                    repeat(2) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                        ) {
                            repeat(2) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 92.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                                    ) {
                                        ProfileSkeletonLine(
                                            modifier = Modifier.fillMaxWidth(0.42f),
                                            height = 18,
                                        )
                                        ProfileSkeletonLine(
                                            modifier = Modifier.fillMaxWidth(0.72f),
                                            height = 16,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        repeat(3) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                    ) {
                        ProfileSkeletonLine(
                            modifier = Modifier.fillMaxWidth(0.44f),
                            height = 18,
                        )
                        repeat(2) {
                            ProfileSkeletonLine(
                                modifier = Modifier.fillMaxWidth(),
                                height = 56,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHomeErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        ProfileMessageCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(MaterialTheme.spacing.xLarge),
            title = stringResource(R.string.profile_error_title),
            message = message,
            actionLabel = stringResource(R.string.root_retry),
            onAction = onRetry,
        )
    }
}

@Composable
private fun ProfilePreviewLoadingState() {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            ) {
                ProfileSkeletonLine(
                    modifier = Modifier.fillMaxWidth(0.34f),
                    height = 16,
                )
                ProfileSkeletonLine(
                    modifier = Modifier.fillMaxWidth(0.28f),
                    height = 24,
                )
            }
        }
        repeat(2) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    ProfileSkeletonLine(
                        modifier = Modifier.fillMaxWidth(0.46f),
                        height = 18,
                    )
                    ProfileSkeletonLine(
                        modifier = Modifier.fillMaxWidth(),
                        height = 16,
                    )
                    ProfileSkeletonLine(
                        modifier = Modifier.fillMaxWidth(0.3f),
                        height = 14,
                    )
                }
            }
        }
    }
}

private fun Double.toUiRating(): String = String.format("%.1f", this)
