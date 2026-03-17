package com.example.newspulse.data.mock

import com.example.newspulse.domain.UserPreferencesRepository

class FakeUserPreferencesRepository : UserPreferencesRepository {
    private var username = "preview_user"
    private var storedEmail = "preview@example.com"
    private var storedPassword = "preview"

    override fun getUsername(): String = username
    override fun setUsername(username: String) {
        this.username = username
    }
    override fun getMemberSince(): String = "Feb 2026"
    override fun setMemberSinceIfFirstTime() {}
    override fun getStoredEmail(): String = storedEmail
    override fun getStoredPassword(): String = storedPassword
    override fun setStoredCredentials(email: String, password: String) {
        storedEmail = email
        storedPassword = password
    }
}
