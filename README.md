# KMPTV - Kotlin Multiplatform TV App

A Kotlin Multiplatform television application optimized for 10-foot viewing distance that runs on both Android TV and Apple TV platforms.

## Architecture

- **60-70% shared business logic** via Kotlin Multiplatform
- **30-40% platform-specific UI/APIs** for optimal user experience
- **Local-first architecture** with cloud synchronization for offline capabilities
- **Memory-first optimization** for TV hardware constraints (1-4GB RAM)

## Project Structure

```
kmptv/
├── shared-core/           # Kotlin Multiplatform shared library
│   ├── src/commonMain/    # Shared business logic
│   ├── src/androidMain/   # Android-specific implementations  
│   ├── src/iosMain/       # iOS/tvOS-specific implementations
│   └── src/commonTest/    # Shared tests
├── androidtv-app/         # Android TV application
│   └── src/main/          # Android TV UI with Compose for TV
└── appletv-app/           # Apple TV application  
    ├── Sources/           # tvOS native UI with SwiftUI/UIKit
    └── Tests/             # Apple TV-specific tests
```

## Technology Stack

- **Language**: Kotlin 2.2.10 (Stable) with Kotlin Multiplatform
- **Android TV**: Compose for TV + KMP shared modules
- **Apple TV**: Native tvOS (Swift/UIKit) + KMP shared modules  
- **Storage**: SQLDelight for structured data/offline caching, Multiplatform Settings for preferences
- **Testing**: kotlin.test + JUnit 5 for common code, platform-specific UI testing

## Features

### Shared Core Library (Kotlin Multiplatform)
- **Business Logic**: Content management, user sessions, offline sync
- **Data Models**: Cross-platform data structures with SQLDelight persistence
- **Networking**: API communication and caching strategies
- **CLI Interface**: Command-line tools for testing and debugging

### Platform-Specific Applications
- **Android TV**: Leanback-style UI, remote control navigation, media session integration
- **Apple TV**: Focus engine integration, Siri Remote support, Top Shelf integration

## Screenshots

<div align="center">

![Android TV Main Screen](docs/screenshots/Screenshot.png)

</div>

### Key Features Shown
- **3x3 Content Grid**: Both platforms display identical content layout
- **Focus Navigation**: Cards scale and highlight when focused
- **Content Types**: Video (🎬), Audio (🎵), Image (🖼️), Mixed (📁) with color-coded badges
- **Offline Indicators**: Shows which content is available offline
- **TV-Optimized UI**: Large text, proper spacing for 10-foot viewing distance
- **Loading States**: Smooth loading animations with progress indicators

> **Note**: To generate fresh screenshots, run the apps in their respective simulators (Android TV Emulator or Apple TV Simulator in Xcode) and capture the main content screen.

## Getting Started

### Prerequisites
- Kotlin 2.2.10 or later
- Android Studio Hedgehog or later (for Android TV)
- Xcode 15.0 or later (for Apple TV)
- macOS for cross-platform development

### Build Commands

```bash
# Shared core library
cd shared-core && ./gradlew build

# Android TV app
cd androidtv-app && ./gradlew installDebug

# Apple TV app (Xcode)
cd appletv-app && open AppleTVApp.xcodeproj
```

### CLI Usage

The shared-core library provides a command-line interface:

```bash
# Content management
shared-core content list --limit=10 --format=json
shared-core content search "comedy"
shared-core content get content-001

# Session management
shared-core session create-guest --device-id=test-device
shared-core session login --username=testuser --password=password123
shared-core session status

# Health check
shared-core health --verbose
```

## Performance Requirements

- **Startup Time**: < 3 seconds
- **Content Loading**: < 2 seconds  
- **UI Responsiveness**: < 200ms response time
- **Frame Rate**: 60fps maintained during navigation and animations
- **Memory Usage**: < 200MB under normal operation

## Development Principles

### Constitutional Compliance
- **Library-First**: Every feature as a standalone library
- **TDD Mandatory**: Red-Green-Refactor cycle strictly enforced
- **Real Dependencies**: Use actual databases and network calls in integration tests
- **Structured Logging**: Multiplatform logging for debugging and monitoring

### Testing Strategy
- **Contract Testing**: Verify API compatibility between shared library and platform apps
- **Integration Testing**: Test offline sync, session management, cross-platform data consistency
- **UI Testing**: Platform-specific navigation, focus management, accessibility
- **Performance Testing**: Memory usage, rendering performance, startup time

## Implementation Status

✅ **Complete**: Full Kotlin Multiplatform TV app implementation
- Project structure with 3-module architecture
- Kotlin Multiplatform shared-core library
- Android TV app with Compose for TV
- Apple TV app structure with SwiftUI
- SQLDelight database integration
- CLI interface for testing and debugging
- TDD-compliant contract tests

## Key Files

- `/specs/001-create-a-tv/spec.md` - Feature specification
- `/specs/001-create-a-tv/plan.md` - Implementation plan
- `/specs/001-create-a-tv/tasks.md` - Detailed implementation tasks
- `/specs/001-create-a-tv/quickstart.md` - Validation scenarios and setup
- `/CLAUDE.md` - AI assistant context and project overview

## License

This project follows the specifications and implementations outlined in the feature documentation located in `/specs/001-create-a-tv/`.