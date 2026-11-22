package com.example.code.Model

import Model
import java.time.Instant

/**
 * one entrant on a lottery waitlist
 *
 * @property lotteryId which lottery they joined
 * @property deviceId device id of the entrant
 * @property joinedAt when they joined
 * @property status if they are waiting won lost declined or reserved
 */
class WaitlistEntry(
    val id: String = "",
    val lotteryId: String="",
    val deviceId: String="",
 //   val joinedAt: Long = 0L, this was here before, but not in UML do we need this? TODO: Chek this
    var status: EntryStatus = EntryStatus.WAITING,
){

}
