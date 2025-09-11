# Tasks: Kotlin Multiplatform TV App

**Input**: Design documents from `/specs/001-create-a-tv/`
**Prerequisites**: plan.md (✓), research.md (✓), data-model.md (✓), contracts/ (✓)

## Execution Flow (main)
```
1. Load plan.md from feature directory
   → Extract: Kotlin 2.2.10, KMP, 3-project structure, SQLDelight, Compose for TV, native tvOS
2. Load design documents:
   → data-model.md: 4 core entities + supporting types
   → contracts/: 3 API contract files  
   → quickstart.md: validation scenarios
3. Generate tasks by category:
   → Setup: KMP project init, dependencies, build configs
   → Tests: contract tests for 3 APIs, integration tests for user scenarios
   → Core: data models, services, CLI commands  
   → Integration: SQLDelight, logging, platform-specific UI
   → Polish: unit tests, performance validation, documentation
4. Apply task rules:
   → Different modules/files = mark [P] for parallel
   → Same file = sequential (no [P])
   → Tests before implementation (TDD)
5. Generate dependency graph and parallel execution examples
```

## Format: `[ID] [P?] Description`
- **[P]**: Can run in parallel (different files/modules, no dependencies)
- Include exact file paths and module names in descriptions

## Path Conventions
Based on plan.md structure (mobile multiplatform):
- **shared-core/**: Kotlin Multiplatform shared library
- **androidtv-app/**: Android TV application with Compose for TV
- **appletv-app/**: Apple TV application with native tvOS

## Phase 3.1: Setup

- [ ] **T001** Create Kotlin Multiplatform project structure: shared-core/, androidtv-app/, appletv-app/ directories
- [ ] **T002** [P] Initialize shared-core KMP library with Kotlin 2.2.10 and required dependencies (SQLDelight, Multiplatform Settings, kotlin.test)
- [ ] **T003** [P] Initialize androidtv-app Android project with Compose for TV dependencies and API level 21+ target
- [ ] **T004** [P] Initialize appletv-app tvOS project with Xcode, Swift/UIKit, and tvOS 15.0+ target
- [ ] **T005** [P] Configure shared-core build.gradle.kts with commonMain, androidMain, iosMain source sets
- [ ] **T006** [P] Configure androidtv-app build.gradle to consume shared-core library
- [ ] **T007** [P] Configure appletv-app project to integrate with shared-core framework
- [ ] **T008** [P] Setup linting and formatting: ktlint for Kotlin, SwiftLint for Swift
- [ ] **T009** Setup SQLDelight database schema in shared-core/src/commonMain/sqldelight/

## Phase 3.2: Tests First (TDD) ⚠️ MUST COMPLETE BEFORE 3.3
**CRITICAL: These tests MUST be written and MUST FAIL before ANY implementation**

### Contract Tests (Based on contracts/)
- [ ] **T010** [P] Contract test TVApplicationManager interface in shared-core/src/commonTest/kotlin/contract/TVApplicationManagerContractTest.kt
- [ ] **T011** [P] Contract test ContentRepository interface in shared-core/src/commonTest/kotlin/contract/ContentRepositoryContractTest.kt  
- [ ] **T012** [P] Contract test SessionManager interface in shared-core/src/commonTest/kotlin/contract/SessionManagerContractTest.kt
- [ ] **T013** [P] Contract test NavigationManager interface in shared-core/src/commonTest/kotlin/contract/NavigationManagerContractTest.kt
- [ ] **T014** [P] Contract test OfflineManager interface in shared-core/src/commonTest/kotlin/contract/OfflineManagerContractTest.kt
- [ ] **T015** [P] Contract test CLI commands in shared-core/src/commonTest/kotlin/contract/CLIContractTest.kt
- [ ] **T016** [P] Contract test Android TV Focus Manager in androidtv-app/src/test/java/contract/AndroidTVFocusManagerTest.kt
- [ ] **T017** [P] Contract test Android TV Remote Control Handler in androidtv-app/src/test/java/contract/RemoteControlHandlerTest.kt
- [ ] **T018** [P] Contract test Apple TV Focus Manager in appletv-app/Tests/contract/AppleTVFocusManagerTests.swift
- [ ] **T019** [P] Contract test Apple TV Siri Remote Handler in appletv-app/Tests/contract/SiriRemoteHandlerTests.swift

### Integration Tests (Based on quickstart.md scenarios)
- [ ] **T020** [P] Integration test app launch and navigation in shared-core/src/commonTest/kotlin/integration/AppLaunchNavigationTest.kt
- [ ] **T021** [P] Integration test content discovery and loading in shared-core/src/commonTest/kotlin/integration/ContentDiscoveryTest.kt
- [ ] **T022** [P] Integration test search functionality in shared-core/src/commonTest/kotlin/integration/SearchFunctionalityTest.kt
- [ ] **T023** [P] Integration test session management in shared-core/src/commonTest/kotlin/integration/SessionManagementTest.kt
- [ ] **T024** [P] Integration test offline functionality in shared-core/src/commonTest/kotlin/integration/OfflineFunctionalityTest.kt
- [ ] **T025** [P] Integration test Android TV UI navigation in androidtv-app/src/androidTest/java/integration/AndroidTVNavigationTest.kt
- [ ] **T026** [P] Integration test Apple TV UI navigation in appletv-app/Tests/integration/AppleTVNavigationTests.swift

## Phase 3.3: Core Implementation (ONLY after tests are failing)

### Data Models (Based on data-model.md entities)
- [ ] **T027** [P] TVApplication model in shared-core/src/commonMain/kotlin/models/TVApplication.kt
- [ ] **T028** [P] ContentItem model in shared-core/src/commonMain/kotlin/models/ContentItem.kt
- [ ] **T029** [P] UserSession model in shared-core/src/commonMain/kotlin/models/UserSession.kt
- [ ] **T030** [P] PlatformConfiguration model in shared-core/src/commonMain/kotlin/models/PlatformConfiguration.kt
- [ ] **T031** [P] Supporting data types (Platform enum, ContentType enum, etc.) in shared-core/src/commonMain/kotlin/models/Types.kt
- [ ] **T032** [P] ContentMetadata and UserPreferences models in shared-core/src/commonMain/kotlin/models/Metadata.kt

### Repository Layer
- [ ] **T033** [P] ContentRepository implementation in shared-core/src/commonMain/kotlin/repositories/ContentRepositoryImpl.kt
- [ ] **T034** [P] SessionRepository implementation in shared-core/src/commonMain/kotlin/repositories/SessionRepositoryImpl.kt
- [ ] **T035** [P] OfflineRepository implementation in shared-core/src/commonMain/kotlin/repositories/OfflineRepositoryImpl.kt

### Service Layer
- [ ] **T036** TVApplicationManager implementation in shared-core/src/commonMain/kotlin/services/TVApplicationManagerImpl.kt
- [ ] **T037** SessionManager implementation in shared-core/src/commonMain/kotlin/services/SessionManagerImpl.kt  
- [ ] **T038** NavigationManager implementation in shared-core/src/commonMain/kotlin/services/NavigationManagerImpl.kt
- [ ] **T039** OfflineManager implementation in shared-core/src/commonMain/kotlin/services/OfflineManagerImpl.kt

### CLI Implementation
- [ ] **T040** [P] Content CLI commands in shared-core/src/commonMain/kotlin/cli/ContentCommands.kt
- [ ] **T041** [P] Session CLI commands in shared-core/src/commonMain/kotlin/cli/SessionCommands.kt
- [ ] **T042** [P] Offline CLI commands in shared-core/src/commonMain/kotlin/cli/OfflineCommands.kt
- [ ] **T043** [P] Database CLI commands in shared-core/src/commonMain/kotlin/cli/DatabaseCommands.kt
- [ ] **T044** CLI main entry point and argument parsing in shared-core/src/commonMain/kotlin/cli/Main.kt

## Phase 3.4: Platform Integration

### Android TV Platform  
- [ ] **T045** [P] Android TV Focus Manager implementation in androidtv-app/src/main/java/focus/AndroidTVFocusManagerImpl.kt
- [ ] **T046** [P] Remote Control Handler implementation in androidtv-app/src/main/java/input/RemoteControlHandlerImpl.kt
- [ ] **T047** [P] TV Launcher integration in androidtv-app/src/main/java/launcher/AndroidTVLauncherImpl.kt
- [ ] **T048** [P] Compose for TV components (TVLazyGrid, TVCard, TVCarousel) in androidtv-app/src/main/java/compose/
- [ ] **T049** Android TV MainActivity with shared-core integration in androidtv-app/src/main/java/MainActivity.kt
- [ ] **T050** Android TV screen adaptation and display handling in androidtv-app/src/main/java/display/AndroidTVDisplayImpl.kt

### Apple TV Platform
- [ ] **T051** [P] Apple TV Focus Manager implementation in appletv-app/Sources/focus/AppleTVFocusManagerImpl.swift  
- [ ] **T052** [P] Siri Remote Handler implementation in appletv-app/Sources/input/SiriRemoteHandlerImpl.swift
- [ ] **T053** [P] Top Shelf integration in appletv-app/Sources/topshelf/AppleTVTopShelfImpl.swift
- [ ] **T054** [P] SwiftUI TV components (TVContentGrid, TVCard, TVCarousel) in appletv-app/Sources/swiftui/
- [ ] **T055** Apple TV main app structure with shared-core integration in appletv-app/Sources/App.swift
- [ ] **T056** Apple TV screen adaptation and display handling in appletv-app/Sources/display/AppleTVDisplayImpl.swift

### Database Integration  
- [ ] **T057** SQLDelight database implementation with content tables in shared-core/src/commonMain/kotlin/database/Database.kt
- [ ] **T058** Platform-specific database drivers in shared-core/src/androidMain/ and shared-core/src/iosMain/
- [ ] **T059** Database migrations and schema versioning in shared-core/src/commonMain/sqldelight/migrations/

### Logging and Observability
- [ ] **T060** [P] Multiplatform logging implementation in shared-core/src/commonMain/kotlin/logging/Logger.kt
- [ ] **T061** [P] Error handling and crash reporting in shared-core/src/commonMain/kotlin/error/ErrorHandler.kt
- [ ] **T062** [P] Performance monitoring and metrics in shared-core/src/commonMain/kotlin/monitoring/PerformanceMonitor.kt

## Phase 3.5: Polish

### Unit Tests
- [ ] **T063** [P] Unit tests for data models in shared-core/src/commonTest/kotlin/unit/models/
- [ ] **T064** [P] Unit tests for repositories in shared-core/src/commonTest/kotlin/unit/repositories/
- [ ] **T065** [P] Unit tests for services in shared-core/src/commonTest/kotlin/unit/services/
- [ ] **T066** [P] Unit tests for Android TV components in androidtv-app/src/test/java/unit/
- [ ] **T067** [P] Unit tests for Apple TV components in appletv-app/Tests/unit/

### Performance and Validation
- [ ] **T068** Performance tests for memory usage (<200MB) in shared-core/src/commonTest/kotlin/performance/MemoryUsageTest.kt
- [ ] **T069** Performance tests for startup time (<3s) and content loading (<2s) in shared-core/src/commonTest/kotlin/performance/TimingTest.kt
- [ ] **T070** Performance tests for 60fps navigation in androidtv-app/src/androidTest/java/performance/ and appletv-app/Tests/performance/
- [ ] **T071** Run quickstart validation scenarios from quickstart.md
- [ ] **T072** 10-foot UI compliance validation on both platforms

### Documentation and Cleanup
- [ ] **T073** [P] Update shared-core library documentation in shared-core/README.md
- [ ] **T074** [P] Update Android TV app documentation in androidtv-app/README.md  
- [ ] **T075** [P] Update Apple TV app documentation in appletv-app/README.md
- [ ] **T076** [P] Update CLAUDE.md with implementation details and build instructions
- [ ] **T077** Code cleanup and remove duplication across modules
- [ ] **T078** Final integration testing across all three modules

## Dependencies

### Critical Path Dependencies
- **Setup (T001-T009)** before everything
- **Tests (T010-T026)** before implementation (T027-T062)
- **Models (T027-T032)** before repositories (T033-T035) and services (T036-T039)  
- **Repositories (T033-T035)** before services (T036-T039)
- **Services (T036-T039)** before CLI (T040-T044) and platform integration (T045-T062)
- **Database schema (T009)** before database implementation (T057-T059)
- **Core implementation (T027-T044)** before platform integration (T045-T062)
- **Implementation (T027-T062)** before polish (T063-T078)

### Module Dependencies
- **shared-core** foundation before platform apps
- **T005** (shared-core build) before **T006** (androidtv integration) and **T007** (appletv integration)
- **T057-T059** (database) before repository implementations (T033-T035)

## Parallel Execution Examples

### Contract Tests (can run simultaneously)
```bash
# Launch T010-T019 together (different test files):
Task: "Contract test TVApplicationManager interface in shared-core/src/commonTest/kotlin/contract/TVApplicationManagerContractTest.kt"
Task: "Contract test ContentRepository interface in shared-core/src/commonTest/kotlin/contract/ContentRepositoryContractTest.kt"  
Task: "Contract test Android TV Focus Manager in androidtv-app/src/test/java/contract/AndroidTVFocusManagerTest.kt"
Task: "Contract test Apple TV Focus Manager in appletv-app/Tests/contract/AppleTVFocusManagerTests.swift"
```

### Data Models (can run simultaneously)  
```bash
# Launch T027-T032 together (different model files):
Task: "TVApplication model in shared-core/src/commonMain/kotlin/models/TVApplication.kt"
Task: "ContentItem model in shared-core/src/commonMain/kotlin/models/ContentItem.kt"
Task: "UserSession model in shared-core/src/commonMain/kotlin/models/UserSession.kt"  
Task: "PlatformConfiguration model in shared-core/src/commonMain/kotlin/models/PlatformConfiguration.kt"
```

### Platform-Specific Components (can run simultaneously)
```bash
# Launch T045-T048 and T051-T054 together (different modules):
Task: "Android TV Focus Manager implementation in androidtv-app/src/main/java/focus/AndroidTVFocusManagerImpl.kt"
Task: "Apple TV Focus Manager implementation in appletv-app/Sources/focus/AppleTVFocusManagerImpl.swift"
Task: "Compose for TV components in androidtv-app/src/main/java/compose/"
Task: "SwiftUI TV components in appletv-app/Sources/swiftui/"
```

## Notes
- **[P] tasks** = different files/modules, no dependencies between them
- **Tests must fail first** - verify before implementing (TDD requirement)
- **Commit after each task** for clear development history
- **SQLDelight schema** (T009) is foundation for all data operations
- **Platform integration** requires shared-core library to be functional first
- **Performance tests** should validate the specific requirements from plan.md

## Task Generation Rules Applied

1. **From Contracts** (3 files):
   - shared-core-api.md → T010-T015 (6 interface contract tests)
   - androidtv-platform-api.md → T016-T017 (2 platform contract tests)  
   - appletv-platform-api.md → T018-T019 (2 platform contract tests)

2. **From Data Model** (4 core entities):
   - TVApplication → T027
   - ContentItem → T028  
   - UserSession → T029
   - PlatformConfiguration → T030
   - Supporting types → T031-T032

3. **From User Stories** (quickstart.md scenarios):
   - App launch/navigation → T020
   - Content discovery → T021
   - Search functionality → T022
   - Session management → T023
   - Offline functionality → T024
   - Platform-specific navigation → T025-T026

4. **Ordering Applied**:
   - Setup (T001-T009) → Tests (T010-T026) → Models (T027-T032) → Services (T033-T044) → Platform Integration (T045-T062) → Polish (T063-T078)

## Validation Checklist
*Verified by task generation process*

- [x] All contracts have corresponding tests (T010-T019)
- [x] All entities have model tasks (T027-T030) 
- [x] All tests come before implementation (T010-T026 before T027+)
- [x] Parallel tasks are truly independent (different files/modules)
- [x] Each task specifies exact file path
- [x] No [P] task modifies same file as another [P] task
- [x] TDD order enforced (tests must fail before implementation)
- [x] Dependencies clearly documented
- [x] Platform-specific paths account for different tech stacks