//
//  ContentDetailView.swift
//  kmptv
//
//  Full-bleed backdrop with title, description, metadata chips, and Play /
//  Watchlist actions. Mirrors the Android TV `ContentDetailScreen`.
//
//  Play is only offered when the item has a playable `videoUrl` — we don't
//  silently fabricate a fallback.
//

import SwiftUI

struct ContentDetailView: View {
    let item: ContentItem
    let onBack: () -> Void
    let onPlay: (ContentItem) -> Void

    var body: some View {
        ZStack(alignment: .leading) {
            // Full-bleed backdrop.
            AsyncImage(url: item.thumbnailUrl) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                default:
                    Color.black
                }
            }
            .ignoresSafeArea()

            // Horizontal scrim for text legibility.
            LinearGradient(
                colors: [Color.black, Color.black.opacity(0.6), .clear],
                startPoint: .leading,
                endPoint: .trailing
            )
            .frame(maxWidth: 1200)
            .ignoresSafeArea()

            // Vertical fade to solid black at the bottom.
            LinearGradient(
                colors: [.clear, Color.black.opacity(0.85)],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 16) {
                // Back
                Button(action: onBack) {
                    Label("Back", systemImage: "chevron.left")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 10)
                        .background(Color.white.opacity(0.15))
                        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                }
                .buttonStyle(TVFocusButtonStyle(focusedScale: 1.03, cornerRadius: 8, strokeWidth: 2))

                Spacer(minLength: 0)

                // Title + metadata chips + description
                VStack(alignment: .leading, spacing: 16) {
                    Text(item.title)
                        .font(.system(size: 54, weight: .bold))
                        .foregroundStyle(.white)
                        .lineLimit(2)

                    HStack(spacing: 10) {
                        if let year = item.releaseYearString { MetadataChip(text: year) }
                        if let duration = item.formattedDuration { MetadataChip(text: duration) }
                        if let rating = item.contentRatingString { MetadataChip(text: rating) }
                        if let genre = item.genre { MetadataChip(text: genre) }
                    }

                    if !item.tags.isEmpty {
                        TagsRow(tags: Array(item.tags.prefix(4)))
                    }

                    if let description = item.description {
                        Text(description)
                            .font(.system(size: 20))
                            .foregroundStyle(.white.opacity(0.8))
                            .lineLimit(4)
                            .lineSpacing(4)
                    }
                }

                // Action buttons
                HStack(spacing: 20) {
                    if item.isPlayable {
                        Button(action: { onPlay(item) }) {
                            HStack(spacing: 10) {
                                Image(systemName: "play.fill")
                                Text("Play")
                            }
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundStyle(.black)
                            .padding(.horizontal, 40)
                            .padding(.vertical, 14)
                            .background(Color.white)
                            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                        }
                        .buttonStyle(TVFocusButtonStyle(focusedScale: 1.04, cornerRadius: 8, strokeWidth: 2))
                    }

                    Button(action: { /* Watchlist is not wired yet */ }) {
                        HStack(spacing: 10) {
                            Image(systemName: "plus")
                            Text("Watchlist")
                        }
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 40)
                        .padding(.vertical, 14)
                        .background(Color.white.opacity(0.15))
                        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                    }
                    .buttonStyle(TVFocusButtonStyle(focusedScale: 1.04, cornerRadius: 8, strokeWidth: 2))
                }
                .padding(.top, 8)
            }
            .frame(maxWidth: 760, alignment: .leading)
            .padding(.horizontal, 60)
            .padding(.vertical, 60)
        }
    }
}

private struct MetadataChip: View {
    let text: String
    var body: some View {
        Text(text)
            .font(.system(size: 15, weight: .medium))
            .foregroundStyle(.white.opacity(0.9))
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .overlay(
                RoundedRectangle(cornerRadius: 4, style: .continuous)
                    .stroke(Color.white.opacity(0.3), lineWidth: 1)
            )
    }
}

private struct TagsRow: View {
    let tags: [String]
    var body: some View {
        HStack(spacing: 10) {
            ForEach(Array(tags.enumerated()), id: \.offset) { idx, tag in
                Text(tag.capitalized)
                    .font(.system(size: 15))
                    .foregroundStyle(.white.opacity(0.55))
                if idx != tags.count - 1 {
                    Text("·")
                        .font(.system(size: 15))
                        .foregroundStyle(.white.opacity(0.35))
                }
            }
        }
    }
}
