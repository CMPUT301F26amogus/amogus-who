package com.example.code.Model

// WAITING -> SELECTED -> RESERVED or DECLINED
// WAITING -> NOT_SELECTED
enum class EntryStatus {
    WAITING,
    SELECTED,
    NOT_SELECTED,
    DECLINED,
    RESERVED,
}
