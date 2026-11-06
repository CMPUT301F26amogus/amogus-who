import androidx.compose.runtime.mutableStateListOf
import com.example.code.Model.Lottery
import com.example.code.Model.Reservation
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{
    // lazy so making a model object doesnt need firebase running eg in unit tests
    val db by lazy { Firebase.firestore }
    /*
    private val userRef = db.collection("User Data")
    private val CampsiteRef = db.collection("Campsite Data")
    private val adminRef = db.collection("Admin Data")

    boiler plate code, for each collection in the database, idk yet
    */



    // views watching this model
    private val views = mutableListOf<View>()

    /**
     * registers a view so it gets updated when this model changes
     *
     * does nothing if the view is already registered
     *
     * @param v the view to add
     */
    override fun addView(v: View){
        if (v !in views) views.add(v)
    }

    /**
     * stops a view from getting updates eg when its screen is destroyed
     *
     * @param v the view to remove
     */
    override fun removeView(v: View){
        views.remove(v)
    }

    /** tells every registered view this model changed so they can redraw */
    protected fun notifyViews(){
        views.forEach { it.update(this) }
    }

    private val usersRef by lazy { db.collection("users") }
    private val campsiteRef by lazy { db.collection("campsite") }
    private val reservationRef by lazy { db.collection("reservations") }
    private val lotteryRef by lazy { db.collection("lotteries") }

    fun updateCampsite(campsite: Campsite, campName: String, location: String, desc: String, maxOcc: Int, price: Double,
                       rating: Double) {
        campsite.campName = campName
        campsite.location = location
        campsite.desc = desc
        campsite.maxOcc = maxOcc
        campsite.price = price
        campsite.rating = rating
        campsiteRef.document(campsite.campId).set(campsite)
        notifyViews()
    }

    fun deleteCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.campId).delete()
        notifyViews()
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

    /**
     * gets all the details of a campsite
     *
     * @param campId id of the campsite
     * @param onResult gets the campsite or null if it wasnt found
     */
    fun getCampsiteInfo(campId: String, onResult: (Campsite?) -> Unit) {
        campsiteRef.document(campId).get()
            .addOnSuccessListener { document ->
                val campsite = document.toObject(Campsite::class.java)
                onResult(campsite)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    /**
     * gets an entrants reservations that are still active
     *
     * @param entrantId device id of the entrant
     * @param onResult gets the reservations or an empty list if it failed
     */
    fun getActiveReservations(entrantId: String, onResult: (List<Reservation>) -> Unit) {
        reservationRef.whereEqualTo("entrantId", entrantId).whereEqualTo("status", "ACTIVE").get()
            .addOnSuccessListener { documents ->
                val reservations = documents.toObjects(Reservation::class.java)
                onResult(reservations)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * gets an entrants cancelled reservations for the reservations view
     *
     * @param entrantId device id of the entrant
     * @param onResult gets the reservations or an empty list if it failed
     */
    fun getCancelledReservations(entrantId: String, onResult: (List<Reservation>) -> Unit) {
        reservationRef.whereEqualTo("entrantId", entrantId).whereEqualTo("status", "CANCELLED").get()
            .addOnSuccessListener { documents ->
                val reservations = documents.toObjects(Reservation::class.java)
                onResult(reservations)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * saves a lottery to the database eg after changing its periods
     *
     * @param lottery the lottery to save
     */
    fun saveLottery(lottery: Lottery) {
        lotteryRef.document(lottery.id).set(lottery)
    }

}
