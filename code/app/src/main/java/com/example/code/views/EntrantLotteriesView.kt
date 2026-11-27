package com.example.code.views

import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Model
import Tab
import View
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

class EntrantLotteriesView(M: Model): View() {
    // state so the screen redraws when the data loads
    val currentLotteries = mutableStateListOf<Lottery>()
    // a lottery only stores campsite ids, so the campsites are kept here by id
    val campsites = mutableStateMapOf<String, Campsite>()
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
        // current = lotteries the user is still waiting on
        model.getLotteriesIn(EntryStatus.WAITING) { lotteries ->
            currentLotteries.clear()
            currentLotteries.addAll(lotteries)
            for (lottery in lotteries) {
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
    private val model = M
    val bgColor = secondaryGreenColor
    val headerbgColor = entrantHeaderBackgroundColor
    val headerTextColor = entrantHeaderTextColor
    val footerTextColor = entrantFooterTextColor
    val footerbgColor = entrantFooterBackgroundColor
    val footerButtonColor = entrantFooterButtonColor
    @Composable
    public fun ShowScreen(navController: NavController) {
        var state by remember {mutableStateOf("Current")}
        // update() only runs when data changes, so also run it once when the screen opens
        // (small break from the UML so the list isn't empty on first open)
        LaunchedEffect(Unit) { update(model) }
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
                                        navController = navController
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
    }
    @Composable
    private fun CurrentCampsiteLotteryRow(lottery: Lottery, campsite: Campsite, navController: NavController) {
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
                            Text(lottery.status.toString())
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
                    Text(
                        "X Cancel", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {controller.cancelLottery(lottery)},
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { controller.viewCampsite(campsite.id, navController = navController)},
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                    Text(
                        "Confirm", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { controller.confirmLottery(lottery)},
                    )
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
fun EntrantLotteriesViewPreview() {
    val mp = EntrantLotteriesView(Model())
    CodeTheme {
        (
            mp.ShowScreen(rememberNavController())
        )
    }
}