package com.example.code.controllers

import Model
import com.example.code.model.Campsite
import com.example.code.model.EntrantReservation
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
     * @param onResult the saved reservation with its campsite or a failure saying whats wrong
     */
    fun makeReservation(
        campsite: Campsite?,
        arrival: String,
        departure: String,
        guests: String,
        rulesAccepted: Boolean = false,
        today: LocalDate = LocalDate.now(),
        onResult: (Result<EntrantReservation>) -> Unit,
    ) {
        val error = validate(campsite, arrival, departure, guests, today)
        if (error != null || campsite == null) {
            onResult(Result.failure(IllegalArgumentException(error ?: "No campsite picked")))
            return
        }
        val checkIn = parseDate(arrival)!!.toString()
        val checkOut = parseDate(departure)!!.toString()

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
     * the csv text for a reservation so the entrant can download it (US 01.05.02)
     *
     * @param reservation the reservation with its campsite
     * @return the csv file contents
     */
    fun reservationCsv(reservation: EntrantReservation): String = reservation.toCsv()

    /**
     * checks the typed fields without saving anything
     *
     * @return what to tell the entrant or null if its all fine
     */
    fun validate(
        campsite: Campsite?,
        arrival: String,
        departure: String,
        guests: String,
        today: LocalDate = LocalDate.now(),
    ): String? {
        if (campsite == null || campsite.id.isBlank()) return "No campsite picked"
        val checkIn = parseDate(arrival) ?: return "Arrival date should look like MM/DD/YYYY"
        val checkOut = parseDate(departure) ?: return "Departure date should look like MM/DD/YYYY"
        if (checkIn.isBefore(today)) return "Arrival date can't be in the past"
        if (!checkOut.isAfter(checkIn)) return "Departure has to be after arrival"
        val count = guests.trim().toIntOrNull() ?: return "Number of guests has to be a number"
        if (count < 1) return "You need at least 1 guest"
        if (count > campsite.maxOccupancy) return "This campsite fits at most ${campsite.maxOccupancy} guests"
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
