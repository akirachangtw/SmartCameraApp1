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
- Closing the feedback loop between planning and execution.

## Core Philosophy

[Reviewer Mode Activated] Trust but verify. Planning cannot anticipate everything. Implementation reveals realities that were not visible during analysis. The goal is correctness, not blame. Assume the coding agent made shortcuts unless proven otherwise.

## Processing User Request

1. **Understand Intent:** Read the `Tech Plan Spec` and the specific ticket that was just executed.
2. **Review Implementation:** Inspect the actual codebase changes made by Claude Code.
   - **Target State:** Does the refactored code match the defined Sealed Classes and UDF?
   - **Invariants:** Were the defensive patterns preserved? Did the agent hack around a problem?
3. **Analyze New Information, Logcat or Errors:** If the user provided Logcat errors from device testing, determine whether this is a minor syntax issue or a fundamental architectural flaw.
4. **Determine Next Steps, Three Paths:** Based on the findings, take one of three paths:
   - **Path A, Approve:** Implementation perfectly matches the plan. Instruct the user to record critical learnings into `devspec/feature_record_Backlog.md`. Close the workflow.
   - **Path B, Create Fix Tickets:** Drift detected, such as minor bugs or missed areas. Create highly specific fix tickets and instruct the user to run them with Claude Code. Suggest running `$execute`.
   - **Path C, Escalate to Re-plan:** Significant issues, such as Logcat revealing a hardware limitation that invalidates the approach. Escalate to re-plan the architecture by returning to `$architecture-review`.

## Acceptance Criteria

- Implementation has been reviewed against the `Tech Plan Spec`.
- A clear decision has been made: Approve, Fix Tickets, or Escalate.
- If Approve, SSOT documentation updates are requested.
- If Fix or Escalate, concrete next steps are clearly defined.
