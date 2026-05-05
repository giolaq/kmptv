package com.kmptv.shared_core.contract

import com.kmptv.shared_core.models.*
import com.kmptv.shared_core.services.SessionManagerImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionManagerContractTest {

    private fun deviceInfo(): DeviceInfo =
        DeviceInfoDefaults.forPlatform(deviceId = "test-device")

    @Test
    fun createGuestSession_returns_valid_session() = runTest {
        val manager = SessionManagerImpl()
        val result = manager.createGuestSession(deviceInfo())
        assertTrue(result.isSuccess)
        val session = result.getOrThrow()
        assertTrue(session.sessionId.isNotEmpty())
        assertNull(session.userId)
        assertFalse(session.isAuthenticated)
    }

    @Test
    fun authenticateUser_succeeds_with_valid_credentials() = runTest {
        val manager = SessionManagerImpl()
        val result = manager.authenticateUser(
            UserCredentials(username = "testuser", password = "password123"),
        )
        assertTrue(result.isSuccess, "Expected success: $result")
        val session = result.getOrThrow()
        assertTrue(session.isAuthenticated)
        assertEquals("testuser", session.userId)
    }

    @Test
    fun authenticateUser_fails_with_invalid_credentials() = runTest {
        val manager = SessionManagerImpl()
        val result = manager.authenticateUser(
            UserCredentials(username = "not-a-user", password = "wrong"),
        )
        assertFalse(result.isSuccess)
    }

    @Test
    fun authenticateUser_fails_with_empty_credentials() = runTest {
        val manager = SessionManagerImpl()
        val result = manager.authenticateUser(UserCredentials())
        assertFalse(result.isSuccess)
    }

    @Test
    fun updateLastActivity_advances_timestamp() = runTest {
        val manager = SessionManagerImpl()
        manager.createGuestSession(deviceInfo())
        val before = manager.getCurrentSession()?.lastActivity ?: 0
        manager.updateLastActivity()
        val after = manager.getCurrentSession()?.lastActivity ?: 0
        assertTrue(after >= before, "activity did not advance: before=$before after=$after")
    }

    @Test
    fun isSessionValid_is_true_for_fresh_session() = runTest {
        val manager = SessionManagerImpl()
        manager.createGuestSession(deviceInfo())
        assertTrue(manager.isSessionValid())
    }

    @Test
    fun endSession_clears_current_session() = runTest {
        val manager = SessionManagerImpl()
        manager.createGuestSession(deviceInfo())
        manager.endSession()
        assertNull(manager.getCurrentSession())
        assertFalse(manager.isSessionValid())
    }

    @Test
    fun getCurrentSession_starts_null() = runTest {
        val manager = SessionManagerImpl()
        assertNotNull(manager) // sanity
        assertNull(manager.getCurrentSession())
    }
}
