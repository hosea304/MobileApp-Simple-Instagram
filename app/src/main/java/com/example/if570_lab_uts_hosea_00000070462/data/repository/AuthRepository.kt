package com.example.if570_lab_uts_hosea_00000070462.data.repository

import com.example.if570_lab_uts_hosea_00000070462.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Create user document if it doesn't exist
                createUserDocumentIfNotExists(user.uid, user.email ?: "", user.displayName)
                Result.success(User(user.uid, user.email ?: "", user.displayName))
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Create user document
                createUserDocument(user.uid, email, null)
                Result.success(User(user.uid, user.email ?: "", user.displayName))
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun googleSignIn(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                // Create user document if it doesn't exist
                createUserDocumentIfNotExists(user.uid, user.email ?: "", user.displayName)
                Result.success(User(user.uid, user.email ?: "", user.displayName))
            } else {
                Result.failure(Exception("Google Sign-In failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createUserDocument(userId: String, email: String, displayName: String?) {
        val userData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "likedStories" to emptyList<String>(),
            "pinnedStories" to emptyList<String>()
        )
        firestore.collection("users").document(userId).set(userData).await()
    }

    private suspend fun createUserDocumentIfNotExists(userId: String, email: String, displayName: String?) {
        val userDocRef = firestore.collection("users").document(userId)
        val snapshot = userDocRef.get().await()
        if (!snapshot.exists()) {
            createUserDocument(userId, email, displayName)
        }
    }
}
