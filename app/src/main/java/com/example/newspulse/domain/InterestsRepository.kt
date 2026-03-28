package com.example.newspulse.domain

interface InterestsRepository {
    fun getFollowedInterestIds(): Set<String>
    fun setFollowedInterestIds(ids: Set<String>)
    fun followInterest(id: String)
    fun unfollowInterest(id: String)

    /** Await persistence (e.g. Supabase insert) so a row exists before dependent writes. */
    suspend fun followInterestSuspend(id: String) {
        followInterest(id)
    }

    suspend fun unfollowInterestSuspend(id: String) {
        unfollowInterest(id)
    }

    fun isOnboardingComplete(): Boolean
    fun setOnboardingComplete()
    fun onUserChanged() {}
}
