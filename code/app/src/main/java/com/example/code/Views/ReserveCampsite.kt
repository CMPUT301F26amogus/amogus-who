package com.example.code.Views

import View
import Model
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    val contentBackground = secondaryGreenColor
    val headerName = "Reserve Site"

    val fieldModifierDate = Modifier
        .background(color = creamBG)
        .padding(8.dp).width(135.dp)

    var cID = 0
    var name = ""
    var park = ""
    var details = ""
    var tos = ""
    var rating = 0.0

    override fun update(model: Model) {
//        c = model.getCampsiteToDisplay()
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
                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(color = contentBackground)
                        .padding(20.dp).width(290.dp),
                ) {
                    Row {  //  box with campsite name, location, and rating
                        Column {
                            Row {
                                Text(
                                    text = name,
                                    fontSize = 30.sp
                                )
                            }
                            Row {
                                Text (
                                    text = park,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Row {
                            Text (
                                "Rating: $rating"
                            )
                        }
                    }
                }

                Spacer(Modifier.padding(20.dp))

                Box (
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(color = contentBackground)
                        .padding(20.dp).width(290.dp),
                ) {
                    Row {
                        DateField(
                            arrivalDateState,
                            "Arrival Date",
                            fieldModifierDate
                        )

                        VerticalDivider()

                        DateField(
                            departureDateState,
                            "Departure Date",
                            fieldModifierDate
                        )
                    }
                }

                Spacer(Modifier.padding(20.dp))

                Row {
                    VariableDigitIntField(
                        numberOfGuestsState,
                        "Number of Guests",
                        2,
                        fieldModifierDate
                    )
                }

                Text(
                    text = details,
                    fontSize = 14.sp
                )

                Spacer(Modifier.padding(20.dp))

                Text(
                    text = tos,
                    fontSize = 14.sp
                )

                Spacer(Modifier.padding(20.dp))

                Button(
                    onClick = {
                        val arrivalDate : Date = stringToDate(arrivalDateState .text.toString())
                        val departureDate: Date = stringToDate(departureDateState.text.toString())
                        val numberOfGuests = numberOfGuestsState.text.toString().toInt()

                        //  call controller.makeReservation() with these fields and pop novController
                    }
                ) {
                    Text(" Make Reservation")
                }
            }
        }

    }  // end of show screen body

}  // end of class body