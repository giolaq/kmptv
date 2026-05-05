package com.kmptv.shared_core.models

/**
 * Main application container that tracks platform-specific configuration and
 * shared session state.
 */
data class TVApplication(
    val id: String,                                // Unique application identifier
    val platform: Platform,                        // Android TV or Apple TV
    val version: String,                           // Application version (MAJOR.MINOR.BUILD)
    val configuration: PlatformConfiguration,      // Platform-specific settings
    val sessionManager: UserSession,               // Current user session
    val isInitialized: Boolean = false,            // Initialization status
    val startupTime: Long = 0L,                    // Application startup timestamp
    val features: List<String> = emptyList(),      // Enabled feature flags
)
