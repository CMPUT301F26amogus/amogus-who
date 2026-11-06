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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import java.sql.Date

class AddEditCampsite (val isEdit: Boolean): View(), InputFieldFunctions {
    val bgColor = secondaryGreenColor
    val headerbgColor = ownerHeaderBackgroundColor
    val headerTextColor = ownerHeaderTextColor
    val footerTextColor = ownerFooterBackgroundColor
    val footerBackgroundColor = ownerFooterButtonColor
    val footerButtonColor = ownerFooterTextColor
    val fieldModifier = Modifier
        .padding(horizontal = 20.dp)
        .background(color = creamBG)
        .padding(20.dp).width(290.dp)
    val fieldModifierDate = Modifier
        .background(color = creamBG)
        .padding(15.dp).width(135.dp)

    var oldCampsite : Campsite? = null
    var campsiteName : String = ""
    var parkName : String = ""

    var campDescription : String = ""

    var address : String = ""

    var maxGuests : String = ""

    var pricePerNight : String = ""

    var bookingPeriodStart : String = ""

    var bookingPeriodEnd : String = ""

//    var tags : String = ""

    var headerName = if (isEdit) {"Edit Campsite"} else { "Add Campsite" }

    override fun update(model: Model) {
//        val tempCampsite : Campsite? = model.getCampsiteToDisplay()
        val tempCampsite : Campsite? = Campsite(
            0,
            0,
            "John Park",
            "Banff",
            "back country",
            10,
            11.0,
            4.9
            )
        if (tempCampsite != null && isEdit){
            oldCampsite = tempCampsite
            campsiteName = tempCampsite.campName
            campDescription = tempCampsite.desc
            parkName = tempCampsite.location
            campDescription = tempCampsite.desc
            address = tempCampsite.location
            maxGuests = tempCampsite.maxOcc.toString()
            pricePerNight = tempCampsite.price.toString()
//            bookingPeriodStart = tempCampsite.
//            bookingPeriodEnd =
        }
        else {
            clear()
//            bookingPeriodStart = ""
//            bookingPeriodEnd = ""
//            tags = ""
        }
    }

    @Composable
    public fun ShowScreen() {

        val campsiteNameState = TextFieldState(campsiteName)
        val parkNameState = TextFieldState(parkName)
        val campDescriptionState = TextFieldState(campDescription)
        val addressState = TextFieldState(address)
        val maxGuestsState = TextFieldState("")
        val pricePerNightState = TextFieldState("")
        val bookingPeriodStartState = TextFieldState("")
        val bookingPeriodEndState = TextFieldState("")

        Column() {
            Row() {
                Header(
                    headerText = headerName,
                    backgroundColor = headerbgColor,
                    textColor = headerTextColor,
                )
            }
            Column (
                Modifier
                .fillMaxWidth()
                .weight(1f).background(secondaryGreenColor)
            ) {
                // Campsite name input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StringField(
                        campsiteNameState,
                        "Campsite Name",
                        fieldModifier
                    )
                }

                // park / area name input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StringField(
                        parkNameState,
                        "Park / Area",
                        fieldModifier
                    )
                     // end of park / area name input
                }

                // description input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StringField(
                        campDescriptionState,
                        "Description",
                        fieldModifier
                    )
                } // end of description input

                Spacer(modifier = Modifier.height(10.dp))

                // address input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StringField(
                        addressState,
                        "Address",
                        fieldModifier
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // max Guests input field
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(20.dp))

                    VariableDigitIntField(
                        maxGuestsState,
                        "Max Guests",
                        2,
                        fieldModifierDate
                    )

                // price per night input field
                    VariableDigitIntField(
                        pricePerNightState,
                        "Price Per Night",
                        4,
                        fieldModifierDate
                    )
                }

                // bookings open and close date input field
                Row(
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Spacer(modifier = Modifier.width(20.dp))
                    Column() {
                        DateField(
                            bookingPeriodStartState,
                            "Bookings Open",
                            fieldModifierDate
                        )
                    }

                    Column() {
                        DateField(
                            bookingPeriodEndState,
                            "Bookings End",
                            fieldModifierDate
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // cancel and save changes
                Row() {
                    Button(
                        onClick = {
                            clear()
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(15.dp)
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
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(15.dp)
                    ){
                        Text("Save Changes")
                    }
                }

            }
            Row() {  //  Footer of screen
                Footer(
                    textColor = footerTextColor,
                    backgroundColor = footerBackgroundColor,
                    buttonColor = footerButtonColor,
                )
            }
        }
    }

    fun clear() {
        campsiteName = ""
        parkName = ""
        campDescription = ""
        address = ""
        maxGuests = ""
        pricePerNight = ""
    }

    @Composable
    fun AddCampsitePreview() {
        val aView = AddEditCampsite(false)
        aView.ShowScreen()
    }

    @Composable
    fun EditCampsitePreview() {
        val aView = AddEditCampsite(true)
        aView.ShowScreen()
    }

}