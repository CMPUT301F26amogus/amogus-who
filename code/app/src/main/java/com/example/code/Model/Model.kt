import com.google.firebase.Firebase
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import com.example.code.Model.Campsite
import com.example.code.Model.DateRange
import com.example.code.Model.EntryStatus
import com.example.code.Model.Lottery
import com.example.code.Model.LotteryStatus
import com.example.code.Model.Notification
import com.example.code.Model.Reservation
import com.example.code.Model.ReservationStatus
import com.example.code.Model.Role
import com.example.code.Model.User
import com.example.code.Model.WaitlistEntry
import kotlin.String

interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{

    //region Properties and Firestore references

    // lazy so making a model object doesnt need firebase running eg in unit tests
    // excluded so firestore doesnt try to save the database inside every object
    @get:Exclude
    val db by lazy { Firebase.firestore }
    val dataRef = db.collection("EVERYTHING")

    @get:Exclude
    var currentDeviceId: String = "" // DeviceID of current user
    var campId: String = "" // Buffer for campsite ID
    var notificationCount: Int = 0 // Tracker for total notification count

    private val usersRef by lazy { db.collection("users") }
    private val campsiteRef by lazy { db.collection("campsite") }
    private val reservationRef by lazy { db.collection("reservations") }
    private val lotteryRef by lazy { db.collection("lotteries") }
    private val waitlistRef by lazy { db.collection("waitlist") }
    //endregion


    //region View registration and updates
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
    //endregion


    //region State held for the views
    public var campsiteList: ArrayList<Campsite> = arrayListOf()

    fun settCampsiteList(newArray: ArrayList<Campsite>?) {
        if (newArray != null) {
            campsiteList = newArray
        }
        notifyViews()
    }
    public var id: String = ""
        set(value) {
            field = value
            notifyViews()
        }

    /**
     * Register that just holds a single campsite cause why not
     */
    var tempCampsite: Campsite? = null

    fun gettTempCampsite(): Campsite? {
        return tempCampsite
    }

    fun settTempCampsite(campsite: Campsite?) {
        tempCampsite = campsite
        notifyViews()
    }

    /**
     * function that sets the device id of the user, this is needed by all models,
     * reference the currentDeviceId attribute.
     *
     * @param deviceId this is the deviceId passed in by the view
     */
    fun setDeviceInformation(deviceId: String){
        this.currentDeviceId = deviceId
    }
    //endregion


    //region Helper Functions for Model

    /**
     * Always check for null, if this is null then user doesnt exist.
     * Otherwise this returns user as an object
     */
    fun getUserObj( onResult: (User?) -> Unit) {
        dataRef.document(currentDeviceId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    onResult(user) // we have to use a callback because getting the data
                    // from the server is slow and so we need to wait for response
                } else {
                    onResult(null)
                }
            }
    }

    fun setNotifCount(onResult: (Int) -> Unit){
        dataRef.document(currentDeviceId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                notificationCount = user!!.notificationCount
                onResult(notificationCount)
            } else {
                onResult(0)
            }
        }
    }


    /**
     * Sets the user's object, useful after getUserObj()
     * WARNING: This will completely remake the ENTIRE document with this User object
     *
     * @param user replacement user
     */
    fun setUserObj(user:User){
        dataRef.document(currentDeviceId).set(user)
    }
    //endregion


    //region User accounts and profiles

    /**
     * Signs up entrant into the database
     * Create the entrant profile, with all their information.
     *
     *
     * @param name user's signUp name from view
     * @param phoneNumber user's phone number from the view
     * @param email user's email from the view
     */
    fun signUp(
        deviceId: String,
        name: String,
        phoneNumber: String,
        email: String,
        roleOf: Role= Role.ENTRANT,
        onSuccess: () -> Unit,
    ) {
        val newUser = User(name = name, email = email, phone = phoneNumber, role = roleOf)
        dataRef.document(deviceId).set(newUser)
            .addOnSuccessListener {
                notifyViews()
                onSuccess()
            }
    }

    /**
     * searches for deviceInformation across all entrants in database
     *
     * @return returns user if user is in database, else returns null.
     * the idea is that:
     *      if returns null, then the view is the sign-up page
     *      if returns user, then view is homepage
     */
    fun signIn(onResult: (User?) -> Unit){
        getUserObj(onResult)
    }

    fun getUserByDevice(deviceId: String, onResult: (User?) -> Unit) {
        usersRef.whereEqualTo("deviceId", deviceId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.documents[0].toObject(User::class.java)
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

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
    fun updateProfile(user: User, name: String="", email: String="", phoneNumber: String=""){
        if (name != "") {
            dataRef.document(currentDeviceId).update(
                "name", name
            )
        }
        if (email != ""){
            dataRef.document(currentDeviceId).update(
                "email", email
            )
        }
        if(phoneNumber != ""){
            dataRef.document(currentDeviceId).update(
                "phone", phoneNumber
            )
        }
        //notify views after change
        notifyViews()

    }

    /**
     * delete a profile (any role)
     *
     */
    fun deleteProfile(){
        dataRef.document(currentDeviceId).delete()
            .addOnSuccessListener {notifyViews()}
    }
    //endregion


    //region Campsites
    fun createCampsite(campsite: Campsite, onResult: (Boolean) -> Unit) {

        val doc = campsiteRef.document()
        campsite.id = doc.id

        doc.set(campsite)
            .addOnSuccessListener {
                notifyViews()
                onResult(true)
            }
    }

    fun updateCampsite(campsite: Campsite, campName: String, location: String, desc: String, maxOcc: Int, price: Double) {
        campsite.name = campName
        campsite.location = location
        campsite.description = desc
        campsite.maxOccupancy = maxOcc
        campsite.pricePerNight = price
        campsiteRef.document(campsite.id).set(campsite)
        notifyViews()
    }

    fun deleteCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.id).delete()
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
                notifyViews()
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun getCampsiteByOwner(ownerId: String, onResult: (List<Campsite>) -> Unit) {
        campsiteRef.whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { documents ->
                val campsites = documents.toObjects(Campsite::class.java)
                onResult(campsites)
                notifyViews()
            }
            .addOnFailureListener {
                onResult(emptyList())
                notifyViews()
            }
    }

    fun getAllCampsites(onResult: (List<Campsite>) -> Unit) {
        campsiteRef.get()
            .addOnSuccessListener { documents ->
                val campsites = documents.toObjects(Campsite::class.java)
                onResult(campsites)
                notifyViews()
            }
            .addOnFailureListener {
                onResult(emptyList())
                notifyViews()
            }
    }

    fun searchCampsite(campName: String, onResult: (List<Campsite>) -> Unit) {
        campsiteRef.whereEqualTo("campName", campName).get()
            .addOnSuccessListener { documents ->
                val campsite = documents.toObjects(Campsite::class.java)
                onResult(campsite)
                notifyViews()
            }
            .addOnFailureListener {
                onResult(emptyList())
                notifyViews()
            }
    }

    fun getCampId(campName: String, onResult: (String) -> Unit){
        campsiteRef.whereEqualTo("campName", campName).get()
            .addOnSuccessListener { documents ->
                val campsite = documents.toObjects(Campsite::class.java)
                onResult(campsite[0].id)
                notifyViews()
            }
    }
    //endregion


    //region Lotteries
    /**
     * saves a lottery to the database eg after changing its periods
     *
     * @param lottery the lottery to save
     */
    fun createLottery(lottery: Lottery) {
        lotteryRef.document(lottery.id).set(lottery)
    }

    fun deleteLottery(lottery: Lottery) {
        lotteryRef.document(lottery.id).delete()
    }

    fun updateLottery(lottery: Lottery, onResult: (Result<Unit>) -> Unit) {
        val updates = mapOf(
            "campsites" to lottery.campsites,
            "registrationStart" to lottery.registrationStart,
            "registrationEnd" to lottery.registrationEnd,
            "bookingStart" to lottery.bookingStart,
            "bookingEnd" to lottery.bookingEnd,
            "numWinners" to lottery.numWinners,
            "waitlistCapacity" to lottery.waitlistCapacity,
            "status" to lottery.status.name
        )

        lotteryRef.document(lottery.id)
            .update(updates)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
                notifyViews()
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    /**
     * Gets a list of lotteries that the user is in
     *
     *@param onResult gets the lotteries or an empty list if it failed
     */
    fun getLotteriesIn(onResult: (List<Lottery>) -> Unit) {
        getUserObj { user -> onResult(user!!.lotteries!!)}
        notifyViews()
    }

    /**
     * get lottery associated with campsite
     *
     * @param campsiteId id of the campsite
     */
    fun getLottery(campsiteId: String, onResult: (Lottery?) -> Unit) {
        dataRef.document(campsiteId).get().addOnSuccessListener {
                snapshot ->
            if (snapshot.exists()) {
                val campsite = snapshot.toObject(Campsite::class.java)
                onResult(campsite?.lottery)
                notifyViews()
            } else {
                onResult(null)
            }
        }
    }

    fun drawLotteryWinner(
        lotId: String,
        onResult: (Result<List<String>>) -> Unit
    ) {
        val lotteryDocument = lotteryRef.document(lotId)

        // First, retrieve the lottery.
        lotteryDocument.get()
            .addOnSuccessListener { lotteryDoc ->

                if (!lotteryDoc.exists()) {
                    onResult(Result.failure(Exception("Lottery not found")))
                    return@addOnSuccessListener
                }

                // Check whether registration has ended.
                val regEnd = lotteryDoc.getLong("regEnd") ?: 0L

                if (System.currentTimeMillis() < regEnd) {
                    onResult(Result.failure(Exception("Registration has not ended")))
                    return@addOnSuccessListener
                }

                // Check whether the lottery has already been drawn.
                val status = lotteryDoc.getString("lotteryStatus")

                if (status == LotteryStatus.DRAWN.name) {
                    onResult(Result.failure(Exception("Lottery has already been drawn")))
                    return@addOnSuccessListener
                }

                val winnerCount = lotteryDoc.getLong("winnerCount")?.toInt() ?: 0

                if (winnerCount <= 0) {
                    onResult(Result.failure(Exception("Invalid winner count")))
                    return@addOnSuccessListener
                }

                // Retrieve all entrants who are still waiting.
                waitlistRef
                    .whereEqualTo("lotId", lotId)
                    .whereEqualTo("waitlistStatus", EntryStatus.WAITING.name)
                    .get()
                    .addOnSuccessListener { documents ->

                        val entries = documents.documents

                        if (entries.size < winnerCount) {
                            onResult(
                                Result.failure(Exception("Not enough eligible entrants"))
                            )
                            return@addOnSuccessListener
                        }

                        // Randomly select winners.
                        val winners = entries.shuffled().take(winnerCount)
                        val winnerIds = winners.mapNotNull {
                            it.getString("entrantId")
                        }

                        val winnerEntryIds = winners.map { it.id }.toSet()

                        // Update all waitlist entries and the lottery.
                        val batch = db.batch()

                        for (entry in entries) {
                            val newStatus =
                                if (entry.id in winnerEntryIds) {
                                    EntryStatus.SELECTED
                                } else {
                                    EntryStatus.NOT_SELECTED
                                }

                            batch.update(
                                entry.reference,
                                "waitlistStatus",
                                newStatus.name
                            )
                        }

                        batch.update(
                            lotteryDocument,
                            "lotteryStatus",
                            LotteryStatus.DRAWN.name
                        )

                        batch.update(
                            lotteryDocument,
                            "winnerIds",
                            winnerIds
                        )

                        batch.commit()
                            .addOnSuccessListener {
                                onResult(Result.success(winnerIds))
                            }
                            .addOnFailureListener { exception ->
                                onResult(Result.failure(exception))
                            }
                    }
                    .addOnFailureListener { exception ->
                        onResult(Result.failure(exception))
                    }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }
    //endregion


    //region Waitlist
    fun joinWaitlistt(lotId: String, entrantId: String, onResult: (Result<Unit>) -> Unit) {
        val entryId = "${lotId}_${entrantId}"
        val entryRef = waitlistRef.document(entryId)

        entryRef.get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getString("waitlistStatus") == "WAITING") {
                    onResult(Result.failure(
                        Exception("You have already joined this waitlist")
                    ))
                } else {
                    val entry = WaitlistEntry(
                        lotteryId = lotId, userId = entrantId, joinedAt = System.currentTimeMillis(),
                        status = EntryStatus.WAITING)

                    entryRef.set(
                        mapOf(
                            "lotId" to entry.lotteryId,
                            "entrantId" to entry.userId,
                            "waitlistStatus" to entry.status.name,
                            "joinedAt" to entry.joinedAt
                        )
                    ).addOnSuccessListener { onResult(Result.success(Unit)) }
                        .addOnFailureListener { exception -> onResult(Result.failure(exception)) }
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // entrants should be able to join the waiting list
    /**
     * Adds the current user to the waitlist of the specified lottery.
     *
     * @param lottery Target lottery to join
     * @param onResult Callback returning true if joined successfully, false if full, already joined, or failed
     */
    fun joinWaitlist(lottery: Lottery, onResult: (Boolean) -> Unit) {
        //if you join a waitlist it should also update the lottery list in the user
        // that is the user has a list of lotteries that they are in
        var tempuser: User? = null
        getUserObj { user -> tempuser = user }
        dataRef.document(lottery.id).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val currentLottery = snapshot.toObject(Lottery::class.java)

                    if (currentLottery != null) {
                        //Check if user is already in the waitlist
                        if (currentLottery.waitlist.any { it.userId == currentDeviceId }) {
                            onResult(false)
                            return@addOnSuccessListener
                        }

                        //Check if waitlist has reached maximum capacity (if capacity is set)
                        val capacity = currentLottery.waitlistCapacity
                        if (capacity != null && currentLottery.waitlist.size >= capacity) {
                            onResult(false) // Waitlist is full
                            return@addOnSuccessListener
                        }

                        //Create the new entry and append it to a new list
                        val newEntry = WaitlistEntry(
                            lotteryId = lottery.id,
                            userId = currentDeviceId,
                            status = EntryStatus.WAITING
                        )

                        // Because `waitlist` is a List (immutable interface), create a updated copy
                        val updatedWaitlist = currentLottery.waitlist + newEntry
                        currentLottery.waitlist = updatedWaitlist

                        // adds the user to the waitlist in the lottery
                        dataRef.document(lottery.id).set(currentLottery)
                            .addOnSuccessListener {
                                // sneakily adds the lottery to the user's waitlist as well :)
                                tempuser!!.lotteries!!.add(lottery)
                                setUserObj(tempuser!!)
                                onResult(true)
                                notifyViews()
                            }
                            .addOnFailureListener { onResult(false) }
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false)
                }
            }
    }

    fun leaveWaitlistt(lotId: String, entrantId: String, onResult: (Result<Unit>) -> Unit) {
        val entryId = "${lotId}_${entrantId}"

        waitlistRef.document(entryId)
            .update("waitlistStatus", "LEFT")
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // entrants need to be able to leave waitlist
    /**
     * leaves the waitlist of the lottery
     *
     * @param lottery Lottery that the entrant wants to leave waitlist from
     */
    fun leaveWaitlist(lottery: Lottery, onResult: (Boolean) -> Unit) {
        dataRef.document(lottery.id).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val tempLottery = snapshot.toObject(Lottery::class.java)

                    if (tempLottery != null) {
                        // Update the user's status in the waitlist
                        tempLottery.waitlist.forEach { entry ->
                            if (entry.userId == currentDeviceId) {
                                entry.status = EntryStatus.DECLINED
                            }
                        }

                        // Save the updated lottery object back to Firestore
                        dataRef.document(lottery.id).set(tempLottery)
                            .addOnSuccessListener {
                                onResult(true) // Successfully updated database
                                notifyViews()
                            }
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false) // Document does not exist
                }
            }
    }

    fun getWaitlist(lotId: String, onResult: (Result<List<WaitlistEntry>>) -> Unit) {
        waitlistRef.whereEqualTo("lotId", lotId)
            .whereEqualTo("waitlistStatus", EntryStatus.WAITING.name)
            .get()
            .addOnSuccessListener { documents ->
                val entries = documents.toObjects(WaitlistEntry::class.java)

                onResult(Result.success(entries))
                notifyViews()
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    /**
     * gets the entire waitlist that the user is in, these are a list of lotteries
     *
     * @return Returns a list of lotteries that the user is in
     */
    fun getWaitlist(onResult: (List<Lottery>) -> Unit){
        val lotteries = ArrayList<Lottery>()
        dataRef.document(currentDeviceId).get().addOnSuccessListener {
                snapshot ->
            if (snapshot.exists()){
                val user = snapshot.toObject(User::class.java)
                lotteries.addAll(user!!.lotteries!!)
            }
            onResult(lotteries)
            notifyViews()
        }
    }
    //endregion


    //region Reservations
    /**
     * Create a reservation for a campsite
     *
     * @param checkIn check in date
     * @param checkOut check out date
     * @param guests number of guests
     * @param campsite the campsite to book
     */
    fun makeReservation(checkIn: String, checkOut: String, guests: Int, campsite: Campsite){
        val newReservation = Reservation(checkIn = checkIn, checkOut = checkOut, guests = guests, campsite = campsite)
        dataRef.add(newReservation).addOnSuccessListener {
                documentReference ->
            newReservation.id = documentReference.id
            newReservation.status = ReservationStatus.ACTIVE
            getUserObj(){ user -> newReservation.entrant = user }
            dataRef.document(documentReference.id).set(newReservation)
        }
    }

    fun rejectReservation(reservationId: String, onResult: (Result<Unit>) -> Unit) {
        val reservationDoc = reservationRef.document(reservationId)

        reservationDoc.get()
            .addOnSuccessListener { document ->
                if(!document.exists()) {
                    onResult(Result.failure(Exception("Reservation not found")))
                    return@addOnSuccessListener
                }

                val status = document.getString("reservationStatus")

                if (status != ReservationStatus.ACTIVE.name) {
                    onResult(Result.failure(Exception("Reservation is not pending")))
                    return@addOnSuccessListener
                }

                reservationDoc.update(
                    "reservationStatus", ReservationStatus.REJECTED.name
                )
                    .addOnSuccessListener {
                        onResult(Result.success(Unit))
                        notifyViews()
                    }
                    .addOnFailureListener { exception ->
                        onResult(Result.failure(exception))
                    }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    /**
     * removes user from reservation, this allows user to decline reservation
     *
     *@param reservation the Reservation object that the user is declining
     */
    fun rejectReservation(reservation: Reservation){
        // I'm assuming that view people will pass in the reservation, because the user needs to click
        // on the reservation that they will cancel from a list of reservations.

        dataRef.document(reservation.id).update(
            "status", ReservationStatus.REJECTED
        )
        notifyViews()
    }

    // models need a way to cancel reservation, I guess a canceled reservation is
    // one where the lottery winner's havent been decided yet?

    /**
     * Cancels the user's reservation
     *
     * @param reservation The reservation object
     */
    fun cancelReservation(reservation: Reservation){
        dataRef.document(reservation.id).update(
            "status", ReservationStatus.CANCELLED
        )
        notifyViews()
    }

    fun getReservationByUID(entrantId: String, onResult: (Result<List<ReservationStatus>>) -> Unit) {
        reservationRef.whereEqualTo("entrantId", entrantId).get()
            .addOnSuccessListener { documents ->
                val statuses = documents.documents.mapNotNull {
                    val status = it.getString("reservationStatus")

                    status?.let {
                        ReservationStatus.valueOf(it)
                    }
                }

                onResult(Result.success(statuses))
                notifyViews()
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
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
                notifyViews()
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
                notifyViews()
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
                notifyViews()
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
    //endregion


    //region Notifications
    /**
     * Sends a notification to the user
     *
     * @param notification The notification message
     */
    fun notifyUser(notification: String) {
        var tempuser: User? = null
        getUserObj({ user -> tempuser = user })
        // if user has notifs enabled
        if (tempuser!!.notificationsEnabled) {
            // creates a document in Notifs folder and device ID folder, for every notif count
            dataRef.document("Notifications/$currentDeviceId/$notificationCount").set(Notification("", currentDeviceId, notification))
        }
        tempuser!!.notificationCount++
        setUserObj(tempuser!!)
    }

    /**
     * gets all notifications from the device ID folder's notifications
     *
     * @param onResult gets the notifications or an empty list if it failed
     */
    fun getNotifications(onResult: (List<Notification>) -> Unit){
        val notifications = ArrayList<Notification>()

        setNotifCount {notificationCount = it}

        for (i in 0..notificationCount){
            // gets all notifications from the device ID folder's notifications
            dataRef.document("Notifications/$currentDeviceId/$i").get().addOnSuccessListener {
                if (it.exists()){
                    notifications.add(it.toObject(Notification::class.java)!!)
                }
            }
            onResult(notifications)
            notifyViews()
        }
    }
    //endregion

}