package com.kmptv.shared_core.services

import com.kmptv.shared_core.models.*

/**
 * Interface for managing user sessions and authentication
 */
interface SessionManager {
    suspend fun createGuestSession(deviceInfo: DeviceInfo): Result<UserSession>
    suspend fun authenticateUser(credentials: UserCredentials): Result<UserSession>
    suspend fun updateLastActivity(): Unit
    suspend fun isSessionValid(): Boolean
    suspend fun renewSession(): Result<UserSession>
    suspend fun endSession(): Unit
}

/**
 * Implementation of SessionManager
 */
class SessionManagerImpl : SessionManager {
    
    private var currentSession: UserSession? = null
    private val validUsers = mapOf(
        "testuser" to "password123",
        "admin" to "admin123",
        "guest" to "guest"
    )
    
    override suspend fun createGuestSession(deviceInfo: DeviceInfo): Result<UserSession> {
        return try {
            val session = UserSession(
                sessionId = generateSessionId(),
                userId = null,
                isAuthenticated = false,
                deviceInfo = deviceInfo,
                lastActivity = System.currentTimeMillis(),
                sessionTimeout = 30 * 60 * 1000L // 30 minutes
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
            
            // Simple authentication check
            val username = credentials.username ?: credentials.email
            val password = credentials.password
            
            if (username == null || password == null) {
                return Result.Error(IllegalArgumentException("Missing credentials"), "Username and password required")
            }
            
            if (validUsers[username] != password) {
                return Result.Error(IllegalArgumentException("Authentication failed"), "Invalid username or password")
            }
            
            val currentTime = System.currentTimeMillis()
            val deviceInfo = currentSession?.deviceInfo ?: createDefaultDeviceInfo()
            
            val authenticatedSession = UserSession(
                sessionId = generateSessionId(),
                userId = username,
                isAuthenticated = true,
                deviceInfo = deviceInfo,
                lastActivity = currentTime,
                sessionTimeout = 2 * 60 * 60 * 1000L // 2 hours for authenticated users
            )
            
            currentSession = authenticatedSession
            Result.Success(authenticatedSession)
        } catch (e: Exception) {
            Result.Error(e, "Authentication failed: ${e.message}")
        }
    }
    
    override suspend fun updateLastActivity() {
        currentSession?.let { session ->
            currentSession = session.updateActivity()
        }
    }
    
    override suspend fun isSessionValid(): Boolean {
        val session = currentSession ?: return false
        return session.isValid()
    }
    
    override suspend fun renewSession(): Result<UserSession> {
        return try {
            val session = currentSession
                ?: return Result.Error(IllegalStateException("No active session"), "No session to renew")
            
            if (!session.isValid()) {
                return Result.Error(IllegalStateException("Session expired"), "Cannot renew expired session")
            }
            
            val renewedSession = session.copy(
                lastActivity = System.currentTimeMillis(),
                sessionTimeout = if (session.isAuthenticated) 2 * 60 * 60 * 1000L else 30 * 60 * 1000L
            )
            
            currentSession = renewedSession
            Result.Success(renewedSession)
        } catch (e: Exception) {
            Result.Error(e, "Failed to renew session: ${e.message}")
        }
    }
    
    override suspend fun endSession() {
        currentSession = null
    }
    
    fun getCurrentSession(): UserSession? = currentSession
    
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    private fun createDefaultDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = "default_device",
            model = "Unknown TV",
            osVersion = "Unknown",
            availableMemory = 2_000_000_000L,
            storageSpace = 8_000_000_000L,
            networkType = NetworkType.WiFi,
            screenWidth = 1920,
            screenHeight = 1080,
            densityDpi = 320
        )
    }
}