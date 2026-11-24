package com.example.code.controllers

import Model
import com.example.code.model.User

class ProfileSettingsController(
    private val model : Model
) {
    /**
     * Updates the active user's profile
     */
    fun updateProfile(name: String, email: String, number: String) {
        try {
            model.updateProfile(name = name, email = email, phoneNumber = number)
            // Optional: tell the View the update started/succeeded
        } catch (e: IllegalArgumentException) {
            // e.message holds the specific validation error from Model
            // (e.g., blank name, invalid phone number, or invalid email)
            print(e.message ?: "Invalid profile input")
        }
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