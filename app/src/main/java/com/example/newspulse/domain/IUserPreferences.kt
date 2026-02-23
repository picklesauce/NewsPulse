package com.example.newspulse.domain

interface IUserPreferences {
    fun getUsername(): String
    fun setUsername(username: String)
    fun getMemberSince(): String
    fun setMemberSinceIfFirstTime()
}
