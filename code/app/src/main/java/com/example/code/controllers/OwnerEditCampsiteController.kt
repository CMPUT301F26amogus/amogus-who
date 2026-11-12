package com.example.code.controllers
import Model
import androidx.navigation.NavController
import com.example.code.model.Campsite

class OwnerEditCampsiteController (
    private val model : Model
) {
    fun onCancel(navController: NavController){
        navController.navigate("owner_home")
    }

    fun onEditCampsite(navController: NavController, old: Campsite, newName: String, newLocation: String, newDescription: String, newMaxOccupants: Int, newPrice: Double) {
        model.updateCampsite(
            old,
            newName,
            newLocation,
            newDescription,
            newMaxOccupants,
            newPrice
        )
        navController.navigate("owner_home")
    }

    /* Gemini 3.6 Flash
     * "There are build errors in RegistrationView.kt and OwnerEditCampsiteController.kt, can you find them and apply some fixes to them?"
     * 2026-11-11
     */
    fun onAddCampsite (navController: NavController, new : Campsite) {
        model.createCampsite(new) { success ->
            if (success) {
                navController.navigate("owner_home")
            }
        }
    }
}