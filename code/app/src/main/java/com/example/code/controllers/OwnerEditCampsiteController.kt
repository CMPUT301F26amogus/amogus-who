package com.example.code.Controllers
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
            if (success) navController.popBackStack()
        }
        navController.navigate("owner_home")
    }

    fun onAddCampsite (navController: NavController,campName: String, location: String, desc: String, maxOcc: Int,
                       price: Double, availability: AvailabilitySchedule, onResult: (Boolean) -> Unit) {
        model.createCampsite(campName, location, desc, maxOcc, price, availability) { success ->
            onResult(success)
            if (success) {
                navController.popBackStack()
            }
        }
    }
}