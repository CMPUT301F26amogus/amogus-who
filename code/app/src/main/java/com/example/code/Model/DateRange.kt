package com.example.code.Model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * a span of dates where start counts and end doesn't
 *
 * for a stay start is check in and end is check out so may 1 to may 3 is 2 nights
 * since end is exclusive back to back stays dont overlap
 *
 * @property start first date in the range or the check in date
 * @property end date right after the range ends or the check out date
 * @throws IllegalArgumentException if end isnt after start
 */
data class DateRange(val start: LocalDate, val end: LocalDate) {

    init {
        require(end.isAfter(start)) { "End date ($end) must be after start date ($start)" }
    }

    /** how many nights are in the range */
    val nights: Long
        get() = ChronoUnit.DAYS.between(start, end)

    /**
     * checks if a date is inside the range so you can write `date in range`
     *
     * @param date the date to check
     * @return true if date is on or after start and before end
     */
    operator fun contains(date: LocalDate): Boolean =
        !date.isBefore(start) && date.isBefore(end)

    /**
     * checks if another range fits fully inside this one
     *
     * @param other the range to check
     * @return true if every night of other is in this range
     */
    operator fun contains(other: DateRange): Boolean =
        !other.start.isBefore(start) && !other.end.isAfter(end)

    /**
     * checks if two ranges share at least one night
     *
     * @param other the range to compare with
     * @return true if they overlap and false for back to back stays
     */
    fun overlaps(other: DateRange): Boolean =
        start.isBefore(other.end) && other.start.isBefore(end)

    /**
     * lists every night in the range
     *
     * @return each date from start up to but not including end
     */
    fun dates(): List<LocalDate> =
        (0 until nights).map { start.plusDays(it) }
}
