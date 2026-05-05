@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.kmptv.androidtv.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kmptv.androidtv.theme.KmptvColors
import com.kmptv.shared_core.models.ContentItem

@Composable
fun ContentDetailScreen(
    item: ContentItem,
    onBack: () -> Unit,
    onPlay: (ContentItem) -> Unit = {},
    onAddToWatchlist: (ContentItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KmptvColors.Background),
    ) {
        AsyncImage(
            model = item.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colors = listOf(
                        KmptvColors.Background,
                        KmptvColors.Background.copy(alpha = 0.6f),
                        Color.Transparent,
                    ),
                    endX = 1200f,
                ),
            ),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, KmptvColors.Background.copy(alpha = 0.8f)),
                    startY = 300f,
                ),
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 550.dp)
                .padding(start = 48.dp, top = 40.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            DetailActionButton(label = "← Back", isPrimary = false, onClick = onBack)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item.metadata.releaseDate?.let { MetadataChip(it) }
                    item.metadata.duration?.let { ms ->
                        val mins = ms / 60_000
                        val h = mins / 60
                        val m = mins % 60
                        MetadataChip(if (h > 0) "${h}h ${m}m" else "${m}m")
                    }
                    item.metadata.rating?.takeIf { it.isNotEmpty() }?.let { MetadataChip(it) }
                    item.metadata.genre?.let { MetadataChip(it) }
                }

                if (item.tags.isNotEmpty()) {
                    val shownTags = item.tags.take(4)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        shownTags.forEachIndexed { index, tag ->
                            Text(
                                text = tag.replaceFirstChar { it.uppercase() },
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f),
                            )
                            if (index != shownTags.lastIndex) {
                                Text("·", fontSize = 13.sp, color = Color.White.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                item.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailActionButton(label = "▶  Play", isPrimary = true, onClick = { onPlay(item) })
                DetailActionButton(label = "+  Watchlist", isPrimary = false, onClick = { onAddToWatchlist(item) })
            }
        }
    }
}

@Composable
private fun DetailActionButton(
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
) {
    // This button needs both (a) scale-on-focus animation and (b) fill-color
    // reaction to focus. We keep a single local `focused` flag to drive both;
    // using `tvFocusScale` here would create two sources of truth for focus.
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.05f else 1f,
        animationSpec = tween(150),
        label = "detailBtnScale",
    )
    val bg = when {
        isPrimary && focused -> Color.White
        focused -> Color.White.copy(alpha = 0.35f)
        else -> KmptvColors.SurfaceFocus
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
        colors = transparentSurfaceColors(),
        shape = ClickableSurfaceDefaults.shape(shape = shape),
    ) {
        Box(
            modifier = Modifier
                .background(bg, shape)
                .padding(horizontal = 28.dp, vertical = 12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
        }
    }
}

@Composable
private fun MetadataChip(text: String) {
    Box(
        modifier = Modifier
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
        )
    }
}
