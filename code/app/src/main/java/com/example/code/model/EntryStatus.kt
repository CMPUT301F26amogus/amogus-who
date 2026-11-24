package com.example.code.model

/**
 * where an entrant is in a lottery
 *
 * WAITING -> SELECTED -> RESERVED or DECLINED
 * WAITING -> NOT_SELECTED
 */
enum class EntryStatus {
    /** on the waitlist and the draw hasnt happened yet */
    WAITING,

    /** won the draw and can pick dates */
    SELECTED,

    /** lost the draw */
    NOT_SELECTED,

    /** won but said no */
    DECLINED,

    /** won and booked dates */
    RESERVED,
}
