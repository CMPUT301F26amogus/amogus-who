package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ReservationTest {

    private fun reservation(checkIn: String = "2027-05-01", checkOut: String = "2027-05-03") = Reservation(
        id = "res-1",
        campsiteId = "site-1",
        entrantId = "user-1",
        checkIn = checkIn,
        checkOut = checkOut,
        numGuests = 4,
        lotteryId = "lottery-1",
    )

    // firestore needs to build one with no arguments when loading
    @Test
    fun noArguments_usesDefaults() {
        val r = Reservation()
        assertEquals("", r.id)
        assertEquals("", r.campsiteId)
        assertEquals(1, r.numGuests)
        assertEquals(ReservationStatus.ACTIVE, r.status)
        assertFalse(r.rulesAccepted)
    }

    @Test
    fun newReservation_isActive() {
        assertEquals(ReservationStatus.ACTIVE, reservation().status)
    }

    @Test
    fun nights_countsFromCheckInToCheckOut() {
        assertEquals(2L, reservation().nights())
    }

    @Test
    fun nights_followsChangedDates() {
        val r = reservation()
        r.checkOut = "2027-05-06"
        assertEquals(5L, r.nights())
    }

    @Test(expected = DateTimeParseException::class)
    fun nights_wrongDateFormatThrows() {
        reservation(checkIn = "05/01/2027").nights()
    }

    @Test(expected = DateTimeParseException::class)
    fun nights_blankDatesThrow() {
        Reservation().nights()
    }

    // US 02.07.01 owner sees active reservations

    @Test
    fun isActive_upcomingStayIsActive() {
        assertTrue(reservation().isActive(today = LocalDate.of(2027, 4, 1)))
    }

    @Test
    fun isActive_stayHappeningNowIsActive() {
        assertTrue(reservation().isActive(today = LocalDate.of(2027, 5, 2)))
    }

    // they havent checked out yet on check out day
    @Test
    fun isActive_checkOutDayIsActive() {
        assertTrue(reservation().isActive(today = LocalDate.of(2027, 5, 3)))
    }

    @Test
    fun isActive_finishedStayIsNotActive() {
        assertFalse(reservation().isActive(today = LocalDate.of(2027, 5, 4)))
    }

    @Test
    fun isActive_cancelledIsNotActive() {
        val r = reservation()
        r.status = ReservationStatus.CANCELLED
        assertFalse(r.isActive(today = LocalDate.of(2027, 4, 1)))
    }

    @Test
    fun isActive_rejectedIsNotActive() {
        val r = reservation()
        r.status = ReservationStatus.REJECTED
        assertFalse(r.isActive(today = LocalDate.of(2027, 4, 1)))
    }

    @Test
    fun isActive_pastIsNotActive() {
        val r = reservation()
        r.status = ReservationStatus.PAST
        assertFalse(r.isActive(today = LocalDate.of(2027, 4, 1)))
    }

    @Test(expected = DateTimeParseException::class)
    fun isActive_blankCheckOutThrows() {
        Reservation().isActive()
    }

    // US 02.07.02 owner sees past reservations

    @Test
    fun isPast_upcomingStayIsNotPast() {
        assertFalse(reservation().isPast(today = LocalDate.of(2027, 4, 1)))
    }

    @Test
    fun isPast_stayHappeningNowIsNotPast() {
        assertFalse(reservation().isPast(today = LocalDate.of(2027, 5, 2)))
    }

    // they havent checked out yet on check out day
    @Test
    fun isPast_checkOutDayIsNotPast() {
        assertFalse(reservation().isPast(today = LocalDate.of(2027, 5, 3)))
    }

    @Test
    fun isPast_dayAfterCheckOutIsPast() {
        assertTrue(reservation().isPast(today = LocalDate.of(2027, 5, 4)))
    }

    @Test
    fun isPast_markedPastIsPastEvenBeforeCheckOut() {
        val r = reservation()
        r.status = ReservationStatus.PAST
        assertTrue(r.isPast(today = LocalDate.of(2027, 4, 1)))
    }

    // past ones dont need real dates since the status already says its over
    @Test
    fun isPast_markedPastWithBlankDatesIsPast() {
        val r = Reservation()
        r.status = ReservationStatus.PAST
        assertTrue(r.isPast())
    }

    @Test
    fun isPast_cancelledFinishedStayIsNotPast() {
        val r = reservation()
        r.status = ReservationStatus.CANCELLED
        assertFalse(r.isPast(today = LocalDate.of(2027, 5, 4)))
    }

    @Test
    fun isPast_rejectedFinishedStayIsNotPast() {
        val r = reservation()
        r.status = ReservationStatus.REJECTED
        assertFalse(r.isPast(today = LocalDate.of(2027, 5, 4)))
    }

    // an active stay is always exactly one of active or past
    @Test
    fun isPast_neverOverlapsIsActive() {
        val r = reservation()
        for (day in 1..10) {
            val today = LocalDate.of(2027, 4, 28).plusDays(day.toLong())
            assertTrue(r.isActive(today) != r.isPast(today))
        }
    }

    @Test(expected = DateTimeParseException::class)
    fun isPast_activeWithBlankCheckOutThrows() {
        Reservation().isPast()
    }

    // US 02.07.03 owner sees cancelled reservations

    @Test
    fun isCancelled_newReservationIsNotCancelled() {
        assertFalse(reservation().isCancelled())
    }

    @Test
    fun isCancelled_cancelledIsCancelled() {
        val r = reservation()
        r.status = ReservationStatus.CANCELLED
        assertTrue(r.isCancelled())
    }

    // the owner rejecting it is a different list
    @Test
    fun isCancelled_rejectedIsNotCancelled() {
        val r = reservation()
        r.status = ReservationStatus.REJECTED
        assertFalse(r.isCancelled())
    }

    @Test
    fun isCancelled_pastIsNotCancelled() {
        val r = reservation()
        r.status = ReservationStatus.PAST
        assertFalse(r.isCancelled())
    }

    // doesnt look at dates so bad ones dont matter
    @Test
    fun isCancelled_blankDatesDontThrow() {
        val r = Reservation()
        r.status = ReservationStatus.CANCELLED
        assertTrue(r.isCancelled())
    }
}
