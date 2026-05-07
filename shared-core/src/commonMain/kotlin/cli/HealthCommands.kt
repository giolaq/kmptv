package com.kmptv.shared_core.cli

import com.kmptv.shared_core.di.ServiceLocator
import com.kmptv.shared_core.models.*
import kotlinx.coroutines.runBlocking

class HealthCommands {

    fun execute(args: Array<String>): Int = runBlocking {
        val verbose = args.any { it == "--verbose" || it == "-v" }
        performHealthCheck(verbose)
    }

    private suspend fun performHealthCheck(verbose: Boolean): Int {
        println("KMPTV Shared Core Health Check")
        println("==============================")

        val results = mutableListOf<HealthResult>()
        var allHealthy = true

        try {
            val contentRepo = ServiceLocator.contentRepository()
            contentRepo.getContentItems(limit = 1)
                .onSuccess {
                    results += HealthResult("Content Repository", true, "Content repository operational")
                }
                .onFailure { e ->
                    results += HealthResult("Content Repository", false, "Content repository error: ${e.message}")
                    allHealthy = false
                }
        } catch (e: Exception) {
            results += HealthResult("Content Repository", false, "Content repository initialization failed: ${e.message}")
            allHealthy = false
        }

        try {
            val sessionManager = ServiceLocator.sessionManager()
            val deviceInfo = DeviceInfoDefaults.forPlatform(deviceId = "health-check-device")
            sessionManager.createGuestSession(deviceInfo)
                .onSuccess {
                    results += HealthResult("Session Manager", true, "Session manager operational")
                    sessionManager.endSession()
                }
                .onFailure { e ->
                    results += HealthResult("Session Manager", false, "Session manager error: ${e.message}")
                    allHealthy = false
                }
        } catch (e: Exception) {
            results += HealthResult("Session Manager", false, "Session manager initialization failed: ${e.message}")
            allHealthy = false
        }

        try {
            val appManager = ServiceLocator.applicationManager()
            val config = PlatformConfiguration(
                platform = Platform.AndroidTV,
                inputMethods = listOf(InputMethod.RemoteControl),
                screenResolution = Resolution(1920, 1080, "16:9"),
                supportedFormats = listOf(MediaFormat("mp4", "h264", "aac", null)),
                navigationStyle = NavigationStyle.DirectionalPad,
            )
            appManager.initialize(Platform.AndroidTV, config)
            appManager.shutdown()
            results += HealthResult("Application Manager", true, "Application manager operational")
        } catch (e: Exception) {
            results += HealthResult("Application Manager", false, "Application manager error: ${e.message}")
            allHealthy = false
        }

        println()
        for (result in results) {
            val status = if (result.healthy) "OK" else "FAIL"
            val line = if (verbose || !result.healthy) result.message else status
            println("${result.component.padEnd(20)} | $line")
        }
        println()

        val healthyCount = results.count { it.healthy }
        val totalCount = results.size
        return if (allHealthy) {
            println("All systems operational ($healthyCount/$totalCount checks passed)")
            0
        } else {
            println("Health check failed ($healthyCount/$totalCount checks passed)")
            1
        }
    }

    private data class HealthResult(
        val component: String,
        val healthy: Boolean,
        val message: String,
    )
}
