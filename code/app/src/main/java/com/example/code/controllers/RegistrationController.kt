package com.example.code.controllers

import Model
import com.example.code.model.Role

class RegistrationController(
    private val model : Model
) {
    /**
     * Registers a user
     *
     * @param role ENTRANT or OWNER, depending on which button was pressed
     * @return null if registered, otherwise a message saying what to fix
     */
    public fun register(name: String, phoneNumber: String, email: String, role: Role = Role.ENTRANT): String? {
        if (name.isBlank()) {
            return "Please enter your name"
        }
        try {
            model.signUp(
                name = name,
                phoneNumber = phoneNumber,
                email = email,
                roleOf = role,
                onSuccess = {}
            )
        } catch (e: IllegalArgumentException) {
            // User throws "Invalid email: ..." or "Invalid phone number: ..."
            val problem = e.message ?: ""
            if (problem.startsWith("Invalid email")) {
                return "Please enter a valid email, like name@example.com"
            }
            if (problem.startsWith("Invalid phone")) {
                return "Please enter a valid phone number (7 to 15 digits)"
            }
            return "Please check your details"
        }
        return null
    }

}