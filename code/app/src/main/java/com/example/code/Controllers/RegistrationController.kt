package com.example.code.Controllers

import Model

class RegistrationController(
    private val model : Model
) {
    /**
     * Registers a user
     */
    public fun register(name: String, phoneNumber: String, email: String){
        model.signUp(name = name, phoneNumber = phoneNumber, email = email)
    }
}