package com.example.code.controllers

import Model

class RegistrationController(
    private val model : Model
) {
    /**
     * Registers a user
     *
     * @return true if registered, false if the name, email or phone number isnt valid
     */
    public fun register(name: String, phoneNumber: String, email: String): Boolean{
        if (name.isBlank()) {
            return false
        }
        try {
            model.signUp(
                name = name,
                phoneNumber = phoneNumber,
                email = email,
                onSuccess = {}
            )
        } catch (e: IllegalArgumentException) {
            return false
        }
        return true
    }

}