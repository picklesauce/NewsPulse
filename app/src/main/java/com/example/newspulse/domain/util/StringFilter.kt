package com.example.newspulse.domain.util

fun List<String>.filterMatchingQuery(query: String): List<String> {
    val q = query.trim().lowercase()
    return if (q.isEmpty()) this else filter { it.lowercase().contains(q) }
}
