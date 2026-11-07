package com.example.code.Views

import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Model
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
import com.example.code.Controllers.OwnerLotteriesController

class DummyLottery(
    val campsiteName: String,
    val location: String,
    val entrants: Int,
    val winners: Int,
    val drawDate: String,
    val state: String,
) {}

/**
 *  OwnerLotteriesView: the view for the owner to see their lotteries
 *
 */
class OwnerLotteriesView(model : Model) : View() {


    val controller by lazy { OwnerLotteriesController(model = model) }

    // modify to pull real lotteries from model
    val lotteries = mutableListOf(
        DummyLottery(
            campsiteName = "Site Alpha",
            location = "Downtown Edmonton",
            entrants = 54,
            winners = 3,
            drawDate = "Dec 10, 2026",
            state = "Open",
        ),
        DummyLottery(
            campsiteName = "Site Beta",
            location = "East Hastings",
            entrants = 105,
            winners = 5,
            drawDate = "Oct 24, 2026",
            state = "Drawn",
        ),
    )

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
    private fun LotteryCard(
        campsiteName: String,
        location: String,
        entrants: Int,
        winners: Int,
        drawDate: String,
        state: String,
    ) {
        val action = if (state == "Open") "Draw now" else "View winners"

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
                            text = campsiteName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                        Text(
                            text = location,
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
                            onClick = {/* Not a button in practice */},
                            modifier = Modifier
                                .height(35.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if (state == "Open") Color(0xff9bfa8f) else Color(0xffe2dde6),
                                contentColor = primaryGreenTextColor,
                            )
                        ) {
                            Text(state)
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SiteInfoBox(
                        info = "Entrants",
                        data = entrants.toString(),
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Winners",
                        data = winners.toString(),
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Draw",
                        data = drawDate,  // TODO: will this stay a string?
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
                    onClick = { controller.navigateToOwnerLottery() },
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
                    onClick = { /* controller.onLotteryActionClick() */ },
                    shape = CircleShape,
                    containerColor = primaryPurple,
                    contentColor = secondaryPurple,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Draw or View",
                        )
                    },
                    text = { Text(action) }
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
            items(count = lotteries.size) { lottery ->
                LotteryCard(
                    campsiteName = lotteries[lottery].campsiteName,
                    location = lotteries[lottery].location,
                    entrants = lotteries[lottery].entrants,
                    winners = lotteries[lottery].winners,
                    drawDate = lotteries[lottery].drawDate,
                    state = lotteries[lottery].state,
                )
                if (lottery != lotteries.size - 1) {
                    Spacer(
                        modifier = Modifier
                            .height(20.dp)
                    )
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                )
            }

        }
    }

    /**
     *  ShowScreen: render the page
     *
     */
    @Composable
    fun ShowScreen(navController: NavController) {
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

                navController = navController,
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
    val view = OwnerLotteriesView(model = Model())
    view.ShowScreen(rememberNavController())
}