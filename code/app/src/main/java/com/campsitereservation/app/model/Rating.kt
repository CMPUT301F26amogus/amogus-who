package com.campsitereservation.app.model

// one entrants score for one campsite
data class Rating(
    val userId: String,
    val campsiteId: String,
    val score: Int,
) {
    init {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        require(campsiteId.isNotBlank()) { "Campsite ID must not be blank" }
        require(score in MIN_SCORE..MAX_SCORE) { "Score must be between $MIN_SCORE and $MAX_SCORE, was $score" }
    }

    companion object {
        const val MIN_SCORE = 1
        const val MAX_SCORE = 5
    }
}
