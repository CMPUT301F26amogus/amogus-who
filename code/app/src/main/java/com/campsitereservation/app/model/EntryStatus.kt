package com.campsitereservation.app.model

// WAITING -> SELECTED -> RESERVED or DECLINED
// WAITING -> NOT_SELECTED
enum class EntryStatus {
    WAITING,
    SELECTED,
    NOT_SELECTED,
    DECLINED,
    RESERVED,
}
