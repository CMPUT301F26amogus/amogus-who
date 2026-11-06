// I changed userId to string because we will be using userId to sort through documents
// documents expect strings. - Hui Yan
class User(val userId: String="", var name: String="", var email: String="",
           var phone: String="", val role: UserRole?=null): Model() {


}