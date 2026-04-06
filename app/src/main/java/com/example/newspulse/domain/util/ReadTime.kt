package com.example.newspulse.domain.util

private const val WORDS_PER_MINUTE = 200

// reading time interest using 200 words/min, formatted string

fun estimateReadTime(text: String): String {
    val wordCount = text.split(Regex("\\s+")).count { it.isNotBlank() }
    val minutes = maxOf(1, (wordCount + WORDS_PER_MINUTE - 1) / WORDS_PER_MINUTE)
    return "$minutes min read"
}
