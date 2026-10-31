import java.sql.Time

class Message(val messageId: Int, val senderId: Int, val recipientId: Int,
              val reservationId: Int, var content:String, var timeStamp: Time): Model() {
}