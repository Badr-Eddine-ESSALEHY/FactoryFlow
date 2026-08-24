# FactoryFlow Current Implementation Status

> Status: Active implementation register  
> Last verified: 2026-08-22

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

- Backend compile and test-compile pass.
- Database-independent backend suite: 126 tests, 0 failures, 0 errors, 1 opt-in
  PaddleOCR runtime test skipped.
- Complete backend suite: 161 tests discovered, 0 assertion failures, 35 Spring context
  errors, 1 skip. All 35 errors are caused by rejected local PostgreSQL authentication
  before integration tests can execute; valid test credentials are still required.
- Android: `compileDebugKotlin`, `testDebugUnitTest`, `lintDebug`, and `assembleDebug`
  pass. The unit suite contains 41 tests with no failures, errors, or skips.
- Remaining external acceptance: live SMTP delivery, production HTTPS API URL, and
  end-to-end/visual verification on the target Vivo device.
