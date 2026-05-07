# 003 - Audit Fixes Plan

Prioritized remediation plan based on the 10-foot UI and KMP architecture audits.

## Priority 1: Critical (Blocks usability or architectural integrity)

### ~~P1.1 — Fix font sizes across both platforms~~ ✅ Done
- **Android TV**: Raised typography floor to 18sp (`bodySmall`, `labelSmall`, `labelMedium`, `labelLarge`). Replaced all inline `fontSize` below 18sp with theme styles.
- **Apple TV**: Raised all font sizes below 23pt to 23-26pt (cards, chips, tags, player time labels).

### ~~P1.2 — Add BackHandler to Android TV navigation~~ ✅ Done
- Added `BackHandler` in `MainScreen` — hardware back on remote now navigates up the stack (player → detail → home) instead of exiting.

### ~~P1.3 — Force dark theme on Android TV~~ ✅ Done
- Removed `LightColorScheme` and `isSystemInDarkTheme()` check. `KMPTVTheme` always uses dark scheme.
- Unified background color to `0xFF0D0D0D` matching `KmptvColors.Background`.

### ~~P1.4 — Wire Apple TV to consume shared-core XCFramework~~ ✅ Done (d0d9cf7)
- Added `applyDefaultHierarchyTemplate()` to wire iosMain → commonMain hierarchy.
- Created `src/iosMain/` and `src/androidMain/` with expect/actual Ktor engine declarations.
- iOS framework links successfully. Apple TV Swift code still mirrors models (full XCFramework consumption is Phase C).

### ~~P1.5 — Fix thread-safety in shared-core repositories~~ ✅ Done (d0d9cf7)
- Added `Mutex` to `ContentRepositoryImpl`, `SessionManagerImpl`, and `TVApplicationManagerImpl`.
- Extracted `ensureCatalogLoaded()` with lock to prevent TOCTOU races.

---

## Priority 2: Important (Degrades experience or correctness)

### ~~P2.1 — Add initial focus request on Android TV home screen~~ ✅ Done
- `FocusRequester` attached to first card in first row; `requestFocus()` called after content loads.

### P2.2 — Make seek bar interactive
- **Android TV**: Make the seek bar focusable; D-pad left/right scrubs. Add long-press acceleration on skip buttons.
- **Apple TV**: Either use native `AVPlayerViewController` or add swipe gesture on the progress region.
- **Files**: `VideoPlayerScreen.kt`, `VideoPlayerView.swift`

### ~~P2.3 — Add vertical overscan margins on Android TV~~ ✅ Done
- Added `top = 24.dp`, `bottom = 48.dp` content padding to `TvLazyColumn`. Row spacing increased to 16dp.

### P2.4 — Integrate Media Session / Now Playing
- **Android TV**: Create `MediaSession` alongside ExoPlayer; register media button receiver.
- **Apple TV**: Configure `MPNowPlayingInfoCenter` and `MPRemoteCommandCenter`.
- **Files**: `VideoPlayerScreen.kt`, `VideoPlayerView.swift`

### P2.5 — Add `.focusSection()` to Apple TV category rows
- Wrap each `ScrollView` in `CategoryRowView` with `.focusSection()`.
- **File**: `CategoryRowView.swift`

### P2.6 — Add accessibility labels
- **Android TV**: Add `contentDescription` to hero banner, backdrop images, player buttons.
- **Apple TV**: Add `.accessibilityLabel` to images; `.accessibilityHidden(true)` for decorative backgrounds.
- **Files**: `MainActivity.kt`, `ContentDetailScreen.kt`, `VideoPlayerScreen.kt`, `HeroBannerView.swift`, `ContentDetailView.swift`

### ~~P2.7 — Configure Ktor HttpClient properly~~ ✅ Done (d0d9cf7)
- Added `HttpTimeout` plugin (connect: 10s, request: 30s, socket: 15s).
- Added HTTP status code validation before deserialization.
- Uses platform-specific engine via expect/actual. Added `close()` method.

### ~~P2.8 — Implement SQLDelight persistence (or remove the claim)~~ ✅ Done (d0d9cf7)
- Removed SQLDelight plugin from root `build.gradle.kts`.
- Updated `CLAUDE.md` to remove SQLDelight/offline claims.

### ~~P2.9 — Introduce dependency injection~~ ✅ Done (d0d9cf7)
- Created `ServiceLocator` in `shared-core/src/commonMain/kotlin/di/`.
- `MainActivity`, `CLI`, and `HealthCommands` now use `ServiceLocator`.

### ~~P2.10 — Remove hardcoded credentials~~ ✅ Done (d0d9cf7)
- Extracted `AuthProvider` functional interface.
- `SessionManagerImpl` takes `AuthProvider` as constructor parameter.

### ~~P2.11 — Fix Kotlin version documentation~~ ✅ Done (d0d9cf7)
- `CLAUDE.md` now correctly states Kotlin 1.9.24.

---

## Priority 3: Minor (Polish and cleanup)

### P3.1 — Add image placeholders/shimmer while loading
- **Android TV**: Add `placeholder` and `error` drawables to `AsyncImage` calls.
- **Apple TV**: Add cross-fade transition on image load.

### P3.2 — Debounce hero banner image updates
- Only update hero after focus is stable for 200-300ms to avoid rapid image loads during fast scrolling.
- **File**: `MainActivity.kt`

### P3.3 — Use proportional hero height
- Replace `height(300.dp)` with `fillMaxHeight(fraction = 0.3f)`.
- **File**: `MainActivity.kt`

### ~~P3.4 — Replace custom `Result<T>` with kotlin.Result or rename to avoid shadowing~~ ⏭️ Deferred
- Kept as-is: explicit package import prevents ambiguity in practice; renaming would touch every file for marginal benefit.

### ~~P3.5 — Remove dead code~~ ✅ Done (d0d9cf7)
- Removed unused `tvFocusScale()` from `FocusModifiers.kt`.
- Removed stale comment in `ContentDetailScreen.kt`.
- Removed unused `runBlocking` import from `CLI`.

### ~~P3.6 — Fix deprecated Gradle APIs~~ ✅ Done (d0d9cf7)
- `rootProject.buildDir` → `rootProject.layout.buildDirectory`.

### ~~P3.7 — Annotate data models with `@Serializable` (partial)~~ ✅ Done (d0d9cf7)
- Replaced manual JSON string construction in CLI with `kotlinx.serialization.json.buildJsonObject`.
- Full `@Serializable` annotations on domain models deferred to when persistence is added.

### P3.8 — Use `rememberSaveable` for navigation state
- **File**: `MainActivity.kt`

### P3.9 — Replace ExoPlayer polling with `Player.Listener` callback
- **File**: `VideoPlayerScreen.kt`

### P3.10 — Use `AVPlayerViewController` or add `.onExitCommand` to detail sheet
- **File**: `ContentView.swift`, `ContentDetailView.swift`

---

## Execution Order

```
Phase A (Foundation):  ✅ COMPLETE (d0d9cf7)
Phase B (UI Critical): ✅ COMPLETE
Phase C (UX Polish):   P2.2, P2.4, P2.5, P2.6, P3.1, P3.2, P3.3, P3.8, P3.9, P3.10
```

Phase A fixes architectural integrity so that subsequent UI work is built on solid ground. Phase B addresses the most impactful usability gaps. Phase C polishes the experience.
