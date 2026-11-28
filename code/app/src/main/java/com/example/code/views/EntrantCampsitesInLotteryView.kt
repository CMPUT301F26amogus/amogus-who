package com.example.code.views

import Model
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.code.controllers.EntrantCampsitesInLotteryController
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.ui.theme.CodeTheme

/**View for displaying campsites within a lottery
 *
 * it pulls the lottery from `theLottery` in model and then grabs the list of `campIds` from it
 * then for each campId it calls `getCampsite` from model to grab the data for the Campsite Cards
 * */
class EntrantCampsitesInLotteryView (M: Model) : SearchResultsView(M) {

    override val controller = EntrantCampsitesInLotteryController(M)
    var showReservationButton: Boolean = true

    override fun update(model: Model) {
        // TODO: update `Campsites` with the campsites within a lottery
        val myLottery = model.gettTheLottery()
        var tempCampsiteIds: List<String> = emptyList()
        val tempCampsiteList = mutableListOf<Campsite>()

        if (myLottery != null){
            tempCampsiteIds = myLottery.campsiteIds
            for (campID in tempCampsiteIds){
                model.getCampsite(campID) { result ->
                    if (result != null){ tempCampsiteList.add(result) }
                }
            }
        }

        CampsiteList = tempCampsiteList
    }

    @Composable
    override fun Contents(navController: NavController) {
        LazyColumn(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            itemsIndexed(CampsiteList) { index, campsite ->
                EntrantCampsiteCard(
                    campsite.name,
                    campsite.location,
                    campsite.maxOccupancy.toString(),
                    if (campsite.averageRating != null)
                    {campsite.averageRating.toString()}
                    else
                    {"0.00"},
                    {controller.onDetails(campsite.id, navController)},
                    primaryGreenTextColor,
                    light,
                    light,
                    secondaryPurple,
                    tag = campsite.name + "Card",
                    onReserve = {controller.onReserve(campsite.id, navController)},
                    showReserve = showReservationButton
                ).Draw()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EntrantCampsitesInLotteryPreview() {
    val mp = EntrantCampsitesInLotteryView(Model())
    mp.CampsiteList = List(2) {
        Campsite(
            name = "Site - $it",
            location = "Banff $it"
        )
    }
    CodeTheme {
        (
                mp.ShowScreen(rememberNavController())
        )
    }
}