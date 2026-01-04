package com.ivor.ivormusic.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ivor.ivormusic.data.ThemePreferences
import com.ivor.ivormusic.data.AppTheme
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing theme and app settings state across the app.
 */
class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themePreferences = ThemePreferences(application)

    val appTheme: StateFlow<AppTheme> = themePreferences.appTheme
    val saveHistoryToYouTube: StateFlow<Boolean> = themePreferences.saveHistoryToYouTube
    val loadLocalSongs: StateFlow<Boolean> = themePreferences.loadLocalSongs

    fun setAppTheme(theme: AppTheme) {
        themePreferences.setAppTheme(theme)
    }

    fun setSaveHistoryToYouTube(enabled: Boolean) {
        themePreferences.setSaveHistoryToYouTube(enabled)
    }

    fun setLoadLocalSongs(load: Boolean) {
        themePreferences.setLoadLocalSongs(load)
    }

    fun toggleLoadLocalSongs() {
        themePreferences.toggleLoadLocalSongs()
    }
}
