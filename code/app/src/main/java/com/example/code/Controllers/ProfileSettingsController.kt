package com.example.code.Controllers

import Model
import com.example.code.Model.User

class ProfileSettingsController(
    private val model : Model
) {
    /**
     * Updates the active user's profile
     */
    public fun updateProfile(name: String, email: String, number: String){
//        var tempuser: User = User()
//        model.getUserObj { user ->
//            if (user != null) {
//                tempuser = user
//            }
//        }
        // I changed the update profile method to not expect User obj, so tempUser isnt needed anymore
        // if this somehow breaks this code just lmk and I'll change it back
        // -- Hui Yan
        model.updateProfile( name = name, email = email, phoneNumber = number)
    }

    /**
     * Deletes the active users profile
     */
    public fun deleteProfile(){
        model.deleteProfile()
    }
}