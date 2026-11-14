package com.example.data.local.prefs

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppLanguage
import com.example.domain.model.TikTokUserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tiktok_cleaner_prefs")

class SecureTokenStorage(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val KEYSTORE_ALIAS = "tiktok_cleaner_token_aes_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private val KEY_ACCESS_TOKEN = stringPreferencesKey("enc_access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("enc_refresh_token")
        private val KEY_TOKEN_EXPIRES_AT = longPreferencesKey("token_expires_at")
        private val KEY_PROFILE_JSON = stringPreferencesKey("profile_json")
        private val KEY_OAUTH_STATE = stringPreferencesKey("oauth_state")
        private val KEY_CODE_VERIFIER = stringPreferencesKey("oauth_code_verifier")
        private val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_HIDE_HISTORY_THUMBS = booleanPreferencesKey("hide_history_thumbs")
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_DARK_MODE] ?: true
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_LANGUAGE]) {
            AppLanguage.ID.name -> AppLanguage.ID
            else -> AppLanguage.EN
        }
    }

    val hideHistoryThumbsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIDE_HISTORY_THUMBS] ?: false
    }

    val userProfileFlow: Flow<TikTokUserProfile?> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_PROFILE_JSON] ?: return@map null
        runCatching { json.decodeFromString<TikTokUserProfile>(raw) }.getOrNull()
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_IS_DARK_MODE] = enabled }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language.name }
    }

    suspend fun setHideHistoryThumbnails(hide: Boolean) {
        context.dataStore.edit { it[KEY_HIDE_HISTORY_THUMBS] = hide }
    }

    suspend fun saveOAuthPendingParams(state: String, codeVerifier: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OAUTH_STATE] = state
            prefs[KEY_CODE_VERIFIER] = codeVerifier
        }
    }

    suspend fun getAndClearOAuthPendingParams(): Pair<String?, String?> {
        val prefs = context.dataStore.data.first()
        val state = prefs[KEY_OAUTH_STATE]
        val verifier = prefs[KEY_CODE_VERIFIER]
        context.dataStore.edit {
            it.remove(KEY_OAUTH_STATE)
            it.remove(KEY_CODE_VERIFIER)
        }
        return state to verifier
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String?, expiresInSeconds: Long) {
        val encAccess = encrypt(accessToken)
        val encRefresh = refreshToken?.let { encrypt(it) }
        val expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000L)
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = encAccess
            if (encRefresh != null) {
                prefs[KEY_REFRESH_TOKEN] = encRefresh
            } else {
                prefs.remove(KEY_REFRESH_TOKEN)
            }
            prefs[KEY_TOKEN_EXPIRES_AT] = expiresAt
        }
    }

    suspend fun getAccessToken(): String? {
        val prefs = context.dataStore.data.first()
        val enc = prefs[KEY_ACCESS_TOKEN] ?: return null
        return decrypt(enc)
    }

    suspend fun saveProfile(profile: TikTokUserProfile) {
        val encoded = json.encodeToString(profile)
        context.dataStore.edit { it[KEY_PROFILE_JSON] = encoded }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_TOKEN_EXPIRES_AT)
            prefs.remove(KEY_PROFILE_JSON)
            prefs.remove(KEY_OAUTH_STATE)
            prefs.remove(KEY_CODE_VERIFIER)
        }
    }

    private fun getOrCreateAesKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGen.init(spec)
        return keyGen.generateKey()
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateAesKey())
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(1 + iv.size + cipherBytes.size)
        combined[0] = iv.size.toByte()
        System.arraycopy(iv, 0, combined, 1, iv.size)
        System.arraycopy(cipherBytes, 0, combined, 1 + iv.size, cipherBytes.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String? {
        return runCatching {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            val ivLength = combined[0].toInt()
            val iv = combined.copyOfRange(1, 1 + ivLength)
            val cipherBytes = combined.copyOfRange(1 + ivLength, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateAesKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        }.getOrNull()
    }
}
