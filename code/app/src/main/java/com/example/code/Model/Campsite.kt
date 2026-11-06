package com.example.code.Model

import Model

/**
 * a campsite listed by an owner
 *
 * two campsites are equal when they have the same [id]
 *
 * @property id unique id for this campsite
 * @property ownerId device id of the owner who listed it
 * @property location where it is on the map
 * @property description what its like and what it has
 * @property rulesAndPolicy rules and cancellation policy entrants agree to
 * @property imageUrl link to the promo image or null if there isnt one
 * @property availability which nights are open and booked
 * @param name cant be blank
 * @param maxOccupancy most guests allowed and must be more than 0
 * @param pricePerNight cost per night and must be 0 or more
 * @param amenities things like cookhouse or tennis court
 * @param tags search tags
 * @param ratings saved ratings to load
 * @throws IllegalArgumentException if any field is invalid
 */
class Campsite(
    val id: String,
    val ownerId: String,
    name: String,
    var location: String,
    var lottery: Lottery = Lottery(),
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

    /**
     * the campsite name trimmed
     *
     * @throws IllegalArgumentException if set to something blank
     */
    var name: String = validName(name)
        set(value) { field = validName(value) }

    /**
     * most guests allowed
     *
     * @throws IllegalArgumentException if set to 0 or less
     */
    var maxOccupancy: Int = validOccupancy(maxOccupancy)
        set(value) { field = validOccupancy(value) }

    /**
     * cost per night
     *
     * @throws IllegalArgumentException if set below 0 or not a real number
     */
    var pricePerNight: Double = validPrice(pricePerNight)
        set(value) { field = validPrice(value) }

    /** amenities trimmed with blanks and duplicates dropped */
    var amenities: List<String> = cleanAmenities(amenities)
        set(value) { field = cleanAmenities(value) }

    /** tags in lowercase so filtering by tag doesnt care about caps */
    var tags: Set<String> = cleanTags(tags)
        set(value) { field = cleanTags(value) }

    // one rating per user keyed by user id
    private val _ratings = mutableMapOf<String, Rating>()

    init {
        ratings.forEach { rate(it) }
    }

    /** every rating with one per user */
    val ratings: List<Rating>
        get() = _ratings.values.toList()

    /** how many people rated it */
    val ratingCount: Int
        get() = _ratings.size

    /** average score or null when nobody has rated it yet */
    val averageRating: Double?
        get() = if (_ratings.isEmpty()) null else _ratings.values.map { it.score }.average()

    /**
     * checks if a group fits
     *
     * @param guests how many people
     * @return true if guests is between 1 and [maxOccupancy]
     */
    fun canHold(guests: Int): Boolean = guests in 1..maxOccupancy

    /**
     * total cost of a stay
     *
     * @param stay the dates being booked
     * @return [pricePerNight] times the number of nights
     */
    fun priceFor(stay: DateRange): Double = pricePerNight * stay.nights

    /**
     * adds a rating and rating again replaces the old one
     *
     * @param rating the rating to add
     * @throws IllegalArgumentException if its for another campsite or from the owner
     */
    fun rate(rating: Rating) {
        require(rating.campsiteId == id) { "Rating is for campsite ${rating.campsiteId}, not $id" }
        require(rating.userId != ownerId) { "Owners cannot rate their own campsite" }
        _ratings[rating.userId] = rating
    }

    /**
     * removes a users rating and does nothing if they never rated it
     *
     * @param userId whose rating to remove
     */
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
