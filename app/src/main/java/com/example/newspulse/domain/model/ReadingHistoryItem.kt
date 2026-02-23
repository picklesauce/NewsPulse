package com.example.newspulse.domain.model

data class ReadingHistoryItem(
    val articleId: String,
    val title: String,
    val readAtMillis: Long
)
