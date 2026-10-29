package com.campsitereservation.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateRangeTest {

    private fun may(day: Int): LocalDate = LocalDate.of(2027, 5, day)

    private fun range(startDay: Int, endDay: Int) = DateRange(may(startDay), may(endDay))

    @Test(expected = IllegalArgumentException::class)
    fun endBeforeStart_throws() {
        range(5, 3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun endEqualToStart_throws() {
        range(5, 5)
    }

    @Test
    fun nights_countsDaysBetweenStartAndEnd() {
        assertEquals(2L, range(1, 3).nights)
    }

    @Test
    fun containsDate_includesStartExcludesEnd() {
        val stay = range(1, 3)
        assertTrue(may(1) in stay)
        assertTrue(may(2) in stay)
        assertFalse(may(3) in stay)
        assertFalse(may(1).minusDays(1) in stay)
    }

    @Test
    fun containsRange_trueOnlyWhenFullyInside() {
        val season = range(1, 31)
        assertTrue(range(1, 31) in season)
        assertTrue(range(10, 12) in season)
        assertFalse(DateRange(may(30), may(31).plusDays(1)) in season)
    }

    @Test
    fun overlaps_trueWhenSharingANight() {
        assertTrue(range(1, 4).overlaps(range(3, 6)))
        assertTrue(range(3, 6).overlaps(range(1, 4)))
        assertTrue(range(1, 10).overlaps(range(4, 5)))
    }

    @Test
    fun overlaps_falseForBackToBackStays() {
        assertFalse(range(1, 3).overlaps(range(3, 5)))
        assertFalse(range(3, 5).overlaps(range(1, 3)))
    }

    @Test
    fun dates_listsEachNight() {
        assertEquals(listOf(may(1), may(2), may(3)), range(1, 4).dates())
    }
}
