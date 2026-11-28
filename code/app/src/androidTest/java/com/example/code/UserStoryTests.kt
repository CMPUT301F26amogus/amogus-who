package com.example.code

import Model
import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
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
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import androidx.compose.material3.Text
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.code.model.AvailabilitySchedule
import com.example.code.model.EntryStatus
import com.example.code.model.Lottery
import com.example.code.model.WaitlistEntry
import com.example.code.views.LotteriesView
import com.example.code.views.ReserveCampsite
import org.junit.After
import org.junit.Before

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
    // the owner rejected this one so it shows in the cancelled tab labelled rejected (US 02.08.01)
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
            .onAllNodesWithText("Cancel")
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

    // the dates the way the cards show them like Jun 12, 2027 then Jun 15, 2027
    private fun cardDates(reservation: Reservation): List<String> {
        val format = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
        return listOf(LocalDate.parse(reservation.checkIn).format(format), LocalDate.parse(reservation.checkOut).format(format))
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 02.07.01", 2026-11-23
    see [1] in Citation.md
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

    /*
    * ==============================================================================================
    * ==============================================================================================
    *
    * USER STORY UNIT TESTS START HERE
    *
    * Each dev should implement or generate their own tests for each user story.
    * Organize them in numerical order so that they're easy to find.
    * Use the user_stories.md file found at the root of this project as reference.
    * Consider edge cases where applicable.
    *
    * ==============================================================================================
    * ==============================================================================================
    */

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 02.07.02", 2026-11-23
    see [2] in Citation.md
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
    see [3] in Citation.md
    */
    // US 02.07.03
    // as a campsite owner i want to view a list of cancelled reservations
    @Test
    fun us_02_07_03_owner_views_cancelled_reservations() {
        openOwnerReservations("Cancelled")

        waitForText(nameOf(cancelledReservation))
        assertShown(nameOf(cancelledReservation))
        composeTestRule
            .onNodeWithTag("reservationLabel-${cancelledReservation.id}")
            .assertTextEquals("Cancelled by entrant")

        // rejected ones are here too but labelled so the owner can tell them apart (US 02.08.01)
        assertShown(nameOf(rejectedReservation))
        composeTestRule
            .onNodeWithTag("reservationLabel-${rejectedReservation.id}")
            .assertTextEquals("Rejected by you")

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
    see [3] in Citation.md
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
    see [12] in Citation.md
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

        // and it moves to the cancelled tab labelled rejected instead of disappearing
        composeTestRule
            .onNodeWithText("Cancelled")
            .performClick()
        waitForText(nameOf(toRejectReservation))
        composeTestRule
            .onNodeWithTag("reservationLabel-${toRejectReservation.id}")
            .assertTextEquals("Rejected by you")
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.08.01), 2026-11-26
    see [12] in Citation.md
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
    see [12] in Citation.md
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
    see [4] in Citation.md
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
        cardDates(myUpcomingReservation).forEach { assertShown(it) }
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
    see [5] in Citation.md
    */
    // US 01.06.02
    // as an entrant i want to view a list of my past reservations
    @Test
    fun us_01_06_02_entrant_views_past_reservations() {
        openEntrantReservations("Previous")

        // finished stays show up whether theyre still saved as active or marked past
        waitForText(campsiteOf(myFinishedReservation))
        assertShown(campsiteOf(myFinishedReservation))
        assertShown(campsiteOf(myMarkedPastReservation))
        cardDates(myFinishedReservation).forEach { assertShown(it) }

        // upcoming cancelled and rejected ones dont belong here
        assertNotShown(campsiteOf(myUpcomingReservation))
        assertNotShown(campsiteOf(myToCancelReservation))
        assertNotShown(campsiteOf(myCancelledReservation))
        assertNotShown(campsiteOf(myRejectedReservation))

        // a stay thats over cant be cancelled
        composeTestRule
            .onNodeWithText("Cancel")
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
    see [6] in Citation.md
    */
    // US 01.07.01
    // as an entrant i want to cancel my campsite reservation
    @Test
    fun us_01_07_01_entrant_cancels_reservation() {
        openEntrantReservations("Current")
        waitForText(campsiteOf(myToCancelReservation))

        // the reservation to cancel checks in first so its cancel button is the first one
        composeTestRule
            .onAllNodesWithText("Cancel")
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
            .onAllNodesWithText("Cancel")
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

        // and it doesnt turn up as previous either
        composeTestRule
            .onNodeWithText("Previous")
            .performClick()
        waitForText(campsiteOf(myFinishedReservation))
        assertNotShown(campsiteOf(myToCancelReservation))
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 01.07.01", 2026-11-24
    see [6] in Citation.md
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
    see [8] in Citation.md
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
    see [8] in Citation.md
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
    see [8] in Citation.md
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
    see [8] in Citation.md
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
    see [8] in Citation.md
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
    see [8] in Citation.md
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

    /* The following function is from Anthropic, Claude Opus 5.5, "ok lets do # 17" (US 02.10.02), 2026-11-27
    */
    // US 02.10.02
    // as a campsite owner i want to view a list of all notifications i have sent
    @Test
    fun us_02_10_02_owner_views_sent_notifications() {
        // rejecting tells the entrant, so the owner has sent them a notification
        openOwnerReservations("Active")
        waitForText(nameOf(toRejectReservation))
        composeTestRule
            .onNodeWithTag("rejectReservation-${toRejectReservation.id}")
            .performClick()
        composeTestRule
            .onNodeWithText("Yes, reject")
            .performClick()
        waitForNotificationTo(toRejectReservation.entrantId, "rejected", testCampsite.name)

        // it shows up on the sent notifications screen from the owner home
        composeTestRule
            .onNodeWithText("Home")
            .performClick()
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("Sent Notifications")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("Sent Notifications")
            .performClick()
        composeTestRule
            .onNodeWithTag("Header: Sent Notifications")
            .assertExists()
        waitForText("was rejected by the campsite owner")
        composeTestRule
            .onAllNodesWithText(testCampsite.name, substring = true)
            .onFirst()
            .assertIsDisplayed()
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "ok lets do # 17" (US 02.10.02), 2026-11-27
    */
    // US 02.10.02
    // only notifications the owner sent are listed, not ones the app sent on its own
    @Test
    fun us_02_10_02_sent_list_only_has_owners_notifications() {
        // the app confirms a reservation by itself so that one has no sender
        assertTrue(reserve(reserveCampsite, LocalDate.now().plusDays(40), LocalDate.now().plusDays(42), "2").isSuccess)
        waitForNotification("is confirmed", reserveCampsite.name)

        // the owner rejecting a stay is sent by the owner
        val model = Model().apply { setDeviceInformation(deviceId) }
        val rejected = CountDownLatch(1)
        model.rejectReservation(toRejectReservation) { rejected.countDown() }
        assertTrue("reject never answered", rejected.await(10, TimeUnit.SECONDS))
        waitForNotificationTo(toRejectReservation.entrantId, "rejected", testCampsite.name)

        var sent: List<com.example.code.model.Notification> = emptyList()
        val loaded = CountDownLatch(1)
        model.getSentNotifications {
            sent = it
            loaded.countDown()
        }
        assertTrue("sent notifications never loaded", loaded.await(10, TimeUnit.SECONDS))

        assertTrue(sent.any { "rejected" in it.content && testCampsite.name in it.content })
        assertTrue(sent.all { it.ownerId == deviceId })
        assertFalse(sent.any { "is confirmed" in it.content })
    }
}

// see [19] in Citation.md
/**
 * user story tests for a lottery winner reserving or declining (US 01.04.01 01.04.02 01.04.03)
 *
 * seeds a lottery with two campsites that this device already won so the draw doesnt have to run
 * shows the lotteries screen and the reserve screen it opens like the app does
 * puts the devices real profile back and deletes what the test made when its done
 */
@RunWith(AndroidJUnit4::class)
class LotteryWinUserStoryTests {
    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
        Settings.Secure.ANDROID_ID,
    )

    private val usersRef by lazy { Firebase.firestore.collection("users") }
    private val campsiteRef by lazy { Firebase.firestore.collection("campsites") }
    private val lotteryRef by lazy { Firebase.firestore.collection("lotteries") }
    private val reservationRef by lazy { Firebase.firestore.collection("reservations") }
    private val notificationRef by lazy { Firebase.firestore.collection("notifications") }
    private val userDoc by lazy { usersRef.document(deviceId) }

    private val today = LocalDate.now()
    // both campsites are open for 10 nights a month from now
    private val openStart = today.plusDays(30)
    private val openEnd = today.plusDays(40)
    // someone else already has 2 nights in the middle of the first campsite
    private val bookedStart = today.plusDays(33)
    private val bookedEnd = today.plusDays(35)

    private val lotteryId = "us-01-04-lottery-$deviceId"
    private val ownerId = "us-01-04-owner-$deviceId"

    private fun testCampsite(id: String, name: String) = Campsite(
        id = id,
        ownerId = ownerId,
        name = name,
        location = "Test Park",
        lotteryId = lotteryId,
        maxOccupancy = 4,
        availability = AvailabilitySchedule(listOf(openStart.toString(), openEnd.toString())),
    )

    private val firstCampsite = testCampsite("us-01-04-site-a-$deviceId", "US 01.04 Lakeside")
    private val secondCampsite = testCampsite("us-01-04-site-b-$deviceId", "US 01.04 Hilltop")

    // the other entrants stay that takes nights out of the first campsite
    private val otherStay = Reservation(
        id = "us-01-04-other-stay-$deviceId",
        campsiteId = firstCampsite.id,
        entrantId = "us-01-04-someone-else-$deviceId",
        checkIn = bookedStart.toString(),
        checkOut = bookedEnd.toString(),
        numGuests = 2,
        lotteryId = lotteryId,
    )

    // registration is over and booking is open so this device can reserve right now
    private val lottery = Lottery(
        name = "US 01.04 Lottery",
        id = lotteryId,
        ownerId = listOf(ownerId),
        campsiteIds = listOf(firstCampsite.id, secondCampsite.id),
        registrationStart = today.minusDays(10).toString(),
        registrationEnd = today.minusDays(1).toString(),
        bookingStart = today.minusDays(1).toString(),
        bookingEnd = today.plusDays(10).toString(),
        numWinners = 1,
        waitlist = listOf(WaitlistEntry(id = deviceId, lotteryId = lotteryId, status = EntryStatus.SELECTED)),
    )

    private lateinit var model: Model
    private var savedUser: Map<String, Any>? = null
    private var reservationsBefore = emptySet<String>()
    private var notificationsBefore = emptySet<String>()

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun <T> await(task: Task<T>): T = Tasks.await(task, 10, TimeUnit.SECONDS)

    private fun myDocIds(ref: CollectionReference): Set<String> =
        await(ref.whereEqualTo("entrantId", deviceId).get()).documents.map { it.id }.toSet()

    // the reservations this test made
    private fun newReservations(): List<Reservation> =
        await(reservationRef.whereEqualTo("entrantId", deviceId).get()).documents
            .filter { it.id !in reservationsBefore }
            .mapNotNull { it.toObject(Reservation::class.java) }

    // where this device is in the saved lottery
    private fun myEntryStatus(): EntryStatus? =
        await(lotteryRef.document(lotteryId).get()).toObject(Lottery::class.java)?.entryFor(deviceId)?.status

    private fun waitForEntryStatus(status: EntryStatus) {
        val giveUpAt = System.currentTimeMillis() + 10_000
        while (myEntryStatus() != status) {
            check(System.currentTimeMillis() < giveUpAt) { "Lottery entry never became $status" }
            Thread.sleep(250)
        }
    }

    @Before
    fun seed() {
        savedUser = await(userDoc.get()).data
        reservationsBefore = myDocIds(reservationRef)
        notificationsBefore = myDocIds(notificationRef)
        await(userDoc.set(User(deviceId = deviceId, name = "US Test ENTRANT", role = Role.ENTRANT)))
        await(campsiteRef.document(firstCampsite.id).set(firstCampsite))
        await(campsiteRef.document(secondCampsite.id).set(secondCampsite))
        await(reservationRef.document(otherStay.id).set(otherStay))
        await(lotteryRef.document(lotteryId).set(lottery))
        // joinWaitlist keeps this list so the lotteries screen can find the ones the user is in
        await(lotteryRef.document(lotteryId).update("entrantIds", listOf(deviceId)))

        model = Model().apply { setDeviceInformation(deviceId) }
        val lotteriesView = LotteriesView(model)
        val reserveView = ReserveCampsite(model)
        // registered like in the app so picking a campsite redraws the reserve screen
        model.addView(lotteriesView)
        model.addView(reserveView)
        lotteriesView.update(model)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Routes.LOTTERIES) {
                composable(Routes.LOTTERIES) { lotteriesView.ShowScreen(navController) }
                composable(Routes.RESERVE_CAMPSITE) { reserveView.ShowScreen(navController) }
                // stand ins so the tests can see where the buttons go
                composable(Routes.RESERVATIONS) { Text("Reservations screen") }
                composable(Routes.CAMPSITE_DETAILS) { Text("Details screen") }
            }
        }
    }

    @After
    fun cleanUp() {
        listOf(reservationRef to reservationsBefore, notificationRef to notificationsBefore).forEach { (ref, before) ->
            await(ref.whereEqualTo("entrantId", deviceId).get()).documents
                .filter { it.id !in before }
                .forEach { await(it.reference.delete()) }
        }
        await(reservationRef.document(otherStay.id).delete())
        await(lotteryRef.document(lotteryId).delete())
        await(campsiteRef.document(firstCampsite.id).delete())
        await(campsiteRef.document(secondCampsite.id).delete())
        savedUser?.let { await(userDoc.set(it)) } ?: await(userDoc.delete())
    }

    private fun waitForTag(tag: String) {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // taps reserve on one campsites row of the won lottery
    private fun openReserveFor(campsite: Campsite) {
        val tag = "reserveLotteryButton-${campsite.id}"
        waitForTag(tag)
        composeTestRule.onNodeWithTag(tag).performClick()
        composeTestRule.onNodeWithTag("campsiteNameBox").assertIsDisplayed()
    }

    // the calendar names each day like Sunday, December 27, 2026
    private val dayLabel = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)

    // a day on the availability calendar
    // material3 puts the date in the days text in newer versions and its content description in older ones
    // checking the text as well as the content description is from [23] in Citation.md
    private fun day(date: LocalDate): SemanticsMatcher {
        val label = date.format(dayLabel)
        return (hasText(label, substring = true) or hasContentDescription(label, substring = true)) and
            hasClickAction() and hasAnyAncestor(hasTestTag("availabilityCalendar"))
    }

    // scrolls the calendar to a days month so it can be checked or tapped
    // see [21] in Citation.md
    private fun showDay(date: LocalDate) = composeTestRule.run {
        onNodeWithTag("availabilityCalendar").performScrollTo()
        onNode(hasScrollAction() and hasAnyAncestor(hasTestTag("availabilityCalendar")))
            .performScrollToNode(day(date))
        onNode(day(date))
    }

    // picks the stay on the calendar like an entrant would then presses the button
    private fun fillAndReserve(arrival: LocalDate, departure: LocalDate, guests: String) {
        showDay(arrival).performClick()
        showDay(departure).performClick()
        composeTestRule.onNodeWithTag("numberOfGuestsInputField").performScrollTo().performTextInput(guests)
        composeTestRule.onNodeWithTag("reserveCampsiteButton").performScrollTo().performClick()
    }

    // how the reserve screen shows dates like 05/01/27
    private fun shown(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MM/dd/yy"))

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.01), 2026-11-27
    see [19] in Citation.md
    */
    // US 01.04.01
    // as an entrant if i win i want to see the available dates i can reserve the campsite for
    @Test
    fun us_01_04_01_winner_sees_available_dates() {
        waitForText("You won!")
        openReserveFor(firstCampsite)

        // the calendar shows up with the free days pickable
        waitForTag("availabilityCalendar")
        composeTestRule.onNodeWithTag("availabilityCalendar").assertIsDisplayed()
        showDay(openStart).assertIsEnabled()
        // checking out the day the other stay checks in and checking in the day it leaves are both fine
        showDay(bookedStart).assertIsEnabled()
        showDay(bookedEnd).assertIsEnabled()
        showDay(openEnd).assertIsEnabled()

        // the other entrants night the day before the window opens and the day after it closes are greyed out
        showDay(bookedStart.plusDays(1)).assertIsNotEnabled()
        showDay(openStart.minusDays(1)).assertIsNotEnabled()
        showDay(openEnd.plusDays(1)).assertIsNotEnabled()

        // and the free stretches are written under it
        composeTestRule.onNodeWithText("${shown(openStart)} to ${shown(bookedStart)}").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("${shown(bookedEnd)} to ${shown(openEnd)}").performScrollTo().assertIsDisplayed()
        assertEquals(2, composeTestRule.onAllNodesWithTag("availableDate").fetchSemanticsNodes().size)
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i thought we were supposed to do something with the calender for this" (US 01.04.01), 2026-11-27
    see [21] in Citation.md
    */
    // US 01.04.01
    // the winner has to pick both days on the calendar before reserving
    @Test
    fun us_01_04_01_must_pick_dates_on_calendar() {
        openReserveFor(firstCampsite)
        waitForTag("availabilityCalendar")

        composeTestRule.onNodeWithTag("numberOfGuestsInputField").performScrollTo().performTextInput("2")
        composeTestRule.onNodeWithTag("reserveCampsiteButton").performScrollTo().performClick()
        waitForText("Pick your check in day on the calendar")

        showDay(openStart).performClick()
        composeTestRule.onNodeWithTag("reserveCampsiteButton").performScrollTo().performClick()
        waitForText("Pick your check out day on the calendar")
        assertTrue(newReservations().isEmpty())
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.01), 2026-11-27
    see [19] in Citation.md
    */
    // US 01.04.01
    // dates that arent available cant be reserved and the win isnt used up
    @Test
    fun us_01_04_01_cannot_reserve_unavailable_dates() {
        openReserveFor(firstCampsite)
        waitForTag("availabilityCalendar")

        // both days can be tapped but the stay runs over the other entrants nights
        fillAndReserve(bookedStart.minusDays(1), bookedEnd.plusDays(1), "2")

        waitForText("Those dates aren't available")
        composeTestRule.onNodeWithText("Reservation confirmed").assertDoesNotExist()
        assertTrue(newReservations().isEmpty())
        assertEquals(EntryStatus.SELECTED, myEntryStatus())
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.02), 2026-11-27
    see [19] in Citation.md
    */
    // US 01.04.02
    // as an entrant if i win a multiple campsite lottery i want to choose which campsite and which dates to reserve
    @Test
    fun us_01_04_02_winner_picks_campsite_and_dates() {
        // every campsite in the lottery gets a row to pick from
        waitForTag("reserveLotteryButton-${firstCampsite.id}")
        waitForTag("reserveLotteryButton-${secondCampsite.id}")
        composeTestRule.onNodeWithText(firstCampsite.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(secondCampsite.name).assertIsDisplayed()

        // picks the second campsite and 3 nights of its dates
        openReserveFor(secondCampsite)
        composeTestRule.onNodeWithText(secondCampsite.name).assertIsDisplayed()
        waitForTag("availabilityCalendar")
        val checkIn = openStart.plusDays(2)
        val checkOut = checkIn.plusDays(3)
        fillAndReserve(checkIn, checkOut, "3")

        waitForTag("reservationConfirmation")
        val saved = newReservations().single()
        assertEquals(secondCampsite.id, saved.campsiteId)
        assertEquals(lotteryId, saved.lotteryId)
        assertEquals(checkIn.toString(), saved.checkIn)
        assertEquals(checkOut.toString(), saved.checkOut)
        assertEquals(3, saved.numGuests)
        assertEquals(ReservationStatus.ACTIVE, saved.status)

        // the win is used up so they cant reserve a second campsite from it
        assertEquals(EntryStatus.RESERVED, myEntryStatus())
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.02), 2026-11-27
    see [19] in Citation.md
    */
    // US 01.04.02
    // a winner can only reserve one stay from a lottery
    @Test
    fun us_01_04_02_cannot_reserve_twice_from_one_win() {
        openReserveFor(firstCampsite)
        waitForTag("availabilityCalendar")
        fillAndReserve(openStart, openStart.plusDays(1), "2")
        waitForTag("reservationConfirmation")
        waitForEntryStatus(EntryStatus.RESERVED)

        // asking the model directly like a second tap would
        var error: String? = null
        model.reserveLotteryWin(
            lottery = lottery,
            campsite = secondCampsite,
            checkIn = openStart.toString(),
            checkOut = openStart.plusDays(1).toString(),
            guests = 2,
        ) { result -> error = result.exceptionOrNull()?.message }
        composeTestRule.waitUntil(10_000) { error != null }
        assertEquals("You already reserved a campsite from this lottery", error)
        assertEquals(1, newReservations().size)
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.03), 2026-11-27
    see [19] in Citation.md
    */
    // US 01.04.03
    // as an entrant if i win i want to decline if i changed my mind
    @Test
    fun us_01_04_03_winner_declines_from_lotteries_screen() {
        waitForTag("declineLotteryButton")
        // theres a row per campsite and declining any of them declines the whole win
        composeTestRule.onAllNodesWithTag("declineLotteryButton")[0].performClick()

        // it asks first and keeping it does nothing
        composeTestRule.onNodeWithTag("declineLotteryDialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep it").performClick()
        composeTestRule.onNodeWithTag("declineLotteryDialog").assertDoesNotExist()
        assertEquals(EntryStatus.SELECTED, myEntryStatus())

        composeTestRule.onAllNodesWithTag("declineLotteryButton")[0].performClick()
        composeTestRule.onNodeWithTag("confirmDeclineLottery").performClick()
        waitForEntryStatus(EntryStatus.DECLINED)
        assertTrue(newReservations().isEmpty())

        // the won lottery leaves the current list
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(firstCampsite.name).fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText(secondCampsite.name).assertDoesNotExist()
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.03), 2026-11-27
    see [19] in Citation.md
    */
    // US 01.04.03
    // as an entrant if i win i want to decline when none of the remaining dates work
    @Test
    fun us_01_04_03_winner_declines_from_reserve_screen() {
        openReserveFor(firstCampsite)
        waitForTag("availabilityCalendar")

        composeTestRule.onNodeWithTag("declineLotteryButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("confirmDeclineLottery").performClick()
        waitForEntryStatus(EntryStatus.DECLINED)
        assertTrue(newReservations().isEmpty())

        // back to the lotteries screen and a declined win cant be reserved anymore
        waitForText("Lotteries")
        var error: String? = null
        model.reserveLotteryWin(
            lottery = lottery,
            campsite = firstCampsite,
            checkIn = openStart.toString(),
            checkOut = openStart.plusDays(1).toString(),
            guests = 2,
        ) { result -> error = result.exceptionOrNull()?.message }
        composeTestRule.waitUntil(10_000) { error != null }
        assertEquals("You already declined this lottery", error)
    }
}
