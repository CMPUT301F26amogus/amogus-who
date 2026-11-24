package com.example.code.views

import Model
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.controllers.SearchResultsController
import com.example.code.model.Campsite
import com.example.code.ui.theme.CodeTheme

class SearchResultsView(M:Model): View() {
    var CampsiteList: List<Campsite> = emptyList()
    override fun update(model: Model) {
        CampsiteList = model.campsiteList
    }
    val controller = SearchResultsController(M)
    @Composable
    public fun ShowScreen(navController: NavController) {
        Column() {
            Row() {
                Header(
                    headerText = "Search Results",
                    backgroundColor = entrantHeaderBackgroundColor,
                    textColor = entrantHeaderTextColor,
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(color = secondaryGreenColor),
            ){
                LazyColumn(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    itemsIndexed(CampsiteList) { index, campsite ->
                        CampsiteCard(
                            campsite.name,
                            campsite.location,
                            campsite.maxOccupancy.toString(),
                            campsite.location,
                            if (campsite.averageRating != null)
                                {campsite.averageRating.toString()}
                            else
                                {"0.00"},
                            {controller.viewCampsite(campsite.id, navController)},
                            primaryGreenTextColor,
                            light,
                            light,
                            secondaryPurple,
                            tag = campsite.name + "Card"
                        ).Draw()
                    }
                }
            }
            Row() {
                Footer(
                    navController = navController,
                    textColor = entrantFooterTextColor,
                    backgroundColor = entrantFooterBackgroundColor,
                    buttonColor = entrantFooterButtonColor,
                )
            }
        }
    }
    @Composable
    private fun CampsiteRow(campsite: Campsite, navController: NavController) {
        Row(modifier = Modifier.padding(50.dp).height(210.dp).width(500.dp)){
            Column() {
                Row(
                    modifier = Modifier.height(160.dp).background(Color(0xff196c4b))
                        .width(500.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 15.dp).width(200.dp)){
                        Row() {
                            Text(campsite.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = light)
                        }
                        Row() {
                            Text(campsite.location, color = light)
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        Row(modifier = Modifier.background(light).padding(horizontal = 20.dp).fillMaxWidth().height(80.dp)) {
                            Text("Tags", fontSize = 13.sp)
                        }
                    }
                    Column(modifier = Modifier.width(50.dp)){
                        Spacer(modifier = Modifier.height(15.dp))
                        Row() {
                            Text(campsite.averageRating.toString(), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = light)
                        }
                        Spacer(modifier = Modifier.height(25.dp))
                        Row() {
                            Text("^: XX", fontWeight = FontWeight.Bold, color = light)
                        }
                    }
                }
                Row(modifier = Modifier.height(30.dp).background(light).width(500.dp)) {
                    Spacer(modifier = Modifier.width(180.dp))
                    Text(
                        "> Details", color = Color(0xff5530a4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { controller.viewCampsite(campsite.id, navController = navController) },
                    )
                }
            }
        }
    }
}

fun exampleCampsite(i : Int) : Campsite {
    return Campsite(
        location = "Banff $i"
    )
}

@Preview(showBackground = true)
@Composable
fun searchResultsViewPreview() {
    val mp = SearchResultsView(Model())
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