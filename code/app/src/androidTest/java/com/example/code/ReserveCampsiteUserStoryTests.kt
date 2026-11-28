package com.example.code

import Model
import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.code.model.Campsite
import com.example.code.model.Reservation
import com.example.code.model.ReservationStatus
import com.example.code.model.Role
import com.example.code.model.User
import com.example.code.views.ReserveCampsite
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * user story tests for the confirmation on the reserve screen
 *
 * nothing in the app opens the reserve screen yet (that comes with US 01.04.01)
 * so this shows the screen on its own with the test campsite picked in the model
 * puts the devices real profile back and deletes what the test made when its done
 */
@RunWith(AndroidJUnit4::class)
class ReserveCampsiteUserStoryTests {
    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
        Settings.Secure.ANDROID_ID,
    )

    private val usersRef by lazy { Firebase.firestore.collection("users") }
    private val campsiteRef by lazy { Firebase.firestore.collection("campsites") }
    private val reservationRef by lazy { Firebase.firestore.collection("reservations") }
    private val notificationRef by lazy { Firebase.firestore.collection("notifications") }
    private val userDoc by lazy { usersRef.document(deviceId) }

    // what the owner wrote that entrants have to agree to (US 02.11.01)
    private val rules = "No campfires after 10pm. Pack out all garbage."

    // fits 4 and has nothing booked so any future dates work
    private val campsite = Campsite(
        id = "us-01-05-screen-site-$deviceId",
        ownerId = "us-01-owner-$deviceId",
        name = "US 01.05.01 Screen Campsite",
        location = "Test Park",
        maxOccupancy = 4,
        rulesAndPolicy = rules,
    )

    // the owner didnt write any rules so theres nothing to agree to
    private val noRulesCampsite = Campsite(
        id = "us-02-11-no-rules-site-$deviceId",
        ownerId = "us-01-owner-$deviceId",
        name = "US 02.11.01 No Rules Campsite",
        location = "Test Park",
        maxOccupancy = 4,
    )

    // kept so a test can swap which campsite the screen is reserving
    private lateinit var model: Model

    private val checkIn = LocalDate.now().plusDays(60)
    private val checkOut = checkIn.plusDays(2)

    private var savedUser: Map<String, Any>? = null
    private var reservationsBefore = emptySet<String>()
    private var notificationsBefore = emptySet<String>()

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun <T> await(task: Task<T>): T = Tasks.await(task, 10, TimeUnit.SECONDS)

    private fun myDocIds(ref: CollectionReference): Set<String> =
        await(ref.whereEqualTo("entrantId", deviceId).get()).documents.map { it.id }.toSet()

    // the reservations this test made at the test campsite
    private fun newReservations(): List<Reservation> =
        await(reservationRef.whereEqualTo("entrantId", deviceId).get()).documents
            .filter { it.id !in reservationsBefore }
            .mapNotNull { it.toObject(Reservation::class.java) }

    @Before
    fun seed() {
        savedUser = await(userDoc.get()).data
        reservationsBefore = myDocIds(reservationRef)
        notificationsBefore = myDocIds(notificationRef)
        await(userDoc.set(User(deviceId = deviceId, name = "US Test ENTRANT", role = Role.ENTRANT)))
        await(campsiteRef.document(campsite.id).set(campsite))
        await(campsiteRef.document(noRulesCampsite.id).set(noRulesCampsite))

        // the screen reserves whatever campsite the model has picked
        model = Model().apply {
            setDeviceInformation(deviceId)
            settTempCampsite(campsite)
        }
        val view = ReserveCampsite(model)
        // registered so picking another campsite redraws the screen like in the app
        model.addView(view)
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "reserve") {
                composable("reserve") { view.ShowScreen(navController) }
                // stands in for the real reservations screen so we can see the done button goes there
                composable(Routes.RESERVATIONS) { Text("Reservations screen") }
            }
        }
    }

    @After
    fun cleanUp() {
        listOf(reservationRef to reservationsBefore, notificationRef to notificationsBefore).forEach { (ref, before) ->
            await(ref.whereEqualTo("entrantId", deviceId).get()).documents
                .filter { it.id !in before }
                .forEach { await(it.reference.delete()) }
        }
        await(campsiteRef.document(campsite.id).delete())
        await(campsiteRef.document(noRulesCampsite.id).delete())
        savedUser?.let { await(userDoc.set(it)) } ?: await(userDoc.delete())
    }

    // types into the reserve form like an entrant would and presses the button
    // ticks the rules box first unless told not to (US 02.11.01)
    private fun fillAndReserve(arrival: LocalDate, departure: LocalDate, guests: String, acceptRules: Boolean = true) {
        val field = DateTimeFormatter.ofPattern("MMddyyyy")
        composeTestRule.onNodeWithTag("arrivalDateInputField").performTextInput(arrival.format(field))
        composeTestRule.onNodeWithTag("departureDateInputField").performTextInput(departure.format(field))
        composeTestRule.onNodeWithTag("numberOfGuestsInputField").performTextInput(guests)
        if (acceptRules) composeTestRule.onNodeWithTag("acceptRulesCheckbox").performClick()
        composeTestRule.onNodeWithTag("reserveCampsiteButton").performClick()
    }

    // how the popup shows dates like 05/01/27
    private fun shown(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MM/dd/yy"))

    private fun waitForTag(tag: String) {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "make the userstorytests" (US 01.05.01), 2026-11-26
    see [8] in Citation.md
    */
    // US 01.05.01
    // as an entrant i want to receive confirmation of my campsite reservation
    @Test
    fun us_01_05_01_reserve_screen_shows_confirmation() {
        composeTestRule.onNodeWithText(campsite.name).assertIsDisplayed()
        fillAndReserve(checkIn, checkOut, "2")

        // the popup confirms the stay with its campsite dates and guests
        waitForTag("reservationConfirmation")
        composeTestRule.onNodeWithText("Reservation confirmed").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("${campsite.name} from ${shown(checkIn)} to ${shown(checkOut)} for 2 guest(s)", substring = true)
            .assertIsDisplayed()

        // and it really was saved
        val saved = newReservations().single()
        assertEquals(campsite.id, saved.campsiteId)
        assertEquals(checkIn.toString(), saved.checkIn)
        assertEquals(checkOut.toString(), saved.checkOut)
        assertEquals(2, saved.numGuests)
        assertEquals(ReservationStatus.ACTIVE, saved.status)

        // done takes them to their reservations
        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.onNodeWithText("Reservations screen").assertIsDisplayed()
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "make the userstorytests" (US 01.05.02), 2026-11-26
    see [8] in Citation.md
    */
    // US 01.05.02
    // the confirmation lets the entrant download the reservation as a csv
    @Test
    fun us_01_05_02_reserve_screen_downloads_csv() {
        fillAndReserve(checkIn, checkOut, "4")
        waitForTag("downloadReservationCsv")
        val saved = newReservations().single()

        CsvPickerStub().use { picker ->
            composeTestRule.onNodeWithTag("downloadReservationCsv").performClick()
            waitForText("CSV saved")

            assertEquals("reservation-${saved.id}.csv", picker.suggestedName)
            val lines = picker.lines()
            assertEquals(listOf(Reservation.CSV_HEADER, saved.toCsvRow(campsite.name)), lines)
            assertTrue(lines[1].endsWith(",4,ACTIVE"))
        }
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "make the userstorytests" (US 01.05.01), 2026-11-26
    see [8] in Citation.md
    */
    // US 01.05.01
    // bad input isnt confirmed and nothing gets saved
    @Test
    fun us_01_05_01_reserve_screen_no_confirmation_for_bad_input() {
        // leaving before arriving
        fillAndReserve(checkOut, checkIn, "2")

        waitForText("Departure has to be after arrival")
        composeTestRule.onNodeWithText("Reservation confirmed").assertDoesNotExist()
        assertTrue(newReservations().isEmpty())
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.11.01), 2026-11-26
    see [12] in Citation.md
    */
    // US 02.11.01
    // the entrant sees the owners rules with a box to agree to them that starts unticked
    @Test
    fun us_02_11_01_rules_shown_with_unticked_checkbox() {
        composeTestRule.onNodeWithTag("tosBox").assertIsDisplayed()
        composeTestRule.onNodeWithText(rules).assertIsDisplayed()
        composeTestRule.onNodeWithTag("acceptRulesCheckbox").assertIsDisplayed().assertIsOff()

        // tapping it ticks it and tapping again unticks it
        composeTestRule.onNodeWithTag("acceptRulesCheckbox").performClick().assertIsOn()
        composeTestRule.onNodeWithTag("acceptRulesCheckbox").performClick().assertIsOff()
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.11.01), 2026-11-26
    see [12] in Citation.md
    */
    // US 02.11.01
    // as a campsite owner i want entrants to confirm theyve read the rules before confirming a reservation
    @Test
    fun us_02_11_01_cannot_reserve_without_accepting_rules() {
        fillAndReserve(checkIn, checkOut, "2", acceptRules = false)

        waitForText("You have to agree to the campsite rules first")
        composeTestRule.onNodeWithText("Reservation confirmed").assertDoesNotExist()
        assertTrue(newReservations().isEmpty())

        // ticking the box and trying again goes through
        composeTestRule.onNodeWithTag("acceptRulesCheckbox").performClick()
        composeTestRule.onNodeWithTag("reserveCampsiteButton").performClick()
        waitForTag("reservationConfirmation")
        assertEquals(1, newReservations().size)
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.11.01), 2026-11-26
    see [12] in Citation.md
    */
    // US 02.11.01
    // the reservation remembers the entrant agreed to the rules
    @Test
    fun us_02_11_01_accepted_rules_saved_on_reservation() {
        fillAndReserve(checkIn, checkOut, "2")

        waitForTag("reservationConfirmation")
        val saved = newReservations().single()
        assertEquals(campsite.id, saved.campsiteId)
        assertTrue(saved.rulesAccepted)
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "lets start with writing the user story tests for these" (US 02.11.01), 2026-11-26
    see [12] in Citation.md
    */
    // US 02.11.01
    // a campsite with no rules has nothing to agree to so theres no box and reserving still works
    @Test
    fun us_02_11_01_no_checkbox_when_campsite_has_no_rules() {
        composeTestRule.runOnIdle { model.settTempCampsite(noRulesCampsite) }
        composeTestRule.onNodeWithText(noRulesCampsite.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag("acceptRulesCheckbox").assertDoesNotExist()

        fillAndReserve(checkIn, checkOut, "2", acceptRules = false)

        waitForTag("reservationConfirmation")
        val saved = newReservations().single()
        assertEquals(noRulesCampsite.id, saved.campsiteId)
        assertFalse(saved.rulesAccepted)
    }
}
