package com.example.newspulse.domain

import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType

interface InterestsCatalogRepository {
    fun getAllInterests(): List<Interest>

    /** Adds a user-created interest to the catalog. Returns the created [Interest]. */
    fun addCustomInterest(name: String, type: InterestType): Interest {
        return Interest(
            id = "interest-${name.lowercase().replace(" ", "-")}",
            type = type,
            name = name
        )
    }
}
