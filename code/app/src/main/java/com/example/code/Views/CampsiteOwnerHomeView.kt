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

package com.example.code.Views

import Option
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.tooling.preview.Preview

class CampsiteOwnerHomeView : HomeView() {
    override val bgColor = entrantBackgroundColor
    override val headerColor = ownerHeaderBackgroundColor
    override val headerTextColor = ownerHeaderTextColor
    override val footerColor = ownerFooterBackgroundColor
    override val footerTextColor = ownerFooterTextColor
    override val footerButtonColor = ownerFooterButtonColor
    override val options = mutableStateListOf(
        Option("My Campsites", ""),
        Option("Reservations", ""),
        Option("Lotteries", ""),
        Option("QR Codes", ""),
        Option("Messages", ""),
    )
}

@Preview(showBackground = true)
@Composable
fun CampsiteOwnerHomeViewPreview() {
    val homeView = CampsiteOwnerHomeView()
    homeView.ShowScreen()
}