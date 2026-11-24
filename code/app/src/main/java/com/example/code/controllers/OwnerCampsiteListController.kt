package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.Routes
import com.example.code.model.Campsite

class OwnerCampsiteListController(
    private val model : Model
) {
    fun loadMyCampsites(onResult: (List<Campsite>) -> Unit) {
        model.getCampsiteByOwner(model.currentDeviceId, onResult)
    }

    fun onEditCampsite(navController : NavController, cID: String, onError: () -> Unit = {}) {
        model.campId = cID
        model.getCampsite(cID) { campsite ->
            if (campsite == null) {
                onError()
                return@getCampsite
            }
            model.settTempCampsite(campsite)
            navController.navigate(Routes.ADD_EDIT_CAMPSITE)
        }
        navController.navigate("add_edit_campsite")
    }

    fun onCampsiteDetails(navController : NavController, cID: String, onError: () -> Unit = {}) {
        model.campId = cID
        model.getCampsite(cID) { campsite ->
            if (campsite == null) {
                onError()
                return@getCampsite
            }
            model.settTempCampsite(campsite)
            navController.navigate(Routes.CAMPSITE_DETAILS)
        }
    }

    fun onRegisterCampsite(navController : NavController) {
        model.campId = ""
        model.settTempCampsite(null)
        navController.navigate(Routes.ADD_EDIT_CAMPSITE)
    }
}