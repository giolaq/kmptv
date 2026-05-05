package com.kmptv.shared_core.services

import com.kmptv.shared_core.models.*

/**
 * Interface for managing user sessions and authentication.
 */
interface SessionManager {
    suspend fun createGuestSession(deviceInfo: DeviceInfo): Result<UserSession>
    suspend fun authenticateUser(credentials: UserCredentials): Result<UserSession>
    suspend fun updateLastActivity()
    suspend fun isSessionValid(): Boolean
    suspend fun endSession()
    fun getCurrentSession(): UserSession?
}

/**
 * In-memory implementation of [SessionManager].
 */
class SessionManagerImpl : SessionManager {

    private var currentSession: UserSession? = null
    private val validUsers = mapOf(
        "testuser" to "password123",
        "admin" to "admin123",
        "guest" to "guest",
    )

    override suspend fun createGuestSession(deviceInfo: DeviceInfo): Result<UserSession> {
        return try {
            val session = UserSession(
                sessionId = IdGenerator.sessionId(),
                userId = null,
                isAuthenticated = false,
                deviceInfo = deviceInfo,
                lastActivity = nowMillis(),
                sessionTimeout = SessionConstants.GUEST_TIMEOUT_MS,
            )
            currentSession = session
            Result.Success(session)
        } catch (e: Exception) {
            Result.Error(e, "Failed to create guest session: ${e.message}")
        }
    }

    override suspend fun authenticateUser(credentials: UserCredentials): Result<UserSession> {
        return try {
            if (!credentials.isValid()) {
                return Result.Error(IllegalArgumentException("Invalid credentials"), "Credentials are not valid")
            }

            val username = credentials.username ?: credentials.email
            val password = credentials.password

            if (username == null || password == null) {
                return Result.Error(
                    IllegalArgumentException("Missing credentials"),
                    "Username and password required",
                )
            }

            if (validUsers[username] != password) {
                return Result.Error(
                    IllegalArgumentException("Authentication failed"),
                    "Invalid username or password",
                )
            }

            val deviceInfo = currentSession?.deviceInfo ?: DeviceInfoDefaults.forPlatform()
            val authenticatedSession = UserSession(
                sessionId = IdGenerator.sessionId(),
                userId = username,
                isAuthenticated = true,
                deviceInfo = deviceInfo,
                lastActivity = nowMillis(),
                sessionTimeout = SessionConstants.AUTHENTICATED_TIMEOUT_MS,
            )
            currentSession = authenticatedSession
            Result.Success(authenticatedSession)
        } catch (e: Exception) {
            Result.Error(e, "Authentication failed: ${e.message}")
        }
    }

    override suspend fun updateLastActivity() {
        currentSession = currentSession?.updateActivity()
    }

    override suspend fun isSessionValid(): Boolean = currentSession?.isValid() ?: false

    override suspend fun endSession() {
        currentSession = null
    }

    override fun getCurrentSession(): UserSession? = currentSession
}
