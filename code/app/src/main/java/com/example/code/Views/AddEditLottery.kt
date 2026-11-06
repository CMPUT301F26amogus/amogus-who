package com.example.code.Views

import Lottery
import View
import Model
import android.icu.util.Output
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import java.util.Date
import kotlin.text.toInt

class AddEditLottery(val isEdit : Boolean) : View(), InputFieldFunctions{

    val bgColor = secondaryGreenColor
    val headerBackgroundColor = ownerHeaderBackgroundColor
    val headerTextColor = ownerHeaderTextColor
    val footerTextColor = ownerFooterBackgroundColor
    val footerBackgroundColor = ownerFooterButtonColor
    val footerButtonColor = ownerFooterTextColor
    val fieldModifier = Modifier
        .padding(horizontal = 20.dp)
        .background(color = creamBG)
        .padding(20.dp).width(290.dp)
    var headerName = if (isEdit) { "Edit Lottery" } else { "Add Lottery" }


    var oldLottery : Lottery? = null
    var lotteryCampsiteID = 0
    var waitListCapacity = 0  //  can be empty, its optional: US 02.04.07
    var drawDate = ""  // the day winners are decided
    var numberOfWinners = 0  // number of winners a lottery can have
//    var registrationDuration = 0  // the period after winning entrants get to register their bookings?
    // US 02.04.03 As a campsite owner, I want to set a registration period.

    override fun update(model: Model){
        if (isEdit){
//            oldLottery = model.getLotteryToDisplay()
            //  extract oldLottery variables
            if (oldLottery != null) {
                val date: Date = this.oldLottery!!.bookStart
                drawDate = dateToString(date)
            }
        }
        else { clear() }
    }

    @Composable
    public fun ShowScreen() {
        val lotteryCampsiteIDState = rememberTextFieldState("")
        val waitListCapacityState = rememberTextFieldState("")
        val drawDateState = rememberTextFieldState("")
        val numberOfWinnersState = rememberTextFieldState("")


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
                // campsite input field, by campsite ID
                Row() {
                    VariableDigitIntField(
                        lotteryCampsiteIDState,
                        "Campsite ID",
                        7,
                        fieldModifier
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                //  max number of people allowed in waitlist input field
                Row() {
                    VariableDigitIntField(
                        waitListCapacityState,
                        "Max Entrants in Waitlist",
                        5,
                        fieldModifier
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                //  date draw opens input field
                Row() {
                    DateField(
                        drawDateState,
                        "Draw Opens",
                        fieldModifier
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                //  number of winners in a lottery input field
                Row() {
                    VariableDigitIntField(
                        numberOfWinnersState,
                        "Draw Duration (days)",
                        3,
                        fieldModifier
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                //  buttons for cancelling or saving changes
                Row() {
                    Button(
                        onClick = {
                            clear()
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
            }  //  end of body of screen
            Row() {  //  Footer of screen
                Footer(
                    textColor = footerTextColor,
                    backgroundColor = footerBackgroundColor,
                    buttonColor = footerButtonColor,
                )
            }
        }
    }

    /**clear
     *
     * resets fields when cancel is pressed
     * */
    fun clear() {
        if (oldLottery == null) {
            // extract old lottery fields
        }
        else {
            lotteryCampsiteID = 0
            waitListCapacity = 0
            drawDate = ""
            numberOfWinners = 0
        }
    }



    @Composable
    public fun EditLotteryPreview() {
        val aView = AddEditLottery(true)
        aView.ShowScreen()
    }
    @Composable
    public fun AddLotteryPreview() {
        val aView = AddEditLottery(false)
        aView.ShowScreen()
    }

}