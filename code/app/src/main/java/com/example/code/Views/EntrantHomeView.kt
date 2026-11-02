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

package com.example.code.Views

import Option
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class EntrantHomeView : HomeView() {
    override val bgColor = entrantBackgroundColor
    override val headerColor = entrantHeaderBackgroundColor
    override val headerTextColor = entrantHeaderTextColor
    override val footerColor = entrantFooterBackgroundColor
    override val footerTextColor = entrantFooterTextColor
    override val footerButtonColor = entrantFooterButtonColor
    override val options = mutableStateListOf(
        Option("My Reservations", ""),
        Option("My Lotteries", ""),
        Option("Search", ""),
        Option("Scan QR Codes", ""),
        Option("Messages", ""),
    )

    @Composable
    override fun CustomHeader() {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            ProfileButton(
                onClickProfile = { /*TODO*/ },
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            NotificationsButton(
                onClickProfile = { /*TODO*/ },
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EntrantHomeViewPreview() {
    val homeView = EntrantHomeView()
    homeView.ShowScreen()
}