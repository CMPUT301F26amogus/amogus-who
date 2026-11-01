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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    protected open fun CustomHeader() {}

    @Composable
    private fun OptionRow(option: Option, onClickOption: (Option) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 16.dp)
                .clickable(enabled = true, onClick = { onClickOption(option) })
        ) {
            Row(
                modifier = Modifier
                    .background(entrantFooterBackgroundColor)
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
                            color = ownerHeaderBackgroundColor,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = ">",
                            fontSize = 30.sp,
                            modifier = Modifier.weight(0.1f),
                            color = ownerHeaderBackgroundColor,
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
    fun ShowScreen() {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Header(
                    backgroundColor = headerColor,
                    textColor = headerTextColor,
                    customContent = { CustomHeader() },
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
                                //navController.navigate(option.)
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
                    backgroundColor = footerColor,
                    textColor = footerTextColor,
                    buttonColor = footerButtonColor,
                )
            }
        }

    }

}

