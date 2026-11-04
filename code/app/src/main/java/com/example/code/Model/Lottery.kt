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
 * @property registrationPeriod when entrants can join the waitlist
 * @property bookingPeriod when winners can reserve their dates
 * @property numWinners how many entrants get drawn
 * @property waitlistCapacity most entrants allowed on the waitlist or null for no limit
 * @property status where the lottery is in its lifecycle
 * @param waitlist saved waitlist entries to load
 */
class Lottery(
    val id: String,
    val ownerId: String,
    var campsiteIds: List<String>,
    var registrationPeriod: DateRange,
    var bookingPeriod: DateRange,
    var numWinners: Int,
    var waitlistCapacity: Int? = null,
    var status: LotteryStatus = LotteryStatus.OPEN,
    waitlist: List<WaitlistEntry> = emptyList(),
) : Model() {
    private val _waitlist = waitlist.toMutableList()

    /** everyone who joined the waitlist */
    val waitlist: List<WaitlistEntry>
        get() = _waitlist.toList()
}
