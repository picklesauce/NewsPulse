package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.domain.InterestsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SupabaseInterestsRepository(
    private val client: SupabaseRestClient,
    private val userIdProvider: () -> String?
) : InterestsRepository {

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var followedIds: MutableSet<String> = mutableSetOf()
    private var onboardingComplete: Boolean = false
    private val stateLock = Any()

    override fun getFollowedInterestIds(): Set<String> = synchronized(stateLock) { followedIds.toSet() }

    override fun setFollowedInterestIds(ids: Set<String>) {
        val userId = userIdProvider() ?: return
        synchronized(stateLock) { followedIds = ids.toMutableSet() }
        ioScope.launch {
            client.delete("followed_interests", mapOf("user_id" to "eq.$userId"))
            ids.forEach { interestId ->
                val row = JSONObject()
                    .put("id", UUID.randomUUID().toString())
                    .put("user_id", userId)
                    .put("interest_id", interestId)
                client.insert(table = "followed_interests", body = row)
            }
        }
    }

    override fun followInterest(id: String) {
        ioScope.launch { followInterestSuspend(id) }
    }

    override suspend fun followInterestSuspend(id: String) {
        val userId = userIdProvider() ?: return
        synchronized(stateLock) {
            if (!followedIds.add(id)) return
        }
        val row = JSONObject()
            .put("id", UUID.randomUUID().toString())
            .put("user_id", userId)
            .put("interest_id", id)
        val ok = client.insert(table = "followed_interests", body = row)
        if (!ok) {
            synchronized(stateLock) { followedIds.remove(id) }
        }
    }

    override fun unfollowInterest(id: String) {
        ioScope.launch { unfollowInterestSuspend(id) }
    }

    override suspend fun unfollowInterestSuspend(id: String) {
        val userId = userIdProvider() ?: return
        synchronized(stateLock) { followedIds.remove(id) }
        client.delete(
            "followed_interests",
            mapOf(
                "user_id" to "eq.$userId",
                "interest_id" to "eq.$id"
            )
        )
    }

    override fun onUserChanged() {
        ioScope.launch { runCatching { reloadSuspend() } }
    }

    override fun isOnboardingComplete(): Boolean = synchronized(stateLock) { onboardingComplete }

    override fun setOnboardingComplete() {
        val userId = userIdProvider() ?: return
        synchronized(stateLock) { onboardingComplete = true }
        ioScope.launch {
            val body = JSONObject().put("onboarding_complete", true)
            client.patch(
                table = "user_profiles",
                body = body,
                filters = mapOf("user_id" to "eq.$userId")
            )
        }
    }

    /** Loads profile + followed interests from Supabase; call from IO dispatcher during bootstrap. */
    suspend fun awaitInitialSync() {
        ensureUserProfileSuspend()
        reloadSuspend()
    }

    private suspend fun reloadSuspend() {
        val userId = userIdProvider() ?: run {
            synchronized(stateLock) {
                followedIds = mutableSetOf()
                onboardingComplete = false
            }
            return
        }
        val followedRows = client.select(
            table = "followed_interests",
            columns = "interest_id",
            filters = mapOf("user_id" to "eq.$userId")
        )
        val loaded = mutableSetOf<String>()
        for (i in 0 until followedRows.length()) {
            val id = followedRows.optJSONObject(i)?.optString("interest_id").orEmpty()
            if (id.isNotBlank()) loaded.add(id)
        }

        val userRows = client.select(
            table = "user_profiles",
            columns = "onboarding_complete",
            filters = mapOf("user_id" to "eq.$userId"),
            limit = 1
        )
        val ob = if (userRows.length() > 0) {
            userRows.optJSONObject(0)?.optBoolean("onboarding_complete", false) == true
        } else {
            false
        }
        synchronized(stateLock) {
            followedIds = loaded
            onboardingComplete = ob
        }
    }

    private suspend fun ensureUserProfileSuspend() {
        val userId = userIdProvider() ?: return
        val rows = client.select(
            table = "user_profiles",
            columns = "user_id",
            filters = mapOf("user_id" to "eq.$userId"),
            limit = 1
        )
        if (rows.length() > 0) return
        val memberSince = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
        val body = JSONObject()
            .put("user_id", userId)
            .put("username", "user_${userId.take(8)}")
            .put("member_since", memberSince)
            .put("onboarding_complete", false)
        client.insert(
            table = "user_profiles",
            body = body,
            onConflict = "user_id",
            upsert = true
        )
    }
}
