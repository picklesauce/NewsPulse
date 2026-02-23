package com.example.newspulse.data.mock

import com.example.newspulse.domain.UserPreferencesRepository

class FakeUserPreferencesRepository : UserPreferencesRepository {
    private var username = "preview_user"

    override fun getUsername(): String = username
    override fun setUsername(username: String) {
        this.username = username
    }
    override fun getMemberSince(): String = "Feb 2026"
    override fun setMemberSinceIfFirstTime() {}
}
