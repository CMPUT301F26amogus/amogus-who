package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.Routes.ADD_EDIT_LOTTERY
import com.example.code.model.Lottery
import com.example.code.model.LotteryStatus

class OwnerLotteriesController(
    private val model: Model
) {
    /**
     * Navigates to the owner lottery screen.
     */
    fun onEditLottery(navController: NavController, lotteryToDisplay: Lottery? = null) {  // Needs to take a NavController and Lottery object
        model.settTheLottery(lotteryToDisplay)
        navController.navigate(ADD_EDIT_LOTTERY)
    }

    /**
     * Draw winner for a lottery
     */
    fun onLotteryActionClick(lottery: Lottery) {
        if (lottery.status == LotteryStatus.OPEN) {
            model.drawLotteryWinner(lottery.id) { }

        }
    }

    fun onDrawLottery() {

    }

    fun onViewWinners() {

    }

    fun onWaitlist() {

    }
}