package com.example.newspulse.data.mock

import com.example.newspulse.domain.TopicsCatalogRepository

class MockTopicsCatalogRepository : TopicsCatalogRepository {
    override fun getAvailableTopics(): List<String> = listOf(
        "USA",
        "Canada",
        "United Kingdom",
        "Germany",
        "Japan",
        "Donald Trump",
        "Elon Musk",
        "Taylor Swift",
        "Tesla",
        "Apple",
        "Google",
        "Microsoft",
        "Technology",
        "Business",
        "Sports",
        "Entertainment",
        "Politics",
        "Science",
        "Health",
        "Climate"
    )
}
