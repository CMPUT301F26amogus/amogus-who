package com.example.code.Controllers
import Model
import androidx.navigation.NavController
import com.example.code.Model.Campsite

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

    fun onAddCampsite (navController: NavController, new : Campsite) {
        model.createCampsite(new)
        navController.navigate("owner_home")
    }
}