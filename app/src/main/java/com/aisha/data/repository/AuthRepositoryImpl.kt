package com.aisha.data.repository

import com.aisha.data.remote.AuthRemoteDataSource
import com.aisha.domain.model.Result
import com.aisha.domain.model.User
import com.aisha.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override val currentUser: Flow<User?> = authRemoteDataSource.currentUser.map { firebaseUser ->
        firebaseUser?.let { authRemoteDataSource.toUser(it) }
    }

    override val isAuthenticated: Flow<Boolean> = authRemoteDataSource.currentUser.map { it != null }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authRemoteDataSource.signIn(email, password)
            Result.Success(authRemoteDataSource.toUser(firebaseUser))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authRemoteDataSource.signUp(email, password)
            Result.Success(authRemoteDataSource.toUser(firebaseUser))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            authRemoteDataSource.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            authRemoteDataSource.sendPasswordResetEmail(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getCurrentUser(): User? {
        return authRemoteDataSource.getCurrentUser()?.let {
            authRemoteDataSource.toUser(it)
        }
    }
}
