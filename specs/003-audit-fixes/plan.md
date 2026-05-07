# 003 - Audit Fixes Plan

Prioritized remediation plan based on the 10-foot UI and KMP architecture audits.

## Priority 1: Critical (Blocks usability or architectural integrity)

### P1.1 — Fix font sizes across both platforms
- **Android TV**: Replace all inline `fontSize` below 18sp with theme typography styles. Update `Type.kt` so the smallest style is 18sp.
- **Apple TV**: Replace all `.font(.system(size: N))` below 23pt. Establish a `TVTypography` style guide.
- **Files**: `TVCard.kt`, `ContentDetailScreen.kt`, `VideoPlayerScreen.kt`, `MainActivity.kt`, `TVCardView.swift`, `HeroBannerView.swift`, `ContentDetailView.swift`, `VideoPlayerView.swift`

### P1.2 — Add BackHandler to Android TV navigation
- Add `BackHandler` composable in `ContentDetailScreen` and `VideoPlayerScreen` to navigate back instead of exiting the app.
- **File**: `MainActivity.kt`, `ContentDetailScreen.kt`, `VideoPlayerScreen.kt`

### P1.3 — Force dark theme on Android TV
- Remove `isSystemInDarkTheme()` conditional. Always use `DarkColorScheme`.
- Unify `KmptvColors.Background` with the theme's `background` color.
- **File**: `Theme.kt`

### P1.4 — Wire Apple TV to consume shared-core XCFramework
- Configure the `shared-core` Gradle build to produce an XCFramework artifact.
- Add `dependsOn(commonMain)` to the `iosMain` source set; link to iOS target source sets.
- Create `iosMain` actual implementations (at minimum: Ktor Darwin engine wiring).
- Replace `ContentItem.swift` and `CatalogFeed.swift` in appletv-app with imports from the framework.
- **Files**: `shared-core/build.gradle.kts`, new `shared-core/src/iosMain/`, `appletv-app/kmptv/`

### P1.5 — Fix thread-safety in shared-core repositories
- Wrap `contentStorage` and `catalogLoaded` in a `Mutex` or use `AtomicReference`.
- Same treatment for `SessionManagerImpl.currentSession` and `TVApplicationManagerImpl`.
- **Files**: `ContentRepository.kt`, `SessionManager.kt`, `TVApplicationManager.kt` (in shared-core)

---

## Priority 2: Important (Degrades experience or correctness)

### P2.1 — Add initial focus request on Android TV home screen
- Use `FocusRequester` on the first card in the first `TvLazyRow`; call `requestFocus()` in a `LaunchedEffect`.
- **File**: `MainActivity.kt`

### P2.2 — Make seek bar interactive
- **Android TV**: Make the seek bar focusable; D-pad left/right scrubs. Add long-press acceleration on skip buttons.
- **Apple TV**: Either use native `AVPlayerViewController` or add swipe gesture on the progress region.
- **Files**: `VideoPlayerScreen.kt`, `VideoPlayerView.swift`

### P2.3 — Add vertical overscan margins on Android TV
- Add 48dp top padding to `HomeScreen` / `TvLazyColumn` content.
- **File**: `MainActivity.kt`

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

### P2.7 — Configure Ktor HttpClient properly
- Add `HttpTimeout` plugin (connect: 10s, request: 30s).
- Add response status validation.
- Close client in a `Closeable`/`close()` pattern.
- **File**: `CatalogService.kt`

### P2.8 — Implement SQLDelight persistence (or remove the claim)
- Either: create `.sq` schema files and apply the plugin, wiring repositories to use the DB.
- Or: remove SQLDelight from `build.gradle.kts` and update `CLAUDE.md` to reflect reality.
- **Files**: `build.gradle.kts`, `shared-core/build.gradle.kts`, `CLAUDE.md`

### P2.9 — Introduce dependency injection
- Add Koin (or a simple manual factory) so repositories/services are singletons shared across the app.
- Remove direct `ContentRepositoryImpl()` construction from `MainActivity`, CLI, and health checks.
- **Files**: `MainActivity.kt`, `CLI.kt`, `HealthCommands.kt`, new DI module file

### P2.10 — Remove hardcoded credentials
- Replace `validUsers` map with an injectable `AuthProvider` interface.
- **File**: `SessionManager.kt`

### P2.11 — Fix Kotlin version documentation
- Update `CLAUDE.md` to reflect actual Kotlin version (1.9.24), or upgrade to Kotlin 2.x.
- **File**: `CLAUDE.md` (and potentially all Gradle files if upgrading)

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

### P3.4 — Replace custom `Result<T>` with kotlin.Result or rename to avoid shadowing
- **File**: `shared-core/src/commonMain/kotlin/models/Result.kt`

### P3.5 — Remove dead code
- Delete unused `tvFocusScale` modifier in `FocusModifiers.kt`.
- Remove unused `runBlocking` import in `Main.kt`.

### P3.6 — Fix deprecated Gradle APIs
- Replace `rootProject.buildDir` with `rootProject.layout.buildDirectory`.
- **File**: `build.gradle.kts`

### P3.7 — Annotate data models with `@Serializable`
- Add annotations to `ContentItem`, `UserSession`, `DeviceInfo`, etc.
- Replace manual JSON construction in CLI with `Json.encodeToString()`.

### P3.8 — Use `rememberSaveable` for navigation state
- **File**: `MainActivity.kt`

### P3.9 — Replace ExoPlayer polling with `Player.Listener` callback
- **File**: `VideoPlayerScreen.kt`

### P3.10 — Use `AVPlayerViewController` or add `.onExitCommand` to detail sheet
- **File**: `ContentView.swift`, `ContentDetailView.swift`

---

## Execution Order

```
Phase A (Foundation):  P1.4, P1.5, P2.7, P2.8, P2.9, P2.10, P2.11, P3.4, P3.5, P3.6
Phase B (UI Critical): P1.1, P1.2, P1.3, P2.1, P2.3
Phase C (UX Polish):   P2.2, P2.4, P2.5, P2.6, P3.1, P3.2, P3.3, P3.7, P3.8, P3.9, P3.10
```

Phase A fixes architectural integrity so that subsequent UI work is built on solid ground. Phase B addresses the most impactful usability gaps. Phase C polishes the experience.
