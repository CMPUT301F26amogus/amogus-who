package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

// US 01.04.01 a lottery winner sees the dates still free at a campsite
class AvailabilityScheduleTest {

    // open for may 2027 so the last check out is june 1
    private val may = AvailabilitySchedule(openNights = listOf("2027-05-01", "2027-06-01"))
    private val today = LocalDate.of(2027, 4, 1)

    private fun stay(start: String, end: String) = DateRange(start, end)

    @Test
    fun nothingBooked_wholePeriodIsFree() {
        assertEquals(listOf(stay("2027-05-01", "2027-06-01")), may.freeStays(emptyList(), today))
    }

    @Test
    fun noOpenPeriod_nothingIsFree() {
        assertTrue(AvailabilitySchedule().freeStays(emptyList(), today).isEmpty())
        assertTrue(AvailabilitySchedule(listOf("2027-05-01")).freeStays(emptyList(), today).isEmpty())
    }

    @Test
    fun bookedStay_isCutOut() {
        val free = may.freeStays(listOf(stay("2027-05-10", "2027-05-12")), today)
        assertEquals(listOf(stay("2027-05-01", "2027-05-10"), stay("2027-05-12", "2027-06-01")), free)
    }

    @Test
    fun overlappingAndOutOfOrderBookings_areMerged() {
        val booked = listOf(stay("2027-05-20", "2027-05-25"), stay("2027-05-03", "2027-05-06"), stay("2027-05-05", "2027-05-08"))
        val free = may.freeStays(booked, today)
        assertEquals(
            listOf(stay("2027-05-01", "2027-05-03"), stay("2027-05-08", "2027-05-20"), stay("2027-05-25", "2027-06-01")),
            free,
        )
    }

    @Test
    fun bookingsAtTheEdges_leaveTheMiddle() {
        val booked = listOf(stay("2027-04-28", "2027-05-04"), stay("2027-05-28", "2027-06-03"))
        assertEquals(listOf(stay("2027-05-04", "2027-05-28")), may.freeStays(booked, today))
    }

    @Test
    fun fullyBooked_nothingIsFree() {
        assertTrue(may.freeStays(listOf(stay("2027-05-01", "2027-06-01")), today).isEmpty())
    }

    @Test
    fun pastNights_arentFree() {
        val midMay = LocalDate.of(2027, 5, 15)
        assertEquals(listOf(stay("2027-05-15", "2027-06-01")), may.freeStays(emptyList(), midMay))
        assertTrue(may.freeStays(emptyList(), LocalDate.of(2027, 6, 1)).isEmpty())
    }

    @Test
    fun savedBookings_countToo() {
        val schedule = AvailabilitySchedule(listOf("2027-05-01", "2027-06-01"), listOf(stay("2027-05-01", "2027-05-31")))
        assertEquals(listOf(stay("2027-05-31", "2027-06-01")), schedule.freeStays(emptyList(), today))
    }

    @Test
    fun badBookingDates_areSkipped() {
        assertEquals(listOf(stay("2027-05-01", "2027-06-01")), may.freeStays(listOf(stay("", "nope")), today))
    }

    @Test
    fun canBook_staysInsideAGap() {
        val booked = listOf(stay("2027-05-10", "2027-05-12"))
        assertTrue(may.canBook(stay("2027-05-01", "2027-05-10"), booked, today))
        // checking in the day the other stay checks out is fine
        assertTrue(may.canBook(stay("2027-05-12", "2027-05-14"), booked, today))
        assertTrue(may.canBook(stay("2027-05-30", "2027-06-01"), booked, today))
    }

    @Test
    fun canBook_rejectsStaysThatDontFit() {
        val booked = listOf(stay("2027-05-10", "2027-05-12"))
        // overlaps the booking
        assertFalse(may.canBook(stay("2027-05-09", "2027-05-11"), booked, today))
        // spans the booking
        assertFalse(may.canBook(stay("2027-05-08", "2027-05-14"), booked, today))
        // goes past the open period
        assertFalse(may.canBook(stay("2027-05-30", "2027-06-02"), booked, today))
        // before the open period
        assertFalse(may.canBook(stay("2027-04-29", "2027-05-02"), booked, today))
        // leaves before arriving
        assertFalse(may.canBook(stay("2027-05-03", "2027-05-01"), booked, today))
        assertFalse(may.canBook(stay("not a date", "2027-05-01"), booked, today))
    }

    // the calendar lets a stay start or end on any of these days
    @Test
    fun checkInAndOutDays_includeTheLastCheckOut() {
        val days = stay("2027-05-30", "2027-06-02").checkInAndOutDays()
        assertEquals(
            listOf(LocalDate.of(2027, 5, 30), LocalDate.of(2027, 5, 31), LocalDate.of(2027, 6, 1), LocalDate.of(2027, 6, 2)),
            days,
        )
    }

    @Test
    fun checkInAndOutDays_emptyForBadRanges() {
        assertTrue(stay("", "2027-05-02").checkInAndOutDays().isEmpty())
        assertTrue(stay("2027-05-02", "2027-05-02").checkInAndOutDays().isEmpty())
        assertTrue(stay("2027-05-03", "2027-05-02").checkInAndOutDays().isEmpty())
    }
}
