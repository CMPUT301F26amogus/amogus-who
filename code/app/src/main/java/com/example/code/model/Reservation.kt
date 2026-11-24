package com.example.code.model

import com.google.firebase.firestore.Exclude
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * a stay an entrant booked after winning a lottery
 *
 * every param has a default so firestore can load it
 * dates are saved as strings since firestore cant store LocalDate
 *
 * @property id unique id for this reservation
 * @property campsiteId which campsite is booked
 * @property lotteryId which lottery they won to get it
 * @property entrantId device id of the entrant who booked it
 * @property checkIn check in date like 2027-05-01
 * @property checkOut check out date like 2027-05-03
 * @property numGuests how many people are coming
 * @property status if its active past cancelled or rejected
 * @property rulesAccepted true once the entrant agreed to the campsite rules
 */
class Reservation(
    var id: String = "",
    var campsiteId: String = "",
    var entrantId: String = "",
    var checkIn: String = "",
    var checkOut: String = "",
    var numGuests: Int = 1, // at least one guest
    var status: ReservationStatus = ReservationStatus.ACTIVE, // is this default?
    var rulesAccepted: Boolean = false,
    var lotteryId: String = ""
) {

    /**
     * how many nights the stay is
     *
     * its a function and not a property so firestore doesnt try to save it
     *
     * @return nights from [checkIn] to [checkOut]
     * @throws java.time.format.DateTimeParseException if a date isnt like 2027-05-01
     */
    fun nights(): Long = ChronoUnit.DAYS.between(LocalDate.parse(checkIn), LocalDate.parse(checkOut))

    /**
     * checks if this stay is still coming up or happening now
     *
     * nothing sets PAST yet so the check out date decides when a stay is over
     * its a function and not a property so firestore doesnt try to save it
     *
     * @param today the date to compare against so tests can pick one
     * @return true if its active and check out hasnt passed yet
     * @throws java.time.format.DateTimeParseException if check out isnt like 2027-05-03
     */
    fun isActive(today: LocalDate = LocalDate.now()): Boolean =
        status == ReservationStatus.ACTIVE && !LocalDate.parse(checkOut).isBefore(today)

    /**
     * checks if this stay is over
     *
     * nothing sets PAST yet so an active stay counts once check out has passed
     * so every active or past reservation is exactly one of [isActive] or isPast
     *
     * @param today the date to compare against so tests can pick one
     * @return true if its marked past or its active and check out was before today
     * @throws java.time.format.DateTimeParseException if its active and check out isnt like 2027-05-03
     */
    fun isPast(today: LocalDate = LocalDate.now()): Boolean = when (status) {
        ReservationStatus.PAST -> true
        ReservationStatus.ACTIVE -> LocalDate.parse(checkOut).isBefore(today)
        else -> false
    }

    /**
     * checks if the entrant cancelled this stay
     *
     * rejected ones dont count since the owner did that not the entrant
     * excluded so firestore doesnt save it as a cancelled field
     *
     * @return true if its cancelled
     */
    @Exclude
    fun isCancelled(): Boolean = status == ReservationStatus.CANCELLED
}
