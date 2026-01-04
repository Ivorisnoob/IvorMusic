package com.ivor.ivormusic.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages session tokens for Spotify authentication.
 */
class SpotifySessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "spotify_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveIsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ENABLED, enabled).apply()
    }

    fun isEnabled(): Boolean {
        return prefs.getBoolean(KEY_IS_ENABLED, false)
    }

    fun clearSession() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_IS_ENABLED = "is_enabled"
        // TODO: Replace with your actual Client ID
        const val CLIENT_ID = "YOUR_SPOTIFY_CLIENT_ID" 
        const val REDIRECT_URI = "ivormusic://spotify-callback"
    }
}
