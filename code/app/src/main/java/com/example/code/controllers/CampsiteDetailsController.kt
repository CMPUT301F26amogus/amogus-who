package com.example.code.controllers

import Model
import com.example.code.model.Lottery

class CampsiteDetailsController(
    private val model : Model
) {
    /**
     * Puts the current user on the lottery's waitlist (US 01.02.01).
     *
     * @param lottery the campsite's lottery
     * @param onResult called with a message to show the user
     */
    public fun joinLottery(lottery: Lottery, onResult: (String) -> Unit) {
        if (lottery.id.isBlank()) {
            onResult("This campsite has no lottery yet")
            return
        }
        model.joinWaitlist(lottery = lottery) { joined ->
            if (joined) {
                onResult("You joined the lottery")
            } else {
                // the model doesn't say why, it's one of these
                onResult("Couldn't join. You may already be in it, or it's full.")
            }
        }
    }

    /**
     * Takes the current user off the lottery's waitlist (US 01.02.02).
     *
     * @param lottery the campsite's lottery
     * @param onResult called with a message to show the user
     */
    public fun leaveLottery(lottery: Lottery, onResult: (String) -> Unit) {
        if (lottery.id.isBlank()) {
            onResult("This campsite has no lottery yet")
            return
        }
        model.leaveWaitlist(lottery = lottery) { left ->
            if (left) {
                onResult("You left the lottery")
            } else {
                onResult("Couldn't leave the lottery")
            }
        }
    }
}
