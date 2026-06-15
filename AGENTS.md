# Repository Agent Bootstrap

This repository uses the CSO-centric, cost-optimized workflow defined in [devspec/gstack_v2_0.md](devspec/gstack_v2_0.md).

## Default Startup Rule

On every new task in this repository:

1. Read `devspec/gstack_v2_0.md` first.
2. Adopt its CSO-centric role split, Context Firewall, cost-routing, and cross-model review constraints before planning, coding, or reviewing.
3. Treat SSOT authority, scope control, and defensive verification as repository defaults.
4. Load `.claude/PROGRESS.md` when the task depends on project progress, active tickets, restart context, or session health. Do not assume a root-level `PROGRESS.md` exists.
5. Load additional CSO files only through the `CSO Decision Loading Matrix` below.

## Lean CSO Session Bootstrap

The CSO briefing system is index-first, not full-context-first. Use `plans/cso_briefing/CSO_INDEX.md` as the navigation entry point and defer all nonessential files until the current task requires them.

For a CSO-oriented session, keep only these always-on inputs:

1. `plans/cso_briefing/CSO_INDEX.md` - static document map and decision matrix.
2. The latest session handoff - prefer the newest `docs/session_handoff/SESSION_HANDOFF_*.md`, or a user-uploaded handoff if explicitly provided.
3. `.claude/PROGRESS.md` - current Dashboard and execution state.

Everything else is loaded on demand. Do not preload archive folders, old handoffs, previous SSOT versions, legacy workflow files, or broad `devspec/` history unless the current task explicitly requires historical traceability.

## CSO Decision Loading Matrix

Use this matrix before making or reviewing CSO decisions. Load only the rows required by the current task; do not preload optional archives or obsolete contracts.

| ID | 文件 | Filesystem 路徑 | 讀取時機 |
|----|------|----------------|---------|
| SSOT | 唯一真理文件 v4.0.6 | `plans/architecture/SSOT_v4_0_6.md` | 任何技術規格判斷 |
| LAYOUT | Layout Spec v2.5.1 | `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md` | UI 佈局判斷 |
| KB | CSO Knowledge Base v3 | `plans/cso_briefing/project/file/Cueshot_CSO_Knowledge_Base_v3.md` | 產品 DNA / gstack v2.0 / 拷問模板 / 治理體系 |
| EFFORT | Model Effort 解析 | `plans/cso_briefing/project/file/Sonnet4.6-Effort等級解析.md` | Mode / Effort 指定 |
| TICKET_TPL | Ticket 範本 v3 | `plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md` | 開票格式參考 |
| CLAUDE | Claude Code 行為契約 | `.claude/CLAUDE.md` | 規則收割 / KAIROS |
| RULES | Claude Code 路徑規則 | `.claude/rules/*.md` | 特定領域審查 |
| PROGRESS | 進度 Dashboard | `.claude/PROGRESS.md` | Session 開場確認狀態 |

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
