package com.example.code.Views

import View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.code.ui.theme.CodeTheme

class MapView: View() {
    @Composable
    public fun ShowScreen(){
        Column() {
            Row() {
                header(
                    headerText = "Map",
                    backgroundColor = entrantHeaderBackgroundColor,
                    textColor = entrantHeaderTextColor,
                )
            }
            Spacer(Modifier.weight(1f))
            Row() {
                footer(
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
fun MapViewPreview() {
    val mp = MapView()
    CodeTheme {
        (
            mp.ShowScreen()
        )
    }
}