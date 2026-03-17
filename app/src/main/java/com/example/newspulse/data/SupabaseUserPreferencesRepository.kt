package com.example.newspulse.data

import android.content.Context
import android.content.SharedPreferences
import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.data.remote.SupabaseUserSession
import com.example.newspulse.domain.UserPreferencesRepository
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SupabaseUserPreferencesRepository(
    context: Context,
    private val client: SupabaseRestClient,
    private val userIdProvider: () -> String?
) : UserPreferencesRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        ensureUserProfileExists()
    }

    override fun getUsername(): String {
        val remote = getRemoteProfileField("username")
        if (remote.isNotBlank()) {
            prefs.edit().putString(KEY_USERNAME, remote).apply()
            return remote
        }
        return prefs.getString(KEY_USERNAME, "") ?: ""
    }

    override fun setUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
        upsertProfileFields(JSONObject().put("username", username))
    }

    override fun getMemberSince(): String {
        val remote = getRemoteProfileField("member_since")
        if (remote.isNotBlank()) {
            prefs.edit().putString(KEY_MEMBER_SINCE_STR, remote).apply()
            return remote
        }
        val local = prefs.getString(KEY_MEMBER_SINCE_STR, null)
        if (!local.isNullOrBlank()) return local
        return "Feb 2026"
    }

    override fun setMemberSinceIfFirstTime() {
        val existing = getMemberSince()
        if (existing != "Feb 2026" || prefs.contains(KEY_MEMBER_SINCE_STR)) return
        val now = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
        prefs.edit().putString(KEY_MEMBER_SINCE_STR, now).apply()
        upsertProfileFields(JSONObject().put("member_since", now))
    }

    override fun getStoredEmail(): String = prefs.getString(KEY_STORED_EMAIL, "") ?: ""

    override fun getStoredPassword(): String = prefs.getString(KEY_STORED_PASSWORD, "") ?: ""

    override fun setStoredCredentials(email: String, password: String) {
        prefs.edit()
            .putString(KEY_STORED_EMAIL, email)
            .putString(KEY_STORED_PASSWORD, password)
            .apply()
    }

    private fun ensureUserProfileExists() {
        val userId = userIdProvider() ?: return
        val rows = client.select(
            table = "user_profiles",
            columns = "user_id",
            filters = mapOf("user_id" to "eq.$userId"),
            limit = 1
        )
        if (rows.length() > 0) return
        val username = prefs.getString(KEY_USERNAME, "").orEmpty().ifBlank { "user_${userId.take(8)}" }
        val memberSince = prefs.getString(KEY_MEMBER_SINCE_STR, null)
            ?: SupabaseUserSession.currentMemberSince().also {
                prefs.edit().putString(KEY_MEMBER_SINCE_STR, it).apply()
            }
        val body = JSONObject()
            .put("user_id", userId)
            .put("username", username)
            .put("member_since", memberSince)
            .put("onboarding_complete", false)
        client.insert(
            table = "user_profiles",
            body = body,
            onConflict = "user_id",
            upsert = true
        )
    }

    private fun getRemoteProfileField(field: String): String {
        val userId = userIdProvider() ?: return ""
        val rows = client.select(
            table = "user_profiles",
            columns = field,
            filters = mapOf("user_id" to "eq.$userId"),
            limit = 1
        )
        if (rows.length() == 0) return ""
        return rows.optJSONObject(0)?.optString(field).orEmpty()
    }

    private fun upsertProfileFields(fields: JSONObject) {
        val userId = userIdProvider() ?: return
        val body = JSONObject().put("user_id", userId)
        fields.keys().forEach { key -> body.put(key, fields.get(key)) }
        client.insert(
            table = "user_profiles",
            body = body,
            onConflict = "user_id",
            upsert = true
        )
    }

    companion object {
        private const val PREFS_NAME = "user"
        private const val KEY_USERNAME = "username"
        private const val KEY_MEMBER_SINCE_STR = "member_since_str"
        private const val KEY_STORED_EMAIL = "stored_email"
        private const val KEY_STORED_PASSWORD = "stored_password"
    }
}
