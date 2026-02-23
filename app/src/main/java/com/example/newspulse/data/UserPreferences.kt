package com.example.newspulse.data

import android.content.Context
import android.content.SharedPreferences
import com.example.newspulse.domain.IUserPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserPreferences(context: Context) : IUserPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    override fun setUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    override fun getMemberSince(): String {
        val ts = prefs.getLong(KEY_MEMBER_SINCE, 0L)
        return if (ts == 0L) "Feb 2026" else SimpleDateFormat("MMM yyyy", Locale.US).format(Date(ts))
    }

    override fun setMemberSinceIfFirstTime() {
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
