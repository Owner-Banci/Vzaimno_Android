package com.vzaimno.app.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.vzaimno.app.core.model.AccessToken
import com.vzaimno.app.core.model.SessionUser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface SecureSessionStorage {
    fun readToken(): AccessToken?
    fun writeToken(token: AccessToken)
    fun clearToken()
    fun readUser(): SessionUser?
    fun writeUser(user: SessionUser)
    fun clearUser()
    fun clear()
}

@Singleton
class EncryptedSecureSessionStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : SecureSessionStorage {

    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        EncryptedSharedPreferences.create(
            PREFS_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun readToken(): AccessToken? = readDecoded(TOKEN_KEY)

    override fun writeToken(token: AccessToken) {
        writeEncoded(TOKEN_KEY, token)
    }

    override fun clearToken() {
        prefs.edit(commit = true) { remove(TOKEN_KEY) }
    }

    override fun readUser(): SessionUser? = readDecoded(USER_KEY)

    override fun writeUser(user: SessionUser) {
        writeEncoded(USER_KEY, user)
    }

    override fun clearUser() {
        prefs.edit(commit = true) { remove(USER_KEY) }
    }

    override fun clear() {
        prefs.edit(commit = true) {
            remove(TOKEN_KEY)
            remove(USER_KEY)
        }
    }

    private inline fun <reified T> readDecoded(key: String): T? {
        val rawValue = prefs.getString(key, null) ?: return null
        return runCatching { json.decodeFromString<T>(rawValue) }
            .getOrElse {
                prefs.edit(commit = true) { remove(key) }
                null
            }
    }

    private inline fun <reified T> writeEncoded(key: String, value: T) {
        prefs.edit(commit = true) {
            putString(key, json.encodeToString(value))
        }
    }

    private companion object {
        const val PREFS_FILE_NAME = "vzaimno_secure_session"
        const val TOKEN_KEY = "access_token"
        const val USER_KEY = "session_user"
    }
}
