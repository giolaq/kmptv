package com.kmptv.shared_core.models

/**
 * Content metadata containing additional information about media items
 */
data class ContentMetadata(
    val duration: Long? = null,                    // Content duration in milliseconds (for video/audio)
    val fileSize: Long? = null,                    // File size in bytes
    val format: MediaFormat? = null,               // Media format information
    val quality: VideoQuality? = null,             // Video quality settings
    val language: String? = null,                  // Content language
    val subtitleLanguages: List<String> = emptyList(), // Available subtitle languages
    val releaseDate: String? = null,               // Release date in ISO format
    val genre: String? = null,                     // Content genre
    val rating: String? = null,                    // Content rating (e.g., "PG", "R")
    val director: String? = null,                  // Director name
    val cast: List<String> = emptyList(),          // Cast member names
    val synopsis: String? = null                   // Detailed synopsis
)

/**
 * User preferences for the TV application
 */
data class UserPreferences(
    val language: String = "en",                   // Preferred interface language
    val subtitleEnabled: Boolean = false,          // Subtitle preferences
    val audioLevel: Float = 0.7f,                 // Audio volume preference (0.0-1.0)
    val parentalControlLevel: ParentalLevel = ParentalLevel.None, // Content filtering level
    val autoplay: Boolean = true,                  // Autoplay preference
    val offlineMode: Boolean = false,              // Offline mode preference
    val theme: String = "dark",                    // UI theme preference
    val gridSize: Int = 3,                        // Number of columns in content grid
    val focusAnimationEnabled: Boolean = true,     // Enable focus animations
    val voiceControlEnabled: Boolean = false       // Voice control preference
)

/**
 * Device information for the TV device
 */
data class DeviceInfo(
    val deviceId: String,                          // Unique device identifier
    val model: String,                             // TV device model
    val osVersion: String,                         // Operating system version
    val availableMemory: Long,                     // Available memory in bytes
    val storageSpace: Long,                        // Available storage space in bytes
    val networkType: NetworkType,                  // Connection type (WiFi, Ethernet)
    val screenWidth: Int,                          // Screen width in pixels
    val screenHeight: Int,                         // Screen height in pixels
    val densityDpi: Int,                           // Screen density DPI
    val supports4K: Boolean = false,               // 4K video support
    val supportsHDR: Boolean = false,              // HDR support
    val maxRefreshRate: Int = 60                   // Maximum refresh rate in Hz
)

/**
 * Hardware capabilities information
 */
data class HardwareInfo(
    val processorInfo: String,                     // CPU information
    val memoryTotal: Long,                         // Total device memory in bytes
    val gpuInfo: String? = null,                   // Graphics processor information
    val supports4K: Boolean = false,               // 4K video support
    val supportsHDR: Boolean = false,              // HDR support
    val maxRefreshRate: Int = 60,                  // Maximum refresh rate in Hz
    val codecSupport: List<String> = emptyList(),  // Supported video/audio codecs
    val networkInterfaces: List<String> = emptyList() // Available network interfaces
)

/**
 * Screen resolution information
 */
data class Resolution(
    val width: Int,                                // Screen width in pixels
    val height: Int,                               // Screen height in pixels
    val aspectRatio: String,                       // Aspect ratio (e.g., "16:9", "4:3")
    val isDolbyVision: Boolean = false,            // HDR Dolby Vision support
    val refreshRate: Int = 60                      // Refresh rate in Hz
)

/**
 * Sync status for content synchronization
 */
data class SyncStatus(
    val lastSyncTimestamp: Long,                   // Last successful sync timestamp
    val itemsSynced: Int,                          // Number of items synchronized
    val conflicts: List<String> = emptyList(),     // List of conflicted item IDs
    val errors: List<String> = emptyList(),        // List of error messages
    val nextSyncScheduled: Long? = null            // Next scheduled sync timestamp
)

/**
 * Download progress for offline content
 */
data class DownloadProgress(
    val contentId: String,                         // Content identifier being downloaded
    val bytesDownloaded: Long,                     // Bytes downloaded so far
    val totalBytes: Long,                          // Total bytes to download
    val percentage: Float,                         // Download percentage (0.0-1.0)
    val status: DownloadStatus,                    // Current download status
    val estimatedTimeRemaining: Long? = null,     // Estimated time remaining in seconds
    val downloadSpeed: Long? = null               // Download speed in bytes per second
)