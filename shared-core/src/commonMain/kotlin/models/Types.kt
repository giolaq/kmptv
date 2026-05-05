package com.kmptv.shared_core.models

/**
 * Platform enumeration for supported TV platforms.
 *
 * FireTV is a future extension; until a Fire TV module is wired into
 * `settings.gradle.kts` it is intentionally absent so stale branches do not
 * linger in production `when` statements.
 */
enum class Platform {
    AndroidTV,
    AppleTV,
}

/**
 * Content type enumeration.
 */
enum class ContentType {
    Video,
    Audio,
    Image,
    Mixed,
}

/**
 * Input method enumeration for TV remotes and controllers.
 */
enum class InputMethod {
    RemoteControl,  // Traditional TV remote
    SiriRemote,     // Apple TV Siri Remote
    GameController, // Gaming controllers
}

/**
 * Navigation style enumeration.
 */
enum class NavigationStyle {
    DirectionalPad,
    TouchGestures,
    VoiceNavigation,
}

/**
 * Network connection type.
 */
enum class NetworkType {
    WiFi,
    Ethernet,
    Cellular,
    Unknown,
}

/**
 * Video quality settings.
 */
enum class VideoQuality {
    SD_480p,
    HD_720p,
    HD_1080p,
    UHD_4K,
}

/**
 * Media format information.
 */
data class MediaFormat(
    val container: String,      // e.g., "mp4", "mkv"
    val videoCodec: String?,    // e.g., "h264", "hevc"
    val audioCodec: String?,    // e.g., "aac", "ac3"
    val bitrate: Long?,         // bits per second
)
