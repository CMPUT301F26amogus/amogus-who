package com.example.code

import Model
import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.code.model.Campsite
import com.example.code.views.AddEditCampsite
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * user story tests for the owner side of US 02.11.01
 *
 * the owner writes the rules entrants have to agree to on the add and edit campsite screen
 * the entrant side where they tick the box is in ReserveCampsiteUserStoryTests
 * shows the screen on its own like ReserveCampsiteUserStoryTests and deletes what the test made when its done
 */
@RunWith(AndroidJUnit4::class)
class CampsiteRulesUserStoryTests {
    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
        Settings.Secure.ANDROID_ID,
    )

    private val campsiteRef by lazy { Firebase.firestore.collection("campsites") }

    // made up name so the add test can find the campsite it made and only delete that one
    private val newCampsiteName = "US 02.11.01 New Campsite $deviceId"

    // a campsite this device owns that already has rules for the edit test
    private val existingCampsite = Campsite(
        id = "us-02-11-edit-site-$deviceId",
        ownerId = deviceId,
        name = "US 02.11.01 Edit Campsite",
        location = "Test Park",
        maxOccupancy = 4,
        pricePerNight = 25.0,
        rulesAndPolicy = "Quiet hours start at 11pm.",
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun <T> await(task: Task<T>): T = Tasks.await(task, 10, TimeUnit.SECONDS)

    // the campsites the add test made with its made up name
    private fun newCampsites(): List<DocumentSnapshot> =
        await(campsiteRef.whereEqualTo("ownerId", deviceId).whereEqualTo("name", newCampsiteName).get()).documents

    // shows the add or edit screen for whatever campsite is picked in the model
    // starts on a stand in for the owners campsite list so saving has somewhere to go back to
    private fun showScreen(picked: Campsite?) {
        val model = Model().apply {
            setDeviceInformation(deviceId)
            settTempCampsite(picked)
        }
        val view = AddEditCampsite(model)
        view.update(model)
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "campsites") {
                composable("campsites") { Text("Owner campsites screen") }
                composable("addEdit") { view.ShowScreen(navController) }
            }
            LaunchedEffect(Unit) { navController.navigate("addEdit") }
        }
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText("Save Changes").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // presses save and waits to land back on the campsite list
    private fun save() {
        composeTestRule.onNodeWithText("Save Changes").performScrollTo().performClick()
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText("Owner campsites screen").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @After
    fun cleanUp() {
        newCampsites().forEach { await(it.reference.delete()) }
        await(campsiteRef.document(existingCampsite.id).delete())
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.11.01), 2026-11-26
    see [12] in Citation.md
    */
    // US 02.11.01
    // the owner writes rules when listing a new campsite and they get saved with it
    @Test
    fun us_02_11_01_owner_adds_campsite_with_rules() {
        val rules = "No pets. Campfires in the fire pit only."
        showScreen(null)

        composeTestRule.onNodeWithText("Campsite Name").performScrollTo().performTextInput(newCampsiteName)
        composeTestRule.onNodeWithText("Park / Area").performScrollTo().performTextInput("Test Park")
        composeTestRule.onNodeWithText("Max Guests").performScrollTo().performTextInput("4")
        composeTestRule.onNodeWithText("Price Per Night").performScrollTo().performTextInput("25")
        composeTestRule.onNodeWithTag("campsiteRulesInputField").performScrollTo().performTextInput(rules)
        save()

        val saved = newCampsites().single().toObject(Campsite::class.java)!!
        assertEquals(rules, saved.rulesAndPolicy)
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.11.01), 2026-11-26
    see [12] in Citation.md
    */
    // US 02.11.01
    // editing a campsite shows the rules it has now and saves the new ones
    @Test
    fun us_02_11_01_owner_edits_campsite_rules() {
        await(campsiteRef.document(existingCampsite.id).set(existingCampsite))
        val newRules = "Quiet hours start at 10pm. No generators."
        showScreen(existingCampsite)

        composeTestRule.onNodeWithTag("campsiteRulesInputField")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(existingCampsite.rulesAndPolicy)
            .performTextReplacement(newRules)
        save()

        val saved = await(campsiteRef.document(existingCampsite.id).get()).toObject(Campsite::class.java)!!
        assertEquals(newRules, saved.rulesAndPolicy)
        // the other fields are left alone
        assertEquals(existingCampsite.name, saved.name)
        assertEquals(existingCampsite.maxOccupancy, saved.maxOccupancy)
    }
}
