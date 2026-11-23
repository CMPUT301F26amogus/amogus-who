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

    // runs before the compose rule because the app picks owner or entrant home as soon as it opens
    @get:Rule(order = 0)
    val seedOwnerData = object : ExternalResource() {
        private var savedUser: Map<String, Any>? = null

        override fun before() {
            // keep whatever profile this device had so we can put it back after
            savedUser = await(userDoc.get()).data
            await(userDoc.set(User(deviceId = deviceId, name = "US 02.07.01 Owner", role = Role.OWNER)))
            await(reservationRef.document(activeReservation.id).set(activeReservation))
        }

        override fun after() {
            await(reservationRef.document(activeReservation.id).delete())
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

        // active reservations are the ones under upcoming
        composeTestRule
            .onNodeWithText("Upcoming")
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
}
