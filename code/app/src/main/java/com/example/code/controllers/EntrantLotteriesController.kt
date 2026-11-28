package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.Routes

class EntrantLotteriesController(
    private val model : Model
) {
    // see [19] in Citation.md
    /**
     * opens the reserve screen for one campsite of a lottery the user won (US 01.04.01 01.04.02)
     *
     * a lottery can have more than one campsite so the one whose row they tapped is the one they picked
     * the reserve screen shows its available dates and lets them pick theirs or decline
     *
     * @param lottery the lottery they won
     * @param campsite which of its campsites they picked
     * @param navController used to open the reserve screen
     */
    public fun reserveFromLottery(lottery: Lottery, campsite: Campsite, navController: NavController) {
        model.settLotteryReservation(lottery, campsite)
        navController.navigate(Routes.RESERVE_CAMPSITE)
    }

    // see [19] in Citation.md
    /**
     * says no to a lottery win eg they changed their mind (US 01.04.03)
     *
     * @param lottery the lottery they won
     * @param onResult a message to show the user
     */
    public fun declineLotteryWin(lottery: Lottery, onResult: (String) -> Unit) {
        model.declineLotteryWin(lottery) { result ->
            result
                .onSuccess { onResult("You declined the lottery") }
                .onFailure { onResult(it.message ?: "Couldn't decline the lottery") }
        }
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
    public fun viewCampsite(cid: String, navController: NavController){
        // getCampsite is async so the campsite is only here inside the callback,
        // set it and navigate in there or the details screen opens with nothing
        model.getCampsite(cid){ result ->
            if (result != null) {
                model.settTempCampsite(result) // why are we even using 2 t's in this model func, idk man
                navController.navigate(Routes.CAMPSITE_DETAILS)
            }
        }
    }
}