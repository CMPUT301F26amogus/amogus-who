/*
* CampsiteDetailsView.kt : the view for updating profile settings
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ProfileSettingsView: View() {
    @Composable
    public fun ShowScreen(){
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var number by remember { mutableStateOf("") }
        var notifications by remember { mutableStateOf("") }
        Column() {
            Row() {
                Header(
                    headerText = "Profile Settings",
                    backgroundColor = entrantHeaderBackgroundColor,
                    textColor = entrantHeaderTextColor,
                )
            }
            Column(modifier = Modifier.fillMaxWidth().weight(1f).background(secondaryGreenColor),
                horizontalAlignment = Alignment.CenterHorizontally){
                Spacer(modifier = Modifier.height(10.dp))
                Box(Modifier.size(120.dp).background(entrantHeaderTextColor, shape = CircleShape))
                ElevatedButton(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                    modifier = Modifier.width(160.dp).height(60.dp),
                ) {
                    Text(text = "Change Picture", color = Color(0xff40139a))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.padding(horizontal = 20.dp).background(color = creamBG)
                    .padding(20.dp).width(280.dp)) {
                    TextField(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        label = {
                            Text("Full Name")
                        },
                        modifier = Modifier.width(220.dp)
                    )
                    Text("Confirm",modifier = Modifier.padding(vertical = 15.dp, horizontal = 5.dp), color = textColor)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.padding(horizontal = 20.dp).background(color = creamBG)
                    .padding(20.dp).width(280.dp)) {
                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        label = {
                            Text("email@example.com")
                        },
                        modifier = Modifier.width(220.dp)
                    )
                    Text("Confirm",modifier = Modifier.padding(vertical = 15.dp, horizontal = 5.dp), color = textColor)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.padding(horizontal = 20.dp).background(color = creamBG)
                    .padding(20.dp).width(280.dp)) {
                    TextField(
                        value = number,
                        onValueChange = {
                            number = it
                        },
                        label = {
                            Text("(555)-555-5555")
                        },
                        modifier = Modifier.width(220.dp)
                    )
                    Text("Confirm",modifier = Modifier.padding(vertical = 15.dp, horizontal = 5.dp), color = textColor)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.background(color = creamBG).padding(20.dp).width(290.dp)){
                    Text("Allow Notifications?",modifier = Modifier.padding(horizontal = 20.dp)
                        , color = textColor,
                        fontWeight = FontWeight.Bold)
                    Text("No",modifier = Modifier, color = textColor)
                    Text(" | ",modifier = Modifier.padding(horizontal = 11.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp)
                    Text("Yes",modifier = Modifier, color = textColor)
                }
                Spacer(modifier = Modifier.height(10.dp))
                ElevatedButton(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffad2cc)),
                    modifier = Modifier.width(140.dp),
                ) {
                    Text(text = "Delete Profile", color = Color(0xff2a2828))
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
fun ProfileSettingsViewPreview() {
    val mp = ProfileSettingsView()
    mp.ShowScreen()
}