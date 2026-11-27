package com.example.code.model

/**
 * a reservation with the campsite the entrant reservations screen shows on each card
 *
 * reservations only keep the campsite id so the model looks it up and hands them back together
 *
 * @property reservation the stay itself
 * @property campsite the campsite its at or null if the campsite is gone
 */
data class EntrantReservation(
    val reservation: Reservation,
    val campsite: Campsite?,
) {
    /**
     * the whole csv file for this reservation with the header and one row
     *
     * works for active and cancelled ones since the status column says which it is
     *
     * @return the csv text ending in a newline
     */
    fun toCsv(): String =
        Reservation.CSV_HEADER + "\n" + reservation.toCsvRow(campsite?.name ?: "") + "\n"

    /**
     * a file name to save [toCsv] as
     *
     * cancelled ones are called cancellation so the entrant can tell them apart
     *
     * @return something like reservation-abc123.csv or cancellation-abc123.csv
     */
    fun csvFileName(): String {
        val kind = if (reservation.isCancelled()) "cancellation" else "reservation"
        return "$kind-${reservation.id}.csv"
    }
}
