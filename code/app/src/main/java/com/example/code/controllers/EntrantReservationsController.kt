package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.model.Campsite
import com.example.code.model.Reservation
import com.example.code.Routes

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
    public fun viewCampsite(cid: String, navController: NavController){
        //set campsite to view
        var tempCampsite: Campsite? = null
        model.getCampsite(cid){
                result ->
            if (result != null) {
                tempCampsite = result
            }
        }
        model.settTempCampsite(tempCampsite)
        navController.navigate(Routes.CAMPSITE_DETAILS)
    }
}