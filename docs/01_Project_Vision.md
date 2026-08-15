# 01_Project_Vision.md

> **FactoryFlow — Project Vision & Product Rationale**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-11
>
> This document explains **why FactoryFlow exists, what problem it solves, who it serves, what success means, and how the product should evolve without losing its original purpose**.
>
> It is the product-level source of truth for the project vision.
>
> Technical implementation details belong in the architecture, backend, Android, database, API, design, and UX documents.
>
> This document must remain understandable to:
>
> - engineers
> - internship supervisors
> - recruiters
> - academic evaluators
> - future contributors
> - AI coding agents
>
> A reader should understand the business problem before reading a single line of code.

---

# 1. Project Name

**FactoryFlow**

---

# 2. Official Project Title

**FactoryFlow: A Mobile Platform for Intelligent Industrial Maintenance KPI Collection and Automated Reporting**

An academic/report variation may use:

> **Design and Development of FactoryFlow: A Mobile Platform for Intelligent Industrial Maintenance KPI Collection and Automated Reporting**

The shorter title remains the primary product title.

---

# 3. Why FactoryFlow Exists

FactoryFlow originates from a real industrial maintenance reporting problem.

The project was not invented to demonstrate Android, Spring Boot, OCR, dashboards, or document generation.

Those technologies were selected only after the operational problem was understood.

The current workflow depends heavily on KPI information received through WhatsApp groups.

Different operators or contributors send maintenance/production indicators in free-form messages.

The messages are inconsistent.

They may use:

```text
:
=
->
spaces
different line orders
different capitalization
different decimal notation
abbreviations
minor spelling mistakes
```

A KPI may appear as:

```text
Choline : 295456
```

or:

```text
Choline -> 295456
```

or simply:

```text
Choline 295456
```

Another field may appear as:

```text
Vrac : 15,8
```

while another sender writes:

```text
Vrac 15.8
```

A typo may produce:

```text
Varc
```

instead of:

```text
Vrac
```

Some messages contain a full report.

Some contain only a subset of expected KPIs.

Some values are left empty.

An empty field may mean:

> the value was not reported

It must not be interpreted automatically as zero.

The maintenance engineer must manually read these messages, identify useful values, copy or rewrite them into Excel, verify the result, and prepare recurring reporting files.

FactoryFlow exists to reduce this repetitive work while preserving the engineer's authority over official data.

---

# 4. The Original Business Need

The underlying supervisor need can be summarized as:

> Centralize these indicators in a mobile application instead of leaving them fragmented across messages and reporting files.

The important word is **centralize**.

The application must create one reliable place where maintenance KPI information can be:

```text
acquired
structured
reviewed
confirmed
stored
searched
visualized
reported
```

This is more than digitizing Excel.

It is a redesign of the reporting workflow.

---

# 5. Existing Workflow

The current manual process is conceptually:

```text
WhatsApp groups
      ↓
Unstructured KPI messages
      ↓
Maintenance engineer reads message
      ↓
Engineer interprets naming/format
      ↓
Engineer extracts values manually
      ↓
Engineer opens reporting workbook
      ↓
Engineer finds correct cells/sections
      ↓
Engineer types or pastes values
      ↓
Engineer verifies entries
      ↓
Engineer prepares final report
```

This process is workable, but it creates repeated low-value manual effort.

---

# 6. Problems With the Existing Workflow

## 6.1 Repetitive Manual Copying

The engineer performs the same transfer process repeatedly.

This consumes time that could be used for higher-value maintenance analysis.

---

## 6.2 Formatting Variability

The data source is human-generated WhatsApp text.

The system cannot assume one rigid template.

---

## 6.3 Transcription Risk

Manual copying introduces opportunities for:

- omitted values
- mistyped numbers
- misplaced values
- decimal mistakes
- wrong field assignment

---

## 6.4 Weak Traceability

Traditional copy/paste into Excel makes it harder to answer later:

```text
Where did this value come from?
Was it corrected?
Who confirmed it?
When was it confirmed?
```

---

## 6.5 Fragmented History

Information exists across messages and files rather than one structured, searchable history.

---

## 6.6 Manual Reporting Effort

Excel preparation remains a repetitive task even after the source values are known.

---

## 6.7 Limited Operational Visibility

Without centralized structured data, it is harder to provide:

- current status
- missing report alerts
- trends
- recent activity
- scheduled reporting visibility

---

# 7. FactoryFlow's Product Response

FactoryFlow replaces the fragmented workflow with:

```text
Acquire information
      ↓
OCR when the source is an image
      ↓
Normalize source text
      ↓
Recognize KPI labels
      ↓
Extract candidate values
      ↓
Evaluate confidence / warnings
      ↓
Human review
      ↓
Correction if required
      ↓
Explicit confirmation
      ↓
Structured persistence
      ↓
Dashboard / History / Statistics
      ↓
Excel / PDF reporting
      ↓
Scheduled generation / delivery
```

The goal is not to remove human judgment.

The goal is to remove unnecessary manual transfer.

---

# 8. Core Product Formula

FactoryFlow is based on:

```text
AUTOMATION
+
HUMAN VALIDATION
+
TRACEABILITY
=
TRUSTWORTHY INDUSTRIAL REPORTING
```

This formula is central to the entire project.

---

# 9. What "Intelligent" Means

The word **intelligent** does not mean that FactoryFlow must use a Large Language Model.

In the current project, intelligence comes from:

- flexible acquisition
- configurable KPI recognition
- deterministic fuzzy matching
- normalization
- confidence evaluation
- plausibility warnings
- workflow automation
- scheduling
- structured history
- operational dashboarding

The platform behaves intelligently without allowing probabilistic AI to decide official industrial values.

---

# 10. Why AI Is Not Used for Official KPI Extraction

Large Language Models are useful for many tasks.

However, official KPI extraction has a different requirement:

```text
explainability
repeatability
traceability
controlled uncertainty
```

A probabilistic model may produce a plausible answer that is not necessarily the correct answer.

For official reporting, that is not acceptable.

Therefore the approved ingestion architecture uses:

```text
deterministic parser
+
configurable vocabulary
+
fuzzy matching
+
warnings
+
human confirmation
```

AI may be added later for use cases where probabilistic behavior is appropriate.

---

# 11. Human-in-the-Loop Principle

The user is not merely an approver at the end.

Human review is the real integrity mechanism.

Automatically extracted values are:

```text
candidates
```

not:

```text
official data
```

The maintenance engineer can:

- edit a value
- remove a false match
- add a missing KPI
- resolve an unknown line
- save a draft
- confirm the report

Only confirmed final values become authoritative.

---

# 12. Zero-Margin-for-Error Interpretation

The project may be described as targeting a zero-margin-for-error workflow.

This must not be misrepresented as:

> OCR and parsing are mathematically perfect.

The real design is:

```text
Automation reduces repetitive work
        +
Warnings expose uncertainty
        +
Human confirmation verifies final values
        +
Auditability preserves traceability
```

This is the mechanism that protects reporting accuracy.

---

# 13. Primary Users

Initial expected users:

```text
2–4 maintenance staff
```

Current effective role:

```text
Maintenance Engineer
```

The first version intentionally avoids:

```text
Admin
Supervisor
Operator
complex RBAC
```

unless real requirements later justify them.

The user model should remain simple.

---

# 14. Why Mobile

The source workflow already happens around WhatsApp and mobile communication.

A mobile application therefore offers important advantages:

- direct access from the device receiving source information
- Android Share Intent
- gallery import
- backend PaddleOCR
- notifications
- quick review
- report access while mobile

The mobile app fits the operational context better than forcing the engineer to return to a desktop for every data capture action.

---

# 15. Why Android-Only

The confirmed scope is Android.

Therefore FactoryFlow uses native Kotlin instead of Flutter.

This decision supports deeper integration with:

- Android Share Intent
- FileProvider
- PaddleOCR backend runtime
- Firebase Cloud Messaging
- Android lifecycle
- system permissions
- system share sheet

Cross-platform complexity is unnecessary for the current requirement.

---

# 16. Why a Backend Exists

FactoryFlow is not a standalone local mobile form.

A backend is required for:

- centralized business rules
- authentication
- authoritative data persistence
- report history
- multi-user consistency
- report generation
- scheduling
- scheduled email
- statistics
- notifications
- realtime events
- auditability

The backend makes FactoryFlow a true information system rather than a local utility app.

---

# 17. Why Spring Boot

Spring Boot was selected because it provides a mature ecosystem for:

- security
- REST
- JPA
- validation
- WebSocket/STOMP
- Quartz
- email
- observability
- messaging if later required

It also provides strong enterprise engineering value for the portfolio.

---

# 18. Why PostgreSQL

The data is relational and auditable.

FactoryFlow needs relationships between:

- users
- reports
- KPI definitions
- KPI entries
- generated documents
- schedules
- notifications
- audit records

PostgreSQL is therefore a natural authoritative datastore.

---

# 19. Product Scope

FactoryFlow's core scope includes:

- authentication
- dashboard
- KPI definition management
- manual KPI entry
- pasted WhatsApp text
- gallery image import
- Android Share Intent image import
- gallery image acquisition
- private backend OCR
- deterministic KPI parser
- fuzzy label recognition
- numeric extraction
- confidence/warnings
- human confirmation
- draft reports
- PostgreSQL persistence
- report history
- search/filter
- Excel generation
- PDF generation
- daily/weekly/monthly scheduling
- native report sharing
- device-side email composition
- scheduled backend email
- realtime updates
- push notifications
- basic statistics/trends

---

# 20. Five Acquisition Methods

FactoryFlow supports five acquisition paths.

## 20.1 Manual Entry

The engineer selects KPI definitions and enters values directly.

---

## 20.2 Paste Text

The engineer pastes the original WhatsApp message.

---

## 20.3 Gallery Image

The engineer selects an existing screenshot/photo.

The Android app runs OCR.

---

## 20.4 Android Share Intent

The engineer shares an image directly from WhatsApp or another Android app into FactoryFlow.

---

## 20.5 Shared and Gallery Images

The engineer selects or shares the relevant report image from Android.

---

# 21. One Unified Pipeline

The architectural value is not merely having five buttons.

All paths converge into one trusted workflow.

```text
Manual
Paste
Gallery
Share
   ↓
Common reporting pipeline
   ↓
Human confirmation
   ↓
Authoritative persistence
```

For images:

```text
Image
→ PaddleOCR API
→ Raw Text
```

For text:

```text
Raw Text
```

Then:

```text
Normalization
→ Label Recognition
→ Value Extraction
→ Confidence / Warnings
→ Confirmation
```

Manual entry may bypass parser recognition, but it still follows validation and persistence rules.

---

# 22. Parser Vision

The parser must tolerate the actual source environment.

It should handle:

- field reordering
- `:`
- `=`
- `->`
- no explicit separator
- decimal comma
- decimal point
- aliases
- case variation
- minor typos
- partial reports
- missing values
- unknown lines
- plausible-range warnings

The parser should become stronger over time through regression tests based on anonymized real examples.

---

# 23. Configurable KPI Vocabulary

KPI knowledge should live in configurable definitions rather than source code.

A KPI definition may include:

```text
code
display name
category
unit
aliases
plausible minimum
plausible maximum
active status
```

This makes the parser adaptable without continuously rewriting matching logic.

---

# 24. Raw Source Preservation

FactoryFlow must preserve the source text.

For paste:

```text
original pasted message
```

For images:

```text
OCR-extracted text
```

User correction must not erase the original extraction context.

This enables:

- traceability
- debugging
- parser improvement
- auditability

---

# 25. Missing Is Not Zero

This is a core business rule.

```text
missing
```

and:

```text
0
```

are different.

The application must never silently convert a missing KPI into zero.

---

# 26. Partial Reports

A source may contain only some KPI values.

FactoryFlow should still:

- extract known values
- preserve missing state
- show appropriate warning/context
- allow confirmation

The system should not reject valid partial information merely because it is incomplete.

---

# 27. Drafts

Mobile work can be interrupted.

A maintenance engineer may:

- receive a call
- switch apps
- lose connectivity
- pause validation
- return later

Drafts therefore preserve work-in-progress.

A draft should retain enough state to resume:

- source
- extraction result
- edits
- warnings
- acquisition method

---

# 28. Dashboard Vision

The Dashboard is the application home.

It should answer:

```text
What is happening today?
What is missing?
What needs attention?
What are the latest KPI values?
What happened recently?
What is scheduled next?
```

It is not simply a collection of charts.

It is an operational control surface.

---

# 29. Dashboard Core Content

Potential high-value sections:

- today's report status
- latest confirmed KPI values
- missing report warning
- draft/pending review
- recent activity
- latest generated reports
- quick actions
- basic trends
- upcoming schedules
- notifications

Only useful information should appear.

---

# 30. History Vision

FactoryFlow should create a structured operational history.

Users should be able to locate reports by:

- date
- type
- submitter
- status
- KPI

This replaces dependence on manually searching old WhatsApp messages or report files.

---

# 31. Reporting Vision

Reporting is a first-class product capability.

FactoryFlow generates:

```text
Excel
PDF
```

The goal is not to produce raw exports.

The generated files should be professional enough for operational use and presentation.

---

# 32. Why Excel

Excel remains useful because it is:

- editable
- familiar
- analytical
- widely used in industrial environments

Apache POI is used for generation.

---

# 33. Why PDF

PDF provides a fixed presentation/distribution format.

It is suitable for:

- management-facing reports
- sharing
- archiving
- fixed visual presentation

Apache PDFBox is selected.

---

# 34. Why PDFBox Instead of iText

PDFBox avoids the licensing concerns associated with iText's AGPL/commercial model for this project.

The choice should be documented honestly as an engineering/licensing decision.

---

# 35. Report Storage Vision

The initial version uses local backend file storage.

The business logic should not depend directly on disk paths.

Use an abstraction:

```text
ReportStorageService
```

with:

```text
LocalReportStorageService
```

as the first implementation.

Future S3-compatible storage remains possible.

MinIO is not required initially.

---

# 36. Scheduling Vision

Recurring reporting should be automated.

FactoryFlow supports:

```text
Daily
Weekly
Monthly
```

scheduled generation.

Quartz is selected because the requirement is scheduling.

Spring Batch was intentionally removed because the project does not require a full batch-processing framework.

---

# 37. User-Initiated Sharing

When an engineer is viewing a generated report, the mobile application should support native sharing.

Examples:

- share PDF
- share Excel
- open email app with attachment

Use:

```text
Android Share Intent
+
FileProvider
```

The user chooses where the file goes.

---

# 38. Scheduled Automatic Email

This is different from mobile sharing.

When Quartz generates a scheduled report without a user actively present, the backend may send the file automatically using:

```text
JavaMailSender
```

These two workflows must remain separate.

---

# 39. Realtime Vision

Spring WebSocket/STOMP may provide realtime updates for:

- report confirmed
- report generated
- notification created
- dashboard-relevant change

Realtime should improve freshness without replacing REST as the authoritative data mechanism.

---

# 40. Push Notification Vision

FCM may notify users about:

- generated report ready
- missing report
- threshold exceeded
- reminder
- scheduled report completed
- delivery issue

Notifications attract attention.

They do not own business state.

---

# 41. Statistics Vision

FactoryFlow should provide practical descriptive insight from confirmed historical KPI data.

Initial examples:

- daily evolution
- weekly averages
- monthly averages
- min/max
- variation
- simple trends

The project should not claim predictive analytics until prediction is genuinely implemented.

---

# 42. Future AI Vision

AI remains a future enhancement.

A strong future use case is natural-language querying over already validated data.

Example:

> What was the average value of KPI X last month?

or:

> Show the weeks where KPI Y was outside its expected range.

This preserves the core integrity boundary.

AI may help query confirmed history.

AI must not silently decide what an ambiguous WhatsApp message meant for official reporting.

---

# 43. Future Predictive Maintenance

Potential future work may include:

- anomaly detection
- forecasting
- predictive maintenance signals

This is outside the current core implementation.

---

# 44. Future Enterprise Integration

Possible future integrations:

- SAP
- ERP
- multi-site deployment
- larger role model

None should be introduced without real business need.

---

# 45. Technologies Intentionally Removed or Deferred

## Spring Batch

Removed.

Reason:

```text
Scheduling requirement != batch framework requirement
```

Quartz is sufficient.

---

## MinIO

Removed from initial version.

Reason:

Local storage abstraction is sufficient for the first scope.

---

## Docker

Not a mandatory requirement.

It may be added later if deployment needs justify it.

Do not add it for appearance.

---

## Complex RBAC

Not required for the initial single-role workflow.

---

## LLM Parsing

Rejected for official KPI extraction.

---

# 46. Optional Advanced Architecture

RabbitMQ is optional.

It is not required by the initial user scale.

It may be added later if it creates deliberate architectural/learning value for asynchronous report generation.

The project must be honest about this motivation.

---

# 47. Resilience4j

Resilience4j is optional and should only protect genuine failure boundaries such as:

- SMTP
- FCM
- RabbitMQ publishing

It should not be added decoratively.

---

# 48. Observability

Prometheus/Grafana/Actuator may be added later to demonstrate:

- API latency
- JVM behavior
- report generation time
- parser duration
- error rates
- DB pool health

Observability must not delay core product completion.

---

# 49. Performance Testing

k6 may be used for:

- baseline
- load
- stress/spike if time permits

Tests should reflect realistic usage.

The initial deployment is small.

The goal is to demonstrate disciplined measurement, not pretend millions of concurrent users exist.

---

# 50. Architecture Philosophy

FactoryFlow should be:

- maintainable
- modular
- testable
- cleanly layered
- easy to explain

But it should not become overengineered.

The project values:

```text
SOLID
Separation of Concerns
DRY
KISS
clear boundaries
```

without unnecessary enterprise theater.

---

# 51. Product Architecture at a Glance

```text
Android Application
    │
    ├── REST
    ├── WebSocket/STOMP
    └── FCM
    │
Spring Boot Backend
    │
    ├── Authentication
    ├── Parser / Validation
    ├── Report Management
    ├── Dashboard / Statistics
    ├── Report Generation
    ├── Scheduling
    ├── Email
    └── Optional Messaging
    │
PostgreSQL
    │
Generated Report Storage
```

---

# 52. Android Architecture at a Glance

```text
Jetpack Compose
      ↓
ViewModel
      ↓
Repository
      ↓
Remote / Local Data Sources
      ↓
Retrofit / Room
```

Android-specific integrations include:

- PaddleOCR backend OCR
- Share Intent
- FileProvider
- FCM

---

# 53. Backend Technology Baseline

Approved core stack:

```text
Java
Spring Boot
Spring Security
Spring Data JPA
PostgreSQL
Flyway
MapStruct
Spring WebSocket/STOMP
Apache POI
Apache PDFBox
Quartz
JavaMailSender
OpenAPI/Swagger
```

Optional/late:

```text
RabbitMQ
Resilience4j
Prometheus
Grafana
k6
```

---

# 54. Android Technology Baseline

```text
Kotlin
Jetpack Compose
Material 3
MVVM
Repository
Hilt
Retrofit
Room
Coroutines
Flow / StateFlow
Navigation Compose
PaddleOCR backend OCR
Android Share Intent
FileProvider
FCM
```

---

# 55. Visual Product Vision

FactoryFlow should feel premium.

Premium means:

- clean hierarchy
- restrained color
- strong spacing
- polished states
- smooth motion
- professional typography
- thoughtful interactions
- excellent mobile behavior

It should not feel:

- flashy
- game-like
- overdesigned
- generic enterprise grey
- like a student Material template

The detailed visual system belongs in `DESIGN.md`.

---

# 56. Design Inspiration

Approved quality references include:

- Apple for restraint, hierarchy, whitespace, polish
- Material 3 for Android-native behavior
- Linear for productivity dashboards
- Stripe for forms and trust
- Notion for structured information

FactoryFlow must not copy proprietary design identity.

---

# 57. UX Product Vision

The strongest FactoryFlow experience is:

```text
WhatsApp screenshot
→ Share to FactoryFlow
→ OCR
→ parser
→ review
→ edit one uncertain value
→ confirm
→ dashboard updates
→ generate report
→ share
```

This flow should become the project's signature demonstration.

---

# 58. Data Integrity Vision

FactoryFlow must always distinguish:

```text
raw source
extracted value
final confirmed value
```

These are not interchangeable.

The final confirmed value is authoritative.

---

# 59. Authoritative Data

Official downstream features use confirmed values:

- Dashboard
- Statistics
- Generated Excel
- Generated PDF
- Historical reporting

Draft/raw candidates should not contaminate official statistics.

---

# 60. Conceptual Data Model

Core conceptual entities include:

```text
User
KPIDefinition
MaintenanceReport
KPIEntry
GeneratedReport
Schedule
Notification
AuditLog
```

Additional entities may be introduced when implementation requires them.

---

# 61. Initial Database Baseline

Conceptually:

```text
users
kpi_definitions
maintenance_reports
kpi_entries
generated_reports
audit_log
```

Later:

```text
refresh tokens
schedules
notifications
device tokens
```

where required.

---

# 62. Conceptual Report Lifecycle

```text
Input
  ↓
Analyze
  ↓
Draft / Pending Review
  ↓
Human Confirmation
  ↓
Confirmed
  ↓
Dashboard / History / Statistics
  ↓
Generated Excel / PDF
```

Generated documents are different objects from maintenance reports.

---

# 63. Success Criteria

FactoryFlow succeeds when:

- KPI collection becomes significantly faster than manual copying
- heterogeneous source messages can be handled robustly
- uncertainty is visible
- users can correct extracted data easily
- official data cannot bypass human confirmation
- history becomes searchable
- reports can be generated professionally
- recurring reporting can be automated
- the mobile workflow is fast and polished
- the codebase remains understandable
- project documentation is strong enough for a new engineer or AI agent to continue
- the developer can explain and defend the architecture

---

# 64. Failure Criteria

The project has failed its vision if it becomes:

- an OCR demo
- a CRUD app
- a fake AI project
- a dashboard with mock data
- a parser that silently guesses
- an Android shell disconnected from backend
- an overengineered infrastructure experiment
- a collection of unfinished technologies
- a visually polished but unreliable prototype
- a backend-only project with poor mobile workflow

---

# 65. Delivery Philosophy

The project has an approximately three-week initial implementation window.

Therefore:

```text
finished core
>
unfinished optional sophistication
```

Priority should remain:

1. trusted KPI workflow
2. complete Android/backend integration
3. report generation
4. dashboard/history
5. scheduling/sharing
6. OCR acquisition
7. realtime/notifications
8. optional messaging/observability

---

# 66. Portfolio Vision

FactoryFlow should become a flagship portfolio project.

It should show that the developer can:

- understand a real industrial workflow
- translate it into requirements
- design architecture
- build native Android
- build enterprise backend
- design relational data
- secure APIs
- implement deterministic parsing
- integrate OCR
- design human validation
- automate reporting
- schedule jobs
- handle realtime updates
- test critical logic
- document decisions
- build professional UX
- use Git professionally

---

# 67. Academic Report Vision

FactoryFlow is complementary to the primary internship project and will likely receive limited report space.

Therefore report coverage should focus on high-value concepts:

- original business problem
- unified acquisition pipeline
- deterministic parser
- human-in-the-loop validation
- mobile/backend architecture
- automated reporting
- one or two advanced engineering features if completed

Do not spend report pages on framework boilerplate.

---

# 68. Report Evidence to Preserve

During implementation capture:

- anonymized source message examples
- acquisition pipeline diagram
- confirmation screen screenshot
- dashboard screenshot
- Swagger/API proof
- Excel screenshot
- PDF screenshot
- Quartz sequence
- OCR/Share Intent demo
- realtime/FCM evidence if implemented
- k6/Grafana only if genuinely measured

---

# 69. GitHub Vision

The public repository should eventually explain:

```text
Why FactoryFlow exists
How it works
Architecture
Main workflows
Technology stack
Screenshots
Testing
Engineering decisions
How to run it
Roadmap
```

The README should tell the project story before listing technologies.

---

# 70. Demo Vision

Ideal portfolio demonstration:

```text
1. Open Dashboard
2. Show current status
3. Share/import a WhatsApp screenshot
4. OCR runs
5. Parser identifies KPI values
6. One value is highlighted as uncertain
7. Engineer corrects it
8. Confirm
9. Dashboard updates
10. Generate PDF/Excel
11. Share report
```

This communicates both business value and technical depth quickly.

---

# 71. Engineering Learning Vision

FactoryFlow is also a learning vehicle.

The developer should be able to explain:

- Spring Security
- JWT/refresh token flow
- JPA relationships
- Flyway
- MapStruct
- deterministic fuzzy matching
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

Generated code is not enough.

The architecture must be understood.

---

# 72. Non-Goals

The current project is not trying to become:

- a full CMMS
- an ERP
- an SAP replacement
- a predictive maintenance suite
- a multi-platform mobile product
- a web portal
- a data warehouse
- a BI platform
- a general-purpose OCR engine
- a chat application
- an LLM product

These may inspire future directions, but they are not the current objective.

---

# 73. Product Boundary Question

When evaluating a new feature, ask:

> Does this help the maintenance engineer collect, validate, centralize, understand, trace, automate, or report maintenance KPI information?

If not, it probably does not belong in the current scope.

---

# 74. Central Product Promise

FactoryFlow promises:

> **Less repetitive reporting work without sacrificing control over official data.**

Every major feature should strengthen this promise.

---

# 75. Final Vision Statement

FactoryFlow should transform an everyday industrial maintenance reporting problem into a complete, polished, technically defensible information system.

The final product should demonstrate that a fragmented manual workflow can be redesigned into:

```text
centralized acquisition
+
deterministic structuring
+
human verification
+
trusted persistence
+
operational visibility
+
automated reporting
```

The strongest outcome is not simply that the application runs.

The strongest outcome is that a maintenance engineer can immediately understand why it is useful, trust how it handles uncertainty, complete reporting work faster, and remain in control of the final data.

That is the reason FactoryFlow exists.

---

# End of 01_Project_Vision.md
