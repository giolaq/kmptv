# Android TV Platform API Contract

## Platform-Specific Interface Contract

The Android TV application provides TV-optimized UI and platform-specific functionality using Compose for TV and Android TV APIs.

### TV-Specific UI Components

#### Focus Management
```kotlin
interface AndroidTVFocusManager {
    fun requestFocus(componentId: String): Boolean
    fun getCurrentFocusedComponent(): String?
    fun setFocusSearchStrategy(strategy: FocusSearchStrategy): Unit
    fun onDirectionalInput(direction: Direction): FocusResult
    fun setFocusHighlight(highlight: FocusHighlight): Unit
}

data class FocusResult(
    val handled: Boolean,
    val newFocusedComponent: String?,
    val boundaryReached: Boolean
)

enum class Direction {
    Up, Down, Left, Right, Center
}
```

#### Remote Control Input
```kotlin
interface RemoteControlHandler {
    fun onDPadInput(direction: Direction): Boolean
    fun onBackPressed(): Boolean
    fun onMenuPressed(): Boolean
    fun onPlayPausePressed(): Boolean
    fun onVolumeChanged(level: Int): Unit
    fun onChannelChanged(delta: Int): Unit
}
```

#### TV Launcher Integration
```kotlin
interface AndroidTVLauncher {
    fun addToHomeScreen(contentItem: ContentItem): Result<Unit>
    fun updateRecommendations(items: List<ContentItem>): Result<Unit>
    fun setWatchNextPrograms(programs: List<WatchNextProgram>): Result<Unit>
    fun clearRecommendations(): Result<Unit>
}
```

### Compose for TV Specific Components

#### Leanback-Style Navigation
```kotlin
@Composable
fun TVLazyGrid(
    items: List<ContentItem>,
    onItemSelected: (ContentItem) -> Unit,
    onItemFocused: (ContentItem) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
fun TVCard(
    item: ContentItem,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

@Composable
fun TVCarousel(
    items: List<ContentItem>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
)
```

#### TV-Optimized Components
```kotlin
@Composable
fun TVButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
)

@Composable
fun TVProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary
)

@Composable
fun TVDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable () -> Unit,
    actions: @Composable () -> Unit
)
```

### Android TV Platform Integration

#### Media Session Integration
```kotlin
interface AndroidTVMediaSession {
    fun createMediaSession(): MediaSession
    fun updatePlaybackState(state: PlaybackState): Unit
    fun updateMetadata(metadata: MediaMetadata): Unit
    fun setMediaSessionCallback(callback: MediaSession.Callback): Unit
}
```

#### Picture-in-Picture Support
```kotlin
interface AndroidTVPiPManager {
    fun enterPictureInPictureMode(params: PictureInPictureParams): Boolean
    fun updatePictureInPictureParams(params: PictureInPictureParams): Unit
    fun isPictureInPictureSupported(): Boolean
}
```

#### TV Input Framework Integration
```kotlin
interface AndroidTVInput {
    fun registerTVInputService(): Unit
    fun handleTuningRequest(channelId: String): Result<Unit>
    fun updateChannelPrograms(programs: List<Program>): Result<Unit>
}
```

### Screen Adaptation

#### TV Screen Handling
```kotlin
interface AndroidTVDisplay {
    fun getScreenDimensions(): ScreenDimensions
    fun getOverscanInfo(): OverscanInfo
    fun getSafeArea(): SafeArea
    fun adjustForOverscan(content: @Composable () -> Unit): @Composable () -> Unit
}

data class ScreenDimensions(
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Int,
    val aspectRatio: String
)

data class OverscanInfo(
    val leftInset: Int,
    val rightInset: Int,
    val topInset: Int,
    val bottomInset: Int
)
```

## Android TV Manifest Requirements

### Required Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="com.android.providers.tv.permission.READ_EPG_DATA" />
<uses-permission android:name="com.android.providers.tv.permission.WRITE_EPG_DATA" />

<!-- TV-specific features -->
<uses-feature 
    android:name="android.software.leanback"
    android:required="true" />
<uses-feature 
    android:name="android.hardware.touchscreen"
    android:required="false" />
```

### TV Intent Filters
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:theme="@style/Theme.KMPTV.TV">
    
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
        <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
    </intent-filter>
    
    <!-- Voice search support -->
    <intent-filter>
        <action android:name="android.media.action.TEXT_OPEN_FROM_SEARCH" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

## Testing Contract for Android TV

### UI Testing Requirements
```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class AndroidTVUITest {
    
    @Test
    fun testRemoteControlNavigation() {
        // Verify D-pad navigation works correctly
        // Test focus movement in all directions
        // Verify boundary handling
    }
    
    @Test
    fun testTVCardFocusVisibility() {
        // Verify focus indicators are visible from 10 feet
        // Test focus highlight animations
        // Verify accessibility compliance
    }
    
    @Test
    fun testLeanbackGridPerformance() {
        // Verify smooth scrolling with 60fps
        // Test memory usage with large content lists
        // Verify image loading performance
    }
}
```

### Platform Integration Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class AndroidTVIntegrationTest {
    
    @Test
    fun testMediaSessionIntegration() {
        // Verify media session creation and updates
        // Test playback state changes
        // Verify metadata updates reflect in system UI
    }
    
    @Test
    fun testTVLauncherIntegration() {
        // Verify recommendations appear in launcher
        // Test watch next program updates
        // Verify deep linking from recommendations
    }
    
    @Test
    fun testDisplayAdaptation() {
        // Test overscan handling on different TV models
        // Verify safe area compliance
        // Test screen density adaptation
    }
}
```

### Performance Testing
```kotlin
class AndroidTVPerformanceTest {
    
    @Test
    fun testUIRenderingPerformance() {
        // Measure frame rates during navigation
        // Verify 60fps maintenance during animations
        // Test memory usage patterns
    }
    
    @Test
    fun testContentLoadingPerformance() {
        // Measure content list loading times
        // Verify thumbnail loading performance
        // Test offline content access speed
    }
}
```

## Error Handling for Android TV

### TV-Specific Error Codes
- `ANDROIDTV_001`: Leanback feature not available
- `ANDROIDTV_002`: Media session creation failed
- `ANDROIDTV_003`: TV input service registration failed
- `ANDROIDTV_004`: Picture-in-Picture not supported
- `ANDROIDTV_005`: Overscan information unavailable
- `ANDROIDTV_006`: Remote control input handling failed

### Error Recovery Strategies
```kotlin
interface AndroidTVErrorRecovery {
    suspend fun recoverFromLeanbackUnavailable(): RecoveryResult
    suspend fun recoverFromMediaSessionFailure(): RecoveryResult
    suspend fun recoverFromDisplayIssues(): RecoveryResult
}

data class RecoveryResult(
    val success: Boolean,
    val fallbackMode: String?,
    val userMessage: String?
)
```

## Accessibility Contract

### Android TV Accessibility Requirements
```kotlin
interface AndroidTVAccessibility {
    fun setContentDescriptions(descriptions: Map<String, String>): Unit
    fun announceForAccessibility(message: String): Unit
    fun setFocusedElementAccessibilityInfo(info: AccessibilityInfo): Unit
    fun supportsTalkBack(): Boolean
}

data class AccessibilityInfo(
    val contentDescription: String,
    val role: AccessibilityRole,
    val state: AccessibilityState
)
```

### TalkBack Integration
- All focusable elements must have meaningful content descriptions
- Navigation announcements for screen readers
- Audio feedback for remote control actions
- Support for accessibility services and external input devices