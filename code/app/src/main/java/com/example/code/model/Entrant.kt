package com.example.code.model

class Entrant: User() {
    /**
     * Arraylist of WaitlistEntryId's
     */
    private val waitlistEntries: ArrayList<String> = arrayListOf()

    /**
     * ArrayList of ReservationId's
     */
    private val reservations : ArrayList<String> = arrayListOf()

    /**
     * Arraylist of MessageId's
     */
    private val messages : ArrayList<String> = arrayListOf()

}