package com.example.newspulse.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringFilterTest {

    @Test
    fun filterMatchingQuery_returnsAll_whenQueryEmpty() {
        val list = listOf("Technology", "Politics", "Business")
        assertEquals(list, list.filterMatchingQuery(""))
        assertEquals(list, list.filterMatchingQuery("   "))
    }

    @Test
    fun filterMatchingQuery_filtersCaseInsensitive() {
        val list = listOf("Technology", "Politics", "Business")
        assertEquals(listOf("Technology"), list.filterMatchingQuery("tech"))
        assertEquals(listOf("Technology"), list.filterMatchingQuery("TECH"))
    }

    @Test
    fun filterMatchingQuery_returnsMatchingSubstrings() {
        val list = listOf("Technology", "Biology", "Psychology")
        assertEquals(listOf("Technology", "Biology", "Psychology"), list.filterMatchingQuery("ology"))
    }

    @Test
    fun filterMatchingQuery_returnsEmpty_whenNoMatch() {
        val list = listOf("Technology", "Politics")
        assertTrue(list.filterMatchingQuery("xyz").isEmpty())
    }

    @Test
    fun filterMatchingQuery_trimsQuery() {
        val list = listOf("Technology")
        assertEquals(listOf("Technology"), list.filterMatchingQuery("  tech  "))
    }
}
