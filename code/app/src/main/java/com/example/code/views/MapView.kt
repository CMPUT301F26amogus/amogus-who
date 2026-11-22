package com.example.code.views

import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import Model
import View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.code.ui.theme.CodeTheme

class MapView: View() {
    override fun update(model: Model) {

    }
    @Composable
    public fun ShowScreen(navController: NavController) {
        Column() {
            Row() {
                Header(
                    headerText = "Map",
                    backgroundColor = entrantHeaderBackgroundColor,
                    textColor = entrantHeaderTextColor,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row() {
                Footer(
                    navController = navController,
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
            mp.ShowScreen(rememberNavController())
        )
    }
}