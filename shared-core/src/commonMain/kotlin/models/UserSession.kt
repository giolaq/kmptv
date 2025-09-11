package com.kmptv.shared_core.models

/**
 * Manages user session state and authentication context across platforms
 */
data class UserSession(
    val sessionId: String,                         // Unique session identifier
    val userId: String? = null,                    // User identifier (null for guest sessions)
    val isAuthenticated: Boolean = false,          // Authentication status
    val preferences: UserPreferences = UserPreferences(), // User-specific settings
    val lastActivity: Long,                        // Timestamp of last user activity
    val sessionTimeout: Long,                      // Session timeout in milliseconds
    val deviceInfo: DeviceInfo,                    // Information about the TV device
    val createdAt: Long = System.currentTimeMillis(), // Session creation timestamp
    val capabilities: List<String> = emptyList()   // Session-specific capabilities
) {
    
    /**
     * Session type enumeration
     */
    enum class Type {
        Guest,
        Authenticated,
        Admin
    }
    
    /**
     * Gets the session type based on authentication status and user ID
     */
    fun getSessionType(): Type {
        return when {
            !isAuthenticated || userId == null -> Type.Guest
            userId.startsWith("admin_") -> Type.Admin
            else -> Type.Authenticated
        }
    }
    
    /**
     * Checks if the session is currently valid based on timeout
     */
    fun isValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastActivity) < sessionTimeout
    }
    
    /**
     * Checks if the session is expired
     */
    fun isExpired(): Boolean {
        return !isValid()
    }
    
    /**
     * Gets remaining session time in milliseconds
     */
    fun getRemainingTime(): Long {
        val elapsed = System.currentTimeMillis() - lastActivity
        return maxOf(0L, sessionTimeout - elapsed)
    }
    
    /**
     * Gets session duration since creation in milliseconds
     */
    fun getSessionDuration(): Long {
        return System.currentTimeMillis() - createdAt
    }
    
    /**
     * Creates a copy with updated last activity timestamp
     */
    fun updateActivity(): UserSession {
        return copy(lastActivity = System.currentTimeMillis())
    }
    
    /**
     * Creates a copy with updated preferences
     */
    fun updatePreferences(newPreferences: UserPreferences): UserSession {
        return copy(preferences = newPreferences)
    }
    
    /**
     * Creates a copy with authentication status
     */
    fun authenticate(newUserId: String): UserSession {
        return copy(
            userId = newUserId,
            isAuthenticated = true,
            lastActivity = System.currentTimeMillis()
        )
    }
    
    /**
     * Creates a guest session copy (removes authentication)
     */
    fun toGuestSession(): UserSession {
        return copy(
            userId = null,
            isAuthenticated = false,
            lastActivity = System.currentTimeMillis()
        )
    }
    
    /**
     * Validates session data integrity
     */
    fun isValidSession(): Boolean {
        return sessionId.isNotEmpty() &&
               sessionTimeout > 0 &&
               lastActivity > 0 &&
               createdAt > 0 &&
               (!isAuthenticated || userId != null)
    }
    
    /**
     * Checks if session has a specific capability
     */
    fun hasCapability(capability: String): Boolean {
        return capabilities.contains(capability)
    }
}