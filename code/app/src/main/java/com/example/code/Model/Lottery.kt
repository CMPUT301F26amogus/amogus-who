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
 * @property campsites list of campsites this lottery is for
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
    var waitlistCapacity: Int = 0,
    var status: LotteryStatus = LotteryStatus.OPEN,
    waitlist: List<WaitlistEntry> = emptyList(),
) {

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


}
