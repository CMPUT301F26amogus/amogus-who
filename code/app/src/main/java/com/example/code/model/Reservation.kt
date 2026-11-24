package com.example.code.model

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

}
