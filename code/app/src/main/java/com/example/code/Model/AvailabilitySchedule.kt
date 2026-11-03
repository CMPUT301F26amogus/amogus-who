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
 * @throws IllegalArgumentException if a saved booking isnt open or overlaps another one
 */
class AvailabilitySchedule(
    openNights: Collection<LocalDate> = emptyList(),
    bookings: Collection<DateRange> = emptyList(),
) {
    private val _openNights = TreeSet<LocalDate>()
    private val _bookings = mutableListOf<DateRange>()

    init {
        _openNights.addAll(openNights)
        bookings.forEach { book(it) }
    }

    /** every open night in order including booked ones */
    val openNights: List<LocalDate>
        get() = _openNights.toList()

    /** current bookings sorted by start date for the owner */
    val bookings: List<DateRange>
        get() = _bookings.sortedBy { it.start }

    /**
     * opens every night in a range for booking
     *
     * @param range the nights to open
     */
    fun open(range: DateRange) {
        _openNights.addAll(range.dates())
    }

    /**
     * closes every night in a range
     *
     * @param range the nights to close
     * @throws IllegalArgumentException if any of those nights are booked
     */
    fun close(range: DateRange) {
        require(_bookings.none { it.overlaps(range) }) { "Cannot close $range because some of it is booked" }
        _openNights.removeAll(range.dates().toSet())
    }

    /**
     * checks if the owner opened every night in a range
     *
     * @param range the nights to check
     * @return true if all of them are open even if some are booked
     */
    fun isOpen(range: DateRange): Boolean = _openNights.containsAll(range.dates())

    /**
     * checks if someone booked a night
     *
     * @param night the night to check
     * @return true if a booking covers it
     */
    fun isBooked(night: LocalDate): Boolean = _bookings.any { night in it }

    /**
     * checks if a range can be booked right now
     *
     * @param range the stay to check
     * @return true if every night is open and none are booked
     */
    fun isAvailable(range: DateRange): Boolean =
        isOpen(range) && _bookings.none { it.overlaps(range) }

    /**
     * books a range
     *
     * @param range the stay to book
     * @throws IllegalArgumentException if it isnt available
     */
    fun book(range: DateRange) {
        require(isAvailable(range)) { "$range is not available" }
        _bookings.add(range)
    }

    /**
     * frees up a booking so the nights can be booked again
     *
     * used when a reservation is cancelled or rejected
     *
     * @param range the exact booking to remove
     * @throws IllegalArgumentException if that range isnt booked
     */
    fun release(range: DateRange) {
        require(_bookings.remove(range)) { "$range is not booked" }
    }

    /**
     * open nights nobody has booked yet for the calendar
     *
     * @return those nights in order
     */
    fun availableNights(): List<LocalDate> = _openNights.filterNot { isBooked(it) }

    /**
     * groups open nights into ranges eg may 1 2 3 becomes may 1 to may 4
     *
     * @return the ranges in order
     */
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
