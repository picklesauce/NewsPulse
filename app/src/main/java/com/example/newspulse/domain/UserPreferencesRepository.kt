package com.example.newspulse.domain

interface UserPreferencesRepository {
    fun getUsername(): String
    fun setUsername(username: String)
    fun getMemberSince(): String
    fun setMemberSinceIfFirstTime()
}
