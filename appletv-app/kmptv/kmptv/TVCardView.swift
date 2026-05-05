//
//  TVCardView.swift
//  kmptv
//
//  16:9 poster card with title overlay and genre badge. Mirrors the Android
//  TV `TVCard` component: the focused card scales up, gains a white border,
//  and a soft shadow.
//

import SwiftUI

struct TVCardView: View {
    let item: ContentItem
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            ZStack(alignment: .topLeading) {
                // Poster image
                AsyncImage(url: item.thumbnailUrl) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    case .failure, .empty:
                        Color.white.opacity(0.05)
                            .overlay(
                                Image(systemName: "photo")
                                    .font(.system(size: 32))
                                    .foregroundStyle(.white.opacity(0.3))
                            )
                    @unknown default:
                        Color.white.opacity(0.05)
                    }
                }

                // Bottom gradient to make the title readable on bright posters.
                LinearGradient(
                    colors: [Color.clear, Color.black.opacity(0.85)],
                    startPoint: .center,
                    endPoint: .bottom
                )

                // Genre badge in the top-right corner.
                if let genre = item.genre {
                    VStack {
                        HStack {
                            Spacer()
                            Text(genre)
                                .font(.system(size: 12, weight: .medium))
                                .foregroundStyle(.white.opacity(0.9))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(Color.black.opacity(0.6))
                                .clipShape(RoundedRectangle(cornerRadius: 4, style: .continuous))
                        }
                        Spacer()
                    }
                    .padding(10)
                }

                // Title pinned to the bottom-left.
                VStack {
                    Spacer()
                    HStack {
                        Text(item.title)
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundStyle(.white)
                            .lineLimit(1)
                        Spacer(minLength: 0)
                    }
                }
                .padding(12)
            }
            .frame(width: 300, height: 300 * 9 / 16)
            .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
        }
        .buttonStyle(TVCardButtonStyle())
    }
}
