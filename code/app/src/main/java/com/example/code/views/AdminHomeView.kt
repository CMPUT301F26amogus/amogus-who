package com.example.code.views

import com.example.code.Routes
import androidx.navigation.compose.rememberNavController
import Option
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.tooling.preview.Preview

class AdminHomeView : HomeView() {
    override val bgColor = secondaryGreenColor
    override val headerColor = adminHeaderBackgroundColor
    override val headerTextColor = adminHeaderTextColor
    override val footerColor = adminFooterBackgroundColor
    override val footerTextColor = adminFooterTextColor
    override val footerButtonColor = adminFooterButtonColor
    override val options = mutableStateListOf(
        Option("Browse Profiles", ""),
        Option("Browse Campsites", ""),
        Option("Browse Images", ""),
        Option("Notification Logs", ""),
        Option("Message Logs", ""),
        Option("View as Camper", Routes.ENTRANT_HOME),
        Option("View as Owner", Routes.OWNER_HOME),
    )
}

@Preview(showBackground = true)
@Composable
fun AdminHomeViewPreview() {
    val homeView = AdminHomeView()
    homeView.ShowScreen(rememberNavController())
}