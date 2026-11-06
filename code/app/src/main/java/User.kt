class User(val userId: String = "", val deviceId: String = "", var name: String = "", var email: String = "",
           var phone: String = "", val role: UserRole = UserRole.ENTRANT) {
}