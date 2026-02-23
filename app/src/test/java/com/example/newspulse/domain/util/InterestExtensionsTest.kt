package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InterestExtensionsTest {

    @Test
    fun filterByType_returnsMatchingInterests() {
        val interests = listOf(
            Interest("i-1", InterestType.Topic, "Tech"),
            Interest("i-2", InterestType.Country, "USA"),
            Interest("i-3", InterestType.Topic, "Science")
        )
        val result = interests.filterByType(InterestType.Topic)
        assertEquals(2, result.size)
        assertTrue(result.all { it.type == InterestType.Topic })
    }

    @Test
    fun filterByType_returnsEmpty_whenNoMatches() {
        val interests = listOf(
            Interest("i-1", InterestType.Topic, "Tech"),
            Interest("i-2", InterestType.Person, "Elon")
        )
        val result = interests.filterByType(InterestType.Company)
        assert(result.isEmpty())
    }

    @Test
    fun filterByType_returnsAll_whenAllMatch() {
        val interests = listOf(
            Interest("i-1", InterestType.Country, "USA"),
            Interest("i-2", InterestType.Country, "UK")
        )
        val result = interests.filterByType(InterestType.Country)
        assertEquals(2, result.size)
    }
}
