package com.example.code.controllers

import Model
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Only bad input is tested here, since good input saves to Firebase.
 */
class RegistrationControllerTest {
    private val controller = RegistrationController(Model())

    @Test
    fun blankNameAsksForName() {
        assertEquals("Please enter your name", controller.register(name = " ", phoneNumber = "", email = ""))
    }

    @Test
    fun badEmailAsksForValidEmail() {
        assertEquals(
            "Please enter a valid email, like name@example.com",
            controller.register(name = "John", phoneNumber = "", email = "Doe"),
        )
    }

    @Test
    fun badPhoneAsksForValidPhone() {
        assertEquals(
            "Please enter a valid phone number (7 to 15 digits)",
            controller.register(name = "John", phoneNumber = "123", email = ""),
        )
    }
}
