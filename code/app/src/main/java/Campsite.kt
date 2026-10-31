class Campsite(val ownerId: Int, var campId: Int, val campName: String,
               val location: String, val desc: String, val maxOcc: Int,
               val price: Double, val rating: Double): Model() {
}