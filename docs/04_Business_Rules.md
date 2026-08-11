# 04_Business_Rules.md

> **FactoryFlow — Business Rules Specification**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-11
>
> This document defines the **business rules that govern FactoryFlow behavior**.
>
> These rules explain what the system is allowed to do, what it must prevent, how industrial KPI data becomes authoritative, how reports move through their lifecycle, how scheduling and generated documents behave, and where user confirmation is mandatory.
>
> This document is authoritative for business behavior.
>
> Technical implementation details belong in:
>
> - `03_Architecture.md`
> - `05_Database.md`
> - `06_API.md`
> - `07_Android.md`
> - `08_Backend.md`
>
> Visual behavior belongs in:
>
> - `DESIGN.md`
> - `UI_UX.md`
>
> When implementation behavior conflicts with this document, the business rule wins unless this document is deliberately updated.
>
> The repository `assets/` folder contains the real WhatsApp screenshots that motivated FactoryFlow.
> When parser or input-format behavior is unclear, Codex should consult those assets before inventing assumptions.

---

# 1. Purpose

FactoryFlow exists to transform fragmented industrial maintenance KPI information into:

```text
structured
verified
traceable
centralized
reportable
```

business data.

The system must reduce repetitive work without reducing data trust.

The most important rule is therefore:

> **Automatically extracted data is never authoritative until a maintenance engineer confirms it.**

---

# 2. Business Terminology

## 2.1 Maintenance Report

A `MaintenanceReport` represents one business record containing KPI information acquired from:

- manual entry
- pasted text
- gallery image
- shared image
- camera image

It is not the same as an Excel or PDF file.

Its persisted lifecycle is `DRAFT`, `PENDING_REVIEW`, or `CONFIRMED`. `ARCHIVED`
exists only if a later approved implementation genuinely needs it. Missing/generated/
combined dashboard indicators are projections, not persisted report states.

Every report has an `effective_date` business date distinct from `submitted_at` and
`confirmed_at`. Multiple reports may share the same effective date.

---

## 2.2 KPI Definition

A `KPIDefinition` defines one recognized maintenance indicator.

It may include:

- code
- display name
- category
- unit
- aliases
- plausible minimum
- plausible maximum
- active status

---

## 2.3 KPI Entry

A `KPIEntry` represents one KPI value attached to a Maintenance Report.

It may preserve:

- extracted value
- current/draft value where needed
- confidence
- whether the user edited it
- final confirmed value
- captured unit
- warnings and source context where relevant

MVP KPI entries do not persist a separate `PENDING`, `VALIDATED`, `REJECTED`, or
`CORRECTED` state machine.

---

## 2.4 Generated Report

A `GeneratedReport` represents a physical document produced from confirmed business data.

Supported formats:

```text
Excel
PDF
```

---

## 2.5 Draft

A draft is a report that has not yet become authoritative.

It may contain:

- parser results
- manual values
- user corrections
- unresolved warnings

---

## 2.6 Confirmation

Confirmation is the explicit business action by which a maintenance engineer accepts the final KPI values as authoritative.

---

# 3. Primary Business Principle

FactoryFlow follows:

```text
Automation assists.
Human validates.
Confirmed data becomes authoritative.
```

No automation mechanism may bypass this rule.

---

# 4. Acquisition Rules

FactoryFlow supports five acquisition methods.

```text
MANUAL
PASTE
GALLERY_OCR
SHARE_OCR
CAMERA_OCR
```

Each report must preserve its acquisition source.

---

# 5. Manual Entry Rule

Manual entry may bypass OCR and parser matching.

However, manual entry must still follow:

- validation
- warning
- confirmation
- persistence
- traceability

Manual entry is not a privileged shortcut around integrity rules.

---

# 6. Paste Rule

Pasted text must preserve the original text exactly enough for later traceability.

Normalization may occur internally for parsing.

The normalized version must not replace the preserved original source.

---

# 7. Image Acquisition Rule

For:

- gallery
- share intent
- camera

the Android application performs OCR first.

The OCR text becomes the source text used by the backend parser.

One screenshot remains one review flow in MVP even when multiple WhatsApp message
bubbles are visible. Do not automatically split it into multiple maintenance reports;
the engineer controls the final draft.

---

# 8. OCR Authority Rule

OCR output is never authoritative.

OCR only produces text candidates.

The parser then interprets that text.

The maintenance engineer confirms the final business values.

---

# 9. Source Preservation Rule

The original source context must be retained where the architecture supports it.

For paste:

```text
raw pasted text
```

For images:

```text
OCR-extracted text
```

Do not overwrite raw source when a user corrects a value.

---

# 10. Raw Source vs Normalized Source

The system may internally derive:

```text
normalizedText
```

from:

```text
rawText
```

for parser use.

But only the raw/original source should be treated as the traceability reference.

---

# 11. Missing Value Rule

Missing is not zero.

Examples:

```text
Vrac :
```

or:

```text
Vrac
```

with no usable number means:

```text
not reported
```

unless the source clearly specifies `0`.

The system must never silently convert missing values into zero.

---

# 12. Zero Value Rule

A source value of:

```text
0
```

is a valid numeric value if parsing rules accept it.

Do not treat it as missing merely because it is zero.

---

# 13. Partial Report Rule

A report may contain only some KPI values.

The report remains valid as an input.

The system may show:

- missing expected KPI warning
- incomplete status
- review requirement

but must not reject all valid supplied data simply because the report is partial.

---

# 14. Field Order Rule

KPI order is not fixed.

The parser must not rely on a specific position.

Example:

```text
KPI A
KPI B
KPI C
```

must be functionally equivalent to:

```text
KPI C
KPI A
KPI B
```

if values are otherwise valid.

---

# 15. Separator Rule

Source formatting may use:

```text
:
=
->
whitespace
```

or another documented separator pattern.

The parser must recognize supported deterministic variants.

---

# 16. Decimal Separator Rule

Both:

```text
12.5
```

and:

```text
12,5
```

may represent decimal values.

The parser must normalize according to agreed rules.

---

# 17. Ambiguous Numeric Rule

If a number cannot be interpreted safely and deterministically:

Do not silently guess.

Return:

- candidate
- warning
- lower confidence

or mark the value unrecognized.

Human review resolves ambiguity.

The parser must explicitly handle attached units and decimal/thousands ambiguity such
as `30.197` versus `30197`. Missing markers such as `---` and `----` remain missing/null,
never zero.

A source line may produce zero, one, or multiple KPI candidates. For example,
`Compresseur 1: 77108-77%` may represent separate measurements and must not be forced
into one composite numeric value.

---

# 18. KPI Label Matching Rule

Label matching should follow deterministic priority.

Recommended conceptual order:

```text
1. Exact canonical match
2. Exact alias match
3. Normalized canonical/alias match
4. Fuzzy deterministic match
5. Unrecognized
```

Exact matches outrank fuzzy matches.

---

# 19. Alias Rule

Aliases belong to configurable KPI definitions.

Do not continuously hardcode new spelling variants directly into parser code when they represent business vocabulary.

---

# 20. Typo Rule

Minor spelling mistakes may be handled through deterministic fuzzy matching.

Example:

```text
Varc
```

may match:

```text
Vrac
```

if the configured similarity threshold supports it.

The result may receive a review warning depending on confidence.

---

# 21. Unknown Label Rule

If a label cannot be matched reliably:

The system must preserve it as unrecognized content.

Do not silently discard.

---

# 22. Duplicate KPI Rule

If the same KPI appears multiple times in one input:

The system must not silently select one candidate without a defined rule.

Preferred behavior:

```text
preserve duplicate candidates
→ warn
→ require human resolution
```

Final API/UI behavior must reflect this rule.

---

# 23. Unit Rule

The expected unit belongs to `KPIDefinition`.

If a source contains a unit:

The system may preserve it as:

```text
captured_unit
```

---

# 24. Unit Mismatch Rule

If captured unit differs from expected unit:

Do not silently convert unless explicit conversion rules exist.

Instead:

```text
warning
→ human review
```

---

# 25. Plausible Range Rule

`plausible_min` and `plausible_max` are warning thresholds.

They are not automatically hard rejection boundaries.

A value outside the range should normally produce:

```text
Needs Review / Warning
```

The maintenance engineer may still confirm it.

---

# 26. Hard Validation Rule

Hard rejection should only occur for conditions that make the business value unusable.

Examples may include:

- malformed required request structure
- unknown KPI identifier in confirmation request
- value format impossible to parse
- invalid report state transition

Do not confuse "suspicious" with "invalid."

---

# 27. Confidence Rule

Confidence is advisory.

It may be based on:

- label match quality
- extraction quality
- unit compatibility
- plausible range
- fallback behavior

Confidence must never authorize auto-confirmation.

---

# 28. Confidence Presentation Rule

The user-facing experience may simplify raw scores into:

```text
High confidence
Review suggested
Low confidence
```

The exact score may remain available for diagnostics.

---

# 29. Parser Determinism Rule

The same:

```text
source input
+
KPI definitions
+
parser configuration
```

must produce the same parser output.

No probabilistic LLM behavior is allowed in official extraction.

---

# 30. LLM Rule

LLMs must not determine official KPI values from source messages in the current system.

Future AI may query already confirmed historical data.

---

# 31. Analyze Rule

`Analyze` does not create authoritative data.

It creates:

```text
ExtractionResult
```

or equivalent temporary analysis output.

---

# 32. Analyze Result Rule

Analyze output should include enough information to review:

- matched KPI
- candidate value
- source label/line
- confidence
- warning
- unit where relevant
- unknown lines

---

# 33. Unrecognized Line Rule

Every meaningful unrecognized line should remain visible to the user during confirmation where practical.

User may:

- assign to KPI
- ignore explicitly
- correct through manual input

Drafts and confirmed traceability persist an explicit resolution for each unknown line:
`UNRESOLVED`, `ASSIGNED`, or `IGNORED`.

---

# 34. Silent Discard Rule

FactoryFlow must not silently discard source content that could plausibly contain KPI information.

---

# 35. Confirmation Rule

All automatically extracted reports require explicit human confirmation.

This rule applies to:

- paste
- gallery OCR
- shared image OCR
- camera OCR

---

# 36. Manual Confirmation Rule

Manual entry also requires final confirmation before becoming authoritative.

The UX may be simpler because no parser uncertainty exists.

---

# 37. Final Value Rule

For each confirmed KPI entry:

```text
final_value
```

is authoritative.

If no edit occurred:

```text
final_value = extracted_value
```

may be stored conceptually.

If user edited:

```text
final_value != extracted_value
```

must remain traceable.

---

# 38. Extracted Value Preservation Rule

When the user changes a parsed value:

Do not overwrite the extracted value.

Preserve:

```text
extracted value
final value
edited flag
```

---

# 39. Edited Flag Rule

`edited_by_user` or equivalent should indicate whether the final value differs due to human correction.

This supports:

- auditability
- parser evaluation
- future parser improvement

---

# 40. Confirmation Transaction Rule

Confirmation is a business-critical state change.

The backend should ensure report status and final entries are persisted coherently.

A partial confirmation transaction must not leave contradictory state.

---

# 41. Confirmation Failure Rule

If persistence fails:

The report must not be presented as confirmed.

The user's review work should remain recoverable where possible.

---

# 42. Confirmation Retry Rule

If the client is uncertain whether confirmation succeeded due to a network failure:

The system should avoid duplicate report creation.

Backend state should be checked before blind retry where needed.

---

# 43. Confirmed Report Immutability Rule

Confirmed reports are read-only by default.

Users must not casually edit confirmed historical KPI values.

If a correction workflow is introduced later, it requires explicit audit semantics.

---

# 44. Draft Rule

A draft is not authoritative.

Draft values must not be used in:

- official dashboard KPI values
- official statistics
- generated reports

unless a specific future workflow explicitly says otherwise.

---

# 45. Draft Save Rule

A user may save incomplete validation work.

The draft should preserve enough state to resume meaningfully.

---

# 46. Draft Resume Rule

Resuming a draft should restore:

- source
- entries
- edits
- warnings
- report state

as far as the implementation supports.

---

# 47. Draft Deletion Rule

Deleting a draft must not delete unrelated confirmed data.

Draft deletion should require deliberate user action.

---

# 48. Draft Age Rule

Do not auto-delete drafts without a defined retention policy.

---

# 49. Dashboard Data Rule

Official dashboard KPI values must use only confirmed final values.

---

# 50. Dashboard Draft Rule

The dashboard may show:

- number of drafts
- draft status
- resume action

but draft KPI values must not appear as official current KPI values.

---

# 51. Statistics Data Rule

Statistics must use confirmed final values only.

---

# 52. Generated Report Data Rule

Excel and PDF generation must use confirmed final values.

Do not generate official reports from:

- raw OCR values
- draft values
- unconfirmed parser output

---

# 53. Maintenance Report vs Generated Report Rule

A Maintenance Report represents business data.

A Generated Report represents a physical document.

They have separate identities and lifecycle.

---

# 54. Generated Report Format Rule

Supported core formats:

```text
EXCEL
PDF
```

---

# 55. Generated Report Type Rule

Potential types:

```text
DAILY
WEEKLY
MONTHLY
MANUAL
```

---

# 56. Generated Report Metadata Rule

Generated reports should preserve:

- type
- format
- generation time
- file reference/path
- generated by
- generation origin where useful
- version/provenance for intentional regeneration

Core generation is synchronous. File-generation status and email-delivery status are
separate, so `generation = READY` with `emailDelivery = FAILED` is valid. Asynchronous
`PENDING` / `GENERATING` behavior requires a later explicit contract change.

---

# 57. Manual Generation Rule

A maintenance engineer may generate a report manually from eligible confirmed data.

---

# 58. Scheduled Generation Rule

Quartz may trigger report generation automatically for:

- daily
- weekly
- monthly

periods.

---

# 59. Reporting Period Rule

Period boundaries must be deterministic.

Examples:

```text
Daily
→ calendar day

Weekly
→ Monday through Sunday calendar week

Monthly
→ calendar month
```

Do not casually implement weekly as rolling 7 days unless the business requirement explicitly says so.

The business timezone is `Africa/Casablanca`. A monthly scheduled run occurs at the
configured time on the first day of the following month and generates the complete
previous calendar month. Quartz may perform one recovery execution after one missed
run and must prevent duplicates for schedule + reporting period + format.

---

# 60. Empty Period Rule

If a scheduled reporting period has no eligible confirmed data:

The system must follow an explicit rule.

Preferred initial behavior:

```text
do not fabricate values
→ mark/report missing data condition
→ notify if appropriate
```

Do not generate fake zero-filled official data.

---

# 61. Report Generation Failure Rule

Failure to generate a document must not alter or delete confirmed KPI business data.

---

# 62. Report Storage Rule

Generated files are stored through:

```text
ReportStorageService
```

Initial implementation:

```text
LocalReportStorageService
```

---

# 63. File Deletion Rule

If generated file deletion is supported:

Deleting a generated file must not delete the underlying Maintenance Report.

---

# 64. Excel Rule

Excel is the editable/analytical report format.

Use Apache POI.

---

# 65. PDF Rule

PDF is the fixed presentation/distribution format.

Use Apache PDFBox.

---

# 66. Report Layout Rule

Generated documents must include enough metadata to identify:

- FactoryFlow/report context
- period
- generation date
- KPI values
- units

The exact layout belongs in report documentation.

---

# 67. User-Initiated Share Rule

When the user manually shares a generated file:

Use Android Share Intent/FileProvider.

The system does not need backend SMTP for this action.

---

# 68. User-Initiated Email Rule

When the user taps Email:

FactoryFlow opens an installed email-capable app with the report attached.

The user chooses recipient and sends.

FactoryFlow must not claim delivery success because the external email app owns that outcome.

---

# 69. Scheduled Email Rule

When a scheduled report is generated without active user involvement:

The backend may send automatically via JavaMailSender.

---

# 70. Generation vs Delivery Rule

These are separate outcomes:

```text
report generated successfully
email delivered successfully
```

One does not imply the other.

---

# 71. Email Failure Rule

If email delivery fails after successful generation:

The report remains valid and stored.

The failure should be visible and retryable where implemented.

---

# 72. Schedule Rule

Quartz determines when scheduled generation runs.

Quartz job code must delegate actual business work to application services.

---

# 73. Schedule Type Rule

Supported core schedules:

```text
Daily
Weekly
Monthly
```

---

# 74. Schedule Enablement Rule

A schedule may be enabled or paused if configuration UX supports it.

Paused schedules must not execute.

---

# 75. Next Run Rule

The authoritative next execution time should be derived consistently from backend schedule configuration.

Android should not invent conflicting schedule calculations.

---

# 76. Schedule Failure Rule

A failed scheduled execution must be visible.

Do not silently skip.

---

# 77. Schedule Duplicate Rule

Repeated scheduler execution for the same intended period should not create uncontrolled duplicate official reports.

Use state/idempotency checks where required.

---

# 78. Notification Rule

Notifications communicate events.

They are not the source of truth.

---

# 79. Push Notification Rule

FCM may notify users about:

- report generated
- report missing
- threshold warning
- reminder
- delivery issue

---

# 80. In-App Notification Rule

If notifications are persisted in-app:

The record in PostgreSQL represents the notification state.

FCM is only a delivery mechanism.

---

# 81. Notification Privacy Rule

Push payloads should avoid unnecessary sensitive industrial details.

---

# 82. Notification Tap Rule

A notification should navigate to the most relevant entity when possible.

---

# 83. Realtime Rule

WebSocket/STOMP may signal that business state changed.

REST/database remains authoritative.

---

# 84. Realtime Payload Rule

Realtime payloads should be minimal.

Example:

```text
REPORT_CONFIRMED + reportId
```

rather than entire business object graphs.

---

# 85. Realtime Recovery Rule

If an event is missed:

The client must be able to recover through authoritative REST refresh.

---

# 86. Realtime Failure Rule

A disconnected WebSocket must not block normal application use.

---

# 87. FCM Failure Rule

A missed push must not cause loss of business state.

The user can still discover reports/notifications inside the app.

---

# 88. Authentication Rule

Business endpoints require authenticated users unless explicitly documented otherwise.

---

# 89. User Role Rule

The current system has one effective role:

```text
Maintenance Engineer
```

Do not create different business behavior by role without a new requirement.

---

# 90. Active User Rule

Inactive users must not be able to authenticate successfully.

---

# 91. Password Rule

Passwords are stored hashed.

Never plaintext.

---

# 92. Token Rule

Access tokens must expire.

Refresh tokens are opaque random values. Store only token hashes server-side with
expiration and revocation metadata, and rotate them on refresh.

---

# 93. Logout Rule

Logout ends the local authenticated session and revokes the active refresh/session
token through `POST /api/auth/logout`.

---

# 94. Session Expiry Rule

If refresh fails:

The user must re-authenticate.

Unsaved report work should be preserved where safely possible.

---

# 95. KPI Definition Active Rule

Inactive KPI definitions should not be available for new report selection/matching by default.

---

# 96. Historical KPI Rule

Deactivating a KPI definition must not make historical reports unreadable.

---

# 97. KPI Code Rule

KPI code should be stable and unique where used as a business identifier.

---

# 98. Alias Uniqueness Rule

Aliases should avoid ambiguous overlap across active KPI definitions.

If two active KPI definitions share a confusing alias:

The configuration should warn or reject according to final implementation policy.

---

# 99. Plausible Range Configuration Rule

A plausible minimum may be absent.

A plausible maximum may be absent.

Do not force range data where the business does not know it.

---

# 100. Category Rule

KPI category is organizational metadata.

Category must not change the KPI's core identity.

---

# 101. Audit Rule

Important business actions should be traceable.

Candidate actions:

- report created
- report analyzed
- draft saved
- KPI edited
- report confirmed
- report generated
- schedule changed

---

# 102. Audit Append Rule

Audit events are append-oriented.

Do not silently rewrite historical audit records.

---

# 103. Audit User Rule

Where an action is performed by a user:

Record the user identifier when practical.

System/scheduled actions may have no human user.

---

# 104. Data Deletion Rule

Do not hard-delete business history casually.

Use deactivation/archival where historical integrity matters.

---

# 105. User Deactivation Rule

If a user is deactivated:

Historical reports submitted by that user remain readable.

---

# 106. KPI Deactivation Rule

If a KPI definition is deactivated:

Historical entries remain linked.

---

# 107. Generated File Retention Rule

Do not auto-delete generated files without an explicit retention policy.

---

# 108. Report Search Rule

Users should be able to search/filter historical reports using meaningful dimensions such as:

- date
- type
- submitter
- status
- KPI

---

# 109. Default History Sort Rule

Newest first.

---

# 110. Draft Sort Rule

Most recently edited first.

---

# 111. Generated Report Sort Rule

Newest generated first.

---

# 112. Dashboard Report Status Rule

The dashboard should distinguish at least conceptually:

```text
Missing
Draft
Pending Review
Confirmed
Generated
```

Exact state mapping belongs in UI/API specifications.

Only Draft, Pending Review, and Confirmed map to persisted maintenance-report states.
Missing and Generated are UI/read-model projections.

---

# 113. Generated Rule

A Maintenance Report becoming "Generated" in UI must not erase the distinction between:

```text
confirmed business data
```

and:

```text
generated document
```

The UI may summarize both while the backend model keeps them separate.

---

# 114. Missing Report Rule

If the business expects a report for a period and none exists:

The system may show:

```text
Missing
```

and notify the user.

Do not create synthetic report records merely to make the dashboard look complete unless the business model intentionally uses expected-report placeholders.

---

# 115. Threshold Warning Rule

A threshold/plausibility warning must be based on configured rules.

Do not label a value as:

```text
AI anomaly
```

unless a real predictive model exists.

---

# 116. Statistics Rule

Simple statistics may include:

- average
- min
- max
- variation
- trend

Only based on confirmed values.

---

# 117. No Predictive Claim Rule

Do not claim predictive maintenance in current product behavior.

---

# 118. No AI Claim Rule

Do not claim AI extraction.

FactoryFlow's parser is deterministic.

---

# 119. Data Freshness Rule

Where the dashboard shows latest KPI values:

The timestamp/period should make freshness understandable where relevant.

---

# 120. Multi-User Conflict Rule

If two maintenance engineers modify the same draft/report:

The system should avoid silent overwrite.

Possible behavior:

```text
state/version conflict
→ ask user to refresh
```

Exact concurrency implementation belongs in backend/API docs.

---

# 121. Confirmation Conflict Rule

A report already confirmed must not be confirmed again as if it were still editable.

---

# 122. Generation Conflict Rule

Repeated generation requests may produce multiple document versions only if that is an explicit supported behavior.

Otherwise use duplicate protection/status checks.

---

# 123. Scheduled vs Manual Generation Rule

Generated-report metadata should preserve whether generation was:

```text
manual
scheduled
```

where useful.

---

# 124. Report Generation Permission Rule

All authenticated maintenance engineers may generate reports in the current single-role system unless future business rules restrict it.

---

# 125. KPI Definition Management Permission Rule

If KPI definition editing is exposed to all authenticated users due single-role scope, that is a deliberate product simplification.

Do not invent admin authorization around it without requirement.

---

# 126. Source Image Persistence Rule

The current product requires preservation of source text, not necessarily indefinite storage of original images.

Do not assume images must be archived forever.

If source image persistence is introduced, it must be documented explicitly.

---

# 127. OCR Retry Rule

OCR may be retried without creating duplicate reports.

No authoritative report exists until confirmation.

---

# 128. Analyze Retry Rule

Analyze may be retried on the same draft/input.

It must not create multiple authoritative reports by itself.

---

# 129. Save Draft Idempotency Rule

Saving the same draft repeatedly updates/resaves the same draft rather than creating uncontrolled duplicates.

---

# 130. Report Confirmation Idempotency Rule

The backend should prevent multiple identical confirmation actions from creating duplicate authoritative records.

---

# 131. Email Recipient Rule

Scheduled email recipients must come from explicit configuration.

Do not hardcode real personal/company addresses in source code.

---

# 132. Email Secret Rule

SMTP credentials are configuration secrets.

---

# 133. File Path Rule

The business layer must not construct arbitrary absolute file paths.

Use storage abstraction.

---

# 134. Filename Rule

Generated filenames should be deterministic and safe.

The exact naming convention belongs in reporting docs.

---

# 135. Report File Corruption Rule

If generation fails partially:

Do not register a broken file as successfully generated.

Clean up incomplete output where practical.

---

# 136. Report Open Rule

File open/view failure does not invalidate the underlying report.

---

# 137. User Share Rule

Sharing a report does not alter the authoritative business record.

---

# 138. Notification Read Rule

Marking a notification read changes notification state only.

It must not acknowledge/resolve the underlying business warning unless an explicit business action does so.

---

# 139. Warning Resolution Rule

A warning is resolved by correcting/confirming business data, not merely by dismissing the visual message.

---

# 140. Unrecognized Line Ignore Rule

If a user chooses to ignore an unrecognized line:

The action should be explicit.

The system should not make it disappear before that choice.

---

# 141. Unknown KPI Assignment Rule

If the user maps an unknown source line to a known KPI:

That mapping applies to the current report.

Automatically adding a new persistent alias should require deliberate behavior and should not happen silently.

---

# 142. Alias Learning Rule

Future alias-learning may be added.

Current parser must not mutate KPI definitions automatically from one user's correction unless explicitly implemented.

---

# 143. Parser Improvement Rule

Real parser failures should become:

```text
anonymized regression test
```

when practical.

---

# 144. Real Data Privacy Rule

Real screenshots/messages used in development/report assets must be treated responsibly.

Before public GitHub exposure:

- remove phone numbers
- remove personal names if sensitive
- remove private industrial details where required
- anonymize values if necessary

---

# 145. Assets Rule

Top-level `assets/` contains the private original screenshots used to understand the
real workflow. They must not be moved, modified, deleted, or publicly exposed.

Only sanitized derivatives created later may be used for parser tests, the academic
report, GitHub, or portfolio/demo material.

---

# 146. Business Rule Change Protocol

If a new requirement conflicts with this document:

1. identify the existing rule
2. explain the conflict
3. obtain approval
4. update this document
5. update affected API/database/UI docs
6. implement

Do not silently change business meaning through code.

---

# 147. Business Rule Testing Priority

Highest-priority rules for automated tests:

```text
missing != zero
partial report accepted
exact label outranks fuzzy
alias matching
decimal comma/point
unknown lines preserved
duplicate KPI warning
out-of-range warning
confirmation required
draft not authoritative
final value used downstream
confirmed report cannot be casually edited
scheduled generation does not fabricate missing values
generation failure does not alter confirmed data
email failure does not invalidate generated file
```

---

# 148. Business Rule Acceptance Checklist

Before marking a feature complete:

```text
[ ] Does it preserve raw source where required?
[ ] Does it keep missing separate from zero?
[ ] Does it support partial reports?
[ ] Does it avoid silent parser guesses?
[ ] Does it preserve unknown content?
[ ] Does it require confirmation?
[ ] Does it preserve extracted vs final value?
[ ] Does official downstream data use final confirmed value?
[ ] Does it preserve history?
[ ] Does it avoid duplicate authoritative state?
[ ] Does it keep report generation and delivery separate?
[ ] Does it keep notification/realtime channels non-authoritative?
```

---

# 149. Canonical Business Flow

```text
Source acquired
    ↓
Source preserved
    ↓
OCR if image
    ↓
Deterministic analysis
    ↓
Candidate KPI values
    ↓
Warnings / uncertainty exposed
    ↓
Maintenance engineer reviews
    ↓
Engineer edits/removes/adds
    ↓
Engineer confirms
    ↓
Final values become authoritative
    ↓
PostgreSQL
    ↓
Dashboard / History / Statistics
    ↓
Excel / PDF
    ↓
Manual sharing or scheduled delivery
```

This is the core business workflow.

---

# 150. Final Business Rule

Every major FactoryFlow decision should preserve one central promise:

> **The system may automate collection, interpretation, and reporting, but it must never silently take ownership of the engineer's final decision about official KPI data.**

---

# End of 04_Business_Rules.md
