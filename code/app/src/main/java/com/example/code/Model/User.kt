package com.example.code.Model

import Model

/**
 * someone using the app
 *
 * users are identified by their device so theres no username or password
 * roles is a set so one person can be an entrant owner and admin at once
 * two users are equal when they have the same [deviceId]
 *
 * @property deviceId id of the device this user signed up on
 * @property profileImageUrl link to their profile picture or null if they dont have one
 * @property notificationsEnabled false if they opted out of notifications
 * @param name shown on their profile and cant be blank
 * @param email optional and checked if given
 * @param phone optional and checked if given
 * @param roles starting roles and must not be empty
 * @throws IllegalArgumentException if any field is invalid
 */
class User(
    val deviceId: String,
    name: String,
    email: String? = null,
    phone: String? = null,
    var profileImageUrl: String? = null,
    var notificationsEnabled: Boolean = true,
    roles: Set<Role> = setOf(Role.ENTRANT),
) : Model() {
    init {
        require(deviceId.isNotBlank()) { "Device ID must not be blank" }
    }

    /**
     * the users name trimmed
     *
     * @throws IllegalArgumentException if set to something blank
     */
    var name: String = validName(name)
        set(value) { field = validName(value) }

    /**
     * the users email or null if not given
     *
     * setting it to blank clears it
     *
     * @throws IllegalArgumentException if it doesnt look like an email
     */
    var email: String? = validEmail(email)
        set(value) { field = validEmail(value) }

    /**
     * the users phone number or null if not given
     *
     * setting it to blank clears it
     * allows plus spaces dashes dots and brackets with 7 to 15 digits
     *
     * @throws IllegalArgumentException if it doesnt look like a phone number
     */
    var phone: String? = validPhone(phone)
        set(value) { field = validPhone(value) }

    private val _roles: MutableSet<Role> = roles.toMutableSet()

    /** read only copy of the users roles so they only change through [addRole] and [removeRole] */
    val roles: Set<Role>
        get() = _roles.toSet()

    init {
        require(_roles.isNotEmpty()) { "A user must have at least one role" }
    }

    /**
     * checks if the user has a role
     *
     * @param role the role to check
     * @return true if they have it
     */
    fun hasRole(role: Role): Boolean = role in _roles

    /**
     * gives the user a role and does nothing if they already have it
     *
     * @param role the role to add
     */
    fun addRole(role: Role) {
        _roles.add(role)
    }

    /**
     * takes a role away from the user
     *
     * @param role the role to remove
     * @throws IllegalArgumentException if its their only role
     */
    fun removeRole(role: Role) {
        require(_roles != setOf(role)) { "Cannot remove a user's only role" }
        _roles.remove(role)
    }

    override fun equals(other: Any?): Boolean =
        other is User && other.deviceId == deviceId

    override fun hashCode(): Int = deviceId.hashCode()

    override fun toString(): String = "User(deviceId=$deviceId, name=$name, roles=$_roles)"

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        val PHONE_CHARS_REGEX = Regex("^\\+?[0-9 ()\\-.]+$")

        fun validName(name: String): String {
            require(name.isNotBlank()) { "Name must not be blank" }
            return name.trim()
        }

        // blank counts as no email
        fun validEmail(email: String?): String? {
            val trimmed = email?.trim()
            if (trimmed.isNullOrEmpty()) return null
            require(EMAIL_REGEX.matches(trimmed)) { "Invalid email: $email" }
            return trimmed
        }

        // blank counts as no phone
        fun validPhone(phone: String?): String? {
            val trimmed = phone?.trim()
            if (trimmed.isNullOrEmpty()) return null
            val digits = trimmed.count { it.isDigit() }
            require(PHONE_CHARS_REGEX.matches(trimmed) && digits in 7..15) { "Invalid phone number: $phone" }
            return trimmed
        }
    }
}
