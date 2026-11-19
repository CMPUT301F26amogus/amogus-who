package com.example.code.model

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
 * @property guests how many people are coming
 * @property status if its active past cancelled or rejected
 * @property rulesAccepted true once the entrant agreed to the campsite rules
 */
class Reservation(
    var id: String = "",
    val campsite: Campsite? = null,
    var entrant: User? = null,
    var checkIn: String = "",
    var checkOut: String = "",
    var guests: Int = 0,
    var status: ReservationStatus = ReservationStatus.ACTIVE,
    var rulesAccepted: Boolean = false,
) {
    /* Gemini 3.6 Flash
     * "There seem to be a ton of files in the test suite that also have build errors, please go through them and apply a fix."
     * 2026-11-11
     */
    var lotteryId: String = ""

    val campsiteId: String
        get() = campsite?.id ?: ""

    val entrantId: String
        get() = entrant?.deviceId ?: ""

    constructor(
        id: String = "",
        campsiteId: String = "",
        lotteryId: String = "",
        entrantId: String = "",
        checkIn: String = "",
        checkOut: String = "",
        guests: Int = 0,
        status: ReservationStatus = ReservationStatus.ACTIVE,
        rulesAccepted: Boolean = false,
    ) : this(
        id = id,
        campsite = Campsite(id = campsiteId),
        entrant = User(deviceId = entrantId),
        checkIn = checkIn,
        checkOut = checkOut,
        guests = guests,
        status = status,
        rulesAccepted = rulesAccepted,
    ) {
        this.lotteryId = lotteryId
    }
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
