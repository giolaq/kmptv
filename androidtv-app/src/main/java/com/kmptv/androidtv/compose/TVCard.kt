package com.kmptv.androidtv.compose

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.kmptv.shared_core.models.ContentItem
import com.kmptv.shared_core.models.ContentType

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TVCard(
    item: ContentItem,
    onItemClick: (ContentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Surface(
        onClick = { onItemClick(item) },
        modifier = modifier
            .size(width = 200.dp, height = 140.dp)
            .scale(if (isFocused) 1.1f else 1.0f)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
        tonalElevation = if (isFocused) 8.dp else 4.dp
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (isFocused) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Content type indicator
                Box(
                    modifier = Modifier
                        .background(
                            color = getContentTypeColor(item.contentType),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.contentType.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Placeholder for thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📺",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Content info
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isFocused) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    item.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isFocused) 
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Offline indicator
                if (item.isOfflineAvailable) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📱",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
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