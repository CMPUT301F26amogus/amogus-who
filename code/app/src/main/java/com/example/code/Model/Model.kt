import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{
    val db = Firebase.firestore
    /*
    private val userRef = db.collection("User Data")
    private val CampsiteRef = db.collection("Campsite Data")
    private val adminRef = db.collection("Admin Data")

    boiler plate code, for each collection in the database, idk yet
    */



    override fun addView(v: View){

    }
    override fun removeView(v: View){

    }

    private val db = Firebase.firestore
    private val usersRef = db.collection("users")
    private val campsiteRef = db.collection("campsite")

    fun createCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.campId).set(campsite)
    }

    fun updateCampsite(campsite: Campsite, campName: String, location: String, desc: String, maxOcc: Int, price: Double,
                       rating: Double) {
        campsite.campName = campName
        campsite.location = location
        campsite.desc = desc
        campsite.maxOcc = maxOcc
        campsite.price = price
        campsite.rating = rating
        campsiteRef.document(campsite.campId).set(campsite)
    }

    fun deleteCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.campId).delete()
    }

    fun getCampsite(campId: String, onResult: (Campsite?) -> Unit) {
        campsiteRef.document(campId).get()
            .addOnSuccessListener { document ->
                val campsite = document.toObject(Campsite::class.java)
                onResult(campsite)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun searchCampsite(campName: String, onResult: (List<Campsite>) -> Unit) {
        campsiteRef.whereEqualTo("campName", campName).get()
            .addOnSuccessListener { documents ->
                val campsite = documents.toObjects(Campsite::class.java)
                onResult(campsite)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}