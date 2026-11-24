package com.example.code.model

/**
 * a reservation with the campsite and entrant name the owner reservations screen shows on each card
 *
 * reservations only keep ids so the model looks these up and hands them back together
 *
 * @property reservation the stay itself
 * @property campsite the campsite its at or null if the campsite is gone
 * @property entrantName name of whoever booked it or blank if their profile is gone
 */
data class OwnerReservation(
    val reservation: Reservation,
    val campsite: Campsite?,
    val entrantName: String,
)
