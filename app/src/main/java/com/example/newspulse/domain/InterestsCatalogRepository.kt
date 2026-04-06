package com.example.newspulse.domain

import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.util.InterestSlug

interface InterestsCatalogRepository {
    fun getAllInterests(): List<Interest>

    // create user created inputted interest, and resturn
    fun addCustomInterest(name: String, type: InterestType): Interest {
        return Interest(
            id = InterestSlug.stableIdForName(name),
            type = type,
            name = name
        )
    }

    // persist user created interest, ensure it exists
    suspend fun addCustomInterestPersisted(name: String, type: InterestType): Interest =
        addCustomInterest(name, type)
}
