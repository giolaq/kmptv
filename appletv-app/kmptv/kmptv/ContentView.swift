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
                // Content Grid optimized for Apple TV
                GeometryReader { geometry in
                    let totalWidth = geometry.size.width
                    let horizontalPadding: CGFloat = 40 // Minimal edge padding
                    let cardSpacing: CGFloat = 50 // Increased spacing for focus scaling
                    let availableWidth = totalWidth - (2 * horizontalPadding) - (2 * cardSpacing)
                    let cardWidth = availableWidth / 3 // Equal width cards across full screen
                    let cardHeight = cardWidth / (16/9) // 16:9 aspect ratio
                    
                    let columns = [
                        GridItem(.fixed(cardWidth), spacing: cardSpacing),
                        GridItem(.fixed(cardWidth), spacing: cardSpacing),
                        GridItem(.fixed(cardWidth), spacing: cardSpacing)
                    ]
                    
                    ScrollView {
                        LazyVGrid(columns: columns, spacing: 40) {
                            ForEach(contentItems) { item in
                                TVCardView(item: item) {
                                    // Handle item selection
                                    print("Selected: \(item.title)")
                                }
                                .frame(width: cardWidth, height: cardHeight)
                            }
                        }
                        .padding(.horizontal, horizontalPadding)
                        .padding(.vertical, 40)
                    }
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
            // Mock content data that matches Android TV app
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
            ZStack {
                // Main card content optimized for 16:9 widescreen
                HStack(spacing: 16) {
                    // Thumbnail placeholder - optimized for widescreen
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.gray.opacity(0.2))
                        .frame(width: 100, height: 56) // 16:9 mini thumbnail
                        .overlay(
                            Text("📺")
                                .font(.system(size: 20))
                        )
                    
                    // Content info takes remaining space
                    VStack(alignment: .leading, spacing: 4) {
                        Text(item.title)
                            .font(.title3)
                            .fontWeight(.semibold)
                            .lineLimit(1)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        
                        if let description = item.description {
                            Text(description)
                                .font(.body)
                                .foregroundColor(.secondary)
                                .lineLimit(2)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        
                        Spacer(minLength: 0)
                    }
                }
                .padding(16)
                
                // Overlaid labels - positioned absolutely to avoid layout conflicts
                VStack {
                    HStack {
                        // Content type indicator
                        Text(item.contentType.rawValue)
                            .font(.caption2)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(item.contentType.color)
                            .cornerRadius(4)
                        
                        Spacer()
                        
                        // Offline indicator
                        if item.isOfflineAvailable {
                            Image(systemName: "arrow.down.circle.fill")
                                .font(.caption2)
                                .foregroundColor(.green)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 16)
                    
                    Spacer()
                }
            }
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(isFocused ? Color.blue.opacity(0.3) : Color.gray.opacity(0.15))
                    .strokeBorder(
                        isFocused ? Color.blue : Color.clear,
                        lineWidth: 3
                    )
            )
            .scaleEffect(isFocused ? 1.02 : 1.0)
            .animation(.easeInOut(duration: 0.2), value: isFocused)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    ContentView()
}
