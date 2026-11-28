package com.example.code.controllers

import Model
import androidx.navigation.NavController

class EntrantCampsitesInLotteryController(
    private val model: Model
) : SearchResultsController(model){
    /**onDetails
     *
     * navigate to the campsite details page of the inputted campID*/
    fun onDetails(campID : String, navController: NavController) {
        super.viewCampsite(campID, navController)
    }

    /**onReserve
     *
     * navigate to the reserve campsite page of the inputted campID*/
    fun onReserve(campID : String, navController: NavController) {
        // TODO: implement traversal to Campsite Reservation view
    }
}