# 11_Coding_Standards.md

> **FactoryFlow — Coding Standards**
>
> Version: 1.0  
> Status: Active  
> Last updated: 2026-08-11
>
> This document defines cross-project coding rules for FactoryFlow.
>
> It complements `SKILLS.md` and the platform-specific documents:
>
> - `07_Android.md`
> - `08_Backend.md`
>
> The objective is consistent, readable, explainable code.

---

# 1. Core Standard

Code should optimize for:

```text
correctness
clarity
maintainability
testability
```

not cleverness.

---

# 2. Business Language

Use FactoryFlow domain terminology.

Preferred:

```text
MaintenanceReport
KpiDefinition
KpiEntry
GeneratedReport
ReportSchedule
ExtractionResult
```

Avoid vague names:

```text
Data
Info
Manager
Thing
Helper
Processor
```

unless meaning is genuinely clear.

---

# 3. Naming

Names must express intent.

Good:

```text
confirmReport
findLatestConfirmedKpiValues
calculateWeeklyPeriod
recognizeKpiLabel
```

Bad:

```text
process
handleData
doWork
execute2
```

---

# 4. Classes

A class should have one coherent responsibility.

Do not allow giant classes to accumulate unrelated behavior.

---

# 5. Methods

Methods should be understandable without tracing many hidden side effects.

Prefer small coherent methods.

Do not split code into meaningless one-line methods purely for style.

---

# 6. Comments

Comments explain:

```text
why
business rule
non-obvious constraint
tradeoff
```

Do not comment obvious syntax.

Bad:

```java
// increment counter
counter++;
```

Good:

```java
// Missing KPI values must remain null; zero is a valid reported value.
```

---

# 7. Public API Documentation

Document public contracts and non-obvious behavior.

Do not force Javadoc/KDoc on every trivial getter.

---

# 8. Magic Values

Avoid scattered magic numbers/strings.

Examples to centralize:

- parser thresholds
- status codes
- date formats
- MIME types
- schedule defaults

---

# 9. Constants

Use constants when value has stable meaning.

Do not create constants for every literal if it reduces readability.

---

# 10. Nullability

Treat null as business meaning, not inconvenience.

Especially:

```text
missing KPI value
optional unit
no generated_by for system schedule
```

Do not convert null to fake defaults.

---

# 11. Exceptions

Use exceptions for exceptional/error conditions.

Do not use exceptions as normal control flow.

---

# 12. Error Messages

Technical logs may be detailed.

User/API messages must remain clear and stable.

---

# 13. Java Standards

Use modern Java language features where they improve clarity.

Prefer:

```text
records
enums
java.time
BigDecimal
streams when readable
constructor injection
```

Avoid:

```text
raw types
legacy Date
field injection
mutable public fields
```

---

# 14. Java Formatting

Use project formatter/IDE defaults consistently.

Do not manually align code with spaces in a way formatter destroys.

---

# 15. Java Class Naming

PascalCase:

```text
ReportConfirmationService
KpiDefinitionRepository
```

---

# 16. Java Method/Field Naming

camelCase:

```text
confirmReport
plausibleMinimum
```

---

# 17. Java Constants

UPPER_SNAKE_CASE:

```text
DEFAULT_FUZZY_THRESHOLD
```

---

# 18. Java Packages

lowercase:

```text
com.factoryflow.report.application
```

---

# 19. Java `BigDecimal`

Use `BigDecimal` for KPI/business decimal values.

Do not use floating-point equality.

---

# 20. Java Time

Use `Instant`, `LocalDate`, `LocalTime`, `ZoneId`, etc.

No business logic based on `System.currentTimeMillis()` where `Clock` improves testability.

---

# 21. Spring Controllers

Thin.

No repository access for workflow logic.

No file generation.

No parser implementation.

---

# 22. Spring Services

Use case-oriented.

Do not build one giant service.

---

# 23. Spring Repositories

Persistence only.

No UI/API-specific presentation logic.

---

# 24. JPA Entities

Do not serialize directly to REST.

Avoid `@Data`.

Use enum strings.

Keep relationships deliberate.

---

# 25. DTOs

DTOs are explicit API contracts.

Name request/response objects clearly.

---

# 26. Validation

Client validation improves UX.

Backend validation protects correctness.

Database constraints protect integrity.

All three have different responsibilities.

---

# 27. Kotlin Standards

Prefer:

```text
val
data class
sealed interface
null safety
coroutines
StateFlow
```

Avoid:

```text
!!
GlobalScope
mutable globals
Java-style boilerplate
```

---

# 28. Kotlin Naming

Follow Kotlin conventions.

Classes:

```text
DashboardViewModel
```

Functions/properties:

```text
loadDashboard
isRefreshing
```

---

# 29. Compose

Composable functions render state and emit events.

No direct API/database calls.

---

# 30. Compose Naming

Screen:

```text
DashboardScreen
```

Reusable component:

```text
StatusChip
KpiValueRow
```

---

# 31. Compose Parameters

Prefer explicit state + callbacks.

Avoid passing ViewModel deep into reusable components.

---

# 32. State

Avoid contradictory booleans.

Use sealed state or coherent data class.

---

# 33. Coroutines

Structured concurrency only.

No `GlobalScope`.

Do not block main thread.

---

# 34. Flow

Use Flow/StateFlow where reactive state adds value.

Do not turn simple one-shot operations into unnecessarily complex streams.

---

# 35. Android Strings

All user-facing text belongs in resources.

---

# 36. Android Colors

Use theme tokens.

No random hardcoded colors in screens.

---

# 37. Android Dimensions

Follow `DESIGN.md`.

Avoid arbitrary spacing values.

---

# 38. API Naming

Use consistent REST naming.

Plural resource names:

```text
/reports
/kpi-definitions
/generated-reports
```

---

# 39. JSON Naming

Use one convention consistently.

Recommended camelCase:

```json
{
  "finalValue": 15.8,
  "confirmedAt": "..."
}
```

---

# 40. Database Naming

snake_case.

---

# 41. Enum Naming

Code enum values uppercase:

```text
CONFIRMED
GALLERY_OCR
```

UI maps to friendly labels.

---

# 42. Test Naming

Tests should state behavior.

Good:

```text
shouldPreserveMissingValueInsteadOfZero
shouldPreferExactAliasOverFuzzyMatch
shouldRejectSecondConfirmation
```

---

# 43. Test Structure

Use Arrange / Act / Assert conceptually.

Do not over-comment tests.

---

# 44. Test Independence

Tests must not rely on execution order.

---

# 45. Parser Tests

Use realistic format cases.

Every parser bug should create regression coverage when practical.

---

# 46. No Dead Code

Remove abandoned implementation.

Do not leave large commented code blocks.

Git already preserves history.

---

# 47. TODOs

A TODO must explain:

```text
what
why
when/under which task
```

Example:

```text
TODO(FF-3501): register refreshed FCM token with backend.
```

Avoid permanent vague TODOs.

---

# 48. Feature Flags

Do not add generic feature-flag infrastructure.

Use build/config switches only when real need exists.

---

# 49. Logging

Logs must be meaningful and safe.

Never log secrets.

---

# 50. Security

Never hardcode credentials.

Never trust Android validation alone.

Never expose raw file paths.

---

# 51. File Handling

Use storage abstractions/backend and FileProvider/Android.

Close streams.

Sanitize names.

---

# 52. Date/Time Handling

Use explicit timezone semantics.

No hidden reliance on server/device defaults for business schedules.

---

# 53. Numeric Formatting

Store precise values.

Format for display separately.

Do not corrupt precision for UI convenience.

---

# 54. Dependency Discipline

Add dependencies only for actual needs.

Avoid duplicate libraries solving same problem.

---

# 55. Architecture Discipline

Do not introduce patterns just to look enterprise.

Do not bypass boundaries just to save a few lines.

---

# 56. Refactoring

Refactor with a concrete reason.

Run tests after critical refactor.

---

# 57. Formatting

Use automated formatter where available.

Avoid mixed styles.

---

# 58. Linting

Android lint/Kotlin static checks and backend static checks should be used where configured.

Do not silence warnings globally without reason.

---

# 59. Warnings

Treat meaningful compiler/linter warnings seriously.

Do not chase harmless warnings at the cost of core delivery.

---

# 60. Build Health

Committed code should build.

Do not leave intentionally broken intermediate state on main.

---

# 61. Documentation Sync

Behavior change may require updating:

```text
04_Business_Rules.md
05_Database.md
06_API.md
07_Android.md
08_Backend.md
TASKS.md
```

---

# 62. AI-Generated Code

Must be reviewed.

Do not accept:

- duplicate models
- imaginary endpoints
- old framework APIs
- overengineered abstractions
- insecure shortcuts

---

# 63. Code Explanation Standard

Important unfamiliar code should remain explainable.

The developer should be able to answer:

```text
why is this class here?
why this pattern?
what is authoritative?
what happens on failure?
```

---

# 64. Pull Request Quality

If PRs are used, describe:

- problem
- solution
- tests
- screenshots/evidence
- migrations
- risks

---

# 65. Final Coding Checklist

```text
[ ] names express domain intent
[ ] no duplicated business rule
[ ] no magic business constants scattered
[ ] null/missing semantics correct
[ ] errors handled
[ ] security respected
[ ] tests meaningful
[ ] no dead/debug code
[ ] docs synchronized
[ ] build passes
```

---

# End of 11_Coding_Standards.md
