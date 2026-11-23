package com.example.code.controllers

import Model
import com.example.code.model.User

class ProfileSettingsController(
    private val model : Model
) {
    /**
     * Updates the active user's profile
     */
    public fun updateProfile(name: String, email: String, number: String){
        var tempuser: User = User()
        model.getUserObj { user ->
            if (user != null) {
                tempuser = user
            }
        }
        model.updateProfile(user = tempuser, name = name, email = email, phoneNumber = number)
    }

    /**
     * Loads whether the current user allows notifications (US 01.10.01).
     *
     * @param onResult called with true if notifications are on; not called if there's no user
     */
    public fun loadNotificationsEnabled(onResult: (Boolean) -> Unit) {
        model.getUserObj { user ->
            if (user != null) {
                onResult(user.notificationsEnabled)
            }
        }
    }

    /**
     * Turns notifications on or off for the current user and saves it (US 01.10.01).
     *
     * @param enabled false to opt out of notifications
     */
    public fun setNotificationsEnabled(enabled: Boolean) {
        model.getUserObj { user ->
            if (user != null) {
                user.notificationsEnabled = enabled
                model.setUserObj(user)
            }
        }
    }

    /**
     * Deletes the active users profile
     */
    public fun deleteProfile(){
        model.deleteProfile()
    }
}