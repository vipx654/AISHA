package com.aisha.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class OnboardingState(
    val userName: String = "",
    val cloudBackupEnabled: Boolean = true,
    val isOnboardingComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("aisha_onboarding", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        _state.value = OnboardingState(
            userName = prefs.getString("user_name", "") ?: "",
            cloudBackupEnabled = prefs.getBoolean("cloud_backup", true),
            isOnboardingComplete = prefs.getBoolean("onboarding_complete", false)
        )
    }

    fun setUserName(name: String) {
        _state.value = _state.value.copy(userName = name)
        prefs.edit().putString("user_name", name).apply()
    }

    fun setCloudBackup(enabled: Boolean) {
        _state.value = _state.value.copy(cloudBackupEnabled = enabled)
        prefs.edit().putBoolean("cloud_backup", enabled).apply()
    }

    fun completeOnboarding() {
        _state.value = _state.value.copy(isOnboardingComplete = true)
        prefs.edit().putBoolean("onboarding_complete", true).apply()
    }

    fun isOnboardingComplete(): Boolean {
        return prefs.getBoolean("onboarding_complete", false)
    }
}
