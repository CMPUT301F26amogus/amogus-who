package com.example.code.Model

/** where a reservation is */
enum class ReservationStatus {
    /** booked and the stay hasnt happened yet */
    ACTIVE,

    /** the stay is over */
    PAST,

    /** the entrant cancelled it */
    CANCELLED,

    /** the owner rejected it */
    REJECTED,
}
