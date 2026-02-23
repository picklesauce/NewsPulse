package com.example.newspulse.domain

import com.example.newspulse.domain.model.ReadingHistoryItem

interface ReadingHistoryRepository {
    fun getReadingHistory(): List<ReadingHistoryItem>
    fun addToHistory(title: String)
}
