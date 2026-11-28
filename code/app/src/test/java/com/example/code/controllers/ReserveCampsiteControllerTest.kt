package com.example.code.controllers

import Model
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

// US 01.05.01 the reserve screen only books when what was typed makes sense
class ReserveCampsiteControllerTest {

    // the model is lazy about firebase so this doesnt need it running
    private val controller = ReserveCampsiteController(Model())
    private val today = LocalDate.of(2027, 4, 1)
    private val campsite = Campsite(id = "camp-1", ownerId = "owner-1", name = "Pine Ridge", maxOccupancy = 4)

    private fun validate(
        arrival: String = "05012027",
        departure: String = "05032027",
        guests: String = "2",
        site: Campsite? = campsite,
        rulesAccepted: Boolean = false,
    ) = controller.validate(site, arrival, departure, guests, today, rulesAccepted)

    // US 02.11.01 a campsite whose owner wrote rules
    private val rulesCampsite = Campsite(
        id = "camp-2", ownerId = "owner-1", name = "Aspen Flats", maxOccupancy = 4,
        rulesAndPolicy = "No campfires after 10pm",
    )

    @Test
    fun validInput_hasNoError() {
        assertNull(validate())
    }

    @Test
    fun isoDates_areAccepted() {
        assertNull(validate(arrival = "2027-05-01", departure = "2027-05-03"))
    }

    @Test
    fun noCampsite_isAnError() {
        assertEquals("No campsite picked", validate(site = null))
    }

    @Test
    fun notARealDate_isAnError() {
        assertEquals("Arrival date should look like MM/DD/YYYY", validate(arrival = "13452027"))
        assertEquals("Departure date should look like MM/DD/YYYY", validate(departure = ""))
    }

    @Test
    fun arrivalInThePast_isAnError() {
        assertEquals("Arrival date can't be in the past", validate(arrival = "03012027"))
    }

    @Test
    fun departureNotAfterArrival_isAnError() {
        assertEquals("Departure has to be after arrival", validate(departure = "05012027"))
    }

    @Test
    fun badGuestCounts_areErrors() {
        assertEquals("Number of guests has to be a number", validate(guests = "two"))
        assertEquals("You need at least 1 guest", validate(guests = "0"))
        assertEquals("This campsite fits at most 4 guests", validate(guests = "5"))
    }

    @Test
    fun invalidInput_failsWithoutTouchingFirebase() {
        var message: String? = null
        controller.makeReservation(campsite, "05012027", "04302027", "2", today = today) {
            message = it.exceptionOrNull()?.message
        }
        assertEquals("Departure has to be after arrival", message)
    }

    // US 02.11.01
    @Test
    fun rulesNotAccepted_isError() {
        assertEquals("You have to agree to the campsite rules first", validate(site = rulesCampsite))
    }

    // US 02.11.01
    @Test
    fun rulesAccepted_hasNoError() {
        assertNull(validate(site = rulesCampsite, rulesAccepted = true))
    }

    // US 02.11.01 blank rules have nothing to agree to
    @Test
    fun noRules_doesntNeedAccepting() {
        assertNull(validate(site = Campsite(id = "camp-3", ownerId = "owner-1", maxOccupancy = 4, rulesAndPolicy = "   ")))
    }

    // US 02.11.01
    @Test
    fun rulesNotAccepted_failsWithoutTouchingFirebase() {
        var message: String? = null
        controller.makeReservation(rulesCampsite, "05012027", "05032027", "2", today = today) {
            message = it.exceptionOrNull()?.message
        }
        assertEquals("You have to agree to the campsite rules first", message)
    }

    // US 01.04.01 lottery winners pick their days on the calendar and get told if one is missing
    // see [21] in Citation.md
    private fun lotteryReserve(lottery: Lottery?, checkIn: LocalDate?, checkOut: LocalDate?): String? {
        var message: String? = null
        controller.makeLotteryReservation(campsite, lottery, checkIn, checkOut, "2", today = today) {
            message = it.exceptionOrNull()?.message
        }
        return message
    }

    // see [21] in Citation.md
    @Test
    fun calendar_needsBothDaysPicked() {
        val lottery = Lottery(id = "lottery-1", campsiteIds = listOf(campsite.id))
        val may1 = LocalDate.of(2027, 5, 1)
        assertEquals("Pick your check in day on the calendar", lotteryReserve(lottery, null, null))
        assertEquals("Pick your check out day on the calendar", lotteryReserve(lottery, may1, null))
        assertEquals("There's no lottery win to reserve from", lotteryReserve(null, may1, may1.plusDays(2)))
    }

    // see [21] in Citation.md
    @Test
    fun calendar_daysStillGetChecked() {
        val lottery = Lottery(id = "lottery-1", campsiteIds = listOf(campsite.id))
        val may3 = LocalDate.of(2027, 5, 3)
        // picked the same day twice so its not a real stay
        assertEquals("Departure has to be after arrival", lotteryReserve(lottery, may3, may3))
    }
}
