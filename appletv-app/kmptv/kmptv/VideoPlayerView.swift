//
//  VideoPlayerView.swift
//  kmptv
//
//  Created by Claude on 12/09/2025.
//

import SwiftUI
import AVKit
import AVFoundation

struct VideoPlayerView: View {
    let item: ContentItem
    let videoURL: String
    let onDismiss: () -> Void
    
    @State private var player: AVPlayer?
    @State private var isPlaying = false
    @State private var showingControls = true
    @Environment(\.isFocused) private var isFocused: Bool
    
    // Demo video URL
    private let defaultVideoURL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    
    init(item: ContentItem, videoURL: String? = nil, onDismiss: @escaping () -> Void) {
        self.item = item
        self.videoURL = videoURL ?? "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        self.onDismiss = onDismiss
    }
    
    var body: some View {
        ZStack {
            // Background
            Color.black
                .ignoresSafeArea()
            
            // Video Player
            if let player = player {
                VideoPlayer(player: player) {
                    // Custom overlay controls
                    if showingControls {
                        VideoPlayerControlsOverlay(
                            player: player,
                            item: item,
                            isPlaying: $isPlaying,
                            onDismiss: onDismiss
                        )
                    }
                }
            } else {
                // Loading state
                VStack(spacing: 20) {
                    ProgressView()
                        .scaleEffect(2.0)
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    
                    Text("Loading video...")
                        .font(.title2)
                        .foregroundColor(.white)
                }
            }
        }
        .onAppear {
            setupPlayer()
        }
        .onDisappear {
            cleanupPlayer()
        }
        .onReceive(NotificationCenter.default.publisher(for: .AVPlayerItemDidPlayToEndTime)) { _ in
            // Handle video end
            player?.seek(to: .zero)
            isPlaying = false
        }
        .onExitCommand {
            // Handle menu button - go back
            player?.pause()
            onDismiss()
        }
    }
    
    private func setupPlayer() {
        guard let url = URL(string: videoURL) else {
            print("Invalid video URL: \(videoURL)")
            return
        }
        
        let playerItem = AVPlayerItem(url: url)
        let newPlayer = AVPlayer(playerItem: playerItem)
        
        // Configure player for TV
        newPlayer.allowsExternalPlayback = true
        newPlayer.usesExternalPlaybackWhileExternalScreenIsActive = true
        
        self.player = newPlayer

        // Auto-play when ready
        newPlayer.play()

        // Add time observer for play state
        let interval = CMTime(seconds: 0.1, preferredTimescale: CMTimeScale(NSEC_PER_SEC))
        newPlayer.addPeriodicTimeObserver(forInterval: interval, queue: .main) { _ in
            isPlaying = newPlayer.timeControlStatus == .playing
        }
        
        // Auto-hide controls after 5 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 5.0) {
            withAnimation(.easeOut(duration: 0.3)) {
                showingControls = false
            }
        }
    }
    
    private func cleanupPlayer() {
        player?.pause()
        player = nil
    }
}

struct VideoPlayerControlsOverlay: View {
    let player: AVPlayer
    let item: ContentItem
    @Binding var isPlaying: Bool
    let onDismiss: () -> Void
    
    // Focus states removed for tvOS compatibility - buttons will use default tvOS focus handling
    
    var body: some View {
        VStack(spacing: 0) {
            // Top controls
            HStack {
                // Back button
                Button(action: {
                    player.pause()
                    onDismiss()
                }) {
                    HStack(spacing: 12) {
                        Image(systemName: "chevron.left")
                            .font(.title2)
                        Text("Back")
                            .font(.title2)
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                    .background(
                        RoundedRectangle(cornerRadius: 10)
                            .fill(Color.black.opacity(0.5))
                    )
                }
                .buttonStyle(PlainButtonStyle())
                
                Spacer()
                
                // Title
                Text(item.title)
                    .font(.title)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
            }
            .padding(.horizontal, 60)
            .padding(.top, 60)
            
            Spacer()
            
            // Bottom controls
            VStack(spacing: 30) {
                // Progress bar (simplified for demo)
                HStack {
                    Text("00:00")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.8))
                    
                    Rectangle()
                        .fill(Color.white.opacity(0.3))
                        .frame(height: 4)
                        .overlay(
                            Rectangle()
                                .fill(Color.blue)
                                .frame(width: 100) // Demo progress
                                .frame(maxWidth: .infinity, alignment: .leading)
                        )
                    
                    Text("10:30")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.8))
                }
                .padding(.horizontal, 60)
                
                // Control buttons
                HStack(spacing: 40) {
                    // Seek backward
                    Button(action: {
                        let currentTime = player.currentTime()
                        let newTime = CMTimeSubtract(currentTime, CMTime(seconds: 10, preferredTimescale: 1))
                        player.seek(to: max(newTime, .zero))
                    }) {
                        Image(systemName: "gobackward.10")
                            .font(.system(size: 32))
                            .foregroundColor(.white)
                    }
                    .buttonStyle(PlainButtonStyle())
                    
                    // Play/Pause button
                    Button(action: {
                        if isPlaying {
                            player.pause()
                        } else {
                            player.play()
                        }
                    }) {
                        Image(systemName: isPlaying ? "pause.circle.fill" : "play.circle.fill")
                            .font(.system(size: 64))
                            .foregroundColor(.white)
                    }
                    .buttonStyle(PlainButtonStyle())
                    
                    // Seek forward
                    Button(action: {
                        let currentTime = player.currentTime()
                        let newTime = CMTimeAdd(currentTime, CMTime(seconds: 10, preferredTimescale: 1))
                        player.seek(to: newTime)
                    }) {
                        Image(systemName: "goforward.10")
                            .font(.system(size: 32))
                            .foregroundColor(.white)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding(.bottom, 80)
            .background(
                LinearGradient(
                    colors: [Color.clear, Color.black.opacity(0.8)],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .frame(height: 200)
            )
        }
    }
}

#Preview {
    VideoPlayerView(
        item: ContentItem(
            title: "Sample Video",
            contentType: .video,
            description: "A sample video for testing",
            isOfflineAvailable: true
        ),
        onDismiss: {}
    )
}