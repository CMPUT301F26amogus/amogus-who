package com.example.code.views

import View
import Model
import com.example.code.model.Lottery
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.code.ui.theme.CodeTheme

class AddEditLottery(model: Model, val isEdit : Boolean = false) : View(), InputFieldFunctions{

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
    var registrationStart = ""
    var registrationEnd = ""

    override fun update(model: Model){
        if (isEdit){
//            oldLottery = model.getLotteryToDisplay()
            //  extract oldLottery variables
            if (oldLottery != null) {
                registrationStart = oldLottery!!.registrationStart
                registrationEnd = oldLottery!!.registrationEnd
            }
        }
        else { clear() }
    }

    @Composable
    public fun ShowScreen(navController: NavController) {
        val lotteryCampsiteIDState = rememberTextFieldState(lotteryCampsiteID.toString())
        val waitListCapacityState = rememberTextFieldState(waitListCapacity.toString())
        val drawDateState = rememberTextFieldState(drawDate)
        val numberOfWinnersState = rememberTextFieldState(numberOfWinners.toString())


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
                    navController = navController
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

}


@Preview(showBackground = true)
@Composable
fun AddLotteryViewPreview() {
    val m = Model()
    val mp = AddEditLottery(m, false)
    CodeTheme {
        (
                mp.ShowScreen(rememberNavController())
                )
    }
}
