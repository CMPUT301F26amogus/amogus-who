package com.example.code.Model

import java.time.LocalDate
import java.util.TreeSet

/**
 * which nights a campsite can be booked and which are already taken
 *
 * the owner opens nights and entrants book ranges of them first come first served
 * stored as single nights so overlapping or back to back open ranges just work
 *
 * @param openNights saved open nights to load
 * @param bookings saved bookings to load and they get the same checks as [book]

 */
//TODO: This whole class needs fixes, changing it from localDates messed some stuff up :(
class AvailabilitySchedule(
    val openNights: Collection<String> = emptyList(),
    val bookings: Collection<DateRange> = emptyList(),
) {





}
