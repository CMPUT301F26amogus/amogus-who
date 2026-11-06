package com.example.code.Controllers

import Model
import com.example.code.Model.Lottery
import com.example.code.Model.LotteryStatus

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