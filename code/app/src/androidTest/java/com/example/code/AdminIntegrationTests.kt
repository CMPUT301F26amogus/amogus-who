package com.example.code

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
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity
    //TODO Write a before to fill role
    @Test
    fun admin_navigate_to_browse_profiles() {
        // navigate to browse profiles
        composeTestRule
            .onNodeWithTag("Browse Profiles")
            .performClick()
        // verify browse profiles
        composeTestRule
            .onNodeWithText("Browse Profiles")
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
            .onNodeWithText("Browse Campsites")
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
            .onNodeWithText("Images")
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
            .onNodeWithText("Notification Logs")
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
            .onNodeWithText("Message Logs")
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
            .onNodeWithText("Profile Settings")
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
            .onNodeWithText("My Campsites")
            .assertExists()
    }
}