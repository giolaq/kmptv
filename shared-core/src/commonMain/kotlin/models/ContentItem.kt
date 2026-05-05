package com.kmptv.shared_core.models

/**
 * Represents any media content that can be displayed and navigated in the TV interface.
 */
data class ContentItem(
    val id: String,                                // Unique content identifier
    val title: String,                             // Content display title (1-100 characters)
    val description: String? = null,               // Optional content description (max 500 characters)
    val thumbnailUrl: String? = null,              // Optional thumbnail image URL
    val contentType: ContentType,                  // Type of content (Video, Audio, Image, Mixed)
    val metadata: ContentMetadata = ContentMetadata(), // Additional content information
    val isOfflineAvailable: Boolean = false,       // Whether content can be viewed offline
    val lastAccessed: Long? = null,                // Timestamp of last user interaction
    val focusable: Boolean = true,                 // Whether item can receive navigation focus
    val collections: List<String> = emptyList(),   // Collection IDs this content belongs to
    val tags: List<String> = emptyList(),          // Content tags for filtering/search
    val priority: Int = 0,                         // Display priority (higher = more prominent)
    val videoUrl: String? = null,                  // Direct video playback URL
) {

    /**
     * Validates the content item fields.
     */
    fun isValid(): Boolean {
        return title.isNotEmpty() &&
            title.length <= 100 &&
            (description?.length ?: 0) <= 500 &&
            isValidThumbnailUrl()
    }

    /**
     * Validates the thumbnail URL format if provided.
     */
    private fun isValidThumbnailUrl(): Boolean {
        return thumbnailUrl?.let { url ->
            url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")
        } ?: true
    }

    /**
     * Creates a copy with updated last accessed timestamp.
     */
    fun markAsAccessed(): ContentItem = copy(lastAccessed = nowMillis())

    /**
     * Creates a copy with updated offline availability.
     */
    fun setOfflineAvailable(available: Boolean): ContentItem = copy(isOfflineAvailable = available)
}
