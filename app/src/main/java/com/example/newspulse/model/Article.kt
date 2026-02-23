package com.example.newspulse.model

data class Article(
    val title: String,
    val source: String,
    val hoursAgo: String,
    val snippet: String = "",
    val topics: List<String> = emptyList()
)

