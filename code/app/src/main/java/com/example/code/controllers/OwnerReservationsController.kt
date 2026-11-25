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
     * loads the cancelled reservations at the owners campsites
     *
     * @param onResult gets the reservations sorted by check in or an empty list if it failed
     */
    fun onCancelledReservations(onResult: (List<OwnerReservation>) -> Unit) {
        model.getOwnerCancelledReservations(onResult)
    }

    fun onReservationReject() {

    }

    fun onReservationAccept() {

    }
}
