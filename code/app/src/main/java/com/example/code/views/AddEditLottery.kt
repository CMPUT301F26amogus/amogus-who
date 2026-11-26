package com.example.code.views

import View
import Model
import com.example.code.model.Lottery
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.code.controllers.AddEditLotteryController
import com.example.code.ui.theme.CodeTheme

class AddEditLottery(model: Model, val isEdit : Boolean = false) : View(), InputFieldFunctions{

    val controller = AddEditLotteryController(model)

    val fieldModifier = Modifier
        .background(color = creamBG)
        .padding(10.dp)
        .clip(shape = RoundedCornerShape(6.dp))
    var headerName = if (isEdit) { "Edit Lottery" } else { "Add Lottery" }


    /** Display variables
     *      the variable versions of each field within Lottery creation
     * */
    var oldLottery : Lottery? = null
    var lotteryCampsiteID = listOf<String>("")
    var waitListCapacity: Int? = 0  //  can be empty, its optional: US 02.04.07
    var drawDate = ""  // the day winners are decided
    var numberOfWinners = 0  // number of winners a lottery can have
//    var registrationDuration = 0  // the period after winning entrants get to register their bookings?
    // US 02.04.03 As a campsite owner, I want to set a registration period.
    var registrationStart = ""
    var registrationEnd = ""

    /** Update
     *      If this view is an edit view then it will grab the values of the lottery to be edited,
     *      otherwise it will clear the variables to be empty fields to act as an add view
     * */
    override fun update(model: Model){
        if (isEdit){
            oldLottery = model.gettTheLottery()
            //  extract oldLottery variables
            if (oldLottery != null) {
//                lotteryCampsiteID = oldLottery!!.campsiteIds
//                waitListCapacity = oldLottery!!.waitlistCapacity
                // TODO: Lotteries need some variable to represent the date in which it is automatically drawn
//                drawDate = oldLottery!!.drawDate
                registrationStart = oldLottery!!.registrationStart
                registrationEnd = oldLottery!!.registrationEnd
            }
            else {
                clear()
            }
        }
        else { clear() }
    }

    /**
     *  This function renders this page
     *
     *  @param navController the controller that handles navigation
     */
    @Composable
    fun ShowScreen(navController: NavController) {
        val lotteryCampsiteIDState = rememberTextFieldState(lotteryCampsiteID.toString())
        val waitListCapacityState =
            if (waitListCapacity == null) { rememberTextFieldState("0") }
            else  { rememberTextFieldState(waitListCapacity.toString()) }
        val drawDateState = rememberTextFieldState(drawDate)
        val numberOfWinnersState = rememberTextFieldState(numberOfWinners.toString())


        Column(
            modifier = Modifier
        ) {
            Header(
                headerText = headerName,
                backgroundColor = ownerHeaderBackgroundColor,
                textColor = ownerHeaderTextColor,
            )

            Column(  //  body of screen
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .background(primaryGreenColor)
                    .fillMaxHeight()
                    .padding(20.dp),
            ) {
                // campsite input field, by campsite ID
                Row(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(6.dp))
                        .background(light)
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = "TODO: campsites dropdown menu",
                        color = primaryTextColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // winners and waitlist caps set here
                Row(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(6.dp))
                        .background(light)
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    TextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // registration period
                Column(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(6.dp))
                        .background(light)
                        .fillMaxWidth()
                        .padding(10.dp)) {

                    Text(
                        text = "Registration Period",
                        modifier = Modifier.padding(bottom = 5.dp),
                        color = primaryTextColor
                    )

                    Row(
                        modifier = Modifier
                    ) {
                        TextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                }

                Spacer(modifier = Modifier.height(10.dp))

                // booking period
                Column(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(6.dp))
                        .background(light)
                        .fillMaxWidth()
                        .padding(10.dp)) {

                    Text(
                        text = "Booking Period",
                        modifier = Modifier.padding(bottom = 5.dp),
                        color = primaryTextColor
                    )

                    Row(
                        modifier = Modifier
                    ) {
                        TextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                }

                Spacer(modifier = Modifier.weight(1f))

                //  buttons for cancelling or saving changes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    ExtendedFloatingActionButton(
                        containerColor = light,
                        contentColor = primaryTextColor,
                        shape = CircleShape,
                        onClick = {
                            clear()
                            controller.onCancel(navController)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Cancel"
                            )
                        },
                        text = {
                            Text("Cancel")
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    FloatingActionButton(
                        containerColor = dangerousActionColor,
                        contentColor = primaryTextColor,
                        shape = CircleShape,
                        onClick = {
//                            controller.onSave()
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Register Lottery",
                            )
                        },
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    ExtendedFloatingActionButton(
                        containerColor = secondaryGreenColor,
                        contentColor = primaryGreenTextColor,
                        shape = CircleShape,
                        onClick = {
//                            controller.onSave()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Save Lottery",
                            )
                        },
                        text = {
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold
                            )
                        },
                    )
                }
            }  //  end of body of screen

            Footer(
                textColor = ownerFooterTextColor,
                backgroundColor = ownerFooterBackgroundColor,
                buttonColor = ownerFooterButtonColor,
                navController = navController
            )
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
            lotteryCampsiteID = listOf("")
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
