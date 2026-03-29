package com.example.newspulse.data.remote

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SupabaseUserSession(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * In-memory copies so REST calls see the JWT immediately after OAuth.
     * [SharedPreferences.apply] persists asynchronously; without this, the next
     * [accessToken] read can still be null and PostgREST falls back to the anon key,
     * which fails RLS for user-scoped rows (empty profile/interests, username stuck empty).
     */
    @Volatile
    private var accessTokenCache: String? = null

    @Volatile
    private var userIdCache: String? = null

    var accessToken: String?
        get() {
            accessTokenCache?.let { return it }
            return prefs.getString(KEY_ACCESS_TOKEN, null).also { accessTokenCache = it }
        }
        set(value) {
            accessTokenCache = value
            prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()
        }

    var userId: String?
        get() {
            userIdCache?.let { return it }
            return prefs.getString(KEY_USER_ID, null).also { userIdCache = it }
        }
        set(value) {
            userIdCache = value
            prefs.edit().putString(KEY_USER_ID, value).apply()
        }

    fun clear() {
        accessTokenCache = null
        userIdCache = null
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "supabase_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"

        fun currentMemberSince(): String =
            SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
    }
}
