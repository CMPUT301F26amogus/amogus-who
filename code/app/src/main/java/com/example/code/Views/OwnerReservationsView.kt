package com.example.code.Views

import Tab
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class OwnerReservationsView : View() {

    private val cardPrimary = Color(0xffcad9c8)
    private val cardAccent = Color(0xfff3fff3)

    val tabs = mutableStateListOf(
        Tab(name = "Current", color = entrantFooterBackgroundColor, option = {}),
        Tab(name = "Previous", color = secondaryGreenColor, option = {}),
    )

    @Composable
    private fun DateInfoBox(startDate: String, endDate: String) {
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = cardAccent),
        ) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .height(14.dp),
            ) {
                Text(
                    text = startDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing / 2))
                VerticalDivider(modifier = Modifier)
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing / 2))
                Text(
                    text = endDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                )
            }
        }
    }

    @Composable
    private fun SiteInfoBox(info: String, data: String) {
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = cardAccent),
        ) {
            Row(
                modifier = Modifier
//                    .height(14.dp)
                    .padding(10.dp),
            ) {
                Text(
                    text = "$info: ",
                    fontSize = 12.sp,
                    color = primaryGreenTextColor,
                )
                Text(
                    text = data,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                )
            }
        }
    }

    @Composable
    private fun CampsiteCard() {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .background(color = cardPrimary)
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .weight(1f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Column(
                        modifier = Modifier,
                    ) {
                        Text(
                            text = "John Pork",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                        Text(
                            text = "Site B - Dust2",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    DateInfoBox(
                        startDate = "May 8, 2001",
                        endDate = "May 11, 2001",
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Guests",
                        data = "4",    // TODO: update with real data
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Nights",
                        data = "3",  // TODO: update with real data
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(color = cardAccent)
                    .padding(10.dp),
            ) {
                ExtendedFloatingActionButton(
                    onClick = { /* ... */ },
                    shape = CircleShape,
                    containerColor = cardAccent,
                    contentColor = primaryTextColor,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "Reject",
                        )
                    },
                    text = { Text("Reject") }
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                ExtendedFloatingActionButton(
                    onClick = { /* ... */ },
                    shape = CircleShape,
                    containerColor = cardAccent,
                    contentColor = primaryTextColor,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Accept",
                        )
                    },
                    text = { Text("Accept") }
                )
            }
        }

    }

    @Composable
    private fun Content() {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CampsiteCard()
            }

            item {
                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                )
            }
        }
    }

    @Composable
    fun ShowScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Header(
                    backgroundColor = ownerHeaderBackgroundColor,
                    textColor = ownerHeaderTextColor,
                    headerText = "Reservations",
                )

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(ownerHeaderBackgroundColor),
            ) {
                Row(modifier = Modifier.height(50.dp)){
                    TabManager(tabs)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(primaryGreenColor)
                        .padding(20.dp),
                ) {
                    Content()
                }
            }

            Footer(
                backgroundColor = ownerFooterBackgroundColor,
                textColor = ownerFooterTextColor,
                buttonColor = ownerFooterButtonColor,
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun OwnerReservationsViewPreview() {
    val view = OwnerReservationsView()
    view.ShowScreen()
}