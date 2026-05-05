package com.kmptv.shared_core.contract

import com.kmptv.shared_core.models.*
import com.kmptv.shared_core.repositories.ContentRepositoryImpl
import com.kmptv.shared_core.services.CatalogSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Contract tests for [ContentRepositoryImpl].
 *
 * These tests inject a [FakeCatalogSource] so they are deterministic and do
 * not depend on network access — the iOS simulator's Kotlin test runner has
 * no reachable network, which previously made these tests silently pass only
 * because a hardcoded sample fallback masked the fetch failure.
 */
class ContentRepositoryContractTest {

    private fun newRepository(
        catalog: CatalogSource = defaultFakeCatalog(),
    ): ContentRepositoryImpl = ContentRepositoryImpl(catalog)

    @Test
    fun getContentItems_respects_default_limit() = runTest {
        val result = newRepository().getContentItems()
        assertTrue(result.isSuccess, "Expected success, got $result")
        val items = result.getOrNull()
        assertNotNull(items)
        assertTrue(items.size <= 50, "Default limit should be 50, got ${items.size}")
        assertTrue(items.isNotEmpty(), "Fake catalogue should have items")
    }

    @Test
    fun getContentItems_respects_custom_limit_and_offset() = runTest {
        val result = newRepository().getContentItems(limit = 2, offset = 1)
        assertTrue(result.isSuccess)
        val items = result.getOrNull()
        assertNotNull(items)
        assertTrue(items.size <= 2)
    }

    @Test
    fun getContentItems_propagates_catalog_failure() = runTest {
        val failingSource = CatalogSource {
            Result.Error(IllegalStateException("offline"), "no catalogue for you")
        }
        val result = newRepository(failingSource).getContentItems()
        assertFalse(result.isSuccess, "Expected failure result, got $result")
    }

    @Test
    fun getContentItem_returns_null_for_unknown_id() = runTest {
        val repository = newRepository()
        // Prime the storage first.
        repository.getContentItems()
        val result = repository.getContentItem("non-existent-id")
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun getContentItem_returns_item_when_present() = runTest {
        val repository = newRepository()
        val items = repository.getContentItems().getOrThrow()
        val first = items.first()
        val fetched = repository.getContentItem(first.id).getOrThrow()
        assertNotNull(fetched)
        assertEquals(first.id, fetched.id)
    }

    @Test
    fun searchContent_matches_title_and_tags() = runTest {
        val repository = newRepository()
        repository.getContentItems()
        val results = repository.searchContent("animation").getOrThrow()
        assertTrue(
            results.isNotEmpty(),
            "Search for 'animation' should match the fake catalogue, got 0 items",
        )
        assertTrue(
            results.all { r ->
                r.title.contains("animation", ignoreCase = true) ||
                    r.description?.contains("animation", ignoreCase = true) == true ||
                    r.tags.any { it.contains("animation", ignoreCase = true) }
            },
        )
    }

    @Test
    fun markContentAccessed_updates_lastAccessed_timestamp() = runTest {
        val repository = newRepository()
        val items = repository.getContentItems().getOrThrow()
        val id = items.first().id

        val beforeStamp = nowMillis()
        val result = repository.markContentAccessed(id)
        assertTrue(result.isSuccess)

        val updated = repository.getContentItem(id).getOrThrow()
        assertNotNull(updated)
        assertNotNull(updated.lastAccessed)
        assertTrue(
            updated.lastAccessed!! >= beforeStamp,
            "lastAccessed (${updated.lastAccessed}) should be >= $beforeStamp",
        )
    }

    @Test
    fun getContentItems_only_fetches_once_on_success() = runTest {
        val fake = defaultFakeCatalog()
        val repository = newRepository(fake)
        repository.getContentItems()
        repository.getContentItems()
        repository.getContentItems()
        assertEquals(1, fake.callCount, "Catalogue should be fetched lazily only once")
    }
}
