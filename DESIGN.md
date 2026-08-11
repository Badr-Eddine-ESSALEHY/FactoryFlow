# DESIGN.md

> **FactoryFlow — Premium Visual Design System**
>
> Version: 1.0
>
> Status: Active
>
> Last updated: 2026-08-11
>
> This document defines the **visual design language of FactoryFlow**.
>
> `AGENTS.md` defines project constitution.
>
> `TASKS.md` defines what to build.
>
> `SKILLS.md` defines how to engineer it.
>
> `DESIGN.md` defines how FactoryFlow should **look and feel**.
>
> `UI_UX.md` will define screen-by-screen behavior and complete user flows.
>
> The design goal is not merely "clean Android UI".
>
> The goal is a **premium industrial product**:
>
> calm, precise, restrained, modern, trustworthy, fast, and visually coherent.
>
> FactoryFlow should feel like software that could plausibly be deployed inside a serious industrial organization,
> while still having the polish expected from a high-end consumer product.

---

# 1. Premium Design Philosophy

FactoryFlow should feel premium without becoming decorative.

Premium means:

- excellent hierarchy
- disciplined spacing
- refined typography
- coherent motion
- high-quality states
- thoughtful micro-interactions
- low visual noise
- strong consistency
- high perceived reliability
- purposeful details
- excellent touch behavior
- excellent information density

Premium does **not** mean:

- gradients everywhere
- glassmorphism everywhere
- excessive shadows
- giant hero sections
- large decorative illustrations
- meaningless animations
- luxury colors added for appearance
- copying Apple layouts directly
- making an Android app behave like a website

The premium feeling must come from restraint.

---

# 2. Design Inspiration

FactoryFlow may draw inspiration from several strong design systems.

These are **quality references**, not templates to copy.

---

## 2.1 Apple

Use Apple as inspiration for:

- whitespace discipline
- visual hierarchy
- restrained use of color
- calm surfaces
- typography scale
- subtle transitions
- clarity
- polished empty/loading states
- emphasis through spacing rather than decoration
- confidence in simplicity

Do not copy:

- SF Pro as a hard requirement
- Apple website marketing layouts
- Apple-specific navigation patterns that conflict with Android
- proprietary visual identity

FactoryFlow is an Android product, not an Apple clone.

---

## 2.2 Material 3

Material 3 is the behavioral foundation for Android.

Use it for:

- correct platform interactions
- accessibility
- navigation conventions
- focus/press states
- dialogs
- bottom sheets
- system integration
- touch targets
- component semantics
- system bars
- back handling

FactoryFlow should visually customize Material 3 heavily enough to have its own identity.

---

## 2.3 Linear

Use Linear as inspiration for:

- information-dense dashboards
- precise alignment
- subtle borders
- compact but readable controls
- quiet motion
- high-quality status visualization
- efficient productivity workflows

---

## 2.4 Stripe

Use Stripe as inspiration for:

- forms
- settings
- data presentation
- trust-building UI
- clear field hierarchy
- professional error states
- polished developer/product surfaces

---

## 2.5 Notion

Use Notion as inspiration for:

- structured information
- clean lists
- restrained separators
- table readability
- content-first layout
- flexible data views

---

# 3. Visual Personality

FactoryFlow should communicate:

```text
Professional
Calm
Precise
Reliable
Modern
Premium
Industrial
Intelligent
```

It should never feel:

```text
Playful
Cartoonish
Game-like
Overly colorful
Futuristic for no reason
Crypto-style
Cyberpunk
Marketing-heavy
Generic enterprise grey
```

---

# 4. Core Visual Principle

The visual system follows:

```text
Hierarchy > Decoration
Clarity > Density
Precision > Personality excess
Whitespace > Borders
Motion > Static abruptness
Consistency > Novelty
```

---

# 5. Surface Philosophy

FactoryFlow uses layered neutral surfaces.

Preferred hierarchy:

```text
App Background
    ↓
Primary Surface
    ↓
Elevated Surface
    ↓
Interactive Surface
```

Surfaces should be distinguished using:

- subtle tonal contrast
- spacing
- very restrained borders
- small elevation differences
- radius

Avoid heavy shadow stacks.

---

# 6. Light Theme Philosophy

Light mode should feel bright, clean, and calm.

Recommended direction:

```text
Background:
near-white / soft neutral

Primary surfaces:
white

Secondary surfaces:
very light cool grey

Text:
near-black

Accent:
single confident blue family
```

Avoid pure white everywhere if it causes visual harshness.

---

# 7. Dark Theme Philosophy

Dark mode should feel premium, not merely inverted.

Use:

- deep charcoal rather than pure black
- slightly lighter cards
- reduced contrast where appropriate
- subtle borders
- restrained accent use

Avoid:

```text
#000000 background everywhere
bright neon blue
glowing cards
```

---

# 8. Accent Color Philosophy

FactoryFlow should use a **single primary accent family**.

Recommended direction:

```text
FactoryFlow Blue
```

The accent should communicate:

- trust
- precision
- confidence
- modern software quality

A blue in the spirit of:

```text
#0066CC
```

is an acceptable reference direction, but the final Material 3 color tokens should be tuned for:

- contrast
- light mode
- dark mode
- Android accessibility

Do not create a rainbow design system.

---

# 9. Semantic Colors

Semantic colors are functional.

Use distinct roles for:

```text
Success
Warning
Error
Information
Neutral
```

Do not use them decoratively.

Examples:

```text
Success → confirmed report
Warning → suspicious KPI
Error → failed operation
Info → processing / guidance
Neutral → draft / inactive
```

---

# 10. Status Color Rule

Never communicate state by color alone.

Combine color with:

- text
- icon
- label
- shape
- position

Example:

```text
Warning icon + "Needs review"
```

is better than an amber border alone.

---

# 11. Color Token System

Do not hardcode colors in individual Composables.

Use semantic tokens.

Conceptual tokens:

```text
color.background
color.surface
color.surfaceRaised
color.surfaceMuted

color.textPrimary
color.textSecondary
color.textTertiary
color.textInverse

color.borderSubtle
color.borderStrong

color.accent
color.accentHover
color.accentPressed

color.success
color.warning
color.error
color.info
```

Material 3 theme should map these roles consistently.

---

# 12. Typography Philosophy

Typography should carry most of the visual hierarchy.

Use:

- strong titles
- concise section labels
- readable body copy
- compact metadata
- tabular alignment where helpful

Avoid excessive font-weight variation.

---

# 13. Typography Family

Preferred:

```text
Android system / Material typography
```

Inter may be used if deliberately configured and licensed appropriately.

Do not require SF Pro.

The product should not depend on Apple proprietary fonts.

---

# 14. Type Scale

Suggested hierarchy:

```text
Display / Hero:
rare

Screen Title:
28–32sp

Section Title:
20–24sp

Card Title:
16–18sp

Body:
14–16sp

Metadata:
12–14sp

Micro label:
11–12sp
```

Actual values may be refined in implementation.

Do not create too many type levels.

---

# 15. Font Weight

Preferred use:

```text
Regular
Medium
Semibold
```

Use bold sparingly.

If everything is bold, nothing is emphasized.

---

# 16. Numeric Typography

KPI values are important.

Use:

- clear larger size
- stable baseline
- good contrast
- tabular figures when available

Example:

```text
295,456
```

should visually dominate its metadata.

Unit should be visually secondary:

```text
295,456  t
```

---

# 17. Text Hierarchy

A KPI card might follow:

```text
Label
Value
Unit / Timestamp / Status
```

Do not give all three equal emphasis.

---

# 18. Copy Density

Industrial software can be information-dense.

Density should come from:

- good alignment
- compact metadata
- grouped content

not from:

- tiny fonts
- insufficient spacing
- compressed tap targets

---

# 19. Spacing System

Use an 8-point spacing philosophy.

Core spacing values:

```text
4
8
12
16
24
32
40
48
64
```

Primary rhythm:

```text
8
16
24
32
```

Do not invent arbitrary values everywhere.

---

# 20. Spacing Hierarchy

Use smaller spacing inside components.

Use larger spacing between conceptual groups.

Example:

```text
Label → value
8px

Card content groups
16px

Between sections
24–32px
```

---

# 21. Screen Padding

Recommended mobile horizontal content padding:

```text
16–20dp
```

Large top-level sections may use:

```text
24dp
```

where visual balance allows.

Do not waste narrow mobile width with desktop-like 40px gutters.

---

# 22. Radius System

FactoryFlow should use soft but restrained rounding.

Suggested:

```text
Small:
8dp

Medium:
12dp

Large:
16dp

XL:
20–24dp
```

Avoid excessive pill shapes.

Use pills only for:

- compact status chips
- segmented controls
- small actions where appropriate

---

# 23. Card Radius

Default card:

```text
16dp
```

High-importance feature card may use:

```text
20dp
```

Do not vary card radius randomly.

---

# 24. Border Philosophy

Borders should be subtle.

Use them when they clarify grouping.

Prefer:

```text
1dp low-contrast border
```

over large shadows.

Avoid visible outlines around every card.

---

# 25. Elevation Philosophy

Use elevation sparingly.

Flat surfaces + tonal contrast are preferred.

Suggested hierarchy:

```text
Base:
0

Card:
1–2dp

Modal / sheet:
3–6dp
```

Do not create floating shadows under everything.

---

# 26. Shadow Quality

Shadows should be:

- soft
- wide
- low opacity
- context-aware

Avoid dark, sharp shadows.

---

# 27. Iconography

Use a consistent icon family.

Preferred:

```text
Material Symbols / high-quality Compose-compatible icon set
```

If Lucide-style icons are introduced on Android, use them consistently and ensure licensing/implementation suitability.

Do not mix:

- filled icons
- outline icons
- custom SVGs
- emojis

without a deliberate system.

---

# 28. Icon Size

Typical:

```text
18dp
20dp
24dp
```

Larger:

```text
28–32dp
```

only for high-emphasis actions.

---

# 29. Icon Meaning

Icons support meaning.

Do not use icons as decoration.

Every icon should correspond to:

- action
- status
- category
- navigation
- object type

---

# 30. Navigation Philosophy

Navigation should feel obvious.

The user should always know:

```text
Where am I?
What can I do here?
How do I go back?
```

Navigation must be Android-native in behavior.

Detailed navigation belongs in `UI_UX.md`.

---

# 31. Bottom Navigation

FactoryFlow uses four primary bottom destinations:

```text
Tableau de bord
Rapports
Créer
Notifications
```

Profile/settings are accessed from the top-level profile action, not a fifth item.

Do not overload bottom navigation.

---

# 32. Floating Action Buttons

Use FAB only for one obvious primary action.

Do not place multiple competing floating buttons.

A possible use is:

```text
New Report
```

But final decision belongs in `UI_UX.md`.

---

# 33. Top App Bar

Top app bars should be visually restrained.

Use:

- screen title
- contextual actions
- optional back

Avoid:

- giant banners
- unnecessary logos on every screen
- repeated company branding

---

# 34. Primary Button

Primary action should be visually unmistakable.

Use:

- accent fill
- strong contrast
- medium/large radius
- clear label

Examples:

```text
Analyze
Confirm Report
Generate PDF
Save Schedule
```

Only one primary action should dominate a screen at a time where practical.

---

# 35. Secondary Button

Use:

- tonal
- outlined
- subtle surface

for secondary actions.

Examples:

```text
Save Draft
Cancel
Edit
Retry
```

---

# 36. Destructive Action

Use explicit error/destructive styling.

Examples:

```text
Delete Draft
Remove Entry
```

Do not use destructive red for ordinary validation warnings.

---

# 37. Button Height

Touch targets should remain comfortable.

Recommended:

```text
48–52dp
```

for standard buttons.

Compact actions may visually be smaller while preserving adequate touch area.

---

# 38. Button Labeling

Use verbs.

Good:

```text
Analyze
Confirm
Save Draft
Retry
Generate PDF
Share
```

Bad:

```text
OK
Continue
Submit
```

when a more precise label exists.

---

# 39. Inputs

Input fields should feel calm and precise.

Use:

- clear labels
- visible focus state
- inline validation
- helpful keyboard type
- unit awareness
- low visual noise

Do not rely on placeholder-only labels.

---

# 40. Input Density

Forms should not resemble a generic enterprise form builder.

Group related fields.

Use sections.

Reduce visible controls when they are not needed.

---

# 41. Numeric KPI Input

For KPI values:

- numeric keyboard
- clear unit
- validation state
- plausible range warning
- strong value readability

Avoid tiny helper text.

---

# 42. Validation Feedback

Validation should appear near the relevant field.

Use:

```text
icon + message
```

where useful.

Do not show generic top-level error banners for field-specific problems unless multiple errors need summary.

---

# 43. Search

Search UI should be simple and fast.

Recommended:

- rounded search field
- leading search icon
- clear action
- inline filter access

Avoid large decorative search hero areas.

---

# 44. Filters

Use:

- chips
- bottom sheet
- compact filter row

depending on complexity.

Filter controls should not overwhelm report history.

---

# 45. Status Chips

Status chips should be compact.

Examples:

```text
Confirmed
Draft
Pending
Generated
Missing
Warning
```

Use semantic colors and text.

Avoid too many colors.

---

# 46. Cards

Cards are important in FactoryFlow.

Use them for:

- KPI summaries
- report status
- recent reports
- schedules
- warnings
- quick actions

Cards must remain visually disciplined.

---

# 47. KPI Cards

KPI card hierarchy:

```text
KPI name
Value
Unit
Trend / status
Updated time
```

Do not overdecorate with multiple icons, mini charts, badges, borders, and buttons all at once.

---

# 48. Report Status Card

Should communicate:

```text
Current period
Status
Completion / warning
Primary next action
```

Example:

```text
Today's Report
Pending validation
3 values need review
[Review]
```

---

# 49. Quick Actions

Quick actions should be:

- recognizable
- low-friction
- limited
- visually consistent

Potential:

```text
Paste
Camera
Gallery
Manual
```

Do not use a 4x4 grid of every feature.

---

# 50. Lists

Lists should use:

- clear primary line
- secondary metadata
- optional status
- optional trailing action

Avoid excessive separators.

Prefer spacing + subtle dividers.

---

# 51. Tables

On phones, avoid desktop-like wide tables.

Use:

- cards
- stacked rows
- horizontal scroll only where necessary
- progressive disclosure

For report-like dense data, use a structured list with aligned values.

---

# 52. Charts

Charts must answer a clear question.

Good:

```text
How did KPI X change this week?
```

Bad:

```text
A chart because dashboards need charts.
```

Use restrained chart styling.

---

# 53. Chart Color

Use:

- accent blue
- semantic colors only when meaningful
- neutral grid lines

Avoid multicolor palettes unless comparing multiple series requires distinction.

---

# 54. Chart Grid

Keep grid lines subtle.

Avoid heavy axes.

Labels should remain readable on mobile.

---

# 55. Chart Animation

Subtle enter/update animation is acceptable.

Do not animate every data point dramatically.

---

# 56. Empty States

Premium empty states should feel intentional.

Each should contain:

```text
simple icon/illustration
clear title
short explanation
next action
```

Example:

```text
No reports yet

Confirmed reports will appear here.

[Create Report]
```

Avoid giant illustrations that dominate the screen.

---

# 57. Loading States

Loading should preserve context where possible.

Use:

- skeletons for dashboard/history
- inline spinner for button actions
- progress state for OCR/report generation
- shimmer only if subtle and consistent

Avoid blank screens.

---

# 58. Skeleton Design

Skeletons should resemble final content.

Do not use random grey blocks.

Use:

- same card shape
- same text-line structure
- subtle animation

---

# 59. OCR Loading

OCR should communicate a meaningful step.

Example:

```text
Reading image...
```

Then:

```text
Analyzing KPI values...
```

if stages are sequential.

Avoid fake percentage progress unless real progress exists.

---

# 60. Report Generation Loading

Generation state may show:

```text
Preparing report
Generating Excel
Generating PDF
Saving
```

only if those states are real and useful.

Do not invent a fake progress animation disconnected from backend state.

---

# 61. Success Feedback

Success should feel satisfying but restrained.

Use:

- check icon
- subtle motion
- clear updated state
- snackbar when appropriate

Avoid full-screen confetti.

---

# 62. Error Feedback

Errors should feel calm and actionable.

Use:

- clear title
- short explanation
- retry / alternative action

Avoid alarming visuals unless the failure is truly critical.

---

# 63. Warning Feedback

Warnings are common in the validation workflow.

Use warning styling that is visible but not hostile.

Examples:

```text
Low confidence
Outside expected range
Missing value
Unrecognized line
```

Warnings should encourage review, not imply system failure.

---

# 64. Confirmation Screen Visual Priority

The confirmation screen is one of FactoryFlow's most important screens.

Visual priority:

```text
1. KPI identity
2. Final editable value
3. Warning/confidence
4. Source/extracted value
5. Unit
6. Supporting metadata
```

User correction must feel easy.

---

# 65. Confidence Display

Do not overemphasize raw percentages if they create false precision.

Possible representation:

```text
High confidence
Review suggested
Low confidence
```

with detailed score available secondarily if useful.

Final behavior belongs in `UI_UX.md`.

---

# 66. Unrecognized Line Visual Design

Unrecognized content should be clearly separated from recognized KPI entries.

Use:

- muted surface
- warning icon
- source text
- action to resolve / ignore

Do not bury it at the bottom in tiny text.

---

# 67. Draft Design

Draft state should feel incomplete but safe.

Use:

- neutral status
- last edited timestamp
- clear resume action

Avoid making drafts look like errors.

---

# 68. Dashboard Philosophy

The dashboard is operational.

It should not become a wall of cards.

Recommended visual hierarchy:

```text
Greeting / Context
Today's status
Critical KPIs / warnings
Quick actions
Recent reports
Trend / statistics
Upcoming schedules
```

The exact hierarchy belongs in `UI_UX.md`.

---

# 69. Dashboard Density

The first viewport should provide high-value information.

Avoid large decorative whitespace before useful content.

Premium does not mean empty.

It means intentional density.

---

# 70. Dashboard Header

The header should remain compact.

Possible content:

```text
FactoryFlow
Good morning / date
Notification action
```

Do not use a giant branded hero.

---

# 71. KPI Overview

Show only the most important KPIs prominently.

Additional metrics may live in:

```text
View all
Statistics
Report detail
```

Do not attempt to show every KPI above the fold.

---

# 72. Recent Activity

Recent activity should be compact and scannable.

Use:

```text
icon
action
object
time
```

Avoid verbose activity descriptions.

---

# 73. Notifications

Notification list visual hierarchy:

```text
status/icon
title
short detail
timestamp
read/unread state
```

Unread should be subtle.

Avoid giant colored cards for every notification.

---

# 74. Schedule Cards

Schedules should clearly show:

```text
Daily / Weekly / Monthly
next run
time
enabled state
formats / delivery
```

Use switches carefully.

A schedule's enabled state should be obvious.

---

# 75. Report History

Report history should prioritize:

```text
date
type/status
submitter if useful
important warning/status
```

Filters should be easy to reach but not constantly occupy large space.

---

# 76. Generated Document Cards

Differentiate:

```text
Excel
PDF
```

using icon + label, not large color differences.

Actions:

```text
Open
Share
Email
```

may appear contextually.

---

# 77. Search Results

Matched text/filters may be subtly emphasized.

Avoid aggressive highlighting.

---

# 78. Dialogs

Use dialogs only for:

- important confirmation
- destructive action
- focused small decision

Do not use dialogs as primary navigation.

---

# 79. Bottom Sheets

Bottom sheets are preferred for:

- filters
- acquisition action choices
- contextual options
- lightweight editing

Use them when they preserve screen context.

---

# 80. Toasts and Snackbars

Prefer Snackbar for actionable temporary feedback.

Do not use Toast for important business state.

---

# 81. Microinteractions

Premium quality lives in small details.

Examples:

- button press response
- card press state
- subtle list insertion
- smooth status transition
- focus animation
- validation reveal
- successful save feedback

Every microinteraction should be fast.

---

# 82. Motion Duration

Suggested ranges:

```text
Fast:
120–160ms

Standard:
180–240ms

Emphasized:
250–320ms
```

Avoid 500ms+ transitions for routine actions.

---

# 83. Motion Easing

Use natural Material easing.

Avoid exaggerated bounce.

Use spring only where it improves direct manipulation or subtle delight.

---

# 84. Screen Transitions

Navigation transitions should preserve spatial logic.

Examples:

```text
list → detail
```

may use subtle horizontal or shared-axis movement.

Do not use dramatic zooms.

---

# 85. Loading Transition

Avoid abrupt content pop when possible.

Use:

- fade
- skeleton replacement
- size animation

subtly.

---

# 86. Confirmation Success Motion

A confirmed report may briefly show:

```text
check
status transition
```

before navigation.

Keep it under approximately one second total.

---

# 87. Reduced Motion

Respect system reduced-motion preferences where practical.

Do not require animation to understand state.

---

# 88. Accessibility Contrast

All text and meaningful icons must meet sensible contrast standards.

Do not use very light grey text for important metadata.

Accent colors must remain legible in both themes.

---

# 89. Touch Targets

Interactive controls should provide at least approximately:

```text
48dp
```

effective touch area where possible.

Do not make tiny icon buttons.

---

# 90. Typography Accessibility

Support system font scaling reasonably.

Do not design layouts that collapse immediately when font size increases.

---

# 91. System Insets

Respect:

- status bar
- navigation bar
- cutouts
- IME/keyboard

Do not let content hide behind system UI unintentionally.

---

# 92. Keyboard Behavior

Forms must remain usable when keyboard is visible.

Ensure:

- focused field remains visible
- submit action reachable
- numeric keyboard for KPI values
- next/done actions meaningful

---

# 93. Light / Dark Consistency

Components must not be designed only for light mode and then mechanically recolored.

Review:

- border visibility
- icon contrast
- warning colors
- chart grid
- elevated surfaces
- disabled states

in both modes.

---

# 94. Disabled States

Disabled controls should remain readable.

Do not reduce opacity so far that text becomes illegible.

Disabled state should communicate unavailable, not broken.

---

# 95. Focus States

Keyboard/accessibility focus should remain visible where relevant.

Do not remove default accessibility focus indicators without replacing them appropriately.

---

# 96. Dividers

Use dividers sparingly.

Prefer:

```text
spacing
surface grouping
```

first.

---

# 97. Section Headers

Use concise section labels.

Examples:

```text
Today
Recent Reports
Needs Attention
Upcoming
```

Avoid verbose headings.

---

# 98. Information Priority

Every screen should identify:

```text
Primary information
Secondary information
Metadata
Actions
```

Do not give every element equal visual weight.

---

# 99. Progressive Disclosure

Show essential information first.

Reveal advanced detail:

- on tap
- in detail screen
- through expansion
- in bottom sheet

This keeps industrial complexity manageable.

---

# 100. Density Modes

FactoryFlow should not implement configurable density modes initially.

Design one balanced density suitable for operational Android use.

---

# 101. Premium Quality Checklist

Every screen should be reviewed for:

```text
[ ] Clear hierarchy
[ ] Consistent spacing
[ ] Strong typography
[ ] Restrained color
[ ] Correct radius
[ ] Correct elevation
[ ] Consistent iconography
[ ] Good empty state
[ ] Good loading state
[ ] Good error state
[ ] Good warning state
[ ] Smooth motion
[ ] Proper touch targets
[ ] Accessibility
[ ] Dark/light consistency if both supported
[ ] No accidental default-looking components
[ ] No decorative clutter
```

---

# 102. "Accidental Default" Rule

FactoryFlow may use Material components.

It must not look like a raw Material sample app.

Customize intentionally:

- typography
- spacing
- shapes
- color
- component density
- iconography
- state presentation

At the same time, do not fight the platform.

---

# 103. Premium vs Overdesigned

Before adding a visual element, ask:

```text
Does this improve:
clarity,
hierarchy,
trust,
speed,
or usability?
```

If not, remove it.

---

# 104. Brand Restraint

FactoryFlow branding should be subtle.

Use:

- logo/app icon
- accent color
- typography
- consistent shapes

Do not repeat a giant logo across every screen.

---

# 105. FactoryFlow Brand Character

The visual identity should suggest:

```text
Flow
Control
Reliability
Precision
Industrial intelligence
```

Avoid literal gears everywhere.

Industrial identity can be communicated through:

- data
- structure
- precision
- terminology
- iconography
- status design

not through cliché machinery graphics.

---

# 106. Illustration Philosophy

Use illustrations sparingly.

Potential use:

- first empty state
- onboarding if ever included
- major error state

Prefer simple line/abstract illustrations.

Do not use cartoon workers or factory clip-art.

---

# 107. Photography

FactoryFlow does not need stock photography.

The product itself is the visual focus.

---

# 108. Logo Usage

The logo should primarily appear:

- app icon
- login/startup
- About
- README/portfolio

It does not need to appear in every top bar.

---

# 109. Splash Screen

Splash should be minimal.

Use platform-native splash behavior.

Possible:

```text
FactoryFlow icon
neutral background
```

Do not create a multi-second animated intro.

---

# 110. Login Screen Direction

Login should feel:

- secure
- simple
- premium
- low-friction

Potential hierarchy:

```text
Brand
Welcome / short context
Email
Password
Primary login button
Subtle supporting information
```

Avoid decorative dashboard previews behind login.

---

# 111. Acquisition Screen Direction

Acquisition options should feel like tools, not settings.

Use recognizable choices:

```text
Paste text
Gallery
Camera
Manual
```

with clear icons.

Share Intent flow may bypass this screen.

---

# 112. OCR Preview Direction

If image preview is shown:

- keep image visible enough for context
- avoid making it dominate
- allow easy transition to extracted text/confirmation

---

# 113. Confirmation Screen Direction

This screen should feel like a professional review workstation adapted to mobile.

Use:

- compact cards/rows
- editable values
- visible warnings
- clear source context
- sticky/accessible final action if appropriate

Avoid turning each KPI into a huge card.

---

# 114. Report Detail Direction

Report detail should emphasize:

```text
period/date
status
KPI values
audit metadata
document actions
```

Use progressive disclosure for less important metadata.

---

# 115. Generated Report Viewer Direction

Do not build a full office suite.

Use Android/system viewer where appropriate.

FactoryFlow's responsibility is:

- file metadata
- open
- share
- email
- regenerate if supported

---

# 116. Statistics Direction

Statistics should feel analytical but calm.

Prefer:

- one key chart per section
- period controls
- clear summary metric
- subtle legends

Avoid dashboard overload.

---

# 117. Notification Direction

Notifications should be informative, not alarming.

Use strong warning visual only for actionable problems.

---

# 118. Settings Direction

If settings exist, follow familiar Android patterns.

Use grouped rows.

Do not over-brand settings.

---

# 119. Profile Direction

Because there is one effective role, profile should remain simple.

Possible:

```text
Name
Email
Session/logout
App information
```

Do not invent complex account management.

---

# 120. Visual Consistency Rule

The same concept should look the same everywhere.

Examples:

```text
Draft
Confirmed
Warning
Generated
Missing
```

must use consistent chip/icon/tone treatment.

Do not redefine status styles screen by screen.

---

# 121. Component Reuse Rule

Build reusable components when visual language repeats.

Examples:

```text
FactoryFlowCard
StatusChip
KpiValueRow
SectionHeader
PrimaryActionButton
EmptyState
ErrorState
LoadingSkeleton
```

Do not build a giant universal component with dozens of boolean parameters.

Reusable components should remain focused.

---

# 122. Theme Token Rule

Do not scatter magic design values.

Centralize:

```text
colors
typography
spacing
shapes
motion values
```

through theme/design-system code.

---

# 123. Preview Rule

Use Compose previews for important reusable components and screens when practical.

Preview:

- light mode
- dark mode
- loading
- error
- representative data

This improves design quality before device testing.

---

# 124. Screenshot Review Rule

For important screens:

1. run on realistic phone
2. capture screenshot
3. inspect alignment
4. inspect empty space
5. inspect hierarchy
6. inspect contrast
7. inspect text wrapping
8. compare with `DESIGN.md`

Do not judge premium quality only from source code.

---

# 125. Real Device Review

At least before presentation:

Test on a physical Android device if available.

Check:

- touch
- keyboard
- camera
- gallery
- Share Intent
- FileProvider
- notification behavior
- performance
- system bars

Premium quality depends on real-device behavior.

---

# 126. Performance and Design

Animations should not reduce responsiveness.

Scrolling must remain smooth.

Avoid:

- heavy recomposition
- expensive blur
- large image transformations on main thread
- excessive nested scrolling

Visual quality must not sacrifice performance.

---

# 127. Premium Industrial Dashboard Rule

The dashboard should communicate **operational control**.

A premium dashboard is not:

```text
12 random cards
4 pie charts
5 gradients
```

It is:

```text
Today's status
Critical information
Important KPIs
Clear next actions
Recent context
Useful trends
```

---

# 128. Premium Form Rule

Forms should feel almost invisible.

The user should think about the data, not the UI.

Use:

- correct keyboard
- clear label
- unit
- inline error
- automatic focus progression where useful
- minimal unnecessary fields

---

# 129. Premium Validation Rule

Validation should guide.

It should not punish.

Low confidence:

```text
Needs review
```

is better than:

```text
ERROR!
```

when the value may still be correct.

---

# 130. Premium Reporting Rule

The mobile UI and generated documents should feel like one product.

Excel/PDF should visually share:

- naming
- hierarchy
- brand restraint
- terminology

They do not need to mimic the Android theme exactly.

---

# 131. Industrial Trust Rule

Avoid visual tricks that reduce trust.

Do not use:

- fake loading percentages
- fake live data
- fake AI indicators
- animated numbers that imply precision without reason
- "smart" labels unsupported by actual behavior

Premium includes honesty.

---

# 132. Data Freshness Display

Where freshness matters, show it subtly.

Examples:

```text
Updated 3 min ago
Today, 14:20
```

Do not place timestamps everywhere.

Use them where they support operational decisions.

---

# 133. Realtime Visual Updates

When WebSocket events update the UI:

Use subtle transition.

Do not flash the entire dashboard.

Possible:

- fade updated value
- small status change
- subtle snackbar

---

# 134. Notification Badge

If used, badge counts should remain small and meaningful.

Avoid badge noise.

---

# 135. Report Status Language

Use concise labels.

Preferred:

```text
Draft
Pending Review
Confirmed
Generated
Failed
Missing
```

Avoid technical backend terminology.

---

# 136. Severity Language

Use:

```text
Info
Review
Warning
Critical
```

only if business rules justify those distinctions.

Do not create severity taxonomies without actual semantics.

---

# 137. Icon + Text Pairing

For important statuses, use both.

Example:

```text
⚠ Needs review
```

but use a proper icon component, not emoji in production UI.

---

# 138. Empty Dashboard

If no data exists, dashboard should still be useful.

Show:

- no confirmed reports
- next action
- acquisition shortcut
- upcoming schedule if relevant

Do not show a dead blank screen.

---

# 139. First-Run Experience

Do not build elaborate onboarding unless needed.

A concise first-run state may explain:

```text
Create your first report by pasting text, importing an image, taking a photo, or entering values manually.
```

That is enough initially.

---

# 140. Help and Guidance

Use contextual helper text where the workflow may be unfamiliar.

Avoid permanent tooltips.

Examples:

```text
Paste the KPI message exactly as received.
```

or:

```text
Review highlighted values before confirming.
```

---

# 141. Confirmation Safety

The final Confirm action should be visually strong and unambiguous.

If unresolved warnings remain, the UI should clearly communicate that before confirmation.

Exact business blocking behavior belongs in `UI_UX.md`.

---

# 142. Draft Safety

If leaving a validation flow with unsaved edits:

The UI should avoid silent loss.

Possible strategies:

- autosave draft
- explicit save prompt
- persistent draft state

Final behavior belongs in `UI_UX.md`.

---

# 143. Destructive Confirmation

For meaningful destructive actions, use a concise confirmation.

Example:

```text
Delete this draft?

Your saved edits will be removed.
```

Actions:

```text
Cancel
Delete Draft
```

---

# 144. Color Restraint in Charts

If several KPI series are displayed, use a controlled palette.

Do not use 10 saturated colors.

Prefer:

- accent family
- nearby muted hues
- semantic colors only for status

---

# 145. Decorative Gradients

Default:

```text
Do not use.
```

A very subtle branded gradient may be considered for a rare marketing-like surface such as login/splash, but only if it improves the design.

Do not use gradients on routine cards.

---

# 146. Glassmorphism

Default:

```text
Do not use.
```

It is visually fashionable but poorly suited to an industrial operational interface.

---

# 147. Blur

Avoid heavy blur effects.

Use only when platform component behavior naturally requires it.

---

# 148. Neumorphism

Do not use.

It reduces accessibility and clarity.

---

# 149. Oversized Typography

Do not use website-style 48–72sp hero typography in normal mobile screens.

Large typography should remain functional.

---

# 150. Decorative Backgrounds

Prefer clean neutral backgrounds.

Avoid:

- patterns
- industrial blueprint textures
- abstract blobs
- factory photos
- animated backgrounds

---

# 151. Premium Details That Matter

Spend design effort on:

```text
perfect spacing
good empty states
correct error copy
button states
input focus
status consistency
smooth loading
screen transitions
touch feedback
clear hierarchy
```

These produce more premium perception than decorative effects.

---

# 152. Dark Mode Accent Control

Accent brightness may require different tone in dark mode.

Do not use the exact same raw hex if it causes excessive glow/contrast.

Use semantic theme roles.

---

# 153. Text Color Hierarchy

Recommended conceptual contrast:

```text
Primary text:
highest

Secondary:
medium

Tertiary:
subtle

Disabled:
lower but still legible
```

Do not create five almost-identical grey levels.

---

# 154. Border Color Hierarchy

Use at most:

```text
subtle
strong
focus
```

in most situations.

---

# 155. Focus Accent

Focused inputs may use accent border/indicator.

Do not combine:

- accent border
- accent background
- glow
- icon change

all at once.

---

# 156. Pressed State

Every tappable surface should visibly respond.

Use:

- ripple
- tonal shift
- slight opacity/scale where appropriate

Material interaction behavior should remain familiar.

---

# 157. Hover

Hover is low priority on phones.

If tablet/stylus/mouse support naturally benefits, maintain Material behavior.

Do not design primarily around hover.

---

# 158. Component Density Consistency

Two cards of the same type should use the same:

- internal spacing
- radius
- label style
- metadata style
- action placement

---

# 159. List Row Height

Rows should be compact but touch-friendly.

Do not use 80–100dp rows unless content requires it.

---

# 160. Section Rhythm

A premium screen should have a visible rhythm.

Example:

```text
Screen title
24dp
Primary status card
24dp
Section header
12dp
List/cards
24dp
Next section
```

Exact values may vary, but rhythm should remain intentional.

---

# 161. Text Wrapping

Design for professional French labels and long KPI names.

Avoid layouts that work only with short English mock text.

---

# 162. Localization Readiness

The Android UI uses professional French and all user-facing copy lives in string
resources. Code/technical identifiers remain English. Design must still avoid
assumptions that make future localization impossible.

Do not hardcode widths around one exact label.

---

# 163. Numbers and Units Alignment

When displaying multiple KPI values, alignment should help scanning.

Consider:

- right alignment for values in table-like rows
- consistent unit column
- tabular numbers

Do not sacrifice mobile readability for desktop-style alignment.

---

# 164. Date Formatting

Dates should be readable and human-oriented.

Examples:

```text
11 Aug 2026
Today, 14:20
This week
August 2026
```

Avoid raw ISO timestamps in UI.

---

# 165. Report Type Identity

Daily / Weekly / Monthly should be distinguishable through:

- text
- icon
- subtle metadata

not separate bright color families.

---

# 166. Loading Copy

Keep loading messages short.

Good:

```text
Analyzing report…
Generating PDF…
Loading history…
```

Avoid verbose technical wording.

---

# 167. Error Copy

Good error copy structure:

```text
What happened
What user can do
```

Example:

```text
Couldn’t analyze this report.
Check the text and try again.
```

---

# 168. Warning Copy

Good:

```text
This value is outside the expected range.
Review it before confirming.
```

Avoid:

```text
INVALID KPI VALUE
```

unless the value is definitively invalid.

---

# 169. Success Copy

Good:

```text
Report confirmed
Draft saved
PDF generated
```

Short and calm.

---

# 170. Empty Copy

Good empty states should explain the absence.

Example:

```text
No generated reports yet

Generate an Excel or PDF report to see it here.
```

---

# 171. Premium Screen Completion Standard

A screen is not visually complete until it has all applicable states:

```text
Normal
Loading
Empty
Error
Warning
Disabled
Success
Pressed
Focused
```

Do not design only the happy path.

---

# 172. UI QA Checklist

Before a major screen is accepted:

```text
[ ] Looks intentional at first glance
[ ] Key action obvious
[ ] Key information obvious
[ ] No accidental visual clutter
[ ] Spacing consistent
[ ] Typography consistent
[ ] Buttons consistent
[ ] Status colors consistent
[ ] Loading state polished
[ ] Empty state polished
[ ] Error state polished
[ ] Warning state polished
[ ] Dark mode reviewed if supported
[ ] Accessibility reviewed
[ ] Keyboard behavior reviewed
[ ] Back navigation reviewed
[ ] Real device screenshot reviewed
```

---

# 173. Premium Benchmark

FactoryFlow should visually compare favorably with high-quality modern productivity software.

The benchmark is not:

> "Does this look better than a default student app?"

The benchmark is:

> "Would this feel credible if installed by a maintenance engineer at a serious company?"

---

# 174. Design Decision Rule

When choosing between two visual solutions:

Prefer the one that is:

```text
simpler
clearer
quieter
more consistent
more Android-native
easier to scan
```

unless the more expressive solution has a clear usability advantage.

---

# 175. Design Handoff Rule

Codex must not invent screen-specific behavior from `DESIGN.md`.

`DESIGN.md` defines the visual system.

`UI_UX.md` defines:

- screen structure
- flows
- navigation
- interaction
- state transitions
- exact actions

Use both together.

---

# 176. Final Premium Principle

FactoryFlow should feel expensive without looking luxurious.

Its premium quality should come from:

```text
precision
restraint
smoothness
clarity
confidence
consistency
```

not from decoration.

---

# 177. Final Visual Goal

The final application should communicate, within seconds:

> This is a serious industrial tool.

Then, through interaction:

> This is also beautifully designed.

The user should never have to choose between professional industrial software and premium modern software.

FactoryFlow must be both.

---

# End of DESIGN.md
