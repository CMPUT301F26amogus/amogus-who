import java.util.Date

class Lottery(var lotId: Int, var campIds: ArrayList<Campsite>,
              val regStart: Date, val regEnd: Date, val bookStart: Date,
              val bookEnd: Date, val winnerCount: Int, val waitlistCap: Int,
              val lotteryStatus: LotteryStatus): Model() {
}