package com.example.code

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * one test or more per user story
 *
 * named after the story id so its easy to match them up with the spec
 * the device has to be signed in as the right role for each story
 */
@RunWith(AndroidJUnit4::class)
class UserStoryTests {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // access MainActivity
    //TODO Write a before to fill role

    // US 02.07.01
    // as a campsite owner i want to view a list of active reservations
    @Test
    fun us_02_07_01_owner_views_active_reservations() {
        // open reservations from the owner home
        composeTestRule
            .onNodeWithTag("My Reservations")
            .performClick()
        composeTestRule
            .onNodeWithTag("Header: Reservations")
            .assertExists()

        // active reservations are the ones under upcoming
        composeTestRule
            .onNodeWithText("Upcoming")
            .performClick()

        // at least one reservation card shows up
        composeTestRule
            .onAllNodesWithText("Guests", substring = true)
            .onFirst()
            .assertIsDisplayed()
        composeTestRule
            .onAllNodesWithText("Nights", substring = true)
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
