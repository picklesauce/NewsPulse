package com.example.newspulse.domain

interface InterestsRepository {
    fun getSelectedInterests(): Set<String>
    fun setSelectedInterests(interests: Set<String>)
    fun isOnboardingComplete(): Boolean
    fun setOnboardingComplete()
}
