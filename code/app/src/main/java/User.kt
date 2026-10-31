class User(val userId: Int, var name: String, var email: String,
           var phone: String, val role: UserRole): Model() {
}