package com.example.code.Views

import Tab
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.code.ui.theme.CodeTheme

class ReservationsView: View() {
    val bgColor = entrantBackgroundColor
    val headerbgColor = entrantHeaderBackgroundColor
    val headerTextColor = entrantHeaderTextColor
    val footerTextColor = entrantFooterTextColor
    val footerbgColor = entrantFooterBackgroundColor
    val footerButtonColor = entrantFooterButtonColor
    val tabs = mutableStateListOf(
        Tab(name = "Current", color = entrantFooterBackgroundColor, option = {}),
        Tab(name = "Previous", color = entrantBackgroundColor, option = {}),
    )
    @Composable
    public fun ShowScreen() {
        Column() {
            Row() {
                Header(
                    headerText = "Reservations",
                    backgroundColor = headerbgColor,
                    textColor = headerTextColor,
                )
            }
            Row(modifier = Modifier.height(50.dp)){
                TabManager(tabs)
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f).background(bgColor)){

            }
            Row() {
                Footer(
                    textColor = footerTextColor,
                    backgroundColor = footerbgColor,
                    buttonColor = footerButtonColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationsViewPreview() {
    val mp = ReservationsView()
    CodeTheme {
        (
            mp.ShowScreen()
        )
    }
}