# AGENTS.md

> **FactoryFlow — Project Constitution**
>
> Version: 1.0
>
> Status: Living Document
>
> This document is the constitutional foundation of the FactoryFlow project.
>
> Every engineer, AI agent, contributor or automation tool must read and fully understand this document before making any modification to the project.
>
> AGENTS.md has the highest priority among all project documents.
>
> If another document contradicts AGENTS.md, this document always takes precedence.

---

# 1. Project Identity

## Project Name

**FactoryFlow**

## Official Project Title

**FactoryFlow: A Mobile Platform for Intelligent Industrial Maintenance KPI Collection and Automated Reporting**

## Project Type

Enterprise Mobile + Backend Platform

## Domain

Industrial Maintenance

Industrial Digitalization

Maintenance KPI Management

Business Process Automation

Industrial Reporting

## Repository Objective

FactoryFlow is intended to become a portfolio-quality software engineering project demonstrating the complete design, architecture, implementation, testing and documentation of a modern enterprise application.

The project is intentionally developed following professional software engineering practices rather than academic shortcuts.

The objective is to produce software that could realistically be deployed inside an industrial company after additional security and infrastructure hardening.

---

# 2. Mission

Industrial maintenance engineers receive operational indicators every day from numerous WhatsApp groups.

These indicators are produced manually by different operators.

Every operator has different writing habits.

Different abbreviations.

Different separators.

Different decimal formats.

Different units.

Different message layouts.

Some values are omitted.

Some labels contain spelling mistakes.

Some reports are partially completed.

As a consequence, maintenance engineers spend a considerable amount of time manually reading, copying, verifying and restructuring information before producing Excel reports.

This process is repetitive.

Time-consuming.

Error-prone.

Low-value.

FactoryFlow exists to eliminate this repetitive manual work while preserving complete human control over the validation process.

The platform centralizes KPI acquisition from multiple sources, extracts structured information, validates every value through an interactive confirmation workflow and automatically generates professional industrial reports.

FactoryFlow does **not** replace the engineer.

FactoryFlow assists the engineer.

The engineer always remains responsible for validating data before it becomes official.

Human validation is therefore a core business principle rather than an optional feature.

---

# 3. Vision

FactoryFlow is not being developed as a university assignment.

It is being developed as if it were a real commercial software product.

Every architectural decision must reflect professional software engineering standards.

Every feature should be maintainable.

Every module should be reusable.

Every interface should be intuitive.

Every interaction should feel polished.

The long-term vision is to build a mobile platform capable of serving industrial maintenance teams by reducing repetitive reporting tasks while improving reliability, traceability and reporting quality.

Although the first version targets a limited number of maintenance engineers, the architecture must remain scalable enough to support larger industrial deployments in the future.

---

# 4. Scope

FactoryFlow is responsible for the complete lifecycle of industrial maintenance KPI reporting.

This lifecycle includes:

- Collecting raw KPI information
- Parsing heterogeneous text messages
- Importing screenshots
- Performing OCR through the authenticated backend and private PaddleOCR runtime
- Accepting manual KPI entry
- Receiving shared images through Android Share Intent
- Validating extracted values
- Managing configurable KPI definitions
- Tracking report completion
- Generating Excel reports
- Generating PDF reports
- Scheduling automatic report generation
- Sending scheduled reports by email
- Allowing users to share reports using Android native sharing
- Displaying dashboards and KPIs
- Maintaining complete report history
- Logging user modifications
- Sending notifications
- Using stable report layouts; configurable templates are a post-MVP enhancement

Anything outside this lifecycle is considered outside the scope of FactoryFlow.

---

# 5. What FactoryFlow IS

FactoryFlow is:

- an Android application
- a Spring Boot backend
- a PostgreSQL-based information system
- a KPI validation platform
- an industrial reporting platform
- a configurable parser
- a dashboard platform
- a report generation system
- a scheduling platform
- a document generation platform
- an engineering portfolio project
- a demonstration of enterprise architecture
- a demonstration of modern Android development
- a demonstration of professional backend engineering
- a showcase of clean software architecture

---

# 6. What FactoryFlow IS NOT

FactoryFlow is NOT:

- a CRUD application
- an OCR application
- a WhatsApp clone
- a chatbot
- a predictive maintenance platform
- a machine learning project
- an artificial intelligence project
- a data warehouse
- a BI platform
- a prototype
- a school assignment
- a toy application
- a quick demonstration
- a collection of disconnected features

Every feature added to the project must reinforce the central objective of intelligent KPI collection and reporting.

Features that do not contribute to this objective should not be implemented.

---

# 7. Intended Users

The primary user is:

**Maintenance Engineer**

The current version of FactoryFlow intentionally supports a single user role.

There are no administrators.

There are no supervisors.

There are no operators.

Every authenticated user is considered a Maintenance Engineer responsible for validating and generating industrial reports.

Future versions may introduce additional roles if required by industrial deployments.

However, the first production-ready version must remain focused on a single-role workflow to maximize usability and minimize unnecessary complexity.

---

# 8. Business Problem

The business problem solved by FactoryFlow is not OCR.

The business problem is not Excel generation.

The business problem is not PDF generation.

The real business problem is the fragmentation of industrial KPI information.

Different people produce information differently.

Different reports contain different fields.

Different naming conventions are used.

Information arrives in an unstructured form.

FactoryFlow transforms heterogeneous operational information into standardized industrial reports.

The parser is therefore a means to solve the problem—not the objective itself.

---

# 9. Core Principles

FactoryFlow is built around five non-negotiable principles.

## 9.1 Human Validation First

Automation accelerates work.

Humans validate work.

No automatically extracted KPI should ever become official without explicit human confirmation.

This principle has absolute priority.

---

## 9.2 Deterministic Processing

Business data requires reliability.

Large Language Models are probabilistic.

KPI extraction must therefore remain deterministic.

Artificial Intelligence is intentionally excluded from the parsing pipeline.

Future AI integrations must never replace deterministic KPI extraction.

---

## 9.3 Enterprise Quality

Every line of code should be written as if the software were going to be deployed inside a real industrial company.

Shortcuts that reduce maintainability are forbidden.

---

## 9.4 Scalability by Design

Even though the application initially targets a limited number of users, every architectural decision should support future growth.

Scalability should emerge naturally from good architecture—not from premature optimization.

---

## 9.5 Premium User Experience

FactoryFlow must never feel like an academic project.

Animations should feel intentional.

Interactions should feel smooth.

Navigation should feel natural.

Visual hierarchy should be clean.

Every screen should look like a professional commercial application.

User experience is considered part of software quality.

---

# 10. Success Criteria

FactoryFlow will be considered successful when:

- KPI collection becomes significantly faster than manual workflows.
- Reports can be generated with minimal user effort.
- Human validation guarantees reporting accuracy.
- The application demonstrates professional software architecture.
- The project serves as a flagship portfolio project.
- The codebase remains maintainable and extensible.
- The documentation is complete enough for another engineer—or an AI coding agent—to continue development without ambiguity.

---

---

# 11. Engineering Constitution

The following rules define the engineering constitution of FactoryFlow.

These rules are mandatory.

They are not recommendations.

Every implementation must respect them without exception.

Whenever there is uncertainty, the engineer or AI agent must choose the solution that best aligns with these principles.

---

# 11.1 Software Engineering Standards

FactoryFlow must always be developed according to modern software engineering practices.

Every decision should prioritize:

- Maintainability
- Readability
- Scalability
- Testability
- Extensibility
- Reliability
- Performance
- Professional quality

The project should demonstrate software engineering maturity rather than implementation speed.

Code that works but violates architectural principles is considered incorrect.

---

# 11.2 Code Quality Philosophy

The objective is not to write code.

The objective is to build software.

Every class must have a clear responsibility.

Every package must have a purpose.

Every dependency must be justified.

Every feature should integrate naturally into the existing architecture.

Whenever possible, code should be self-explanatory.

Comments should explain **why**, never **what**.

Good naming is preferred over excessive comments.

---

# 11.3 Mandatory Engineering Principles

Every implementation must respect the following principles.

## SOLID

The complete project must follow SOLID principles.

- Single Responsibility Principle
- Open / Closed Principle
- Liskov Substitution Principle
- Interface Segregation Principle
- Dependency Inversion Principle

Whenever an implementation violates SOLID, the implementation must be redesigned.

---

## DRY

Never duplicate business logic.

If logic is repeated more than once, evaluate whether it should become a reusable component.

---

## KISS

Prefer simple solutions over clever ones.

Complexity is only acceptable when it solves a real engineering problem.

---

## Separation of Concerns

Each layer has a single responsibility.

Presentation must never contain business logic.

Business logic must never depend on infrastructure.

Infrastructure must never dictate business decisions.

---

## Composition over Inheritance

Inheritance should only be used when there is a true "is-a" relationship.

Otherwise prefer composition.

---

## Interface First

Whenever appropriate, expose behavior through interfaces.

Concrete implementations should remain replaceable.

---

# 11.4 Definition of Professional Code

Professional code is:

- easy to understand
- easy to maintain
- easy to test
- easy to extend
- predictable
- consistent
- modular

Professional code is NOT:

- over-engineered
- clever
- unnecessarily abstract
- excessively generic
- tightly coupled

---

# 12. Architecture Constitution

FactoryFlow follows Clean Architecture.

The architecture itself is considered a business asset.

It must never be simplified for convenience.

Every new feature must integrate into the existing architecture rather than bypass it.

---

# 12.1 Architectural Layers

The project is divided into clearly separated layers.

Android Presentation

↓

Application Layer

↓

Domain Layer

↓

Infrastructure Layer

↓

Database

Dependencies always point inward.

Outer layers may depend on inner layers.

Inner layers must never depend on outer layers.

---

# 12.2 Layer Responsibilities

## Presentation Layer

Responsible only for:

- Screens
- Navigation
- ViewModels
- UI State
- User interaction

Forbidden:

- SQL
- Business rules
- Parsing logic
- Report generation
- KPI validation

---

## Domain Layer

Responsible for:

- Business rules
- Entities
- Use Cases
- Domain Services

The Domain Layer must remain independent from frameworks.

---

## Application Layer

Responsible for:

- Workflow orchestration
- Coordination
- Transactions
- Scheduling coordination

---

## Infrastructure Layer

Responsible for:

- PostgreSQL
- File generation
- Email
- RabbitMQ
- Quartz
- Firebase
- External APIs

Infrastructure must never contain business rules.

---

# 13. Technology Constitution

The following technologies are officially adopted.

Changing them requires explicit approval.

---

## Android

- Kotlin
- Jetpack Compose
- Material Design 3
- MVVM
- Hilt
- Room (offline cache if required)
- Navigation Compose
- Coroutines
- Flow

The Android UI language is professional French. Every user-facing string must live
in Android string resources. Code identifiers, API/database contracts, packages,
Git history, and technical documentation remain in English.

---

## Backend

- A currently supported Java LTS compatible with the selected Spring Boot version
- Java 21 is the preferred current implementation choice, not a business invariant
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven

---

## Reporting

Excel

Apache POI

PDF

Apache PDFBox

---

## Scheduling

Quartz Scheduler

---

## Messaging

RabbitMQ is a COULD-level late enhancement.

If implemented, it is used exclusively for asynchronous processing.

Never used for synchronous communication.

---

## Notifications

Firebase Cloud Messaging is the selected SHOULD-level push mechanism.

---

## OCR

PaddleOCR PP-OCRv5 through the private backend OCR runtime.

Android uploads gallery and shared images to the authenticated backend OCR endpoint.

Direct CameraX acquisition and on-device ML Kit OCR are not part of the current implementation.

---

## Email

JavaMailSender

Used exclusively for automatic scheduled report delivery.

---

## User Sharing

Android Share Intent

Used whenever the user manually chooses to share a generated report.

This feature does NOT require backend email services.

---

# 14. Mandatory Design Patterns

Whenever appropriate, the following patterns should be used.

- Repository
- Service
- Strategy
- Factory
- Builder
- Dependency Injection
- Observer
- Command (when applicable)

Patterns should never be added only because they are fashionable.

Every pattern must solve a real problem.

---

# 15. Forbidden Practices

The following practices are strictly prohibited.

❌ Massive God Classes

❌ Business logic inside Controllers

❌ SQL inside UI

❌ Duplicate business logic

❌ Static utility classes replacing proper services

❌ Circular dependencies

❌ Hardcoded configuration

❌ Hardcoded credentials

❌ Magic numbers

❌ Deep inheritance hierarchies

❌ Copy-paste programming

❌ Temporary hacks committed to Git

❌ Ignoring warnings

❌ Dead code

❌ Commented-out code

❌ TODOs left indefinitely

---

# 16. Performance Philosophy

Performance matters.

However, readability comes first.

Premature optimization is discouraged.

Optimization should only occur when:

- a measurable bottleneck exists
- profiling confirms the issue
- the optimization remains understandable

Readable software that is fast enough is preferred over unreadable software that is slightly faster.

---

# 17. Security Philosophy

Security is part of software quality.

Every feature must be developed assuming that production deployment is possible.

The application must never expose:

- credentials
- secrets
- API keys
- internal configuration
- stack traces
- database passwords

JWT authentication must remain secure.

Sensitive information must never be logged.

---

# 18. Documentation Philosophy

Documentation is considered part of the software.

Undocumented architecture is incomplete architecture.

Every important engineering decision should be reflected somewhere inside the documentation.

Documentation must evolve together with the codebase.

If architecture changes, documentation must change before the feature is considered complete.

---

---

# 19. AI Agent Operating Rules

FactoryFlow is expected to be developed with the assistance of AI coding agents such as Codex.

AI agents are implementation partners.

They are not product owners.

They are not architects unless explicitly asked to perform an architectural review.

They must never silently redefine the project.

The project documentation is the source of truth.

---

# 19.1 Mandatory Behavior Before Any Task

Before modifying code, an AI agent must:

1. Read `AGENTS.md`.
2. Read the active section of `TASKS.md`.
3. Read `SKILLS.md`.
4. Read `DESIGN.md` when the task affects UI, UX, interaction, visuals, Compose, charts, animations, accessibility, typography, spacing, components, or screen behavior.
5. Read `UI_UX.md` when the task affects any Android screen or user flow.
6. Read the relevant document inside `/docs`.
7. Inspect the existing code related to the task.
8. Identify the smallest coherent implementation scope.
9. Verify that the requested task has not already been implemented.
10. Only then begin implementation.

Never start coding from the user prompt alone when project documentation exists.

---

# 19.2 Source-of-Truth Hierarchy

When information conflicts, use this priority order:

1. `AGENTS.md`
2. Explicit current-user instruction
3. `TASKS.md`
4. `SKILLS.md`
5. `DESIGN.md`
6. `UI_UX.md`
7. Relevant `/docs/*.md`
8. Existing implementation
9. AI assumptions

The AI must never allow an old implementation to silently override a newer approved specification.

If a contradiction remains unresolved, stop and ask.

---

# 19.3 Never Guess Important Requirements

An AI agent must stop and request clarification when uncertainty could materially change:

- database structure
- public API contracts
- security behavior
- authentication
- persistence rules
- parser interpretation
- KPI validation behavior
- report content
- scheduling
- email delivery
- WebSocket behavior
- destructive operations
- data migrations
- user-visible behavior

Minor implementation choices that preserve the documented behavior may be made autonomously.

---

# 19.4 Never Invent Scope

AI agents must not introduce features simply because they are technically interesting.

Do not introduce:

- extra user roles
- iOS support
- web frontend
- additional databases
- microservices
- Kubernetes
- MinIO
- Spring Batch
- AI-based KPI extraction
- new message brokers
- new authentication mechanisms
- additional cloud infrastructure

unless the project documentation is intentionally amended first.

FactoryFlow must remain ambitious without becoming uncontrolled.

---

# 19.5 Never Remove Scope Silently

An AI agent must never solve schedule pressure by silently removing:

- Dashboard
- KPI validation
- manual entry
- text paste
- gallery import
- WhatsApp Share Intent
- camera acquisition
- OCR
- Excel generation
- PDF generation
- report history
- search/filtering
- scheduling
- real-time functionality
- notifications
- report sharing
- automatic scheduled email
- statistics

If a task must be postponed, mark it explicitly in `TASKS.md`.

Never pretend a postponed requirement was completed.

---

# 19.6 Do Not Rewrite Working Systems Without Reason

Before refactoring working code, establish:

- what problem exists
- why the current implementation is insufficient
- what measurable improvement the refactor provides
- what tests protect behavior during the change

Avoid large speculative rewrites.

Prefer incremental improvement.

---

# 19.7 Code Generation Policy

AI-generated code must be treated as production code.

Before code is considered accepted, it must be:

- syntactically correct
- integrated with the existing architecture
- compilable
- tested where applicable
- readable
- consistent with project naming
- free from unnecessary duplication
- documented where reasoning is non-obvious

Do not generate placeholder implementations unless the active task explicitly calls for scaffolding.

---

# 19.8 Comment Policy

Prefer self-documenting code.

Do not fill source files with obvious comments.

Bad:

```java
// Save report
reportRepository.save(report);
```

Good comments explain architectural or business reasoning.

Example:

```java
// Extraction is intentionally not persisted here.
// Every parsed KPI must pass through explicit human confirmation first.
```

Comments are especially appropriate for:

- parser edge cases
- zero-error validation rules
- unusual Android lifecycle behavior
- asynchronous workflows
- scheduling decisions
- security-sensitive code
- non-obvious workarounds

Do not add comments merely to increase documentation volume.

---

# 20. Development Session Protocol

Every development session must have a clearly defined objective.

A session should never begin with:

> "Continue FactoryFlow."

Instead, identify the active task from `TASKS.md`.

Examples:

- Implement JWT login endpoint
- Implement KPI fuzzy matching
- Build dashboard KPI cards
- Integrate Android Share Intent
- Generate Excel workbook
- Add Quartz daily schedule

---

# 20.1 Session Start

At the beginning of a session:

1. Read this file.
2. Inspect Git status.
3. Read the current milestone in `TASKS.md`.
4. Identify the first incomplete task.
5. Read relevant documentation.
6. Inspect existing implementation.
7. State the intended implementation scope internally.
8. Begin only that scope.

---

# 20.2 One Task at a Time

A task should represent one coherent engineering change.

Do not implement unrelated functionality in the same task.

Example:

Good:

```text
Implement KPI parser numeric normalization.
```

Bad:

```text
Implement parser, dashboard, authentication, PDF export and notifications.
```

Small task boundaries improve:

- debugging
- reviews
- Git history
- rollback
- testing
- documentation
- learning

---

# 20.3 Implementation Order

For most features, prefer:

```text
Understand requirement
        ↓
Inspect current architecture
        ↓
Define domain behavior
        ↓
Define API/data contract if required
        ↓
Implement backend/domain behavior
        ↓
Add persistence/infrastructure
        ↓
Implement Android integration
        ↓
Implement UI
        ↓
Test
        ↓
Update documentation
        ↓
Update TASKS.md
        ↓
Commit
```

Not every task requires every step.

Do not manufacture unnecessary layers.

---

# 20.4 Backend and Android Must Evolve Together

FactoryFlow is a single product with two codebases.

The backend is not "finished first" and forgotten.

The Android app is not an isolated visual shell.

The API contract connects both.

When a backend feature is intended for Android:

1. Define or confirm the API contract.
2. Implement backend behavior.
3. Verify with Swagger or automated tests.
4. Implement Retrofit contract.
5. Connect Repository/ViewModel.
6. Implement screen behavior.
7. Test the entire flow end-to-end.

Do not fake frontend data once the real backend contract exists.

Mock data may only be used temporarily for isolated UI construction and must be removed before the feature is marked complete.

---

# 20.5 Running the Two Applications

The backend and Android application use different development environments but share the same repository.

Recommended repository structure:

```text
FactoryFlow/
├── AGENTS.md
├── TASKS.md
├── SKILLS.md
├── DESIGN.md
├── UI_UX.md
├── README.md
├── backend/
├── android/
├── docs/
├── report/
└── assets/
```

The Spring Boot backend may be run from:

- Codex-supported terminal
- IntelliJ IDEA
- command line

The Android application is normally built, previewed, debugged and run through Android Studio.

Codex may edit both `backend/` and `android/`.

Android Studio is the execution/debugging environment for the mobile app, not a separate source of truth.

---

# 20.6 Local API Connectivity

Android must never assume `localhost` refers to the development computer.

When running on the Android Emulator, the host machine is typically accessed through the emulator host address rather than `localhost`.

The environment-specific API URL must be configurable.

Never scatter development URLs throughout source code.

Use environment/build configuration.

The final mechanism must be documented in `docs/07_Android.md`.

---

# 21. Task Tracking Constitution

`TASKS.md` is the living execution roadmap.

It must accurately reflect project reality.

It is not a wish list.

---

# 21.1 Task States

Every task should use one of these states:

```text
[ ] Not Started
[-] In Progress
[x] Completed
[!] Blocked
[~] Deferred
```

Do not mark a task complete because code was generated.

A task becomes complete only after its Definition of Done is satisfied.

---

# 21.2 Task Granularity

Tasks should be small enough to:

- understand independently
- implement independently
- test independently
- commit independently

A task may contain subtasks when necessary.

Example:

```text
[ ] Implement authentication
    [ ] Create login DTOs
    [ ] Configure Spring Security
    [ ] Implement JWT generation
    [ ] Implement refresh token flow
    [ ] Add auth integration tests
    [ ] Connect Android Retrofit login
    [ ] Implement Android login state
```

---

# 21.3 Task Documentation

For important tasks, `TASKS.md` should preserve:

- objective
- relevant docs
- implementation notes
- test expectations
- expected Git commit
- report evidence to capture

This keeps development, GitHub and the academic report synchronized.

---

# 22. Definition of Done

"Code exists" does not mean "done."

A feature is complete only when all applicable conditions below are satisfied.

---

## 22.1 Functional

- Requirement implemented
- Expected happy path works
- Relevant edge cases handled
- Errors handled
- User cannot accidentally bypass required validation
- No placeholder behavior remains

---

## 22.2 Backend

When applicable:

- Controller contract implemented
- Service/domain logic implemented
- Persistence implemented
- Validation implemented
- Errors mapped properly
- Swagger reflects the endpoint
- Tests pass

---

## 22.3 Android

When applicable:

- Screen implemented
- ViewModel state implemented
- Repository/API integration implemented
- Loading state implemented
- Empty state implemented
- Error state implemented
- Success state implemented
- Back navigation behaves correctly
- Configuration changes do not corrupt state
- Relevant accessibility behavior included

---

## 22.4 UI / UX

When applicable:

- `DESIGN.md` respected
- `UI_UX.md` respected
- Visual hierarchy is clear
- Spacing is consistent
- Typography is consistent
- No accidental default-looking Compose components
- Interaction feedback exists
- Touch targets are appropriate
- Animations serve a purpose
- Light/dark behavior is correct if supported

---

## 22.5 Quality

- No compilation errors
- No new unexplained warnings
- No dead code
- No debug leftovers
- No secrets committed
- No duplicated business logic
- Naming follows conventions
- Code is understandable

---

## 22.6 Testing

- Relevant automated tests added
- Existing tests still pass
- Manual test completed where required
- Edge case behavior checked

---

## 22.7 Documentation

- Relevant documentation updated
- `TASKS.md` updated
- API docs updated if contract changed
- Database docs updated if schema changed
- Architecture docs updated if structure changed

---

## 22.8 Portfolio / Report Evidence

For visually or architecturally important features:

- Capture a useful screenshot if relevant
- Record important engineering decisions
- Record measurable results if applicable
- Add report hint/evidence to the appropriate documentation

Examples:

- OCR confirmation screen screenshot
- Grafana dashboard screenshot
- k6 result
- generated Excel workbook
- generated PDF
- WebSocket live update demonstration
- Swagger endpoint screenshot
- architecture diagram

---

## 22.9 Git

- Changes reviewed
- Git status inspected
- Commit contains only the intended task
- Commit message follows project convention

Only after these checks may the task be marked `[x]`.

---

# 23. Git Constitution

Git history is part of the portfolio.

A recruiter reviewing the repository should be able to understand how FactoryFlow evolved.

---

# 23.1 Commit Frequency

Commit after each coherent completed subtask.

Do not wait until the end of a day to create one massive commit.

Do not commit broken intermediate states unless explicitly working on a dedicated branch where that behavior is intentional.

---

# 23.2 Conventional Commits

Use Conventional Commits.

Preferred types:

```text
feat
fix
refactor
test
docs
style
perf
build
ci
chore
```

Examples:

```text
feat(auth): implement JWT access-token login

feat(parser): add fuzzy KPI label matching

feat(android): add report acquisition method selector

feat(ocr): integrate PaddleOCR through the backend provider boundary

feat(report): generate Excel workbook with Apache POI

feat(pdf): add manager-facing PDF report generation

feat(scheduler): add Quartz daily report generation

feat(realtime): broadcast report updates over STOMP

test(parser): cover decimal and separator variations

docs(api): document report confirmation workflow
```

---

# 23.3 Forbidden Commit Messages

Do not use:

```text
update
changes
final
final version
fix
stuff
work
test
new
done
latest
version 2
hhhh
```

Commit history must communicate intent.

---

# 23.4 Commit Scope

One commit should represent one coherent change.

Do not mix:

- UI redesign
- database migration
- parser changes
- authentication changes

into one commit unless they are inseparable parts of a single feature.

---

# 23.5 AI and Git

An AI agent may prepare or suggest commits.

Before committing, it must:

1. inspect `git diff`
2. inspect `git status`
3. ensure unrelated files are not accidentally included
4. ensure secrets are absent
5. ensure generated/binary files that should be ignored are not tracked
6. use the expected commit message from `TASKS.md` when one exists

Never force-push unless explicitly instructed by the user.

Never rewrite shared history casually.

---

# 24. Documentation Reading Order

At the beginning of work, documentation should be consulted in this order:

```text
1. AGENTS.md
2. TASKS.md
3. SKILLS.md
4. DESIGN.md
5. UI_UX.md
6. docs/01_Project_Vision.md
7. docs/03_Architecture.md
8. docs/04_Business_Rules.md
9. docs/05_Database.md
10. docs/06_API.md
11. docs/07_Android.md
12. docs/08_Backend.md
13. docs/09_Report_Guide.md
14. docs/10_Git_Strategy.md
15. docs/11_Coding_Standards.md
16. docs/12_Roadmap.md
17. docs/13_UML.md
```

Not every document needs to be reread in full before every small change.

The agent must always read the top-level operating documents and then read the technical documents relevant to the active task.

---

# 25. Documentation Ownership

Each document has one responsibility.

## `AGENTS.md`

Defines how contributors and AI agents must behave.

It is the constitution.

---

## `TASKS.md`

Defines what remains to be built and what is currently active.

It is the execution plan.

---

## `SKILLS.md`

Defines implementation standards, preferred patterns, engineering expectations and technology-specific practices.

It answers:

> How should this be engineered?

---

## `DESIGN.md`

Defines the FactoryFlow visual language.

It contains:

- visual philosophy
- inspiration sources
- colors
- typography
- spacing
- shapes
- motion
- icons
- charts
- component design principles
- accessibility rules

Apple, Mastercard and other design references belong here.

---

## `UI_UX.md`

Defines actual FactoryFlow product behavior.

It contains:

- every screen
- every flow
- every state
- every interaction
- every loading behavior
- every empty state
- every error state
- every transition
- navigation behavior
- confirmation behavior
- dashboard experience
- report experience
- OCR experience
- scheduling experience

---

## `/docs`

Contains detailed technical and product specifications.

Do not duplicate entire sections across multiple documents.

Reference the authoritative document instead.

---

# 26. Documentation Update Rule

A feature that changes documented behavior must update its documentation in the same task.

Examples:

Database schema changes:

→ update `docs/05_Database.md`

REST contract changes:

→ update `docs/06_API.md`

Android architecture changes:

→ update `docs/07_Android.md`

Backend architecture changes:

→ update `docs/08_Backend.md`

Visual design changes:

→ update `DESIGN.md`

Screen behavior changes:

→ update `UI_UX.md`

Project scope changes:

→ review `AGENTS.md`, `TASKS.md`, Project Vision and Roadmap

Documentation lag is considered technical debt and should not be normalized.

---

# 27. Learning While Building

The project is developed under a real time constraint.

The developer is intentionally learning Spring Boot and native Android while building FactoryFlow.

Therefore AI agents should optimize for two objectives simultaneously:

1. Deliver the application efficiently.
2. Make the implementation understandable to the developer.

For important unfamiliar concepts, provide a short explanation before or alongside implementation.

Examples:

- Spring dependency injection
- JPA relationships
- Spring Security filter chain
- JWT access-token session flow
- Kotlin coroutines
- `StateFlow`
- Compose state
- Retrofit
- Room
- WebSocket/STOMP
- Quartz
- RabbitMQ
- Android Share Intent
- `FileProvider`
- backend `OcrProvider` / PaddleOCR boundary

Explanations should be concise and implementation-focused.

Do not turn every coding task into a lecture.

Do not hide important concepts behind unexplained generated code.

---

# 28. Preserve Report Material During Development

FactoryFlow will be discussed in an engineering internship report.

The report section is limited, therefore the strongest engineering decisions must be preserved while they happen.

For significant features, record:

- problem being solved
- selected solution
- why it was selected
- meaningful alternative rejected
- implementation challenge
- result
- useful screenshot/diagram
- technical concept worth explaining

Examples of report-worthy decisions include:

- deterministic parsing instead of LLM extraction
- human-in-the-loop confirmation
- five acquisition methods converging to one pipeline
- native Kotlin instead of Flutter because Android-only was confirmed
- PDFBox instead of iText due to licensing
- local storage abstraction instead of MinIO
- Quartz instead of Spring Batch
- RabbitMQ as deliberate asynchronous architecture learning
- WebSocket/STOMP for real-time synchronization
- private PaddleOCR recognition behind the backend provider boundary
- separate device-side manual email sharing from backend scheduled email delivery

Do not allow these engineering decisions to disappear from project history.

---

# 29. End-of-Task Protocol

After completing a task, the AI agent should perform the following sequence:

```text
Implementation complete
        ↓
Compile/build
        ↓
Run relevant tests
        ↓
Review diff
        ↓
Check architecture compliance
        ↓
Check design/UI compliance if applicable
        ↓
Update documentation
        ↓
Update TASKS.md
        ↓
Prepare focused Git commit
        ↓
Report concise completion summary
        ↓
Stop
```

Do not automatically start the next task unless explicitly asked or the user has instructed the agent to work through a predefined task batch.

---

# 30. End-of-Session Handoff

Before a development session ends, ensure the repository itself contains enough state to resume work later.

At minimum:

- `TASKS.md` reflects reality
- unfinished work is clearly marked
- blocked items include reason
- relevant docs are updated
- no important decision exists only inside chat context

When appropriate, add a short `Current Work` section to `TASKS.md` containing:

```text
Current milestone:
Current task:
Last completed task:
Next task:
Known blocker:
Relevant files:
```

The project repository, not conversational memory, must be sufficient to resume work.

---

# 31. AI Continuity Principle

No AI assistant should be expected to remember FactoryFlow forever.

The repository is the memory.

If a new Codex session begins with no prior conversational context, reading the project documents should be sufficient to understand:

- what FactoryFlow is
- why it exists
- what has been decided
- what has already been implemented
- what must not change
- what is currently being built
- what comes next

This is a deliberate project requirement.

---

---

# 32. Detailed Architecture Rules

This section defines the concrete architectural rules that every contributor and AI agent must respect when implementing FactoryFlow.

These rules apply to both the Spring Boot backend and the Android application.

The goal is not merely to keep the code organized.

The goal is to preserve a coherent, extensible, testable architecture throughout the entire life of the project.

---

# 32.1 Repository Structure

FactoryFlow is maintained as one repository containing both backend and Android code.

Recommended top-level structure:

```text
FactoryFlow/
├── AGENTS.md
├── TASKS.md
├── SKILLS.md
├── DESIGN.md
├── UI_UX.md
├── README.md
├── .gitignore
│
├── backend/
├── android/
│
├── docs/
├── report/
├── diagrams/
├── assets/
└── scripts/
```

The repository must remain understandable at first glance.

Do not create unnecessary top-level folders.

The original WhatsApp business-input screenshots live only in top-level `assets/`.
Do not move, modify, delete, or publicly expose them. Parser fixtures, report evidence,
GitHub, and portfolio material must use sanitized derivatives created later.

Do not hide core application code inside generic folders such as:

```text
misc/
stuff/
common2/
utils2/
temp/
old/
backup/
```

---

# 32.2 Backend Package Structure

The Spring Boot backend follows a feature-oriented modular-monolith package structure.

Recommended baseline:

```text
com.factoryflow
├── auth
├── report
├── kpi
├── parser
├── generation
├── schedule
├── notification
└── shared
```

Each feature may use internal `api/`, `application/`, `domain/`, and `persistence/`
packages where they add clarity. Do not force empty layers into trivial features.

Any structural change must preserve separation of concerns.

---

# 32.3 Backend Dependency Rule

Controllers may depend on services.

Services may depend on repositories and other services through well-defined interfaces.

Repositories depend on persistence infrastructure.

Entities must not depend on controllers, DTOs or presentation logic.

Controllers must never directly call repositories.

Bad:

```java
@RestController
class ReportController {
    private final ReportRepository repository;
}
```

Preferred:

```java
@RestController
class ReportController {
    private final ReportService reportService;
}
```

---

# 32.4 Controller Rules

Controllers are transport adapters.

They are responsible for:

- receiving HTTP requests
- validating request structure
- calling application services
- mapping responses
- returning appropriate HTTP status codes

Controllers must not contain:

- KPI parsing logic
- report generation logic
- scheduling logic
- business validation
- SQL
- JPA query logic
- file-system operations
- email logic
- RabbitMQ orchestration
- WebSocket business logic

Controllers should remain thin.

---

# 32.5 Service Rules

Services contain use-case orchestration and business behavior.

A service should have a clearly defined responsibility.

Examples:

```text
AuthenticationService
ReportService
ParserService
KPIValidationService
ExcelReportService
PdfReportService
StatisticsService
NotificationService
SchedulerService
StorageService
EmailService
```

Avoid creating one generic:

```text
FactoryFlowService
```

or

```text
CommonService
```

that accumulates unrelated behavior.

---

# 32.6 Repository Rules

Repositories are persistence abstractions.

They are responsible for:

- loading entities
- saving entities
- query operations
- database-specific retrieval

Repositories should not decide business outcomes.

Bad:

```text
ReportRepository.calculateWhetherReportIsValid()
```

Preferred:

```text
ReportRepository.findEntriesByReportId()
```

followed by validation inside the appropriate domain/service layer.

---

# 32.7 DTO Rules

DTOs define transport contracts.

They must be separate from persistent entities.

Do not expose JPA entities directly from REST endpoints.

DTOs may represent:

- requests
- responses
- parser output
- dashboard projections
- statistics
- report generation status

DTOs should contain data only.

No business logic.

---

# 32.8 Mapping

Use MapStruct for non-trivial mappings between:

```text
Entity ↔ DTO
```

Avoid large amounts of repetitive manual mapping.

Do not introduce MapStruct where a simple direct constructor is clearer.

Use judgment.

---

# 32.9 Domain Entities

Persistent domain entities must model the real business concepts.

Core examples include:

```text
User
MaintenanceReport
KPIEntry
KPIDefinition
GeneratedReport
Schedule
Notification
AuditLog
```

Entities must not become dumping grounds for unrelated helper behavior.

Business logic that naturally belongs to the entity may stay there.

Cross-aggregate workflows belong in services.

---

# 32.10 Enumerations

Use enums for finite business states.

Examples:

```text
ReportStatus
ValidationStatus
AcquisitionMethod
ReportFormat
ScheduleType
NotificationType
```

Do not represent stable finite states using arbitrary strings.

Bad:

```java
String status;
```

Preferred:

```java
ReportStatus status;
```

Persist enums safely and explicitly.

---

# 33. Database Constitution

PostgreSQL is the authoritative persistent datastore.

The database should model business reality rather than mirror UI screens.

---

# 33.1 Core Data Model

The baseline domain includes:

```text
users
kpi_definitions
maintenance_reports
kpi_entries
generated_reports
notifications
schedules
audit_log
```

Additional tables may be introduced only when justified.

---

# 33.2 KPI Catalog

KPI names must not be hardcoded into parser logic.

The database-backed KPI catalog must support configuration such as:

- canonical name
- display name
- unit
- category
- aliases
- plausible minimum
- plausible maximum
- active status

The parser must use this catalog as its reference.

This is a non-negotiable design rule.

---

# 33.3 Partial Reports

The system must naturally support partial reports.

A MaintenanceReport does not need to contain every known KPI.

Only detected or manually entered KPI values should produce `KPIEntry` records.

Do not model every KPI as a fixed column in `maintenance_reports`.

That would make the schema brittle and incompatible with the real data.

---

# 33.4 Raw Input Preservation

The original source text must always be preserved.

For pasted messages:

```text
store original pasted text
```

For OCR-based input:

```text
store OCR-extracted text
```

Never overwrite the original source when values are corrected.

The original source is part of the audit trail.

---

# 33.5 Validation State

The persisted `MaintenanceReport` lifecycle is exactly:

- `DRAFT`
- `PENDING_REVIEW`
- `CONFIRMED`

`ARCHIVED` must not be introduced unless a later approved implementation genuinely needs it.
Missing/generated/combined dashboard indicators are projections, not persisted report states.

Do not collapse all report lifecycle states into a single boolean.

---

# 33.6 Auditability

Important modifications must be traceable.

Where appropriate, capture:

- who changed the value
- when
- old value
- new value
- reason if provided

The audit system should support investigation, not merely produce logs.

---

# 33.7 Database Migrations

Use Flyway.

Every schema change must be represented by a versioned migration.

Do not manually modify production-like schemas without a corresponding migration.

Examples:

```text
V1__create_initial_schema.sql
V2__add_kpi_aliases.sql
V3__add_report_schedule.sql
```

Never edit an already-applied migration merely to change history.

Create a new migration.

---

# 33.8 Constraints

Use database constraints where they protect data integrity.

Examples:

- NOT NULL where truly required
- foreign keys
- unique email
- unique KPI code where appropriate
- valid report relationships

Application validation complements database constraints.

It does not replace them.

---

# 33.9 Indexes

Create indexes based on real query patterns.

Likely candidates include:

- report date
- submitted_by
- KPI definition
- generated report date
- status

Do not add indexes blindly to every column.

---

# 34. API Constitution

The REST API is the contract between Android and Spring Boot.

Treat the API as a stable product surface.

---

# 34.1 REST Principles

Use nouns for resources.

Preferred:

```text
/api/reports
/api/reports/{id}
/api/kpi-definitions
/api/notifications
```

Avoid RPC-like naming unless the operation genuinely represents an action.

Report creation and generation actions use the canonical resource contracts:

```text
POST /api/reports/analyze
POST /api/reports/drafts
POST /api/reports/{id}/confirm
POST /api/generated-reports
```

`POST /api/reports/analyze` is side-effect-free. Period-based generated documents may
aggregate several maintenance reports, so report-specific generation routes are not production contracts.

---

# 34.2 HTTP Methods

Use methods consistently.

```text
GET     retrieve
POST    create / trigger
PUT     full replacement where appropriate
PATCH   partial update
DELETE  delete
```

Do not use POST for every action merely because it is easy.

---

# 34.3 HTTP Status Codes

Use meaningful status codes.

Examples:

```text
200 OK
201 Created
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
422 Unprocessable Entity
500 Internal Server Error
```

Do not return `200 OK` for every failure.

---

# 34.4 Error Contract

The API should return a consistent error structure.

Example concept:

```json
{
  "timestamp": "...",
  "status": 400,
  "code": "REPORT_VALIDATION_FAILED",
  "message": "One or more KPI values require confirmation.",
  "details": []
}
```

Exact format belongs in `docs/06_API.md`.

---

# 34.5 Validation

Server-side validation is mandatory.

Android validation improves UX.

It does not provide security.

Never trust client input.

---

# 34.6 Pagination

Any endpoint that can grow significantly should support pagination where justified.

Examples:

- report history
- audit log
- notifications

Do not paginate tiny configuration lists unnecessarily.

---

# 34.7 Filtering

History/report APIs should support useful filters such as:

- date range
- report status
- report type
- submitting engineer
- KPI

Filtering should be explicit and documented.

---

# 34.8 OpenAPI

Every public endpoint must be visible in Swagger/OpenAPI.

Document:

- purpose
- parameters
- request body
- response
- status codes
- validation errors

Swagger is not optional documentation.

---

# 35. Authentication and Security Rules

FactoryFlow currently uses short-lived JWT access-token authentication.

Refresh-token rotation is not implemented and must not be described as current behavior.

---

# 35.1 Authentication

The implemented authentication flow is:

```text
Login
    ↓
Access Token
    ↓
Authenticated API Requests
```

When the access token expires, Android clears the session and requires login again.
A future refresh-token design would require an explicit documented contract and tests.

---

# 35.2 Passwords

Passwords must be hashed using BCrypt.

Never store plaintext passwords.

Never log passwords.

Never return password hashes through APIs.

---

# 35.3 Token Storage

Android token storage must use an appropriately secure mechanism.

Do not store sensitive authentication tokens casually in plaintext preferences.

The final approach must be documented in `docs/07_Android.md`.

---

# 35.4 Authorization

Current version:

```text
One authenticated role:
Maintenance Engineer
```

Do not add fake role complexity.

Authentication is mandatory.

Fine-grained RBAC is intentionally out of scope unless future requirements change.

---

# 35.5 Secrets

Never commit:

- SMTP passwords
- Firebase secrets
- JWT signing secrets
- database passwords
- API keys

Use environment variables, secure configuration or developer-local secret mechanisms.

Provide safe example configuration files where needed.

---

# 36. KPI Acquisition Architecture

FactoryFlow supports multiple input methods that converge into one processing pipeline.

The acquisition method must not alter downstream business rules.

---

# 36.1 Supported Acquisition Methods

The approved input modes are:

1. Manual entry
2. Paste text
3. Import image from gallery
4. Share image directly from another Android app such as WhatsApp
5. Capture an image using the device camera

Do not invent separate parser implementations per input source.

---

# 36.2 Unified Pipeline

All input should converge conceptually to:

```text
Source
   ↓
Image? ── yes ──> OCR
   │
   no
   ↓
Raw Text
   ↓
Normalization
   ↓
Label Matching
   ↓
Value Extraction
   ↓
Confidence Scoring
   ↓
Human Confirmation
   ↓
Persistence
```

This unified pipeline is a core architectural decision.

---

# 36.3 Manual Entry

Manual entry bypasses OCR and parsing where appropriate.

However, it must still pass through:

- validation
- confirmation
- persistence rules

Manual input is not a privileged path that can bypass integrity controls.

---

# 37. OCR Constitution

OCR is performed by the private PaddleOCR runtime behind the Spring Boot `OcrProvider`
boundary. Images are accepted from Android gallery and Share Intent workflows through
the authenticated OCR endpoint.

---

# 37.1 OCR Responsibilities

Android is responsible for:

- selecting or receiving image
- uploading the supported image to the authenticated OCR endpoint
- receiving the extracted text and OCR metadata
- forwarding extracted text into the same backend parsing flow

The OCR layer must not decide KPI business meaning.

It only extracts text.

---

# 37.2 OCR Failure

If OCR fails:

- display a clear error
- allow retry
- allow manual correction
- allow fallback to manual entry

Never silently produce an empty report.

---

# 38. Parser Constitution

The parser is one of the most technically important parts of FactoryFlow.

It must prioritize explainability and robustness.

---

# 38.1 Parser Philosophy

The parser is deterministic.

The parser is best-effort.

The parser is not trusted blindly.

Human confirmation remains mandatory.

---

# 38.2 Real-World Input Variability

The parser must tolerate:

- `:`
- `=`
- `->`
- whitespace-separated values
- decimal comma
- decimal point
- missing values
- different ordering
- partial reports
- minor spelling mistakes
- aliases
- unrecognized lines

Do not assume one fixed WhatsApp format.

---

# 38.3 Label Matching

KPI identification should use:

- canonical KPI names
- configured aliases
- normalization
- fuzzy matching where justified

Levenshtein or another deterministic similarity method may be used.

Thresholds must be configurable rather than embedded throughout the code.

---

# 38.4 Value Extraction

Value extraction must remain separate from label recognition.

One source line may yield zero, one, or multiple KPI extraction candidates. A line
such as `Compresseur 1: 77108-77%` must not be forced into one composite number when
it represents separate measurements.

This separation improves:

- testing
- maintainability
- debugging
- future parser improvements

---

# 38.5 Confidence Score

Parsed values should include a confidence indication where appropriate.

Confidence may consider:

- quality of label match
- extraction pattern quality
- plausible range
- unit match
- fallback behavior

Confidence is guidance.

It is never permission to auto-save.

---

# 38.6 Unrecognized Content

Never silently discard unrecognized lines.

The user must be able to see that something was not understood.

Possible actions:

- manually associate with KPI
- ignore explicitly
- correct source text

Drafts and confirmed reports persist unknown lines with an explicit resolution:
`UNRESOLVED`, `ASSIGNED`, or `IGNORED`.

---

# 38.7 Parser Testing

The parser requires the strongest unit-test coverage in the project.

Use real anonymized message variations as test fixtures.

Test:

- separators
- decimals
- empty values
- aliases
- typos
- partial reports
- invalid numbers
- duplicate labels
- unknown lines
- plausible-range warnings
- WhatsApp UI/OCR noise and multiple visible message bubbles
- `---` / `----` missing markers
- attached units and multiple measurements on one line
- decimal/thousands ambiguity such as `30.197` versus `30197`
- different field orders and `:`, `=`, `->`, or whitespace separators

---

# 39. Human Confirmation Workflow

Human confirmation is the primary integrity mechanism.

Every automatically extracted report must reach a confirmation screen before final persistence.

---

# 39.1 Confirmation Screen Requirements

The confirmation experience should show:

- original source text
- extracted KPI label
- extracted value
- unit
- confidence
- validation warning
- editable final value
- unknown/unmatched lines

Low-confidence or suspicious values must be visually emphasized.

---

# 39.2 Confirmation Actions

The user should be able to:

- correct values
- confirm values
- remove incorrect extracted entries
- manually add missing KPI entries
- assign an unknown line
- save as draft
- confirm entire report

---

# 39.3 Zero-Error Interpretation

"Zero margin for error" does not mean pretending the parser is perfect.

It means:

```text
automation
+
validation
+
human confirmation
+
traceability
```

Together provide controlled data quality.

This distinction must remain visible in both implementation and report documentation.

---

# 40. Report Generation Architecture

Reports are first-class business objects.

Excel and PDF are not afterthoughts.

---

# 40.1 Excel

Use Apache POI.

Expected capability:

- professional formatting
- company header
- multiple sheets where useful
- formulas where useful
- summary sections
- conditional formatting
- clean file naming
- periodic report support

Do not create ugly raw table dumps and call them "professional Excel reports."

---

# 40.2 PDF

Use Apache PDFBox.

PDF is intended as a cleaner, distribution-friendly, management-facing representation.

Excel remains the editable/analytical format.

PDF remains the fixed presentation format.

---

# 40.3 Report Generation Interface

Prefer a clean abstraction around generated documents.

Conceptually:

```text
ReportGenerator
      ↑
      ├── ExcelReportGenerator
      └── PdfReportGenerator
```

The exact implementation may vary, but document-generation responsibilities must remain separated.

---

# 40.4 Storage

Generated files should be persisted using a storage abstraction.

Example:

```text
ReportStorageService
        ↑
LocalReportStorageService
```

Do not tie business logic directly to absolute disk paths.

MinIO is deliberately excluded.

Future storage implementations should be replaceable without rewriting report generation.

---

# 40.5 Naming

Generated files should follow deterministic names.

Example:

```text
FactoryFlow_Daily_2026-08-11.xlsx
FactoryFlow_Weekly_2026-W33.pdf
```

Exact naming conventions belong in the reporting documentation.

---

# 41. Scheduling Constitution

Quartz Scheduler handles automatic report scheduling.

Do not use Spring Batch for this requirement.

---

# 41.1 Supported Schedules

FactoryFlow should support:

- daily
- weekly
- monthly

report generation.

Schedules should be configurable where practical.

---

# 41.2 Scheduled Workflow

Conceptually:

```text
Quartz Trigger
     ↓
Report Generation
     ↓
Excel
     ↓
PDF
     ↓
Storage
     ↓
Notification
     ↓
Automatic Email
```

Failure at one step must not result in silent data loss.

---

# 41.3 Scheduling Responsibilities

Quartz determines **when** work happens.

It should not contain the full report-generation business logic.

The scheduled job should delegate to application services.

---

# 42. Email Constitution

FactoryFlow deliberately contains two completely different email/share workflows.

Do not confuse them.

---

# 42.1 User-Initiated Email / Sharing

When a user is actively viewing a generated report:

```text
Download
Share
Email
```

The Android application should use native Android sharing.

For email:

- prepare the file
- expose it safely through `FileProvider`
- open an email-capable application
- prefill attachment
- optionally prefill suggested subject/body
- let the user choose recipient and send

No backend SMTP call is needed for this workflow.

---

# 42.2 Automatic Scheduled Email

For scheduled report generation when no user is present:

the Spring Boot backend sends the generated report through `JavaMailSender`.

This workflow requires backend email configuration.

Keep both mechanisms distinct.

---

# 43. Real-Time Architecture

Spring WebSocket with STOMP is the preferred SHOULD-level mechanism if realtime
synchronization is implemented after the trusted core is stable.

SignalR belongs to a separate industrial project and is not a FactoryFlow technology.

---

# 43.1 Valid Real-Time Use Cases

Examples:

- new report confirmed
- generated report ready
- report generation progress
- dashboard refresh trigger
- new notification

Do not use WebSockets simply because they exist.

REST remains the primary mechanism for ordinary CRUD/request-response behavior.

---

# 43.2 No Polling When Real-Time Exists

For explicitly real-time events, avoid periodic polling if STOMP already provides the event.

However, WebSocket events should generally tell the client that something changed.

The client may still retrieve authoritative state over REST.

This avoids duplicating entire business payloads through the real-time channel unnecessarily.

---

# 44. RabbitMQ Constitution

RabbitMQ is a COULD-level late enhancement. It must not delay the synchronous core
and must not be presented as part of the implemented architecture until it exists.

---

# 44.1 Purpose

RabbitMQ may later decouple selected asynchronous workflows such as report generation,
but only through an explicit future API and architecture change.

Example:

```text
API Request
    ↓
Publish generation message
    ↓
RabbitMQ
    ↓
Worker/Consumer
    ↓
Generate files
    ↓
Persist status
    ↓
Notify client
```

---

# 44.2 Do Not Overuse RabbitMQ

Do not send every internal operation through RabbitMQ.

Use it only where asynchronous decoupling adds architectural value.

---

# 44.3 Failure Handling

Publishing/consuming failures must be visible and recoverable.

Where appropriate:

- retry
- dead-letter handling
- explicit error state
- logging

Resilience4j may protect appropriate external/infrastructure interactions.

---

# 45. Resilience4j Constitution

Resilience4j is not a decorative dependency.

Use it only around genuine failure boundaries.

Potential candidates include:

- RabbitMQ publishing
- SMTP scheduled email
- external notification services

Do not wrap normal in-process methods in circuit breakers.

Do not wrap the local authenticated OCR call in a circuit breaker without a measured failure need.

---

# 46. Dashboard Architecture

The dashboard is the application home.

It is not a persisted entity merely because it appears in UML.

It is a projection of multiple underlying data sources.

---

# 46.1 Dashboard Purpose

The dashboard should answer quickly:

- What is the current reporting status?
- What are today's important KPIs?
- Is anything missing?
- Are there warnings?
- What was recently generated?
- What should the engineer do next?

---

# 46.2 Dashboard Data

Potential dashboard sections:

- current KPI cards
- daily report status
- recent activity
- latest generated reports
- pending validation
- missing KPI warnings
- quick actions
- basic trend/statistics
- upcoming schedules
- recent notifications

The final visual specification belongs in `UI_UX.md`.

---

# 47. Statistics Constitution

Statistics should provide practical maintenance insight.

Initial statistics may include:

- daily values
- weekly averages
- monthly averages
- variation
- simple trends
- consumption evolution

Do not claim predictive analytics unless predictive algorithms are genuinely implemented.

---

# 48. Notification Constitution

Notifications may exist in multiple channels.

---

## In-App / Real-Time

Used while users are connected.

Currently powered through persisted backend notification records and ordinary REST refresh.
WebSocket events are a future option only.

---

## Push Notifications

Firebase Cloud Messaging.

Examples:

- report ready
- report missing
- threshold warning
- scheduled report completed

---

## Scheduled Email

Backend SMTP workflow.

Do not confuse these channels.

---

# 49. Android Architecture Constitution

FactoryFlow is native Android.

Flutter is deliberately excluded because the confirmed requirement is Android-only.

---

# 49.1 Required Architecture

Use:

```text
Compose Screen
      ↓
ViewModel
      ↓
Repository
      ↓
Remote / Local Data Source
```

Use `StateFlow` or an equivalent modern state mechanism.

---

# 49.2 ViewModels

ViewModels manage UI-related state and coordinate use cases/repositories.

ViewModels must not:

- execute SQL
- generate Excel
- parse KPI text directly
- contain file-system business logic
- contain large networking implementations

---

# 49.3 Compose

Composable functions should remain focused.

Large screens should be decomposed into reusable components.

Do not create one 1,000-line composable.

---

# 49.4 State

Every asynchronous screen must model explicit states where appropriate:

```text
Idle
Loading
Success
Empty
Error
```

Avoid fragile collections of unrelated booleans such as:

```text
isLoading
hasError
isEmpty
isSuccess
```

when a well-defined state model is clearer.

---

# 49.5 Navigation

Navigation must be centralized and understandable.

Do not scatter literal route strings throughout the application.

Navigation flows are defined in `UI_UX.md`.

---

# 49.6 Retrofit

REST interfaces belong in the data layer.

Do not call Retrofit directly from Composables.

---

# 49.7 Room

Room is intended for limited offline/cache behavior, not as an independent source of business truth.

PostgreSQL remains authoritative.

Define cache invalidation behavior before implementing broad offline functionality.

---

# 49.8 Android Native Integrations

Use Android-native mechanisms correctly for:

- camera
- gallery selection
- Share Intent
- FileProvider
- permissions
- external email/share apps
- push notifications

These features are part of the portfolio value of using native Kotlin.

---

# 50. Design System Compliance

All Android UI must comply with:

```text
DESIGN.md
UI_UX.md
```

Codex must not invent unrelated visual styles.

The interface should combine:

- premium restraint inspired by Apple
- strong Android-native interaction patterns from Material 3
- information density appropriate to industrial software
- dashboard clarity inspired by high-quality modern productivity tools

The design must never become a copy of Apple's website.

The inspirations must be translated into an industrial Android product.

---

# 51. Test Architecture

Testing is not an end-of-project task.

Tests must grow with the implementation.

---

# 51.1 Backend Tests

Use:

- JUnit
- Mockito
- Spring integration testing where appropriate

Highest priority:

```text
KPI parser
KPI validation
report lifecycle
authentication
report generation
```

---

# 51.2 Android Tests

Use appropriate:

- ViewModel tests
- repository tests
- Compose UI tests for critical flows where feasible

Do not spend disproportionate time testing purely decorative UI.

Prioritize business-critical flows.

---

# 51.3 End-to-End Manual Scenarios

Before project completion, validate complete scenarios such as:

```text
Login
→ Paste WhatsApp text
→ Parse
→ Correct one KPI
→ Confirm
→ View dashboard
→ Generate Excel
→ Generate PDF
→ Download
→ Share by email
```

and:

```text
Share screenshot from WhatsApp
→ FactoryFlow receives image
→ OCR
→ Parse
→ Confirm
→ Save
```

and:

```text
Quartz schedule fires
→ Report generated
→ Files stored
→ Notification sent
→ Automatic email delivered
```

---

# 52. Observability Constitution

Monitoring exists primarily to demonstrate professional engineering quality and support report evidence.

Use:

- Spring Boot Actuator / Micrometer
- Prometheus
- Grafana
- PostgreSQL exporter if required
- k6

Do not allow monitoring work to delay core FactoryFlow functionality.

---

# 52.1 Useful Metrics

Possible metrics include:

- request rate
- response latency
- error rate
- JVM memory
- CPU
- DB connection metrics
- parser duration
- report generation duration
- generated report count
- WebSocket connections

Only expose useful metrics.

---

# 53. Performance Testing

Use k6 for representative REST workflows.

Do not create unrealistic tests merely to produce impressive graphs.

Test scenarios should reflect:

- normal usage
- higher-than-normal usage
- stress/spike behavior if time permits

Performance evidence may be used in the report and GitHub documentation.

---

# 54. Professional Dependency Discipline

Do not add libraries casually.

Before adding a dependency, verify:

1. the problem cannot be solved cleanly with existing dependencies
2. the library is actively maintained
3. the license is compatible
4. the dependency does not introduce disproportionate complexity
5. it fits the approved architecture

Examples of deliberate decisions already made:

- Apache PDFBox instead of iText because of licensing concerns
- no MinIO for the first version
- no Spring Batch
- no Spring AI for parsing

---

# 55. No Fake Enterprise Complexity

FactoryFlow should demonstrate enterprise engineering.

It should not imitate enterprise complexity for appearance.

Do not introduce:

- microservices for 2–4 users
- distributed tracing without a use case
- Kubernetes
- service mesh
- CQRS/event sourcing without justification
- multiple databases
- unnecessary caches

Strong architecture is often simpler than buzzword architecture.

---

# 56. Architecture Change Protocol

If implementation reveals that an approved architectural decision is flawed:

1. Do not silently work around it.
2. Document the issue.
3. Explain why the existing decision is insufficient.
4. Propose a replacement.
5. Identify affected documentation/code.
6. Obtain approval.
7. Update documentation first or in the same task.
8. Implement incrementally.

Architecture is allowed to evolve.

It is not allowed to drift.

---

---

# 57. Project Delivery Philosophy

FactoryFlow must be completed with a balance between ambition and execution discipline.

The project is intentionally ambitious.

However, ambition must never become uncontrolled scope.

The first objective is a polished, reliable, defensible core product.

Advanced technologies should only be added when they strengthen the architecture or portfolio value without threatening completion.

A smaller system that is complete, tested, documented and visually polished is better than a larger system containing unfinished integrations.

---

# 58. Scope Priority

FactoryFlow features are organized by delivery priority.

The exact current status belongs in `TASKS.md`, but the following hierarchy defines how scope should be treated.

---

## 58.1 Must Have

The project is not considered complete without:

- Authentication
- Dashboard as application home
- KPI definition management
- Manual KPI entry
- Text paste input
- Gallery image import
- WhatsApp/Android Share Intent image input
- Gallery and Share Intent image acquisition
- Private backend PaddleOCR recognition
- Deterministic parser
- Fuzzy KPI label recognition
- Numeric normalization
- Confidence and warning logic
- Human confirmation workflow
- Draft reports
- PostgreSQL persistence
- Report history
- Search and filtering
- Excel report generation
- PDF report generation
- Daily scheduling
- Weekly scheduling
- Monthly scheduling
- Native device-side report sharing
- Scheduled backend email delivery
- API documentation
- Database migrations
- Basic automated testing

A Must Have item may only be deferred through an explicit decision recorded in `TASKS.md`.

---

## 58.2 Should Have

Strong secondary features include:

- WebSocket/STOMP real-time updates
- Firebase Cloud Messaging
- dashboard statistics
- trend visualization
- upcoming report schedules
- richer notification experience
- stronger offline/cache behavior
- advanced reporting UX
- polished report previews

These should be implemented after the core workflow is stable.

---

## 58.3 Could Have

Optional advanced engineering features include:

- RabbitMQ asynchronous processing
- Resilience4j
- advanced Apache POI formatting
- ZIP export
- deeper monitoring
- extended performance testing
- richer audit visualizations
- additional statistics

These features must never delay core product completion.

---

## 58.4 Future Scope

The following are explicitly future-facing:

- Predictive maintenance
- anomaly prediction
- KPI forecasting
- LLM-powered natural-language historical queries
- SAP integration
- ERP integration
- multi-site deployment
- advanced RBAC
- iOS application
- browser-based frontend

Do not implement future features unless the project scope is formally changed.

---

# 59. Time Constraint Rule

FactoryFlow is being developed under a limited delivery window.

This constraint must influence execution strategy, but never justify careless engineering.

When time pressure exists:

Prefer:

- complete core workflows
- clean architecture
- working integrations
- tests for critical logic
- strong documentation
- polished key screens

Over:

- speculative infrastructure
- unused abstractions
- excessive generalization
- decorative technologies
- unfinished secondary features

Do not solve time pressure by damaging the architecture.

Solve it by prioritizing scope.

---

# 60. Technical Decision Registry

Important technical decisions must remain traceable.

At minimum, the following decisions are considered approved unless documentation is intentionally changed.

---

## 60.1 Native Android

FactoryFlow uses Kotlin and Jetpack Compose.

Flutter is intentionally excluded because the product requirement is Android-only.

This allows deeper use of:

- Android Share Intent
- FileProvider
- Android lifecycle APIs
- native platform behavior

---

## 60.2 Spring Boot Backend

Java with Spring Boot is used deliberately to strengthen enterprise backend engineering skills and provide a mature ecosystem for:

- security
- JPA
- WebSocket
- scheduling
- messaging
- email
- validation
- observability

---

## 60.3 PostgreSQL

PostgreSQL is the primary persistent database.

It is suitable for the relational and auditable nature of KPI reporting.

No additional database should be introduced without a demonstrated requirement.

---

## 60.4 Deterministic Parsing

LLMs are not used for official KPI extraction.

The approved parser uses deterministic normalization, matching, extraction and validation.

This decision exists because explainability and data integrity are more important than probabilistic flexibility.

---

## 60.5 Human-in-the-Loop Validation

Every automatically extracted KPI must pass through explicit confirmation.

This is the real data-quality guarantee of FactoryFlow.

---

## 60.6 PaddleOCR

OCR is performed by the private PaddleOCR runtime behind the backend `OcrProvider`.
Android sends gallery/shared images to the authenticated endpoint; deterministic KPI
interpretation remains separate in the parser.

---

## 60.7 Apache POI

Excel generation uses Apache POI.

Excel is a first-class reporting format, not simply a data export.

---

## 60.8 Apache PDFBox

PDF generation uses Apache PDFBox.

This was chosen over iText partly to avoid licensing complications associated with iText's AGPL/commercial model.

---

## 60.9 Quartz

Quartz handles recurring report scheduling.

Spring Batch is intentionally excluded because FactoryFlow does not currently require a batch-processing framework.

---

## 60.10 Storage Abstraction

Generated files are accessed through a `ReportStorageService` abstraction.

The first implementation uses local filesystem storage.

MinIO is intentionally excluded from the first version.

A future object-storage implementation may be added without rewriting report-generation business logic.

---

## 60.11 Two Sharing Models

FactoryFlow deliberately separates:

### User-initiated sharing

Android Share Intent / FileProvider

from:

### unattended scheduled delivery

Spring `JavaMailSender`

Never collapse these into one mechanism.

---

## 60.12 WebSocket/STOMP

Spring WebSocket with STOMP is the approved SHOULD-level real-time mechanism if it is
implemented later. It is not part of the current deployed FactoryFlow path.

REST remains the primary authoritative request/response channel.

---

## 60.13 RabbitMQ

RabbitMQ is optional and deliberate.

It exists as an event-driven architecture learning opportunity, not because the initial user count requires distributed messaging.

The implementation must remain honest about that fact.

---

## 60.14 No Docker Requirement

Docker is not a mandatory FactoryFlow requirement.

Do not introduce it merely because backend projects often use Docker.

It may be reconsidered later if deployment needs justify it.

---

# 61. Business Integrity Rules

Certain rules are so important that they override convenience.

---

## 61.1 Never Silently Correct Source Data

If the parser detects a likely typo, suspicious number or alias, it may interpret it.

It must not silently modify the original source.

Preserve the original input.

---

## 61.2 Missing Means Missing

An empty KPI field may mean:

> not reported

It must not automatically become:

```text
0
```

Zero is a valid numerical value.

Missing is a different business state.

Never confuse them.

---

## 61.3 Partial Reports Are Valid Inputs

A source message may contain only some KPIs.

Do not reject the entire report simply because it is incomplete.

The system may warn about missing expected information while preserving the valid supplied values.

---

## 61.4 Confirmation Is Mandatory

No confidence threshold, even 100%, may bypass confirmation for automatically parsed data unless the business rules are explicitly changed in the future.

---

## 61.5 Preserve Manual Corrections

When a user changes an extracted value, retain enough information to distinguish:

- extracted value
- final confirmed value
- whether the user edited it

This information is useful for:

- auditability
- parser evaluation
- future parser improvement

---

# 62. Data Model Baseline

Unless superseded by `docs/05_Database.md`, the approved conceptual database model includes the following.

---

## `users`

Core information:

```text
id
name
email
password_hash
active
created_at
```

No artificial role field is required for the initial single-role system.

---

## `kpi_definitions`

Core information:

```text
id
code
display_name
category
unit
plausible_min
plausible_max
aliases
active
```

---

## `maintenance_reports`

Core information:

```text
id
submitted_by
effective_date
submitted_at
raw_text
source
status
```

Approved acquisition source concepts include:

```text
paste
gallery_ocr
share_ocr
camera_ocr
manual
```

`effective_date` is the business/report date and is distinct from `submitted_at`
and `confirmed_at`. Multiple maintenance reports may share the same effective date.

---

## `kpi_entries`

Core information:

```text
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
```

MVP KPI entries do not persist a separate `PENDING` / `VALIDATED` / `REJECTED` /
`CORRECTED` state machine. Traceability comes from extracted, draft/current, and
final values plus edit metadata, confidence, warnings, and source/unit context.

---

## `generated_reports`

Core information:

```text
id
type
format
generated_at
file_path
generated_by
```

Core generation is synchronous initially. Intentional regeneration creates a new
`GeneratedReport` version with provenance. File-generation status and email-delivery
status are separate; for example, `generation = READY` with `emailDelivery = FAILED`
is valid. Async `PENDING` / `GENERATING` behavior requires a later explicit API change.

Report types may include:

```text
daily
weekly
monthly
manual
```

Formats include:

```text
excel
pdf
```

---

## `audit_log`

Core information:

```text
id
user_id
action
entity_type
entity_id
occurred_at
```

The final database design may add fields required for proper auditing.

---

# 63. API Baseline

Unless replaced by the authoritative API specification, the approved conceptual API includes capabilities equivalent to:

```text
POST /api/auth/login
GET  /api/users/me

POST /api/reports/analyze
POST /api/reports/drafts
PUT  /api/reports/{id}/draft
POST /api/reports/{id}/confirm
GET  /api/reports

GET  /api/kpi-definitions
POST /api/kpi-definitions

POST /api/generated-reports
POST /api/generated-reports/individual

GET  /api/statistics
```

The exact request and response shapes are standardized in `docs/06_API.md`.

Do not create multiple competing API conventions during development.

---

# 64. Report Lifecycle

The conceptual report lifecycle is:

```text
Input received
      ↓
Raw text available
      ↓
Analyzed (side-effect-free)
      ↓
Extraction results
      ↓
User review
      ↓
Server-side Draft ─┐
      │             │
      └── Resume ───┘
      ↓
Confirmed
      ↓
Stored as authoritative KPI data
      ↓
Available to dashboard/statistics
      ↓
Can be included in generated reports
```

A generated Excel or PDF file is not the same object as the underlying maintenance report.

Keep these concepts separate.

---

# 65. Draft Constitution

Draft support is a first-class requirement.

Users may interrupt validation.

The application must allow them to return later without losing work.

A draft should preserve enough information to resume:

- source text
- extraction results
- user corrections already made
- report state
- acquisition method

Do not force users to complete a long validation flow in one uninterrupted session.

---

# 66. Search and History Constitution

Historical reports are not simply an archive screen.

They are part of the operational value of FactoryFlow.

Users should be able to locate relevant information efficiently.

Supported search/filter dimensions should include where appropriate:

- date
- report type
- submitter
- KPI
- status

The system should distinguish between:

- maintenance reports
- generated Excel/PDF documents

Both may have history views, but they represent different business concepts.

---

# 67. Dashboard Is the Home Screen

The dashboard must remain the primary post-login destination.

Do not make:

- parser
- report form
- OCR
- history

the application home.

The dashboard should provide operational orientation before the user chooses an action.

The first few seconds inside FactoryFlow should answer:

```text
What is happening today?
What is missing?
What requires attention?
What has recently changed?
What can I do now?
```

---

# 68. UI Quality Gate

A feature that technically works but looks unfinished is not complete.

Before marking any user-facing feature done, inspect:

- visual hierarchy
- spacing
- alignment
- typography
- touch feedback
- loading state
- empty state
- error state
- success state
- navigation
- animation
- accessibility
- consistency with surrounding screens

Screenshots should be reviewed as product artifacts, not merely debugging evidence.

---

# 69. Design Inspiration Rule

FactoryFlow may learn from excellent products.

Approved inspiration categories include:

- Apple for restraint, hierarchy, whitespace and polish
- Material 3 for Android-native interaction and accessibility
- Linear for modern productivity dashboards and subtle motion
- Stripe for professional forms and settings
- Notion for clean structured information

Inspiration must never become imitation.

Do not copy proprietary visual identities.

Do not turn FactoryFlow into an Apple website inside an Android application.

The final visual identity must belong to FactoryFlow.

---

# 70. Material 3 Rule

Material 3 provides the behavioral foundation for the Android interface.

Use native Android expectations for:

- navigation
- accessibility
- system bars
- touch interaction
- modal behavior
- permissions
- back navigation
- keyboard handling
- dynamic device constraints

FactoryFlow may customize visual appearance extensively while preserving correct Android behavior.

---

# 71. Animation Rule

Animations must have purpose.

Good reasons include:

- explaining navigation
- communicating state change
- showing successful completion
- revealing additional information
- preserving spatial context
- drawing attention to warnings

Avoid:

- constant movement
- decorative bouncing
- long transitions
- excessive spring effects
- animation that slows routine work

Industrial software should feel polished and fast.

---

# 72. Accessibility Rule

Accessibility is not optional polish.

The interface must consider:

- contrast
- readable text
- touch target size
- semantic labels
- screen reader compatibility where practical
- state communication beyond color alone
- scalable content
- understandable error messages

Never rely solely on red/green differentiation to communicate business status.

---

# 73. Error Handling Philosophy

Errors are expected system states.

They must be designed deliberately.

Do not show users:

```text
NullPointerException
500
IllegalStateException
JSON parsing failed
```

Translate technical failures into actionable language.

Examples:

```text
Unable to analyze this report.
Check the text and try again.
```

or:

```text
The report was generated, but the automatic email could not be sent.
You can retry delivery from report history.
```

Preserve useful technical details in logs, not in end-user interfaces.

---

# 74. Offline and Network Failure Rule

FactoryFlow is network-dependent for backend operations.

However, mobile connectivity may be unreliable.

At minimum:

- do not destroy user-entered data when a request fails
- preserve drafts where practical
- explain connectivity errors
- provide retry
- avoid duplicated submissions after retries
- prevent accidental loss of long validation work

Do not claim full offline support unless it is actually implemented.

---

# 75. Idempotency and Duplicate Protection

Operations that may be retried due to network uncertainty must be reviewed for duplication risk.

Examples:

- report confirmation
- report generation
- scheduled processing
- notification dispatch

Where duplicate execution would cause harm, use appropriate guards.

Do not assume a mobile request executes exactly once.

---

# 76. Logging Rules

Logs exist for troubleshooting and observability.

Use appropriate levels:

```text
TRACE
DEBUG
INFO
WARN
ERROR
```

Production-like logs must avoid:

- passwords
- JWT tokens
- raw secrets
- sensitive configuration

Log business identifiers where useful for traceability without exposing unnecessary data.

Avoid logging every trivial method call.

---

# 77. Exception Handling

Use centralized backend exception handling.

Prefer a global exception mechanism such as Spring's controller advice instead of repetitive `try/catch` blocks in every controller.

Create meaningful domain exceptions where they improve clarity.

Do not catch exceptions only to ignore them.

Bad:

```java
try {
    ...
} catch (Exception ignored) {
}
```

Failures must either:

- be handled meaningfully
- be translated
- be retried appropriately
- propagate to the responsible boundary

---

# 78. Configuration Rule

Environment-dependent values belong in configuration.

Examples:

- database URL
- JWT secret
- token duration
- SMTP settings
- report storage directory
- parser similarity thresholds
- frontend/backend base URLs
- RabbitMQ connection
- Firebase configuration

Never scatter these values through code.

---

# 79. Environment Strategy

At minimum, development should distinguish configuration appropriate for local development from production-like configuration.

Possible Spring profiles:

```text
dev
test
prod
```

Do not create unnecessary environment complexity before it is needed.

Test configuration must never accidentally connect to a real production-like database.

---

# 80. Naming Constitution

Naming must reflect the business domain.

Prefer:

```text
MaintenanceReport
KpiDefinition
ReportGenerationService
ReportStatus
ConfirmationResult
```

Avoid generic names:

```text
Data
Manager
Helper
Thing
ObjectInfo
Processor2
UtilStuff
```

A developer should understand intent from names without opening every implementation.

---

# 81. Method Size and Class Size

There is no arbitrary maximum line count.

However, unusually large methods/classes must trigger architectural review.

When a method performs several independent conceptual steps, extract responsibilities.

When a class requires descriptions containing several unrelated "and" clauses, reconsider its responsibility.

Do not split code mechanically merely to reduce line count.

Cohesion matters more than numerical limits.

---

# 82. Utility Class Rule

Utility classes are acceptable for genuinely stateless low-level behavior.

They must not become an escape hatch for avoiding architecture.

Examples of potentially acceptable utility behavior:

- normalized string transformations
- safe date formatting
- narrow numeric parsing helpers

Business workflows belong in business services.

---

# 83. Dependency Injection Rule

Use dependency injection consistently.

Prefer constructor injection in Spring.

Dependencies should be visible and explicit.

Avoid hidden service locators.

Avoid manually creating framework-managed services with `new`.

---

# 84. Transaction Rule

Transaction boundaries should exist around meaningful business operations.

Examples:

```text
Confirming a maintenance report
```

may require multiple writes that should succeed or fail together.

Do not annotate every method as transactional without understanding the transaction boundary.

Keep external network operations out of long-running database transactions where possible.

---

# 85. Date and Time Rule

Industrial reporting depends heavily on dates.

Use modern Java time APIs.

Prefer:

```text
Instant
LocalDate
LocalDateTime
ZonedDateTime
```

depending on domain meaning.

Do not rely on legacy `Date` APIs without reason.

The meaning of stored timestamps and timezone strategy must be documented.

---

# 86. Numeric Precision Rule

KPI values must use appropriate numeric types.

Do not blindly use floating-point numbers for values where decimal precision matters.

Use `BigDecimal` where appropriate.

The parser must preserve intended decimal interpretation.

Examples:

```text
12,5
12.5
```

may both represent the same decimal depending on source conventions.

---

# 87. Unit Rule

Units are part of KPI meaning.

Do not treat:

```text
10 kg
10 t
```

as equivalent.

A KPI definition should identify its expected unit.

Captured source units may be preserved when useful.

Automatic unit conversion should not be introduced without explicit business rules.

---

# 88. Plausibility Validation

`plausible_min` and `plausible_max` exist to detect suspicious values.

A value outside the plausible range should generally produce a warning.

It should not automatically be destroyed or rewritten.

Human confirmation determines the final result.

Plausibility ranges are not necessarily hard physical constraints.

---

# 89. Parser Alias Governance

Aliases belong in configurable KPI definitions.

Do not continuously patch parser source code with hardcoded variants such as:

```java
if (label.equals("abc") || label.equals("abcd") || ...)
```

when those variants are business vocabulary.

Store and manage aliases through the KPI catalog.

---

# 90. Parser Explainability

Given an extraction result, the system should be able to explain at least conceptually:

- which line was interpreted
- which KPI was matched
- what value was extracted
- confidence
- warning state

Avoid opaque parser behavior that cannot be debugged.

---

# 91. AI Future Boundary

Artificial intelligence is allowed as a future enhancement where probabilistic behavior is appropriate.

A strong future use case is:

> Natural-language querying over already validated historical KPI data.

Examples:

```text
What was the average value of KPI X last month?
```

or:

```text
Show the weeks where KPI Y exceeded its normal range.
```

In this future design, the AI interacts with validated data.

It does not decide what the original WhatsApp message "probably meant" for official reporting.

This boundary must remain clear.

---

# 92. Report Quality Standard

Generated documents are part of the product interface.

They must be treated with the same care as mobile screens.

A professional report should consider:

- title
- company/project identity
- reporting period
- generation timestamp
- clear KPI grouping
- units
- readable tables
- consistent typography
- page numbering for PDF
- meaningful filename
- sensible margins
- summary information
- metadata where useful

Do not ship developer-looking files.

---

# 93. Scheduled Report Semantics

Daily, weekly and monthly reports must have clearly defined time windows.

The same report period must produce consistent results.

Avoid ambiguous logic such as:

```text
last 7 days
```

when the business requirement actually means a calendar week.

The business timezone is `Africa/Casablanca`.

Weekly reporting uses calendar weeks from Monday through Sunday.

Monthly scheduled reporting generates the previous calendar month at the configured
time on the first day of the following month. It must not use arbitrary
`dayOfMonth = 31` semantics.

If the server misses one scheduled execution, Quartz may perform one recovery
execution. Generation must be idempotent for the same schedule, reporting period,
and format.

---

# 94. Automatic Email Reliability

Automatic scheduled email delivery is infrastructure-dependent.

Email failure must not erase a successfully generated report.

These are separate outcomes:

```text
Generation succeeded
Delivery failed
```

Record and expose that distinction.

Provide a reasonable retry or manual resend path if implemented.

---

# 95. Notification Reliability

Notifications are not the source of truth.

If an FCM push is lost, the underlying report or warning must still exist in the application.

Push notifications attract attention.

They do not own business state.

---

# 96. WebSocket Reliability

A disconnected WebSocket must not corrupt application state.

REST/database state remains authoritative.

On reconnect, the client should be able to recover current state without requiring every missed WebSocket event.

---

# 97. RabbitMQ Reliability

If RabbitMQ is implemented:

- consumers must acknowledge messages appropriately
- duplicate processing risk must be considered
- failure state must be observable
- messages must not contain unnecessary sensitive data
- retry/dead-letter behavior should be documented

Do not pretend message delivery automatically gives exactly-once business execution.

---

# 98. Performance Before Scale

The initial expected user count is small.

Performance work should focus on:

- avoiding obviously inefficient queries
- correct indexes
- avoiding N+1 problems
- sensible payload sizes
- efficient report generation
- responsive Android interactions

Do not architect FactoryFlow as if it serves millions of concurrent users.

Design for clean future evolution.

---

# 99. Database Query Discipline

Watch for JPA issues such as:

- N+1 queries
- lazy-loading failures
- enormous entity graphs
- unnecessary eager fetching
- loading entire tables for simple projections

Use DTO projections or explicit queries when they materially improve behavior.

Do not optimize without measuring.

---

# 100. File Handling Rules

Generated and imported files require careful handling.

Consider:

- safe filenames
- MIME type
- extension validation
- temporary file cleanup
- storage path traversal
- Android URI permissions
- FileProvider
- file existence
- failed generation cleanup

Never trust arbitrary user-supplied file paths.

---

# 101. Image Handling Rules

OCR input may originate from:

- gallery
- camera
- WhatsApp share
- other Android share sources

The Android application must handle content URIs correctly.

Do not assume every shared image provides a traditional filesystem path.

Use Android-supported URI/content APIs.

---

# 102. Permissions Rule

Request only permissions that are actually required.

Prefer modern Android APIs that minimize broad storage permissions.

Permission denial must have a graceful UX.

Do not block unrelated parts of FactoryFlow because a user denied camera permission.

---

# 103. Destructive Actions

Destructive operations require deliberate UX.

Examples:

- delete draft
- delete generated file
- disable KPI definition
- sign out with unsaved work

Use confirmation where accidental activation could cause meaningful loss.

Avoid unnecessary confirmation dialogs for reversible actions.

---

# 104. Empty State Standard

Every important list or dashboard section must have a designed empty state.

An empty state should explain:

- what is absent
- whether that is normal
- what the user can do next

Bad:

```text
No data
```

Better:

```text
No reports have been confirmed today.
Paste a KPI message or import a screenshot to create the first report.
```

Exact wording belongs in `UI_UX.md`.

---

# 105. Loading State Standard

Do not show indefinite blank screens.

Use the appropriate mechanism:

- progress indicator
- skeleton
- local optimistic UI
- disabled action with progress

Do not overuse global full-screen loaders.

Loading behavior should preserve context whenever possible.

---

# 106. Success State Standard

Successful actions must provide appropriate feedback.

Examples:

- report confirmed
- draft saved
- report generated
- schedule created

Feedback may use:

- updated state
- Snackbar
- subtle animation
- navigation

Do not display intrusive dialogs for every successful action.

---

# 107. Warning State Standard

Warnings are not errors.

Examples:

- value outside plausible range
- low-confidence match
- missing KPI
- report incomplete

Warnings should allow deliberate continuation when business rules permit.

---

# 108. Error State Standard

An error prevents completion of an operation.

Examples:

- authentication failure
- report persistence failure
- unreadable image
- backend unavailable

Errors should:

- explain what happened
- preserve recoverable user work
- provide retry or next action where possible

---

# 109. Navigation Back-Stack Rule

Back navigation must feel natural.

Do not create loops such as:

```text
Dashboard
→ History
→ Report
→ Dashboard
→ Back
→ Report
```

After terminal workflows such as successful login or confirmed report submission, adjust navigation stack deliberately.

Full navigation semantics belong in `UI_UX.md`.

---

# 110. Resume Workflow Rule

Users may:

- switch applications
- receive a call
- lock the device
- kill the app
- lose connectivity

Long-running input/validation work must be resilient enough that reasonable interruptions do not unnecessarily destroy progress.

Draft support is a major part of this requirement.

---

# 111. Device-Side Sharing Rule

When sharing generated files:

- use `FileProvider`
- use content URIs
- grant temporary URI read permissions
- set correct MIME type
- avoid exposing raw private filesystem paths

Follow Android platform conventions.

---

# 112. Image Acquisition Rule

Current Android image acquisition uses gallery selection and Android Share Intent.
CameraX and the direct camera route were removed by the approved M3 scope decision and
must not be described as implemented.

---

# 113. FCM Rule

Firebase Cloud Messaging may deliver relevant operational notifications.

The app should handle:

- foreground notification behavior
- background notification behavior
- tapping a notification
- navigation to relevant content where feasible

Push implementation must not leak sensitive report information unnecessarily onto lock screens.

---

# 114. Dashboard Statistics Rule

Statistics must be based on confirmed data.

Draft or unconfirmed parser output must not influence official dashboard statistics.

This protects the integrity of analytical views.

---

# 115. Generated Report Data Rule

Generated Excel/PDF reports must use authoritative confirmed KPI values.

Never use raw extracted values when a corrected final value exists.

The confirmed value is authoritative.

---

# 116. Audit Log Rule

Audit logs must be append-oriented.

Do not silently rewrite historical audit events.

The audit trail should represent what actually occurred.

---

# 117. Soft Delete vs Hard Delete

Do not introduce soft delete automatically everywhere.

Use soft deletion only where historical traceability/business value requires it.

Examples such as KPI definitions may be better represented using:

```text
active = false
```

instead of deleting records referenced by historical reports.

---

# 118. Referential Integrity

Historical reports must remain interpretable even when configuration changes.

Do not design KPI-definition updates in a way that destroys the meaning of past data.

Consider carefully how renamed/deactivated KPI definitions affect history.

---

# 119. Schema Evolution Rule

Database changes should favor backward-safe evolution when reasonable.

Avoid destructive schema changes late in development unless necessary.

Before dropping or converting data:

- identify impact
- migrate existing data
- update documentation
- test migration

---

# 120. API Evolution Rule

Avoid casually changing existing API contracts once Android depends on them.

If a breaking change is required:

1. identify clients affected
2. update backend and Android coherently
3. update OpenAPI/docs
4. test the complete workflow

Do not leave Android compiled against obsolete DTOs.

---

# 121. Build Health Rule

The repository should return to a healthy build state after every completed task.

A task must not be marked complete while:

- backend compilation fails
- Android compilation fails due to the task
- migrations fail
- critical tests fail

Unrelated pre-existing failures must be documented rather than hidden.

---

# 122. Dependency Version Rule

Do not constantly chase newest versions during the sprint.

Once a stable compatible stack is established, prioritize implementation stability.

Upgrade dependencies when:

- security requires it
- compatibility requires it
- a needed feature requires it
- the upgrade is deliberately scheduled

---

# 123. Warning Discipline

Warnings should be investigated, not blindly suppressed.

Suppression is acceptable only when:

- the warning is understood
- suppression is intentional
- there is a documented reason where non-obvious

Never globally suppress broad categories to make the IDE "look clean."

---

# 124. Linting and Formatting

Use consistent automatic formatting.

Backend and Android code should follow their ecosystem conventions.

Do not spend development time manually aligning whitespace.

Formatting changes should not be mixed into unrelated huge diffs where avoidable.

---

# 125. Documentation Tone

Engineering documentation should be:

- precise
- professional
- concise enough to use
- detailed enough to remove ambiguity

Avoid:

- marketing exaggeration
- unsupported claims
- buzzwords without implementation
- pretending optional features are complete

Documentation must reflect the actual project.

---

# 126. README Standard

The final GitHub README should function as a professional project landing page.

It should eventually contain:

- project problem
- solution
- main features
- architecture
- technology stack
- screenshots
- workflow diagram
- API/documentation references
- installation/run instructions
- engineering decisions
- testing/performance evidence
- roadmap

Do not turn the README into a copy of all internal documentation.

---

# 127. GitHub Portfolio Standard

FactoryFlow is a flagship repository.

Before public presentation:

- remove secrets
- remove useless binaries
- verify `.gitignore`
- clean accidental generated files
- ensure repository structure is clear
- ensure README screenshots are polished
- ensure commit history is understandable
- ensure application naming is consistent
- ensure unfinished experimental code is not presented as production-ready

Portfolio quality requires honesty and clarity.

---

# 128. Academic Report Standard

FactoryFlow is complementary to the main industrial internship project and may receive limited report space.

Therefore report content must focus on high-value engineering decisions instead of documenting every class.

Prefer explaining:

- business problem
- unified acquisition pipeline
- deterministic parser
- human validation
- architecture
- mobile/backend integration
- report automation
- selected advanced technologies
- measurable result

Avoid wasting pages on boilerplate code.

---

# 129. UML Rule

Two levels of UML may exist.

### Report UML

Keep diagrams understandable and business-focused.

Approximately 10–15 core business concepts are usually enough for the class diagram.

### Development UML

More detailed diagrams may live in project documentation/GitHub when they provide implementation value.

Do not force every framework class into academic UML diagrams.

---

# 130. UML Core Concepts

Candidate report-level concepts include:

```text
MaintenanceEngineer
KPIReport / MaintenanceReport
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
```

And appropriate enums such as:

```text
ReportStatus
ValidationStatus
AcquisitionMethod
ReportFormat
ScheduleType
```

The final UML must model actual implementation rather than preserve outdated conceptual names blindly.

---

# 131. Diagram Quality Rule

Diagrams must be readable enough for:

- engineering documentation
- academic report
- GitHub

Prefer:

- white/light clean background
- clear hierarchy
- readable labels
- meaningful cardinalities
- correct UML relationships
- limited crossing lines
- visual consistency

A beautiful diagram with incorrect semantics is unacceptable.

Correctness comes first.

---

# 132. Report Evidence Capture Rule

Do not wait until the end of the project to recreate evidence.

When an important milestone works, capture it immediately.

Examples:

- successful Swagger flow
- parser confirmation screen
- OCR from WhatsApp screenshot
- dashboard
- generated Excel
- generated PDF
- Quartz scheduled generation
- WebSocket update
- FCM notification
- k6 results
- Grafana metrics

Store report-ready assets in an organized directory.

---

# 133. Suggested Evidence Structure

A canonical evidence structure is:

```text
report/evidence/factoryflow/
├── screenshots/
├── tests/
└── exports/
```

Source-controlled diagrams belong in the top-level `diagrams/` directory.

Do not commit low-quality duplicate screenshots indefinitely.

Curate evidence as the project matures.

---

# 134. Learning Record

When a meaningful new engineering concept is introduced, the developer should be able to explain:

- what it is
- why FactoryFlow uses it
- where it is implemented
- what alternative could have been used
- what tradeoff was made

The objective is not merely to possess generated code.

The developer must be able to defend the architecture during interviews and academic evaluation.

---

# 135. No Technology Theater

Never add a technology just to make the technology stack longer.

A technology earns its place when it provides:

- business value
- architectural value
- deliberate learning value

and the cost remains acceptable.

Examples:

Quartz has a clear purpose.

PDFBox has a clear purpose.

PaddleOCR has a clear purpose behind the private backend provider boundary.

RabbitMQ is acceptable only if its deliberate asynchronous architecture value justifies its complexity.

---

# 136. Feature Completion Over Feature Count

A finished feature includes:

- implementation
- integration
- UI
- errors
- testing
- documentation
- evidence

Ten incomplete technologies are less valuable than five fully integrated engineering capabilities.

---

# 137. Refactoring Rule

Refactoring is encouraged when it improves:

- readability
- cohesion
- testability
- architecture
- duplication

But refactoring must preserve behavior.

When meaningful risk exists, establish tests first.

Do not use "refactor" as an excuse for uncontrolled redesign during a time-constrained milestone.

---

# 138. Technical Debt Rule

Not all technical debt is forbidden.

Deliberate temporary compromises may be accepted when:

- they are explicitly identified
- they do not compromise critical correctness/security
- the reason is clear
- a follow-up task exists if necessary

Hidden technical debt is unacceptable.

---

# 139. Placeholder Rule

Placeholders are allowed during controlled scaffolding only.

Examples:

```text
temporary fake dashboard data
temporary report template
temporary local notification
```

Before the related feature becomes complete, placeholders must either:

- be replaced
- be explicitly documented as future behavior

Never ship placeholder business behavior while marking the feature complete.

---

# 140. Mock Data Rule

Mock data may support UI development.

It must be clearly separated from production repositories/data sources.

Do not sprinkle hardcoded fake business values through Composables.

When the real API is ready, remove or isolate mocks.

---

# 141. Seed Data Rule

Development seed data is useful for:

- KPI definitions
- test users
- example reports

Seed data must not be confused with migrations containing real production values.

Sensitive real company information must not be included in public seed data.

---

# 142. Real Data Privacy

If real industrial WhatsApp messages or KPI values are used for testing/documentation, anonymize sensitive information before public GitHub exposure.

Do not expose:

- private phone numbers
- employee identities
- company-sensitive operational values
- private messages
- credentials

Portfolio quality includes data responsibility.

---

# 143. Business Rule Location

A business rule should exist in one authoritative location.

Examples:

```text
"KPI must be confirmed before becoming authoritative"
```

must not be independently reimplemented with slightly different behavior in:

- Android UI
- backend controller
- report generator

Backend/domain logic owns authoritative rules.

Android may mirror them for UX but must not become the only enforcement point.

---

# 144. Backend Is Authoritative

For business-critical state:

```text
Backend + PostgreSQL
```

are authoritative.

Android state and Room cache are projections/caches.

Never allow client-only state to become the sole record of confirmed business information.

---

# 145. Android UX May Be Optimistic Carefully

Optimistic UI is allowed where failure is reversible and behavior is safe.

Do not optimistically declare a maintenance report officially confirmed before the backend successfully persists confirmation.

Critical integrity operations require authoritative success.

---

# 146. API Retry Rule

Automatic retries must consider whether the operation is safe to repeat.

GET requests are generally safe.

POST confirmation/report-generation requests may not be.

Do not add generic retry middleware that can create duplicate business actions.

---

# 147. Observability Is Evidence, Not Decoration

Prometheus/Grafana/k6 should demonstrate actual engineering properties.

Useful questions include:

- How fast is the API?
- What happens under load?
- How long does report generation take?
- Are errors occurring?
- How does database behavior respond?

Avoid dashboards full of irrelevant metrics merely to create impressive screenshots.

---

# 148. Performance Targets

Do not invent arbitrary "enterprise-grade" SLA numbers.

Establish performance targets based on:

- observed baseline
- realistic application usage
- report generation complexity
- development environment

Record actual measured results honestly.

---

# 149. Testing Priority

When time is constrained, prioritize tests in this order:

1. Parser correctness
2. Confirmation/data integrity
3. Authentication/security
4. Report generation
5. Scheduling
6. Critical API integration
7. Android ViewModel/business-state behavior
8. Important UI flows
9. Decorative behavior

This hierarchy reflects business risk.

---

# 150. Parser Regression Suite

Every real parser bug that is fixed should produce a regression test whenever practical.

If a WhatsApp variation breaks parsing:

1. anonymize the input
2. add it as a test fixture
3. fix parser behavior
4. keep the test permanently

The parser should become stronger over time.

---

# 151. Manual Testing Record

For important end-to-end flows, maintain a short repeatable manual test checklist.

Example:

```text
[ ] Login
[ ] Paste report
[ ] Analyze
[ ] Correct extracted KPI
[ ] Save draft
[ ] Resume draft
[ ] Confirm
[ ] Dashboard updates
[ ] Generate Excel
[ ] Generate PDF
[ ] Share PDF
```

This protects against integration regressions AI-generated unit tests may miss.

---

# 152. Release Readiness

Before declaring FactoryFlow ready for presentation:

- core Must Have features complete
- application builds reliably
- backend starts reliably
- migrations work from clean database
- no known critical data-integrity bugs
- no exposed secrets
- parser regression suite passes
- primary end-to-end workflow works
- report files look professional
- screenshots are current
- README reflects reality
- architecture/docs reflect reality
- demo data is safe
- GitHub repository is clean

---

# 153. Demo Readiness

The application must be demoable without fragile manual preparation.

Maintain a predictable demo path.

Example:

```text
Launch backend
      ↓
Launch Android app
      ↓
Login with demo account
      ↓
Use anonymized example KPI message
      ↓
Analyze
      ↓
Correct one intentionally uncertain value
      ↓
Confirm
      ↓
Dashboard refresh
      ↓
Generate PDF/Excel
      ↓
Show history/share
```

A professional demo should show the problem and the engineering solution clearly.

---

# 154. Interview Readiness

The project should enable the developer to explain:

- why Kotlin instead of Flutter
- why Spring Boot
- how JWT authentication works
- how refresh tokens work
- how JPA persistence works
- why DTOs are separate
- parser architecture
- fuzzy matching
- why human validation is mandatory
- how OCR integrates
- how Android Share Intent works
- how report generation works
- why PDFBox was selected
- why Quartz was selected
- WebSocket/STOMP architecture
- RabbitMQ tradeoffs if implemented
- database model
- testing strategy
- Git workflow
- performance measurements

If the developer cannot explain an important feature, AI assistance has not fulfilled its learning objective.

---

# 155. Definition of Project Success

FactoryFlow succeeds when it demonstrates all of the following together:

### Business Value

It reduces repetitive KPI-reporting work.

### Reliability

It prevents uncertain machine-extracted information from silently becoming authoritative.

### Engineering Quality

Its architecture, code and data model are coherent.

### User Experience

It feels polished and efficient.

### Automation

It converts validated data into professional scheduled reports.

### Traceability

Historical reports and important actions can be understood later.

### Documentation

Another engineer can continue development.

### Portfolio Value

The project communicates real software engineering ability rather than simple framework familiarity.

---

# 156. Definition of Project Failure

FactoryFlow should be considered unsuccessful if it becomes any of the following:

- a collection of disconnected demos
- an OCR screen connected to a database
- a CRUD project with fancy technology names
- a dashboard using fake values
- a parser that silently guesses official data
- a mobile UI disconnected from the backend
- a backend with no usable mobile experience
- a GitHub repository with undocumented generated code
- an application whose author cannot explain its architecture
- a project containing many unfinished advanced technologies
- a visually polished prototype with unreliable business logic
- a technically strong backend with poor user experience

The project must remain balanced.

---

# 157. Final AI Agent Checklist

Before modifying FactoryFlow, ask:

```text
Do I understand the active task?
Have I read the relevant documentation?
Am I respecting approved architecture?
Am I changing scope without authorization?
Am I introducing unnecessary technology?
Am I preserving business integrity?
Am I keeping human confirmation mandatory?
Am I following the design system?
Can this implementation be tested?
Will documentation remain accurate?
Can this change become one coherent Git commit?
```

If any answer is uncertain, investigate before implementation.

---

# 158. Final Task Checklist

Before marking a task completed:

```text
[ ] Requirement implemented
[ ] Relevant edge cases handled
[ ] Business rules preserved
[ ] Backend builds
[ ] Android builds if affected
[ ] Relevant tests pass
[ ] No sensitive information introduced
[ ] No unnecessary dependency introduced
[ ] UI states handled if applicable
[ ] Documentation updated
[ ] TASKS.md updated
[ ] Report/GitHub evidence captured if valuable
[ ] Git diff reviewed
[ ] Focused commit prepared
```

---

# 159. Final Project Principle

FactoryFlow must never optimize solely for:

> "Does it run?"

The standard is:

> "Is it correct, understandable, maintainable, usable, defensible and professionally presented?"

Every decision should move the project toward that standard.

---

# 160. Final Goal

FactoryFlow is expected to become a flagship engineering project demonstrating the ability to understand a real industrial problem and transform it into a complete information system.

The finished platform should demonstrate:

- requirements analysis
- business-process understanding
- software architecture
- native Android engineering
- enterprise backend development
- relational database design
- security
- deterministic data processing
- OCR integration
- human-in-the-loop validation
- automated document generation
- scheduling
- real-time communication
- notifications
- asynchronous architecture where justified
- testing
- performance analysis
- technical documentation
- UI/UX design
- professional Git practices

FactoryFlow should be strong enough to discuss confidently during:

- final-year internship evaluation
- engineering interviews
- international internship applications
- graduate program applications
- scholarship applications
- technical portfolio reviews

The goal is not to prove that many technologies can be placed in one repository.

The goal is to prove that a real business problem can be understood, architected and solved with disciplined engineering.

---

# 161. Constitution Authority

This document is the highest-level operating authority of the FactoryFlow repository.

An AI agent or contributor must not silently override it.

When the project legitimately evolves:

1. discuss the architectural or product change
2. approve the new direction
3. update the relevant source-of-truth documents
4. update this constitution if necessary
5. implement the change

Documentation follows deliberate decisions.

Implementation does not silently redefine decisions.

---

# 162. Living Document Rule

`AGENTS.md` is a living document.

It may evolve as FactoryFlow evolves.

However, it should not be modified casually during routine feature implementation.

Changes to this document should represent meaningful changes to:

- project philosophy
- architecture
- scope
- development process
- engineering standards
- AI behavior
- source-of-truth hierarchy

Minor implementation details belong in the appropriate technical document instead.

---

# 163. Closing Statement

FactoryFlow is being built with AI assistance, but it must never feel AI-assembled.

Its architecture must be intentional.

Its code must be coherent.

Its design must be consistent.

Its decisions must be explainable.

Its documentation must preserve context.

Its Git history must tell a story.

Its reports must look professional.

Its data must remain trustworthy.

Its user experience must respect the engineer using it every day.

AI accelerates implementation.

Engineering judgment defines the product.

---

# End of AGENTS.md
