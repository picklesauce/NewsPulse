package com.example.newspulse.domain.model

import com.example.newspulse.domain.util.estimateReadTime
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

    fun matches(query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        val lower = q.lowercase()
        return title.lowercase().contains(lower) ||
            source.lowercase().contains(lower) ||
            summary.lowercase().contains(lower) ||
            interests.any { it.name.lowercase().contains(lower) }
    }

    fun matchesInterests(selected: Set<String>): Boolean =
        selected.isEmpty() || interests.any { it.name in selected }

    val hoursAgo: String get() = publishedAt.toHoursAgo()
    val snippet: String get() = summary
    val topics: List<String> get() = interests.map { it.name }
    /** Estimated read time from title + summary (used on article cards). */
    val readTime: String get() = estimateReadTime("$title $summary")
}
