package com.example.code.views

import Model
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Tab
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.controllers.EntrantReservationsController
import com.example.code.model.EntrantReservation
import com.example.code.ui.theme.CodeTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReservationsView(model: Model): View() {
    // one list per tab so each tab only shows its own reservations
    val currentReservations = mutableStateListOf<EntrantReservation>()
    val previousReservations = mutableStateListOf<EntrantReservation>()
    var controller = EntrantReservationsController(model)

    // swaps a lists contents with what firestore sent back
    private fun replaceWith(list: MutableList<EntrantReservation>, result: List<EntrantReservation>) {
        list.clear()
        list.addAll(result)
    }

    // loads both tabs, called when the screen opens and after a cancel
    private fun loadReservations() {
        controller.onActiveReservations { replaceWith(currentReservations, it) }
        controller.onPastReservations { replaceWith(previousReservations, it) }
    }

    // shows 2027-05-01 as 05/01/27 and leaves anything it cant read as it is
    private fun formatDate(date: String): String =
        runCatching { LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MM/dd/yy")) }
            .getOrDefault(date)

    val bgColor = secondaryGreenColor
    val headerbgColor = entrantHeaderBackgroundColor
    val headerTextColor = entrantHeaderTextColor
    val footerTextColor = entrantFooterTextColor
    val footerbgColor = entrantFooterBackgroundColor
    val footerButtonColor = entrantFooterButtonColor
    @Composable
    public fun ShowScreen(navController: NavController) {
        var state by remember { mutableStateOf("Current") }
        // the reservation waiting for the entrant to confirm the cancel or null if theres no popup
        var toCancel by remember { mutableStateOf<EntrantReservation?>(null) }
        // tells the entrant if their cancel worked or why it didnt
        var message by remember { mutableStateOf<String?>(null) }
        // the reservation that was just cancelled so the confirmation popup can show (US 01.07.02)
        var cancelled by remember { mutableStateOf<EntrantReservation?>(null) }
        // saves a reservation or cancellation as a csv (US 01.05.02 01.07.02)
        val downloadCsv = rememberCsvDownloader { message = it }
        val saveCsv = { reservation: EntrantReservation ->
            downloadCsv(controller.csvFileName(reservation), controller.reservationCsv(reservation))
        }

        // load every tab when the screen opens since nothing calls update for reservations
        LaunchedEffect(Unit) { loadReservations() }

        val tabs = mutableListOf<Tab>(
            Tab(
                name = "Current",
                color = entrantFooterBackgroundColor,
                option = {state = "Current"}),
            Tab(
                name = "Previous",
                color = secondaryGreenColor,
                option = {state = "Previous"}),
        )
        Column() {
            Row() {
                Header(
                    headerText = "Reservations",
                    backgroundColor = headerbgColor,
                    textColor = headerTextColor,
                )
            }
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
            ) {
                TabManager(tabs, state, backgroundColor = headerbgColor)
            }
            message?.let {
                Row(modifier = Modifier.fillMaxWidth().background(light).padding(10.dp)) {
                    Text(it, fontWeight = FontWeight.Bold, color = primaryGreenTextColor)
                }
            }
            if (state=="Current") {
                Row(modifier = Modifier.weight(1f).background(primaryGreenColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 10.dp),
                    ) {
                        itemsIndexed(currentReservations) { index, reservation ->
                            ReservationCard(
                                reservation = reservation,
                                navController = navController,
                                onCancel = { toCancel = reservation },
                                onCsv = { saveCsv(reservation) },
                            )
                        }
                    }
                }
            }else{
                Row(modifier = Modifier.weight(1f).background(secondaryGreenColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 10.dp),
                    ) {
                        itemsIndexed(previousReservations) { index, reservation ->
                            ReservationCard(
                                reservation = reservation,
                                navController = navController,
                                onCancel = null,
                                onCsv = { saveCsv(reservation) },
                            )
                        }
                    }
                }
            }
            Row() {
                Footer(
                    navController = navController,
                    textColor = footerTextColor,
                    backgroundColor = footerbgColor,
                    buttonColor = footerButtonColor,
                )
            }
        }

        // ask before cancelling since it cant be undone
        toCancel?.let { pending ->
            val campsiteName = pending.campsite?.name ?: "this campsite"
            val stay = "${formatDate(pending.reservation.checkIn)} to ${formatDate(pending.reservation.checkOut)}"
            AlertDialog(
                onDismissRequest = { toCancel = null },
                title = { Text("Cancel reservation?") },
                text = { Text("Cancel your stay at $campsiteName from $stay? This can't be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        toCancel = null
                        controller.cancelReservation(pending) { result ->
                            result
                                .onSuccess {
                                    message = null
                                    cancelled = it
                                    loadReservations()
                                }
                                .onFailure { message = "Couldn't cancel: ${it.message}" }
                        }
                    }) { Text("Yes, cancel") }
                },
                dismissButton = {
                    TextButton(onClick = { toCancel = null }) { Text("Keep it") }
                },
            )
        }

        // confirms the cancel and offers the csv (US 01.07.02)
        // the notification only goes out if they havent turned notifications off
        cancelled?.let { done ->
            val campsiteName = done.campsite?.name ?: "this campsite"
            val stay = "${formatDate(done.reservation.checkIn)} to ${formatDate(done.reservation.checkOut)}"
            AlertDialog(
                onDismissRequest = { cancelled = null },
                modifier = Modifier.testTag("cancelConfirmation"),
                title = { Text("Reservation cancelled") },
                text = {
                    Text(
                        "Your stay at $campsiteName from $stay was cancelled. " +
                            "You'll get a notification if they're turned on, or you can download a CSV copy."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            cancelled = null
                            saveCsv(done)
                        },
                        modifier = Modifier.testTag("downloadCancellationCsv"),
                    ) { Text("Download CSV") }
                },
                dismissButton = {
                    TextButton(onClick = { cancelled = null }) { Text("OK") }
                },
            )
        }
    }

    // one reservation drawn like the mockup, name and park with the rating pill, then the dates and guests
    // the bottom bar has cancel only for current stays, csv for both (US 01.05.02) and details
    // see [26] in Citation.md
    @Composable
    private fun ReservationCard(
        reservation: EntrantReservation,
        navController: NavController,
        onCancel: (() -> Unit)?,
        onCsv: () -> Unit,
    ) {
        val campsite = reservation.campsite
        // no rating yet shows a dash instead of null
        val rating = campsite?.averageRating?.let { "%.1f".format(it) } ?: "-"
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardPrimary),
        ) {
            Column(
                modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        // at most 2 lines so a long name cant push the dates out of the card
                        Text(
                            campsite?.name ?: "Unknown campsite",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = primaryGreenTextColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(campsite?.location ?: "", fontSize = 13.sp, color = primaryGreenTextColor)
                    }
                    RatingPill(rating)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StayDatesBox(reservation.reservation.checkIn, reservation.reservation.checkOut, cardAccent)
                    GuestsBox(reservation.reservation.numGuests, cardAccent)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().background(cardAccent).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onCancel != null) {
                    CardAction("Cancel", Icons.Outlined.Clear, onClick = onCancel)
                }
                // download this reservation as a csv (US 01.05.02)
                CardAction("CSV", Icons.Outlined.Share, tag = "reservationCsv", onClick = onCsv)
                Spacer(modifier = Modifier.weight(1f))
                CardAction("Details", Icons.Filled.PlayArrow) {
                    controller.viewCampsite(reservation.reservation.campsiteId, navController = navController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationsViewPreview() {
    val mp = ReservationsView(Model())
    CodeTheme {
        (
            mp.ShowScreen(rememberNavController())
        )
    }
}
