package com.example.newspulse.domain

import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.util.InterestSlug

interface InterestsCatalogRepository {
    fun getAllInterests(): List<Interest>

    /** Adds a user-created interest to the catalog. Returns the created [Interest]. */
    fun addCustomInterest(name: String, type: InterestType): Interest {
        return Interest(
            id = InterestSlug.stableIdForName(name),
            type = type,
            name = name
        )
    }

    /** Ensures the interest exists in the backing store before following (e.g. Supabase FK). */
    suspend fun addCustomInterestPersisted(name: String, type: InterestType): Interest =
        addCustomInterest(name, type)
}
