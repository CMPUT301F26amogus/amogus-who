package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.Routes
import com.example.code.model.Campsite
import com.example.code.model.DateInput
import java.time.LocalDate

class SearchController(
    private val model : Model
) {
    private val dateInput = DateInput()

    /**
     * Searches for campsites, filtered by location, number of guests and dates (US 01.01.02)
     *
     * every filter is optional, a blank one doesn't filter anything
     *
     * @param name campsite name, location or description to look for
     * @param location part of the campsite's location, eg "Edmonton"
     * @param guests how many people are staying
     * @param arrival first night, typed as mm/dd/yyyy
     * @param departure the day they leave, typed as mm/dd/yyyy
     * @param onError called with a message to show if a filter isn't valid
     */
    public fun search(
        name: String,
        navController: NavController,
        location: String = "",
        guests: String = "",
        arrival: String = "",
        departure: String = "",
        onError: (String) -> Unit = {},
    ){
        // blank means any number of guests, and every campsite fits at least 1
        val guestCount = if (guests.isBlank()) 1 else guests.trim().toIntOrNull()
        if (guestCount == null || guestCount < 1) {
            onError("Number of guests must be a number of at least 1")
            return
        }

        // dates are both blank (no date filter) or both real dates
        val arrivalDate = dateInput.toLocalDate(arrival)
        val departureDate = dateInput.toLocalDate(departure)
        val noDates = arrival.isBlank() && departure.isBlank()
        if (!noDates) {
            if (arrivalDate == null || departureDate == null) {
                onError("Please enter both dates as mm/dd/yyyy")
                return
            }
            if (!departureDate.isAfter(arrivalDate)) {
                onError("Departure must be after arrival")
                return
            }
        }

        // filterCampsite checks location and guests, then we check name and dates
        val filter = Campsite(location = location.trim(), maxOccupancy = guestCount)
        // filterCampsite is async so the results are only here inside the callback,
        // save them and navigate in there or the results screen opens with an old list
        model.filterCampsite(filter) { filtered ->
            val results = filtered.filter {
                matchesName(it, name) && isOpenFor(it, arrivalDate, departureDate)
            }
            model.settCampsiteList(ArrayList(results))
            navController.navigate(Routes.SEARCH_RESULTS)
        }
    }

    /**
     * same rule as searchCampsite: blank matches everything, otherwise the
     * name, location or description has to contain it
     */
    fun matchesName(campsite: Campsite, name: String): Boolean {
        val query = name.trim()
        if (query.isEmpty()) return true
        return campsite.name.contains(query, ignoreCase = true) ||
                campsite.location.contains(query, ignoreCase = true) ||
                campsite.description.contains(query, ignoreCase = true)
    }

    /**
     * true if the campsite can be booked for the whole stay
     *
     * the owner's booking period is saved as availability.openNights = [first night, last day],
     * the stay has to fit inside it and can't overlap a booking.
     * no dates searched means every campsite is fine, but if dates are searched a campsite
     * without a booking period doesn't show, since we can't tell if it's open
     */
    fun isOpenFor(campsite: Campsite, arrival: LocalDate?, departure: LocalDate?): Boolean {
        if (arrival == null || departure == null) return true

        val open = campsite.availability.openNights
        val periodStart = dateInput.toLocalDate(open.getOrNull(0) ?: "")
        val periodEnd = dateInput.toLocalDate(open.getOrNull(1) ?: "")
        if (periodStart == null || periodEnd == null) return false
        if (arrival.isBefore(periodStart) || departure.isAfter(periodEnd)) return false

        for (booking in campsite.availability.bookings) {
            val bookedFrom = dateInput.toLocalDate(booking.start) ?: continue
            val bookedTo = dateInput.toLocalDate(booking.end) ?: continue
            // stays overlap if each starts before the other ends
            if (arrival.isBefore(bookedTo) && bookedFrom.isBefore(departure)) return false
        }
        return true
    }
}
