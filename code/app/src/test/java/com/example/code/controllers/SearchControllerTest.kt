package com.example.code.controllers

import Model
import com.example.code.model.AvailabilitySchedule
import com.example.code.model.Campsite
import com.example.code.model.DateInput
import com.example.code.model.DateRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Tests the search filter rules (US 01.01.02). The Firestore part isn't tested here.
 */
class SearchControllerTest {
    private val controller = SearchController(Model())
    private val dates = DateInput()

    // open June 1 to June 30 2027, with June 10-12 already booked
    private val june = Campsite(
        name = "Pine Lake",
        location = "Elk Island",
        description = "Quiet lakeside site",
        availability = AvailabilitySchedule(
            openNights = listOf("2027-06-01", "2027-06-30"),
            bookings = listOf(DateRange("2027-06-10", "2027-06-12")),
        ),
    )
    private val noDates = Campsite(name = "No Dates")

    @Test
    fun blankNameMatchesEverything() {
        assertTrue(controller.matchesName(june, ""))
    }

    @Test
    fun nameMatchesNameLocationOrDescription() {
        assertTrue(controller.matchesName(june, "pine"))
        assertTrue(controller.matchesName(june, "elk"))
        assertTrue(controller.matchesName(june, "lakeside"))
        assertFalse(controller.matchesName(june, "banff"))
    }

    @Test
    fun noDatesSearchedShowsEveryCampsite() {
        assertTrue(controller.isOpenFor(june, null, null))
        assertTrue(controller.isOpenFor(noDates, null, null))
    }

    @Test
    fun stayInsideBookingPeriodIsOpen() {
        assertTrue(controller.isOpenFor(june, LocalDate.of(2027, 6, 2), LocalDate.of(2027, 6, 5)))
    }

    @Test
    fun stayOutsideBookingPeriodIsNotOpen() {
        assertFalse(controller.isOpenFor(june, LocalDate.of(2027, 5, 28), LocalDate.of(2027, 6, 3)))
        assertFalse(controller.isOpenFor(june, LocalDate.of(2027, 6, 28), LocalDate.of(2027, 7, 2)))
    }

    @Test
    fun stayOverlappingABookingIsNotOpen() {
        assertFalse(controller.isOpenFor(june, LocalDate.of(2027, 6, 9), LocalDate.of(2027, 6, 11)))
    }

    @Test
    fun stayRightAfterABookingIsOpen() {
        // the booking ends the morning of the 12th, so arriving the 12th is fine
        assertTrue(controller.isOpenFor(june, LocalDate.of(2027, 6, 12), LocalDate.of(2027, 6, 14)))
    }

    @Test
    fun campsiteWithoutBookingPeriodIsHiddenWhenDatesSearched() {
        assertFalse(controller.isOpenFor(noDates, LocalDate.of(2027, 6, 2), LocalDate.of(2027, 6, 5)))
    }

    @Test
    fun readsTypedDates() {
        assertEquals(LocalDate.of(2027, 6, 1), dates.toLocalDate("06/01/2027"))
        assertEquals(LocalDate.of(2027, 6, 1), dates.toLocalDate("06012027"))
        assertEquals(LocalDate.of(2027, 6, 1), dates.toLocalDate("06/01/27"))
        assertEquals(LocalDate.of(2027, 6, 1), dates.toLocalDate("2027-06-01"))
    }

    @Test
    fun badDatesAreNull() {
        assertNull(dates.toLocalDate(""))
        assertNull(dates.toLocalDate("13/01/2027"))
        assertNull(dates.toLocalDate("02/30/2027"))
        assertNull(dates.toLocalDate("hello"))
    }

    @Test
    fun savesDatesAsYearMonthDay() {
        assertEquals("2027-06-01", dates.toSaved("06/01/2027"))
        assertEquals("", dates.toSaved(""))
    }
}
