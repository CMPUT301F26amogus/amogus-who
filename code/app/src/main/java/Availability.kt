import java.sql.Date

class Availability(val availabilityId: Int, val campId: Int,
                   var startDate: Date, var endDate: Date, var isBooked: Boolean
): Model() {
}