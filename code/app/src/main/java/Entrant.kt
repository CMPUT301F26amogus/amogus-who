class Entrant(var user: User, var notificationPref: Boolean) {

    public fun updateProfile(name: String, email: String, phone: String) {
        user.name = name
        user.email = email
        user.phone = phone
    }


}