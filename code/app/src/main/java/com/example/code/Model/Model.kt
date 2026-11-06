import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateListOf
import com.example.code.Model.DateRange
import com.example.code.Model.Lottery
import com.example.code.Model.Reservation
import com.google.firebase.Firebase
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import android.content.Context
import android.provider.Settings

interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{
    // lazy so making a model object doesnt need firebase running eg in unit tests
    // excluded so firestore doesnt try to save the database inside every object
    @get:Exclude
    val db by lazy { Firebase.firestore }

    // device id of whoever is using the app
    // named currentDeviceId so it doesnt clash with User.deviceId
    // excluded so it doesnt get saved into every document
    @get:Exclude
    var currentDeviceId: String = ""

    /**
     * function that sets the device id of the user, this is needed by all models,
     * reference the currentDeviceId attribute.
     *
     * @param deviceId this is the deviceId passed in by the view
     */
    fun setDeviceInformation(deviceId: String){
        this.currentDeviceId = deviceId
    }



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

    /**
     * gets all the details of a campsite
     *
     * @param campId id of the campsite
     * @param onResult gets the campsite or null if it wasnt found
     */
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
     * gets the booked dates of a campsite for its owner
     *
     * a date is booked when an active reservation covers it
     *
     * @param campId id of the campsite
     * @param onResult gets each stay sorted by check in or an empty list if it failed
     */
    fun getBookedDates(campId: String, onResult: (List<DateRange>) -> Unit) {
        reservationRef.whereEqualTo("campsiteId", campId).whereEqualTo("status", "ACTIVE").get()
            .addOnSuccessListener { documents ->
                // skip any reservation saved with bad dates instead of crashing
                val stays = documents.toObjects(Reservation::class.java)
                    .mapNotNull { runCatching { it.stay() }.getOrNull() }
                    .sortedBy { it.start }
                onResult(stays)
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
