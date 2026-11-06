package com.example.code.Model

import Model
import androidx.compose.animation.core.snap

class EntrantModel: Model() {



    /**
     * Signs up entrant into the database
     * Create the entrant profile, with all their information.
     *
     *
     * @param name user's signUp name from view
     * @param phoneNumber user's phone number from the view
     * @param email user's email from the view
     */
    fun signUp(name: String, phoneNumber: String, email: String){
        val newUser = User(currentDeviceId, name, email=email,phone=phoneNumber)
        dataRef.document(currentDeviceId).set(User())

    }



    /**
     * searches for deviceInformation across all entrants in database
     *
     * @return returns user if user is in database, else returns null.
     * the idea is that:
     *      if returns null, then the view is the sign-up page
     *      if returns user, then view is homepage
     */
    fun signIn(onResult: (User?) -> Unit) {
        getUserObj { user ->
            // When getUserObj finishes, forward the result to the caller of signIn
            onResult(user)
        }
    }

    // needs further improvement
    /**
     * Signs entrant up for lottery
     *
     *
     * @param Lottery the lottery the entrant wants to join
     * @return The lottery that the entrant joined, if zero then lottery failed to join
     */
    fun joinLottery(Lottery: String): Boolean{
        return false
    }

    /**
     * Takes a user profile and edits the user properties
     *
     * 'This function doesnt do any filtering for invalid phone numbers or emails, or names'
     *
     * @param user Users object, should be remembered within the controller upon login.
     * @param name optional, set new name of the user
     * @param email optional, set new email for user
     * @param phoneNumber optional, set new phone number for user
     */
    fun updateProfile(user: User, name: String="", email: String="", phoneNumber: String=""){
        if (name != "") {
            dataRef.document(currentDeviceId).update(
                "name", name
            )
        }
        if (email != ""){
            dataRef.document(currentDeviceId).update(
                "email", email
            )
        }
        if(phoneNumber != ""){
            dataRef.document(currentDeviceId).update(
                "phone", phoneNumber
            )
        }
    }



    /**
     * removes user from reservation, this allows user to decline reservation
     *
     *@param reservation the Reservation object that the user is declining
     */
    fun declineReservation(reservation: Reservation){
        // entrants should be able to decline a lottery win, that is we change the reservation status
        // I'm assuming that view people will pass in the reservation, because the user needs to click
        // on the reservation that they will cancel from a list of reservations.

        dataRef.document(reservation.id).update(
            "status", ReservationStatus.REJECTED
        )
    }

    // models need a way to cancel reservation, I guess a canceled reservation is
    // one where the lottery winner's havent been decided yet?

    /**
     * Cancels the user's reservation
     *
     * @param reservation The reservation object
     */
    fun cancelReservation(reservation: Reservation){
        dataRef.document(reservation.id).update(
            "status", ReservationStatus.CANCELLED
        )
    }

    // entrants need to be able to leave waitlist
    /**
     * leaves the waitlist of the lottery
     *
     * @param lottery Lottery that the entrant wants to leave waitlist from
     */
    fun leaveWaitlist(lottery: Lottery, onResult: (Boolean) -> Unit) {
        dataRef.document(lottery.id).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val tempLottery = snapshot.toObject(Lottery::class.java)

                    if (tempLottery != null) {
                        // Update the user's status in the waitlist
                        tempLottery.waitlist.forEach { entry ->
                            if (entry.userId == currentDeviceId) {
                                entry.status = EntryStatus.DECLINED
                            }
                        }

                        // Save the updated lottery object back to Firestore
                        dataRef.document(lottery.id).set(tempLottery)
                            .addOnSuccessListener {
                                onResult(true) // Successfully updated database
                            }
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false) // Document does not exist
                }
            }
    }


    // entrants should be able to join the waiting list
    /**
     * Adds the current user to the waitlist of the specified lottery.
     *
     * @param lottery Target lottery to join
     * @param onResult Callback returning true if joined successfully, false if full, already joined, or failed
     */
    fun joinWaitlist(lottery: Lottery, onResult: (Boolean) -> Unit) {
        dataRef.document(lottery.id).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    var currentLottery = snapshot.toObject(Lottery::class.java)

                    if (currentLottery != null) {
                        //Check if user is already in the waitlist
                        if (currentLottery.waitlist.any { it.userId == currentDeviceId }) {
                            onResult(false)
                            return@addOnSuccessListener
                        }

                        //Check if waitlist has reached maximum capacity (if capacity is set)
                        val capacity = currentLottery.waitlistCapacity
                        if (capacity != null && currentLottery.waitlist.size >= capacity) {
                            onResult(false) // Waitlist is full
                            return@addOnSuccessListener
                        }

                        //Create the new entry and append it to a new list
                        val newEntry = WaitlistEntry(
                            lotteryId = lottery.id,
                            userId = currentDeviceId,
                            status = EntryStatus.WAITING
                        )

                        // Because `waitlist` is a List (immutable interface), create a updated copy
                        val updatedWaitlist = currentLottery.waitlist + newEntry
                        currentLottery.waitlist = updatedWaitlist

                        // the updated lottery object back to Firestore
                        dataRef.document(lottery.id).set(currentLottery)
                            .addOnSuccessListener { onResult(true) }
                            .addOnFailureListener { onResult(false) }
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false)
                }
            }
    }




}