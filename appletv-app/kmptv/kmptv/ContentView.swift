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

    // Function to get video URL for streaming
    func getVideoUrl() -> String? {
        guard contentType == .video else { return nil }

        switch title {
        case "Big Buck Bunny":
            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        case "Elephant Dreams":
            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        case "Tears of Steel":
            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        default:
            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" // Default video
        }
    }
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
    @State private var selectedItem: ContentItem?
    @State private var showingDetail = false
    @State private var showingVideoPlayer = false
    
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
                                    // Handle item selection - show detail screen
                                    selectedItem = item
                                    showingDetail = true
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
        .fullScreenCover(isPresented: $showingVideoPlayer) {
            if let selectedItem = selectedItem {
                VideoPlayerView(
                    item: selectedItem,
                    videoURL: selectedItem.getVideoUrl()
                ) {
                    // Handle back navigation from video player
                    showingVideoPlayer = false
                    showingDetail = false
                    self.selectedItem = nil
                }
            }
        }
        .sheet(isPresented: $showingDetail) {
            if let selectedItem = selectedItem {
                ContentDetailView(
                    item: selectedItem,
                    onBack: {
                        // Handle back navigation from detail
                        showingDetail = false
                        self.selectedItem = nil
                    },
                    onPlay: { item in
                        // Handle play action - show video player
                        showingDetail = false
                        showingVideoPlayer = true
                    }
                )
            }
        }
    }
    
    private func loadContent() {
        // Simulate loading delay
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            // Mock content data that matches Kotlin shared core
            contentItems = [
                ContentItem(title: "Big Buck Bunny", contentType: .video, description: "Blender Foundation's open movie featuring a giant rabbit", isOfflineAvailable: true),
                ContentItem(title: "Music Playlist", contentType: .audio, description: "Your favorite tracks", isOfflineAvailable: false),
                ContentItem(title: "Photo Gallery", contentType: .image, description: "Recent photos", isOfflineAvailable: true),
                ContentItem(title: "Mixed Media Pack", contentType: .mixed, description: "Videos and photos", isOfflineAvailable: false),
                ContentItem(title: "Elephant Dreams", contentType: .video, description: "Blender Foundation's first open movie project", isOfflineAvailable: true),
                ContentItem(title: "Podcast Collection", contentType: .audio, description: "Tech podcasts", isOfflineAvailable: false),
                ContentItem(title: "Travel Photos", contentType: .image, description: "Vacation memories", isOfflineAvailable: true),
                ContentItem(title: "Entertainment Bundle", contentType: .mixed, description: "Movies and music", isOfflineAvailable: true),
                ContentItem(title: "Tears of Steel", contentType: .video, description: "Blender Foundation's fourth open movie with live action and CGI", isOfflineAvailable: false)
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
            .scaleEffect(isFocused ? 1.05 : 1.0)
            .animation(.easeInOut(duration: 0.2), value: isFocused)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// MARK: - Content Detail View
struct ContentDetailView: View {
    let item: ContentItem
    let onBack: () -> Void
    let onPlay: (ContentItem) -> Void
    @Environment(\.isFocused) private var isFocused: Bool
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Background
                Color.black.ignoresSafeArea()
                
                VStack(spacing: 40) {
                    // Header with back navigation
                    HStack {
                        TVFocusableButton(
                            action: onBack,
                            content: {
                                HStack(spacing: 12) {
                                    Image(systemName: "chevron.left")
                                        .font(.title2)
                                    Text("Back")
                                        .font(.title2)
                                }
                                .foregroundColor(.white)
                                .padding(.horizontal, 20)
                                .padding(.vertical, 10)
                            }
                        )
                        
                        Spacer()
                        
                        Text("KMPTV")
                            .font(.title)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal, 60)
                    .padding(.top, 40)
                    
                    // Main content area
                    HStack(spacing: 60) {
                        // Hero image/thumbnail - make it focusable if it's a video
                        if item.contentType == .video {
                            TVFocusableButton(
                                action: { onPlay(item) },
                                content: {
                                    RoundedRectangle(cornerRadius: 20)
                                        .fill(Color.gray.opacity(0.3))
                                        .frame(width: 600, height: 337) // 16:9 aspect ratio
                                        .overlay(
                                            VStack(spacing: 20) {
                                                Text("📺")
                                                    .font(.system(size: 80))
                                                Text("Tap to Play")
                                                    .font(.title2)
                                                    .foregroundColor(.white)
                                            }
                                        )
                                }
                            )
                        } else {
                            RoundedRectangle(cornerRadius: 20)
                                .fill(Color.gray.opacity(0.3))
                                .frame(width: 600, height: 337) // 16:9 aspect ratio
                                .overlay(
                                    VStack(spacing: 20) {
                                        Text("📺")
                                            .font(.system(size: 80))
                                        Text(item.contentType.rawValue)
                                            .font(.title2)
                                            .foregroundColor(.white)
                                    }
                                )
                        }
                        
                        // Content details
                        VStack(alignment: .leading, spacing: 24) {
                            // Title and content type
                            VStack(alignment: .leading, spacing: 12) {
                                Text(item.title)
                                    .font(.largeTitle)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)
                                
                                HStack(spacing: 16) {
                                    Text(item.contentType.rawValue)
                                        .font(.headline)
                                        .fontWeight(.semibold)
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 8)
                                        .background(item.contentType.color)
                                        .cornerRadius(8)
                                    
                                    if item.isOfflineAvailable {
                                        HStack(spacing: 6) {
                                            Image(systemName: "arrow.down.circle.fill")
                                                .foregroundColor(.green)
                                            Text("Available Offline")
                                                .foregroundColor(.green)
                                        }
                                        .font(.subheadline)
                                    }
                                }
                            }
                            
                            // Description
                            if let description = item.description {
                                Text(description)
                                    .font(.title3)
                                    .foregroundColor(.secondary)
                                    .lineLimit(4)
                            }
                            
                            // Additional details
                            VStack(alignment: .leading, spacing: 12) {
                                DetailRow(title: "Duration", value: "2h 15m")
                                DetailRow(title: "Quality", value: "4K HDR")
                                DetailRow(title: "Audio", value: "Dolby Atmos")
                                DetailRow(title: "Released", value: "2024")
                            }
                            
                            // Action buttons
                            HStack(spacing: 30) {
                                TVFocusableButton(
                                    action: { onPlay(item) },
                                    content: {
                                        HStack(spacing: 12) {
                                            Image(systemName: "play.fill")
                                                .font(.title3)
                                            Text("Play")
                                                .font(.title2)
                                                .fontWeight(.semibold)
                                        }
                                        .foregroundColor(.black)
                                        .padding(.horizontal, 40)
                                        .padding(.vertical, 16)
                                        .background(Color.white)
                                        .cornerRadius(12)
                                    }
                                )

                                TVFocusableButton(
                                    action: { print("Add to watchlist: \(item.title)") },
                                    content: {
                                        HStack(spacing: 12) {
                                            Image(systemName: "plus")
                                                .font(.title3)
                                            Text("Watchlist")
                                                .font(.title2)
                                                .fontWeight(.semibold)
                                        }
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 40)
                                        .padding(.vertical, 16)
                                        .background(Color.gray.opacity(0.4))
                                        .cornerRadius(12)
                                    }
                                )
                            }
                            .padding(.top, 20)
                            
                            Spacer()
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(.horizontal, 60)
                    
                    Spacer()
                }
            }
        }
    }
}

// MARK: - Detail Row Helper
struct DetailRow: View {
    let title: String
    let value: String
    
    var body: some View {
        HStack {
            Text(title)
                .font(.body)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .font(.body)
                .fontWeight(.medium)
                .foregroundColor(.white)
        }
    }
}


// MARK: - TV Focusable Button Component
struct TVFocusableButton<Content: View>: View {
    let action: () -> Void
    let content: () -> Content
    @Environment(\.isFocused) private var isFocused: Bool

    var body: some View {
        Button(action: action) {
            content()
        }
        .buttonStyle(PlainButtonStyle())
        .background(
            RoundedRectangle(cornerRadius: 12)
                .strokeBorder(
                    isFocused ? Color.white : Color.clear,
                    lineWidth: 4
                )
                .shadow(
                    color: isFocused ? Color.white.opacity(0.4) : Color.clear,
                    radius: isFocused ? 8 : 0,
                    x: 0,
                    y: isFocused ? 4 : 0
                )
        )
        .scaleEffect(isFocused ? 1.08 : 1.0)
        .animation(.easeInOut(duration: 0.2), value: isFocused)
    }
}

#Preview {
    ContentView()
}
