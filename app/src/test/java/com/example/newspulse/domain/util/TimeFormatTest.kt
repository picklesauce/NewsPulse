package com.example.newspulse.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    private val ms = { h: Long -> h * 60 * 60 * 1000L }
    private val ds = { d: Long -> d * 24 * 60 * 60 * 1000L }

    @Test
    fun toHoursAgo_underOneHour_returnsMinutes() {
        val past = System.currentTimeMillis() - (45 * 60 * 1000L)
        assertEquals("45m ago", past.toHoursAgo())
    }

    @Test
    fun toHoursAgo_oneToTwentyThreeHours_returnsHours() {
        val past = System.currentTimeMillis() - ms(2)
        assertEquals("2h ago", past.toHoursAgo())
    }

    @Test
    fun toHoursAgo_oneToSixDays_returnsDays() {
        val past = System.currentTimeMillis() - ds(3)
        assertEquals("3d ago", past.toHoursAgo())
    }

    @Test
    fun toHoursAgo_sevenOrMoreDays_returnsWeeks() {
        val past = System.currentTimeMillis() - ds(14)
        assertEquals("2w ago", past.toHoursAgo())
    }

    @Test
    fun toHoursAgo_futureTimestamp_returnsNegativeMinutes() {
        val future = System.currentTimeMillis() + (30 * 60 * 1000L)
        assertEquals("-30m ago", future.toHoursAgo())
    }
}
