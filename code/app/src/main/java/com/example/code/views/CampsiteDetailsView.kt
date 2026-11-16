/*
* CampsiteDetailsView.kt : the view for details of a campsite
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

package com.example.code.views

import Model
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.code.controllers.CampsiteDetailsController
import com.example.code.ui.theme.CodeTheme
import com.example.code.model.Campsite

class CampsiteDetailsView(M:Model): View() {
    var campsiteToView: Campsite? = null
    val controller = CampsiteDetailsController(M)
    override fun update(model: Model) {
        campsiteToView = model.tempCampsite
    }

    @Composable
    public fun ShowScreen(navController: NavController) {
        val bgColor = secondaryGreenColor
        val headerbgColor = entrantHeaderBackgroundColor
        val headerTextColor = entrantHeaderTextColor
        val footerTextColor = entrantFooterTextColor
        val footerbgColor = entrantFooterBackgroundColor
        val footerButtonColor = entrantFooterButtonColor
        Column() {
            Row() {
                Header(
                    headerText = campsiteToView!!.name,
                    subheaderText = campsiteToView!!.location,
                    backgroundColor = headerbgColor,
                    textColor = headerTextColor,
                )
            }
            Row(modifier = Modifier.fillMaxWidth().height(65.dp).background(light)) {
                Text(text = "Owner:", fontSize = 20.sp, color = gray)
                Spacer(Modifier.width(15.dp))
                Column(Modifier.fillMaxWidth().height(100.dp)){
                    Spacer(Modifier.height(25.dp))
                    Text(text = "OWNERNAME", fontSize = 30.sp, color = dark)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f).background(bgColor)) {
                Column(modifier = Modifier.width(500.dp)) {
                    //main screen here
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.height(180.dp)) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(Modifier.width(150.dp).background(bgColor)) {
                            //ratings widget
                            Row(modifier = Modifier.fillMaxWidth().height(50.dp).background(light)) {
                                Text(text = "Rating: ${campsiteToView!!.averageRating.toString()}", fontSize = 15.sp, color = textColor)
                            }
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.fillMaxWidth().height(50.dp).background(light)) {
                                Text(text = "Max Occupancy: ${campsiteToView!!.maxOccupancy}", fontSize = 15.sp, color = textColor)
                            }
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.fillMaxWidth().height(50.dp).background(light)) {
                                Text(text = "Address: ${campsiteToView!!.location}", fontSize = 15.sp, color = textColor)
                            }
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        Column(Modifier.width(225.dp).background(light)) {
                            //map widget
                            Spacer(modifier = Modifier.fillMaxHeight())
                        }
                        Spacer(modifier = Modifier.width(15.dp))
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Row(modifier = Modifier.height(100.dp)){
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(Modifier.fillMaxWidth().background(bgColor)) {
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.width(400.dp).height(70.dp).background(light)) {
                                Text(text = "", fontSize = 13.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Row(modifier = Modifier.height(100.dp)){
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(Modifier.fillMaxWidth().background(bgColor)) {
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.width(400.dp).height(70.dp).background(light)) {
                                Text(text = "Tags:", fontSize = 15.sp, color = textColor)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Row(modifier = Modifier.height(80.dp)){
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(Modifier.fillMaxWidth().background(bgColor)) {
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.width(400.dp).height(65.dp).background(light)) {
                                Text(text = "TOS: ${campsiteToView!!.rulesAndPolicy}", fontSize = 15.sp, color = textColor)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Row(modifier = Modifier.height(90.dp)){
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(Modifier.width(250.dp).background(bgColor)) {
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.width(250.dp).height(65.dp).background(light)) {
                                Text(text = "Details: ${campsiteToView!!.description}", fontSize = 15.sp, color = textColor)
                            }
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(Modifier.width(200.dp).background(bgColor)) {
                            ElevatedButton(
                                onClick = {controller.joinLottery(campsiteToView!!.lottery)},
                                colors = ButtonDefaults.buttonColors(containerColor = light),
                                modifier = Modifier.width(140.dp),
                            ) {
                                Text(text = "Join Lottery", color = Color(0xff40139a))
                            }
                            ElevatedButton(
                                onClick = {controller.leaveLottery(campsiteToView!!.lottery)},
                                colors = ButtonDefaults.buttonColors(containerColor = light),
                                modifier = Modifier.width(140.dp),
                            ) {
                                Text(text = "Join Waitlist", color = Color(0xff2a2828))
                            }
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
}

@Preview(showBackground = true)
@Composable
fun CampsiteDetailsPreview() {
    val v = CampsiteDetailsView(Model())
    CodeTheme {
        (
            v.ShowScreen(rememberNavController())
        )
    }
}