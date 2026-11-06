package com.example.code.Model

import Model
import View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LotteryTest {

    private fun may(day: Int): LocalDate = LocalDate.of(2027, 5, day)

    private fun range(startDay: Int, endDay: Int) = DateRange(may(startDay), may(endDay))

    // registration may 1 to 10 then booking may 15 to 25
    private fun lottery() = Lottery(
        id = "lottery-1",
        ownerId = "owner-1",
        campsiteIds = listOf("site-1"),
        registrationPeriod = range(1, 10),
        bookingPeriod = range(15, 25),
        numWinners = 3,
    )

    // counts how many times the lottery told it to redraw
    private class CountingView : View() {
        var updates = 0
        override fun update(model: Model) {
            updates++
        }
    }

    @Test
    fun newLottery_isOpenWithEmptyWaitlist() {
        val l = lottery()
        assertEquals(LotteryStatus.OPEN, l.status)
        assertTrue(l.waitlist.isEmpty())
    }

    @Test
    fun setRegistrationPeriod_validPeriodIsSet() {
        val l = lottery()
        l.setRegistrationPeriod(range(2, 12))
        assertEquals(range(2, 12), l.registrationPeriod())
    }

    @Test(expected = IllegalArgumentException::class)
    fun setRegistrationPeriod_endingAfterBookingStartsThrows() {
        lottery().setRegistrationPeriod(range(1, 20))
    }

    @Test
    fun setRegistrationPeriod_canEndTheDayBookingStarts() {
        val l = lottery()
        l.setRegistrationPeriod(range(1, 15))
        assertEquals(range(1, 15), l.registrationPeriod())
    }

    @Test
    fun setRegistrationPeriod_invalidKeepsOldPeriod() {
        val l = lottery()
        runCatching { l.setRegistrationPeriod(range(1, 20)) }
        assertEquals(range(1, 10), l.registrationPeriod())
    }

    @Test
    fun setBookingPeriod_validPeriodIsSet() {
        val l = lottery()
        l.setBookingPeriod(range(12, 30))
        assertEquals(range(12, 30), l.bookingPeriod())
    }

    @Test(expected = IllegalArgumentException::class)
    fun setBookingPeriod_startingBeforeRegistrationEndsThrows() {
        lottery().setBookingPeriod(range(5, 20))
    }

    @Test
    fun setBookingPeriod_canStartTheDayRegistrationEnds() {
        val l = lottery()
        l.setBookingPeriod(range(10, 20))
        assertEquals(range(10, 20), l.bookingPeriod())
    }

    @Test
    fun setBookingPeriod_invalidKeepsOldPeriod() {
        val l = lottery()
        runCatching { l.setBookingPeriod(range(5, 20)) }
        assertEquals(range(15, 25), l.bookingPeriod())
    }

    @Test
    fun setPeriods_notifyViews() {
        val l = lottery()
        val view = CountingView()
        l.addView(view)
        l.setRegistrationPeriod(range(2, 12))
        l.setBookingPeriod(range(13, 20))
        assertEquals(2, view.updates)
    }

    @Test
    fun invalidPeriod_doesNotNotifyViews() {
        val l = lottery()
        val view = CountingView()
        l.addView(view)
        runCatching { l.setBookingPeriod(range(5, 20)) }
        assertEquals(0, view.updates)
    }

    // firestore needs to build one with no arguments when loading
    @Test
    fun noArguments_hasNoPeriodsYet() {
        val l = Lottery()
        assertNull(l.registrationPeriod())
        assertNull(l.bookingPeriod())
        assertEquals(LotteryStatus.OPEN, l.status)
    }

    @Test
    fun blankLottery_periodsCanBeSetInEitherOrder() {
        val l = Lottery()
        l.setBookingPeriod(range(15, 25))
        l.setRegistrationPeriod(range(1, 10))
        assertEquals(range(1, 10), l.registrationPeriod())
        assertEquals(range(15, 25), l.bookingPeriod())
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankLottery_stillChecksOnceOnePeriodIsSet() {
        val l = Lottery()
        l.setRegistrationPeriod(range(1, 10))
        l.setBookingPeriod(range(5, 20))
    }

    @Test
    fun periods_areSavedAsDateStrings() {
        val l = lottery()
        assertEquals("2027-05-01", l.registrationStart)
        assertEquals("2027-05-10", l.registrationEnd)
        assertEquals("2027-05-15", l.bookingStart)
        assertEquals("2027-05-25", l.bookingEnd)
    }

    @Test
    fun savedDateStrings_loadAsPeriods() {
        val l = Lottery(registrationStart = "2027-05-01", registrationEnd = "2027-05-10",
            bookingStart = "2027-05-15", bookingEnd = "2027-05-25")
        assertEquals(range(1, 10), l.registrationPeriod())
        assertEquals(range(15, 25), l.bookingPeriod())
    }

    @Test
    fun brokenSavedDates_countAsNotSet() {
        val l = Lottery(registrationStart = "05/01/2027", registrationEnd = "2027-05-10")
        assertNull(l.registrationPeriod())
    }

    @Test
    fun constructor_loadsSavedWaitlist() {
        val entries = listOf(WaitlistEntry("lottery-1", "user-1"), WaitlistEntry("lottery-1", "user-2"))
        val l = Lottery("lottery-1", "owner-1", listOf("site-1"), range(1, 10), range(15, 25), 3, waitlist = entries)
        assertEquals(listOf("user-1", "user-2"), l.waitlist.map { it.userId })
    }

    @Test
    fun waitlist_cannotBeChangedFromOutside() {
        val l = lottery()
        (l.waitlist as? MutableList<WaitlistEntry>)?.add(WaitlistEntry("lottery-1", "user-1"))
        assertTrue(l.waitlist.isEmpty())
    }
}
