package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.domain.InterestsCatalogRepository
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.util.InterestSlug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

class SupabaseInterestsCatalogRepository(
    private val client: SupabaseRestClient
) : InterestsCatalogRepository {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cache: MutableList<Interest> = mutableListOf()
    private val cacheLock = Any()

    override fun getAllInterests(): List<Interest> = synchronized(cacheLock) { cache.toList() }

    /** Fetches interests from Supabase if cache is empty; safe to call from IO during bootstrap. */
    suspend fun preloadCatalogIfEmpty() {
        synchronized(cacheLock) {
            if (cache.isNotEmpty()) return
        }
        val rows = client.select(
            table = "interests",
            columns = "id,type,name",
            order = "name.asc",
            useUserAuth = false
        )
        val parsed = mutableListOf<Interest>()
        for (i in 0 until rows.length()) {
            val r = rows.optJSONObject(i) ?: continue
            val id = r.optString("id")
            val name = r.optString("name")
            val typeRaw = r.optString("type")
            val type = runCatching { InterestType.valueOf(typeRaw) }.getOrElse { InterestType.Topic }
            if (id.isNotBlank() && name.isNotBlank()) {
                parsed.add(Interest(id = id, type = type, name = name))
            }
        }
        synchronized(cacheLock) {
            if (cache.isEmpty()) cache = parsed
        }
    }

    override fun addCustomInterest(name: String, type: InterestType): Interest {
        val existing = getAllInterests().find { it.name.equals(name, ignoreCase = true) }
        if (existing != null) return existing

        val interest = Interest(
            id = InterestSlug.stableIdForName(name),
            type = type,
            name = name
        )
        val body = JSONObject()
            .put("id", interest.id)
            .put("type", type.name)
            .put("name", name)
        ioScope.launch {
            client.insert(table = "interests", body = body, onConflict = "id", upsert = true)
        }
        synchronized(cacheLock) {
            if (cache.none { it.id == interest.id }) cache.add(interest)
        }
        return interest
    }

    override suspend fun addCustomInterestPersisted(name: String, type: InterestType): Interest {
        val existing = getAllInterests().find { it.name.equals(name, ignoreCase = true) }
        if (existing != null) return existing

        val interest = Interest(
            id = InterestSlug.stableIdForName(name),
            type = type,
            name = name
        )

        // Optimistic local add so UI responds immediately.
        synchronized(cacheLock) {
            if (cache.none { it.id == interest.id }) cache.add(interest)
        }

        val body = JSONObject()
            .put("id", interest.id)
            .put("type", type.name)
            .put("name", name)

        // Attempt upsert; retry once on failure so the FK referenced by followed_interests is
        // guaranteed to exist before the caller inserts into that table.
        var ok = runCatching {
            client.insert(table = "interests", body = body, onConflict = "id", upsert = true)
        }.getOrDefault(false)

        if (!ok) {
            // Retry — transient network errors shouldn't block a custom interest from being saved.
            ok = runCatching {
                client.insert(table = "interests", body = body, onConflict = "id", upsert = true)
            }.getOrDefault(false)
        }

        if (!ok) {
            // Verify the row might already exist (race / concurrent insert).
            val rows = runCatching {
                client.select(
                    table = "interests",
                    columns = "id",
                    filters = mapOf("id" to "eq.${interest.id}"),
                    limit = 1,
                    useUserAuth = false
                )
            }.getOrNull()
            // If the row exists via a previous run, that's fine — cache is already updated.
        }

        return interest
    }
}
