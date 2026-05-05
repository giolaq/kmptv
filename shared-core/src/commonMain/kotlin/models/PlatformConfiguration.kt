package com.kmptv.shared_core.models

/**
 * Platform-specific settings and configuration options.
 */
data class PlatformConfiguration(
    val platform: Platform,                        // Target platform (AndroidTV, AppleTV)
    val inputMethods: List<InputMethod>,           // Supported input types
    val screenResolution: Resolution,              // TV screen resolution info
    val supportedFormats: List<MediaFormat>,       // Supported media formats
    val navigationStyle: NavigationStyle,          // Platform navigation patterns
    val uiScaling: Float = 1.0f,                   // UI scaling factor for screen size (0.5-3.0)
    val features: Map<String, Boolean> = emptyMap(), // Platform-specific feature flags
) {

    /**
     * Validates the platform configuration.
     */
    fun isValid(): Boolean {
        return inputMethods.isNotEmpty() &&
            uiScaling in 0.5f..3.0f &&
            isValidForPlatform() &&
            hasRequiredInputMethods()
    }

    /**
     * Checks if configuration is valid for the specified platform.
     */
    private fun isValidForPlatform(): Boolean = when (platform) {
        Platform.AndroidTV ->
            inputMethods.contains(InputMethod.RemoteControl) &&
                navigationStyle in listOf(NavigationStyle.DirectionalPad, NavigationStyle.TouchGestures)
        Platform.AppleTV ->
            inputMethods.contains(InputMethod.SiriRemote) &&
                navigationStyle in listOf(NavigationStyle.TouchGestures, NavigationStyle.DirectionalPad)
    }

    /**
     * Checks if platform has required input methods. All supported platforms
     * must offer at least one directional-navigation input.
     */
    private fun hasRequiredInputMethods(): Boolean =
        inputMethods.any { it == InputMethod.RemoteControl || it == InputMethod.SiriRemote }
}
