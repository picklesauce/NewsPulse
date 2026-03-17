package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.domain.InterestsRepository
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SupabaseInterestsRepository(
    private val client: SupabaseRestClient,
    private val userIdProvider: () -> String?
) : InterestsRepository {
    private var followedIds: MutableSet<String> = mutableSetOf()
    private var onboardingComplete: Boolean = false

    init {
        ensureUserProfile()
        reload()
    }

    override fun getFollowedInterestIds(): Set<String> = followedIds.toSet()

    override fun setFollowedInterestIds(ids: Set<String>) {
        val userId = userIdProvider() ?: return
        followedIds = ids.toMutableSet()
        client.delete("followed_interests", mapOf("user_id" to "eq.$userId"))
        ids.forEach { interestId ->
            val row = JSONObject()
                .put("id", UUID.randomUUID().toString())
                .put("user_id", userId)
                .put("interest_id", interestId)
            client.insert(table = "followed_interests", body = row)
        }
    }

    override fun followInterest(id: String) {
        val userId = userIdProvider() ?: return
        if (followedIds.add(id)) {
            val row = JSONObject()
                .put("id", UUID.randomUUID().toString())
                .put("user_id", userId)
                .put("interest_id", id)
            client.insert(table = "followed_interests", body = row)
        }
    }

    override fun unfollowInterest(id: String) {
        val userId = userIdProvider() ?: return
        followedIds.remove(id)
        client.delete(
            "followed_interests",
            mapOf(
                "user_id" to "eq.$userId",
                "interest_id" to "eq.$id"
            )
        )
    }

    override fun isOnboardingComplete(): Boolean = onboardingComplete

    override fun setOnboardingComplete() {
        val userId = userIdProvider() ?: return
        onboardingComplete = true
        val body = JSONObject().put("onboarding_complete", true)
        client.patch(
            table = "user_profiles",
            body = body,
            filters = mapOf("user_id" to "eq.$userId")
        )
    }

    private fun reload() {
        val userId = userIdProvider() ?: run {
            followedIds = mutableSetOf()
            onboardingComplete = false
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
        followedIds = loaded

        val userRows = client.select(
            table = "user_profiles",
            columns = "onboarding_complete",
            filters = mapOf("user_id" to "eq.$userId"),
            limit = 1
        )
        onboardingComplete = if (userRows.length() > 0) {
            userRows.optJSONObject(0)?.optBoolean("onboarding_complete", false) == true
        } else {
            false
        }
    }

    private fun ensureUserProfile() {
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
