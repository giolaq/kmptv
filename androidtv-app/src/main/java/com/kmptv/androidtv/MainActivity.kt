package com.kmptv.androidtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import com.kmptv.androidtv.compose.ContentDetailScreen
import com.kmptv.androidtv.compose.TVCard
import com.kmptv.androidtv.compose.VideoPlayerScreen
import com.kmptv.androidtv.theme.KMPTVTheme
import com.kmptv.androidtv.theme.KmptvColors
import com.kmptv.shared_core.models.ContentItem
import com.kmptv.shared_core.repositories.ContentRepositoryImpl
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val contentRepository = ContentRepositoryImpl()

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KMPTVTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().background(KmptvColors.Background)) {
                        MainScreen()
                    }
                }
            }
        }
    }

    @Composable
    private fun MainScreen() {
        var contentItems by remember { mutableStateOf<List<ContentItem>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var selectedItem by remember { mutableStateOf<ContentItem?>(null) }
        var showingDetail by remember { mutableStateOf(false) }
        var showingVideoPlayer by remember { mutableStateOf(false) }
        var focusedItem by remember { mutableStateOf<ContentItem?>(null) }

        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            try {
                contentRepository.getContentItems()
                    .onSuccess { items ->
                        contentItems = items
                        focusedItem = items.firstOrNull()
                        isLoading = false
                    }
                    .onFailure { exception ->
                        errorMessage = exception.message
                        isLoading = false
                    }
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }

        when {
            showingVideoPlayer && selectedItem?.videoUrl != null -> {
                VideoPlayerScreen(
                    item = selectedItem!!,
                    videoUrl = selectedItem!!.videoUrl!!,
                    onBack = {
                        showingVideoPlayer = false
                        showingDetail = false
                        selectedItem = null
                    },
                )
            }

            showingDetail && selectedItem != null -> {
                ContentDetailScreen(
                    item = selectedItem!!,
                    onBack = {
                        showingDetail = false
                        selectedItem = null
                    },
                    onPlay = {
                        // Only enter the player when the item actually has a playable URL.
                        // Items without one (audio-only, images, mixed) just stay on the
                        // detail screen — we don't silently swap in a fake video.
                        if (selectedItem?.videoUrl != null) showingVideoPlayer = true
                    },
                    onAddToWatchlist = {},
                )
            }

            else -> when {
                isLoading -> LoadingScreen()
                errorMessage != null -> ErrorScreen(errorMessage!!)
                else -> HomeScreen(
                    items = contentItems,
                    focusedItem = focusedItem,
                    onFocusChanged = { focusedItem = it },
                    onItemClick = { item ->
                        scope.launch { contentRepository.markContentAccessed(item.id) }
                        selectedItem = item
                        showingDetail = true
                    },
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
            Text(
                "Loading movies…",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ErrorScreen(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun HomeScreen(
    items: List<ContentItem>,
    focusedItem: ContentItem?,
    onFocusChanged: (ContentItem) -> Unit,
    onItemClick: (ContentItem) -> Unit,
) {
    var focusedRowIndex by remember { mutableIntStateOf(-1) }
    val categoryList = remember(items) {
        items.groupBy { it.metadata.genre ?: "Other" }
            .filter { it.value.isNotEmpty() }
            .toSortedMap()
            .toList()
    }

    Column(Modifier.fillMaxSize()) {
        HeroBanner(focusedItem)

        TvLazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            categoryList.forEachIndexed { index, (genre, genreItems) ->
                item {
                    val alpha by animateFloatAsState(
                        targetValue = if (focusedRowIndex >= 0 && index == focusedRowIndex - 1) 0f else 1f,
                        animationSpec = tween(300),
                        label = "rowAlpha",
                    )
                    ContentRow(
                        title = genre,
                        items = genreItems,
                        alpha = alpha,
                        onRowFocused = { focusedRowIndex = index },
                        onFocusChanged = onFocusChanged,
                        onItemClick = onItemClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBanner(item: ContentItem?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(KmptvColors.Background),
    ) {
        Crossfade(
            targetState = item?.thumbnailUrl,
            animationSpec = tween(600),
            label = "heroCrossfade",
        ) { url ->
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(Modifier.fillMaxSize().background(KmptvColors.SurfaceElevated))
            }
        }

        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, KmptvColors.Background),
                    startY = 100f,
                ),
            ),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colors = listOf(KmptvColors.Background.copy(alpha = 0.7f), Color.Transparent),
                    endX = 600f,
                ),
            ),
        )

        item?.let {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 48.dp, bottom = 24.dp)
                    .widthIn(max = 500.dp),
            ) {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                it.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    it.metadata.genre?.let { g -> InfoChip(g) }
                    it.metadata.releaseDate?.let { y -> InfoChip(y) }
                    it.metadata.rating?.takeIf { r -> r.isNotEmpty() }?.let { r -> InfoChip(r) }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text = text, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
private fun ContentRow(
    title: String,
    items: List<ContentItem>,
    alpha: Float = 1f,
    onRowFocused: () -> Unit = {},
    onFocusChanged: (ContentItem) -> Unit,
    onItemClick: (ContentItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(top = 12.dp)
            .graphicsLayer { this.alpha = alpha }
            .onFocusChanged { if (it.hasFocus) onRowFocused() },
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(start = 48.dp, bottom = 12.dp),
        )
        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items, key = { it.id }) { item ->
                TVCard(
                    item = item,
                    onItemClick = onItemClick,
                    onItemFocused = onFocusChanged,
                )
            }
        }
    }
}
