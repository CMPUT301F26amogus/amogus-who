/*
* CampsiteDetailsView.kt : the view for details of active and past reservations
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

import Model
import com.example.code.Routes
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.Controllers.EntrantReservationsController
import com.example.code.Model.Lottery
import com.example.code.Model.Reservation
import com.example.code.ui.theme.CodeTheme

class ReservationsView(model: Model): View() {
    var currentReservations = emptyList<Reservation>()
    var previousReservations = emptyList<Reservation>()
    var controller = EntrantReservationsController(model)
    override fun update(model: Model) {
        model.getCancelledReservations(entrantId = model.currentDeviceId){
                result -> previousReservations = result
        }
        model.getActiveReservations(entrantId = model.currentDeviceId){
                result -> currentReservations = result
        }
    }
    val bgColor = secondaryGreenColor
    val headerbgColor = entrantHeaderBackgroundColor
    val headerTextColor = entrantHeaderTextColor
    val footerTextColor = entrantFooterTextColor
    val footerbgColor = entrantFooterBackgroundColor
    val footerButtonColor = entrantFooterButtonColor
    @Composable
    public fun ShowScreen(navController: NavController) {
        var state by remember { mutableStateOf("Current") }
        val tabs = mutableListOf<Tab>(
            Tab(name = "Current", color = entrantFooterBackgroundColor, option = {state = "Current"}),
            Tab(name = "Previous", color = secondaryGreenColor, option = {state = "Previous"})
        )
        Column() {
            Row() {
                Header(
                    headerText = "Reservations",
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
                        itemsIndexed(currentReservations) { index, reservation ->
                            CurrentReservationRow(
                                reservation = reservation,
                                navController = navController
                            )
                        }
                    }
                }
            }else{
                Row(modifier = Modifier.weight(1f).background(secondaryGreenColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(previousReservations) { index, reservation ->
                            PreviousReservationRow(
                                reservation = reservation,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
    @Composable
    private fun CurrentReservationRow(reservation: Reservation, navController: NavController) {
        Row(modifier = Modifier.padding(50.dp).height(150.dp).width(500.dp)){
            Column() {
                Row(
                    modifier = Modifier.height(120.dp).background(entrantFooterButtonColor)
                        .width(500.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 15.dp).width(200.dp)){
                        Row() {
                            Text(reservation.campsite!!.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Row() {
                            Text(reservation.campsite!!.location)
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        Row(modifier = Modifier.background(light).padding(horizontal = 20.dp).fillMaxWidth()) {
                            Text("MM/DD/YY | MM/DD/YY", fontSize = 13.sp)
                        }
                    }
                    Column(modifier = Modifier.width(50.dp)){
                        Spacer(modifier = Modifier.height(15.dp))
                        Row() {
                            Text(reservation.campsite!!.averageRating.toString(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(25.dp))
                        Row() {
                            Text("^: ${reservation.guests}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(modifier = Modifier.height(30.dp).background(light).width(500.dp)) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "X Cancel", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { controller.cancelReservation(reservation) },

                    )
                    Spacer(modifier = Modifier.width(105.dp))
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { controller.viewCampsite(reservation.campsite!!.id, navController = navController) },
                    )
                }
            }
        }
    }
    @Composable
    private fun PreviousReservationRow(reservation: Reservation, navController: NavController) {
        Row(modifier = Modifier.padding(50.dp).height(150.dp).width(500.dp)){
            Column() {
                Row(
                    modifier = Modifier.height(120.dp).background(Color(0xffcadac8))
                        .width(500.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 15.dp).width(200.dp)){
                        Row() {
                            Text(reservation.campsite!!.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Row() {
                            Text(reservation.campsite!!.location)
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        Row(modifier = Modifier.background(light).padding(horizontal = 20.dp).fillMaxWidth()) {
                            Text("MM/DD/YY | MM/DD/YY", fontSize = 13.sp)
                        }
                    }
                    Column(modifier = Modifier.width(50.dp)){
                        Spacer(modifier = Modifier.height(15.dp))
                        Row() {
                            Text(reservation.campsite!!.averageRating.toString(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(25.dp))
                        Row() {
                            Text("^: ${reservation.guests}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(modifier = Modifier.height(30.dp).background(light).width(500.dp)) {
                    Spacer(modifier = Modifier.width(180.dp))
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { controller.viewCampsite(reservation.campsite!!.id, navController = navController) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationsViewPreview() {
    val mp = ReservationsView(Model())
    CodeTheme {
        (
            mp.ShowScreen(rememberNavController())
        )
    }
}