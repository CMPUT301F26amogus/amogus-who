/*
* MapView.kt : the view for a campsite map
* Copyright (C) 2026 Cash Popik (cpopik@ualberta.ca)
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

import View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.code.ui.theme.CodeTheme

class SearchView: View() {
    @Composable
    public fun ShowScreen(){
        var search by remember {mutableStateOf("")}
        Column() {
            Row() {
                Header(
                    customContent = {
                        TextField(
                            value = search,
                            onValueChange = {
                                search = it
                            },
                            label = {
                                Text("Camp Name")
                            },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp).background(color = creamBG).padding(20.dp).fillMaxWidth()
                        )
                    }
                )
            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(entrantFooterBackgroundColor).height(120.dp).fillMaxWidth()){

            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(secondaryGreenColor).height(120.dp).fillMaxWidth()){

            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(entrantFooterBackgroundColor).height(120.dp).fillMaxWidth()){

            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(secondaryGreenColor).height(120.dp).fillMaxWidth()){

            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row(modifier = Modifier.background(entrantFooterBackgroundColor).height(120.dp).fillMaxWidth()){

            }
            HorizontalDivider(modifier = Modifier.height(2.dp).background(dark))
            Row() {
                Footer(
                    textColor = entrantFooterTextColor,
                    backgroundColor = entrantFooterBackgroundColor,
                    buttonColor = entrantFooterButtonColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchViewPreview() {
    val mp = SearchView()
    CodeTheme {
        (
            mp.ShowScreen()
        )
    }
}