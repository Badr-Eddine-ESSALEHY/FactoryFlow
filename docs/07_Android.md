# 07_Android.md

> **FactoryFlow — Android Application Architecture & Implementation Specification**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-11
>
> This document defines the **Android application architecture, package organization, implementation rules, state-management patterns, navigation behavior, data access, backend OCR integration, Share Intent handling, FileProvider usage, notifications, Room usage, Retrofit contracts, testing expectations, and premium Compose implementation standards** for FactoryFlow.
>
> This document must remain aligned with:
>
> - `AGENTS.md`
> - `TASKS.md`
> - `SKILLS.md`
> - `DESIGN.md`
> - `UI_UX.md`
> - `03_Architecture.md`
> - `04_Business_Rules.md`
> - `05_Database.md`
> - `06_API.md`
> - `08_Backend.md`
>
> The top-level `assets/` folder contains the private original WhatsApp screenshots.
> Review them when OCR/parser context matters, but never modify, move, delete, or publicly expose them.
> Tests, reports, GitHub, and demos must use sanitized derivatives created later.
>
> FactoryFlow is an **Android-only native application**.
>
> Approved primary stack:
>
> ```text
> Kotlin
> Jetpack Compose
> Material 3
> MVVM
> Repository pattern
> Hilt
> Retrofit
> Room
> Coroutines
> Flow / StateFlow
> Navigation Compose
> PaddleOCR backend integration
> Android Share Intent
> FileProvider
> Firebase Cloud Messaging (SHOULD after the trusted core)
> ```
>
> The exact dependency versions belong in Gradle/build files and must be chosen for compatibility and stability, not simply because they are the newest available.
>
> The Android UI language is professional French. All user-facing copy lives in
> string resources. Code identifiers, package/class names, API/database contracts,
> Git commits, and technical documentation remain in English.

---

# 1. Android Product Objective

The Android app exists because the real maintenance workflow already begins on mobile.

The maintenance engineer receives KPI information through WhatsApp.

The ideal product interaction is therefore:

```text
WhatsApp
   ↓
Share image to FactoryFlow
   ↓
OCR
   ↓
Parser
   ↓
Review
   ↓
Correct
   ↓
Confirm
   ↓
Dashboard
   ↓
Generate / Share report
```

The Android app should reduce friction between receiving the information and turning it into trusted structured data.

---

# 2. Android Architecture Goals

The Android application must be:

- maintainable
- testable
- lifecycle-safe
- responsive
- premium in presentation
- robust during interruptions
- clear in state management
- easy to explain during interview/report defense

It must not become:

- one Activity with all logic
- Composables calling Retrofit directly
- a collection of mutable global objects
- a copy of backend business rules
- an offline-first synchronization experiment unless explicitly scoped

---

# 3. Architecture Style

FactoryFlow Android uses:

```text
MVVM
+
Repository
+
Unidirectional UI State
```

Recommended flow:

```text
Composable
    ↓ user event
ViewModel
    ↓
Repository
    ↓
RemoteDataSource / LocalDataSource
    ↓
Retrofit / Room
    ↓
Result
    ↓
ViewModel updates UiState
    ↓
Composable renders state
```

---

# 4. Clean Architecture Position

The Android app should respect clean boundaries without forcing excessive ceremony.

A practical structure is preferred over textbook layering for every feature.

Do not create:

```text
UseCase
Interactor
Gateway
Mapper
Repository
DataSource
Adapter
Facade
Coordinator
```

for a trivial feature unless those layers provide real value.

The goal is clarity and separation, not pattern count.

---

# 5. Recommended Package Structure

A feature-oriented package structure is preferred.

Example:

```text
com.factoryflow.app
│
├── app/
│   ├── FactoryFlowApplication.kt
│   ├── MainActivity.kt
│   └── AppState.kt
│
├── core/
│   ├── design/
│   ├── navigation/
│   ├── network/
│   ├── database/
│   ├── auth/
│   ├── util/
│   └── model/
│
├── feature/
│   ├── auth/
│   ├── dashboard/
│   ├── acquisition/
│   ├── confirmation/
│   ├── reports/
│   ├── generatedreports/
│   ├── schedules/
│   ├── notifications/
│   ├── statistics/
│   ├── profile/
│   └── settings/
│
└── di/
```

---

# 6. Feature Package Structure

A feature may contain:

```text
feature/dashboard/
├── ui/
├── DashboardViewModel.kt
├── DashboardUiState.kt
├── DashboardEvent.kt
├── DashboardRepository.kt
└── model/
```

For larger features:

```text
feature/reports/
├── data/
├── domain/
├── ui/
├── model/
└── navigation/
```

Do not force identical package depth on every feature.

---

# 7. Core vs Feature Code

Place code in `core/` only when multiple features genuinely reuse it.

Good core candidates:

```text
API client
auth/session
Room database
design tokens/components
navigation primitives
error mapping
date formatting
common models
```

Avoid dumping miscellaneous helpers into `core/util`.

---

# 8. Dependency Injection

Use Hilt.

Inject:

- Retrofit API services
- repositories
- Room database
- DAOs
- data sources
- session manager
- report file manager
- OCR wrapper if abstraction is useful
- notification/token services

Do not inject Android Context everywhere.

Prefer narrow abstractions.

---

# 9. Application Class

Use:

```kotlin
@HiltAndroidApp
class FactoryFlowApplication : Application()
```

Keep application startup minimal.

Do not start heavy network/database work from `Application.onCreate()` without real need.

---

# 10. Main Activity

Use one primary `MainActivity`.

Responsibilities:

- host Compose
- receive intent updates
- initialize navigation shell
- route external Share Intent when appropriate
- integrate system UI

Do not put business logic in Activity.

---

# 11. Single-Activity Architecture

FactoryFlow should use a single-activity Compose architecture.

Feature navigation occurs through Navigation Compose.

Avoid one Activity per screen.

---

# 12. Compose Entry Point

Conceptually:

```kotlin
setContent {
    FactoryFlowTheme {
        FactoryFlowApp()
    }
}
```

`FactoryFlowApp()` owns:

- app-level navigation
- session destination
- global snackbar host if needed
- app-level system UI integration

---

# 13. Theme Architecture

Theme implementation must follow `DESIGN.md`.

Centralize:

```text
colors
typography
shapes
spacing
motion values
```

Do not hardcode:

```kotlin
Color(0xFF0066CC)
```

through random Composables.

---

# 14. Material 3

Use Material 3 components where appropriate.

Examples:

```text
Scaffold
TopAppBar
NavigationBar
Button
OutlinedTextField
ModalBottomSheet
AlertDialog
SnackbarHost
DatePicker
TimePicker
```

Customize appearance through FactoryFlow theme.

---

# 15. Premium Visual Requirement

FactoryFlow must not look like a default Material sample.

Implementation should intentionally customize:

- typography hierarchy
- spacing
- surface tones
- card shape
- button shape
- status chips
- empty states
- skeletons
- animations
- chart styling

The premium goal comes from restraint and consistency.

---

# 16. Compose Component Strategy

Create reusable components when they repeat.

Recommended candidates:

```text
FactoryFlowCard
PrimaryActionButton
SecondaryActionButton
StatusChip
SectionHeader
KpiValueRow
KpiWarningRow
EmptyState
ErrorState
LoadingSkeleton
ReportSummaryCard
GeneratedFileCard
ScheduleCard
NotificationRow
```

Do not create one universal component with dozens of flags.

---

# 17. Stateless Composable Preference

Prefer:

```kotlin
@Composable
fun ReportConfirmationScreen(
    state: ConfirmationUiState,
    onEvent: (ConfirmationEvent) -> Unit
)
```

rather than reading repositories directly.

State and actions should be explicit.

---

# 18. Preview Strategy

Use Compose previews for:

- reusable components
- dashboard
- confirmation
- history rows
- empty/error states
- dark/light themes if supported

Use safe mock UI models.

Do not depend on live backend in previews.

---

# 19. UiState Pattern

Use explicit `UiState`.

Example:

```kotlin
sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val data: DashboardUiModel
    ) : DashboardUiState

    data class Error(
        val message: UiMessage
    ) : DashboardUiState
}
```

For screens that can show content while refreshing, a data class state may be better.

Choose the state model that prevents impossible combinations.

---

# 20. Data-Class State Pattern

Example:

```kotlin
data class ConfirmationUiState(
    val isLoading: Boolean = false,
    val report: ReviewReportUiModel? = null,
    val isSavingDraft: Boolean = false,
    val isConfirming: Boolean = false,
    val error: UiMessage? = null,
    val success: ConfirmationSuccess? = null
)
```

Do not accumulate twenty unrelated booleans if a sealed state is clearer.

---

# 21. UI Events

Represent user actions explicitly.

Example:

```kotlin
sealed interface ConfirmationEvent {
    data class ValueChanged(
        val entryId: String,
        val value: String
    ) : ConfirmationEvent

    data class RemoveEntry(
        val entryId: String
    ) : ConfirmationEvent

    data object SaveDraft : ConfirmationEvent
    data object Confirm : ConfirmationEvent
}
```

---

# 22. One-Off Effects

Navigation/snackbar actions should not become sticky state that replays after rotation.

Use a lifecycle-aware effect channel/flow where appropriate.

Do not use old Event wrapper patterns blindly.

---

# 23. StateFlow

ViewModels should generally expose:

```kotlin
StateFlow<UiState>
```

and keep mutation private.

Example:

```kotlin
private val _uiState = MutableStateFlow(...)
val uiState = _uiState.asStateFlow()
```

---

# 24. Lifecycle Collection

Use lifecycle-aware Compose collection.

Preferred:

```kotlin
collectAsStateWithLifecycle()
```

where supported by selected libraries.

Do not collect forever outside lifecycle awareness.

---

# 25. Coroutines

Use structured concurrency.

ViewModels:

```kotlin
viewModelScope.launch { ... }
```

Repositories may perform suspend calls.

Never use:

```kotlin
GlobalScope
```

---

# 26. Dispatcher Rules

Retrofit suspend calls are already non-blocking.

Room supports suspend/Flow.

CPU-heavy image/text operations may use appropriate dispatcher.

Do not manually switch to IO everywhere without understanding library behavior.

---

# 27. Cancellation

Long-running UI-bound work should cancel when no longer relevant where appropriate.

Examples:

- search request
- image OCR
- history refresh

Do not leave stale work updating a screen the user has left.

---

# 28. Repository Pattern

Repository owns data access coordination.

Example:

```kotlin
interface ReportsRepository {
    suspend fun analyzeReport(request: AnalyzeReportRequest): Result<AnalyzeReport>
    suspend fun confirmReport(...): Result<MaintenanceReport>
    fun observeDrafts(): Flow<List<DraftSummary>>
}
```

The exact interface should remain pragmatic.

---

# 29. Repository Interface Rule

Interfaces are appropriate when:

- multiple implementations exist
- testing benefits
- architectural boundary is meaningful

Do not create an interface solely because every repository "should" have one.

---

# 30. Remote Data Source

Retrofit services may be used directly inside repositories if that remains clear.

A separate `RemoteDataSource` is useful when:

- response mapping is complex
- multiple API calls are coordinated
- consistent network handling is required

Do not create an empty pass-through class for appearance.

---

# 31. Local Data Source

Room DAOs may be wrapped if repository logic benefits.

Again, avoid redundant pass-through layers.

---

# 32. Retrofit Configuration

Use one primary Retrofit instance for FactoryFlow backend.

Configure:

- base URL
- JSON converter
- OkHttp client
- auth interceptor
- timeout values
- logging only in safe development builds

---

# 33. JSON Serialization

Use one consistent library.

Recommended options:

```text
kotlinx.serialization
Moshi
Jackson
```

Choose one based on Retrofit compatibility and project preference.

Do not mix serializers unnecessarily.

---

# 34. Base URL

Base URL must come from build/configuration.

Do not hardcode:

```text
http://192.168.x.x:8080
```

inside API interfaces.

---

# 35. Development Base URL

For Android Emulator, local backend may use:

```text
10.0.2.2
```

instead of `localhost`.

For physical device testing:

use development machine LAN IP or another reachable host.

Document exact development setup after backend port is finalized.

---

# 36. HTTPS

Production-like deployment should use HTTPS.

Local development may use HTTP according to development network security config.

Do not globally disable Android network security protections.

---

# 37. OkHttp Auth Interceptor

Add access token to protected requests.

Conceptually:

```text
Request
→ read access token
→ Authorization: Bearer ...
```

Do not add token to endpoints where inappropriate if architecture separates public API clients.

---

# 38. Token Refresh

Use a safe refresh strategy.

Requirements:

- avoid infinite refresh loop
- synchronize concurrent refresh attempts
- retry only original safe request
- logout if refresh fails
- preserve draft state where possible

Avoid every failed request starting its own token refresh concurrently.

---

# 39. Token Storage

Use secure Android storage appropriate to selected Android SDK/library stack.

Do not store access/refresh tokens in plain shared preferences.

Do not log tokens.

---

# 40. Session Manager

A dedicated session abstraction should provide:

- current tokens
- authenticated state
- user summary where needed
- logout/clear session

It should not become a global service locator.

---

# 41. API Error Mapping

Map backend error envelope into Android domain errors.

Example:

```text
AUTH_INVALID_CREDENTIALS
→ InvalidCredentials

REPORT_ALREADY_CONFIRMED
→ ReportAlreadyConfirmed

NETWORK
→ ConnectionUnavailable
```

UI then maps to user-facing text.

---

# 42. UiMessage

Use a structured UI message abstraction if helpful.

Example:

```kotlin
sealed interface UiMessage {
    data class Resource(@StringRes val id: Int) : UiMessage
    data class Dynamic(val value: String) : UiMessage
}
```

Do not hardcode all error strings in ViewModels.

---

# 43. Localization Readiness

Place every user-facing string in Android resources and write the first-release UI
in professional French. Do not mix French and English labels.

Do not hardcode:

```kotlin
Text("Confirm Report") // code example of what not to hardcode
```

throughout the codebase.

Canonical UI examples include `Créer un rapport`, `Analyser`, `Enregistrer le brouillon`,
`Confirmer le rapport`, `Valeur manquante`, `Contenu non reconnu`, `Générer le PDF`,
`Générer le fichier Excel`, `Partager`, and `Réessayer`.

---

# 44. API DTOs

Network DTOs should reflect `06_API.md`.

Examples:

```text
LoginRequestDto
LoginResponseDto
AnalyzeReportRequestDto
AnalyzeReportResponseDto
ConfirmReportRequestDto
ReportSummaryDto
GeneratedReportDto
ScheduleDto
NotificationDto
```

---

# 45. DTO Mapping

Map DTOs into UI/domain models when doing so:

- protects UI from contract shape
- simplifies presentation
- isolates enums/dates
- improves testability

Do not create duplicate data classes with identical fields everywhere.

---

# 46. Room Purpose

Room is optional support for:

- cache
- drafts
- KPI definitions
- notification history
- resiliency during interruptions

PostgreSQL remains authoritative.

---

# 47. Room Database

If Room is used:

```kotlin
@Database(
    entities = [...],
    version = 1
)
abstract class FactoryFlowDatabase : RoomDatabase()
```

Use explicit migrations once schema changes.

Do not use destructive migration in presentation-ready builds without justification.

---

# 48. Room Entities

Room entities should be local-storage models.

Do not reuse Retrofit DTOs as Room entities merely to reduce files if semantics differ.

---

# 49. Draft Persistence in Room

Drafts are the strongest local-persistence candidate.

A local draft can preserve:

- raw text
- source type
- current reviewed values
- warnings
- backend draft ID if one exists
- last modified time

Exact sync behavior must remain simple.

---

# 50. Draft Authority

A local draft is work-in-progress.

It is not official business state.

Only backend confirmation makes data authoritative.

---

# 51. Draft Sync Strategy

Preferred initial strategy:

```text
User edits
→ ViewModel state
→ explicit Save Draft
→ backend draft
→ optional Room backup/cache
```

Do not build complex bidirectional sync unless needed.

---

# 52. Cache Strategy

For any cached data, document:

```text
source of truth
cache freshness
invalidating event
offline behavior
```

Do not create cache because Room is available.

---

# 53. KPI Definition Cache

Reasonable candidate.

Benefits:

- fast manual-entry selector
- parser-related display metadata
- basic resilience

Refresh when:

- app starts/session established
- backend signals definition change
- user manually refreshes if needed

---

# 54. Dashboard Cache

Optional.

If implemented, cached dashboard must show freshness.

Do not present stale values as current without indication when that distinction matters.

---

# 55. Offline Position

FactoryFlow is not initially a full offline-first product.

The app should be resilient to temporary connectivity problems.

It should not claim full synchronization.

---

# 56. Offline Behavior

At minimum:

- preserve current user input
- preserve draft edits where possible
- show connection error
- allow retry
- do not mark confirmation success locally
- do not fabricate dashboard freshness

---

# 57. Network Monitor

A network-state observer may improve UX.

Do not rely on connectivity state alone to decide request success.

A network can be "connected" while backend is unreachable.

---

# 58. Authentication Navigation

Startup flow:

```text
Splash
→ Session Check
→ Refresh if needed
→ Dashboard
```

or:

```text
Splash
→ Login
```

Login success clears Login from back stack.

---

# 59. Navigation Graph

Recommended primary routes:

```text
login
dashboard
create
paste
gallery
manual
confirmation/{draftId?}
reports
report/{id}
generatedReports
generatedReport/{id}
schedules
schedule/{id}
notifications
statistics
profile
settings
```

Use typed routes where supported.

---

# 60. Route Constants

Do not scatter literal route strings throughout the app.

Centralize navigation destinations.

---

# 61. Navigation Arguments

Prefer small identifiers:

```text
reportId
generatedReportId
scheduleId
```

Do not pass large JSON objects through routes.

Load detail from repository/ViewModel.

---

# 62. Bottom Navigation

Recommended primary destinations:

```text
Tableau de bord
Rapports
Créer
Notifications
```

Profile/settings accessible from top bar or secondary destination.

Final structure follows `UI_UX.md`.

---

# 63. Bottom Navigation Visibility

Hide during focused workflows:

```text
Login
Gallery OCR
OCR processing
Confirmation
Schedule edit
```

Show on primary top-level screens.

---

# 64. Back Stack After Login

Use navigation options so:

```text
Dashboard
→ Back
```

does not return to Login.

---

# 65. Back Stack After Confirmation

After authoritative confirmation:

Do not allow Back to reopen an editable pre-confirmation state.

Navigate to:

```text
Report Detail
```

and remove/replace confirmation route.

---

# 66. Report History Return

When:

```text
Reports
→ Report Detail
→ Back
```

preserve:

- search
- filters
- scroll position when practical

---

# 67. External Intent Navigation

Share Intent may enter app at:

```text
Acquisition
```

while preserving authentication requirement.

External input should not bypass session checks.

---

# 68. Pending External Input

If user must log in after sharing:

Temporarily preserve:

- URI
- MIME type
- source context

Then resume after successful login if possible.

---

# 69. Cold Start Share Intent

Handle shared intent in `MainActivity` startup.

Do not rely only on `onNewIntent`.

---

# 70. Warm Start Share Intent

Handle `onNewIntent` or equivalent Compose/activity integration.

Avoid duplicating same shared content after recomposition.

---

# 71. Intent Consumption

Shared intents should be consumed exactly once per user action.

Do not reprocess on rotation/recomposition.

---

# 72. Supported Inbound MIME Types

At minimum:

```text
image/*
```

The actual intent filter should remain as narrow as practical.

---

# 73. Unsupported Shared Content

If unsupported:

Show user-friendly message.

Do not crash.

---

# 74. Content URI Handling

Use `ContentResolver`.

Never assume:

```text
content://...
```

can be converted into a real filesystem path.

---

# 75. URI Permission

Use granted URI permissions correctly.

Copy into app-private temporary storage if long-lived access is needed beyond the original grant.

---

# 76. Image Lifecycle

For OCR:

```text
URI
→ authenticated multipart upload
→ PaddleOCR runtime
→ text
```

Avoid unnecessary full-resolution bitmap loading.

---

# 77. Gallery Picker

Use modern Activity Result API.

Prefer Android photo picker where supported.

Fallback only where necessary.

---

# 78. Gallery Cancellation

If user cancels:

Return cleanly.

No error.

---

# 79. Gallery Preview

A compact preview may be shown.

Do not create unnecessary extra steps if image is clearly selected and OCR can begin directly.

---

# 80. Image Acquisition Architecture

Android acquisition owns:

- gallery selection
- shared content URI validation
- lifecycle-safe handoff
- authenticated multipart upload

It does not own OCR business logic.

---

# 81. URI Permission

Consume only the read permission granted with the picker or Share Intent.

If the URI is unreadable, show a recoverable error and keep the other acquisition methods available.

# 82. Gallery and Share-Image Use Cases

```text
Select or receive image
→ validate MIME and bounded size
→ upload to OCR API
→ inspect recognized text
→ deterministic parser
→ human review
```

# 85. Image Rotation

The OCR runtime applies EXIF orientation before recognition.

OCR should receive properly oriented input.

---

# 86. Image Error Handling

Handle:

- unsupported MIME type
- unreadable or missing URI
- oversized upload
- OCR timeout or unavailability

Gracefully.

---

# 87. Backend PaddleOCR Architecture

Use the private PaddleOCR runtime through the authenticated backend endpoint.

OCR responsibility:

```text
Image → Text
```

It does not:

- match KPI definitions
- evaluate plausible range
- persist report
- confirm data

---

# 88. OCR Service Abstraction

A small wrapper may improve testability.

Example:

```kotlin
interface TextRecognitionService {
    suspend fun recognizeText(uri: Uri): OcrResult
}
```

Use only if it provides value.

---

# 89. OCR Result

Conceptual:

```kotlin
data class OcrResult(
    val text: String,
    val blocks: List<OcrBlock> = emptyList()
)
```

For FactoryFlow core, full recognized text may be enough.

Do not interpret OCR blocks as KPI semantics on Android.

One screenshot feeds one review flow in MVP even when OCR sees multiple WhatsApp
bubbles. Preserve all recognized content and unknown lines; do not auto-split it into
multiple `MaintenanceReport` records.

---

# 90. OCR Empty Result

If recognized text is empty/insufficient:

Return a distinct outcome.

UI offers:

- retry
- another image
- manual entry

---

# 91. OCR Progress

Expose stage:

```text
Reading image
```

Then backend analyze stage:

```text
Analyzing KPI values
```

Do not fake percentage progress.

---

# 92. OCR Error Mapping

Technical OCR errors should map to:

```text
Couldn’t read this image.
```

with retry/fallback.

---

# 93. OCR Testability

Separate:

```text
image → OCR
```

from:

```text
text → parser
```

Parser has deterministic unit tests.

OCR integration gets controlled image/manual tests.

---

# 94. Paste Flow Architecture

```text
PasteTextScreen
    ↓
PasteViewModel
    ↓
ReportsRepository.analyze()
    ↓
Retrofit
    ↓
Analyze response
    ↓
Confirmation navigation
```

Preserve raw text.

---

# 95. Clipboard

Clipboard access should be user-triggered when appropriate.

Possible action:

```text
Paste from Clipboard
```

Avoid silently reading clipboard.

---

# 96. Manual Entry Architecture

Manual entry should use cached/backend KPI definitions.

Flow:

```text
KPI selector
→ value
→ review
→ draft
→ confirmation
```

Do not create a separate persistence endpoint solely for manual input if the common report lifecycle can handle it.

---

# 97. Manual KPI Selector

Use:

- search
- category if useful
- friendly display name
- unit

Do not expose parser aliases as primary UI labels.

---

# 98. Manual Numeric Input

Use numeric keyboard.

Store input text while editing.

Convert to decimal only at validation/submission boundaries.

Do not fight locale separator while user types.

---

# 99. Confirmation Architecture

The confirmation feature should own:

- parser candidate display
- edited values
- warnings
- unknown lines
- add/remove
- save draft
- final confirmation request

This is the central Android business workflow.

---

# 100. Confirmation UiModel

Conceptual:

```kotlin
data class ReviewEntryUiModel(
    val id: String,
    val kpiDefinitionId: Long,
    val displayName: String,
    val extractedValue: String?,
    val editedValue: String,
    val unit: String?,
    val confidenceLevel: ConfidenceLevel,
    val warnings: List<ReviewWarningUiModel>,
    val editedByUser: Boolean,
    val sourceLine: String?
)
```

---

# 101. String Input vs BigDecimal

Keep user editing as string state.

Convert safely when sending validated request.

This prevents cursor/decimal formatting problems.

---

# 102. Edited Flag

Android may visually indicate edited state.

Backend should still derive/validate final differences.

Do not trust UI-only flags for audit correctness.

---

# 103. Warning Navigation

ViewModel can expose:

- number of unresolved warnings
- next warning index

to support fast review.

---

# 104. Unknown Lines

Represent explicitly.

Example:

```kotlin
data class UnrecognizedLineUiModel(
    val id: String,
    val text: String,
    val resolution: ResolutionState
)
```

Do not discard them before user decision.

Resolution values sent to the backend are `UNRESOLVED`, `ASSIGNED`, or `IGNORED`.

---

# 105. Add Missing KPI

Confirmation screen opens KPI selector.

Selected KPI becomes a manually added entry.

---

# 106. Remove Candidate

Removing an incorrect extraction updates review state.

If undo is available:

Use Snackbar.

---

# 107. Save Draft Architecture

ViewModel calls repository.

While saving:

- disable duplicate save
- preserve editability where safe
- show loading on action

On success:

- update backend draft ID/version
- show snackbar

---

# 108. Confirmation Architecture

On Confirm:

1. validate local input
2. create request DTO
3. disable duplicate confirm
4. call `POST /api/reports/{id}/confirm`
5. handle state conflict
6. navigate only after authoritative success

---

# 109. No Optimistic Confirmation

Do not set:

```text
Confirmed
```

before backend success.

---

# 110. Confirmation Conflict

If backend says already confirmed/stale:

Show:

```text
This report was updated elsewhere.
```

Then reload authoritative report.

---

# 111. Draft ViewModel Persistence

Use `SavedStateHandle` where appropriate for:

- IDs
- small navigation state

Do not store huge report payloads in SavedStateHandle if Room/repository is more appropriate.

---

# 112. Process Death

Critical draft content should survive realistic process death when draft support is implemented.

Prefer local persistence rather than relying only on ViewModel memory.

---

# 113. Dashboard ViewModel

Responsibilities:

- load dashboard
- expose refreshing state
- receive realtime invalidation
- refresh relevant data
- preserve partial content on section failure where architecture supports

---

# 114. Dashboard Data Model

Use a UI model optimized for rendering.

Do not expose backend DTO directly if UI needs derived labels/status.

---

# 115. Dashboard Realtime

When event arrives:

```text
REPORT_CONFIRMED
```

ViewModel triggers:

```text
refreshDashboard()
```

Do not trust WebSocket event payload as full authoritative dashboard data.

---

# 116. Report History Architecture

Use paging if history can grow.

Potential use:

```text
Paging 3
```

only if project needs justify it.

Simple backend pagination with incremental loading is also acceptable.

Do not add Paging 3 merely because it exists.

---

# 117. History Filters

Store active filters in ViewModel.

Preserve while navigating to detail and back.

---

# 118. Search Debounce

If backend search:

Use debounce around:

```text
300–500ms
```

as an implementation direction.

Cancel stale searches.

---

# 119. Report Detail

Load by ID.

Show confirmed state read-only.

Do not provide edit action unless future correction workflow exists.

---

# 120. Generated Reports

Generated report feature handles:

- list
- detail
- download/cache file
- open
- share
- email

It does not generate Excel/PDF locally.

Backend generates documents.

---

# 121. File Download

Use authenticated HTTP request.

Save file to app-private cache/files or appropriate MediaStore destination if explicit export is implemented.

---

# 122. File Naming

Use server-provided safe filename when available.

Sanitize before local filesystem use.

---

# 123. File MIME Type

Map:

```text
PDF
→ application/pdf

EXCEL
→ application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

---

# 124. FileProvider

Configure provider in Android manifest.

Expose only intended app-private paths.

Do not expose broad root storage.

---

# 125. FileProvider Share

Conceptual:

```kotlin
Intent(Intent.ACTION_SEND).apply {
    type = mimeType
    putExtra(Intent.EXTRA_STREAM, contentUri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
```

Use chooser.

---

# 126. Email Intent

Prefer an email-capable intent/chooser while still respecting Android app availability.

Attach report URI.

Suggested subject/body may be populated.

Do not assume the email was sent after launching external app.

---

# 127. Open File

Use `ACTION_VIEW` with content URI and MIME type.

Handle `ActivityNotFoundException`.

---

# 128. Generated File Cache

Downloaded generated files may be cached.

Define cleanup behavior.

Do not let cache grow forever.

---

# 129. Schedules Architecture

Schedules feature uses backend as authority.

Android does not locally schedule report generation.

Quartz backend owns execution.

---

# 130. Schedule ViewModel

Responsibilities:

- load schedules
- edit configuration
- validate user input
- send update
- show next run from backend

---

# 131. Schedule Timezone

Display backend timezone-aware next run using the canonical business timezone
`Africa/Casablanca`.

Do not independently calculate a different schedule using device timezone unless explicitly intended.

Weekly periods are Monday through Sunday. Monthly schedule editing exposes only the
execution time; the backend always runs on the first day and generates the previous
calendar month. Do not expose a `dayOfMonth` field for MVP.

---

# 132. Notifications Architecture

In-app notification list comes from backend if implemented.

FCM is delivery transport.

---

# 133. FCM Token Registration

On token availability/refresh:

```text
FCM token
→ authenticated backend registration
```

If user not logged in yet:

store token temporarily and register after authentication.

---

# 134. FCM Token Refresh

When Firebase refreshes token:

update backend.

Do not assume token is permanent.

---

# 135. FCM Logout

On logout:

The backend device-token association may be removed/deactivated.

Exact security behavior belongs in backend/API docs.

---

# 136. Foreground FCM

When app active:

Prefer in-app feedback for relevant notifications.

Example:

```text
Daily report generated
[View]
```

---

# 137. Background FCM

System notification.

Deep link to relevant destination.

---

# 138. Notification Deep Link

Use route data:

```text
type
entityId
```

Then fetch authoritative entity.

Do not place full report data in FCM payload.

---

# 139. Notification Permission

On Android versions requiring notification permission:

Ask in context.

Do not request at first launch without explanation.

---

# 140. WebSocket/STOMP Architecture

Android STOMP client is optional/SHOULD.

It receives small invalidation events.

Recommended lifecycle:

```text
authenticated session
→ connect
→ subscribe
→ app background/foreground behavior
→ reconnect if needed
→ disconnect on logout
```

---

# 141. STOMP Authentication

Use access token in connection headers according to backend protocol.

Do not create separate login.

---

# 142. STOMP Reconnection

Use bounded backoff.

Do not retry continuously at full speed.

---

# 143. STOMP Event Handling

Route events to appropriate repository/event bus/app state.

Avoid global mutable singleton event handlers.

---

# 144. STOMP Missed Events

On reconnect:

Refresh key REST state.

---

# 145. App-Wide Event Coordination

A small application event stream may be useful.

Example:

```kotlin
sealed interface AppEvent {
    data class ReportConfirmed(val id: Long) : AppEvent
    data class GeneratedReportReady(val id: Long) : AppEvent
}
```

Keep it minimal.

Do not build a full event bus framework.

---

# 146. Statistics Architecture

Statistics feature requests aggregated backend data.

Android renders.

Do not download all historical KPI values for local calculation.

---

# 147. Charting

Use a maintained Compose-compatible chart library or custom Canvas only if needed.

Dependency must pass project dependency checklist.

Do not introduce a large chart library for one simple line chart without considering complexity.

---

# 148. Chart UI Model

Pre-map:

```text
labels
points
unit
summary
```

outside Composable where useful.

---

# 149. Chart Accessibility

Provide textual summary.

Charts are supplemental.

---

# 150. Loading State Standard

Every async screen defines:

```text
initial loading
refresh loading
action loading
```

where relevant.

Do not use one `isLoading` for unrelated actions if it causes bad UX.

---

# 151. Error State Standard

Differentiate:

```text
network unavailable
server error
validation error
state conflict
authentication error
file error
OCR error
```

UI copy should match.

---

# 152. Empty State Standard

Use reusable FactoryFlow empty-state component.

Each empty state should specify:

- icon
- title
- short body
- primary action if useful

---

# 153. Snackbar Standard

Use Snackbar for:

- draft saved
- entry removed + undo
- schedule updated
- transient non-critical error

Do not use Snackbar for critical confirmation errors that require persistent attention.

---

# 154. Dialog Standard

Use dialogs for:

- destructive actions
- unresolved warning confirmation
- logout with unsaved work

Do not put full forms in dialogs.

---

# 155. Bottom Sheet Standard

Use bottom sheets for:

- KPI selection
- filters
- acquisition option if needed
- contextual generated-file actions

---

# 156. Form Validation

Validate locally for immediate UX.

Backend remains authoritative.

Examples:

- email syntax
- empty password
- empty KPI value
- invalid decimal text
- schedule time/day

---

# 157. Server Validation Mapping

If backend returns field errors:

Map to appropriate UI field.

Do not only show generic top snackbar.

---

# 158. Number Parsing on Android

Accept:

```text
12.5
12,5
```

according to user locale/business rules.

Normalize carefully before API submission.

Do not strip separators blindly.

---

# 159. Number Display

Use locale-aware formatting where appropriate.

Preserve meaningful decimal precision.

---

# 160. Dates

Backend timestamps parsed into modern Kotlin/Java time types where supported.

Display using localized formatters.

Do not pass raw ISO strings into Text.

---

# 161. Timezones

Display reporting/schedule timestamps using agreed application/business timezone.

Do not mix server UTC and local time labels without conversion.

---

# 162. Android Permissions

Potential runtime permissions:

```text
Gallery / Share image
Notifications
```

Broad storage permission should not be required with modern picker/FileProvider approach.

---

# 163. Permission UX

Ask only when feature needs permission.

Explain purpose.

Provide alternatives.

---

# 164. Shared URI Permission Loss

If user permanently denies:

Offer:

```text
Ouvrir les paramètres
```

and alternative acquisition paths.

---

# 165. Notification Denial

App remains usable.

Notifications screen may show a subtle enable prompt.

---

# 166. Image Privacy

Do not upload original images unless a future requirement adds image archival/backend OCR.

The current architecture performs OCR locally.

---

# 167. Temporary Image Cleanup

Delete stale temporary capture/import copies according to safe cleanup policy.

Do not delete while OCR/share flow still depends on them.

---

# 168. Logging

Use Android logging carefully.

Development logs may include:

- route
- status
- high-level request outcome

Never log:

- passwords
- access tokens
- refresh tokens
- sensitive raw messages
- private file URIs unnecessarily

---

# 169. Network Logging

OkHttp logging interceptor may be enabled only in debug builds.

Use BODY logging cautiously because payloads may contain real industrial source text.

Safer default:

```text
BASIC / HEADERS
```

with sensitive headers redacted.

---

# 170. Crash Safety

Do not crash on malformed API data if it can be handled as recoverable error.

Unexpected contract mismatches should fail visibly and be logged.

---

# 171. Strict Nullability

Use Kotlin nullability meaningfully.

Avoid `!!`.

If backend contract says nullable:

handle nullable.

---

# 172. Enum Compatibility

When parsing backend enums:

Unknown future values can crash strict serializers.

Choose serialization strategy carefully.

If robust unknown handling is needed, map to:

```text
UNKNOWN
```

where safe.

Do not silently accept unknown critical business state.

---

# 173. Configuration

Environment-specific Android values:

- backend base URL
- WebSocket URL
- Firebase config
- build type labels

belong in build configuration/resources.

---

# 174. Firebase Configuration

`google-services.json` handling must follow project security/public-repo requirements.

Do not commit unrelated private service-account credentials.

Android Firebase client config is not the same as backend Firebase Admin secret.

---

# 175. Build Types

Recommended:

```text
debug
release
```

Optional:

```text
staging
```

only if useful.

Do not create complicated flavor matrix without need.

---

# 176. Debug Tools

Debug build may include:

- network logs
- mock/demo screen
- environment label

Release should remove unsafe debug behavior.

---

# 177. ProGuard / R8

Release build should use standard optimization/shrinking as compatible.

Keep rules only where libraries require them.

Do not disable shrinking globally because of one issue without investigation.

---

# 178. Min SDK / Target SDK

Select based on:

- modern Android feature requirements
- device availability
- gallery/share URI and OCR API compatibility
- notification permission behavior

Do not invent a device-support requirement without checking actual project needs.

The exact values belong in Gradle.

---

# 179. Dependency Governance

Before adding dependency:

- verify maintenance
- verify license
- verify Compose compatibility
- verify size/complexity
- verify actual need

Do not add multiple libraries solving the same problem.

---

# 180. No Flutter

Do not introduce Flutter modules.

The product is native Kotlin.

---

# 181. No XML Screen Requirement

Compose is primary UI framework.

XML layouts may exist only for unavoidable library/platform integration.

Do not build parallel XML and Compose UI systems.

---

# 182. No LiveData Requirement

Prefer Flow/StateFlow.

Do not introduce LiveData unless integrating with a library that genuinely requires it.

---

# 183. No RxJava Requirement

Coroutines/Flow are the standard async model.

Do not add RxJava.

---

# 184. No Service Locator

Do not use global:

```kotlin
object ServiceLocator
```

for app dependencies when Hilt already solves DI.

---

# 185. No Repository From Composable

Composable must never:

```kotlin
val api = Retrofit...
api.getReports()
```

---

# 186. No Business Rules in Composable

Composable may show warning.

It does not decide authoritative validation semantics.

---

# 187. No Backend Rule Duplication

Android may mirror validation for UX.

Backend remains final authority.

Example:

Android may warn that a KPI is outside expected range.

Backend must still validate business state.

---

# 188. Accessibility

Use:

- semantic labels
- content descriptions
- roles
- state descriptions
- minimum touch targets
- text contrast
- scalable typography

---

# 189. Semantics for KPI Rows

A screen reader should understand something like:

```text
Vrac, 15.8 tonnes, needs review
```

rather than reading five disconnected elements.

---

# 190. Icon Button Semantics

Examples:

```text
Open notifications
Delete draft
Show password
Share PDF
```

---

# 191. Chart Semantics

Provide summary outside chart.

---

# 192. Touch Targets

Aim for:

```text
48dp
```

effective touch area.

---

# 193. System Insets

Use Compose inset APIs.

Respect:

- status bar
- navigation bar
- keyboard
- display cutouts

---

# 194. Keyboard

Use correct keyboard type.

Examples:

```text
Email
Password
Decimal
```

Use IME actions.

---

# 195. Confirmation Keyboard

Sticky bottom actions must not be hidden behind IME.

---

# 196. Scroll Behavior

Use `LazyColumn` for long lists.

Avoid nested vertical scrolling.

---

# 197. Dashboard Scroll

Single vertical scroll hierarchy.

---

# 198. History Scroll

Preserve scroll state when returning from detail if practical.

---

# 199. Performance

Watch recomposition.

Use stable state/models where helpful.

Do not prematurely annotate everything with `@Stable`.

Measure actual problems.

---

# 200. Images

Use efficient decoding.

Do not load a full-resolution shared image into memory for a tiny preview.

---

# 201. Compose Performance

Avoid heavy work inside composition.

Move:

- formatting
- sorting
- mapping
- image processing

outside Composable where appropriate.

---

# 202. Derived State

Use `derivedStateOf` only when it provides real recomposition benefit.

Do not use as decoration.

---

# 203. `remember`

Use `remember` for local UI state.

Do not use it for authoritative data that must survive process recreation.

---

# 204. `rememberSaveable`

Use for lightweight UI state that should survive recreation.

Do not store giant analysis payloads.

---

# 205. ViewModel Scope

Screen-level ViewModel for screen state.

Shared ViewModel only when several destinations genuinely share one workflow.

---

# 206. Acquisition Shared State

The acquisition → confirmation flow may need shared state.

Preferred approaches:

- persist draft/analysis and navigate with ID
- repository-held temporary workflow state
- scoped shared ViewModel

Prefer IDs/server draft over fragile large in-memory transfer when architecture allows.

---

# 207. Analysis Payload

Do not encode entire parser result in route arguments.

Persist temporarily or keep in scoped state.

---

# 208. Process-Safe Acquisition

If OCR finishes then app backgrounds before confirmation:

Preserve enough state to recover.

Draft/local persistence is preferred.

---

# 209. ViewModel Testing

Test:

- initial loading
- success
- network failure
- retry
- user edit
- warning handling
- draft save
- confirmation success
- confirmation conflict

---

# 210. Repository Testing

Mock API/DAO boundaries.

Test mapping and cache policy.

Do not overmock implementation details.

---

# 211. Compose UI Tests

Priority:

- Login
- Paste
- Confirmation
- Draft resume
- Report history
- generated file action
- permission/error states where practical

---

# 212. OCR Integration Tests

Use sample images from sanitized test assets.

Do not depend on private screenshots in public test packages unless anonymized.

---

# 213. Share Intent Tests

Test manually/on emulator/device:

- cold start
- warm start
- authenticated
- unauthenticated
- unsupported MIME
- permission expiry

---

# 214. Gallery and Shared-Image Tests

Test on physical device if possible.

---

# 215. FCM Tests

Test:

- foreground
- background
- terminated app
- token refresh
- tap navigation

---

# 216. FileProvider Tests

Verify:

- PDF open
- Excel open
- share
- email
- URI permission
- no raw path exposure

---

# 217. Navigation Tests

Critical:

```text
Login → Dashboard
Dashboard → Create
Confirm → Detail
Reports → Detail → Back
Notification → Target
Share Intent → Confirmation
```

---

# 218. Dark Mode

If implemented:

test manually.

Do not simply assume theme correctness.

---

# 219. Premium UI QA

For every major screen:

```text
[ ] hierarchy clear
[ ] spacing consistent
[ ] typography consistent
[ ] premium but restrained
[ ] loading state polished
[ ] empty state polished
[ ] error state polished
[ ] warning state polished
[ ] motion subtle
[ ] touch response immediate
[ ] keyboard correct
[ ] accessibility acceptable
```

## 219.1 Vivo real-device before-state baseline

The 12 Vivo screenshots supplied on 13 August 2026 are the explicit BEFORE-state
reference for final Android visual acceptance. The original WhatsApp images remain
outside the repository because they are private QA evidence.

The final Vivo pass must compare the same representative screens and verify:

```text
[ ] no clipped headings, cards, chips, KPI values, or acquisition descriptions
[ ] the canonical four-destination bottom navigation is balanced and legible
[ ] focused workflows and detail screens do not show the bottom navigation
[ ] scroll content remains clear of app navigation and Vivo system navigation
[ ] report lists and KPI rows use compact, value-first hierarchy
[ ] empty states have restrained scale and intentional vertical balance
[ ] dark-theme surfaces, text, status tones, and accent colors retain clear contrast
[ ] screenshots are captured at the Vivo display/font settings used for the baseline
```

The baseline covers Dashboard, recent/history content, confirmed and draft report
detail, Reports, Create Report, Statistics, Notifications, and Profile. Final QA
should preserve business-state differences while comparing layout, hierarchy,
density, clipping, and navigation behavior.

---

# 220. Dashboard Screen Implementation

Compose pieces may include:

```text
DashboardTopBar
TodayReportCard
KpiOverviewSection
QuickActionsSection
NeedsAttentionSection
RecentReportsSection
TrendPreviewCard
UpcomingScheduleCard
```

Do not create one 600-line `DashboardScreen`.

The production Dashboard visual foundation is documented in
`docs/14_Android_Design_System.md`. Its route continues to consume the existing
`DashboardViewModel` and `DashboardDto`; the redesign is presentation-only. The
screen uses the shared Flow components for daily summary cards, quick actions,
list rows, progress, smooth charting, empty state, and the common bottom shell.

The navigation shell presents four destinations around one centered creation
action:

```text
Accueil | Rapports | + | Stats | Alertes
```

The blue creation FAB is a system-level action and is not a fifth labeled tab.
Dashboard previews must wrap the production `DashboardContent` in the production
`FactoryFlowAppShell`; debug fixtures provide data only and must not duplicate UI.

---

# 221. Dashboard Refresh

Use pull-to-refresh if selected.

During refresh:

keep old content visible.

---

# 222. Dashboard Partial Error

If one backend endpoint is consolidated into one response, full dashboard may fail together.

If sections are independently loaded, show section-level failures.

Choose one strategy and keep UX coherent.

---

# 223. Dashboard Realtime Refresh Debounce

If many realtime events arrive rapidly:

coalesce/debounce refresh to avoid request storm.

---

# 224. Login Implementation

ViewModel state:

```text
email
password
isPasswordVisible
isSubmitting
fieldErrors
generalError
```

Use local state for password visibility if preferred.

---

# 225. Login Validation

Client-side:

- non-empty
- valid email form

Server decides credentials.

---

# 226. Login Security

Do not persist password.

Do not log password.

Clear password from state after successful auth.

---

# 227. Reports List Implementation

Use lightweight summary model.

Do not fetch `rawText` for every row.

---

# 228. Reports Detail Implementation

Load full detail only when opened.

---

# 229. Generated Reports List

Show:

- type
- format
- period
- generated time
- status
- email status

---

# 230. Generated File Download State

UiState may include:

```text
Idle
Downloading
Ready(uri)
Error
```

---

# 231. Generated File Open

If cached file exists and is valid:

reuse.

Otherwise download.

---

# 232. File Cache Invalidation

If backend regenerates same generated report ID/path:

respect updated metadata/version if available.

Initial simple implementation may redownload on explicit open.

---

# 233. Schedules Form State

Use typed values.

Do not store day/time as raw unvalidated strings where Material pickers can provide structured data.

---

# 234. Schedule Validation

Android should enforce obvious rules before POST/PUT.

Backend remains authoritative.

---

# 235. Notifications List

Use lazy list.

Unread state is subtle.

Tapping marks read according to chosen behavior then navigates.

---

# 236. Notification Read Timing

Possible:

```text
tap notification
→ mark read
→ navigate
```

or mark once detail opens.

Choose one consistent behavior.

---

# 237. Statistics ViewModel

State includes:

```text
selected KPI
selected period
summary
points
loading
error
```

---

# 238. Statistics Refresh

Changing KPI/period triggers new request.

Cancel stale request.

---

# 239. KPI Definitions Screen

If implemented:

Use list + detail/edit.

Because current role model is single-role, no special admin navigation is required.

---

# 240. KPI Definition Editing

Display warning:

```text
Changes affect future parsing.
Historical confirmed reports remain unchanged.
```

---

# 241. Alias Editing

Use chip-style editor.

Normalize only on backend.

Android may trim obvious whitespace.

---

# 242. Settings

Keep minimal.

Possible:

```text
Theme
Notifications
About
Logout
```

Do not invent settings.

---

# 243. Profile

Show:

```text
Name
Email
Maintenance Engineer
```

No avatar-upload requirement.

---

# 244. App Version

Expose in About.

Useful for support/demo.

---

# 245. Startup Splash

Use Android SplashScreen API where compatible.

No custom 3-second animated splash.

---

# 246. Startup Session Restoration

Do not block excessively.

Attempt refresh if needed.

If backend unavailable and token expired:

navigate according to security policy.

Do not let stale invalid token show official data as current.

---

# 247. Session Expiry During Draft

Preserve local draft.

After re-login, resume if appropriate.

---

# 248. Process-Wide Snackbar

A centralized `SnackbarHostState` may be provided through app scaffold.

Do not use it as a dumping ground for every error.

---

# 249. System UI

Set status/navigation bar behavior to match theme.

Ensure icons have correct light/dark appearance.

---

# 250. Edge-to-Edge

Use modern edge-to-edge behavior if compatible.

Respect insets carefully.

---

# 251. Orientation

Portrait-first is acceptable.

Do not lock orientation unless there is a strong reason.

Ensure state survives configuration changes.

---

# 252. Tablet

Tablet-specific optimized layout is optional.

Base responsive Compose should avoid broken stretched UI.

---

# 253. Screen Size

Design for realistic Android phone widths.

Do not tune only for one emulator.

---

# 254. Animation

Follow `DESIGN.md`.

Typical durations:

```text
120–320ms
```

No dramatic navigation.

---

# 255. Success Animation

Confirmation success may show short checkmark motion.

Do not block navigation for several seconds.

---

# 256. Skeletons

Use reusable skeleton components.

Do not overuse shimmer.

---

# 257. Pull-To-Refresh

Apply where useful:

```text
Dashboard
Reports
Notifications
Statistics
```

---

# 258. Swipe-to-Delete

Drafts may support swipe delete only if visible alternative exists.

---

# 259. Undo

If an action is reversible:

Snackbar Undo is acceptable.

Example:

```text
Remove candidate
```

Do not use Undo for confirmed business deletion.

---

# 260. Accessibility Testing

At least manually review:

- TalkBack navigation
- large font
- contrast
- status semantics
- icon buttons

---

# 261. String Resources

Group by feature or coherent naming.

Example:

```text
auth_login_title
auth_email_label
report_confirm_action
report_warning_low_confidence
```

Avoid generic:

```text
text1
button_ok
```

---

# 262. Resource Naming

Use consistent lowercase underscore.

---

# 263. Test IDs

Compose semantics/test tags may be added for critical elements.

Do not pollute production code with excessive tags where content semantics already suffice.

---

# 264. Analytics

No user analytics platform is required initially.

Do not add Firebase Analytics automatically just because FCM is used.

---

# 265. Crash Reporting

Optional.

If Firebase Crashlytics is considered, evaluate privacy and project need.

Not core.

---

# 266. WorkManager

Do not introduce WorkManager for backend report scheduling.

Quartz handles server schedules.

WorkManager may be appropriate for:

- retrying device token registration
- local background cleanup

only if needed.

---

# 267. AlarmManager

Not required for report scheduling.

---

# 268. Foreground Service

Not required for OCR/report workflows.

Do not add.

---

# 269. Background OCR

Not required.

OCR occurs as part of user flow.

---

# 270. Background File Download

For current report sizes, foreground user-triggered download is likely sufficient.

Use system download manager only if actual requirements justify it.

---

# 271. Notifications Channels

If system notifications are used:

Create appropriate channel(s).

Potential:

```text
Reports
Reminders
Warnings
```

Keep channel count small.

---

# 272. Notification Importance

Do not set every notification to highest priority.

Use importance based on business severity.

---

# 273. Notification Content Privacy

Avoid showing full KPI values in lock-screen notifications unless product explicitly requires it.

---

# 274. Deep Link Route Safety

Validate entity ID from notification before navigation.

If entity missing:

show graceful fallback.

---

# 275. App Links

No web App Link requirement initially.

Internal deep links are enough.

---

# 276. File Sharing Security

Never expose:

```text
file://
```

URIs.

---

# 277. Temporary Read Permission

Grant only for share/view intent.

---

# 278. Share Chooser

Always allow user to choose target app.

---

# 279. Email App Availability

If no compatible email app:

show:

```text
No email app is available on this device.
```

Offer generic Share.

---

# 280. Download Failure

Preserve generated report metadata.

Show retry.

---

# 281. Open Failure

File may still be shared.

Do not mark backend file invalid solely because no viewer installed.

---

# 282. Room Encryption

Not required initially unless sensitive local persistence requires it.

Tokens must still use secure storage.

If raw industrial drafts are stored locally and privacy requirements become stricter, evaluate encrypted storage deliberately.

---

# 283. Sensitive Screenshots

No screenshot-blocking requirement initially.

Do not add `FLAG_SECURE` unless business privacy requirements justify it.

---

# 284. Clipboard Privacy

Do not copy sensitive values to clipboard automatically.

---

# 285. Error Telemetry

If telemetry is later implemented:

Avoid sending raw industrial messages by default.

---

# 286. Android Definition of Done

An Android feature is complete only when:

```text
[ ] follows UI_UX.md
[ ] follows DESIGN.md
[ ] architecture boundaries respected
[ ] ViewModel state explicit
[ ] repository/network integration works
[ ] loading state implemented
[ ] error state implemented
[ ] empty state implemented where relevant
[ ] lifecycle handled
[ ] back behavior correct
[ ] permission behavior correct if relevant
[ ] tests added
[ ] screenshots/evidence captured if valuable
[ ] no secrets/log leaks
[ ] build passes
```

---

# 287. Android Milestone Order

Recommended:

```text
1. App/theme foundation
2. Hilt
3. Retrofit
4. session/auth
5. Navigation shell
6. Login
7. Dashboard baseline
8. KPI definitions
9. Paste acquisition
10. Confirmation
11. Draft persistence
12. Manual entry
13. Reports/history
14. Report detail
15. Generated reports
16. File open/share/email
17. Scheduling
18. Gallery OCR
19. Share Intent
20. Backend OCR
21. Notifications/FCM
22. Realtime/STOMP
23. Statistics
24. Premium polish/hardening
```

---

# 288. Core Android Acceptance Flow

Must work:

```text
Launch
→ Login
→ Dashboard
→ Create
→ Paste
→ Analyze
→ Confirmation
→ Edit
→ Save Draft
→ Resume
→ Confirm
→ Report Detail
→ Generate/Open Excel
```

---

# 289. Premium Android Acceptance Flow

Before presentation:

```text
WhatsApp
→ Share screenshot
→ FactoryFlow
→ OCR
→ Analyze
→ Highlight uncertain KPI
→ Correct
→ Confirm
→ Dashboard updates
→ Generate PDF
→ Share via Android
```

This is the signature FactoryFlow mobile demo.

---

# 290. Android Code Review Checklist

Before committing Android code:

```text
[ ] no Retrofit call from Composable
[ ] no Room call from Composable
[ ] no blocking main-thread work
[ ] no GlobalScope
[ ] no raw tokens in logs
[ ] no hardcoded API base URL
[ ] no raw user-facing strings
[ ] no random hardcoded colors
[ ] no accidental state loss
[ ] no duplicate navigation event
[ ] no permission crash
[ ] no file:// URI
[ ] no fake offline success
```

---

# 291. Android Learning Checklist

The developer should be able to explain:

```text
Why Compose?
Why MVVM?
Why StateFlow?
Why repository?
Why Hilt?
How Retrofit authentication works?
How token refresh works?
Why Room is not authoritative?
How gallery and Share Intent acquisition work?
How PaddleOCR integrates through the backend?
How Share Intent is received?
How FileProvider protects files?
How FCM token registration works?
How STOMP reconnects?
Why confirmation is not optimistic?
```

---

# 292. Final Android Principle

The Android application is the place where FactoryFlow's engineering becomes tangible to the maintenance engineer.

It must make the difficult workflow feel simple:

```text
receive
→ capture
→ review
→ confirm
→ report
```

while keeping all uncertainty visible and all official decisions under human control.

The final application should feel like:

> **a premium native Android productivity tool built for real industrial maintenance work, not a generic student mobile app.**

---

## Delivery Stabilization Notes

The Android file boundaries follow these release rules:

- ContentResolver image reads, generated-document downloads, and user-selected document writes execute on Dispatchers.IO.
- Retrofit error-body parsing also executes away from Main.
- OCR, analysis, draft submission, confirmation, generation, download, and refresh actions guard active jobs to prevent duplicate concurrent requests.
- Share intents are consumed once by SharedAcquisitionStore; recomposition does not repeat acquisition processing.
- The terminal confirmation route clears the completed acquisition/review stack before exposing report and document actions.
- Android app and splash branding use the FactoryFlow identity. Alf Mabrouk artwork remains reserved for backend-generated PDF and Excel documents.

---

# End of 07_Android.md
