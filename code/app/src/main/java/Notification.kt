import java.sql.Time

class Notification(var notificationId: Int, var recipientId: Int, val message: String,
                   val timeStamp: Time): Model() {
}