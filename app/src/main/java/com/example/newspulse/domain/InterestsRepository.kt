package com.example.newspulse.domain

interface InterestsRepository {
    fun getFollowedInterestIds(): Set<String>
    fun setFollowedInterestIds(ids: Set<String>)
    fun followInterest(id: String)
    fun unfollowInterest(id: String)
    fun isOnboardingComplete(): Boolean
    fun setOnboardingComplete()
}
