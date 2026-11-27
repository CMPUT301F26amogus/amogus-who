package com.example.code

import Model
import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.material3.Text
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.code.model.AvailabilitySchedule
import com.example.code.model.Campsite
import com.example.code.model.EntryStatus
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.ReservationStatus
import com.example.code.model.Role
import com.example.code.model.User
import com.example.code.model.WaitlistEntry
import com.example.code.views.LotteriesView
import com.example.code.views.ReserveCampsite
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * user story tests for a lottery winner reserving or declining (US 01.04.01 01.04.02 01.04.03)
 *
 * seeds a lottery with two campsites that this device already won so the draw doesnt have to run
 * shows the lotteries screen and the reserve screen it opens like the app does
 * puts the devices real profile back and deletes what the test made when its done
 */
@RunWith(AndroidJUnit4::class)
class LotteryWinUserStoryTests {
    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
        Settings.Secure.ANDROID_ID,
    )

    private val usersRef by lazy { Firebase.firestore.collection("users") }
    private val campsiteRef by lazy { Firebase.firestore.collection("campsites") }
    private val lotteryRef by lazy { Firebase.firestore.collection("lotteries") }
    private val reservationRef by lazy { Firebase.firestore.collection("reservations") }
    private val notificationRef by lazy { Firebase.firestore.collection("notifications") }
    private val userDoc by lazy { usersRef.document(deviceId) }

    private val today = LocalDate.now()
    // both campsites are open for 10 nights a month from now
    private val openStart = today.plusDays(30)
    private val openEnd = today.plusDays(40)
    // someone else already has 2 nights in the middle of the first campsite
    private val bookedStart = today.plusDays(33)
    private val bookedEnd = today.plusDays(35)

    private val lotteryId = "us-01-04-lottery-$deviceId"
    private val ownerId = "us-01-04-owner-$deviceId"

    private fun testCampsite(id: String, name: String) = Campsite(
        id = id,
        ownerId = ownerId,
        name = name,
        location = "Test Park",
        lotteryId = lotteryId,
        maxOccupancy = 4,
        availability = AvailabilitySchedule(listOf(openStart.toString(), openEnd.toString())),
    )

    private val firstCampsite = testCampsite("us-01-04-site-a-$deviceId", "US 01.04 Lakeside")
    private val secondCampsite = testCampsite("us-01-04-site-b-$deviceId", "US 01.04 Hilltop")

    // the other entrants stay that takes nights out of the first campsite
    private val otherStay = Reservation(
        id = "us-01-04-other-stay-$deviceId",
        campsiteId = firstCampsite.id,
        entrantId = "us-01-04-someone-else-$deviceId",
        checkIn = bookedStart.toString(),
        checkOut = bookedEnd.toString(),
        numGuests = 2,
        lotteryId = lotteryId,
    )

    // registration is over and booking is open so this device can reserve right now
    private val lottery = Lottery(
        name = "US 01.04 Lottery",
        id = lotteryId,
        ownerId = ownerId,
        campsiteIds = listOf(firstCampsite.id, secondCampsite.id),
        registrationStart = today.minusDays(10).toString(),
        registrationEnd = today.minusDays(1).toString(),
        bookingStart = today.minusDays(1).toString(),
        bookingEnd = today.plusDays(10).toString(),
        numWinners = 1,
        waitlist = listOf(WaitlistEntry(id = deviceId, lotteryId = lotteryId, status = EntryStatus.SELECTED)),
    )

    private lateinit var model: Model
    private var savedUser: Map<String, Any>? = null
    private var reservationsBefore = emptySet<String>()
    private var notificationsBefore = emptySet<String>()

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun <T> await(task: Task<T>): T = Tasks.await(task, 10, TimeUnit.SECONDS)

    private fun myDocIds(ref: CollectionReference): Set<String> =
        await(ref.whereEqualTo("entrantId", deviceId).get()).documents.map { it.id }.toSet()

    // the reservations this test made
    private fun newReservations(): List<Reservation> =
        await(reservationRef.whereEqualTo("entrantId", deviceId).get()).documents
            .filter { it.id !in reservationsBefore }
            .mapNotNull { it.toObject(Reservation::class.java) }

    // where this device is in the saved lottery
    private fun myEntryStatus(): EntryStatus? =
        await(lotteryRef.document(lotteryId).get()).toObject(Lottery::class.java)?.entryFor(deviceId)?.status

    private fun waitForEntryStatus(status: EntryStatus) {
        val giveUpAt = System.currentTimeMillis() + 10_000
        while (myEntryStatus() != status) {
            check(System.currentTimeMillis() < giveUpAt) { "Lottery entry never became $status" }
            Thread.sleep(250)
        }
    }

    @Before
    fun seed() {
        savedUser = await(userDoc.get()).data
        reservationsBefore = myDocIds(reservationRef)
        notificationsBefore = myDocIds(notificationRef)
        await(userDoc.set(User(deviceId = deviceId, name = "US Test ENTRANT", role = Role.ENTRANT)))
        await(campsiteRef.document(firstCampsite.id).set(firstCampsite))
        await(campsiteRef.document(secondCampsite.id).set(secondCampsite))
        await(reservationRef.document(otherStay.id).set(otherStay))
        await(lotteryRef.document(lotteryId).set(lottery))
        // joinWaitlist keeps this list so the lotteries screen can find the ones the user is in
        await(lotteryRef.document(lotteryId).update("entrantIds", listOf(deviceId)))

        model = Model().apply { setDeviceInformation(deviceId) }
        val lotteriesView = LotteriesView(model)
        val reserveView = ReserveCampsite(model)
        // registered like in the app so picking a campsite redraws the reserve screen
        model.addView(lotteriesView)
        model.addView(reserveView)
        lotteriesView.update(model)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Routes.LOTTERIES) {
                composable(Routes.LOTTERIES) { lotteriesView.ShowScreen(navController) }
                composable(Routes.RESERVE_CAMPSITE) { reserveView.ShowScreen(navController) }
                // stand ins so the tests can see where the buttons go
                composable(Routes.RESERVATIONS) { Text("Reservations screen") }
                composable(Routes.CAMPSITE_DETAILS) { Text("Details screen") }
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
        await(reservationRef.document(otherStay.id).delete())
        await(lotteryRef.document(lotteryId).delete())
        await(campsiteRef.document(firstCampsite.id).delete())
        await(campsiteRef.document(secondCampsite.id).delete())
        savedUser?.let { await(userDoc.set(it)) } ?: await(userDoc.delete())
    }

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

    // taps reserve on one campsites row of the won lottery
    private fun openReserveFor(campsite: Campsite) {
        val tag = "reserveLotteryButton-${campsite.id}"
        waitForTag(tag)
        composeTestRule.onNodeWithTag(tag).performClick()
        composeTestRule.onNodeWithTag("campsiteNameBox").assertIsDisplayed()
    }

    // the calendar names each day like Sunday, December 27, 2026
    private val dayLabel = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)

    // a day on the availability calendar
    private fun day(date: LocalDate): SemanticsMatcher =
        hasContentDescription(date.format(dayLabel), substring = true) and hasClickAction() and
            hasAnyAncestor(hasTestTag("availabilityCalendar"))

    // scrolls the calendar to a days month so it can be checked or tapped
    private fun showDay(date: LocalDate) = composeTestRule.run {
        onNodeWithTag("availabilityCalendar").performScrollTo()
        onNode(hasScrollAction() and hasAnyAncestor(hasTestTag("availabilityCalendar")))
            .performScrollToNode(day(date))
        onNode(day(date))
    }

    // picks the stay on the calendar like an entrant would then presses the button
    private fun fillAndReserve(arrival: LocalDate, departure: LocalDate, guests: String) {
        showDay(arrival).performClick()
        showDay(departure).performClick()
        composeTestRule.onNodeWithTag("numberOfGuestsInputField").performScrollTo().performTextInput(guests)
        composeTestRule.onNodeWithTag("reserveCampsiteButton").performScrollTo().performClick()
    }

    // how the reserve screen shows dates like 05/01/27
    private fun shown(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MM/dd/yy"))

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.01), 2026-11-27
    */
    // US 01.04.01
    // as an entrant if i win i want to see the available dates i can reserve the campsite for
    @Test
    fun us_01_04_01_winner_sees_available_dates() {
        waitForText("You won!")
        openReserveFor(firstCampsite)

        // the calendar shows up with the free days pickable
        waitForTag("availabilityCalendar")
        composeTestRule.onNodeWithTag("availabilityCalendar").assertIsDisplayed()
        showDay(openStart).assertIsEnabled()
        // checking out the day the other stay checks in and checking in the day it leaves are both fine
        showDay(bookedStart).assertIsEnabled()
        showDay(bookedEnd).assertIsEnabled()
        showDay(openEnd).assertIsEnabled()

        // the other entrants night the day before the window opens and the day after it closes are greyed out
        showDay(bookedStart.plusDays(1)).assertIsNotEnabled()
        showDay(openStart.minusDays(1)).assertIsNotEnabled()
        showDay(openEnd.plusDays(1)).assertIsNotEnabled()

        // and the free stretches are written under it
        composeTestRule.onNodeWithText("${shown(openStart)} to ${shown(bookedStart)}").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("${shown(bookedEnd)} to ${shown(openEnd)}").performScrollTo().assertIsDisplayed()
        assertEquals(2, composeTestRule.onAllNodesWithTag("availableDate").fetchSemanticsNodes().size)
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i thought we were supposed to do something with the calender for this" (US 01.04.01), 2026-11-27
    */
    // US 01.04.01
    // the winner has to pick both days on the calendar before reserving
    @Test
    fun us_01_04_01_must_pick_dates_on_calendar() {
        openReserveFor(firstCampsite)
        waitForTag("availabilityCalendar")

        composeTestRule.onNodeWithTag("numberOfGuestsInputField").performScrollTo().performTextInput("2")
        composeTestRule.onNodeWithTag("reserveCampsiteButton").performScrollTo().performClick()
        waitForText("Pick your check in day on the calendar")

        showDay(openStart).performClick()
        composeTestRule.onNodeWithTag("reserveCampsiteButton").performScrollTo().performClick()
        waitForText("Pick your check out day on the calendar")
        assertTrue(newReservations().isEmpty())
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.01), 2026-11-27
    */
    // US 01.04.01
    // dates that arent available cant be reserved and the win isnt used up
    @Test
    fun us_01_04_01_cannot_reserve_unavailable_dates() {
        openReserveFor(firstCampsite)
        waitForTag("availabilityCalendar")

        // both days can be tapped but the stay runs over the other entrants nights
        fillAndReserve(bookedStart.minusDays(1), bookedEnd.plusDays(1), "2")

        waitForText("Those dates aren't available")
        composeTestRule.onNodeWithText("Reservation confirmed").assertDoesNotExist()
        assertTrue(newReservations().isEmpty())
        assertEquals(EntryStatus.SELECTED, myEntryStatus())
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.02), 2026-11-27
    */
    // US 01.04.02
    // as an entrant if i win a multiple campsite lottery i want to choose which campsite and which dates to reserve
    @Test
    fun us_01_04_02_winner_picks_campsite_and_dates() {
        // every campsite in the lottery gets a row to pick from
        waitForTag("reserveLotteryButton-${firstCampsite.id}")
        waitForTag("reserveLotteryButton-${secondCampsite.id}")
        composeTestRule.onNodeWithText(firstCampsite.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(secondCampsite.name).assertIsDisplayed()

        // picks the second campsite and 3 nights of its dates
        openReserveFor(secondCampsite)
        composeTestRule.onNodeWithText(secondCampsite.name).assertIsDisplayed()
        waitForTag("availabilityCalendar")
        val checkIn = openStart.plusDays(2)
        val checkOut = checkIn.plusDays(3)
        fillAndReserve(checkIn, checkOut, "3")

        waitForTag("reservationConfirmation")
        val saved = newReservations().single()
        assertEquals(secondCampsite.id, saved.campsiteId)
        assertEquals(lotteryId, saved.lotteryId)
        assertEquals(checkIn.toString(), saved.checkIn)
        assertEquals(checkOut.toString(), saved.checkOut)
        assertEquals(3, saved.numGuests)
        assertEquals(ReservationStatus.ACTIVE, saved.status)

        // the win is used up so they cant reserve a second campsite from it
        assertEquals(EntryStatus.RESERVED, myEntryStatus())
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.02), 2026-11-27
    */
    // US 01.04.02
    // a winner can only reserve one stay from a lottery
    @Test
    fun us_01_04_02_cannot_reserve_twice_from_one_win() {
        openReserveFor(firstCampsite)
        waitForTag("availabilityCalendar")
        fillAndReserve(openStart, openStart.plusDays(1), "2")
        waitForTag("reservationConfirmation")
        waitForEntryStatus(EntryStatus.RESERVED)

        // asking the model directly like a second tap would
        var error: String? = null
        model.reserveLotteryWin(
            lottery = lottery,
            campsite = secondCampsite,
            checkIn = openStart.toString(),
            checkOut = openStart.plusDays(1).toString(),
            guests = 2,
        ) { result -> error = result.exceptionOrNull()?.message }
        composeTestRule.waitUntil(10_000) { error != null }
        assertEquals("You already reserved a campsite from this lottery", error)
        assertEquals(1, newReservations().size)
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.03), 2026-11-27
    */
    // US 01.04.03
    // as an entrant if i win i want to decline if i changed my mind
    @Test
    fun us_01_04_03_winner_declines_from_lotteries_screen() {
        waitForTag("declineLotteryButton")
        // theres a row per campsite and declining any of them declines the whole win
        composeTestRule.onAllNodesWithTag("declineLotteryButton")[0].performClick()

        // it asks first and keeping it does nothing
        composeTestRule.onNodeWithTag("declineLotteryDialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Keep it").performClick()
        composeTestRule.onNodeWithTag("declineLotteryDialog").assertDoesNotExist()
        assertEquals(EntryStatus.SELECTED, myEntryStatus())

        composeTestRule.onAllNodesWithTag("declineLotteryButton")[0].performClick()
        composeTestRule.onNodeWithTag("confirmDeclineLottery").performClick()
        waitForEntryStatus(EntryStatus.DECLINED)
        assertTrue(newReservations().isEmpty())

        // the won lottery leaves the current list
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText(firstCampsite.name).fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithText(secondCampsite.name).assertDoesNotExist()
    }

    /* The following function is from Anthropic, Claude Opus 5.5, "i am trying to implement user stories 01.04.03, 01.04.01, 01.04.02" (US 01.04.03), 2026-11-27
    */
    // US 01.04.03
    // as an entrant if i win i want to decline when none of the remaining dates work
    @Test
    fun us_01_04_03_winner_declines_from_reserve_screen() {
        openReserveFor(firstCampsite)
        waitForTag("availabilityCalendar")

        composeTestRule.onNodeWithTag("declineLotteryButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("confirmDeclineLottery").performClick()
        waitForEntryStatus(EntryStatus.DECLINED)
        assertTrue(newReservations().isEmpty())

        // back to the lotteries screen and a declined win cant be reserved anymore
        waitForText("Lotteries")
        var error: String? = null
        model.reserveLotteryWin(
            lottery = lottery,
            campsite = firstCampsite,
            checkIn = openStart.toString(),
            checkOut = openStart.plusDays(1).toString(),
            guests = 2,
        ) { result -> error = result.exceptionOrNull()?.message }
        composeTestRule.waitUntil(10_000) { error != null }
        assertEquals("You already declined this lottery", error)
    }
}
