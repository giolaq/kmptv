//
//  CategoryRowView.swift
//  kmptv
//
//  Horizontal row of poster cards grouped under one genre label. Mirrors the
//  Android TV `ContentRow`. Cards report focus back to the parent via the
//  shared `@FocusState` binding so the hero banner can update.
//

import SwiftUI

struct CategoryRowView: View {
    let title: String
    let items: [ContentItem]
    @FocusState.Binding var focusedCardID: String?
    let onSelect: (ContentItem) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.system(size: 24, weight: .semibold))
                .foregroundStyle(.white)
                .padding(.leading, 60)

            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(spacing: 20) {
                    ForEach(items) { item in
                        TVCardView(item: item) { onSelect(item) }
                            .focused($focusedCardID, equals: item.id)
                    }
                }
                .padding(.horizontal, 60)
                // Extra vertical padding so that the focused scale-up isn't
                // clipped by the next row's edge.
                .padding(.vertical, 24)
            }
        }
    }
}
