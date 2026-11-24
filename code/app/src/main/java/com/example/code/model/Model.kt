
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlin.String

import com.example.code.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}



open class Model: ModelUpdater<View>{

    // All the code below is refactored with Claude Sonnet 5.5
    // Prompt: "Fix this, double tripple check, ask me if you nee help"
    // 2026-11-23

    //region Properties and Firestore references

    // lazy so making a model object doesnt need firebase running eg in unit tests
    // excluded so firestore doesnt try to save the database inside every object
    @get:Exclude
    val db by lazy { Firebase.firestore }

    @get:Exclude
    var currentDeviceId: String = "" // DeviceID of current user
    var campId: String = "" // Buffer for campsite ID
    var notificationCount: Int = 0 // Tracker for total notification count

    // Top collections
    private val usersRef by lazy { db.collection("users") } // admin, owner, and entrants
    private val campsiteRef by lazy { db.collection("campsites") } // campsites and their availibilities
    private val reservationRef by lazy { db.collection("reservations") } // winners have their reservations stored here
    private val lotteryRef by lazy { db.collection("lotteries") } // lotteries and their entries/waitlist
    private val notificationRef by lazy { db.collection("notifications") }
    // we will need these in the future so why not
    private val reviewRef by lazy { db.collection("reviews") }
    private val messageRef by lazy { db.collection("messages") }
    private val imageRef by lazy { db.collection("images") }

    // Subcollections
    // waitlist entries live under their lottery: lotteries/{lotId}/entrants/{lotId}_{deviceId}
    // entry is a WaitlistEntry object
    private fun waitlistRef(lotId: String) = lotteryRef.document(lotId).collection("entrants") // collection "entrants" will hold the waitlists for the entrants
    // availability lives under its campsite: campsites/{campId}/availability/{docId}
    // It could be better to make document for each date? that could be easier querying, but idk it'll bloat our database up
    private fun availabilityRef(campId: String) = campsiteRef.document(campId).collection("availability")
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
        usersRef.document(currentDeviceId).get()
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

    /**
     * gets how many notifications the current user has and keeps it in [notificationCount]
     *
     * @param onResult gets the count or 0 if the user doesnt exist
     */
    fun setNotifCount(onResult: (Int) -> Unit){
        usersRef.document(currentDeviceId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                // users now hold a list of notification ids instead of a count
                notificationCount = user?.notifications?.size ?: 0
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
        usersRef.document(currentDeviceId).set(user)
    }
    //endregion


    //region User accounts and profiles

    /**
     * Signs up entrant into the database
     * Create the entrant profile, with all their information.
     *
     * the account is saved under [currentDeviceId]
     *
     * @param name user's signUp name from view
     * @param phoneNumber user's phone number from the view
     * @param email user's email from the view
     * @param roleOf the starting role of the user
     *
     *
     *  @throws IllegalArgumentException if name is blank
     *  @throws IllegalArgumentException if it doesnt look like a phone number
     *  @throws IllegalArgumentException if it doesnt look like an email
     */
    fun signUp(
        name: String,
        phoneNumber: String,
        email: String,
        roleOf: Role= Role.ENTRANT,
        onSuccess: () -> Unit,
    ) {
        // deviceId has to be set on the user so the saved user has it, User equality is based on it
        val newUser = User(deviceId = currentDeviceId, name = name, email = email, phone = phoneNumber, role = roleOf)
        usersRef.document(currentDeviceId).set(newUser)
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

    /**
     * Takes a user profile and edits the user properties
     *
     * 'This function doesnt do any filtering for invalid phone numbers or emails, or names'
     *
     * @param name optional, set new name of the user
     * @param email optional, set new email for user
     * @param phoneNumber optional, set new phone number for user
     */
    fun updateProfile( name: String="", email: String="", phoneNumber: String=""){
        if (name != "") {
            usersRef.document(currentDeviceId).update(
                "name", name
            )
        }
        if (email != ""){
            usersRef.document(currentDeviceId).update(
                "email", email
            )
        }
        if(phoneNumber != ""){
            usersRef.document(currentDeviceId).update(
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
        usersRef.document(currentDeviceId).delete()
            .addOnSuccessListener {notifyViews()}
    }
    //endregion


    //region Campsites
    fun createCampsite(campsite: Campsite, onResult: (Boolean) -> Unit) {

        val doc = campsiteRef.document()
        campsite.id = doc.id

        doc.set(campsite)
            .addOnSuccessListener {
                onResult(true)
                notifyViews()
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

    /**
     * gets the campsites the current user owns
     *
     * @param onResult gets the campsites or an empty list if it failed
     */
    fun getCampsiteByOwner(onResult: (List<Campsite>) -> Unit) {
        campsiteRef.whereEqualTo("ownerId", currentDeviceId).get()
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
        // the campsite field is "name" now, it used to be "campName"
        campsiteRef.whereEqualTo("name", campName).get()
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
        campsiteRef.whereEqualTo("name", campName).get()
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
     * users dont hold a list of lotteries anymore so this finds the users waitlist
     * entries across every lottery and loads the lotteries they belong to
     * entries the user left (DECLINED) dont count
     *
     * this needs a collection group index on "entrants" for the field "deviceId",
     * firestore logs a link to make it the first time this runs without one
     *
     *@param onResult gets the lotteries or an empty list if it failed
     */
    fun getLotteriesIn(onResult: (List<Lottery>) -> Unit) {
        db.collectionGroup("entrants")
            .whereEqualTo("deviceId", currentDeviceId)
            .get()
            .addOnSuccessListener { documents ->
                val lotteryIds = documents.toObjects(WaitlistEntry::class.java)
                    .filter { it.status != EntryStatus.DECLINED }
                    .map { it.lotteryId }
                    .distinct()

                if (lotteryIds.isEmpty()) {
                    onResult(emptyList())
                    notifyViews()
                    return@addOnSuccessListener
                }

                Tasks.whenAllSuccess<DocumentSnapshot>(lotteryIds.map { lotteryRef.document(it).get() })
                    .addOnSuccessListener { snapshots ->
                        onResult(snapshots.mapNotNull { it.toObject(Lottery::class.java) })
                        notifyViews()
                    }
                    .addOnFailureListener { onResult(emptyList()) }
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    /**
     * get lottery associated with campsite
     *
     * @param campsiteId id of the campsite
     */
    fun getLottery(campsiteId: String, onResult: (Lottery?) -> Unit) {
        campsiteRef.document(campsiteId).get().addOnSuccessListener {
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

    /**
     * draws the winners of a lottery from the entrants still waiting
     *
     * the lottery has to be OPEN and its registration period has to be over
     * winners get SELECTED, everyone else NOT_SELECTED and the lottery becomes DRAWN
     *
     * @param lotId id of the lottery
     * @param onResult gets the device ids of the winners or the failure
     */
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
                // registrationEnd is saved as a string like 2027-05-01 and the end day is exclusive
                // if it was never set there is nothing to wait for
                val regEnd = lotteryDoc.getString("registrationEnd")
                    ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

                if (regEnd != null && LocalDate.now().isBefore(regEnd)) {
                    onResult(Result.failure(Exception("Registration has not ended")))
                    return@addOnSuccessListener
                }

                // Check whether the lottery has already been drawn.
                val status = lotteryDoc.getString("status")

                if (status != null && status != LotteryStatus.OPEN.name) {
                    onResult(Result.failure(Exception("Lottery has already been drawn")))
                    return@addOnSuccessListener
                }

                val winnerCount = lotteryDoc.getLong("numWinners")?.toInt() ?: 0

                if (winnerCount <= 0) {
                    onResult(Result.failure(Exception("Invalid winner count")))
                    return@addOnSuccessListener
                }

                // Retrieve all entrants who are still waiting.
                // the waitlist is already inside this lottery so no need to filter by lottery id
                waitlistRef(lotId)
                    .whereEqualTo("status", EntryStatus.WAITING.name)
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
                        // WaitlistEntry stores the entrants device id as "deviceId"
                        val winnerIds = winners.mapNotNull {
                            it.getString("deviceId")
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
                                "status",
                                newStatus.name
                            )
                        }

                        batch.update(
                            lotteryDocument,
                            "status",
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

    // entrants should be able to join the waiting list
    /**
     * Adds the current user to the waitlist of the specified lottery.
     *
     * the entry is saved under the lottery as lotteries/{id}/entrants/{id}_{deviceId}
     * a waitlistCapacity of 0 or less means no limit, and people who left dont count towards it
     *
     * @param lottery Target lottery to join
     * @param onResult Callback returning true if joined successfully, false if full, already joined, or failed
     */
    fun joinWaitlist(lottery: Lottery, onResult: (Boolean) -> Unit) {
        val entryId = "${lottery.id}_$currentDeviceId"
        val entryDoc = waitlistRef(lottery.id).document(entryId)

        lotteryRef.document(lottery.id).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onResult(false)
                    return@addOnSuccessListener
                }

                val capacity = snapshot.getLong("waitlistCapacity")?.toInt() ?: 0

                //Check if user is already in the waitlist
                entryDoc.get()
                    .addOnSuccessListener { existing ->
                        if (existing.exists()) {
                            onResult(false)
                            return@addOnSuccessListener
                        }

                        //Check if waitlist has reached maximum capacity (if capacity is set)
                        waitlistRef(lottery.id).get()
                            .addOnSuccessListener { entries ->
                                val taken = entries.documents.count {
                                    it.getString("status") != EntryStatus.DECLINED.name
                                }
                                if (capacity > 0 && taken >= capacity) {
                                    onResult(false) // Waitlist is full
                                    return@addOnSuccessListener
                                }

                                val newEntry = WaitlistEntry(
                                    id = entryId,
                                    lotteryId = lottery.id,
                                    deviceId = currentDeviceId,
                                    status = EntryStatus.WAITING
                                )

                                entryDoc.set(newEntry)
                                    .addOnSuccessListener {
                                        onResult(true)
                                        notifyViews()
                                    }
                                    .addOnFailureListener { onResult(false) }
                            }
                            .addOnFailureListener { onResult(false) }
                    }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener { onResult(false) }
    }

    // entrants need to be able to leave waitlist
    /**
     * leaves the waitlist of the lottery
     *
     * @param lottery Lottery that the entrant wants to leave waitlist from
     * @param onResult true if the entry was deleted, false if it failed
     */
    fun leaveWaitlist(lottery: Lottery, onResult: (Boolean) -> Unit) {
        // same id joinWaitlist saves the entry under, so we delete the right one
        waitlistRef(lottery.id).document("${lottery.id}_$currentDeviceId").delete()
            .addOnSuccessListener {
                onResult(true) //even if user isnt in lottery it will return true, but i dont think we need to implement this
                notifyViews()
            }
            .addOnFailureListener { onResult(false) }
    }

    /**
     * gets the entrants still waiting on a lottery, for the owner
     *
     * @param lottery the lottery to get the waitlist of
     */
    fun getWaitlist(lottery: Lottery, onResult: (Result<List<WaitlistEntry>>) -> Unit) {
        waitlistRef(lottery.id)
            .whereEqualTo("status", EntryStatus.WAITING.name)
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
    //endregion


    //region Reservations
    /**
     * Create a reservation for a campsite
     *
     * the current user is the entrant and the lottery is the one the campsite is in
     *
     * @param checkIn check in date like 2027-05-01
     * @param checkOut check out date like 2027-05-03
     * @param guests number of guests
     * @param campsite the campsite to book
     * @param rulesAccepted true if the entrant agreed to the campsite rules
     */
    fun makeReservation(checkIn: String, checkOut: String, guests: Int, campsite: Campsite, rulesAccepted: Boolean = false){
        val doc = reservationRef.document()
        val newReservation = Reservation(
            id = doc.id,
            campsiteId = campsite.id,
            entrantId = currentDeviceId,
            checkIn = checkIn,
            checkOut = checkOut,
            numGuests = guests,
            status = ReservationStatus.ACTIVE,
            rulesAccepted = rulesAccepted,
            lotteryId = campsite.lottery.id
        )
        doc.set(newReservation).addOnSuccessListener { notifyViews() }
    }

    /**
     * removes user from reservation, this allows user to decline reservation
     *
     *@param reservation the Reservation object that the user is declining
     */
    fun rejectReservation(reservation: Reservation){
        // I'm assuming that view people will pass in the reservation, because the user needs to click
        // on the reservation that they will cancel from a list of reservations.

        reservationRef.document(reservation.id).update(
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
        reservationRef.document(reservation.id).update(
            "status", ReservationStatus.CANCELLED
        )
        notifyViews()
    }

    /**
     * gets the status of every reservation the current user has
     */
    fun getReservationByUID(onResult: (Result<List<ReservationStatus>>) -> Unit) {
        reservationRef.whereEqualTo("entrantId", currentDeviceId).get()
            .addOnSuccessListener { documents ->
                val statuses = documents.documents.mapNotNull { doc ->
                    doc.getString("status")?.let { ReservationStatus.valueOf(it) }
                }

                onResult(Result.success(statuses))
                notifyViews()
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    /**
     * gets the current users reservations that are still active
     *
     * @param onResult gets the reservations or an empty list if it failed
     */
    fun getActiveReservations(onResult: (List<Reservation>) -> Unit) {
        reservationRef.whereEqualTo("entrantId", currentDeviceId).whereEqualTo("status", "ACTIVE").get()
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
     * gets the current users cancelled reservations for the reservations view
     *
     * @param onResult gets the reservations or an empty list if it failed
     */
    fun getCancelledReservations(onResult: (List<Reservation>) -> Unit) {
        reservationRef.whereEqualTo("entrantId", currentDeviceId).whereEqualTo("status", "CANCELLED").get()
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
                // DateRange holds strings now so dates like 2027-05-01 sort correctly as text
                // skip any reservation saved without dates instead of crashing
                val stays = documents.toObjects(Reservation::class.java)
                    .filter { it.checkIn.isNotBlank() && it.checkOut.isNotBlank() }
                    .map { DateRange(it.checkIn, it.checkOut) }
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
     * saves it in the notifications collection and adds its id to the users notifications list
     * does nothing if the user turned notifications off
     *
     * @param notification The notification message
     */
    fun notifyUser(notification: String) {
        // getUserObj is async so everything that needs the user has to happen inside the callback
        getUserObj { user ->
            if (user == null || !user.notificationsEnabled) return@getUserObj

            val doc = notificationRef.document()
            val newNotification = Notification(
                id = doc.id,
                entrantId = currentDeviceId,
                content = notification,
                // format YYYY-MM-DDTHH:MM:SS, truncated so the seconds always show
                timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )

            doc.set(newNotification).addOnSuccessListener {
                // arrayUnion adds just the id so we dont overwrite the whole user document
                usersRef.document(currentDeviceId).update("notifications", FieldValue.arrayUnion(doc.id))
                notificationCount = user.notifications.size + 1
                notifyViews()
            }
        }
    }

    /**
     * gets all notifications for this device ID from the notifications collection
     *
     * @param onResult gets the notifications oldest first or an empty list if it failed
     */
    fun getNotifications(onResult: (List<Notification>) -> Unit){
        notificationRef.whereEqualTo("entrantId", currentDeviceId).get()
            .addOnSuccessListener { documents ->
                val notifications = documents.toObjects(Notification::class.java)
                    .sortedBy { it.timestamp }
                notificationCount = notifications.size
                onResult(notifications)
                notifyViews()
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
    //endregion

}
