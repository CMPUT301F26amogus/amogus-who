
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlin.String

import com.example.code.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FieldPath
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

    /**
     * tells every registered view this model changed so they can redraw
     *
     * only call this from functions that CHANGE data, never from get functions.
     * some views fetch data in update(), so a get function that calls this starts
     * an endless loop of queries that crashes the app (out of memory)
     */
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
    fun getUserObj(deviceId: String = currentDeviceId, onResult: (User?) -> Unit) {
        usersRef.document(deviceId).get()
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
        roleOf: Role = Role.ENTRANT,
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
        getUserObj{user -> onResult(user)}
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
     * delete a profile (any role) and everything that belongs to it
     *
     * removes, in this order:
     *  - the lotteries the user owns, with their waitlists
     *  - the campsites the user owns, active reservations other people had on them get rejected
     *    and those people get a notification
     *  - the users own waitlist entries, reservations and notifications
     *  - the user profile itself, last
     *
     * the profile goes last on purpose, if a step fails the profile still exists
     * and this can just be run again, every step is safe to repeat
     *
     * @param onResult true if everything was deleted, false if a step failed
     */
    fun deleteProfile(onResult: (Boolean) -> Unit = {}){

            usersRef.document(currentDeviceId).delete()
            .addOnSuccessListener {
                onResult(true)
                notifyViews()
            }
            .addOnFailureListener { onResult(false) }
    }
    //endregion


    //region Campsites
    fun createCampsite(
        campName: String,
        location: String,
        desc: String,
        maxOcc: Int,
        price: Double,
        availability: AvailabilitySchedule,
        onResult: (Boolean) -> Unit
    ) {
        if (currentDeviceId.isBlank()) {
            onResult(false)
            return
        }
        val campsiteId = campsiteRef.document().id
        val campsite = try {
            Campsite(
                id = campsiteId,
                ownerId = currentDeviceId,
                name = campName,
                location = location,
                description = desc,
                maxOccupancy = maxOcc,
                pricePerNight = price,
                availability = availability
            )
        } catch (e: IllegalArgumentException) {
            Log.e("Model", "Invalid campsite: ${e.message}")
            onResult(false)
            return
        }
        try {
            campsiteRef.document(campsiteId).set(campsite)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        } catch (e: RuntimeException) {
            Log.e("Model", "Could not save campsite", e)
            onResult(false)
        }
    }
    //fun createCampsite(campsite: Campsite, onResult: (Boolean) -> Unit) {

    //    val doc = campsiteRef.document()
    //        campsite.id = doc.id

    //        doc.set(campsite)
    //        .addOnSuccessListener {
    //            onResult(true)
    //        }
    //}

    //fun updateCampsite(campsite: Campsite, campName: String, location: String, desc: String, maxOcc: Int, price: Double, onResult: (Boolean) -> Unit) {
    //    campsite.name = campName
    //    campsite.location = location
    //    campsite.description = desc
    //    campsite.maxOccupancy = maxOcc
    //    campsite.pricePerNight = price
    //    campsiteRef.document(campsite.id).set(campsite)
    //        .addOnSuccessListener {
    //            onResult(true)
    //        }
    //        .addOnFailureListener {
    //            onResult(false)
    //        }
    //    notifyViews()
    //}

    private fun validateCampsiteUpdate(updates: Map<String, Any>): String? {
        if (updates.isEmpty()) {
            return "No changes were provided"
        }

        val allowedValues = setOf(
            "name", "location", "description", "rulesAndPolicy",
            "maxOccupancy", "pricePerNight", "imageUrl", "amenities", "tags"
        )

        for (value in updates.keys) {
            if (value !in allowedValues) {
                return "You cannot update the field: $value"
            }
        }

        //Validate name
        if (updates.containsKey("name")) {
            val name = updates["name"] as? String

            if (name.isNullOrBlank()) {
                return "Campsite name cannot be blank"
            }
        }

        //Validate description type
        if (updates.containsKey("description") && updates["description"] !is String) {
            return "Description must be text"
        }

        //Validate location type
        if (updates.containsKey("location") && updates["location"] !is String) {
            return "Location must be text"
        }

        //Validate rules and policy type
        if (updates.containsKey("rulesAndPolicy") && updates["rulesAndPolicy"] !is String) {
            return "Rules and Policy must be text"
        }

        //Validate max Occupancy
        if (updates.containsKey("maxOccupancy")) {
            val occupancy = updates["maxOccupancy"] as? Int

            if (occupancy == null || occupancy <= 0) {
                return "Max Occupancy must be greater than 0"
            }
        }

        //Validate price
        if (updates.containsKey("pricePerNight")) {
            val price = updates["pricePerNight"] as? Number

            if (price == null || !price.toDouble().isFinite() || price.toDouble() < 0) {
                return "Price must be a finite number greater than or equal to 0"
            }
        }

        //Validate image URL
        if (updates.containsKey("imageUrl")) {
            val imageUrl = updates["imageUrl"]

            if (imageUrl != null && imageUrl !is String) {
                return "Image URL must be a text or null"
            }
        }

        //Validate amenities
        if (updates.containsKey("amenities")) {
            val amenities = updates["amenities"] as? List<*>

            if (amenities == null || amenities.any { it !is String }) {
                return "Amenities must be a list of strings"
            }
        }

        // Validates tags
        if (updates.containsKey("tags")) {
            val tags = updates["tags"] as? Collection<*>

            if (tags == null || tags.any { it !is String }) {
                return "Tags must be a collection of strings"
            }
        }

        return null
    }

    fun updateCampsite(
        campsiteId: String,
        deviceId: String,
        updates: Map<String, Any>,
        onResult: (Boolean, String?) -> Unit
    ) {
        // Check that the campsite ID is valid
        if (campsiteId.isBlank()) {
            onResult(false, "Invalid campsite ID.")
            return
        }

        // Check that the device ID is valid
        if (deviceId.isBlank()) {
            onResult(false, "Invalid device ID.")
            return
        }

        // Validate the fields being updated
        val validationError = validateCampsiteUpdate(updates)

        if (validationError != null) {
            onResult(false, validationError)
            return
        }

        // Prepare the updates for Firestore
        val firestoreUpdates = updates.toMutableMap()

        // Trim the campsite name
        if (firestoreUpdates.containsKey("name")) {
            firestoreUpdates["name"] =
                (firestoreUpdates["name"] as String).trim()
        }

        // Trim the location
        if (firestoreUpdates.containsKey("location")) {
            firestoreUpdates["location"] =
                (firestoreUpdates["location"] as String).trim()
        }

        // Clean amenities
        if (firestoreUpdates.containsKey("amenities")) {
            val amenities = firestoreUpdates["amenities"] as List<String>

            firestoreUpdates["amenities"] = amenities
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
        }

        // Clean tags and convert Set to List for Firestore
        if (firestoreUpdates.containsKey("tags")) {
            val tags = firestoreUpdates["tags"] as Collection<*>

            firestoreUpdates["tagList"] = tags
                .map { (it as String).trim().lowercase() }
                .filter { it.isNotEmpty() }
                .distinct()
        }

        // Convert price to Double
        if (firestoreUpdates.containsKey("pricePerNight")) {
            firestoreUpdates["pricePerNight"] =
                (firestoreUpdates["pricePerNight"] as Number).toDouble()
        }

        // Check ownership and update in a transaction
        db.runTransaction { transaction ->

            // Retrieve the existing campsite
            val document = transaction.get(campsiteRef.document(campsiteId))

            // Check that the campsite exists
            if (!document.exists()) {
                throw Exception("Campsite does not exist.")
            }

            // Check that the user owns the campsite
            val ownerId = document.getString("ownerId")

            if (ownerId != deviceId) {
                throw Exception("You do not own this campsite.")
            }

            // Apply each update
            for ((field, value) in firestoreUpdates) {
                transaction.update(campsiteRef.document(campsiteId), field, value)
            }

            null
        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { exception ->
            onResult(false, exception.message ?: "Failed to update campsite")
        }
    }

    fun deleteCampsite(campId: String, deviceId: String, onResult: (Boolean, String?) -> Unit) {
        //Check that the campsite ID is valid
        if (campId.isBlank()) {
            onResult(false, "Invalid campsite ID.")
            return
        }

        //Check that the device ID is valid
        if (deviceId.isBlank()) {
            onResult(false, "Invalid device ID.")
            return
        }

        //Check ownership and delete in a transaction
        db.runTransaction { transaction ->

            // Retrieve the campsite
            val document = transaction.get(campsiteRef.document(campId))

            //Check that the campsite exists
            if (!document.exists()) {
                throw Exception("Campsite does not exist.")
            }

            //Check that the user owns the campsite
            val ownerId = document.getString("ownerId")

            if (ownerId != deviceId) {
                throw Exception("You do not own this campsite.")
            }

            // Check that the stored campsite ID matches
            // the document ID
            val storedId = document.getString("id")

            if (storedId != campId) {
                throw Exception("Campsite ID does not match its document ID.")
            }

            //Delete the campsite
            transaction.delete(campsiteRef.document(campId))

            null

        }.addOnSuccessListener {

            //Deletion successful
            notifyViews()
            onResult(true, null)

        }.addOnFailureListener { exception ->

            //Deletion failed
            onResult(
                false,
                exception.message ?: "Failed to delete campsite."
            )
        }
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
                if (document.exists()) {
                    val campsite = try {
                        document.toObject(Campsite::class.java)
                    } catch (e: RuntimeException) {
                        Log.e("Model", "Could not read campsite ${document.id}", e)
                        null
                    }
                    onResult(campsite)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun getCampsiteByOwner(ownerId: String, onResult: (List<Campsite>) -> Unit) {
        campsiteRef.whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { documents ->
                val campsites = documents.toCampsites()
                onResult(campsites)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getAllCampsites(onResult: (List<Campsite>) -> Unit) {
        campsiteRef.get()
            .addOnSuccessListener { documents ->
                val campsites = documents.toCampsites()
                onResult(campsites)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun searchCampsite(query: String, onResult: (List<Campsite>) -> Unit) {
        campsiteRef.get()
            .addOnSuccessListener { documents ->
                val campsite = documents.toCampsites()

                val filter = campsite.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.location.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                }
                onResult(filter)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    private fun QuerySnapshot.toCampsites(): List<Campsite> = documents.mapNotNull { d ->
        try {
            d.toObject(Campsite::class.java)
        } catch (e: RuntimeException) {
            Log.e("Model", "Skipping unreadable campsite ${d.id}", e)
            null
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

    fun getCampId(campName: String, onResult: (String) -> Unit) {
        campsiteRef.whereEqualTo("name", campName).get()
            .addOnSuccessListener { documents ->
                val campsite = documents.toObjects(Campsite::class.java)
                onResult(campsite[0].id)
            }
    }
    //endregion

    //region Lotteries
    /** Register to hold a specific lottery to be displayed, for the purposes of edit lottery
     * Ideally this is a lottery ID, which getLottery() would use to query the thing on the spot */
    var theLottery: Lottery? = null  //  "the" suggests a singular specific item I also didn't want to possibly interfere with leaveWaitlist

    /** returns the specific lottery to be displayed*/
    fun gettTheLottery() : Lottery? {
        /** Ideally this would pull the lottery in question straight from the database on the spot
         * to get the most current version*/
        return theLottery
    }

    /** Sets the specific lottery to be displayed */
    fun settTheLottery(newLottery: Lottery?) {
        theLottery = newLottery
        notifyViews()
    }

    public

    //region Lotteries
    /**
     * creates a lottery for a campsite of the current user
     *
     * the id is made here and the current user becomes the owner
     * the lottery starts OPEN and is saved on the lottery and on the campsite
     * since getLottery(campsiteId) reads it from the campsite
     *
     * dates are strings like 2027-05-01 and the end days are exclusive like DateRange
     * registration has to end before booking starts
     *
     * @param campsiteId id of the campsite, it needs to be saved already
     * @param registrationStart first day entrants can join the waitlist
     * @param registrationEnd day registration closes
     * @param bookingStart first day winners can reserve their dates
     * @param bookingEnd day booking closes
     * @param numWinners how many entrants get drawn, more than 0
     * @param waitlistCapacity most entrants allowed on the waitlist, 0 means no limit
     * @param onResult gets the new lottery or the failure, eg if something wasnt valid
     */
    fun createLottery(
        campsiteId: String,
        registrationStart: String,
        registrationEnd: String,
        bookingStart: String,
        bookingEnd: String,
        numWinners: Int,
        waitlistCapacity: Int = 0,
        onResult: (Result<Lottery>) -> Unit
    ) {
        try {
            require(campsiteId.isNotBlank()) { "A lottery needs a saved campsite" }
            val regStart = LocalDate.parse(registrationStart)
            val regEnd = LocalDate.parse(registrationEnd)
            val bookStart = LocalDate.parse(bookingStart)
            val bookEnd = LocalDate.parse(bookingEnd)
            require(regStart.isBefore(regEnd)) { "Registration must end after it starts" }
            require(bookStart.isBefore(bookEnd)) { "Booking must end after it starts" }
            require(!bookStart.isBefore(regEnd)) { "Registration period must end before the booking period starts" }
            require(numWinners > 0) { "Number of winners must be more than 0, was $numWinners" }
            require(waitlistCapacity >= 0) { "Waitlist capacity must be 0 or more, was $waitlistCapacity" }
        } catch (e: Exception) { // bad values or dates that dont parse
            onResult(Result.failure(e))
            return
        }

        val doc = lotteryRef.document()
        val lottery = Lottery(
            id = doc.id,
            ownerId = currentDeviceId,
            campsiteIds = listOf(campsiteId),
            registrationStart = registrationStart,
            registrationEnd = registrationEnd,
            bookingStart = bookingStart,
            bookingEnd = bookingEnd,
            numWinners = numWinners,
            waitlistCapacity = waitlistCapacity,
            status = LotteryStatus.OPEN
        )

        // one batch so the lottery and the campsite pointing at it are saved together or not at all
        val batch = db.batch()
        batch.set(doc, lottery)
        batch.update(campsiteRef.document(campsiteId), "lotteryId", doc.id)

        batch.commit()
            .addOnSuccessListener {
                onResult(Result.success(lottery))
                notifyViews()
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun deleteLottery(lottery: Lottery, onResult: (Boolean) -> Unit = {}) {
        val batch = db.batch()
        batch.delete(lotteryRef.document(lottery.id))
        lottery.campsiteIds.forEach { batch.update(campsiteRef.document(it), "lotteryId", "") }
        batch.commit()
            .addOnSuccessListener {
                onResult(true)
                notifyViews()
            }
            .addOnFailureListener { onResult(false) }
    }
        // TODO: this doesnt work, still need to fix this.
        fun updateLottery(lottery: Lottery, onResult: (Result<Unit>) -> Unit) {
            val updates = mapOf(
                "name" to lottery.name,
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
        fun getLotteriesIn(status: EntryStatus = EntryStatus.DECLINED, onResult: (List<Lottery>) -> Unit) {
            db.collectionGroup("entrants")
                .whereEqualTo("deviceId", currentDeviceId)
                .get()
                .addOnSuccessListener { documents ->
                    val lotteryIds = documents.toObjects(WaitlistEntry::class.java)
                        .filter { it.status == status }
                        .map { it.lotteryId }
                        .distinct()

                    if (lotteryIds.isEmpty()) {
                        onResult(emptyList())
                        return@addOnSuccessListener
                    }

                    Tasks.whenAllSuccess<DocumentSnapshot>(lotteryIds.map {
                        lotteryRef.document(it).get()
                    })
                        .addOnSuccessListener { snapshots ->
                            onResult(snapshots.mapNotNull { it.toObject(Lottery::class.java) })
                        }
                        .addOnFailureListener { onResult(emptyList()) }
                }
                .addOnFailureListener { onResult(emptyList()) }
        }

        /**
         * get lottery associated with campsite
         *
         * @param lotteryId id of the campsite
         */
        fun getLotteryById(lotteryId: String, onResult: (Lottery?) -> Unit) {

            lotteryRef.document(lotteryId).get()
                .addOnSuccessListener { lotterySnapshot ->
                    onResult(lotterySnapshot.toObject(Lottery::class.java))
                }
                .addOnFailureListener { onResult(null) }
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

            // returns the winners and the people who didnt win
            db.runTransaction<Pair<List<String>, List<String>>> { transaction ->
                val snapshot = transaction.get(lotteryDocument)
                val lottery = snapshot.toObject(Lottery::class.java)
                if (!snapshot.exists() || lottery == null) throw Exception("Lottery not found")

                if (!lottery.registrationEndedOn()) throw Exception("Registration has not ended")
                if (lottery.status != LotteryStatus.OPEN) throw Exception("Lottery has already been drawn")
                if (lottery.numWinners <= 0) throw Exception("Invalid winner count")

                val waiting = lottery.waitlist.filter { it.status == EntryStatus.WAITING }

                // fewer entrants than winners means everyone wins, take() just returns them all
                val winnerIds = waiting.shuffled().take(lottery.numWinners).map { it.id }
                val winnerSet = winnerIds.toSet()
                waiting.forEach {
                    it.status = if (it.id in winnerSet) EntryStatus.SELECTED else EntryStatus.NOT_SELECTED
                }

                transaction.update(
                    lotteryDocument,
                    "waitlist", lottery.waitlist,
                    "status", LotteryStatus.DRAWN.name,
                    "winnerIds", winnerIds
                )
                Pair(winnerIds, waiting.map { it.id }.filter { it !in winnerSet })
            }
                .addOnSuccessListener { (winners, others) ->
                    // only the device whose transaction went through gets here, so nobody is notified twice
                    winners.forEach { notifyUser("You won the lottery, you can book your dates now", it) }
                    others.forEach { notifyUser("You were not selected in the lottery this time", it) }
                    onResult(Result.success(winners))
                    notifyViews()
                }
                .addOnFailureListener { onResult(Result.failure(it)) }
        }
        //endregion



    //region Waitlist
        // This entire region is rewritten by claude sonnet 5.5
        // Prompt: "Can you change the Waitlist functions in model to update the waitlist in the lottery when someone joins and leaves? Should we use a whole subcollection to store waitlist entries? it is probably easier to get rid of the subcollection, yea get rid of the sub collection and change it so that all waitlist entries are just stored in the list that is in the lottery's document."
        // 2026-11-23

        // entrants should be able to join the waiting list
    /**
     * Adds the current user to the waitlist of the specified lottery.
     *
     * the entry is added to the waitlist list in the lottery document, in a transaction so two people
     * joining at once cant overwrite each other or go over the capacity
     * a waitlistCapacity of 0 or less means no limit
     *
     * @param lottery Target lottery to join
     * @param onResult true if joined successfully, false if full, already joined, or failed
     */
    fun joinWaitlist(lottery: Lottery, onResult: (Boolean) -> Unit) {
        val lotteryDoc = lotteryRef.document(lottery.id)

        db.runTransaction<Boolean> { transaction ->
            val snapshot = transaction.get(lotteryDoc)
            if (!snapshot.exists()) return@runTransaction false // lottery doesnt exist

            val current = snapshot.toObject(Lottery::class.java) ?: return@runTransaction false
            val entries = current.waitlist

            if (entries.any { it.id == currentDeviceId }) return@runTransaction false // already joined
            if (current.waitlistCapacity > 0 && entries.size >= current.waitlistCapacity) {
                return@runTransaction false // waitlist is full
            }

            val newEntry = WaitlistEntry(
                id = currentDeviceId,
                lotteryId = lottery.id,
                status = EntryStatus.WAITING
            )
            transaction.update(
                lotteryDoc,
                "waitlist", entries + newEntry,
                "entrantIds", FieldValue.arrayUnion(currentDeviceId)
            )
            true
        }
            .addOnSuccessListener { joined ->
                onResult(joined)
                if (joined) notifyViews()
            }
            .addOnFailureListener { onResult(false) }
    }

        // entrants need to be able to leave waitlist
    /**
     * leaves the waitlist of the lottery
     *
     * @param lottery Lottery that the entrant wants to leave waitlist from
     * @param onResult true if they are off the waitlist, false if it failed
     */
    fun leaveWaitlist(lottery: Lottery, onResult: (Boolean) -> Unit) {
        removeFromWaitlist(lottery.id)
            .addOnSuccessListener {
                onResult(true) // also true if they were never on it
                notifyViews()
            }
            .addOnFailureListener { onResult(false) }
    }

    // takes the current user out of one lottery, arrayRemove cant be used because it needs the exact entry
    // and the entry status may have changed since they joined
    fun removeFromWaitlist(lotteryId: String): Task<Void> {
        val lotteryDoc = lotteryRef.document(lotteryId)
        return db.runTransaction<Void> { transaction ->
            val snapshot = transaction.get(lotteryDoc)
            val entries = snapshot.toObject(Lottery::class.java)?.waitlist ?: emptyList()
            transaction.update(
                lotteryDoc,
                "waitlist", entries.filter { it.id != currentDeviceId },
                "entrantIds", FieldValue.arrayRemove(currentDeviceId)
            )
            null
        }
    }

    fun getWaitlist(lottery: Lottery, onResult: (Result<List<WaitlistEntry>>) -> Unit) {
        lotteryRef.document(lottery.id).get()
            .addOnSuccessListener { snapshot ->
                val current = snapshot.toObject(Lottery::class.java)
                if (current == null) {
                    onResult(Result.failure(Exception("Lottery not found")))
                    return@addOnSuccessListener
                }
                onResult(Result.success(current.waitlist.filter { it.status == EntryStatus.WAITING }))
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
        fun makeReservation(
            checkIn: String,
            checkOut: String,
            guests: Int,
            campsite: Campsite,
            rulesAccepted: Boolean = false
        ) {
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
                lotteryId = campsite.lotteryId
            )
            doc.set(newReservation).addOnSuccessListener { notifyViews() }
        }

        /**
         * lets an owner reject a booked reservation eg when a fire closes the campsite
         *
         * only stays that are active and not over yet can be rejected
         *
         * @param reservation the reservation to reject
         * @param onResult success once its rejected or a failure if it wasnt found wasnt active or saving failed
         */
        fun rejectReservation(reservation: Reservation, onResult: (Result<Unit>) -> Unit = {}) {
            endActiveReservation(reservation.id, ReservationStatus.REJECTED, onResult)
        }

        /**
         * lets an entrant cancel one of their reservations
         *
         * only stays that are active and not over yet can be cancelled
         * cancelling frees the dates since booked dates only count active reservations
         *
         * @param reservation the reservation to cancel
         * @param onResult success once its cancelled or a failure if it wasnt found wasnt active or saving failed
         */
        fun cancelReservation(reservation: Reservation, onResult: (Result<Unit>) -> Unit = {}) {
            endActiveReservation(reservation.id, ReservationStatus.CANCELLED, onResult)
        }

        /**
         * moves an active reservation to cancelled or rejected
         *
         * reads the saved reservation first since the one the view has might be out of date
         *
         * @param reservationId id of the reservation
         * @param newStatus what to set it to
         * @param onResult success once its saved or a failure if it wasnt found wasnt active or saving failed
         */
        private fun endActiveReservation(
            reservationId: String,
            newStatus: ReservationStatus,
            onResult: (Result<Unit>) -> Unit,
        ) {
            val reservationDoc = reservationRef.document(reservationId)

            reservationDoc.get()
                .addOnSuccessListener { document ->
                    val saved = document.toObject(Reservation::class.java)
                    if (saved == null) {
                        onResult(Result.failure(Exception("Reservation not found")))
                        return@addOnSuccessListener
                    }

                    // a stay thats already over cant be cancelled or rejected even if its still saved as active
                    if (!runCatching { saved.isActive() }.getOrDefault(false)) {
                        onResult(Result.failure(Exception("Reservation is not active")))
                        return@addOnSuccessListener
                    }

                    reservationDoc.update("status", newStatus.name)
                        .addOnSuccessListener {
                            onResult(Result.success(Unit))
                            notifyViews()
                        }
                        .addOnFailureListener { exception -> onResult(Result.failure(exception)) }
                }
                .addOnFailureListener { exception -> onResult(Result.failure(exception)) }
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
            reservationRef.whereEqualTo("entrantId", currentDeviceId)
                .whereEqualTo("status", "ACTIVE").get()
                .addOnSuccessListener { documents ->
                    val reservations = documents.toObjects(Reservation::class.java)
                    onResult(reservations)
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
            reservationRef.whereEqualTo("entrantId", currentDeviceId)
                .whereEqualTo("status", "CANCELLED").get()
                .addOnSuccessListener { documents ->
                    val reservations = documents.toObjects(Reservation::class.java)
                    onResult(reservations)
                }
                .addOnFailureListener {
                    onResult(emptyList())
                }
        }

        // Avery's owner reservation functions (#194), brought back after the dev merge dropped them.
        // Changed to match this branch: they use the current user instead of an ownerId parameter
        // (like getCampsiteByOwner) and don't call notifyViews since they only read data

        /**
         * removes user from reservation, this allows user to decline reservation
         *
         * saved as cancelled since rejected is only for the owner rejecting the entrant
         *
         * @param reservation the Reservation object that the user is declining
         */
        fun declineReservation(reservation: Reservation) {
            reservationRef.document(reservation.id).update(
                "status", ReservationStatus.CANCELLED.name
            ).addOnSuccessListener { notifyViews() }
        }

        /**
         * gets every reservation at the current owners campsites with the campsite and entrant name for each card
         *
         * reservations only keep ids so this finds the owners campsites first then their reservations
         * then the entrants names
         * whereIn takes at most 30 values so the ids are asked for in chunks
         *
         * @param onResult gets the reservations or an empty list if anything failed
         */
        private fun getOwnerReservations(onResult: (List<OwnerReservation>) -> Unit) {
            campsiteRef.whereEqualTo("ownerId", currentDeviceId).get()
                .addOnSuccessListener { campDocs ->
                    val campsites = campDocs.toObjects(Campsite::class.java).associateBy { it.id }
                    if (campsites.isEmpty()) {
                        onResult(emptyList())
                        return@addOnSuccessListener
                    }

                    val reservationQueries = campsites.keys.chunked(30).map {
                        reservationRef.whereIn("campsiteId", it).get()
                    }
                    Tasks.whenAllSuccess<QuerySnapshot>(reservationQueries)
                        .addOnSuccessListener { reservationSnaps ->
                            val reservations = reservationSnaps.flatMap { it.toObjects(Reservation::class.java) }
                            val entrantIds = reservations.map { it.entrantId }.filter { it.isNotBlank() }.distinct()
                            if (entrantIds.isEmpty()) {
                                onResult(reservations.map { OwnerReservation(it, campsites[it.campsiteId], "") })
                                return@addOnSuccessListener
                            }

                            // user docs are keyed by device id so look them up by document id
                            val userQueries = entrantIds.chunked(30).map {
                                usersRef.whereIn(FieldPath.documentId(), it).get()
                            }
                            Tasks.whenAllSuccess<QuerySnapshot>(userQueries)
                                .addOnSuccessListener { userSnaps ->
                                    val names = userSnaps.flatMap { it.documents }
                                        .associate { it.id to (it.getString("name") ?: "") }
                                    onResult(reservations.map {
                                        OwnerReservation(it, campsites[it.campsiteId], names[it.entrantId] ?: "")
                                    })
                                }
                                .addOnFailureListener { onResult(emptyList()) }
                        }
                        .addOnFailureListener { onResult(emptyList()) }
                }
                .addOnFailureListener { onResult(emptyList()) }
        }

        /**
         * gets the active reservations at all of the current owners campsites
         *
         * @param onResult gets the reservations sorted by check in or an empty list if it failed
         */
        fun getOwnerActiveReservations(onResult: (List<OwnerReservation>) -> Unit) {
            getOwnerReservations { all ->
                val today = LocalDate.now()
                // skip any reservation saved with bad dates instead of crashing
                onResult(all.filter { runCatching { it.reservation.isActive(today) }.getOrDefault(false) }
                    .sortedBy { it.reservation.checkIn })
            }
        }

        /**
         * gets the past reservations at all of the current owners campsites
         *
         * @param onResult gets the reservations newest first or an empty list if it failed
         */
        fun getOwnerPastReservations(onResult: (List<OwnerReservation>) -> Unit) {
            getOwnerReservations { all ->
                val today = LocalDate.now()
                // skip any reservation saved with bad dates instead of crashing
                onResult(all.filter { runCatching { it.reservation.isPast(today) }.getOrDefault(false) }
                    .sortedByDescending { it.reservation.checkIn })
            }
        }

        /**
         * gets the cancelled reservations at all of the current owners campsites
         *
         * @param onResult gets the reservations sorted by check in or an empty list if it failed
         */
        fun getOwnerCancelledReservations(onResult: (List<OwnerReservation>) -> Unit) {
            getOwnerReservations { all ->
                onResult(all.filter { it.reservation.isCancelled() }.sortedBy { it.reservation.checkIn })
            }
        }

        /**
         * gets every reservation an entrant made with the campsite for each card
         *
         * reservations only keep the campsite id so this looks the campsites up after
         * whereIn takes at most 30 values so the ids are asked for in chunks
         *
         * @param entrantId device id of the entrant
         * @param onResult gets the reservations or an empty list if anything failed
         */
        private fun getEntrantReservations(entrantId: String, onResult: (List<EntrantReservation>) -> Unit) {
            reservationRef.whereEqualTo("entrantId", entrantId).get()
                .addOnSuccessListener { reservationDocs ->
                    val reservations = reservationDocs.toObjects(Reservation::class.java)
                    val campsiteIds = reservations.map { it.campsiteId }.filter { it.isNotBlank() }.distinct()

                    // campsite docs are keyed by campsite id so look them up by document id
                    val campsiteQueries = campsiteIds.chunked(30).map {
                        campsiteRef.whereIn(FieldPath.documentId(), it).get()
                    }
                    Tasks.whenAllSuccess<QuerySnapshot>(campsiteQueries)
                        .addOnSuccessListener { campsiteSnaps ->
                            val campsites = campsiteSnaps.flatMap { it.toObjects(Campsite::class.java) }
                                .associateBy { it.id }
                            onResult(reservations.map { EntrantReservation(it, campsites[it.campsiteId]) })
                        }
                        .addOnFailureListener { onResult(emptyList()) }
                }
                .addOnFailureListener { onResult(emptyList()) }
        }

        /**
         * gets an entrants reservations that are coming up or happening now
         *
         * stays whose check out has passed are left out even though theyre still saved as active
         *
         * @param entrantId device id of the entrant
         * @param onResult gets the reservations sorted by check in or an empty list if it failed
         */
        fun getEntrantActiveReservations(entrantId: String, onResult: (List<EntrantReservation>) -> Unit) {
            getEntrantReservations(entrantId) { all ->
                val today = LocalDate.now()
                // skip any reservation saved with bad dates instead of crashing
                onResult(all.filter { runCatching { it.reservation.isActive(today) }.getOrDefault(false) }
                    .sortedBy { it.reservation.checkIn })
            }
        }

        /**
         * gets an entrants reservations that are over
         *
         * nothing sets PAST yet so finished stays still saved as active count too
         *
         * @param entrantId device id of the entrant
         * @param onResult gets the reservations newest first or an empty list if it failed
         */
        fun getEntrantPastReservations(entrantId: String, onResult: (List<EntrantReservation>) -> Unit) {
            getEntrantReservations(entrantId) { all ->
                val today = LocalDate.now()
                // skip any reservation saved with bad dates instead of crashing
                onResult(all.filter { runCatching { it.reservation.isPast(today) }.getOrDefault(false) }
                    .sortedByDescending { it.reservation.checkIn })
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
         * @param content The notification message
         * @param userId The user to send the notification to
         */
        fun notifyUser(content: String, userId: String) {
            // getUserObj is async so everything that needs the user has to happen inside the callback
            getUserObj(userId) { user ->
                if (user == null || !user.notificationsEnabled) return@getUserObj

                val doc = notificationRef.document()
                val newNotification = Notification(
                    id = doc.id,
                    entrantId = userId,
                    content = content,
                    // format YYYY-MM-DDTHH:MM:SS, truncated so the seconds always show
                    timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )

                doc.set(newNotification).addOnSuccessListener {
                    // arrayUnion adds just the id so we dont overwrite the whole user document
                    usersRef.document(userId).update("notifications", FieldValue.arrayUnion(doc.id))
                    notifyViews()
                }
            }
        }

        /**
         * gets all notifications for this device ID from the notifications collection
         *
         * @param onResult gets the notifications oldest first or an empty list if it failed
         */
        fun getNotifications(
            userId: String = currentDeviceId,
            onResult: (List<Notification>) -> Unit
        ) {
            notificationRef.whereEqualTo("entrantId", userId).get()
                .addOnSuccessListener { documents ->
                    val notifications = documents.toObjects(Notification::class.java)
                        .sortedBy { it.timestamp }
                    onResult(notifications)
                }
                .addOnFailureListener {
                    onResult(emptyList())
                }
        }
        //endregion

    //region Deleting data
        // these return a Task so deleteProfile can chain them, onSuccessTask stops the chain at the first failure

        // firestore batches hold at most 500 writes so big lists get split up
        fun deleteDocs(refs: List<DocumentReference>): Task<Void> =
            Tasks.whenAll(refs.chunked(500).map { chunk ->
                val batch = db.batch()
                chunk.forEach { batch.delete(it) }
                batch.commit()
            })

        // deletes every document a query finds
        fun deleteQuery(query: Query): Task<Void> =
            query.get()
                .onSuccessTask { snapshot -> deleteDocs(snapshot.documents.map { it.reference }) }

        fun Task<Void>.reportTo(onResult: (Boolean) -> Unit) {
            addOnSuccessListener {
                onResult(true)
                notifyViews()
            }
            addOnFailureListener { onResult(false) }
        }

        fun deleteMyNotifications(): Task<Void> =
            deleteQuery(notificationRef.whereEqualTo("entrantId", currentDeviceId))

    // takes the user off every waitlist they are on, whatever their entry status
        fun deleteMyWaitlistEntries(): Task<Void> =
            lotteryRef.whereArrayContains("entrantIds", currentDeviceId).get().onSuccessTask { snapshot ->
                Tasks.whenAll(snapshot.documents.map { removeFromWaitlist(it.id) })
        }

        // deleting a reservation also frees its dates since getBookedDates only looks at reservations
        fun deleteMyReservations(): Task<Void> =
            deleteQuery(reservationRef.whereEqualTo("entrantId", currentDeviceId))

    /**
     * removes the current user from every waitlist they are on, even lotteries they already lost
     *
     * @param onResult true if they were removed, false if it failed
     */
    fun deleteWaitlistEntries(onResult: (Boolean) -> Unit = {}) {
        deleteMyWaitlistEntries().reportTo(onResult)
    }

    /**
     * deletes every reservation the current user made
     *
     * @param onResult true if they were deleted, false if it failed
     */
    fun deleteReservations(onResult: (Boolean) -> Unit = {}) {
        deleteMyReservations().reportTo(onResult)
    }

    // lotteries the user runs, their waitlists are inside them so they go too
    fun deleteOwnedLotteries(): Task<Void> =
        deleteQuery(lotteryRef.whereEqualTo("ownerId", currentDeviceId))

    // campsites the user owns
    fun deleteOwnedCampsites(): Task<Void> =
        campsiteRef.whereEqualTo("ownerId", currentDeviceId).get().onSuccessTask { snapshot ->
            val campsites = snapshot.documents
            Tasks.whenAll(campsites.map { rejectActiveReservationsAt(it.id) })
                .onSuccessTask { Tasks.whenAll(campsites.map { deleteQuery(availabilityRef(it.id)) }) }
                .onSuccessTask { deleteDocs(campsites.map { it.reference }) }
        }

    // other people booked these stays so reject them and tell them instead of just deleting
    fun rejectActiveReservationsAt(campsiteId: String): Task<Void> =
        reservationRef.whereEqualTo("campsiteId", campsiteId)
            .whereEqualTo("status", ReservationStatus.ACTIVE.name).get()
            .onSuccessTask { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach {
                    batch.update(it.reference, "status", ReservationStatus.REJECTED.name)
                }
                snapshot.documents.mapNotNull { it.getString("entrantId") }.distinct().forEach {
                    notifyUser(
                        "Your reservation was cancelled because the owner removed their campsite",
                        it
                    )
                }
                batch.commit()
            }


    //endregion

}
