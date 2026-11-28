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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

    private val controller = AddEditLotteryController(model)

    var headerName = if (isEdit) { "Edit Lottery" } else { "Add Lottery" }


    /** Display variables
     *      the variable versions of each field within Lottery creation
     * */
    private var oldLottery: Lottery? = null
    private val lotteryName = mutableStateOf("")
    private val availableCampsites = mutableStateListOf<Campsite>()
    private val selectedCampsites = mutableStateListOf<String>()    // reference by ID
    private val maxWinners = mutableStateOf("")
    private val maxWaitlist = mutableStateOf("")
    private val registrationStart = mutableStateOf("")      // [1]
    private val registrationEnd = mutableStateOf("")        // [1]
    private val bookingStart = mutableStateOf("")
    private val bookingEnd = mutableStateOf("")

    private val errorMessage = mutableStateOf("")

    /** Update
     *      If this view is an edit view then it will grab the values of the lottery to be edited,
     *      otherwise it will clear the variables to be empty fields to act as an add view
     * */
    override fun update(model: Model){
        if (isEdit){
            oldLottery = model.gettTheLottery()
        }
        reset()

        model.getCampsitesByOwner(
            ownerId = model.currentDeviceId,
            onResult = { result ->
                availableCampsites.clear()
                availableCampsites.addAll(result)
            }
        )
    }

    /**clear
     *
     * resets fields when cancel is pressed
     * */
    fun reset() {
        if (oldLottery != null) {
            selectedCampsites.addAll(oldLottery!!.campsiteIds)
            maxWinners.value = oldLottery!!.numWinners.toString()
            maxWaitlist.value = oldLottery!!.waitlistCapacity.toString()
            registrationStart.value = oldLottery!!.registrationStart      // [1]
            registrationEnd.value = oldLottery!!.registrationEnd          // [1]
            bookingStart.value = oldLottery!!.bookingStart
            bookingEnd.value = oldLottery!!.bookingEnd
        } else {
            selectedCampsites.clear()
            maxWinners.value = ""
            maxWaitlist.value = ""
            registrationStart.value = ""
            registrationEnd.value = ""
            bookingStart.value = ""
            bookingEnd.value = ""
            errorMessage.value = ""
        }
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
    private fun CampsiteSelect(
        availableCampsites: List<Campsite>,
        selectedCampsites: MutableList<String>  // reference by ID
    ) {
        var expanded by remember { mutableStateOf(false) }
        val fieldColor = tertiaryPurple
        val dividerColor = primaryTextColor.copy(alpha = 0.55f)
        val visibleCampsites = if (expanded) {
            availableCampsites
        } else {
            availableCampsites.filter { it.id in selectedCampsites }
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
                    .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
                    .testTag("onSelectCampsites"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCampsites.isEmpty()) {
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
                        val isSelected = campsite.id in selectedCampsites
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Checkbox) {
                                    if (isSelected) {
                                        selectedCampsites.remove(campsite.id)
                                    } else {
                                        selectedCampsites.add(campsite.id)
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
                                        selectedCampsites.remove(campsite.id)
                                    } else {
                                        selectedCampsites.add(campsite.id)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = primaryTextColor,
                                    uncheckedColor = secondaryPurple,
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
    private fun WinnersAndWaitlistCaps(
        winners: MutableState<String>,
        waitlist: MutableState<String>,
    ) {
        Row {
            TextField(
                value = winners.value, // required field
                onValueChange = {
                    if (it.all { char -> char in '0'..'9' }) {
                        winners.value = it
                    }
                },
                label = { Text("Max Winners*") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = tertiaryPurple,
                    unfocusedContainerColor = tertiaryPurple,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = secondaryPurple,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("onMaxWinners")
            )
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                value = waitlist.value,
                onValueChange = {
                    if (it.all { char -> char in '0'..'9' }) {
                        waitlist.value = it
                    }
                },
                label = { Text("Max Waitlist") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = tertiaryPurple,
                    unfocusedContainerColor = tertiaryPurple,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = secondaryPurple,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("onMaxWaitlist")
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
    private fun PeriodSelect(
        start: MutableState<String>,                                                    // [1]
        end: MutableState<String>,                                                      // [1]
        purpose: String = "",
    ) {
        var startDate by start                                                          // [1]
        var endDate by end                                                              // [1]
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
            Box(modifier = Modifier.weight(1f)) {
                TextField(
                    value = startDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date*") },
                    placeholder = { Text("Select date") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = tertiaryPurple,
                        unfocusedContainerColor = tertiaryPurple,
                        focusedTextColor = primaryTextColor,
                        unfocusedTextColor = secondaryPurple,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("on${purpose}Start")
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            openDatePicker(startDate) {
                                startDate = it
                            }
                        }
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                TextField(
                    value = endDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date*") },
                    placeholder = { Text("Select date") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = tertiaryPurple,
                        unfocusedContainerColor = tertiaryPurple,
                        focusedTextColor = primaryTextColor,
                        unfocusedTextColor = secondaryPurple,
                        ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("on${purpose}End")
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            openDatePicker(endDate) {
                                endDate = it
                            }
                        }
                )
            }
        }
    }

    @Composable
    private fun ErrorMessage(errorMessage: String = "<error message here>") {
        if (errorMessage.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(size = 6.dp))
                    .fillMaxWidth()
                    .background(color = dangerousActionColor)
                    .padding(10.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = primaryTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(6.dp))
                                .background(light)
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            TextField(
                                value = lotteryName.value,
                                onValueChange = { lotteryName.value = it },
                                label = { Text("Name") },
                                placeholder = { Text("Name this lottery") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = tertiaryPurple,
                                    unfocusedContainerColor = tertiaryPurple,
                                    focusedTextColor = primaryTextColor,
                                    unfocusedTextColor = secondaryPurple,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onLotteryName")
                            )
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
                            CampsiteSelect(
                                availableCampsites,
                                selectedCampsites,
                            )
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
                            WinnersAndWaitlistCaps(maxWinners, maxWaitlist)
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
                                PeriodSelect(
                                    start = registrationStart,    // [1]
                                    end = registrationEnd,        // [1]
                                )
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
                                PeriodSelect(
                                    start = bookingStart,
                                    end = bookingEnd,
                                )
                            }
                        }
                    }
                }

                // place the buttons at the bottom of the screen
                Spacer(modifier = Modifier.height(10.dp))

                ErrorMessage(errorMessage.value)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                ) {
                    ExtendedFloatingActionButton(
                        containerColor = light,
                        contentColor = primaryTextColor,
                        shape = CircleShape,
                        onClick = {
                            reset()
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
                        },
                        modifier = Modifier
                            .testTag("onCancel")
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (oldLottery != null) {
                        FloatingActionButton(
                            containerColor = dangerousActionColor,
                            contentColor = primaryTextColor,
                            shape = CircleShape,
                            onClick = {
                                controller.onDelete(
                                    lottery = oldLottery!!,
                                    navController = navController
                                )
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Lottery",
                                )
                            },
                            modifier = Modifier
                                .testTag("onDelete")
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    ExtendedFloatingActionButton(
                        containerColor = secondaryGreenColor,
                        contentColor = primaryGreenTextColor,
                        shape = CircleShape,
                        onClick = {
                             errorMessage.value = controller.onSave(
                                 navController = navController,
                                 campsites = selectedCampsites,
                                 numWinners = maxWinners.value,
                                 waitlistCapacity = maxWaitlist.value,
                                 registrationStart = registrationStart.value,
                                 registrationEnd = registrationEnd.value,
                                 bookingStart = bookingStart.value,
                                 bookingEnd = bookingEnd.value,
                                 name = lotteryName.value,
                             )
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
                        modifier = Modifier
                            .testTag("onSave")
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
