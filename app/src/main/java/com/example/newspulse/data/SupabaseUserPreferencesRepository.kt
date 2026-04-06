package com.example.newspulse.data

import android.content.Context
import android.content.SharedPreferences
import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.data.remote.SupabaseSdkHolder
import com.example.newspulse.data.remote.SupabaseUserSession
import com.example.newspulse.domain.UserPreferencesRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var usernameMemory: String? = null

    override suspend fun refreshProfileFromRemote() {
        withContext(Dispatchers.IO) { bootstrapProfile() }
    }

    override fun clearCachedProfileForAccountSwitch() {
        usernameMemory = null
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_MEMBER_SINCE_STR)
            .commit()
    }

    suspend fun bootstrapProfile() {
        val userId = userIdProvider() ?: return
        val rows = client.select(
            table = "user_profiles",
            columns = "user_id,username,member_since",
            filters = mapOf("user_id" to "eq.$userId"),
            limit = 1
        )
        if (rows.length() > 0) {
            val o = rows.optJSONObject(0)
            var u = o?.optString("username").orEmpty()
            val m = o?.optString("member_since").orEmpty()
            if (u.isBlank()) {
                u = fallbackDisplayNameFromAuthEmail().orEmpty()
                if (u.isNotBlank()) {
                    upsertProfileFields(JSONObject().put("username", u))
                }
            }
            if (u.isBlank()) {
                u = "user_${userId.take(8)}"
                upsertProfileFields(JSONObject().put("username", u))
            }
            usernameMemory = u
            prefs.edit().apply {
                putString(KEY_USERNAME, u)
                if (m.isNotBlank()) putString(KEY_MEMBER_SINCE_STR, m)
            }.commit()
            return
        }
        val username = prefs.getString(KEY_USERNAME, "").orEmpty()
            .takeIf { it.isNotBlank() }
            ?: fallbackDisplayNameFromAuthEmail()
            ?: "user_${userId.take(8)}"
        val memberSince = prefs.getString(KEY_MEMBER_SINCE_STR, null)
            ?: SupabaseUserSession.currentMemberSince().also {
                prefs.edit().putString(KEY_MEMBER_SINCE_STR, it).apply()
            }
        val body = JSONObject()
            .put("user_id", userId)
            .put("username", username)
            .put("member_since", memberSince)
            .put("onboarding_complete", false)
        val ok = client.insert(
            table = "user_profiles",
            body = body,
            onConflict = "user_id",
            upsert = true
        )
        if (ok) {
            usernameMemory = username
            prefs.edit().apply {
                putString(KEY_USERNAME, username)
                putString(KEY_MEMBER_SINCE_STR, memberSince)
            }.commit()
        }
    }

    // supabase auth email for when oauth exist
    private fun fallbackDisplayNameFromAuthEmail(): String? {
        val email = SupabaseSdkHolder.client?.auth?.currentSessionOrNull()?.user?.email
            ?: return null
        if (email.endsWith("@placeholder.local")) return null
        val local = email.substringBefore("@").trim()
        return local.takeIf { it.isNotBlank() }
    }

    override fun getUsername(): String {
        usernameMemory?.let { return it }
        val fromPrefs = prefs.getString(KEY_USERNAME, "").orEmpty()
        if (fromPrefs.isNotBlank()) usernameMemory = fromPrefs
        return fromPrefs
    }

    override fun setUsername(username: String) {
        usernameMemory = username
        prefs.edit().putString(KEY_USERNAME, username).commit()
        ioScope.launch {
            upsertProfileFields(JSONObject().put("username", username))
        }
    }

    override fun getMemberSince(): String {
        val local = prefs.getString(KEY_MEMBER_SINCE_STR, null)
        if (!local.isNullOrBlank()) return local
        return "Feb 2026"
    }

    override fun setMemberSinceIfFirstTime() {
        val existing = getMemberSince()
        if (existing != "Feb 2026" || prefs.contains(KEY_MEMBER_SINCE_STR)) return
        val now = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
        prefs.edit().putString(KEY_MEMBER_SINCE_STR, now).apply()
        ioScope.launch {
            upsertProfileFields(JSONObject().put("member_since", now))
        }
    }

    override fun getStoredEmail(): String = prefs.getString(KEY_STORED_EMAIL, "") ?: ""

    override fun getStoredPassword(): String = prefs.getString(KEY_STORED_PASSWORD, "") ?: ""

    override fun setStoredCredentials(email: String, password: String) {
        prefs.edit()
            .putString(KEY_STORED_EMAIL, email)
            .putString(KEY_STORED_PASSWORD, password)
            .apply()
    }

    private suspend fun upsertProfileFields(fields: JSONObject) {
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
