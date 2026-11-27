package com.example.code.controllers

import Model
import com.example.code.model.Campsite
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
    ) = controller.validate(site, arrival, departure, guests, today)

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
}
