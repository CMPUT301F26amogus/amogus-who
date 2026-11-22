package com.example.code.Model

import Model
import java.time.LocalDate

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
     * the check in and check out dates as a range
     *
     * its a function and not a property so firestore doesnt try to save it
     *
     * @return the stay from [checkIn] to [checkOut]
     * @throws java.time.format.DateTimeParseException if a date isnt like 2027-05-01
     * @throws IllegalArgumentException if check out isnt after check in
     */
    fun stay(): DateRange = DateRange(LocalDate.parse(checkIn), LocalDate.parse(checkOut))
}
