package com.example.code.Model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ReservationTest {

    private fun reservation(checkIn: String = "2027-05-01", checkOut: String = "2027-05-03") = Reservation(
        id = "res-1",
        campsiteId = "site-1",
        lotteryId = "lottery-1",
        entrantId = "user-1",
        checkIn = checkIn,
        checkOut = checkOut,
        guests = 4,
    )

    // firestore needs to build one with no arguments when loading
    @Test
    fun noArguments_usesDefaults() {
        val r = Reservation()
        assertEquals("", r.id)
        assertEquals("", r.campsiteId)
        assertEquals(0, r.guests)
        assertEquals(ReservationStatus.ACTIVE, r.status)
        assertFalse(r.rulesAccepted)
    }

    @Test
    fun newReservation_isActive() {
        assertEquals(ReservationStatus.ACTIVE, reservation().status)
    }

    @Test
    fun stay_readsCheckInAndCheckOut() {
        val stay = reservation().stay()
        assertEquals(LocalDate.of(2027, 5, 1), stay.start)
        assertEquals(LocalDate.of(2027, 5, 3), stay.end)
        assertEquals(2L, stay.nights)
    }

    @Test
    fun stay_followsChangedDates() {
        val r = reservation()
        r.checkOut = "2027-05-06"
        assertEquals(5L, r.stay().nights)
    }

    @Test(expected = DateTimeParseException::class)
    fun stay_wrongDateFormatThrows() {
        reservation(checkIn = "05/01/2027").stay()
    }

    @Test(expected = DateTimeParseException::class)
    fun stay_blankDatesThrow() {
        Reservation().stay()
    }

    @Test(expected = IllegalArgumentException::class)
    fun stay_checkOutBeforeCheckInThrows() {
        reservation(checkIn = "2027-05-03", checkOut = "2027-05-01").stay()
    }

    @Test(expected = IllegalArgumentException::class)
    fun stay_sameDayCheckOutThrows() {
        reservation(checkIn = "2027-05-01", checkOut = "2027-05-01").stay()
    }
}
