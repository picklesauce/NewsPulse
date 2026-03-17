package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.data.remote.SupabaseUserSession
import com.example.newspulse.domain.AuthRepository
import com.example.newspulse.domain.AuthResult
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class SupabaseAuthRepository(
    private val client: SupabaseRestClient,
    private val session: SupabaseUserSession
) : AuthRepository {
    private val usersTable = "app_users"
    private val ioExecutor = Executors.newSingleThreadExecutor()

    override fun signUp(email: String, password: String): AuthResult {
        return runOnIo {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail.isBlank() || password.isBlank()) {
                return@runOnIo AuthResult(false, "Email and password are required")
            }

            val existing = client.select(
                table = usersTable,
                columns = "id",
                filters = mapOf("email" to "eq.$normalizedEmail"),
                limit = 1,
                useUserAuth = false
            )
            if (existing.length() > 0) {
                return@runOnIo AuthResult(false, "An account with this email already exists")
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
                    return@runOnIo AuthResult(false, "An account with this email already exists")
                }
                return@runOnIo AuthResult(
                    false,
                    "Sign up failed. ${client.lastError ?: "Insert rejected by database policy."}"
                )
            }
            session.userId = userId
            session.accessToken = null
            AuthResult(true)
        }
    }

    override fun signIn(email: String, password: String): AuthResult {
        return runOnIo {
            val normalizedEmail = email.trim().lowercase()
            if (normalizedEmail.isBlank() || password.isBlank()) {
                return@runOnIo AuthResult(false, "Email and password are required")
            }
            val rows = client.select(
                table = usersTable,
                columns = "id,password",
                filters = mapOf("email" to "eq.$normalizedEmail"),
                limit = 1,
                useUserAuth = false
            )
            if (rows.length() == 0) {
                return@runOnIo AuthResult(false, "Invalid email or password")
            }
            val row = rows.optJSONObject(0) ?: return@runOnIo AuthResult(false, "Invalid email or password")
            val storedPassword = row.optString("password")
            if (storedPassword != password) {
                return@runOnIo AuthResult(false, "Invalid email or password")
            }
            val userId = row.optString("id")
            if (userId.isBlank()) return@runOnIo AuthResult(false, "Invalid account record")
            session.userId = userId
            session.accessToken = null
            AuthResult(true)
        }
    }

    override fun signOut() {
        session.clear()
    }

    override fun getCurrentUserId(): String? = session.userId

    private fun runOnIo(block: () -> AuthResult): AuthResult =
        runCatching { ioExecutor.submit(Callable { block() }).get() }
            .getOrElse { AuthResult(false, it.message ?: "Unknown network error") }
}
