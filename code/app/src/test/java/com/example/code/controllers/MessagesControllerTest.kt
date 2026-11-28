package com.example.code.controllers

import Model
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests the checks MessagesController does before sending (US 01.14.01, 02.12.01).
 * Sending itself goes to Firestore, so it's tested in the app.
 */
class MessagesControllerTest {
    private val model = Model()
    private val controller = MessagesController(model)

    @Test
    fun blankMessageIsNotSent() {
        model.messageReservationId = "res-1"
        var error: String? = null
        controller.sendMessage("   ") { error = it }
        assertEquals("Please write a message first", error)
    }

    @Test
    fun needsAConversationToSend() {
        model.messageReservationId = ""
        var error: String? = null
        controller.sendMessage("Hello") { error = it }
        assertEquals("Pick a reservation to message about", error)
    }
}
