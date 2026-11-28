package com.example.code.views

import Model
import View
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

open class ReviewsView (model: Model) : View(){
    var headerBackgroundColour = entrantHeaderBackgroundColor
    var headerTextColour = entrantHeaderTextColor
    var bodyBackgroundColour = secondaryGreenColor
    var footerTextColour = entrantFooterTextColor
    var footerBackgroundColor = entrantFooterBackgroundColor
    var footerButtonColor = entrantFooterButtonColor
    var headerText = "Reviews"

    override fun update(model: Model) {
        super.update(model)
    }

    @Composable
    fun ShowScreen(navController: NavController){

    }

    @Composable
    fun CampTitle(
        campName: String,
        campLocation: String,
        rating: String,
        textColor : Color
    ) {
        Row (
            modifier = Modifier
                .background(
                    color = cardAccent,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = campName,
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = campLocation,
                    color = textColor,
                    fontSize = 15.sp,
                )
            }
            Column(
                modifier = Modifier.height(48.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                IconButton(
                    onPress = {},
                    fillColour = primaryPurple,
                    textColor = secondaryPurple,
                    label = rating,
                    icon ={
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star",
                        )
                    },
                    tag = "RatingsDisplay"
                )
            }
        }
    }

    @Composable
    fun ReviewBody() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Column (
                modifier = Modifier
                    .f
            ) {

            }
        }
    }

    @Composable
    fun CreateBody() {

    }

}