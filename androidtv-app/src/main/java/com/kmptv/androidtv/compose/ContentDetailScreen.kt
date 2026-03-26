@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kmptv.androidtv.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.kmptv.shared_core.models.ContentItem

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContentDetailScreen(
    item: ContentItem,
    onBack: () -> Unit,
    onPlay: (ContentItem) -> Unit = {},
    onAddToWatchlist: (ContentItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // Full-bleed backdrop image
        AsyncImage(
            model = item.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlays for readability
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0D0D0D), Color(0xFF0D0D0D).copy(alpha = 0.6f), Color.Transparent),
                    endX = 1200f
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF0D0D0D).copy(alpha = 0.8f)),
                    startY = 300f
                )
            )
        )

        // Content overlay — left-aligned like streaming services
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 550.dp)
                .padding(start = 48.dp, top = 40.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            DetailActionButton(
                label = "← Back",
                isPrimary = false,
                onClick = onBack
            )

            // Middle: title + metadata
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Metadata chips row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item.metadata.releaseDate?.let { MetadataChip(it) }
                    item.metadata.duration?.let { ms ->
                        val mins = ms / 60000
                        val h = mins / 60
                        val m = mins % 60
                        val text = if (h > 0) "${h}h ${m}m" else "${m}m"
                        MetadataChip(text)
                    }
                    item.metadata.rating?.takeIf { it.isNotEmpty() }?.let { MetadataChip(it) }
                    item.metadata.genre?.let { MetadataChip(it) }
                }

                // Tags row
                if (item.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.tags.take(4).forEach { tag ->
                            Text(
                                text = tag.replaceFirstChar { it.uppercase() },
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            if (tag != item.tags.take(4).last()) {
                                Text("·", fontSize = 13.sp, color = Color.White.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Description
                item.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
                    )
                }
            }

            // Bottom: action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailActionButton(
                    label = "▶  Play",
                    isPrimary = true,
                    onClick = { onPlay(item) }
                )
                DetailActionButton(
                    label = "+  Watchlist",
                    isPrimary = false,
                    onClick = { onAddToWatchlist(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DetailActionButton(
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.05f else 1.0f,
        animationSpec = tween(150),
        label = "btnScale"
    )
    val bg = when {
        isPrimary && focused -> Color.White
        focused -> Color.White.copy(alpha = 0.35f)
        else -> Color(0xFF2A2A2A)
    }
    val fg = if (isPrimary && focused) Color.Black else Color.White
    val shape = RoundedCornerShape(8.dp)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .then(if (focused) Modifier.border(2.dp, Color.White, shape) else Modifier),
        tonalElevation = 0.dp,
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent, pressedContainerColor = Color.Transparent),
        shape = ClickableSurfaceDefaults.shape(shape = shape)
    ) {
        Box(
            modifier = Modifier
                .background(bg, shape)
                .padding(horizontal = 28.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
        }
    }
}

@Composable
private fun MetadataChip(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium
        )
    }
}
