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
        // the details screen reads the active campsite from the model, this never set it before
        model.getCampsite(cid){ result ->
            if (result != null) {
                model.settTempCampsite(result)
                navController.navigate(Routes.CAMPSITE_DETAILS)
            }
        }
    }
}