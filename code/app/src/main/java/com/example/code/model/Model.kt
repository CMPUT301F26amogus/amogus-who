import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlin.String

import android.net.Uri
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storage

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

    /**
     * gives every registered view its data once, called by App right after the app starts.
     *
     * update() normally only runs when data changes, so without this screens like
     * My Campsites are empty on a fresh launch until something changes.
     * safe to call because get functions never call notifyViews (no loop)
     */
    fun loadViews() {
        notifyViews()
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

    // see [19] in Citation.md
    /**
     * the lottery the reserve screen is booking a win from (US 01.04.01)
     *
     * null means the reserve screen books [tempCampsite] without a lottery
     */
    var lotteryToReserve: Lottery? = null
        private set

    // see [19] in Citation.md
    /**
     * picks a campsite from a lottery the user won for the reserve screen (US 01.04.02)
     *
     * sets both at once so the views only redraw once
     *
     * @param lottery the lottery they won or null to book without one
     * @param campsite which of its campsites they picked
     */
    fun settLotteryReservation(lottery: Lottery?, campsite: Campsite?) {
        lotteryToReserve = lottery
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


    // profile pictures are files in firebase storage and the link to show one is kept on the user as profileImageUrl
    // each user has one fixed path so a new picture replaces the old file and nothing is left behind
    private val storage by lazy { Firebase.storage }
    private fun profileImageFile() = storage.reference.child("profileImages/$currentDeviceId")

    /**
     * adds or changes the current users profile image
     *
     * uploads the picture and saves its link as the users profileImageUrl
     * a new picture replaces the old one
     *
     * @param imageUri the picture the user picked, eg from the photo picker
     * @param onResult gets the link to the picture or the failure
     */
    fun setProfileImage(imageUri: Uri, onResult: (Result<String>) -> Unit) {
        if (currentDeviceId.isBlank()) {
            onResult(Result.failure(IllegalStateException("No device id set")))
            return
        }
        val file = profileImageFile()
        file.putFile(imageUri)
            .onSuccessTask { file.downloadUrl }
            .onSuccessTask { url ->
                usersRef.document(currentDeviceId).update("profileImageUrl", url.toString())
                    .onSuccessTask { Tasks.forResult(url.toString()) }
            }
            .addOnSuccessListener { link ->
                onResult(Result.success(link))
                notifyViews()
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    /**
     * removes the current users profile image, does nothing if they never had one
     *
     * @param onResult true if the picture is gone, false if it failed
     */
    fun removeProfileImage(onResult: (Boolean) -> Unit = {}) {
        deleteStoredProfileImage()
            .onSuccessTask { usersRef.document(currentDeviceId).delete() }
            .addOnSuccessListener {
                onResult(true)
                notifyViews()
            }
            .addOnFailureListener { onResult(false) }
    }

    // deletes the file, no file there counts as success since the goal is that its gone
    private fun deleteStoredProfileImage(): Task<Void> =
        profileImageFile().delete().continueWithTask { task ->
            val error = task.exception
            if (error == null ||
                (error is StorageException && error.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND)
            ) {
                Tasks.forResult<Void>(null)
            } else {
                Tasks.forException<Void>(error)
            }
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
        rules: String = "",
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
                rulesAndPolicy = rules.trim(),
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

    fun getCampsitesByOwner(ownerId: String, onResult: (List<Campsite>) -> Unit) {
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

    fun filterCampsite(filter: Campsite, onResult: (List<Campsite>) -> Unit) {
        campsiteRef.get()
            .addOnSuccessListener { result ->
                val campsites = result.toCampsites()

                val filtered = campsites.filter { campsite ->

                    val matchesLocation = filter.location.isBlank() ||
                            campsite.location.contains(filter.location, ignoreCase = true)

                    val matchesGuest = campsite.maxOccupancy >= filter.maxOccupancy

                    matchesLocation && matchesGuest
                }
                onResult(filtered)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getCampsiteDetails(onResult: (List<Campsite>, String?) -> Unit) {
        campsiteRef.get()
            .addOnSuccessListener { result ->
                val summaries = result.documents.mapNotNull { document ->
                    val campsite = document.toObject(Campsite::class.java)

                    campsite?.let {
                        Campsite(
                            name = it.name,
                            location = it.location,
                            maxOccupancy = it.maxOccupancy,
                            pricePerNight = it.pricePerNight,
                            description = it.description,
                            rulesAndPolicy = it.rulesAndPolicy,
                            imageUrl = it.imageUrl,
                            amenities = it.amenities,
                            availability = it.availability,
                            ratings = it.ratings
                        )
                    }
                }
                onResult(summaries, null)
            }
            .addOnFailureListener { exception ->
                onResult(emptyList(), exception.message?: "Failed to retrieve campsite details")
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
    fun settTheLottery(newLottery: Lottery?, afterNotify: () -> Unit = {}) { // [4]
        theLottery = newLottery
        notifyViews()
        afterNotify() // [4]
    }

    /**
     * creates a lottery for one or more campsites of the current user
     *
     * the id is made here and the current user becomes the initial owner
     * the lottery starts OPEN and is saved on the lottery and on each selected campsite
     *
     * dates are strings like 2027-05-01 and the end days are exclusive like DateRange
     * registration has to end before booking starts
     *
     * @param campsiteIds ids of the campsites, they need to be saved already
     * @param registrationStart first day entrants can join the waitlist
     * @param registrationEnd day registration closes
     * @param bookingStart first day winners can reserve their dates
     * @param bookingEnd day booking closes
     * @param numWinners how many entrants get drawn, more than 0
     * @param waitlistCapacity most entrants allowed on the waitlist, 0 means no limit
     * @param name optional name for the lottery
     * @param onResult gets the new lottery or the failure, eg if something wasnt valid
     */
    fun createLottery(
        campsiteIds: List<String>,
        registrationStart: String,
        registrationEnd: String,
        bookingStart: String,
        bookingEnd: String,
        numWinners: Int,
        waitlistCapacity: Int = 0,
        name: String = "",
        onResult: (Result<Lottery>) -> Unit
    ) {
        val cleanCampsiteIds = campsiteIds.filter { it.isNotBlank() }.distinct()
        try {
            require(currentDeviceId.isNotBlank()) { "No device id set" }
            require(cleanCampsiteIds.isNotEmpty()) { "A lottery needs at least one saved campsite" }
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
            name = name,
            id = doc.id,
            ownerId = listOf(currentDeviceId),
            campsiteIds = cleanCampsiteIds,
            registrationStart = registrationStart,
            registrationEnd = registrationEnd,
            bookingStart = bookingStart,
            bookingEnd = bookingEnd,
            numWinners = numWinners,
            waitlistCapacity = waitlistCapacity,
            status = LotteryStatus.OPEN
        )

        // one batch so the lottery and every campsite pointing at it are saved together or not at all
        val batch = db.batch()
        batch.set(doc, lottery)
        cleanCampsiteIds.forEach { campId ->
            batch.update(campsiteRef.document(campId), "lotteryId", doc.id)
        }

        batch.commit()
            .addOnSuccessListener {
                notifyViews() // [4]
                onResult(Result.success(lottery)) // [4]
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    /**
     * fetches all users with the OWNER role
     *
     * @param includeSelf if false (default), excludes the current user so they dont invite themselves
     * @param onResult list of campsite owners, or empty list on failure
     */
    fun getAllCampsiteOwners(
        includeSelf: Boolean = false,
        onResult: (List<User>) -> Unit
    ) {
        usersRef.whereEqualTo("role", Role.OWNER.name).get()
            .addOnSuccessListener { documents ->
                val owners = documents.toObjects(User::class.java)
                    .filter { includeSelf || it.deviceId != currentDeviceId }
                onResult(owners)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * invites another campsite owner to add a campsite to a lottery
     *
     * saves a pending invite under lotteries/{lotteryId}/invites/{invitedOwnerId}
     * and sends a notification to the invited owner
     *
     * @param lotteryId id of the lottery
     * @param invitedOwnerId device id of the owner being invited
     * @param campsiteId id of the campsite to register into the lottery
     * @param onResult success or failure
     */
    fun inviteOwnerToLottery(
        lotteryId: String,
        invitedOwnerId: String,
        campsiteId: String,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        if (currentDeviceId.isBlank()) {
            onResult(Result.failure(IllegalStateException("No device id set")))
            return
        }
        if (lotteryId.isBlank() || invitedOwnerId.isBlank() || campsiteId.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Lottery ID, owner ID, and campsite ID are required")))
            return
        }
        if (invitedOwnerId == currentDeviceId) {
            onResult(Result.failure(IllegalArgumentException("You cannot invite yourself")))
            return
        }

        val inviteData = mapOf(
            "lotteryId" to lotteryId,
            "inviterId" to currentDeviceId,
            "invitedOwnerId" to invitedOwnerId,
            "campsiteId" to campsiteId,
            "status" to "PENDING"
        )

        lotteryRef.document(lotteryId)
            .collection("invites")
            .document(invitedOwnerId)
            .set(inviteData)
            .addOnSuccessListener {
                notifyUser(
                    content = "You have been invited to add your campsite to a lottery.",
                    userId = invitedOwnerId,
                    ownerId = currentDeviceId,
                    lotteryId = lotteryId
                )
                notifyViews()
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    /**
     * accepts a lottery invite for the current owner, adding them as a co-owner
     * and attaching their campsite to the lottery
     *
     * @param lotteryId id of the lottery
     * @param campsiteId id of the campsite being added to the lottery
     * @param onResult success or failure
     */
    fun acceptLotteryInvite(
        lotteryId: String,
        campsiteId: String,
        onResult: (Result<Unit>) -> Unit = {}
    ) {
        if (currentDeviceId.isBlank()) {
            onResult(Result.failure(IllegalStateException("No device id set")))
            return
        }
        if (lotteryId.isBlank() || campsiteId.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Lottery ID and campsite ID are required")))
            return
        }

        val lotteryDoc = lotteryRef.document(lotteryId)
        val inviteDoc = lotteryDoc.collection("invites").document(currentDeviceId)
        val campDoc = campsiteRef.document(campsiteId)

        val batch = db.batch()
        batch.update(lotteryDoc, "ownerId", FieldValue.arrayUnion(currentDeviceId))
        batch.update(lotteryDoc, "campsiteIds", FieldValue.arrayUnion(campsiteId))
        batch.update(campDoc, "lotteryId", lotteryId)
        batch.update(inviteDoc, "status", "ACCEPTED")

        batch.commit()
            .addOnSuccessListener {
                notifyViews()
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun deleteLottery(lottery: Lottery, onResult: (Boolean) -> Unit = {}) {
        val batch = db.batch()
        batch.delete(lotteryRef.document(lottery.id))
        lottery.campsiteIds.forEach { batch.update(campsiteRef.document(it), "lotteryId", "") }
        batch.commit()
            .addOnSuccessListener {
                notifyViews() // [4]
                onResult(true) // [4]
            }
            .addOnFailureListener { onResult(false) }
    }

    fun updateLottery( // The following portion of this function was modified with AI [3]
        lotteryId: String,
        campsiteIds: List<String>,
        registrationStart: String,
        registrationEnd: String,
        bookingStart: String,
        bookingEnd: String,
        numWinners: Int,
        waitlistCapacity: Int = 0,
        name: String = "",
        onResult: (Result<Unit>) -> Unit
    ) {
        val updates = mapOf(
            "name" to name,
            "campsiteIds" to campsiteIds,
            "registrationStart" to registrationStart,
            "registrationEnd" to registrationEnd,
            "bookingStart" to bookingStart,
            "bookingEnd" to bookingEnd,
            "numWinners" to numWinners,
            "waitlistCapacity" to waitlistCapacity
        )

        lotteryRef.document(lotteryId)
            .update(updates)                   // the rest of this fn was not modified by [3]
            .addOnSuccessListener {
                notifyViews() // [4]
                onResult(Result.success(Unit)) // [4]
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // rewritten in [19] in Citation.md
    /**
     * Gets a list of lotteries that the user is in
     *
     * waitlist entries live in the lottery document now so this finds the lotteries
     * whose entrantIds has the user then keeps the ones where their entry has one of the statuses
     * eg WAITING and SELECTED for the lotteries they can still do something about
     *
     * @param statuses which entry statuses count, every status if none are given
     * @param onResult gets the lotteries or an empty list if it failed
     */
    fun getLotteriesIn(vararg statuses: EntryStatus, onResult: (List<Lottery>) -> Unit) {
        lotteryRef.whereArrayContains("entrantIds", currentDeviceId).get()
            .addOnSuccessListener { documents ->
                val lotteries = documents.toObjects(Lottery::class.java).filter { lottery ->
                    val entry = lottery.entryFor(currentDeviceId) ?: return@filter false
                    statuses.isEmpty() || entry.status in statuses
                }
                onResult(lotteries)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    /**
     * gets one lottery by its id
     *
     * @param lotteryId id of the lottery, eg a campsites lotteryId, blank means no lottery
     * @param onResult gets the lottery or null if there isnt one or it couldnt be read
     */
    fun getLotteryById(lotteryId: String, onResult: (Lottery?) -> Unit) {
        if (lotteryId.isBlank()) {
            onResult(null)
            return
        }
        lotteryRef.document(lotteryId).get()
            .addOnSuccessListener { snapshot ->
                val lottery = try {
                    snapshot.toObject(Lottery::class.java)
                } catch (e: RuntimeException) {
                    Log.e("Model", "Skipping unreadable lottery ${snapshot.id}", e)
                    null
                }
                onResult(lottery)
            }
            .addOnFailureListener { onResult(null) }
    }

    /**
     * gets the lotteries an owner runs or co-owns
     *
     * Lottery.ownerId is a list so an owner who accepted an invite is found too
     *
     * @param ownerId device id of the owner, the current user by default
     * @param onResult gets the lotteries or an empty list if it failed
     */
    fun getLotteriesByOwner(ownerId: String = currentDeviceId, onResult: (List<Lottery>) -> Unit) {
        if (ownerId.isBlank()) {
            onResult(emptyList())
            return
        }
        lotteryRef.whereArrayContains("ownerId", ownerId).get()
            .addOnSuccessListener { documents ->
                // one bad document shouldnt hide the rest, same as toCampsites
                onResult(documents.documents.mapNotNull { d ->
                    try {
                        d.toObject(Lottery::class.java)
                    } catch (e: RuntimeException) {
                        Log.e("Model", "Skipping unreadable lottery ${d.id}", e)
                        null
                    }
                })
            }
            .addOnFailureListener { onResult(emptyList()) }
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
                // the owner drawing is the sender so it shows in their sent notifications (US 02.10.02)
                winners.forEach {
                    notifyUser("You won the lottery, you can book your dates now", it, currentDeviceId, lotId)
                }
                others.forEach {
                    notifyUser("You were not selected in the lottery this time", it, currentDeviceId, lotId)
                }
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

    // see [19] in Citation.md
    /**
     * lets a lottery winner say no to their win (US 01.04.03)
     *
     * eg they changed their mind or none of the available dates work
     * their entry goes from SELECTED to DECLINED in a transaction so it cant clash with reserving
     * it stays on the waitlist so the owner can still see who declined
     *
     * @param lottery the lottery they won
     * @param onResult success once its declined or a failure saying why eg they already reserved
     */
    fun declineLotteryWin(lottery: Lottery, onResult: (Result<Unit>) -> Unit = {}) {
        if (lottery.id.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Lottery not found")))
            return
        }
        val lotteryDoc = lotteryRef.document(lottery.id)
        // gives back why it couldnt decline or null if it did so the message isnt lost inside firestores exception
        db.runTransaction<String?> { transaction ->
            val current = transaction.get(lotteryDoc).toObject(Lottery::class.java)
                ?: return@runTransaction "Lottery not found"
            current.whyNotAWinner(currentDeviceId)?.let { return@runTransaction it }
            transaction.update(lotteryDoc, "waitlist", withMyStatus(current, EntryStatus.DECLINED))
            null
        }
            .addOnSuccessListener { error ->
                if (error != null) {
                    onResult(Result.failure(IllegalStateException(error)))
                    return@addOnSuccessListener
                }
                onResult(Result.success(Unit))
                notifyViews()
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    // the lotterys waitlist with the current users entry set to a new status
    // see [19] in Citation.md
    private fun withMyStatus(lottery: Lottery, status: EntryStatus): List<WaitlistEntry> =
        lottery.waitlist.map {
            if (it.id == currentDeviceId) WaitlistEntry(id = it.id, lotteryId = it.lotteryId, status = status) else it
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
         * once its saved the entrant gets a confirmation notification (US 01.05.01)
         * if the campsite has rules the entrant has to have agreed to them first (US 02.11.01)
         *
         * @param checkIn check in date like 2027-05-01
         * @param checkOut check out date like 2027-05-03
         * @param guests number of guests
         * @param campsite the campsite to book
         * @param rulesAccepted true if the entrant agreed to the campsite rules
         * @param onResult gets the saved reservation or a failure if the rules werent agreed to or saving failed
         */
        fun makeReservation(
            checkIn: String,
            checkOut: String,
            guests: Int,
            campsite: Campsite,
            rulesAccepted: Boolean = false,
            onResult: (Result<Reservation>) -> Unit = {}
        ) {
            if (campsite.hasRules() && !rulesAccepted) {
                onResult(Result.failure(IllegalArgumentException("You have to agree to the campsite rules first")))
                return
            }
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
            doc.set(newReservation)
                .addOnSuccessListener {
                    // notifyUser skips it if they opted out so the csv download is the backup (US 01.05.02)
                    notifyUser(
                        "Your reservation at ${campsite.name} from $checkIn to $checkOut is confirmed",
                        currentDeviceId
                    )
                    onResult(Result.success(newReservation))
                    notifyViews()
                }
                .addOnFailureListener { exception -> onResult(Result.failure(exception)) }
        }

        // see [19] in Citation.md
        /**
         * books a stay at one of the campsites of a lottery the current user won (US 01.04.01 01.04.02)
         *
         * the stay has to fit in the campsites available dates and the winner can only reserve once
         * the reservation is saved and their entry goes from SELECTED to RESERVED in one transaction
         * so tapping twice cant book two stays
         * once its saved the entrant gets a confirmation notification (US 01.05.01)
         *
         * the booked dates are read before the transaction since firestore transactions cant run queries
         * so two winners booking the same nights at the exact same time could still both get them
         *
         * @param lottery the lottery they won
         * @param campsite which of the lotterys campsites they picked
         * @param checkIn check in date like 2027-05-01
         * @param checkOut check out date like 2027-05-03
         * @param guests number of guests
         * @param rulesAccepted true if the entrant agreed to the campsite rules (US 02.11.01)
         * @param today the date to compare against so tests can pick one
         * @param onResult gets the saved reservation or a failure saying whats wrong
         */
        fun reserveLotteryWin(
            lottery: Lottery,
            campsite: Campsite,
            checkIn: String,
            checkOut: String,
            guests: Int,
            rulesAccepted: Boolean = false,
            today: LocalDate = LocalDate.now(),
            onResult: (Result<Reservation>) -> Unit = {}
        ) {
            val fail = { message: String -> onResult(Result.failure(IllegalArgumentException(message))) }
            if (lottery.id.isBlank()) return fail("Lottery not found")
            if (campsite.id !in lottery.campsiteIds) return fail("That campsite isn't part of this lottery")
            if (!campsite.canHold(guests)) return fail("This campsite fits at most ${campsite.maxOccupancy} guests")
            if (campsite.hasRules() && !rulesAccepted) return fail("You have to agree to the campsite rules first")

            getBookedDates(campsite.id) { booked ->
                if (!campsite.availability.canBook(DateRange(checkIn, checkOut), booked, today)) {
                    return@getBookedDates fail("Those dates aren't available")
                }

                val lotteryDoc = lotteryRef.document(lottery.id)
                val reservationDoc = reservationRef.document()
                val newReservation = Reservation(
                    id = reservationDoc.id,
                    campsiteId = campsite.id,
                    entrantId = currentDeviceId,
                    checkIn = checkIn,
                    checkOut = checkOut,
                    numGuests = guests,
                    status = ReservationStatus.ACTIVE,
                    rulesAccepted = rulesAccepted,
                    lotteryId = lottery.id
                )

                // gives back why it couldnt reserve or null if it did so the message isnt lost inside firestores exception
                db.runTransaction<String?> { transaction ->
                    // read the saved lottery since the one the view has might be out of date
                    val current = transaction.get(lotteryDoc).toObject(Lottery::class.java)
                        ?: return@runTransaction "Lottery not found"
                    current.whyCantReserve(currentDeviceId, today)?.let { return@runTransaction it }
                    transaction.set(reservationDoc, newReservation)
                    transaction.update(lotteryDoc, "waitlist", withMyStatus(current, EntryStatus.RESERVED))
                    null
                }
                    .addOnSuccessListener { error ->
                        if (error != null) return@addOnSuccessListener fail(error)
                        notifyUser(
                            "Your reservation at ${campsite.name} from $checkIn to $checkOut is confirmed",
                            currentDeviceId
                        )
                        onResult(Result.success(newReservation))
                        notifyViews()
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
        }

        /**
         * lets an owner reject a booked reservation eg when a fire closes the campsite (US 02.08.01)
         *
         * only stays that are active and not over yet can be rejected
         * rejecting frees the dates since booked dates only count active reservations
         *
         * once its rejected the entrant who booked it gets a notification
         *
         * @param reservation the reservation to reject
         * @param onResult success once its rejected or a failure if it wasnt found wasnt active or saving failed
         */
        fun rejectReservation(reservation: Reservation, onResult: (Result<Unit>) -> Unit = {}) {
            endActiveReservation(reservation.id, ReservationStatus.REJECTED) { result ->
                if (result.isSuccess) {
                    notifyReservationEnded(reservation, reservation.entrantId, "was rejected by the campsite owner", currentDeviceId)
                }
                onResult(result)
            }
        }

        /**
         * lets an entrant cancel one of their reservations
         *
         * only stays that are active and not over yet can be cancelled
         * cancelling frees the dates since booked dates only count active reservations
         *
         * once its cancelled the entrant gets a confirmation notification (US 01.07.02)
         *
         * @param reservation the reservation to cancel
         * @param onResult success once its cancelled or a failure if it wasnt found wasnt active or saving failed
         */
        fun cancelReservation(reservation: Reservation, onResult: (Result<Unit>) -> Unit = {}) {
            endActiveReservation(reservation.id, ReservationStatus.CANCELLED) { result ->
                if (result.isSuccess) {
                    // the entrant is cancelling their own so its them if the id wasnt saved
                    notifyReservationEnded(reservation, reservation.entrantId.ifBlank { currentDeviceId }, "was cancelled")
                }
                onResult(result)
            }
        }

        /**
         * tells the entrant who booked a reservation that it was cancelled or rejected
         *
         * reservations only keep the campsite id so this looks up the name for the message
         *
         * @param reservation the reservation that ended
         * @param entrantId who to tell and nobody gets told if its blank
         * @param what how it ended like was cancelled
         * @param ownerId the owner who ended it, or blank if the entrant cancelled it themselves (US 02.10.02)
         */
        private fun notifyReservationEnded(reservation: Reservation, entrantId: String, what: String, ownerId: String = "") {
            if (entrantId.isBlank()) return
            val sendNotification = { campsite: Campsite? ->
                val where = campsite?.name ?: "your campsite"
                notifyUser(
                    "Your reservation at $where from ${reservation.checkIn} to ${reservation.checkOut} $what",
                    entrantId,
                    ownerId
                )
            }
            // firestore throws on a blank document id so skip the lookup then
            if (reservation.campsiteId.isBlank()) sendNotification(null)
            else getCampsite(reservation.campsiteId, sendNotification)
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
         * gets the cancelled and rejected reservations at all of the current owners campsites
         *
         * rejected ones are in here too so they dont disappear after the owner rejects them (US 02.08.01)
         *
         * @param onResult gets the reservations sorted by check in or an empty list if it failed
         */
        fun getOwnerCancelledReservations(onResult: (List<OwnerReservation>) -> Unit) {
            getOwnerReservations { all ->
                onResult(all.filter { it.reservation.isCancelled() || it.reservation.isRejected() }
                    .sortedBy { it.reservation.checkIn })
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

    // see [19] in Citation.md
    /**
     * gets the stays a lottery winner can still book at a campsite (US 01.04.01)
     *
     * the owners open period minus the past and every active reservation
     *
     * @param campsite the campsite to check
     * @param today nights before this arent shown so tests can pick one
     * @param onResult gets each free stretch as check in to latest check out sorted by date
     */
    fun getAvailableDates(campsite: Campsite, today: LocalDate = LocalDate.now(), onResult: (List<DateRange>) -> Unit) {
        getBookedDates(campsite.id) { booked ->
            onResult(campsite.availability.freeStays(booked, today))
        }
    }
    //endregion


    //region Notifications
    // [2]

    /**
     * Sends a notification to the user
     *
     * saves it in the notifications collection and adds its id to the users notifications list
     * does nothing if the user turned notifications off
     *
     * @param content The notification message
     * @param userId The user to send the notification to
     * @param ownerId Device id of the owner sending the notification, or blank if system-sent
     * @param lotteryId Id of the lottery this notification is about, or blank if not about one
     */
    fun notifyUser(
        content: String,
        userId: String,
        ownerId: String = "",
        lotteryId: String = "",
    ) {
        // getUserObj is async so everything that needs the user has to happen inside the callback
        getUserObj(userId) { user ->
            if (user == null || !user.notificationsEnabled) return@getUserObj

            val doc = notificationRef.document()
            val newNotification = Notification(
                id = doc.id,
                entrantId = userId,
                ownerId = ownerId,
                content = content,
                // format YYYY-MM-DDTHH:MM:SS, truncated so the seconds always show
                timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                lotteryId = lotteryId,
            )

            doc.set(newNotification).addOnSuccessListener {
                // arrayUnion adds just the id so we dont overwrite the whole user document
                usersRef.document(userId).update("notifications", FieldValue.arrayUnion(doc.id))
                notifyViews()
            }
        }
    }

    /**
     * gets all notifications received by this device ID from the notifications collection
     *
     * @param userId device id of the recipient, defaults to [currentDeviceId]
     * @param onResult gets the notifications oldest first or an empty list if it failed
     */
    fun getNotifications(
        userId: String = currentDeviceId,
        onResult: (List<Notification>) -> Unit,
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

    /**
     * gets all notifications sent by an owner
     *
     * @param ownerId device id of the owner, defaults to [currentDeviceId]
     * @param onResult gets the sent notifications newest first or an empty list if it failed
     */
    fun getSentNotifications(
        ownerId: String = currentDeviceId,
        onResult: (List<Notification>) -> Unit,
    ) {
        if (ownerId.isBlank()) {
            onResult(emptyList())
            return
        }
        notificationRef.whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { documents ->
                val notifications = documents.toObjects(Notification::class.java)
                    .sortedByDescending { it.timestamp }
                onResult(notifications)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * sends a notification from the current owner to all entrants currently waiting on a lottery waitlist
     *
     * verifies the lottery exists and is owned by [currentDeviceId] before notifying
     *
     * @param lotteryId id of the lottery
     * @param content the notification message (must not be blank)
     * @param onResult gets the number of waiting entrants notified or the failure
     */
    fun notifyWaitlistEntrants(
        lotteryId: String,
        content: String,
        onResult: (Result<Int>) -> Unit = {},
    ) {
        if (currentDeviceId.isBlank()) {
            onResult(Result.failure(IllegalStateException("No device id set")))
            return
        }
        if (lotteryId.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Invalid lottery ID")))
            return
        }
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Notification content cannot be blank")))
            return
        }

        lotteryRef.document(lotteryId).get()
            .addOnSuccessListener { snapshot ->
                val lottery = snapshot.toObject(Lottery::class.java)
                if (!snapshot.exists() || lottery == null) {
                    onResult(Result.failure(Exception("Lottery not found")))
                    return@addOnSuccessListener
                }
                if (currentDeviceId !in lottery.ownerId) {
                    onResult(Result.failure(IllegalStateException("You do not own this lottery")))
                    return@addOnSuccessListener
                }

                val waitingEntrantIds = lottery.waitlist
                    .filter { it.status == EntryStatus.WAITING }
                    .map { it.id }
                    .distinct()

                waitingEntrantIds.forEach { entrantId ->
                    notifyUser(
                        content = trimmedContent,
                        userId = entrantId,
                        ownerId = currentDeviceId,
                        lotteryId = lotteryId,
                    )
                }
                onResult(Result.success(waitingEntrantIds.size))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
    //endregion

    //region Messages

    // which conversation the Messages screen shows (US 01.14.01, 02.12.01)
    // a blank id means the list of conversations
    var messageReservationId: String = ""
    var messageTitle: String = ""

    /**
     * picks the conversation the Messages screen shows, like settTempCampsite does for campsites
     *
     * @param reservationId the reservation the conversation is about, "" for the list of conversations
     * @param title what to show at the top, eg the campsite or entrant name
     */
    fun settMessageConversation(reservationId: String, title: String) {
        messageReservationId = reservationId
        messageTitle = title
        notifyViews()
    }

    // The following functions are from Google, Gemini, "Implement entrant and owner reservation messaging in Model.kt", 2026-11-26
    // [1]
    /**
     * sends a message from the current entrant to the campsite owner for an active reservation
     *
     * @param reservation the reservation the entrant is messaging about
     * @param contents the message text (must not be blank)
     * @param onResult gets the saved message or the failure
     */
    fun sendMessageToOwner(
        reservation: Reservation,
        contents: String,
        onResult: (Result<Message>) -> Unit = {},
    ) {
        sendReservationMessage(
            reservation = reservation,
            contents = contents,
            expectedRole = Role.ENTRANT,
            onResult = onResult,
        )
    }

    /**
     * sends a message from the current campsite owner to the entrant for an active reservation
     *
     * @param reservation the reservation the owner is messaging about
     * @param contents the message text (must not be blank)
     * @param onResult gets the saved message or the failure
     */
    fun sendMessageToEntrant(
        reservation: Reservation,
        contents: String,
        onResult: (Result<Message>) -> Unit = {},
    ) {
        sendReservationMessage(
            reservation = reservation,
            contents = contents,
            expectedRole = Role.OWNER,
            onResult = onResult,
        )
    }

    /**
     * sends a message to the other party on an active reservation
     *
     * automatically routes entrant -> owner or owner -> entrant based on [currentDeviceId]
     *
     * @param reservation the reservation the message is about
     * @param contents the message text (must not be blank)
     * @param onResult gets the saved message or the failure
     */
    fun sendMessage(
        reservation: Reservation,
        contents: String,
        onResult: (Result<Message>) -> Unit = {},
    ) {
        sendReservationMessage(
            reservation = reservation,
            contents = contents,
            expectedRole = null,
            onResult = onResult,
        )
    }

    /**
     * shared helper that validates the reservation, checks that [currentDeviceId] is either the
     * entrant or the campsite owner, saves the message, and notifies the recipient
     *
     * @param expectedRole if set, enforces that [currentDeviceId] is acting in that role
     */
    private fun sendReservationMessage(
        reservation: Reservation,
        contents: String,
        expectedRole: Role?,
        onResult: (Result<Message>) -> Unit,
    ) {
        if (currentDeviceId.isBlank()) {
            onResult(Result.failure(IllegalStateException("No device id set")))
            return
        }
        if (reservation.id.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Invalid reservation ID")))
            return
        }
        val trimmedContents = contents.trim()
        if (trimmedContents.isBlank()) {
            onResult(Result.failure(IllegalArgumentException("Message cannot be blank")))
            return
        }

        reservationRef.document(reservation.id).get()
            .addOnSuccessListener { resDoc ->
                val savedReservation = resDoc.toObject(Reservation::class.java)
                if (savedReservation == null) {
                    onResult(Result.failure(Exception("Reservation not found")))
                    return@addOnSuccessListener
                }
                if (!runCatching { savedReservation.isActive() }.getOrDefault(false)) {
                    onResult(Result.failure(IllegalStateException("Can only message about an active reservation")))
                    return@addOnSuccessListener
                }

                campsiteRef.document(savedReservation.campsiteId).get()
                    .addOnSuccessListener { campDoc ->
                        val campsite = campDoc.toObject(Campsite::class.java)
                        if (campsite == null || campsite.ownerId.isBlank()) {
                            onResult(Result.failure(Exception("Campsite owner not found")))
                            return@addOnSuccessListener
                        }

                        val recipientId = when {
                            currentDeviceId == savedReservation.entrantId && expectedRole != Role.OWNER ->
                                campsite.ownerId
                            currentDeviceId == campsite.ownerId && expectedRole != Role.ENTRANT ->
                                savedReservation.entrantId
                            else -> {
                                onResult(Result.failure(IllegalStateException("You are not authorized to message on this reservation")))
                                return@addOnSuccessListener
                            }
                        }

                        if (recipientId.isBlank()) {
                            onResult(Result.failure(Exception("Recipient not found")))
                            return@addOnSuccessListener
                        }

                        val doc = messageRef.document()
                        val message = Message(
                            id = doc.id,
                            senderId = currentDeviceId,
                            recipientId = recipientId,
                            reservationId = savedReservation.id,
                            contents = trimmedContents,
                            timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        )

                        doc.set(message)
                            .addOnSuccessListener {
                                notifyUser("New message about ${campsite.name}: $trimmedContents", recipientId)
                                notifyViews()
                                onResult(Result.success(message))
                            }
                            .addOnFailureListener { onResult(Result.failure(it)) }
                    }
                    .addOnFailureListener { onResult(Result.failure(it)) }
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    /**
     * gets all messages for a specific reservation, oldest first
     *
     * @param reservationId id of the reservation
     * @param onResult gets the messages or an empty list if it failed
     */
    fun getMessagesForReservation(
        reservationId: String,
        onResult: (List<Message>) -> Unit,
    ) {
        if (reservationId.isBlank()) {
            onResult(emptyList())
            return
        }
        messageRef.whereEqualTo("reservationId", reservationId).get()
            .addOnSuccessListener { documents ->
                val messages = documents.toObjects(Message::class.java)
                    .sortedBy { it.timestamp }
                onResult(messages)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * gets all messages sent or received by a user, oldest first
     *
     * @param userId device id of the user, defaults to [currentDeviceId]
     * @param onResult gets the messages or an empty list if it failed
     */
    fun getMessages(
        userId: String = currentDeviceId,
        onResult: (List<Message>) -> Unit,
    ) {
        if (userId.isBlank()) {
            onResult(emptyList())
            return
        }
        val sentQuery = messageRef.whereEqualTo("senderId", userId).get()
        val receivedQuery = messageRef.whereEqualTo("recipientId", userId).get()

        Tasks.whenAllSuccess<QuerySnapshot>(sentQuery, receivedQuery)
            .addOnSuccessListener { snapshots ->
                val messages = snapshots
                    .flatMap { it.toObjects(Message::class.java) }
                    .distinctBy { it.id }
                    .sortedBy { it.timestamp }
                onResult(messages)
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
        deleteQuery(lotteryRef.whereArrayContains("ownerId", currentDeviceId))

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
                        it,
                        currentDeviceId
                    )
                }
                batch.commit()
            }


    //endregion

}