# KMPTV — Kotlin Multiplatform TV App

A Kotlin Multiplatform television app optimized for 10-foot viewing that ships on both **Android TV** (Compose for TV) and **Apple TV** (SwiftUI). A shared Kotlin core provides the domain models, the catalogue feed client, session management and a CLI; each platform renders its own native UI on top.

## Screenshots

<div align="center">

**Android TV**
![Android TV home screen](docs/screenshots/Screenshot.png)

**Apple TV**
![Apple TV home screen](docs/screenshots/tvOSscreenshot.png)

</div>

Both platforms now share the same information architecture:

- **Fixed hero banner** at the top showing the focused item's backdrop, title, description and metadata chips.
- **Genre-grouped card rows** below the hero, scrolled independently.
- **Focus-scale poster cards** with a dark theme (`#2A2A2A`) and bright white border + scale on focus.
- **Row dissolve effect** (Android TV) — the row above the currently focused row fades out while scrolling.
- **Full-bleed content detail view** with play / trailer / info actions using `TVFocusButtonStyle` / `TVCardButtonStyle`.
- **Native video playback** — ExoPlayer on Android TV, `AVPlayer` on Apple TV, with a real progress bar wired to the player's current time and duration.

## Architecture

```
kmptv/
├── shared-core/                     # Kotlin Multiplatform library
│   └── src/
│       ├── commonMain/kotlin/
│       │   ├── models/              # ContentItem, UserSession, PlatformConfiguration, ...
│       │   ├── services/            # CatalogService, SessionManager, TVApplicationManager
│       │   ├── repositories/        # ContentRepository (fed by CatalogSource)
│       │   └── cli/                 # ContentCommands, SessionCommands, HealthCommands
│       ├── androidMain/             # Ktor OkHttp engine
│       ├── iosMain/                 # Ktor Darwin engine
│       └── commonTest/              # 23 contract tests (JVM + iOS simulator)
│
├── androidtv-app/                   # Android TV application
│   └── src/main/java/com/kmptv/androidtv/
│       ├── MainActivity.kt          # Hero + rows + navigation state
│       ├── compose/
│       │   ├── TVCard.kt
│       │   ├── FocusModifiers.kt    # tvFocusScale, transparentSurfaceColors
│       │   ├── ContentDetailScreen.kt
│       │   └── VideoPlayerScreen.kt # ExoPlayer + seek bar with BoxWithConstraints
│       └── theme/
│           ├── KmptvColors.kt       # Shared palette
│           └── Theme.kt
│
└── appletv-app/kmptv/               # Apple TV (tvOS) application
    └── kmptv/
        ├── HomeView.swift           # Hero + category rows
        ├── HeroBannerView.swift
        ├── CategoryRowView.swift
        ├── TVCardView.swift
        ├── TVCardButtonStyle.swift  # Poster-card focus style
        ├── TVFocusButtonStyle.swift # Dark button focus style
        ├── ContentDetailView.swift
        ├── VideoPlayerView.swift    # AVPlayer + real currentTime/duration
        ├── CatalogFeed.swift        # URLSession + Codable
        ├── ContentItem.swift
        └── ContentView.swift
```

### Why this split?

- **Domain logic and data** live in `shared-core` so the two apps stay consistent.
- **UI is native on each platform** (Compose for TV, SwiftUI with the tvOS focus engine) so focus behaviour feels right to each platform's users — generic cross-platform UI toolkits don't produce correct 10-foot UX on TV.

## Technology Stack

| Layer             | Choice                                                                 |
|-------------------|------------------------------------------------------------------------|
| Language (shared) | **Kotlin 1.9.24** (Multiplatform)                                      |
| Android TV        | **Compose for TV** (`androidx.tv:tv-foundation / tv-material`), ExoPlayer (Media3 1.4.1) |
| Apple TV          | **SwiftUI** + `AVPlayer`, targeting **tvOS 18.2+**, Apple TV 4K verified |
| Networking        | **Ktor 2.3.12** (OkHttp on Android, Darwin on iOS/tvOS)                |
| Serialization     | `kotlinx.serialization` 1.6.3                                          |
| Dates / time      | `kotlinx.datetime` 0.6.0 (no JVM-only time APIs in `commonMain`)       |
| Concurrency       | `kotlinx.coroutines` 1.8.1                                             |
| Android Gradle    | 8.2.2, JDK 17, `minSdk 21`, `compileSdk 34`                            |
| Testing           | `kotlin.test` — 23 contract tests across JVM and iOS simulator         |

## Catalogue Feed

Both apps fetch their content from a hosted feed:

```
https://giolaq.github.io/scrap-tv-feed/catalog.json
```

- `shared-core` calls it through `CatalogService` using Ktor + kotlinx-serialization.
- `ContentRepositoryImpl` takes a `CatalogSource` fun-interface; tests inject a fake so contract tests run deterministically without reaching the network (the iOS simulator in particular cannot reach the live feed from the Kotlin test runner).
- There is no longer a hardcoded sample fallback — the feed is the single source of truth, and fetch failures surface as `Result.Error`.
- The Apple TV app currently has its own Swift mirror of the feed model (`CatalogFeed.swift`) while the shared-core XCFramework bridge is being finished.

## Getting Started

### Prerequisites
- JDK **17**
- Android Studio Hedgehog or later (for Android TV)
- Xcode **15.0+** with a tvOS 18.2 simulator or an Apple TV 4K device (for Apple TV)
- macOS for cross-platform development (Apple TV build requires Xcode)

### Build

```bash
# Shared core library (compiles Android + iOS/tvOS targets, runs all common tests)
./gradlew :shared-core:build

# Run shared-core tests explicitly
./gradlew :shared-core:jvmTest
./gradlew :shared-core:iosSimulatorArm64Test

# Android TV app — install a debug build on a connected device/emulator
./gradlew :androidtv-app:installDebug

# Apple TV app — open in Xcode
open appletv-app/kmptv/kmptv.xcodeproj
```

### Verified-green build surface

The most recent refactor verified the following commands succeed:

- `shared-core` JVM unit tests (23 tests)
- `shared-core` iOS simulator ARM64 tests (23 tests)
- `androidtv-app` `assembleDebug`
- `appletv-app` `xcodebuild` for Apple TV 4K (tvOS 18.2)
- Manual smoke test: Android TV emulator and tvOS simulator both load feed data with working focus navigation.

### CLI Usage

`shared-core` ships a small CLI useful for testing the catalogue feed and session plumbing:

```bash
# Content
shared-core content list --limit=10 --format=json
shared-core content search "comedy"
shared-core content get content-001

# Session
shared-core session create-guest --device-id=test-device
shared-core session login --username=testuser --password=password123
shared-core session status

# Health
shared-core health --verbose
```

## Performance Targets

- **Startup time**: < 3 s
- **Content loading**: < 2 s
- **UI response**: < 200 ms
- **Frame rate**: 60 fps during navigation and animations
- **Memory**: < 200 MB under normal operation

## Development Principles

- **Shared domain, native UI** — business logic and the feed client live in `shared-core`; each platform renders its own UI using its native focus engine.
- **Real dependencies in integration tests** — contract tests use a `FakeCatalogSource` but exercise the real repository, session manager and application manager code paths.
- **`commonMain` stays pure Kotlin Multiplatform** — no JVM-only APIs slip in; dates, ids and formatting all go through `kotlinx-datetime` and tiny shared helpers.
- **Feed is the source of truth** — no hardcoded sample fallbacks in product code; fetch failures surface as `Result.Error` and the UI handles them.

## Key Files

- [`specs/001-create-a-tv/spec.md`](specs/001-create-a-tv/spec.md) — original feature specification
- [`specs/001-create-a-tv/plan.md`](specs/001-create-a-tv/plan.md) — implementation plan
- [`specs/001-create-a-tv/tasks.md`](specs/001-create-a-tv/tasks.md) — task breakdown
- [`specs/001-create-a-tv/quickstart.md`](specs/001-create-a-tv/quickstart.md) — validation scenarios
- [`specs/002-update-the-specification/spec.md`](specs/002-update-the-specification/spec.md) — updated spec reflecting the implemented system
- [`CLAUDE.md`](CLAUDE.md) — AI assistant context

## License

Released under the [MIT License](LICENSE). See the `LICENSE` file for the full text.
