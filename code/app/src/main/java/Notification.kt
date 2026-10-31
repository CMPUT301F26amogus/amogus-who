import java.sql.Time

class Notification(val notificationId: Int, val recipientId: Int, var message: String,
                   var timeStamp: Time): Model() {
}