package com.ivor.ivormusic.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ivor.ivormusic.data.ThemePreferences
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing theme and app settings state across the app.
 */
class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themePreferences = ThemePreferences(application)

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
    val loadLocalSongs: StateFlow<Boolean> = themePreferences.loadLocalSongs

    fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    fun setLoadLocalSongs(load: Boolean) {
        themePreferences.setLoadLocalSongs(load)
    }

    fun toggleLoadLocalSongs() {
        themePreferences.toggleLoadLocalSongs()
    }
}
