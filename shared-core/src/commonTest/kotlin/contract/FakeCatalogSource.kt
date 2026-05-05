package com.kmptv.shared_core.contract

import com.kmptv.shared_core.models.*
import com.kmptv.shared_core.services.CatalogSource

/**
 * Test double for [CatalogSource] that returns a fixed in-memory catalogue.
 *
 * Lets contract tests run deterministically without depending on host
 * network access — the iOS simulator Kotlin test runner in particular has
 * no reachable network, which would otherwise blow every repository test up.
 */
class FakeCatalogSource(private val items: List<ContentItem>) : CatalogSource {
    var callCount: Int = 0
        private set

    override suspend fun fetchCatalog(): Result<List<ContentItem>> {
        callCount++
        return Result.Success(items)
    }

    companion object {
        /** A small, deterministic catalogue sufficient for contract assertions. */
        fun defaultCatalog(): List<ContentItem> = listOf(
            ContentItem(
                id = "test-001",
                title = "Big Buck Bunny",
                description = "Open-movie rabbit caper used as a known-good test fixture",
                contentType = ContentType.Video,
                metadata = ContentMetadata(
                    duration = 596_000L,
                    quality = VideoQuality.HD_1080p,
                    genre = "Animation",
                ),
                priority = 10,
                tags = listOf("animation", "popular"),
                isOfflineAvailable = true,
                videoUrl = "https://example.invalid/BigBuckBunny.mp4",
            ),
            ContentItem(
                id = "test-002",
                title = "Elephant Dreams",
                description = "Another open-movie fixture, this time with dreaming elephants",
                contentType = ContentType.Video,
                metadata = ContentMetadata(
                    duration = 653_000L,
                    genre = "Animation",
                ),
                priority = 9,
                tags = listOf("animation"),
                videoUrl = "https://example.invalid/ElephantsDream.mp4",
            ),
            ContentItem(
                id = "test-003",
                title = "Tears of Steel",
                description = "Sci-fi short with live action and CGI",
                contentType = ContentType.Video,
                metadata = ContentMetadata(
                    duration = 734_000L,
                    genre = "Sci-Fi",
                ),
                priority = 8,
                tags = listOf("sci-fi", "action"),
                videoUrl = "https://example.invalid/TearsOfSteel.mp4",
            ),
        )
    }
}

/** Constructs a pre-populated fake catalogue source ready for use in tests. */
internal fun defaultFakeCatalog(): FakeCatalogSource =
    FakeCatalogSource(FakeCatalogSource.defaultCatalog())
