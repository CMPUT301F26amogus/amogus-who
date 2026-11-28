package com.example.code

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.code.model.Campsite

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.example.code.model.Role
import com.example.code.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.junit.Before
import org.junit.rules.ExternalResource
import java.util.concurrent.TimeUnit

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class EntrantIntegrationTests {
    // the device id the app will read in MainActivity
    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
        Settings.Secure.ANDROID_ID,
    )

    private val userDoc by lazy { Firebase.firestore.collection("users").document(deviceId) }

    private val searchCampsite = Campsite(
        id = "integration-search-$deviceId",
        ownerId = "integration-owner-$deviceId",
        name = "Integration Test Campsite",
        location = "Test Park",
        maxOccupancy = 4,
    )
    private val campsiteDoc by lazy { Firebase.firestore.collection("campsites").document(searchCampsite.id) }

    // blocks until firestore answers so the entrant is saved before the app opens
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 10, TimeUnit.SECONDS)

    // signs this device in as an entrant then puts its real profile back after
    // runs before the compose rule because the app picks which home to show as soon as it opens
    // see [30] in Citation.md
    @get:Rule(order = 0)
    val signInAsEntrant = object : ExternalResource() {
        private var savedUser: Map<String, Any>? = null

        override fun before() {
            savedUser = await(userDoc.get()).data
            await(userDoc.set(User(deviceId = deviceId, name = "Entrant Integration Test", role = Role.ENTRANT)))
            // a campsite so the search always has at least one result to show
            await(campsiteDoc.set(searchCampsite))
        }

        override fun after() {
            await(campsiteDoc.delete())
            val user = savedUser
            if (user != null) await(userDoc.set(user)) else await(userDoc.delete())
        }
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity

    // the app shows a spinner until it loads the user so wait for the entrant home
    @Before
    fun waitForEntrantHome() {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("My Reservations")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun entrant_navigate_to_messages() {
        // navigate to Messages
        composeTestRule
            .onNodeWithTag("Messages")
            .performClick()
        // verify messages
        composeTestRule
            .onNodeWithTag("Header: Messages")
            .assertExists()
    }
    // see [31] in Citation.md
    @Test
    fun entrant_navigate_to_search() {
        // navigate to search, the home has a search row and a search pill both tagged Search so take the first
        composeTestRule
            .onAllNodesWithTag("Search")
            .onFirst()
            .performClick()
        // verify search, its header is a search box so it has no Header tag but only it has the clear button
        composeTestRule
            .onNodeWithText("X CLEAR")
            .assertExists()
        // execute a search, the search screen has a search button by the name box and one at the bottom
        composeTestRule
            .onAllNodes(hasText("Search") and hasClickAction())
            .onFirst()
            .performClick()
        // verify results page once firestore answers
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("Header: Search Results").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // result cards are tagged with the campsite name then Card like Pine Ridge Site 12Card
    private val resultCard = SemanticsMatcher("test tag ends with Card") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.endsWith("Card") == true
    }

    // see [31] in Citation.md
    @Test
    fun assert_test_results_show() {
        entrant_navigate_to_search()
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodes(resultCard).fetchSemanticsNodes().isNotEmpty()
        }
        // there can be more than one campsite so check the first card has its parts
        composeTestRule
            .onAllNodes(resultCard)
            .onFirst()
            .assertExists()
        composeTestRule
            .onAllNodesWithTag("DetailButton")
            .onFirst()
            .assertExists()
        composeTestRule
            .onAllNodesWithTag("maxOccupantsDetail")
            .onFirst()
            .assertExists()
        composeTestRule
            .onAllNodesWithTag("addressDetail")
            .onFirst()
            .assertExists()
    }
    @Test
    fun entrant_navigate_to_lotteries() {
        // navigate to lotteries
        composeTestRule
            .onNodeWithTag("My Lotteries")
            .performClick()
        // verify lotteries
        composeTestRule
            .onNodeWithTag("Header: Lotteries")
            .assertExists()
    }
    @Test
    fun entrant_navigate_to_reservations() {
        // navigate to reservations
        composeTestRule
            .onNodeWithTag("My Reservations")
            .performClick()
        // verify reservations
        composeTestRule
            .onNodeWithTag("Header: Reservations")
            .assertExists()
    }
    @Test
    fun entrant_navigate_to_scan() {
        // navigate to scan
        composeTestRule
            .onNodeWithTag("Scan Codes")
            .performClick()
        // verify scan
        composeTestRule
            .onNodeWithTag("Header: Scan Codes")
            .assertExists()
    }
    @Test
    fun entrant_navigate_to_profile() {
        // navigate to profile
        composeTestRule
            .onNodeWithTag("Profile")
            .performClick()
        // verify profile
        composeTestRule
            .onNodeWithTag("Header: Profile Settings")
            .assertExists()
    }
    @Test
    fun entrant_navigate_to_notifications() {
        // navigate to notifications, the bell is labelled by its content description not a tag
        // see [31] in Citation.md
        composeTestRule
            .onNodeWithContentDescription("Notifications")
            .performClick()
        // verify campsites
        composeTestRule
            .onNodeWithTag("Header: Notifications")
            .assertExists()
    }
}