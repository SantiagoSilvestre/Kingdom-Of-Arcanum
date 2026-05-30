package br.com.silvestresantiago732.kingdomofarcanum.domain.repository

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun login(email: String, pass: String): Result<FirebaseUser?>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?>
    suspend fun signUp(email: String, pass: String): Result<FirebaseUser?>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun logout()
}
