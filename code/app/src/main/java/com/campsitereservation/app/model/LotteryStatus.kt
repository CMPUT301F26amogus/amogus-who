package com.campsitereservation.app.model

// OPEN -> DRAWING -> BOOKING -> CLOSED
// open is when people can join the waitlist
// booking is when winners pick their dates first come first served
enum class LotteryStatus {
    OPEN,
    DRAWING,
    BOOKING,
    CLOSED,
}
