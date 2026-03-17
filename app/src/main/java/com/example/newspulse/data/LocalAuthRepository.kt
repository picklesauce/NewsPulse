package com.example.newspulse.data

import com.example.newspulse.domain.AuthRepository
import com.example.newspulse.domain.AuthResult
import com.example.newspulse.domain.UserPreferencesRepository

class LocalAuthRepository(
    private val userPreferencesRepository: UserPreferencesRepository
) : AuthRepository {
    override fun signUp(email: String, password: String): AuthResult {
        userPreferencesRepository.setStoredCredentials(email, password)
        return AuthResult(success = true)
    }

    override fun signIn(email: String, password: String): AuthResult {
        val ok = userPreferencesRepository.getStoredEmail() == email &&
            userPreferencesRepository.getStoredPassword() == password
        return if (ok) AuthResult(success = true) else AuthResult(false, "Invalid email or password")
    }

    override fun signOut() = Unit

    override fun getCurrentUserId(): String? = null
}
