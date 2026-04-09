package com.vzaimno.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.model.ReviewRole

@Composable
fun ProfileReviewsRoute(
    onBack: () -> Unit,
    viewModel: ProfileReviewsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    ProfileReviewsScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onRoleSelected = viewModel::selectRole,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileReviewsScreen(
    state: ProfileReviewsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onRoleSelected: (ReviewRole) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.profile_reviews_screen_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.profile_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        when (val screenState = state.screenState) {
            ProfileReviewsScreenState.Idle,
            ProfileReviewsScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProfileReviewsScreenState.Error -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                        ),
                    contentPadding = PaddingValues(
                        start = MaterialTheme.spacing.xLarge,
                        top = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.xLarge,
                        bottom = 40.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    item {
                        ProfileRoleSegmentedRow(
                            selectedRole = state.selectedRole,
                            onRoleSelected = onRoleSelected,
                        )
                    }
                    item {
                        ProfileMessageCard(
                            title = stringResource(R.string.profile_reviews_error_title),
                            message = screenState.message,
                            actionLabel = stringResource(R.string.root_retry),
                            onAction = onRetry,
                        )
                    }
                }
            }

            ProfileReviewsScreenState.Empty,
            ProfileReviewsScreenState.Loaded -> {
                PullToRefreshBox(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                            ),
                        contentPadding = PaddingValues(
                            start = MaterialTheme.spacing.xLarge,
                            top = MaterialTheme.spacing.medium,
                            end = MaterialTheme.spacing.xLarge,
                            bottom = 40.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                    ) {
                        item {
                            ProfileRoleSegmentedRow(
                                selectedRole = state.selectedRole,
                                onRoleSelected = onRoleSelected,
                            )
                        }

                        if (!state.contentMessage.isNullOrBlank()) {
                            item {
                                ProfileMessageCard(
                                    title = stringResource(R.string.profile_reviews_inline_error_title),
                                    message = state.contentMessage,
                                    actionLabel = stringResource(R.string.root_retry),
                                    onAction = onRetry,
                                )
                            }
                        }

                        item {
                            ProfileReviewsSummaryCard(
                                role = state.selectedRole,
                                summary = state.summary,
                            )
                        }

                        if (screenState == ProfileReviewsScreenState.Empty) {
                            item {
                                ProfileMessageCard(
                                    title = stringResource(R.string.profile_reviews_empty_title),
                                    message = stringResource(
                                        if (state.selectedRole == ReviewRole.Performer) {
                                            R.string.profile_reviews_empty_performer
                                        } else {
                                            R.string.profile_reviews_empty_customer
                                        },
                                    ),
                                )
                            }
                        } else {
                            items(
                                items = state.reviews,
                                key = { review -> review.id },
                            ) { review ->
                                ProfileReviewCard(review = review)
                            }
                        }
                    }
                }
            }
        }
    }
}
