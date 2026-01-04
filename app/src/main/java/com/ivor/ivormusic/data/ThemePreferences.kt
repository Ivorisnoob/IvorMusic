package com.ivor.ivormusic.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

/**
 * Manages app preferences (theme, local songs toggle, YouTube history, etc.).
 */
class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val _appTheme = MutableStateFlow(getAppThemePreference())
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    private val _saveHistoryToYouTube = MutableStateFlow(getSaveHistoryPreference())
    val saveHistoryToYouTube: StateFlow<Boolean> = _saveHistoryToYouTube.asStateFlow()

    private val _loadLocalSongs = MutableStateFlow(getLoadLocalSongsPreference())
    val loadLocalSongs: StateFlow<Boolean> = _loadLocalSongs.asStateFlow()

    companion object {
        private const val PREFS_NAME = "ivor_music_theme_prefs"
        private const val KEY_THEME = "app_theme"
        private const val KEY_DARK_MODE = "dark_mode" // Deprecated, migrate to KEY_THEME if needed, but we'll just ignore for now or migrate
        private const val KEY_LOAD_LOCAL_SONGS = "load_local_songs"
        private const val KEY_SAVE_HISTORY = "save_history_yt"
    }

    /**
     * Get the stored app theme preference. Defaults to SYSTEM.
     */
    private fun getAppThemePreference(): AppTheme {
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        return try {
            AppTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            AppTheme.SYSTEM
        }
    }

    /**
     * Get the stored save history preference. Defaults to true.
     */
    private fun getSaveHistoryPreference(): Boolean {
        return prefs.getBoolean(KEY_SAVE_HISTORY, true)
    }

    /**
     * Get the stored load local songs preference. Defaults to true.
     */
    private fun getLoadLocalSongsPreference(): Boolean {
        return prefs.getBoolean(KEY_LOAD_LOCAL_SONGS, true)
    }

    /**
     * Save app theme preference and update the flow.
     */
    fun setAppTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _appTheme.value = theme
    }

    /**
     * Save history preference and update the flow.
     */
    fun setSaveHistoryToYouTube(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SAVE_HISTORY, enabled).apply()
        _saveHistoryToYouTube.value = enabled
    }

    /**
     * Toggle save history setting.
     */
    fun toggleSaveHistory() {
        setSaveHistoryToYouTube(!_saveHistoryToYouTube.value)
    }

    /**
     * Save load local songs preference and update the flow.
     */
    fun setLoadLocalSongs(load: Boolean) {
        prefs.edit().putBoolean(KEY_LOAD_LOCAL_SONGS, load).apply()
        _loadLocalSongs.value = load
    }

    /**
     * Toggle load local songs setting.
     */
    fun toggleLoadLocalSongs() {
        setLoadLocalSongs(!_loadLocalSongs.value)
    }
}
