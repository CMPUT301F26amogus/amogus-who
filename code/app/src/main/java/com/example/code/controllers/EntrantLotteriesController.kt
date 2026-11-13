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