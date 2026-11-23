package com.example.code.model

import java.time.Instant

/**
 * a notification sent to one or more users
 *
 * admins can see all of them in the log
 *
 * @property id unique id for this notification
 * @property senderId device id of who sent it
 * @property recipientIds device ids of who gets it
 * @property message what it says
 * @property sentAt when it was sent
 * @property lotteryId the lottery its about or null if its not about one
 */
data class Notification(
    var id: String = "",
    var entrantId: String = "",
    var ownerId: String = "",
    var content: String = "",
    var timestamp: String = "", // Timestamp in format YYYY-MM-DDTHH:MM:SS, example: 2020-06-07T17:38:21
    var lotteryId: String = ""
){
    // maybe make timestamp into a temp constructor parameter to create a timestamp checker

}
