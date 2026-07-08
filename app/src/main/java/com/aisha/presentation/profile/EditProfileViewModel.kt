package com.aisha.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.domain.model.Result
import com.aisha.domain.model.User
import com.aisha.domain.usecase.GetCurrentUserUseCase
import com.aisha.domain.usecase.UpdateUserProfileUseCase
import com.aisha.domain.usecase.UploadProfilePhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val displayName: String = "",
    val photoUrl: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadProfilePhotoUseCase: UploadProfilePhotoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            user?.let {
                _state.value = _state.value.copy(
                    displayName = it.displayName,
                    photoUrl = it.photoUrl,
                    isLoading = false
                )
            } ?: run {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun onDisplayNameChange(displayName: String) {
        _state.value = _state.value.copy(displayName = displayName, error = null)
    }

    fun onPhotoSelected(userId: String, photoUri: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            
            when (val result = uploadProfilePhotoUseCase(userId, photoUri)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        photoUrl = result.data,
                        isSaving = false
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        error = result.exception.message,
                        isSaving = false
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun saveProfile() {
        val currentState = _state.value
        if (currentState.displayName.isBlank()) {
            _state.value = currentState.copy(error = "Display name cannot be empty")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isSaving = true)
            
            val user = getCurrentUserUseCase().first()
            user?.let {
                val updatedUser = it.copy(
                    displayName = currentState.displayName,
                    photoUrl = currentState.photoUrl
                )
                
                when (val result = updateUserProfileUseCase(updatedUser)) {
                    is Result.Success -> {
                        _state.value = _state.value.copy(isSaving = false, isSuccess = true)
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            error = result.exception.message,
                            isSaving = false
                        )
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
