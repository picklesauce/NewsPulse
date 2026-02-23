package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.InterestType
import org.junit.Assert.assertEquals
import org.junit.Test

class InterestFactoryTest {

    @Test
    fun toInterest_createsInterestWithCorrectFields() {
        val interest = "Technology".toInterest(idx = 0)
        assertEquals("i-0-Technology", interest.id)
        assertEquals(InterestType.Topic, interest.type)
        assertEquals("Technology", interest.name)
    }

    @Test
    fun toInterest_withCustomType_usesProvidedType() {
        val interest = "USA".toInterest(idx = 1, type = InterestType.Country)
        assertEquals(InterestType.Country, interest.type)
    }
}
