# TASKS.md

> **FactoryFlow — Living Engineering Roadmap**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-11
>
> This document defines what FactoryFlow must become, why each major capability exists,
> what has already been completed, what must be built next, how tasks should be validated,
> what Git history should record, and which implementation evidence should be preserved
> for the internship report and GitHub portfolio.
>
> `TASKS.md` is not a feature wish list.
>
> It is the execution plan for turning the real industrial maintenance problem that
> motivated FactoryFlow into a finished, reliable Android + Spring Boot platform.

---

# 1. Why We Are Building FactoryFlow

This section must never be removed.

The project must always remain connected to the real problem that created it.

FactoryFlow did **not** begin because we wanted to build:

- an Android application
- an OCR demonstration
- a Spring Boot API
- a dashboard
- an Excel generator
- a RabbitMQ project
- a portfolio project

Those became engineering tools **after the business problem was identified**.

The project began because of a real industrial maintenance reporting workflow.

---

## 1.1 Current Industrial Workflow

Maintenance KPI information is currently communicated through WhatsApp groups.

Different people send operational values in free-form messages.

The messages are not standardized.

A message may resemble:

```text
Choline : 295456
Vrac : 15,8
...
Another person may write:

Choline -> 295456
Vrac 15.8

Another may change the order completely.

Another may misspell a KPI:

Varc

instead of:

Vrac

Some messages contain all expected KPIs.

Some contain only part of the report.

Some fields are empty because the value was not reported.

Different people use:

:
=
->
spaces
commas
periods

differently.

There is no reliable universal message template.

1.2 The Maintenance Engineer's Problem

The maintenance team receives these messages and must manually transfer the useful values into structured Excel reporting files.

The engineer therefore performs repetitive work:

Open WhatsApp
      ↓
Find relevant message
      ↓
Read KPI
      ↓
Understand operator formatting
      ↓
Copy value
      ↓
Open Excel
      ↓
Locate correct field
      ↓
Paste / type value
      ↓
Repeat for next KPI
      ↓
Verify values
      ↓
Prepare report

This workflow:

consumes engineering time
requires repetitive manual copying
creates opportunities for transcription mistakes
depends on inconsistent WhatsApp formatting
makes historical information harder to structure
requires manual Excel preparation
provides limited traceability
offers little automation

The maintenance engineer should spend time analyzing maintenance information.

They should not spend a significant portion of their workflow acting as a human copy/paste bridge between WhatsApp and Excel.

1.3 Supervisor Requirement

The underlying business request can be summarized by the supervisor's need:

Centralize these indicators in a mobile application instead of leaving them fragmented in files/messages.

FactoryFlow exists to answer that request professionally.

1.4 FactoryFlow's Response

FactoryFlow transforms the workflow into:

Acquire KPI information
        ↓
Extract text when necessary
        ↓
Understand KPI labels deterministically
        ↓
Extract candidate values
        ↓
Show everything to the engineer
        ↓
Engineer verifies / corrects
        ↓
Confirm
        ↓
Persist structured data
        ↓
Dashboard / History / Statistics
        ↓
Generate professional Excel + PDF reports
        ↓
Schedule recurring reporting
        ↓
Share / email reports

The objective is not:

Remove the human.

The objective is:

Remove repetitive work while keeping human control where correctness matters.

2. Core Product Principle

FactoryFlow follows:

AUTOMATION
+
HUMAN VALIDATION
+
TRACEABILITY
=
TRUSTWORTHY INDUSTRIAL REPORTING

The parser helps.

OCR helps.

Scheduling helps.

Report generation helps.

But the engineer confirms the data.

That is the central integrity mechanism of the application.

No implementation task is allowed to weaken this rule.

3. Product Definition

Official title:

FactoryFlow: A Mobile Platform for Intelligent Industrial Maintenance KPI Collection and Automated Reporting

FactoryFlow is an Android-first industrial maintenance information system composed of:

Native Android application
        +
Spring Boot backend
        +
PostgreSQL database
        +
Document generation
        +
Scheduling
        +
Notifications / realtime

Primary users:

Approximately 2–4 maintenance staff

Initial effective role:

Maintenance Engineer

FactoryFlow does not currently need:

Admin
Supervisor
Operator
RBAC hierarchy

Do not invent roles simply to make the project look larger.

4. What Success Looks Like

A successful FactoryFlow demonstration should allow the following story:

Engineer receives KPI information
        ↓
Shares screenshot to FactoryFlow
        OR
imports image
        OR
takes photo
        OR
pastes text
        OR
enters values manually
        ↓
FactoryFlow extracts candidates
        ↓
Engineer verifies values
        ↓
Engineer corrects one uncertain KPI
        ↓
Report is confirmed
        ↓
Dashboard immediately reflects confirmed information
        ↓
Engineer later finds the report in history
        ↓
Excel/PDF is generated
        ↓
Report is downloaded/shared

And independently:

Quartz schedule becomes due
        ↓
FactoryFlow determines reporting period
        ↓
Report is generated automatically
        ↓
Files are stored
        ↓
Users are notified
        ↓
Backend email delivery occurs

If these workflows feel complete and polished, the project is succeeding.

5. Development Constraints
5.1 Delivery Window

FactoryFlow is being built under an approximately three-week initial implementation window.

Therefore priorities are:

1. Correct core workflow
2. Complete integrations
3. Good architecture
4. Strong UX
5. Critical testing
6. Documentation
7. Advanced optional technologies

Optional technology must never destroy core completion.

5.2 Portfolio Objective

FactoryFlow should demonstrate more than CRUD development.

The project should provide evidence of:

information systems engineering
business-process analysis
backend architecture
native Android development
relational database design
API design
security
deterministic parsing
fuzzy matching
OCR
document generation
scheduling
notifications
real-time communication
asynchronous architecture if justified
observability
testing
professional Git usage
UI/UX design
technical documentation
5.3 Academic Report Constraint

FactoryFlow is complementary to the main internship project.

Its final report coverage is expected to be limited compared with the primary project.

Therefore development must capture high-value evidence while building rather than attempting to reconstruct everything later.

Focus report material on:

Problem
Architecture
Acquisition pipeline
Parser
Human validation
Automation
Mobile/backend integration
Reporting
Relevant advanced engineering decisions

Do not waste report space documenting boilerplate CRUD code.

6. Task Status Convention

Use:

[ ] Not Started
[-] In Progress
[x] Completed
[!] Blocked
[~] Deferred

A task becomes [x] only when all applicable Definition of Done requirements in AGENTS.md are satisfied.

Code generation alone does not mean completion.

7. Priority Convention

Tasks may additionally be classified:

MUST
SHOULD
COULD
FUTURE

Meaning:

MUST

Required for the core FactoryFlow product.

SHOULD

Strong product/portfolio value after the core stabilizes.

COULD

Optional enhancement.

FUTURE

Explicitly outside the current implementation.

8. Current Project State
Documentation Foundation
[x] Define FactoryFlow project identity
[x] Define real industrial business problem
[x] Freeze project title
[x] Decide native Android approach
[x] Decide Spring Boot backend
[x] Decide PostgreSQL
[x] Decide deterministic parser
[x] Decide mandatory human confirmation
[x] Decide five acquisition methods
[x] Decide Excel + PDF reporting
[x] Decide Quartz scheduling
[x] Decide Android native sharing
[x] Decide backend scheduled email
[x] Classify WebSocket/STOMP as a SHOULD after the trusted core
[x] Classify FCM notifications as a SHOULD after the trusted core
[x] Remove Spring Batch from architecture
[x] Remove MinIO from initial architecture
[x] Remove Docker as project requirement
[x] Define RabbitMQ as optional/late architecture enhancement
[x] Define future AI boundary
[x] Create AGENTS.md
[x] Create TASKS.md
[x] Create SKILLS.md
[x] Create DESIGN.md
[x] Create UI_UX.md
[~] Complete the final M0 documentation consistency follow-up


9. Required Documentation Roadmap

The following documents must eventually exist.

FactoryFlow/
├── AGENTS.md
├── TASKS.md
├── SKILLS.md
├── DESIGN.md
├── UI_UX.md
├── README.md
│
└── docs/
    ├── 01_Project_Vision.md
    ├── 03_Architecture.md
    ├── 04_Business_Rules.md
    ├── 05_Database.md
    ├── 06_API.md
    ├── 07_Android.md
    ├── 08_Backend.md
    ├── 09_Report_Guide.md
    ├── 10_Git_Strategy.md
    ├── 11_Coding_Standards.md
    ├── 12_Roadmap.md
    └── 13_UML.md


10. Pre-Implementation Documentation Tasks
FF-DOC-001 — Finish AGENTS.md

Priority: MUST

[x] Define mission
[x] Define project philosophy
[x] Define AI rules
[x] Define architecture constraints
[x] Define security expectations
[x] Define development workflow
[x] Define task workflow
[x] Define Git workflow
[x] Define testing expectations
[x] Define project success/failure
[x] Define reporting principles

Suggested commit:

docs(project): establish FactoryFlow engineering constitution

Report value:

Low direct report value.
High engineering-process/GitHub value.
FF-DOC-002 — Create TASKS.md

Priority: MUST

[x] Document original industrial problem
[x] Document complete execution roadmap
[x] Define milestones
[x] Define task IDs
[x] Define test expectations
[x] Define commit hints
[x] Define report evidence
[x] Track project state

Suggested commit:

docs(roadmap): add FactoryFlow implementation task plan
FF-DOC-003 — Create SKILLS.md

Priority: MUST

Must cover:

[x] Java/Spring Boot standards
[x] Kotlin standards
[x] Compose standards
[x] MVVM
[x] Retrofit
[x] Room
[x] Coroutines / Flow
[x] PostgreSQL / JPA
[x] Flyway
[x] selective MapStruct usage
[x] Spring Security
[x] JWT
[x] parser engineering
[x] testing
[x] report generation
[x] Quartz
[x] WebSocket/STOMP
[x] FCM
[x] RabbitMQ if implemented
[x] error handling
[x] API design
[x] Git discipline
[x] documentation
[x] AI coding expectations

Suggested commit:

docs(engineering): define FactoryFlow implementation standards
FF-DOC-004 — Create DESIGN.md

Priority: MUST before significant Android UI implementation.

Must cover:

[x] Design philosophy
[x] FactoryFlow visual identity
[x] Apple-inspired restraint
[x] Material 3 behavior
[x] Linear/Stripe/Notion inspiration boundaries
[x] Light theme
[x] Dark theme
[x] Color tokens
[x] Typography
[x] Spacing
[x] Radius
[x] Elevation
[x] Icons
[x] Charts
[x] Motion
[x] Accessibility
[x] Component behavior
[x] Form design
[x] Dashboard design
[x] Loading treatment
[x] Error treatment
[x] Empty-state treatment

Suggested commit:

docs(design): define FactoryFlow visual system

Report evidence:

Potential design-system excerpt / UI comparison.
FF-DOC-005 — Create UI_UX.md

Priority: MUST before broad Android screen implementation.

Must define every important screen and flow.

[x] Splash/startup
[x] Login
[x] Dashboard
[x] Acquisition method selection
[x] Paste text
[x] Manual entry
[x] Gallery import
[x] Share Intent
[x] Camera
[x] OCR processing
[x] Analyze state
[x] Confirmation
[x] Draft handling
[x] Report history
[x] Report details
[x] Generated documents
[x] PDF/Excel actions
[x] Search/filter
[x] Notifications
[x] Schedules
[x] Statistics
[x] Settings/profile if included
[x] Loading states
[x] Empty states
[x] Error states
[x] Warning states
[x] Success states
[x] Back-navigation behavior
[x] Motion/transition behavior
[x] Offline/network-loss behavior

Suggested commit:

docs(ux): specify FactoryFlow screens and interaction flows

FF-DOC-006 — Resolve Documentation Consistency

Priority: MUST documentation follow-up — currently deferred

[x] Remove the obsolete handoff and its source-of-truth references
[~] Finish normalization follow-up not required by the active backend foundation
[x] Preserve original WhatsApp assets unchanged and private
[~] Complete a second full consistency audit later
[x] Receive explicit user direction to begin M0/M1 backend implementation

This follow-up was deferred by explicit user direction on 2026-08-11 and is not the active implementation task.

Suggested commit:

docs(project): resolve FactoryFlow specification conflicts

11. Milestone Overview

The implementation roadmap is:

M0 — Foundation & Specifications

M1 — Trusted KPI Core
     Authentication
     KPI definitions
     Parser
     Validation
     Drafts
     Persistence
     Excel

M2 — Operational Product
     Dashboard
     History
     Search/filter
     PDF
     Quartz scheduling
     Sharing/email

M3 — Mobile Acquisition & Realtime
     Gallery OCR
     Share Intent
     CameraX
     ML Kit
     WebSocket/STOMP
     FCM
     Statistics

M4 — Engineering Enhancements
     RabbitMQ if justified
     Resilience4j
     Observability
     k6
     Advanced report polish

M5 — Portfolio & Delivery
     Testing hardening
     UML
     Report evidence
     README
     screenshots
     demo
     final cleanup

Future — AI / Predictive / ERP
12. Milestone 0 — Project Foundation
Objective

Create a stable project structure and specifications before feature implementation expands.

FF-0001 — Create Repository Structure

Priority: MUST

Target:

FactoryFlow/
├── backend/
├── android/
├── docs/
├── diagrams/
├── assets/
├── report/
├── scripts/
├── AGENTS.md
├── TASKS.md
├── SKILLS.md
├── DESIGN.md
├── UI_UX.md
├── README.md
└── .gitignore

Acceptance:

[ ] repository structure clean
[ ] no unrelated previous-project code
[ ] no DosageAnalysis source mixed into FactoryFlow
[ ] .gitignore configured
[ ] README placeholder identifies FactoryFlow correctly
[ ] original WhatsApp screenshots remain unchanged and private in top-level `assets/`

Only sanitized derivatives may later be used in parser fixtures, report evidence,
GitHub, or portfolio/demo material.

Suggested commit:

chore(project): initialize FactoryFlow repository structure

Report value:

None directly.
FF-0002 — Initialize Spring Boot Backend

Priority: MUST

Stack:

Java 21 preferred when compatible with the selected Spring Boot release
Spring Boot
Maven
Spring Web
Spring Validation
Spring Security
Spring Data JPA
PostgreSQL driver
Flyway
MapStruct only where mapping complexity justifies it
OpenAPI/Swagger
Actuator where appropriate

WebSocket/STOMP, FCM, and RabbitMQ must not be added during core bootstrap merely
because they may be used later.

Acceptance:

[x] backend starts
[x] health endpoint works
[x] environment-based configuration structured
[x] no secrets committed
[x] application package uses FactoryFlow naming

Suggested commit:

build(backend): initialize Spring Boot application
FF-0003 — Initialize Native Android Application

Priority: MUST

Stack:

Kotlin
Jetpack Compose
Material 3
MVVM
Repository pattern
Retrofit
Room
Coroutines
Flow / StateFlow
Navigation Compose
CameraX
ML Kit eventually
FCM eventually

Only include dependencies when their milestone begins unless base setup genuinely requires them.

Acceptance:

[ ] Android project builds
[ ] app launches
[ ] package naming finalized
[ ] Compose theme initialized
[ ] navigation shell ready
[ ] all user-facing strings live in Android resources and use professional French

Canonical bottom navigation: `Tableau de bord`, `Rapports`, `Créer`, `Notifications`.
Profile/settings use the top-level profile action, not a fifth bottom item.

Suggested commit:

build(android): initialize native FactoryFlow application

Report evidence:

Early baseline screenshot only if useful for before/after comparison.
FF-0004 — Configure PostgreSQL

Priority: MUST

[x] local development database
[x] Spring datasource configuration
[x] environment-safe credentials
[x] PostgreSQL integration-test strategy
[x] Flyway enabled
[x] clean startup migration

Suggested commit:

build(database): configure PostgreSQL and Flyway
FF-0005 — Establish API Documentation

Priority: MUST

[x] OpenAPI dependency configured
[x] Swagger UI available
[x] API title/version configured
[x] Bearer authentication scheme prepared

Suggested commit:

docs(api): initialize OpenAPI documentation

Report evidence:

Swagger screenshot later when meaningful APIs exist.
13. Milestone 1 — Trusted KPI Core
Objective

Build the most important FactoryFlow capability:

Convert heterogeneous KPI input into structured data without sacrificing human validation.

This milestone contains the project's highest-value business logic.

14. Authentication
FF-1001 — User Entity and Persistence

Priority: MUST

Baseline:

users
-----
id
name
email
password_hash
active
created_at

Important:

DO NOT add unnecessary role column.
Initial FactoryFlow has one effective role.

Acceptance:

[x] User entity
[x] repository
[x] case-insensitive unique email
[x] BCrypt-compatible password storage
[x] migration
[x] tests

Suggested commit:

feat(auth): add user persistence model
FF-1002 — JWT Authentication

Priority: MUST

[ ] login DTO
[ ] login endpoint
[ ] password verification
[ ] access token generation
[ ] authentication filter
[ ] protected API behavior
[ ] consistent unauthorized response

Endpoint concept:

POST /api/auth/login

Suggested commit:

feat(auth): implement JWT authentication

Report evidence:

Swagger authenticated request screenshot if useful.
FF-1003 — Refresh Token Flow

Priority: MUST

[ ] refresh token model/strategy
[ ] rotation/lifecycle decision
[ ] refresh endpoint
[ ] expiry handling
[ ] invalid token handling
[ ] tests

Endpoint:

POST /api/auth/refresh

Suggested commit:

feat(auth): implement refresh token workflow
FF-1004 — Android Login

Priority: MUST

[ ] login screen
[ ] email validation
[ ] password validation
[ ] loading state
[ ] invalid credentials state
[ ] network error
[ ] Retrofit integration
[ ] secure token persistence
[ ] authenticated app session
[ ] logout behavior

Suggested commit:

feat(android-auth): implement authenticated login flow

Report evidence:

Final login screenshot if visually strong.
15. KPI Definition Management
FF-1101 — Create KPI Definition Model

Priority: MUST

Baseline:

kpi_definitions
---------------
id
code
display_name
category
unit
plausible_min
plausible_max
aliases
active

Acceptance:

[ ] entity
[ ] migration
[ ] repository
[ ] service
[ ] DTOs
[ ] validation

Suggested commit:

feat(kpi): add configurable KPI definitions

Report importance:

HIGH

This demonstrates that parser vocabulary is configuration-driven,
not hardcoded.
FF-1102 — KPI Definition API

Priority: MUST

Conceptual endpoints:

GET  /api/kpi-definitions
POST /api/kpi-definitions

Potential update/deactivation endpoints should be added when needed.

Acceptance:

[ ] list definitions
[ ] active filtering
[ ] create/update behavior
[ ] alias support
[ ] plausible ranges
[ ] unit handling
[ ] Swagger documentation

Suggested commit:

feat(kpi): expose KPI definition management API
FF-1103 — Seed Initial KPI Catalog

Priority: MUST

Use real project vocabulary only when safe.

If company-sensitive values exist, anonymize public examples.

[ ] initial canonical KPI labels
[ ] aliases
[ ] known spelling variations
[ ] units
[ ] plausible ranges where known

Suggested commit:

chore(kpi): seed initial KPI catalog
16. Parser Core
FF-1201 — Input Normalization

Priority: MUST

Normalize safely:

whitespace
line endings
Unicode variants where needed
label casing
common separator forms
decimal handling preparation

Do not destroy original raw text.

Suggested commit:

feat(parser): implement input normalization

Tests:

[ ] Windows/Linux line endings
[ ] extra whitespace
[ ] casing
[ ] colon
[ ] equals
[ ] arrow separator
[ ] no explicit separator
FF-1202 — KPI Label Recognition

Priority: MUST

Recognition sources:

canonical KPI name
display name
aliases
normalized forms
deterministic fuzzy matching

Example expected resilience:

Vrac
Varc
VRAC
vrac

should be handled appropriately according to configured thresholds.

Suggested commit:

feat(parser): add configurable KPI label matching
FF-1203 — Fuzzy Matching

Priority: MUST

Possible deterministic mechanism:

Levenshtein distance / similarity

Requirements:

[ ] configurable threshold
[ ] exact match preferred
[ ] alias match preferred before weak fuzzy match
[ ] explainable match result
[ ] low-quality matches receive warning

Do not introduce LLM extraction.

Suggested commit:

feat(parser): add deterministic fuzzy KPI matching

Report importance:

VERY HIGH

Explain deterministic parser vs probabilistic LLM.
FF-1204 — Numeric Extraction

Priority: MUST

Must support realistic input:

12.5
12,5
295456
1 250

where business rules permit.

Missing value is not zero.

Acceptance:

[ ] decimal comma
[ ] decimal point
[ ] integer
[ ] invalid numeric content
[ ] empty field
[ ] unit-adjacent value

Suggested commit:

feat(parser): implement robust KPI numeric extraction
FF-1205 — Parser Confidence

Priority: MUST

Extraction result should be able to represent:

matched KPI
source label
candidate value
confidence
warnings
source line

Confidence may incorporate:

label similarity
alias/exact match
value extraction quality
unit compatibility
plausible range

Important:

CONFIDENCE MUST NEVER AUTO-CONFIRM.

Suggested commit:

feat(parser): calculate extraction confidence and warnings
FF-1206 — Plausibility Validation

Priority: MUST

Use:

plausible_min
plausible_max

as warning thresholds.

Out-of-range value:

WARNING

not silently corrected.

Suggested commit:

feat(validation): add KPI plausibility warnings
FF-1207 — Unrecognized Lines

Priority: MUST

The parser must preserve/report content it does not understand.

[ ] unknown lines returned
[ ] no silent discard
[ ] confirmation UI can surface them later
[ ] drafts and confirmed reports persist `UNRESOLVED`, `ASSIGNED`, or `IGNORED` resolution

One source line may produce zero, one, or multiple KPI candidates. Do not force
multi-measurement lines such as `Compresseur 1: 77108-77%` into one composite value.

Suggested commit:

feat(parser): preserve unrecognized source lines
FF-1208 — Partial Reports

Priority: MUST

A message may contain only some KPIs.

Expected:

known values extracted
missing values remain missing
report still analyzable
warning if relevant

Do not generate artificial zeroes.

Suggested commit:

feat(parser): support partial KPI reports
FF-1209 — Parser Analyze API

Priority: MUST

Endpoint:

POST /api/reports/analyze

Conceptual flow:

raw text
    ↓
normalize
    ↓
recognize labels
    ↓
extract values
    ↓
confidence/warnings
    ↓
return analysis result

Important:

This endpoint does NOT make the data authoritative.

Suggested commit:

feat(parser): expose report analysis endpoint
FF-1210 — Parser Regression Test Suite

Priority: MUST

Create anonymized fixtures covering real variations.

Minimum scenarios:

[ ] WhatsApp UI/OCR noise
[ ] multiple visible message bubbles
[ ] `---` / `----` missing markers
[ ] colon separator
[ ] arrow separator
[ ] equals separator
[ ] whitespace separator
[ ] decimal comma
[ ] decimal point
[ ] attached units
[ ] decimal/thousands ambiguity such as `30.197` vs `30197`
[ ] multiple measurements in one line
[ ] typo alias
[ ] fuzzy typo
[ ] missing field
[ ] partial report
[ ] unknown line
[ ] duplicate KPI
[ ] suspicious value
[ ] invalid number
[ ] different KPI order

Suggested commit:

test(parser): add real-world KPI parsing regression suite

Report evidence:

Test matrix / parser result examples.

For MVP, OCR all visible input from one screenshot into one review flow. Do not
automatically split visible WhatsApp bubbles into separate `MaintenanceReport` records;
the engineer controls the final draft during review.
17. Maintenance Report Persistence
FF-1301 — Maintenance Report Model

Priority: MUST

Baseline:

maintenance_reports
-------------------
id
submitted_by
effective_date
submitted_at
raw_text
source
status

Canonical statuses are `DRAFT`, `PENDING_REVIEW`, and `CONFIRMED`. Multiple reports
may share the same `effective_date`; it is distinct from submission and confirmation timestamps.

Source concepts:

paste
gallery_ocr
share_ocr
camera_ocr
manual

Suggested commit:

feat(report): add maintenance report persistence
FF-1302 — KPI Entry Model

Priority: MUST

Baseline:

kpi_entries
-----------
id
report_id
kpi_definition_id
extracted_value
current_value
confidence_score
edited_by_user
final_value
captured_unit
warnings

This model must preserve:

What automation extracted
vs
What engineer finally confirmed

Do not add a separate persisted per-KPI `PENDING` / `VALIDATED` / `REJECTED` /
`CORRECTED` lifecycle for MVP.

Suggested commit:

feat(report): persist extracted and confirmed KPI values

Report importance:

HIGH

Excellent evidence for the human-in-the-loop architecture.
18. Human Confirmation
FF-1401 — Confirmation API

Priority: MUST

Endpoint:

POST /api/reports/{id}/confirm

Requirements:

[ ] accepts reviewed values
[ ] rejects malformed confirmation
[ ] stores final values
[ ] records edits
[ ] marks report confirmed
[ ] transactionally persists report + entries

Suggested commit:

feat(validation): implement report confirmation workflow
FF-1402 — Android Confirmation Screen

Priority: MUST

This is one of the most important screens in FactoryFlow.

Must show:

Original source
Matched KPI
Extracted value
Unit
Confidence
Warnings
Editable final value
Unrecognized lines

User actions:

Edit
Remove false extraction
Add missing KPI
Resolve unknown line where supported
Save draft
Confirm

After successful confirmation, navigate to Report Detail and remove the editable
confirmation destination from the Back stack.

Suggested commit:

feat(android): build KPI confirmation workflow

Report evidence:

VERY HIGH

Capture a polished screenshot showing:
- extracted values
- one low-confidence warning
- one corrected value
19. Draft Reports
FF-1501 — Draft Persistence

Priority: MUST

Endpoint concept:

PUT /api/reports/{id}/draft

Preserve:

raw source
parsed entries
current user edits
source/acquisition method
validation state

Suggested commit:

feat(report): persist resumable report drafts
FF-1502 — Android Draft Resume

Priority: MUST

[ ] interrupted validation can be saved
[ ] draft appears later
[ ] draft reopens with corrections intact
[ ] user can continue confirmation
[ ] no duplicate authoritative report created

Suggested commit:

feat(android): add resumable report validation drafts

Report evidence:

Potential UX screenshot.
20. Manual Entry
FF-1601 — Manual KPI Entry Flow

Priority: MUST

Manual entry must remain available even when parsing/OCR is unavailable.

[ ] choose KPI
[ ] enter value
[ ] show unit
[ ] plausibility warning
[ ] add multiple KPIs
[ ] review
[ ] confirm

Manual entries do not need OCR/parser recognition.

They still follow validation/persistence rules.

Suggested commit:

feat(android): implement manual KPI report entry
21. Text Paste Acquisition
FF-1701 — Paste WhatsApp Text

Priority: MUST

[ ] multiline text field
[ ] clipboard-friendly behavior
[ ] analyze action
[ ] loading
[ ] parser error
[ ] empty text validation
[ ] navigation to confirmation

Flow:

Paste
→ Analyze API
→ Confirmation
→ Confirm

Suggested commit:

feat(android): add WhatsApp text paste acquisition

Report evidence:

Good simple illustration of raw → structured workflow.
22. Excel Reporting
FF-1801 — Report Generation Domain

Priority: MUST

Separate:

maintenance report data

from:

generated document

Do not treat them as the same entity.

Suggested commit:

feat(report): define generated report domain model
FF-1802 — Generated Report Persistence

Baseline:

generated_reports
-----------------
id
type
format
period_start
period_end
generated_at
file_path
generated_by
generation_status
email_delivery_status
version/provenance

Types:

daily
weekly
monthly
manual

Formats:

excel
pdf

Core generation is synchronous. Generation status and email-delivery status remain
separate, so `READY` + `FAILED` is valid. Async `PENDING` / `GENERATING` states require
a later explicit API change. Regeneration creates a new version with provenance.

Suggested commit:

feat(report): persist generated report metadata
FF-1803 — Report Storage Abstraction

Priority: MUST

Interface concept:

ReportStorageService

Initial implementation:

LocalReportStorageService

Example storage:

/reports/excel
/reports/pdf

Do not introduce MinIO.

Suggested commit:

feat(storage): add generated report storage abstraction

Report value:

Architecture decision worth mentioning briefly.
FF-1804 — Apache POI Excel Generator

Priority: MUST

[ ] workbook creation
[ ] report title
[ ] reporting period
[ ] KPI table
[ ] units
[ ] professional styling
[ ] reasonable column widths
[ ] generation timestamp
[ ] deterministic filename
[ ] valid downloadable file

Do not stop at an unformatted table dump.

Suggested commit:

feat(report): generate professional Excel reports with Apache POI

Report evidence:

VERY HIGH

Capture actual workbook screenshot.
FF-1805 — Generated Report API

Priority: MUST

Canonical endpoint:

POST /api/generated-reports

The request supplies a reporting period and format because a daily, weekly, or
monthly document may aggregate multiple maintenance reports. Core generation is
synchronous. Intentional regeneration creates a new version with provenance.

Suggested commit:

feat(report): expose Excel report generation API
23. Milestone 1 Completion Gate

Milestone 1 is successful when this complete flow works:

Login
  ↓
Paste realistic KPI message
  ↓
Analyze
  ↓
Parser handles formatting variation
  ↓
Confirmation screen
  ↓
Engineer corrects one value
  ↓
Save draft
  ↓
Resume draft
  ↓
Confirm
  ↓
PostgreSQL contains raw + extracted + final values
  ↓
Generate professional Excel report

Required evidence:

[ ] parser test suite
[ ] Swagger analyze endpoint
[ ] confirmation UI screenshot
[ ] database persistence proof
[ ] generated Excel screenshot

Suggested milestone tag:

v0.1-trusted-kpi-core
24. Milestone 2 — Operational Product
Objective

Turn the trusted KPI pipeline into a practical daily maintenance application.

This milestone adds:

Dashboard
History
Search
PDF
Scheduling
Sharing
Email
25. Dashboard
FF-2001 — Dashboard Statistics API

Priority: MUST

The dashboard uses confirmed data only.

Potential data:

today status
latest KPI values
pending drafts
latest reports
recent activity
missing information
basic trends
upcoming schedules

Suggested commit:

feat(dashboard): expose operational dashboard summary
FF-2002 — Dashboard Android Screen

Priority: MUST

Dashboard is the post-login home screen.

Must answer:

What is happening today?
What requires attention?
What is missing?
What happened recently?
What can I do next?

Potential sections:

Current KPIs
Today's report status
Quick actions
Latest reports
Recent activity
Warnings
Statistics/trends
Schedules
Notifications

Suggested commit:

feat(android): implement FactoryFlow operational dashboard

Report evidence:

VERY HIGH

Likely one of the primary report/GitHub screenshots.
26. Report History
FF-2101 — Report History API

Priority: MUST

Support:

date
type
submitter
status
KPI

where appropriate.

Pagination when dataset can grow.

Suggested commit:

feat(history): add searchable report history API
FF-2102 — Android Report History

Priority: MUST

[ ] list
[ ] search
[ ] filters
[ ] date range
[ ] empty state
[ ] loading
[ ] error
[ ] report detail navigation

Suggested commit:

feat(android): build searchable report history
27. Generated Document History
FF-2201 — Generated Report List

Priority: MUST

Distinguish:

Maintenance reports

from:

Generated Excel/PDF documents

Suggested commit:

feat(report): expose generated document history
28. PDF Reporting
FF-2301 — Apache PDFBox Generator

Priority: MUST

Professional output:

[ ] title/header
[ ] period
[ ] KPI data
[ ] units
[ ] readable layout
[ ] page numbering if multiple pages
[ ] generation metadata
[ ] deterministic filename

Suggested commit:

feat(report): generate PDF reports with Apache PDFBox

Report evidence:

VERY HIGH

Show sample generated PDF.
FF-2302 — PDF Generation API

Use the same canonical period-based generation contract with `format = PDF`:

POST /api/generated-reports

Suggested commit:

feat(report): expose PDF generation endpoint
29. Android Report Viewing / Downloading
FF-2401 — Generated Report Detail

Priority: MUST

Actions:

View metadata
Download/open
Share
Email

Suggested commit:

feat(android): add generated report actions
30. Device-Side Sharing
FF-2501 — FileProvider

Priority: MUST

[ ] secure content URI
[ ] temporary read permission
[ ] MIME type
[ ] no raw private path exposure

Suggested commit:

feat(android): configure secure generated-file sharing
FF-2502 — Native Share Intent

Priority: MUST

Use:

Intent.ACTION_SEND

Allow user to share generated reports through installed applications.

Suggested commit:

feat(android): share generated reports through native intents
FF-2503 — User-Initiated Email

Priority: MUST

This is not backend SMTP.

Flow:

User taps Email
      ↓
Android prepares attachment
      ↓
Email application opens
      ↓
Attachment + suggested subject/body
      ↓
User selects recipient
      ↓
User sends

Suggested commit:

feat(android): add email sharing for generated reports
31. Quartz Scheduling
FF-2601 — Schedule Domain Model

Priority: MUST

Support:

Daily
Weekly
Monthly

Define:

schedule type
execution time
enabled status
report format/options
business timezone (`Africa/Casablanca`)

Suggested commit:

feat(scheduler): add report schedule model
FF-2602 — Quartz Integration

Priority: MUST

Quartz decides when.

Application services decide what.

Quartz job
→ Report service
→ Generate
→ Store

Allow one recovery execution after one missed run. Prevent duplicate generation for
the same schedule + reporting period + format.

Suggested commit:

feat(scheduler): integrate Quartz report scheduling
FF-2603 — Daily Report Schedule

Priority: MUST

[ ] define daily period semantics
[ ] trigger generation
[ ] persist generated report
[ ] error handling

Suggested commit:

feat(scheduler): automate daily report generation
FF-2604 — Weekly Report Schedule

Priority: MUST

Use calendar weeks from Monday through Sunday.

Suggested commit:

feat(scheduler): automate weekly report generation
FF-2605 — Monthly Report Schedule

Priority: MUST

At the configured time on the first day of a month, generate the complete previous
calendar month. Do not use arbitrary `dayOfMonth = 31` behavior.

Suggested commit:

feat(scheduler): automate monthly report generation
32. Backend Automatic Email
FF-2701 — JavaMailSender Configuration

Priority: SHOULD

[ ] SMTP config externalized
[ ] secrets excluded
[ ] test/development behavior defined

Suggested commit:

feat(email): configure scheduled report delivery
FF-2702 — Scheduled Email Delivery

Priority: SHOULD

Flow:

Quartz
  ↓
Generate report
  ↓
Store successfully
  ↓
Send email

Important:

Report generation success
!=
Email delivery success

Never delete a successful report because email failed.

Suggested commit:

feat(email): deliver scheduled reports automatically

Report evidence:

Good architectural sequence diagram.
33. Milestone 2 Completion Gate

Expected flow:

Confirmed KPI data
      ↓
Dashboard
      ↓
History/search
      ↓
Excel/PDF
      ↓
Manual share/email

AND

Quartz schedule
      ↓
Automatic generation
      ↓
Storage
      ↓
Backend email

Suggested tag:

v0.2-operational-reporting
34. Milestone 3 — Mobile Acquisition
Objective

Eliminate the need for manually retyping information by supporting every practical Android acquisition path.

All methods converge to the same trusted validation pipeline.

35. Gallery OCR
FF-3001 — Gallery Image Picker

Priority: MUST

[ ] choose image
[ ] content URI handling
[ ] preview
[ ] cancellation
[ ] invalid image state

Suggested commit:

feat(android): add KPI screenshot gallery import
FF-3002 — ML Kit OCR Integration

Priority: MUST

OCR occurs on-device.

Image
→ ML Kit
→ Extracted text
→ Analyze API
→ Confirmation

Suggested commit:

feat(ocr): integrate on-device ML Kit text recognition

Report evidence:

VERY HIGH

This is one of the strongest visual demo flows.
36. WhatsApp / Android Share Intent
FF-3101 — Receive Shared Image

Priority: MUST

FactoryFlow should appear as a valid Android share target where appropriate.

WhatsApp image
      ↓
Share
      ↓
FactoryFlow
      ↓
Receive content URI
      ↓
OCR
      ↓
Parser
      ↓
Confirmation

Suggested commit:

feat(android): receive shared KPI screenshots

Report evidence:

VERY HIGH
FF-3102 — Share Intent Error Handling
[ ] unsupported type
[ ] unreadable URI
[ ] missing permissions
[ ] OCR failure
[ ] retry

Suggested commit:

fix(android): harden shared-image acquisition flow
37. CameraX
FF-3201 — Camera Permission UX

Priority: MUST

[ ] request when needed
[ ] denial
[ ] permanent denial
[ ] settings path if appropriate
[ ] app remains usable without camera

Suggested commit:

feat(android): add camera permission workflow
FF-3202 — CameraX Capture

Priority: MUST

Capture
→ preview
→ OCR
→ parser
→ confirmation

Suggested commit:

feat(camera): capture KPI reports with CameraX
38. Unified Acquisition Entry Point
FF-3301 — Acquisition Method Selector

Priority: MUST

Quick options:

Paste text
Manual entry
Gallery
Camera

Share Intent may enter directly from outside the app.

All must converge to the same validation architecture.

Suggested commit:

feat(android): unify KPI acquisition methods

Report evidence:

Useful architecture diagram:

5 acquisition methods → 1 validation pipeline
39. Real-Time Updates
FF-3401 — Spring WebSocket/STOMP Configuration

Priority: SHOULD

[ ] endpoint
[ ] authentication behavior
[ ] topic convention
[ ] connection lifecycle

Suggested commit:

feat(realtime): configure authenticated STOMP messaging
FF-3402 — Report Events

Possible events:

report confirmed
report generated
generation failed
notification created

Suggested commit:

feat(realtime): publish report lifecycle events
FF-3403 — Android STOMP Client

Priority: SHOULD

[ ] connect authenticated
[ ] reconnect safely
[ ] lifecycle handling
[ ] update UI state

Suggested commit:

feat(android): consume real-time FactoryFlow events
FF-3404 — Dashboard Real-Time Refresh

Prefer:

event says state changed
→ Android fetches authoritative updated state

instead of unnecessarily duplicating full domain objects in STOMP messages.

Suggested commit:

feat(dashboard): refresh dashboard from real-time events

Report evidence:

Strong technical demonstration/GIF.
40. Firebase Cloud Messaging
FF-3501 — FCM Integration

Priority: SHOULD

[ ] Android Firebase setup
[ ] device token registration
[ ] backend notification sender
[ ] secure configuration

Suggested commit:

feat(notifications): integrate Firebase Cloud Messaging
FF-3502 — Notification Use Cases

Potential:

Generated report ready
Missing expected report
Threshold warning
Reminder
Scheduled report completed

Suggested commit:

feat(notifications): deliver FactoryFlow operational alerts
FF-3503 — Notification Navigation

Tap notification:

→ relevant screen/report

where feasible.

Suggested commit:

feat(android): add notification deep-link navigation
41. Statistics
FF-3601 — Statistics Backend

Priority: SHOULD

Confirmed data only.

Initial examples:

Daily KPI evolution
Weekly averages
Monthly averages
Variation
Simple trends

No fake predictive analytics.

Suggested commit:

feat(statistics): calculate confirmed KPI trends
FF-3602 — Android Statistics UI

Priority: SHOULD

[ ] charts
[ ] period filters
[ ] readable labels
[ ] empty state
[ ] loading

Suggested commit:

feat(android): visualize KPI statistics and trends

Report evidence:

High if visually polished and based on real confirmed data.
42. Milestone 3 Completion Gate

Demonstrate:

WhatsApp screenshot
→ Share to FactoryFlow
→ ML Kit OCR
→ deterministic parser
→ warning/confirmation
→ user correction
→ save
→ dashboard refresh
→ notification

Suggested tag:

v0.3-mobile-intelligence
43. Milestone 4 — Optional Engineering Enhancements

These tasks do not outrank unfinished core functionality.

44. RabbitMQ
FF-4001 — Decide Final RabbitMQ Use Case

Priority: COULD

Before implementation answer:

What asynchronous boundary improves from RabbitMQ?

Potential candidate:

report generation

Do not implement messaging only to lengthen the stack.

Decision must be documented.

Suggested commit:

docs(architecture): define RabbitMQ async boundary
FF-4002 — RabbitMQ Report Generation

Priority: COULD

Possible flow:

request
→ publish generation command
→ RabbitMQ
→ consumer
→ generate
→ persist
→ WebSocket notification

Suggested commit:

feat(messaging): process report generation asynchronously
FF-4003 — Failure Handling

If RabbitMQ exists:

[ ] acknowledgement strategy
[ ] retry
[ ] duplicate risk
[ ] error status
[ ] dead-letter behavior if justified

Suggested commit:

feat(messaging): harden asynchronous report processing
45. Resilience4j
FF-4101 — Identify Genuine Failure Boundaries

Priority: COULD

Candidates:

SMTP
external FCM interaction
RabbitMQ publishing where useful

Do not wrap internal business methods unnecessarily.

FF-4102 — Add Targeted Resilience Policies

Possible patterns:

retry
circuit breaker
timeout

only where technically justified.

Suggested commit:

feat(resilience): protect external infrastructure boundaries
46. Observability
FF-4201 — Actuator / Micrometer

Priority: COULD

[ ] health
[ ] JVM metrics
[ ] HTTP metrics
[ ] useful application metrics

Suggested commit:

feat(observability): expose FactoryFlow service metrics
FF-4202 — Prometheus

Priority: COULD

Suggested commit:

feat(observability): export metrics to Prometheus
FF-4203 — Grafana

Priority: COULD

Useful panels:

request latency
error rate
JVM
database pool
report generation duration
parser duration

Suggested commit:

feat(observability): add FactoryFlow Grafana dashboard

Report evidence:

Very useful if included.
47. Performance Testing
FF-4301 — k6 Baseline

Priority: COULD

Representative APIs:

login
analyze report
history
statistics

Suggested commit:

test(performance): establish FactoryFlow API baseline
FF-4302 — Load Test
[ ] realistic scenario
[ ] latency
[ ] throughput
[ ] error rate
FF-4303 — Stress / Spike

Only if time permits.

Do not invent impressive numbers.

Record actual observations.

Suggested commit:

test(performance): evaluate API behavior under load
48. Milestone 5 — Hardening and Presentation
49. Security Review
FF-5001
[ ] no secrets
[ ] passwords BCrypt
[ ] JWT expiry
[ ] refresh flow
[ ] protected APIs
[ ] safe Android token storage
[ ] FileProvider security
[ ] input validation
[ ] upload/content URI handling
[ ] logs contain no sensitive tokens

Suggested commit:

chore(security): harden FactoryFlow presentation build
50. Database Review
FF-5101
[ ] migrations work from clean database
[ ] foreign keys
[ ] indexes based on query needs
[ ] enum persistence
[ ] no accidental N+1 hotspots
[ ] historical reports remain valid
[ ] inactive KPI definitions preserve history

Suggested commit:

refactor(database): harden persistence and query behavior
51. API Review
FF-5201
[ ] consistent URL style
[ ] status codes
[ ] validation
[ ] error contract
[ ] pagination
[ ] filters
[ ] Swagger
[ ] no entity exposure

Suggested commit:

refactor(api): standardize FactoryFlow REST contracts
52. Android UX Review
FF-5301

Review every screen against DESIGN.md and UI_UX.md.

[ ] loading
[ ] empty
[ ] error
[ ] success
[ ] warning
[ ] back behavior
[ ] touch targets
[ ] accessibility
[ ] keyboard
[ ] system bars
[ ] dark/light theme if supported
[ ] orientation/configuration behavior where relevant

Suggested commit:

refactor(android): polish FactoryFlow user experience
53. End-to-End Regression
FF-5401 — Text Flow
[ ] Login
[ ] Paste
[ ] Analyze
[ ] Correct
[ ] Draft
[ ] Resume
[ ] Confirm
[ ] Dashboard
[ ] Excel
[ ] PDF
[ ] Share
FF-5402 — Gallery OCR Flow
[ ] Select image
[ ] OCR
[ ] Analyze
[ ] Confirm
[ ] Save
FF-5403 — WhatsApp Share Flow
[ ] Share screenshot
[ ] FactoryFlow opens
[ ] OCR
[ ] Analyze
[ ] Confirm
FF-5404 — Camera Flow
[ ] Camera
[ ] Capture
[ ] OCR
[ ] Analyze
[ ] Confirm
FF-5405 — Scheduled Flow
[ ] Quartz fires
[ ] period correct
[ ] report generated
[ ] stored
[ ] email attempted
[ ] success/failure status correct
[ ] notification

Suggested commit:

test(e2e): validate core FactoryFlow workflows
54. UML Deliverables
FF-5501 — Business Class Diagram

Priority: MUST for documentation/report.

Target approximately 10–15 meaningful concepts.

Candidates:

MaintenanceEngineer
MaintenanceReport
KPIEntry
KPIDefinition
ParserConfiguration
ExtractionResult
GeneratedReport
ReportTemplate
Notification
Schedule
Dashboard
AuditLog

Possible enums:

ReportStatus
ValidationStatus
AcquisitionMethod
ReportFormat
ScheduleType

Important:

Use real UML:
associations
composition where correct
cardinalities
inheritance only when real

Suggested commit:

docs(uml): add FactoryFlow business class diagram
FF-5502 — Architecture Diagram

Show:

Android
→ REST/WebSocket
→ Spring Boot
→ PostgreSQL
→ reporting/storage/email

and optional external services.

Suggested commit:

docs(architecture): add FactoryFlow system diagram
FF-5503 — Acquisition Pipeline Diagram

Very important:

Manual
Paste
Gallery
Share Intent
Camera
   ↓
Unified processing
   ↓
OCR where needed
   ↓
Parser
   ↓
Human confirmation
   ↓
PostgreSQL

Suggested commit:

docs(architecture): document unified KPI acquisition pipeline

Report evidence:

VERY HIGH
FF-5504 — Sequence Diagrams

Recommended:

KPI analysis/confirmation
OCR import
Scheduled report generation

Optional:

WebSocket notification
RabbitMQ generation if implemented
55. GitHub Showcase
FF-5601 — Final README

Priority: MUST

README should explain:

Problem
Solution
Why FactoryFlow exists
Architecture
Features
Stack
Screenshots
Acquisition pipeline
Human validation
Reports
API
Testing
Performance if measured
Setup
Roadmap

Suggested commit:

docs(readme): publish FactoryFlow project showcase
FF-5602 — Screenshots

Capture polished:

Login
Dashboard
Acquisition
Confirmation
History
Statistics
Generated report
Notification

Do not use screenshots from unfinished UI in final README.

FF-5603 — Demo GIF / Video

Priority: SHOULD

Ideal demonstration:

Share WhatsApp screenshot
→ OCR
→ confirmation
→ dashboard
→ report
56. Academic Report Evidence

Maintain evidence while implementing.

Evidence E01 — Industrial Problem

Preserve:

Current WhatsApp → manual Excel workflow

Use anonymized examples.

Evidence E02 — Architecture

Capture final architecture diagram.

Evidence E03 — Unified Acquisition Pipeline

Show five input methods converging to one trusted process.

Evidence E04 — Deterministic Parser

Explain:

normalization
label matching
value extraction
confidence
warnings

and why LLM extraction was rejected.

Evidence E05 — Human-in-the-Loop

Show extracted vs corrected vs final value.

Evidence E06 — Mobile Integration

Show Share Intent / ML Kit / CameraX.

Evidence E07 — Reporting

Show professional Excel and PDF.

Evidence E08 — Automation

Show Quartz sequence.

Evidence E09 — Advanced Engineering

If implemented:

WebSocket
RabbitMQ
FCM
Prometheus
Grafana
k6

Only show technologies that genuinely work.

57. Future Backlog

These must not enter the three-week core sprint.

FF-F001 — Natural-Language Historical KPI Queries

Future AI capability:

"What was the average Choline KPI last month?"

AI operates over already validated historical data.

It does not replace extraction validation.

Status:

[~] FUTURE
FF-F002 — Predictive Maintenance

Potential future:

anomaly detection
forecasting
predictive signals

Status:

[~] FUTURE
FF-F003 — ERP / SAP Integration

Status:

[~] FUTURE
FF-F004 — Multi-Role Authorization

Possible future:

Admin
Supervisor
Engineer

Only if actual business requirements appear.

Status:

[~] FUTURE
FF-F005 — Multi-Site Industrial Deployment

Status:

[~] FUTURE
58. Explicitly Rejected / Removed Scope

This section prevents future AI sessions from resurrecting old ideas.

Spring Batch
REMOVED

Reason:

FactoryFlow requires recurring scheduled workflows, not a full batch-processing framework.

Quartz handles scheduling.

MinIO
REMOVED FROM INITIAL VERSION

Reason:

Local storage abstraction is sufficient.

Use:

ReportStorageService

so a future S3/object-storage implementation remains possible.

Docker
NOT REQUIRED

Do not add Docker only because the backend uses Spring Boot/PostgreSQL.

Deployment needs may change this later.

iText
NOT SELECTED

Use Apache PDFBox.

Licensing concerns make PDFBox more appropriate for the project.

Flutter
NOT SELECTED

FactoryFlow is Android-only.

Native Kotlin provides better alignment with:

Share Intent
CameraX
ML Kit
FileProvider
FCM
Android lifecycle
AI/LLM Parsing
REJECTED FOR OFFICIAL KPI EXTRACTION

Reason:

Official industrial data requires deterministic, explainable processing with mandatory human validation.

Complex RBAC
NOT REQUIRED

Initial user model:

Maintenance Engineer
59. Current Source-of-Truth Business Rules

Until replaced by the dedicated Business_Rules.md, preserve these rules.

1. Raw source must be preserved.

2. Empty/missing does not mean zero.

3. Partial reports are valid.

4. Automatically extracted KPI data must be confirmed by a human.

5. Parser confidence does not authorize auto-save.

6. Plausibility-range violations create warnings rather than silent corrections.

7. User correction becomes the authoritative final value.

8. Extracted value should remain available for traceability.

9. Dashboard/statistics use confirmed values.

10. Generated Excel/PDF uses confirmed final values.

11. KPI labels/aliases come from configurable KPI definitions.

12. Unrecognized input must not disappear silently.

13. All acquisition methods converge to the same integrity workflow.

14. Manual entry still obeys validation/persistence rules.

15. Successful report generation and successful email delivery are separate states.

16. Notifications are not the source of truth.

17. WebSocket is not the source of truth.

18. PostgreSQL/backend state is authoritative.

19. Historical information must remain interpretable after KPI configuration changes.

20. No future AI capability may silently redefine confirmed historical data.
60. Current Conceptual Database Baseline

Until docs/05_Database.md becomes authoritative:

users
-----
id
name
email
password_hash
active
created_at
kpi_definitions
---------------
id
code
display_name
category
unit
plausible_min
plausible_max
aliases
active
maintenance_reports
-------------------
id
submitted_by
effective_date
submitted_at
raw_text
source
status
kpi_entries
-----------
id
report_id
kpi_definition_id
extracted_value
current_value
confidence_score
edited_by_user
final_value
captured_unit
warnings
generated_reports
-----------------
id
type
format
generated_at
file_path
generated_by
audit_log
---------
id
user_id
action
entity_type
entity_id
occurred_at

Additional tables for schedules, notifications, refresh tokens, device tokens, etc. should be added deliberately during their corresponding tasks.

61. Current Conceptual API Baseline
POST /api/auth/login
POST /api/auth/refresh

POST /api/reports/analyze
POST /api/reports/drafts
PATCH /api/reports/{id}/draft
POST /api/reports/{id}/confirm

GET  /api/reports

GET  /api/kpi-definitions
POST /api/kpi-definitions

POST /api/generated-reports

GET /api/statistics

Final naming belongs in docs/06_API.md.

Do not allow backend and Android to create competing conventions.

62. Current Parser Pipeline

The canonical parser flow is:

Input Normalization
        ↓
Label Recognition
        ↓
Value Extraction
        ↓
Confidence / Warning Evaluation
        ↓
Validation Screen
        ↓
Database

Image flows prepend:

Image
 ↓
On-device OCR
 ↓
Raw text

Manual flow may bypass recognition/extraction but not confirmation/integrity rules.

63. Current Acquisition Matrix
Acquisition method	OCR	Parser	Human confirmation	Persistence
Manual entry	No	No/limited	Yes	Yes
Paste text	No	Yes	Yes	Yes
Gallery screenshot	Yes	Yes	Yes	Yes
Android/WhatsApp Share	Yes	Yes	Yes	Yes
CameraX photo	Yes	Yes	Yes	Yes

The important architectural fact is not that FactoryFlow has five features.

It is that five different entry paths converge into one trusted reporting pipeline.

64. Current Reporting Matrix
Capability	Technology	Priority
Excel generation	Apache POI	MUST
PDF generation	Apache PDFBox	MUST
Daily schedule	Quartz	MUST
Weekly schedule	Quartz	MUST
Monthly schedule	Quartz	MUST
User sharing	Android Share Intent + FileProvider	MUST
User email	Installed Android email app	MUST
Scheduled automatic email	Spring JavaMailSender	SHOULD
Local generated-file storage	ReportStorageService	MUST
S3/object storage	Future	FUTURE
65. Current Realtime / Async Matrix
Capability	Technology	Priority
Realtime app updates	Spring WebSocket/STOMP	SHOULD
Push notifications	FCM	SHOULD
Async event processing	RabbitMQ	COULD
Retry/circuit breaking	Resilience4j	COULD
Metrics	Micrometer/Prometheus	COULD
Dashboards	Grafana	COULD
Load/performance tests	k6	COULD

Do not allow optional infrastructure to displace the business-critical workflow.

66. Current Sprint Order

When implementation begins, unless a blocking dependency requires adjustment, use:

1. Repository structure
2. Backend bootstrap
3. Android bootstrap
4. PostgreSQL + Flyway
5. Authentication
6. KPI definitions
7. Parser normalization
8. Label matching
9. Numeric extraction
10. Confidence/warnings
11. Parser tests
12. Maintenance report persistence
13. Confirmation API
14. Android paste flow
15. Android confirmation
16. Drafts
17. Manual entry
18. Excel generation
19. Dashboard
20. History/search
21. PDF generation
22. Quartz
23. Sharing
24. Scheduled email
25. Gallery OCR
26. Share Intent acquisition
27. CameraX
28. Realtime
29. FCM
30. Statistics
31. Optional RabbitMQ/resilience
32. Monitoring/performance
33. Hardening
34. UML/report evidence
35. GitHub presentation

This sequence may be updated in TASKS.md as implementation reality changes.

Do not silently reorder the project because a different feature looks more exciting.

67. Current Work
Current milestone:
M0/M1 — Backend Foundation

Current task:
Verified Spring Boot, PostgreSQL/Flyway, OpenAPI, shared error, and user persistence foundation

Last completed:
FF-1001 — User Entity and Persistence

In progress:
None

Next:
FF-1002 — JWT Authentication, only when explicitly started

Primary blocker:
None

Implementation status:
Backend foundation implemented; remaining product features not started

Update this section after every meaningful development session.

68. End-of-Session Update Template

At the end of each Codex session, update:

Date:

Completed:
-

Tests executed:
-

Build result:
-

Current issue/blocker:
-

Next task:
-

Suggested/actual commit:
-

Report evidence captured:
-
69. Final Product Acceptance Checklist

FactoryFlow cannot be considered presentation-ready until:

[ ] Real business problem clearly demonstrated

[ ] Authentication works

[ ] Five KPI acquisition methods work

[ ] OCR works on-device

[ ] Deterministic parser handles realistic variations

[ ] Parser regression tests exist

[ ] Human confirmation cannot be bypassed

[ ] Drafts work

[ ] PostgreSQL stores trusted structured values

[ ] Dashboard uses confirmed data

[ ] History/search/filter works

[ ] Excel report is professional

[ ] PDF report is professional

[ ] Daily Quartz scheduling works

[ ] Weekly Quartz scheduling works

[ ] Monthly Quartz scheduling works

[ ] Android user sharing works

[ ] Scheduled backend email works

[ ] Swagger/OpenAPI is complete

[ ] Critical security review complete

[ ] Core tests pass

[ ] Android UI follows DESIGN.md

[ ] Android flows follow UI_UX.md

[ ] UML reflects actual implementation

[ ] Report evidence is captured

[ ] README reflects actual project

[ ] No secrets are committed

[ ] No fake metrics are presented

[ ] No unfinished optional technology is presented as complete

[ ] Repository can be understood without old ChatGPT conversations

[ ] Developer can explain the architecture and major technology decisions
70. Final Reminder

Every task in FactoryFlow must remain connected to the original problem:

Maintenance engineers receive heterogeneous KPI information
through WhatsApp and manually transfer it into reporting files.

FactoryFlow exists to transform that fragmented process into:

Centralized acquisition
        +
Deterministic structuring
        +
Human verification
        +
Trusted persistence
        +
Automated reporting

When evaluating whether a feature belongs in the project, ask:

Does this improve the engineer's ability to collect, validate, understand, trace, or report industrial maintenance KPI information?

If the answer is no, the feature probably does not belong in the current FactoryFlow scope.

End of TASKS.md
