package com.kmptv.shared_core.models

/**
 * Represents any media content that can be displayed and navigated in the TV interface
 */
data class ContentItem(
    val id: String,                                // Unique content identifier
    val title: String,                             // Content display title (1-100 characters)
    val description: String? = null,               // Optional content description (max 500 characters)
    val thumbnailUrl: String? = null,              // Optional thumbnail image URL
    val contentType: ContentType,                  // Type of content (Video, Audio, Image, Mixed)
    val metadata: ContentMetadata = ContentMetadata(), // Additional content information
    val isOfflineAvailable: Boolean = false,      // Whether content can be viewed offline
    val lastAccessed: Long? = null,                // Timestamp of last user interaction
    val focusable: Boolean = true,                 // Whether item can receive navigation focus
    val collections: List<String> = emptyList(),   // Collection IDs this content belongs to
    val tags: List<String> = emptyList(),          // Content tags for filtering/search
    val priority: Int = 0                          // Display priority (higher = more prominent)
) {
    
    /**
     * Content state enumeration
     */
    enum class State {
        New,
        Loaded,
        Focused,
        Selected,
        Playing,
        Paused,
        Stopped,
        Error
    }
    
    /**
     * Validates the content item fields
     */
    fun isValid(): Boolean {
        return title.isNotEmpty() && 
               title.length <= 100 &&
               (description?.length ?: 0) <= 500 &&
               isValidThumbnailUrl()
    }
    
    /**
     * Validates the thumbnail URL format if provided
     */
    private fun isValidThumbnailUrl(): Boolean {
        return thumbnailUrl?.let { url ->
            url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")
        } ?: true
    }
    
    /**
     * Checks if the content is suitable for TV viewing (10-foot UI)
     */
    fun isTVOptimized(): Boolean {
        // Title should be readable from 10 feet
        return title.length <= 50 && // Shorter titles are better for TV
               focusable && // Must be focusable for remote navigation
               thumbnailUrl != null // Visual representation important for TV
    }
    
    /**
     * Gets content age in days since last access
     */
    fun getContentAge(): Long {
        return lastAccessed?.let { 
            (System.currentTimeMillis() - it) / (24 * 60 * 60 * 1000)
        } ?: Long.MAX_VALUE
    }
    
    /**
     * Checks if content should be prioritized based on recent access
     */
    fun isRecentlyAccessed(dayThreshold: Int = 7): Boolean {
        return getContentAge() <= dayThreshold
    }
    
    /**
     * Creates a copy with updated last accessed timestamp
     */
    fun markAsAccessed(): ContentItem {
        return copy(lastAccessed = System.currentTimeMillis())
    }
    
    /**
     * Creates a copy with updated offline availability
     */
    fun setOfflineAvailable(available: Boolean): ContentItem {
        return copy(isOfflineAvailable = available)
    }
}