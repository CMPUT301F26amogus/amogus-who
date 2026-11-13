/*
* CampsiteOwnerHomeView.kt : the homepage view for campsite owners
* Copyright (C) 2026 Nathaniel Edillon (nedillon@ualberta.ca)
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published
* by the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
* */

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
        Option("QR Codes", ""),
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