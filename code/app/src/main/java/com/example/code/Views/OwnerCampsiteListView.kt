package com.example.code.Views

import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class OwnerCampsiteListView : View() {

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
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(primaryGreenColor)
                ) {

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