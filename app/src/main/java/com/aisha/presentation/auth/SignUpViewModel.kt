package com.aisha.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.domain.model.Result
import com.aisha.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    fun onDisplayNameChange(displayName: String) {
        _state.value = _state.value.copy(displayName = displayName, error = null)
    }

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun signUp() {
        val currentState = _state.value

        if (currentState.displayName.isBlank() || 
            currentState.email.isBlank() || 
            currentState.password.isBlank() ||
            currentState.confirmPassword.isBlank()) {
            _state.value = currentState.copy(error = "Please fill in all fields")
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _state.value = currentState.copy(error = "Passwords do not match")
            return
        }

        if (currentState.password.length < 6) {
            _state.value = currentState.copy(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)
            
            when (val result = signUpUseCase(currentState.email, currentState.password, currentState.displayName)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Sign up failed"
                    )
                }
                is Result.Loading -> {
                    // Already handled
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
