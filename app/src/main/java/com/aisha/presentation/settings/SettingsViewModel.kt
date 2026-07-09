package com.aisha.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.data.repository.MemoryRepository
import com.aisha.domain.model.User
import com.aisha.domain.usecase.GetCurrentUserUseCase
import com.aisha.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val voiceEnabled: Boolean = true,
    val appVersion: String = "1.0.0",
    val showLogoutDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            _state.value = _state.value.copy(
                user = user,
                isLoading = false
            )
        }
    }

    fun toggleDarkMode() {
        _state.value = _state.value.copy(
            darkModeEnabled = !_state.value.darkModeEnabled
        )
    }

    fun toggleNotifications() {
        _state.value = _state.value.copy(
            notificationsEnabled = !_state.value.notificationsEnabled
        )
    }

    fun toggleVoice() {
        _state.value = _state.value.copy(
            voiceEnabled = !_state.value.voiceEnabled
        )
    }

    fun showLogoutDialog() {
        _state.value = _state.value.copy(showLogoutDialog = true)
    }

    fun hideLogoutDialog() {
        _state.value = _state.value.copy(showLogoutDialog = false)
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase()
            onSignedOut()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            // Clear all local data
            memoryRepository.saveMood(com.aisha.domain.model.Mood())
            memoryRepository.saveBond(0f)
            // Note: In production, you'd clear SharedPreferences here
        }
    }
}
