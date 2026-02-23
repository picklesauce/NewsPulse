package com.example.newspulse.domain

interface IOnboardingPreferences {
    fun isOnboardingComplete(): Boolean
    fun setOnboardingComplete()
    fun getSelectedTopics(): Set<String>
    fun setSelectedTopics(topics: Set<String>)
}
