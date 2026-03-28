package com.example.newspulse.domain

import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType

interface InterestsCatalogRepository {
    fun getAllInterests(): List<Interest>

    /**
     * Adds a user-created interest to the catalog (persisted when using Supabase) and returns it.
     * Implementations should use stable ids: `interest-{slug-from-name}`.
     */
    suspend fun addCustomInterest(name: String, type: InterestType): Interest
}
