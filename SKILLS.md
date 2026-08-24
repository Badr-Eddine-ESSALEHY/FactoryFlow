# SKILLS.md

> **FactoryFlow — Engineering Skills & Implementation Standards**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-11
>
> This document defines **how FactoryFlow should be engineered**.
>
> `AGENTS.md` defines the constitution.
>
> `TASKS.md` defines what to build and in what order.
>
> `SKILLS.md` defines the implementation skills, technical standards, patterns, quality expectations,
> and practical engineering behavior required to execute those tasks correctly.
>
> This file is written primarily for Codex and any future engineer or AI coding agent working in the repository.
>
> The goal is not to force textbook purity.
>
> The goal is to produce code that is clean, explainable, testable, maintainable, secure, and appropriate
> for a real industrial mobile platform developed under a limited implementation window.

---

# 1. How to Use This Document

Before implementing a task:

1. Read `AGENTS.md`.
2. Read the active task in `TASKS.md`.
3. Read the relevant section of this file.
4. Read `DESIGN.md` and `UI_UX.md` for any Android UI/UX work.
5. Read the relevant technical document in `/docs`.
6. Inspect existing code before generating new code.

Do not treat this file as a reason to redesign unrelated working code.

Apply only the skills and rules relevant to the active task.

---

# 2. Engineering Mindset

FactoryFlow should be engineered with four priorities:

```text
Correctness
    ↓
Clarity
    ↓
Maintainability
    ↓
Useful sophistication
```

Sophistication is valuable only when it solves a real problem.

Do not choose a more complex approach merely because it appears more "enterprise."

FactoryFlow should look professionally engineered because its decisions are coherent, not because its dependency list is long.

---

# 3. Business Understanding Is a Technical Skill

The first engineering skill required in FactoryFlow is understanding the real workflow.

The system exists because industrial maintenance KPI information currently arrives through heterogeneous WhatsApp messages and must be manually transferred into reporting files.

Every technical decision must preserve this reality.

Examples:

- Input formats are not stable.
- KPI order is not stable.
- Separators are not stable.
- Decimal formats are not stable.
- Labels may contain typos.
- Some KPIs may be absent.
- An empty value does not mean zero.
- Some reports are partial.
- Automation cannot silently decide official values.
- The maintenance engineer remains the final authority.

When implementation conflicts with this business reality, the implementation is wrong even if it is technically elegant.

---

# 4. Required Skill Domains

FactoryFlow development requires competence in:

```text
Java
Maven
Spring Boot
Spring Security
JWT
Spring Data JPA
PostgreSQL
Flyway
REST APIs
OpenAPI
MapStruct
Apache POI
Apache PDFBox
Quartz
JavaMailSender
WebSocket/STOMP (SHOULD after the trusted core)

Kotlin
Jetpack Compose
MVVM
Repository pattern
Hilt
Retrofit
Room
Coroutines
Flow / StateFlow
Navigation Compose
PaddleOCR backend integration
Android Share Intent
FileProvider
Firebase Cloud Messaging (SHOULD after the trusted core)

Testing
Git
Technical documentation
UML
UI/UX implementation
Performance analysis
Observability
```

RabbitMQ, Resilience4j, Prometheus, Grafana and k6 are later skills and must not displace unfinished core functionality.

---

# 5. Java Skill Standard

Backend code uses a currently supported Java LTS compatible with the selected Spring
Boot version. Java 21 is the preferred current implementation choice, not a business
invariant. The backend build tool is Maven; Android uses Gradle Kotlin DSL.

Prefer:

- immutable data where practical
- clear constructors
- records for simple immutable transport/value objects where appropriate
- enums for finite states
- `Optional` only where it improves API clarity
- modern collection APIs
- `java.time`
- `BigDecimal` where decimal precision matters
- checked/unchecked exceptions deliberately

Avoid:

- legacy date APIs
- raw types
- deeply nested conditionals
- unnecessary mutable shared state
- reflection-heavy custom mechanisms
- generic helper classes that hide domain meaning

---

# 6. Java Naming

Use domain language.

Good:

```java
MaintenanceReport
KpiDefinition
ExtractionResult
ReportGenerationService
ReportStorageService
ReportStatus
AcquisitionMethod
```

Bad:

```java
DataManager
InfoService
MainProcessor
Helper2
Thing
CommonUtil
```

Method names should describe intent.

Good:

```java
analyzeReport()
confirmReport()
findActiveKpiDefinitions()
generateDailyReport()
validatePlausibility()
```

Avoid vague names:

```java
process()
handle()
doStuff()
runLogic()
```

unless the surrounding abstraction makes their meaning obvious.

---

# 7. Method Design

Methods should normally perform one coherent operation.

Prefer early validation/returns over deep nesting.

Bad:

```java
if (...) {
    if (...) {
        if (...) {
            ...
        }
    }
}
```

Prefer:

```java
validateInput();
validateState();
performOperation();
```

Do not split a method into many tiny methods merely to reduce line count.

Extract when the new method has meaningful intent.

---

# 8. Immutability

Prefer immutable request/response/value objects.

Do not expose mutable collections unnecessarily.

For parser output especially, extraction results should behave predictably after creation.

Entity mutability may be required by JPA, but business state changes should still occur through explicit methods or services rather than arbitrary field mutation from controllers.

---

# 9. `BigDecimal` for KPI Values

Do not blindly use `double` for industrial KPI values.

Use `BigDecimal` where decimal representation and reporting accuracy matter.

Examples:

```text
12,5
12.5
0.10
295456
```

must be parsed intentionally.

Never let floating-point formatting create incorrect displayed or exported KPI values.

---

# 10. Java Time

Use:

```java
Instant
LocalDate
LocalDateTime
ZonedDateTime
```

according to domain semantics.

Examples:

- reporting period date → `LocalDate`
- globally meaningful generated-at timestamp → `Instant`
- configured local schedule time → domain-specific local time + timezone strategy

Do not use `java.util.Date` unless required by a library boundary.

---

# 11. Spring Boot Skill Standard

Spring Boot must be used as a framework, not as an excuse to mix concerns.

Preferred request path:

```text
HTTP Request
    ↓
Controller
    ↓
Application / Service
    ↓
Repository / Infrastructure
    ↓
PostgreSQL
```

Controllers remain thin.

Application services coordinate use cases.

Repositories persist/retrieve.

Infrastructure adapters handle external concerns.

Organize the backend by feature (`auth`, `report`, `kpi`, `parser`, `generation`,
`schedule`, `notification`). Use internal `api/`, `application/`, `domain/`, and
`persistence/` packages only where useful; do not force empty layers into trivial features.

---

# 12. Dependency Injection

Use constructor injection.

Preferred:

```java
@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }
}
```

If Lombok is later deliberately introduced, constructor generation may be used consistently, but do not add Lombok only to save a few lines.

Do not use field injection.

Avoid:

```java
@Autowired
private ReportRepository reportRepository;
```

Do not manually instantiate Spring-managed services with `new`.

---

# 13. Interface Discipline

Use interfaces where they provide an actual architectural boundary.

Strong candidates:

```text
ReportStorageService
ReportGenerator
NotificationGateway
EmailGateway
```

Do not mechanically create:

```text
UserService
UserServiceImpl
```

when only one implementation exists and no meaningful abstraction boundary is gained.

Interfaces are architectural tools, not decoration.

---

# 14. Controller Skill Standard

Controllers should:

- receive requests
- trigger Jakarta Bean Validation
- invoke use-case/service logic
- map domain results to DTOs
- return correct HTTP semantics

Controllers should not:

- calculate report statistics
- parse KPI text
- query repositories directly
- generate files
- send mail
- contain transaction logic
- implement JWT internals
- decide report validity

If a controller starts becoming long, move business decisions downward rather than extracting private controller helper methods indefinitely.

---

# 15. Service Skill Standard

A service should represent a meaningful application capability.

Good examples:

```text
AuthenticationService
ReportAnalysisService
ReportConfirmationService
ReportGenerationService
KpiDefinitionService
StatisticsService
ScheduleService
EmailDeliveryService
```

Do not create one giant `ReportService` if analysis, confirmation, generation, querying and scheduling become independently complex.

At the same time, do not split the project into dozens of one-method services before complexity exists.

Refactor based on cohesion.

---

# 16. Repository Skill Standard

Use Spring Data JPA for normal persistence.

Repositories should expose domain-meaningful queries.

Examples:

```java
findByEmailIgnoreCase(...)
findByActiveTrue(...)
findBySubmittedAtBetween(...)
findByReportId(...)
```

Avoid leaking repository behavior into Android-facing API semantics.

Do not put business policy into JPQL because it is convenient.

---

# 17. JPA Entity Skill Standard

Entities should model persistent business state.

Rules:

- avoid exposing entities directly through REST
- use explicit relationships
- avoid accidental eager graph loading
- understand owning side of relationships
- avoid circular JSON serialization
- use database constraints
- preserve historical referential integrity

Be particularly careful with:

```text
MaintenanceReport → KPIEntry
KPIEntry → KPIDefinition
MaintenanceReport → User
GeneratedReport → User
```

Do not use bidirectional relationships unless they provide real value.

---

# 18. Fetch Strategy

Default to deliberate loading.

Watch for:

- N+1 queries
- `LazyInitializationException`
- huge eager graphs

Prefer:

- fetch joins where appropriate
- projections for dashboard/statistics endpoints
- pagination for history

Do not solve every lazy-loading issue by making everything `EAGER`.

---

# 19. Transaction Skill Standard

Use transactions around meaningful business state changes.

Example:

```text
Confirm maintenance report
    ↓
Persist report status
    ↓
Persist final KPI entries
    ↓
Persist audit information
```

These writes may need one transaction.

Do not keep transactions open while:

- sending email
- waiting for FCM
- performing unrelated external calls
- doing long file I/O

Persist state, commit, then perform external work where the business flow allows it.

---

# 20. DTO Skill Standard

Use request/response DTOs rather than exposing entities.

Examples:

```text
LoginRequest
LoginResponse
AnalyzeReportRequest
AnalyzeReportResponse
ExtractionResultDto
ConfirmReportRequest
ReportSummaryResponse
DashboardResponse
```

DTOs should be shaped for API consumers.

Entities should be shaped for persistence/domain behavior.

Do not force one model to serve both jobs.

---

# 21. MapStruct Skill Standard

Use MapStruct when mapping becomes repetitive or multi-field.

Good use:

```text
MaintenanceReport → ReportSummaryResponse
GeneratedReport → GeneratedReportDto
```

Do not use MapStruct for a mapping that is clearer as:

```java
return new LoginResponse(accessToken, refreshToken);
```

Tooling should reduce noise, not hide logic.

MapStruct is selective. It is never required for every mapping.

---

# 22. Validation Skill Standard

Use Jakarta Bean Validation for structural request validation.

Examples:

```java
@NotBlank
@Email
@NotNull
@Positive
@Size
```

But business validation belongs in services/domain logic.

Example:

```text
"final KPI value must correspond to a known active definition"
```

is not merely a DTO annotation problem.

Android validation improves usability.

Backend validation remains authoritative.

---

# 23. Error Handling

Use centralized Spring error translation.

Prefer:

```text
Domain/Application exception
        ↓
Global exception handler
        ↓
Stable API error response
```

Do not place repetitive `try/catch` logic in controllers.

Errors should have stable machine-readable codes where useful.

Examples:

```text
AUTH_INVALID_CREDENTIALS
REPORT_NOT_FOUND
REPORT_ALREADY_CONFIRMED
KPI_DEFINITION_NOT_FOUND
REPORT_VALIDATION_FAILED
REPORT_GENERATION_FAILED
```

Do not expose Java stack traces to Android.

---

# 24. REST API Skill Standard

Design APIs around resources and business capabilities.

Use consistent routes.

Avoid route naming drift such as:

```text
/api/report
/api/reports
/api/reporting
/api/getReports
```

Pick one convention and preserve it.

The detailed contract belongs in `docs/06_API.md`.

---

# 25. API Versioning

Do not add versioning prematurely just to look enterprise.

If only one application client exists during initial development, a stable `/api/...` contract is acceptable.

Introduce explicit versioning such as:

```text
/api/v1/...
```

only when there is a concrete compatibility need or when the API specification intentionally standardizes it from the beginning.

Whichever convention is chosen in `docs/06_API.md`, backend and Android must use it consistently.

---

# 26. HTTP Semantics

Use correct status codes.

Examples:

```text
POST create resource       → 201
GET success                → 200
successful no-content op   → 204
invalid request            → 400
unauthenticated            → 401
forbidden                  → 403
not found                  → 404
state conflict             → 409
semantic validation issue  → 422 when appropriate
```

Do not return `200` with `{ "success": false }` for every failure.

---

# 27. Pagination and Filtering

History endpoints may grow.

Use pagination where needed.

Filters should reflect real user search behavior:

```text
date range
report type
submitter
status
KPI
```

Do not create an over-general dynamic query framework before these needs exist.

---

# 28. OpenAPI Skill Standard

Swagger/OpenAPI should stay synchronized with the API.

Document:

- endpoint purpose
- authentication requirements
- request schema
- response schema
- errors
- filtering/pagination parameters

Do not rely on method names alone as API documentation.

---

# 29. Spring Security Skill Standard

Security architecture must remain understandable.

Primary flow:

```text
Login credentials
      ↓
Authentication
      ↓
Access token
      ↓
Protected API request
      ↓
JWT validation
      ↓
Authenticated user context
```

Keep security configuration focused.

Do not create role rules when the application does not have roles.

---

# 30. Password Security

Use BCrypt.

Never:

- store plaintext password
- return password hash
- log credentials
- seed public repositories with real passwords

Demo credentials must be clearly non-production and safe.

---

# 31. JWT Skill Standard

Access tokens should contain only required claims.

Do not place large user profiles or sensitive business information inside JWTs.

Validate:

- signature
- expiry
- expected token type where needed

Use externalized signing secrets.

---

# 32. Refresh Tokens

Refresh tokens should have a defined lifecycle.

The implementation must decide and document:

- storage
- expiration
- revocation behavior
- rotation behavior
- logout behavior

Do not implement a refresh endpoint that accepts an unlimited permanent token.

---

# 33. PostgreSQL Skill Standard

Model the business rather than the UI.

Core principle:

```text
MaintenanceReport
    has many
KPIEntry
    references
KPIDefinition
```

Do not add one database column per KPI.

The KPI catalog must remain configurable.

---

# 34. Database Constraints

Use the database to protect fundamental invariants.

Examples:

```text
unique user email
unique KPI code
foreign keys
non-null relationships where required
```

Do not rely only on UI validation.

---

# 35. Flyway Skill Standard

Every schema evolution must be a migration.

Good:

```text
V1__create_initial_schema.sql
V2__add_refresh_tokens.sql
V3__create_report_schedules.sql
```

Never edit an already-applied migration to make current development convenient.

Create the next migration.

Ensure a clean database can rebuild from migration history.

---

# 36. Indexing

Index according to access patterns.

Likely useful columns:

```text
maintenance_reports.submitted_at
maintenance_reports.effective_date
maintenance_reports.submitted_by
maintenance_reports.status
kpi_entries.kpi_definition_id
generated_reports.generated_at
generated_reports.type
```

Do not index every column.

`effective_date` is the business date and is distinct from `submitted_at` and
`confirmed_at`; multiple reports may share it. Persisted report states are exactly
`DRAFT`, `PENDING_REVIEW`, and `CONFIRMED`. Do not persist a separate per-KPI review
state machine for MVP.

Measure/query-plan if performance becomes relevant.

---

# 37. Auditability Skill Standard

Auditability matters because the project handles validated industrial information.

Important events may include:

```text
report created
draft saved
report confirmed
KPI corrected
report generated
schedule changed
```

The final audit model must be proportionate to the project.

Do not build a full compliance platform.

But do not lose the distinction between:

```text
extracted value
```

and:

```text
user-confirmed value
```

---

# 38. Parser Engineering Skill Standard

The parser is the highest-priority correctness component.

It must be:

- deterministic
- explainable
- modular
- testable
- configuration-driven
- tolerant of realistic formatting differences

It must not become one giant regular expression.

---

# 39. Parser Pipeline Design

Implement distinct conceptual stages:

```text
Raw input
   ↓
Normalization
   ↓
Line/segment interpretation
   ↓
Label recognition
   ↓
Value extraction
   ↓
Unit interpretation if present
   ↓
Confidence / warning evaluation
   ↓
Extraction result
```

Each stage should be testable independently where useful.

---

# 40. Input Normalization

Normalization may handle:

- trimming
- whitespace collapse
- line endings
- case normalization for matching
- Unicode punctuation variants
- known separator forms

Never mutate the preserved original source.

Maintain:

```text
rawText
```

separately from:

```text
normalizedText
```

when useful.

---

# 41. Label Matching Strategy

Recommended priority:

```text
1. Exact canonical label
2. Exact alias
3. Normalized canonical/alias
4. Deterministic fuzzy match
5. Unknown
```

Exact matches should outrank fuzzy matches.

Do not fuzzy-match everything immediately.

This reduces false positives.

---

# 42. Fuzzy Matching Skill

A deterministic similarity algorithm such as Levenshtein distance is acceptable.

Threshold must be configurable.

Do not hardcode:

```java
if (similarity > 0.75)
```

across multiple classes.

Centralize parser configuration.

Return enough information to debug why a label matched.

---

# 43. Numeric Extraction

Support business-realistic formats.

Examples:

```text
295456
12.5
12,5
1 250
```

according to agreed parsing rules.

Be cautious with:

```text
1,234
```

because comma may mean decimal or thousands separator depending on context.

When ambiguity cannot be resolved deterministically, warn rather than pretend certainty.

---

# 44. Missing Values

Missing is a first-class state.

Never convert:

```text
""
null source field
missing line
```

to:

```text
0
```

unless explicit business rules define that behavior.

A report can be partial.

---

# 45. Duplicate Labels

If the same KPI appears more than once in one source:

Do not silently choose one value.

Possible behavior:

- preserve both candidates
- create warning
- require human decision

The exact rule belongs in `docs/04_Business_Rules.md`.

---

# 46. Confidence Skill Standard

Confidence is a user aid.

It may incorporate:

- exact vs fuzzy match
- similarity score
- numeric extraction quality
- unit compatibility
- plausible range
- parsing ambiguity

Confidence must never become an auto-confirmation rule.

---

# 47. Plausibility Validation

Use configured:

```text
plausible_min
plausible_max
```

to generate warnings.

Out-of-range does not automatically mean incorrect.

Do not overwrite.

Do not reject automatically unless a future explicit business rule requires it.

---

# 48. Unrecognized Content

Every unrecognized line should remain visible in the analysis result when practical.

Do not silently throw away information.

This is especially important for detecting:

- new KPI vocabulary
- spelling changes
- parser regressions
- unusual message formats

Persisted drafts and confirmed traceability use `UNRESOLVED`, `ASSIGNED`, or `IGNORED`
resolution. One source line may produce zero, one, or multiple KPI candidates.

---

# 49. Parser Testing Skill

Maintain a regression suite built from anonymized real variations.

Every fixed real parsing bug should add a test where practical.

Minimum test categories:

```text
different order
WhatsApp UI/OCR noise
multiple visible message bubbles
--- / ---- missing markers
colon
equals
arrow
whitespace separator
decimal comma
decimal point
decimal/thousands ambiguity (30.197 vs 30197)
missing value
partial report
alias
typo
fuzzy typo
unknown line
duplicate label
invalid number
unit beside number
multiple measurements in one line
out-of-range value
```

One screenshot remains one review flow in MVP; OCR must not automatically split
visible message bubbles into multiple maintenance reports.

Parser tests should be deterministic and fast.

---

# 50. Human Confirmation Skill Standard

The validation workflow must preserve:

```text
source
extracted candidate
warning/confidence
user edit
final authoritative value
```

Do not update `extracted_value` to equal the edited value and lose the distinction.

The final confirmed value is authoritative for:

- dashboard
- history
- statistics
- generated reports

---

# 51. Manual Entry Skill

Manual entry should reuse as much validation/persistence behavior as possible.

Do not create a second independent report system for manual input.

Manual entry may bypass OCR/parser stages.

It must not bypass:

- plausibility warning
- review
- persistence
- audit rules

---

# 52. Report Generation Skill Standard

Generated Excel/PDF files are product outputs.

Their code should be separated from:

- controller logic
- persistence logic
- Android UI logic

Prefer a clear generation boundary.

Conceptually:

```text
ReportGenerationService
    ↓
ExcelReportGenerator
    ↓
Apache POI
```

and:

```text
ReportGenerationService
    ↓
PdfReportGenerator
    ↓
PDFBox
```

The canonical API is period-based `POST /api/generated-reports`, and core generation
is synchronous. Intentional regeneration creates a new version with provenance.
Generation status and email-delivery status are separate. Async `PENDING` /
`GENERATING` behavior requires an explicit future contract change.

---

# 53. Apache POI Skill Standard

Excel output should be professional.

At minimum consider:

- title
- reporting period
- generated timestamp
- clear column headings
- KPI names
- values
- units
- readable widths
- freeze pane if useful
- consistent number formatting
- clear grouping
- summary rows where meaningful

Do not over-style.

Industrial reports should remain readable and practical.

---

# 54. Apache POI Performance

Avoid repeatedly creating thousands of duplicate style objects.

Reuse styles.

Close workbooks/streams correctly.

For the expected FactoryFlow data volume, regular `XSSFWorkbook` is likely sufficient.

Do not introduce streaming workbook APIs unless file size genuinely requires them.

---

# 55. PDFBox Skill Standard

PDF output should be deterministic and readable.

Handle:

- page margins
- line wrapping
- table pagination
- headers
- footers/page numbers where useful
- fonts available to the application environment
- numeric formatting
- reporting period

Do not depend on proprietary font files committed illegally.

---

# 56. Generated File Storage

Code that generates files should not decide final absolute storage paths itself.

Use the approved abstraction:

```text
ReportStorageService
```

Initial implementation:

```text
LocalReportStorageService
```

This enables future S3-compatible storage without changing report business logic.

Do not add MinIO now.

---

# 57. File Safety

Generated filenames should be predictable and safe.

Never concatenate arbitrary user-provided path segments.

Sanitize names where needed.

Prevent path traversal.

Close streams.

Handle cleanup of failed partial generation.

---

# 58. Quartz Skill Standard

Quartz owns scheduling, not business logic.

Good:

```text
Quartz Job
    ↓
ReportGenerationService.generateScheduledReport(...)
```

Bad:

```text
Quartz Job
    contains 300 lines of
    query + Excel + PDF + SMTP logic
```

Jobs should be thin orchestrators.

---

# 59. Report Period Semantics

Define reporting windows explicitly.

Examples:

```text
Daily   → calendar day
Weekly  → Monday through Sunday calendar week
Monthly → calendar month
```

Do not casually implement weekly as "now minus 7 days" unless that is explicitly the business definition.

The business timezone is `Africa/Casablanca`. Monthly scheduled generation runs at
the configured time on the first day of a month for the complete previous calendar
month. Allow one Quartz recovery execution after one missed run and deduplicate by
schedule + period + format.

---

# 60. Scheduling Failure Handling

Track outcomes separately.

Possible states:

```text
generation succeeded
storage succeeded
email failed
notification succeeded
```

One infrastructure failure should not erase already successful work.

---

# 61. JavaMailSender Skill Standard

Scheduled automatic email belongs to backend infrastructure.

Use externalized SMTP configuration.

Do not:

- commit credentials
- hardcode recipients
- couple file generation directly to mail implementation

Prefer:

```text
Schedule
    ↓
Generate report
    ↓
Store
    ↓
EmailDeliveryService
```

---

# 62. Android User Email Sharing

User-initiated email is a different skill.

Use Android sharing.

Concept:

```text
Generated report
    ↓
FileProvider content URI
    ↓
ACTION_SEND
    ↓
Email-capable app
```

The user remains in control of recipient and final send action.

Do not send SMTP mail from the Android app.

---

# 63. WebSocket/STOMP Skill Standard

Use WebSocket/STOMP only for genuinely real-time events.

Good use:

```text
report confirmed
generated report ready
notification created
dashboard-relevant change
```

REST/database remains authoritative.

Prefer lightweight event messages.

Example:

```json
{
  "type": "REPORT_CONFIRMED",
  "reportId": 123
}
```

Then Android can fetch updated authoritative data.

Do not replicate entire application state over STOMP.

---

# 64. WebSocket Reconnection

Android real-time code must tolerate disconnects.

Handle:

- application backgrounding
- connectivity loss
- server restart
- token expiry
- reconnection

Missing an event must not permanently corrupt UI state.

REST refresh remains recovery mechanism.

---

# 65. RabbitMQ Skill Standard

RabbitMQ is optional/late.

Do not add it until the core product works.

If implemented, it must solve a deliberate asynchronous boundary.

Possible candidate:

```text
report generation request
    ↓
queue
    ↓
consumer
    ↓
generate report
```

Do not send every internal event through RabbitMQ.

---

# 66. RabbitMQ Reliability Skills

If used, understand:

- producer confirms where relevant
- consumer acknowledgements
- duplicate delivery
- idempotency
- retry
- dead-letter queues
- message schema
- failure visibility

Do not claim exactly-once behavior without implementing business-level idempotency.

---

# 67. Resilience4j Skill Standard

Use only around true failure boundaries.

Potential:

- SMTP
- FCM integration
- messaging publishing

Patterns may include:

```text
retry
circuit breaker
time limiter
```

Do not decorate ordinary service-to-service calls inside the same JVM.

Javadoc/KDoc is required for non-obvious public contracts and important business
behavior, not for every trivial method, getter, or self-explanatory implementation.

---

# 68. Kotlin Skill Standard

Android uses idiomatic Kotlin.

Prefer:

- immutable `val`
- data classes
- sealed interfaces/classes for state
- null safety
- extension functions only when they improve readability
- coroutines
- structured concurrency
- Flow/StateFlow

Avoid:

- Java-style mutable boilerplate
- `!!` unless logically guaranteed and justified
- global mutable state
- blocking network/database calls on main thread

All Android user-facing strings live in resources and use professional French.
Code identifiers, packages/classes, API/database contracts, Git commits, and technical
documentation remain in English.

---

# 69. Android Architecture

Preferred flow:

```text
Composable
    ↓
ViewModel
    ↓
Repository
    ↓
RemoteDataSource / LocalDataSource
```

UI observes state.

UI sends user intents/events.

ViewModel coordinates.

Repository abstracts data access.

Do not allow Composables to directly call Retrofit or Room DAOs.

---

# 70. Hilt Skill Standard

Use Hilt for Android dependency injection.

Inject:

- repositories
- Retrofit services
- Room database/DAOs
- data sources
- use cases when introduced

Do not use Hilt to hide dependencies.

A ViewModel should still clearly declare what it needs.

Avoid service-locator style access to application objects.

---

# 71. Compose Skill Standard

Compose should be declarative.

Composable functions should:

- render state
- emit events
- remain as stateless as practical
- avoid business logic

Preferred:

```kotlin
@Composable
fun ConfirmationScreen(
    state: ConfirmationUiState,
    onEvent: (ConfirmationEvent) -> Unit
)
```

The screen does not decide whether a KPI may become authoritative.

Backend/domain logic owns that rule.

---

# 72. Compose State Hoisting

Hoist state when:

- multiple child components need it
- parent coordinates behavior
- state belongs to screen logic

Keep purely local transient UI state local when appropriate.

Do not move every boolean into ViewModel automatically.

Examples of local state:

```text
expanded dropdown
temporary dialog visibility
local animation state
```

Examples of ViewModel state:

```text
loaded report
confirmation edits
network status
submission state
```

---

# 73. UI State Modeling

Prefer explicit state.

Example:

```kotlin
sealed interface ReportHistoryUiState {
    data object Loading : ReportHistoryUiState
    data class Success(val reports: List<ReportSummary>) : ReportHistoryUiState
    data object Empty : ReportHistoryUiState
    data class Error(val message: UiMessage) : ReportHistoryUiState
}
```

Avoid contradictory booleans.

---

# 74. One-Off UI Events

Navigation, snackbars and similar one-time events should not be modeled as permanently true state that replays incorrectly.

Use an appropriate event mechanism.

Keep event handling lifecycle-aware.

Do not create event wrappers copied from old Android patterns without understanding them.

---

# 75. Coroutines

Use structured concurrency.

ViewModels should use:

```kotlin
viewModelScope
```

for UI-related async work.

Repositories/data sources may switch dispatchers where needed.

Do not create uncontrolled `GlobalScope`.

Do not block main thread.

---

# 76. Flow / StateFlow

Use `StateFlow` for observable state where appropriate.

Combine streams deliberately.

Avoid creating a huge reactive graph for simple request/response screens.

Clarity first.

---

# 77. Retrofit Skill Standard

Define typed interfaces.

Example:

```kotlin
interface ReportsApi {
    @POST("api/reports/analyze")
    suspend fun analyze(@Body request: AnalyzeReportRequest): AnalyzeReportResponse
}
```

Do not expose Retrofit response mechanics throughout the UI.

Translate network failures in the data/repository boundary.

---

# 78. API Models on Android

Separate network DTOs from UI/domain models when the distinction provides value.

Do not create four layers of identical data classes merely to claim Clean Architecture.

For stable simple models, pragmatic mapping is acceptable.

For business-critical objects where backend DTO shape should not leak into UI, use explicit mapping.

---

# 79. Room Skill Standard

Room provides local persistence/cache where useful.

PostgreSQL remains authoritative.

Good Room candidates:

- cached report summaries
- locally preserved draft support if architecture requires it
- cached KPI definitions
- user-visible data needed during short network interruptions

Do not attempt full offline-first synchronization unless explicitly scoped.

---

# 80. Cache Rules

Every cache requires answers to:

```text
What is cached?
How long?
What invalidates it?
What is authoritative?
What happens offline?
```

If these cannot be answered, do not build a broad cache yet.

---

# 81. Navigation Compose Skill Standard

Centralize route definitions.

Prefer typed/navigation-safe patterns available in the selected Compose Navigation version where practical.

Do not scatter strings:

```kotlin
navController.navigate("reportDetails/${report.id}")
```

through many Composables.

Navigation rules belong in `UI_UX.md`.

---

# 82. Back Navigation

Back behavior is part of UX correctness.

Special care:

- login success
- confirmation complete
- draft save
- notification deep links
- external Share Intent entry
- gallery/share OCR flow

Do not allow terminal workflows to recreate invalid screens through back stack.

---

# 83. Android Share Intent Inbound

FactoryFlow must correctly receive images shared from WhatsApp or other Android apps.

Understand:

- intent filters
- MIME types
- content URIs
- URI permission grants
- lifecycle when app is cold-started
- lifecycle when app is already running

Do not assume the shared image has a normal filesystem path.

---

# 84. FileProvider Outbound

For generated file sharing:

Use:

- `FileProvider`
- content URI
- read permission flag
- correct MIME type

Never expose:

```text
file:///private/path/report.pdf
```

to external applications.

---

# 85. Image Acquisition Skill Standard

Images are acquired through gallery selection or Android Share Intent. Direct camera acquisition is intentionally unsupported.

Required concerns:

- permission
- lifecycle binding
- capture
- file/URI handling
- rotation/orientation
- failure state
- transition to OCR

Keep the workflow short.

---

# 86. Gallery Selection

Use modern Activity Result APIs.

Do not depend on deprecated storage-selection patterns.

Prefer platform pickers where appropriate.

Handle cancellation gracefully.

---

# 87. PaddleOCR Skill Standard

OCR runs in the private FactoryFlow PaddleOCR runtime behind the backend `OcrProvider` contract.

Responsibilities:

```text
image
    ↓
text recognition
    ↓
raw OCR text
```

OCR must not contain KPI business matching.

OCR output goes into the same parser pipeline as pasted text.

Do not duplicate parser logic in Android.

---

# 88. OCR UX

OCR is not instant in every case.

UI should provide:

- processing state
- clear failure state
- retry
- fallback to manual/paste where reasonable
- preview/source context if defined by `UI_UX.md`

Do not silently navigate to an empty confirmation screen after OCR failure.

---

# 89. OCR Test Strategy

Automated OCR testing may be limited by platform/library constraints.

Use:

- unit tests for text-to-parser stages
- controlled sample images
- manual/device tests for OCR integration
- regression samples for important failure cases

Do not pretend parser tests prove OCR quality.

---

# 90. Firebase Cloud Messaging Skill Standard

FCM is a notification transport.

Backend business state remains authoritative.

Implement:

- device token registration
- token refresh
- safe message payload
- foreground handling
- background handling
- navigation from notification where useful

Do not place sensitive detailed industrial data in notification payloads unnecessarily.

---

# 91. Material 3 Skill Standard

Use Material 3 behavior correctly.

FactoryFlow's visual identity may customize:

- color
- shape
- typography
- spacing
- component appearance

But should preserve Android-native usability for:

- navigation
- system bars
- back handling
- dialogs
- sheets
- touch states
- accessibility

`DESIGN.md` is authoritative for appearance.

`UI_UX.md` is authoritative for behavior.

---

# 92. Responsive Android Layout

Support realistic phone sizes.

Avoid hardcoding layout assumptions to one emulator resolution.

Use:

- adaptive constraints
- appropriate max widths
- scrolling
- safe insets
- keyboard handling

Tablet-specific layouts are optional unless explicitly scoped.

---

# 93. Accessibility Skill Standard

For user-facing UI:

- sufficient contrast
- meaningful semantics/content descriptions
- touch target size
- readable text
- no status communicated only by color
- error messages that identify the problem

Accessibility is part of Definition of Done.

---

# 94. Motion Skill Standard

Motion should communicate state.

Use normal Material easing by default. Use springs only when they genuinely improve
direct manipulation or subtle interaction feedback.

Good:

- screen transition
- expanded card
- successful confirmation
- loading progression
- warning emphasis

Bad:

- permanent bouncing
- large ornamental animations
- slow transitions
- unnecessary parallax

Industrial software should feel calm and fast.

---

# 95. Loading States

Every network or processing feature must define loading behavior.

Examples:

```text
Login
Parser analysis
OCR
History
Dashboard
PDF generation
Excel generation
```

Avoid blocking the entire application if only one section is loading.

---

# 96. Empty States

Empty is not error.

Examples:

```text
No reports today
No generated documents
No notifications
No history results for selected filters
```

Provide next action where useful.

Exact copy belongs in `UI_UX.md`.

---

# 97. Error UX

Translate infrastructure errors.

Bad:

```text
java.net.ConnectException
```

Good:

```text
FactoryFlow could not reach the server.
Check your connection and try again.
```

Preserve technical details in logs.

Do not expose stack traces.

---

# 98. Draft UX Skill

Drafts exist because mobile work is interruptible.

Preserve progress when practical.

Think about:

- app background
- process recreation
- navigation away
- explicit save
- network loss

Do not allow a long validation session to disappear unnecessarily.

---

# 99. Testing Philosophy

Test according to business risk.

Priority:

```text
1. Parser
2. Human confirmation / data integrity
3. Authentication
4. Report generation
5. Scheduling
6. API integration
7. Android ViewModel/state behavior
8. Critical Compose flows
9. Decorative details
```

Do not optimize for coverage percentage alone.

---

# 100. Backend Unit Testing

Use JUnit.

Use Mockito when isolation is useful.

Prefer real values and behavior-focused tests.

Bad test:

```text
verify method X called exactly once
```

when the real requirement is:

```text
confirmed report persists corrected final KPI value
```

Test observable behavior.

---

# 101. Spring Integration Testing

Use Spring integration tests for boundaries such as:

- authentication
- repository mappings
- controller/API behavior
- transaction behavior
- Flyway startup

Do not turn every unit test into a full Spring context test.

---

# 102. Database Integration Tests

If a suitable test PostgreSQL strategy is established, prefer testing PostgreSQL-specific behavior against PostgreSQL rather than assuming an in-memory database behaves identically.

If using a lighter test database for speed, document the limitation.

Do not claim production-equivalent persistence testing when it is not.

---

# 103. Android ViewModel Tests

Test:

- loading → success
- loading → error
- user edits
- confirmation state
- draft state
- retry
- navigation-trigger state when applicable

ViewModel tests should not require real Android UI where avoidable.

---

# 104. Compose UI Tests

Prioritize critical interactions:

- login
- confirmation
- report acquisition
- important navigation

Do not spend large amounts of sprint time testing every decorative component.

---

# 105. End-to-End Testing

At milestone gates, test complete workflows.

Core:

```text
Login
→ Paste
→ Analyze
→ Correct
→ Draft
→ Resume
→ Confirm
→ Dashboard
→ Generate Excel/PDF
→ Share
```

OCR:

```text
Share image
→ OCR
→ Analyze
→ Confirm
```

Scheduled:

```text
Quartz
→ Generate
→ Store
→ Email
→ Notify
```

---

# 106. Test Data

Use anonymized realistic examples.

Do not fill tests only with:

```text
foo
bar
123
```

when real formatting complexity is central to the feature.

Parser tests especially should resemble real input structure.

---

# 107. Regression Testing

When a bug is fixed:

1. reproduce it
2. write a failing test when practical
3. fix it
4. keep the regression test

Do this especially for parser bugs and data-integrity bugs.

---

# 108. Performance Skill Standard

Do not optimize before measuring.

Potential bottlenecks:

- parser analysis
- history queries
- dashboard aggregation
- report generation
- large file handling

Use appropriate measurement before changing architecture.

---

# 109. k6 Skill Standard

If performance testing milestone is reached:

Use realistic workflows.

Measure:

- latency
- request rate
- error rate
- throughput

Do not create unrealistic 100,000-user scenarios for a 2–4 user initial deployment simply to produce impressive graphs.

A reasonable load test can still demonstrate engineering maturity.

---

# 110. Observability Skill Standard

If monitoring is implemented:

Use metrics that answer useful questions.

Examples:

```text
API latency
error count
parser analysis duration
report generation duration
JVM memory
DB connection pool
WebSocket connections
```

Do not build Grafana dashboards with unrelated metrics for appearance.

---

# 111. Logging Skill Standard

Use structured, meaningful logs.

Examples:

```text
INFO  report confirmed reportId=...
WARN  suspicious KPI value reportId=... kpiCode=...
ERROR scheduled email failed generatedReportId=...
```

Do not log:

- passwords
- JWTs
- SMTP credentials
- private source messages unnecessarily
- full sensitive payloads

---

# 112. Git Skill Standard

Each coherent subtask should produce a focused Conventional Commit when the session workflow calls for committing.

Examples:

```text
feat(parser): add deterministic fuzzy KPI matching
feat(android): build report confirmation workflow
test(parser): cover decimal and separator variations
docs(api): document confirmation contract
```

Never use:

```text
update
final
changes
fix stuff
```

Do not push or force-push unless explicitly instructed.

---

# 113. Diff Review Skill

Before completion:

```text
git status
git diff
```

Check for:

- accidental unrelated edits
- secrets
- generated files
- debug logs
- formatting explosions
- abandoned code
- test changes hiding failures

AI agents must inspect their own changes.

---

# 114. Refactoring Skill

Refactor for a concrete reason.

Good reasons:

- duplicated parser logic
- untestable service
- God class
- confusing API model
- repeated mapping
- UI component too large to reason about

Bad reason:

> "This architecture pattern is more fashionable."

Preserve behavior.

Use tests when refactoring critical paths.

---

# 115. Documentation Skill Standard

Documentation should preserve decisions that code cannot explain alone.

Especially:

- why deterministic parser
- why human confirmation
- why native Android
- why PDFBox
- why Quartz
- why local storage abstraction
- why backend email and Android email are separate
- why RabbitMQ is optional

Update docs when behavior changes.

Do not let chat conversations become the only place where decisions exist.

---

# 116. UML Skill Standard

Use UML to communicate architecture/business structure, not to document every framework class.

For report diagrams:

- keep core business concepts readable
- use proper cardinalities
- use composition only when lifecycle semantics justify it
- avoid decorative inheritance

Implementation diagrams can be more detailed if useful.

---

# 117. Report Evidence Skill

When a significant milestone works, capture evidence immediately.

Examples:

- confirmation screenshot
- Swagger endpoint
- generated Excel
- generated PDF
- OCR flow
- Share Intent
- dashboard
- WebSocket update
- Grafana/k6 if implemented

Store only polished/useful evidence.

---

# 118. GitHub Skill Standard

A flagship repository should be understandable without prior conversation context.

Final README should explain:

```text
Problem
Solution
Architecture
Core flow
Stack
Screenshots
Testing
Important engineering decisions
How to run
Roadmap
```

Do not claim optional features are complete until they work.

Do not publish private industrial data.

---

# 119. Security Skill Checklist

Before completing security-sensitive work:

```text
[ ] server validates input
[ ] passwords hashed
[ ] secrets externalized
[ ] tokens not logged
[ ] authorization assumptions documented
[ ] file URIs safe
[ ] no raw private paths shared
[ ] no sensitive FCM payloads
[ ] no dangerous SQL concatenation
[ ] error messages do not leak internals
```

---

# 120. Android Skill Checklist

Before completing an Android task:

```text
[ ] follows UI_UX.md
[ ] follows DESIGN.md
[ ] Composable has no business logic
[ ] ViewModel state is explicit
[ ] network work off main thread
[ ] errors handled
[ ] loading handled
[ ] empty handled if applicable
[ ] back navigation correct
[ ] lifecycle considered
[ ] accessibility considered
[ ] configuration/rotation does not corrupt critical state
```

---

# 121. Backend Skill Checklist

Before completing a backend task:

```text
[ ] controller thin
[ ] business rule in correct layer
[ ] DTO separate from entity
[ ] validation present
[ ] transaction boundary correct
[ ] database constraint considered
[ ] errors translated
[ ] Swagger updated
[ ] tests added
[ ] no secret/config hardcoding
```

---

# 122. Parser Skill Checklist

Before completing parser work:

```text
[ ] deterministic
[ ] raw source preserved
[ ] exact match priority
[ ] aliases supported
[ ] fuzzy threshold centralized
[ ] decimal comma handled
[ ] decimal point handled
[ ] missing != zero
[ ] partial reports supported
[ ] unknown lines preserved
[ ] suspicious values warned
[ ] confidence cannot auto-confirm
[ ] regression tests updated
```

---

# 123. Reporting Skill Checklist

Before completing Excel/PDF work:

```text
[ ] confirmed final values used
[ ] generated report metadata persisted
[ ] correct period
[ ] professional layout
[ ] unit displayed
[ ] deterministic filename
[ ] stream/file resources closed
[ ] storage abstraction used
[ ] generation failure handled
[ ] output manually opened and verified
```

---

# 124. Scheduling Skill Checklist

Before completing a Quartz task:

```text
[ ] period semantics documented
[ ] Quartz job thin
[ ] business service called
[ ] generation status persisted
[ ] failure visible
[ ] duplicate execution considered
[ ] email failure separated from generation failure
[ ] test/manual verification performed
```

---

# 125. Realtime Skill Checklist

Before completing WebSocket work:

```text
[ ] event has real realtime value
[ ] REST remains authoritative
[ ] authentication considered
[ ] reconnect considered
[ ] missed event recoverable
[ ] payload minimal
[ ] Android lifecycle handled
```

---

# 126. Dependency Addition Checklist

Before adding a dependency:

```text
[ ] solves real problem
[ ] approved scope
[ ] maintained
[ ] license acceptable
[ ] compatible with current stack
[ ] simpler existing dependency cannot solve it
[ ] complexity justified
```

If any answer is unclear, investigate first.

---

# 127. No Overengineering Rule

Do not introduce:

- microservices
- CQRS
- event sourcing
- service mesh
- Kubernetes
- multi-database architecture
- generic plugin system
- complex rule engine
- unnecessary abstraction layers

unless a real requirement appears.

The strongest FactoryFlow implementation is the simplest architecture that cleanly solves the problem and supports future evolution.

---

# 128. No Underengineering Rule

Do not use "keep it simple" as an excuse for:

- controller business logic
- hardcoded KPI labels
- plaintext passwords
- fake validation
- duplicated parser paths
- direct PostgreSQL access from Android
- unversioned database schema
- untested parser
- raw file path sharing
- silent error swallowing

Simplicity must remain professional.

---

# 129. AI Coding Skill

When Codex writes code:

1. inspect current implementation
2. reuse established patterns
3. make the smallest coherent change
4. do not redesign unrelated modules
5. explain important unfamiliar architecture when needed
6. run relevant tests/build
7. inspect diff
8. update documentation/task state
9. report limitations honestly

Do not generate huge code dumps without integration.

Do not claim tests passed unless they actually ran.

---

# 130. Learning Skill

FactoryFlow is also intended to strengthen the developer's ability to explain the technologies used.

For important concepts, generated implementation should remain understandable enough to answer:

```text
What is this?
Why is it here?
How does it work?
What would break if removed?
What alternative existed?
```

Particularly important:

- Spring Security
- JWT
- JPA
- Flyway
- MapStruct
- Kotlin coroutines
- StateFlow
- MVVM
- Retrofit
- Room
- PaddleOCR backend runtime
- Share Intent
- FileProvider
- Quartz
- WebSocket/STOMP
- RabbitMQ if implemented

AI should accelerate learning, not hide architecture.

---

# 131. Technology-Specific Boundaries

The following responsibilities must remain separate:

```text
PaddleOCR
→ text recognition only
```

```text
Parser
→ KPI interpretation only
```

```text
Human confirmation
→ final data authority
```

```text
PostgreSQL
→ authoritative persistence
```

```text
Apache POI
→ Excel generation
```

```text
PDFBox
→ PDF generation
```

```text
Quartz
→ scheduling
```

```text
JavaMailSender
→ unattended backend email
```

```text
Android Share Intent
→ user-initiated sharing/email
```

```text
WebSocket/STOMP
→ realtime event delivery
```

```text
FCM
→ push notification delivery
```

```text
RabbitMQ
→ optional asynchronous decoupling
```

Do not blur these boundaries.

---

# 132. Canonical Unified Acquisition Flow

Every engineer and AI agent must understand this architecture:

```text
MANUAL ENTRY
      │
      ├─────────────────────────────────────────────┐
      │                                             │
PASTE TEXT                                          │
      │                                             │
      ├───────────────> RAW / STRUCTURED INPUT      │
      │                                             │
GALLERY IMAGE                                       │
      │                                             │
      └─> BACKEND OCR API / PADDLEOCR ──────────────┤
                                                    │
SHARED IMAGE                                        │
      │                                             │
      └─> PADDLE OCR API ───────────────────────────┘
                        ↓
                Deterministic parser
                        ↓
                 Extraction results
                        ↓
              Mandatory human review
                        ↓
               Final confirmed values
                        ↓
                    PostgreSQL
                        ↓
           Dashboard / History / Statistics
                        ↓
                 Excel / PDF reports
```

Manual entry may skip parser recognition but must join the same validation/persistence integrity model.

This diagram represents the technical heart of FactoryFlow.

---

# 133. Skill Priority During the Sprint

When time is limited, apply engineering effort in this order:

```text
Business correctness
Parser correctness
Data integrity
Authentication
Core Android flow
Report generation
Dashboard/history
Scheduling
OCR/mobile integrations
Realtime/notifications
Optional messaging
Observability/performance
```

Do not spend two days perfecting optional infrastructure while the confirmation flow is incomplete.

---

# 134. Final Engineering Standard

FactoryFlow code should make a reviewer think:

> The engineer understood the business problem, chose appropriate technologies,
> separated responsibilities clearly, protected data integrity, and finished the important workflows.

It should not make a reviewer think:

> Many libraries were added because they looked impressive.

Every skill in this document exists to support that outcome.

---

# End of SKILLS.md
