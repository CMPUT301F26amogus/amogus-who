package com.example.code.Controllers
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
                campsiteId =  old.id,
                deviceId = model.currentDeviceId,
                updates = mapOf(
                    "name" to newName,
                    "location" to newLocation,
                    "amenities" to newDescription,
                    "maxOccupancy" to newMaxOccupants,
                    "pricePerNight" to newPrice,
                ),
                onResult = { pass, ret ->
                    if (pass) {
                        navController.popBackStack()
                    }
                },
            )
        } catch (e: IllegalArgumentException) {
            return false
        }
        navController.popBackStack() // TODO: Is this the right screen to go to after editing? idk
        return true
    }

    fun onAddCampsite (navController: NavController, name: String, location: String, desc: String, maxOcc: String, price: String) {
        // createCampsite needs a callback now, only leave the screen once its saved
        model.createCampsite(
            campName = name,
            location = location,
            desc = desc,
            maxOcc = maxOcc.toInt(),
            price = price.toDouble()
        ) { saved ->
            if (saved) navController.popBackStack()
        }
    }
}