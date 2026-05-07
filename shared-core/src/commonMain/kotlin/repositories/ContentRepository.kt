package com.kmptv.shared_core.repositories

import com.kmptv.shared_core.models.*
import com.kmptv.shared_core.services.CatalogSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface ContentRepository {
    suspend fun getContentItems(limit: Int = 50, offset: Int = 0): Result<List<ContentItem>>
    suspend fun getContentItem(id: String): Result<ContentItem?>
    suspend fun searchContent(query: String): Result<List<ContentItem>>
    suspend fun markContentAccessed(contentId: String): Result<Unit>
}

class ContentRepositoryImpl(
    private val catalogService: CatalogSource,
) : ContentRepository {

    private val mutex = Mutex()
    private val contentStorage = mutableMapOf<String, ContentItem>()
    private var catalogLoaded = false

    private suspend fun ensureCatalogLoaded(): Result<Unit> = mutex.withLock {
        if (catalogLoaded) return Result.Success(Unit)
        return when (val fetchResult = catalogService.fetchCatalog()) {
            is Result.Success -> {
                fetchResult.data.forEach { contentStorage[it.id] = it }
                catalogLoaded = true
                Result.Success(Unit)
            }
            is Result.Error -> Result.Error(fetchResult.exception, fetchResult.message)
        }
    }

    override suspend fun getContentItems(limit: Int, offset: Int): Result<List<ContentItem>> {
        val loadResult = ensureCatalogLoaded()
        if (loadResult is Result.Error) return Result.Error(loadResult.exception, loadResult.message)

        return mutex.withLock {
            val sorted = contentStorage.values.sortedByDescending { it.priority }
            Result.Success(sorted.drop(offset).take(limit))
        }
    }

    override suspend fun getContentItem(id: String): Result<ContentItem?> {
        return mutex.withLock {
            Result.Success(contentStorage[id])
        }
    }

    override suspend fun searchContent(query: String): Result<List<ContentItem>> {
        val loadResult = ensureCatalogLoaded()
        if (loadResult is Result.Error) return Result.Error(loadResult.exception, loadResult.message)

        return mutex.withLock {
            val matching = contentStorage.values.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                    item.description?.contains(query, ignoreCase = true) == true ||
                    item.tags.any { it.contains(query, ignoreCase = true) }
            }
            Result.Success(matching.sortedByDescending { it.priority })
        }
    }

    override suspend fun markContentAccessed(contentId: String): Result<Unit> {
        return mutex.withLock {
            val item = contentStorage[contentId]
                ?: return Result.Error(IllegalArgumentException("Content not found"), "Content with id $contentId not found")
            contentStorage[contentId] = item.markAsAccessed()
            Result.Success(Unit)
        }
    }
}
