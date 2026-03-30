package com.example.newspulse.data.mock

import com.example.newspulse.domain.InterestsRepository

/**
 * Fake implementation for previews. Pre-seeded with interests from MockDB.
 */
class FakeInterestsRepository : InterestsRepository {
    private var complete = false
    private val followedIds = MockDB.interests
        .filter { it.name in listOf("Technology", "Business") }
        .map { it.id }
        .toMutableSet()

    override fun getFollowedInterestIds(): Set<String> = followedIds.toSet()
    override fun setFollowedInterestIds(ids: Set<String>) {
        followedIds.clear()
        followedIds.addAll(ids)
    }
    override fun followInterest(id: String) {
        followedIds.add(id)
    }
    override fun unfollowInterest(id: String) {
        followedIds.remove(id)
    }
    override fun isOnboardingComplete(): Boolean = complete
    override fun setOnboardingComplete() {
        complete = true
    }
}
