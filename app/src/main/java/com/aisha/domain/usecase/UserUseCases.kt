package com.aisha.domain.usecase

import com.aisha.domain.model.Result
import com.aisha.domain.model.User
import com.aisha.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Result<User>> {
        return userRepository.getUserProfile(userId)
    }
}

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return userRepository.updateUserProfile(user)
    }
}

class UploadProfilePhotoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, photoUri: String): Result<String> {
        return userRepository.uploadProfilePhoto(userId, photoUri)
    }
}
