//
//  CatalogFeed.swift
//  kmptv
//
//  Fetches the hosted TV catalogue JSON over HTTPS and maps it to
//  `ContentItem`. Mirrors `shared-core/services/CatalogService.kt` so both
//  Android TV and tvOS apps show the same content.
//

import Foundation

/// Raised when the feed response is malformed or the network call failed.
enum CatalogFeedError: LocalizedError {
    case network(Error)
    case badStatus(Int)
    case decode(Error)

    var errorDescription: String? {
        switch self {
        case .network(let e): return "Network error: \(e.localizedDescription)"
        case .badStatus(let code): return "Catalog request failed with HTTP \(code)"
        case .decode(let e): return "Failed to decode catalog: \(e.localizedDescription)"
        }
    }
}

/// Live client for the giolaq.github.io catalogue feed.
struct CatalogFeed {

    static let defaultURL = URL(string: "https://giolaq.github.io/scrap-tv-feed/catalog.json")!

    let url: URL
    private let session: URLSession

    init(url: URL = CatalogFeed.defaultURL, session: URLSession = .shared) {
        self.url = url
        self.session = session
    }

    /// Fetches the catalog and returns items ordered by descending priority.
    /// Priority mirrors the Kotlin convention: first feed item has highest priority.
    func fetchItems() async throws -> [ContentItem] {
        let (data, response) = try await session.data(from: url)

        if let http = response as? HTTPURLResponse, !(200..<300).contains(http.statusCode) {
            throw CatalogFeedError.badStatus(http.statusCode)
        }

        let decoded: FeedEnvelope
        do {
            decoded = try JSONDecoder().decode(FeedEnvelope.self, from: data)
        } catch {
            throw CatalogFeedError.decode(error)
        }

        let total = decoded.items.count
        return decoded.items.enumerated().map { index, item in
            item.toContentItem(priority: total - index)
        }
    }
}

// MARK: - Wire-format DTOs

private struct FeedEnvelope: Decodable {
    let items: [FeedItem]
}

private struct FeedItem: Decodable {
    let id: String
    let title: String
    let category: String?
    let genres: [String]?
    let trending: Bool?
    let contentRating: String?
    let releaseYear: Int?
    let durationSec: Int?
    let images: FeedImages
    let sources: [FeedSource]?
    let description: String?

    enum CodingKeys: String, CodingKey {
        case id, title, category, genres, trending, images, sources, description
        case contentRating = "content_rating"
        case releaseYear   = "release_year"
        case durationSec   = "duration_sec"
    }
}

private struct FeedImages: Decodable {
    let poster16x9: String

    enum CodingKeys: String, CodingKey {
        case poster16x9 = "poster_16x9"
    }
}

private struct FeedSource: Decodable {
    let type: String
    let url: String
}

// MARK: - DTO → domain conversion

private extension FeedItem {
    func toContentItem(priority: Int) -> ContentItem {
        // The hosted feed tags video sources as `mp4`; other deployments may
        // use the MIME form `video/mp4`. Accept either, falling through to
        // the first available source if neither matches.
        let videoTypes: Set<String> = ["video/mp4", "mp4"]
        let rawVideoUrl = sources?.first(where: { videoTypes.contains($0.type) })?.url
            ?? sources?.first?.url

        let trimmedDescription = description?.isEmpty == false ? description : nil

        var tagList = genres ?? []
        if trending == true { tagList.append("trending") }

        return ContentItem(
            id: id,
            title: title,
            description: trimmedDescription,
            thumbnailUrl: URL(string: images.poster16x9),
            videoUrl: rawVideoUrl.flatMap(URL.init(string:)),
            genre: genres?.first ?? category,
            releaseYear: releaseYear,
            contentRating: contentRating,
            durationMs: (durationSec ?? 0) * 1000,
            tags: tagList,
            priority: priority
        )
    }
}
