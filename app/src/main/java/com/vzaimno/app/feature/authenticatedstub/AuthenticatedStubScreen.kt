package com.vzaimno.app.feature.authenticatedstub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vzaimno.app.core.designsystem.theme.spacing
import com.vzaimno.app.core.session.SessionAccessLevel
import com.vzaimno.app.core.session.SessionState

@Composable
fun AuthenticatedStubScreen(
    state: SessionState.Authenticated,
    isLoggingOut: Boolean,
    onRetryRestore: () -> Unit,
    onLogout: () -> Unit,
) {
    val details = buildList {
        add("Статус сессии" to if (state.accessLevel == SessionAccessLevel.Verified) "Подтверждена сервером" else "Восстановлена из локального кэша")
        add("Email" to (state.user?.email ?: "Пока недоступен"))
        add("Роль" to (state.user?.role ?: "Будет загружена позже"))
        add("Режим" to if (state.isOffline) "Оффлайн / ожидание сети" else "Онлайн")
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = MaterialTheme.spacing.xLarge,
                top = MaterialTheme.spacing.xxLarge,
                end = MaterialTheme.spacing.xLarge,
                bottom = MaterialTheme.spacing.xxxLarge,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(MaterialTheme.spacing.xLarge),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    ) {
                        Text(
                            text = "Сессия активна",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = state.user?.email ?: "Пользователь авторизован, но профиль ещё не успел обновиться.",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "На этом этапе основной shell ещё не поднимается. Вместо него показываем надёжный placeholder с контролем сессии и logout.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            if (state.statusMessage != null) {
                item {
                    Surface(
                        color = if (state.isOffline) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(MaterialTheme.spacing.large),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
                        ) {
                            Text(
                                text = if (state.isOffline) "Сессия ждёт сеть" else "Состояние сессии",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = state.statusMessage,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Минимальный authenticated stub",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            items(details) { detail ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.spacing.large),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xSmall),
                    ) {
                        Text(
                            text = detail.first,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = detail.second,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                ) {
                    if (state.accessLevel == SessionAccessLevel.Cached) {
                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            onClick = onRetryRestore,
                        ) {
                            Text(text = "Повторить проверку сессии")
                        }
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoggingOut,
                        onClick = onLogout,
                    ) {
                        Text(
                            text = if (isLoggingOut) "Выходим..." else "Выйти из аккаунта",
                        )
                    }
                }
            }
        }
    }
}
