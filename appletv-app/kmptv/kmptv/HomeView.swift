//
//  HomeView.swift
//  kmptv
//
//  Hero banner pinned to the top, a vertical stack of genre rows below.
//  Matches the Android TV `HomeScreen` layout. Focus on any card updates the
//  hero banner to that card's content.
//

import SwiftUI

struct HomeView: View {
    let items: [ContentItem]
    let onSelect: (ContentItem) -> Void

    @FocusState private var focusedCardID: String?
    @State private var focusedItem: ContentItem?

    /// Genre → items, sorted alphabetically. Empty genres are filtered out.
    private var rows: [(String, [ContentItem])] {
        Dictionary(grouping: items, by: { $0.genre ?? "Other" })
            .filter { !$0.value.isEmpty }
            .sorted { $0.key < $1.key }
            .map { ($0.key, $0.value.sorted { $0.priority > $1.priority }) }
    }

    var body: some View {
        VStack(spacing: 0) {
            HeroBannerView(item: focusedItem ?? items.first, height: 420)

            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(rows, id: \.0) { pair in
                        CategoryRowView(
                            title: pair.0,
                            items: pair.1,
                            focusedCardID: $focusedCardID,
                            onSelect: onSelect
                        )
                    }
                }
                .padding(.bottom, 40)
            }
        }
        .background(Color.black.ignoresSafeArea())
        .onAppear {
            // Seed the hero with the highest-priority item so the banner isn't empty
            // before the user has moved focus.
            focusedItem = items.max(by: { $0.priority < $1.priority }) ?? items.first
        }
        .onChange(of: focusedCardID) { _, newID in
            guard let id = newID, let match = items.first(where: { $0.id == id }) else { return }
            focusedItem = match
        }
    }
}
