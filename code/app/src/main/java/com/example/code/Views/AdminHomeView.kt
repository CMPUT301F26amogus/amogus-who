/*
* AdminHomeView.kt : the homepage view for admin
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

class AdminHomeView : HomeView() {
    override val bgColor = entrantBackgroundColor
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
        Option("View as Camper", ""),
        Option("View as Owner", ""),
    )
}

@Preview(showBackground = true)
@Composable
fun AdminHomeViewPreview() {
    val homeView = AdminHomeView()
    homeView.ShowScreen()
}