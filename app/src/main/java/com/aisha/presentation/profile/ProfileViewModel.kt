package com.aisha.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.domain.model.Result
import com.aisha.domain.model.User
import com.aisha.domain.usecase.GetCurrentUserUseCase
import com.aisha.domain.usecase.GetUserProfileUseCase
import com.aisha.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { currentUser ->
                currentUser?.let { user ->
                    getUserProfileUseCase(user.uid).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _state.value = _state.value.copy(user = result.data, isLoading = false)
                            }
                            is Result.Error -> {
                                // Fall back to auth user data if Firestore profile doesn't exist
                                _state.value = _state.value.copy(user = user, isLoading = false)
                            }
                            is Result.Loading -> {
                                _state.value = _state.value.copy(isLoading = true)
                            }
                        }
                    }
                } ?: run {
                    _state.value = _state.value.copy(user = null, isLoading = false)
                }
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase()
            onSignedOut()
        }
    }
}
