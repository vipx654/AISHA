package com.aisha.domain.repository

import com.aisha.domain.model.Result
import com.aisha.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(userId: String): Flow<Result<User>>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun uploadProfilePhoto(userId: String, photoUri: String): Result<String>
    suspend fun saveUser(user: User): Result<Unit>
}
