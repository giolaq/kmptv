package com.kmptv.androidtv.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.kmptv.shared_core.models.ContentItem
import com.kmptv.shared_core.models.ContentType

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContentDetailScreen(
    item: ContentItem,
    onBack: () -> Unit,
    onPlay: (ContentItem) -> Unit = {},
    onAddToWatchlist: (ContentItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var backButtonFocused by remember { mutableStateOf(false) }
    var playButtonFocused by remember { mutableStateOf(false) }
    var watchlistButtonFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 48.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with back navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    modifier = Modifier
                        .scale(if (backButtonFocused) 1.1f else 1.0f)
                        .onFocusChanged { focusState ->
                            backButtonFocused = focusState.isFocused
                        }
                        .background(
                            color = if (backButtonFocused) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                Color.Transparent,
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
                    text = "KMPTV",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Main content area - using Column for better vertical space management
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Hero image/thumbnail - responsive sizing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Fixed height for consistent layout
                        .background(
                            color = Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📺",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "Play",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                    }
                }
                
                // Content details
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Title and content type
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = getContentTypeColor(item.contentType),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = item.contentType.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            
                            if (item.isOfflineAvailable) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⬇️",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Available Offline",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Green
                                    )
                                }
                            }
                        }
                    }
                    
                    // Description
                    item.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = MaterialTheme.typography.headlineSmall.lineHeight
                        )
                    }
                    
                    // Additional details
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRow(title = "Duration", value = "2h 15m")
                        DetailRow(title = "Quality", value = "4K HDR")
                        DetailRow(title = "Audio", value = "Dolby Atmos")
                        DetailRow(title = "Released", value = "2024")
                    }
                    
                    // Action buttons with proper spacing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Play button - responsive sizing
                        Surface(
                            onClick = { onPlay(item) },
                            modifier = Modifier
                                .weight(1f, false) // Allow button to shrink if needed
                                .scale(if (playButtonFocused) 1.05f else 1.0f) // Smaller scale for TV
                                .onFocusChanged { focusState ->
                                    playButtonFocused = focusState.isFocused
                                }
                                .background(
                                    color = if (playButtonFocused) 
                                        Color.White.copy(alpha = 0.9f)
                                    else 
                                        Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            tonalElevation = if (playButtonFocused) 8.dp else 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 14.dp)
                                    .widthIn(min = 120.dp), // Minimum width for TV
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "▶️",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Play",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }
                        }
                        
                        // Watchlist button - responsive sizing
                        Surface(
                            onClick = { onAddToWatchlist(item) },
                            modifier = Modifier
                                .weight(1f, false) // Allow button to shrink if needed
                                .scale(if (watchlistButtonFocused) 1.05f else 1.0f) // Smaller scale for TV
                                .onFocusChanged { focusState ->
                                    watchlistButtonFocused = focusState.isFocused
                                }
                                .background(
                                    color = if (watchlistButtonFocused) 
                                        Color.Gray.copy(alpha = 0.6f)
                                    else 
                                        Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            tonalElevation = if (watchlistButtonFocused) 8.dp else 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 14.dp)
                                    .widthIn(min = 140.dp), // Minimum width for TV
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "➕",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Watchlist",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Add bottom spacer to ensure buttons are always visible
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DetailRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

private fun getContentTypeColor(contentType: ContentType): Color {
    return when (contentType) {
        ContentType.Video -> Color(0xFF1976D2)    // Blue
        ContentType.Audio -> Color(0xFF388E3C)    // Green
        ContentType.Image -> Color(0xFFD32F2F)    // Red
        ContentType.Mixed -> Color(0xFF7B1FA2)    // Purple
    }
}