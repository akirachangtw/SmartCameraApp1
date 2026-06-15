# Repository Agent Bootstrap

This repository uses the defensive workflow defined in [devspec/Ultimate-Defensive-Hybrid-Workflow_Guide.md](devspec/Ultimate-Defensive-Hybrid-Workflow_Guide.md).

## Default Startup Rule

On every new task in this repository:

1. Read `devspec/Ultimate-Defensive-Hybrid-Workflow_Guide.md` first.
2. Adopt its role and constraints before planning, coding, or reviewing.
3. Treat its SSOT, scope-control, and defensive PDCA loop as repository defaults.
4. Load current state from `.claude/PROGRESS.md` when the task depends on project progress, open tickets, or restart context. Do not assume a root-level `PROGRESS.md` exists.

## Lean CSO Session Bootstrap

The CSO briefing system is index-first, not full-context-first. Use `plans/cso_briefing/CSO_INDEX.md` as the navigation entry point and `plans/cso_briefing/CSO_Session_Startup_SOP.md` as the loading policy.

For a new CSO-oriented session, keep only these always-on inputs:

1. `plans/cso_briefing/CSO_INDEX.md` - static document map and decision matrix.
2. The latest session handoff - prefer the newest `docs/session_handoff/SESSION_HANDOFF_*.md`, or a user-uploaded handoff if explicitly provided.
3. `.claude/PROGRESS.md` - current Dashboard and execution state.

Everything else is loaded on demand. Do not preload archive folders, old handoffs, previous SSOT versions, or broad `devspec/` history unless the current task explicitly requires historical traceability.

## CSO Decision Loading Matrix

Use this matrix before making or reviewing decisions:

| Decision type | Required context | Optional context |
| --- | --- | --- |
| Technical specification ruling | `plans/architecture/SSOT_v4_0_6.md` relevant sections | Active contract |
| Ticket repair or scope ruling | Active contract from `plans/cso_briefing/project/file/` + relevant SSOT appendix | `.claude/CLAUDE.md` for forbidden zones |
| Architecture change | SSOT + `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md` when UI/layout is involved | `.claude/rules/*.md` for domain rules |
| KAIROS / rule harvesting | `.claude/CLAUDE.md` + relevant `.claude/rules/*.md` | CSO knowledge base + `plans/cso_briefing/project/Postponed.md` |
| Cross-model workflow or effort routing | CSO knowledge base + `plans/cso_briefing/project/file/Sonnet4.6-Effort等級解析.md` | Current handoff |
| Pending or postponed work review | `plans/cso_briefing/project/Pending/` + `plans/cso_briefing/project/Postponed.md` | Related archived decision reports |

Current primary authorities:

- SSOT: `plans/architecture/SSOT_v4_0_6.md`. It supersedes v3.0-v3.3, Delta v3.4-v3.4.1, and v4.0-v4.0.4.
- Layout: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`.
- CSO index: `plans/cso_briefing/CSO_INDEX.md`.
- CSO startup policy: `plans/cso_briefing/CSO_Session_Startup_SOP.md`.
- Current CSO contract: `plans/cso_briefing/project/file/P08_Codebase_Sanitization_Contract.md` unless a newer handoff or ticket explicitly replaces it.
- Claude execution contract: `.claude/CLAUDE.md`.
- Progress dashboard: `.claude/PROGRESS.md`.

Obsolete or non-resident context:

- Do not preload old handoffs after a newer handoff supersedes them.
- Do not use gstack v1.5 as an active authority when the current CSO knowledge base or startup SOP points to gstack v2.0.
- Do not treat `ClaudeCode-Model-Effort.txt` as resident context; effort guidance is expected to be internalized into the current Slice Protocol / contract fields.
- Do not read archived CSO deliverables unless tracing a specific historical decision.

## Session Open / Close Rules

At session open:

1. Confirm the latest handoff's Done, Current State, TODO, and Key Decisions.
2. Check `.claude/PROGRESS.md`.
3. Inspect `plans/cso_briefing/project/Pending/` for active tickets when implementation or execution planning is requested.
4. Check `plans/cso_briefing/project/Postponed.md` when the task touches postponed decisions, KAIROS review, or strategic sequencing.
5. Use the CSO index decision matrix to load only the files required by the current question.

At session close or context handoff:

1. Produce a compact handoff with Done, Current State, TODO, Key Decisions, and exact next files to read.
2. Preserve SSOT names, ticket IDs, forbidden zones, and unresolved blockers exactly.
3. Do not claim verification passed unless the verification command or review was actually run in the current session.
4. Move completed or postponed work through the project `Pending/`, archive, or `Postponed.md` flow only when explicitly part of the task.

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
