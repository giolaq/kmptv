# Data Model: Kotlin Multiplatform TV App

## Core Entities

### TVApplication
**Purpose**: Main application container that manages platform-specific configurations and shared state.

**Fields**:
- `id: String` - Unique application identifier
- `platform: Platform` - Android TV or Apple TV
- `version: String` - Application version (MAJOR.MINOR.BUILD)
- `configuration: PlatformConfiguration` - Platform-specific settings
- `sessionManager: UserSession` - Current user session handler

**Relationships**: 
- Has one PlatformConfiguration
- Has one UserSession
- Manages multiple ContentItems

**Validation Rules**:
- Platform must be AndroidTV or AppleTV
- Version must follow semantic versioning format
- Configuration must match platform type

### ContentItem
**Purpose**: Represents any media content that can be displayed and navigated in the TV interface.

**Fields**:
- `id: String` - Unique content identifier
- `title: String` - Content display title (min 1, max 100 characters)
- `description: String?` - Optional content description (max 500 characters)
- `thumbnailUrl: String?` - Optional thumbnail image URL
- `contentType: ContentType` - Type of content (Video, Audio, Image, Mixed)
- `metadata: ContentMetadata` - Additional content information
- `isOfflineAvailable: Boolean` - Whether content can be viewed offline
- `lastAccessed: Long?` - Timestamp of last user interaction
- `focusable: Boolean` - Whether item can receive navigation focus

**Relationships**:
- Belongs to zero or more ContentCollections
- Has one ContentMetadata
- Can have multiple FocusStates

**Validation Rules**:
- Title must be non-empty and TV-readable (appropriate font size for 10-foot viewing)
- ThumbnailUrl must be valid URL format if provided
- Metadata must be appropriate for contentType
- lastAccessed must be valid timestamp if provided

**State Transitions**:
- New → Loaded → Focused → Selected → Playing/Viewing
- Any state → Error (on content load failure)
- Playing → Paused → Stopped

### UserSession
**Purpose**: Manages user session state and authentication context across platforms.

**Fields**:
- `sessionId: String` - Unique session identifier
- `userId: String?` - User identifier (null for guest sessions)
- `isAuthenticated: Boolean` - Authentication status
- `preferences: UserPreferences` - User-specific settings
- `lastActivity: Long` - Timestamp of last user activity
- `sessionTimeout: Long` - Session timeout in milliseconds
- `deviceInfo: DeviceInfo` - Information about the TV device

**Relationships**:
- Has one UserPreferences
- Has one DeviceInfo
- Associated with TVApplication

**Validation Rules**:
- SessionId must be globally unique
- UserId must be valid if authenticated
- LastActivity must be valid timestamp
- SessionTimeout must be positive value

### PlatformConfiguration
**Purpose**: Platform-specific settings and configuration options.

**Fields**:
- `platform: Platform` - Target platform (AndroidTV, AppleTV)
- `inputMethods: List<InputMethod>` - Supported input types
- `screenResolution: Resolution` - TV screen resolution info
- `supportedFormats: List<MediaFormat>` - Supported media formats
- `navigationStyle: NavigationStyle` - Platform navigation patterns
- `uiScaling: Float` - UI scaling factor for screen size
- `hardwareCapabilities: HardwareInfo` - TV hardware information

**Relationships**:
- Belongs to one TVApplication
- Has one HardwareInfo

**Validation Rules**:
- Platform must match application platform
- InputMethods must include at least one directional input
- ScreenResolution must be valid TV resolution
- UiScaling must be between 0.5 and 3.0
- NavigationStyle must be appropriate for platform

## Supporting Data Types

### Platform (Enum)
- `AndroidTV`
- `AppleTV` 
- `FireTV` (future extension)

### ContentType (Enum)
- `Video`
- `Audio`
- `Image`
- `Mixed`

### InputMethod (Enum)
- `RemoteControl` - Traditional TV remote
- `SiriRemote` - Apple TV Siri Remote
- `GameController` - Gaming controllers
- `Voice` - Voice input commands

### NavigationStyle (Enum)
- `DirectionalPad` - Up/Down/Left/Right navigation
- `TouchGestures` - Swipe and touch gestures
- `VoiceNavigation` - Voice-controlled navigation

### Resolution (Data Class)
- `width: Int` - Screen width in pixels
- `height: Int` - Screen height in pixels
- `aspectRatio: String` - Aspect ratio (e.g., "16:9", "4:3")
- `isDolbyVision: Boolean` - HDR support

### ContentMetadata (Data Class)
- `duration: Long?` - Content duration in milliseconds (for video/audio)
- `fileSize: Long?` - File size in bytes
- `format: MediaFormat?` - Media format information
- `quality: VideoQuality?` - Video quality settings
- `language: String?` - Content language
- `subtitleLanguages: List<String>` - Available subtitle languages

### UserPreferences (Data Class)
- `language: String` - Preferred interface language
- `subtitleEnabled: Boolean` - Subtitle preferences
- `audioLevel: Float` - Audio volume preference (0.0-1.0)
- `parentalControlLevel: ParentalLevel` - Content filtering level
- `autoplay: Boolean` - Autoplay preference
- `offlineMode: Boolean` - Offline mode preference

### DeviceInfo (Data Class)
- `deviceId: String` - Unique device identifier
- `model: String` - TV device model
- `osVersion: String` - Operating system version
- `availableMemory: Long` - Available memory in bytes
- `storageSpace: Long` - Available storage space in bytes
- `networkType: NetworkType` - Connection type (WiFi, Ethernet)

### HardwareInfo (Data Class)
- `processorInfo: String` - CPU information
- `memoryTotal: Long` - Total device memory
- `gpuInfo: String?` - Graphics processor information
- `supports4K: Boolean` - 4K video support
- `supportsHDR: Boolean` - HDR support
- `maxRefreshRate: Int` - Maximum refresh rate in Hz

## Data Persistence Strategy

### SQLDelight Schema
```sql
-- Content table for offline storage
CREATE TABLE ContentItem (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    thumbnailUrl TEXT,
    contentType TEXT NOT NULL,
    isOfflineAvailable INTEGER NOT NULL DEFAULT 0,
    lastAccessed INTEGER,
    focusable INTEGER NOT NULL DEFAULT 1,
    metadataJson TEXT
);

-- User sessions table
CREATE TABLE UserSession (
    sessionId TEXT PRIMARY KEY,
    userId TEXT,
    isAuthenticated INTEGER NOT NULL DEFAULT 0,
    preferencesJson TEXT,
    lastActivity INTEGER NOT NULL,
    sessionTimeout INTEGER NOT NULL,
    deviceInfoJson TEXT
);

-- Platform configuration cache
CREATE TABLE PlatformConfiguration (
    platform TEXT PRIMARY KEY,
    inputMethodsJson TEXT NOT NULL,
    screenResolutionJson TEXT NOT NULL,
    supportedFormatsJson TEXT NOT NULL,
    navigationStyle TEXT NOT NULL,
    uiScaling REAL NOT NULL DEFAULT 1.0,
    hardwareCapabilitiesJson TEXT
);
```

### Multiplatform Settings (Key-Value Storage)
- User preferences that need quick access
- Session tokens and authentication data
- Feature flags and configuration overrides
- Temporary UI state (last focused item, scroll position)

## Offline Sync Strategy

### Local-First Architecture
1. **Primary Data Source**: Local SQLDelight database
2. **Cloud Sync**: Periodic synchronization with remote services
3. **Conflict Resolution**: Last-write-wins with user override options
4. **Data Types**:
   - **Always Local**: User preferences, session data, device info
   - **Sync Required**: Content metadata, user watchlists, progress tracking
   - **Cache Only**: Content thumbnails, temporary media files

### Sync Implementation
- Background sync when network available
- Exponential backoff for failed sync attempts
- User notification for sync conflicts requiring resolution
- Graceful degradation when offline (local data only)