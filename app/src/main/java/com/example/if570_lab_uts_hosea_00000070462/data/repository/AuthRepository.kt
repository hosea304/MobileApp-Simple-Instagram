package com.example.if570_lab_uts_hosea_00000070462.data.repository

import com.example.if570_lab_uts_hosea_00000070462.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
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
                Result.success(User(user.uid, user.email ?: "", user.displayName))
            } else {
                Result.failure(Exception("Google Sign-In failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}