package com.example.code.model

import java.time.LocalDate

/**
 * which nights a campsite can be booked and which are already taken
 *
 * the owner opens nights and entrants book ranges of them first come first served
 *
 * the owners booking period is saved as openNights = [first night, last check out day]
 * which is what the add campsite screen saves and search reads
 *
 * @param openNights the open period as [first night, last check out day] like 2027-05-01
 * @param bookings saved bookings to load
 */
//TODO: This whole class needs fixes, changing it from localDates messed some stuff up :(
class AvailabilitySchedule(
    val openNights: List<String> = emptyList(), // a list since firestore cant load collections
    val bookings: List<DateRange> = emptyList(),
) {

    // see [19] in Citation.md
    /**
     * the stays that can still be booked (US 01.04.01)
     *
     * starts from the open period then cuts out the past and every booked stay
     * back to back stays dont overlap so a stay can check in the day another checks out
     * has params so firestore doesnt try to save it
     *
     * @param booked stays already booked eg the active reservations at the campsite
     * @param today nights before this cant be booked so tests can pick one
     * @return each free stretch as check in to latest check out sorted by date
     * or empty if the owner never set an open period or its all taken
     */
    fun freeStays(booked: List<DateRange>, today: LocalDate = LocalDate.now()): List<DateRange> {
        val dateInput = DateInput()
        val openStart = dateInput.toLocalDate(openNights.getOrNull(0) ?: "") ?: return emptyList()
        val openEnd = dateInput.toLocalDate(openNights.getOrNull(1) ?: "") ?: return emptyList()

        // the booked stays as dates sorted by check in, skipping any that dont parse
        val taken = (bookings + booked).mapNotNull { stay ->
            val start = dateInput.toLocalDate(stay.start) ?: return@mapNotNull null
            val end = dateInput.toLocalDate(stay.end) ?: return@mapNotNull null
            if (end.isAfter(start)) start to end else null
        }.sortedBy { it.first }

        // walk through the bookings and keep the gaps between them
        val free = mutableListOf<DateRange>()
        var cursor = maxOf(openStart, today)
        for ((start, end) in taken) {
            if (!start.isAfter(cursor)) {
                if (end.isAfter(cursor)) cursor = end
                continue
            }
            if (!cursor.isBefore(openEnd)) break
            free.add(DateRange(cursor.toString(), minOf(start, openEnd).toString()))
            if (end.isAfter(cursor)) cursor = end
        }
        if (cursor.isBefore(openEnd)) free.add(DateRange(cursor.toString(), openEnd.toString()))
        return free
    }

    // see [19] in Citation.md
    /**
     * checks if a whole stay fits inside one of the [freeStays]
     *
     * @param stay check in to check out like 2027-05-01 to 2027-05-03
     * @param booked stays already booked
     * @param today nights before this cant be booked
     * @return true if every night of the stay is free and false if any isnt or a date doesnt parse
     */
    fun canBook(stay: DateRange, booked: List<DateRange>, today: LocalDate = LocalDate.now()): Boolean {
        val dateInput = DateInput()
        val checkIn = dateInput.toLocalDate(stay.start) ?: return false
        val checkOut = dateInput.toLocalDate(stay.end) ?: return false
        if (!checkOut.isAfter(checkIn)) return false
        return freeStays(booked, today).any { free ->
            !checkIn.isBefore(LocalDate.parse(free.start)) && !checkOut.isAfter(LocalDate.parse(free.end))
        }
    }
}
