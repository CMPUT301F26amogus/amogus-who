import java.sql.Date

class Availability(var availabilityId: Int, var campId: Int,
                   val startDate: Date, val endDate: Date, val isBooked: Boolean
): Model() {
}