package com.example.newspulse.domain

data class AuthResult(
    val success: Boolean,
    val errorMessage: String? = null
)

interface AuthRepository {
    fun signUp(email: String, password: String): AuthResult
    fun signIn(email: String, password: String): AuthResult
    fun signOut()
    fun getCurrentUserId(): String?
}
