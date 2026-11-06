package com.example.code.Views

import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import com.example.code.Model.Campsite
import java.sql.Date

class AddEditCampsite : View() {
    val bgColor = secondaryGreenColor
    val headerbgColor = ownerHeaderBackgroundColor
    val headerTextColor = ownerHeaderTextColor
    val footerTextColor = ownerFooterBackgroundColor
    val footerbgColor = ownerFooterButtonColor
    val footerButtonColor = ownerFooterTextColor



    @Composable
    public fun ShowScreen(oldCampsite: Campsite? = null) {
        var campsiteName by remember {
            mutableStateOf("")
        }
        var parkName by remember {
            mutableStateOf("")
        }

        var campDescription by remember {
            mutableStateOf("")
        }

        var address by remember {
            mutableStateOf("")
        }

        var maxGuests by remember {
            mutableStateOf("")
        }

        var pricePerNight by remember {
            mutableStateOf("")
        }

        var reservationPeriodStart by remember {
            mutableStateOf(value = Date(2026, 1, 1))
        }

        var reservationPeriodEnd by remember {
            mutableStateOf(value = Date(2026, 1, 1))
        }

        var tags by remember {
            mutableStateOf("")
        }

//        if (oldCampsite != null){
//            // update values
//        }

        Column() {
            Row() {

                Header(
                    "Edit Campsite",
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

                Spacer(modifier = Modifier.height(10.dp))

                // park / area name input field
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

                Spacer(modifier = Modifier.height(10.dp))

                // description input field
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
                ) // end of description input

                Spacer(modifier = Modifier.height(10.dp))

                // address input field
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

                Spacer(modifier = Modifier.height(10.dp))

                // max Guests input field
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

                // price per night input field
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

                // price per night input field
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

                // cancel and save changes
                Row() {
                    Button(
                        onClick = {
//                             if ( oldCampsite != null) {
//                                val newCampsite = Campsite (
//                                )
//                                  maxGuests
//
//                            }
                        }
                    ){

                    }
                }

            }
        }
    }

    fun checkCampsite(campId: Int){
        // getCampsite(campId)

    }

}