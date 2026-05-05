package com.kmptv.shared_core.services

import com.kmptv.shared_core.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val CATALOG_URL = "https://giolaq.github.io/scrap-tv-feed/catalog.json"

/**
 * Abstraction over "somewhere to fetch the catalogue from".
 *
 * Lets [com.kmptv.shared_core.repositories.ContentRepositoryImpl] take a fake
 * in tests so the repository contract can be exercised without depending on
 * host-machine network access (the iOS simulator in particular cannot reach
 * the live feed from the Kotlin test runner).
 */
fun interface CatalogSource {
    suspend fun fetchCatalog(): Result<List<ContentItem>>
}

@Serializable
internal data class CatalogImage(val poster_16x9: String)

@Serializable
internal data class CatalogMediaSource(val type: String, val url: String)

@Serializable
internal data class CatalogItem(
    val id: String,
    val type: String,
    val title: String,
    val category: String,
    val genres: List<String> = emptyList(),
    val trending: Boolean = false,
    val rating_count: Int = 0,
    val rating_stars: Double = 0.0,
    val content_rating: String = "",
    val release_year: Int = 0,
    val duration_sec: Int = 0,
    val images: CatalogImage,
    val sources: List<CatalogMediaSource> = emptyList(),
    val description: String = "",
)

@Serializable
internal data class CatalogResponse(
    val catalog_version: String = "",
    val updated_at: String = "",
    val items: List<CatalogItem> = emptyList(),
)

/**
 * Live [CatalogSource] backed by the hosted JSON catalogue feed.
 */
class CatalogService : CatalogSource {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun fetchCatalog(): Result<List<ContentItem>> {
        return try {
            val response: CatalogResponse = client.get(CATALOG_URL).body()
            val items = response.items.mapIndexed { index, item ->
                item.toContentItem(priority = response.items.size - index)
            }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e, "Failed to fetch catalog: ${e.message}")
        }
    }
}

private fun CatalogItem.toContentItem(priority: Int): ContentItem {
    val videoUrl = sources.firstOrNull { it.type == "video/mp4" }?.url
        ?: sources.firstOrNull()?.url

    return ContentItem(
        id = id,
        title = title,
        description = description,
        thumbnailUrl = images.poster_16x9,
        contentType = ContentType.Video,
        metadata = ContentMetadata(
            duration = duration_sec * 1000L,
            genre = genres.firstOrNull() ?: category,
            rating = content_rating,
            releaseDate = release_year.toString(),
        ),
        tags = genres + listOfNotNull(if (trending) "trending" else null),
        priority = priority,
        videoUrl = videoUrl,
    )
}
