package com.example.newspulse.preferences

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun setUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getMemberSince(): String {
        val ts = prefs.getLong(KEY_MEMBER_SINCE, 0L)
        return if (ts == 0L) "Feb 2026" else SimpleDateFormat("MMM yyyy", Locale.US).format(Date(ts))
    }

    fun setMemberSinceIfFirstTime() {
        if (prefs.getLong(KEY_MEMBER_SINCE, 0L) == 0L) {
            prefs.edit().putLong(KEY_MEMBER_SINCE, System.currentTimeMillis()).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "user"
        private const val KEY_USERNAME = "username"
        private const val KEY_MEMBER_SINCE = "member_since"
    }
}
