package com.example.code.views

import Model
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Tab
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.controllers.OwnerReservationsController
import com.example.code.model.Reservation

/**
 *  OwnerReservationsView: the view for the owner to see reservations at their campsites
 *
 */
class OwnerReservationsView(model : Model) : View() {

    val controller by lazy { OwnerReservationsController(model = model) }
    val reservations = mutableStateListOf<Reservation>()

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
                    text = startDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing / 2))
                VerticalDivider(modifier = Modifier)
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing / 2))
                Text(
                    text = endDate,
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
    @Composable
    private fun CampsiteCard(
        name: String,
        campsiteName: String,
        location: String,
        startDate: String,
        endDate: String,
        guests: Int,
        nights: Int,
        state: String,
    ) {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .background(color = cardPrimary)
                .fillMaxWidth()
                .height(150.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .weight(1f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                            text = "$campsiteName - $location",
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
                    .height(50.dp)
                    .background(color = cardAccent)
                    .padding(10.dp),
            ) {
                if (state == "ACTIVE") {
                    ExtendedFloatingActionButton(
                        onClick = { controller.onReservationReject() },
                        shape = CircleShape,
                        containerColor = cardAccent,
                        contentColor = primaryTextColor,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Reject",
                            )
                        },
                        text = { Text("Reject") }
                    )
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    ExtendedFloatingActionButton(
                        onClick = { controller.onReservationAccept() },
                        shape = CircleShape,
                        containerColor = cardAccent,
                        contentColor = primaryTextColor,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Accept",
                            )
                        },
                        text = { Text("Accept") }
                    )

                }

            }
        }

    }

    // This component is sandwiched between the tab manager and footer
    @Composable
    private fun Content(state: String) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(reservations.size) { reservation ->
                CampsiteCard(
                    name = reservations[reservation].entrant?.name ?: "<unnamed camper>",
                    campsiteName = reservations[reservation].campsite?.name ?: "<unnamed campsite>",
                    location = reservations[reservation].campsite?.location ?: "<unnamed location>",
                    startDate = reservations[reservation].checkIn,
                    endDate = reservations[reservation].checkOut,
                    guests = reservations[reservation].guests,
                    nights = 0,     // TODO: figure out how to math endDate - startDate
                    state = state,
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
        val tabs = remember {
            mutableStateListOf(
                Tab(name = "Active", color = activeColor, option = {state = "Active"}),
                Tab(name = "Past", color = pastColor, option = {state = "Past"}),
                Tab(name = "Cancelled", color = cancelledColor, option = {state = "Cancelled"}),
            )
        }
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
                    Content(state = state)
                }
            }

            Footer(
                navController = navController,
                backgroundColor = ownerFooterBackgroundColor,
                textColor = ownerFooterTextColor,
                buttonColor = ownerFooterButtonColor,
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