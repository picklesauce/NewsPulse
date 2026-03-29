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

    /** Reloads profile from remote after auth identity changes (e.g. Google sign-in). No-op for local-only prefs. */
    suspend fun refreshProfileFromRemote() {}

    /** Clears cached display profile so the next remote load matches the logged-in account. */
    fun clearCachedProfileForAccountSwitch() {}
}
