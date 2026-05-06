//
//  ContentView.swift
//  kmptv
//
//  Top-level navigation shell: owns the catalogue fetch and routes between
//  the Loading / Error / Home / Detail / Player states. Mirrors the Android
//  TV `MainActivity` state machine.
//

import SwiftUI

struct ContentView: View {
    @State private var contentItems: [ContentItem] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    @State private var selectedItem: ContentItem?
    @State private var playingItem: ContentItem?

    var body: some View {
        Group {
            if isLoading {
                LoadingView()
            } else if let message = errorMessage {
                ErrorView(message: message) {
                    Task { await loadContent() }
                }
            } else {
                HomeView(items: contentItems) { item in
                    selectedItem = item
                }
            }
        }
        .background(Color.black.ignoresSafeArea())
        .task {
            await loadContent()
        }
        .sheet(item: $selectedItem) { item in
            // Stack the player on top of the detail rather than swapping
            // between them: setting `selectedItem = nil` here would dismiss
            // the sheet first, exposing the home view for a frame before the
            // player's fullScreenCover takes over (the "flash" bug). Instead
            // we keep the detail underneath and present the player on top.
            // When the player dismisses, the user returns to the detail screen
            // — matching standard streaming-app back-navigation.
            ContentDetailView(
                item: item,
                onBack: { selectedItem = nil },
                onPlay: { tapped in
                    guard tapped.isPlayable else { return }
                    playingItem = tapped
                }
            )
            .fullScreenCover(item: $playingItem) { playing in
                if let url = playing.videoUrl {
                    VideoPlayerView(item: playing, videoURL: url) {
                        playingItem = nil
                    }
                }
            }
        }
    }

    @MainActor
    private func loadContent() async {
        isLoading = true
        errorMessage = nil
        do {
            let items = try await CatalogFeed().fetchItems()
            contentItems = items
            isLoading = false
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
        }
    }
}

// MARK: - Loading

private struct LoadingView: View {
    var body: some View {
        VStack(spacing: 20) {
            ProgressView()
                .scaleEffect(1.5)
                .progressViewStyle(CircularProgressViewStyle(tint: .white))
            Text("Loading catalogue…")
                .font(.title3)
                .foregroundStyle(.white.opacity(0.7))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.black)
    }
}

// MARK: - Error

private struct ErrorView: View {
    let message: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 48))
                .foregroundStyle(.orange)

            Text("Couldn't load the catalogue")
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundStyle(.white)

            Text(message)
                .font(.body)
                .foregroundStyle(.white.opacity(0.7))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 80)

            Button(action: onRetry) {
                Text("Try again")
                    .font(.system(size: 18, weight: .medium))
                    .foregroundStyle(.black)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 12)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            }
            .buttonStyle(TVFocusButtonStyle(focusedScale: 1.04, cornerRadius: 8, strokeWidth: 2))
            .padding(.top, 12)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.black)
    }
}

#Preview {
    ContentView()
}
