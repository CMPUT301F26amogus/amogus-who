package com.campsitereservation.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationTest {

    @Test
    fun validLocation_isCreated() {
        val jasper = Location(52.8737, -118.0814, "Jasper, AB")
        assertEquals("Jasper, AB", jasper.displayName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun latitudeTooHigh_throws() {
        Location(91.0, 0.0, "Nowhere")
    }

    @Test(expected = IllegalArgumentException::class)
    fun longitudeTooLow_throws() {
        Location(0.0, -181.0, "Nowhere")
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankName_throws() {
        Location(0.0, 0.0, "  ")
    }
}
