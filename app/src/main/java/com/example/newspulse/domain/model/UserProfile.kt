package com.example.newspulse.domain.model

data class UserProfile(
    val username: String,
    val memberSince: String,
    val selectedInterests: List<Interest> = emptyList()
)
