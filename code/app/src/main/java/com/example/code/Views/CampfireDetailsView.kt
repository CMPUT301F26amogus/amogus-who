package com.example.code.Views

import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.ui.theme.CodeTheme

class CampfireDetailsView(): View() {
    @Composable
    public fun ShowScreen(){
        val bgColor = entrantBackgroundColor
        val headerbgColor = entrantHeaderBackgroundColor
        val headerTextColor = entrantHeaderTextColor
        val footerTextColor = entrantFooterTextColor
        val footerbgColor = entrantFooterBackgroundColor
        val footerButtonColor = entrantFooterButtonColor
        Column() {
            Row() {
                header(
                    headerText = "CAMPSITENAME",
                    subheaderText = "LOCATION",
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
                                Text(text = "Rating: RATING", fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.fillMaxWidth().height(50.dp).background(light)) {
                                Text(text = "Max Occupancy: OCCUPANCY", fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.fillMaxWidth().height(50.dp).background(light)) {
                                Text(text = "Address: ADDRESS", fontSize = 15.sp)
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
                                Text(text = "Tags:", fontSize = 15.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Row(modifier = Modifier.height(80.dp)){
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(Modifier.fillMaxWidth().background(bgColor)) {
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.width(400.dp).height(65.dp).background(light)) {
                                Text(text = "TOS:", fontSize = 15.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Row(modifier = Modifier.height(90.dp)){
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(Modifier.width(250.dp).background(bgColor)) {
                            Spacer(modifier = Modifier.height(15.dp).background(bgColor).fillMaxWidth())
                            Row(modifier = Modifier.width(250.dp).height(65.dp).background(light)) {
                                Text(text = "Details:", fontSize = 15.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(Modifier.width(200.dp).background(bgColor)) {
                            ElevatedButton(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffe8def8)),
                                modifier = Modifier.width(140.dp),
                            ) {
                                Text(text = "Edit Photos", color = Color(0xff40139a))
                            }
                            ElevatedButton(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffad2cc)),
                                modifier = Modifier.width(140.dp),
                            ) {
                                Text(text = "Delete", color = Color(0xff2a2828))
                            }
                        }
                    }
                }
            }
            Row() {
                footer(
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
fun CampfireDetailsPreview() {
    val v = CampfireDetailsView()
    CodeTheme {
        (
                v.ShowScreen()
        )
    }
}