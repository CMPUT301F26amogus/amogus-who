package com.example.code.controllers

import Model
import com.example.code.model.Lottery
import com.example.code.model.LotteryStatus

class OwnerLotteriesController(
    private val model: Model
) {
    /**
     * Navigates to the owner lottery screen.
     */
    fun navigateToOwnerLottery() {

    }

    /**
     * Draw winner for a lottery
     */
    fun onLotteryActionClick(lottery: Lottery) {
        if (lottery.status == LotteryStatus.OPEN) {
            model.drawLotteryWinner(lottery.id) { }

        }
    }

    // TODO: should auto draw, we need to still figure it out
    fun onDrawLottery() {

    }

    //Navigates the owner to a view that contains the winners of the lottery
    fun onViewWinners() {

    }
    // Navigates the owner to a view that contains the entrants in the waitlist of the lottery
    fun onWaitlist() {

    }
}