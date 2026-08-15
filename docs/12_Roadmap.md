# 12_Roadmap.md

> **FactoryFlow — Product & Engineering Roadmap**
>
> Version: 1.0  
> Status: Active  
> Last updated: 2026-08-11
>
> This document summarizes the implementation roadmap.
>
> `TASKS.md` remains the detailed living task tracker.
>
> This file presents the roadmap at project/milestone level for easier planning, documentation, GitHub, and report use.

---

# 1. Roadmap Principle

FactoryFlow must prioritize:

```text
business value
→ trusted data
→ complete workflow
→ premium UX
→ optional sophistication
```

The project should not sacrifice core completion for unnecessary infrastructure.

---

# 2. Delivery Context

Initial implementation window:

```text
approximately 3 weeks
```

Therefore:

```text
finished core > unfinished advanced architecture
```

---

# 3. Phase 0 — Foundation

Goals:

- repository structure
- operating documentation
- backend bootstrap
- Android bootstrap
- PostgreSQL
- Flyway
- OpenAPI
- design system foundation

Deliverables:

```text
AGENTS.md
TASKS.md
SKILLS.md
DESIGN.md
UI_UX.md
docs/*
backend/
android/
```

---

# 4. Phase 1 — Trusted KPI Core

This is the most important phase.

Features:

- users
- authentication
- JWT
- refresh token
- KPI definitions
- aliases
- deterministic parser
- normalization
- fuzzy matching
- numeric extraction
- confidence/warnings
- drafts
- confirmation
- persistence
- manual entry
- pasted text
- Excel generation

Success:

```text
Paste realistic message
→ analyze
→ review
→ edit
→ save draft
→ resume
→ confirm
→ generate Excel
```

---

# 5. Phase 2 — Operational Product

Features:

- dashboard
- report history
- filters/search
- report detail
- PDF generation
- generated file history
- native sharing
- user email handoff
- Quartz schedules
- backend automatic email

Success:

```text
confirmed data
→ dashboard
→ history
→ Excel/PDF
→ schedules
→ delivery
```

---

# 6. Phase 3 — Mobile Acquisition

Features:

- gallery import
- PaddleOCR OCR
- Android Share Intent
- unified acquisition UI

Signature success flow:

```text
WhatsApp image
→ Share to FactoryFlow
→ OCR
→ parser
→ review
→ confirm
```

---

# 7. Phase 4 — Realtime & Notifications

Features:

- WebSocket/STOMP
- realtime dashboard refresh
- FCM
- in-app notifications
- notification deep links

Success:

```text
business event
→ realtime/push
→ Android refresh/navigation
```

---

# 8. Phase 5 — Statistics

Features:

- KPI trends
- averages
- min/max
- variations
- period filters
- polished charts

Only confirmed data.

---

# 9. Phase 6 — Optional Engineering Enhancements

Only after core is complete.

Potential:

- RabbitMQ
- Resilience4j
- Actuator
- Prometheus
- Grafana
- k6
- advanced POI/PDF styling

---

# 10. Phase 7 — Hardening

Focus:

- security review
- DB review
- API consistency
- Android UX polish
- regression tests
- failure-state testing
- real-device testing
- performance sanity
- privacy review

---

# 11. Phase 8 — Academic & Portfolio Delivery

Deliverables:

- architecture diagrams
- UML
- screenshots
- parser test evidence
- generated reports
- README
- demo GIF/video
- academic report section
- GitHub showcase

---

# 12. MVP Scope

Must deliver:

```text
authentication
dashboard
KPI definitions
manual entry
paste text
gallery OCR
share OCR
human confirmation
drafts
PostgreSQL
history/search/filter
Excel
PDF
Quartz daily/weekly/monthly
Swagger/OpenAPI
```

---

# 13. Strongly Recommended

```text
WebSocket/STOMP
FCM
statistics
device-side share/email
```

---

# 14. Optional

```text
RabbitMQ
Resilience4j
Prometheus
Grafana
k6
advanced document styling
```

---

# 15. Future

```text
natural-language historical queries
predictive maintenance
anomaly detection
forecasting
ERP/SAP integration
multi-role authorization
multi-site deployment
```

---

# 16. Explicitly Excluded From Core

```text
Flutter
LLM official extraction
Spring Batch
MinIO
Docker requirement
microservices
Kubernetes
complex RBAC
```

---

# 17. Suggested Week 1

Target:

```text
foundation
auth
KPI catalog
parser
parser tests
draft persistence
confirmation backend
paste flow
confirmation UI
```

Do not leave parser testing until the end.

---

# 18. Suggested Week 2

Target:

```text
manual entry
Excel
dashboard
history
PDF
file actions
Quartz
scheduled email
```

---

# 19. Suggested Week 3

Target:

```text
gallery OCR
Share Intent
realtime
FCM
statistics
hardening
report evidence
README/UML
```

If delayed:

cut optional features before damaging core quality.

---

# 20. Milestone Tags

Suggested:

```text
v0.1-trusted-kpi-core
v0.2-operational-reporting
v0.3-mobile-intelligence
v1.0-demo
```

---

# 21. M0 Acceptance

```text
[ ] repo structure
[ ] docs
[ ] backend runs
[ ] Android runs
[ ] DB/Flyway
[ ] Swagger
```

---

# 22. M1 Acceptance

```text
[ ] login
[ ] KPI definitions
[ ] parser
[ ] real format tests
[ ] drafts
[ ] confirmation
[ ] manual/paste
[ ] Excel
```

---

# 23. M2 Acceptance

```text
[ ] dashboard
[ ] history
[ ] filters
[ ] PDF
[ ] generated files
[ ] share
[ ] Quartz
[ ] scheduled email
```

---

# 24. M3 Acceptance

```text
[ ] gallery
[ ] OCR
[ ] Share Intent
[x] Direct camera acquisition removed from approved scope
[ ] unified acquisition
```

---

# 25. M4 Acceptance

```text
[ ] WebSocket if included
[ ] FCM if included
[ ] statistics if included
```

---

# 26. Final Product Demo

Ideal:

```text
Login
→ Dashboard
→ Share WhatsApp screenshot
→ OCR
→ parser
→ review warning
→ correct value
→ confirm
→ dashboard refresh
→ generate PDF
→ share
```

---

# 27. Cut Strategy

If time becomes critical, cut in this order:

```text
1. Grafana
2. Prometheus
3. Resilience4j
4. RabbitMQ
5. advanced document styling
6. non-essential statistics
```

Do not cut:

```text
human confirmation
parser tests
core acquisition
report generation
history
```

---

# 28. Report Priority

Capture evidence for:

```text
problem
acquisition pipeline
parser
human validation
architecture
Excel/PDF
dashboard
scheduling
```

---

# 29. Portfolio Priority

Show:

```text
premium Android UI
real workflow
clean architecture
tests
Swagger
generated reports
Git history
README
```

---

# 30. Long-Term Product Roadmap

After MVP:

```text
Phase A — historical intelligence
Phase B — anomaly detection
Phase C — predictive maintenance
Phase D — ERP integration
Phase E — multi-site / advanced authorization
```

---

# 31. Roadmap Governance

`TASKS.md` is updated continuously.

This roadmap changes only when project direction changes materially.

Do not use this file as a second detailed task tracker.

---

# 32. Final Roadmap Principle

FactoryFlow succeeds by finishing the workflow that solves the real maintenance problem.

Advanced technology is valuable only after that workflow is complete.

---

# End of 12_Roadmap.md
