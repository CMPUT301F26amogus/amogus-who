package com.example.code.views

import View
import Model
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import java.time.format.DateTimeFormatter
import com.example.code.Routes
import com.example.code.controllers.ReserveCampsiteController
import com.example.code.model.Campsite
import com.example.code.model.DateRange
import com.example.code.model.EntrantReservation
import com.example.code.model.Lottery

/**
 * Screen where an entrant picks dates and guests and books the campsite in the model's temp campsite.
 * Shows a confirmation with a CSV download once it's booked (US 01.05.01, 01.05.02).
 * If the campsite has rules the entrant has to tick that they agree before booking (US 02.11.01).
 * When it's opened from a lottery win the dates are picked on a calendar that greys out booked days,
 * and the entrant can decline the win (US 01.04.01, 01.04.02, 01.04.03).
 *
 * @param model the app's model
 */
class ReserveCampsite(model: Model) : View(), InputFieldFunctions {

    val controller = ReserveCampsiteController(model)
    // state so the screen redraws when update() gives it the campsite
    var campsite by mutableStateOf<Campsite?>(model.gettTempCampsite())
    // the lottery they won to get here or null if theres no lottery
    var lottery by mutableStateOf<Lottery?>(model.lotteryToReserve)
    // the stays still free at the campsite as check in to latest check out (US 01.04.01)
    val availableDates = mutableStateListOf<DateRange>()
    // false until the dates come back so it doesnt say nothings free while loading
    var datesLoaded by mutableStateOf(false)

    init {
        loadAvailableDates(model)
    }

    val bgColor = secondaryGreenColor
    val headerBackgroundColor = entrantHeaderBackgroundColor
    val headerTextColor = entrantHeaderTextColor
    val footerTextColor = entrantFooterBackgroundColor
    val footerBackgroundColor = entrantFooterButtonColor
    val footerButtonColor = entrantFooterTextColor
    val contentBackground = creamBG
    val headerName = "Reserve Site"

    val fieldModifierDate = Modifier
        .background(color = creamBG)
        .padding(8.dp).width(135.dp)

    // what the boxes show, read from the campsite so they stay in sync with the model
    val name get() = campsite?.name ?: "Campsite Name"
    val park get() = campsite?.location ?: ""
    val details get() = campsite?.amenities?.joinToString(", ") ?: ""
    val tos get() = campsite?.rulesAndPolicy ?: ""
    // a dash when nobody rated it yet since the box is too narrow for no ratings yet
    val rating get() = campsite?.averageRating?.let { "%.1f".format(it) } ?: "-"

    // the calendar works in utc midnight millis so these turn days into that and back
    private fun millisOf(day: LocalDate): Long = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun dayOf(millis: Long?): LocalDate? =
        millis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }

    // a calendar that opens on the first free month and only lets the pickable days be tapped
    @OptIn(ExperimentalMaterial3Api::class)
    private fun calendarStateFor(pickable: Set<LocalDate>): DateRangePickerState {
        val first = pickable.minOrNull() ?: LocalDate.now()
        val last = pickable.maxOrNull() ?: first
        val years = first.year..last.year
        return DateRangePickerState(
            locale = Locale.getDefault(),
            initialDisplayedMonthMillis = millisOf(first.withDayOfMonth(1)),
            yearRange = years,
            initialDisplayMode = DisplayMode.Picker,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = dayOf(utcTimeMillis) in pickable
                override fun isSelectableYear(year: Int): Boolean = year in years
            },
        )
    }

    // shows 2027-05-01 as 05/01/27 like the reservation cards and leaves anything it cant read as it is
    private fun formatDate(date: String): String =
        runCatching { LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MM/dd/yy")) }
            .getOrDefault(date)

    /**
     * gets the campsite being reserved from the model
     *
     * @param model the model, passed in when it notifies its views
     */
    override fun update(model: Model) {
        campsite = model.gettTempCampsite()
        lottery = model.lotteryToReserve
        loadAvailableDates(model)
    }

    // asks the model for the free dates when its a lottery win, a get so it doesnt loop back into update
    private fun loadAvailableDates(model: Model) {
        val site = campsite
        if (lottery == null || site == null) {
            availableDates.clear()
            datesLoaded = false
            return
        }
        model.getAvailableDates(site) { free ->
            // another campsite might have been picked while this one was loading
            if (campsite?.id != site.id) return@getAvailableDates
            availableDates.clear()
            availableDates.addAll(free)
            datesLoaded = true
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    public fun ShowScreen(navController: NavController) {

        // remembered so typing isnt lost when the screen redraws
        val arrivalDateState = rememberTextFieldState("")
        val departureDateState = rememberTextFieldState("")
        val numberOfGuestsState = rememberTextFieldState("")
        // why the last try didnt work or null
        var error by remember { mutableStateOf<String?>(null) }
        // the reservation that was just made so the confirmation popup can show (US 01.05.01)
        var confirmed by remember { mutableStateOf<EntrantReservation?>(null) }
        val downloadCsv = rememberCsvDownloader { error = it }
        // if they ticked that they agree to the rules (US 02.11.01)
        // keyed on the campsite so ticking it for one campsite doesnt count for another
        var rulesAccepted by remember(campsite?.id) { mutableStateOf(false) }
        // shows the are you sure popup before declining the lottery win (US 01.04.03)
        var askDecline by remember { mutableStateOf(false) }
        // every day a stay could start or end on, the calendar greys out the rest (US 01.04.01)
        val pickable = remember(availableDates.toList()) {
            availableDates.flatMap { it.checkInAndOutDays() }.toSet()
        }
        // made again when the free days change since the calendar keeps which days are pickable
        val calendarState = remember(campsite?.id, pickable) { calendarStateFor(pickable) }

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
                    .background(bgColor)
                    // scrolls so the lottery boxes dont push the buttons off small screens
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.padding(15.dp))

                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(color = contentBackground)
                        .padding(20.dp)
                        .width(320.dp),
                ) {
                    Row {  //  box with campsite name, location, and rating
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .padding(vertical = 10.dp)
                        ) {
                            // fixed width but not height so a long name wraps instead of getting cut off
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 22.sp,
                                    lineHeight = 26.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("campsiteNameBox")
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = park,
                                    fontSize = 20.sp,
                                    modifier = Modifier.testTag("parkAreaBox")
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                        ) {
                            Text("Rating: ")
                            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                            Text(
                                text = rating,
                                modifier = Modifier.testTag("ratingBox"))
                        }
                    }
                }


                Spacer(Modifier.padding(8.dp))

                // a calendar of the days still free for a lottery winner to pick from (US 01.04.01)
                // booked past and out of window days are greyed out
                if (lottery != null) {
                    Box(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(8.dp))
                            .background(color = contentBackground)
                            .padding(vertical = 8.dp).width(360.dp)
                            .testTag("availableDatesBox"),
                    ) {
                        Column {
                            when {
                                !datesLoaded -> Text(
                                    "Loading available dates...",
                                    modifier = Modifier.padding(16.dp),
                                )
                                availableDates.isEmpty() -> Text(
                                    "No dates left at this campsite. You can pick another one or decline.",
                                    modifier = Modifier.padding(16.dp).testTag("noAvailableDates"),
                                )
                                else -> {
                                    DateRangePicker(
                                        state = calendarState,
                                        // the picker scrolls through months so it needs a set height inside this scrolling screen
                                        modifier = Modifier.height(470.dp).testTag("availabilityCalendar"),
                                        title = {
                                            Text(
                                                "Pick your check in and check out",
                                                modifier = Modifier.padding(start = 24.dp, top = 12.dp),
                                            )
                                        },
                                        showModeToggle = false,
                                        colors = DatePickerDefaults.colors(containerColor = contentBackground),
                                    )
                                    // the same free stays in words since a stay can run across months
                                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                        Text("Free (check in to latest check out):", fontSize = 14.sp)
                                        availableDates.forEach { free ->
                                            Text(
                                                "${formatDate(free.start)} to ${formatDate(free.end)}",
                                                fontSize = 14.sp,
                                                modifier = Modifier.testTag("availableDate"),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // typing the dates is only for booking without a lottery
                    Box(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(8.dp))
                            .background(color = contentBackground)
                            .padding(20.dp).width(320.dp),
                    ) {
                        Row {
                            //  arrival date input
                            DateField(
                                arrivalDateState,
                                "Arrival Date",
                                fieldModifierDate
                                    .testTag("arrivalDateInputField")
                            )

                            //  departure date input
                            DateField(
                                departureDateState,
                                "Departure Date",
                                fieldModifierDate
                                    .testTag("departureDateInputField")
                            )
                        }
                    }
                }

                Spacer(Modifier.padding(8.dp))

                Row {
                    //  number of guests input field
                    VariableDigitIntField(
                        numberOfGuestsState,
                        "Number of Guests",
                        2,
                        fieldModifierDate
                            .testTag("numberOfGuestsInputField")
                    )

                    Spacer(modifier = Modifier
                        .padding(horizontal = 8.dp))

                    Box(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(8.dp))
                            .background(color = contentBackground)
                            .padding(20.dp).width(160.dp).height(80.dp)
                    ) {
                        Column {
                            Text("Details: ")
                            Text(
                                text = details,
                                fontSize = 14.sp,
                                modifier = Modifier.testTag("detailsBox")
                            )
                        }
                    }
                }

                Spacer(Modifier.padding(8.dp))

                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(color = contentBackground)
                        .padding(20.dp).width(290.dp)
                ) {
                    Column {
                        Text("TOS: ")
                        Text(
                            text = tos,
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("tosBox")
                        )
                        // only when theres rules to agree to (US 02.11.01)
                        if (campsite?.hasRules() == true) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rulesAccepted,
                                    onCheckedChange = { rulesAccepted = it },
                                    modifier = Modifier.testTag("acceptRulesCheckbox")
                                )
                                Text("I've read and agree to the rules", fontSize = 14.sp)
                            }
                        }
                    }

                }

                Spacer(Modifier.padding(8.dp))

                error?.let {
                    Text(it, color = primaryGreenTextColor, modifier = Modifier.testTag("reserveError"))
                }

                Button(
                    onClick = {
                        val showResult = { result: Result<EntrantReservation> ->
                            result
                                .onSuccess {
                                    error = null
                                    confirmed = it
                                }
                                .onFailure { error = it.message ?: "Couldn't make the reservation" }
                            Unit
                        }
                        // the controller checks the input so the view just passes what was picked or typed
                        if (lottery != null) {
                            controller.makeLotteryReservation(
                                campsite = campsite,
                                lottery = lottery,
                                checkIn = dayOf(calendarState.selectedStartDateMillis),
                                checkOut = dayOf(calendarState.selectedEndDateMillis),
                                guests = numberOfGuestsState.text.toString(),
                                rulesAccepted = rulesAccepted,
                                onResult = showResult,
                            )
                        } else {
                            controller.makeReservation(
                                campsite = campsite,
                                arrival = arrivalDateState.text.toString(),
                                departure = departureDateState.text.toString(),
                                guests = numberOfGuestsState.text.toString(),
                                rulesAccepted = rulesAccepted,
                                onResult = showResult,
                            )
                        }
                    },
                    modifier = Modifier.testTag("reserveCampsiteButton")
                ) {
                    Text(" Make Reservation")
                }
                // none of the dates work or they changed their mind (US 01.04.03)
                if (lottery != null) {
                    TextButton(
                        onClick = { askDecline = true },
                        modifier = Modifier.testTag("declineLotteryButton"),
                    ) {
                        Text("Decline this lottery win", color = primaryGreenTextColor)
                    }
                }
                Footer(
                    textColor = entrantFooterTextColor,
                    backgroundColor = entrantFooterBackgroundColor,
                    buttonColor = entrantFooterButtonColor,
                    navController = navController
                )
            }
        }

        // confirms the booking and offers the csv (US 01.05.01 01.05.02)
        // the notification only goes out if they havent turned notifications off
        confirmed?.let { done ->
            AlertDialog(
                onDismissRequest = {},
                modifier = Modifier.testTag("reservationConfirmation"),
                title = { Text("Reservation confirmed") },
                text = {
                    Text(
                        "Your stay at ${done.campsite?.name ?: "the campsite"} from " +
                            "${formatDate(done.reservation.checkIn)} to ${formatDate(done.reservation.checkOut)} " +
                            "for ${done.reservation.numGuests} guest(s) is booked. " +
                            "You'll get a notification if they're turned on, or you can download a CSV copy."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            downloadCsv(done.csvFileName(), controller.reservationCsv(done))
                        },
                        modifier = Modifier.testTag("downloadReservationCsv"),
                    ) { Text("Download CSV") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        confirmed = null
                        navController.navigate(Routes.RESERVATIONS)
                    }) { Text("Done") }
                },
            )
        }

        // makes sure they meant it since a declined win cant be undone (US 01.04.03)
        if (askDecline) {
            AlertDialog(
                onDismissRequest = { askDecline = false },
                modifier = Modifier.testTag("declineLotteryDialog"),
                title = { Text("Decline this lottery?") },
                text = { Text("You won't be able to reserve a campsite from this lottery anymore.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            askDecline = false
                            controller.declineLotteryWin(lottery) { result ->
                                result
                                    .onSuccess {
                                        error = null
                                        navController.navigate(Routes.LOTTERIES)
                                    }
                                    .onFailure { error = it.message ?: "Couldn't decline the lottery" }
                            }
                        },
                        modifier = Modifier.testTag("confirmDeclineLottery"),
                    ) { Text("Decline") }
                },
                dismissButton = {
                    TextButton(onClick = { askDecline = false }) { Text("Keep it") }
                },
            )
        }

    }  // end of show screen body

}  // end of class body