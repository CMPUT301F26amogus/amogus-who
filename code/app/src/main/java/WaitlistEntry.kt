import java.util.Date

class WaitlistEntry(val entryId: String = "", val lotId: String = "", val entrantId: String = "",
                    var joinedAt: Long = 0L, var waitlistStatus: WaitlistStatus = WaitlistStatus.WAITING) {
}