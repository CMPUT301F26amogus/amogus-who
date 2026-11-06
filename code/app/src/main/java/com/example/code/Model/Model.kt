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
import com.example.code.Model.User
import com.example.code.Model.WaitlistEntry
import kotlin.String

interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{
    // lazy so making a model object doesnt need firebase running eg in unit tests
    // excluded so firestore doesnt try to save the database inside every object
    @get:Exclude
    val db by lazy { Firebase.firestore }
    val dataRef = db.collection("EVERYTHING")

    // device id of whoever is using the app
    // named currentDeviceId so it doesnt clash with User.deviceId
    // excluded so it doesnt get saved into every document
    @get:Exclude
    var currentDeviceId: String = ""
    var campId: String = ""
    var notificationCount: Int = 0



    // HELPER FUNCTIONS FOR US MODEL PEOPLES, use at your own caution

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
                                    // from the server is slow and so we need to
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






    // Everything under this line should be functions that are used by the view
    /**
     * function that sets the device id of the user, this is needed by all models,
     * reference the currentDeviceId attribute.
     *
     * @param deviceId this is the deviceId passed in by the view
     */
    fun setDeviceInformation(deviceId: String){
        this.currentDeviceId = deviceId
    }

    /**
     * delete a profile (any role)
     *
     */
    fun deleteProfile(){
        dataRef.document(currentDeviceId).delete()
    }



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
    private val waitlistRef by lazy { db.collection("waitlist") }

    public var campsiteList: ArrayList<Campsite> = arrayListOf()
        fun setCampsite(newArray: ArrayList<Campsite>) {
            campsiteList = newArray
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

    fun getTempCampsite(): Campsite? {
        return tempCampsite
    }

    fun setTempCampsite(campsite: Campsite?) {
        tempCampsite = campsite
        notifyViews()
    }




    fun createCampsite(campsite: Campsite) {
        campsiteRef.document(campsite.id).set(campsite)
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
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getAllCampsites(onResult: (List<Campsite>) -> Unit) {
        campsiteRef.get()
            .addOnSuccessListener { documents ->
                val campsites = documents.toObjects(Campsite::class.java)
                onResult(campsites)
            }
            .addOnFailureListener {
                onResult(emptyList())
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

    fun createLottery(lottery: Lottery) {
        lotteryRef.document(lottery.id).set(lottery)
    }

    fun deleteLottery(lottery: Lottery) {
        lotteryRef.document(lottery.id).delete()
    }

    fun updateLottery(lottery: Lottery, onResult: (Result<Unit>) -> Unit) {
        val updates = mapOf(
            "campsiteIds" to lottery.campsiteIds,
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
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    fun joinWaitlist(lotId: String, entrantId: String, onResult: (Result<Unit>) -> Unit) {
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

    fun leaveWaitlist(lotId: String, entrantId: String, onResult: (Result<Unit>) -> Unit) {
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

    fun getWaitlist(lotId: String, onResult: (Result<List<WaitlistEntry>>) -> Unit) {
        waitlistRef.whereEqualTo("lotId", lotId)
            .whereEqualTo("waitlistStatus", EntryStatus.WAITING.name)
            .get()
            .addOnSuccessListener { documents ->
                val entries = documents.toObjects(WaitlistEntry::class.java)

                onResult(Result.success(entries))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
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
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { exception ->
                        onResult(Result.failure(exception))
                    }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
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
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    fun getCampId(campName: String, onResult: (String) -> Unit){
        campsiteRef.whereEqualTo("campName", campName).get()
            .addOnSuccessListener { documents ->
                val campsite = documents.toObjects(Campsite::class.java)
                onResult(campsite[0].id)
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
    fun setLottery(lottery: Lottery) {
        lotteryRef.document(lottery.id).set(lottery)
    }

    /**
     * Updates lottery information, right now only updates
     * registration period, booking period, and number of winners
     *
     * @param lottery the lottery to update
     */
    fun updateLottery(lottery: Lottery) {
        lotteryRef.document(lottery.id).update(
            "registrationPeriod", lottery.registrationPeriod(),
            "bookingPeriod", lottery.bookingPeriod(),
            "numWinners", lottery.numWinners
        )
        notifyViews()
    }

    /**
     * Gets a list of lotteries that the user is in
     *
     *@param onResult gets the lotteries or an empty list if it failed
     */
    fun getLotteries(onResult: (List<Lottery>) -> Unit) {
        getUserObj { user -> onResult(user!!.lotteries!!)}
    }





    /**
     *
     */

    /**
     * Signs up entrant into the database
     * Create the entrant profile, with all their information.
     *
     *
     * @param name user's signUp name from view
     * @param phoneNumber user's phone number from the view
     * @param email user's email from the view
     */
    fun signUp(name: String, phoneNumber: String, email: String){
        val newUser = User(currentDeviceId, name, email=email,phone=phoneNumber)
        dataRef.document(currentDeviceId).set(User())

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
    }




    /**
     * removes user from reservation, this allows user to decline reservation
     *
     *@param reservation the Reservation object that the user is declining
     */
    fun declineReservation(reservation: Reservation){
        // entrants should be able to decline a lottery win, that is we change the reservation status
        // I'm assuming that view people will pass in the reservation, because the user needs to click
        // on the reservation that they will cancel from a list of reservations.

        dataRef.document(reservation.id).update(
            "status", ReservationStatus.REJECTED
        )
    }

    // models need a way to cancel reservation, I guess a canceled reservation is
    // one where the lottery winner's havent been decided yet?

    /**
     * Cancels the user's reservation
     *
     * @param reservation The reservation object
     */
    fun cancelReservation(reservation: com.example.code.Model.Reservation){
        dataRef.document(reservation.id).update(
            "status", ReservationStatus.CANCELLED
        )
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
                            }
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false) // Document does not exist
                }
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
        }
    }




    /**
     * Sends a notification to the user
     *
     * @param notification The notification message
     */
    fun notifyUser(notification: String) {
        var tempuser: User? = null
        getUserObj({ user -> tempuser = user })

        if (tempuser!!.notificationsEnabled) {
            dataRef.document("Notifications/$currentDeviceId/$notificationCount").set(Notification("", currentDeviceId, notification))
        }
        tempuser!!.notificationCount++
        setUserObj(tempuser!!)
    }

    /**
     *
     */
    fun getNotifications(onResult: (List<Notification>) -> Unit){
        val notifications = ArrayList<Notification>()

        setNotifCount {notificationCount = it}

        for (i in 0..notificationCount){
            dataRef.document("Notifications/$currentDeviceId/$i").get().addOnSuccessListener {
                if (it.exists()){
                    notifications.add(it.toObject(Notification::class.java)!!)
                }
            }
            onResult(notifications)
        }
    }



    // Add

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
            } else {
                onResult(null)
            }
        }
    }




}
