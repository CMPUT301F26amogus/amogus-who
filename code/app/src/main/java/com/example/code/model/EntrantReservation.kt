package com.example.code.model

/**
 * a reservation with the campsite the entrant reservations screen shows on each card
 *
 * reservations only keep the campsite id so the model looks it up and hands them back together
 *
 * @property reservation the stay itself
 * @property campsite the campsite its at or null if the campsite is gone
 */
data class EntrantReservation(
    val reservation: Reservation,
    val campsite: Campsite?,
)
