package com.example.newspulse.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class AuthResult(
    val success: Boolean,
    val errorMessage: String? = null
)

interface AuthRepository {
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signIn(email: String, password: String): AuthResult

    // google oauth
    suspend fun signInWithGoogle(): AuthResult =
        AuthResult(false, "Google sign-in is not configured")

    // supabase oauth
    fun observeSupabaseAuthUserId(): Flow<String?> = emptyFlow()

    suspend fun syncSupabaseAuthSessionToApp(): Boolean = false

    fun signOut()
    fun getCurrentUserId(): String?
}
