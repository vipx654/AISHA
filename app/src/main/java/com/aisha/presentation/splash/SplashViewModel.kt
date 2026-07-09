package com.aisha.presentation.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.domain.usecase.IsAuthenticatedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val isAuthenticatedUseCase: IsAuthenticatedUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated.asStateFlow()

    private val _showOnboarding = MutableStateFlow<Boolean?>(null)
    val showOnboarding: StateFlow<Boolean?> = _showOnboarding.asStateFlow()

    private val onboardingPrefs = context.getSharedPreferences("aisha_onboarding", Context.MODE_PRIVATE)

    init {
        checkStates()
    }

    private fun checkStates() {
        viewModelScope.launch {
            // Check if onboarding is complete
            val onboardingComplete = onboardingPrefs.getBoolean("onboarding_complete", false)
            
            if (!onboardingComplete) {
                _showOnboarding.value = true
            } else {
                // Only check auth if onboarding is done
                val isAuth = isAuthenticatedUseCase().first()
                _isAuthenticated.value = isAuth
            }
        }
    }
}
