package com.example.code.views

import Model
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.code.controllers.EntrantCampsitesInLotteryController

class EntrantCampsitesInLotteryView (M: Model) : SearchResultsView(M) {

    val thisController = EntrantCampsitesInLotteryController(M)

    override fun update(model: Model) {
        super.update(model)
    }

    @Composable
    override fun Contents(navController: NavController) {
        super.Contents(navController)
    }
}