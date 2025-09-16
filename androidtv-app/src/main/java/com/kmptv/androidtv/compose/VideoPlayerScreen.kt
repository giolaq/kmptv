package com.kmptv.androidtv.compose

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.tv.material3.*
import com.kmptv.shared_core.models.ContentItem

@OptIn(ExperimentalTvMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    item: ContentItem,
    videoUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", // Demo video
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var backButtonFocused by remember { mutableStateOf(false) }
    
    // ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                // Create media source
                val dataSourceFactory = DefaultDataSource.Factory(context)
                val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))
                
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true // Auto-play when ready

                // Add listener for playback events
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isPlaying = playbackState == Player.STATE_READY && playWhenReady
                    }
                    
                    override fun onPlayerError(error: PlaybackException) {
                        // Handle player errors
                    }
                })
            }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video Player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // We'll create custom controls
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Custom TV-optimized controls overlay
        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top controls - Back button and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            exoPlayer.pause()
                            onBack()
                        },
                        modifier = Modifier
                            .scale(if (backButtonFocused) 1.1f else 1.0f)
                            .onFocusChanged { focusState ->
                                backButtonFocused = focusState.isFocused
                            }
                            .background(
                                color = if (backButtonFocused) 
                                    Color.White.copy(alpha = 0.2f)
                                else 
                                    Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        tonalElevation = if (backButtonFocused) 8.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "←",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                        }
                    }
                    
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Bottom controls - Play/Pause and progress
                VideoPlayerControls(
                    exoPlayer = exoPlayer,
                    isPlaying = isPlaying,
                    onPlayPause = {
                        if (isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    }
                )
            }
        }
    }
    
    // Auto-hide controls after 5 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            kotlinx.coroutines.delay(5000)
            showControls = false
        }
    }
    
    // Show controls when user interacts
    LaunchedEffect(Unit) {
        showControls = true
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun VideoPlayerControls(
    exoPlayer: ExoPlayer,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    var playButtonFocused by remember { mutableStateOf(false) }
    var seekBackFocused by remember { mutableStateOf(false) }
    var seekForwardFocused by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress bar (simplified for demo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp)
                )
        ) {
            // Progress indicator (placeholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f) // Demo: 30% progress
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
        
        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seek backward button
            Surface(
                onClick = { 
                    exoPlayer.seekTo(maxOf(0, exoPlayer.currentPosition - 10000)) // 10 seconds back
                },
                modifier = Modifier
                    .scale(if (seekBackFocused) 1.1f else 1.0f)
                    .onFocusChanged { focusState ->
                        seekBackFocused = focusState.isFocused
                    },
                tonalElevation = if (seekBackFocused) 8.dp else 0.dp
            ) {
                Text(
                    text = "⏪",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Play/Pause button
            Surface(
                onClick = onPlayPause,
                modifier = Modifier
                    .scale(if (playButtonFocused) 1.1f else 1.0f)
                    .onFocusChanged { focusState ->
                        playButtonFocused = focusState.isFocused
                    },
                tonalElevation = if (playButtonFocused) 8.dp else 0.dp
            ) {
                Text(
                    text = if (isPlaying) "⏸️" else "▶️",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.padding(20.dp),
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Seek forward button
            Surface(
                onClick = {
                    exoPlayer.seekTo(exoPlayer.currentPosition + 10000) // 10 seconds forward
                },
                modifier = Modifier
                    .scale(if (seekForwardFocused) 1.1f else 1.0f)
                    .onFocusChanged { focusState ->
                        seekForwardFocused = focusState.isFocused
                    },
                tonalElevation = if (seekForwardFocused) 8.dp else 0.dp
            ) {
                Text(
                    text = "⏩",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White
                )
            }
        }
    }
}