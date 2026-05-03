package com.vzaimno.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vzaimno.app.R
import com.vzaimno.app.core.designsystem.components.ExpandableOutlinedTextField
import com.vzaimno.app.core.designsystem.theme.spacing
import kotlinx.coroutines.launch

@Composable
fun ProfileEditRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onLogout: () -> Unit,
    isLoggingOut: Boolean,
    viewModel: ProfileEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var isLogoutDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadIfNeeded()
    }

    if (isLogoutDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoggingOut) {
                    isLogoutDialogVisible = false
                }
            },
            title = {
                Text(text = stringResource(R.string.profile_logout_dialog_title))
            },
            text = {
                Text(text = stringResource(R.string.profile_logout_dialog_message))
            },
            confirmButton = {
                TextButton(
                    enabled = !isLoggingOut,
                    onClick = {
                        isLogoutDialogVisible = false
                        onLogout()
                    },
                ) {
                    Text(text = stringResource(R.string.profile_logout_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isLoggingOut,
                    onClick = { isLogoutDialogVisible = false },
                ) {
                    Text(text = stringResource(R.string.profile_logout_cancel))
                }
            },
        )
    }

    ProfileEditScreen(
        state = state,
        isLoggingOut = isLoggingOut,
        onBack = onBack,
        onRetry = viewModel::retry,
        onDisplayNameChanged = viewModel::onDisplayNameChanged,
        onBioChanged = viewModel::onBioChanged,
        onCityChanged = viewModel::onCityChanged,
        onPreferredAddressChanged = viewModel::onPreferredAddressChanged,
        onSave = {
            scope.launch {
                if (viewModel.save()) {
                    onSaved()
                }
            }
        },
        onLogout = { isLogoutDialogVisible = true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditScreen(
    state: ProfileEditUiState,
    isLoggingOut: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onPreferredAddressChanged: (String) -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit,
) {
    val isBusy = state.isLoading || state.isSaving || isLoggingOut

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.profile_edit_title))
                },
                navigationIcon = {
                    IconButton(
                        enabled = !isBusy,
                        onClick = onBack,
                    ) {
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
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = MaterialTheme.spacing.xLarge)
                        .padding(top = MaterialTheme.spacing.medium, bottom = MaterialTheme.spacing.large)
                        .height(56.dp),
                    enabled = state.profile != null && !isBusy,
                    onClick = onSave,
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(text = stringResource(R.string.profile_edit_save))
                    }
                }
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading && state.profile == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.loadErrorMessage != null && state.profile == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                        ),
                ) {
                    ProfileMessageCard(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(MaterialTheme.spacing.xLarge),
                        title = stringResource(R.string.profile_edit_error_title),
                        message = state.loadErrorMessage,
                        actionLabel = stringResource(R.string.root_retry),
                        onAction = onRetry,
                    )
                }
            }

            else -> {
                val profile = state.profile
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                        )
                        .imePadding(),
                    contentPadding = PaddingValues(
                        start = MaterialTheme.spacing.xLarge,
                        top = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.xLarge,
                        bottom = 140.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                ) {
                    item {
                        ProfileSectionCard(
                            title = stringResource(R.string.profile_edit_section_identity),
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                            ) {
                                ProfileTextField(
                                    value = state.displayName,
                                    onValueChange = onDisplayNameChanged,
                                    label = stringResource(R.string.profile_edit_display_name_label),
                                    placeholder = stringResource(R.string.profile_edit_display_name_placeholder),
                                    error = state.displayNameError,
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next,
                                )

                                profile?.let { loadedProfile ->
                                    ProfileInfoRow(
                                        icon = Icons.Outlined.Badge,
                                        title = stringResource(R.string.profile_edit_contact_title),
                                        subtitle = loadedProfile.primaryContact
                                            ?: stringResource(R.string.profile_primary_contact_missing),
                                    )
                                }
                            }
                        }
                    }

                    item {
                        ProfileSectionCard(
                            title = stringResource(R.string.profile_edit_section_about),
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                            ) {
                                ProfileTextField(
                                    value = state.city,
                                    onValueChange = onCityChanged,
                                    label = stringResource(R.string.profile_edit_city_label),
                                    placeholder = stringResource(R.string.profile_edit_city_placeholder),
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationCity,
                                            contentDescription = null,
                                        )
                                    },
                                )
                                ProfileTextField(
                                    value = state.bio,
                                    onValueChange = onBioChanged,
                                    label = stringResource(R.string.profile_edit_bio_label),
                                    placeholder = stringResource(R.string.profile_edit_bio_placeholder),
                                    error = state.bioError,
                                    minLines = 4,
                                    maxLines = 6,
                                    supportingText = stringResource(
                                        R.string.profile_edit_bio_counter,
                                        state.bio.length,
                                        ProfileBioMaxLength,
                                    ),
                                )
                            }
                        }
                    }

                    item {
                        ProfileSectionCard(
                            title = stringResource(R.string.profile_edit_section_location),
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                            ) {
                                ProfileTextField(
                                    value = state.preferredAddress,
                                    onValueChange = onPreferredAddressChanged,
                                    label = stringResource(R.string.profile_edit_address_label),
                                    placeholder = stringResource(R.string.profile_edit_address_placeholder),
                                    error = state.preferredAddressError,
                                    minLines = 2,
                                    maxLines = 4,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.PinDrop,
                                            contentDescription = null,
                                        )
                                    },
                                )
                                Text(
                                    text = stringResource(R.string.profile_edit_address_caption),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    profile?.let { loadedProfile ->
                        item {
                            ProfileSectionCard(
                                title = stringResource(R.string.profile_edit_section_stats),
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                                ) {
                                    listOf(
                                        Triple(
                                            Icons.Outlined.StarOutline,
                                            R.string.profile_metric_rating,
                                            loadedProfile.stats.ratingAverage.formatForUi(),
                                        ),
                                        Triple(
                                            Icons.Outlined.StarOutline,
                                            R.string.profile_metric_rating_count,
                                            loadedProfile.stats.ratingCount.toString(),
                                        ),
                                        Triple(
                                            Icons.Outlined.StarOutline,
                                            R.string.profile_metric_completed_count,
                                            loadedProfile.stats.completedCount.toString(),
                                        ),
                                        Triple(
                                            Icons.Outlined.StarOutline,
                                            R.string.profile_metric_cancelled_count,
                                            loadedProfile.stats.cancelledCount.toString(),
                                        ),
                                    ).forEachIndexed { index, entry ->
                                        ProfileInfoRow(
                                            icon = entry.first,
                                            title = stringResource(entry.second),
                                            subtitle = entry.third,
                                        )
                                        if (index != 3) {
                                            ProfileDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!state.formErrorMessage.isNullOrBlank()) {
                        item {
                            ProfileMessageCard(
                                title = stringResource(R.string.profile_edit_save_error_title),
                                message = state.formErrorMessage,
                            )
                        }
                    }

                    item {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isBusy,
                            onClick = onLogout,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = null,
                            )
                            Text(
                                modifier = Modifier.padding(start = MaterialTheme.spacing.small),
                                text = stringResource(
                                    if (isLoggingOut) {
                                        R.string.shell_profile_logout_loading
                                    } else {
                                        R.string.profile_logout_action
                                    },
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    supportingText: String? = null,
    minLines: Int = 1,
    maxLines: Int = 1,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = ImeAction.Default,
    leadingIcon: (@Composable (() -> Unit))? = null,
) {
    val supportingContent: @Composable (() -> Unit) = {
        when {
            error != null -> Text(text = error)
            !supportingText.isNullOrBlank() -> Text(text = supportingText)
        }
    }
    val keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
        capitalization = capitalization,
        imeAction = imeAction,
    )

    if (maxLines > 1) {
        ExpandableOutlinedTextField(
            modifier = modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label) },
            placeholder = { Text(text = placeholder) },
            isError = error != null,
            minLines = minLines,
            collapsedMaxLines = maxLines,
            leadingIcon = leadingIcon,
            keyboardOptions = keyboardOptions,
            supportingText = supportingContent,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        )
    } else {
        OutlinedTextField(
            modifier = modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label) },
            placeholder = { Text(text = placeholder) },
            isError = error != null,
            minLines = minLines,
            maxLines = maxLines,
            leadingIcon = leadingIcon,
            keyboardOptions = keyboardOptions,
            supportingText = {
            when {
                error != null -> Text(text = error)
                !supportingText.isNullOrBlank() -> Text(text = supportingText)
            }
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        )
    }
}

private fun Double.formatForUi(): String = String.format("%.1f", this)
