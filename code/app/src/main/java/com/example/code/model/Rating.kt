package com.example.code.model

/**
 * one entrants score for one campsite
 *
 * @property userId who gave the rating
 * @property campsiteId which campsite it is for
 * @property score from [MIN_SCORE] to [MAX_SCORE]
 * @throws IllegalArgumentException if an id is blank or the score is out of range
 */
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
        /** lowest score allowed */
        const val MIN_SCORE = 1

        /** highest score allowed */
        const val MAX_SCORE = 5
    }
}
