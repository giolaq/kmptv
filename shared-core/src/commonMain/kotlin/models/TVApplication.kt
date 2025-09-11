package com.kmptv.shared_core.models

/**
 * Main application container that manages platform-specific configurations and shared state
 */
data class TVApplication(
    val id: String,                                // Unique application identifier
    val platform: Platform,                       // Android TV or Apple TV
    val version: String,                           // Application version (MAJOR.MINOR.BUILD)
    val configuration: PlatformConfiguration,     // Platform-specific settings
    val sessionManager: UserSession,              // Current user session handler
    val isInitialized: Boolean = false,           // Initialization status
    val startupTime: Long = 0L,                   // Application startup timestamp
    val features: List<String> = emptyList()      // Enabled feature flags
) {
    /**
     * Validates that the platform matches the configuration
     */
    fun isValidConfiguration(): Boolean {
        return configuration.platform == platform
    }
    
    /**
     * Checks if the application version follows semantic versioning
     */
    fun isValidVersion(): Boolean {
        val versionPattern = Regex("""^\d+\.\d+\.\d+$""")
        return versionPattern.matches(version)
    }
    
    /**
     * Gets the application uptime in milliseconds
     */
    fun getUptime(): Long {
        return if (startupTime > 0) {
            System.currentTimeMillis() - startupTime
        } else {
            0L
        }
    }
}