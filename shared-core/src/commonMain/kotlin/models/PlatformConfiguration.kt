package com.kmptv.shared_core.models

/**
 * Platform-specific settings and configuration options
 */
data class PlatformConfiguration(
    val platform: Platform,                       // Target platform (AndroidTV, AppleTV)
    val inputMethods: List<InputMethod>,           // Supported input types
    val screenResolution: Resolution,              // TV screen resolution info
    val supportedFormats: List<MediaFormat>,       // Supported media formats
    val navigationStyle: NavigationStyle,          // Platform navigation patterns
    val uiScaling: Float = 1.0f,                  // UI scaling factor for screen size (0.5-3.0)
    val hardwareCapabilities: HardwareInfo? = null, // TV hardware information
    val features: Map<String, Boolean> = emptyMap(), // Platform-specific feature flags
    val limits: PlatformLimits = PlatformLimits(), // Platform resource limits
    val accessibility: AccessibilitySettings = AccessibilitySettings() // Accessibility settings
) {
    
    /**
     * Platform resource limits
     */
    data class PlatformLimits(
        val maxConcurrentStreams: Int = 1,         // Maximum concurrent video streams
        val maxCacheSize: Long = 500_000_000L,     // Maximum cache size in bytes (500MB)
        val maxThumbnailSize: Long = 2_000_000L,   // Maximum thumbnail size in bytes (2MB)
        val maxMemoryUsage: Long = 200_000_000L,   // Maximum memory usage in bytes (200MB)
        val networkTimeoutMs: Long = 30_000L,      // Network timeout in milliseconds
        val renderingTargetFps: Int = 60           // Target rendering frame rate
    )
    
    /**
     * Accessibility settings for the platform
     */
    data class AccessibilitySettings(
        val voiceOverEnabled: Boolean = false,     // Screen reader support
        val highContrastMode: Boolean = false,     // High contrast mode
        val focusAnimationDuration: Int = 200,     // Focus animation duration in ms
        val fontSize: Float = 1.0f,                // Font size multiplier
        val colorBlindnessSupport: Boolean = false, // Color blindness accommodations
        val reducedMotion: Boolean = false         // Reduced motion for accessibility
    )
    
    /**
     * Validates the platform configuration
     */
    fun isValid(): Boolean {
        return inputMethods.isNotEmpty() &&
               uiScaling in 0.5f..3.0f &&
               isValidForPlatform() &&
               hasRequiredInputMethods()
    }
    
    /**
     * Checks if configuration is valid for the specified platform
     */
    private fun isValidForPlatform(): Boolean {
        return when (platform) {
            Platform.AndroidTV -> {
                inputMethods.contains(InputMethod.RemoteControl) &&
                navigationStyle in listOf(NavigationStyle.DirectionalPad, NavigationStyle.TouchGestures)
            }
            Platform.AppleTV -> {
                inputMethods.contains(InputMethod.SiriRemote) &&
                navigationStyle in listOf(NavigationStyle.TouchGestures, NavigationStyle.DirectionalPad)
            }
            Platform.FireTV -> {
                inputMethods.contains(InputMethod.RemoteControl) &&
                navigationStyle == NavigationStyle.DirectionalPad
            }
        }
    }
    
    /**
     * Checks if platform has required input methods
     */
    private fun hasRequiredInputMethods(): Boolean {
        // All platforms must support some form of directional navigation
        return inputMethods.any { method ->
            method in listOf(InputMethod.RemoteControl, InputMethod.SiriRemote)
        }
    }
    
    /**
     * Gets optimal UI scaling based on screen resolution and platform
     */
    fun getOptimalUIScaling(): Float {
        return when {
            screenResolution.width >= 3840 -> 1.5f  // 4K displays
            screenResolution.width >= 1920 -> 1.0f  // 1080p displays
            screenResolution.width >= 1280 -> 0.8f  // 720p displays
            else -> 0.7f                             // Lower resolution displays
        }.coerceIn(0.5f, 3.0f)
    }
    
    /**
     * Checks if a specific media format is supported
     */
    fun supportsFormat(format: MediaFormat): Boolean {
        return supportedFormats.any { supportedFormat ->
            supportedFormat.container == format.container &&
            (supportedFormat.videoCodec == null || supportedFormat.videoCodec == format.videoCodec) &&
            (supportedFormat.audioCodec == null || supportedFormat.audioCodec == format.audioCodec)
        }
    }
    
    /**
     * Gets platform-specific feature flag value
     */
    fun getFeature(featureName: String): Boolean {
        return features[featureName] ?: false
    }
    
    /**
     * Creates a copy with updated UI scaling
     */
    fun withOptimalUIScaling(): PlatformConfiguration {
        return copy(uiScaling = getOptimalUIScaling())
    }
    
    /**
     * Creates a copy with updated feature flags
     */
    fun withFeature(featureName: String, enabled: Boolean): PlatformConfiguration {
        return copy(features = features + (featureName to enabled))
    }
    
    /**
     * Gets recommended memory allocation based on hardware capabilities
     */
    fun getRecommendedMemoryAllocation(): Long {
        val totalMemory = hardwareCapabilities?.memoryTotal ?: 2_000_000_000L // Default 2GB
        return minOf(limits.maxMemoryUsage, totalMemory / 4) // Use max 25% of total memory
    }
}