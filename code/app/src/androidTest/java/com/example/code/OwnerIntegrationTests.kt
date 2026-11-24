package com.example.code

import Model
import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.code.model.Role
import com.example.code.model.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
@RunWith(AndroidJUnit4::class)
class OwnerIntegrationTests {
    // the device id the app will read in MainActivity
    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
        Settings.Secure.ANDROID_ID,
    )

    private val userDoc by lazy { Model().dataRef.document(deviceId) }

    // blocks until firestore answers so the owner is saved before the app opens
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 10, TimeUnit.SECONDS)

    // signs this device in as an owner then puts its real profile back after
    // runs before the compose rule because the app picks owner or entrant home as soon as it opens
    @get:Rule(order = 0)
    val signInAsOwner = object : ExternalResource() {
        private var savedUser: Map<String, Any>? = null

        override fun before() {
            savedUser = await(userDoc.get()).data
            await(userDoc.set(User(deviceId = deviceId, name = "Owner Integration Test", role = Role.OWNER)))
        }

        override fun after() {
            val user = savedUser
            if (user != null) await(userDoc.set(user)) else await(userDoc.delete())
        }
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity

    // the app shows a spinner until it loads the user so wait for the owner home
    @Before
    fun waitForOwnerHome() {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("My Campsites")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun owner_navigate_to_messages() {
        // navigate to Messages
        composeTestRule
            .onNodeWithTag("Messages")
            .performClick()
        // verify messages
        composeTestRule
            .onNodeWithTag("Header: Messages")
            .assertExists()
    }
    @Test
    fun owner_navigate_to_lotteries() {
        // navigate to lotteries
        composeTestRule
            .onNodeWithTag("Lotteries")
            .performClick()
        // verify lotteries
        composeTestRule
            .onNodeWithTag("Header: Lotteries")
            .assertExists()
    }
    @Test
    fun owner_navigate_to_reservations() {
        // navigate to reservations
        composeTestRule
            .onNodeWithTag("Reservations")
            .performClick()
        // verify reservations
        composeTestRule
            .onNodeWithTag("Header: Reservations")
            .assertExists()
    }
    @Test
    fun owner_navigate_to_scan() {
        // navigate to scan
        composeTestRule
            .onNodeWithTag("QR Codes")
            .performClick()
        // verify scan
        composeTestRule
            .onNodeWithTag("Header: QR Code")
            .assertExists()
    }
    @Test
    fun owner_navigate_to_profile() {
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
    fun owner_navigate_to_campsites() {
        // navigate to campsites
        composeTestRule
            .onNodeWithTag("My Campsites")
            .performClick()
        // verify campsites
        composeTestRule
            .onNodeWithTag("Header: My Campsites")
            .assertExists()
    }
    @Test
    fun owner_navigate_to_notifications() {
        // navigate to campsites
        composeTestRule
            .onNodeWithTag("Notifications")
            .performClick()
        // verify campsites
        composeTestRule
            .onNodeWithTag("Header: Notifications")
            .assertExists()
    }
}