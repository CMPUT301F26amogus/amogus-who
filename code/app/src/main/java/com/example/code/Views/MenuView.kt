package com.example.code.Views

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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.ui.theme.CodeTheme

class Option(name:String,route:String){
    var name = name
    var route = route
}
class AdminMenuView: View() {
    val bgColor = entrantBackgroundColor
    val headerbgColor = entrantHeaderBackgroundColor
    val headerTextColor = entrantHeaderTextColor
    val footerTextColor = entrantFooterTextColor
    val footerbgColor = entrantFooterBackgroundColor
    val footerButtonColor = entrantFooterButtonColor
    val options = mutableStateListOf(
        Option("Browse Profiles","CampfireDetailsView"),
        Option("Browse Campsites","CampfireDetailsView"))
    @Composable
    public fun ShowScreen(){
        Column() {
            Row() {
                TextHeader(
                    backgroundColor = headerbgColor,
                    textColor = headerTextColor,
                )
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f).background(bgColor)) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(options) { index,option:Option ->
                        OptionRow(option = option, onClickOption ={
                            //navController.navigate(option.)
                        })
                        if (index < options.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
            Row() {
                footer(
                    textColor = footerTextColor,
                    backgroundColor = footerbgColor,
                    buttonColor = footerButtonColor,
                )
            }
        }
    }
    @Composable
    private fun OptionRow(option: Option, onClickOption: (Option)->Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 16.dp)
                .clickable(enabled = true, onClick = { onClickOption(option) })
        ) {
            Row(modifier = Modifier.background(entrantFooterBackgroundColor).height(60.dp)) {
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
}

@Preview(showBackground = true)
@Composable
fun AdminMenuViewPreview() {
    val mp = AdminMenuView()
    CodeTheme {
        (
                mp.ShowScreen()
        )
    }
}