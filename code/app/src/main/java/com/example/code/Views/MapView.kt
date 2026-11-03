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

package com.example.code.Views

import View
import android.graphics.Paint.Align
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontSynthesis.Companion.Weight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.ui.theme.CodeTheme

class MapView: View() {
    @Composable
    public fun ShowScreen(){
        Column() {
            Row() {
                Header(
                    headerText = "Map",
                    backgroundColor = entrantHeaderBackgroundColor,
                    textColor = entrantHeaderTextColor,
                )
            }
            Row(modifier = Modifier.padding(50.dp).height(150.dp).width(500.dp)){
                Column() {
                    Row(
                        modifier = Modifier.height(120.dp).background(Color(0xffcadac8))
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
                                Text("MM/DD/YY | MM/DD/YY", fontSize = 13.sp)
                            }
                        }
                        Column(modifier = Modifier.width(50.dp)){
                            Spacer(modifier = Modifier.height(15.dp))
                            Row() {
                                Text("Rating", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(25.dp))
                            Row() {
                                Text("^: XX", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(modifier = Modifier.height(30.dp).background(light).width(500.dp)) {
                        Spacer(modifier = Modifier.width(180.dp))
                        Text(
                            "> Details", color = Color(0xff5530a4),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Row() {
                Footer(
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
fun MapViewPreview() {
    val mp = MapView()
    CodeTheme {
        (
            mp.ShowScreen()
        )
    }
}