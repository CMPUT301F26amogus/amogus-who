package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.Routes.ADD_EDIT_CAMPSITE
import com.example.code.Routes.OWNER_CAMPSITES

class OwnerCampsiteListController(
    private val model : Model
) {
    fun onEditCampsite(navController : NavController, cID: String) {
        model.getCampsite(cID) { result ->
            model.settTempCampsite(result)
        }
        navController.navigate(ADD_EDIT_CAMPSITE)
    }

    fun onCampsiteDetails(navController : NavController, cID: String) {
        model.getCampsite(cID) { result ->
            model.settTempCampsite(result)
        }
        navController.navigate(OWNER_CAMPSITES)
    }

    fun onRegisterCampsite(navController : NavController) {
        model.settTempCampsite(null)
        navController.navigate(ADD_EDIT_CAMPSITE)
    }
}