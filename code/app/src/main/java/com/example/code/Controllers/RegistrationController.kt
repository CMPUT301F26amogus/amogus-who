package com.example.code.Controllers

import Model
import androidx.navigation.NavController
import com.example.code.Routes

class RegistrationController(
    private val model : Model
) {
    /**
     * Registers a user
     */
    public fun register(name: String, phoneNumber: String, email: String, navController: NavController){
        //model.signUp(name = name, phoneNumber = phoneNumber, email = email,
//            onSuccess = {
//                navController.navigate(Routes.ENTRANT_HOME) {
//                    popUpTo(Routes.REGISTRATION) { inclusive = true }
//                }
//            })
        navController.navigate(Routes.ENTRANT_HOME) {
            popUpTo(Routes.REGISTRATION) { inclusive = true }
        }
    }

}