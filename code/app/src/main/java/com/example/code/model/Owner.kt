package com.example.code.model

class Owner(
    /**
     * arraylist of lotteryId
     */
    private val lotteries: ArrayList<String> = arrayListOf(),
    /**
     * Arraylist of campsiteId's
     */
    private val campsites: ArrayList<String> = arrayListOf(),
    /**
     * arraylist of messageId
     */
    private val messages: ArrayList<String> = arrayListOf()
) : User()
{
    fun createCampsite(campsite: Campsite) {
        // TODO: Add campsite logic (e.g., campsites.add(campsite.id))
    }

    fun updateCampsite(campsite: Campsite) {
        // TODO: Update campsite logic
    }

    fun deleteCampsite(campsiteId: String) {
        campsites.remove(campsiteId)
    }

    fun getMyCampsites(): List<String> {
        return campsites.toList()
    }
}