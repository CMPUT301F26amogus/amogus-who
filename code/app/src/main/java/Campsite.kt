class Campsite(val ownerId: Int, val campId: Int, var campName: String,
               var location: String, var desc: String, var maxOcc: Int,
               var price: Double, var rating: Double): Model() {
}