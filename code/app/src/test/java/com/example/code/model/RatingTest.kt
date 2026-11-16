package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RatingTest {

    @Test
    fun validScores_areAccepted() {
        assertEquals(1, Rating("user-1", "site-1", 1).score)
        assertEquals(5, Rating("user-1", "site-1", 5).score)
    }

    @Test(expected = IllegalArgumentException::class)
    fun scoreZero_throws() {
        Rating("user-1", "site-1", 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun scoreSix_throws() {
        Rating("user-1", "site-1", 6)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankUserId_throws() {
        Rating("", "site-1", 3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankCampsiteId_throws() {
        Rating("user-1", " ", 3)
    }
}
