package com.example.newspulse.data

import com.example.newspulse.domain.AuthRepository
import com.example.newspulse.domain.AuthResult
import com.example.newspulse.domain.UserPreferencesRepository

class LocalAuthRepository(
    private val userPreferencesRepository: UserPreferencesRepository
) : AuthRepository {
    override suspend fun signUp(email: String, password: String): AuthResult {
        userPreferencesRepository.setStoredCredentials(email, password)
        return AuthResult(success = true)
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        val ident = email.trim()
        if (userPreferencesRepository.getStoredPassword() != password) {
            return AuthResult(false, "Invalid email or password")
        }
        val emailOk = userPreferencesRepository.getStoredEmail().equals(ident, ignoreCase = true)
        val usernameOk =
            userPreferencesRepository.getUsername().isNotBlank() &&
                userPreferencesRepository.getUsername().equals(ident, ignoreCase = true)
        val ok = emailOk || usernameOk
        return if (ok) AuthResult(success = true) else AuthResult(false, "Invalid email or password")
    }

    override fun signOut() = Unit

    override fun getCurrentUserId(): String? = null
}
