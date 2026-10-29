package com.campsitereservation.app.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

// start is check in and end is check out so may 1 to may 3 is 2 nights
// end is exclusive so back to back stays dont overlap
data class DateRange(val start: LocalDate, val end: LocalDate) {

    init {
        require(end.isAfter(start)) { "End date ($end) must be after start date ($start)" }
    }

    val nights: Long
        get() = ChronoUnit.DAYS.between(start, end)

    operator fun contains(date: LocalDate): Boolean =
        !date.isBefore(start) && date.isBefore(end)

    // true if the other range fits fully inside this one
    operator fun contains(other: DateRange): Boolean =
        !other.start.isBefore(start) && !other.end.isAfter(end)

    // true if they share at least one night
    fun overlaps(other: DateRange): Boolean =
        start.isBefore(other.end) && other.start.isBefore(end)

    // every night in the range not including the end date
    fun dates(): List<LocalDate> =
        (0 until nights).map { start.plusDays(it) }
}
