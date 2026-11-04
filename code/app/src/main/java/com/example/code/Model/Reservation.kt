package com.example.code.Model

import Model

/**
 * a stay an entrant booked after winning a lottery
 *
 * @property id unique id for this reservation
 * @property campsiteId which campsite is booked
 * @property lotteryId which lottery they won to get it
 * @property entrantId device id of the entrant who booked it
 * @property stay check in and check out dates
 * @property guests how many people are coming
 * @property status if its active past cancelled or rejected
 * @property rulesAccepted true once the entrant agreed to the campsite rules
 */
class Reservation(
    val id: String,
    val campsiteId: String,
    val lotteryId: String,
    val entrantId: String,
    var stay: DateRange,
    var guests: Int,
    var status: ReservationStatus = ReservationStatus.ACTIVE,
    var rulesAccepted: Boolean = false,
) : Model()
