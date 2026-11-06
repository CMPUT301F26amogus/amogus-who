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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class OwnerLotteriesView : View() {

    private val cardPrimary = Color(0xffcad9c8)
    private val cardAccent = light

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
    private fun NewLotteryCard() {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .background(color = light)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = "Start a new lottery",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primaryGreenTextColor,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Campsite",
                color = primaryTextColor,
            )
            Spacer(modifier = Modifier.height(10.dp))

        }
    }

    @Composable
    private fun LotteryCard() {
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
                            fontWeight = FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                        Text(
                            text = "Location",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier
                            .width(90.dp)
                    ) {
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .height(35.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xff9bfa8f),
                                contentColor = primaryGreenTextColor,
                            )
                        ) {
                            Text("Open")
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SiteInfoBox(
                        info = "Entrants",
                        data = "100",     // TODO: update with real data
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Winners",
                        data = "5",    // TODO: update with real data
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Draw",
                        data = "Sept 11, 2001",  // TODO: update with real data
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
                    containerColor = primaryPurple,
                    contentColor = secondaryPurple,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Details",
                        )
                    },
                    text = { Text("Draw now") }
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
                LotteryCard()
            }

            item {
                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                )
            }

            item {
                NewLotteryCard()
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
                    headerText = "Lotteries",
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
fun OwnerLotteriesViewPreview() {
    val view = OwnerLotteriesView()
    view.ShowScreen()
}