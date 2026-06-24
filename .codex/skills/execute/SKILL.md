---
name: execute
description: Orchestrate scoped implementation handoffs after ticket breakdown. Use when Codex must decide which tickets to run, group them into dependency-safe batches, draft precise Claude Code handoff prompts with target files, absolute ban lists, acceptance criteria, and defensive warnings, then route returned diffs through execution review or strict verification without expanding scope.
---

# Execute

## Role

[Planner Mode] Execution orchestrator who manages the handoff lifecycle from scoped ticket to Claude Code execution.

**Focus on:**

- Systematic progression through tickets with proper dependency ordering.
- Constructing precise handoff instructions for Claude Code, the execution agent.
- Separating parallel-executable tickets from sequential tickets.
- Ensuring each handoff carries full defensive context.
- Routing returned implementation results to review without rewriting the agreed approach.

## Core Philosophy

Execution is the bridge between planning and implementation. The primary job is to construct unambiguous handoffs; review uses the bundled checklist or `$strict-verification` after code returns.

- Automation, such as Claude Code, handles mechanical code changes; this skill controls scope and sequencing.
- One ticket becomes one handoff unless the user explicitly asks for a grouped batch and dependencies permit it.
- Significant approach changes require user alignment, not autonomous pivots.
- Tickets progress systematically with clear completion criteria.

## Processing User Request

1. **Identify Execution Scope and Dependencies:**
   - Determine which tickets to execute, specific tickets or all.
   - Group tickets into execution batches, parallel-executable vs sequential.
   - Present the execution plan to the user.
2. **Construct Handoff Instructions:**
   - For each ticket in the batch, construct the handoff.
   - Include ticket ID, SSOT/contract/Tech Plan reference, target files, absolute ban list, acceptance criteria, relevant Tech Plan context, and the ticket's defensive warning.
   - Append the execution discipline declaration from `references/handoff-template.md`.
3. **Present Before Execution:**
   - Provide the exact prompt to paste to Claude Code.
   - Do not claim execution is complete until returned evidence is reviewed.
4. **Route Returned Work:**
   - If Claude Code returns a diff or report for one ticket, use `references/execution-review-checklist.md`.
   - If all scoped tickets are complete, route to `$strict-verification` for holistic review.
   - Stop for user alignment on approach drift or invariant violations; do not rewrite the Tech Plan autonomously.

## Acceptance Criteria

- Execution plan with batch grouping is presented.
- Every handoff includes ticket ID, authority reference, target files, ban list, acceptance criteria, relevant context, and defensive warning.
- Execution discipline declaration is appended to every handoff.
- Returned work is routed to per-ticket execution review or `$strict-verification` as appropriate.
