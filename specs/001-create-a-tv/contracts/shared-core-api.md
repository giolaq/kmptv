# Shared Core API Contract

## Library Interface Contract

The shared-core library provides business logic and data management for TV applications across platforms.

### Core API Methods

#### Application Management
```kotlin
interface TVApplicationManager {
    suspend fun initialize(platform: Platform, configuration: PlatformConfiguration): TVApplication
    suspend fun shutdown(): Unit
    fun getCurrentSession(): UserSession?
    suspend fun updateConfiguration(config: PlatformConfiguration): Result<Unit>
}
```

#### Content Management
```kotlin
interface ContentRepository {
    suspend fun getContentItems(limit: Int = 50, offset: Int = 0): Result<List<ContentItem>>
    suspend fun getContentItem(id: String): Result<ContentItem?>
    suspend fun searchContent(query: String): Result<List<ContentItem>>
    suspend fun markContentAccessed(contentId: String): Result<Unit>
    suspend fun setContentOfflineAvailable(contentId: String, available: Boolean): Result<Unit>
    suspend fun syncContentWithRemote(): Result<SyncStatus>
}
```

#### Session Management
```kotlin
interface SessionManager {
    suspend fun createGuestSession(deviceInfo: DeviceInfo): Result<UserSession>
    suspend fun authenticateUser(credentials: UserCredentials): Result<UserSession>
    suspend fun updateLastActivity(): Unit
    suspend fun isSessionValid(): Boolean
    suspend fun renewSession(): Result<UserSession>
    suspend fun endSession(): Unit
}
```

#### Navigation & Focus Management
```kotlin
interface NavigationManager {
    fun setFocusedItem(contentId: String): Result<Unit>
    fun getFocusedItem(): String?
    fun navigateUp(): NavigationResult
    fun navigateDown(): NavigationResult
    fun navigateLeft(): NavigationResult  
    fun navigateRight(): NavigationResult
    fun selectCurrentItem(): Result<ContentItem>
    fun getNavigationState(): NavigationState
}
```

#### Offline Data Management
```kotlin
interface OfflineManager {
    suspend fun enableOfflineMode(): Result<Unit>
    suspend fun disableOfflineMode(): Result<Unit>
    fun isOfflineMode(): Boolean
    suspend fun downloadContentForOffline(contentId: String): Result<DownloadProgress>
    suspend fun getOfflineContent(): Result<List<ContentItem>>
    suspend fun clearOfflineData(): Result<Unit>
}
```

## Data Transfer Objects

### Request Models
```kotlin
data class InitializeRequest(
    val platform: Platform,
    val deviceInfo: DeviceInfo,
    val configuration: PlatformConfiguration
)

data class ContentSearchRequest(
    val query: String,
    val contentTypes: List<ContentType> = emptyList(),
    val limit: Int = 20
)

data class UserCredentials(
    val username: String?,
    val email: String?,
    val password: String?,
    val oauthToken: String?
)
```

### Response Models  
```kotlin
data class InitializeResponse(
    val application: TVApplication,
    val session: UserSession,
    val success: Boolean,
    val message: String?
)

data class ContentListResponse(
    val items: List<ContentItem>,
    val totalCount: Int,
    val hasMore: Boolean,
    val nextOffset: Int?
)

data class NavigationResult(
    val success: Boolean,
    val newFocusedItem: String?,
    val boundaryReached: Boolean = false
)

data class SyncStatus(
    val lastSyncTimestamp: Long,
    val itemsSynced: Int,
    val conflicts: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)

data class DownloadProgress(
    val contentId: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percentage: Float,
    val status: DownloadStatus
)
```

### Result Types
```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: Exception, val message: String) : Result<T>()
    data class Loading<T>(val progress: Float? = null) : Result<T>()
}

enum class DownloadStatus {
    Pending,
    Downloading,
    Completed,
    Failed,
    Cancelled
}

enum class NavigationState {
    Grid,
    List,
    Details,
    Settings,
    Search
}
```

## CLI Interface Contract

The shared-core library exposes functionality via command-line interface for testing and debugging.

### Commands

#### Content Commands
```bash
# List all content items
shared-core content list [--limit=N] [--offset=N] [--format=json|table]

# Search for content
shared-core content search "query" [--type=video|audio|image] [--format=json|table]

# Get specific content item
shared-core content get <content-id> [--format=json|table]

# Mark content as accessed
shared-core content access <content-id>
```

#### Session Commands  
```bash
# Create guest session
shared-core session create-guest --device-id=<id> --platform=<android|apple>

# Authenticate user
shared-core session login --username=<user> --password=<pass>

# Check session status
shared-core session status [--format=json|table]

# End current session
shared-core session logout
```

#### Offline Commands
```bash
# Enable offline mode
shared-core offline enable

# Download content for offline use
shared-core offline download <content-id>

# List offline content
shared-core offline list [--format=json|table]

# Clear offline data
shared-core offline clear
```

#### Database Commands
```bash
# Initialize database
shared-core db init [--reset]

# Run database migrations  
shared-core db migrate

# Export data
shared-core db export --output=<file> [--format=json|sql]

# Import data
shared-core db import --input=<file>
```

#### Utility Commands
```bash
# Show library version
shared-core --version

# Show help information
shared-core --help

# Validate configuration
shared-core config validate --config=<file>

# Health check (test all systems)
shared-core health [--verbose]
```

### CLI Output Format

#### JSON Format
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "content-123",
        "title": "Sample Video",
        "contentType": "Video",
        "isOfflineAvailable": false
      }
    ],
    "totalCount": 1
  },
  "message": null,
  "timestamp": "2025-09-10T12:00:00Z"
}
```

#### Table Format  
```
ID          | Title        | Type  | Offline | Last Accessed
----------- | ------------ | ----- | ------- | -------------
content-123 | Sample Video | Video | No      | 2025-09-10
```

### Error Handling Contract

#### Standard Error Codes
- `INIT_001`: Application initialization failed
- `AUTH_001`: Authentication failed
- `AUTH_002`: Session expired
- `CONTENT_001`: Content not found
- `CONTENT_002`: Content access denied
- `NETWORK_001`: Network connection failed
- `STORAGE_001`: Database operation failed
- `STORAGE_002`: Insufficient storage space
- `NAV_001`: Navigation boundary reached
- `NAV_002`: Invalid navigation state

#### Error Response Format
```kotlin
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val recoverable: Boolean = false
)
```

## Testing Contract

### Contract Test Requirements

Each API method must have corresponding contract tests that verify:
1. Method signatures match exactly
2. Request/response data structures are valid
3. Error conditions are properly handled
4. CLI commands produce expected output formats
5. Database operations maintain data integrity

### Integration Test Requirements

Tests must verify:
1. Cross-platform data consistency
2. Offline/online mode transitions
3. Session management across app restarts
4. Content sync conflict resolution
5. Navigation state preservation