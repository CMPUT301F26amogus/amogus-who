package com.example.code.Model

import Model
// the old Campsite in the default package not the one in this package
import Campsite

/**
 * everything a campsite owner works with
 *
 * views showing the owners campsites register here and get notified when the list changes
 *
 * @property ownerId device id of the owner
 */
class OwnerModel(val ownerId: String) : Model() {
    private val campsiteRef by lazy { db.collection("campsite") }

    /** the owners campsites and it only changes through methods like [createCampsite] */
    var campsites: List<Campsite> = emptyList()
        private set

    /**
     * registers a new campsite for this owner
     *
     * makes the campsite id and starts the rating at 0
     * once its saved it gets added to [campsites] and views get notified
     *
     * @param name campsite name and cant be blank
     * @param location where it is and cant be blank
     * @param desc what the campsite is like
     * @param maxOcc most guests allowed and must be more than 0
     * @param price cost per night and must be 0 or more
     * @param onResult gets true if it saved and false if it didnt
     * @throws IllegalArgumentException if any detail is invalid
     */
    fun createCampsite(name: String, location: String, desc: String, maxOcc: Int, price: Double,
                       onResult: (Boolean) -> Unit = {}) {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(location.isNotBlank()) { "Location must not be blank" }
        require(maxOcc > 0) { "Max occupancy must be more than 0, was $maxOcc" }
        require(price.isFinite() && price >= 0) { "Price must be 0 or more, was $price" }

        // firestore makes a unique id without needing the network
        val document = campsiteRef.document()
        val campsite = Campsite(ownerId, document.id, name.trim(), location.trim(), desc.trim(), maxOcc, price, 0.0)
        document.set(campsite)
            .addOnSuccessListener {
                campsites = campsites + campsite
                notifyViews()
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}
