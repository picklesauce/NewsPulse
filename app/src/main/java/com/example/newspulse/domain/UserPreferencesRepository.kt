package com.example.newspulse.domain

interface UserPreferencesRepository {
    fun getUsername(): String
    fun setUsername(username: String)
    fun getMemberSince(): String
    fun setMemberSinceIfFirstTime()

    /** Stored credentials for login verification (set at sign up). */
    fun getStoredEmail(): String
    fun getStoredPassword(): String
    fun setStoredCredentials(email: String, password: String)
}
