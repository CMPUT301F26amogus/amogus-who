package com.example.code.Model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AvailabilityScheduleTest {

    private fun may(day: Int): LocalDate = LocalDate.of(2027, 5, day)

    private fun range(startDay: Int, endDay: Int) = DateRange(may(startDay), may(endDay))

    // open may 1 to may 10 so nights 1 through 9
    private fun scheduleOpenMay1to10() = AvailabilitySchedule().apply { open(range(1, 10)) }

    @Test
    fun newSchedule_hasNothingOpen() {
        val s = AvailabilitySchedule()
        assertTrue(s.openNights.isEmpty())
        assertFalse(s.isAvailable(range(1, 2)))
    }

    @Test
    fun open_makesNightsAvailable() {
        val s = scheduleOpenMay1to10()
        assertTrue(s.isAvailable(range(1, 10)))
        assertTrue(s.isAvailable(range(3, 5)))
    }

    @Test
    fun rangePastOpenNights_isNotAvailable() {
        val s = scheduleOpenMay1to10()
        assertFalse(s.isAvailable(range(8, 12)))
    }

    @Test
    fun backToBackOpenRanges_canBeBookedAcross() {
        val s = AvailabilitySchedule()
        s.open(range(1, 3))
        s.open(range(3, 5))
        assertTrue(s.isAvailable(range(2, 4)))
        assertEquals(listOf(range(1, 5)), s.openRanges())
    }

    @Test
    fun book_blocksOverlappingStays() {
        val s = scheduleOpenMay1to10()
        s.book(range(3, 5))
        assertFalse(s.isAvailable(range(4, 6)))
        assertFalse(s.isAvailable(range(1, 4)))
        assertFalse(s.isAvailable(range(3, 5)))
    }

    @Test
    fun book_allowsBackToBackStays() {
        val s = scheduleOpenMay1to10()
        s.book(range(3, 5))
        assertTrue(s.isAvailable(range(1, 3)))
        assertTrue(s.isAvailable(range(5, 7)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun book_takenDatesThrows() {
        val s = scheduleOpenMay1to10()
        s.book(range(3, 5))
        s.book(range(4, 6))
    }

    @Test(expected = IllegalArgumentException::class)
    fun book_closedDatesThrows() {
        scheduleOpenMay1to10().book(range(9, 11))
    }

    @Test
    fun release_freesNightsAgain() {
        val s = scheduleOpenMay1to10()
        s.book(range(3, 5))
        s.release(range(3, 5))
        assertTrue(s.isAvailable(range(3, 5)))
        assertTrue(s.bookings.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun release_unbookedRangeThrows() {
        scheduleOpenMay1to10().release(range(3, 5))
    }

    @Test
    fun close_removesNights() {
        val s = scheduleOpenMay1to10()
        s.close(range(4, 6))
        assertFalse(s.isAvailable(range(4, 5)))
        assertEquals(listOf(range(1, 4), range(6, 10)), s.openRanges())
    }

    @Test(expected = IllegalArgumentException::class)
    fun close_bookedNightsThrows() {
        val s = scheduleOpenMay1to10()
        s.book(range(3, 5))
        s.close(range(4, 6))
    }

    @Test
    fun availableNights_skipsBookedNights() {
        val s = AvailabilitySchedule()
        s.open(range(1, 5))
        s.book(range(2, 4))
        assertEquals(listOf(may(1), may(4)), s.availableNights())
    }

    @Test
    fun bookings_areSortedByStart() {
        val s = scheduleOpenMay1to10()
        s.book(range(6, 8))
        s.book(range(1, 3))
        assertEquals(listOf(range(1, 3), range(6, 8)), s.bookings)
    }

    @Test
    fun constructor_loadsSavedData() {
        val s = AvailabilitySchedule(range(1, 10).dates(), listOf(range(2, 4)))
        assertTrue(s.isBooked(may(2)))
        assertFalse(s.isAvailable(range(3, 5)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun constructor_rejectsOverlappingSavedBookings() {
        AvailabilitySchedule(range(1, 10).dates(), listOf(range(2, 4), range(3, 5)))
    }

    @Test
    fun openRanges_emptyWhenNothingOpen() {
        assertTrue(AvailabilitySchedule().openRanges().isEmpty())
    }
}
