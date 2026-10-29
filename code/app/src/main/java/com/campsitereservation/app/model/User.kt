package com.campsitereservation.app.model

// users are identified by their device so no username or password
// roles is a set so one person can be an entrant owner and admin at once
class User(
    val deviceId: String,
    name: String,
    email: String? = null,
    phone: String? = null,
    var profileImageUrl: String? = null,
    var notificationsEnabled: Boolean = true,
    roles: Set<Role> = setOf(Role.ENTRANT),
) {
    init {
        require(deviceId.isNotBlank()) { "Device ID must not be blank" }
    }

    var name: String = validName(name)
        set(value) { field = validName(value) }

    // email and phone are optional and null if not given
    var email: String? = validEmail(email)
        set(value) { field = validEmail(value) }

    var phone: String? = validPhone(phone)
        set(value) { field = validPhone(value) }

    private val _roles: MutableSet<Role> = roles.toMutableSet()

    // read only so roles can only change through addRole and removeRole
    val roles: Set<Role>
        get() = _roles.toSet()

    init {
        require(_roles.isNotEmpty()) { "A user must have at least one role" }
    }

    fun hasRole(role: Role): Boolean = role in _roles

    fun addRole(role: Role) {
        _roles.add(role)
    }

    // cant remove the last role
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
        // allows plus spaces dashes dots and brackets with 7 to 15 digits
        fun validPhone(phone: String?): String? {
            val trimmed = phone?.trim()
            if (trimmed.isNullOrEmpty()) return null
            val digits = trimmed.count { it.isDigit() }
            require(PHONE_CHARS_REGEX.matches(trimmed) && digits in 7..15) { "Invalid phone number: $phone" }
            return trimmed
        }
    }
}
