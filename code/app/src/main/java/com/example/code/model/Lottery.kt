package com.example.code.model

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * a lottery an owner runs for a campsite
 *
 * entrants join the waitlist during the registration period
 * then winners get drawn and book their dates during the booking period
 *
 * every param has a default so firestore can load it
 * dates are saved as strings like 2027-05-01 since firestore cant store LocalDate,
 * they are turned into LocalDate only to be checked and compared
 * a blank date means not set yet, and a period needs both its start and its end or neither
 * end days are exclusive like [DateRange]
 *@property name name of the lottery
 * @property id unique id for this lottery
 * @property ownerId device id of the owner running it
 * @property campsiteIds ids of the campsites this lottery is for
 * @property numWinners how many entrants get drawn
 * @property waitlistCapacity most entrants allowed on the waitlist, 0 means no limit
 * @property status where the lottery is in its lifecycle
 * @param registrationStart first day entrants can join the waitlist
 * @param registrationEnd day registration closes
 * @param bookingStart first day winners can reserve their dates
 * @param bookingEnd day booking closes
 * @param waitlist not used, entries live in the lottery's entrants subcollection
 * @throws IllegalArgumentException if a date isnt like 2027-05-01, a period ends before it starts
 * or registration doesnt end before booking starts
 */
class Lottery(
    var name: String = "",
    val id: String = "",
    val ownerId: String = "",
    var campsiteIds: List<String> = emptyList(),
    registrationStart: String = "",
    registrationEnd: String = "",
    bookingStart: String = "",
    bookingEnd: String = "",
    var numWinners: Int = 0,
    var waitlistCapacity: Int = 0,
    var status: LotteryStatus = LotteryStatus.OPEN,
    val waitlist: List<WaitlistEntry> = emptyList()
) {
    //Methods are created using Claude Sonnet 5.5
    // Prompt: "I think we need functions to do the syntax checking on the temporary constructor variables? I think they should all be dates in the format: YYYY-MM-DD, which is of type LocalDate, maybe use the LocalDate type to do operations and checks on them but turn them back into strings for storage because firebase doesnt support LocalDate"
    // 2026-11-24

    /** first day of registration like 2027-05-01 and it only changes through [setRegistrationPeriod] */
    var registrationStart: String = registrationStart.trim()
        private set

    /** day registration closes and it only changes through [setRegistrationPeriod] */
    var registrationEnd: String = registrationEnd.trim()
        private set

    /** first day of booking and it only changes through [setBookingPeriod] */
    var bookingStart: String = bookingStart.trim()
        private set

    /** day booking closes and it only changes through [setBookingPeriod] */
    var bookingEnd: String = bookingEnd.trim()
        private set

    init {
        // blank dates are fine so firestore can make an empty lottery before filling it in
        checkPeriods(this.registrationStart, this.registrationEnd, this.bookingStart, this.bookingEnd)
    }

    /**
     * when entrants can join the waitlist
     *
     * its a function and not a property so firestore doesnt try to save it
     *
     * @return the registration period or null if it hasnt been set
     */
    fun registrationPeriod(): DateRange? = periodOrNull(registrationStart, registrationEnd)

    /**
     * when winners can reserve their dates
     *
     * its a function and not a property so firestore doesnt try to save it
     *
     * @return the booking period or null if it hasnt been set
     */
    fun bookingPeriod(): DateRange? = periodOrNull(bookingStart, bookingEnd)

    /**
     * sets when entrants can join the waitlist
     *
     * @param period the new registration period
     * @throws IllegalArgumentException if a date isnt like 2027-05-01, it ends before it starts
     * or it ends after booking starts, to move both use [setPeriods]
     */
    fun setRegistrationPeriod(period: DateRange) {
        checkPeriods(period.start, period.end, bookingStart, bookingEnd)
        registrationStart = period.start.trim()
        registrationEnd = period.end.trim()
    }

    /**
     * sets when winners can reserve their dates
     *
     * @param period the new booking period
     * @throws IllegalArgumentException if a date isnt like 2027-05-01, it ends before it starts
     * or it starts before registration ends, to move both use [setPeriods]
     */
    fun setBookingPeriod(period: DateRange) {
        checkPeriods(registrationStart, registrationEnd, period.start, period.end)
        bookingStart = period.start.trim()
        bookingEnd = period.end.trim()
    }

    /**
     * sets both periods at once, for when moving one alone would clash with the other
     *
     * @param registration the new registration period
     * @param booking the new booking period
     * @throws IllegalArgumentException if either period is invalid or registration doesnt end before booking starts
     */
    fun setPeriods(registration: DateRange, booking: DateRange) {
        checkPeriods(registration.start, registration.end, booking.start, booking.end)
        registrationStart = registration.start.trim()
        registrationEnd = registration.end.trim()
        bookingStart = booking.start.trim()
        bookingEnd = booking.end.trim()
    }

    /**
     * checks if entrants can join the waitlist on a day
     *
     * @param date the day to check, today by default
     * @return true if the day is in the registration period, false if its over or not set
     */
    fun registrationOpenOn(date: LocalDate = LocalDate.now()): Boolean =
        isWithin(registrationStart, registrationEnd, date)

    /**
     * checks if registration is over so the draw can happen
     *
     * @param date the day to check, today by default
     * @return true if the registration end day has come, false if it hasnt or isnt set
     */
    fun registrationEndedOn(date: LocalDate = LocalDate.now()): Boolean {
        val end = dateOrNull(registrationEnd) ?: return false
        return !date.isBefore(end)
    }

    /**
     * checks if winners can reserve on a day
     *
     * @param date the day to check, today by default
     * @return true if the day is in the booking period, false if its over or not set
     */
    fun bookingOpenOn(date: LocalDate = LocalDate.now()): Boolean =
        isWithin(bookingStart, bookingEnd, date)

    // saved dates were checked when they were set so they parse
    private fun dateOrNull(value: String): LocalDate? =
        if (value.isBlank()) null else LocalDate.parse(value)

    private fun periodOrNull(start: String, end: String): DateRange? =
        if (start.isBlank() || end.isBlank()) null else DateRange(start, end)

    private fun isWithin(start: String, end: String, date: LocalDate): Boolean {
        val first = dateOrNull(start) ?: return false
        val last = dateOrNull(end) ?: return false
        return !date.isBefore(first) && date.isBefore(last)
    }

    private companion object {
        // blank counts as not set
        fun parseDate(label: String, value: String): LocalDate? {
            if (value.isBlank()) return null
            return try {
                LocalDate.parse(value.trim())
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("$label must look like 2027-05-01, was $value")
            }
        }

        fun checkPeriods(regStart: String, regEnd: String, bookStart: String, bookEnd: String) {
            val rs = parseDate("Registration start", regStart)
            val re = parseDate("Registration end", regEnd)
            val bs = parseDate("Booking start", bookStart)
            val be = parseDate("Booking end", bookEnd)

            require((rs == null) == (re == null)) { "Registration needs both a start and an end date" }
            require((bs == null) == (be == null)) { "Booking needs both a start and an end date" }
            if (rs != null && re != null) {
                require(rs.isBefore(re)) { "Registration must end after it starts" }
            }
            if (bs != null && be != null) {
                require(bs.isBefore(be)) { "Booking must end after it starts" }
            }
            if (re != null && bs != null) {
                require(!bs.isBefore(re)) { "Registration period must end before the booking period starts" }
            }
        }
    }
}