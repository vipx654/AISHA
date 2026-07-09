package com.aisha.domain.usecase

import com.aisha.domain.model.Result
import com.aisha.domain.model.User
import com.aisha.domain.repository.AuthRepository
import com.aisha.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.signIn(email, password)
    }
}

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String, displayName: String): Result<User> {
        // Create Firebase Auth account first
        val authResult = authRepository.signUp(email, password)
        
        if (authResult is Result.Success) {
            val user = authResult.data.copy(displayName = displayName)
            
            // Save user to Firestore in background - don't await
            // This ensures sign up succeeds even if Firestore fails
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    userRepository.saveUser(user)
                } catch (e: Exception) {
                    // Ignore - auth account was created
                }
            }
        }
        
        return authResult
    }
}

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> {
        return authRepository.currentUser
    }
}

class IsAuthenticatedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isAuthenticated
    }
}

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.resetPassword(email)
    }
}
