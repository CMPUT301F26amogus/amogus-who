package com.example.code.Model

import java.time.LocalDate
import java.util.TreeSet

// the owner opens nights and entrants book ranges of them first come first served
// stored as single nights so overlapping or back to back open ranges just work
class AvailabilitySchedule(
    openNights: Collection<LocalDate> = emptyList(),
    bookings: Collection<DateRange> = emptyList(),
) {
    private val _openNights = TreeSet<LocalDate>()
    private val _bookings = mutableListOf<DateRange>()

    init {
        _openNights.addAll(openNights)
        // goes through book so saved data gets the same checks
        bookings.forEach { book(it) }
    }

    val openNights: List<LocalDate>
        get() = _openNights.toList()

    // currently booked dates for the owner
    val bookings: List<DateRange>
        get() = _bookings.sortedBy { it.start }

    fun open(range: DateRange) {
        _openNights.addAll(range.dates())
    }

    // cant close nights that someone already booked
    fun close(range: DateRange) {
        require(_bookings.none { it.overlaps(range) }) { "Cannot close $range because some of it is booked" }
        _openNights.removeAll(range.dates().toSet())
    }

    fun isOpen(range: DateRange): Boolean = _openNights.containsAll(range.dates())

    fun isBooked(night: LocalDate): Boolean = _bookings.any { night in it }

    fun isAvailable(range: DateRange): Boolean =
        isOpen(range) && _bookings.none { it.overlaps(range) }

    fun book(range: DateRange) {
        require(isAvailable(range)) { "$range is not available" }
        _bookings.add(range)
    }

    // for cancelled or rejected reservations so the nights free up again
    fun release(range: DateRange) {
        require(_bookings.remove(range)) { "$range is not booked" }
    }

    // open nights nobody has booked yet for the calendar
    fun availableNights(): List<LocalDate> = _openNights.filterNot { isBooked(it) }

    // open nights grouped into ranges eg may 1 2 3 becomes may 1 to may 4
    fun openRanges(): List<DateRange> {
        val ranges = mutableListOf<DateRange>()
        var start: LocalDate? = null
        var prev: LocalDate? = null
        for (night in _openNights) {
            if (start == null) {
                start = night
            } else if (night != prev!!.plusDays(1)) {
                ranges.add(DateRange(start, prev.plusDays(1)))
                start = night
            }
            prev = night
        }
        if (start != null) ranges.add(DateRange(start, prev!!.plusDays(1)))
        return ranges
    }
}
