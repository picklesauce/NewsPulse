package com.example.newspulse.domain

interface UserPreferencesRepository {
    fun getUsername(): String
    fun setUsername(username: String)
    fun getMemberSince(): String
    fun setMemberSinceIfFirstTime()

    // Stored credentials for login verification 
    fun getStoredEmail(): String
    fun getStoredPassword(): String
    fun setStoredCredentials(email: String, password: String)

    // Reloads profile from remote after auth identity changes
    suspend fun refreshProfileFromRemote() {}

    // Clears cached display profile so the next remote load matches the loggedin account
    fun clearCachedProfileForAccountSwitch() {}
}
