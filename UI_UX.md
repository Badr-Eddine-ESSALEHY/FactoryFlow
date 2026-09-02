# UI_UX.md

> **FactoryFlow — Complete Mobile UI/UX Specification**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-22
>
> This document defines **how FactoryFlow behaves on screen**.
>
> `AGENTS.md` defines project constitution.
>
> `TASKS.md` defines what to build.
>
> `SKILLS.md` defines engineering standards.
>
> `DESIGN.md` defines visual language.
>
> `UI_UX.md` defines:
>
> - screen structure
> - navigation
> - user flows
> - interaction behavior
> - screen states
> - empty/loading/error/success behavior
> - transitions
> - validation behavior
> - OCR acquisition flows
> - report lifecycle UX
> - scheduling UX
> - notification UX
> - history/search/filter UX
> - dashboard UX
> - back-navigation rules
> - interruption/recovery behavior
>
> This is the canonical screen and interaction specification for the Android application.
>
> The Android UI language is professional French. Every user-facing string must live
> in Android string resources. English words in route names, type names, and technical
> flow diagrams are implementation terminology, not literal UI copy.
>
> The application must feel **premium, industrial, calm, clear, and fast**.
>
> The objective is not simply to make every screen attractive.
>
> The objective is to make the complete workflow feel effortless while preserving the central business rule:
>
> **automation assists; the maintenance engineer validates.**

---

# 1. Product UX Mission

FactoryFlow exists to replace a fragmented manual workflow:

```text
WhatsApp message
    ↓
Read values manually
    ↓
Open Excel
    ↓
Copy / type values
    ↓
Verify
    ↓
Prepare report
```

with a controlled digital workflow:

```text
Acquire information
    ↓
Extract / structure
    ↓
Review
    ↓
Correct
    ↓
Confirm
    ↓
Store
    ↓
Dashboard / history / statistics
    ↓
Generate / schedule / share reports
```

The UI must make this new workflow feel faster than the old workflow from the first use.

---

# 2. Primary User

Primary user:

```text
Maintenance Engineer
```

Expected user count in the first deployment:

```text
Approximately 2–4 maintenance staff
```

The UX must therefore optimize for:

- speed
- familiarity
- reliability
- low training cost
- repeat daily use

Do not design as if FactoryFlow were a social application or a complex administrative suite.

---

# 3. UX Principles

Every screen must follow these principles.

## 3.1 Immediate Orientation

Within seconds, the user should understand:

```text
Where am I?
What is the current status?
What can I do next?
```

---

## 3.2 One Primary Action

Each screen should have one dominant action where practical.

Examples:

```text
Se connecter
Analyser
Confirmer le rapport
Générer
Enregistrer la planification
```

Secondary actions remain visible but less prominent.

---

## 3.3 Progressive Disclosure

Show essential information first.

Reveal advanced detail when requested.

Do not overwhelm users with:

- parser internals
- database metadata
- technical confidence math
- unnecessary fields

---

## 3.4 Human Validation First

Every automatically extracted report must clearly lead to review.

The UI must never imply that OCR/parser results are already official.

---

## 3.5 Preserve User Work

Mobile sessions are interruptible.

The UX must protect:

- entered text
- manual KPI values
- parser results
- user corrections
- drafts

---

## 3.6 Calm Error Handling

Errors should guide.

Avoid:

```text
Something went wrong.
```

when a more useful action can be offered.

---

## 3.7 Operational Speed

Frequent actions must require minimal taps.

The engineer should not navigate through several decorative screens just to paste a report.

---

# 4. App Information Architecture

Primary conceptual destinations:

```text
Tableau de bord
Rapports
Créer
Notifications
```

The canonical navigation model is:

```text
Bottom Navigation

Tableau de bord
Rapports
Créer
Notifications
```

Profile/settings are accessed from the top-level profile action, not a fifth bottom destination.

The dashboard remains the default post-login destination.

---

# 5. Canonical Navigation Tree

```text
Splash
  ↓
Session Check
  ├── Not Authenticated → Login
  └── Authenticated → Dashboard

Dashboard
  ├── Create Report
  ├── Today Status
  ├── KPI Detail / Statistics
  ├── Recent Report Detail
  ├── Notifications
  └── Schedules

Create Report
  ├── Paste Text
  ├── Gallery
  └── Manual Entry

Paste / Gallery / Share
  ↓
Analyze / OCR
  ↓
Confirmation
  ├── Save Draft
  └── Confirm
       ↓
       Success
       ↓
       Report Detail / Dashboard

Reports
  ├── Maintenance Reports
  ├── Drafts
  ├── Generated Documents
  └── Search / Filters

Report Detail
  ├── KPI values
  ├── Source
  ├── Audit / metadata
  ├── Generate Excel
  ├── Generate PDF
  ├── Share
  └── Email

Notifications
  └── Deep link to relevant entity

Schedules
  ├── Daily
  ├── Weekly
  ├── Monthly
  └── Schedule Detail/Edit

Top-level Profile Action
  ├── Profile
  ├── Settings
  ├── About
  └── Logout
```

---

# 6. Global Navigation Rules

## 6.1 Dashboard Is Home

After successful login:

```text
Login
→ Dashboard
```

Back from Dashboard must not return to Login.

---

## 6.2 Terminal Flow Cleanup

After report confirmation:

Do not leave the confirmation stack in a way that allows:

```text
Confirmed report
→ Back
→ editable confirmation screen
```

After successful confirmation, the old editable workflow must be removed or made read-only.

---

## 6.3 External Entry

When FactoryFlow opens from Android Share Intent:

```text
External app
→ FactoryFlow acquisition
→ OCR
→ Confirmation
```

After the workflow completes, back behavior should return logically to FactoryFlow rather than recreating the external share input state.

---

## 6.4 Notification Deep Links

Tapping a notification should navigate to the most relevant screen.

Examples:

```text
Generated report ready
→ Generated Report Detail

Report needs review
→ Confirmation / Draft Detail

Schedule failed
→ Schedule / Generated Report status
```

---

## 6.5 Preserve Navigation Context

Opening a report from history:

```text
Reports
→ Report Detail
→ Back
→ Reports with filters preserved
```

Do not reset search/filter state unnecessarily.

---

# 7. App Startup

## 7.1 Splash Screen

Purpose:

- brand presence
- platform startup
- session restoration

Visual:

```text
FactoryFlow icon
neutral/premium background
```

Do not show:

- long slogans
- loading percentage
- animation sequence

Duration should be platform-controlled and short.

---

## 7.2 Startup Decision

Flow:

```text
Splash
  ↓
Read local session state
  ↓
Access token valid?
  ├── Yes → Dashboard
  └── No → Clear session → Login
```

Refresh tokens are not implemented in the current client/backend contract.

---

## 7.3 Startup Loading

If session restoration takes noticeable time:

Use a clean centered progress indicator.

Do not display a blank white screen.

---

## 7.4 Startup Error

If local session state is corrupted:

Clear invalid session safely and navigate to Login.

Do not trap user on splash.

---

# 8. Login Screen

## 8.1 Goal

Authenticate quickly and establish trust.

---

## 8.2 Layout

Recommended hierarchy:

```text
FactoryFlow mark
Welcome back
Short supporting line

Email
Password

Login button

Optional subtle environment/build text
```

No registration flow unless explicitly added later.

---

## 8.3 Email Input

Behavior:

- email keyboard
- auto-capitalization disabled
- trim surrounding whitespace
- visible label
- inline invalid-email error

---

## 8.4 Password Input

Behavior:

- masked by default
- trailing show/hide action
- no auto-correction
- keyboard done action triggers login if valid

---

## 8.5 Login Button

States:

```text
Disabled
Enabled
Loading
```

Loading should occur inside the button or as a focused screen state.

Do not allow repeated login taps.

---

## 8.6 Invalid Credentials

Message:

```text
L’adresse e-mail ou le mot de passe est incorrect.
```

Do not reveal whether a user account exists.

---

## 8.7 Network Error

Message:

```text
FactoryFlow ne parvient pas à joindre le serveur.
Vérifiez votre connexion, puis réessayez.
```

Action:

```text
Réessayer
```

Preserve entered email.

---

## 8.8 Successful Login

Flow:

```text
Login
→ store session securely
→ Dashboard
```

Use a subtle crossfade/transition.

No celebratory modal.

---

# 9. Dashboard

## 9.1 Purpose

Dashboard is the operational home.

It must answer:

```text
What is happening today?
What needs attention?
What are the latest KPI values?
What was recently generated?
What can I do next?
```

---

## 9.2 Dashboard Layout

Recommended structure:

```text
Top Bar
  FactoryFlow / date
  Notification action
  Optional profile avatar

Today's Report Status

Critical / Latest KPI Overview

Quick Actions

Needs Attention

Recent Reports

Statistics / Trend Preview

Upcoming Schedule
```

Do not make every section equally large.

---

# 10. Dashboard Top Bar

Possible content:

```text
Good morning
Tuesday, 11 August
```

or simply:

```text
FactoryFlow
11 August 2026
```

Right side:

```text
Notifications icon
Profile avatar/menu
```

Do not display full navigation redundantly.

---

# 11. Today's Report Status

This is the highest-priority dashboard card.

Possible states:

```text
Valeur manquante
À vérifier
Brouillon
Confirmé
Généré
```

Examples:

### Missing

```text
Rapport du jour
Aucun rapport confirmé

[Créer un rapport]
```

### Draft

```text
Rapport du jour
Brouillon en cours
3 valeurs KPI déjà vérifiées

[Reprendre]
```

### Pending Review

```text
Rapport du jour
À vérifier
2 valeurs nécessitent votre attention

[Vérifier]
```

### Confirmed

```text
Rapport du jour
Confirmé à 14:12

[Voir le rapport]
```

### Generated

```text
Rapport du jour
Confirmé et généré

[Ouvrir le rapport]
```

Only `DRAFT`, `PENDING_REVIEW`, and `CONFIRMED` are persisted report states. Missing
and generated indicators are dashboard/read-model projections.

---

# 12. KPI Overview

Show a limited number of high-value KPIs.

Each card/row may include:

```text
KPI name
latest confirmed value
unit
trend/status
last updated
```

Do not show all KPI definitions at once if there are many.

Action:

```text
View all / Statistics
```

---

# 13. Quick Actions

Recommended quick actions:

```text
Paste Text
Gallery
Manual Entry
```

Visual:

- compact
- consistent
- icon + label

Do not include Share Intent as a visible quick action because it is entered from external apps.

---

# 14. Needs Attention

Possible items:

```text
Draft waiting
Low-confidence report awaiting review
Missing expected report
Scheduled generation failed
Threshold warning
```

Show only actionable items.

---

# 15. Recent Reports

Display last few confirmed/generated reports.

Each item:

```text
Date
Type
Status
Submitted by
Generated formats if available
```

Tap:

```text
→ Report Detail
```

---

# 16. Trend Preview

One small useful chart only.

Example:

```text
KPI evolution — last 7 days
```

Action:

```text
View Statistics
```

Do not place multiple decorative charts on Dashboard.

---

# 17. Upcoming Schedule

Compact summary:

```text
Next scheduled report
Daily · Today 18:00
Excel + PDF
```

Tap:

```text
→ Schedule Detail
```

---

# 18. Dashboard Loading

Use skeletons matching:

- report status card
- KPI rows
- recent report rows

Avoid one giant spinner.

---

# 19. Dashboard Empty State

If there is no data at all:

```text
No reports yet

Create your first maintenance report by pasting text,
importing an image, taking a photo, or entering values manually.

[Create Report]
```

Still show quick actions.

---

# 20. Dashboard Partial Failure

If one section fails:

Do not blank the whole dashboard.

Example:

```text
Latest KPI data unavailable
[Retry]
```

while other sections remain visible.

---

# 21. Dashboard Realtime Update

When WebSocket event arrives:

```text
event
→ refresh relevant authoritative REST data
→ subtle update transition
```

Do not flash full screen.

---

# 22. Create Report Entry

The Create action opens a simple acquisition screen or bottom sheet.

Recommended title:

```text
Create Report
```

Supporting text:

```text
Choose how you want to add KPI information.
```

Options:

```text
Paste Text
Import from Gallery
Take Photo
Enter Manually
```

Each option includes:

- icon
- one-line description

---

# 23. Paste Text Screen

## 23.1 Purpose

Fastest path for copying raw WhatsApp text.

---

## 23.2 Layout

```text
Top Bar
Title: Paste KPI Message

Supporting text

Multiline text area

Optional Paste from Clipboard action

Analyze button
```

---

## 23.3 Text Area

Requirements:

- multiline
- large enough for realistic messages
- preserve line breaks
- clear focus state
- supports long pasted content
- auto-scroll

Do not auto-format the source text destructively.

---

## 23.4 Clipboard Action

If clipboard content exists:

```text
Paste from Clipboard
```

may be offered.

Do not automatically read clipboard without user action if platform behavior/privacy discourages it.

---

## 23.5 Empty Input

Analyze disabled until meaningful text exists.

If user attempts:

```text
Paste or type the KPI message first.
```

---

## 23.6 Analyze

Tap:

```text
Analyze
```

Flow:

```text
Raw text
→ backend /analyze
→ loading
→ Confirmation
```

---

## 23.7 Analyze Loading

Keep source visible where practical.

Button:

```text
Analyzing…
```

Do not permit duplicate requests.

---

## 23.8 Analyze Error

Preserve text.

Display inline/banner:

```text
Couldn’t analyze this message.
Your text has been kept.

[Try Again]
```

Optional secondary:

```text
Enter Manually
```

---

# 24. Gallery Import

## 24.1 Entry

Tap:

```text
Import from Gallery
```

Use modern Android picker.

---

## 24.2 Image Selected

Show a short preview screen or compact preview state.

Content:

```text
Selected image preview
Replace image
Continue
```

If OCR can begin immediately without sacrificing clarity, skip a redundant confirmation screen.

---

## 24.3 OCR Flow

```text
Image selected
→ PaddleOCR backend OCR
→ extracted text
→ Analyze API
→ Confirmation
```

---

## 24.4 OCR Progress

State copy may be staged:

```text
Reading image…
```

then:

```text
Analyzing KPI values…
```

Only display stages that actually exist.

---

## 24.5 OCR Failure

Message:

```text
FactoryFlow couldn’t read enough text from this image.
```

Actions:

```text
Try Another Image
Enter Manually
```

If partial OCR text exists and is useful, allow:

```text
Review Extracted Text
```

---

## 24.6 Unsupported Image

Message:

```text
This image format could not be opened.
Choose another image.
```

---

# 25. Android Share Intent Acquisition

## 25.1 Purpose

This is one of the strongest FactoryFlow flows.

A maintenance engineer should be able to:

```text
WhatsApp
→ open screenshot/image
→ Share
→ FactoryFlow
```

and continue directly.

---

## 25.2 Cold Start

If app is closed:

```text
Share image
→ FactoryFlow splash/session check
→ shared image acquisition
```

If authentication is required:

```text
shared payload retained temporarily
→ Login
→ resume acquisition
```

Do not silently discard the shared image after login.

---

## 25.3 Warm Start

If app already open:

Open acquisition flow without destroying the existing navigation stack unexpectedly.

---

## 25.4 Shared Image Screen

Optional compact context:

```text
Imported from another app
[image preview]
Reading image…
```

No need to mention WhatsApp specifically because Android sharing can come from other apps.

---

## 25.5 Share OCR Failure

Preserve shared URI while valid.

Actions:

```text
Retry
Choose another method
```

---

# 27. Manual Entry

## 27.1 Purpose

Manual entry is the reliable fallback and a first-class acquisition path.

---

## 27.2 Layout

```text
Title: Enter KPI Values

Add KPI

KPI rows/cards

Review button
```

---

## 27.3 Adding KPI

Tap:

```text
Add KPI
```

Open bottom sheet/search selector.

Show:

```text
search
KPI name
category
unit
```

Selecting KPI adds it to current report.

---

## 27.4 KPI Entry Row

Content:

```text
KPI label
value field
unit
warning/helper
remove action
```

Use numeric keyboard.

---

## 27.5 Duplicate KPI

If already added:

```text
This KPI is already in the report.
```

Do not create duplicate entries silently.

---

## 27.6 Plausibility Warning

If entered value is outside configured range:

```text
Outside expected range
Review before confirming.
```

Do not block unless business rules explicitly require.

---

## 27.7 Manual Review

Tap:

```text
Review
```

Manual data proceeds to the same confirmation/review semantics where practical.

The user should see a consistent final confirmation experience.

---

# 28. Analyze Result Transition

After parser returns results:

Never jump directly to saved report.

Always navigate to:

```text
Confirmation
```

for parsed/OCR input.

---

# 29. Confirmation Screen

## 29.1 Purpose

This is the most important integrity screen in FactoryFlow.

It must make verification:

- fast
- obvious
- safe
- low-friction

---

## 29.2 Screen Structure

Recommended:

```text
Top Bar
Title: Review Report
Source type / timestamp

Summary banner
  X values recognized
  Y need review
  Z unrecognized lines

KPI Entry List

Unrecognized Content

Source Preview / Raw Text

Sticky bottom actions
  Save Draft
  Confirm Report
```

---

# 30. Confirmation Summary

Possible example:

```text
12 KPI values found
2 need review
1 line was not recognized
```

Do not use alarming language unless necessary.

---

# 31. Confirmation KPI Entry

Each entry should show:

```text
KPI name
Editable final value
Unit
Status / warning
Extracted source value if different
Confidence status
```

Possible visual states:

```text
Normal
Edited
Needs Review
Out of Range
Unknown Unit
```

---

# 32. Confidence Presentation

Prefer human-readable labels:

```text
High confidence
Review suggested
Low confidence
```

Raw confidence score may appear in secondary detail if useful.

Do not make percentages the primary UX.

---

# 33. Edited Value

When user changes a value:

Show subtle:

```text
Edited
```

Original extracted value may remain visible as secondary metadata:

```text
Detected: 14.8
```

Final field:

```text
15.8
```

---

# 34. Out-of-Range Warning

Example:

```text
Hors de la plage attendue
Valeur attendue : environ 10–20 t
```

Do not automatically reject.

---

# 35. Remove False Extraction

User may remove an incorrect extracted entry.

Action:

```text
Supprimer
```

Use contextual menu or swipe only if discoverable.

If removal is reversible, offer snackbar:

```text
Valeur supprimée
Annuler
```

---

# 36. Add Missing KPI

Action:

```text
Ajouter un KPI
```

opens KPI selector.

This is important when parser misses a field.

---

# 37. Unrecognized Lines

Section:

```text
À vérifier
```

or:

```text
Contenu non reconnu
```

Each item shows:

```text
original line
```

Actions may include:

```text
Associer à un KPI
Ignorer
```

For a KPI-like unresolved entry, FactoryFlow chooses the primary action from the
deterministic match result:

- when an existing KPI suggestion exists, show `Associer à <KPI>` and keep the
  manual `Associer à un indicateur` alternative;
- when no equivalent/suggested KPI exists, show `Ajouter un nouvel indicateur`;
- creation happens only after the engineer taps the action, creates or reuses the
  normalized definition, associates the current extraction, and stays in Review.

The beginning of `Non reconnus` exposes `Ignorer tout`. This action delegates to
the backend source-line classifier and ignores only safe metadata/noise; it never
blindly resolves uncertain KPI-like content.

Resolving, assigning, creating, or ignoring an item keeps the current Review tab
and approximately the same lazy-list position while actionable items remain.

Entries in `À vérifier` expose `Valider`; accepting the editable value moves that
entry to `Prêtes` without leaving the screen. Attention and unresolved entries block
final confirmation until explicitly processed. Duplicate KPI conflicts still require
the user to remove the unwanted observation rather than acknowledging both.

If `Ignore` is allowed, make it explicit rather than silently dropping the line.

Persisted resolution is one of `UNRESOLVED`, `ASSIGNED`, or `IGNORED`; these are API
values and are displayed with French labels.

---

# 38. Raw Source

Provide access to the original source.

Options:

- collapsible section
- bottom sheet
- dedicated "View source" action

Do not force a long raw message to remain expanded above the validation list.

---

# 39. Save Draft

Tap:

```text
Enregistrer le brouillon
```

Behavior:

```text
persist current state
→ success snackbar
→ remain or navigate depending context
```

If user explicitly leaves afterward:

Navigate to Dashboard or Reports.

---

# 40. Auto-Save Draft

If technically reliable, temporary edits may auto-save locally/server-side.

But do not claim auto-save until implemented.

If explicit save is the initial implementation, use it consistently.

---

# 41. Confirm Report

Primary action:

```text
Confirmer le rapport
```

Before request:

Validate that required decisions are resolved.

---

# 42. Confirmation with Warnings

If warnings remain but are allowed:

Show a concise final sheet/dialog:

```text
2 valeurs nécessitent encore votre attention

Vous pouvez les confirmer telles quelles, mais une vérification est recommandée.

[Vérifier à nouveau]
[Confirmer quand même]
```

Only if business rules allow confirmation with warnings.

Do not create this extra step when no warnings remain.

---

# 43. Confirmation Loading

Primary button becomes:

```text
Confirmation…
```

Disable editing while authoritative confirmation request is being committed.

---

# 44. Confirmation Success

Use a short success state:

```text
Rapport confirmé
```

with subtle check animation.

Then navigate to:

```text
Détail du rapport
```

because the user can immediately verify result and generate/share outputs.

Remove the editable confirmation destination from the Back stack. Back must not reopen it.

A clear "Back to Dashboard" action may be available.

---

# 45. Confirmation Failure

Preserve all edits.

Message:

```text
Couldn’t confirm this report.
Your changes are still here.

[Try Again]
```

Do not clear the form.

---

# 46. Draft List

Drafts should appear in Reports.

Each row:

```text
Date / created time
source type
number of reviewed KPI values
last edited
status: Draft
```

Primary action:

```text
Resume
```

---

# 47. Draft Deletion

Tap delete:

```text
Delete this draft?

Your saved edits will be removed.

[Cancel]
[Delete Draft]
```

---

# 48. Reports Main Screen

Recommended tabs or segmented control:

```text
Reports
Drafts
Generated
```

Alternative:

```text
Maintenance Reports
Generated Documents
```

with Drafts as filter.

Preferred for clarity:

```text
Maintenance
Generated
```

and show Draft/Confirmed status within Maintenance.

Final choice should prioritize simplicity.

---

# 49. Maintenance Reports List

Each row/card:

```text
Date
Status
Source
Submitted by
KPI count
Warnings if relevant
```

Tap:

```text
→ Report Detail
```

---

# 50. Report Search

Search field at top.

Search may match:

- KPI name
- report identifier
- submitter
- date text where practical

Do not overpromise full-text search if backend only supports filters.

---

# 51. Report Filters

Open filter bottom sheet.

Possible controls:

```text
Date range
Status
Source
Submitter
KPI
```

Actions:

```text
Reset
Apply Filters
```

Selected filter count can appear on filter icon.

---

# 52. Filter Persistence

Returning from Report Detail should preserve active filters.

Leaving Reports entirely may preserve session state until app restart.

---

# 53. No Search Results

```text
No matching reports

Try changing your search or filters.
```

Action:

```text
Clear Filters
```

---

# 54. Report Detail

## 54.1 Header

Show:

```text
Report date
status
source
submitted by
confirmed time
```

---

## 54.2 KPI Values

Use clean structured rows:

```text
KPI
final value
unit
```

If user edited from extracted value, optional detail:

```text
Detected 14.8 → Confirmed 15.8
```

Do not show this metadata for every row unless it adds value.

---

## 54.3 Source

Provide:

```text
View Original Source
```

for pasted/OCR reports.

---

## 54.4 Actions

Possible:

```text
Exporter ce rapport uniquement
  ├── Excel
  └── PDF
Voir les fichiers générés
Partager
```

Only show relevant actions.

---

## 54.5 Audit Metadata

Secondary section:

```text
Created
Confirmed by
Confirmation time
Acquisition method
```

Detailed audit history may be future/secondary.

---

# 55. Generated Documents Screen

The screen begins with a distinct action:

```text
Générer un rapport consolidé
```

Its bottom sheet offers `Jour`, `Semaine`, `Mois`, or `Période personnalisée`,
an appropriate Material date/range picker, and independent Excel/PDF selection.
This action is never presented as exporting one report.

Each item:

```text
Filename
Excel / PDF
Report type
Period
Generated time
Delivery status if scheduled
```

Actions:

```text
Ouvrir
Partager
Envoyer par e-mail
```

---

# 56. Generated Document Detail

Show:

```text
File name
Format
Reporting period
Generated at
Generated by / Scheduled
File size if available
Delivery state
```

Actions:

```text
Ouvrir
Partager
Envoyer par e-mail
Régénérer
```

Regenerate only if business flow supports it.

---

# 57. Excel Generation

Tap:

```text
Générer le fichier Excel
```

State:

```text
Preparing Excel…
```

On success:

```text
Excel report generated
```

Actions:

```text
Open
Share
```

---

# 58. PDF Generation

Same pattern.

```text
Generating PDF…
```

Success:

```text
PDF report generated
```

---

# 59. Report Generation Failure

Message:

```text
Couldn’t generate the PDF.
The confirmed KPI data is safe.

[Try Again]
```

Important: communicate that business data was not lost.

---

# 60. Open Generated File

Use appropriate Android viewer.

If no app can open:

```text
No compatible app was found to open this file.
```

Offer:

```text
Share
```

---

# 61. Share Generated File

Use Android share sheet.

No custom contact picker required.

---

# 62. Email Generated File

Tap Email:

Open email-capable chooser/application with:

```text
attachment
suggested subject
suggested body
```

User selects recipient and sends.

Do not display "Email sent" unless the app can genuinely know that outcome.

Because external email app owns final delivery.

---

# 63. Scheduled Reports Screen

Purpose:

Manage daily/weekly/monthly generation schedules.

Display schedule times in `Africa/Casablanca`. If one missed run is recovered, show
one resulting run/document only; duplicate recovery output must not appear in the UI.

---

# 64. Schedule List

Each card:

```text
Rapport quotidien
Activé
18:00
Excel + PDF
Prochaine exécution : aujourd’hui à 18:00
```

or:

```text
Rapport hebdomadaire
Lundi · 08:00
```

---

# 65. Schedule Status

Use:

```text
Activé
En pause
La dernière exécution a échoué
```

Do not overuse red.

---

# 66. Create/Edit Schedule

Fields:

```text
Type de planification
Heure
Jour de la semaine pour une planification hebdomadaire
Formats
Envoi automatique par e-mail et destinataires, si configurés
Activé
```

Only expose controls actually supported by backend.

---

# 67. Daily Schedule

Required:

```text
Heure
Formats
Options d’envoi
Activé
```

---

# 68. Weekly Schedule

Required:

```text
Jour de la semaine
Heure
Formats
Envoi
Activé
```

The generated reporting period is always the previous Monday-through-Sunday calendar week.

---

# 69. Monthly Schedule

Required:

```text
Heure
Formats
Envoi
Activé
```

Monthly semantics are fixed: at the configured time on the first day of a month,
generate the complete previous calendar month. Do not expose a day-of-month field.

---

# 70. Schedule Save

Primary:

```text
Enregistrer la planification
```

Success:

```text
Planification mise à jour
```

---

# 71. Schedule Delete

Only if schedules are user-created.

If system schedules are fixed, use Enable/Disable rather than Delete.

---

# 72. Scheduled Generation Status

Generated report detail may show:

```text
Generated automatically
Email delivered
```

or:

```text
Generated automatically
Email delivery failed
```

Provide:

```text
Retry Email
```

if implemented.

---

# 73. Notifications Screen

List structure:

```text
Unread notification
Read notification
```

Each:

```text
icon
title
short message
time
```

---

# 74. Notification Types

Examples:

```text
Report generated
Report missing
Threshold exceeded
Draft reminder
Scheduled delivery failed
```

---

# 75. Unread State

Use subtle:

- accent dot
- slightly stronger title
- tonal background

Do not use giant color blocks.

---

# 76. Notification Tap

Navigate to relevant entity.

Examples:

```text
Report generated
→ Generated Report Detail

Threshold exceeded
→ Report Detail / Statistics

Draft reminder
→ Draft
```

---

# 77. Notification Empty State

```text
No notifications

Important report updates and reminders will appear here.
```

---

# 78. Notification Error

If server unavailable:

Keep cached notifications if available.

Show inline:

```text
Couldn’t refresh notifications.
```

---

# 79. Future FCM Foreground Behavior

FCM is not implemented. Current notification UX uses persisted in-app notifications.
If FCM is added later:

When app is active:

Prefer in-app notification/snackbar rather than always showing a system notification.

Example:

```text
Daily report generated
[View]
```

---

# 80. Future FCM Background Behavior

Use system notification.

Tapping should deep link appropriately.

---

# 81. Statistics Screen

Purpose:

Help engineer understand confirmed KPI trends.

---

# 82. Statistics Header

Controls:

```text
KPI selector
Period selector
```

Potential periods:

```text
7 days
30 days
This month
Custom
```

Do not create too many filters initially.

---

# 83. Statistics Summary

Show:

```text
Latest
Average
Min
Max
Variation
```

only where meaningful.

---

# 84. Statistics Chart

One main chart.

Use:

- clear axis
- unit
- date labels
- subtle grid
- selected-point tooltip

---

# 85. Statistics Empty State

```text
Not enough confirmed data yet

Statistics will appear after more KPI reports are confirmed.
```

---

# 86. Statistics Error

```text
Couldn’t load KPI statistics.
[Retry]
```

---

# 87. KPI Detail

If separate screen exists:

```text
KPI name
latest value
unit
expected range
trend
recent values
```

Do not expose configuration editing to normal users unless KPI management is intentionally part of app UX.

---

# 88. KPI Definition Management

If included in the mobile application, it should remain an advanced screen.

Possible path:

```text
More
→ KPI Definitions
```

Because all current users have same effective role, do not create admin-only visuals.

---

# 89. KPI Definition List

Show:

```text
display name
code
unit
active state
alias count
```

---

# 90. KPI Definition Detail/Edit

Fields:

```text
Display name
Code
Category
Unit
Plausible min
Plausible max
Aliases
Active
```

Use careful validation.

Changing KPI definitions may affect future parsing.

Show a concise information banner:

```text
Changes affect future report analysis.
Historical confirmed reports are preserved.
```

---

# 91. Alias Editing

Aliases should be easy to add/remove.

Use chip-like input.

Prevent duplicate aliases where possible.

---

# 92. Profile Screen

Simple.

Show:

```text
Name
Email
Role label: Maintenance Engineer
App/session information
Logout
```

Do not create unnecessary profile customization.

---

# 93. Settings Screen

Potential settings:

```text
Theme
Notification preferences if supported
Default report format if supported
About
```

Do not create settings for features not implemented.

---

# 94. Theme Setting

If manual theme selection is supported:

```text
System
Light
Dark
```

Otherwise follow system automatically.

---

# 95. About Screen

Show:

```text
FactoryFlow
Version
Short mission
Technology / legal links if needed
```

Keep concise.

---

# 96. Logout

Tap:

```text
Logout
```

If unsaved local draft edits exist:

Prompt appropriately.

Otherwise:

```text
clear session
→ Login
```

Do not delete server-side drafts.

---

# 97. Global Loading Behavior

Use three categories:

## Inline

For button actions.

## Section

For dashboard/list section refresh.

## Full Screen

Only when screen cannot meaningfully render before data exists.

---

# 98. Global Empty Behavior

Every empty state should answer:

```text
What is empty?
Why?
What can I do?
```

---

# 99. Global Error Behavior

Error message structure:

```text
What failed
What remains safe
What user can do
```

Example:

```text
Couldn’t generate the PDF.
Your confirmed report is safe.
Try again when the connection is available.
```

---

# 100. Global Warning Behavior

Warnings are review cues.

They should not be visually confused with hard errors.

Use:

```text
Needs review
Outside expected range
Missing value
```

---

# 101. Global Success Behavior

Success states should be concise.

Examples:

```text
Draft saved
Report confirmed
PDF generated
Schedule updated
```

Use snackbar or inline state.

Avoid unnecessary modal dialogs.

---

# 102. Network Offline Behavior

FactoryFlow depends on backend for authoritative operations.

If network disappears:

- preserve entered text
- preserve current edits where practical
- prevent destructive reset
- show connection state
- offer retry

Do not claim full offline support.

---

# 103. Offline During Confirmation

If user taps Confirm with no network:

Do not mark report confirmed locally.

Message:

```text
No connection
Your changes are still saved locally/in this draft.
Reconnect and try again.
```

Exact persistence depends on implemented draft architecture.

---

# 104. Offline During Draft

If server save fails and local Room draft support exists:

Save locally and communicate pending sync.

If local persistence is not yet implemented:

Preserve in ViewModel/process-safe state where possible and explain limitation.

Do not fake sync behavior.

---

# 105. Session Expiry

If access token expires during use:

Attempt refresh silently.

If refresh fails:

```text
Session expired
Please sign in again.
```

Preserve unsaved draft data if safely possible.

---

# 106. Duplicate Submission Protection

If confirmation request is in progress:

Disable repeated confirm taps.

If response is uncertain after network loss:

Do not blindly resubmit without checking authoritative state if duplicate creation is possible.

---

# 107. Back Navigation During Editing

If user has unsaved meaningful changes:

Either:

- auto-save draft
- show save/discard prompt

Do not silently discard.

---

# 108. App Backgrounding

On background:

Do not lose active report state.

On return:

Restore exact workflow screen when practical.

---

# 109. Process Recreation

Critical user state should survive reasonable Android recreation through ViewModel/saved state/local persistence where appropriate.

---

# 110. Rotation

If orientation changes are supported:

Do not reset forms or restart OCR/report generation accidentally.

If app is portrait-optimized, still ensure rotation does not corrupt state.

---

# 111. Keyboard UX

For forms:

- use correct IME
- next action moves focus
- done triggers appropriate action
- focused field remains visible
- keyboard does not cover sticky action

---

# 112. Accessibility

Screen readers should understand:

- button labels
- status
- warnings
- KPI values
- navigation

Do not rely only on icon appearance.

---

# 113. Touch Targets

Critical actions:

```text
minimum ~48dp effective target
```

Do not use tiny edit/delete icons.

---

# 114. Pull-to-Refresh

Appropriate on:

```text
Dashboard
Reports
Notifications
Statistics
```

if consistent with selected Material behavior.

Do not add pull-to-refresh on static forms.

---

# 115. Swipe Actions

Use sparingly.

Potential:

```text
Swipe draft → delete
```

only if also accessible through visible menu/action.

Do not hide critical functionality solely behind gestures.

---

# 116. Long Press

Avoid unless standard Android behavior makes sense.

Do not require long-press for essential actions.

---

# 117. Bottom Sheets

Use for:

```text
filters
KPI selector
acquisition choices
context actions
```

Keep them task-focused.

---

# 118. Dialogs

Use for:

```text
destructive confirmation
important unresolved warning decision
session/logout with unsaved work
```

Avoid using dialogs as information screens.

---

# 119. Snackbars

Use for temporary feedback:

```text
Draft saved
Entry removed — Undo
Schedule updated
```

Include action only if useful.

---

# 120. Motion Principles

Use:

```text
Fast
Subtle
Purposeful
```

Recommended:

```text
120–320ms
```

Avoid long full-screen transitions.

---

# 121. Screen Transition Guidance

### Dashboard → Report Detail

Subtle shared-axis/horizontal.

### Create → Acquisition

Simple forward transition.

### Analyze → Confirmation

Crossfade/forward transition.

### Confirm → Success → Detail

Short success state + forward navigation.

### Modal/bottom sheet

Material standard motion.

---

# 122. State Transition Motion

Good uses:

- status chip changes
- warning expansion
- list item insertion
- report status update

Do not animate large layout shifts aggressively.

---

# 123. Reduced Motion

Respect system preference where practical.

Critical information must not depend on animation.

---

# 124. Premium Interaction Standard

Every tappable element should feel responsive immediately.

Even if backend work takes time:

```text
tap
→ immediate pressed state
→ loading state
```

Never make the user wonder whether the tap registered.

---

# 125. Premium Waiting Standard

If operation takes > approximately one second:

Show context.

Examples:

```text
Reading image…
Analyzing report…
Generating PDF…
```

Do not use fake progress percentages.

---

# 126. Premium Error Standard

Never punish the user for backend complexity.

Bad:

```text
500 Internal Server Error
```

Good:

```text
Couldn’t generate this report.
Your confirmed KPI data is safe.
```

---

# 127. Premium Validation Standard

Validation must feel collaborative.

Use:

```text
Needs review
```

rather than:

```text
Invalid
```

when the system is uncertain rather than certain.

---

# 128. First-Run Experience

No long onboarding.

On empty Dashboard:

```text
Create your first report

Paste a KPI message, import a screenshot,
take a photo, or enter values manually.

[Create Report]
```

That teaches the system through action.

---

# 129. Demo Flow

The strongest presentation flow should be rehearsable as:

```text
Open FactoryFlow
→ Dashboard
→ Create Report
→ Share/import WhatsApp screenshot
→ OCR
→ Confirmation
→ edit one low-confidence value
→ Confirm
→ Dashboard updates
→ Open Report
→ Generate PDF
→ Share
```

This flow should be visually polished enough for portfolio recording.

---

# 130. Secondary Demo Flow

```text
Open Reports
→ Search
→ Filter
→ Open historical report
→ View final confirmed KPI values
→ Open generated Excel
```

---

# 131. Automation Demo Flow

```text
Open Schedules
→ Show Daily schedule
→ Trigger/test scheduled generation
→ Generated report appears
→ Notification arrives
→ Delivery status visible
```

---

# 132. Realtime Demo Flow

If WebSocket implemented:

```text
Confirm report
→ dashboard status updates without manual refresh
```

Use subtle UI transition.

---

# 133. FCM Demo Flow

If FCM implemented:

```text
App in background
→ report generated
→ notification
→ tap
→ generated report detail
```

---

# 134. No-Data Demo Preparation

For clean demonstrations, maintain safe anonymized demo data.

Do not rely on private industrial messages.

---

# 135. UI Copy Style

Tone:

```text
Professional
Direct
Calm
Short
```

Avoid:

```text
Oops!
Awesome!
Great job!
Uh-oh!
```

unless product tone intentionally changes.

---

# 136. Button Copy

Prefer specific actions.

```text
Analyser
Vérifier
Confirmer le rapport
Enregistrer le brouillon
Générer le PDF
Partager
Réessayer
```

---

# 137. Status Copy

Canonical status language should remain consistent.

Recommended:

```text
Brouillon
À vérifier
Confirmé
Généré
Valeur manquante
Échec
À vérifier
```

---

# 138. Date Copy

Use human-readable dates.

Examples:

```text
Aujourd’hui, 14:20
11 août 2026
Cette semaine
Août 2026
```

---

# 139. Numeric Copy

Use locale-aware display where appropriate.

Do not silently change stored precision.

Display unit beside value consistently.

---

# 140. Loading Copy Library

Approved patterns:

```text
Loading dashboard…
Analyzing report…
Reading image…
Generating Excel…
Generating PDF…
Saving draft…
Confirming report…
Refreshing history…
```

---

# 141. Error Copy Library

Examples:

```text
Couldn’t reach the server.
Check your connection and try again.

Couldn’t read this image.
Try another image or enter the values manually.

Couldn’t analyze this report.
Your source text has been preserved.

Couldn’t confirm this report.
Your changes are still here.

Couldn’t generate the PDF.
Your confirmed report is safe.
```

---

# 142. Empty Copy Library

Examples:

```text
No reports yet
Confirmed reports will appear here.

No drafts
Saved report drafts will appear here.

No generated files
Generate an Excel or PDF report to see it here.

No notifications
Important report updates will appear here.

No matching reports
Try changing your search or filters.
```

---

# 143. Warning Copy Library

Examples:

```text
Needs review

Low-confidence match

Outside expected range

No value reported

Unrecognized line
```

---

# 144. Success Copy Library

Examples:

```text
Report confirmed

Draft saved

Excel generated

PDF generated

Schedule updated
```

---

# 145. Dashboard Screen States Matrix

| State | UX |
|---|---|
| Loading | Skeleton cards/rows |
| Empty | First-report CTA |
| Partial data | Show available sections |
| Error | Section-level retry |
| Realtime update | Subtle refresh transition |
| Offline | Banner + cached content if available |

---

# 146. Paste Screen States Matrix

| State | UX |
|---|---|
| Empty | Analyze disabled |
| Editing | Normal input |
| Loading | Analyze button progress |
| Error | Preserve text + retry |
| Success | Navigate to Confirmation |

---

# 147. OCR Screen States Matrix

| State | UX |
|---|---|
| No image | acquisition |
| Selected | preview |
| Reading | OCR loading |
| Analyze | parser loading |
| OCR failure | retry/manual fallback |
| Analyze failure | retry/preserve text |
| Success | Confirmation |

---

# 148. Confirmation States Matrix

| State | UX |
|---|---|
| Normal | editable KPI list |
| Warnings | highlighted entries |
| Edited | edited marker |
| Saving draft | disabled action/loading |
| Draft saved | snackbar |
| Confirming | lock duplicate action |
| Confirmation failed | preserve edits |
| Success | short success → detail |

---

# 149. Reports States Matrix

| State | UX |
|---|---|
| Loading | skeleton list |
| Empty | create-report CTA |
| Filtered empty | clear filters |
| Error | retry |
| Offline cached | stale-data indicator if applicable |

---

# 150. Generated Files States Matrix

| State | UX |
|---|---|
| Empty | explain generation |
| Generating | inline progress |
| Success | open/share |
| Failed | retry |
| Email failed | report remains available |

---

# 151. Schedule States Matrix

| State | UX |
|---|---|
| No schedule | create CTA if user-configurable |
| Enabled | next run visible |
| Paused | muted status |
| Running | progress/status |
| Last run failed | actionable warning |

---

# 152. Notifications States Matrix

| State | UX |
|---|---|
| Loading | skeleton |
| Empty | informative empty state |
| Error | retry |
| Unread | subtle emphasis |
| Read | neutral |
| Deep link unavailable | open closest relevant screen |

---

# 153. Statistics States Matrix

| State | UX |
|---|---|
| Loading | chart skeleton |
| Not enough data | explanatory empty |
| Success | chart + summary |
| Error | retry |
| Filter change | preserve context |

---

# 154. Report Lifecycle UX

Canonical:

```text
Created
  ↓
Draft / Analysis
  ↓
Pending Review
  ↓
Confirmed
  ↓
Eligible for dashboard/statistics
  ↓
Generated document(s)
```

Do not use UI status labels that imply a draft is already official.

---

# 155. Acquisition Method Labels

Recommended user-facing labels:

```text
Paste Text
Import Image
Take Photo
Enter Manually
```

External:

```text
Shared Image
```

Do not expose internal enum names such as:

```text
gallery_ocr
```

---

# 156. Source Type Display

In Report Detail, use friendly labels:

```text
Pasted text
Gallery image
Shared image
Manual entry
```

---

# 157. KPI Warning Hierarchy

Recommended:

### Neutral

No issue.

### Review

Low confidence / uncertain mapping.

### Warning

Outside expected range / duplicate / unusual value.

### Blocking Error

Only when data cannot be confirmed according to business rules.

---

# 158. Primary CTA Placement

On long forms/review screens, primary CTA may be bottom-sticky if:

- it does not obscure content
- keyboard behavior is correct
- safe insets are respected

Confirmation is a strong candidate.

---

# 159. Secondary CTA Placement

`Save Draft` should remain easy to access but visually secondary to `Confirm Report`.

---

# 160. Create Flow Exit

If no input has been entered:

Back exits normally.

If meaningful data exists:

Prompt/save draft behavior applies.

---

# 161. OCR Image Privacy

Do not persist original images indefinitely unless needed by the business model.

If images are only used for OCR, the UX should not imply permanent archival unless actually implemented.

---

# 162. Raw Text Privacy

Raw OCR/paste text is retained for traceability per business rules.

If shown in UI, keep it in secondary detail rather than constantly exposed.

---

# 163. Sensitive Notification Content

System notification text should be concise and avoid unnecessary industrial details on lock screen.

Good:

```text
Daily report generated
Tap to view.
```

---

# 164. Report Share Privacy

Before sharing, user should understand which file is being shared.

Use filename + format clearly.

---

# 165. File Selection UX

If both Excel and PDF exist:

Offer:

```text
Share Excel
Share PDF
```

or a simple format selector.

Do not silently choose.

---

# 166. Multi-Format Generation

If user chooses:

```text
Generate Report
```

and both formats are supported, use a bottom sheet:

```text
Excel
PDF
Both
```

Only if this simplifies UI.

Otherwise keep separate buttons.

---

# 167. History Sorting

Default:

```text
Newest first
```

Provide alternate sorting only if a real need appears.

---

# 168. Notifications Sorting

Default:

```text
Newest first
```

Unread may be indicated, not grouped aggressively unless useful.

---

# 169. Draft Sorting

Default:

```text
Most recently edited first
```

---

# 170. Schedule Sorting

Recommended:

```text
Daily
Weekly
Monthly
```

or by next run if schedules become more flexible.

---

# 171. KPI Selector Search

Search should support aliases/display names if backend exposes them.

UI should display canonical friendly name.

---

# 172. KPI Categories

If categories exist:

Use subtle section headers or filters.

Do not create deeply nested category navigation.

---

# 173. Manual Entry Add Speed

Allow repeated:

```text
Add KPI
→ select
→ enter
→ Add another
```

without returning to Dashboard.

---

# 174. Confirmation Bulk Efficiency

If many KPI rows exist:

- keep rows compact
- avoid requiring separate edit screen per KPI
- allow inline editing
- keep warning navigation efficient

Potential:

```text
Review 2 warnings
```

action scrolls to next warning.

---

# 175. Warning Navigation

If several warnings:

Provide a summary and optional:

```text
Next issue
```

Do not force user to hunt visually through a long list.

---

# 176. Confirmation Completion Indicator

Optional:

```text
10 of 12 reviewed
```

only if "reviewed" is a real tracked state.

Do not create fake progress merely for appearance.

---

# 177. Confirmation Source Comparison

For difficult cases, user may tap:

```text
View source
```

and see raw line beside current KPI.

Do not keep side-by-side desktop layout on small phones.

Use bottom sheet or expandable detail.

---

# 178. Search Filter Chips

After filters applied:

Show compact chips:

```text
This week
Confirmed
KPI: Choline
```

Each removable.

Avoid a large permanent filter summary.

---

# 179. Report Detail Editing

Confirmed reports should be read-only by default.

Do not let users casually alter confirmed historical values.

If correction workflow is ever required, it must be explicitly designed with audit semantics.

---

# 180. Audit View

If audit log UI is included:

Keep it secondary.

Use chronological activity list.

Examples:

```text
14:02 Report analyzed
14:05 KPI Vrac edited
14:07 Report confirmed
```

Do not expose backend event names.

---

# 181. Refresh Behavior

Manual refresh should not erase current list position or active filters.

---

# 182. Pagination UX

Prefer seamless/lazy loading for history.

If backend page loading fails:

Show retry at end.

Do not reload first page unnecessarily.

---

# 183. Large Data Lists

Use LazyColumn.

Keep row rendering lightweight.

Do not render every historical report at once.

---

# 184. Accessibility of Charts

Provide textual summary near chart.

Do not make chart the only way to understand trend.

---

# 185. Accessibility of Status

Use icon + text.

Do not rely only on:

```text
green = confirmed
red = failed
```

---

# 186. Accessibility of Icon Buttons

Every icon button must have semantic label.

Examples:

```text
Open notifications
Share report
Delete draft
Show password
```

---

# 187. System Back Gesture

Support Android predictive/system back behavior where compatible with chosen stack.

Do not override back globally without reason.

---

# 188. Deep Link Recovery

If deep-linked entity no longer exists:

Show:

```text
This report is no longer available.
```

Action:

```text
Back to Reports
```

---

# 189. Authentication Deep Link Recovery

If notification/share deep link requires login:

```text
target saved temporarily
→ Login
→ navigate to target
```

where practical.

---

# 190. Share Intent Unsupported Content

If FactoryFlow receives unsupported type:

```text
FactoryFlow can currently import report images.
```

Action:

```text
Open Dashboard
```

---

# 191. Permission Denial Philosophy

Permission denial should never feel punitive.

Examples:

Shared URI denied:

```text
You can still import an image or paste text.
```

Notifications denied:

App remains fully functional except push alerts.

---

# 192. Notification Permission

On Android versions requiring runtime notification permission:

Ask after context exists, not immediately on first launch unless the UX specification later justifies it.

Better:

```text
Enable notifications to receive report reminders and generation updates.
```

---

# 193. Shared URI Permission Timing

Use only the permission granted by the system picker or Share Intent.

---

# 194. Storage Permission

Prefer modern picker/FileProvider APIs that avoid broad storage permission.

Do not ask for legacy storage permission without need.

---

# 195. Report Download UX

If a report is opened/shared from app-private storage, no traditional "download" may be necessary.

If explicit export is implemented:

Clearly show destination/result.

Do not promise a Downloads copy unless it is actually created.

---

# 196. Report Generation From Period

If user generates weekly/monthly report manually:

Provide period selector before generation.

Use:

```text
Week
Month
Custom period if supported
```

Do not make user type dates if a picker can prevent mistakes.

---

# 197. Date Picker

Use Material date picker behavior.

For range selection:

Show selected start/end clearly.

---

# 198. Time Picker

Use Material time picker consistent with locale/device.

---

# 199. Confirmation Before Automatic Email

If schedule config includes recipients:

Show them clearly.

Do not hide delivery destination behind advanced settings.

---

# 200. Email Recipient Management

If backend recipients are configurable:

Use validated chips/list.

Do not implement a complex contact manager.

---

# 201. Schedule Failure UX

If last automatic report failed:

Schedule card should show:

```text
Last run failed
```

Tap:

```text
→ detail with reason/retry if supported
```

---

# 202. Generated Report Delivery Status

Possible:

```text
Generated
Email delivered
Email failed
```

Do not combine them into one status.

---

# 203. Dashboard Report Status Priority

If today has:

- confirmed report
- email failure

Dashboard primary report status remains:

```text
Confirmed
```

Delivery failure appears as secondary warning.

---

# 204. Threshold Warnings

If a confirmed KPI exceeds configured plausible/attention range:

Dashboard may show warning.

Do not call it an anomaly prediction unless predictive logic exists.

---

# 205. Statistics Data Integrity

Only confirmed values.

No drafts.

No raw extraction candidates.

---

# 206. Dashboard Data Integrity

Only authoritative data for official KPI values.

Draft counts/status may appear separately.

---

# 207. Report Generation Data Integrity

Always use final confirmed values.

---

# 208. Manual vs Automatic Reports

Generated document metadata should indicate:

```text
Generated manually
```

or:

```text
Generated automatically
```

where useful.

---

# 209. User Identity Display

Because all current users are maintenance engineers:

Show name where relevant.

Avoid repeatedly showing role in every screen.

---

# 210. Multi-User Conflicts

If two engineers access same draft/report:

Initial version may not support collaborative live editing.

If backend detects state conflict:

Show:

```text
This report was updated elsewhere.
Refresh before continuing.
```

Do not silently overwrite confirmed state.

---

# 211. Optimistic UI

Safe for:

- read/unread notification state
- simple local view toggles

Not safe for:

- report confirmation
- generated report success
- scheduled email success

Do not show authoritative success before backend confirms it.

---

# 212. Form Save Reliability

On save/confirm:

Disable duplicate action.

Preserve values until response.

---

# 213. Retry Design

Retry should be near the failed action.

Do not force user back to previous screen unnecessarily.

---

# 214. Retry After OCR Failure

Possible:

```text
Retry OCR
Choose Another Image
Enter Manually
```

---

# 215. Retry After Analyze Failure

```text
Try Again
Edit Text
Enter Manually
```

---

# 216. Retry After Generation Failure

```text
Try Again
```

Confirmed source data remains unchanged.

---

# 217. Retry After Email Failure

If backend automatic delivery:

```text
Retry Email
```

if supported.

Do not regenerate file unnecessarily.

---

# 218. Notification Retry

FCM itself is not user-retriable.

Refresh in-app notification state via REST.

---

# 219. Visual Hierarchy on Long Screens

Use:

- section headings
- cards
- spacing
- sticky actions

Avoid excessive nested cards.

---

# 220. Bottom Navigation Visibility

Hide bottom navigation during focused workflows where it could cause accidental exit:

```text
Gallery OCR
OCR processing
Confirmation
Schedule edit
Login
```

Show it on primary destinations.

---

# 221. Top App Bar Back

Focused/detail screens:

```text
Back
Title
Context action if needed
```

---

# 222. Dashboard Scroll

Dashboard may scroll vertically.

Do not create nested vertical scrolling sections.

---

# 223. History Search Position

Search stays near top.

Optional sticky filter/search bar if implementation remains smooth.

---

# 224. Confirmation Scroll

Sticky bottom action must not cover final rows.

Use content padding.

---

# 225. Long Raw Text

Raw source should be scrollable in a contained area or separate sheet.

Do not expand indefinitely inside main validation list.

---

# 226. Huge KPI Count

If many entries:

Use lazy list.

Warnings remain discoverable.

---

# 227. Loading Skeleton Count

Show a realistic small number of skeleton rows.

Do not render 20.

---

# 228. Pull-to-Refresh Loading

Keep content visible while refreshing.

Do not replace with full-screen loader.

---

# 229. Refresh Timestamp

Only show "Last updated" where it adds operational value.

Dashboard KPI status is a candidate.

---

# 230. Theme Persistence

If user chooses theme:

Persist locally.

Do not require backend.

---

# 231. Settings Sync

Only sync settings to backend if multi-device consistency is a real requirement.

---

# 232. Language

The initial and canonical Android UI language is professional French.

Do not mix French and English labels in the same UI.

All user-facing strings live in Android string resources. Technical identifiers remain English.

---

# 233. Industrial Terminology

Use real maintenance/reporting vocabulary consistently.

Avoid replacing domain terms with vague generic words.

---

# 234. "Smart" Language

Avoid marketing claims:

```text
AI-powered
Intelligent prediction
Smart anomaly engine
```

unless implemented.

"Intelligent" in project title refers to the overall assisted workflow, not hidden generative AI.

---

# 235. UX of Determinism

The app should visibly communicate confidence and review.

This helps users trust the system because it is honest about uncertainty.

---

# 236. UX of Traceability

Users should be able to answer:

```text
Where did this value come from?
Was it edited?
Who confirmed it?
When?
```

without exposing database internals.

---

# 237. UX of Automation

Automation should feel like:

```text
less work
```

not:

```text
loss of control
```

Always preserve manual correction path.

---

# 238. UX of Scheduling

Scheduling should feel predictable.

Always show:

```text
what
when
format
delivery
next run
```

---

# 239. UX of Notifications

Notifications should help action.

Avoid notification spam.

Do not send push for every minor dashboard change.

---

# 240. UX of Reports

Reports should be easy to:

```text
find
open
generate
share
understand
```

---

# 241. UX of Drafts

Drafts communicate safety.

The user should feel comfortable leaving a report unfinished.

---

# 242. UX of Warnings

Warnings should increase trust.

The user should understand:

```text
FactoryFlow noticed something unusual and is asking me to verify it.
```

---

# 243. UX of Empty Values

If source includes KPI label with no value:

Show:

```text
No value reported
```

not:

```text
0
```

---

# 244. UX of Partial Reports

Do not make a partial report look broken.

Show:

```text
8 KPI values found
4 expected values were not reported
```

if expected set is known.

---

# 245. UX of Unknown Lines

Unknown lines are visible and actionable.

This is important for future parser improvement.

---

# 246. UX of Duplicate KPI

If duplicate:

Show warning:

```text
This KPI appears more than once.
Choose the value to confirm.
```

Exact behavior depends on parser/business rules.

---

# 247. UX of Unit Mismatch

If captured unit differs:

```text
Unit differs from expected value.
Review before confirming.
```

Do not auto-convert unless business rules exist.

---

# 248. UX of Plausibility

If value outside range:

```text
Outside expected range
```

with expected range shown subtly.

---

# 249. UX of Confidence

Confidence should guide prioritization.

High-confidence entries can remain visually quiet.

Low-confidence entries receive emphasis.

This lets engineers review quickly.

---

# 250. UX of Manual Override

Manual correction should be first-class.

Never make the user fight the parser.

---

# 251. UX of Source Visibility

Source context must be one tap away during confirmation.

This allows fast verification without leaving the workflow.

---

# 252. UX of Generated Report History

Generated file history should make scheduled automation visible.

Examples:

```text
Daily · 11 Aug 2026
PDF
Generated automatically at 18:00
Email delivered
```

---

# 253. UX of Schedule Next Run

Always calculate/display next run from backend authoritative schedule when possible.

Do not independently compute contradictory times on Android.

---

# 254. UX of Failed Schedule

Failure reason should be human-readable.

Examples:

```text
Report generated, but email delivery failed.
```

rather than:

```text
SMTPAuthenticationException
```

---

# 255. UX of Notification Permission Denied

No persistent red banner.

A subtle settings reminder may appear in Notifications screen if push alerts are disabled.

---

# 256. UX of Shared URI Permission Loss

Show:

```text
The shared image is no longer accessible.
Share it again or choose it from the gallery.

[Open Settings]
```

and keep other acquisition methods available.

---

# 257. UX of Invalid Session

Return to login gracefully.

If user has unsaved work, preserve locally when feasible before logout redirect.

---

# 258. UX of Server Maintenance

If backend unavailable:

Use generic connectivity/service message.

Do not assume user's internet is always the cause.

Example:

```text
FactoryFlow service is temporarily unavailable.
Try again shortly.
```

if backend health semantics support it.

---

# 259. UX of Slow Network

Use progress states.

Do not show timeout instantly.

Retry messaging only after actual failure.

---

# 260. UX of Large PDF/Excel Generation

If generation becomes asynchronous:

Show report generation status:

```text
Queued
Generating
Ready
Failed
```

This becomes especially relevant if RabbitMQ is later implemented.

---

# 261. UX of Async Generation

If user leaves screen while generation continues:

Dashboard/notification/generated history should surface completion.

Do not force user to wait on one screen.

---

# 262. UX of RabbitMQ

RabbitMQ should remain invisible to users.

UI uses business language:

```text
Generating
Ready
Failed
```

never:

```text
Queued in RabbitMQ
```

---

# 263. UX of WebSocket

WebSocket should remain invisible.

Users simply see timely updates.

---

# 264. UX of FCM

FCM should remain invisible.

Users see notifications.

---

# 265. UX of Room

Room/cache should remain invisible.

If cached data is stale and that matters:

Show subtle freshness indicator.

---

# 266. UX of Retry Idempotency

If a user retries after uncertain completion:

The app should check backend state when feasible.

Do not show duplicate reports.

---

# 267. UX of Search Debounce

If search calls backend:

Debounce typing.

Do not request on every keystroke.

---

# 268. UX of Filter Application

Use explicit `Apply Filters` if several filter fields exist.

For one-tap chips, update immediately.

---

# 269. UX of KPI Selector

Support fast search.

Keyboard opens automatically when selector search is tapped.

---

# 270. UX of Validation Keyboard

Numeric field edits should not reset list scroll when keyboard opens.

---

# 271. UX of Field Formatting

Do not aggressively reformat number while user is typing if it moves cursor unexpectedly.

Normalize on blur/submit where appropriate.

---

# 272. UX of Decimal Separator

Accept user locale-friendly separator where possible.

Display consistently afterward.

---

# 273. UX of Unit

Units should be visible but not editable unless explicitly supported.

---

# 274. UX of Manual Add

After selecting KPI, focus value field automatically.

---

# 275. UX of Multiple Manual KPIs

After completing one field, allow quick add next.

Avoid repeated full-screen navigation.

---

# 276. UX of Draft Resume

Resume should reopen exactly where practical:

- source
- KPI rows
- edits
- warnings
- scroll position optional

At minimum, preserve content and state.

---

# 277. UX of Draft Age

Show:

```text
Edited 2 hours ago
```

or date/time.

---

# 278. UX of Old Drafts

Do not auto-delete without policy.

If cleanup is introduced, document it.

---

# 279. UX of Confirmed Report Mutability

Confirmed reports read-only.

Any future correction requires explicit correction workflow.

---

# 280. UX of Generated Document Deletion

If file deletion is allowed:

Clarify whether only file is deleted or underlying report.

Never let user think deleting PDF deletes business data.

---

# 281. UX of Report Archive

If archival exists later:

Use neutral status.

Do not remove from history silently.

---

# 282. UX of KPI Deactivation

If a KPI definition is deactivated:

Historical reports still display it.

KPI selector excludes it for new input unless explicitly viewing all.

---

# 283. UX of Alias Changes

No visible effect on historical confirmed reports.

Only future parsing behavior changes.

---

# 284. UX of Dashboard Missing Report

If scheduled/expected report has not been confirmed:

Use actionable warning:

```text
Today's report is still missing.
[Create Report]
```

---

# 285. UX of Dashboard Draft

If draft exists:

Prefer:

```text
Resume Draft
```

over creating a second duplicate report for same period unless multiple reports are valid.

---

# 286. UX of Multiple Reports Per Day

Multiple maintenance reports may share one `effective_date`. Dashboard status must
summarize clearly and must not assume one report per day.

---

# 287. UX of Report Type

Daily/weekly/monthly generated documents are not the same as maintenance input reports.

UI labels must make this distinction clear.

---

# 288. UX of "Report"

Avoid ambiguous language where two concepts coexist.

Use:

```text
Rapport de maintenance
Rapport généré
Rapport Excel
Rapport PDF
```

where needed.

---

# 289. UX of Statistics Period

Default to a useful period such as last 7 days.

Do not default to an empty range.

---

# 290. UX of Trend Direction

If arrows are used:

Combine with value/text.

Example:

```text
↑ 4.2% vs previous period
```

Do not use arrow alone.

---

# 291. UX of No Trend

If insufficient comparison:

Show:

```text
Not enough data
```

rather than `0%`.

---

# 292. UX of Upcoming Schedule

If none:

Do not show an empty card on Dashboard unless schedule setup is important.

Could show:

```text
No automatic schedule configured
```

with setup action only if user can configure it.

---

# 293. UX of Automatic Email Recipients

Display masked/clear recipient list responsibly.

Do not hide important delivery configuration.

---

# 294. UX of Delivery Failures

Delivery failure should be actionable but secondary to report generation.

---

# 295. UX of Push Notification Count

If badge exists, count unread actionable notifications.

Do not keep large counts forever.

---

# 296. UX of Mark All Read

Optional.

Only add if notification volume justifies it.

---

# 297. UX of Notification Categories

Do not expose category filters initially unless volume becomes high.

---

# 298. UX of Profile Avatar

Use initials if no profile image functionality.

Do not build image-upload profile feature without need.

---

# 299. UX of App Version

Show in About/Settings.

Useful for debugging/support.

---

# 300. UX of Environment

Development builds may show subtle environment label.

Production-like presentation should not expose debug endpoints/config.

---

# 301. UI Performance

Keep scrolling smooth.

Avoid:

- nested LazyColumns
- expensive recompositions
- loading giant images unscaled
- unnecessary animated state

---

# 302. Image Preview Performance

Downsample large images for preview.

OCR may use appropriate source resolution.

Do not block main thread.

---

# 303. Accessibility of OCR

Provide text alternative after OCR.

User can review extracted text even if image itself is difficult to inspect.

---

# 304. Accessibility of Confirmation

Warnings should be announced semantically.

Editable values need clear labels:

```text
Vrac value, tonnes
```

---

# 305. Accessibility of Dashboard

Each KPI card should expose a coherent semantic description.

Avoid screen readers reading disconnected icon/value/unit fragments confusingly.

---

# 306. Accessibility of Navigation

Bottom nav items:

```text
Tableau de bord
Rapports
Créer
Notifications
```

with selected state announced.

---

# 307. Accessibility of Charts

Provide summary text:

```text
Choline increased 4% over the last 7 days.
```

if such calculation is supported.

---

# 308. Accessibility of Status Chips

Chip text must be explicit.

---

# 309. UX Documentation Sync

When screen behavior changes:

Update `UI_UX.md` in the same task.

When visual tokens change:

Update `DESIGN.md`.

Do not allow implementation to drift.

---

# 310. UI Implementation Order

Recommended:

```text
1. Theme / design tokens
2. Navigation shell
3. Login
4. Dashboard skeleton
5. Create Report selector
6. Paste flow
7. Confirmation
8. Drafts
9. Manual entry
10. Reports/history
11. Report detail
12. Generated files
13. PDF/Excel actions
14. Scheduling
15. Gallery OCR
16. Share Intent
17. Backend OCR
18. Notifications
19. Statistics
20. Final polish
```

This aligns UX work with the broader project roadmap.

---

# 311. Core Flow Acceptance

Before broader polish, this must work end-to-end:

```text
Login
→ Dashboard
→ Create Report
→ Paste Text
→ Analyze
→ Confirmation
→ Edit
→ Save Draft
→ Resume
→ Confirm
→ Report Detail
→ Generate Excel
```

---

# 312. Premium Flow Acceptance

Before presentation, this must feel polished:

```text
WhatsApp screenshot
→ Share to FactoryFlow
→ OCR
→ Confirmation
→ Edit low-confidence value
→ Confirm
→ Dashboard realtime update
→ Generate PDF
→ Share
```

---

# 313. UI Screenshot Targets

Capture final polished screenshots of:

```text
Login
Dashboard
Create Report
Paste
Confirmation
Manual Entry
Reports
Report Detail
Generated Report
Schedules
Notifications
Statistics
```

Share Intent/OCR flow may be captured as GIF/video.

---

# 314. Empty State Screenshot Targets

At least one polished empty state should appear in portfolio material if it demonstrates design quality.

Best candidates:

```text
Reports empty
Notifications empty
Dashboard first run
```

---

# 315. Error State Review

Before release, intentionally test:

```text
wrong password
no backend
analyze failure
OCR failure
confirmation failure
PDF failure
email failure
notification permission denied
shared image permission denied
```

Review UX for each.

---

# 316. Warning State Review

Test:

```text
low-confidence match
out-of-range value
missing KPI
unknown line
duplicate KPI
```

---

# 317. Dark Theme Review

If dark theme is included:

Review every major screen manually.

Do not assume Material theme mapping makes it correct automatically.

---

# 318. Light Theme Review

Light theme remains primary visual reference unless project direction changes.

---

# 319. Premium QA Questions

For every screen ask:

```text
Is the primary action obvious?

Can the user understand the screen in 3 seconds?

Is any element visually louder than its importance?

Is there unnecessary text?

Is there unnecessary decoration?

Can a warning be understood without color?

Does this feel like one product with the rest of FactoryFlow?

Would this still feel credible in a real industrial environment?
```

---

# 320. FactoryFlow UX Identity

The experience should feel like:

```text
an industrial maintenance tool
designed with the care of a premium productivity application
```

not:

```text
a student CRUD app with a modern theme
```

---

# 321. Final UX Principle

FactoryFlow should make the difficult parts of industrial reporting feel controlled.

The user should feel:

```text
I can see what the system understood.
I can correct it.
I know what becomes official.
I can find it later.
I can generate the report.
I remain in control.
```

That feeling is more important than any individual animation or component.

---

## 322. Delivery Stabilization Behavior

- Statistics is a top-level bottom-navigation destination and therefore has no nested-screen back affordance.
- A successful report confirmation clears the completed acquisition/review workflow from the back stack.
- Both the confirmation back action and system Back leave the terminal screen without reopening completed review work.
- Create Report keeps its title at the top and vertically balances acquisition actions inside the remaining responsive viewport; compact screens and larger fonts remain scrollable.
- Statistics renders a trend only when at least two valid confirmed observations exist. Otherwise it explicitly displays “Données insuffisantes”.
- Notification rows open their related confirmed report or generated document when the backend supplies that relationship.

### Review completion hierarchy

- `ATTENTION_ACKNOWLEDGE` and `ATTENTION_DUPLICATE` always expose an explicit validation action.
- Duplicate validation acknowledges one observation; it never merges or overwrites another occurrence.
- Untouched missing values remain legitimate. Entering a replacement creates `MISSING_CORRECTED`, with “Annuler la saisie” and “Valider”.
- Weak suggestions keep “Ajouter un nouvel indicateur” as the primary action and display their numeric confidence only as optional help.
- Strong suggestions prioritize association while retaining manual selection and new-KPI creation.
- The Non tab separates KPI-like unresolved content from safe noise. “Ignorer tout” stays pinned near the top of the Non content and acts on every unresolved line carrying the backend `safeToIgnore` flag, regardless of its visual subsection. Unflagged KPI-like content is never included.
- Save uses visible progress and Snackbar feedback. Dirty Back navigation offers Save Draft, leave without saving, and cancel.
- Persistent workflow actions use one navigation-bar-safe bottom container. Input and Review scaffolds resize for the IME so the active field, scrolling content, and bottom action remain usable without stacked inset gaps.
- Duplicate-observation cards reserve equal horizontal space for the secondary removal action and the primary validation action; labels may wrap only at word boundaries and never collapse letter-by-letter.

---

## 323. Maintenance Intelligence Experience

Maintenance Intelligence is reachable from Statistics and opens as a focused nested
analytical workspace. The global overview answers which KPIs have usable analyses,
forecasts, anomalies, insufficient history, or contextual alerts. Selecting a KPI opens a
horizontally scrollable five-part workspace: Overview, Anomalies, Forecast, Trend, and
Model/Quality. Each part owns one analytical question and the selected KPI identity remains
visible above the tabs.

Charts use shape, line style, color, and text together. Confirmed observations are circles
on solid lines; anomalies are diamonds; forecasts are triangles on dashed lines; contextual
deviations use a distinct triangle; forecast intervals are translucent bands. Drag/tap
inspection exposes the selected date and value. A nearby textual summary ensures that
analytical meaning is not available only through graphics.

The trend reference reproduces the backend least-squares fit rather than fitting a second
mobile model. Successive-change bars disclose their signed square-root display scale so
small movements remain visible beside an outlier. Analytical status pills wrap instead of
requiring an undisclosed horizontal gesture, and alert titles may occupy two lines when
needed.

Analytical abstention is a designed state, not an error. Insufficient history, duplicate
effective dates, missing-cadence ambiguity, irregular spacing, cadence mismatch, and model
failure receive concise explanations without manufacturing a result. Technical/network
failures remain visually distinct and provide retry. A missing persisted analysis offers an
explicit first-analysis action.

Contextual alert detail explains which independent signals agreed, shows actual and
expected evidence without calling it failure probability, links to the KPI workspace, and
links to the confirmed source report. `HIGH` means corroborating analytical signals, not
equipment failure severity.

---

# End of UI_UX.md
