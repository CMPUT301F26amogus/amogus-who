package com.example.code.model

// The following class is from Google, Gemini, "Implement messaging the campsite owner for a reservation in Model.kt", 2026-11-26

/**
 * a message sent between an entrant and a campsite owner about a reservation
 *
 * every param has a default so firestore can load it
 *
 * @property id unique id for this message
 * @property reservationId id of the reservation this message is about
 * @property senderId device id of the user who sent the message
 * @property recipientId device id of the user receiving the message
 * @property contents the message text
 * @property timestamp when it was sent in YYYY-MM-DDTHH:MM:SS format
 */
data class Message(
    var id: String = "",
    var senderId: String = "",
    var recipientId: String = "",
    var reservationId: String = "",
    var contents: String = "",
    var timestamp: String = "",
)