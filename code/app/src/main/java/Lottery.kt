import java.util.Date

class Lottery(val lotId: Int, val campIds: ArrayList<Campsite>,
              var regStart: Date, var regEnd: Date, var bookStart: Date,
              var bookEnd: Date, var winnerCount: Int, var waitlistCap: Int,
              var lotteryStatus: LotteryStatus): Model() {
}