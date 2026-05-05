//
//  HeroBannerView.swift
//  kmptv
//
//  Fixed-at-top banner showing a large backdrop of whichever card currently
//  has focus. Mirrors the Android TV `HeroBanner`: dark gradients for text
//  legibility, title + description + inline metadata chips.
//

import SwiftUI

struct HeroBannerView: View {
    let item: ContentItem?
    let height: CGFloat

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            Color.black

            if let item = item {
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
                .id(item.id) // force fresh image on focus change
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .clipped()
                .transition(.opacity)

                // Vertical fade into the page background at the bottom.
                LinearGradient(
                    colors: [Color.clear, Color.black],
                    startPoint: .top,
                    endPoint: .bottom
                )

                // Horizontal scrim on the left so text has contrast against the art.
                LinearGradient(
                    colors: [Color.black.opacity(0.8), Color.clear],
                    startPoint: .leading,
                    endPoint: .trailing
                )
                .frame(maxWidth: 800)

                // Title + description + metadata
                VStack(alignment: .leading, spacing: 12) {
                    Text(item.title)
                        .font(.system(size: 46, weight: .bold))
                        .foregroundStyle(.white)
                        .lineLimit(1)

                    if let description = item.description {
                        Text(description)
                            .font(.system(size: 18))
                            .foregroundStyle(.white.opacity(0.8))
                            .lineLimit(2)
                    }

                    HStack(spacing: 12) {
                        if let genre = item.genre { HeroChip(text: genre) }
                        if let year = item.releaseYearString { HeroChip(text: year) }
                        if let rating = item.contentRatingString { HeroChip(text: rating) }
                    }
                }
                .padding(.leading, 60)
                .padding(.bottom, 30)
                .frame(maxWidth: 700, alignment: .leading)
            }
        }
        .frame(height: height)
        .clipped()
        .animation(.easeInOut(duration: 0.3), value: item?.id)
    }
}

private struct HeroChip: View {
    let text: String
    var body: some View {
        Text(text)
            .font(.system(size: 14, weight: .medium))
            .foregroundStyle(.white.opacity(0.9))
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(Color.white.opacity(0.15))
            .clipShape(RoundedRectangle(cornerRadius: 4, style: .continuous))
    }
}
