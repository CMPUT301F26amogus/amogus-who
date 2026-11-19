package com.example.code.model

/**
 * where a campsite is
 *
 * set by the owner and shown to entrants on the map
 *
 * @property latitude between -90 and 90
 * @property longitude between -180 and 180
 * @property displayName readable name like jasper ab
 * @throws IllegalArgumentException if a coordinate is out of range or the name is blank
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val displayName: String,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90, was $latitude" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180, was $longitude" }
        require(displayName.isNotBlank()) { "Display name must not be blank" }
    }
}
