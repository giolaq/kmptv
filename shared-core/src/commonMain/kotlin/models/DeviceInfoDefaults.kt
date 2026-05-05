package com.kmptv.shared_core.models

/**
 * Produces plausible defaults for `DeviceInfo` when real device details are not
 * available (tests, CLI, early app init, fallback paths).
 *
 * Centralising this removes four near-identical blocks that previously lived
 * in `SessionManagerImpl`, `TVApplicationManagerImpl`, `SessionCommands`, and
 * `HealthCommands`.
 */
internal object DeviceInfoDefaults {
    fun forPlatform(
        platform: Platform = Platform.AndroidTV,
        resolution: Resolution? = null,
        deviceId: String = IdGenerator.deviceId(),
    ): DeviceInfo = DeviceInfo(
        deviceId = deviceId,
        model = when (platform) {
            Platform.AndroidTV -> "Android TV Device"
            Platform.AppleTV -> "Apple TV"
        },
        osVersion = "Unknown",
        availableMemory = 2_000_000_000L, // 2 GB
        storageSpace = 8_000_000_000L,    // 8 GB
        networkType = NetworkType.WiFi,
        screenWidth = resolution?.width ?: 1920,
        screenHeight = resolution?.height ?: 1080,
        densityDpi = 320,
        supports4K = (resolution?.width ?: 0) >= 3840,
        supportsHDR = false,
        maxRefreshRate = resolution?.refreshRate ?: 60,
    )
}
