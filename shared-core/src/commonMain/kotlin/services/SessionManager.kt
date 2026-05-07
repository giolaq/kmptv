package com.kmptv.shared_core.services

import com.kmptv.shared_core.models.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface SessionManager {
    suspend fun createGuestSession(deviceInfo: DeviceInfo): Result<UserSession>
    suspend fun authenticateUser(credentials: UserCredentials): Result<UserSession>
    suspend fun updateLastActivity()
    suspend fun isSessionValid(): Boolean
    suspend fun endSession()
    fun getCurrentSession(): UserSession?
}

fun interface AuthProvider {
    suspend fun authenticate(username: String, password: String): Boolean
}

class SessionManagerImpl(
    private val authProvider: AuthProvider,
) : SessionManager {

    private val mutex = Mutex()
    private var currentSession: UserSession? = null

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
            mutex.withLock { currentSession = session }
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

            if (!authProvider.authenticate(username, password)) {
                return Result.Error(
                    IllegalArgumentException("Authentication failed"),
                    "Invalid username or password",
                )
            }

            val deviceInfo = mutex.withLock { currentSession?.deviceInfo } ?: DeviceInfoDefaults.forPlatform()
            val authenticatedSession = UserSession(
                sessionId = IdGenerator.sessionId(),
                userId = username,
                isAuthenticated = true,
                deviceInfo = deviceInfo,
                lastActivity = nowMillis(),
                sessionTimeout = SessionConstants.AUTHENTICATED_TIMEOUT_MS,
            )
            mutex.withLock { currentSession = authenticatedSession }
            Result.Success(authenticatedSession)
        } catch (e: Exception) {
            Result.Error(e, "Authentication failed: ${e.message}")
        }
    }

    override suspend fun updateLastActivity() {
        mutex.withLock { currentSession = currentSession?.updateActivity() }
    }

    override suspend fun isSessionValid(): Boolean = mutex.withLock { currentSession?.isValid() ?: false }

    override suspend fun endSession() {
        mutex.withLock { currentSession = null }
    }

    override fun getCurrentSession(): UserSession? = currentSession
}
