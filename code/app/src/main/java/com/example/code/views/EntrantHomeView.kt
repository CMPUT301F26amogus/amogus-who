/*
* EntrantHomeView.kt : the homepage view for entrants
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class EntrantHomeView : HomeView() {
    override val bgColor = secondaryGreenColor
    override val headerColor = entrantHeaderBackgroundColor
    override val headerTextColor = entrantHeaderTextColor
    override val footerColor = entrantFooterBackgroundColor
    override val footerTextColor = entrantFooterTextColor
    override val footerButtonColor = entrantFooterButtonColor
    override val options = mutableStateListOf(
        Option("My Reservations", Routes.RESERVATIONS),
        Option("My Lotteries", Routes.LOTTERIES),
        Option("Search", Routes.SEARCH),
        Option("Scan QR Codes", ""),
        Option("Messages", ""),
    )

    @Composable
    private fun SearchPill(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryPurple,
                contentColor = primaryTextColor,
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text("Search")
        }

    }

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

            SearchPill(onClick = { navController.navigate(Routes.SEARCH) }, modifier = Modifier)

            Spacer(modifier = Modifier.width(10.dp))

            NotificationsButton(
                onClickNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EntrantHomeViewPreview() {
    val homeView = EntrantHomeView()
    homeView.ShowScreen(rememberNavController())
}