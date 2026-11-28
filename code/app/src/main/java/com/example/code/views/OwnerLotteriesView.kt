package com.example.code.views

import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Model
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue        // [4]
import androidx.compose.runtime.mutableStateOf  // [4]
import androidx.compose.runtime.setValue        // [4]
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.controllers.OwnerLotteriesController
import com.example.code.model.Lottery
import com.example.code.model.LotteryStatus
import com.example.code.model.WaitlistEntry


/**
 *  OwnerLotteriesView: the page that lists an owner's lotteries
 *
 *  @param model the app model to pull backend information from
 *
 */
class OwnerLotteriesView(model : Model) : View() {
    val openStatusColor = Color(0xff9bfa8f)
    val drawnStatusColor = Color(0xffe2dde6)
    val controller by lazy { OwnerLotteriesController(model = model) }

    // modify to pull real lotteries from model
    var lotteries by mutableStateOf<List<Lottery>>(emptyList()) // [4]

    override fun update(model: Model) {
        model.getLotteriesByOwner(ownerId = model.currentDeviceId) { lotteries = it }
    }

    /**
     * FOR PREVIEW ONLY: insert test data for developer testing
     *
     * @param lotteries a list of lotteries
     *
     */
    fun previewLotteryInsert(lotteries: List<Lottery>) {
        this.lotteries = lotteries
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
                    .padding(5.dp),
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
    private fun LotteryCard(
        lottery: Lottery,
        navController: NavController
    ) {

        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .background(color = cardPrimary)
                .fillMaxWidth()
                .height(150.dp)
        ) {
            // Information Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .weight(1f),
            ) {
                // Lottery name and status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Column(
                        modifier = Modifier,
                    ) {
                        Text(
                            text = lottery.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreenTextColor,
                        )
                    }

                    // Fill whole middle space
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(12.dp))
                            .background(
                                color =
                                    if (lottery.status == LotteryStatus.OPEN) openStatusColor
                                    else drawnStatusColor
                            )
                            .width(90.dp)
                            .height(30.dp)
                            .padding(5.dp)
                    ) {
                        Text(
                            text = lottery.status.toString(),
                            color = primaryGreenTextColor,
                            fontWeight = FontWeight.Bold,
                        )

                    }
                }

                // Info about waitlist, winners, and draw deadline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .weight(1f)
                        .padding(top = 5.dp)
                ) {
                    SiteInfoBox(
                        info = "Entrants",
                        data = lottery.waitlist.size.toString(),
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Winners",
                        data = 0.toString(),    // TODO: needs a method to obtain the list of winners
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    SiteInfoBox(
                        info = "Draw",
                        data = lottery.registrationEnd,
                    )
                }
            }

            // Action section: edit lottery, view waitlist, view winners
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(color = cardAccent)
                    .padding(10.dp),
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        controller.onEditLottery(
                            navController = navController,
                            lotteryToDisplay = lottery
                        )
                    },
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

                Spacer(modifier = Modifier.weight(1f))

                ExtendedFloatingActionButton(
                    onClick = { controller.onWaitlist(navController, Lottery()) },
                    shape = CircleShape,
                    containerColor = cardAccent,
                    contentColor = primaryTextColor,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "View waitlist",
                        )
                    },
                    text = { Text("Waitlist") }
                )
            }
        }

    }

    @Composable
    private fun Content(navController: NavController) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(count = lotteries.size) { i ->
                LotteryCard(
                    lotteries[i],
                    navController
                )
                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                )
            }

            // TODO: change to add lottery button
            item {
                ExtendedFloatingActionButton(
                    onClick = {
                        controller.onNewLottery(navController)
                    },
                    shape = CircleShape,
                    containerColor = primaryPurple,
                    contentColor = secondaryPurple,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create Lottery",
                        )
                    },
                    text = { Text("New Lottery") }
                )
            }

        }
    }

    /**
     *  ShowScreen: render the page
     *
     *  @param navController the controller to navigate throughout the app
     *
     */
    @Composable
    fun ShowScreen(navController: NavController) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Header(
                backgroundColor = ownerHeaderBackgroundColor,
                textColor = ownerHeaderTextColor,
                headerText = "Lotteries",
            )

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
                    Content(navController)
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

    // dummy waitlists
    val waitlistAlpha = listOf(
        WaitlistEntry(),
        WaitlistEntry(),
        WaitlistEntry(),
    )

    val waitlistBeta = listOf(
        WaitlistEntry(),
        WaitlistEntry(),
    )

    // dummy lottery list
    val lotteries = listOf(
        Lottery(
            id = "id_alpha1",
            name = "Lotto Alpha",
            waitlist = waitlistAlpha,
            registrationStart = "2026-10-01",
            registrationEnd = "2026-10-24",
            status = LotteryStatus.INACTIVE
        ),
        Lottery(
            id = "id_beta2",
            name = "Lotto Beta",
            waitlist = waitlistBeta,
            registrationStart = "2026-10-01",
            registrationEnd = "2026-12-24",
        ),
    )
    view.previewLotteryInsert(lotteries)
    view.ShowScreen(rememberNavController())
}