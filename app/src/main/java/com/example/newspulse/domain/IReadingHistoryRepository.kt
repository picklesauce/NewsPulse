package com.example.newspulse.domain

import com.example.newspulse.domain.model.ReadingHistoryItem

interface IReadingHistoryRepository {
    fun getReadingHistory(): List<ReadingHistoryItem>
    fun addToHistory(title: String)
}
