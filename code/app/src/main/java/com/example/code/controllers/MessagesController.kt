package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.Routes
import com.example.code.model.Reservation

/**
 * Handles the Messages screen: opening a conversation about a reservation and sending
 * messages in it (US 01.14.01 entrant to owner, US 02.12.01 owner to entrant)
 */
class MessagesController(
    private val model : Model
) {
    /**
     * opens the conversation about a reservation, from a reservation card
     *
     * @param reservationId the reservation to message about
     * @param title what to show at the top, eg the campsite or entrant name
     */
    fun openConversation(navController: NavController, reservationId: String, title: String) {
        model.settMessageConversation(reservationId, title)
        navController.navigate(Routes.MESSAGES)
    }

    /**
     * opens a conversation from the list on the Messages screen (already on the screen)
     */
    fun showConversation(reservationId: String, title: String) {
        model.settMessageConversation(reservationId, title)
    }

    /**
     * goes back to the list of conversations
     */
    fun showConversationList() {
        model.settMessageConversation("", "")
    }

    /**
     * sends a message in the open conversation
     *
     * the model works out if it goes to the owner or the entrant, and refreshes the
     * screen once it's saved
     *
     * @param text what the user typed
     * @param onResult null if it was sent, otherwise a message saying why not
     */
    fun sendMessage(text: String, onResult: (String?) -> Unit) {
        if (text.isBlank()) {
            onResult("Please write a message first")
            return
        }
        if (model.messageReservationId.isBlank()) {
            onResult("Pick a reservation to message about")
            return
        }
        // sendMessage only needs the id, it loads the rest of the reservation itself
        model.sendMessage(Reservation(id = model.messageReservationId), text) { result ->
            result.onSuccess { onResult(null) }
            result.onFailure { error -> onResult(error.message ?: "Couldn't send the message") }
        }
    }
}
