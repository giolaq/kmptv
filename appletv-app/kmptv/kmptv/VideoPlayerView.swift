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
    let videoURL: URL
    let onDismiss: () -> Void

    @State private var player: AVPlayer?
    @State private var isPlaying = false
    @State private var showingControls = true
    @State private var currentSeconds: Double = 0
    @State private var durationSeconds: Double = 0

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            if let player = player {
                VideoPlayer(player: player) {
                    if showingControls {
                        VideoPlayerControlsOverlay(
                            player: player,
                            item: item,
                            isPlaying: $isPlaying,
                            currentSeconds: currentSeconds,
                            durationSeconds: durationSeconds,
                            onDismiss: onDismiss
                        )
                    }
                }
            } else {
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
        .onAppear { setupPlayer() }
        .onDisappear { cleanupPlayer() }
        .onReceive(NotificationCenter.default.publisher(for: .AVPlayerItemDidPlayToEndTime)) { _ in
            player?.seek(to: .zero)
            isPlaying = false
        }
        .onExitCommand {
            player?.pause()
            onDismiss()
        }
    }

    private func setupPlayer() {
        let playerItem = AVPlayerItem(url: videoURL)
        let newPlayer = AVPlayer(playerItem: playerItem)

        newPlayer.allowsExternalPlayback = true
        newPlayer.usesExternalPlaybackWhileExternalScreenIsActive = true

        self.player = newPlayer
        newPlayer.play()

        // Poll playback state + position every 0.5s.
        let interval = CMTime(seconds: 0.5, preferredTimescale: CMTimeScale(NSEC_PER_SEC))
        newPlayer.addPeriodicTimeObserver(forInterval: interval, queue: .main) { time in
            isPlaying = newPlayer.timeControlStatus == .playing
            currentSeconds = CMTimeGetSeconds(time)
            if let d = newPlayer.currentItem?.duration, CMTIME_IS_NUMERIC(d) {
                durationSeconds = CMTimeGetSeconds(d)
            }
        }

        // Auto-hide controls after 5s.
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

private struct VideoPlayerControlsOverlay: View {
    let player: AVPlayer
    let item: ContentItem
    @Binding var isPlaying: Bool
    let currentSeconds: Double
    let durationSeconds: Double
    let onDismiss: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Top controls
            HStack {
                Button(action: {
                    player.pause()
                    onDismiss()
                }) {
                    HStack(spacing: 12) {
                        Image(systemName: "chevron.left").font(.title2)
                        Text("Back").font(.title2)
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                    .background(
                        RoundedRectangle(cornerRadius: 10)
                            .fill(Color(white: 0.15))
                    )
                }
                .buttonStyle(TVFocusButtonStyle())

                Spacer()

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

            VStack(spacing: 30) {
                // Progress bar wired to real player state.
                let progress: Double = durationSeconds > 0
                    ? min(max(currentSeconds / durationSeconds, 0), 1)
                    : 0
                HStack {
                    Text(formatTime(currentSeconds))
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.8))

                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            Rectangle()
                                .fill(Color.white.opacity(0.3))
                                .frame(height: 4)
                            Rectangle()
                                .fill(Color.blue)
                                .frame(width: geo.size.width * CGFloat(progress), height: 4)
                        }
                    }
                    .frame(height: 4)

                    Text(formatTime(durationSeconds))
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.8))
                }
                .padding(.horizontal, 60)

                // Control buttons
                HStack(spacing: 40) {
                    Button(action: {
                        let newTime = CMTimeSubtract(
                            player.currentTime(),
                            CMTime(seconds: 10, preferredTimescale: 1)
                        )
                        player.seek(to: max(newTime, .zero))
                    }) {
                        Image(systemName: "gobackward.10")
                            .font(.system(size: 32))
                            .foregroundColor(.white)
                            .padding(12)
                            .background(RoundedRectangle(cornerRadius: 10).fill(Color(white: 0.15)))
                    }
                    .buttonStyle(TVFocusButtonStyle())

                    Button(action: {
                        if isPlaying { player.pause() } else { player.play() }
                    }) {
                        Image(systemName: isPlaying ? "pause.circle.fill" : "play.circle.fill")
                            .font(.system(size: 64))
                            .foregroundColor(.white)
                            .padding(12)
                            .background(Circle().fill(Color(white: 0.15)))
                    }
                    .buttonStyle(TVFocusButtonStyle(cornerRadius: 40))

                    Button(action: {
                        let newTime = CMTimeAdd(
                            player.currentTime(),
                            CMTime(seconds: 10, preferredTimescale: 1)
                        )
                        player.seek(to: newTime)
                    }) {
                        Image(systemName: "goforward.10")
                            .font(.system(size: 32))
                            .foregroundColor(.white)
                            .padding(12)
                            .background(RoundedRectangle(cornerRadius: 10).fill(Color(white: 0.15)))
                    }
                    .buttonStyle(TVFocusButtonStyle())
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

    private func formatTime(_ seconds: Double) -> String {
        guard seconds.isFinite, seconds > 0 else { return "0:00" }
        let totalSec = Int(seconds)
        let h = totalSec / 3600
        let m = (totalSec % 3600) / 60
        let s = totalSec % 60
        if h > 0 {
            return String(format: "%d:%02d:%02d", h, m, s)
        } else {
            return String(format: "%d:%02d", m, s)
        }
    }
}

#Preview {
    VideoPlayerView(
        item: ContentItem(
            id: "preview",
            title: "Sample Video",
            description: "A sample video for testing",
            thumbnailUrl: nil,
            videoUrl: URL(string: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            genre: "Animation",
            releaseYear: 2008,
            contentRating: "TV-G",
            durationMs: 596_000,
            tags: ["animation"],
            priority: 0
        ),
        videoURL: URL(string: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")!,
        onDismiss: {}
    )
}
