package com.kmptv.shared_core.repositories

import com.kmptv.shared_core.models.*

/**
 * Interface for content management operations
 */
interface ContentRepository {
    suspend fun getContentItems(limit: Int = 50, offset: Int = 0): Result<List<ContentItem>>
    suspend fun getContentItem(id: String): Result<ContentItem?>
    suspend fun searchContent(query: String): Result<List<ContentItem>>
    suspend fun markContentAccessed(contentId: String): Result<Unit>
    suspend fun setContentOfflineAvailable(contentId: String, available: Boolean): Result<Unit>
    suspend fun syncContentWithRemote(): Result<SyncStatus>
}

/**
 * Implementation of ContentRepository
 */
class ContentRepositoryImpl : ContentRepository {
    
    // In-memory storage for demo purposes
    // In real implementation, this would use SQLDelight database
    private val contentStorage = mutableMapOf<String, ContentItem>()
    
    init {
        // Initialize with sample content for demo
        initializeSampleContent()
    }
    
    override suspend fun getContentItems(limit: Int, offset: Int): Result<List<ContentItem>> {
        return try {
            val allItems = contentStorage.values.sortedByDescending { it.priority }
            val paginatedItems = allItems.drop(offset).take(limit)
            Result.Success(paginatedItems)
        } catch (e: Exception) {
            Result.Error(e, "Failed to retrieve content items: ${e.message}")
        }
    }
    
    override suspend fun getContentItem(id: String): Result<ContentItem?> {
        return try {
            val item = contentStorage[id]
            Result.Success(item)
        } catch (e: Exception) {
            Result.Error(e, "Failed to retrieve content item: ${e.message}")
        }
    }
    
    override suspend fun searchContent(query: String): Result<List<ContentItem>> {
        return try {
            val matchingItems = contentStorage.values.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                item.description?.contains(query, ignoreCase = true) == true ||
                item.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
            Result.Success(matchingItems.sortedByDescending { it.priority })
        } catch (e: Exception) {
            Result.Error(e, "Failed to search content: ${e.message}")
        }
    }
    
    override suspend fun markContentAccessed(contentId: String): Result<Unit> {
        return try {
            val item = contentStorage[contentId]
                ?: return Result.Error(IllegalArgumentException("Content not found"), "Content with id $contentId not found")
            
            val updatedItem = item.markAsAccessed()
            contentStorage[contentId] = updatedItem
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to mark content as accessed: ${e.message}")
        }
    }
    
    override suspend fun setContentOfflineAvailable(contentId: String, available: Boolean): Result<Unit> {
        return try {
            val item = contentStorage[contentId]
                ?: return Result.Error(IllegalArgumentException("Content not found"), "Content with id $contentId not found")
            
            val updatedItem = item.setOfflineAvailable(available)
            contentStorage[contentId] = updatedItem
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update offline availability: ${e.message}")
        }
    }
    
    override suspend fun syncContentWithRemote(): Result<SyncStatus> {
        return try {
            // Simulate sync operation
            val currentTime = System.currentTimeMillis()
            val syncStatus = SyncStatus(
                lastSyncTimestamp = currentTime,
                itemsSynced = contentStorage.size,
                conflicts = emptyList(),
                errors = emptyList(),
                nextSyncScheduled = currentTime + (60 * 60 * 1000L) // Next sync in 1 hour
            )
            Result.Success(syncStatus)
        } catch (e: Exception) {
            Result.Error(e, "Failed to sync content: ${e.message}")
        }
    }
    
    private fun initializeSampleContent() {
        val sampleItems = listOf(
            ContentItem(
                id = "content-001",
                title = "Big Buck Bunny",
                description = "Blender Foundation's open movie featuring a giant rabbit",
                thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                contentType = ContentType.Video,
                metadata = ContentMetadata(
                    duration = 596000L, // ~10 minutes
                    quality = VideoQuality.HD_1080p,
                    genre = "Animation"
                ),
                priority = 10,
                tags = listOf("animation", "popular"),
                isOfflineAvailable = true
            ),
            ContentItem(
                id = "content-002", 
                title = "Music Playlist",
                description = "Your favorite tracks",
                contentType = ContentType.Audio,
                metadata = ContentMetadata(
                    duration = 3600000L, // 1 hour
                    quality = VideoQuality.HD_720p,
                    genre = "Music"
                ),
                priority = 9,
                tags = listOf("music", "playlist"),
                isOfflineAvailable = false
            ),
            ContentItem(
                id = "content-003",
                title = "Photo Gallery",
                description = "Recent photos",
                contentType = ContentType.Image,
                metadata = ContentMetadata(
                    duration = 0L,
                    quality = VideoQuality.UHD_4K,
                    genre = "Photography"
                ),
                priority = 8,
                tags = listOf("photos", "gallery"),
                isOfflineAvailable = true
            ),
            ContentItem(
                id = "content-004",
                title = "Mixed Media Pack",
                description = "Videos and photos",
                contentType = ContentType.Mixed,
                metadata = ContentMetadata(
                    duration = 5400000L, // 1.5 hours
                    quality = VideoQuality.HD_1080p,
                    genre = "Mixed"
                ),
                priority = 7,
                tags = listOf("mixed", "media"),
                isOfflineAvailable = false
            ),
            ContentItem(
                id = "content-005",
                title = "Elephant Dreams",
                description = "Blender Foundation's first open movie project",
                thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                contentType = ContentType.Video,
                metadata = ContentMetadata(
                    duration = 653000L, // ~11 minutes
                    quality = VideoQuality.HD_720p,
                    genre = "Animation"
                ),
                priority = 6,
                tags = listOf("animation", "education"),
                isOfflineAvailable = true
            ),
            ContentItem(
                id = "content-006",
                title = "Podcast Collection",
                description = "Tech podcasts",
                contentType = ContentType.Audio,
                metadata = ContentMetadata(
                    duration = 1800000L, // 30 minutes
                    quality = VideoQuality.HD_720p,
                    genre = "Podcast"
                ),
                priority = 5,
                tags = listOf("podcast", "tech"),
                isOfflineAvailable = false
            ),
            ContentItem(
                id = "content-007",
                title = "Travel Photos",
                description = "Vacation memories",
                contentType = ContentType.Image,
                metadata = ContentMetadata(
                    duration = 0L,
                    quality = VideoQuality.UHD_4K,
                    genre = "Travel"
                ),
                priority = 4,
                tags = listOf("travel", "vacation"),
                isOfflineAvailable = true
            ),
            ContentItem(
                id = "content-008",
                title = "Entertainment Bundle",
                description = "Movies and music",
                contentType = ContentType.Mixed,
                metadata = ContentMetadata(
                    duration = 9000000L, // 2.5 hours
                    quality = VideoQuality.HD_1080p,
                    genre = "Entertainment"
                ),
                priority = 3,
                tags = listOf("entertainment", "bundle"),
                isOfflineAvailable = true
            ),
            ContentItem(
                id = "content-009",
                title = "Tears of Steel",
                description = "Blender Foundation's fourth open movie with live action and CGI",
                thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
                contentType = ContentType.Video,
                metadata = ContentMetadata(
                    duration = 734000L, // ~12 minutes
                    quality = VideoQuality.HD_1080p,
                    genre = "Sci-Fi"
                ),
                priority = 2,
                tags = listOf("sci-fi", "action"),
                isOfflineAvailable = false
            )
        )
        
        sampleItems.forEach { item ->
            contentStorage[item.id] = item
        }
    }
}