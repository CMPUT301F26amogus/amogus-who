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

    fun onReservationReject() {

    }

    fun onReservationAccept() {

    }
}
