package com.example.newspulse.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterestTest {

    @Test
    fun isValid_returnsTrue_whenIdAndNameAreNonBlank() {
        val interest = Interest(id = "i-1", type = InterestType.Topic, name = "Technology")
        assertTrue(interest.isValid())
    }

    @Test
    fun isValid_returnsFalse_whenIdIsBlank() {
        val interest = Interest(id = "", type = InterestType.Topic, name = "Technology")
        assertFalse(interest.isValid())
    }

    @Test
    fun isValid_returnsFalse_whenNameIsBlank() {
        val interest = Interest(id = "i-1", type = InterestType.Topic, name = "")
        assertFalse(interest.isValid())
    }
}
