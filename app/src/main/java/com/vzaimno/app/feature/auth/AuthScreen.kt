package com.vzaimno.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vzaimno.app.core.designsystem.theme.spacing

@Composable
fun AuthRoute(
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    AuthScreen(
        state = state,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onToggleMode = viewModel::onToggleMode,
        onSubmit = viewModel::submit,
    )
}

@Composable
private fun AuthScreen(
    state: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                AuthBottomBar(
                    state = state,
                    onSubmit = onSubmit,
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = MaterialTheme.spacing.xLarge)
                    .padding(top = MaterialTheme.spacing.xxLarge)
                    .padding(bottom = MaterialTheme.spacing.xxLarge)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            ) {
                AuthHero(mode = state.mode)

                if (state.isOffline) {
                    InfoCard(
                        title = "Нет подключения",
                        body = "Форма доступна, но отправить вход или регистрацию получится только после восстановления сети.",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
                    ) {
                        Text(
                            text = if (state.mode == AuthMode.Login) {
                                "Войдите в аккаунт"
                            } else {
                                "Создайте аккаунт"
                            },
                            style = MaterialTheme.typography.titleLarge,
                        )

                        FormTextField(
                            value = state.email,
                            onValueChange = onEmailChanged,
                            label = "Email",
                            error = state.emailError,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        )

                        FormTextField(
                            value = state.password,
                            onValueChange = onPasswordChanged,
                            label = "Пароль",
                            error = state.passwordError,
                            keyboardType = KeyboardType.Password,
                            imeAction = if (state.mode == AuthMode.Login) ImeAction.Done else ImeAction.Next,
                            isPassword = true,
                        )

                        if (state.mode == AuthMode.Register) {
                            FormTextField(
                                value = state.confirmPassword,
                                onValueChange = onConfirmPasswordChanged,
                                label = "Повторите пароль",
                                error = state.confirmPasswordError,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                isPassword = true,
                            )
                        }

                        if (state.errorMessage != null) {
                            InfoCard(
                                title = "Не получилось",
                                body = state.errorMessage,
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }

                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = !state.isSubmitting,
                    onClick = onToggleMode,
                ) {
                    Text(
                        text = if (state.mode == AuthMode.Login) {
                            "Нет аккаунта? Создать"
                        } else {
                            "Уже есть аккаунт? Войти"
                        },
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xxxLarge))
            }
        }
    }
}

@Composable
private fun AuthHero(mode: AuthMode) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
    ) {
        Text(
            text = "Vzaimno",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = if (mode == AuthMode.Login) {
                "Возвращайтесь к своим задачам без лишних шагов"
            } else {
                "Создайте аккаунт и подготовьте приложение к следующему этапу"
            },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Android-клиент сначала проверяет сессию, аккуратно работает с сетью и только потом открывает авторизованную часть приложения.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AuthBottomBar(
    state: AuthUiState,
    onSubmit: () -> Unit,
) {
    Surface(
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = MaterialTheme.spacing.xLarge)
                .padding(top = MaterialTheme.spacing.medium, bottom = MaterialTheme.spacing.large),
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isSubmitting,
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
                onClick = onSubmit,
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = state.submitLabel,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { message -> { Text(text = message) } },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
private fun InfoCard(
    title: String,
    body: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                textAlign = TextAlign.Start,
            )
        }
    }
}
