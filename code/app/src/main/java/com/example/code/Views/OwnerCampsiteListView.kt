package com.example.code.Views

import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class OwnerCampsiteListView : View() {

    @Composable
    fun CampsiteCard() {}

    @Composable
    private fun Content() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(creamBG),    // TODO: remove
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                CampsiteCard()
            }

            item {
                ExtendedFloatingActionButton(
                    onClick = { /* ... */ },
                    shape = CircleShape,
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Register campsite") },
                    text = { Text("Register Campsite") },
                )
            }
        }
    }

    @Composable
    fun ShowScreen() {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
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
                    .background(primaryGreenColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
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