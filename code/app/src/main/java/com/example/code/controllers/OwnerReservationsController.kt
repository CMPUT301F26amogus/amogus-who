package com.example.code.controllers

import Model
import com.example.code.model.OwnerReservation

class OwnerReservationsController(
    private val model : Model
) {
    /**
     * loads the active reservations at the owners campsites
     *
     * @param onResult gets the reservations sorted by check in or an empty list if it failed
     */
    fun onActiveReservations(onResult: (List<OwnerReservation>) -> Unit) {
        model.getOwnerActiveReservations(onResult)
    }

    /**
     * loads the past reservations at the owners campsites
     *
     * @param onResult gets the reservations newest first or an empty list if it failed
     */
    fun onPastReservations(onResult: (List<OwnerReservation>) -> Unit) {
        model.getOwnerPastReservations(onResult)
    }

    /**
     * loads the cancelled and rejected reservations at the owners campsites
     *
     * @param onResult gets the reservations sorted by check in or an empty list if it failed
     */
    fun onCancelledReservations(onResult: (List<OwnerReservation>) -> Unit) {
        model.getOwnerCancelledReservations(onResult)
    }

    /**
     * rejects a reservation at one of the owners campsites (US 02.08.01)
     *
     * the model tells the entrant and notifies the views so the lists reload
     *
     * @param reservation the reservation to reject
     * @param onResult success once its rejected or a failure saying why it couldnt be
     */
    fun onReservationReject(reservation: OwnerReservation, onResult: (Result<Unit>) -> Unit = {}) {
        model.rejectReservation(reservation.reservation, onResult)
    }
}
