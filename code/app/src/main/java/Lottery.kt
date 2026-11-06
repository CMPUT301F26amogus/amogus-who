import java.util.Date

class Lottery(val lotId: String = "", val campIds: List<String> = emptyList(), var winnerIds: List<String> = emptyList(),
              var regStart: Date, var regEnd: Date, var bookStart: Date,
              var bookEnd: Date, var winnerCount: Int = 0, var waitlistCap: Int = 0,
              var lotteryStatus: LotteryStatus = LotteryStatus.OPEN) {
}