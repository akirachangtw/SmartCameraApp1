# Repository Agent Bootstrap

This repository uses the defensive workflow defined in [devspec/Ultimate-Defensive-Hybrid-Workflow_Guide.md](devspec/Ultimate-Defensive-Hybrid-Workflow_Guide.md).

## Default Startup Rule

On every new task in this repository:

1. Read `devspec/Ultimate-Defensive-Hybrid-Workflow_Guide.md` first.
2. Adopt its role and constraints before planning, coding, or reviewing.
3. Treat its SSOT, scope-control, and defensive PDCA loop as repository defaults.

## Required Working Style

- Prefer defense over speed.
- Guard scope aggressively; do not edit unrelated files.
- Respect SSOT contracts; do not invent replacement models, states, or outputs.
- Use minimal changes unless the guide explicitly requires architectural extraction.
- If the requested change conflicts with a scope ban or requires crossing module boundaries, stop and return to architecture review instead of improvising.

## Workflow Mapping

Use the repository skills in this order when the task matches:

1. `$trigger-workflow` for initial grounding and requirement shaping.
2. `$architecture-review` for technical plan design and defensive validation.
3. `$ticket-breakdown` for scoped implementation tickets.
4. `$execute` for controlled implementation handoff/execution.
5. `$strict-verification` for final quality gate.

## Camera / Android Safety Rules

- Assume CameraX and hardware interactions have lifecycle, threading, and physical constraints.
- Prefer decoupling high-frequency visual tracking from low-frequency hardware focus actions.
- Treat front camera as fixed-focus unless proven otherwise by the code and device capability path.
- Reject thread leaks, lifecycle races, and hidden fallback behavior.

## Review Rule

After implementation, inspect the diff for unauthorized file changes and require cleanup before approval.
