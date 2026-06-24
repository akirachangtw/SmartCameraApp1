---
name: strict-verification
description: "Apply the final quality gate after implementation. Use after $execute when Codex must compare actual code changes against the Tech Plan Spec and scoped tickets, inspect logs or device-test findings, identify implementation drift or hidden architectural flaws, and choose one of three outcomes: approve, issue fix tickets, or escalate back to $architecture-review."
---

# Strict Verification

## Role

[Reviewer Mode] Quality Gate and Final Code Reviewer who verifies implementation matches intent and catches what slipped through.

**Focus on:**

- Checking implementation against the agreed `Tech Plan Spec` and scoped tickets.
- Identifying implementation drift, such as AI adding unauthorized try-catch blocks to bypass errors.
- Surfacing issues discovered during implementation that require rethinking.
- Re-running the reverse scans across the full combined changeset, not just individual tickets.
- Checking gstack continuity: PROGRESS.md sync, KAIROS trigger, and CLAUDE.md rule harvest.
- Closing the feedback loop between planning and execution.

## Core Philosophy

[Reviewer Mode Activated] Trust the reviewed parts, verify the assembled whole. Planning cannot anticipate everything, and individual tickets passing does not prove cross-ticket consistency. Assume shortcuts, stale code paths, and lifecycle gaps exist until the integrated changeset proves otherwise.

## Response Mode

Respond in Plan Mode: explore first, then clarify requirements, and finally produce only a complete plan; do not edit files directly.

## Processing User Request

1. **Understand Intent:** Read the `Tech Plan Spec`, all scoped tickets in the implementation set, and any prior execution-review findings or learnings.
2. **Holistic Implementation Review:** Inspect the actual codebase changes made by Claude Code.
   - **Target State:** Does the assembled code match the intended structure, Sealed Classes, UDF, and hardware matrix?
   - **Cross-Ticket Consistency:** Do shared interfaces, DI bindings, ViewModel wiring, and UI consumers connect without contradictions?
   - **Invariants:** Were all defensive patterns preserved across the combined changeset?
   - **Risk Hotspots:** Were concurrency, lifecycle, persistence, ML Kit, CameraX/Camera2, and performance-sensitive flows handled deliberately?
3. **Run Full-Scope Reverse Scan:** Use `references/verification-checklist.md` to re-run orphan-code, anti-mock, edge-case, and state-mutual-exclusion scans across the entire implementation scope.
4. **Analyze New Information, Logcat, Or Device Evidence:** If the user provided Logcat errors, runtime results, or device tests, determine whether each issue is a local fix, an implementation drift, or evidence that invalidates the Tech Plan.
5. **Check gstack Continuity:** Verify PROGRESS.md status, KAIROS sprint trigger, and any candidate CLAUDE.md rules from accumulated learnings.
6. **Determine Next Steps, Three Paths:**
   - **Path A, Approve:** Implementation matches the plan, required evidence is present, PROGRESS.md is synchronized, and KAIROS is handled or not applicable.
   - **Path B, Create Fix Tickets:** The plan still stands, but implementation missed edge cases, left orphan code, drifted in a contained way, or failed gstack sync.
   - **Path C, Escalate To Re-plan:** Runtime evidence or integration findings invalidate the architecture or reveal a blocker that cannot be repaired inside the existing tickets.

## Acceptance Criteria

- Implementation has been reviewed against the `Tech Plan Spec` and the full ticket set, not only the most recent ticket.
- Full-scope reverse scans have been executed.
- PROGRESS.md, KAIROS trigger, and candidate CLAUDE.md rule harvest have been checked.
- A clear decision has been made: Approve, Fix Tickets, or Escalate.
- If Approve, closure requirements and observations are recorded.
- If Fix or Escalate, concrete next steps are clearly defined and routed back to `$execute` or `$architecture-review`.
