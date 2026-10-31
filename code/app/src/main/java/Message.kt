import java.sql.Time

class Message(var messageId: Int, var senderId: Int, var recipientId: Int,
              var reservationId: Int, val content:String, val timeStamp: Time): Model() {
}