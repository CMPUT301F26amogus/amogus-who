package com.example.code.views

import Model
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Tab
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.controllers.OwnerReservationsController
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.outlined.Email
import com.example.code.controllers.MessagesController
import com.example.code.model.OwnerReservation

/**
 *  OwnerReservationsView: the view for the owner to see reservations at their campsites
 *
 */
class OwnerReservationsView(model : Model) : View() {

    val controller by lazy { OwnerReservationsController(model = model) }
    // opens the chat with the camper about a reservation (US 02.12.01)
    private val messagesController = MessagesController(model)
    // one list per tab so each tab only shows its own reservations
    val activeReservations = mutableStateListOf<OwnerReservation>()
    val pastReservations = mutableStateListOf<OwnerReservation>()
    val cancelledReservations = mutableStateListOf<OwnerReservation>()

    // the reservation the owner tapped reject on so the confirm popup can show (US 02.08.01)
    var toReject by mutableStateOf<OwnerReservation?>(null)
    // why the last reject didnt work or null
    var rejectError by mutableStateOf<String?>(null)

    // swaps a lists contents with what firestore sent back
    private fun replaceWith(list: MutableList<OwnerReservation>, result: List<OwnerReservation>) {
        list.clear()
        list.addAll(result)
    }

    // asks the controller for every tab so they all match whats saved
    private fun reload() {
        controller.onActiveReservations { replaceWith(activeReservations, it) }
        controller.onPastReservations { replaceWith(pastReservations, it) }
        controller.onCancelledReservations { replaceWith(cancelledReservations, it) }
    }

    /**
     * reloads every tab when the model changes like after a reservation is rejected
     *
     * @param model the model, passed in when it notifies its views
     */
    override fun update(model: Model) {
        reload()
    }

    // This component is to be used in each reservation card
    // It stores the dates of a given reservation
    @Composable
    private fun DateInfoBox(startDate: String, endDate: String) {
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = cardAccent),
        ) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .height(14.dp),
            ) {
                Text(
                    text = prettyDate(startDate),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing / 2))
                VerticalDivider(modifier = Modifier)
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing / 2))
                Text(
                    text = prettyDate(endDate),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                )
            }
        }
    }

    // Another component used in each reservation card
    // It can store any relevant information in the format:
    // "Info: Data"
    // e.g. "Guests: 4", "Nights: 2"
    @Composable
    private fun SiteInfoBox(info: String, data: String) {
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = cardAccent),
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
            ) {
                Text(
                    text = "$info: ",
                    fontSize = 12.sp,
                    color = primaryGreenTextColor,
                )
                Text(
                    text = data,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                )
            }
        }
    }

    // This is the card that holds all the information needed to display for one
    // reservation
    //
    // One card per reservation in a given list will display in a lazy column
    // onReject is null when the card cant be rejected so theres no button
    // label says why its in the cancelled tab or null when theres nothing to say
    @Composable
    private fun CampsiteCard(
        name: String,
        campsiteName: String,
        location: String,
        startDate: String,
        endDate: String,
        guests: Int,
        nights: Int,
        rejectTag: String,
        onReject: (() -> Unit)?,
        label: String? = null,
        labelTag: String = "",
        onMessage: (() -> Unit)?,
    ) {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .background(color = cardPrimary)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier,
                    ) {
                        Text(
                            text = name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                        Text(
                            text = "$campsiteName · $location",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    DateInfoBox(
                        startDate = startDate,
                        endDate = endDate,
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Guests",
                        data = guests.toString(),
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Nights",
                        data = nights.toString(),  // TODO: update with real data
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(color = cardAccent)
                    .padding(horizontal = 4.dp),
            ) {
                // theres no accept since reservations are already booked when theyre made
                // see [26] in Citation.md
                if (onReject != null) {
                    CardAction("Reject", Icons.Outlined.Clear, tag = rejectTag, onClick = onReject)
                }
                // rejected and cancelled both end up in the cancelled tab so the label tells them apart
                if (label != null) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor,
                        modifier = Modifier.padding(horizontal = 6.dp).testTag(labelTag),
                    )
                }
                // message the camper about this stay (US 02.12.01)
                if (onMessage != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    CardAction("Message", Icons.Outlined.Email, tag = "messageEntrant", onClick = onMessage)
                }

            }
        }

    }

    // This component is sandwiched between the tab manager and footer
    @Composable
    private fun Content(state: String, navController: NavController) {
        val reservations = when (state) {
            "Active" -> activeReservations
            "Past" -> pastReservations
            else -> cancelledReservations
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(reservations.size) { reservation ->
                val row = reservations[reservation]
                CampsiteCard(
                    name = row.entrantName.ifBlank { "<unnamed camper>" },
                    campsiteName = row.campsite?.name ?: "<unnamed campsite>",
                    location = row.campsite?.location ?: "<unnamed location>",
                    startDate = row.reservation.checkIn,
                    endDate = row.reservation.checkOut,
                    guests = row.reservation.numGuests,
                    // 0 if the dates were saved wrong instead of crashing
                    nights = runCatching { row.reservation.nights().toInt() }.getOrDefault(0),
                    rejectTag = "rejectReservation-${row.reservation.id}",
                    // only active ones can be rejected (US 02.08.01)
                    onReject = if (state == "Active") ({ toReject = row }) else null,
                    label = when {
                        row.reservation.isRejected() -> "Rejected by you"
                        row.reservation.isCancelled() -> "Cancelled by entrant"
                        else -> null
                    },
                    labelTag = "reservationLabel-${row.reservation.id}",
                    // only active stays can be messaged about
                    onMessage = if (state == "Active") ({
                        messagesController.openConversation(
                            navController,
                            row.reservation.id,
                            row.entrantName.ifBlank { "Camper" },
                        )
                    }) else null,
                )
                if (reservation != reservations.size - 1) {
                    Spacer(
                        modifier = Modifier
                            .height(20.dp)
                    )
                }
            }
        }
    }

    /**
     *  Renders the page of an owner's reservations
     *  @param navController the navigation controller to use
     *
     */
    @Composable
    fun ShowScreen(navController: NavController) {
        var state by remember { mutableStateOf("Active") }
        val activeColor = if (state == "Active") primaryGreenColor else secondaryGreenColor
        val pastColor = if (state == "Past") primaryGreenColor else secondaryGreenColor
        val cancelledColor = if (state == "Cancelled") primaryGreenColor else secondaryGreenColor
        // made on every redraw so the picked tab takes the list colour and the others go lighter
        // see [28] in Citation.md
        val tabs = listOf(
            Tab(name = "Active", color = activeColor, option = {state = "Active"}),
            Tab(name = "Past", color = pastColor, option = {state = "Past"}),
            Tab(name = "Cancelled", color = cancelledColor, option = {state = "Cancelled"}),
        )
        // load every tab when the screen opens so its fresh even if nothing notified the views
        LaunchedEffect(Unit) { reload() }
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Header(
                backgroundColor = ownerHeaderBackgroundColor,
                textColor = ownerHeaderTextColor,
                headerText = "Reservations",
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ownerHeaderBackgroundColor),
            ) {
                Row(modifier = Modifier.height(50.dp)){
                    TabManager(tabs = tabs, state = state)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(primaryGreenColor)
                        .padding(20.dp),
                ) {
                    Content(state = state, navController = navController)
                }
            }

            Footer(
                navController = navController,
                backgroundColor = ownerFooterBackgroundColor,
                textColor = ownerFooterTextColor,
                buttonColor = ownerFooterButtonColor,
            )
        }

        // asks before rejecting since the entrant loses their stay (US 02.08.01)
        toReject?.let { row ->
            AlertDialog(
                onDismissRequest = { toReject = null },
                title = { Text("Reject reservation?") },
                text = {
                    Text(
                        "${row.entrantName.ifBlank { "This camper" }} will lose their stay at " +
                            "${row.campsite?.name ?: "your campsite"} from ${row.reservation.checkIn} " +
                            "to ${row.reservation.checkOut} and get a notification."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        toReject = null
                        // the model notifies the views so update reloads the lists
                        controller.onReservationReject(row) { result ->
                            rejectError = result.exceptionOrNull()?.let { it.message ?: "Couldn't reject the reservation" }
                        }
                    }) { Text("Yes, reject") }
                },
                dismissButton = {
                    TextButton(onClick = { toReject = null }) { Text("Keep it") }
                },
            )
        }

        rejectError?.let { message ->
            AlertDialog(
                onDismissRequest = { rejectError = null },
                title = { Text("Couldn't reject") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { rejectError = null }) { Text("OK") }
                },
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun OwnerReservationsViewPreview() {
    val view = OwnerReservationsView(model = Model())
    view.ShowScreen(rememberNavController())
}