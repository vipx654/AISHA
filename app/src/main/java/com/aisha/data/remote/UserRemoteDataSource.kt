package com.aisha.data.remote

import com.aisha.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    companion object {
        private const val USERS_COLLECTION = "users"
    }

    fun getUserProfile(userId: String): Flow<User> = callbackFlow {
        val docRef = firestore.collection(USERS_COLLECTION).document(userId)
        
        // First try to get the document once
        docRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    user?.let { trySend(it) }
                } else {
                    // Document doesn't exist, emit a default user based on auth data
                    // The actual user data should come from FirebaseAuth
                    close()
                }
            }
            .addOnFailureListener { exception ->
                close(exception)
            }
        
        awaitClose { }
    }

    suspend fun updateUserProfile(user: User) {
        val userMap = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "photoUrl" to user.photoUrl,
            "createdAt" to user.createdAt
        )
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(userMap)
            .await()
    }

    suspend fun saveUser(user: User) {
        val userMap = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "photoUrl" to user.photoUrl,
            "createdAt" to user.createdAt
        )
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(userMap)
            .await()
    }

    suspend fun uploadProfilePhoto(userId: String, photoUri: String): String {
        val ref = storage.reference.child("profile_photos/$userId.jpg")
        ref.putBytes(android.util.Base64.decode(
            photoUri.substringAfter(","),
            android.util.Base64.DEFAULT
        )).await()
        return ref.downloadUrl.await().toString()
    }
}
