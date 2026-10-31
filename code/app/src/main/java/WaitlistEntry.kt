import java.util.Date

class WaitlistEntry(val entryId: Int, val lotId: Int, val entrantId: Int,
                    var joinedAt: Date, var waitlistStatus: WaitlistStatus): Model() {
}