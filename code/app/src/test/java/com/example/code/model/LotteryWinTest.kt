package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

// US 01.04.01 01.04.02 01.04.03 only a winner who hasnt answered yet can reserve or decline
class LotteryWinTest {

    private fun lottery(status: EntryStatus?, bookingStart: String = "2027-04-10", bookingEnd: String = "2027-04-20") =
        Lottery(
            id = "lottery-1",
            campsiteIds = listOf("site-1", "site-2"),
            registrationStart = "2027-03-01",
            registrationEnd = "2027-04-01",
            bookingStart = bookingStart,
            bookingEnd = bookingEnd,
            numWinners = 1,
            waitlist = listOfNotNull(
                WaitlistEntry(id = "someone-else", lotteryId = "lottery-1", status = EntryStatus.NOT_SELECTED),
                status?.let { WaitlistEntry(id = "me", lotteryId = "lottery-1", status = it) },
            ),
        )

    private val duringBooking = LocalDate.of(2027, 4, 15)

    @Test
    fun entryFor_findsTheirEntry() {
        assertEquals(EntryStatus.SELECTED, lottery(EntryStatus.SELECTED).entryFor("me")?.status)
        assertNull(lottery(null).entryFor("me"))
    }

    @Test
    fun winner_canReserveAndDecline() {
        val won = lottery(EntryStatus.SELECTED)
        assertNull(won.whyNotAWinner("me"))
        assertNull(won.whyCantReserve("me", duringBooking))
    }

    @Test
    fun notAWinner_cantReserveOrDecline() {
        for (status in listOf(null, EntryStatus.WAITING, EntryStatus.NOT_SELECTED)) {
            assertEquals("Only lottery winners can do that", lottery(status).whyNotAWinner("me"))
            assertEquals("Only lottery winners can do that", lottery(status).whyCantReserve("me", duringBooking))
        }
    }

    @Test
    fun alreadyAnswered_cantAnswerAgain() {
        assertEquals(
            "You already reserved a campsite from this lottery",
            lottery(EntryStatus.RESERVED).whyCantReserve("me", duringBooking),
        )
        assertEquals("You already declined this lottery", lottery(EntryStatus.DECLINED).whyNotAWinner("me"))
    }

    @Test
    fun reserving_onlyDuringTheBookingPeriod() {
        val won = lottery(EntryStatus.SELECTED)
        assertEquals("Booking for this lottery opens 2027-04-10", won.whyCantReserve("me", LocalDate.of(2027, 4, 5)))
        // the end day is exclusive
        assertEquals("Booking for this lottery has closed", won.whyCantReserve("me", LocalDate.of(2027, 4, 20)))
        // declining still works outside it since they can change their mind any time
        assertNull(won.whyNotAWinner("me"))
    }

    @Test
    fun noBookingPeriod_canReserveAnyTime() {
        val won = lottery(EntryStatus.SELECTED, bookingStart = "", bookingEnd = "")
        assertNull(won.whyCantReserve("me", LocalDate.of(2030, 1, 1)))
    }
}
