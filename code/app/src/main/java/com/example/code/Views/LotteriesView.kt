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

import Option
import Tab
import View
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.ui.theme.CodeTheme

class LotteriesView: View() {
    val bgColor = entrantBackgroundColor
    val headerbgColor = entrantHeaderBackgroundColor
    val headerTextColor = entrantHeaderTextColor
    val footerTextColor = entrantFooterTextColor
    val footerbgColor = entrantFooterBackgroundColor
    val footerButtonColor = entrantFooterButtonColor
    @Composable
    public fun ShowScreen() {
        val tabs = mutableListOf<Tab>(
            Tab(name = "Current", color = entrantFooterBackgroundColor, option = {}),
            Tab(name = "Previous", color = entrantBackgroundColor, option = {}),
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
                TabManager(tabs)
            }
            if (true) {
                Row(modifier = Modifier.weight(1f).background(entrantFooterBackgroundColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {

                    }
                }
            }else{
                Row(modifier = Modifier.weight(1f).background(entrantBackgroundColor)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {

                    }
                }
            }
            Row() {
                Footer(
                    textColor = footerTextColor,
                    backgroundColor = footerbgColor,
                    buttonColor = footerButtonColor,
                )
            }
        }
    }
    @Composable
    private fun CurrentCampsiteLotteryRow(){
        Row(modifier = Modifier.padding(50.dp).height(150.dp).width(500.dp)){
            Column() {
                Row(
                    modifier = Modifier.height(120.dp).background(entrantFooterButtonColor)
                        .width(500.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 15.dp).width(200.dp)){
                        Row() {
                            Text("Campsite Name", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Row() {
                            Text("Park/Location")
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        Row(modifier = Modifier.background(light).padding(horizontal = 20.dp).fillMaxWidth()) {
                            Text("Winner!")
                        }
                    }
                    Column(modifier = Modifier.width(50.dp)){
                        Spacer(modifier = Modifier.height(15.dp))
                        Row() {
                            Text("Rating", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                    Text(
                        "Confirm", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    @Composable
    private fun PreviousCampsiteLotteryRow(){
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
                                        "Campsite Name",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                Row() {
                                    Text("Park/Location")
                                }
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                            Column(modifier = Modifier.width(50.dp)) {
                                Spacer(modifier = Modifier.height(15.dp))
                                Row() {
                                    Text(
                                        "Rating",
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
                                Text("Winner!")
                            }
                        }
                    }
                }
                Row(modifier = Modifier.height(30.dp).background(light).width(500.dp)) {
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.width(500.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LotteriesViewPreview() {
    val mp = LotteriesView()
    CodeTheme {
        (
            mp.ShowScreen()
        )
    }
}