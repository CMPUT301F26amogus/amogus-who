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
        val newUser = User(deviceId, name, email=email,phone=phoneNumber)
        dataRef.document(deviceId).set(User())

    }




    /**
     * This function is for signing the user into the app, make sure to
     *
     * @return returns user if user is in database, else returns null.
     * the idea is that:
     *      if returns null, then the view is the sign-up page
     *      if returns user, then view is homepage
     */
    fun signIn(): User?{
        return getUserObj()
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
            dataRef.document(user.deviceId).update(
                "name", name
            )
        }
        if (email != ""){
            dataRef.document(user.deviceId).update(
                "email", email
            )
        }
        if(phoneNumber != ""){
            dataRef.document(user.deviceId).update(
                "phone", phoneNumber
            )
        }
    }



    /**
     * removes user from list of lottery winners
     *
     *
     */
    fun declineReservation(){
        // entrants should be able to decline a lottery win, but we dont have any connections set up


    }

}