package com.example.code

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
class AdminIntegrationTests {
    // the device id the app will read in MainActivity
    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
        Settings.Secure.ANDROID_ID,
    )

    private val userDoc by lazy { Firebase.firestore.collection("users").document(deviceId) }

    // blocks until firestore answers so the admin is saved before the app opens
    private fun <T> await(task: Task<T>): T = Tasks.await(task, 10, TimeUnit.SECONDS)

    // signs this device in as an admin then puts its real profile back after
    // runs before the compose rule because the app picks which home to show as soon as it opens
    // see [30] in Citation.md
    @get:Rule(order = 0)
    val signInAsAdmin = object : ExternalResource() {
        private var savedUser: Map<String, Any>? = null

        override fun before() {
            savedUser = await(userDoc.get()).data
            await(userDoc.set(User(deviceId = deviceId, name = "Admin Integration Test", role = Role.ADMIN)))
        }

        override fun after() {
            val user = savedUser
            if (user != null) await(userDoc.set(user)) else await(userDoc.delete())
        }
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity

    // the app shows a spinner until it loads the user so wait for the admin home
    @Before
    fun waitForAdminHome() {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag("Browse Profiles")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun admin_navigate_to_browse_profiles() {
        // navigate to browse profiles
        composeTestRule
            .onNodeWithTag("Browse Profiles")
            .performClick()
        // verify browse profiles
        composeTestRule
            .onNodeWithTag("Header: Browse Profiles")
            .assertExists()
    }
    @Test
    fun admin_navigate_to_browse_campsites() {
        // navigate to browse campsites
        composeTestRule
            .onNodeWithTag("Browse Campsites")
            .performClick()
        // verify browse campsites
        composeTestRule
            .onNodeWithTag("Header: Browse Campsites")
            .assertExists()
    }
    @Test
    fun admin_navigate_to_browse_images() {
        // navigate to browse images
        composeTestRule
            .onNodeWithTag("Browse Images")
            .performClick()
        // verify browse images
        composeTestRule
            .onNodeWithTag("Header: Images")
            .assertExists()
    }
    @Test
    fun admin_navigate_to_notification_logs() {
        // navigate to notification logs
        composeTestRule
            .onNodeWithTag("Notification Logs")
            .performClick()
        // verify notification logs
        composeTestRule
            .onNodeWithTag("Header: Notification Logs")
            .assertExists()
    }
    @Test
    fun admin_navigate_to_message_logs() {
        // navigate to message logs
        composeTestRule
            .onNodeWithTag("Message Logs")
            .performClick()
        // verify notification logs
        composeTestRule
            .onNodeWithTag("Header: Message Logs")
            .assertExists()
    }
    @Test
    fun admin_navigate_to_profile() {
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
    fun admin_navigate_to_campsites() {
        // navigate to campsites
        composeTestRule
            .onNodeWithTag("My Campsites")
            .performClick()
        // verify campsites
        composeTestRule
            .onNodeWithTag("Header: My Campsites")
            .assertExists()
    }
}