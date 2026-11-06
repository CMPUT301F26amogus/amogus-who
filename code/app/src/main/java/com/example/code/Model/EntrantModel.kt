package com.example.code.Model

import Model
import androidx.compose.animation.core.snap

class EntrantModel: Model() {



    /**
     *
     *
     * deletes an entrant based on the username
     * and returns true upon successful deletion
     * returns false if username wasnt found
     *
     * @param entrant 'username of entrant'
     * @return 'true if deleted, false if couldnt delete'
     */
    fun deleteProfile(){

    }

    /**
     * 'updates the information of an entrant'
     *
     * 'detailed description and/or uses'
     *
     * '(if applicable) fail triggers and fail response'
     *
     * @param 'entrant' 'username of entrant'
     */
    fun updateEntrant(entrant: String){
        // look for entrant in the database,

    }


    /**
     * Signs up entrant into the database, this should always be called when new signup
     * Create the entrant profile, with all their information.
     *
     *
     * @param deviceInformation entrants device identification number // REPLACE
     * @param name user's signUp name from view
     * @param phoneNumber user's phone number from the view
     * @param email user's email from the view
     */
    fun signUp(deviceInformation: String, name: String, phoneNumber: String, email: String){
        val newUser = User(deviceInformation, name, email=email,phone=phoneNumber)
        userRef.document(deviceInformation).set(User())

    }

    /**
     * auto sign in, run this function to determine if the user is a new user -> register page
     * or if user is in the database.
     *
     * @param deviceInformation user's device information, we use this to find the user
     * @return true if
     */
    fun doesUserExist(deviceInformation: String): Boolean{
        var exist = false
        userRef.document(deviceInformation).get().addOnSuccessListener {
            snapshot -> //snapshot represents the state of the document
            exist = snapshot.exists(); // .exists() checks if the document exists
        }
        return exist // return if the document exists.
    }


    /**
     * This function is for signing the user into the app, make sure to
     * check if the user exists with "doesUserExist" function, before running signIn
     */
    fun signIn() {
        // not written yet
    }


    /**
     * searches for deviceInformation across all entrants in database
     *
     *
     * '(if applicable) fail triggers and fail response'
     *
     * @param deviceInformation 'description of the inputted variable'
     * @return Entrant, this should return the entrant found, or something else undecided if not found
     */
    fun login(deviceInformation: String): Boolean{

        return true // havent implemented returning entrant
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

    private val userRef = db.collection("Users")

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
            userRef.document(user.deviceId).update(
                "name", name
            )
        }
        if (email != ""){
            userRef.document(user.deviceId).update(
                "email", email
            )
        }
        if(phoneNumber != ""){
            userRef.document(user.deviceId).update(
                "phone", phoneNumber
            )
        }
    }




    /**
     * Deletes the user's profile from the database
     *
     * This will work even if the user isnt in the database.
     *
     * @param user user's object
     */
    fun deleteProfile(user: User){
        userRef.document(user.deviceId).delete()
    }

    /**
     * Lets user decline a lottery win
     *
     * @param user the user that won the lottery
     *
     */

    fun declineWin(){
        // entrants should be able to decline a lottery win, but we dont have any connections set up






    }

}