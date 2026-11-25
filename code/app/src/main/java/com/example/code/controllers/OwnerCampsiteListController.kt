package com.example.code.controllers

import Model
import androidx.navigation.NavController

class OwnerCampsiteListController(
    private val model : Model
) {
    fun onEditCampsite(navController : NavController, cID: String) {
        model.getCampsite(
            campId = cID){ result ->
            model.settTempCampsite(result)
        }
        navController.navigate("add_edit_campsite")
    }

    fun onCampsiteDetails(navController : NavController, cID: String) {
        model.getCampsite(
            campId = cID){ result ->
            model.settTempCampsite(result)
        }
        navController.navigate("campsite_details")
    }

    fun onRegisterCampsite(navController : NavController) {
        model.settTempCampsite(null)
        navController.navigate("add_edit_campsite")
    }
}