package com.campsitereservation.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTest {

    private fun user(roles: Set<Role> = setOf(Role.ENTRANT)) =
        User(deviceId = "device-1", name = "Alex", roles = roles)

    @Test
    fun newUser_defaultsToEntrantWithNotificationsOn() {
        val u = User(deviceId = "device-1", name = "Alex")
        assertEquals(setOf(Role.ENTRANT), u.roles)
        assertTrue(u.notificationsEnabled)
        assertNull(u.email)
        assertNull(u.phone)
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankDeviceId_throws() {
        User(deviceId = " ", name = "Alex")
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankName_throwsInConstructor() {
        User(deviceId = "device-1", name = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankName_throwsInSetter() {
        user().name = "   "
    }

    @Test
    fun name_isTrimmed() {
        val u = user()
        u.name = "  Sam  "
        assertEquals("Sam", u.name)
    }

    @Test
    fun email_validIsKeptAndBlankBecomesNull() {
        val u = user()
        u.email = " alex@example.com "
        assertEquals("alex@example.com", u.email)
        u.email = ""
        assertNull(u.email)
    }

    @Test(expected = IllegalArgumentException::class)
    fun email_invalidThrows() {
        user().email = "not-an-email"
    }

    @Test
    fun phone_acceptsCommonFormats() {
        val u = user()
        u.phone = "(780) 555-0123"
        assertEquals("(780) 555-0123", u.phone)
        u.phone = "+1 780 555 0123"
        assertEquals("+1 780 555 0123", u.phone)
    }

    @Test(expected = IllegalArgumentException::class)
    fun phone_withLettersThrows() {
        user().phone = "780-CALL-NOW"
    }

    @Test(expected = IllegalArgumentException::class)
    fun phone_tooShortThrows() {
        user().phone = "12345"
    }

    @Test
    fun admin_canAlsoBeEntrantAndOwner() {
        val u = user(setOf(Role.ADMIN))
        u.addRole(Role.ENTRANT)
        u.addRole(Role.OWNER)
        assertTrue(u.hasRole(Role.ADMIN))
        assertTrue(u.hasRole(Role.ENTRANT))
        assertTrue(u.hasRole(Role.OWNER))
    }

    @Test
    fun removeRole_removesIt() {
        val u = user(setOf(Role.ENTRANT, Role.OWNER))
        u.removeRole(Role.OWNER)
        assertFalse(u.hasRole(Role.OWNER))
    }

    @Test(expected = IllegalArgumentException::class)
    fun removeRole_cannotRemoveLastRole() {
        user(setOf(Role.ENTRANT)).removeRole(Role.ENTRANT)
    }

    @Test(expected = IllegalArgumentException::class)
    fun noRoles_throws() {
        user(emptySet())
    }

    @Test
    fun roles_cannotBeChangedFromOutside() {
        val u = user()
        // roles gives back a copy or a locked set so the user shouldnt change either way
        runCatching { (u.roles as? MutableSet<Role>)?.add(Role.ADMIN) }
        assertFalse(u.hasRole(Role.ADMIN))
    }

    @Test
    fun equality_isByDeviceId() {
        assertEquals(User("device-1", "Alex"), User("device-1", "Different Name"))
        assertNotEquals(User("device-1", "Alex"), User("device-2", "Alex"))
    }
}
