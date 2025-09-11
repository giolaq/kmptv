package com.kmptv.shared_core.cli

import com.kmptv.shared_core.repositories.ContentRepositoryImpl
import com.kmptv.shared_core.services.SessionManagerImpl
import com.kmptv.shared_core.services.TVApplicationManagerImpl
import kotlinx.coroutines.runBlocking

/**
 * CLI commands for system health checks
 */
class HealthCommands {
    
    fun execute(args: Array<String>): Int {
        return runBlocking {
            var verbose = false
            
            for (arg in args) {
                if (arg == "--verbose" || arg == "-v") {
                    verbose = true
                }
            }
            
            performHealthCheck(verbose)
        }
    }
    
    private suspend fun performHealthCheck(verbose: Boolean): Int {
        println("KMPTV Shared Core Health Check")
        println("==============================")
        
        var allHealthy = true
        val results = mutableListOf<HealthResult>()
        
        // Test content repository
        try {
            val contentRepo = ContentRepositoryImpl()
            val contentResult = contentRepo.getContentItems(limit = 1)
            
            contentResult.onSuccess {
                results.add(HealthResult("Content Repository", true, "✓ Content repository operational"))
            }.onFailure { exception ->
                results.add(HealthResult("Content Repository", false, "✗ Content repository error: ${exception.message}"))
                allHealthy = false
            }
        } catch (e: Exception) {
            results.add(HealthResult("Content Repository", false, "✗ Content repository initialization failed: ${e.message}"))
            allHealthy = false
        }
        
        // Test session manager
        try {
            val sessionManager = SessionManagerImpl()
            val deviceInfo = com.kmptv.shared_core.models.DeviceInfo(
                deviceId = "health-check-device",
                model = "Health Check",
                osVersion = "Test",
                availableMemory = 1_000_000_000L,
                storageSpace = 10_000_000_000L,
                networkType = com.kmptv.shared_core.models.NetworkType.WiFi,
                screenWidth = 1920,
                screenHeight = 1080,
                densityDpi = 320
            )
            
            val sessionResult = sessionManager.createGuestSession(deviceInfo)
            sessionResult.onSuccess {
                results.add(HealthResult("Session Manager", true, "✓ Session manager operational"))
                sessionManager.endSession() // Clean up test session
            }.onFailure { exception ->
                results.add(HealthResult("Session Manager", false, "✗ Session manager error: ${exception.message}"))
                allHealthy = false
            }
        } catch (e: Exception) {
            results.add(HealthResult("Session Manager", false, "✗ Session manager initialization failed: ${e.message}"))
            allHealthy = false
        }
        
        // Test application manager
        try {
            val appManager = TVApplicationManagerImpl()
            val config = com.kmptv.shared_core.models.PlatformConfiguration(
                platform = com.kmptv.shared_core.models.Platform.AndroidTV,
                inputMethods = listOf(com.kmptv.shared_core.models.InputMethod.RemoteControl),
                screenResolution = com.kmptv.shared_core.models.Resolution(1920, 1080, "16:9"),
                supportedFormats = listOf(com.kmptv.shared_core.models.MediaFormat("mp4", "h264", "aac", null)),
                navigationStyle = com.kmptv.shared_core.models.NavigationStyle.DirectionalPad
            )
            
            val appResult = appManager.initialize(com.kmptv.shared_core.models.Platform.AndroidTV, config)
            results.add(HealthResult("Application Manager", true, "✓ Application manager operational"))
            appManager.shutdown() // Clean up
        } catch (e: Exception) {
            results.add(HealthResult("Application Manager", false, "✗ Application manager error: ${e.message}"))
            allHealthy = false
        }
        
        // Memory check
        try {
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val maxMemory = runtime.maxMemory()
            
            val memoryUsagePercent = (usedMemory.toDouble() / maxMemory.toDouble()) * 100
            
            if (memoryUsagePercent < 80) {
                results.add(HealthResult("Memory Usage", true, "✓ Memory usage: ${String.format("%.1f", memoryUsagePercent)}% (${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB)"))
            } else {
                results.add(HealthResult("Memory Usage", false, "⚠ High memory usage: ${String.format("%.1f", memoryUsagePercent)}% (${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB)"))
                allHealthy = false
            }
        } catch (e: Exception) {
            results.add(HealthResult("Memory Usage", false, "✗ Memory check failed: ${e.message}"))
            allHealthy = false
        }
        
        // System properties check
        try {
            val osName = System.getProperty("os.name") ?: "Unknown"
            val javaVersion = System.getProperty("java.version") ?: "Unknown"
            results.add(HealthResult("System Info", true, "✓ OS: $osName, Java: $javaVersion"))
        } catch (e: Exception) {
            results.add(HealthResult("System Info", false, "✗ System info check failed: ${e.message}"))
        }
        
        // Print results
        println()
        for (result in results) {
            if (verbose || !result.healthy) {
                println("${result.component.padEnd(20)} | ${result.message}")
            } else {
                println("${result.component.padEnd(20)} | ${if (result.healthy) "✓ OK" else "✗ FAIL"}")
            }
        }
        
        println()
        val healthyCount = results.count { it.healthy }
        val totalCount = results.size
        
        if (allHealthy) {
            println("✓ All systems operational ($healthyCount/$totalCount checks passed)")
            return 0
        } else {
            println("✗ Health check failed ($healthyCount/$totalCount checks passed)")
            return 1
        }
    }
    
    private data class HealthResult(
        val component: String,
        val healthy: Boolean,
        val message: String
    )
}