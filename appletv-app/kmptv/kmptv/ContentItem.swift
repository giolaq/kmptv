//
//  ContentItem.swift
//  kmptv
//
//  Platform-local mirror of the feed's content schema. This duplicates the
//  Kotlin `ContentItem` in `shared-core`. The canonical long-term plan is to
//  consume `shared-core` as an XCFramework and delete this file; until then
//  the two sides must evolve together.
//

import Foundation

struct ContentItem: Identifiable, Hashable {
    let id: String
    let title: String
    let description: String?
    let thumbnailUrl: URL?
    let videoUrl: URL?
    let genre: String?
    let releaseYear: Int?
    let contentRating: String?
    let durationMs: Int
    let tags: [String]
    let priority: Int

    /// Whether this item has a playable video source.
    var isPlayable: Bool { videoUrl != nil }

    /// Duration formatted as `1h 23m` / `23m`, or nil if unknown (0).
    var formattedDuration: String? {
        guard durationMs > 0 else { return nil }
        let totalMinutes = durationMs / 60_000
        let hours = totalMinutes / 60
        let minutes = totalMinutes % 60
        return hours > 0 ? "\(hours)h \(minutes)m" : "\(minutes)m"
    }

    /// Release year as a display string, or nil if missing.
    var releaseYearString: String? {
        guard let y = releaseYear, y > 0 else { return nil }
        return String(y)
    }

    /// Content rating only if non-empty.
    var contentRatingString: String? {
        guard let r = contentRating, !r.isEmpty else { return nil }
        return r
    }
}
