package com.example.code.views

import Model
import View
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.code.model.Campsite
import com.example.code.model.Rating
import com.google.apps.card.v1.Columns

open class ReviewsView (model: Model) : View(){
    var headerBackgroundColor = entrantHeaderBackgroundColor
    var headerTextColor = entrantHeaderTextColor
    var bodyBackgroundColor = secondaryGreenColor
    var footerTextColor = entrantFooterTextColor
    var footerBackgroundColor = entrantFooterBackgroundColor
    var footerButtonColor = entrantFooterButtonColor
    var headerText = "Reviews"

    var campName = ""
    var location = ""
    var aveRating = ""
    var reviewList : List<Rating> = emptyList()
    var myCampsite : Campsite? = null

    override fun update(model: Model) {
        myCampsite = model.gettTempCampsite()
        if (myCampsite != null){
            reviewList = myCampsite!!.ratings
            aveRating = myCampsite!!.ratingText
            campName = myCampsite!!.name
            location = myCampsite!!.location
        }
        else {
            reviewList = emptyList()
            aveRating = "0.0"
        }
    }

    @Composable
    fun ShowScreen(navController: NavController){
        var showReviews by remember { mutableStateOf(true) }
        Column() {
            Row() {
                Header(
                    headerText = "Search Results",
                    backgroundColor = entrantHeaderBackgroundColor,
                    textColor = entrantHeaderTextColor,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = secondaryGreenColor
                    )
                    .padding(horizontal = 14.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(space = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CampTitle(
                    campName = campName,
                    campLocation = location,
                    rating = aveRating,
                    textColor = primaryGreenTextColor
                )
                if (showReviews){
                    ReviewBody { showReviews = !showReviews }
                }
                else {
                    CreateBody()
                }
            }
            Row() {
                Footer(
                    navController = navController,
                    textColor = footerTextColor,
                    backgroundColor = footerBackgroundColor,
                    buttonColor = footerButtonColor,
                )
            }
        }
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
                ).Draw()
            }
        }
    }

    @Composable
    fun ReviewBody(onWrite: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LazyColumn (
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ){
                items(reviewList){ review ->
                    ReviewItem(
                        entrantName = review.userId,
                        ratingValue = review.score,
                        title = "",
                        text = ""
                    )
                }
            }
            IconButton(
                onPress = onWrite,
                fillColour = light,
                textColor = secondaryPurple,
                label = "Write Review",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Create,
                        contentDescription = "Pencil"
                    )
                },
                tag = "WriteReviewButton"
            ).Draw()
        }
    }

    @Composable
    fun ReviewItem(
        entrantName: String,
        ratingValue: Int,
        title: String? = null,
        text: String? = null,
    ) {
        Column(  // The whole thing
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(  //  The upper half
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy( space = 16.dp, alignment = Alignment.Start),
                verticalAlignment = Alignment.Top
            ) {
                Column(  //  title, names and stuff
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row( // headline and reviews
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
                    )
                    {
                        if (title != null) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = primaryTextColor
                            )
                        }

                        Row () {
                            for (i in 1..5){
                                if (i <= ratingValue) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Star",
                                    )
                                }
                                else {
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = "Star",
                                    )
                                }
                            }
                        }

                    }

                    Text(
                        text = entrantName,
                        fontSize = 14.sp,
                        color = primaryTextColor
                    )
                }
                if (text != null) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = text,
                            fontSize = 14.sp,
                            color = primaryTextColor
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun CreateBody() {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(

            ) { }
        }
    }

}