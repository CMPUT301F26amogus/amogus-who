package com.example.code.Controllers

import Model
import androidx.navigation.NavController
import com.example.code.Model.Campsite

class OwnerCampsiteListController(
    private val model : Model
) {
    fun onEditCampsite(navController : NavController, cID: String) {
        model.campId = cID
        navController.navigate("add_edit_campsite")
    }

    fun onCampsiteDetails(navController : NavController, cID: String) {
        model.campId = cID
        navController.navigate("campsite_details")
    }

    fun onRegisterCampsite(navController : NavController) {
        model.campId = ""
        navController.navigate("add_edit_campsite")
    }
}