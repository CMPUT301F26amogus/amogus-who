package com.example.code.controllers

import Model
import androidx.navigation.NavController
import com.example.code.model.EntrantReservation
import com.example.code.model.Reservation
import com.example.code.model.ReservationStatus
import com.example.code.Routes

class EntrantReservationsController(
    private val model : Model
) {
    /**
     * loads the current users reservations that are coming up or happening now
     *
     * @param onResult gets the reservations sorted by check in or an empty list if it failed
     */
    fun onActiveReservations(onResult: (List<EntrantReservation>) -> Unit) {
        model.getEntrantActiveReservations(model.currentDeviceId, onResult)
    }

    /**
     * loads the current users reservations that are over
     *
     * @param onResult gets the reservations newest first or an empty list if it failed
     */
    fun onPastReservations(onResult: (List<EntrantReservation>) -> Unit) {
        model.getEntrantPastReservations(model.currentDeviceId, onResult)
    }

    /**
     * cancels one of the current users reservations
     *
     * @param reservation the reservation to cancel
     * @param onResult success once its cancelled or a failure saying why it couldnt be
     */
    public fun cancelReservation(reservation: Reservation, onResult: (Result<Unit>) -> Unit = {}) {
        model.cancelReservation(reservation, onResult)
    }

    /**
     * cancels a reservation and hands back the cancelled copy for the confirmation (US 01.07.02)
     *
     * the model sends the cancellation notification
     * the copy has status cancelled so its csv says so
     *
     * @param reservation the reservation with its campsite
     * @param onResult the cancelled reservation or a failure saying why it couldnt be
     */
    fun cancelReservation(reservation: EntrantReservation, onResult: (Result<EntrantReservation>) -> Unit) {
        model.cancelReservation(reservation.reservation) { result ->
            onResult(result.map { reservation.copy(reservation = cancelledCopy(reservation.reservation)) })
        }
    }

    // a new reservation with the same fields but cancelled so the one in the list isnt changed
    private fun cancelledCopy(r: Reservation) = Reservation(
        id = r.id,
        campsiteId = r.campsiteId,
        entrantId = r.entrantId,
        checkIn = r.checkIn,
        checkOut = r.checkOut,
        numGuests = r.numGuests,
        status = ReservationStatus.CANCELLED,
        rulesAccepted = r.rulesAccepted,
        lotteryId = r.lotteryId,
    )

    /**
     * the csv text for a reservation or cancellation so the entrant can download it (US 01.05.02 01.07.02)
     *
     * @param reservation the reservation with its campsite
     * @return the csv file contents
     */
    fun reservationCsv(reservation: EntrantReservation): String = reservation.toCsv()

    /**
     * what to name the downloaded csv
     *
     * @param reservation the reservation with its campsite
     * @return something like reservation-abc123.csv
     */
    fun csvFileName(reservation: EntrantReservation): String = reservation.csvFileName()

    /**
     * Sets the active site to view
     */
    public fun viewCampsite(cid: String, navController: NavController){
        // getCampsite is async so the campsite is only here inside the callback,
        // set it and navigate in there or the details screen opens with nothing
        model.getCampsite(cid){ result ->
            if (result != null) {
                model.settTempCampsite(result)
                navController.navigate(Routes.CAMPSITE_DETAILS)
            }
        }
    }
}