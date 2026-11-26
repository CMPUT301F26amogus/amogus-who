package com.example.code.views

import Model
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Tab
import View
import androidx.compose.foundation.background
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

        // load every tab when the screen opens since nothing calls update for reservations
        LaunchedEffect(Unit) { loadReservations() }

        val tabs = mutableListOf<Tab>(
            Tab(
                name = "Current",
                color = entrantFooterBackgroundColor,
                option = {state = "Current"}),
            Tab(
                name = "Past",
                color = secondaryGreenColor,
                option = {state = "Past"}),
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
                TabManager(tabs,state)
            }
            message?.let {
                Row(modifier = Modifier.fillMaxWidth().background(light).padding(10.dp)) {
                    Text(it, fontWeight = FontWeight.Bold, color = primaryGreenTextColor)
                }
            }
            if (state=="Current") {
                Row(modifier = Modifier.weight(1f).background(primaryGreenColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(currentReservations) { index, reservation ->
                            CurrentReservationRow(
                                reservation = reservation,
                                navController = navController,
                                onCancel = { toCancel = reservation },
                            )
                        }
                    }
                }
            }else{
                Row(modifier = Modifier.weight(1f).background(secondaryGreenColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(previousReservations) { index, reservation ->
                            PreviousReservationRow(
                                reservation = reservation,
                                navController = navController
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
                        controller.cancelReservation(pending.reservation) { result ->
                            result
                                .onSuccess {
                                    message = "Your reservation at $campsiteName was cancelled"
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
    }

    // the top part of a card with the campsite dates rating and guests
    @Composable
    private fun ReservationInfo(reservation: EntrantReservation, background: Color) {
        val campsite = reservation.campsite
        Row(
            modifier = Modifier.height(120.dp).background(background)
                .width(500.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 15.dp).width(200.dp)){
                Row() {
                    // at most 2 lines so a long name cant push the dates out of the card
                    Text(
                        campsite?.name ?: "Unknown campsite",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row() {
                    Text(campsite?.location ?: "")
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row(modifier = Modifier.background(light).padding(horizontal = 20.dp).fillMaxWidth()) {
                    Text(
                        "${formatDate(reservation.reservation.checkIn)} | ${formatDate(reservation.reservation.checkOut)}",
                        fontSize = 13.sp,
                    )
                }
            }
            // labelled so its clear what each number is, same as the owner cards
            Column(modifier = Modifier.width(80.dp)){
                Spacer(modifier = Modifier.height(15.dp))
                Row() {
                    // no rating yet shows a dash instead of null
                    val rating = campsite?.averageRating?.let { "%.1f".format(it) } ?: "-"
                    Text("Rating: $rating", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(25.dp))
                Row() {
                    Text("Guests: ${reservation.reservation.numGuests}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    @Composable
    private fun CurrentReservationRow(
        reservation: EntrantReservation,
        navController: NavController,
        onCancel: () -> Unit,
    ) {
        Row(modifier = Modifier.padding(50.dp).height(150.dp).width(500.dp)){
            Column() {
                ReservationInfo(reservation, entrantFooterButtonColor)
                Row(modifier = Modifier.height(30.dp).background(light).width(500.dp)) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "X Cancel", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onCancel() },
                    )
                    Spacer(modifier = Modifier.width(105.dp))
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            controller.viewCampsite(reservation.reservation.campsiteId, navController = navController)
                        },
                    )
                }
            }
        }
    }
    @Composable
    private fun PreviousReservationRow(reservation: EntrantReservation, navController: NavController) {
        Row(modifier = Modifier.padding(50.dp).height(150.dp).width(500.dp)){
            Column() {
                ReservationInfo(reservation, Color(0xffcadac8))
                Row(modifier = Modifier.height(30.dp).background(light).width(500.dp)) {
                    Spacer(modifier = Modifier.width(180.dp))
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            controller.viewCampsite(reservation.reservation.campsiteId, navController = navController)
                        },
                    )
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
