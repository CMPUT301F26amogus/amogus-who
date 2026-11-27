package com.example.code.controllers

import Model
import androidx.compose.runtime.MutableState
import androidx.core.text.isDigitsOnly
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
        numWinners: String,
        waitlistCapacity: String,
        registrationStart: String,
        registrationEnd: String,
        bookingStart: String,
        bookingEnd: String,
    ) : String {
        var winners = 0
        var waitlist = 0
        var errorMessage = ""

        if (!numWinners.isDigitsOnly()) {
            winners = numWinners.toInt()
        }

        if (!waitlistCapacity.isDigitsOnly()) {
            waitlist = waitlistCapacity.toInt()
        }

        model.createLottery(
            campsiteIds = campsites,
            registrationStart = registrationStart,
            registrationEnd = registrationEnd,
            bookingStart = bookingStart,
            bookingEnd = bookingEnd,
            numWinners = winners,
            waitlistCapacity = waitlist,
        ) {
            result ->
            result.onSuccess { print("success") }
            result.onFailure { e ->
                print(e.message) // The exception is caught in the model, this is error message if the exception is thrown.
                errorMessage = e.message!!
            }
        }

        return errorMessage
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