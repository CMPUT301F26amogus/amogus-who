import android.companion.DeviceId
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Date

interface ModelUpdater<V>{
    fun addView(v:V)
    fun removeView(v:V)
}

open class Model: ModelUpdater<View>{
    override fun addView(v: View){

    }
    override fun removeView(v: View){

    }

    private val db = Firebase.firestore
    private val usersRef = db.collection("users")
    private val campsiteRef = db.collection("campsite")
    private val lotteryRef = db.collection("lottery")
    private val waitlistRef = db.collection("waitlist")
    private val reservationRef = db.collection("reservation")

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
                val campsites = documents.toObjects(Campsite::class.java)
                onResult(campsites)
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
        lotteryRef.document(lottery.lotId).set(lottery)
    }

    fun deleteLottery(lottery: Lottery) {
        lotteryRef.document(lottery.lotId).delete()
    }

    fun updateLottery(lottery: Lottery, regStart: Date, regEnd: Date, bookStart: Date,
                      bookEnd: Date, winnerCount: Int, waitlistCap: Int, lotteryStatus: LotteryStatus) {
        lottery.regStart = regStart
        lottery.regEnd = regEnd
        lottery.bookStart = bookStart
        lottery.bookEnd = bookEnd
        lottery.winnerCount = winnerCount
        lottery.waitlistCap = waitlistCap
        lottery.lotteryStatus = lotteryStatus

        lotteryRef.document(lottery.lotId).set(lottery)
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
                        entryId = entryId, lotId = lotId, entrantId = entrantId, joinedAt = System.currentTimeMillis(),
                        waitlistStatus = WaitlistStatus.WAITING)

                    entryRef.set(
                        mapOf(
                            "entryId" to entry.entryId,
                            "lotId" to entry.lotId,
                            "entrantId" to entry.entrantId,
                            "waitlistStatus" to entry.waitlistStatus.name,
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
            .whereEqualTo("waitlistStatus", WaitlistStatus.WAITING.name)
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
                    .whereEqualTo("waitlistStatus", WaitlistStatus.WAITING.name)
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
                                    WaitlistStatus.SELECTED
                                } else {
                                    WaitlistStatus.NOT_SELECTED
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

                if (status != ReservationStatus.PENDING.name) {
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
}