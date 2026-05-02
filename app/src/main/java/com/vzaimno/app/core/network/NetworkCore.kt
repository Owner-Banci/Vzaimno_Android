package com.vzaimno.app.core.network

import java.io.IOException
import javax.net.ssl.SSLException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

enum class ApiErrorKind {
    Unauthorized,
    Forbidden,
    Validation,
    Client,
    Server,
    Connectivity,
    Serialization,
    Unknown,
}

data class ApiError(
    val kind: ApiErrorKind,
    val message: String,
    val statusCode: Int? = null,
) {
    val invalidatesSession: Boolean
        get() = kind == ApiErrorKind.Unauthorized || kind == ApiErrorKind.Forbidden
}

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

data class UploadFilePayload(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
)

interface AuthTokenProvider {
    fun currentToken(): String?
}

class BearerTokenInterceptor @Inject constructor(
    private val tokenProvider: AuthTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain) = chain.proceed(
        chain.request().newBuilder().apply {
            val token = tokenProvider.currentToken()
            val alreadyHasAuthorization = chain.request().header("Authorization") != null
            if (!token.isNullOrBlank() && !alreadyHasAuthorization) {
                header("Authorization", "Bearer $token")
            }
        }.build()
    )
}

@Singleton
class ApiErrorMapper @Inject constructor(
    private val json: Json,
) {

    fun map(throwable: Throwable): ApiError = when (throwable) {
        is HttpException -> map(
            statusCode = throwable.code(),
            responseBody = throwable.response()?.errorBody()?.string(),
        )

        is IOException -> ApiError(
            kind = ApiErrorKind.Connectivity,
            message = buildConnectivityMessage(throwable),
        )

        is SerializationException -> ApiError(
            kind = ApiErrorKind.Serialization,
            message = "Не удалось обновить данные. Попробуйте ещё раз.",
        )

        else -> ApiError(
            kind = ApiErrorKind.Unknown,
            message = throwable.message ?: "Неизвестная ошибка.",
        )
    }

    fun map(statusCode: Int, responseBody: String?): ApiError {
        val parsedMessage = responseBody?.let(::extractFastApiMessage).orEmpty()
        val fallbackMessage = when (statusCode) {
            401 -> "Сессия истекла. Войдите снова."
            403 -> "Доступ запрещён."
            in 400..499 -> "Ошибка запроса."
            in 500..599 -> "Сервер временно недоступен."
            else -> "HTTP $statusCode"
        }
        val message = parsedMessage.ifBlank { responseBody?.trim().orEmpty() }.ifBlank { fallbackMessage }

        return ApiError(
            kind = when (statusCode) {
                401 -> ApiErrorKind.Unauthorized
                403 -> ApiErrorKind.Forbidden
                422 -> ApiErrorKind.Validation
                in 400..499 -> ApiErrorKind.Client
                in 500..599 -> ApiErrorKind.Server
                else -> ApiErrorKind.Unknown
            },
            message = message,
            statusCode = statusCode,
        )
    }

    private fun extractFastApiMessage(rawBody: String): String {
        val payload = runCatching {
            json.decodeFromString(FastApiErrorPayload.serializer(), rawBody)
        }.getOrNull() ?: return ""

        return when (val detail = payload.detail) {
            null -> ""
            is JsonArray -> {
                val messages = detail.mapNotNull { element ->
                    (element as? JsonObject)?.get("msg")?.let { it as? JsonPrimitive }?.contentOrNull
                }
                if (messages.any { it.contains("value is not a valid email address") }) {
                    "Неверный email. Пример: name@mail.com"
                } else {
                    messages.joinToString(separator = "\n")
                }
            }

            else -> (detail as? JsonPrimitive)?.contentOrNull.orEmpty()
        }
    }
}

private fun buildConnectivityMessage(throwable: IOException): String {
    val details = throwable.message?.trim().orEmpty()
    val isTlsError = throwable is SSLException ||
        details.contains("tls", ignoreCase = true) ||
        details.contains("ssl", ignoreCase = true) ||
        details.contains("secure connection", ignoreCase = true)
    return if (isTlsError) {
        "Не удалось установить безопасное соединение. Проверьте подключение к интернету и попробуйте снова."
    } else {
        "Не удалось загрузить данные. Проверьте подключение к интернету и попробуйте снова."
    }
}

suspend inline fun <T> safeApiCall(
    apiErrorMapper: ApiErrorMapper,
    crossinline block: suspend () -> T,
): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (throwable: Throwable) {
    ApiResult.Failure(apiErrorMapper.map(throwable))
}

fun UploadFilePayload.toMultipartPart(fieldName: String = "files"): MultipartBody.Part {
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
}

@Serializable
private data class FastApiErrorPayload(
    val detail: JsonElement? = null,
)
