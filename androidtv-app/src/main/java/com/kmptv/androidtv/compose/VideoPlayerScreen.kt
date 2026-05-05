@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kmptv.androidtv.compose

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.tv.material3.*
import com.kmptv.androidtv.theme.KmptvColors
import com.kmptv.shared_core.models.ContentItem
import kotlinx.coroutines.delay

private const val THUMB_SIZE_DP = 14

@androidx.media3.common.util.UnstableApi
@Composable
fun VideoPlayerScreen(
    item: ContentItem,
    videoUrl: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var controlsInteraction by remember { mutableLongStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val source = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
                .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))
            setMediaSource(source)
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) duration = this@apply.duration
                    isPlaying = state == Player.STATE_READY && playWhenReady
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            if (duration <= 0L) duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    LaunchedEffect(controlsInteraction) {
        showControls = true
        delay(5000)
        if (isPlaying) showControls = false
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
        ) {
            Box(Modifier.fillMaxSize()) {
                // Top scrim + title
                Box(
                    Modifier.fillMaxWidth().height(120.dp).align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent),
                            ),
                        ),
                )
                Row(
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 40.dp, top = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PlayerButton(
                        label = "←",
                        onClick = { exoPlayer.pause(); onBack() },
                        onInteraction = { controlsInteraction++ },
                    )
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }

                // Bottom controls
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            ),
                        )
                        .padding(horizontal = 40.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SeekBar(currentPosition = currentPosition, duration = duration)

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${formatTime(currentPosition)} / ${formatTime(duration)}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PlayerButton(
                                label = "−10s",
                                onClick = { exoPlayer.seekTo(maxOf(0, exoPlayer.currentPosition - 10_000)) },
                                onInteraction = { controlsInteraction++ },
                            )
                            PlayerButton(
                                label = if (isPlaying) "⏸" else "▶",
                                isPrimary = true,
                                onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                                onInteraction = { controlsInteraction++ },
                            )
                            PlayerButton(
                                label = "+10s",
                                onClick = { exoPlayer.seekTo(exoPlayer.currentPosition + 10_000) },
                                onInteraction = { controlsInteraction++ },
                            )
                        }

                        item.metadata.genre?.let {
                            Text(it, fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f))
                        } ?: Spacer(Modifier.width(1.dp))
                    }
                }
            }
        }
    }
}

/**
 * Seek bar with a thumb that actually tracks playback position.
 *
 * The previous implementation used `.offset(x = ((progress * 100).dp * 0.01f * 10))`
 * which simplifies to `progress.dp` — at most 1 dp of travel across a full-width
 * bar, making the thumb effectively invisible. We now use `BoxWithConstraints`
 * to get the measured width and offset the thumb in real pixels.
 */
@Composable
private fun SeekBar(currentPosition: Long, duration: Long) {
    val progress = if (duration > 0) {
        (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
    } else {
        0f
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(THUMB_SIZE_DP.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val barWidth = maxWidth
        val thumbSize = THUMB_SIZE_DP.dp

        // Track
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.2f)),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(KmptvColors.Accent),
            )
        }

        // Thumb — centered on the progress point, clipped within the bar.
        val thumbX = (barWidth - thumbSize) * progress
        Box(
            Modifier
                .offset(x = thumbX)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
private fun PlayerButton(
    label: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
    onInteraction: () -> Unit = {},
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.1f else 1f,
        animationSpec = tween(150),
        label = "pbScale",
    )
    val shape = if (isPrimary) CircleShape else RoundedCornerShape(6.dp)
    val bg = when {
        isPrimary && focused -> Color.White
        focused -> Color.White.copy(alpha = 0.35f)
        else -> KmptvColors.SurfaceFocus
    }
    val fg = if (isPrimary && focused) Color.Black else Color.White

    Surface(
        onClick = { onInteraction(); onClick() },
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .then(
                if (focused && !isPrimary) {
                    Modifier.border(1.5.dp, Color.White.copy(alpha = 0.5f), shape)
                } else {
                    Modifier
                },
            ),
        tonalElevation = 0.dp,
        colors = transparentSurfaceColors(),
        shape = ClickableSurfaceDefaults.shape(shape = shape),
    ) {
        Box(
            modifier = Modifier
                .background(bg, shape)
                .padding(if (isPrimary) 16.dp else 8.dp)
                .then(if (!isPrimary) Modifier.padding(horizontal = 8.dp) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                fontSize = if (isPrimary) 22.sp else 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) {
        "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    } else {
        "${m}:${s.toString().padStart(2, '0')}"
    }
}
