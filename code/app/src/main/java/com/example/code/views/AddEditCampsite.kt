package com.example.code.views

import Model
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.code.controllers.OwnerEditCampsiteController
import com.example.code.model.Campsite
import com.example.code.ui.theme.CodeTheme

class AddEditCampsite (model: Model, val isEdit: Boolean = false): View(), InputFieldFunctions {

    val controller = OwnerEditCampsiteController(model)

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
        val tempCampsite = model.gettTempCampsite()
        oldCampsite = tempCampsite
        if (tempCampsite != null && isEdit){
            oldCampsite = tempCampsite
            campsiteName = tempCampsite.name
            campDescription = tempCampsite.description
            parkName = tempCampsite.location
            maxGuests = tempCampsite.maxOccupancy.toString()
            pricePerNight = tempCampsite.pricePerNight.toString()
        }
        else {
            clear()
        }
    }

    @Composable
    public fun ShowScreen(navController: NavController) {

        val campsiteNameState = TextFieldState(campsiteName)
        val parkNameState = TextFieldState(parkName)
        val campDescriptionState = TextFieldState(campDescription)
        val addressState = TextFieldState(address)
        val maxGuestsState = TextFieldState(maxGuests)
        val pricePerNightState = TextFieldState(pricePerNight)
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

                            if ( oldCampsite != null) {  //  we are editing a campsite
                                //  call Controller to call editCampsite()
                                controller.onEditCampsite(
                                    navController,
                                    oldCampsite!!,
                                    newName = campsiteNameState.text.toString(),
                                    newLocation = addressState.text.toString(),
                                    newDescription = campDescriptionState.text.toString(),
                                    newMaxOccupants = maxGuestsState.text.toString().toInt(),
                                    newPrice = pricePerNightState.text.toString().toDouble()
                                    )
                            }
                            else {  //  we are registering a brand-new campsite
                                //  call Controller to call newCampsite()
                                val newCampsite = Campsite(
                                    id = "",
                                    ownerId = "",
                                    name = campsiteNameState.text.toString(),
                                    location = addressState.text.toString(),
                                    description = campDescriptionState.text.toString(),
                                    maxOccupancy = maxGuestsState.text.toString().toInt(),
                                    pricePerNight = pricePerNightState.text.toString().toDouble(),
                                    )
                                controller.onAddCampsite(navController, newCampsite)
                            }
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
                    navController = navController
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
}

@Preview(showBackground = true)
@Composable
fun AddCampsiteViewPreview() {
    val m = Model()
    val mp = AddEditCampsite(m, false)
    CodeTheme {
        (
                mp.ShowScreen(rememberNavController())
                )
    }
}
