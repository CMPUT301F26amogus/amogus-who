package com.example.code

import Model
import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.code.model.Campsite
import com.example.code.model.EntryStatus
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.ReservationStatus
import com.example.code.model.Role
import com.example.code.model.User
import com.example.code.model.WaitlistEntry
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    private val lotteryRef by lazy { Firebase.firestore.collection("lotteries") }
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

    private val seededReservations = listOf(
        activeReservation,
        finishedReservation,
        markedPastReservation,
        cancelledReservation,
        rejectedReservation,
        toCancelReservation,
        toDeclineReservation,
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

    // ---- model fixtures for the lottery and notification tests ----
    // these tests call the model directly and read the result back from firestore
    // so nothing here is shown on screen and the lotteries belong to a made up owner

    // everyone who gets a notification in the tests, deleted with their notifications after
    // 4 are on the draw lottery's waitlist, the bystander isnt and the muted one turned notifications off
    private fun lotteryUser(key: String, notificationsEnabled: Boolean = true) = User(
        deviceId = "us-01-03-$key-$deviceId",
        name = "US 01.03 Entrant $key",
        notificationsEnabled = notificationsEnabled,
    )

    private val drawEntrants = (1..4).map { lotteryUser("entrant$it") }
    private val bystander = lotteryUser("bystander")
    private val mutedEntrant = lotteryUser("muted", notificationsEnabled = false)
    private val lotteryUsers = drawEntrants + bystander + mutedEntrant

    // a second device that joins the same waitlist, it has no profile because joining doesnt need one
    private val secondDeviceId = "us-01-02-second-device-$deviceId"

    private fun day(daysFromNow: Long): String = LocalDate.now().plusDays(daysFromNow).toString()

    // a lottery that is still taking entrants, an entrants id is also the id of their waitlist entry
    private fun openLottery(key: String, capacity: Int, waitlistIds: List<String>): Lottery {
        val id = "us-01-02-$key-lottery-$deviceId"
        return Lottery(
            id = id,
            ownerId = "us-01-owner-$deviceId",
            registrationStart = day(-1),
            registrationEnd = day(5),
            bookingStart = day(5),
            bookingEnd = day(15),
            numWinners = 1,
            waitlistCapacity = capacity,
            waitlist = waitlistIds.map { WaitlistEntry(id = it, lotteryId = id, status = EntryStatus.WAITING) },
        )
    }

    private val joinLottery = openLottery("join", capacity = 0, waitlistIds = emptyList())
    private val fullLottery = openLottery("full", capacity = 1, waitlistIds = listOf("us-01-02-other-entrant-$deviceId"))

    // registration ended yesterday so it can be drawn, 2 of the 4 entrants win
    private val drawLottery: Lottery = run {
        val id = "us-01-03-draw-lottery-$deviceId"
        Lottery(
            id = id,
            ownerId = "us-01-owner-$deviceId",
            registrationStart = day(-10),
            registrationEnd = day(-1),
            bookingStart = day(-1),
            bookingEnd = day(10),
            numWinners = 2,
            waitlist = drawEntrants.map {
                WaitlistEntry(id = it.deviceId, lotteryId = id, status = EntryStatus.WAITING)
            },
        )
    }

    private val lotteryFixtures = listOf(joinLottery, fullLottery, drawLottery)

    // runs before the compose rule because the app picks owner or entrant home as soon as it opens
    // every test gets all the data since owner and entrant data cant show up in each others lists
    @get:Rule(order = 0)
    val seedData = TestRule { base, description ->
        object : Statement() {
            override fun evaluate() {
                // entrant stories start with us_01 and need the entrant home so everything else is an owner
                val role = if (description.methodName.startsWith("us_01")) Role.ENTRANT else Role.OWNER
                // keep whatever profile this device had so we can put it back after
                val savedUser = await(userDoc.get()).data
                try {
                    await(userDoc.set(User(deviceId = deviceId, name = "US Test ${role.name}", role = role)))
                    await(campsiteRef.document(testCampsite.id).set(testCampsite))
                    entrantCampsites.forEach { await(campsiteRef.document(it.id).set(it)) }
                    seededEntrants.forEach { await(usersRef.document(it.deviceId).set(it)) }
                    (seededReservations + entrantReservations).forEach {
                        await(reservationRef.document(it.id).set(it))
                    }
                    lotteryUsers.forEach { await(usersRef.document(it.deviceId).set(it)) }
                    lotteryFixtures.forEach { await(lotteryRef.document(it.id).set(it)) }
                    base.evaluate()
                } finally {
                    lotteryFixtures.forEach { await(lotteryRef.document(it.id).delete()) }
                    // the model saves notifications as they are sent so find them by who got them
                    lotteryUsers.forEach { user ->
                        await(notificationRef.whereEqualTo("entrantId", user.deviceId).get())
                            .documents.forEach { await(it.reference.delete()) }
                        await(usersRef.document(user.deviceId).delete())
                    }
                    (seededReservations + entrantReservations).forEach {
                        await(reservationRef.document(it.id).delete())
                    }
                    seededEntrants.forEach { await(usersRef.document(it.deviceId).delete()) }
                    entrantCampsites.forEach { await(campsiteRef.document(it.id).delete()) }
                    await(campsiteRef.document(testCampsite.id).delete())
                    if (savedUser != null) await(userDoc.set(savedUser)) else await(userDoc.delete())
                }
            }
        }
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity

    // blocks until firestore answers so the data is there before the app opens
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 10, TimeUnit.SECONDS)

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

    // ---- helpers for the model tests ----

    // a model signed in as a device, the app has its own model so the tests make their own
    private fun modelFor(id: String = deviceId): Model = Model().also { it.setDeviceInformation(id) }

    // model functions answer through a callback so this waits for it and hands back what it said
    // usage: awaitResult<Boolean> { answer -> model.joinWaitlist(lottery, answer) }
    private fun <T> awaitResult(call: ((T) -> Unit) -> Unit): T {
        var result: T? = null
        val answered = CountDownLatch(1)
        call {
            result = it
            answered.countDown()
        }
        assertTrue("the model never answered", answered.await(10, TimeUnit.SECONDS))
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    // some model functions save in the background and dont tell us when theyre done
    // so keep checking until what we expect shows up in firestore
    private fun eventually(what: String, check: () -> Boolean) {
        val giveUpAt = System.currentTimeMillis() + 10_000
        while (!check()) {
            check(System.currentTimeMillis() < giveUpAt) { "Gave up waiting for $what" }
            Thread.sleep(250)
        }
    }

    // who is on a lottery's waitlist right now as entrant id to entry status
    // read straight from the lottery document so it checks what was saved and not what the model says
    @Suppress("UNCHECKED_CAST")
    private fun waitlistOf(lotteryId: String): Map<String, String> {
        val entries = await(lotteryRef.document(lotteryId).get()).get("waitlist") as? List<Map<String, Any?>>
        return (entries ?: emptyList()).associate { (it["id"] as String) to (it["status"] as String) }
    }

    // the messages of every notification this user has
    private fun notificationsFor(userId: String): List<String> =
        await(notificationRef.whereEqualTo("entrantId", userId).get()).documents
            .mapNotNull { it.getString("content") }

    private fun waitForNotification(userId: String): List<String> {
        eventually("a notification for $userId") { notificationsFor(userId).isNotEmpty() }
        return notificationsFor(userId)
    }

    // draws the draw lottery like the owner would and gives back the winners ids
    private fun drawTheLottery(): List<String> =
        awaitResult<Result<List<String>>> { answer ->
            modelFor().drawLotteryWinner(drawLottery.id, answer)
        }.getOrThrow()

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

        // active ones are already booked so theres nothing to accept or reject
        composeTestRule
            .onNodeWithText("Reject")
            .assertDoesNotExist()
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

    // ---- model tests, these only call the model and read the result back from firestore ----

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #138", 2026-11-26
    */
    // issue 138
    // the model needs to be able to get the device id from the users device
    @Test
    fun issue_138_model_keeps_the_device_id_it_is_given() {
        // the id MainActivity reads from the device has to be a real one
        assertTrue("this device has no id", deviceId.isNotBlank())

        val model = Model()
        model.setDeviceInformation(deviceId)

        assertEquals(deviceId, model.currentDeviceId)
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #138", 2026-11-26
    */
    // issue 138
    // with the device id set the model finds that devices user
    @Test
    fun issue_138_model_finds_the_user_for_the_device_id() {
        val user = awaitResult<User?> { answer -> modelFor().getUserObj { answer(it) } }

        assertNotNull(user)
        assertEquals(deviceId, user!!.deviceId)
        assertTrue(user.hasRole(Role.OWNER))
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #138", 2026-11-26
    */
    // issue 138
    // a device nobody signed up on has no user so the app sends it to registration
    @Test
    fun issue_138_unknown_device_id_has_no_user() {
        val user = awaitResult<User?> { answer ->
            modelFor("issue-138-unknown-device-$deviceId").signIn { answer(it) }
        }

        assertNull(user)
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #88", 2026-11-26
    */
    // US 01.02.01 (issue 88)
    // as an entrant i want to join the waiting list for a campsite lottery
    @Test
    fun us_01_02_01_join_adds_the_entrant_to_the_lottery_waitlist() {
        val joined = awaitResult<Boolean> { answer -> modelFor().joinWaitlist(joinLottery, answer) }

        assertTrue(joined)
        // the entrant is saved in the lottery itself and is waiting for the draw
        assertEquals(mapOf(deviceId to EntryStatus.WAITING.name), waitlistOf(joinLottery.id))
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #88", 2026-11-26
    */
    // US 01.02.01 (issue 88)
    // joining a lottery you are already on doesnt add you again
    @Test
    fun us_01_02_01_join_twice_only_adds_the_entrant_once() {
        assertTrue(awaitResult<Boolean> { answer -> modelFor().joinWaitlist(joinLottery, answer) })
        val joinedAgain = awaitResult<Boolean> { answer -> modelFor().joinWaitlist(joinLottery, answer) }

        assertFalse(joinedAgain)
        assertEquals(mapOf(deviceId to EntryStatus.WAITING.name), waitlistOf(joinLottery.id))
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #88", 2026-11-26
    */
    // US 01.02.01 (issue 88)
    // a lottery with a full waitlist turns the entrant away and leaves the waitlist alone
    @Test
    fun us_01_02_01_join_full_waitlist_is_refused() {
        val before = waitlistOf(fullLottery.id)

        val joined = awaitResult<Boolean> { answer -> modelFor().joinWaitlist(fullLottery, answer) }

        assertFalse(joined)
        assertEquals(before, waitlistOf(fullLottery.id))
        assertFalse(deviceId in waitlistOf(fullLottery.id))
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #88", 2026-11-26
    */
    // US 01.02.01 (issue 88)
    // joining a lottery that doesnt exist fails and doesnt make one
    @Test
    fun us_01_02_01_join_missing_lottery_is_refused() {
        val missing = Lottery(id = "us-01-02-missing-lottery-$deviceId")

        val joined = awaitResult<Boolean> { answer -> modelFor().joinWaitlist(missing, answer) }

        assertFalse(joined)
        assertFalse(await(lotteryRef.document(missing.id).get()).exists())
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #88", 2026-11-26
    */
    // US 01.02.01 (issue 88)
    // two entrants joining at the same moment both end up on the waitlist
    // the waitlist is one list in the lottery so a careless save would lose one of them
    @Test
    fun us_01_02_01_entrants_joining_at_the_same_time_are_both_kept() {
        val results = arrayOfNulls<Boolean>(2)
        val bothAnswered = CountDownLatch(2)
        modelFor().joinWaitlist(joinLottery) {
            results[0] = it
            bothAnswered.countDown()
        }
        modelFor(secondDeviceId).joinWaitlist(joinLottery) {
            results[1] = it
            bothAnswered.countDown()
        }
        assertTrue("the model never answered", bothAnswered.await(10, TimeUnit.SECONDS))

        assertEquals(listOf<Boolean?>(true, true), results.toList())
        assertEquals(setOf(deviceId, secondDeviceId), waitlistOf(joinLottery.id).keys)
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #127", 2026-11-26
    */
    // US 01.03.01 (issue 127)
    // as an entrant i want to receive a notification when i win the campsite lottery
    @Test
    fun us_01_03_01_winners_are_notified_when_the_lottery_is_drawn() {
        val winners = drawTheLottery()

        assertEquals(drawLottery.numWinners, winners.size)
        winners.forEach { winner ->
            assertTrue(winner in drawEntrants.map { it.deviceId })
            // they are marked as selected and told
            assertEquals(EntryStatus.SELECTED.name, waitlistOf(drawLottery.id)[winner])
            assertTrue("$winner was never notified", waitForNotification(winner).isNotEmpty())
        }
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #127", 2026-11-26
    */
    // US 01.03.01 (issue 127)
    // only the people on the waitlist hear about the result
    @Test
    fun us_01_03_01_people_not_on_the_waitlist_are_not_notified() {
        val everyone = drawTheLottery() + drawEntrants.map { it.deviceId }
        everyone.toSet().forEach { waitForNotification(it) }

        assertTrue(notificationsFor(bystander.deviceId).isEmpty())
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #127", 2026-11-26
    */
    // US 01.03.01 (issue 127)
    // drawing a lottery that was already drawn fails and doesnt send everyone another notification
    @Test
    fun us_01_03_01_drawing_twice_does_not_notify_again() {
        drawTheLottery()
        drawEntrants.forEach { waitForNotification(it.deviceId) }
        val before = drawEntrants.associate { it.deviceId to notificationsFor(it.deviceId).size }

        val second = awaitResult<Result<List<String>>> { answer ->
            modelFor().drawLotteryWinner(drawLottery.id, answer)
        }
        assertTrue(second.isFailure)

        // nothing to wait for so give a wrong notification time to turn up
        Thread.sleep(2_000)
        assertEquals(before, drawEntrants.associate { it.deviceId to notificationsFor(it.deviceId).size })
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #126", 2026-11-26
    */
    // US 01.03.02 (issue 126)
    // as an entrant i want to receive a notification when i lose the campsite lottery
    @Test
    fun us_01_03_02_losers_are_notified_when_the_lottery_is_drawn() {
        val winners = drawTheLottery()
        val losers = drawEntrants.map { it.deviceId } - winners.toSet()
        assertEquals(drawEntrants.size - drawLottery.numWinners, losers.size)

        losers.forEach { loser ->
            // they are marked as not selected and told
            assertEquals(EntryStatus.NOT_SELECTED.name, waitlistOf(drawLottery.id)[loser])
            assertTrue("$loser was never notified", waitForNotification(loser).isNotEmpty())
        }

        // winning and losing arent the same message
        assertNotEquals(waitForNotification(winners.first()), waitForNotification(losers.first()))
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #126", 2026-11-26
    */
    // US 01.03.02 (issue 126)
    // the model can trigger a notification on an entrant and it is saved for them
    @Test
    fun us_01_03_02_notify_user_saves_a_notification_for_the_entrant() {
        val entrant = drawEntrants.first().deviceId
        val message = "US 01.03.02 direct notification"

        modelFor().notifyUser(message, entrant)

        eventually("the notification for $entrant") { message in notificationsFor(entrant) }
        // the notification id is also kept on the users profile so the app can find it
        eventually("the id on the profile of $entrant") {
            (await(usersRef.document(entrant).get()).get("notifications") as? List<*>)?.isNotEmpty() == true
        }
    }

    /* The following function is from Anthropic, Claude Sonnet 5.5, "Implement an android test case for issue #126", 2026-11-26
    */
    // US 01.03.02 (issue 126)
    // someone who turned notifications off isnt sent any
    @Test
    fun us_01_03_02_notify_user_respects_notifications_turned_off() {
        val model = modelFor()
        model.notifyUser("US 01.03.02 muted notification", mutedEntrant.deviceId)
        // a notification to someone with them on, once it arrives the muted one has had its chance too
        val control = drawEntrants.first().deviceId
        model.notifyUser("US 01.03.02 control notification", control)
        eventually("the control notification") { notificationsFor(control).isNotEmpty() }
        Thread.sleep(1_000)

        assertTrue(notificationsFor(mutedEntrant.deviceId).isEmpty())
    }
}