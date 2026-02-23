package com.example.newspulse.data.mock

import com.example.newspulse.domain.InterestsRepository

class FakeInterestsRepository : InterestsRepository {
    private var complete = false
    private var topics = setOf<String>("Technology", "Business")

    override fun getSelectedInterests(): Set<String> = topics
    override fun setSelectedInterests(interests: Set<String>) {
        topics = interests
    }
    override fun isOnboardingComplete(): Boolean = complete
    override fun setOnboardingComplete() {
        complete = true
    }
}
