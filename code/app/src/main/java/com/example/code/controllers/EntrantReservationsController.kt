package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.model.Campsite
import com.example.code.model.EntrantReservation
import com.example.code.model.Reservation
import com.example.code.Routes

class EntrantReservationsController(
    private val model : Model
) {
    /**
     * loads the current users reservations that are coming up or happening now
     *
     * @param onResult gets the reservations sorted by check in or an empty list if it failed
     */
    fun onActiveReservations(onResult: (List<EntrantReservation>) -> Unit) {
//        model.getEntrantActiveReservations(model.currentDeviceId, onResult)
    }

    /**
     * loads the current users reservations that are over
     *
     * @param onResult gets the reservations newest first or an empty list if it failed
     */
    fun onPastReservations(onResult: (List<EntrantReservation>) -> Unit) {
//        model.getCancelledReservations(onResult)
    }

    /**
     * cancels one of the current users reservations
     *
     * @param reservation the reservation to cancel
     * @param onResult success once its cancelled or a failure saying why it couldnt be
     */
    public fun cancelReservation(reservation: Reservation, onResult: (Result<Unit>) -> Unit = {}) {
//        model.cancelReservation(reservation)
    }

    /**
     * Sets the active site to view
     */
    public fun viewCampsite(cid: String, navController: NavController){
        // getCampsite is async so the campsite is only here inside the callback,
        // set it and navigate in there or the details screen opens with nothing
        model.getCampsite(cid){ result ->
            if (result != null) {
                model.settTempCampsite(result)
                navController.navigate(Routes.CAMPSITE_DETAILS)
            }
        }
    }
}