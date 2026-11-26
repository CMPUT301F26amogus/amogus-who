package com.example.code.views

import Model
import com.example.code.Routes
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.controllers.OwnerCampsiteListController
import com.example.code.model.Campsite


/**
 *  OwnerCampsiteListView: the view for the owner to see their campsites
 *
 */
class OwnerCampsiteListView(model: Model) : View() {
   val controller = OwnerCampsiteListController(model)
    // modify to pull real list from model

    var campsites : List<Campsite> = emptyList()
    override fun update(model: Model) {
        val myID = model.currentDeviceId
        model.getCampsiteByOwner(
            onResult = { result ->
                campsites = result
            }
        )
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
                    headerText = "My Campsites",
                )

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(primaryGreenColor),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    itemsIndexed(campsites) { index, c ->
                        OwnerCampsiteCard(
                            title = c.name,
                            subtitle = c.location,
                            rating = if (c.averageRating != null) {
                                c.averageRating.toString()
                            } else {
                                "0.00"
                            },
                            onDetail = { controller.onCampsiteDetails(navController, c.id) },
                            onEdit = { controller.onEditCampsite(navController, c.id) },
                            colourPrimary = cardPrimary,
                            colourSecondary = cardAccent,
                            colourTitleText = primaryGreenTextColor,
                            colourActionText = secondaryPurple,
                            tag = "Card"
                        ).Draw()
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = { controller.onRegisterCampsite(navController) },
                                shape = CircleShape,
                                containerColor = primaryPurple,
                                contentColor = primaryTextColor,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add Button"
                                    )
                                },
                                text = { Text("Register Campsite") },
                                modifier = Modifier
                                    .testTag("RegisterCampsiteButton")
                                    .padding(horizontal = 12.dp),
                            )
                        }
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

}

@Preview(showBackground = true)
@Composable
fun OwnerCampsiteListViewPreview() {
    val listView = OwnerCampsiteListView(Model())
    listView.campsites = List(2) {
        Campsite(
            name = "Site - $it",
            location = "Banff $it"
        )
    }
    listView.ShowScreen(rememberNavController())
}