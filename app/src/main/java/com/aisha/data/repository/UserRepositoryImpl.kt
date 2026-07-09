package com.aisha.data.repository

import com.aisha.data.remote.UserRemoteDataSource
import com.aisha.domain.model.Result
import com.aisha.domain.model.User
import com.aisha.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource
) : UserRepository {

    override fun getUserProfile(userId: String): Flow<Result<User>> = flow {
        try {
            var hasData = false
            userRemoteDataSource.getUserProfile(userId).collect { user ->
                hasData = true
                emit(Result.Success(user))
            }
            if (!hasData) {
                emit(Result.Error(Exception("User profile not found")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            userRemoteDataSource.updateUserProfile(user)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun uploadProfilePhoto(userId: String, photoUri: String): Result<String> {
        return try {
            val photoUrl = userRemoteDataSource.uploadProfilePhoto(userId, photoUri)
            Result.Success(photoUrl)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        return try {
            userRemoteDataSource.saveUser(user)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
