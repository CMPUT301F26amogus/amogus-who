package com.example.code.Controllers

import Model
import androidx.navigation.NavController
import com.example.code.Model.Lottery
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

        navController.navigate(Routes.CAMPSITE_DETAILS)
    }
}