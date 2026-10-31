import java.util.Date

class Reservation(val reservationId: Int, val entrantId: Int,
                  val campId: Int, val lotId: Int, var startDate: Date,
                  var endDate: Date, var reservationStatus: ReservationStatus): Model() {
}