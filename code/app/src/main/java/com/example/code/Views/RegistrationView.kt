/*
* CampsiteDetailsView.kt : the view for registering with the app
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
import com.example.code.Routes
import View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.code.Controllers.RegistrationController
import com.example.code.ui.theme.CodeTheme
import Model

class RegistrationView(M:Model): View() {
    var controller = RegistrationController(M)
    @Composable
    public fun ShowScreen(navController: NavController) {
        var name by remember {
            mutableStateOf("")
        }
        var number by remember {
            mutableStateOf("")
        }
        var email by remember {
            mutableStateOf("")
        }
        Column() {
            Row() {
                Header(
                    headerText = "Register",
                    backgroundColor = entrantHeaderBackgroundColor,
                    textColor = entrantHeaderTextColor,
                )
            }
            Row(modifier = Modifier.weight(1f)){
                Column(){
                    Spacer(modifier = Modifier.height(50.dp))
                    Text("Name",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = textColor,
                        fontWeight = FontWeight.Bold)
                    TextField(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        label = {
                            Text("Enter your full name")
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Email",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = textColor,
                        fontWeight = FontWeight.Bold)
                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        label = {
                            Text("Enter your email")
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Phone Number",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = textColor,
                        fontWeight = FontWeight.Bold)
                    TextField(
                        value = number,
                        onValueChange = {
                            number = it
                        },
                        label = {
                            Text("Enter your phone number")
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    Row() {
                        ElevatedButton(
                            onClick = {
                                if (controller.register(name = name, email = email, phoneNumber = number)) {
                                    goToHome(navController, Routes.ENTRANT_HOME)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffe8def8)),
                            modifier = Modifier.width(160.dp).height(60.dp)
                                .padding(horizontal = 20.dp),
                        ) {
                            Text(text = "Register As Entrant", color = Color(0xff40139a))
                        }
                        ElevatedButton(
                            onClick = {
                                if (controller.register(name = name, email = email, phoneNumber = number)) {
                                    goToHome(navController, Routes.OWNER_HOME)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xffe8def8)),
                            modifier = Modifier.width(160.dp).height(60.dp).padding(horizontal = 20.dp),
                        ) {
                            Text(text = "Register As Owner", color = Color(0xff40139a))
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            Row() {
                Footer(
                    navController = navController,
                    textColor = entrantFooterTextColor,
                    backgroundColor = entrantFooterBackgroundColor,
                    buttonColor = entrantFooterButtonColor,
                    excludeButtons = true
                )
            }
        }
    }

    /* Gemini 3.6 Flash
     * "There are build errors in RegistrationView.kt and OwnerEditCampsiteController.kt, can you find them and apply some fixes to them?"
     * 2026-11-11
     */
    private fun goToHome(navController: NavController, homeRoute: String) {
        navController.navigate(homeRoute) {
            popUpTo(Routes.REGISTRATION) { inclusive = true }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun RegistrationViewPreview() {
    val M = Model()
    val mp = RegistrationView(M)
    CodeTheme {
        (
        mp.ShowScreen(rememberNavController())
        )
    }
}