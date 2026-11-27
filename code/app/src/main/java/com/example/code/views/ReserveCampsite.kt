package com.example.code.views

import View
import Model
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.code.Routes
import com.example.code.controllers.ReserveCampsiteController
import com.example.code.model.Campsite
import com.example.code.model.EntrantReservation

/**
 * Screen where an entrant picks dates and guests and books the campsite in the model's temp campsite.
 * Shows a confirmation with a CSV download once it's booked (US 01.05.01, 01.05.02).
 *
 * @param model the app's model
 */
class ReserveCampsite(model: Model) : View(), InputFieldFunctions {

    val controller = ReserveCampsiteController(model)
    // state so the screen redraws when update() gives it the campsite
    var campsite by mutableStateOf<Campsite?>(model.gettTempCampsite())

    val bgColor = secondaryGreenColor
    val headerBackgroundColor = entrantHeaderBackgroundColor
    val headerTextColor = entrantHeaderTextColor
    val footerTextColor = entrantFooterBackgroundColor
    val footerBackgroundColor = entrantFooterButtonColor
    val footerButtonColor = entrantFooterTextColor
    val contentBackground = creamBG
    val headerName = "Reserve Site"

    val fieldModifierDate = Modifier
        .background(color = creamBG)
        .padding(8.dp).width(135.dp)

    // what the boxes show, read from the campsite so they stay in sync with the model
    val name get() = campsite?.name ?: "Campsite Name"
    val park get() = campsite?.location ?: ""
    val details get() = campsite?.amenities?.joinToString(", ") ?: ""
    val tos get() = campsite?.rulesAndPolicy ?: ""
    // a dash when nobody rated it yet since the box is too narrow for no ratings yet
    val rating get() = campsite?.averageRating?.let { "%.1f".format(it) } ?: "-"

    // shows 2027-05-01 as 05/01/27 like the reservation cards and leaves anything it cant read as it is
    private fun formatDate(date: String): String =
        runCatching { LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MM/dd/yy")) }
            .getOrDefault(date)

    /**
     * gets the campsite being reserved from the model
     *
     * @param model the model, passed in when it notifies its views
     */
    override fun update(model: Model) {
        campsite = model.gettTempCampsite()
    }

    @Composable
    public fun ShowScreen(navController: NavController) {

        // remembered so typing isnt lost when the screen redraws
        val arrivalDateState = rememberTextFieldState("")
        val departureDateState = rememberTextFieldState("")
        val numberOfGuestsState = rememberTextFieldState("")
        // why the last try didnt work or null
        var error by remember { mutableStateOf<String?>(null) }
        // the reservation that was just made so the confirmation popup can show (US 01.05.01)
        var confirmed by remember { mutableStateOf<EntrantReservation?>(null) }
        val downloadCsv = rememberCsvDownloader { error = it }

        Column() {
            Row() {  //  Header of screen
                Header(
                    headerText = headerName,
                    backgroundColor = headerBackgroundColor,
                    textColor = headerTextColor,
                )
            }
            Column(  //  body of screen
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .background(bgColor),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.padding(15.dp))

                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(color = contentBackground)
                        .padding(20.dp)
                        .width(320.dp),
                ) {
                    Row {  //  box with campsite name, location, and rating
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(vertical = 10.dp)
                        ) {
                            // fixed width but not height so a long name wraps instead of getting cut off
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 22.sp,
                                    lineHeight = 26.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("campsiteNameBox")
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = park,
                                    fontSize = 20.sp,
                                    modifier = Modifier.testTag("parkAreaBox")
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                        ) {
                            Text("Rating: ")
                            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                            Text(
                                text = rating,
                                modifier = Modifier.testTag("ratingBox"))
                        }
                    }
                }


                Spacer(Modifier.padding(8.dp))

                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(color = contentBackground)
                        .padding(20.dp).width(320.dp),
                ) {
                    Row {
                        //  arrival date input
                        DateField(
                            arrivalDateState,
                            "Arrival Date",
                            fieldModifierDate
                                .testTag("arrivalDateInputField")
                        )

                        //  departure date input
                        DateField(
                            departureDateState,
                            "Departure Date",
                            fieldModifierDate
                                .testTag("departureDateInputField")
                        )
                    }
                }

                Spacer(Modifier.padding(8.dp))

                Row {
                    //  number of guests input field
                    VariableDigitIntField(
                        numberOfGuestsState,
                        "Number of Guests",
                        2,
                        fieldModifierDate
                            .testTag("numberOfGuestsInputField")
                    )

                    Spacer(modifier = Modifier
                        .padding(horizontal = 8.dp))

                    Box(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(8.dp))
                            .background(color = contentBackground)
                            .padding(20.dp).width(160.dp).height(80.dp)
                    ) {
                        Column {
                            Text("Details: ")
                            Text(
                                text = details,
                                fontSize = 14.sp,
                                modifier = Modifier.testTag("detailsBox")
                            )
                        }
                    }
                }

                Spacer(Modifier.padding(8.dp))

                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(color = contentBackground)
                        .padding(20.dp).width(290.dp)
                ) {
                    Column {
                        Text("TOS: ")
                        Text(
                            text = tos,
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("tosBox")
                        )
                    }

                }

                Spacer(Modifier.padding(8.dp))

                error?.let {
                    Text(it, color = primaryGreenTextColor, modifier = Modifier.testTag("reserveError"))
                }

                Button(
                    onClick = {
                        // the controller checks the fields so the view just passes what was typed
                        controller.makeReservation(
                            campsite = campsite,
                            arrival = arrivalDateState.text.toString(),
                            departure = departureDateState.text.toString(),
                            guests = numberOfGuestsState.text.toString(),
                        ) { result ->
                            result
                                .onSuccess {
                                    error = null
                                    confirmed = it
                                }
                                .onFailure { error = it.message ?: "Couldn't make the reservation" }
                        }
                    },
                    modifier = Modifier.testTag("reserveCampsiteButton")
                ) {
                    Text(" Make Reservation")
                }
                Footer(
                    textColor = entrantFooterTextColor,
                    backgroundColor = entrantFooterBackgroundColor,
                    buttonColor = entrantFooterButtonColor,
                    navController = navController
                )
            }
        }

        // confirms the booking and offers the csv (US 01.05.01 01.05.02)
        // the notification only goes out if they havent turned notifications off
        confirmed?.let { done ->
            AlertDialog(
                onDismissRequest = {},
                modifier = Modifier.testTag("reservationConfirmation"),
                title = { Text("Reservation confirmed") },
                text = {
                    Text(
                        "Your stay at ${done.campsite?.name ?: "the campsite"} from " +
                            "${formatDate(done.reservation.checkIn)} to ${formatDate(done.reservation.checkOut)} " +
                            "for ${done.reservation.numGuests} guest(s) is booked. " +
                            "You'll get a notification if they're turned on, or you can download a CSV copy."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            downloadCsv(done.csvFileName(), controller.reservationCsv(done))
                        },
                        modifier = Modifier.testTag("downloadReservationCsv"),
                    ) { Text("Download CSV") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        confirmed = null
                        navController.navigate(Routes.RESERVATIONS)
                    }) { Text("Done") }
                },
            )
        }

    }  // end of show screen body

}  // end of class body