package com.example.code.Model

/**
 * what a user is allowed to do
 *
 * a user can have more than one of these eg an admin who is also an entrant
 */
enum class Role {
    /** joins lotteries and reserves campsites */
    ENTRANT,

    /** lists campsites and runs lotteries */
    OWNER,

    /** moderates campsites profiles and images */
    ADMIN,
}
