package com.kmptv.shared_core.services

import com.kmptv.shared_core.models.*
import com.kmptv.shared_core.platformEngine
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val CATALOG_URL = "https://giolaq.github.io/scrap-tv-feed/catalog.json"

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

class CatalogService : CatalogSource {

    private val client = HttpClient(platformEngine()) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 15_000
        }
    }

    override suspend fun fetchCatalog(): Result<List<ContentItem>> {
        return try {
            val httpResponse = client.get(CATALOG_URL)
            if (httpResponse.status.value !in 200..299) {
                return Result.Error(
                    RuntimeException("HTTP ${httpResponse.status.value}"),
                    "Catalog request failed with status ${httpResponse.status.value}",
                )
            }
            val response: CatalogResponse = httpResponse.body()
            val items = response.items.mapIndexed { index, item ->
                item.toContentItem(priority = response.items.size - index)
            }
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e, "Failed to fetch catalog: ${e.message}")
        }
    }

    fun close() {
        client.close()
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
