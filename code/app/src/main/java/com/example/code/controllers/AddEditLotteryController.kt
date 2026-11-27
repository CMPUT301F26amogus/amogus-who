package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.model.Lottery
import com.example.code.model.LotteryStatus
import com.example.code.Routes

class AddEditLotteryController(
    private val model: Model
) {

    /**
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
    fun onSave(
        campsites: List<String>,
        numWinners: Int,
        waitlistCapacity: Int,
        registrationStart: String,
        registrationEnd: String,
        bookingStart: String,
        bookingEnd: String,
    ) {

        model.createLottery(campsites[0], registrationStart, registrationEnd, bookingStart, bookingEnd, numWinners, waitlistCapacity) {
            result ->
            result.onSuccess { print("success") }
            result.onFailure { e-> print(e.message) } // The exception is caught in the model, this is error message if the exception is thrown.
        }


    }

    /**
     * Cancel lottery creation/update
     * @param navController to switch out of this page
     */
    fun onCancel(navController: NavController) {
        navController.popBackStack()
    }

    /**
     * Delete the lottery being edited
     * @param lottery to be deleted
     * @param navController to switch out of this page once deleted
     */
    fun onDelete(lottery: Lottery,navController: NavController) {
        model.deleteLottery(lottery)
        navController.popBackStack()
    }
    
}