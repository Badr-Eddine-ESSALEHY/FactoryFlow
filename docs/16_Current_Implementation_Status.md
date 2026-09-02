# FactoryFlow Current Implementation Status

> Status: Active implementation register  
> Last verified: 2026-09-02

This file records what exists in the repository now. Design documents may describe
future options, but they must not be presented as implemented unless this register and
the code agree.

## Implemented product flows

- Native Android client in Kotlin, Jetpack Compose, MVVM, Hilt, Retrofit, Coroutines,
  StateFlow, Navigation Compose, and Material 3.
- French user-facing copy in Android resources.
- JWT access-token login and local secure session storage. Token expiry returns to login.
- KPI definition management, deterministic parsing, fuzzy alias matching, numeric
  normalization, draft persistence, explicit human validation, and confirmed history.
- Manual entry, pasted text, gallery image, shared text, and shared image acquisition.
- Authenticated backend OCR endpoint backed by the private PaddleOCR PP-OCRv5 runtime.
- Dashboard/statistics from confirmed final values.
- Exact single-report Excel/PDF export for one confirmed Maintenance Report ID.
- Consolidated Excel/PDF generation for daily, weekly, monthly, and custom inclusive
  periods, using confirmed source reports only.
- One-sheet Apache POI workbook with official Alf Mabrouk logo and manager-facing detail
  columns: Date, Indicateur, Valeur, Valeur associée, Unité.
- Apache PDFBox reports with official logo, warm white/sage identity, pagination,
  traceability, and weekly/monthly charts only when enough confirmed points exist.
- Generated-file storage abstraction, history, download/open/share/e-mail intents.
- Quartz daily/weekly/monthly schedules in `Africa/Casablanca`, format selection,
  weekday/time configuration, recipients, enabled/paused state, and persisted run history.
- One grouped scheduled e-mail with every requested attachment, HTML plus plain-text
  alternative, and separate generation/e-mail delivery states.
- Durable, user-scoped in-app notifications.
- Maintenance Intelligence profiles, versioned analytical snapshots, contextual alerts,
  and secured retrieval APIs over confirmed KPI history.
- Android Maintenance Intelligence overview, five-page per-KPI workspace, contextual-alert
  list/detail, persisted-notification deep links, and interactive analytical charts.
- PostgreSQL + Flyway production persistence and automated unit/integration test suites.

## Intentionally not implemented

- CameraX/direct camera acquisition. It was removed by the approved M3 scope decision.
- On-device ML Kit OCR. OCR currently runs in the private backend PaddleOCR runtime.
- Refresh-token rotation or server-side logout/revocation. Authentication currently uses
  access tokens only.
- WebSocket/STOMP realtime transport. REST remains authoritative.
- Firebase Cloud Messaging. Notifications are persisted in-app.
- RabbitMQ. Scheduled/report processing remains synchronous and in-process.
- SignalR. It belongs to a separate project and is not part of FactoryFlow.
- Android-side anomaly detection, forecasting, trend classification, or contextual-alert
  decisions. The mobile client presents persisted backend results and never recomputes them.
- Per-observation Isolation Forest feature-vector charts and rolling-origin actual-versus-
  predicted charts. The current API exposes feature names and aggregate/per-horizon
  validation metrics, not the underlying point-level values needed to draw those views.

## Reporting and delivery invariants

1. Draft or extracted values are never official report data.
2. Individual export contains exactly the requested confirmed report ID.
3. Consolidated export contains every confirmed report inside the explicit inclusive
   period and excludes drafts.
4. Missing values remain missing and are never converted to zero.
5. File generation and e-mail delivery are separate outcomes.
6. SMTP failure never removes a successfully generated READY file.
7. A partial multi-format generation sends no misleading partial e-mail.
8. One schedule execution creates at most one grouped e-mail and one failure notification
   for a batch-level delivery/generation failure.

## Configuration truth

- Development/release API base URLs are build configuration, not scattered literals.
- Release builds require a real HTTPS `FACTORYFLOW_RELEASE_API_BASE_URL`.
- SMTP defaults to Gmail-compatible port 587 with authentication and STARTTLS enabled.
- `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM` are environment values; no real
  mailbox, app password, database credential, or token belongs in Git.

## Verification state

- Backend compile and test-compile pass. The focused Maintenance Intelligence notification
  and API-contract run passes 2 tests with no failures, errors, or skips.
- The current complete PostgreSQL-backed backend run passes 188 tests with 0 failures,
  0 errors, and 2 intentional skips. All 13 Flyway migrations validate against PostgreSQL
  18.4; Flyway emits its expected newer-server compatibility warning.
- Android: `testDebugUnitTest`, `lintDebug`, and `assembleDebug` pass. The unit suite contains
  48 tests across 14 suites with no failures, errors, or skips.
- Backend-connected emulator inspection covered the overview, five KPI analytical pages,
  contextual-alert list/detail, notification deep link, long names, short/long histories,
  anomaly/no-anomaly, interval/no-interval, duplicate dates, cadence ambiguity, and retained
  analysis after technical refresh failure. Phase 3 is accepted and frozen; the remaining
  emulator rerun was explicitly waived as a non-blocking visual acceptance item.
- Remaining external acceptance outside the frozen Phase 3 checkpoint: live SMTP delivery
  and the production HTTPS API URL.
