package com.example.code.Views

import Campsite
import Model
import View
import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import java.sql.Date

class AddEditCampsite : View() {
    val bgColor = secondaryGreenColor
    val headerbgColor = ownerHeaderBackgroundColor
    val headerTextColor = ownerHeaderTextColor
    val footerTextColor = ownerFooterBackgroundColor
    val footerbgColor = ownerFooterButtonColor
    val footerButtonColor = ownerFooterTextColor

    var oldCampsite : Campsite? = null
    var campsiteName : String = ""
    var parkName : String = ""

    var campDescription : String = ""

    var address : String = ""

    var maxGuests : String = ""

    var pricePerNight : String = ""

    var bookingPeriodStart : String = ""

    var bookingPeriodEnd : String = ""

    var tags : String = ""

    var headerName = ""

    override fun update(model: Model) {
//        val tempCampsite = model.getCampsiteToDisplay()
//        if (tempCampsite != null){
//            oldCampsite = tempCampsite
//            campsiteName = tempCampsite.campName
//            parkName = tempCampsite.location
//            campDescription = tempCampsite.desc
//            address = tempCampsite.location
//            maxGuests = tempCampsite.maxOcc.toString()
//            pricePerNight = tempCampsite.price.toString()
//            bookingPeriodStart = //  can a campsite be listed without a lottery / booking period?
//            bookingPeriodEnd =
//            headerName = "Edit Campsite"
//        }
//        else {
//            campsiteName = ""
//             parkName = ""
//            campDescription = ""
//            address = ""
//            maxGuests = ""
//            pricePerNight = ""
//            bookingPeriodStart = ""
//            bookingPeriodEnd = ""
//            tags = ""
//            headerName = "Register Campsite"
//        }
    }

    @Composable
    public fun ShowScreen() {

        Column() {
            Row() {
                Header(
                    headerText = headerName,
                    backgroundColor = headerbgColor,
                    textColor = headerTextColor,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .background(bgColor),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Campsite name input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = campsiteName,
                        onValueChange = {
                            campsiteName = it
                        },
                        label = {
                            Text("Campsite Name")
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(color = creamBG)
                            .padding(20.dp).width(290.dp)
                    ) // end of camp name input
                }

                Spacer(modifier = Modifier.height(10.dp))

                // park / area name input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = parkName,
                        onValueChange = {
                            parkName = it
                        },
                        label = {
                            Text("Park / Area")
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(color = creamBG)
                            .padding(20.dp).width(290.dp)
                    ) // end of park / area name input
                }

                Spacer(modifier = Modifier.height(10.dp))

                // description input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = campDescription,
                        onValueChange = {
                            campDescription = it
                        },
                        label = {
                            Text("Description")
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(color = creamBG)
                            .padding(20.dp).width(290.dp)
                    )
                } // end of description input

                Spacer(modifier = Modifier.height(10.dp))

                // address input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = address,
                        onValueChange = {
                            address = it
                        },
                        label = {
                            Text("Address")
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(color = creamBG)
                            .padding(20.dp).width(290.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // max Guests input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = maxGuests,
                        onValueChange = {
                            maxGuests = it
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        label = {
                            Text("Max Guests")
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(color = creamBG)
                            .padding(20.dp).width(290.dp)
                    )
                }

                // price per night input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = pricePerNight,
                        onValueChange = {
                            pricePerNight = it
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        label = {
                            Text("Max Guests")
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(color = creamBG)
                            .padding(20.dp).width(290.dp)
                    )
                }

                // price per night input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val maxGuestsState = rememberTextFieldState("")
                    TextField(
                        state = maxGuestsState,
                        label = {
                            Text("Max Guests")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        inputTransformation = InputTransformation.maxLength(2).then {
                            if (!asCharSequence().isDigitsOnly()) {
                                revertAllChanges()
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(color = creamBG)
                            .padding(20.dp).width(290.dp)
                    )
                }

                // bookings open and close date input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val periodStartState = rememberTextFieldState("")
                    TextField(
                        state = periodStartState,
                        label = {
                            Text("Bookings Open")
                        },
                        placeholder = {
                            Text("Mo/Da/Year")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        inputTransformation = InputTransformation.maxLength(8).then {
                            if (!asCharSequence().isDigitsOnly()) {
                                revertAllChanges()
                            }
                        },
                        outputTransformation = OutputTransformation {
                            if (length > 2) insert(2, "/")
                            if (length > 5) insert(5, "/")
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(color = creamBG)
                            .padding(20.dp).width(290.dp)
                    )
                    bookingPeriodStart = periodStartState.text.toString()
                }

                // bookings open and close date input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val periodEndState = rememberTextFieldState("")
                    TextField(
                        state = periodEndState,
                        label = {
                            Text("Bookings Close")
                        },
                        placeholder = {
                            Text("Mo/Da/Year")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        inputTransformation = InputTransformation.maxLength(8).then {
                            if (!asCharSequence().isDigitsOnly()) {
                                revertAllChanges()
                            }
                        },
                        outputTransformation = OutputTransformation {
                            if (length > 2) insert(2, "/")
                            if (length > 5) insert(5, "/")
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(color = creamBG)
                            .padding(20.dp).width(290.dp)
                    )
                    bookingPeriodEnd = periodEndState.text.toString()
                }

                // cancel and save changes
                Row() {
                    Button(
                        onClick = {
                            // call Controller to navController to pop
                        }
                    ){
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
//                            val newCampsite = Campsite(
//                                // all the variables (convert to Int and Floats as needed)
//                            )
//                            if ( oldCampsite != null) {  //  we are editing a campsite
//                                //  call Controller to call editCampsite()
//                            }
//                            else {  //  we are registering a brand-new campsite
//                                //  call Controller to call newCampsite()
//                            }
                            // call Controller to navController to pop
                        }
                    ){
                        Text("Save Changes")
                    }
                }

            }
        }
    }

}