import com.google.firebase.Firebase
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import com.example.code.model.Campsite
import com.example.code.model.DateRange
import com.example.code.model.EntryStatus
import com.example.code.model.Lottery
import com.example.code.model.LotteryStatus
import com.example.code.model.Notification
import com.example.code.model.Reservation
import com.example.code.model.ReservationStatus
import com.example.code.model.Role
import com.example.code.model.User
import com.example.code.model.WaitlistEntry
import kotlin.String

interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View> {
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
    fun getUserObj(onResult: (User?) -> Unit) {
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

    fun setNotifCount(onResult: (Int) -> Unit) {
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
    fun setUserObj(user: User) {
        dataRef.document(currentDeviceId).set(user)
    }


    // Everything under this line should be functions that are used by the view
    /**
     * function that sets the device id of the user, this is needed by all models,
     * reference the currentDeviceId attribute.
     *
     * @param deviceId this is the deviceId passed in by the view
     */
    fun setDeviceInformation(deviceId: String) {
        this.currentDeviceId = deviceId
    }

    /**
     * delete a profile (any role)
     *
     */
    fun deleteProfile() {
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
    override fun addView(v: View) {
        if (v !in views) views.add(v)
    }

    /**
     * stops a view from getting updates eg when its screen is destroyed
     *
     * @param v the view to remove
     */
    override fun removeView(v: View) {
        views.remove(v)
    }

    /** tells every registered view this model changed so they can redraw */
    protected fun notifyViews() {
        views.forEach { it.update(this) }
    }

    private val usersRef by lazy { db.collection("users") }
    private val campsiteRef by lazy { db.collection("campsite") }
    private val reservationRef by lazy { db.collection("reservations") }
    private val lotteryRef by lazy { db.collection("lotteries") }
    private val waitlistRef by lazy { db.collection("waitlist") }

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

    fun createCampsite(
        campName: String,
        location: String,
        desc: String,
        maxOcc: Int,
        price: Double,
        onResult: (Boolean) -> Unit
    ) {
        val campsiteId = campsiteRef.document().id
        val campsite: Campsite = Campsite(
            id = campsiteId, name = campName, location = location,
            description = desc, maxOccupancy = maxOcc, pricePerNight = price
        )

        campsiteRef.document(campsiteId).set(campsite)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
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
            val tags = updates["tags"] as? Set<*>

            if (tags == null || tags.any { it !is String }) {
                return "Tags must be a set of strings"
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
        if (campId.isBlank()) {
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
            val tags = firestoreUpdates["tags"] as Set<String>

            firestoreUpdates["tags"] = tags
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .toSet()
                .toList()
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
                        val campsite = document.toObject(Campsite::class.java)
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

        fun searchCampsite(query: String, onResult: (List<Campsite>) -> Unit) {
            campsiteRef.get()
                .addOnSuccessListener { documents ->
                    val campsite = documents.toObjects(Campsite::class.java)

                    val filter = campsite.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.location.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true)
                    }
                    onResult(campsite)
                }
                .addOnFailureListener {
                    onResult(emptyList())
                }
        }

    fun filterCampsite() {

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
                        onResult(
                            Result.failure(
                                Exception("You have already joined this waitlist")
                            )
                        )
                    } else {
                        val entry = WaitlistEntry(
                            lotteryId = lotId,
                            userId = entrantId,
                            joinedAt = System.currentTimeMillis(),
                            status = EntryStatus.WAITING
                        )

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
                    if (!document.exists()) {
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

        fun getReservationByUID(
            entrantId: String,
            onResult: (Result<List<ReservationStatus>>) -> Unit
        ) {
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

        fun getCampId(campName: String, onResult: (String) -> Unit) {
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
            reservationRef.whereEqualTo("entrantId", entrantId).whereEqualTo("status", "ACTIVE")
                .get()
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
            reservationRef.whereEqualTo("entrantId", entrantId).whereEqualTo("status", "CANCELLED")
                .get()
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
        fun makeReservation(checkIn: String, checkOut: String, guests: Int, campsite: Campsite) {
            val newReservation = Reservation(
                checkIn = checkIn,
                checkOut = checkOut,
                guests = guests,
                campsite = campsite
            )
            dataRef.add(newReservation).addOnSuccessListener { documentReference ->
                newReservation.id = documentReference.id
                newReservation.status = ReservationStatus.ACTIVE
                getUserObj() { user -> newReservation.entrant = user }
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
            getUserObj { user -> onResult(user!!.lotteries!!) }
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
        fun signUp(
            name: String,
            phoneNumber: String,
            email: String,
            roleOf: Role = Role.ENTRANT,
            onSuccess: () -> Unit,
        ) {
            val newUser = User(
                deviceId = currentDeviceId,
                name = name,
                email = email,
                phone = phoneNumber,
                role = roleOf
            )

            dataRef.document(currentDeviceId)
                .set(newUser)
                .addOnSuccessListener {
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
        fun signIn(onResult: (User?) -> Unit) {
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
        fun updateProfile(
            user: User,
            name: String = "",
            email: String = "",
            phoneNumber: String = ""
        ) {
            if (name != "") {
                dataRef.document(currentDeviceId).update(
                    "name", name
                )
            }
            if (email != "") {
                dataRef.document(currentDeviceId).update(
                    "email", email
                )
            }
            if (phoneNumber != "") {
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
        fun declineReservation(reservation: Reservation) {
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
        fun cancelReservation(reservation: com.example.code.model.Reservation) {
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
        fun getWaitlist(onResult: (List<Lottery>) -> Unit) {
            val lotteries = ArrayList<Lottery>()
            dataRef.document(currentDeviceId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
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
                dataRef.document("Notifications/$currentDeviceId/$notificationCount")
                    .set(Notification("", currentDeviceId, notification))
            }
            tempuser!!.notificationCount++
            setUserObj(tempuser!!)
        }

        /**
         *
         */
        fun getNotifications(onResult: (List<Notification>) -> Unit) {
            val notifications = ArrayList<Notification>()

            setNotifCount { notificationCount = it }

            for (i in 0..notificationCount) {
                dataRef.document("Notifications/$currentDeviceId/$i").get().addOnSuccessListener {
                    if (it.exists()) {
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
            dataRef.document(campsiteId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val campsite = snapshot.toObject(Campsite::class.java)
                    onResult(campsite?.lottery)
                } else {
                    onResult(null)
                }
            }
        }


}
