package com.example.code.views

import View
import Model
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import java.time.temporal.ChronoUnit
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
    // see [19] in Citation.md
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

    // what the boxes show, read from the campsite so they stay in sync with the model
    val name get() = campsite?.name ?: "Campsite Name"
    val park get() = campsite?.location ?: ""
    val amenities get() = campsite?.amenities ?: emptyList()
    val tos get() = campsite?.rulesAndPolicy ?: ""
    // a dash when nobody rated it yet since the box is too narrow for no ratings yet
    val rating get() = campsite?.averageRating?.let { "%.1f".format(it) } ?: "-"

    // the calendar works in utc midnight millis so these turn days into that and back
    // see [21] in Citation.md
    private fun millisOf(day: LocalDate): Long = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    // see [21] in Citation.md
    private fun dayOf(millis: Long?): LocalDate? =
        millis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }

    // a calendar that opens on the first free month and only lets the pickable days be tapped
    // see [21] in Citation.md
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

    // a rounded box like the ones in the mockup
    @Composable
    private fun Section(
        modifier: Modifier = Modifier,
        padding: Int = 14,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(contentBackground)
                .padding(padding.dp),
            content = content,
        )
    }

    // the date fields keep 8 digits like 06122027 so these go between that and a day
    private fun typedDay(digits: String): LocalDate? =
        if (digits.length != 8) null
        else runCatching {
            LocalDate.of(digits.substring(4).toInt(), digits.substring(0, 2).toInt(), digits.substring(2, 4).toInt())
        }.getOrNull()

    private fun typed(day: LocalDate): String = "%02d%02d%04d".format(day.monthValue, day.dayOfMonth, day.year)

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
    // see [19] in Citation.md
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
        // the decline button and popup are from [19] in Citation.md
        var askDecline by remember { mutableStateOf(false) }
        // the calendar popup for a normal stay and whether long rules are showing in full
        var showDatePicker by remember { mutableStateOf(false) }
        var showAllRules by remember(campsite?.id) { mutableStateOf(false) }
        var rulesCutOff by remember(campsite?.id) { mutableStateOf(false) }
        // every day a stay could start or end on, the calendar greys out the rest (US 01.04.01)
        // the calendar is from [21] in Citation.md
        val pickable = remember(availableDates.toList()) {
            availableDates.flatMap { it.checkInAndOutDays() }.toSet()
        }
        // made again when the free days change since the calendar keeps which days are pickable
        val calendarState = remember(campsite?.id, pickable) { calendarStateFor(pickable) }

        // the mockup layout, see [26] in Citation.md
        // the stay picked so far either typed in or tapped on the lottery calendar, used for the nights box
        val pickedCheckIn = if (lottery != null) dayOf(calendarState.selectedStartDateMillis) else typedDay(arrivalDateState.text.toString())
        val pickedCheckOut = if (lottery != null) dayOf(calendarState.selectedEndDateMillis) else typedDay(departureDateState.text.toString())
        val nights = if (pickedCheckIn != null && pickedCheckOut != null && pickedCheckOut.isAfter(pickedCheckIn)) {
            ChronoUnit.DAYS.between(pickedCheckIn, pickedCheckOut).toString()
        } else {
            "-"
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Header(
                headerText = headerName,
                backgroundColor = headerBackgroundColor,
                textColor = headerTextColor,
            )
            Column(  //  body of screen
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .background(bgColor)
                    // scrolls so the boxes dont push the button off small screens
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // campsite name and park with the rating pill
                Section {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            // wraps instead of getting cut off when the name is long
                            Text(
                                text = name,
                                fontSize = 24.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreenTextColor,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("campsiteNameBox"),
                            )
                            Text(
                                text = park,
                                fontSize = 15.sp,
                                color = primaryGreenTextColor,
                                modifier = Modifier.testTag("parkAreaBox"),
                            )
                        }
                        RatingPill(rating, tag = "ratingBox")
                    }
                }

                // a calendar of the days still free for a lottery winner to pick from (US 01.04.01)
                // booked past and out of window days are greyed out
                if (lottery != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = RoundedCornerShape(12.dp))
                            .background(color = contentBackground)
                            .padding(vertical = 8.dp)
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
                    // typed dates with a calendar button that fills them in and an x that clears them
                    Section(padding = 8) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DateField(
                                arrivalDateState,
                                "Arrival",
                                Modifier.weight(1f).testTag("arrivalDateInputField"),
                            )
                            Spacer(Modifier.width(6.dp))
                            DateField(
                                departureDateState,
                                "Departure",
                                Modifier.weight(1f).testTag("departureDateInputField"),
                            )
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.testTag("openDatePicker"),
                            ) {
                                Icon(Icons.Outlined.DateRange, contentDescription = "Pick dates on a calendar", tint = primaryGreenTextColor)
                            }
                            IconButton(
                                onClick = {
                                    arrivalDateState.clearText()
                                    departureDateState.clearText()
                                },
                                modifier = Modifier.testTag("clearDates"),
                            ) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Clear dates", tint = primaryGreenTextColor)
                            }
                        }
                    }
                }

                // guests and the nights the picked dates come to
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Section(modifier = Modifier.weight(1f), padding = 8) {
                        VariableDigitIntField(
                            numberOfGuestsState,
                            "Number of Guests",
                            2,
                            Modifier.fillMaxWidth().testTag("numberOfGuestsInputField"),
                        )
                    }
                    Section(modifier = Modifier.weight(1f)) {
                        Text("Nights", fontSize = 14.sp, color = primaryGreenTextColor)
                        Text(
                            nights,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreenTextColor,
                            modifier = Modifier.testTag("nightsBox"),
                        )
                    }
                }

                // amenities as a list next to where the map goes
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Section(modifier = Modifier.weight(1.4f).fillMaxHeight()) {
                        Text("Details:", fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, color = primaryGreenTextColor)
                        Column(modifier = Modifier.testTag("detailsBox")) {
                            if (amenities.isEmpty()) {
                                Text("No details listed", fontSize = 14.sp)
                            }
                            amenities.forEach { Text("• $it", fontSize = 14.sp) }
                        }
                    }
                    // campsites dont save map coordinates yet so this just shows where it is
                    Section(modifier = Modifier.weight(1f).fillMaxHeight().testTag("mapBox")) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Outlined.Place, contentDescription = null, tint = primaryGreenTextColor)
                            Text("Map", fontWeight = FontWeight.Bold, color = primaryGreenTextColor)
                            Text(park, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                // the campsites rules, long ones are cut short with a more button
                Section {
                    Text("TOS:", fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, color = primaryGreenTextColor)
                    Text(
                        text = tos.ifBlank { "This campsite has no rules listed" },
                        fontSize = 14.sp,
                        maxLines = if (showAllRules) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { if (it.hasVisualOverflow) rulesCutOff = true },
                        modifier = Modifier.testTag("tosBox"),
                    )
                    if (rulesCutOff) {
                        TextButton(
                            onClick = { showAllRules = !showAllRules },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(if (showAllRules) "Less" else "More", color = secondaryPurple)
                        }
                    }
                }

                // only when theres rules to agree to (US 02.11.01)
                if (campsite?.hasRules() == true) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(contentBackground)
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = rulesAccepted,
                            onCheckedChange = { rulesAccepted = it },
                            modifier = Modifier.testTag("acceptRulesCheckbox"),
                        )
                        Text("I accept the TOS", fontSize = 14.sp)
                    }
                }

                error?.let {
                    Text(it, color = primaryGreenTextColor, modifier = Modifier.testTag("reserveError"))
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // none of the dates work or they changed their mind (US 01.04.03)
                    if (lottery != null) {
                        TextButton(
                            onClick = { askDecline = true },
                            modifier = Modifier.testTag("declineLotteryButton"),
                        ) {
                            Text("Decline this lottery win", color = primaryGreenTextColor)
                        }
                    }
                    Spacer(Modifier.weight(1f))
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
                        // greyed out until the rules box is ticked when there are rules (US 02.11.01)
                        // the controller and model still check it too
                        enabled = campsite?.hasRules() != true || rulesAccepted,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryPurple, contentColor = secondaryPurple),
                        modifier = Modifier.testTag("reserveCampsiteButton"),
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Make Reservation", fontWeight = FontWeight.Bold)
                    }
                }
            }
            // outside the scrolling part so it stays at the bottom like every other screen
            Footer(
                textColor = entrantFooterTextColor,
                backgroundColor = entrantFooterBackgroundColor,
                buttonColor = entrantFooterButtonColor,
                navController = navController
            )
        }

        // a calendar for picking a normal stay that fills in the typed boxes
        if (showDatePicker) {
            val pickerState = rememberDateRangePickerState(
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        dayOf(utcTimeMillis)?.isBefore(LocalDate.now()) == false
                },
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                modifier = Modifier.testTag("datePickerDialog"),
                confirmButton = {
                    TextButton(
                        onClick = {
                            dayOf(pickerState.selectedStartDateMillis)?.let { arrivalDateState.setTextAndPlaceCursorAtEnd(typed(it)) }
                            dayOf(pickerState.selectedEndDateMillis)?.let { departureDateState.setTextAndPlaceCursorAtEnd(typed(it)) }
                            showDatePicker = false
                        },
                        enabled = pickerState.selectedEndDateMillis != null,
                        modifier = Modifier.testTag("confirmDatePicker"),
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                },
            ) {
                DateRangePicker(
                    state = pickerState,
                    modifier = Modifier.weight(1f),
                    title = {
                        Text("Pick your arrival and departure", modifier = Modifier.padding(start = 24.dp, top = 16.dp))
                    },
                    showModeToggle = false,
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