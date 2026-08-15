# 09_Report_Guide.md

> **FactoryFlow — Academic Report Writing & Evidence Guide**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-11
>
> This document defines **how FactoryFlow should be documented inside the internship report**.
>
> FactoryFlow is a complementary project, not the primary internship project.
>
> Its report coverage is expected to be limited compared with the main project, potentially around **five pages**.
>
> Therefore the objective is not to document every class, dependency, screen, or endpoint.
>
> The objective is to explain the **real industrial problem, the engineering response, the architecture, the critical data-integrity mechanism, and the most valuable implemented results** in a compact professional form.
>
> This document also defines what evidence must be captured during implementation so the report can be written from real, verified material rather than reconstructed from memory at the end.
>
> This guide must remain aligned with:
>
> - `01_Project_Vision.md`
> - `03_Architecture.md`
> - `04_Business_Rules.md`
> - `05_Database.md`
> - `06_API.md`
> - `07_Android.md`
> - `08_Backend.md`
> - `TASKS.md`
>
> The repository `assets/` folder contains the real WhatsApp screenshots that motivated FactoryFlow.
> These may be used as internal evidence and, if appropriate, as report illustrations only after privacy/confidentiality review and anonymization.

---

# 1. Report Objective

The FactoryFlow section of the academic report should demonstrate:

```text
real business problem
        ↓
requirements analysis
        ↓
technical design
        ↓
implementation choices
        ↓
data integrity
        ↓
working result
```

The section should make it clear that FactoryFlow was not built because mobile/OCR technologies were interesting.

It was built because a real industrial reporting workflow was inefficient and fragmented.

---

# 2. Central Narrative

The strongest report narrative is:

> Maintenance KPI information is received through heterogeneous WhatsApp messages and must be manually transferred into Excel reporting files. FactoryFlow centralizes this process in a native Android application, assists data acquisition through OCR and deterministic parsing, keeps a maintenance engineer in the validation loop, stores confirmed data centrally, and automates recurring Excel/PDF reporting.

Everything in the FactoryFlow report section should support this story.

---

# 3. What the Report Must Not Become

Do not turn the FactoryFlow section into:

- a dependency list
- a CRUD catalog
- a screen-by-screen manual
- a long code explanation
- a technology résumé
- a UML dump
- a generic Android tutorial
- a generic Spring Boot tutorial

The report should explain the **engineering problem and solution**.

---

# 4. Recommended Report Length

Target:

```text
4–5 pages
```

if the academic report truly limits FactoryFlow to a small complementary section.

If more space becomes available, the same structure can expand.

Do not artificially stretch the section.

---

# 5. Recommended Structure

A compact high-value structure:

```text
1. Context and Problem
2. Functional Solution
3. Architecture and Technologies
4. Intelligent KPI Acquisition and Validation
5. Automated Reporting and Results
6. Short Conclusion / Perspectives
```

These headings may be adapted to the report's chapter numbering and language.

---

# 6. Suggested Page Distribution

For approximately five pages:

```text
Context and Problem                 ~0.6 page
Functional Solution                 ~0.6 page
Architecture                        ~0.9 page
Acquisition / Parser / Validation   ~1.2 pages
Reporting / Automation / Results    ~1.1 pages
Conclusion / Perspectives           ~0.4 page
```

Figures may slightly alter this distribution.

---

# 7. Section 1 — Context and Problem

This section must explain why FactoryFlow exists.

Core facts:

- KPI values arrive through WhatsApp groups.
- Message structure is inconsistent.
- Maintenance staff manually transfer useful values into Excel.
- Fields may appear in different orders.
- separators vary
- decimal formats vary
- some values are missing
- minor spelling mistakes occur
- reports may be partial
- manual copying consumes time and introduces transcription risk

Keep this concise.

---

# 8. Suggested Problem Paragraph

A professional formulation may follow this logic:

```text
During the internship, an operational difficulty was identified in the maintenance reporting process.
Several KPI values were communicated through WhatsApp messages whose format was not strictly standardized.
Maintenance staff then manually extracted these values and transferred them into Excel reporting files.
This repetitive workflow increased processing time, limited traceability, and created a risk of transcription errors.
```

Do not exaggerate.

Do not claim severe operational failures unless evidence supports them.

---

# 9. Supervisor Need

The report should mention the business request in professional language:

> The identified need was to centralize maintenance indicators in a mobile application rather than leaving the reporting process distributed across messages and files.

Do not quote informal wording if it weakens academic presentation unless the supervisor explicitly wants original phrasing.

---

# 10. Real Source Evidence

The two real WhatsApp screenshots in `assets/` are useful to validate the report narrative.

Before including them in the final report:

```text
[ ] remove phone numbers
[ ] remove private names
[ ] remove confidential values if necessary
[ ] remove identifying group information
[ ] verify company confidentiality
```

A sanitized screenshot may strongly illustrate the original problem.

---

# 11. Recommended Figure — Existing Workflow

A simple diagram:

```text
WhatsApp Messages
      ↓
Manual Interpretation
      ↓
Manual Copying
      ↓
Excel Files
      ↓
Reporting
```

Caption example:

> **Figure X — Existing manual KPI reporting workflow**

This diagram is more useful than a screenshot if confidentiality prevents using real messages.

---

# 12. Section 2 — Functional Solution

Explain FactoryFlow from the user perspective.

Do not immediately start with Spring Boot.

Core solution:

```text
Mobile acquisition
→ automatic assistance
→ human verification
→ centralized persistence
→ automated reporting
```

---

# 13. Five Acquisition Methods

Briefly mention:

1. Manual entry
2. Paste WhatsApp text
3. Import screenshot from gallery
4. Share an image directly from WhatsApp/Android

Do not spend one paragraph on each method.

The architectural point is that all methods converge into one validation pipeline.

---

# 14. Recommended Functional Diagram

This is one of the best figures for the report.

```text
Manual Entry ───────────────┐
Paste Text ────────────────┤
Gallery Image → OCR ───────┤
Shared Image → OCR ────────┤
             ↓
   Deterministic Parser
             ↓
      Human Validation
             ↓
     Confirmed KPI Data
             ↓
 Dashboard / Reports / History
```

Caption:

> **Figure X — Unified KPI acquisition and validation workflow**

This figure should be considered high priority.

---

# 15. Section 3 — Architecture

The architecture section should explain the system at a high level.

Recommended architecture:

```text
Android Application
       ↓ REST / WebSocket
Spring Boot Backend
       ↓
PostgreSQL
       ↓
Report Generation / Scheduling / Email
```

Optional external services:

```text
FCM
SMTP
```

Only include RabbitMQ/Prometheus/Grafana if actually implemented and relevant.

---

# 16. Recommended Architecture Figure

Use a clean component diagram.

Suggested blocks:

```text
Maintenance Engineer
        ↓
Android App
  Kotlin / Compose
  Gallery / Share Intent
        ↓
REST + WebSocket/STOMP
        ↓
Spring Boot Backend
  Security
  Parser
  Reporting
  Scheduling
        ↓
PostgreSQL

Backend → PDFBox / POI → Reports
Backend → Quartz → Scheduled Generation
Backend → JavaMailSender → Email
Backend → FCM → Push Notifications
```

Keep optional components visually secondary.

---

# 17. Architecture Text

The architecture paragraph should explain **responsibility boundaries**, not merely technologies.

Example logic:

```text
The Android application handles data acquisition and user interaction.
OCR is performed by the private FactoryFlow PaddleOCR runtime through the authenticated backend API.
The Spring Boot backend centralizes authentication, parsing, validation, persistence, report generation and scheduling.
PostgreSQL stores authoritative confirmed data.
```

This is stronger than:

> The application uses Kotlin, Java, PostgreSQL, Retrofit, Room, Hilt...

---

# 18. Technology Table

A compact table is appropriate.

Example:

| Layer | Technology | Purpose |
|---|---|---|
| Mobile | Kotlin + Jetpack Compose | Native Android interface |
| OCR | PaddleOCR PP-OCRv5 | Private service-oriented text recognition |
| Backend | Java + Spring Boot | Business logic and API |
| Security | Spring Security + JWT | Authentication |
| Persistence | PostgreSQL + JPA | Centralized structured data |
| Excel | Apache POI | Excel report generation |
| PDF | Apache PDFBox | PDF report generation |
| Scheduling | Quartz | Daily/weekly/monthly automation |
| Realtime | WebSocket/STOMP | Live state updates |
| Notifications | FCM | Push notifications |

Only include technologies actually implemented in the final system.

---

# 19. Why Native Android

A short engineering justification is enough:

- direct Android Share Intent integration
- PaddleOCR backend OCR
- FileProvider
- FCM
- Android-only target

Do not turn this into a Flutter comparison essay.

---

# 20. Why Spring Boot

Keep concise:

- mature security ecosystem
- REST APIs
- JPA
- scheduling
- WebSocket
- email
- maintainable backend architecture

---

# 21. Why PostgreSQL

Explain:

- relational data
- integrity
- history
- filtering/statistics
- multi-user centralization

---

# 22. Section 4 — Intelligent KPI Acquisition

This should receive significant attention because it differentiates FactoryFlow from a generic CRUD application.

---

# 23. Input Variability

Explain real examples of variability:

```text
Choline : 295456
Choline -> 295456
Choline 295456
```

and:

```text
Vrac : 15,8
Vrac 15.8
Varc 15.8
```

These examples should be anonymized if necessary.

---

# 24. Parser Pipeline

Describe:

```text
Normalization
      ↓
Label Recognition
      ↓
Value Extraction
      ↓
Confidence / Warning
      ↓
Human Validation
```

This can be shown as a small figure or inline diagram.

---

# 25. Deterministic Parser Explanation

The report should state that the parser is deterministic.

It recognizes:

- canonical KPI names
- configured aliases
- normalized labels
- fuzzy matches for minor typos

It then extracts candidate numeric values.

---

# 26. Fuzzy Matching

If Levenshtein or another similarity algorithm is implemented, mention it explicitly.

Example:

> A deterministic similarity measure is used to tolerate minor spelling errors while keeping the matching process reproducible and explainable.

Do not claim fuzzy matching if final implementation uses only aliases/regex.

---

# 27. Decimal Handling

Mention support for:

```text
comma decimals
period decimals
```

because this is a real formatting issue.

---

# 28. Missing Values

Important sentence:

> Missing fields are preserved as missing information and are never automatically converted to zero.

This demonstrates data-integrity awareness.

---

# 29. Partial Reports

Briefly explain:

> The application accepts partial KPI reports and preserves the distinction between available and unreported values.

---

# 30. Why No LLM in Official Extraction

This is a strong engineering decision worth explaining.

Suggested formulation:

> A generative AI model was deliberately excluded from the official ingestion pipeline. Because the extracted values may be used in industrial reporting, the system favors deterministic processing and explicit human validation over probabilistic automatic interpretation.

This shows judgment rather than lack of AI.

---

# 31. Human-in-the-Loop

This is the central quality mechanism.

Explain:

```text
OCR/parser output = candidate
human confirmation = authoritative value
```

The engineer can:

- correct values
- add missed KPI entries
- remove false detections
- resolve unknown lines
- save a draft
- confirm the final result

---

# 32. Recommended Confirmation Screenshot

Capture a final polished screen showing:

- several detected KPIs
- one normal entry
- one warning
- one user-edited value
- Confirm button

This is probably one of the best report screenshots.

Caption:

> **Figure X — Human validation of automatically extracted KPI values**

---

# 33. Traceability

Explain that the data model preserves:

```text
raw source
extracted candidate
final confirmed value
```

This enables explanation of corrections and future parser improvement.

---

# 34. Suggested Data Integrity Table

If space allows:

| Data element | Meaning |
|---|---|
| Raw source | Original text used for analysis |
| Extracted value | Parser candidate |
| Final value | Engineer-confirmed authoritative value |
| Edited flag | Indicates human correction |
| Confidence | Parser assistance indicator |

This table may replace a longer paragraph.

---

# 35. Section 5 — Reporting and Automation

Explain that once data is confirmed, it becomes available for:

- dashboard
- history
- statistics
- Excel
- PDF
- scheduling

---

# 36. Excel Reporting

Mention Apache POI.

Explain that FactoryFlow generates structured Excel files automatically.

Do not claim advanced styling unless implemented.

---

# 37. PDF Reporting

Mention Apache PDFBox.

Explain why PDF is useful:

- fixed format
- distribution
- archiving

---

# 38. Why PDFBox

A short note may say:

> Apache PDFBox was selected to avoid licensing constraints associated with alternatives such as iText while still providing programmatic PDF generation.

Only include this if technology-choice discussion fits available space.

---

# 39. Quartz Scheduling

Explain:

> Quartz automates daily, weekly, and monthly report generation according to configured schedules.

Do not describe scheduler internals unless required.

---

# 40. Scheduled Email

If implemented:

> Scheduled reports can be distributed automatically through the backend using JavaMailSender.

If not completed, move this to perspectives/future work.

---

# 41. Mobile Sharing

If implemented:

> The Android application can share generated Excel/PDF files using the native Android share mechanism and FileProvider.

This demonstrates native mobile integration.

---

# 42. Realtime

If implemented:

> WebSocket/STOMP is used to propagate relevant report-state changes so the Android interface can refresh without manual reload.

Do not claim this if only planned.

---

# 43. Push Notifications

If implemented:

> Firebase Cloud Messaging notifies users about report generation, reminders or operational alerts.

Again: implementation status must be honest.

---

# 44. Dashboard

The report may include one dashboard screenshot if visually strong.

Recommended content:

- today's status
- current KPI values
- quick actions
- recent reports
- warning/schedule

Caption:

> **Figure X — FactoryFlow operational dashboard**

---

# 45. Statistics

If implemented, mention simple confirmed-data trends.

Avoid language like:

```text
predictive analytics
AI anomaly detection
```

unless actually implemented.

---

# 46. Results Section

The report should state what was actually achieved.

Possible result statements:

```text
The application centralizes KPI records.
The parser tolerates heterogeneous source formatting.
OCR reduces manual transcription from screenshots.
Human validation prevents uncertain extraction from becoming authoritative automatically.
Excel/PDF reports can be generated from confirmed data.
Scheduled generation reduces recurring manual reporting work.
```

Only keep statements supported by the final implementation.

---

# 47. Do Not Invent Metrics

Never write:

```text
70% faster
99.9% accurate
2x productivity
```

unless measured.

Qualitative statements are acceptable when quantitative testing was not performed.

---

# 48. Measured Results

If measurements are performed, document:

- test conditions
- dataset
- device/server
- number of samples
- measurement method

Potential useful metrics:

```text
parser test pass rate
OCR sample success rate
average report generation time
API response time under k6
number of real format variants supported
```

Do not cherry-pick.

---

# 49. Parser Evaluation

A useful compact evaluation can use a test matrix.

Example:

| Scenario | Expected | Result |
|---|---|---|
| `:` separator | Parsed | Pass |
| `->` separator | Parsed | Pass |
| Decimal comma | Parsed | Pass |
| Field reordering | Parsed | Pass |
| Minor typo | Fuzzy match + review | Pass |
| Missing value | Preserved as missing | Pass |
| Unknown line | Preserved for review | Pass |

This is strong academic evidence.

---

# 50. Performance Testing

If k6 is implemented:

Report only actual results.

A small graph/table may show:

- average latency
- p95 latency
- error rate

Use realistic load.

Do not make user-scale claims beyond the project.

---

# 51. Observability

If Prometheus/Grafana are actually implemented, one screenshot may be used to demonstrate engineering maturity.

But because report space is limited, parser/confirmation/report screenshots are usually more valuable.

---

# 52. RabbitMQ

If RabbitMQ is not implemented:

Do not include it in architecture as if it exists.

It may appear in perspectives as:

> future asynchronous processing enhancement

If implemented:

Explain the actual use case, such as asynchronous report generation.

---

# 53. Resilience4j

Same rule.

Do not list optional technologies as completed.

---

# 54. Suggested Five-Page Layout

A practical layout could be:

## Page 1

- Problem/context
- existing workflow figure
- solution summary

## Page 2

- architecture figure
- technology table

## Page 3

- acquisition pipeline
- parser explanation
- input examples

## Page 4

- human validation screenshot
- data integrity/trust explanation

## Page 5

- dashboard/report screenshot
- Excel/PDF/scheduling
- conclusion/perspectives

This is a strong default.

---

# 55. Alternative Compact Layout

If only 3 pages are available:

```text
Page 1:
Problem + architecture

Page 2:
Acquisition/parser/human validation

Page 3:
Reporting/results/perspectives
```

Keep only two or three figures.

---

# 56. Figure Priority

If space is limited, prioritize:

```text
1. Unified acquisition pipeline
2. Confirmation screen
3. Architecture diagram
4. Dashboard
5. Generated Excel/PDF
```

The existing workflow diagram can be merged into the acquisition figure if needed.

---

# 57. Screenshot Quality

Screenshots must:

- use final premium UI
- avoid debug banners
- avoid private information
- use realistic anonymized data
- crop cleanly
- have readable text
- show meaningful state

Do not use early placeholder UI.

---

# 58. Screenshot Naming

Store report-ready screenshots in:

```text
report/evidence/factoryflow/
```

Recommended names:

```text
01_dashboard.png
02_acquisition.png
03_confirmation.png
04_report_history.png
05_generated_pdf.png
06_generated_excel.png
07_schedule.png
```

Adjust to actual repository structure if needed.

---

# 59. Diagram Naming

Recommended:

```text
diagrams/factoryflow/
architecture.png
acquisition_pipeline.png
class_diagram.png
sequence_confirmation.png
sequence_scheduled_report.png
```

Source files should also be retained if using PlantUML/Mermaid/other diagram tooling.

---

# 60. Evidence Register

`TASKS.md` should record valuable evidence as features are completed.

For each milestone:

```text
Task:
Feature:
Evidence:
File:
Report value:
Notes:
```

Example:

```text
Task: FF-1402
Feature: Confirmation screen
Evidence: Screenshot with low-confidence KPI corrected manually
File: report/evidence/factoryflow/03_confirmation.png
Report value: Very High
```

---

# 61. Evidence Must Be Captured Immediately

Do not wait until the final week.

Reasons:

- UI may change
- logs may disappear
- test outputs may be overwritten
- development data may be reset
- old working behavior may be harder to recreate

Capture evidence at milestone completion.

---

# 62. Evidence Quality Categories

Use:

```text
VERY HIGH
HIGH
MEDIUM
LOW
```

### Very High

Directly explains problem/solution or core engineering.

### High

Strong proof of completed feature.

### Medium

Useful supporting detail.

### Low

Mostly process/setup.

---

# 63. Very High Value Evidence

Examples:

- acquisition pipeline
- parser regression test matrix
- confirmation screenshot
- Excel/PDF final output
- final architecture diagram

---

# 64. High Value Evidence

Examples:

- dashboard
- Share Intent demo
- OCR flow
- Quartz sequence
- Swagger endpoint

---

# 65. Medium Value Evidence

Examples:

- notification screen
- schedule configuration
- statistics chart
- Git history

---

# 66. Low Value Evidence

Examples:

- installation terminal
- dependency setup
- blank database
- IDE screenshots

Do not waste report space on these.

---

# 67. UML Strategy

The report should use business-oriented UML.

Recommended class diagram size:

```text
10–15 important business classes/concepts
```

Do not include every Spring controller/DTO/repository.

---

# 68. Recommended UML Classes

Potential:

```text
MaintenanceEngineer
MaintenanceReport
KPIEntry
KPIDefinition
ExtractionResult
GeneratedReport
ReportTemplate
Schedule
Notification
AuditLog
Dashboard
ParserConfiguration
```

Enums:

```text
ReportStatus
AcquisitionMethod
ReportFormat
ScheduleType
```

Only include actual final concepts.

---

# 69. UML Relationships

Examples:

```text
MaintenanceEngineer 1 ─── * MaintenanceReport

MaintenanceReport 1 ◆── * KPIEntry

KPIEntry * ─── 1 KPIDefinition

GeneratedReport * ─── * MaintenanceReport

Schedule 1 ─── * GeneratedReport
```

Use composition only where lifecycle semantics are correct.

---

# 70. UML Cardinalities

Always include meaningful multiplicities.

Do not draw unlabeled random arrows.

---

# 71. UML Implementation vs Report

Detailed implementation UML may contain many more classes.

Report UML should remain readable.

The report is not source-code documentation.

---

# 72. Sequence Diagram — Confirmation

Highly recommended.

Flow:

```text
Engineer
  ↓
Android
  ↓ Analyze request
Backend Parser
  ↓
Android Confirmation UI
  ↓ Correct value
Backend Confirmation Service
  ↓
PostgreSQL
  ↓
Realtime/Event
```

This diagram communicates the human-in-the-loop architecture extremely well.

---

# 73. Sequence Diagram — Scheduled Reporting

Optional/high value:

```text
Quartz
  ↓
ReportGenerationService
  ↓
PostgreSQL
  ↓
POI/PDFBox
  ↓
ReportStorageService
  ↓
JavaMailSender
  ↓
Notification
```

Use if scheduling is implemented.

---

# 74. Activity Diagram

Not necessary if acquisition pipeline already explains the workflow.

Avoid diagram overload.

---

# 75. Use Case Diagram

Optional.

May show:

```text
Authenticate
Acquire KPI Data
Review Extraction
Confirm Report
Search History
Generate Report
View Dashboard
Manage Schedule
```

But in a five-page section, architecture + pipeline + sequence are usually more valuable.

---

# 76. Database Diagram

A full ERD is probably too detailed for limited space.

If included, show only:

```text
User
MaintenanceReport
KPIEntry
KPIDefinition
GeneratedReport
Schedule
```

---

# 77. Code Snippets

Use code snippets only if they prove a meaningful algorithm.

A small parser snippet may be acceptable.

Do not include:

- entity boilerplate
- controller mappings
- Gradle dependencies

Diagrams/tables/screenshots provide more value.

---

# 78. Parser Pseudocode

If technical explanation needs code-like detail, pseudocode is better:

```text
for each input line:
    normalize line
    detect candidate KPI label
    try exact/alias match
    if not found:
        apply fuzzy matching
    extract numeric value
    evaluate plausibility
    return candidate + warnings
```

This is concise and understandable.

---

# 79. Report Language

Use academic/professional language.

Avoid conversational wording such as:

```text
the app is super smart
it automatically understands everything
we made a cool dashboard
```

Prefer:

```text
The application assists KPI extraction through deterministic parsing and on-device OCR.
```

---

# 80. Claims Discipline

Every statement should be one of:

```text
Implemented
Measured
Observed
Designed
Planned/Future
```

Do not blur categories.

---

# 81. Implemented Language

Use:

```text
The system provides...
The application uses...
The backend generates...
```

only when implemented.

---

# 82. Planned Language

Use:

```text
A future extension could...
The architecture allows...
This functionality is planned...
```

for unimplemented features.

---

# 83. Avoid Future Features in Results

Do not list:

- predictive maintenance
- LLM queries
- RabbitMQ
- Grafana
- FCM

as results unless they actually exist.

---

# 84. Report Terminology Consistency

Use one naming convention.

Recommended:

```text
Maintenance Report
KPI Entry
KPI Definition
Generated Report
Acquisition Method
Human Validation
```

Do not switch between:

```text
ticket
record
form
job
report
```

without reason.

---

# 85. "AI" Terminology

Be careful.

Do not call deterministic parser:

```text
Artificial Intelligence
```

The project title can say intelligent because of workflow assistance.

If asked academically, explain the distinction clearly.

---

# 86. OCR Terminology

OCR = Optical Character Recognition.

Define first use.

Example:

> PaddleOCR extracts text from gallery or shared images inside the controlled FactoryFlow environment.

---

# 87. STOMP Terminology

If used:

> Spring WebSocket/STOMP provides realtime event communication between backend and Android.

No need to deeply explain protocol frames.

---

# 88. JWT Terminology

If security is mentioned:

> JWT-based authentication secures API access, with refresh tokens used to maintain sessions.

One sentence may be enough.

---

# 89. Quartz Terminology

> Quartz manages recurring daily, weekly, and monthly report-generation schedules.

---

# 90. Room Terminology

If included:

> Room is used only for local cache/draft resilience; PostgreSQL remains authoritative.

This is useful architecture clarity.

---

# 91. Report Conclusions

A short conclusion should reconnect to business value.

Suggested logic:

```text
FactoryFlow centralizes a previously fragmented reporting workflow.
The platform reduces repetitive manual transfer while preserving data integrity through human validation.
Its modular architecture also provides a basis for future analytics and predictive maintenance features.
```

---

# 92. Perspectives

Possible future perspectives:

- natural-language query over confirmed history
- anomaly detection
- predictive maintenance
- ERP/SAP integration
- multi-site deployment

Keep this short.

---

# 93. Do Not Present Future AI as Current

Clearly label:

```text
Perspective
Future work
```

---

# 94. Academic Defense Talking Points

The developer should be ready to answer:

```text
Why mobile?
Why native Android?
Why OCR on-device?
Why deterministic parser?
Why human confirmation?
Why PostgreSQL?
Why Spring Boot?
Why Quartz?
Why PDFBox instead of iText?
Why not use AI for extraction?
Why keep raw/extracted/final values?
```

---

# 95. Best Defense Answer — Why Human Validation

Core idea:

> OCR and parsing reduce manual work, but the application deals with operational data that may be reported inconsistently. The confirmation step ensures that automation never silently transforms uncertain extraction into official data.

---

# 96. Best Defense Answer — Why Deterministic Parser

Core idea:

> The same input should produce the same extraction result, and uncertain matches must be explainable. Deterministic parsing with aliases and fuzzy matching provides this control more reliably than a generative model for official data ingestion.

---

# 97. Best Defense Answer — Why Native Android

Core idea:

> The workflow already occurs on Android through WhatsApp. Native development provides direct Share Intent and FileProvider integration while the backend owns OCR orchestration.

---

# 98. Best Defense Answer — Why Quartz

Core idea:

> The requirement is recurring scheduled report generation, so Quartz directly addresses the scheduling problem without introducing the heavier Spring Batch framework.

---

# 99. Best Defense Answer — Why PostgreSQL

Core idea:

> The system requires centralized, structured, relational, auditable history across users, reports, KPI entries, schedules and generated files.

---

# 100. Best Defense Answer — Why PDFBox

Core idea:

> PDFBox satisfies the technical requirement for PDF generation while avoiding the licensing constraints associated with iText for this project.

---

# 101. Best Defense Answer — Why Keep Extracted and Final Values

Core idea:

> This preserves traceability. The system can show what automation proposed and what the engineer finally validated, which is important for trust, auditability and future parser improvement.

---

# 102. Report Testing Evidence

At least one testing artifact should appear if space allows.

Best choice:

```text
parser regression test matrix
```

because parser reliability is central.

---

# 103. Report Security Evidence

Security does not need a screenshot unless required.

Mention:

- Spring Security
- JWT
- BCrypt
- authenticated endpoints

briefly in architecture.

---

# 104. Report Database Detail

Do not list every column.

Explain the conceptual relationship:

```text
MaintenanceReport
  has many
KPIEntry
  references
KPIDefinition
```

and:

```text
extracted value
vs final value
```

This is enough for limited space.

---

# 105. Report API Detail

Do not print the endpoint list.

Mention representative APIs if needed:

```text
analyze
confirm
history
generate
statistics
```

Swagger screenshot can prove API implementation.

---

# 106. Swagger Evidence

A Swagger screenshot is useful for GitHub and appendix, but not necessarily among the top report figures.

---

# 107. Generated Excel Evidence

Capture a real workbook showing:

- title
- reporting period
- KPI rows
- units
- formatting

Do not use a blank or raw sheet.

---

# 108. Generated PDF Evidence

Capture first page or viewer screenshot.

Ensure:

- readable
- no private data
- professionally formatted

---

# 109. Schedule Evidence

If space permits, use either:

- schedule UI screenshot
- Quartz sequence diagram

The sequence diagram is usually more academically valuable.

---

# 110. Share Intent Evidence

Best shown in a GIF/video for GitHub/demo.

A static report screenshot may not explain the flow well.

If needed in report, use a 2–3 image mini-flow:

```text
WhatsApp Share
→ FactoryFlow OCR
→ Confirmation
```

---

# 111. Image OCR Evidence

Usually lower report priority than Share Intent because Share Intent is more tightly connected to the original workflow.

---

# 112. Notification Evidence

Only include if space remains.

---

# 113. Dark Mode Evidence

Portfolio-only unless design is a report objective.

---

# 114. Git Evidence

Professional Git history is valuable for GitHub/interview.

It usually does not belong in the academic FactoryFlow section unless methodology requires it.

---

# 115. Commit-to-Evidence Mapping

`TASKS.md` should associate:

```text
task
commit
evidence
```

This makes report reconstruction easy.

Example:

```text
FF-1209
feat(parser): expose report analysis endpoint
Evidence: Swagger analyze endpoint
```

---

# 116. Suggested Report Asset Folder

Recommended:

```text
report/
└── evidence/
    └── factoryflow/
        ├── screenshots/
        ├── diagrams/
        ├── tests/
        └── exports/
```

---

# 117. Evidence File Naming

Use stable descriptive names.

Avoid:

```text
Screenshot_2026-08-11_183029.png
image2.png
finalfinal.png
```

Prefer:

```text
confirmation_low_confidence.png
dashboard_final.png
excel_daily_report.png
parser_test_matrix.png
```

---

# 118. Evidence Metadata

Optionally keep:

```text
report/evidence/factoryflow/README.md
```

listing:

```text
file
date
feature
task
commit
notes
```

This is highly useful if many assets accumulate.

---

# 119. Report Reproducibility

Every claim about generated reports or tests should be reproducible from the codebase where practical.

Do not rely only on screenshots.

---

# 120. Sanitized Demo Dataset

Create a small anonymized dataset for:

- screenshots
- report exports
- tests
- demos

It should resemble real format variation without exposing private industrial information.

---

# 121. Example Demo Input

A safe example can resemble:

```text
Choline : 295456
Vrac -> 15,8
KPI-X 42
```

Use actual KPI vocabulary only if confidentiality permits.

---

# 122. Parser Test Dataset

Include cases for:

```text
different order
different separators
decimal comma
decimal point
minor typo
missing value
partial report
unknown line
duplicate KPI
out-of-range value
```

---

# 123. Report Result Language

Good:

> The parser successfully handled the tested formatting variants and preserved uncertain or unrecognized content for manual review.

Bad:

> The parser understands every WhatsApp message perfectly.

---

# 124. OCR Result Language

Good:

> OCR reduces the need to retype information from screenshots, while the confirmation step compensates for recognition uncertainty.

Bad:

> OCR output is probabilistic and always requires deterministic parsing plus explicit human validation.

---

# 125. Automation Result Language

Good:

> Recurring report generation reduces repetitive manual preparation.

Bad:

> FactoryFlow completely eliminates human reporting work.

---

# 126. Efficiency Claims

If no timing study:

Use qualitative phrasing:

```text
reduces repetitive transfer
streamlines acquisition
centralizes history
automates recurring generation
```

Avoid percentages.

---

# 127. Accuracy Claims

If no measured accuracy study:

Use:

```text
improves control
exposes uncertainty
requires validation
reduces transcription risk
```

Do not claim accuracy percentage.

---

# 128. Comparison Table

If useful, show:

| Existing workflow | FactoryFlow |
|---|---|
| WhatsApp + manual Excel transfer | Centralized mobile workflow |
| Manual reading | OCR/parser assistance |
| Repetitive transcription | Structured extraction |
| Weak traceability | Raw/extracted/final values preserved |
| Manual report generation | Automated Excel/PDF |
| Fragmented history | Searchable centralized history |

This table is highly efficient in limited space.

---

# 129. Recommended Final FactoryFlow Report Structure

A polished final structure could be:

```text
X. FactoryFlow: Centralized Mobile Maintenance KPI Reporting

X.1 Context and Need
X.2 Proposed Solution
X.3 Technical Architecture
X.4 KPI Acquisition and Human Validation
X.5 Automated Reporting and Results
X.6 Perspectives
```

---

# 130. Example X.1 Content Plan

Include:

- current WhatsApp workflow
- manual Excel transfer
- inconsistency/problem
- centralization need

Figure:

```text
existing workflow or sanitized screenshot
```

---

# 131. Example X.2 Content Plan

Include:

- Android mobile platform
- five acquisition methods
- dashboard/history/reporting

Figure:

```text
unified acquisition pipeline
```

---

# 132. Example X.3 Content Plan

Include:

- Android
- Spring Boot
- PostgreSQL
- PaddleOCR
- report generation/scheduling

Figure:

```text
architecture diagram
```

---

# 133. Example X.4 Content Plan

Include:

- normalization
- label matching
- fuzzy matching
- confidence/warnings
- missing != zero
- human confirmation
- raw/extracted/final traceability

Figure:

```text
confirmation screenshot
```

---

# 134. Example X.5 Content Plan

Include:

- dashboard/history
- Excel/PDF
- Quartz
- sharing/email
- actual tests/results

Figure:

```text
dashboard OR generated report
```

---

# 135. Example X.6 Content Plan

Include:

- future AI historical query
- predictive maintenance
- ERP integration

Maximum:

```text
one short paragraph
```

---

# 136. Report Page Economy

To save space:

Use:

- tables
- diagrams
- figure captions
- concise paragraphs

Avoid:

- long bullet lists
- repeated technology descriptions
- code snippets unless essential

---

# 137. Caption Quality

Captions should explain purpose.

Good:

> **Figure X — Unified acquisition pipeline from WhatsApp-derived input to human-confirmed KPI data**

Bad:

> **Figure X — Application**

---

# 138. Figure Referencing

Every figure must be referenced in text.

Example:

> Figure X illustrates how the five acquisition methods converge toward a single validation pipeline.

Do not place orphan screenshots.

---

# 139. Table Referencing

Likewise:

> Table X summarizes the main technologies and their responsibilities.

---

# 140. Academic Coherence

Each section should transition logically.

Example:

```text
Problem
→ solution
→ architecture
→ critical algorithm
→ result
```

Do not jump from screenshot to database to OCR without narrative.

---

# 141. Report Voice

Use a consistent voice according to institution guidelines.

Examples:

```text
Nous avons conçu...
Le système permet...
La solution repose sur...
```

or English equivalent.

Do not mix first person and impersonal voice randomly.

---

# 142. French Report Adaptation

If the final report is written in French, preferred terminology:

```text
indicateurs KPI
maintenance
collecte des données
validation humaine
extraction
reconnaissance OCR
génération des rapports
ordonnancement
traçabilité
historique
```

Use institution-preferred terminology consistently.

---

# 143. Possible French Project Title

If needed:

> **Conception et développement de FactoryFlow : une plateforme mobile intelligente pour la collecte des KPI de maintenance industrielle et la génération automatisée de rapports**

Keep the product name FactoryFlow unchanged.

---

# 144. Report Technology Naming

First occurrence:

```text
Jetpack Compose
Spring Boot
PostgreSQL
PaddleOCR
Apache POI
Apache PDFBox
Quartz
```

Later use shorter references.

---

# 145. Acronym Definitions

Define first use:

```text
KPI — Key Performance Indicator
OCR — Optical Character Recognition
JWT — JSON Web Token
FCM — Firebase Cloud Messaging
```

No need to define well-known Java/SQL unless guidelines require it.

---

# 146. Confidentiality Review

Before final report submission:

```text
[ ] WhatsApp screenshots sanitized
[ ] company names allowed
[ ] personal names removed where necessary
[ ] emails removed
[ ] phone numbers removed
[ ] real credentials absent
[ ] sensitive KPI values reviewed
```

---

# 147. Public GitHub vs Academic Report

The academic report may contain information that should not be public.

Before copying assets into GitHub README, run a separate privacy review.

Do not assume report-safe = public-safe.

---

# 148. Final Evidence Checklist

Before writing the final FactoryFlow section:

```text
[ ] final architecture diagram
[ ] unified acquisition pipeline
[ ] confirmation screenshot
[ ] dashboard screenshot
[ ] Excel screenshot/file
[ ] PDF screenshot/file
[ ] parser test evidence
[ ] schedule evidence if implemented
[ ] Share Intent/OCR evidence
[ ] final list of implemented vs planned features
[ ] sanitized demo data
```

---

# 149. Final Writing Checklist

Before considering the FactoryFlow report section complete:

```text
[ ] starts from real business problem
[ ] explains supervisor need
[ ] explains solution before technology
[ ] architecture responsibilities clear
[ ] deterministic parser described
[ ] human validation emphasized
[ ] missing != zero mentioned
[ ] partial reports mentioned if space allows
[ ] generated reports described accurately
[ ] scheduling described only if implemented
[ ] optional technology not presented as complete
[ ] no fake metrics
[ ] figures readable
[ ] screenshots sanitized
[ ] conclusion returns to business value
```

---

# 150. Final Report Principle

The FactoryFlow report section should leave the evaluator with one clear understanding:

> A real manual industrial reporting workflow was identified, analyzed, and transformed into a centralized mobile information system that automates repetitive work while preserving human control over official KPI data.

That is the main story.

The technologies are evidence of how the problem was solved.

They are not the story themselves.

---

# End of 09_Report_Guide.md
