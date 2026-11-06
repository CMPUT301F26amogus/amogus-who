/*
* View.kt : the base View class
* Copyright (C) 2026 Cash Popik (cpopik@ualberta.ca), Nathaniel Edillon (nedillon@ualberta.ca)
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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

interface ViewObserver<M> {
    fun update( model: M )
}

abstract class View: ViewObserver<Model>{
    override fun update(model: Model) {

    }
    //Common colors go here
    protected val light = Color(0xfff3fff3)
    protected val dark = Color(0xff000000)
    protected val gray = Color(0xff7d7e7d)
    protected val primaryGreenColor = Color(0xff93cd8b)
    protected val secondaryGreenColor = Color(0xffcae99d)
    protected val primaryGreenTextColor = Color(0xff054f33)
    protected val primaryTextColor = Color(0xff4a4459)
    protected val primaryPurple = Color(0xffe8def8)
    protected val secondaryPurple = Color(0xFF6750A4)
    protected val tertiaryPurple = Color(0xfff7f2fa)
    protected val entrantHeaderBackgroundColor = Color(0xff28b019)
    protected val entrantHeaderTextColor = Color(0xffffffff)
    protected val entrantFooterBackgroundColor = primaryGreenColor
    protected val entrantFooterButtonColor = Color(0xff9bfa8e)
    protected val entrantFooterTextColor = primaryTextColor

    protected val ownerHeaderBackgroundColor = primaryGreenTextColor
    protected val ownerHeaderTextColor = Color(0xffffffff)
    protected val ownerFooterBackgroundColor = Color(0xff57bc78)
    protected val ownerFooterButtonColor = Color(0xff9bfa8e)
    protected val ownerFooterTextColor = primaryTextColor

    protected val adminHeaderBackgroundColor = Color(0xff033131)
    protected val adminHeaderTextColor = Color(0xffffffff)
    protected val adminFooterBackgroundColor = Color(0xff5aae8c)
    protected val adminFooterButtonColor = Color(0xff9bfa8e)
    protected val adminFooterTextColor = primaryTextColor
    protected val textColor = Color(0xff12553a)
    protected val creamBG = Color(0xffe4eeed)
    protected val cardPrimary = Color(0xffcad9c8)
    protected val cardAccent = light


    private val headerHeight = 120
    //Generic Component Composables go here
    @Composable
    protected fun Header(
        headerText: String = "",
        subheaderText: String = "",
        backgroundColor: Color = entrantHeaderBackgroundColor,
        textColor: Color = entrantHeaderTextColor,
        headerTextSize: Int = 40,
        subheaderTextSize: Int = 30,
        customContent: @Composable (() -> Unit)? = null,
    ) {
        Column(Modifier
            .fillMaxWidth()
            .height(headerHeight.dp)
            .background(backgroundColor)
        ) {
            if (customContent == null) {
                if (headerText != "") {
                    Spacer(Modifier.size(25.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(backgroundColor)
                    ) {
                        Spacer(Modifier.size(20.dp))
                        Text(
                            text = headerText,
                            fontSize = headerTextSize.sp,
                            color = textColor,
                        )
                    }
                }
                if (subheaderText != "") {
                    Spacer(Modifier.size(5.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(backgroundColor)
                    ) {
                        Spacer(Modifier.size(60.dp))
                        Text(
                            text = subheaderText,
                            fontSize = subheaderTextSize.sp,
                            color = textColor,
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(30.dp))
                customContent() // takes up 90.dp
            }
        }
    }
    @Composable
    protected fun Footer(
        backgroundColor: Color,
        textColor: Color,
        buttonColor: Color,
        excludeButtons: Boolean = false,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(backgroundColor)
        ) {
            Spacer(Modifier.size(25.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(backgroundColor)
            ) {
                if (!excludeButtons) {
                    Spacer(Modifier.size(40.dp))
                    ElevatedButton(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    ) {
                        Text(
                            text = "Back",
                            color = textColor,
                        )
                    }
                    Spacer(Modifier.size(40.dp))
                    ElevatedButton(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier.testTag("Home"),
                    ) {
                        Text(
                            text = "Home",
                            color = textColor,
                        )
                    }
                    Spacer(Modifier.size(40.dp))
                    ElevatedButton(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier.testTag("Profile"),
                    ) {
                        Text(
                            text = "Profile",
                            color = textColor,
                        )
                    }
                }
            }
        }
    }
    @Composable
    protected fun TabManager(tabs:List<Tab>, state:String){
        Column(modifier = Modifier.height(50.dp)) {
            LazyRow(modifier = Modifier.height(50.dp)) {
                itemsIndexed(tabs) { index,tab: Tab ->
                    TabCol(tab = tab, state)
                }
            }
        }
    }
    @Composable
    private fun TabCol(tab: Tab, state: String) {
        Column(
            modifier = Modifier
                .height(60.dp)
                .clickable(enabled = true, onClick = { tab.option(state) })
        ) {
            Row(
                modifier = Modifier
                    .background(tab.color)
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.width(10.dp))
                Column(Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tab.name,
                        fontSize = 25.sp,
                        color = primaryGreenTextColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}
class Tab(var name: String, var color: Color, var option: (String) -> Unit) {}
class Option(var name: String, var route: String) {}
