package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.model.Lottery
import com.example.code.model.User

// The following controller updates are from Google, Gemini, "Fix AddEditLotteryController for multi-campsite and multi-owner lotteries", 2026-11-27

class AddEditLotteryController(
    private val model: Model
) {

    /**
     * A campsite owner presses the save button and creates/updates a lottery
     * 
     * @param navController to switch out of this page once saved
     * @param campsites a list of campsite IDs
     * @param numWinners the number of the winner cap
     * @param waitlistCapacity the number of the waitlist cap, defaults to 0
     * @param registrationStart the start date of registration
     * @param registrationEnd the end date of registration
     * @param bookingStart the start date of the booking period
     * @param bookingEnd the end date of the booking period
     * @param name optional name of the lottery
     * @param onResult optional callback with the created lottery or failure
     */
    fun onSave(
        navController: NavController? = null, // [4]
        campsites: List<String>,
        numWinners: String,
        waitlistCapacity: String,
        registrationStart: String,
        registrationEnd: String,
        bookingStart: String,
        bookingEnd: String,
        name: String = "Unnamed Lottery",
        onResult: (Result<Lottery>) -> Unit = {},
    ) : String {

        var errorMessage = ""

        if (model.gettTheLottery() == null) {
            model.createLottery(
                campsiteIds = campsites,
                registrationStart = registrationStart,
                registrationEnd = registrationEnd,
                bookingStart = bookingStart,
                bookingEnd = bookingEnd,
                numWinners = numWinners.toIntOrNull() ?: 0,
                waitlistCapacity = waitlistCapacity.toIntOrNull() ?: 0,
                name = name
            ) {
                result ->
                result.onSuccess {
                    print("success")
                    navController?.popBackStack() // [4]
                }
                result.onFailure { e ->
                    print(e.message) // The exception is caught in the model, this is error message if the exception is thrown.
                    errorMessage = e.message!!
                }
                onResult(result)
            }
        } else { // the following else block is AI-generated [3]
            val lottery = model.gettTheLottery()!!
            model.updateLottery(
                lotteryId = lottery.id,
                campsiteIds = campsites,
                registrationStart = registrationStart,
                registrationEnd = registrationEnd,
                bookingStart = bookingStart,
                bookingEnd = bookingEnd,
                numWinners = numWinners.toIntOrNull() ?: 0,
                waitlistCapacity = waitlistCapacity.toIntOrNull() ?: 0,
                name = name,
            ) { result -> // START citation [4]
                result.onSuccess {
                    navController?.popBackStack()
                }
                result.onFailure { exception ->
                    print(exception.message)
                } // END citation [4]
            }
        }
        return errorMessage
    }

    /**
     * Loads all other campsite owners so the current owner can invite them to add campsites.
     */
    fun loadCampsiteOwners(onResult: (List<User>) -> Unit) {
        model.getAllCampsiteOwners(includeSelf = false, onResult = onResult)
    }

    /**
     * Invites another campsite owner to add their campsite to the lottery.
     */
    fun onInviteOwner(
        lotteryId: String,
        invitedOwnerId: String,
        campsiteId: String,
        onResult: (Result<Unit>) -> Unit = {},
    ) {
        model.inviteOwnerToLottery(
            lotteryId = lotteryId,
            invitedOwnerId = invitedOwnerId,
            campsiteId = campsiteId,
            onResult = onResult,
        )
    }


    /**
     * Accepts a lottery invite for the current owner and attaches their campsite.
     */
    fun onAcceptInvite(
        lotteryId: String,
        campsiteId: String,
        onResult: (Result<Unit>) -> Unit = {},
    ) {
        model.acceptLotteryInvite(
            lotteryId = lotteryId,
            campsiteId = campsiteId,
            onResult = onResult,
        )
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
    fun onDelete(lottery: Lottery,navController: NavController) { // the following fn was modded [4]
        model.deleteLottery(lottery) { deleted ->
            if (deleted) navController.popBackStack()
        }
    }
}