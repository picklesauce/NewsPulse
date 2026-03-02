package com.example.newspulse.domain.util

private const val WORDS_PER_MINUTE = 200

/**
 * Estimates reading time from text using ~200 words per minute.
 * Returns formatted string e.g. "3 min read" or "1 min read".
 */
fun estimateReadTime(text: String): String {
    val wordCount = text.split(Regex("\\s+")).count { it.isNotBlank() }
    val minutes = maxOf(1, (wordCount + WORDS_PER_MINUTE - 1) / WORDS_PER_MINUTE)
    return "$minutes min read"
}
