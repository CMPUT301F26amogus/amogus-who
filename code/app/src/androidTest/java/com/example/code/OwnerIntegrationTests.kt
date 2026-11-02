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
class OwnerIntegrationTests {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity
    //TODO Write a before to fill role
    @Test
    fun owner_navigate_to_messages() {
        // navigate to Messages
        composeTestRule
            .onNodeWithTag("Messages")
            .performClick()
        // verify messages
        composeTestRule
            .onNodeWithText("Messages")
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
            .onNodeWithText("Lotteries")
            .assertExists()
    }
    @Test
    fun owner_navigate_to_reservations() {
        // navigate to reservations
        composeTestRule
            .onNodeWithTag("My Reservations")
            .performClick()
        // verify reservations
        composeTestRule
            .onNodeWithText("Reservations")
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
            .onNodeWithText("QR Code")
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
            .onNodeWithText("Profile Settings")
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
            .onNodeWithText("My Campsites")
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
            .onNodeWithText("Notifications")
            .assertExists()
    }
}