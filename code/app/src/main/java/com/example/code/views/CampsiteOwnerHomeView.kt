package com.example.code.views

import com.example.code.Routes
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Option
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class CampsiteOwnerHomeView : HomeView() {
    override val bgColor = secondaryGreenColor
    override val headerColor = ownerHeaderBackgroundColor
    override val headerTextColor = ownerHeaderTextColor
    override val footerColor = ownerFooterBackgroundColor
    override val footerTextColor = ownerFooterTextColor
    override val footerButtonColor = ownerFooterButtonColor
    override val options = mutableStateListOf(
        Option("My Campsites", Routes.OWNER_CAMPSITES),
        Option("Reservations", Routes.OWNER_RESERVATIONS),
        Option("Lotteries", Routes.OWNER_LOTTERIES),
        Option("QR Codes", Routes.OWNER_QR),
        Option("Messages", ""),
    )

    @Composable
    override fun CustomHeader(navController: NavController) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            ProfileButton(
                onClickProfile = { navController.navigate(Routes.PROFILE) },
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            NotificationsButton(
                onClickNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampsiteOwnerHomeViewPreview() {
    val homeView = CampsiteOwnerHomeView()
    homeView.ShowScreen(rememberNavController())
}