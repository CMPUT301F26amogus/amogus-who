import java.util.Date

class WaitlistEntry(var entryId: Int, var lotId: Int, var entrantId: Int,
                    val joinedAt: Date, val waitlistStatus: WaitlistStatus): Model() {
}