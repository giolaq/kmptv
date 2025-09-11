# Quickstart Guide: Kotlin Multiplatform TV App

## Overview
This quickstart guide demonstrates the core functionality of the KMPTV application across both Android TV and Apple TV platforms, validating the key user scenarios from the feature specification.

## Prerequisites

### Development Environment
- Kotlin 2.2.10 or later
- Android Studio Hedgehog or later (for Android TV)
- Xcode 15.0 or later (for Apple TV)
- macOS for cross-platform development

### Target Devices  
- Android TV device or emulator (API level 21+)
- Apple TV device or simulator (tvOS 15.0+)

## Quick Setup

### 1. Initialize Shared Core Library
```bash
# Clone and setup the project
git clone <repository-url>
cd kmptv

# Initialize shared-core library
cd shared-core
./gradlew build

# Verify CLI functionality
./shared-core --version
# Expected output: shared-core 0.1.0
```

### 2. Setup Android TV Application
```bash
# Navigate to Android TV module
cd ../androidtv-app

# Build and install on Android TV device/emulator
./gradlew installDebug

# Run automated tests
./gradlew connectedAndroidTest
```

### 3. Setup Apple TV Application  
```bash
# Navigate to Apple TV module
cd ../appletv-app

# Open in Xcode
open AppleTVApp.xcodeproj

# Build and deploy to Apple TV simulator/device
# In Xcode: Product → Run (⌘R)
```

## Core Functionality Validation

### Scenario 1: Application Launch and Initial Navigation

#### Android TV Validation
1. Launch the KMPTV app from Android TV home screen
2. Verify the main screen displays with proper 10-foot UI scaling
3. Use D-pad remote to navigate between UI elements
4. Confirm focus indicators are clearly visible from viewing distance
5. Press center button to select focused item

**Expected Results:**
- App launches within 3 seconds
- All UI elements are appropriately sized for 10-foot viewing
- Focus moves smoothly with D-pad navigation
- Focus indicators have clear visual feedback (scale, shadow, or border)
- Selection works consistently with center button press

#### Apple TV Validation  
1. Launch the KMPTV app from Apple TV home screen
2. Verify the main screen displays with proper tvOS focus engine integration
3. Use Siri Remote touchpad to navigate between UI elements
4. Confirm focus parallax effects and scaling work properly
5. Click on Siri Remote touchpad to select focused item

**Expected Results:**
- App launches within 3 seconds
- Focus engine properly manages focus updates
- Swipe gestures on touchpad navigate correctly
- Focus effects include parallax and scaling animations
- Click selection works consistently

### Scenario 2: Content Discovery and Navigation

#### Cross-Platform Content Loading
```bash
# Test shared core content functionality
shared-core content list --limit=10 --format=json
```

**Expected JSON Output:**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "content-001",
        "title": "Sample TV Show",
        "contentType": "Video",
        "isOfflineAvailable": false,
        "focusable": true
      }
    ],
    "totalCount": 10,
    "hasMore": true
  }
}
```

#### Platform-Specific Content Display

**Android TV:**
1. Navigate to content grid using D-pad
2. Scroll through content items horizontally and vertically  
3. Verify images load properly and display at appropriate resolution
4. Test boundary navigation (edges of grid)
5. Verify content details appear on focus

**Apple TV:**
1. Navigate to content grid using Siri Remote swipes
2. Scroll through content items with smooth animations
3. Verify parallax effects on focused content cards
4. Test edge cases and focus boundaries
5. Verify content preview appears on focus

**Validation Criteria:**
- Content loads within 2 seconds
- Images are optimized for TV resolution (no pixelation)  
- Navigation reaches all content items
- Boundary navigation handled gracefully (no focus loss)
- Content metadata displays correctly

### Scenario 3: Search Functionality

#### Voice Search (Apple TV Only)
1. Press and hold Siri button on remote
2. Say "Search for comedy shows"
3. Verify search results appear with relevant content
4. Navigate through search results with focus

#### Text Search (Both Platforms)
```bash
# Test search via shared core
shared-core content search "comedy" --format=json
```

**Android TV:**
1. Navigate to search screen using remote
2. Use on-screen keyboard to enter search terms
3. Verify search results update in real-time
4. Navigate through filtered results

**Apple TV:**
1. Navigate to search screen
2. Use tvOS text input methods or Siri Remote keyboard
3. Verify search results update as typing
4. Navigate through filtered results with focus

**Validation Criteria:**
- Search returns results within 1 second
- Results are relevant to search query
- Search works both online and offline (cached content)
- Focus management works properly in search results

### Scenario 4: Session Management

#### Guest Session Creation
```bash
# Create guest session via CLI
shared-core session create-guest --device-id="test-device" --platform="android"

# Verify session status
shared-core session status --format=json
```

**Expected Output:**
```json
{
  "success": true,
  "data": {
    "sessionId": "guest-session-123",
    "userId": null,
    "isAuthenticated": false,
    "lastActivity": 1694356800000,
    "sessionTimeout": 1800000
  }
}
```

**Cross-Platform Validation:**
1. Launch app on both platforms simultaneously
2. Verify each platform maintains independent sessions
3. Test session persistence across app restarts
4. Verify session timeout handling

### Scenario 5: Offline Functionality

#### Enable Offline Mode
```bash
# Enable offline mode
shared-core offline enable

# Download content for offline viewing
shared-core offline download content-001

# List offline content
shared-core offline list --format=table
```

**Expected Table Output:**
```
ID          | Title           | Size    | Downloaded | Status
----------- | --------------- | ------- | ---------- | ----------
content-001 | Sample TV Show  | 2.1 GB  | 2025-09-10 | Complete
```

**Platform Testing:**
1. Disconnect internet connection on TV device
2. Launch KMPTV app
3. Navigate to offline content section
4. Verify offline content loads and plays properly
5. Test that online-only features show appropriate messaging

### Scenario 6: Performance Validation

#### Memory Usage Testing
```bash
# Monitor memory usage during operation
shared-core health --verbose
```

**Performance Benchmarks:**
- App startup time: < 3 seconds
- Content loading time: < 2 seconds
- Navigation response time: < 100ms
- Memory usage: < 200MB under normal operation
- Frame rate: 60fps maintained during navigation and animations

#### Platform-Specific Performance

**Android TV:**
- Test on various Android TV devices (different RAM/CPU)
- Verify smooth scrolling in large content grids
- Test video playback performance
- Verify no memory leaks during extended usage

**Apple TV:**
- Test focus engine performance with complex layouts
- Verify parallax effects maintain 60fps
- Test on different Apple TV generations
- Verify proper memory management with ARC integration

## Error Scenarios Testing

### Network Connectivity Issues
1. Start app with internet connection
2. Load content successfully  
3. Disconnect internet during usage
4. Verify graceful offline mode transition
5. Reconnect internet and verify sync resumption

### Memory Pressure Simulation
1. Load large amounts of content
2. Navigate extensively through the app
3. Monitor memory usage and garbage collection
4. Verify no crashes or performance degradation

### Invalid Content Handling  
1. Attempt to load corrupted or missing content
2. Verify appropriate error messages display
3. Test error recovery mechanisms
4. Verify app remains stable after errors

## Success Criteria Checklist

### Functional Requirements Validation
- [ ] ✅ 10-foot UI optimization verified on both platforms
- [ ] ✅ Directional navigation works with all input methods
- [ ] ✅ Clear focus indicators visible from viewing distance  
- [ ] ✅ Android TV platform runs with full feature parity
- [ ] ✅ Apple TV platform runs with full feature parity
- [ ] ✅ Proper handling of various TV screen resolutions
- [ ] ✅ Consistent user experience across platforms
- [ ] ✅ Platform-specific input methods work correctly

### Performance Requirements Validation
- [ ] ✅ App startup time under 3 seconds
- [ ] ✅ Content loading under 2 seconds
- [ ] ✅ 60fps maintained during navigation
- [ ] ✅ Memory usage within acceptable limits
- [ ] ✅ Smooth operation on target hardware

### Cross-Platform Validation
- [ ] ✅ Shared business logic works identically
- [ ] ✅ Data synchronization across platforms
- [ ] ✅ Consistent content presentation
- [ ] ✅ Platform-specific optimizations applied

## Troubleshooting Common Issues

### Build Issues
- Verify Kotlin version compatibility
- Check platform-specific SDK versions
- Ensure proper dependency resolution

### Focus Navigation Issues
- Verify focus indicators are implemented correctly
- Check focus traversal order is logical
- Test boundary conditions thoroughly

### Performance Issues
- Profile memory usage and optimize large object handling
- Verify image assets are properly sized for TV
- Check for memory leaks in long-running sessions

### Platform Integration Issues
- Verify platform-specific manifests and Info.plist
- Check permissions and capabilities are properly configured
- Test platform-specific features (TV launcher, Top Shelf, etc.)

## Next Steps

After successful quickstart validation:
1. Review detailed implementation tasks in `tasks.md`
2. Begin TDD implementation cycle
3. Setup continuous integration for both platforms
4. Configure automated testing pipeline
5. Plan beta testing with real TV devices