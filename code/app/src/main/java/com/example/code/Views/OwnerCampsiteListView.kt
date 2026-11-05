package com.example.code.Views

import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class OwnerCampsiteListView : View() {

    private val cardPrimary = Color(0xffcad9c8)
    private val cardAccent = Color(0xfff3fff3)

    @Composable
    private fun SiteInfoBox(info: String, data: String) {
        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .background(color = cardAccent),
        ) {
            Row(
                modifier = Modifier
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
                            text = "Campsite Name",
                            fontSize = 20.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                        Text(
                            text = "Location",
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .height(35.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { /* ... */ },
                            shape = CircleShape,
                            containerColor = tertiaryPurple,
                            contentColor = secondaryPurple,
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Star,
                                    contentDescription = "Edit",
                                )
                            },
                            text = { Text("4.0") }  // fill with real rating
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SiteInfoBox(
                        info = "Upcoming",
                        data = "0",     // TODO: update with real data
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Requests",
                        data = "0",    // TODO: update with real data
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Lottery",
                        data = "None",  // TODO: update with real data
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
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                        )
                    },
                    text = { Text("Edit") }
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
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Details",
                        )
                    },
                    text = { Text("Details") }
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

            item {
                ExtendedFloatingActionButton(
                    onClick = { /* ... */ },
                    shape = CircleShape,
                    containerColor = primaryPurple,
                    contentColor = primaryTextColor,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Register campsite"
                        )
                           },
                    text = { Text("Register Campsite") },
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
                    headerText = "My Campsites",
                )

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(primaryGreenColor),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
fun OwnerCampsiteListViewPreview() {
    val listView = OwnerCampsiteListView()
    listView.ShowScreen()
}