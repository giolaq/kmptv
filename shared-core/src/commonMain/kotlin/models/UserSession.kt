package com.kmptv.shared_core.models

/**
 * Manages user session state and authentication context across platforms.
 */
data class UserSession(
    val sessionId: String,                         // Unique session identifier
    val userId: String? = null,                    // User identifier (null for guest sessions)
    val isAuthenticated: Boolean = false,          // Authentication status
    val preferences: UserPreferences = UserPreferences(), // User-specific settings
    val lastActivity: Long,                        // Timestamp of last user activity
    val sessionTimeout: Long,                      // Session timeout in milliseconds
    val deviceInfo: DeviceInfo,                    // Information about the TV device
    val createdAt: Long = nowMillis(),             // Session creation timestamp
    val capabilities: List<String> = emptyList(),  // Session-specific capabilities
) {

    /**
     * Session type enumeration.
     */
    enum class Type {
        Guest,
        Authenticated,
        Admin,
    }

    /**
     * Gets the session type based on authentication status and user ID.
     */
    fun getSessionType(): Type = when {
        !isAuthenticated || userId == null -> Type.Guest
        userId.startsWith("admin_") -> Type.Admin
        else -> Type.Authenticated
    }

    /**
     * Checks if the session is currently valid based on timeout.
     */
    fun isValid(): Boolean = (nowMillis() - lastActivity) < sessionTimeout

    /**
     * Checks if the session is expired.
     */
    fun isExpired(): Boolean = !isValid()

    /**
     * Gets remaining session time in milliseconds.
     */
    fun getRemainingTime(): Long {
        val elapsed = nowMillis() - lastActivity
        return maxOf(0L, sessionTimeout - elapsed)
    }

    /**
     * Creates a copy with updated last activity timestamp.
     */
    fun updateActivity(): UserSession = copy(lastActivity = nowMillis())
}
