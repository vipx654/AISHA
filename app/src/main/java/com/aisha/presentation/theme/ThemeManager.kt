package com.aisha.presentation.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("aisha_settings", Context.MODE_PRIVATE)
    
    var isDarkMode by mutableStateOf(
        prefs.getBoolean("dark_mode", false)
    )
        private set
    
    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
    }
    
    var isNotificationsEnabled by mutableStateOf(
        prefs.getBoolean("notifications", true)
    )
        private set
    
    fun toggleNotifications() {
        isNotificationsEnabled = !isNotificationsEnabled
        prefs.edit().putBoolean("notifications", isNotificationsEnabled).apply()
    }
    
    var isVoiceEnabled by mutableStateOf(
        prefs.getBoolean("voice", true)
    )
        private set
    
    fun toggleVoice() {
        isVoiceEnabled = !isVoiceEnabled
        prefs.edit().putBoolean("voice", isVoiceEnabled).apply()
    }
}
