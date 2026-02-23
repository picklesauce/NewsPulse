package com.example.newspulse.ui.preview

import com.example.newspulse.domain.IOnboardingPreferences
import com.example.newspulse.domain.IReadingHistoryRepository
import com.example.newspulse.domain.IUserPreferences
import com.example.newspulse.domain.model.ReadingHistoryItem

object FakeOnboardingPreferences : IOnboardingPreferences {
    private var complete = false
    private var topics = setOf<String>("Technology", "Business")

    override fun isOnboardingComplete(): Boolean = complete
    override fun setOnboardingComplete() { complete = true }
    override fun getSelectedTopics(): Set<String> = topics
    override fun setSelectedTopics(topics: Set<String>) { this.topics = topics }
}

object FakeUserPreferences : IUserPreferences {
    private var username = "preview_user"
    override fun getUsername(): String = username
    override fun setUsername(username: String) { this.username = username }
    override fun getMemberSince(): String = "Feb 2026"
    override fun setMemberSinceIfFirstTime() {}
}

object FakeReadingHistoryRepository : IReadingHistoryRepository {
    override fun getReadingHistory(): List<ReadingHistoryItem> = emptyList()
    override fun addToHistory(title: String) {}
}
