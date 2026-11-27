package com.example.code.controllers
import Model
import androidx.navigation.NavController
import com.example.code.model.AvailabilitySchedule
import com.example.code.model.Campsite

class OwnerEditCampsiteController (
    private val model : Model
) {
    fun onCancel(navController: NavController){
        navController.popBackStack()
    }

    /**
     * saves changes to a campsite
     *
     * @return true if saved, false if the name, occupancy or price isnt valid
     */
    fun onEditCampsite(navController: NavController, old: Campsite, updates: Map<String, Any>, onResult: (Boolean, String?) -> Unit) {
        model.updateCampsite(
            old.id,
            model.currentDeviceId,
            updates
        ) { success, error ->
            onResult(success,error)
            if (success) navController.popBackStack() // TODO: Is this the right screen to go to after editing? idk
        }
    }

    /**
     * lists a new campsite for the current owner
     *
     * @param rules rules and policies entrants have to agree to before reserving or blank for none (US 02.11.01)
     * @param onResult true if it was saved
     */
    fun onAddCampsite (navController: NavController,campName: String, location: String, desc: String, maxOcc: Int,
                       price: Double, availability: AvailabilitySchedule, rules: String = "", onResult: (Boolean) -> Unit) {
        model.createCampsite(campName, location, desc, maxOcc, price, availability, rules) { success ->
            onResult(success)
            if (success) {
                navController.popBackStack()
            }
        }
    }
}