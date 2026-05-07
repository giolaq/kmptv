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

### ~~P2.2 — Make seek bar interactive~~ ✅ Done
- **Android TV**: Seek bar is focusable; D-pad left/right scrubs in 1% increments. Visual feedback on focus (thicker track, accent thumb).
- **Apple TV**: Remote skip commands via `MPRemoteCommandCenter` (skip forward/back 10s).

### ~~P2.3 — Add vertical overscan margins on Android TV~~ ✅ Done
- Added `top = 24.dp`, `bottom = 48.dp` content padding to `TvLazyColumn`. Row spacing increased to 16dp.

### ~~P2.4 — Integrate Media Session / Now Playing~~ ✅ Done
- **Android TV**: `MediaSession` created alongside ExoPlayer with title/genre metadata. Hardware play/pause buttons now work.
- **Apple TV**: `MPNowPlayingInfoCenter` updated every 0.5s; `MPRemoteCommandCenter` handles play/pause/skip.

### ~~P2.5 — Add `.focusSection()` to Apple TV category rows~~ ✅ Done
- Added `.focusSection()` to `ScrollView` in `CategoryRowView` — focus engine now treats each row as a discrete navigation group.

### ~~P2.6 — Add accessibility labels~~ ✅ Done
- **Android TV**: `contentDescription` on hero banner and detail backdrop images.
- **Apple TV**: `.accessibilityLabel` on hero banner; `.accessibilityHidden(true)` on decorative detail backdrop.

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

### ~~P3.1 — Add image placeholders/shimmer while loading~~ ✅ Done
- **Android TV**: Added subtle background tint (5% white) to card `AsyncImage` so loading cards aren't invisible.
- **Apple TV**: Already had placeholder state (photo icon) — unchanged.

### ~~P3.2 — Debounce hero banner image updates~~ ✅ Done
- Hero banner now waits 250ms of stable focus before updating the image. Prevents rapid-fire loads during fast D-pad scrolling.

### ~~P3.3 — Use proportional hero height~~ ✅ Done
- `height(300.dp)` → `fillMaxHeight(0.35f)` — scales properly on 720p and 1080p displays.

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

### ~~P3.8 — Use `rememberSaveable` for navigation state~~ ✅ Done
- `showingDetail` and `showingVideoPlayer` now survive process death.

### ~~P3.9 — Replace ExoPlayer polling with `Player.Listener` callback~~ ✅ Done
- Removed `while(true) { delay(500) }` loop. Position now updates via `onEvents` callback.
- Switched from `ProgressiveMediaSource` to simple `setMediaItem()`.

### ~~P3.10 — Add `.onExitCommand` to detail view~~ ✅ Done
- `ContentDetailView` now handles Menu button via `.onExitCommand(perform: onBack)`.

---

## Execution Order

```
Phase A (Foundation):  ✅ COMPLETE (d0d9cf7)
Phase B (UI Critical): ✅ COMPLETE
Phase C (UX Polish):   ✅ COMPLETE
```

Phase A fixes architectural integrity so that subsequent UI work is built on solid ground. Phase B addresses the most impactful usability gaps. Phase C polishes the experience.
