package com.campsitereservation.app.model

// a user can have more than one of these eg an admin who is also an entrant
enum class Role {
    ENTRANT,
    OWNER,
    ADMIN,
}
