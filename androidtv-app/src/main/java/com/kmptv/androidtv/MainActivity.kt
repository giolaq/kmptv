package com.kmptv.androidtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.items
import com.kmptv.androidtv.compose.TVCard
import com.kmptv.androidtv.compose.ContentDetailScreen
import com.kmptv.androidtv.compose.VideoPlayerScreen
import com.kmptv.androidtv.theme.KMPTVTheme
import com.kmptv.shared_core.models.*
import com.kmptv.shared_core.repositories.ContentRepositoryImpl
import com.kmptv.shared_core.services.TVApplicationManagerImpl
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val contentRepository = ContentRepositoryImpl()
    private val applicationManager = TVApplicationManagerImpl()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            KMPTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
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
        
        val scope = rememberCoroutineScope()
        
        LaunchedEffect(Unit) {
            scope.launch {
                // Initialize application
                try {
                    val deviceInfo = DeviceInfo(
                        deviceId = "androidtv_device_001",
                        model = "Android TV",
                        osVersion = android.os.Build.VERSION.RELEASE,
                        availableMemory = Runtime.getRuntime().maxMemory(),
                        storageSpace = filesDir.totalSpace,
                        networkType = NetworkType.WiFi,
                        screenWidth = resources.displayMetrics.widthPixels,
                        screenHeight = resources.displayMetrics.heightPixels,
                        densityDpi = resources.displayMetrics.densityDpi
                    )
                    
                    val resolution = Resolution(
                        width = resources.displayMetrics.widthPixels,
                        height = resources.displayMetrics.heightPixels,
                        aspectRatio = "16:9",
                        refreshRate = 60
                    )
                    
                    val configuration = PlatformConfiguration(
                        platform = Platform.AndroidTV,
                        inputMethods = listOf(InputMethod.RemoteControl, InputMethod.GameController),
                        screenResolution = resolution,
                        supportedFormats = listOf(
                            MediaFormat("mp4", "h264", "aac", null),
                            MediaFormat("mkv", "hevc", "ac3", null)
                        ),
                        navigationStyle = NavigationStyle.DirectionalPad
                    )
                    
                    applicationManager.initialize(Platform.AndroidTV, configuration)
                    
                    // Load content
                    val result = contentRepository.getContentItems()
                    result.onSuccess { items ->
                        contentItems = items
                        isLoading = false
                    }.onFailure { exception ->
                        errorMessage = exception.message
                        isLoading = false
                    }
                } catch (e: Exception) {
                    errorMessage = e.message
                    isLoading = false
                }
            }
        }
        
        when {
            showingVideoPlayer && selectedItem != null -> {
                // Show video player screen
                VideoPlayerScreen(
                    item = selectedItem!!,
                    videoUrl = selectedItem!!.getVideoUrl() ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    onBack = {
                        showingVideoPlayer = false
                        showingDetail = false
                        selectedItem = null
                    }
                )
            }
            
            showingDetail && selectedItem != null -> {
                // Show detail screen
                ContentDetailScreen(
                    item = selectedItem!!,
                    onBack = {
                        showingDetail = false
                        selectedItem = null
                    },
                    onPlay = { item ->
                        // Navigate to video player
                        showingVideoPlayer = true
                    },
                    onAddToWatchlist = { item ->
                        scope.launch {
                            // Handle watchlist action
                            println("Add to watchlist: ${item.title}")
                        }
                    }
                )
            }
            
            else -> {
            // Show main screen
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header
            Text(
                text = "KMPTV",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading content...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: $errorMessage",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                else -> {
                    // Content Grid
                    TvLazyVerticalGrid(
                        columns = TvGridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(contentItems) { item ->
                            TVCard(
                                item = item,
                                onItemClick = { clickedItem ->
                                    scope.launch {
                                        contentRepository.markContentAccessed(clickedItem.id)
                                    }
                                    // Navigate to detail screen
                                    selectedItem = clickedItem
                                    showingDetail = true
                                }
                            )
                        }
                    }
                }
            }
            }
        }
    }
    }
}