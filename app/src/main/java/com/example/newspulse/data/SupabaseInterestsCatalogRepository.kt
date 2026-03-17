package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.domain.InterestsCatalogRepository
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType

class SupabaseInterestsCatalogRepository(
    private val client: SupabaseRestClient
) : InterestsCatalogRepository {
    private var cache: List<Interest> = emptyList()

    override fun getAllInterests(): List<Interest> {
        if (cache.isNotEmpty()) return cache
        val rows = client.select(
            table = "interests",
            columns = "id,type,name",
            order = "name.asc",
            useUserAuth = false
        )
        val parsed = buildList {
            for (i in 0 until rows.length()) {
                val r = rows.optJSONObject(i) ?: continue
                val id = r.optString("id")
                val name = r.optString("name")
                val typeRaw = r.optString("type")
                val type = runCatching { InterestType.valueOf(typeRaw) }.getOrElse { InterestType.Topic }
                if (id.isNotBlank() && name.isNotBlank()) {
                    add(Interest(id = id, type = type, name = name))
                }
            }
        }
        cache = parsed
        return cache
    }
}
