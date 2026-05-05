package com.kmptv.shared_core.repositories

import com.kmptv.shared_core.models.*
import com.kmptv.shared_core.services.CatalogService
import com.kmptv.shared_core.services.CatalogSource

/**
 * Interface for content management operations.
 */
interface ContentRepository {
    suspend fun getContentItems(limit: Int = 50, offset: Int = 0): Result<List<ContentItem>>
    suspend fun getContentItem(id: String): Result<ContentItem?>
    suspend fun searchContent(query: String): Result<List<ContentItem>>
    suspend fun markContentAccessed(contentId: String): Result<Unit>
}

/**
 * In-memory implementation of [ContentRepository].
 *
 * Content is loaded lazily from [CatalogService] on first access. There is
 * intentionally no hardcoded sample fallback — the remote catalogue is the
 * source of truth. A failed fetch surfaces as a `Result.Error` so the app can
 * render a real error state; subsequent calls will retry the fetch.
 */
class ContentRepositoryImpl(
    private val catalogService: CatalogSource = CatalogService(),
) : ContentRepository {

    private val contentStorage = mutableMapOf<String, ContentItem>()
    private var catalogLoaded = false

    override suspend fun getContentItems(limit: Int, offset: Int): Result<List<ContentItem>> {
        if (!catalogLoaded) {
            val fetchResult = catalogService.fetchCatalog()
            when (fetchResult) {
                is Result.Success -> {
                    fetchResult.data.forEach { contentStorage[it.id] = it }
                    catalogLoaded = true
                }
                is Result.Error -> return Result.Error(fetchResult.exception, fetchResult.message)
            }
        }
        return try {
            val sorted = contentStorage.values.sortedByDescending { it.priority }
            Result.Success(sorted.drop(offset).take(limit))
        } catch (e: Exception) {
            Result.Error(e, "Failed to retrieve content items: ${e.message}")
        }
    }

    override suspend fun getContentItem(id: String): Result<ContentItem?> {
        return try {
            Result.Success(contentStorage[id])
        } catch (e: Exception) {
            Result.Error(e, "Failed to retrieve content item: ${e.message}")
        }
    }

    override suspend fun searchContent(query: String): Result<List<ContentItem>> {
        return try {
            val matching = contentStorage.values.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                    item.description?.contains(query, ignoreCase = true) == true ||
                    item.tags.any { it.contains(query, ignoreCase = true) }
            }
            Result.Success(matching.sortedByDescending { it.priority })
        } catch (e: Exception) {
            Result.Error(e, "Failed to search content: ${e.message}")
        }
    }

    override suspend fun markContentAccessed(contentId: String): Result<Unit> {
        return try {
            val item = contentStorage[contentId]
                ?: return Result.Error(IllegalArgumentException("Content not found"), "Content with id $contentId not found")
            contentStorage[contentId] = item.markAsAccessed()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to mark content as accessed: ${e.message}")
        }
    }
}
