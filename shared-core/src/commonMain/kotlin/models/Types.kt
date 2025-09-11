package com.kmptv.shared_core.models

/**
 * Platform enumeration for supported TV platforms
 */
enum class Platform {
    AndroidTV,
    AppleTV,
    FireTV
}

/**
 * Content type enumeration
 */
enum class ContentType {
    Video,
    Audio,
    Image,
    Mixed
}

/**
 * Input method enumeration for TV remotes and controllers
 */
enum class InputMethod {
    RemoteControl,  // Traditional TV remote
    SiriRemote,     // Apple TV Siri Remote
    GameController, // Gaming controllers
    Voice          // Voice input commands
}

/**
 * Navigation style enumeration
 */
enum class NavigationStyle {
    DirectionalPad,   // Up/Down/Left/Right navigation
    TouchGestures,    // Swipe and touch gestures
    VoiceNavigation   // Voice-controlled navigation
}

/**
 * Navigation result for directional input
 */
data class NavigationResult(
    val success: Boolean,
    val newFocusedItem: String?,
    val boundaryReached: Boolean = false
)

/**
 * Navigation state enumeration
 */
enum class NavigationState {
    Grid,
    List,
    Details,
    Settings,
    Search
}

/**
 * Download status for offline content
 */
enum class DownloadStatus {
    Pending,
    Downloading,
    Completed,
    Failed,
    Cancelled
}

/**
 * Network connection type
 */
enum class NetworkType {
    WiFi,
    Ethernet,
    Cellular,
    Unknown
}

/**
 * Parental control levels
 */
enum class ParentalLevel {
    None,
    Mild,
    Moderate,
    Strict
}

/**
 * Video quality settings
 */
enum class VideoQuality {
    SD_480p,
    HD_720p,
    HD_1080p,
    UHD_4K
}

/**
 * Media format information
 */
data class MediaFormat(
    val container: String,      // e.g., "mp4", "mkv"
    val videoCodec: String?,    // e.g., "h264", "hevc"
    val audioCodec: String?,    // e.g., "aac", "ac3"
    val bitrate: Long?          // bits per second
)