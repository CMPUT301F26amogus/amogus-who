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
import com.example.code.model.Reservation
import com.example.code.model.ReservationStatus
import com.example.code.model.Role
import com.example.code.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * one test or more per user story
 *
 * named after the story id so its easy to match them up with the spec
 * the seed rule signs this device in as an owner and puts the test data in firestore
 * then puts the devices real profile back when the test is done
 */
@RunWith(AndroidJUnit4::class)
class UserStoryTests {
    // the device id the app will read in MainActivity
    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
        Settings.Secure.ANDROID_ID,
    )

    private val userDoc by lazy { Model().dataRef.document(deviceId) }
    private val reservationRef by lazy { Firebase.firestore.collection("reservations") }

    // made up names so the test can find its own card and not someone elses
    private val testCampsite = Campsite(
        id = "us-02-07-01-site",
        ownerId = deviceId,
        name = "US 02.07.01 Campsite",
        location = "Test Park",
    )
    private val testEntrant = User(deviceId = "us-02-07-01-entrant", name = "US 02.07.01 Entrant")
    private val activeReservation = Reservation(
        id = "us-02-07-01-active-$deviceId",
        campsite = testCampsite,
        entrant = testEntrant,
        checkIn = LocalDate.now().plusDays(7).toString(),
        checkOut = LocalDate.now().plusDays(9).toString(),
        guests = 2,
        status = ReservationStatus.ACTIVE,
    )

    // makes a reservation at the test campsite with its own entrant so each card can be found by name
    private fun seededReservation(
        key: String,
        entrantName: String,
        checkInDaysFromNow: Long,
        status: ReservationStatus,
    ) = Reservation(
        id = "us-02-07-$key-$deviceId",
        campsite = testCampsite,
        entrant = User(deviceId = "us-02-07-$key-entrant", name = entrantName),
        checkIn = LocalDate.now().plusDays(checkInDaysFromNow).toString(),
        checkOut = LocalDate.now().plusDays(checkInDaysFromNow + 2).toString(),
        guests = 2,
        status = status,
    )

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

    // runs before the compose rule because the app picks owner or entrant home as soon as it opens
    @get:Rule(order = 0)
    val seedOwnerData = object : ExternalResource() {
        private var savedUser: Map<String, Any>? = null

        override fun before() {
            // keep whatever profile this device had so we can put it back after
            savedUser = await(userDoc.get()).data
            await(userDoc.set(User(deviceId = deviceId, name = "US 02.07 Owner", role = Role.OWNER)))
            seededReservations.forEach { await(reservationRef.document(it.id).set(it)) }
        }

        override fun after() {
            seededReservations.forEach { await(reservationRef.document(it.id).delete()) }
            val user = savedUser
            if (user != null) await(userDoc.set(user)) else await(userDoc.delete())
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
        waitForText(finishedReservation.entrant!!.name)
        assertShown(finishedReservation.entrant!!.name)
        assertShown(markedPastReservation.entrant!!.name)

        // active cancelled and rejected ones dont belong here
        assertNotShown(testEntrant.name)
        assertNotShown(cancelledReservation.entrant!!.name)
        assertNotShown(rejectedReservation.entrant!!.name)

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
        assertNotShown(finishedReservation.entrant!!.name)
        assertNotShown(markedPastReservation.entrant!!.name)
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "Implement an android test case for user story 02.07.03", 2026-11-23
    */
    // US 02.07.03
    // as a campsite owner i want to view a list of cancelled reservations
    @Test
    fun us_02_07_03_owner_views_cancelled_reservations() {
        openOwnerReservations("Cancelled")

        waitForText(cancelledReservation.entrant!!.name)
        assertShown(cancelledReservation.entrant!!.name)

        // rejected is the owner turning someone down not a cancellation
        assertNotShown(rejectedReservation.entrant!!.name)
        assertNotShown(testEntrant.name)
        assertNotShown(finishedReservation.entrant!!.name)

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
        waitForText(toCancelReservation.entrant!!.name)
        assertShown(toCancelReservation.entrant!!.name)
        assertShown(toDeclineReservation.entrant!!.name)

        // and theyre gone from active
        composeTestRule
            .onNodeWithText("Active")
            .performClick()
        waitForText(testEntrant.name)
        assertNotShown(toCancelReservation.entrant!!.name)
        assertNotShown(toDeclineReservation.entrant!!.name)
    }
}
