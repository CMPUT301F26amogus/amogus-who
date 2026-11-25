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
    fun onEditLottery() {

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