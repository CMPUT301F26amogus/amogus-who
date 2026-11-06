package com.example.code.Controllers

import Model
import com.example.code.Model.Lottery

class EntrantLotteriesController(
    private val model : Model
) {
    /**
     * Confirms a lottery
     */
    public fun confirmLottery(lottery: Lottery){

    }

    /**
     * Cancels a lottery
     */
    public fun cancelLottery(lottery: Lottery){
        model.leaveWaitlist(
            lottery = lottery,
        ){}
    }

    /**
     * Sets the active site to view
     */
    public fun viewSite(){

    }
}