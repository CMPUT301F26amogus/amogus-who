package com.example.code.Model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CampsiteTest {

    private val hinton = Location(53.4, -117.57, "Hinton, AB")

    private fun campsite(
        maxOccupancy: Int = 8,
        pricePerNight: Double = 65.0,
    ) = Campsite(
        id = "site-1",
        ownerId = "owner-1",
        name = "Hinton Grande Cache",
        location = hinton,
        maxOccupancy = maxOccupancy,
        pricePerNight = pricePerNight,
    )

    @Test
    fun newCampsite_hasNoRatings() {
        val c = campsite()
        assertNull(c.averageRating)
        assertEquals(0, c.ratingCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankId_throws() {
        Campsite("", "owner-1", "Site", hinton, 4, 10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankOwnerId_throws() {
        Campsite("site-1", "", "Site", hinton, 4, 10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankName_throwsInSetter() {
        campsite().name = "  "
    }

    @Test
    fun name_isTrimmed() {
        val c = campsite()
        c.name = "  Jasper Woods  "
        assertEquals("Jasper Woods", c.name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroOccupancy_throws() {
        campsite(maxOccupancy = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeOccupancyInSetter_throws() {
        campsite().maxOccupancy = -1
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativePrice_throws() {
        campsite(pricePerNight = -5.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nanPrice_throws() {
        campsite().pricePerNight = Double.NaN
    }

    @Test
    fun freeCampsite_isAllowed() {
        assertEquals(0.0, campsite(pricePerNight = 0.0).pricePerNight, 0.0)
    }

    @Test
    fun canHold_checksGuestCount() {
        val c = campsite(maxOccupancy = 8)
        assertTrue(c.canHold(1))
        assertTrue(c.canHold(8))
        assertFalse(c.canHold(9))
        assertFalse(c.canHold(0))
    }

    @Test
    fun priceFor_multipliesByNights() {
        val stay = DateRange(LocalDate.of(2027, 5, 1), LocalDate.of(2027, 5, 3))
        assertEquals(130.0, campsite(pricePerNight = 65.0).priceFor(stay), 0.001)
    }

    @Test
    fun tags_areLowercasedTrimmedAndDeduped() {
        val c = campsite()
        c.tags = setOf(" Lakeside", "lakeside", "PET FRIENDLY", "")
        assertEquals(setOf("lakeside", "pet friendly"), c.tags)
    }

    @Test
    fun amenities_dropBlanksAndDuplicates() {
        val c = campsite()
        c.amenities = listOf("Cookhouse", " Tennis court ", "", "Cookhouse")
        assertEquals(listOf("Cookhouse", "Tennis court"), c.amenities)
    }

    @Test
    fun rate_updatesAverageAndCount() {
        val c = campsite()
        c.rate(Rating("user-1", "site-1", 4))
        c.rate(Rating("user-2", "site-1", 5))
        assertEquals(2, c.ratingCount)
        assertEquals(4.5, c.averageRating!!, 0.001)
    }

    @Test
    fun rateAgain_replacesOldRating() {
        val c = campsite()
        c.rate(Rating("user-1", "site-1", 2))
        c.rate(Rating("user-1", "site-1", 5))
        assertEquals(1, c.ratingCount)
        assertEquals(5.0, c.averageRating!!, 0.001)
    }

    @Test
    fun removeRating_removesIt() {
        val c = campsite()
        c.rate(Rating("user-1", "site-1", 3))
        c.removeRating("user-1")
        assertEquals(0, c.ratingCount)
        assertNull(c.averageRating)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rate_otherCampsiteThrows() {
        campsite().rate(Rating("user-1", "site-2", 4))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rate_ownerCannotRateOwnCampsite() {
        campsite().rate(Rating("owner-1", "site-1", 5))
    }

    @Test
    fun constructor_loadsSavedRatings() {
        val c = Campsite(
            id = "site-1",
            ownerId = "owner-1",
            name = "Site",
            location = hinton,
            maxOccupancy = 4,
            pricePerNight = 10.0,
            ratings = listOf(Rating("user-1", "site-1", 3), Rating("user-2", "site-1", 5)),
        )
        assertEquals(4.0, c.averageRating!!, 0.001)
    }

    @Test
    fun availability_isPartOfCampsite() {
        val c = campsite()
        val weekend = DateRange(LocalDate.of(2027, 5, 1), LocalDate.of(2027, 5, 3))
        c.availability.open(weekend)
        assertTrue(c.availability.isAvailable(weekend))
    }

    @Test
    fun equality_isById() {
        val a = campsite()
        val b = Campsite("site-1", "owner-2", "Other", hinton, 2, 1.0)
        val c = Campsite("site-2", "owner-1", "Hinton Grande Cache", hinton, 8, 65.0)
        assertEquals(a, b)
        assertNotEquals(a, c)
    }
}
