package com.example.newspulse.data.mock

import com.example.newspulse.domain.InterestsRepository

class FakeInterestsRepository : InterestsRepository {
    private var complete = false
    private val followedIds = mutableSetOf("interest-technology", "interest-business")

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
