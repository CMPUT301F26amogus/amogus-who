package com.example.code.Model

import Model
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
    val id: String,
    val recipientIds: List<String>,
    val message: String,
    val sentAt: Instant = Instant.now(),
    val lotteryId: String? = null,
){

}
