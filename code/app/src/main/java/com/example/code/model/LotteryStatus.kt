package com.example.code.model

/**
 * where a lottery is in its lifecycle
 *
 * OPEN -> DRAWING -> BOOKING -> CLOSED
 */
enum class LotteryStatus {
    /** registration period so people can join the waitlist */
    OPEN,

    /** registration is over and winners are being drawn */
    DRAWN,

    /** winners pick their dates first come first served */
    BOOKING,

    /** booking period is over */
    CLOSED,
}
