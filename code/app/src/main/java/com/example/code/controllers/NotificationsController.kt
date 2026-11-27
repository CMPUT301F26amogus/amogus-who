package com.example.code.controllers

import Model
import com.example.code.model.Notification

class NotificationsController(
    private val model: Model
) {
    /**
     * loads the current users notifications newest first
     *
     * this is where reservation and cancellation confirmations show up (US 01.05.01 01.07.02)
     *
     * @param onResult gets the notifications or an empty list if it failed
     */
    fun onNotifications(onResult: (List<Notification>) -> Unit) {
        model.getNotifications(model.currentDeviceId) { onResult(it.reversed()) }
    }
}
