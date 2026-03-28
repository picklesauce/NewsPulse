package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.data.remote.SupabaseUserSession
import com.example.newspulse.domain.AuthRepository
import com.example.newspulse.domain.AuthResult
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SupabaseAuthRepository(
    private val client: SupabaseRestClient,
    private val session: SupabaseUserSession
) : AuthRepository {
    private val usersTable = "app_users"

    override suspend fun signUp(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || password.isBlank()) {
            return AuthResult(false, "Email and password are required")
        }

        val existing = client.select(
            table = usersTable,
            columns = "id",
            filters = mapOf("email" to "eq.$normalizedEmail"),
            limit = 1,
            useUserAuth = false
        )
        if (existing.length() > 0) {
            return AuthResult(false, "An account with this email already exists")
        }

        val userId = UUID.randomUUID().toString()
        val body = JSONObject()
            .put("id", userId)
            .put("email", normalizedEmail)
            .put("password", password)
        val inserted = client.insert(
            table = usersTable,
            body = body,
            useUserAuth = false
        )
        if (!inserted) {
            val existsAfter = client.select(
                table = usersTable,
                columns = "id",
                filters = mapOf("email" to "eq.$normalizedEmail"),
                limit = 1,
                useUserAuth = false
            )
            if (existsAfter.length() > 0) {
                return AuthResult(false, "An account with this email already exists")
            }
            return AuthResult(
                false,
                "Sign up failed. ${client.lastError ?: "Insert rejected by database policy."}"
            )
        }
        session.userId = userId
        session.accessToken = null
        ensureUserProfile(userId, normalizedEmail)
        return AuthResult(true)
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        val raw = email.trim()
        if (raw.isBlank() || password.isBlank()) {
            return AuthResult(false, "Email/username and password are required")
        }
        val rows = if (looksLikeEmail(raw)) {
            val normalizedEmail = raw.lowercase()
            client.select(
                table = usersTable,
                columns = "id,password,email",
                filters = mapOf("email" to "eq.$normalizedEmail"),
                limit = 1,
                useUserAuth = false
            )
        } else {
            val profiles = client.select(
                table = "user_profiles",
                columns = "user_id",
                filters = mapOf("username" to "ilike.$raw"),
                limit = 1,
                useUserAuth = false
            )
            if (profiles.length() == 0) {
                return AuthResult(false, "Invalid email or password")
            }
            val userId = profiles.optJSONObject(0)?.optString("user_id").orEmpty()
            if (userId.isBlank()) {
                return AuthResult(false, "Invalid email or password")
            }
            client.select(
                table = usersTable,
                columns = "id,password,email",
                filters = mapOf("id" to "eq.$userId"),
                limit = 1,
                useUserAuth = false
            )
        }
        if (rows.length() == 0) {
            return AuthResult(false, "Invalid email or password")
        }
        val row = rows.optJSONObject(0) ?: return AuthResult(false, "Invalid email or password")
        val storedPassword = row.optString("password")
        if (storedPassword != password) {
            return AuthResult(false, "Invalid email or password")
        }
        val userId = row.optString("id")
        if (userId.isBlank()) return AuthResult(false, "Invalid account record")
        val emailForProfile = row.optString("email").ifBlank { raw }
        session.userId = userId
        session.accessToken = null
        ensureUserProfile(userId, emailForProfile)
        return AuthResult(true)
    }

    private fun looksLikeEmail(s: String): Boolean = "@" in s

    override fun signOut() {
        session.clear()
    }

    override fun getCurrentUserId(): String? = session.userId

    private suspend fun ensureUserProfile(userId: String, email: String) {
        val existing = client.select(
            table = "user_profiles",
            columns = "user_id",
            filters = mapOf("user_id" to "eq.$userId"),
            limit = 1,
            useUserAuth = false
        )
        if (existing.length() > 0) return

        val username = email.substringBefore("@").ifBlank { "user_${userId.take(8)}" }
        val memberSince = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
        val body = JSONObject()
            .put("user_id", userId)
            .put("username", username)
            .put("member_since", memberSince)
            .put("onboarding_complete", false)
        client.insert(
            table = "user_profiles",
            body = body,
            useUserAuth = false
        )
    }
}
