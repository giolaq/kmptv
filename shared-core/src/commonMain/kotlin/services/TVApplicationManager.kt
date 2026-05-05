package com.kmptv.shared_core.services

import com.kmptv.shared_core.models.*

/**
 * Interface for managing TV application lifecycle and configuration.
 */
interface TVApplicationManager {
    suspend fun initialize(platform: Platform, configuration: PlatformConfiguration): TVApplication
    suspend fun shutdown()
    fun getCurrentSession(): UserSession?
    suspend fun updateConfiguration(config: PlatformConfiguration): Result<Unit>
}

/**
 * In-memory implementation of [TVApplicationManager].
 */
class TVApplicationManagerImpl : TVApplicationManager {
    private var currentApplication: TVApplication? = null
    private var isInitialized = false

    override suspend fun initialize(platform: Platform, configuration: PlatformConfiguration): TVApplication {
        if (isInitialized) {
            currentApplication?.let { return it }
        }

        if (configuration.platform != platform) {
            throw IllegalArgumentException(
                "Configuration platform ${configuration.platform} does not match requested platform $platform",
            )
        }

        if (!configuration.isValid()) {
            throw IllegalArgumentException("Invalid platform configuration")
        }

        val deviceInfo = DeviceInfoDefaults.forPlatform(
            platform = platform,
            resolution = configuration.screenResolution,
        )

        val initialSession = UserSession(
            sessionId = IdGenerator.sessionId(),
            deviceInfo = deviceInfo,
            lastActivity = nowMillis(),
            sessionTimeout = SessionConstants.GUEST_TIMEOUT_MS,
        )

        val application = TVApplication(
            id = IdGenerator.applicationId(),
            platform = platform,
            version = "0.1.0",
            configuration = configuration,
            sessionManager = initialSession,
            isInitialized = true,
            startupTime = nowMillis(),
        )

        currentApplication = application
        isInitialized = true
        return application
    }

    override suspend fun shutdown() {
        currentApplication = null
        isInitialized = false
    }

    override fun getCurrentSession(): UserSession? = currentApplication?.sessionManager

    override suspend fun updateConfiguration(config: PlatformConfiguration): Result<Unit> {
        return try {
            val currentApp = currentApplication
                ?: return Result.Error(IllegalStateException("Application not initialized"), "Application not initialized")

            if (!config.isValid()) {
                return Result.Error(IllegalArgumentException("Invalid configuration"), "Configuration validation failed")
            }

            if (config.platform != currentApp.platform) {
                return Result.Error(
                    IllegalArgumentException("Platform mismatch"),
                    "Cannot change platform after initialization",
                )
            }

            currentApplication = currentApp.copy(configuration = config)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update configuration: ${e.message}")
        }
    }
}
