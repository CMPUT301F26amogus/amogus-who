package com.example.code.controllers

import Model
import com.example.code.model.Campsite
import com.example.code.model.EntrantReservation
import com.example.code.model.Lottery
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class ReserveCampsiteController(
    private val model: Model
) {
    /**
     * checks what the entrant typed and books the campsite (US 01.05.01)
     *
     * the date fields give 8 digits like 05012027 so those get turned into 2027-05-01
     * the model sends the confirmation notification once its saved
     *
     * @param campsite the campsite being reserved
     * @param arrival what was typed in the arrival field
     * @param departure what was typed in the departure field
     * @param guests what was typed in the guests field
     * @param rulesAccepted true if the entrant agreed to the campsite rules
     * @param today the date to compare against so tests can pick one
     * @param lottery the lottery they won to get this campsite or null to book without one
     * @param onResult the saved reservation with its campsite or a failure saying whats wrong
     */
    fun makeReservation(
        campsite: Campsite?,
        arrival: String,
        departure: String,
        guests: String,
        rulesAccepted: Boolean = false,
        today: LocalDate = LocalDate.now(),
        lottery: Lottery? = null,
        onResult: (Result<EntrantReservation>) -> Unit,
    ) {
        val error = validate(campsite, arrival, departure, guests, today, rulesAccepted)
        if (error != null || campsite == null) {
            onResult(Result.failure(IllegalArgumentException(error ?: "No campsite picked")))
            return
        }
        val checkIn = parseDate(arrival)!!.toString()
        val checkOut = parseDate(departure)!!.toString()

        // a lottery win has to fit the available dates and can only be used once (US 01.04.01 01.04.02)
        // the model checks all that so the controller just hands it over
        if (lottery != null) {
            model.reserveLotteryWin(
                lottery = lottery,
                campsite = campsite,
                checkIn = checkIn,
                checkOut = checkOut,
                guests = guests.trim().toInt(),
                rulesAccepted = rulesAccepted,
                today = today,
            ) { result ->
                onResult(result.map { EntrantReservation(it, campsite) })
            }
            return
        }

        // someone else might have booked these dates first
        // not a transaction so two people at the exact same time could still both get it
        model.getBookedDates(campsite.id) { booked ->
            // dates like 2027-05-01 sort right as text and back to back stays dont count
            if (booked.any { it.start < checkOut && checkIn < it.end }) {
                onResult(Result.failure(IllegalArgumentException("Those dates are already booked")))
                return@getBookedDates
            }
            model.makeReservation(
                checkIn = checkIn,
                checkOut = checkOut,
                guests = guests.trim().toInt(),
                campsite = campsite,
                rulesAccepted = rulesAccepted,
            ) { result ->
                onResult(result.map { EntrantReservation(it, campsite) })
            }
        }
    }

    /**
     * books the days a lottery winner picked on the calendar (US 01.04.01 01.04.02)
     *
     * the calendar only lets them tap free days but a stay can still run over a booked night
     * in between so the model checks the whole stay
     *
     * @param campsite the campsite being reserved
     * @param lottery the lottery they won to get it
     * @param checkIn the first day they picked or null if they havent
     * @param checkOut the second day they picked or null if they havent
     * @param guests what was typed in the guests field
     * @param rulesAccepted true if the entrant agreed to the campsite rules
     * @param today the date to compare against so tests can pick one
     * @param onResult the saved reservation with its campsite or a failure saying whats wrong
     */
    fun makeLotteryReservation(
        campsite: Campsite?,
        lottery: Lottery?,
        checkIn: LocalDate?,
        checkOut: LocalDate?,
        guests: String,
        rulesAccepted: Boolean = false,
        today: LocalDate = LocalDate.now(),
        onResult: (Result<EntrantReservation>) -> Unit,
    ) {
        val missing = when {
            lottery == null -> "There's no lottery win to reserve from"
            checkIn == null -> "Pick your check in day on the calendar"
            checkOut == null -> "Pick your check out day on the calendar"
            else -> null
        }
        if (missing != null) {
            onResult(Result.failure(IllegalArgumentException(missing)))
            return
        }
        makeReservation(
            campsite = campsite,
            arrival = checkIn.toString(),
            departure = checkOut.toString(),
            guests = guests,
            rulesAccepted = rulesAccepted,
            today = today,
            lottery = lottery,
            onResult = onResult,
        )
    }

    /**
     * says no to a lottery win eg none of the available dates work (US 01.04.03)
     *
     * @param lottery the lottery they won
     * @param onResult success once its declined or a failure saying why
     */
    fun declineLotteryWin(lottery: Lottery?, onResult: (Result<Unit>) -> Unit) {
        if (lottery == null) {
            onResult(Result.failure(IllegalArgumentException("There's no lottery win to decline")))
            return
        }
        model.declineLotteryWin(lottery, onResult)
    }

    /**
     * the csv text for a reservation so the entrant can download it (US 01.05.02)
     *
     * @param reservation the reservation with its campsite
     * @return the csv file contents
     */
    fun reservationCsv(reservation: EntrantReservation): String = reservation.toCsv()

    /**
     * checks the typed fields without saving anything
     *
     * if the campsite has rules the entrant has to tick that they agree to them (US 02.11.01)
     *
     * @param rulesAccepted true if the entrant ticked the rules box
     * @return what to tell the entrant or null if its all fine
     */
    fun validate(
        campsite: Campsite?,
        arrival: String,
        departure: String,
        guests: String,
        today: LocalDate = LocalDate.now(),
        rulesAccepted: Boolean = false,
    ): String? {
        if (campsite == null || campsite.id.isBlank()) return "No campsite picked"
        val checkIn = parseDate(arrival) ?: return "Arrival date should look like MM/DD/YYYY"
        val checkOut = parseDate(departure) ?: return "Departure date should look like MM/DD/YYYY"
        if (checkIn.isBefore(today)) return "Arrival date can't be in the past"
        if (!checkOut.isAfter(checkIn)) return "Departure has to be after arrival"
        val count = guests.trim().toIntOrNull() ?: return "Number of guests has to be a number"
        if (count < 1) return "You need at least 1 guest"
        if (count > campsite.maxOccupancy) return "This campsite fits at most ${campsite.maxOccupancy} guests"
        if (campsite.hasRules() && !rulesAccepted) return "You have to agree to the campsite rules first"
        return null
    }

    // reads 05012027 from the date field or 2027-05-01 and gives null if its not a real date
    private fun parseDate(text: String): LocalDate? {
        val trimmed = text.trim()
        val pattern = if (trimmed.contains("-")) "uuuu-MM-dd" else "MMdduuuu"
        val formatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT)
        return runCatching { LocalDate.parse(trimmed.replace("/", ""), formatter) }.getOrNull()
    }
}
