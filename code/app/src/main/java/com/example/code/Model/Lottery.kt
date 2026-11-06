package com.example.code.Model

import Model
import java.time.LocalDate

/**
 * a lottery an owner runs for one or more of their campsites
 *
 * entrants join the waitlist during the registration period
 * then winners get drawn and book their dates during the booking period
 *
 * every param has a default so firestore can load it
 * dates are saved as strings like 2027-05-01 since firestore cant store LocalDate
 *
 * @property id unique id for this lottery
 * @property ownerId device id of the owner running it
 * @property campsiteIds ids of the campsites in this lottery
 * @property numWinners how many entrants get drawn
 * @property waitlistCapacity most entrants allowed on the waitlist or null for no limit
 * @property status where the lottery is in its lifecycle
 * @param registrationStart first day entrants can join the waitlist
 * @param registrationEnd day registration closes
 * @param bookingStart first day winners can reserve their dates
 * @param bookingEnd day booking closes
 * @param waitlist saved waitlist entries to load
 */
class Lottery(
    val id: String = "",
    val ownerId: String = "",
    var campsites: List<Campsite> = emptyList(),
    registrationStart: String = "",
    registrationEnd: String = "",
    bookingStart: String = "",
    bookingEnd: String = "",
    var numWinners: Int = 0,
    var waitlistCapacity: Int? = null,
    var status: LotteryStatus = LotteryStatus.OPEN,
    waitlist: List<WaitlistEntry> = emptyList(),
) : Model() {

    /**
     * makes a lottery from date ranges instead of strings
     *
     * @param registrationPeriod when entrants can join the waitlist
     * @param bookingPeriod when winners can reserve their dates
     */
    constructor(
        id: String,
        ownerId: String,
        campsiteIds: List<String>,
        registrationPeriod: DateRange,
        bookingPeriod: DateRange,
        numWinners: Int,
        waitlistCapacity: Int? = null,
        status: LotteryStatus = LotteryStatus.OPEN,
        waitlist: List<WaitlistEntry> = emptyList(),
    ) : this(
        id, ownerId, campsiteIds,
        registrationPeriod.start.toString(), registrationPeriod.end.toString(),
        bookingPeriod.start.toString(), bookingPeriod.end.toString(),
        numWinners, waitlistCapacity, status, waitlist,
    )

    /** first day of registration like 2027-05-01 and it only changes through [setRegistrationPeriod] */
    var registrationStart: String = registrationStart
        private set

    /** day registration closes and it only changes through [setRegistrationPeriod] */
    var registrationEnd: String = registrationEnd
        private set

    /** first day of booking and it only changes through [setBookingPeriod] */
    var bookingStart: String = bookingStart
        private set

    /** day booking closes and it only changes through [setBookingPeriod] */
    var bookingEnd: String = bookingEnd
        private set

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
     * @throws IllegalArgumentException if it ends after booking starts
     */
    fun setRegistrationPeriod(period: DateRange) {
        val booking = bookingPeriod()
        require(booking == null || !booking.start.isBefore(period.end)) {
            "Registration period must end before the booking period starts"
        }
        registrationStart = period.start.toString()
        registrationEnd = period.end.toString()
        notifyViews()
    }

    /**
     * sets when winners can reserve their dates
     *
     * @param period the new booking period
     * @throws IllegalArgumentException if it starts before registration ends
     */
    fun setBookingPeriod(period: DateRange) {
        val registration = registrationPeriod()
        require(registration == null || !period.start.isBefore(registration.end)) {
            "Booking period must start after the registration period ends"
        }
        bookingStart = period.start.toString()
        bookingEnd = period.end.toString()
        notifyViews()
    }

    private val _waitlist = waitlist.toMutableList()

    /** everyone who joined the waitlist */
    var waitlist: List<WaitlistEntry> = _waitlist
        get() = _waitlist.toList()

    // blank or broken saved dates count as not set
    private fun periodOrNull(start: String, end: String): DateRange? =
        runCatching { DateRange(LocalDate.parse(start), LocalDate.parse(end)) }.getOrNull()
}
