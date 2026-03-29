package com.example.newspulse.domain

interface InterestsRepository {
    fun getFollowedInterestIds(): Set<String>
    fun setFollowedInterestIds(ids: Set<String>)
    fun followInterest(id: String)
    fun unfollowInterest(id: String)

    fun isOnboardingComplete(): Boolean
    fun setOnboardingComplete()
    fun onUserChanged() {}

    /** True when writes go to Supabase and require [AuthRepository.getCurrentUserId]. */
    fun needsAuthenticatedUserForWrite(): Boolean = false

    /** Await remote persistence (Supabase); default uses sync methods. */
    suspend fun followInterestSuspend(id: String): Boolean {
        followInterest(id)
        return true
    }

    suspend fun unfollowInterestSuspend(id: String): Boolean {
        unfollowInterest(id)
        return true
    }

    suspend fun setFollowedInterestIdsSuspend(ids: Set<String>): Boolean {
        setFollowedInterestIds(ids)
        return true
    }

    suspend fun setOnboardingCompleteSuspend(): Boolean {
        setOnboardingComplete()
        return true
    }
}
