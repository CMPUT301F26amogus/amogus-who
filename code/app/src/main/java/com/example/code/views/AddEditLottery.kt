package com.example.code.views

import android.app.DatePickerDialog
import View
import Model
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.code.controllers.AddEditLotteryController
import com.example.code.ui.theme.CodeTheme
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Locale

class AddEditLottery(model: Model, val isEdit : Boolean = false) : View(), InputFieldFunctions{

    val controller = AddEditLotteryController(model)
    private val availableCampsites = model.campsiteList

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

    /*
     * Function implemented by GPT-6 Luna (Github Copilot) to create the component
     * 
     * "Use the screenshot as reference. I want to make a campsite dropdown menu as shown in the photo, 
     *  and it should be implemented in CampsiteSelect(). Don't provide any functionality outside of 
     *  making this component work as a user would expect to interact with it (only front-end 
     *  functionality). Expect a list of campsite objects, and display the name and location of the 
     *  campsite as found in the Campsite class. 
     * 
     *  Ask me first about any further assumptions."
     * 
     * 2026-11-26
     */
    @Composable
    private fun CampsiteSelect(campsites: List<Campsite>) {
        var expanded by remember { mutableStateOf(false) }
        val selectedCampsiteIds = remember { mutableStateListOf<String>() }
        val fieldColor = tertiaryPurple
        val dividerColor = primaryTextColor.copy(alpha = 0.55f)
        val visibleCampsites = if (expanded) {
            campsites
        } else {
            campsites.filter { it.id in selectedCampsiteIds }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(fieldColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCampsiteIds.isEmpty()) {
                        "Select Campsite(s)*"
                    } else {
                        "Selected Campsite(s)"
                    },
                    modifier = Modifier.weight(1f),
                    color = primaryTextColor
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = if (expanded) {
                        "Collapse campsite options"
                    } else {
                        "Expand campsite options"
                    },
                    modifier = Modifier.rotate(if (expanded) 180f else 0f),
                    tint = primaryTextColor
                )
            }

            if (visibleCampsites.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    visibleCampsites.forEach { campsite ->
                        val isSelected = campsite.id in selectedCampsiteIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Checkbox) {
                                    if (isSelected) {
                                        selectedCampsiteIds.remove(campsite.id)
                                    } else {
                                        selectedCampsiteIds.add(campsite.id)
                                    }
                                }
                                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = campsite.name,
                                    color = primaryTextColor
                                )
                                if (campsite.location.isNotBlank()) {
                                    Text(
                                        text = campsite.location,
                                        color = primaryTextColor,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (isSelected) {
                                        selectedCampsiteIds.remove(campsite.id)
                                    } else {
                                        selectedCampsiteIds.add(campsite.id)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = primaryTextColor,
                                    uncheckedColor = primaryTextColor,
                                    checkmarkColor = fieldColor
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(dividerColor)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun WinnersAndWaitlistCaps() {
        Row {
            TextField(
                value = "Max winners*", // required field
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                value = "Waitlist",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    /*
     * This function was implemented by GPT-6 Luna to create the component
     * 
     * "So far the only change I made was changing the field colour to one defined previously by a 
     *  previous dev. Referring to the screenshot again, please implement Registration Period, where 
     *  selecting the field should prompt the user to select from a date picker or calendar, whatever 
     *  is supported on Android. The field should be in the format YYYY-MM-DD, such as 2027-05-21. 
     *  Only handle the front-end."
     * 
     * 2026-11-26
     */
    @Composable
    private fun RegistrationPeriod() {
        var startDate by remember(registrationStart) {
            mutableStateOf(registrationStart)
        }
        var endDate by remember(registrationEnd) {
            mutableStateOf(registrationEnd)
        }
        val context = LocalContext.current

        fun openDatePicker(currentDate: String, onDateSelected: (String) -> Unit) {
            val initialDate = try {
                LocalDate.parse(currentDate)
            } catch (_: DateTimeParseException) {
                LocalDate.now()
            }

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format(
                        Locale.ROOT,
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        dayOfMonth
                    )
                    onDateSelected(selectedDate)
                },
                initialDate.year,
                initialDate.monthValue - 1,
                initialDate.dayOfMonth
            ).show()
        }

        Row {
            Column(modifier = Modifier.weight(1f)) {
                TextField(
                    value = startDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date*") },
                    placeholder = { Text("Select date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            openDatePicker(startDate) {
                                startDate = it
                                registrationStart = it
                            }
                        }
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                TextField(
                    value = endDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date*") },
                    placeholder = { Text("Select date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            openDatePicker(endDate) {
                                endDate = it
                                registrationEnd = it
                            }
                        }
                )
            }
        }
    }

    @Composable
    private fun BookingPeriod() {
        Row {
            TextField(
                value = "Start Date*", // required field
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                value = "End Date*", // required field
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    /**
     *  This function renders this page
     *
     *  @param navController the controller that handles navigation
     */
    @Composable
    fun ShowScreen(navController: NavController) {

        Column(
            modifier = Modifier
        ) {
            Header(
                headerText = headerName,
                backgroundColor = ownerHeaderBackgroundColor,
                textColor = ownerHeaderTextColor,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .background(primaryGreenColor)
                    .fillMaxHeight()
                    .padding(20.dp),
            ) {
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(6.dp))
                                .background(light)
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            CampsiteSelect(availableCampsites)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(6.dp))
                                .background(light)
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            WinnersAndWaitlistCaps()
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
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
                                RegistrationPeriod()
                            }

                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
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
                                BookingPeriod()
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // place the buttons at the bottom of the screen
                Spacer(modifier = Modifier.weight(1f))

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
                            controller.onDelete(navController)
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Lottery",
                            )
                        },
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    ExtendedFloatingActionButton(
                        containerColor = secondaryGreenColor,
                        contentColor = primaryGreenTextColor,
                        shape = CircleShape,
                        onClick = {
                            // controller.onSave()
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
            }

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
