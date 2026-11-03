package com.example.code.Model

// campsites are equal when they have the same id
class Campsite(
    val id: String,
    val ownerId: String,
    name: String,
    var location: Location,
    maxOccupancy: Int,
    pricePerNight: Double,
    var description: String = "",
    var rulesAndPolicy: String = "",
    var imageUrl: String? = null,
    amenities: List<String> = emptyList(),
    tags: Set<String> = emptySet(),
    val availability: AvailabilitySchedule = AvailabilitySchedule(),
    ratings: Collection<Rating> = emptyList(),
) {
    init {
        require(id.isNotBlank()) { "Campsite ID must not be blank" }
        require(ownerId.isNotBlank()) { "Owner ID must not be blank" }
    }

    var name: String = validName(name)
        set(value) { field = validName(value) }

    var maxOccupancy: Int = validOccupancy(maxOccupancy)
        set(value) { field = validOccupancy(value) }

    var pricePerNight: Double = validPrice(pricePerNight)
        set(value) { field = validPrice(value) }

    // trimmed with blanks dropped
    var amenities: List<String> = cleanAmenities(amenities)
        set(value) { field = cleanAmenities(value) }

    // lowercase so filtering by tag doesnt care about caps
    var tags: Set<String> = cleanTags(tags)
        set(value) { field = cleanTags(value) }

    // one rating per user keyed by user id
    private val _ratings = mutableMapOf<String, Rating>()

    init {
        ratings.forEach { rate(it) }
    }

    val ratings: List<Rating>
        get() = _ratings.values.toList()

    val ratingCount: Int
        get() = _ratings.size

    // null when nobody has rated it yet
    val averageRating: Double?
        get() = if (_ratings.isEmpty()) null else _ratings.values.map { it.score }.average()

    fun canHold(guests: Int): Boolean = guests in 1..maxOccupancy

    fun priceFor(stay: DateRange): Double = pricePerNight * stay.nights

    // rating again replaces the old one
    fun rate(rating: Rating) {
        require(rating.campsiteId == id) { "Rating is for campsite ${rating.campsiteId}, not $id" }
        require(rating.userId != ownerId) { "Owners cannot rate their own campsite" }
        _ratings[rating.userId] = rating
    }

    fun removeRating(userId: String) {
        _ratings.remove(userId)
    }

    override fun equals(other: Any?): Boolean = other is Campsite && other.id == id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "Campsite(id=$id, name=$name)"

    private companion object {
        fun validName(name: String): String {
            require(name.isNotBlank()) { "Name must not be blank" }
            return name.trim()
        }

        fun validOccupancy(maxOccupancy: Int): Int {
            require(maxOccupancy > 0) { "Max occupancy must be more than 0, was $maxOccupancy" }
            return maxOccupancy
        }

        fun validPrice(price: Double): Double {
            require(price.isFinite() && price >= 0) { "Price must be 0 or more, was $price" }
            return price
        }

        fun cleanAmenities(amenities: List<String>): List<String> =
            amenities.map { it.trim() }.filter { it.isNotEmpty() }.distinct()

        fun cleanTags(tags: Set<String>): Set<String> =
            tags.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
    }
}
