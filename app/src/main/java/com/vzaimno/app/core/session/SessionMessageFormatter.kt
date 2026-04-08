package com.vzaimno.app.core.session

import com.vzaimno.app.core.network.ApiError
import com.vzaimno.app.core.network.ApiErrorKind
import java.util.Locale

object SessionMessageFormatter {

    fun loginError(error: ApiError): String = when {
        error.kind == ApiErrorKind.Connectivity ->
            "Нет подключения к интернету. Проверьте сеть и попробуйте снова."

        error.kind == ApiErrorKind.Unauthorized || error.message.matchesAny(
            "invalid credential",
            "invalid credentials",
            "incorrect password",
            "wrong password",
            "invalid email or password",
            "incorrect email or password",
            "not authenticated",
        ) -> "Неверный email или пароль."

        error.kind == ApiErrorKind.Forbidden ->
            "Вход для этого аккаунта сейчас недоступен."

        error.kind == ApiErrorKind.Validation ->
            safeBackendMessage(error.message, fallback = "Проверьте email и пароль.")

        error.kind == ApiErrorKind.Server ->
            "Сервер временно недоступен. Попробуйте чуть позже."

        error.kind == ApiErrorKind.Client ->
            safeBackendMessage(error.message, fallback = "Не удалось войти. Проверьте введённые данные.")

        else ->
            "Не удалось войти. Попробуйте ещё раз."
    }

    fun registerError(error: ApiError): String = when {
        error.kind == ApiErrorKind.Connectivity ->
            "Нет подключения к интернету. Проверьте сеть и попробуйте снова."

        error.message.matchesAny(
            "already exists",
            "already registered",
            "already in use",
            "user exists",
            "email exists",
            "duplicate",
        ) -> "Аккаунт с таким email уже существует."

        error.kind == ApiErrorKind.Validation ->
            safeBackendMessage(error.message, fallback = "Проверьте данные формы.")

        error.kind == ApiErrorKind.Server ->
            "Сейчас не удалось создать аккаунт. Попробуйте чуть позже."

        error.kind == ApiErrorKind.Client ->
            safeBackendMessage(error.message, fallback = "Не удалось создать аккаунт. Проверьте введённые данные.")

        else ->
            "Не удалось создать аккаунт. Попробуйте ещё раз."
    }

    fun restoreError(error: ApiError): String = when (error.kind) {
        ApiErrorKind.Connectivity ->
            "Не удалось проверить сохранённую сессию без интернета."

        ApiErrorKind.Server ->
            "Сервер временно недоступен. Попробуйте восстановить сессию ещё раз."

        ApiErrorKind.Unauthorized,
        ApiErrorKind.Forbidden,
        -> "Сессия истекла. Войдите снова."

        else ->
            "Не удалось восстановить сессию. Попробуйте ещё раз."
    }

    fun cachedSessionBanner(error: ApiError): String = when (error.kind) {
        ApiErrorKind.Connectivity ->
            "Не удалось подтвердить сессию без интернета. Показываем сохранённые данные."

        ApiErrorKind.Server ->
            "Сервер временно недоступен. Используем сохранённую сессию."

        else ->
            "Не удалось обновить данные сессии. Используем сохранённое состояние."
    }

    fun postAuthBanner(error: ApiError): String = when (error.kind) {
        ApiErrorKind.Connectivity ->
            "Вход выполнен, но профиль пока недоступен без интернета."

        ApiErrorKind.Server ->
            "Вход выполнен, но сервер временно недоступен. Сессия сохранена."

        else ->
            "Вход выполнен, но обновить данные профиля сейчас не удалось."
    }

    private fun safeBackendMessage(message: String, fallback: String): String {
        val trimmed = message.trim()
        return if (trimmed.isBlank() || trimmed.looksTechnical()) {
            fallback
        } else {
            trimmed
        }
    }

    private fun String.looksTechnical(): Boolean {
        val normalized = lowercase(Locale.ROOT)
        return contains("{") ||
            contains("}") ||
            contains("[") ||
            contains("]") ||
            normalized.contains("traceback") ||
            normalized.contains("exception") ||
            normalized.contains("stack") ||
            normalized.contains("html") ||
            normalized.startsWith("http ")
    }

    private fun String.matchesAny(vararg markers: String): Boolean {
        val normalized = lowercase(Locale.ROOT)
        return markers.any { marker -> normalized.contains(marker) }
    }
}
