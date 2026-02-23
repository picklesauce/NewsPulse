package com.example.newspulse.data.mock

import com.example.newspulse.domain.InterestsCatalogRepository
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType

class MockInterestsCatalogRepository : InterestsCatalogRepository {
    override fun getAllInterests(): List<Interest> {
        val countries = listOf("USA", "Canada", "United Kingdom", "Germany", "Japan")
        val persons = listOf("Donald Trump", "Elon Musk", "Taylor Swift")
        val companies = listOf("Tesla", "Apple", "Google", "Microsoft")
        val topics = listOf(
            "Technology", "Business", "Sports", "Entertainment",
            "Politics", "Science", "Health", "Climate"
        )
        return buildList {
            countries.forEach { add(Interest("interest-${it.lowercase().replace(" ", "-")}", InterestType.Country, it)) }
            persons.forEach { add(Interest("interest-${it.lowercase().replace(" ", "-")}", InterestType.Person, it)) }
            companies.forEach { add(Interest("interest-${it.lowercase().replace(" ", "-")}", InterestType.Company, it)) }
            topics.forEach { add(Interest("interest-${it.lowercase().replace(" ", "-")}", InterestType.Topic, it)) }
        }
    }
}
