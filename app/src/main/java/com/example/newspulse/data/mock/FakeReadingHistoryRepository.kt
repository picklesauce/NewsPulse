package com.example.newspulse.data.mock

import com.example.newspulse.domain.ReadingHistoryRepository
import com.example.newspulse.domain.model.ReadingHistoryItem

class FakeReadingHistoryRepository : ReadingHistoryRepository {
    override fun getReadingHistory(): List<ReadingHistoryItem> = emptyList()
    override fun addToHistory(articleId: String, title: String) {}
}
