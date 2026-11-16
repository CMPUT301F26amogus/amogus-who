package com.example.code.model

/**
 * one entrant on a lottery waitlist
 *
 * @property lotteryId which lottery they joined
 * @property userId device id of the entrant
 * @property joinedAt when they joined
 * @property status if they are waiting won lost declined or reserved
 */
class WaitlistEntry(
    val lotteryId: String="", // needed default value for the database to convert to obj
    val userId: String="",
    val joinedAt: Long = 0L,
    var status: EntryStatus = EntryStatus.WAITING,
){

}
