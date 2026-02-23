package com.example.newspulse.data

import android.content.Context
import android.content.SharedPreferences
import com.example.newspulse.domain.IOnboardingPreferences

class OnboardingPreferences(context: Context) : IOnboardingPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isOnboardingComplete(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    override fun setOnboardingComplete() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    override fun getSelectedTopics(): Set<String> {
        val saved = prefs.getString(KEY_SELECTED_TOPICS, null) ?: return emptySet()
        return if (saved.isEmpty()) emptySet() else saved.split(DELIMITER).toSet()
    }

    override fun setSelectedTopics(topics: Set<String>) {
        prefs.edit()
            .putString(KEY_SELECTED_TOPICS, topics.joinToString(DELIMITER))
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "onboarding"
        private const val KEY_ONBOARDING_COMPLETE = "has_completed_onboarding"
        private const val KEY_SELECTED_TOPICS = "selected_topics"
        private const val DELIMITER = ","
    }
}
