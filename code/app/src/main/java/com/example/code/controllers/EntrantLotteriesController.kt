package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.Routes

class EntrantLotteriesController(
    private val model : Model
) {
    /**
     * Confirms a lottery
     */
    public fun confirmLottery(lottery: Lottery){

    }

    /**
     * Cancels a lottery
     */
    public fun cancelLottery(lottery: Lottery){
        model.leaveWaitlist(
            lottery = lottery,
        ){}
    }

    /**
     * Sets the active site to view
     */
    public fun viewCampsite(cid: String, navController: NavController){
        // getCampsite is async so the campsite is only here inside the callback,
        // set it and navigate in there or the details screen opens with nothing
        model.getCampsite(cid){ result ->
            if (result != null) {
                model.settTempCampsite(result) // why are we even using 2 t's in this model func, idk man
                navController.navigate(Routes.CAMPSITE_DETAILS)
            }
        }
    }
}