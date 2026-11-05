package com.example.code.Model

import Model

class EntrantModel: Model() {

    // just making the function outlines so we can do the UML


    /**
     * deletes an entrant from the database
     *
     * deletes an entrant based on the username
     * and returns true upon successful deletion
     * returns false if username wasnt found
     *
     * @param 'entrant' 'username of entrant'
     * @return 'true if deleted, false if couldnt delete'
     */
    fun deleteEntrant(entrant: String): Boolean{


        return false
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
     * @param deviceInformation entrants device identification number MAC address?
     */
    fun signUp(deviceInformation: String){

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

    fun declineWin(user: User){
        // entrants should be able to decline a lottery win, but we dont have any connections set up
        // like the entrants should have a list of lotteries they are participating in
        // I think, but I still need to ask the rest of the group how these classes are working together.
    }

}