# 03_Architecture.md

> **FactoryFlow — System Architecture Specification**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-11
>
> This document defines the **technical architecture of FactoryFlow**.
>
> It explains how the Android application, Spring Boot backend, PostgreSQL database,
> report-generation subsystem, scheduling, realtime communication, notifications,
> storage, and optional asynchronous components fit together.
>
> This document must be read together with:
>
> - `AGENTS.md`
> - `TASKS.md`
> - `SKILLS.md`
> - `DESIGN.md`
> - `UI_UX.md`
>
> The top-level `assets/` folder contains the private original WhatsApp workflow screenshots.
> Review them when source-message structure matters, but never modify, move, delete, or publicly expose them.
> Tests, reports, GitHub, and portfolio material must use sanitized derivatives created later.

---

# 1. Architecture Objective

FactoryFlow must solve one real operational problem:

```text
Unstructured industrial KPI messages
        ↓
Structured, verified, traceable KPI data
        ↓
Automated reporting
```

The architecture must therefore optimize for:

- data integrity
- explainability
- maintainability
- mobile usability
- clear separation of responsibilities
- future extensibility
- minimal unnecessary infrastructure

It must not optimize for artificial scale or technology count.

---

# 2. Architecture Style

FactoryFlow uses a **modular layered architecture with Clean Architecture principles**.

This means:

- business rules are isolated from transport/UI concerns
- controllers do not own business logic
- Android UI does not own backend rules
- persistence is accessed through repositories
- external integrations are isolated behind dedicated services/adapters
- domain concepts remain central

The project does **not** require textbook Clean Architecture package purity if that would create unnecessary ceremony.

The implementation should remain pragmatic and understandable.

---

# 3. High-Level System Context

```text
┌──────────────────────────────┐
│      Maintenance Engineer    │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│      Android Application     │
│ Kotlin + Jetpack Compose     │
│                              │
│ - Dashboard                  │
│ - Acquisition               │
│ - OCR                        │
│ - Confirmation              │
│ - Reports                   │
│ - Notifications             │
│ - Schedules                 │
└───────┬─────────┬────────────┘
        │ REST    │ WebSocket/STOMP
        │         │
        ▼         ▼
┌──────────────────────────────┐
│      Spring Boot Backend     │
│                              │
│ - Authentication             │
│ - KPI parser                 │
│ - Validation                 │
│ - Report lifecycle           │
│ - Statistics                 │
│ - Document generation        │
│ - Scheduling                 │
│ - Email                      │
│ - Realtime events            │
└───────┬──────────┬───────────┘
        │          │
        ▼          ▼
┌───────────────┐  ┌──────────────────┐
│ PostgreSQL    │  │ Report Storage   │
│ Authoritative │  │ Local filesystem │
│ business data │  │ via abstraction  │
└───────────────┘  └──────────────────┘

Optional integrations:
    ├── Firebase Cloud Messaging
    ├── RabbitMQ
    ├── Prometheus / Grafana
    └── SMTP
```

---

# 4. Core Architectural Principle

The central rule is:

```text
All acquisition methods converge into one trusted reporting pipeline.
```

FactoryFlow does not implement separate business systems for:

- manual entry
- paste
- OCR
- gallery
- camera
- Share Intent

They are only different **input adapters**.

After acquisition, the same integrity model applies.

---

# 5. Canonical Acquisition Architecture

```text
MANUAL ENTRY
      │
      ├─────────────────────────────┐
      │                             │
PASTE TEXT                          │
      │                             │
      ├───────────────┐             │
      │               │             │
GALLERY IMAGE         │             │
      │               │             │
      ▼               │             │
   ML KIT OCR          │             │
      │               │             │
      ├───────────────┤             │
      │               │             │
SHARED IMAGE          │             │
      │               │             │
      ▼               │             │
   ML KIT OCR          │             │
      │               │             │
      ├───────────────┤             │
      │               │             │
CAMERA IMAGE          │             │
      │               │             │
      ▼               │             │
   ML KIT OCR          │             │
      │               │             │
      └───────────────┘             │
              │                     │
              ▼                     │
           RAW TEXT                 │
              │                     │
              ▼                     │
      DETERMINISTIC PARSER          │
              │                     │
              ▼                     │
       EXTRACTION RESULTS           │
              │                     │
              └──────────────┐      │
                             ▼      ▼
                     HUMAN CONFIRMATION
                             │
                             ▼
                     AUTHORITATIVE DATA
                             │
                             ▼
                         PostgreSQL
```

Manual entry may bypass parser recognition but must still pass through validation and confirmation rules.

---

# 6. Android Responsibility Boundary

The Android application is responsible for:

- user interaction
- screen state
- navigation
- local UI state
- secure token storage
- acquiring text/images
- CameraX
- gallery selection
- Share Intent
- ML Kit OCR
- displaying parser results
- collecting human corrections
- local cache/drafts where appropriate
- receiving realtime/push updates
- opening/sharing generated files

Android is **not** authoritative for:

- official KPI business rules
- authentication policy
- confirmed business state
- report-generation logic
- scheduling
- official statistics
- server-side persistence

---

# 7. Backend Responsibility Boundary

The Spring Boot backend is authoritative for:

- authentication
- user identity
- KPI definitions
- deterministic parsing
- plausibility rules
- report lifecycle
- confirmation
- persistence
- official statistics
- generated report metadata
- scheduling
- automatic email
- audit logging
- realtime event publication
- optional async processing

---

# 8. Database Responsibility

PostgreSQL is the authoritative source for confirmed business state.

It stores:

- users
- KPI definitions
- maintenance reports
- KPI entries
- generated reports
- schedules
- notifications
- audit information
- refresh/device token data where required

Android Room is never authoritative over PostgreSQL.

---

# 9. Backend Module Decomposition

Recommended logical modules:

```text
auth
kpi
report
parser
validation
dashboard
statistics
generation
storage
schedule
email
notification
realtime
audit
```

These may be implemented using package boundaries rather than separate deployable modules.

The canonical backend structure is feature-oriented. Features such as `auth/`,
`report/`, `kpi/`, `parser/`, `generation/`, `schedule/`, and `notification/` may
contain internal `api/`, `application/`, `domain/`, and `persistence/` packages where
useful. Do not force empty layers into trivial features.

FactoryFlow remains a single Spring Boot application.

---

# 10. Authentication Architecture

```text
Android Login
    ↓
POST /api/auth/login
    ↓
Spring Security
    ↓
Credential Verification
    ↓
Access Token + Refresh Token
    ↓
Android secure storage
    ↓
Authenticated requests
```

When the access token expires:

```text
401 / token expiry
    ↓
Refresh token request
    ↓
New access token
    ↓
Retry original request where safe
```

If refresh fails:

```text
Logout/session expired
```

Refresh tokens are opaque random values. The backend stores only token hashes with
expiration and revocation metadata, rotates them on every refresh, and revokes the
active refresh/session token through `POST /api/auth/logout`.

---

# 11. Authentication Boundaries

The Android UI never:

- validates passwords against local data
- generates JWTs
- trusts role claims without backend validation

The backend never:

- returns password hashes
- stores plaintext passwords
- logs tokens or credentials

---

# 12. Single-Role Architecture

Current effective role:

```text
Maintenance Engineer
```

Do not add:

```text
ROLE_ADMIN
ROLE_SUPERVISOR
ROLE_OPERATOR
```

unless the business model changes.

Authentication is required.

Complex authorization is not.

---

# 13. KPI Definition Architecture

KPI definitions are configuration-driven.

Conceptual structure:

```text
KPIDefinition
    ├── code
    ├── displayName
    ├── category
    ├── unit
    ├── plausibleMin
    ├── plausibleMax
    ├── aliases
    └── active
```

The parser loads active KPI definitions and uses them as its vocabulary.

---

# 14. Parser Architecture

The parser should remain modular.

Recommended conceptual components:

```text
InputNormalizer
LabelMatcher
ValueExtractor
UnitInterpreter
ConfidenceEvaluator
PlausibilityValidator
ExtractionResultAssembler
```

Do not force one class per concept if unnecessary.

The architectural goal is separation of parser responsibilities.

---

# 15. Parser Execution Flow

```text
Raw text
    ↓
InputNormalizer
    ↓
Line/segment extraction
    ↓
LabelMatcher
    ↓
ValueExtractor
    ↓
Unit interpretation
    ↓
ConfidenceEvaluator
    ↓
PlausibilityValidator
    ↓
ExtractionResult
```

One source line may produce zero, one, or multiple KPI candidates. Unknown lines are
preserved through draft and confirmation with `UNRESOLVED`, `ASSIGNED`, or `IGNORED`
resolution; they never disappear silently.

---

# 16. Deterministic Parsing

The parser must produce repeatable output for the same:

- input
- KPI catalog
- parser configuration

No LLM participates in official extraction.

---

# 17. Parser Configuration

Configurable concerns may include:

- fuzzy-match threshold
- accepted separators
- decimal normalization rules
- alias handling
- warning thresholds

Configuration should be centralized.

Do not scatter magic constants.

---

# 18. Human Validation Architecture

Parser results are temporary candidates.

Flow:

```text
Analyze
    ↓
ExtractionResult
    ↓
Android Confirmation UI
    ↓
User edits/removes/adds
    ↓
POST Confirm
    ↓
Backend validates
    ↓
Transaction
    ↓
Confirmed report + final KPI values
```

---

# 19. Data Authority Boundary

Three values may exist conceptually:

```text
source text
extracted value
final confirmed value
```

The official value is:

```text
final confirmed value
```

if confirmation has occurred.

---

# 20. Draft Architecture

Drafts exist before authoritative confirmation.

They may preserve:

- raw text
- acquisition method
- parsed candidates
- user edits
- warning state
- last update time

Draft state may exist server-side and optionally in Room for interruption resilience.

The exact persistence strategy belongs in Android/backend docs.

---

# 21. Report Lifecycle Architecture

```text
Input
  ↓
Analyzed (side-effect-free)
  ↓
Server-side `DRAFT` / `PENDING_REVIEW`
  ↓
`CONFIRMED`
  ↓
Available for dashboard/statistics
  ↓
Eligible for generated reporting
```

Generated documents have their own lifecycle.

`ARCHIVED`, missing/generated/combined dashboard indicators, and similar projections
are not persisted `MaintenanceReport` states. `effective_date` is the report/business
date and is distinct from `submitted_at` and `confirmed_at`; multiple reports may
share one effective date.

---

# 22. Maintenance Report vs Generated Report

These are different concepts.

```text
MaintenanceReport
```

represents confirmed structured KPI data.

```text
GeneratedReport
```

represents a physical Excel/PDF document produced from business data.

Never merge them into one entity.

Core generation is synchronous. Intentional regeneration creates a new
`GeneratedReport` version with provenance. File-generation status and email-delivery
status are independent; `READY` generation with `FAILED` delivery is valid.

---

# 23. Dashboard Architecture

Dashboard is a projection.

It is not a primary persisted aggregate.

Backend may compose:

- current report status
- latest confirmed KPIs
- drafts/pending review
- recent reports
- warnings
- upcoming schedule
- recent generated documents

into one dashboard response optimized for Android.

---

# 24. Dashboard Read Model

Avoid loading full entity graphs.

Use:

- projections
- dedicated DTOs
- targeted queries

where appropriate.

---

# 25. Statistics Architecture

Statistics use only:

```text
confirmed final KPI values
```

Never:

```text
draft values
raw extracted candidates
```

Potential calculations:

- average
- min
- max
- variation
- simple trend
- period grouping

---

# 26. Report Generation Architecture

Conceptual flow:

```text
ReportGenerationService
       │
       ├── ExcelReportGenerator
       │      └── Apache POI
       │
       └── PdfReportGenerator
              └── PDFBox
```

The generation service retrieves authoritative confirmed data.

---

# 27. Report Generator Boundary

Generators are responsible for:

- document construction
- layout
- formatting
- output bytes/file

They are not responsible for:

- deciding who may generate
- querying arbitrary business data directly
- sending email
- scheduling
- notifications

---

# 28. Storage Architecture

Use:

```text
ReportStorageService
```

Initial implementation:

```text
LocalReportStorageService
```

Conceptual paths:

```text
/reports/excel
/reports/pdf
```

Business code should not depend directly on those paths.

---

# 29. Why No MinIO

MinIO is intentionally excluded from the first version.

The storage abstraction preserves future replacement without adding infrastructure cost now.

---

# 30. Scheduling Architecture

Quartz is responsible for trigger timing.

```text
Quartz Trigger
    ↓
Thin Job
    ↓
Application Service
    ↓
Generate report
    ↓
Store
    ↓
Persist metadata
    ↓
Email / Notification
```

Quartz jobs must not contain large business logic.

---

# 31. Schedule Types

Supported:

```text
Daily
Weekly
Monthly
```

Each schedule requires explicit reporting-period semantics.

The business timezone is `Africa/Casablanca`. Weekly periods run Monday through
Sunday. On the first day of each month at the configured time, monthly scheduling
generates the complete previous calendar month. Quartz may recover one missed run,
with duplicate protection by schedule + reporting period + format.

---

# 32. Why No Spring Batch

Spring Batch is intentionally excluded.

FactoryFlow needs recurring scheduling, not a generalized batch-processing framework.

Quartz is more directly aligned with the requirement.

---

# 33. User-Initiated Sharing Architecture

```text
Generated file
    ↓
Android receives file/URI
    ↓
FileProvider
    ↓
ACTION_SEND
    ↓
System share sheet / email app
```

The backend does not send email for this user-initiated action.

---

# 34. Scheduled Email Architecture

```text
Quartz
    ↓
Generate report
    ↓
Store
    ↓
JavaMailSender
    ↓
SMTP
```

Generation and delivery are separate outcomes.

---

# 35. Realtime Architecture

Spring WebSocket/STOMP is a SHOULD-level enhancement after the trusted core is stable
and may handle selected realtime events if implemented.

Flow:

```text
Backend business event
    ↓
STOMP publish
    ↓
Android subscriber
    ↓
UI triggers authoritative refresh
```

Realtime does not replace REST.

---

# 36. Realtime Event Examples

Potential:

```text
REPORT_CONFIRMED
REPORT_GENERATED
REPORT_GENERATION_FAILED
NOTIFICATION_CREATED
DASHBOARD_CHANGED
```

Payloads should be small.

---

# 37. Realtime Reliability

Android must recover from missed events.

Authoritative recovery:

```text
REST refresh
```

Never require complete event history merely to rebuild state.

---

# 38. FCM Architecture

FCM handles push delivery.

Conceptual flow:

```text
Backend event
    ↓
Notification service
    ↓
FCM
    ↓
Android system notification
    ↓
Deep link
```

Push notification payload should not expose unnecessary sensitive data.

---

# 39. Notification Domain

Notifications are business records only if the product needs in-app history.

If so:

```text
Notification
```

may be persisted in PostgreSQL.

FCM remains only one delivery channel.

---

# 40. Optional RabbitMQ Architecture

RabbitMQ is **not core**.

Core generated reporting is synchronous. RabbitMQ may introduce asynchronous
`PENDING` / `GENERATING` / `READY` behavior only through a later explicit API and
architecture change.

If implemented:

```text
API / Scheduler
    ↓
Publish command
    ↓
RabbitMQ
    ↓
Consumer
    ↓
Report generation
    ↓
Persist result
    ↓
Realtime/push update
```

Use only where async decoupling adds value.

---

# 41. RabbitMQ Non-Goals

Do not use RabbitMQ for:

- every CRUD event
- synchronous requests
- simple internal method calls
- artificial microservice simulation

---

# 42. Resilience4j Boundary

Potential valid use cases:

- SMTP
- FCM
- RabbitMQ publishing

Do not wrap:

- parser
- JPA repository calls
- ordinary local services

without a real failure boundary.

---

# 43. Observability Architecture

Optional:

```text
Spring Boot Actuator
    ↓
Micrometer
    ↓
Prometheus
    ↓
Grafana
```

Potential custom metrics:

- parser duration
- report generation duration
- generated report count
- error count

---

# 44. Performance Testing Architecture

k6 interacts with REST APIs externally.

Primary scenarios:

- login
- analyze report
- history
- dashboard/statistics

No artificial massive-scale claims.

---

# 45. Android Internal Architecture

Recommended package layers:

```text
ui/
navigation/
viewmodel/
domain/
data/
network/
database/
di/
feature/
```

A feature-oriented variant is acceptable if consistency is maintained.

Example:

```text
feature/dashboard
feature/report
feature/auth
feature/confirmation
```

Do not mix arbitrary structures.

---

# 46. Android State Flow

```text
Composable
    ↓ user event
ViewModel
    ↓
Repository
    ↓
Retrofit / Room
    ↓
Result
    ↓
ViewModel UiState
    ↓
Composable
```

---

# 47. Android Domain Boundary

Android may define lightweight domain models for UI/business clarity.

Do not duplicate the entire backend domain architecture unnecessarily.

The backend remains authoritative for official business rules.

---

# 48. Retrofit Boundary

Retrofit belongs to data/network layer.

Composable must never call API directly.

---

# 49. Room Boundary

Room may cache:

- report summaries
- KPI definitions
- drafts
- notifications

where useful.

Cache design must define invalidation.

Do not implement full offline synchronization by accident.

---

# 50. OCR Boundary

ML Kit lives entirely on Android.

```text
Image
→ ML Kit
→ text
```

No backend OCR service exists.

For MVP, one screenshot feeds one review flow even when OCR sees multiple WhatsApp
bubbles. The system does not automatically split it into multiple maintenance reports;
human review controls the final draft.

---

# 51. Camera Boundary

CameraX handles image acquisition only.

Captured image then enters:

```text
OCR
→ parser
→ confirmation
```

---

# 52. Share Intent Boundary

Inbound:

```text
External Android app
→ FactoryFlow
→ content URI
→ OCR
```

Outbound:

```text
FactoryFlow generated file
→ FileProvider
→ Android share sheet
```

These are two separate intent flows.

---

# 53. API Boundary

REST is the main contract between Android and backend.

Conceptual baseline:

```text
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout

POST /api/reports/analyze
POST /api/reports/drafts
PUT  /api/reports/{id}/draft
POST /api/reports/{id}/confirm
GET  /api/reports

GET  /api/kpi-definitions
POST /api/kpi-definitions

POST /api/generated-reports

GET /api/statistics
```

Exact routes belong in `06_API.md`.

Analysis is side-effect-free. Generated-report creation is period-based because one
daily, weekly, or monthly document may aggregate multiple maintenance reports.

---

# 54. API Error Architecture

Backend uses one consistent error response.

Conceptual:

```json
{
  "timestamp": "...",
  "status": 400,
  "code": "REPORT_VALIDATION_FAILED",
  "message": "...",
  "details": []
}
```

Android maps technical errors into user-friendly UI state.

---

# 55. API Security

All business endpoints except authentication/health/docs as intentionally configured require authentication.

Do not create public report endpoints accidentally.

---

# 56. Database Architecture

Core relationships:

```text
User
  1
  │
  └── * MaintenanceReport

MaintenanceReport
  1
  │
  └── * KPIEntry

KPIDefinition
  1
  │
  └── * KPIEntry

User
  1
  │
  └── * GeneratedReport
```

Schedules, notifications, audit, tokens extend this model.

---

# 57. Database Evolution

Flyway is mandatory.

Schema changes flow:

```text
Requirement
→ migration
→ entity/model update
→ repository/service update
→ docs update
```

---

# 58. Historical Integrity

A KPI definition may later become inactive.

Historical entries must remain understandable.

Do not hard-delete configuration referenced by confirmed history without a migration/business plan.

---

# 59. Concurrency

The system may have several engineers.

Initial concurrency concerns include:

- two users opening same draft
- duplicate confirmation
- duplicate report generation
- retry after network uncertainty

Use transactional/state checks where appropriate.

---

# 60. Idempotency

Critical repeatable actions should consider duplicate protection.

Especially:

- confirmation
- generation
- scheduled execution
- async queue consumption

Do not assume HTTP/mobile delivery is exactly once.

---

# 61. File Architecture

Generated files live outside PostgreSQL.

PostgreSQL stores metadata/path/reference.

Do not store large report binaries directly in relational tables unless a future requirement justifies it.

---

# 62. File Naming

Use deterministic safe names.

Examples:

```text
FactoryFlow_Daily_2026-08-11.xlsx
FactoryFlow_Daily_2026-08-11.pdf
FactoryFlow_Weekly_2026-W33.pdf
```

Final naming belongs in reporting docs.

---

# 63. Secrets Architecture

Environment-specific secrets include:

- DB password
- JWT signing secret
- SMTP credentials
- Firebase secrets
- RabbitMQ credentials if used

Do not commit them.

---

# 64. Environment Architecture

Recommended Spring profiles:

```text
dev
test
prod
```

Android should use configurable backend base URLs/build configs.

Do not scatter IPs or localhost strings.

---

# 65. Emulator Networking

Android Emulator does not treat backend host `localhost` as the development PC.

The selected dev base URL mechanism must account for emulator host routing.

Document final setup in `07_Android.md`.

---

# 66. Build Architecture

Repository:

```text
FactoryFlow/
├── backend/
├── android/
├── docs/
├── assets/
├── diagrams/
├── report/
└── scripts/
```

Spring Boot and Android build independently but share one product contract.

The backend uses Maven. Android uses Gradle Kotlin DSL. Java 21 is the preferred
current LTS implementation choice when compatible with the selected Spring Boot
version, not a business invariant.

---

# 67. No Microservices

FactoryFlow remains a modular monolith.

Reason:

- initial user scale is small
- one codebase is easier to deliver/test
- no independent scaling requirement exists
- microservices would add operational complexity without business value

---

# 68. No Kubernetes

Not required.

---

# 69. No Service Mesh

Not required.

---

# 70. No Additional Database

PostgreSQL is sufficient.

Room is client cache, not a second business database.

---

# 71. No General Rule Engine

KPI parsing/configuration does not require a generic enterprise rules engine.

Use explicit configuration and deterministic code.

---

# 72. Future S3/Object Storage

The storage abstraction permits future:

```text
S3ReportStorageService
```

without changing generation logic.

This is the intended extensibility point.

---

# 73. Future AI Query Layer

Possible future:

```text
Natural-language question
    ↓
AI query layer
    ↓
Validated historical data
    ↓
Answer
```

This layer must sit **after** data validation.

It never replaces parser confirmation.

---

# 74. Future Predictive Layer

Possible future:

```text
confirmed historical KPI data
    ↓
analytics / anomaly detection / forecasting
```

Outside current core.

---

# 75. Architecture Quality Gates

Before accepting an architecture-affecting feature:

```text
[ ] responsibility in correct layer
[ ] no duplicate source of truth
[ ] backend remains authoritative
[ ] parser/human-confirmation boundary preserved
[ ] no unnecessary infrastructure
[ ] API contract explicit
[ ] persistence migration defined
[ ] error path defined
[ ] testing approach defined
[ ] docs updated
```

---

# 76. Anti-Patterns to Reject

Reject:

```text
Android → PostgreSQL directly

Controller → Repository for business workflows

Composable → Retrofit

OCR → authoritative persistence

Parser → auto-confirm

Dashboard → draft values as official metrics

Quartz job → hundreds of lines of business logic

Report generator → send email itself

RabbitMQ everywhere

Hardcoded KPI aliases

Hardcoded credentials

One giant ReportService
```

---

# 77. Architecture Decision Summary

Approved:

```text
Native Android
Spring Boot modular monolith
PostgreSQL
REST as primary contract
WebSocket/STOMP as a SHOULD-level realtime mechanism
ML Kit OCR on-device
Deterministic parser
Mandatory human confirmation
Apache POI
Apache PDFBox
Quartz
JavaMailSender for SHOULD-level scheduled email
Local report storage abstraction
Flyway
Selective MapStruct usage
```

Optional/late:

```text
FCM (SHOULD)
RabbitMQ
Resilience4j
Prometheus
Grafana
k6
```

Rejected/removed:

```text
Flutter
LLM KPI extraction
Spring Batch
MinIO for initial version
Docker requirement
complex RBAC
microservices
```

---

# 78. Architecture Success Criteria

The architecture is successful when:

- all five acquisition methods reuse one trusted pipeline
- parser behavior is testable and deterministic
- user correction is easy and traceable
- confirmed values are authoritative everywhere
- Android remains independent from persistence internals
- backend business rules remain independent from UI
- report generation is modular
- scheduling is separate from generation
- sharing and automatic email remain separate
- optional infrastructure can be added without rewriting the core
- another engineer can understand the system from documentation

---

# 79. Final Architecture Principle

FactoryFlow should be architecturally strong enough to demonstrate professional engineering, but simple enough to complete, understand, test, and defend.

The architecture exists to support the business problem.

It is not the product itself.

The product remains:

> **a mobile platform that turns fragmented maintenance KPI information into verified, centralized, automated reporting without removing human control.**

---

# End of 03_Architecture.md
