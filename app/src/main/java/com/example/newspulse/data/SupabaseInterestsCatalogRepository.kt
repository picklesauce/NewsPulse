package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.domain.InterestsCatalogRepository
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType

class SupabaseInterestsCatalogRepository(
    private val client: SupabaseRestClient
) : InterestsCatalogRepository {
    private var cache: MutableList<Interest> = mutableListOf()

    override fun getAllInterests(): List<Interest> {
        if (cache.isNotEmpty()) return cache.toList()
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
        cache = parsed
        return cache.toList()
    }

    override fun addCustomInterest(name: String, type: InterestType): Interest {
        val existing = getAllInterests().find { it.name.equals(name, ignoreCase = true) }
        if (existing != null) return existing

        val interest = Interest(
            id = "interest-${name.lowercase().replace(" ", "-")}",
            type = type,
            name = name
        )
        val body = org.json.JSONObject()
            .put("id", interest.id)
            .put("type", type.name)
            .put("name", name)
        client.insert(table = "interests", body = body, onConflict = "id", upsert = true)
        cache.add(interest)
        return interest
    }
}
