package com.example.code.Views

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
import com.example.code.ui.theme.CodeTheme

class RegistrationView: View() {
    @Composable
    public fun ShowScreen(){
        var username by remember {
            mutableStateOf("")
        }
        var password by remember {
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
                    Text("Username",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = textColor,
                        fontWeight = FontWeight.Bold)
                    TextField(
                        value = username,
                        onValueChange = {
                            username = it
                        },
                        label = {
                            Text("username")
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Password",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = textColor,
                        fontWeight = FontWeight.Bold)
                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        label = {
                            Text("Enter your password")
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
                    ElevatedButton(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xffe8def8)),
                        modifier = Modifier.width(160.dp).height(60.dp).padding(horizontal = 20.dp),
                    ) {
                        Text(text = "Register", color = Color(0xff40139a))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            Row() {
                footer(
                    textColor = entrantFooterTextColor,
                    backgroundColor = entrantFooterBackgroundColor,
                    buttonColor = entrantFooterButtonColor,
                    excludeButtons = true
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationViewPreview() {
    val mp = RegistrationView()
    CodeTheme {
        (
                mp.ShowScreen()
                )
    }
}