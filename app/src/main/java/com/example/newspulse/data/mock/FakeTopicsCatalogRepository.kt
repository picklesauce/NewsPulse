package com.example.newspulse.data.mock

import com.example.newspulse.domain.TopicsCatalogRepository

class FakeTopicsCatalogRepository : TopicsCatalogRepository {
    override fun getAvailableTopics(): List<String> = MockTopicsCatalogRepository().getAvailableTopics()
}
