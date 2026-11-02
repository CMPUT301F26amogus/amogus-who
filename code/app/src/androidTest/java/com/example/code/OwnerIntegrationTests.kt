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
class EntrantIntegrationTests {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<EntrantHomeView>() // access MainActivity
    @Test
    fun entrant_navigate_to_messages() {
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
    fun entrant_navigate_to_search() {
        // navigate to search
        composeTestRule
            .onNodeWithTag("Search")
            .performClick()
        // verify search
        composeTestRule
            .onNodeWithText("Search")
            .assertExists()
        // execute a search
        composeTestRule
            .onNodeWithTag("Search")
            .performClick()
        // verify results page
        composeTestRule
            .onNodeWithText("Search Results")
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
            .onNodeWithText("Lotteries")
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
            .onNodeWithText("Reservations")
            .assertExists()
    }
    @Test
    fun entrant_navigate_to_scan() {
        // navigate to scan
        composeTestRule
            .onNodeWithTag("Scan Codes")
            .performClick()
        // verify reservations
        composeTestRule
            .onNodeWithText("Scan Codes")
            .assertExists()
    }
    @Test
    fun entrant_navigate_to_profile() {
        // navigate to profile
        composeTestRule
            .onNodeWithTag("Profile")
            .performClick()
        // verify reservations
        composeTestRule
            .onNodeWithText("Profile Settings")
            .assertExists()
    }
    @Test
    fun _navigate_to_profile() {
        // navigate to profile
        composeTestRule
            .onNodeWithTag("Profile")
            .performClick()
        // verify reservations
        composeTestRule
            .onNodeWithText("Profile Settings")
            .assertExists()
    }
}