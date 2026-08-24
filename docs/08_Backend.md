# 08_Backend.md

> **FactoryFlow — Spring Boot Backend Architecture & Implementation Specification**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-22
>
> This document defines the **backend architecture, package organization, implementation rules, Spring patterns, security model, parser architecture, persistence behavior, report generation, scheduling, email, realtime communication, optional messaging, error handling, observability, performance, and testing standards** for FactoryFlow.
>
> This document must remain aligned with:
>
> - `AGENTS.md`
> - `TASKS.md`
> - `SKILLS.md`
> - `03_Architecture.md`
> - `04_Business_Rules.md`
> - `05_Database.md`
> - `06_API.md`
> - `07_Android.md`
>
> The backend is the **authoritative business layer** of FactoryFlow.
>
> Android collects and presents data.
>
> Spring Boot decides and enforces:
>
> - authentication
> - KPI definition behavior
> - parser behavior
> - validation
> - confirmation
> - report lifecycle
> - authoritative persistence
> - dashboard/statistics
> - report generation
> - scheduling
> - automatic email
> - notification state
>
> The repository `assets/` folder contains the real WhatsApp screenshots that motivated the project.
> When parser assumptions or source-message structure are unclear, review those assets before inventing new parsing behavior.

---

# 1. Backend Mission

The backend transforms unstructured maintenance input into trusted centralized business state.

Its core mission is:

```text
receive
→ validate
→ analyze
→ persist draft
→ confirm
→ expose authoritative data
→ generate reports
→ schedule automation
→ notify users
```

The backend must protect the project's strongest guarantee:

> **No automatically extracted KPI value becomes authoritative without human confirmation.**

---

# 2. Approved Core Stack

Implemented core backend technologies:

```text
Java
Spring Boot
Spring Web
Spring Validation
Spring Security
Spring Data JPA
PostgreSQL
Flyway
MapStruct
OpenAPI / Swagger
Apache POI
Apache PDFBox
Quartz
JavaMailSender
```

Optional/late:

```text
Spring WebSocket / STOMP
Firebase Admin SDK
RabbitMQ
Resilience4j
Spring Boot Actuator
Micrometer
Prometheus
Grafana
```

Do not add optional infrastructure before the core workflow is stable.

---

# 3. Java Version

Do not hard-freeze the Java version in architecture documentation until the actual build configuration selects it.

Use a currently supported LTS or project-approved version compatible with selected Spring Boot release.

The build file is authoritative for the exact Java version.

Do not silently change Java version during feature work.

---

# 4. Spring Boot Version

Select a stable Spring Boot version compatible with:

- chosen Java version
- Spring Security
- Spring Data JPA
- Quartz
- WebSocket/STOMP
- OpenAPI integration
- selected dependencies

Do not upgrade the framework casually mid-project.

---

# 5. Modular Monolith

FactoryFlow backend is a modular monolith.

One deployable Spring Boot application contains cohesive business modules.

Reasons:

- small initial user count
- one product team
- limited delivery window
- no independent scaling requirement
- easier testing
- easier deployment
- easier defense/explanation

Do not split into microservices.

---

# 6. Recommended Package Structure

Recommended domain-oriented package layout:

```text
com.factoryflow
│
├── FactoryFlowApplication.java
│
├── config/
│
├── security/
│
├── auth/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── persistence/
│
├── user/
│
├── kpi/
│
├── report/
│
├── parser/
│
├── dashboard/
│
├── statistics/
│
├── generation/
│
├── storage/
│
├── schedule/
│
├── notification/
│
├── realtime/
│
├── mail/
│
├── audit/
│
└── shared/
    ├── error/
    ├── api/
    ├── time/
    └── util/
```

Exact nesting should remain practical.

Do not create empty layer folders for appearance.

---

# 7. Feature Package Pattern

A medium-sized feature can use:

```text
report/
├── api/
│   ├── ReportController.java
│   └── dto/
│
├── application/
│   ├── ReportAnalysisService.java
│   ├── ReportConfirmationService.java
│   └── ReportQueryService.java
│
├── domain/
│   ├── MaintenanceReport.java
│   ├── KPIEntry.java
│   └── ReportStatus.java
│
└── persistence/
    ├── MaintenanceReportRepository.java
    └── Jpa...
```

Do not force four subpackages if the feature is still tiny.

---

# 8. Dependency Direction

Preferred dependency direction:

```text
API layer
    ↓
Application/service layer
    ↓
Domain/persistence abstractions
    ↓
Infrastructure
```

External infrastructure should not define business rules.

---

# 9. Controller Rule

Controllers must remain thin.

Responsibilities:

- parse HTTP request
- trigger Bean Validation
- call application service
- return response DTO
- map HTTP status

Controllers must not:

- parse WhatsApp text
- query repositories directly for workflow logic
- generate files
- send emails
- calculate dashboard statistics
- build JWTs manually
- contain transaction orchestration

---

# 10. Application Service Rule

Application services coordinate use cases.

Examples:

```text
AuthenticationService
ReportAnalysisService
DraftReportService
ReportConfirmationService
ReportQueryService
DashboardService
StatisticsService
GeneratedReportService
ScheduleService
NotificationService
```

Split services when responsibilities become distinct.

Do not create one giant `FactoryFlowService`.

---

# 11. Domain Service Rule

Use domain services only where logic genuinely belongs to the domain and does not fit one entity/value object.

Examples:

```text
Parser matching logic
Plausibility evaluation
Reporting-period calculation
```

Do not create domain services for simple repository CRUD.

---

# 12. Repository Rule

Repositories represent persistence access.

Use Spring Data JPA for normal relational work.

Repository methods should use domain language.

Good:

```java
findByEmailIgnoreCase(...)
findByActiveTrueOrderByDisplayNameAsc(...)
findByStatusAndSubmittedAtBetween(...)
```

Avoid generic "findEverythingCustom" repositories.

---

# 13. DTO Rule

Never expose JPA entities directly through REST.

Use request/response DTOs.

Examples:

```text
LoginRequest
LoginResponse
AnalyzeReportRequest
AnalyzeReportResponse
ConfirmReportRequest
ReportSummaryResponse
DashboardResponse
GeneratedReportResponse
```

---

# 14. MapStruct

Use MapStruct when mapping becomes repetitive.

Appropriate:

```text
MaintenanceReport → ReportSummaryResponse
KPIDefinition → KPIDefinitionResponse
GeneratedReport → GeneratedReportResponse
```

Do not hide important business transformation inside generated mapping.

---

# 15. Record Use

Java records are good for immutable DTO/value objects where compatible with chosen libraries.

Example:

```java
public record LoginRequest(
    String email,
    String password
) {}
```

Use conventional classes when framework/mutation requirements make them clearer.

---

# 16. Constructor Injection

Use constructor injection.

Do not use field injection.

Preferred:

```java
@Service
public class ReportConfirmationService {

    private final MaintenanceReportRepository reportRepository;

    public ReportConfirmationService(
        MaintenanceReportRepository reportRepository
    ) {
        this.reportRepository = reportRepository;
    }
}
```

---

# 17. Interface Rule

Use interfaces at meaningful boundaries.

Strong candidates:

```text
ReportStorageService
ExcelReportGenerator
PdfReportGenerator
MailDeliveryService
NotificationPushGateway
```

Do not create `Service` + `ServiceImpl` pairs automatically.

---

# 18. Validation

Use Jakarta Bean Validation for transport-level validation.

Examples:

```text
@NotBlank
@Email
@NotNull
@Size
@Positive
```

Business validation remains in application/domain logic.

---

# 19. Global Error Handling

Use a global exception handler.

Flow:

```text
Exception
→ @RestControllerAdvice
→ stable API error envelope
```

Do not repeat try/catch in every controller.

---

# 20. Exception Types

Prefer meaningful custom exceptions.

Examples:

```text
InvalidCredentialsException
ReportNotFoundException
ReportAlreadyConfirmedException
KpiDefinitionNotFoundException
ReportGenerationException
InvalidReportStateException
ScheduleValidationException
```

Do not throw `RuntimeException("bad")`.

---

# 21. Error Codes

Align with `06_API.md`.

Examples:

```text
AUTH_INVALID_CREDENTIALS
REPORT_NOT_FOUND
REPORT_ALREADY_CONFIRMED
REPORT_VALIDATION_FAILED
KPI_DEFINITION_NOT_FOUND
REPORT_GENERATION_FAILED
```

Do not use exception class names as API codes.

---

# 22. Logging

Use SLF4J.

Logs should be concise and contextual.

Good:

```text
Report confirmed reportId=42 userId=1
Generated PDF generatedReportId=501 period=2026-08-11
Scheduled email failed generatedReportId=501
```

Avoid logging sensitive payloads.

---

# 23. Logging Levels

Use:

```text
DEBUG
INFO
WARN
ERROR
```

meaningfully.

Examples:

```text
DEBUG → parser internal diagnostic
INFO  → important successful business event
WARN  → recoverable suspicious condition
ERROR → failed operation requiring attention
```

Do not log every repository query manually.

---

# 24. Sensitive Logging

Never log:

- passwords
- JWTs
- refresh tokens
- SMTP passwords
- Firebase service credentials
- raw private WhatsApp data unnecessarily

Development request logging must be cautious.

---

# 25. Authentication Architecture

Flow:

```text
POST /api/auth/login
    ↓
AuthenticationManager / authentication service
    ↓
Password verification
    ↓
JWT access token
```

Protected request:

```text
Bearer token
    ↓
JWT filter
    ↓
SecurityContext
    ↓
Controller
```

---

# 26. Password Hashing

Use BCrypt.

Do not implement custom password hashing.

---

# 27. JWT Claims

Include only needed claims.

Potential:

```text
sub / userId
email
issuedAt
expiry
token type if useful
```

No role claim required initially unless Spring Security structure benefits from a constant authority.

Do not add fake multi-role semantics.

---

# 28. JWT Secret

Externalized configuration.

Never commit real secret.

Use environment variable / secrets mechanism.

---

# 29. Access Token Expiry

Access token must expire.

Exact duration belongs in configuration.

Do not make tokens permanent.

---

# 30. Future Refresh Token Strategy

Refresh tokens are not implemented. If approved later, choose and document one strategy.

Recommended secure approach:

- opaque random refresh token
- store hash server-side
- expiry
- revocation
- rotation on refresh

Do not store raw refresh token if hashing model is used.

---

# 31. Refresh Token Rotation

If implemented:

```text
old token
→ validate
→ revoke
→ issue replacement
→ link replacement
```

Prevents indefinite reuse.

---

# 32. Logout

If server-side refresh tokens exist:

Logout revokes current refresh token/session.

Android clears local credentials.

---

# 33. User Active State

Authentication must reject inactive users.

Historical data remains.

---

# 34. Spring Security Configuration

Keep configuration readable.

Define:

- public endpoints
- authenticated endpoints
- JWT filter
- stateless session
- password encoder
- authentication manager

Avoid obsolete/deprecated configuration style.

---

# 35. CSRF

For stateless bearer-token REST API:

CSRF may be disabled appropriately.

Document rationale.

Do not disable unrelated security features blindly.

---

# 36. CORS

Native Android does not require browser CORS.

Only configure CORS for Swagger/dev/future browser needs.

Do not use permissive `*` in production-like environment without reason.

---

# 37. KPI Definition Service

Responsibilities:

- create/update definitions
- active/inactive state
- alias management
- plausible range validation
- query active catalog

It does not parse reports itself.

---

# 38. Alias Normalization

Centralize normalization.

Possible operations:

- trim
- lower-case
- collapse spaces
- normalize punctuation/accents if business rules require

Do not implement different alias normalization rules in DB service and parser.

---

# 39. KPI Code

KPI code is stable unique identity.

Display name may change.

Parser should use canonical vocabulary + aliases.

---

# 40. Parser Architecture

Recommended components:

```text
InputNormalizer
LineSegmenter
LabelMatcher
NumericValueExtractor
UnitInterpreter
ConfidenceEvaluator
PlausibilityValidator
ParserResultAssembler
```

Combine components if classes would otherwise be trivial.

Do not make one giant regex function.

---

# 41. Parser Entry Point

Conceptual:

```java
ExtractionResult analyze(
    String rawText,
    List<KpiDefinition> catalog
)
```

or application-service equivalent.

---

# 42. Input Normalization

Normalize only for analysis.

Potential:

- line endings
- whitespace
- punctuation variants
- case for matching
- supported separator forms

Do not destroy raw source.

---

# 43. Line Segmentation

Parser should preserve original line context.

If messages contain multiple KPI pairs per line later, segmentation can evolve.

Start with real observed patterns from `assets/`.

---

# 44. Label Matching Order

Use deterministic priority:

```text
exact canonical
→ exact alias
→ normalized exact
→ fuzzy
→ unknown
```

Do not fuzzy-match before checking exact aliases.

---

# 45. Fuzzy Algorithm

A deterministic algorithm such as Levenshtein similarity is acceptable.

Threshold is centralized/configurable.

Do not embed threshold in multiple methods.

---

# 46. Match Result

Conceptual:

```java
record LabelMatch(
    KpiDefinition definition,
    MatchType matchType,
    BigDecimal score,
    String matchedSourceLabel
) {}
```

Exact shape can vary.

---

# 47. Match Type

Potential:

```text
EXACT_CANONICAL
EXACT_ALIAS
NORMALIZED
FUZZY
UNMATCHED
```

Useful for confidence/explainability.

---

# 48. Numeric Extraction

Support:

```text
12.5
12,5
295456
```

and approved thousands behavior.

Use `BigDecimal`.

---

# 49. Ambiguous Numeric Formats

If ambiguity remains:

Do not guess silently.

Return warning/lower confidence or unresolved candidate.

For a token such as `30.197`, preserve the decimal reading as the editable detected
value while returning `AMBIGUOUS_NUMBER` and both deterministic alternatives. It is
an attention candidate, never a missing value, and becomes authoritative only after
explicit human validation.

---

# 50. Missing Numeric Value

Missing stays missing.

Never map missing to zero.

---

# 51. Unit Parsing

If source includes unit:

Capture it.

Compare with expected unit if business rules support that.

Do not auto-convert without explicit unit-conversion rules.

---

# 52. Duplicate KPI

If same KPI appears multiple times:

Preserve candidates or mark duplicate warning.

Do not silently take first/last.

---

# 53. Partial Report

Parser returns recognized subset.

Do not reject entire source because some expected KPIs are absent.

---

# 54. Unknown Lines

Return them explicitly.

Potential:

```text
sourceLine
reason
```

No silent discard.

`SourceLineClassifier` may safely classify WhatsApp timestamps, day separators,
source-date headers, isolated punctuation/single OCR characters, known conversation
headers, and other deterministic metadata as ignored source lines. The raw source
remains preserved. Bulk ignore must call this backend classifier rather than
reimplementing its rules in Android.

---

# 55. Confidence

Confidence is advisory.

Potential factors:

- match type
- fuzzy score
- numeric extraction certainty
- unit compatibility
- plausible range

Do not auto-confirm based on confidence.

---

# 56. Confidence Scale

If numeric:

```text
0.0–1.0
```

Use `BigDecimal`/double carefully.

Presentation category:

```text
HIGH
REVIEW
LOW
```

---

# 57. Plausibility Validation

Use configured min/max.

Outside range:

```text
warning
```

not automatic rejection.

---

# 58. Parser Result

Must be rich enough for Android confirmation.

Include:

- definition identity
- source label
- source line
- extracted value
- units
- confidence
- warnings
- unknown lines
- counts

---

# 59. Parser Exception Behavior

A malformed individual line should not necessarily fail whole report.

Prefer partial analysis with per-line warning when possible.

Only fail entire analysis if input cannot meaningfully be processed.

---

# 60. Parser Testing

High priority.

Use parameterized tests for format variants.

Every real parser bug should add regression coverage.

---

# 61. Parser Test Fixtures

Use anonymized realistic messages.

Review `assets/` for source structure.

Do not hardcode private data into public tests.

---

# 62. Draft Architecture

A draft is non-authoritative.

Recommended persistence flow:

```text
create draft
→ save parser/manual state
→ update edits
→ resume
→ confirm
```

---

# 63. Draft Entity State

Recommended status:

```text
DRAFT
PENDING_REVIEW
CONFIRMED
```

Do not use generated-document statuses on MaintenanceReport.

---

# 64. Draft Save

A save updates same draft.

Do not create duplicate drafts on repeated save.

---

# 65. Draft Versioning

Consider optimistic locking if multiple users may edit same draft.

Use `@Version` on mutable aggregate if needed.

---

# 66. Report Confirmation Service

This is a critical application service.

Responsibilities:

1. load draft
2. verify state
3. validate submitted final values
4. preserve extracted values
5. update final values
6. mark confirmed
7. set confirmed timestamp
8. write audit events
9. commit transaction
10. publish post-commit realtime/notification event

---

# 67. Confirmation Transaction

Use:

```java
@Transactional
```

around database state change.

Do not send external email inside this transaction.

---

# 68. Already Confirmed

Return conflict.

Do not overwrite.

---

# 69. Confirmed Report Mutability

No normal update endpoint for final KPI values after confirmation.

Future correction workflow must be explicit.

---

# 70. Audit Integration

Audit important state changes.

Prefer an application-level audit service.

Do not put all audit logic into JPA entity callbacks.

---

# 71. Audit Content

For correction:

Preserve enough context:

```text
old extracted/current value
new final value
user
timestamp
```

Avoid logging huge raw source repeatedly.

---

# 72. Report Query Service

Read-side service handles:

- paginated history
- detail
- filters
- draft retrieval

Use DTO projections when useful.

---

# 73. History Filtering

Support agreed filters from `06_API.md`.

Do not build over-general dynamic query framework unless necessary.

Spring Data Specifications may be used if filters become complex.

---

# 74. Pagination

Use `Pageable`.

Map to stable response DTO.

Do not expose raw framework serialization if unstable.

---

# 75. Dashboard Service

Dashboard is a read-model orchestration service.

It may query:

- today's state
- latest KPIs
- recent reports
- draft count
- recent generated documents
- unread notifications
- upcoming schedule

Keep queries efficient.

---

# 76. Dashboard Query Optimization

Avoid:

```text
load all reports
→ calculate dashboard in memory
```

Use targeted queries/projections.

---

# 77. Latest KPI Query

Need efficient:

```text
latest confirmed final value per KPI
```

Use PostgreSQL/query design rather than N queries if practical.

---

# 78. Statistics Service

Statistics uses confirmed final values only.

Potential calculations:

```text
average
min
max
latest
variation
grouped time series
```

---

# 79. Statistics in SQL

Prefer database aggregation.

PostgreSQL is good at:

```text
AVG
MIN
MAX
GROUP BY
DATE_TRUNC
```

Do not fetch every historical row to Java for basic aggregation.

---

# 80. Reporting Period Service

Centralize daily/weekly/monthly period calculation.

Conceptual:

```java
ReportPeriod calculate(
    ReportType type,
    LocalDate referenceDate,
    ZoneId zone
)
```

Do not duplicate date logic in Quartz and generation controller.

---

# 81. Daily Period

Calendar day in configured business timezone.

---

# 82. Weekly Period

Use approved calendar week semantics.

Document first day of week.

Do not use "last 7 days" unless business requirement says so.

---

# 83. Monthly Period

Calendar month.

---

# 84. Generated Report Service

Responsibilities:

- validate generation request
- calculate/accept period
- load confirmed source data
- invoke generator
- store output
- persist generated report metadata
- link source reports if implemented
- publish completion/failure event

Current manual generation has two explicit entry points: period-based consolidated
generation (`DAILY`, `WEEKLY`, `MONTHLY`, `CUSTOM`) and exact-ID `INDIVIDUAL` export.
Both load confirmed source data only. Individual generation binds precisely one source
report; consolidated generation uses every confirmed report in the inclusive period.

---

# 85. Generator Interface

Possible:

```java
public interface ReportGenerator {
    ReportFormat supports();
    GeneratedFile generate(ReportData data);
}
```

or separate Excel/PDF interfaces.

Keep it simple.

---

# 86. Excel Generator

Use Apache POI.

Responsibilities:

- workbook
- sheets
- headers
- KPI rows
- units
- period
- metadata
- readable styles

The implemented workbook contains one sheet named `Rapport`. Its main detail table is
limited to Date, Indicateur, Valeur, Valeur associée, and Unité. The official packaged
Alf Mabrouk PNG is the only drawing; no Excel charts or visible internal audit formulas
are generated.

---

# 87. Excel Styling

Correctness first.

Professional styling:

- title
- header emphasis
- widths
- number formats
- freeze pane if useful
- consistent alignment

Advanced styling is lower priority than correct output.

---

# 88. POI Resource Management

Use try-with-resources.

Close workbook/output stream.

Reuse style objects.

---

# 89. PDF Generator

Use Apache PDFBox.

Responsibilities:

- document
- page creation
- text/table layout
- pagination
- metadata
- period
- values/units

The implemented PDF uses the same warm white/sage visual identity and official logo.
Daily and individual PDFs omit trends. Weekly/monthly PDFs add one compact trend chart
only when at least two confirmed points exist for one KPI and page space is sufficient.

---

# 90. PDF Font

Use legal/available fonts.

Do not rely on proprietary Apple fonts.

---

# 91. PDF Layout

Handle page overflow.

Do not truncate data silently.

---

# 92. Storage Service

Interface:

```text
ReportStorageService
```

Core methods may include:

```text
store
open/read
delete if supported
exists
```

---

# 93. Local Storage Implementation

Store under configured root.

Example conceptual:

```text
reports/excel/
reports/pdf/
```

Do not hardcode absolute OS-specific path.

---

# 94. Path Safety

Prevent path traversal.

Generate filenames server-side.

Never trust raw user filename.

---

# 95. Atomic File Success

Do not mark report READY until file has been generated/stored successfully.

Clean partial files on failure when practical.

---

# 96. Generated Report Metadata

Store:

- type
- format
- period
- origin
- status
- file reference
- generated_at
- generated_by
- email status

---

# 97. Manual Generation

Authenticated user initiates.

Backend records user.

---

# 98. Scheduled Generation

Quartz initiates.

`generated_by` may be null/system.

Origin:

```text
SCHEDULED
```

---

# 99. Quartz Configuration

Quartz belongs in backend.

Use persistent job store only if project requirements justify it.

For initial development, choose a reliable configuration proportional to needs.

Do not create cluster configuration for one instance.

---

# 100. Quartz Job

Thin:

```java
public void execute(...) {
    scheduledReportService.run(scheduleId);
}
```

No Excel/SMTP details inside job.

---

# 101. Schedule Service

Responsibilities:

- validate schedule
- persist configuration
- create/update Quartz trigger
- calculate next run
- enable/disable
- expose schedule history

---

# 102. Schedule Timezone

Use explicit `ZoneId`.

Do not depend on server default.

---

# 103. Quartz Misfire

Choose/document misfire behavior.

Example considerations:

- server was down at scheduled time
- run immediately after recovery?
- skip missed run?

Do not leave default behavior unexplained if it affects business.

---

# 104. Schedule Run Recording

Recommended:

```text
STARTED
SUCCEEDED
PARTIAL_SUCCESS
FAILED
```

Persist execution outcome.

---

# 105. Scheduled Empty Period

Do not fabricate zero-filled report.

Record missing/no-data condition according to business rule.

---

# 106. Email Service

Use `JavaMailSender`.

Separate:

```text
EmailDeliveryService
```

from generation.

---

# 107. Email Configuration

Externalize:

- host
- port
- username
- password
- TLS
- sender

---

# 108. Email Recipient Configuration

Recipients come from schedule/config.

No hardcoded real recipient addresses.

---

# 109. Email Attachment

Read generated file through storage service.

Do not reconstruct report unnecessarily.

For a schedule requesting Excel and PDF, load both stored files and send one multipart
message with two attachments, a plain-text alternative, and a professional inline-CSS
HTML body. One schedule execution must not send one e-mail per format.

---

# 110. Email Failure

If report already generated:

- keep report READY
- mark email FAILED
- record/log failure
- optionally notify
- allow retry if implemented

If one requested format fails, retain every valid generated file but send no partial
e-mail. Record failed/partial run states and create one schedule failure notification.

---

# 111. Email Retry

Manual retry endpoint/service may resend existing file.

Do not regenerate by default.

---

# 112. Future Realtime Architecture

Spring WebSocket/STOMP is not implemented. If approved later, use it only as a small
invalidation/event channel while REST remains authoritative.

Suggested responsibilities:

- configure broker endpoint
- authenticate connection
- publish small business events
- user-specific notification queue where needed

---

# 113. STOMP Endpoint

Conceptual:

```text
/ws
```

Exact path must align with Android.

---

# 114. STOMP Topics

Potential:

```text
/topic/reports
/user/queue/notifications
```

Do not create many topics without use.

---

# 115. STOMP Event Envelope

Example:

```json
{
  "type": "REPORT_CONFIRMED",
  "entityId": 42,
  "occurredAt": "2026-08-11T14:25:00Z"
}
```

---

# 116. Event Publication Timing

Publish after successful business commit.

Do not tell clients a report was confirmed before transaction succeeds.

Use transaction event listener if needed.

---

# 117. WebSocket Authority

WebSocket event says:

```text
something changed
```

REST returns authoritative data.

---

# 118. WebSocket Security

Authenticate based on JWT.

Do not permit anonymous business-topic subscription.

---

# 119. Notification Service

Business notification service may:

- persist notification
- publish STOMP event
- send FCM push

Keep channels separate.

---

# 120. FCM Backend

If FCM is implemented:

Use Firebase Admin SDK/backend service account.

Do not place server service credentials in Android app.

---

# 121. FCM Push Gateway

Meaningful abstraction candidate:

```text
PushNotificationGateway
```

Implementation:

```text
FirebasePushNotificationGateway
```

---

# 122. FCM Payload

Keep minimal:

```text
type
entityId
```

Avoid sensitive raw KPI values.

---

# 123. Device Token Management

Backend associates FCM token with authenticated user.

Handle:

- new token
- refresh
- logout
- invalid/stale token

---

# 124. Notification Failure

Push failure does not invalidate business event.

Persisted in-app notification/REST state remains discoverable.

---

# 125. Optional RabbitMQ

RabbitMQ is late/optional.

Do not add before core is stable.

---

# 126. RabbitMQ Use Case

Best candidate:

```text
asynchronous report generation
```

Possible:

```text
generation request
→ persist PENDING
→ publish command
→ consumer generates
→ mark READY
→ notify
```

---

# 127. RabbitMQ Command

Use explicit message schema.

Example:

```json
{
  "generatedReportId": 501,
  "requestedAt": "...",
  "type": "DAILY",
  "format": "PDF"
}
```

Do not serialize JPA entities into messages.

---

# 128. RabbitMQ Idempotency

Consumer must tolerate duplicate message delivery.

Check report state before generating again.

---

# 129. RabbitMQ Acknowledgement

Acknowledge after successful handling.

Failure strategy must be explicit.

---

# 130. Dead Letter

Use DLQ only if RabbitMQ is implemented and failure handling justifies it.

Do not add elaborate topology for portfolio appearance.

---

# 131. Resilience4j

Optional.

Use around real remote/external boundaries.

Potential:

```text
SMTP
FCM
RabbitMQ publish
```

---

# 132. Retry Rule

Retry only transient failures.

Do not retry:

- invalid credentials
- validation errors
- nonexistent entity
- permanent configuration errors

---

# 133. Circuit Breaker

Only useful for repeated external-service failure.

Do not circuit-break internal services.

---

# 134. Timeout

Configure sensible network/external timeouts.

Do not allow SMTP/API calls to hang indefinitely.

---

# 135. Database Transactions

Keep transactions short.

Avoid external network calls inside transaction.

---

# 136. JPA Entity Rules

Use entities for persistence.

Do not expose them.

Avoid giant bidirectional graphs.

---

# 137. Entity Equality

Be careful with `equals/hashCode` on JPA entities.

Do not include lazy collections.

Use a consistent identity strategy.

---

# 138. Lombok

Not required.

If added deliberately, use consistently and understand generated behavior.

Avoid `@Data` on JPA entities because it can create problematic equality/toString behavior.

---

# 139. JPA Enum

Use:

```java
@Enumerated(EnumType.STRING)
```

Never ordinal.

---

# 140. JPA Fetching

Default relationships should be deliberately lazy where appropriate.

Avoid blanket EAGER.

---

# 141. N+1

Review history/detail/dashboard queries.

Use:

- projections
- fetch joins
- entity graphs

when needed.

---

# 142. JPA Cascade

Use only when lifecycle ownership is real.

Do not cascade delete from KPI definition to historical entries.

---

# 143. Optimistic Locking

Consider:

```java
@Version
```

for drafts/schedules.

Return conflict on stale updates.

---

# 144. Database Constraints

Application validation does not replace DB constraints.

Use:

- unique email
- unique KPI code
- foreign keys
- range checks where appropriate

---

# 145. Flyway

Mandatory.

Path:

```text
src/main/resources/db/migration
```

---

# 146. Migration Rules

Never edit applied migration.

Use new migration.

Keep schema + entity model aligned.

---

# 147. Seed Data

Use anonymized development data.

Do not seed real private WhatsApp content.

---

# 148. PostgreSQL JSONB

Use for audit metadata or flexible technical metadata only.

Do not move core KPI data into JSONB.

---

# 149. Repository Query Testing

Test important custom queries.

Especially:

- latest KPI
- history filters
- dashboard projections
- statistics aggregation

---

# 150. OpenAPI

Use Springdoc/OpenAPI-compatible library.

Document:

- endpoints
- auth
- request/response
- errors
- examples

---

# 151. OpenAPI Examples

Use realistic anonymized KPI examples.

Avoid `foo/bar`.

---

# 152. Swagger Security

Configure Bearer authentication.

Make testing protected endpoints easy.

---

# 153. API Contract Discipline

Once Android depends on endpoint:

Breaking changes require coordinated update.

Do not casually rename fields.

---

# 154. API Versioning

No `/v1` required initially.

If adopted later, migrate coherently.

---

# 155. Health Endpoint

Actuator health may be enabled.

Expose only safe health data.

---

# 156. Actuator

Optional/core-lite depending on build.

Useful:

```text
health
info
metrics
```

Do not expose environment/secrets publicly.

---

# 157. Micrometer

If observability phase is reached:

Use Micrometer.

Potential custom timers:

```text
factoryflow.parser.duration
factoryflow.report.generation.duration
```

---

# 158. Prometheus

Optional.

Expose metrics endpoint only when monitoring stack exists.

---

# 159. Grafana

Optional.

Dashboards should show real measured system properties.

No fake metrics.

---

# 160. Performance

Initial user count is small.

Focus first on correctness and responsive behavior.

Potential hot spots:

- parser
- dashboard query
- statistics query
- report generation
- file transfer

---

# 161. k6

Optional late.

Use realistic scenarios.

Do not simulate absurd scale to inflate results.

---

# 162. Parser Performance

Parser should be fast for small messages.

Do not prematurely build caches/index structures without measurement.

---

# 163. Report Generation Performance

For expected data volume:

Apache POI/PDFBox synchronous generation is likely sufficient.

Measure before introducing RabbitMQ.

---

# 164. Async Upgrade Trigger

Consider async generation only if:

- generation becomes noticeably slow
- user should leave screen while job continues
- scheduler workload benefits
- portfolio learning value is intentionally prioritized after core

---

# 165. API Rate Limiting

Not required initially.

---

# 166. Caching

No Redis required.

Use database queries efficiently first.

---

# 167. Search

PostgreSQL sufficient.

No Elasticsearch.

---

# 168. Background Jobs

Quartz owns recurring backend schedules.

Do not use Spring Batch.

---

# 169. Docker

Not required.

Backend must run directly via Spring Boot.

PostgreSQL may be local/service-installed.

---

# 170. Environment Configuration

Use Spring profiles:

```text
dev
test
prod
```

where useful.

---

# 171. `application.yml`

Store safe defaults.

Secrets come from environment/external config.

---

# 172. Environment Variables

Potential:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
SMTP_HOST
SMTP_USERNAME
SMTP_PASSWORD
FIREBASE_CREDENTIALS
```

Exact names should be standardized.

---

# 173. No Secret Defaults

Do not put:

```text
password=admin
jwt-secret=secret
```

as production-like fallback.

Development-only defaults must be clearly isolated.

---

# 174. Test Profile

Test profile should:

- use test DB
- disable real SMTP
- disable real FCM
- control Quartz
- avoid sending external side effects

---

# 175. Email Test Double

Use mock/fake mail delivery in tests.

Do not send real email during unit/integration tests.

---

# 176. FCM Test Double

Use interface/mock.

Do not send real push during automated test.

---

# 177. Quartz Testability

Extract schedule business logic into services.

Test period/generation logic without waiting for real clock trigger.

---

# 178. Clock Abstraction

For time-sensitive business logic, inject:

```java
Clock
```

where useful.

This makes daily/weekly/monthly tests deterministic.

---

# 179. ZoneId Configuration

Centralize business timezone.

Do not call `ZoneId.systemDefault()` throughout code.

---

# 180. File Storage Testability

Use temporary directory in tests.

Do not write into real `/reports` during unit tests.

---

# 181. Unit Testing

Use JUnit.

Mockito where isolation helps.

Prioritize behavior.

---

# 182. Parser Unit Tests

Highest priority.

Parameterized tests for real formatting variants.

---

# 183. Confirmation Service Tests

Test:

- correct final values
- edited flag
- transaction/state
- already confirmed
- unknown KPI
- invalid state
- partial report

---

# 184. Authentication Tests

Test:

- valid login
- wrong password
- unknown email
- inactive user
- expired token
- invalid refresh

---

# 185. Repository Integration Tests

Use PostgreSQL-compatible environment.

Test important queries.

---

# 186. Controller Integration Tests

Use MockMvc or equivalent.

Verify:

- status
- JSON contract
- validation
- auth
- errors

---

# 187. Report Generator Tests

Generate actual file.

Open/inspect basic structure.

For Excel:

- workbook opens
- expected sheet/cells exist

For PDF:

- document opens
- expected text/pages present

Do not only mock Apache POI/PDFBox.

---

# 188. Schedule Tests

Test period calculations with fixed `Clock`.

Test daily/weekly/monthly semantics.

---

# 189. Email Tests

Verify attachment/recipient request through mocked mail sender/service.

---

# 190. WebSocket Tests

Test publication where useful.

Do not spend large sprint time on exhaustive broker testing.

---

# 191. Integration Test Priority

Core flow:

```text
login
→ analyze
→ draft
→ confirm
→ dashboard
→ history
→ generate
```

This is more valuable than isolated CRUD-only tests.

---

# 192. Test Data

Use realistic anonymized values/messages.

---

# 193. No Fake Test Success

Do not disable failing tests to make build green.

Fix or document genuine blocker.

---

# 194. Build

Use Maven or Gradle according to actual backend project choice.

Do not switch build tools mid-project without reason.

The build file is authoritative.

---

# 195. Maven Structure

If Maven:

```text
pom.xml
mvn test
mvn spring-boot:run
```

---

# 196. Gradle Structure

If Gradle:

```text
build.gradle.kts
./gradlew test
./gradlew bootRun
```

---

# 197. Dependency Management

Prefer framework-managed versions.

Do not manually pin transitive libraries unnecessarily.

---

# 198. Dependency Review

Before adding library:

- why?
- maintenance?
- license?
- Spring compatibility?
- existing stack already solves it?

---

# 199. Apache PDFBox License

Approved.

Keep dependency/version documented.

---

# 200. iText

Do not introduce.

PDFBox is the approved PDF library.

---

# 201. MinIO

Do not introduce in initial version.

---

# 202. Spring Batch

Do not introduce.

---

# 203. Microservices

Do not introduce.

---

# 204. GraphQL

Not required.

REST is approved.

---

# 205. Redis

Not required.

---

# 206. Kafka

Not required.

RabbitMQ is already the optional messaging candidate.

Do not add both.

---

# 207. AI/LLM Extraction

Do not introduce.

Future AI only after confirmed data.

---

# 208. Domain Events

Internal domain/application events may be used when they simplify post-commit concerns.

Example:

```text
ReportConfirmedEvent
GeneratedReportReadyEvent
```

Do not build elaborate event sourcing.

---

# 209. Post-Commit Event Handling

Good use:

```text
transaction commits
→ publish realtime notification
```

This avoids announcing failed state.

---

# 210. Notification Event

One business event may feed:

- audit
- in-app notification
- STOMP
- FCM

Keep channels decoupled.

---

# 211. Audit Failure

Decide whether critical audit persistence failure should fail business transaction.

For report confirmation, audit may reasonably be part of same transaction if considered mandatory.

Do not silently lose required audit.

---

# 212. Mail Failure

Must not rollback already successful confirmed data or generated report.

---

# 213. FCM Failure

Must not rollback business state.

---

# 214. WebSocket Failure

Must not rollback business state.

---

# 215. RabbitMQ Publish Failure

If async generation depends on queue publication:

request state must reflect failure.

Use transactional/outbox only if genuinely required later.

Do not add outbox now.

---

# 216. Outbox Pattern

Future option if RabbitMQ reliability requires it.

Not core.

---

# 217. Scheduled Job Idempotency

Use unique business key if necessary:

```text
schedule + period + format
```

to avoid duplicate generated reports.

---

# 218. Manual Generation Duplicates

Decide whether user may intentionally regenerate.

If yes:

each generation can be a new GeneratedReport.

If no:

return existing ready report.

Document final policy.

---

# 219. File Download Endpoint

Backend streams file through authenticated endpoint.

Do not return raw local path.

---

# 220. Streaming Files

Use appropriate Spring resource/stream response.

Avoid loading huge file entirely into memory if unnecessary.

Expected files are small, but implement cleanly.

---

# 221. MIME Types

PDF:

```text
application/pdf
```

Excel:

```text
application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

---

# 222. Content-Disposition

Use safe filename.

---

# 223. Raw Source Exposure

Full report detail may return raw text.

History summary should not.

---

# 224. Privacy

Do not expose other unnecessary private data in API.

Current users share one role, but still use authenticated access.

---

# 225. Public GitHub Safety

Before publishing:

- no secrets
- no private email recipients
- no production DB dump
- no confidential screenshots without sanitization
- no real SMTP config

---

# 226. README Backend Setup

Final README/backend docs should explain:

- prerequisites
- DB creation
- environment variables
- migration
- run command
- Swagger
- demo credentials if safe

---

# 227. Backend Definition of Done

A backend feature is complete only when:

```text
[ ] business rule implemented in correct layer
[ ] DTO contract aligned with 06_API.md
[ ] validation present
[ ] transaction boundary correct
[ ] DB migration exists if needed
[ ] repository query correct
[ ] errors mapped
[ ] security correct
[ ] OpenAPI updated
[ ] tests added
[ ] external failure behavior handled
[ ] no secrets
[ ] build/test passes
```

---

# 228. Core Backend Implementation Order

Recommended:

```text
1. Spring Boot bootstrap
2. PostgreSQL/Flyway
3. shared error handling
4. users
5. Spring Security
6. JWT/refresh
7. KPI definitions/aliases
8. parser core
9. parser tests
10. drafts/reports
11. confirmation
12. dashboard
13. history/filtering
14. Excel generation
15. PDF generation
16. storage
17. generated report history/file
18. Quartz schedules
19. JavaMailSender
20. statistics
21. WebSocket/STOMP
22. notifications
23. FCM
24. optional RabbitMQ
25. optional resilience
26. observability/performance
```

---

# 229. Core Backend Acceptance Flow

Must work end-to-end:

```text
POST login
→ JWT
→ POST analyze realistic message
→ save draft
→ correct final value
→ confirm
→ PostgreSQL contains extracted + final values
→ dashboard returns confirmed value
→ history returns report
→ generate Excel/PDF
→ authenticated file download works
```

---

# 230. Scheduled Backend Acceptance Flow

Must work:

```text
Quartz trigger
→ reporting period
→ confirmed data query
→ generate file
→ store
→ persist metadata
→ email
→ delivery status
→ notification/realtime event
```

---

# 231. Parser Acceptance Flow

Using anonymized real input:

```text
different field order
different separators
decimal comma
typo alias
missing value
partial report
unknown line
```

must produce explainable deterministic output.

---

# 232. Backend Code Review Checklist

Before commit:

```text
[ ] controller thin
[ ] no entity exposed
[ ] constructor injection
[ ] no field injection
[ ] no hardcoded secret
[ ] no business logic in repository/controller
[ ] transactions short
[ ] no external call inside critical DB transaction
[ ] enum stored as STRING
[ ] no N+1 obvious
[ ] parser rules centralized
[ ] confirmed data only used downstream
[ ] errors stable
[ ] tests meaningful
[ ] diff focused
```

---

# 233. Backend Learning Checklist

The developer should be able to explain:

```text
Why modular monolith?
How Spring Security JWT flow works?
Why refresh tokens exist?
Why PostgreSQL is authoritative?
Why Flyway?
Why MapStruct?
How parser matching works?
Why confidence cannot auto-confirm?
Why @Transactional on confirmation?
Why generators are separate from email?
Why Quartz instead of Spring Batch?
Why PDFBox?
Why storage abstraction?
How STOMP differs from REST?
Why RabbitMQ is optional?
Why email failure does not rollback generation?
```

---

# 234. Final Backend Principle

The backend should feel like a coherent industrial information-system core, not a pile of controllers and frameworks.

Its job is to make these guarantees true:

```text
input can be messy
analysis can be uncertain
human review is explicit
confirmed data is trusted
history is preserved
reports are reproducible
automation is predictable
failures are visible
```

The strongest backend implementation is not the one with the most infrastructure.

It is the one where the business rules are obvious, the boundaries are clean, and the system can be confidently explained and tested.

---

## Review persistence guarantees

Flyway migration `V11__preserve_review_classification.sql` adds non-destructive metadata for suggestion strength/method and unknown-line classification. Existing rows default to conservative `KPI_LIKE`, `UNCLASSIFIED`, and `safe_to_ignore = false` semantics.

Report confirmation is observation-based. `kpi_entries.id` identifies each retained occurrence, so duplicate KPI observations remain separate and traceable. Confirmation validates that the submitted KPI definition still matches the persisted observation but does not impose uniqueness on the definition ID.

Safe bulk ignore uses persisted deterministic classification rather than reclassifying display text at click time. Removing an extraction creates an ignored source trace with reason `REMOVED_EXTRACTION`.

---

# End of 08_Backend.md
