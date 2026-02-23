package com.example.newspulse.data.mock

import com.example.newspulse.domain.InterestsRepository

/**
 * Mock implementation of [InterestsRepository] for demo/testing.
 * Persists follow/unfollow state in-memory for the app session.
 */
class MockInterestsRepository : InterestsRepository {
    private val _followedIds = mutableSetOf<String>()
    private var _onboardingComplete = false

    override fun getFollowedInterestIds(): Set<String> = _followedIds.toSet()
    override fun setFollowedInterestIds(ids: Set<String>) {
        _followedIds.clear()
        _followedIds.addAll(ids)
    }
    override fun followInterest(id: String) {
        _followedIds.add(id)
    }
    override fun unfollowInterest(id: String) {
        _followedIds.remove(id)
    }
    override fun isOnboardingComplete(): Boolean = _onboardingComplete
    override fun setOnboardingComplete() {
        _onboardingComplete = true
    }
}
