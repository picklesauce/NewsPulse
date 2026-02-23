package com.example.newspulse.data

import android.content.Context
import android.content.SharedPreferences
import com.example.newspulse.domain.InterestsRepository

class OnboardingPreferences(context: Context) : InterestsRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isOnboardingComplete(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    override fun setOnboardingComplete() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    override fun getFollowedInterestIds(): Set<String> {
        val saved = prefs.getString(KEY_FOLLOWED_IDS, null) ?: return emptySet()
        return if (saved.isEmpty()) emptySet() else saved.split(DELIMITER).toSet()
    }

    override fun setFollowedInterestIds(ids: Set<String>) {
        prefs.edit()
            .putString(KEY_FOLLOWED_IDS, ids.joinToString(DELIMITER))
            .apply()
    }

    override fun followInterest(id: String) {
        val current = getFollowedInterestIds().toMutableSet()
        current.add(id)
        setFollowedInterestIds(current)
    }

    override fun unfollowInterest(id: String) {
        val current = getFollowedInterestIds().toMutableSet()
        current.remove(id)
        setFollowedInterestIds(current)
    }

    companion object {
        private const val PREFS_NAME = "onboarding"
        private const val KEY_ONBOARDING_COMPLETE = "has_completed_onboarding"
        private const val KEY_FOLLOWED_IDS = "followed_interest_ids"
        private const val DELIMITER = ","
    }
}
