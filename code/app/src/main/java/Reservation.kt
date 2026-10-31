import java.util.Date

class Reservation(var reservationId: Int, var entrantId: Int,
                  var campId: Int, var lotId: Int, val startDate: Date,
                  val endDate: Date, val reservationStatus: ReservationStatus): Model() {
}