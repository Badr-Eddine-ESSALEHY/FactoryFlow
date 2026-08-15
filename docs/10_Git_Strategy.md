# 10_Git_Strategy.md

> **FactoryFlow — Git Strategy & Repository Discipline**
>
> Version: 1.0  
> Status: Active  
> Last updated: 2026-08-11
>
> This document defines how Git must be used during FactoryFlow development.
>
> The goal is to produce a repository whose history explains the engineering work clearly enough for:
>
> - future maintenance
> - internship report evidence
> - portfolio review
> - debugging
> - AI-assisted development
>
> Git history is part of the engineering deliverable.

---

# 1. Core Principle

Each commit should represent **one coherent engineering change**.

Good history:

```text
feat(auth): implement JWT authentication
feat(parser): add deterministic KPI label matching
test(parser): cover decimal separator variants
feat(android): build KPI confirmation screen
feat(report): generate PDF reports with PDFBox
```

Bad history:

```text
update
changes
final
fix
more work
everything
```

---

# 2. Safety Rules

Never:

- force-push unless explicitly requested
- push automatically without explicit instruction
- delete branches casually
- rewrite shared history casually
- commit credentials
- commit generated secrets
- commit private database dumps
- commit confidential screenshots without review

AI agents may propose commits.

AI agents may create local commits only when the current instruction/session explicitly allows it.

---

# 3. Branch Strategy

For this project, keep branching simple.

Recommended:

```text
main
```

for stable integrated work.

Feature branches may be used when useful:

```text
feat/auth
feat/parser
feat/android-confirmation
feat/report-generation
```

Do not create complex GitFlow unless collaboration requirements justify it.

---

# 4. Main Branch Rule

`main` should remain buildable whenever practical.

Before merging significant work:

```text
build
tests
diff review
```

should be completed.

---

# 5. Conventional Commits

Use Conventional Commits.

Allowed common types:

```text
feat
fix
test
docs
refactor
chore
build
ci
perf
style
```

---

# 6. Scope Naming

Use meaningful scopes.

Examples:

```text
auth
parser
report
dashboard
android
android-auth
ocr
image-ocr
history
scheduler
email
realtime
notifications
database
api
docs
uml
observability
```

---

# 7. Commit Format

Preferred:

```text
type(scope): concise imperative description
```

Examples:

```text
feat(parser): support fuzzy KPI label matching
fix(report): preserve missing values during confirmation
test(parser): cover duplicate KPI inputs
docs(api): define generated report contract
refactor(android): simplify confirmation state handling
```

---

# 8. Commit Size

A commit should be:

- small enough to understand
- large enough to represent a complete coherent change

Avoid both extremes:

```text
one-character commits
```

and:

```text
three days of unrelated work in one commit
```

---

# 9. Task-to-Commit Mapping

`TASKS.md` should record:

```text
Task ID
Suggested commit
Actual commit
Evidence
Status
```

Example:

```text
FF-1203
feat(parser): add deterministic fuzzy KPI matching
```

This makes implementation progress traceable.

---

# 10. Before Commit Checklist

Before creating a commit:

```text
[ ] active task complete
[ ] relevant tests run
[ ] build passes where applicable
[ ] git status reviewed
[ ] git diff reviewed
[ ] no unrelated edits
[ ] no secrets
[ ] no debug leftovers
[ ] docs updated if behavior changed
[ ] TASKS.md updated
```

---

# 11. `git status`

Always inspect:

```bash
git status
```

before committing.

Understand every changed/untracked file.

---

# 12. `git diff`

Review:

```bash
git diff
```

and staged diff:

```bash
git diff --staged
```

Do not commit files without understanding why they changed.

---

# 13. Staging

Prefer selective staging.

Example:

```bash
git add backend/src/...
git add docs/06_API.md
```

Avoid blindly staging everything if unrelated files exist.

---

# 14. Documentation Commits

Documentation work should also be coherent.

Examples:

```text
docs(project): establish FactoryFlow project vision
docs(architecture): document unified system architecture
docs(database): define persistence model
docs(ux): specify complete Android interaction flows
```

---

# 15. Refactor Commits

A refactor commit should not intentionally change behavior.

Example:

```text
refactor(parser): separate normalization from label matching
```

If behavior changes, use `feat` or `fix` accordingly.

---

# 16. Fix Commits

A bug fix should explain the problem.

Good:

```text
fix(parser): preserve empty KPI values instead of converting to zero
```

Bad:

```text
fix(parser): bug fix
```

---

# 17. Test Commits

Use dedicated test commits when meaningful.

Example:

```text
test(parser): add real-world WhatsApp format regression cases
```

Tests may also be committed with the feature if tightly coupled.

---

# 18. Build Commits

Use for build/dependency/tooling changes.

Examples:

```text
build(backend): add Spring WebSocket dependency
build(ocr): configure PaddleOCR runtime
```

Do not hide feature implementation under `build`.

---

# 19. Database Migration Commits

Schema changes should clearly indicate purpose.

Examples:

```text
feat(database): add KPI definition and alias tables
feat(database): persist report schedules
```

Flyway migrations belong in the same coherent change as the feature using them when practical.

---

# 20. No Secret Commits

Never commit:

```text
JWT secrets
database passwords
SMTP passwords
Firebase Admin credentials
private keys
real refresh/access tokens
```

Use environment configuration.

---

# 21. `.gitignore`

Must cover:

- IDE files
- build output
- local environment files
- temporary generated reports
- local DB artifacts
- secret files
- Android local properties
- OS junk

Do not ignore source assets accidentally.

---

# 22. Android Git Hygiene

Do not commit:

```text
android/local.properties
build/
.gradle/
captures/
```

unless a specific file is intentionally required.

---

# 23. Backend Git Hygiene

Do not commit:

```text
target/
build/
runtime report files
local secrets
logs
```

---

# 24. Generated Report Files

Sample anonymized generated reports may be committed under a dedicated demo/evidence folder if useful.

Operational runtime report output should not be committed.

---

# 25. Evidence Files

Report/portfolio evidence may be versioned if:

- sanitized
- meaningful
- not huge
- not confidential

Recommended:

```text
report/evidence/factoryflow/
```

---

# 26. Commit Evidence Link

When a milestone creates report evidence, record it in `TASKS.md`.

Example:

```text
Commit:
feat(android): build KPI confirmation workflow

Evidence:
report/evidence/factoryflow/confirmation_low_confidence.png
```

---

# 27. Merge Strategy

For small-team work:

- merge commits or squash may both be acceptable
- preserve coherent history
- avoid unnecessary merge noise

If using squash, ensure final message is meaningful.

---

# 28. Rebase Rule

Rebase local unpublished work if useful.

Do not rebase shared/public history casually.

---

# 29. Force Push Rule

Default:

```text
FORBIDDEN
```

unless user explicitly requests and understands the consequence.

---

# 30. Push Rule

AI must never assume permission to push.

Local commit permission does not imply remote push permission.

---

# 31. Branch Deletion

Delete only branches known to be merged/obsolete.

Do not delete remote branches without explicit instruction.

---

# 32. Reset Rule

Avoid destructive:

```bash
git reset --hard
```

unless explicitly approved.

Prefer safe recovery approaches.

---

# 33. Clean Rule

Avoid:

```bash
git clean -fd
```

without explicit review/permission.

Untracked files may contain valuable work.

---

# 34. Stash

Use stash carefully.

Name it:

```bash
git stash push -m "wip parser refactor"
```

Do not hide work indefinitely.

---

# 35. Conflict Resolution

When conflicts occur:

1. understand both changes
2. preserve intended behavior
3. run tests
4. review diff

Never choose "ours" or "theirs" blindly.

---

# 36. Commit Order

Prefer history that follows implementation logic.

Example:

```text
build(database): configure PostgreSQL and Flyway
feat(auth): add user persistence
feat(auth): implement JWT authentication
test(auth): cover login and refresh flow
```

---

# 37. Milestone Tags

Optional useful tags:

```text
v0.1-trusted-kpi-core
v0.2-operational-reporting
v0.3-mobile-intelligence
v1.0-demo
```

Tag only meaningful stable milestones.

---

# 38. Release Tag Rule

Do not tag broken or incomplete milestones.

---

# 39. Commit Message Language

Use English for Git history for consistency unless repository policy later changes.

Documentation/report content may be French or English.

---

# 40. Repository Root

Expected:

```text
FactoryFlow/
├── backend/
├── android/
├── docs/
├── assets/
├── diagrams/
├── report/
├── scripts/
├── AGENTS.md
├── TASKS.md
├── SKILLS.md
├── DESIGN.md
├── UI_UX.md
└── README.md
```

Do not mix previous project repositories into this one.

---

# 41. Old Project Separation

FactoryFlow is separate from the Dosage Analysis project.

Do not:

- copy old history
- reuse repository identity
- mix unrelated commits
- accidentally retain old app names/config

---

# 42. AI Session Workflow

At the start of an implementation session:

```text
git status
git branch --show-current
git log --oneline -n 10
```

Then read:

```text
AGENTS.md
TASKS.md
relevant docs
```

---

# 43. AI End-of-Task Workflow

At end:

```text
run tests
run build
git status
git diff
update TASKS
update relevant docs
propose commit
```

Commit locally only if authorized.

---

# 44. Commit Proposal Format

AI should propose:

```text
Suggested commit:
feat(parser): support configurable fuzzy KPI matching
```

If multiple coherent changes exist, propose multiple commits.

---

# 45. No Giant AI Commit

AI must not generate:

```text
feat: implement entire FactoryFlow
```

with dozens of unrelated modules.

---

# 46. No Formatting Noise

Do not reformat entire repository while implementing one feature unless formatting task is explicit.

Formatting noise makes review difficult.

---

# 47. Dependency Changes

Dependency changes should be visible in commit scope.

Example:

```text
feat(ocr): add PaddleOCR provider integration
```

Do not bundle random upgrades.

---

# 48. Lockfile/Build Files

Commit required build metadata consistently.

Do not manually edit generated files unless expected by build tool.

---

# 49. GitHub Portfolio Quality

The history should allow a recruiter to see progression:

```text
foundation
auth
parser
confirmation
reporting
OCR
automation
realtime
hardening
```

This communicates engineering discipline.

---

# 50. Final Git Principle

Git is not only backup.

For FactoryFlow it is:

```text
engineering history
+
task traceability
+
report evidence
+
portfolio proof
```

Every commit should help a future reader understand what changed and why.

---

# End of 10_Git_Strategy.md
