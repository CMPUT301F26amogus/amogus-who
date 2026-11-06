class Entrant(val user: User, var notificationPref: Boolean): Model() {

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
    fun updateProfile(user: User,name: String="", email: String="", phoneNumber: String=""){
        if (name != "") {
            user.name = name;
        }
        if (email != ""){
            user.email = email
        }
        if(phoneNumber != ""){
            user.phone = phoneNumber
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
        userRef.document(user.userId).delete()
    }



}