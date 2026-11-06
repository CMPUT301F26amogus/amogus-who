package com.example.code.Model

import Model

/**
 * a lottery an owner runs for one or more of their campsites
 *
 * entrants join the waitlist during the registration period
 * then winners get drawn and book their dates during the booking period
 *
 * @property id unique id for this lottery
 * @property ownerId device id of the owner running it
 * @property campsiteIds ids of the campsites in this lottery
 * @property numWinners how many entrants get drawn
 * @property waitlistCapacity most entrants allowed on the waitlist or null for no limit
 * @property status where the lottery is in its lifecycle
 * @param registrationPeriod when entrants can join the waitlist
 * @param bookingPeriod when winners can reserve their dates
 * @param waitlist saved waitlist entries to load
 */
class Lottery(
    val id: String,
    val ownerId: String,
    var campsiteIds: List<String>,
    registrationPeriod: DateRange,
    bookingPeriod: DateRange,
    var numWinners: Int,
    var waitlistCapacity: Int? = null,
    var status: LotteryStatus = LotteryStatus.OPEN,
    waitlist: List<WaitlistEntry> = emptyList(),
) : Model() {
    /** when entrants can join the waitlist and it only changes through [setRegistrationPeriod] */
    var registrationPeriod: DateRange = registrationPeriod
        private set

    /** when winners can reserve their dates and it only changes through [setBookingPeriod] */
    var bookingPeriod: DateRange = bookingPeriod
        private set

    /**
     * sets when entrants can join the waitlist
     *
     * @param period the new registration period
     * @throws IllegalArgumentException if it ends after booking starts
     */
    fun setRegistrationPeriod(period: DateRange) {
        require(!bookingPeriod.start.isBefore(period.end)) {
            "Registration period must end before the booking period starts"
        }
        registrationPeriod = period
        notifyViews()
    }

    /**
     * sets when winners can reserve their dates
     *
     * @param period the new booking period
     * @throws IllegalArgumentException if it starts before registration ends
     */
    fun setBookingPeriod(period: DateRange) {
        require(!period.start.isBefore(registrationPeriod.end)) {
            "Booking period must start after the registration period ends"
        }
        bookingPeriod = period
        notifyViews()
    }

    private val _waitlist = waitlist.toMutableList()

    /** everyone who joined the waitlist */
    val waitlist: List<WaitlistEntry>
        get() = _waitlist.toList()
}
