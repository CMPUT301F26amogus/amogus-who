package com.example.code.Controllers

import Model
import com.example.code.Model.Lottery

class CampsiteDetailsController(
    private val model : Model
) {
    /**
     * Joins a lottery for the active user and site
     */
    public fun joinLottery(lottery: Lottery){
        model.joinWaitlist(
            lottery = lottery,
        ){}
    }
    /**
     * Leaves a lottery for the active user and site
     */
    public fun leaveLottery(lottery: Lottery){
        model.leaveWaitlist(
            lottery = lottery,
        ){}
    }

    /**
     * Joins the waitlist for an active user and site
     */
    public fun joinWaitlist(){

    }

    /**
     * Leaves the waitlist for an active user and site
     */
    public fun leaveWaitlist(){

    }
}