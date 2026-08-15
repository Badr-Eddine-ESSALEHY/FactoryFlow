# FactoryFlow Android Flow Design System

This document records the production design foundation established for the Dashboard redesign. It describes implemented code, not a separate preview-only specification. Later screen migrations must reuse these tokens and components rather than recreating screen-local visual systems.

## Visual foundation

The Android UI uses a soft productivity-dashboard language:

- light canvas `#F4F6FB` with pure-white elevated cards;
- dark canvas `#11141B` with `#191D27` and `#202532` elevated surfaces;
- primary system identity `#4E7FFF`, with `#3355E0` for pressed and gradient depth;
- semantic functional accents: orange for pasted input/attention, purple for manual entry, teal for automation, green for confirmed data, indigo for documents, and pink/red for unresolved or failed states;
- 22dp cards, 24dp hero surfaces, 13dp icon tiles, 14dp controls, circular avatars/FABs, and pill-only compact status controls;
- soft neutral card shadows and a stronger blue-tinted center-FAB shadow.

The authoritative implementation lives in:

- `core/design/Color.kt` for light, dark, primary, accent, tint, and compatibility colors;
- `core/design/Tokens.kt` for spacing, radius, size, elevation, opacity, and motion values;
- `core/design/Theme.kt` for Material color, typography, and shape mappings;
- `core/design/Components.kt` for production Flow composables.

## Production components

The shared component inventory is:

- `FlowScreen`
- `FlowContentSurface`
- `FlowTopBar`
- `FlowPageHeader`
- `FlowSectionHeader`
- `FlowCard`
- `FlowCategoryCard`
- `FlowListRow`
- `FlowIconTile`
- `FlowMetricBadge`
- `FlowStatusPill`
- `FlowProgressRing`
- `FlowSegmentedControl`
- `FlowMiniChart`
- `FlowHeroBanner`
- `FlowBottomNavigation`
- `FlowFab`
- `FlowEmptyState`

Cards and primary actions provide restrained press-scale feedback. Progress rings animate from zero, the chart reveals a smooth cubic path and supports selected periods, and the center FAB compresses its scale and shadow while pressed. Animation is state-driven and does not run continuously for decoration.

## Dashboard mapping

The Dashboard translates confirmed backend data into the Flow grammar without fabricating values:

- reports use blue;
- reports requiring continuation use orange;
- generated documents and schedules use teal/indigo;
- manual entry uses purple;
- confirmed status uses green;
- missing values use amber;
- errors remain red/pink.

The Dashboard continues to render `DashboardDto` from `DashboardViewModel`. Its route callbacks, repository behavior, API contract, and refresh behavior remain unchanged.

## Application-wide migration

The accepted Flow foundation is now applied across the Android presentation layer without changing route, ViewModel, repository, or API contracts:

- Login uses a compact, logo-free Flow identity, borderless elevated inputs, and the shared primary action.
- Reports and generated documents use compact `FlowListRow` history; report detail and confirmed export retain their production data and file actions in the same card grammar.
- Create, paste, manual entry, gallery OCR, and shared-image OCR use permanent source accents, dominant source/input surfaces, and Flow loading and empty states.
- Review preserves ready, attention, missing, and unresolved semantics using green, orange, amber, and pink/red while keeping source text collapsible and missing values distinct from zero.
- Statistics uses one real-data analytics surface rather than a hero plus four detached KPI tiles. Its interactive cubic area chart exposes the selected value and effective date, animates its reveal, and groups latest/average/minimum/maximum into one compact summary strip. Missing samples remain excluded from calculations.
- Schedules and schedule editing use teal automation rows, Flow selectors, inputs, toggles, and execution history.
- Notifications and Profile use the same compact list, empty, identity, settings, theme, and destructive-action grammar.
- Startup continues to render the production `LoadingPane` while session restoration runs.

No corporate logo is rendered inside the normal Android interface.

## Preview contract

Preview files may supply debug-only fixtures, but must call production composables. The current authoritative set is:

- Login light;
- Login dark;
- Dashboard light at 411 × 891 with system UI;
- Dashboard dark at 411 × 891 with system UI;
- Dashboard full-page at 411 × 2800.
- Create light and dark;
- Paste light and dark;
- Manual entry light/dark and OCR result light/dark;
- report history, generated documents, report detail, and confirmation/export;
- full Review content light and dark;
- Statistics light and dark;
- schedule list and schedule form light/dark;
- Notifications and Profile light/dark coverage.

Screen-level composables own Hilt, lifecycle collection, navigation effects, and
system integrations. Previewable production `*Content` composables receive state
and callbacks only. `FlowScreen` applies safe drawing insets to standalone/top-level
pages, while `FactoryFlowScaffold` owns safe insets for focused routes and
`FlowContentSurface` guarantees the correct canvas behind extracted content in both
runtime and previews.

The 411 × 891 Dashboard previews render `DashboardContent` inside the production
`FactoryFlowAppShell`, so their navigation and centered FAB are the same
implementation used by the APK. The 411 × 2800 inspection preview renders the
production `DashboardContent` directly; this exposes the full scrolling page without
an artificial bottom-pinned navigation gap.

The compact Dashboard baseline uses a 34dp avatar, 114dp Situation cards, 128dp
Quick Action cards, 40dp icon tiles, approximately 58dp list rows, a 108dp mini
chart, a 60dp icon-only navigation surface, and a 58dp centered FAB rising 10dp
above that surface. Situation cards use `icon → category → compact count`; missing
KPI values remain a separate conditional warning pill.
