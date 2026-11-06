/*
* HomeView.kt : the homepage view
* Copyright (C) 2026 Nathaniel Edillon (nedillon@ualberta.ca), Cash Popik (cpopik@ualberta.ca)
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

import androidx.navigation.NavController
import Option
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

abstract class HomeView : View() {

    abstract val bgColor: Color
    abstract val headerColor: Color
    abstract val headerTextColor: Color
    abstract val footerColor: Color
    abstract val footerTextColor: Color
    abstract val footerButtonColor: Color
    abstract val options: MutableList<Option>

    @Composable
    protected open fun CustomHeader(navController: NavController) {}

    @Composable
    protected fun ProfileButton(
        onClickProfile: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        FilledIconButton(
            onClick = onClickProfile,
            modifier = modifier,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = secondaryPurple,
                contentColor = Color.White,
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Profile",
            )
        }
    }

    @Composable
    protected fun NotificationsButton(
        onClickNotifications: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        FilledIconButton(
            onClick = onClickNotifications,
            modifier = modifier,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = primaryPurple,
                contentColor = primaryTextColor,
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
            )
        }
    }

    @Composable
    private fun OptionRow(option: Option, onClickOption: (Option) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 16.dp)
                .clickable(enabled = true, onClick = { onClickOption(option) })
                .testTag(option.name)
        ) {
            Row(
                modifier = Modifier
                    .background(primaryGreenColor)
                    .height(60.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row() {
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            text = option.name,
                            fontSize = 30.sp,
                            modifier = Modifier.weight(1f),
                            color = primaryGreenTextColor,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = ">",
                            fontSize = 30.sp,
                            modifier = Modifier.weight(0.1f),
                            color = primaryGreenTextColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }

    @Composable
    fun ShowScreen(navController: NavController) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Header(
                    backgroundColor = headerColor,
                    textColor = headerTextColor,
                    customContent = { CustomHeader(navController) },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(67f)    // could be any weight, fill remaining space with content
                    .background(bgColor)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(options) {
                        index,
                        option: Option ->
                        OptionRow(
                            option = option,
                            onClickOption = {
                                // options without a screen yet have an empty route
                                if (option.route.isNotEmpty()) {
                                    navController.navigate(option.route)
                                }
                            }
                        )
                        if (index < options.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Footer(
                    navController = navController,
                    backgroundColor = footerColor,
                    textColor = footerTextColor,
                    buttonColor = footerButtonColor,
                )
            }
        }
    }
}

