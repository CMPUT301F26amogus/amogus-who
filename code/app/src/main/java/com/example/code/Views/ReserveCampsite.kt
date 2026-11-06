package com.example.code.Views

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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.Model.Campsite
import java.util.Date

class ReserveCampsite : View(), InputFieldFunctions {

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

    var cID = 0
    var name = "Campsite Name"
    var park = "Park"
    var details = "Toilets, plug-ins etc"
    var tos = "Don't feed animals, etc"
    var rating = 4.9

    override fun update(model: Model) {
//        val c = model.getCampsiteToDisplay()
//        campsiteID = c.
//        campsiteName = c.
//        campsitePark = c.
//        campsiteDetails = c.
//        campsiteTOS = c.
    }

    @Composable
    public fun ShowScreen() {

        val arrivalDateState = TextFieldState("")
        val departureDateState = TextFieldState("")
        val numberOfGuestsState = TextFieldState("")

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
                            Box(
                                modifier = Modifier
                                    .size(width = 140.dp, height = 60.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 30.sp,
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
                                text = rating.toString(),
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

                Button(
                    onClick = {
                        if (
                            !arrivalDateState.text.isBlank() ||
                            !departureDateState.text.isBlank() ||
                            !numberOfGuestsState.text.isBlank()
                            ) {
//                            val arrivalDate: Date = stringToDate(arrivalDateState.text.toString())
//                            val departureDate: Date = stringToDate(departureDateState.text.toString())
                            val numberOfGuests = numberOfGuestsState.text.toString().toInt()

                            val arrivalDate = arrivalDateState.text.toString()
                            val departureDate = departureDateState.text.toString()

                            //  call controller.makeReservation() with these fields and pop novController
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
                )
            }
        }

    }  // end of show screen body

}  // end of class body