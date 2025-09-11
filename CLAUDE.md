# KMPTV - Kotlin Multiplatform TV App

## Project Overview
KMPTV is a Kotlin Multiplatform television application optimized for 10-foot viewing distance that runs on both Android TV and Apple TV platforms. The app provides consistent cross-platform functionality with platform-specific input handling and proper UI scaling for various TV screen resolutions.

## Architecture

### Technology Stack
- **Language**: Kotlin 2.2.10 (Stable) with Kotlin Multiplatform
- **Android TV**: Compose for TV + KMP shared modules
- **Apple TV**: Native tvOS (Swift/UIKit) + KMP shared modules  
- **Storage**: SQLDelight for structured data/offline caching, Multiplatform Settings for preferences
- **Testing**: kotlin.test + JUnit 5 for common code, platform-specific UI testing

### Project Structure
```
shared-core/          # Kotlin Multiplatform shared library
├── src/commonMain/   # Shared business logic
├── src/androidMain/  # Android-specific implementations  
├── src/iosMain/      # iOS/tvOS-specific implementations
└── src/commonTest/   # Shared tests

androidtv-app/        # Android TV application
├── src/main/         # Android TV UI with Compose for TV
└── src/androidTest/  # Android TV-specific tests

appletv-app/          # Apple TV application  
├── Sources/          # tvOS native UI with SwiftUI/UIKit
└── Tests/            # Apple TV-specific tests
```

### Key Principles
- **60-70% shared business logic** via Kotlin Multiplatform
- **30-40% platform-specific UI/APIs** for optimal user experience
- **Local-first architecture** with cloud synchronization for offline capabilities
- **Memory-first optimization** for TV hardware constraints (1-4GB RAM)

## Core Features

### Shared Core Library (Kotlin Multiplatform)
- **Business Logic**: Content management, user sessions, offline sync
- **Data Models**: Cross-platform data structures with SQLDelight persistence
- **Networking**: API communication and caching strategies
- **CLI Interface**: Command-line tools for testing and debugging

### Platform-Specific Applications
- **Android TV**: Leanback-style UI, remote control navigation, media session integration
- **Apple TV**: Focus engine integration, Siri Remote support, Top Shelf integration

## Development Workflow

### Test-Driven Development (TDD)
1. **Contract Tests**: Define API interfaces and data contracts
2. **Integration Tests**: Test cross-platform functionality with real dependencies
3. **Platform Tests**: UI and platform-specific feature testing
4. **Unit Tests**: Component-level testing

### Constitutional Principles
- **Library-First**: Every feature as a standalone library
- **TDD Mandatory**: Red-Green-Refactor cycle strictly enforced
- **Real Dependencies**: Use actual databases and network calls in integration tests
- **Structured Logging**: Multiplatform logging for debugging and monitoring

## Performance Requirements
- **Startup Time**: < 3 seconds
- **Content Loading**: < 2 seconds  
- **UI Responsiveness**: < 200ms response time
- **Frame Rate**: 60fps maintained during navigation and animations
- **Memory Usage**: < 200MB under normal operation

## Current Implementation Status

### Phase 0: Research ✅
- Technology stack decisions finalized
- Cross-platform architecture validated
- Performance constraints identified

### Phase 1: Design & Contracts ✅  
- Data models defined with SQLDelight integration
- API contracts specified for all three layers (shared-core, androidtv, appletv)
- Quickstart guide with validation scenarios created

### Phase 2: Task Planning ✅
- 78 detailed implementation tasks generated
- TDD task ordering (tests before implementation)
- Dependency management between platform modules

### Phase 3: Implementation ✅
- **shared-core**: Kotlin Multiplatform library with data models, repositories, services, and CLI
- **androidtv-app**: Android TV application with Compose for TV UI components
- **appletv-app**: Apple TV application structure with SwiftUI components
- **SQLDelight**: Database schema and queries for content, sessions, and configuration
- **Contract Tests**: TDD-compliant failing tests for all major interfaces
- **CLI Interface**: Command-line tools for content, session, and health management

## Key Files
- `/specs/001-create-a-tv/spec.md` - Feature specification
- `/specs/001-create-a-tv/plan.md` - Implementation plan
- `/specs/001-create-a-tv/research.md` - Technology research findings
- `/specs/001-create-a-tv/data-model.md` - Data structures and persistence
- `/specs/001-create-a-tv/contracts/` - API contracts for all modules
- `/specs/001-create-a-tv/quickstart.md` - Validation scenarios and setup

## Testing Strategy
- **Contract Testing**: Verify API compatibility between shared library and platform apps
- **Integration Testing**: Test offline sync, session management, cross-platform data consistency
- **UI Testing**: Platform-specific navigation, focus management, accessibility
- **Performance Testing**: Memory usage, rendering performance, startup time

## Build Commands
```bash
# Shared core library
cd shared-core && ./gradlew build

# Android TV app
cd androidtv-app && ./gradlew installDebug

# Apple TV app (Xcode)
cd appletv-app && open AppleTVApp.xcodeproj
```

## Recent Changes
- **Feature Implementation Completed**: Full Kotlin Multiplatform TV app implementation
- **Project Structure**: 3-module architecture (shared-core, androidtv-app, appletv-app) 
- **Core Library**: Kotlin Multiplatform shared-core with data models, repositories, services
- **Android TV App**: Compose for TV application with 10-foot UI optimization
- **Apple TV App**: SwiftUI-based application structure with focus management
- **Database Integration**: SQLDelight schemas for content, sessions, and platform configuration
- **CLI Tools**: Command-line interface for content management, sessions, and health checks
- **TDD Implementation**: Contract tests implemented following constitutional requirements