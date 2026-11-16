/*
* MapView.kt : the view for a campsite map
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

import com.example.code.Routes
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.ui.theme.CodeTheme
import Model
import com.example.code.controllers.SearchController

class SearchView(M: Model): View() {
    var controller = SearchController(M)
    @Composable
    public fun ShowScreen(navController: NavController) {
        var search by remember {mutableStateOf("")}
        var arrival by remember {mutableStateOf("")}
        var departure by remember {mutableStateOf("")}
        var park by remember {mutableStateOf("")}
        var campsite by remember {mutableStateOf("")}
        var guests by remember { mutableStateOf("") }
        var tags by remember {mutableStateOf("")}
        Column() {
            Row() {
                Header(
                    customContent = {
                            Row() {
                                TextField(
                                    value = search,
                                    onValueChange = {
                                        search = it
                                    },
                                    label = {
                                        Text("Campsite Name")
                                    },
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                                        .background(color = creamBG).padding(20.dp).width(200.dp)
                                )
                                ElevatedButton(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = light),
                                    modifier = Modifier.width(100.dp).height(90.dp),
                                ) {
                                    Text(text = "Search", color = Color(0xff2a2828))
                                }
                            }
                    }
                )
            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(entrantFooterBackgroundColor).height(120.dp).padding(5.dp)){
                Row(modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)
                    .background(color = creamBG).padding(20.dp).fillMaxWidth()) {
                    Column(
                    ) {
                        Text("Arrival Date", fontSize = 15.sp, color = textColor)
                        TextField(
                            value = arrival,
                            onValueChange = {
                                arrival = it
                            },
                            label = {
                                Text("mm/dd/yy", fontSize = 15.sp)
                            },
                            modifier = Modifier
                                .background(color = creamBG).width(120.dp)
                        )
                    }
                    Column(
                    ) {
                        Text("Departure Date", fontSize = 15.sp, color = textColor)
                        TextField(
                            value = departure,
                            onValueChange = {
                                departure = it
                            },
                            label = {
                                Text("mm/dd/yy", fontSize = 15.sp)
                            },
                            modifier = Modifier
                                .background(color = creamBG).width(120.dp)
                        )
                    }
                    ElevatedButton(
                        onClick =
                            {
                            departure = ""
                            arrival = ""
                            },
                        colors = ButtonDefaults.buttonColors(containerColor = light),
                        modifier = Modifier.width(90.dp).height(90.dp).padding(horizontal = 15.dp),
                    ) {
                        Text(text = "X", color = Color(0xff2a2828), fontSize = 25.sp)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(secondaryGreenColor).padding(5.dp).height(120.dp).fillMaxWidth()){
                Row(modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)
                    .background(color = creamBG).padding(20.dp).fillMaxWidth()) {
                    Column(
                    ) {
                        Text("Park", fontSize = 15.sp, color = textColor)
                        TextField(
                            value = park,
                            onValueChange = {
                                park = it
                            },
                            label = {
                                Text("name", fontSize = 15.sp)
                            },
                            modifier = Modifier
                                .background(color = creamBG).width(120.dp)
                        )
                    }
                    Column(
                    ) {
                        Text("Campsite", fontSize = 15.sp, color = textColor)
                        TextField(
                            value = campsite,
                            onValueChange = {
                                campsite = it
                            },
                            label = {
                                Text("name", fontSize = 15.sp)
                            },
                            modifier = Modifier
                                .background(color = creamBG).width(120.dp)
                        )
                    }
                    ElevatedButton(
                        onClick = {
                            park = ""
                            campsite = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = light),
                        modifier = Modifier.width(90.dp).height(90.dp).padding(horizontal = 15.dp),
                    ) {
                        Text(text = "X", color = Color(0xff2a2828), fontSize = 25.sp)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(entrantFooterBackgroundColor).padding(5.dp).height(120.dp).fillMaxWidth()){
                Row(modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)
                    .background(color = creamBG).padding(20.dp).width(160.dp)) {
                    Column(
                    ) {
                        Text("Number of Guests:", fontSize = 15.sp, color = textColor)
                        TextField(
                            value = guests,
                            onValueChange = {
                                guests = it
                            },
                            label = {
                                Text("Number", fontSize = 15.sp)
                            },
                            modifier = Modifier
                                .background(color = creamBG).width(120.dp)
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(secondaryGreenColor).padding(5.dp).height(120.dp).fillMaxWidth()){
                Row(modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)
                    .background(color = creamBG).padding(20.dp).width(120.dp)) {
                    Column(
                    ) {
                        Text("Tags:", fontSize = 15.sp, color = textColor)
                        TextField(
                            value = tags,
                            onValueChange = {
                                tags = it
                            },
                            label = {
                                Text("tags", fontSize = 15.sp)
                            },
                            modifier = Modifier
                                .background(color = creamBG).width(120.dp)
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(entrantFooterBackgroundColor).padding(5.dp).height(50.dp).fillMaxWidth()){
                ElevatedButton(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = light),
                    modifier = Modifier.width(130.dp).height(50.dp).padding(horizontal = 15.dp),
                ) {
                    Text(text = "< Return", color = Color(0xff5530a4), fontSize = 13.sp,
                        modifier = Modifier.clickable(enabled = true, onClick = {
                            // only go back if there is a screen to go back to
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            }
                        }))
                }
                ElevatedButton(
                    onClick = {
                        search = ""
                        arrival = ""
                        departure = ""
                        park = ""
                        campsite = ""
                        guests = ""
                        tags = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = light),
                    modifier = Modifier.width(130.dp).height(50.dp).padding(horizontal = 15.dp),
                ) {
                    Text(text = "X CLEAR", color = Color(0xff5530a4), fontSize = 13.sp)
                }
                ElevatedButton(
                    onClick = { navController.navigate(Routes.SEARCH_RESULTS) },
                    colors = ButtonDefaults.buttonColors(containerColor = light),
                    modifier = Modifier.width(130.dp).height(50.dp).padding(horizontal = 15.dp),
                ) {
                    Text(text = "Search", color = Color(0xff5530a4), fontSize = 13.sp,
                        modifier = Modifier.clickable(enabled = true, onClick = {
                            controller.search(name = campsite, navController = navController)
                        }))
                }
            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row() {
                Footer(
                    navController = navController,
                    textColor = entrantFooterTextColor,
                    backgroundColor = entrantFooterBackgroundColor,
                    buttonColor = entrantFooterButtonColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchViewPreview() {
    val mp = SearchView(Model())
    CodeTheme {
        (
            mp.ShowScreen(rememberNavController())
        )
    }
}
