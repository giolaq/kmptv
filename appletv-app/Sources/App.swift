//
//  ContentView.swift
//  kmptv
//
//  Created by Giovanni Laquidara on 11/09/2025.
//

import SwiftUI

// MARK: - Content Item Models (Mock data for now)
struct ContentItem: Identifiable {
    let id = UUID()
    let title: String
    let contentType: ContentType
    let description: String?
    let isOfflineAvailable: Bool
}

enum ContentType: String, CaseIterable {
    case video = "Video"
    case audio = "Audio"
    case image = "Image"
    case mixed = "Mixed"
    
    var color: Color {
        switch self {
        case .video: return Color.blue
        case .audio: return Color.green
        case .image: return Color.red
        case .mixed: return Color.purple
        }
    }
}

// MARK: - Main Content View
struct ContentView: View {
    @State private var contentItems: [ContentItem] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    
    var body: some View {
        VStack(spacing: 16) {
            // Header
            Text("KMPTV")
                .font(.largeTitle)
                .fontWeight(.bold)
                .frame(maxWidth: .infinity)
                .padding(.top)
            
            if isLoading {
                // Loading State
                Spacer()
                VStack {
                    ProgressView()
                        .scaleEffect(1.5)
                        .padding()
                    Text("Loading content...")
                        .font(.title2)
                        .foregroundColor(.secondary)
                }
                Spacer()
            } else if let error = errorMessage {
                // Error State
                Spacer()
                VStack {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 48))
                        .foregroundColor(.red)
                        .padding()
                    Text("Error: \(error)")
                        .font(.title2)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                }
                Spacer()
            } else {
                // Content Grid
                ScrollView {
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 3), spacing: 20) {
                        ForEach(contentItems) { item in
                            TVCardView(item: item) {
                                // Handle item selection
                                print("Selected: \(item.title)")
                            }
                        }
                    }
                    .padding()
                }
            }
        }
        .padding(.horizontal, 24)
        .onAppear {
            loadContent()
        }
    }
    
    private func loadContent() {
        // Simulate loading delay
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            // Content data matching Android TV app's shared-core repository exactly
            contentItems = [
                ContentItem(title: "Action Movie Collection", contentType: .video, description: "Latest action movies", isOfflineAvailable: true),
                ContentItem(title: "Music Playlist", contentType: .audio, description: "Your favorite tracks", isOfflineAvailable: false),
                ContentItem(title: "Photo Gallery", contentType: .image, description: "Recent photos", isOfflineAvailable: true),
                ContentItem(title: "Mixed Media Pack", contentType: .mixed, description: "Videos and photos", isOfflineAvailable: false),
                ContentItem(title: "Documentary Series", contentType: .video, description: "Educational content", isOfflineAvailable: true),
                ContentItem(title: "Podcast Collection", contentType: .audio, description: "Tech podcasts", isOfflineAvailable: false),
                ContentItem(title: "Travel Photos", contentType: .image, description: "Vacation memories", isOfflineAvailable: true),
                ContentItem(title: "Entertainment Bundle", contentType: .mixed, description: "Movies and music", isOfflineAvailable: true),
                ContentItem(title: "Sports Highlights", contentType: .video, description: "Best moments", isOfflineAvailable: false)
            ]
            isLoading = false
        }
    }
}

// MARK: - TV Card View Component
struct TVCardView: View {
    let item: ContentItem
    let onItemClick: () -> Void
    @Environment(\.isFocused) private var isFocused: Bool
    
    var body: some View {
        Button(action: onItemClick) {
            VStack(spacing: 12) {
                // Content type indicator
                HStack {
                    Text(item.contentType.rawValue)
                        .font(.caption2)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(item.contentType.color)
                        .cornerRadius(4)
                    
                    Spacer()
                }
                
                // Thumbnail placeholder
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.gray.opacity(0.3))
                    .frame(height: 80)
                    .overlay(
                        Text("📺")
                            .font(.title)
                    )
                
                // Content info
                VStack(alignment: .leading, spacing: 4) {
                    Text(item.title)
                        .font(.headline)
                        .fontWeight(.bold)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    
                    if let description = item.description {
                        Text(description)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                
                // Offline indicator
                if item.isOfflineAvailable {
                    HStack {
                        Image(systemName: "iphone")
                            .font(.caption2)
                        Text("Offline")
                            .font(.caption2)
                            .foregroundColor(.blue)
                        Spacer()
                    }
                }
            }
            .padding(12)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(isFocused ? Color.blue.opacity(0.3) : Color.gray.opacity(0.1))
            )
            .scaleEffect(isFocused ? 1.05 : 1.0)
            .animation(.easeInOut(duration: 0.2), value: isFocused)
        }
        .buttonStyle(PlainButtonStyle())
        .frame(width: 200, height: 180)
    }
}

#Preview {
    ContentView()
}
