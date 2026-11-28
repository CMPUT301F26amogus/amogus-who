package com.example.code.views

import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Model
import Tab
import View
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.controllers.EntrantLotteriesController
import com.example.code.model.Campsite
import com.example.code.model.EntryStatus
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.ui.theme.CodeTheme

class LotteriesView(M: Model): View() {
    // state so the screen redraws when the data loads
    val currentLotteries = mutableStateListOf<Lottery>()
    // a lottery only stores campsite ids, so the campsites are kept here by id
    val campsites = mutableStateMapOf<String, Campsite>()
    // where this user is in each lottery by lottery id so winners get reserve and decline (US 01.04.01)
    // see [19] in Citation.md
    val myStatus = mutableStateMapOf<String, EntryStatus>()
    var previousLotteries = emptyList<Reservation>()

    /** update
     * gets the lotteries this user joined and the campsites in them (UML: views only call model get-functions here)
     *
     * @param model the model, passed in when it notifies its views
     */
    override fun update(model: Model) {
        model.getCancelledReservations(){
            result -> previousLotteries = result
        }
        // current = lotteries the user is still waiting on or won and hasnt answered yet
        model.getLotteriesIn(EntryStatus.WAITING, EntryStatus.SELECTED) { lotteries ->
            currentLotteries.clear()
            currentLotteries.addAll(lotteries)
            myStatus.clear()
            for (lottery in lotteries) {
                lottery.entryFor(model.currentDeviceId)?.let { myStatus[lottery.id] = it.status }
                for (campsiteId in lottery.campsiteIds) {
                    model.getCampsite(campsiteId) { campsite ->
                        if (campsite != null) {
                            campsites[campsiteId] = campsite
                        }
                    }
                }
            }
        }
    }
    val controller = EntrantLotteriesController(M)
    val bgColor = secondaryGreenColor
    val headerbgColor = entrantHeaderBackgroundColor
    val headerTextColor = entrantHeaderTextColor
    val footerTextColor = entrantFooterTextColor
    val footerbgColor = entrantFooterBackgroundColor
    val footerButtonColor = entrantFooterButtonColor
    @Composable
    public fun ShowScreen(navController: NavController) {
        var state by remember {mutableStateOf("Current")}
        // the won lottery waiting on the are you sure popup before its declined (US 01.04.03)
        // the decline popup is from [19] in Citation.md
        var toDecline by remember { mutableStateOf<Lottery?>(null) }
        val context = LocalContext.current
        val tabs = mutableListOf<Tab>(
            Tab(name = "Current", color = entrantFooterBackgroundColor, option = {state="Current"}),
            Tab(name = "Previous", color = secondaryGreenColor, option = {state="Previous"}),
        )
        Column() {
            Row() {
                Header(
                    headerText = "Lotteries",
                    backgroundColor = headerbgColor,
                    textColor = headerTextColor,
                )
            }
            Row(modifier = Modifier.height(50.dp)){
                TabManager(tabs,state)
            }
            if (state=="Current") {
                Row(modifier = Modifier.weight(1f).background(entrantFooterBackgroundColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(currentLotteries) { index, lottery ->
                            for (campsiteId in lottery.campsiteIds) {
                                // campsites[campsiteId] is filled in update(), skip it until it loads
                                val campsite = campsites[campsiteId]
                                if (campsite != null) {
                                    CurrentCampsiteLotteryRow(
                                        lottery = lottery,
                                        campsite = campsite,
                                        won = myStatus[lottery.id] == EntryStatus.SELECTED,
                                        navController = navController,
                                        onDecline = { toDecline = lottery },
                                    )
                                }
                            }
                        }
                    }
                }
            }else{
                Row(modifier = Modifier.weight(1f).background(secondaryGreenColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        /*TODO: this doesnt work anymore
                        itemsIndexed(previousLotteries) { index, reservation ->
                            PreviousCampsiteLotteryRow(
                                lottery = reservation.campsite!!.lottery,
                                campsite = reservation.campsite,
                                navController = navController
                            )
                        }*/
                    }
                }
            }
            Row() {
                Footer(
                    navController = navController,
                    textColor = footerTextColor,
                    backgroundColor = footerbgColor,
                    buttonColor = footerButtonColor,
                )
            }
        }

        // makes sure they meant it since a declined win cant be undone (US 01.04.03)
        toDecline?.let { lottery ->
            AlertDialog(
                onDismissRequest = { toDecline = null },
                modifier = Modifier.testTag("declineLotteryDialog"),
                title = { Text("Decline this lottery?") },
                text = { Text("You won't be able to reserve a campsite from this lottery anymore.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            toDecline = null
                            controller.declineLotteryWin(lottery) { message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("confirmDeclineLottery"),
                    ) { Text("Decline") }
                },
                dismissButton = {
                    TextButton(onClick = { toDecline = null }) { Text("Keep it") }
                },
            )
        }
    }
    // the won decline and reserve parts are from [19] in Citation.md
    /**
     * one campsite of a lottery the user is in
     *
     * a lottery with more than one campsite gets a row for each so a winner picks one by tapping its reserve (US 01.04.02)
     *
     * @param won true if they won and havent reserved or declined yet
     * @param onDecline called when they tap decline so the screen can ask if theyre sure
     */
    @Composable
    private fun CurrentCampsiteLotteryRow(
        lottery: Lottery,
        campsite: Campsite,
        won: Boolean,
        navController: NavController,
        onDecline: () -> Unit,
    ) {
        Row(modifier = Modifier.padding(50.dp).height(150.dp).width(500.dp)){
            Column() {
                Row(
                    modifier = Modifier.height(120.dp).background(entrantFooterButtonColor)
                        .width(500.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 15.dp).width(200.dp)){
                        Row() {
                            Text(campsite.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Row() {
                            Text(campsite.location)
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        Row(modifier = Modifier.background(light).padding(horizontal = 20.dp).fillMaxWidth()) {
                            Text(if (won) "You won!" else lottery.status.toString())
                        }
                    }
                    Column(modifier = Modifier.width(50.dp)){
                        Spacer(modifier = Modifier.height(15.dp))
                        Row() {
                            Text(campsite.averageRating.toString(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(25.dp))
                        Row() {
                            Text("Cal")
                        }
                    }
                }
                Row(modifier = Modifier.height(30.dp).background(light).width(500.dp)) {
                    // winners decline instead of leaving since leaving would wipe out their win
                    if (won) {
                        Text(
                            "X Decline", color = Color(0xff5530a4),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onDecline() }.testTag("declineLotteryButton"),
                        )
                    } else {
                        Text(
                            "X Cancel", color = Color(0xff5530a4),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {controller.cancelLottery(lottery)},
                        )
                    }
                    Spacer(modifier = Modifier.width(25.dp))
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { controller.viewCampsite(campsite.id, navController = navController)},
                    )
                    // only winners can reserve, this row is the campsite they pick (US 01.04.02)
                    if (won) {
                        Spacer(modifier = Modifier.width(25.dp))
                        Text(
                            "Reserve", color = Color(0xff5530a4),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { controller.reserveFromLottery(lottery, campsite, navController) }
                                .testTag("reserveLotteryButton-${campsite.id}"),
                        )
                    }
                }
            }
        }
    }
    @Composable
    private fun PreviousCampsiteLotteryRow(lottery: Lottery, campsite: Campsite?, navController: NavController) {
        Row(modifier = Modifier.padding(50.dp).height(150.dp).width(500.dp)){
            Column() {
                Row(
                    modifier = Modifier.height(120.dp).background(entrantFooterButtonColor)
                        .width(500.dp)
                ) {
                    Column() {
                        Row() {
                            Column(
                                modifier = Modifier.padding(horizontal = 15.dp).width(200.dp)
                            ) {
                                Row() {
                                    Text(
                                        campsite!!.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                Row() {
                                    Text(campsite!!.location)
                                }
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                            Column(modifier = Modifier.width(50.dp)) {
                                Spacer(modifier = Modifier.height(15.dp))
                                Row() {
                                    Text(
                                        campsite!!.averageRating.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(25.dp))
                            }
                        }
                        Row(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Row(
                                modifier = Modifier.background(light)
                                    .padding(horizontal = 10.dp)
                                    .width(500.dp)
                            ) {
                                Text(lottery.status.toString())
                            }
                        }
                    }
                }
                Row(modifier = Modifier.height(30.dp).background(light).width(500.dp)) {
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp, textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(500.dp)
                            .clickable { controller.viewCampsite(campsite!!.id, navController = navController) }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LotteriesViewPreview() {
    val mp = LotteriesView(Model())
    CodeTheme {
        (
            mp.ShowScreen(rememberNavController())
        )
    }
}