package com.example.code.Model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// only checks the input rules since those run before anything touches firebase
// saving a valid campsite needs firebase so it belongs in an androidTest
class OwnerModelTest {

    private val owner = OwnerModel("owner-1")

    private fun create(
        name: String = "Hinton Grande Cache",
        location: String = "Hinton, AB",
        maxOcc: Int = 8,
        price: Double = 65.0,
    ) = owner.createCampsite(name, location, "quiet site by the lake", maxOcc, price)

    @Test
    fun newOwnerModel_hasNoCampsites() {
        assertEquals("owner-1", owner.ownerId)
        assertTrue(owner.campsites.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun createCampsite_blankNameThrows() {
        create(name = "   ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun createCampsite_blankLocationThrows() {
        create(location = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun createCampsite_zeroOccupancyThrows() {
        create(maxOcc = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createCampsite_negativeOccupancyThrows() {
        create(maxOcc = -2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createCampsite_negativePriceThrows() {
        create(price = -1.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createCampsite_nanPriceThrows() {
        create(price = Double.NaN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createCampsite_infinitePriceThrows() {
        create(price = Double.POSITIVE_INFINITY)
    }

    @Test
    fun createCampsite_invalidInputAddsNothing() {
        runCatching { create(name = "") }
        assertTrue(owner.campsites.isEmpty())
    }
}
