package com.example.code.model

class Admin {
    fun removeCampsite(campsite: String): List<User> {
        TODO("Remove campsite and return affected users")
    }

    fun removeProfile(user: String) {
        TODO("Remove user profile")
    }

    fun removeImage(imageUrl: String) {
        TODO("Remove image at imageUrl")
    }

    fun browseCampsites(): List<String> {
        TODO("Fetch and return list of campsites")
    }

    fun browseProfiles(): List<String> {
        TODO("Fetch and return list of user profiles")
    }

    fun browseImages(): List<String> {
        TODO("Fetch and return list of image URLs")
    }

    fun viewNotificationLogs(): List<Notification> {
        TODO("Fetch and return notification logs")
    }

    fun removeCampsiteRating(campsite: String, rating: Int) {
        TODO("Remove rating from campsite")
    }

    fun removeCampsiteReview(campsite: String, review: String) {
        TODO("Remove review from campsite")
    }

    fun viewMessageLogs(): List<Notification> {
        TODO("Fetch and return message logs")
    }
}