package com.example.newspulse.domain.model

import com.example.newspulse.domain.util.toHoursAgo

data class Article(
    val id: String,
    val title: String,
    val source: String,
    val url: String = "",
    val publishedAt: Long,
    val summary: String = "",
    val imageUrl: String = "",
    val interests: List<Interest> = emptyList()
) {
    fun isValid(): Boolean = id.isNotBlank() && title.isNotBlank()

    val hoursAgo: String get() = publishedAt.toHoursAgo()
    val snippet: String get() = summary
    val topics: List<String> get() = interests.map { it.name }
}
