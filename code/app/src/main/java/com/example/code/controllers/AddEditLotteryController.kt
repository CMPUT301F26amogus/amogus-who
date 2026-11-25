package com.example.code.controllers

import Model
import com.example.code.model.Lottery
import com.example.code.model.LotteryStatus

class AddEditLotteryController(
    private val model: Model
) {

    /*
     * A campsite owner presses the save button and creates/updates a lottery
     * 
     * @param campsites a list of campsite IDs
     * @param numWinners the number of the winner cap
     * @param waitlistCapacity the number of the waitlist cap, defaults to 0
     * @param registrationStart the start date of registration 
     * @param registrationEnd the end date of registration 
     * @param bookingStart the start date of the booking period
     * @param bookingEnd the start date of the booking period
     * 
     */
    fun onSaveLottery(
        campsites: List<String>,
        numWinners: Int,
        waitlistCapacity: Int,
        registrationStart: String,
        registrationEnd: String,
        bookingStart: String,
        bookingEnd: String,
    ) {
        // TODO: add this lottery to the database
        // TODO: throw an exception when one of these args are invalid
        // if you're not sure about this part, leave it and we can work on 
        // exception handling later
    }

    /*
     * Cancel lottery creation/update
     * 
    */
    fun onCancel() {
        // some navigation: go back to the lotteries page, 
        // or just steal the "back" navigation function
    }
    
}