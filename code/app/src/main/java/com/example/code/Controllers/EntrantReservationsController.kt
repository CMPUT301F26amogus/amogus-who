package com.example.code.Controllers

import Model
import Reservation

class EntrantReservationsController(
    private val model : Model
) {
    /**
     * Cancels a reservation
     */
    public fun cancelReservation(reservation: Reservation){
        model.cancelReservation(reservation)
    }

    /**
     * Sets the active site to view
     */
    public fun viewCampsite(){

    }
}