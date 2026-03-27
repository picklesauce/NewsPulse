package com.example.newspulse.data.mock

import com.example.newspulse.domain.InterestsCatalogRepository
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType

class MockInterestsCatalogRepository : InterestsCatalogRepository {
    private val customInterests = mutableListOf<Interest>()

    override fun getAllInterests(): List<Interest> = MockDB.interests + customInterests

    override fun addCustomInterest(name: String, type: InterestType): Interest {
        val all = getAllInterests()
        val existing = all.find { it.name.equals(name, ignoreCase = true) }
        if (existing != null) return existing

        val interest = Interest(
            id = "interest-${name.lowercase().replace(" ", "-")}",
            type = type,
            name = name
        )
        customInterests.add(interest)
        return interest
    }
}
