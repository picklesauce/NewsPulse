package com.example.newspulse.preferences

import android.content.Context
import android.content.SharedPreferences

class OnboardingPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboardingComplete(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setOnboardingComplete() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    fun getSelectedTopics(): Set<String> {
        val saved = prefs.getString(KEY_SELECTED_TOPICS, null) ?: return emptySet()
        return if (saved.isEmpty()) emptySet() else saved.split(DELIMITER).toSet()
    }

    fun setSelectedTopics(topics: Set<String>) {
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
