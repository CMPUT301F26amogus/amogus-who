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

    /**
     * saves changes to a campsite
     *
     * @return true if saved, false if the name, occupancy or price isnt valid
     */
    fun onEditCampsite(navController: NavController, old: Campsite, newName: String, newLocation: String, newDescription: String, newMaxOccupants: Int, newPrice: Double): Boolean {
        // Campsite checks its fields now and throws if one is bad
        try {
            model.updateCampsite(
                old,
                newName,
                newLocation,
                newDescription,
                newMaxOccupants,
                newPrice
            )
        } catch (e: IllegalArgumentException) {
            return false
        }
        navController.navigate("owner_home") // TODO: Is this the right screen to go to after editing? idk
        return true
    }

    fun onAddCampsite (navController: NavController, new : Campsite) {
        // createCampsite needs a callback now, only leave the screen once its saved
        model.createCampsite(new) { saved ->
            if (saved) navController.navigate("owner_home")
        }
    }
}