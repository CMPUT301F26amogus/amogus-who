package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.Routes

class SearchResultsController(
    private val model : Model
) {
    /**
     * Sets the active campsite to view
     */
    public fun viewCampsite(cid: String, navController: NavController){
        //set campsite to view
        navController.navigate(Routes.CAMPSITE_DETAILS)
    }
}