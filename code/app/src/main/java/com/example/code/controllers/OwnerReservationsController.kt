package com.example.code.controllers

import Model
import com.example.code.model.Reservation

class OwnerReservationsController(
    private val model : Model
) {
    /**
     * loads the active reservations at the owners campsites
     *
     * @param onResult gets the reservations sorted by check in or an empty list if it failed
     */
    fun loadActiveReservations(onResult: (List<Reservation>) -> Unit) {
        model.getOwnerActiveReservations(model.currentDeviceId, onResult)
    }

    /**
     * loads the past reservations at the owners campsites
     *
     * @param onResult gets the reservations newest first or an empty list if it failed
     */
    fun loadPastReservations(onResult: (List<Reservation>) -> Unit) {
        model.getOwnerPastReservations(model.currentDeviceId, onResult)
    }

    /**
     * loads the cancelled reservations at the owners campsites
     *
     * @param onResult gets the reservations sorted by check in or an empty list if it failed
     */
    fun loadCancelledReservations(onResult: (List<Reservation>) -> Unit) {
        model.getOwnerCancelledReservations(model.currentDeviceId, onResult)
    }

    fun onReservationReject() {

    }

    fun onReservationAccept() {

    }
}
