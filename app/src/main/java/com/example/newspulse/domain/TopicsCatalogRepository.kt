package com.example.newspulse.domain

interface TopicsCatalogRepository {
    fun getAvailableTopics(): List<String>
}
