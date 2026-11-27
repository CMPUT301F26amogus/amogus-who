package com.example.code

import Model
import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.code.controllers.ReserveCampsiteController
import com.example.code.model.Campsite
import com.example.code.model.EntrantReservation
import com.example.code.model.Reservation
import com.example.code.model.ReservationStatus
import com.example.code.model.Role
import com.example.code.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * one test or more per user story
 *
 * named after the story id so its easy to match them up with the spec
 * the seed rule signs this device in as an entrant for us_01 tests and an owner for the rest
 * puts the test data in firestore then puts the devices real profile back when the test is done
 */
@RunWith(AndroidJUnit4::class)
class UserStoryTests {
    // the device id the app will read in MainActivity
    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
        Settings.Secure.ANDROID_ID,
    )

    private val usersRef by lazy { Firebase.firestore.collection("users") }
    private val campsiteRef by lazy { Firebase.firestore.collection("campsites") }
    private val reservationRef by lazy { Firebase.firestore.collection("reservations") }
    private val notificationRef by lazy { Firebase.firestore.collection("notifications") }
    private val userDoc by lazy { usersRef.document(deviceId) }

    // made up names so the test can find its own card and not someone elses
    // ids end in the device id so two people running the tests dont share data
    private val testCampsite = Campsite(
        id = "us-02-07-site-$deviceId",
        ownerId = deviceId,
        name = "US 02.07.01 Campsite",
        location = "Test Park",
    )

    // reservations only keep the entrants id so each entrant gets a user doc for the name on the card
    private val seededEntrants = mutableListOf<User>()

    // makes a reservation at the test campsite with its own entrant so each card can be found by name
    private fun seededReservation(
        key: String,
        entrantName: String,
        checkInDaysFromNow: Long,
        status: ReservationStatus,
    ): Reservation {
        val entrant = User(deviceId = "us-02-07-$key-entrant-$deviceId", name = entrantName)
        seededEntrants.add(entrant)
        return Reservation(
            id = "us-02-07-$key-$deviceId",
            campsiteId = testCampsite.id,
            entrantId = entrant.deviceId,
            checkIn = LocalDate.now().plusDays(checkInDaysFromNow).toString(),
            checkOut = LocalDate.now().plusDays(checkInDaysFromNow + 2).toString(),
            numGuests = 2,
            status = status,
        )
    }

    // the name on this reservations card
    private fun nameOf(reservation: Reservation): String =
        seededEntrants.first { it.deviceId == reservation.entrantId }.name

    private val activeReservation =
        seededReservation("active", "US 02.07.01 Entrant", 7, ReservationStatus.ACTIVE)
    private val testEntrant = seededEntrants.single { it.deviceId == activeReservation.entrantId }
    // still saved as active since nothing sets PAST but check out was a week ago
    private val finishedReservation =
        seededReservation("finished", "US 02.07.02 Finished Entrant", -10, ReservationStatus.ACTIVE)
    private val markedPastReservation =
        seededReservation("marked-past", "US 02.07.02 Marked Past Entrant", -20, ReservationStatus.PAST)
    // lists sort by check in and only cards on screen can be found
    // so dates are picked so each test's cards are at the top of their tab
    private val cancelledReservation =
        seededReservation("cancelled", "US 02.07.03 Cancelled Entrant", 30, ReservationStatus.CANCELLED)
    // the owner rejected this one so it shouldnt show as cancelled
    private val rejectedReservation =
        seededReservation("rejected", "US 02.07.03 Rejected Entrant", 7, ReservationStatus.REJECTED)
    // start active and get cancelled or declined by the entrant during the test
    // check in after the 02.07.01 one so it stays first under active
    private val toCancelReservation =
        seededReservation("to-cancel", "US 02.07.03 Cancels Entrant", 8, ReservationStatus.ACTIVE)
    private val toDeclineReservation =
        seededReservation("to-decline", "US 02.07.03 Declines Entrant", 9, ReservationStatus.ACTIVE)
    // starts active and the owner rejects it during the 02.08.01 tests
    // checks in before every other active one so its card is first under active
    private val toRejectReservation =
        seededReservation("to-reject", "US 02.08.01 Rejected Entrant", 6, ReservationStatus.ACTIVE)

    private val seededReservations = listOf(
        activeReservation,
        finishedReservation,
        markedPastReservation,
        cancelledReservation,
        rejectedReservation,
        toCancelReservation,
        toDeclineReservation,
        toRejectReservation,
    )

    // entrant cards show the campsite not the entrant so each entrant reservation gets its own campsite
    // they belong to a made up owner so they never show up in the owner tests
    private val entrantCampsites = mutableListOf<Campsite>()

    // makes a reservation for this device at its own campsite so the card can be found by campsite name
    private fun entrantReservation(
        key: String,
        campsiteName: String,
        checkInDaysFromNow: Long,
        status: ReservationStatus,
    ): Reservation {
        val campsite = Campsite(
            id = "us-01-$key-site-$deviceId",
            ownerId = "us-01-owner-$deviceId",
            name = campsiteName,
            location = "Test Park",
        )
        entrantCampsites.add(campsite)
        return Reservation(
            id = "us-01-$key-$deviceId",
            campsiteId = campsite.id,
            entrantId = deviceId,
            checkIn = LocalDate.now().plusDays(checkInDaysFromNow).toString(),
            checkOut = LocalDate.now().plusDays(checkInDaysFromNow + 2).toString(),
            numGuests = 3,
            status = status,
        )
    }

    // the campsite name on this reservations card
    private fun campsiteOf(reservation: Reservation): String =
        entrantCampsites.first { it.id == reservation.campsiteId }.name

    // current cards sort by check in so the one the cancel test uses comes first
    private val myToCancelReservation =
        entrantReservation("to-cancel", "US 01.07.01 Cancel Campsite", 3, ReservationStatus.ACTIVE)
    private val myUpcomingReservation =
        entrantReservation("upcoming", "US 01.06.01 Upcoming Campsite", 5, ReservationStatus.ACTIVE)
    // past cards sort newest first so the finished one comes first
    // still saved as active since nothing sets PAST but check out was a week ago
    private val myFinishedReservation =
        entrantReservation("finished", "US 01.06.02 Finished Campsite", -10, ReservationStatus.ACTIVE)
    private val myMarkedPastReservation =
        entrantReservation("marked-past", "US 01.06.02 Marked Past Campsite", -20, ReservationStatus.PAST)
    // cancelled and rejected ones shouldnt show on either tab
    private val myCancelledReservation =
        entrantReservation("cancelled", "US 01.06 Cancelled Campsite", 12, ReservationStatus.CANCELLED)
    private val myRejectedReservation =
        entrantReservation("rejected", "US 01.06 Rejected Campsite", 14, ReservationStatus.REJECTED)

    private val entrantReservations = listOf(
        myToCancelReservation,
        myUpcomingReservation,
        myFinishedReservation,
        myMarkedPastReservation,
        myCancelledReservation,
        myRejectedReservation,
    )

    // a campsite with nothing booked that the 01.05 tests reserve through the controller
    // fits 4 so the guest check passes
    private val reserveCampsite = Campsite(
        id = "us-01-05-site-$deviceId",
        ownerId = "us-01-owner-$deviceId",
        name = "US 01.05.01 Reserve Campsite",
        location = "Test Park",
        maxOccupancy = 4,
    ).also { entrantCampsites.add(it) }

    // runs before the compose rule because the app picks owner or entrant home as soon as it opens
    // every test gets all the data since owner and entrant data cant show up in each others lists
    @get:Rule(order = 0)
    val seedData = TestRule { base, description ->
        object : Statement() {
            override fun evaluate() {
                // entrant stories start with us_01 and need the entrant home so everything else is an owner
                val role = if (description.methodName.startsWith("us_01")) Role.ENTRANT else Role.OWNER
                // opted out tests check the csv still works when notifications are off (US 01.10.01)
                val notificationsOn = !description.methodName.contains("opted_out")
                // keep whatever profile this device had so we can put it back after
                val savedUser = await(userDoc.get()).data
                // reserving and cancelling make docs with new ids so remember what was already there
                val reservationsBefore = myDocIds(reservationRef)
                val notificationsBefore = myDocIds(notificationRef)
                try {
                    await(userDoc.set(User(
                        deviceId = deviceId,
                        name = "US Test ${role.name}",
                        notificationsEnabled = notificationsOn,
                        role = role,
                    )))
                    await(campsiteRef.document(testCampsite.id).set(testCampsite))
                    entrantCampsites.forEach { await(campsiteRef.document(it.id).set(it)) }
                    seededEntrants.forEach { await(usersRef.document(it.deviceId).set(it)) }
                    (seededReservations + entrantReservations).forEach {
                        await(reservationRef.document(it.id).set(it))
                    }
                    base.evaluate()
                } finally {
                    (seededReservations + entrantReservations).forEach {
                        await(reservationRef.document(it.id).delete())
                    }
                    seededEntrants.forEach {
                        // rejecting sends the seeded entrant a notification (US 02.08.01)
                        deleteNewDocs(notificationRef, emptySet(), it.deviceId)
                        await(usersRef.document(it.deviceId).delete())
                    }
                    entrantCampsites.forEach { await(campsiteRef.document(it.id).delete()) }
                    await(campsiteRef.document(testCampsite.id).delete())
                    deleteNewDocs(reservationRef, reservationsBefore)
                    deleteNewDocs(notificationRef, notificationsBefore)
                    if (savedUser != null) await(userDoc.set(savedUser)) else await(userDoc.delete())
                }
            }
        }
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity

    // blocks until firestore answers so the data is there before the app opens
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 10, TimeUnit.SECONDS)

    // ids of this devices docs in a collection thats keyed by entrantId
    private fun myDocIds(ref: CollectionReference): Set<String> =
        await(ref.whereEqualTo("entrantId", deviceId).get()).documents.map { it.id }.toSet()

    // deletes this devices docs the test made and leaves the ones that were there before
    // or a seeded entrants docs when given their id
    private fun deleteNewDocs(ref: CollectionReference, before: Set<String>, entrantId: String = deviceId) {
        await(ref.whereEqualTo("entrantId", entrantId).get()).documents
            .filter { it.id !in before }
            .forEach { await(it.reference.delete()) }
    }

    // what every notification sent to this device says or to a seeded entrant when given their id
    private fun myNotifications(entrantId: String = deviceId): List<String> =
        await(notificationRef.whereEqualTo("entrantId", entrantId).get()).documents
            .mapNotNull { it.getString("content") }

    // the model sends notifications without handing back a task so keep checking until one shows up
    private fun waitForNotification(vararg parts: String): String = waitForNotificationTo(deviceId, *parts)

    // same as waitForNotification but for someone other than this device like a seeded entrant
    private fun waitForNotificationTo(entrantId: String, vararg parts: String): String {
        val giveUpAt = System.currentTimeMillis() + 10_000
        while (true) {
            myNotifications(entrantId).firstOrNull { message -> parts.all { it in message } }?.let { return it }
            check(System.currentTimeMillis() < giveUpAt) { "No notification saying ${parts.toList()}" }
            Thread.sleep(250)
        }
    }

    // how the date fields on the reserve screen give dates like 05012027
    private fun fieldDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MMddyyyy"))

    // reserves through the same controller the reserve screen uses and waits for the answer
    private fun reserve(
        campsite: Campsite,
        checkIn: LocalDate,
        checkOut: LocalDate,
        guests: String,
    ): Result<EntrantReservation> {
        var result: Result<EntrantReservation>? = null
        val done = CountDownLatch(1)
        val model = Model().apply { setDeviceInformation(deviceId) }
        ReserveCampsiteController(model).makeReservation(
            campsite, fieldDate(checkIn), fieldDate(checkOut), guests,
        ) {
            result = it
            done.countDown()
        }
        assertTrue("reserve never answered", done.await(10, TimeUnit.SECONDS))
        return result!!
    }

    // goes from the entrant home to the notifications screen
    private fun openEntrantNotifications() {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("My Reservations")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithContentDescription("Notifications")
            .performClick()
        composeTestRule
            .onNodeWithTag("Header: Notifications")
            .assertExists()
    }

    // cancels the first current reservation through the popup like an entrant would
    private fun cancelFirstCurrentReservation() {
        openEntrantReservations("Current")
        waitForText(campsiteOf(myToCancelReservation))
        composeTestRule
            .onAllNodesWithText("X Cancel")
            .onFirst()
            .performClick()
        composeTestRule
            .onNodeWithText("Yes, cancel")
            .performClick()
    }

    // waits for firestore to load something onto the screen
    private fun waitForText(text: String) {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(text, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    // goes from the owner home to one tab of the reservations screen
    private fun openOwnerReservations(tab: String) {
        // the app shows a spinner until it loads the user so wait for the owner home
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("Reservations")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("Reservations")
            .performClick()
        composeTestRule
            .onNodeWithTag("Header: Reservations")
            .assertExists()
        composeTestRule
            .onNodeWithText(tab)
            .performClick()
    }

    // the card for this entrant is on screen
    private fun assertShown(entrantName: String) {
        composeTestRule
            .onAllNodesWithText(entrantName)
            .onFirst()
            .assertIsDisplayed()
    }

    // the card for this entrant isnt on this tab
    private fun assertNotShown(entrantName: String) {
        composeTestRule
            .onNodeWithText(entrantName)
            .assertDoesNotExist()
    }

    // cancel and decline dont hand back a task so keep checking firestore until the status changes
    private fun waitForStatus(reservationId: String, status: ReservationStatus) {
        val giveUpAt = System.currentTimeMillis() + 10_000
        while (await(reservationRef.document(reservationId).get()).getString("status") != status.name) {
            check(System.currentTimeMillis() < giveUpAt) { "Reservation $reservationId never became $status" }
            Thread.sleep(250)
        }
    }

    // goes from the entrant home to one tab of the reservations screen
    private fun openEntrantReservations(tab: String) {
        // the app shows a spinner until it loads the user so wait for the entrant home
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("My Reservations")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("My Reservations")
            .performClick()
        composeTestRule
            .onNodeWithTag("Header: Reservations")
            .assertExists()
        composeTestRule
            .onNodeWithText(tab)
            .performClick()
    }

    // the dates the way the cards show them like 05/01/27 | 05/03/27
    private fun cardDates(reservation: Reservation): String {
        val format = DateTimeFormatter.ofPattern("MM/dd/yy")
        return "${LocalDate.parse(reservation.checkIn).format(format)} | ${LocalDate.parse(reservation.checkOut).format(format)}"
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 02.07.01", 2026-11-23
    */
    // US 02.07.01
    // as a campsite owner i want to view a list of active reservations
    @Test
    fun us_02_07_01_owner_views_active_reservations() {
        // the app shows a spinner until it loads the user so wait for the owner home
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("Reservations")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // open reservations from the owner home
        composeTestRule
            .onNodeWithTag("Reservations")
            .performClick()
        composeTestRule
            .onNodeWithTag("Header: Reservations")
            .assertExists()

        // active reservations are the ones under the active tab
        composeTestRule
            .onNodeWithText("Active")
            .performClick()

        // the seeded reservation shows up with its entrant and campsite
        waitForText(testEntrant.name)
        composeTestRule
            .onAllNodesWithText(testEntrant.name)
            .onFirst()
            .assertIsDisplayed()
        composeTestRule
            .onAllNodesWithText(testCampsite.name, substring = true)
            .onFirst()
            .assertIsDisplayed()

        // active ones can be rejected (US 02.08.01) but theyre already booked so theres nothing to accept
        composeTestRule
            .onAllNodesWithText("Reject")
            .onFirst()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Accept")
            .assertDoesNotExist()
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 02.07.02", 2026-11-23
    */
    // US 02.07.02
    // as a campsite owner i want to view a list of past reservations
    @Test
    fun us_02_07_02_owner_views_past_reservations() {
        openOwnerReservations("Past")

        // finished stays show up whether theyre still saved as active or marked past
        waitForText(nameOf(finishedReservation))
        assertShown(nameOf(finishedReservation))
        assertShown(nameOf(markedPastReservation))

        // active cancelled and rejected ones dont belong here
        assertNotShown(testEntrant.name)
        assertNotShown(nameOf(cancelledReservation))
        assertNotShown(nameOf(rejectedReservation))

        // past ones are over so theres nothing to accept or reject
        composeTestRule
            .onNodeWithText("Reject")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Accept")
            .assertDoesNotExist()

        // and a finished stay isnt still listed as active
        composeTestRule
            .onNodeWithText("Active")
            .performClick()
        waitForText(testEntrant.name)
        assertNotShown(nameOf(finishedReservation))
        assertNotShown(nameOf(markedPastReservation))
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 02.07.03", 2026-11-23
    */
    // US 02.07.03
    // as a campsite owner i want to view a list of cancelled reservations
    @Test
    fun us_02_07_03_owner_views_cancelled_reservations() {
        openOwnerReservations("Cancelled")

        waitForText(nameOf(cancelledReservation))
        assertShown(nameOf(cancelledReservation))

        // rejected is the owner turning someone down not a cancellation
        assertNotShown(nameOf(rejectedReservation))
        assertNotShown(testEntrant.name)
        assertNotShown(nameOf(finishedReservation))

        // cancelled ones are already done so theres nothing to accept or reject
        composeTestRule
            .onNodeWithText("Reject")
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Accept")
            .assertDoesNotExist()
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 02.07.03", 2026-11-23
    */
    // US 02.07.03
    // an entrant cancelling or declining through the app should land in the owners cancelled list
    @Test
    fun us_02_07_03_entrant_cancel_and_decline_show_as_cancelled() {
        val model = Model()
        model.cancelReservation(toCancelReservation)
        model.declineReservation(toDeclineReservation)
        waitForStatus(toCancelReservation.id, ReservationStatus.CANCELLED)
        waitForStatus(toDeclineReservation.id, ReservationStatus.CANCELLED)

        openOwnerReservations("Cancelled")
        waitForText(nameOf(toCancelReservation))
        assertShown(nameOf(toCancelReservation))
        assertShown(nameOf(toDeclineReservation))

        // and theyre gone from active
        composeTestRule
            .onNodeWithText("Active")
            .performClick()
        waitForText(testEntrant.name)
        assertNotShown(nameOf(toCancelReservation))
        assertNotShown(nameOf(toDeclineReservation))
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.08.01), 2026-11-26
    */
    // US 02.08.01
    // as a campsite owner i want to reject a reservation
    @Test
    fun us_02_08_01_owner_rejects_reservation() {
        openOwnerReservations("Active")
        waitForText(nameOf(toRejectReservation))
        val rejectButton = "rejectReservation-${toRejectReservation.id}"

        // backing out of the popup leaves the reservation alone
        composeTestRule
            .onNodeWithTag(rejectButton)
            .performClick()
        composeTestRule
            .onNodeWithText("Reject reservation?")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Keep it")
            .performClick()
        composeTestRule
            .onNodeWithText("Reject reservation?")
            .assertDoesNotExist()
        assertEquals(
            ReservationStatus.ACTIVE.name,
            await(reservationRef.document(toRejectReservation.id).get()).getString("status"),
        )

        // confirming actually rejects it
        composeTestRule
            .onNodeWithTag(rejectButton)
            .performClick()
        composeTestRule
            .onNodeWithText("Yes, reject")
            .performClick()
        waitForStatus(toRejectReservation.id, ReservationStatus.REJECTED)

        // the active list reloads without it but the other active stays are still there
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(nameOf(toRejectReservation))
                .fetchSemanticsNodes().isEmpty()
        }
        assertShown(testEntrant.name)
        assertEquals(
            ReservationStatus.ACTIVE.name,
            await(reservationRef.document(activeReservation.id).get()).getString("status"),
        )
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.08.01), 2026-11-26
    */
    // US 02.08.01
    // the entrant gets told when the owner rejects their reservation
    @Test
    fun us_02_08_01_entrant_notified_of_rejection() {
        var result: Result<Unit>? = null
        val done = CountDownLatch(1)
        Model().apply { setDeviceInformation(deviceId) }.rejectReservation(toRejectReservation) {
            result = it
            done.countDown()
        }
        assertTrue("reject never answered", done.await(10, TimeUnit.SECONDS))
        assertTrue(result!!.isSuccess)

        // the notification goes to whoever booked it not the owner and names the campsite and dates
        val message = waitForNotificationTo(
            toRejectReservation.entrantId,
            "rejected",
            testCampsite.name,
            toRejectReservation.checkIn,
            toRejectReservation.checkOut,
        )
        assertTrue(message.isNotBlank())
        assertFalse(myNotifications().any { "rejected" in it })
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.08.01), 2026-11-26
    */
    // US 02.08.01
    // a stay thats already over cant be rejected even though its still saved as active
    @Test
    fun us_02_08_01_owner_cannot_reject_finished_stay() {
        var result: Result<Unit>? = null
        val done = CountDownLatch(1)
        Model().apply { setDeviceInformation(deviceId) }.rejectReservation(finishedReservation) {
            result = it
            done.countDown()
        }
        assertTrue("reject never answered", done.await(10, TimeUnit.SECONDS))

        assertTrue(result!!.isFailure)
        assertEquals(
            ReservationStatus.ACTIVE.name,
            await(reservationRef.document(finishedReservation.id).get()).getString("status"),
        )
        // and nobody gets told it was rejected
        assertTrue(myNotifications(finishedReservation.entrantId).none { "rejected" in it })
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 01.06.01", 2026-11-24
    */
    // US 01.06.01
    // as an entrant i want to view a list of my active reservations
    @Test
    fun us_01_06_01_entrant_views_active_reservations() {
        openEntrantReservations("Current")

        // both upcoming stays show up
        waitForText(campsiteOf(myUpcomingReservation))
        assertShown(campsiteOf(myToCancelReservation))
        assertShown(campsiteOf(myUpcomingReservation))

        // the card shows the real dates and guests not placeholders
        assertShown(cardDates(myUpcomingReservation))
        composeTestRule
            .onAllNodesWithText("Guests: 3")
            .onFirst()
            .assertIsDisplayed()

        // finished cancelled and rejected ones dont belong here
        assertNotShown(campsiteOf(myFinishedReservation))
        assertNotShown(campsiteOf(myMarkedPastReservation))
        assertNotShown(campsiteOf(myCancelledReservation))
        assertNotShown(campsiteOf(myRejectedReservation))
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 01.06.02", 2026-11-24
    */
    // US 01.06.02
    // as an entrant i want to view a list of my past reservations
    @Test
    fun us_01_06_02_entrant_views_past_reservations() {
        openEntrantReservations("Past")

        // finished stays show up whether theyre still saved as active or marked past
        waitForText(campsiteOf(myFinishedReservation))
        assertShown(campsiteOf(myFinishedReservation))
        assertShown(campsiteOf(myMarkedPastReservation))
        assertShown(cardDates(myFinishedReservation))

        // upcoming cancelled and rejected ones dont belong here
        assertNotShown(campsiteOf(myUpcomingReservation))
        assertNotShown(campsiteOf(myToCancelReservation))
        assertNotShown(campsiteOf(myCancelledReservation))
        assertNotShown(campsiteOf(myRejectedReservation))

        // a stay thats over cant be cancelled
        composeTestRule
            .onNodeWithText("X Cancel")
            .assertDoesNotExist()

        // and a finished stay isnt still listed as current
        composeTestRule
            .onNodeWithText("Current")
            .performClick()
        waitForText(campsiteOf(myUpcomingReservation))
        assertNotShown(campsiteOf(myFinishedReservation))
        assertNotShown(campsiteOf(myMarkedPastReservation))
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 01.07.01", 2026-11-24
    */
    // US 01.07.01
    // as an entrant i want to cancel my campsite reservation
    @Test
    fun us_01_07_01_entrant_cancels_reservation() {
        openEntrantReservations("Current")
        waitForText(campsiteOf(myToCancelReservation))

        // the reservation to cancel checks in first so its cancel button is the first one
        composeTestRule
            .onAllNodesWithText("X Cancel")
            .onFirst()
            .performClick()

        // backing out of the popup leaves the reservation alone
        composeTestRule
            .onNodeWithText("Cancel reservation?")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Keep it")
            .performClick()
        composeTestRule
            .onNodeWithText("Cancel reservation?")
            .assertDoesNotExist()
        assertEquals(
            ReservationStatus.ACTIVE.name,
            await(reservationRef.document(myToCancelReservation.id).get()).getString("status"),
        )

        // confirming actually cancels it
        composeTestRule
            .onAllNodesWithText("X Cancel")
            .onFirst()
            .performClick()
        composeTestRule
            .onNodeWithText("Yes, cancel")
            .performClick()
        waitForText("was cancelled")
        waitForStatus(myToCancelReservation.id, ReservationStatus.CANCELLED)
        // close the cancelled popup from US 01.07.02 so the tabs can be clicked
        composeTestRule
            .onNodeWithText("OK")
            .performClick()

        // the list reloads without it but the other upcoming stay is still there
        // exact text so the banner that names the campsite doesnt count
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(campsiteOf(myToCancelReservation))
                .fetchSemanticsNodes().isEmpty()
        }
        assertShown(campsiteOf(myUpcomingReservation))

        // and it doesnt turn up as past either
        composeTestRule
            .onNodeWithText("Past")
            .performClick()
        waitForText(campsiteOf(myFinishedReservation))
        assertNotShown(campsiteOf(myToCancelReservation))
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 01.07.01", 2026-11-24
    */
    // US 01.07.01
    // a stay thats already over cant be cancelled even though its still saved as active
    @Test
    fun us_01_07_01_entrant_cannot_cancel_finished_stay() {
        var result: Result<Unit>? = null
        val done = CountDownLatch(1)
        Model().cancelReservation(myFinishedReservation) {
            result = it
            done.countDown()
        }
        assertTrue("cancel never answered", done.await(10, TimeUnit.SECONDS))

        assertTrue(result!!.isFailure)
        assertEquals(
            ReservationStatus.ACTIVE.name,
            await(reservationRef.document(myFinishedReservation.id).get()).getString("status"),
        )
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "make the userstorytests" (US 01.05.01), 2026-11-26
    */
    // US 01.05.01
    // as an entrant i want to receive confirmation of my campsite reservation
    @Test
    fun us_01_05_01_entrant_gets_reservation_confirmation() {
        val checkIn = LocalDate.now().plusDays(40)
        val checkOut = checkIn.plusDays(2)
        val result = reserve(reserveCampsite, checkIn, checkOut, "2")

        // the reservation is saved as active for this device
        assertTrue("reserve failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val saved = await(reservationRef.document(result.getOrThrow().reservation.id).get())
        assertEquals(ReservationStatus.ACTIVE.name, saved.getString("status"))
        assertEquals(deviceId, saved.getString("entrantId"))
        assertEquals(reserveCampsite.id, saved.getString("campsiteId"))
        assertEquals(checkIn.toString(), saved.getString("checkIn"))
        assertEquals(checkOut.toString(), saved.getString("checkOut"))

        // a confirmation is sent naming the campsite and dates
        waitForNotification(reserveCampsite.name, checkIn.toString(), "is confirmed")

        // and the entrant sees it on their notifications screen
        openEntrantNotifications()
        waitForText("${reserveCampsite.name} from $checkIn to $checkOut is confirmed")
        composeTestRule
            .onAllNodesWithText("is confirmed", substring = true)
            .onFirst()
            .assertIsDisplayed()
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "make the userstorytests" (US 01.05.01), 2026-11-26
    */
    // US 01.05.01
    // dates someone already booked arent confirmed and nothing is sent
    @Test
    fun us_01_05_01_entrant_not_confirmed_for_booked_dates() {
        val campsite = entrantCampsites.first { it.id == myUpcomingReservation.campsiteId }
        val bookedCheckIn = LocalDate.parse(myUpcomingReservation.checkIn)
        val before = myDocIds(reservationRef)

        // starts on the second night of the existing stay so they overlap
        val result = reserve(campsite, bookedCheckIn.plusDays(1), bookedCheckIn.plusDays(3), "1")

        assertTrue(result.isFailure)
        assertEquals("Those dates are already booked", result.exceptionOrNull()?.message)
        assertEquals(before, myDocIds(reservationRef))
        assertFalse(myNotifications().any { campsite.name in it && "is confirmed" in it })
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "make the userstorytests" (US 01.05.02), 2026-11-26
    */
    // US 01.05.02
    // as an entrant i want to receive my reservation by notification or downloadable csv
    @Test
    fun us_01_05_02_entrant_downloads_reservation_csv() {
        openEntrantReservations("Current")
        waitForText(campsiteOf(myToCancelReservation))

        CsvPickerStub().use { picker ->
            // the reservation checking in first is the first card so its csv link is the first one
            composeTestRule
                .onAllNodesWithTag("reservationCsv")
                .onFirst()
                .performClick()
            waitForText("CSV saved")

            assertEquals("reservation-${myToCancelReservation.id}.csv", picker.suggestedName)
            val lines = picker.lines()
            assertEquals(2, lines.size)
            assertEquals(Reservation.CSV_HEADER, lines[0])
            assertEquals(
                myToCancelReservation.toCsvRow(campsiteOf(myToCancelReservation)),
                lines[1],
            )
            assertTrue(lines[1].endsWith(",ACTIVE"))
        }
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "make the userstorytests" (US 01.05.02), 2026-11-26
    */
    // US 01.05.02
    // an entrant who turned notifications off still gets their reservation as a csv
    @Test
    fun us_01_05_02_opted_out_entrant_gets_csv_not_notification() {
        val checkIn = LocalDate.now().plusDays(50)
        val result = reserve(reserveCampsite, checkIn, checkIn.plusDays(2), "3")
        assertTrue("reserve failed: ${result.exceptionOrNull()?.message}", result.isSuccess)

        // give the model time to have sent one if it was going to
        Thread.sleep(3_000)
        assertFalse(myNotifications().any { reserveCampsite.name in it })

        // the csv for that reservation still has everything in it
        val lines = result.getOrThrow().toCsv().trimEnd().lines()
        assertEquals(Reservation.CSV_HEADER, lines[0])
        assertTrue(lines[1].startsWith("${result.getOrThrow().reservation.id},${reserveCampsite.name},"))
        assertTrue(lines[1].contains(",$checkIn,"))
        assertTrue(lines[1].endsWith(",3,ACTIVE"))
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "make the userstorytests" (US 01.07.02), 2026-11-26
    */
    // US 01.07.02
    // as an entrant i want to receive confirmation of cancellation by notification or downloadable csv
    @Test
    fun us_01_07_02_entrant_gets_cancellation_notification() {
        cancelFirstCurrentReservation()

        // the app confirms it straight away
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("cancelConfirmation")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Reservation cancelled")
            .assertIsDisplayed()
        waitForStatus(myToCancelReservation.id, ReservationStatus.CANCELLED)

        // and sends a notification naming the campsite and dates
        val campsite = campsiteOf(myToCancelReservation)
        waitForNotification(campsite, myToCancelReservation.checkIn, "was cancelled")

        // which shows on the notifications screen
        composeTestRule
            .onNodeWithText("OK")
            .performClick()
        composeTestRule
            .onNodeWithTag("Home")
            .performClick()
        openEntrantNotifications()
        waitForText("$campsite from ${myToCancelReservation.checkIn}")
        composeTestRule
            .onAllNodesWithText("was cancelled", substring = true)
            .onFirst()
            .assertIsDisplayed()
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "make the userstorytests" (US 01.07.02), 2026-11-26
    */
    // US 01.07.02
    // an entrant who turned notifications off can download the cancellation as a csv instead
    @Test
    fun us_01_07_02_opted_out_entrant_downloads_cancellation_csv() {
        cancelFirstCurrentReservation()
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("downloadCancellationCsv")
                .fetchSemanticsNodes().isNotEmpty()
        }
        waitForStatus(myToCancelReservation.id, ReservationStatus.CANCELLED)

        CsvPickerStub().use { picker ->
            composeTestRule
                .onNodeWithTag("downloadCancellationCsv")
                .performClick()
            waitForText("CSV saved")

            assertEquals("cancellation-${myToCancelReservation.id}.csv", picker.suggestedName)
            val lines = picker.lines()
            assertEquals(Reservation.CSV_HEADER, lines[0])
            assertTrue(lines[1].startsWith("${myToCancelReservation.id},${campsiteOf(myToCancelReservation)},"))
            assertTrue(lines[1].endsWith(",CANCELLED"))
        }

        // they turned notifications off so none was sent
        Thread.sleep(3_000)
        assertFalse(myNotifications().any { "was cancelled" in it })
    }
}
