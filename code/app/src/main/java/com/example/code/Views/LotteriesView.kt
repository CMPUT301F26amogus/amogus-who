/*
* LotteriesView.kt : the view for details of a entrant's lottery information
* Copyright (C) 2026 Cash Popik (cpopik@ualberta.ca)
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published
* by the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
* */

package com.example.code.Views

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
import com.example.code.Controllers.EntrantLotteriesController
import com.example.code.Model.Campsite
import com.example.code.Model.Lottery
import com.example.code.Model.Reservation
import com.example.code.ui.theme.CodeTheme

class LotteriesView(M: Model): View() {
    var currentLotteries = emptyList<Lottery>()
    var previousLotteries = emptyList<Reservation>()
    override fun update(model: Model) {
        model.getCancelledReservations(entrantId = model.currentDeviceId){
            result -> previousLotteries = result
        }
        model.getWaitlist(){
                result -> currentLotteries = result
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
                            for (campsite in lottery.campsites)
                                when (campsite) {
                                    campsite ->
                                    CurrentCampsiteLotteryRow(
                                        lottery = lottery,
                                        campsite = campsite,
                                        navController = navController
                                    )
                                }
                        }
                    }
                }
            }else{
                Row(modifier = Modifier.weight(1f).background(secondaryGreenColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(previousLotteries) { index, reservation ->
                            PreviousCampsiteLotteryRow(
                                lottery = reservation.lottery,
                                campsite = reservation.campsite,
                                navController = navController
                            )
                        }
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
fun LotteriesViewPreview() {
    val mp = LotteriesView(Model())
    CodeTheme {
        (
            mp.ShowScreen(rememberNavController())
        )
    }
}