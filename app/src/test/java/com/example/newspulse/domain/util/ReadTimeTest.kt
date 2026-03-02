package com.example.newspulse.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

internal class ReadTimeTest {

    @Test
    fun estimateReadTime_returnsOneMinForShortText() {
        assertEquals("1 min read", estimateReadTime("Hello world"))
    }

    @Test
    fun estimateReadTime_returnsOneMinForEmptyText() {
        assertEquals("1 min read", estimateReadTime(""))
    }

    @Test
    fun estimateReadTime_calculatesCorrectlyForLongerText() {
        // 200 words = 1 min, 400 words = 2 min
        val words = (1..400).joinToString(" ") { "word" }
        assertEquals("2 min read", estimateReadTime(words))
    }

    @Test
    fun estimateReadTime_roundsUp() {
        // 201 words should round up to 2 min
        val words = (1..201).joinToString(" ") { "word" }
        assertEquals("2 min read", estimateReadTime(words))
    }

    @Test
    fun estimateReadTime_handlesWhitespaceOnly() {
        assertEquals("1 min read", estimateReadTime("   \n\t  "))
    }
}
