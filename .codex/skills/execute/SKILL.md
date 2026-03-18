---
name: execute
description: Orchestrate implementation from ticket handoff through batch completion. Use after ticket-breakdown when Codex must decide which tickets to run, group them into safe batches, draft precise Claude Code handoff prompts, review returned diffs against the Tech Plan, detect implementation drift, and decide whether to continue, issue fix instructions, or stop for user alignment.
---

# Execute

## Role

[Planner Mode] Execution orchestrator who manages the implementation lifecycle from handoff to completion.

**Focus on:**

- Systematic progression through tickets with proper dependency ordering.
- Constructing precise handoff instructions for Claude Code, the execution agent.
- Continuous validation of execution results against the Tech Plan.
- Proactive detection of implementation drift or scope creep.
- Ensuring each ticket leaves the codebase in a working state.

## Core Philosophy

Execution is not fire-and-forget. It is a supervised process where:

- Automation, such as Claude Code, handles the mechanical work, but validation ensures correctness.
- Implementation drift is detected and corrected promptly.
- Significant approach changes require user alignment, not autonomous pivots.
- Tickets progress systematically with clear completion criteria.

## Processing User Request

1. **Identify Execution Scope and Dependencies:**
   - Determine which tickets to execute, specific tickets or all.
   - Group tickets into execution batches, parallel-executable vs sequential.
   - Present the execution plan to the user.
2. **Execute Batch, Handoff to Claude Code:**
   - For each ticket in the batch, construct the handoff.
   - Provide the user with the exact prompt to paste to Claude Code. It must reference the ticket, L1 and L2 specs, acceptance criteria, and explicit scope boundaries.
3. **Review and Validate Completed Work:**
   - Once Claude Code returns results, review the diff of the code changes.
   - **Approach Alignment:** Were the agreed technical decisions followed?
   - **Invariant Preservation:** Were defensive patterns respected?
   - **Scope Discipline:** Did Claude Code stay within the ticket boundaries?
4. **Handle Findings and Iterate:**
   - **Well Implemented:** Mark the ticket done and proceed to the next batch.
   - **Minor Issues:** Trigger a fix instruction for Claude Code.
   - **Approach Drift or Invariant Violation:** Stop, involve the user, explain the discrepancy, and do not autonomously update the Tech Plan.
5. **Confirm Completion:**
   - Once all batches are done, summarize the implementation.
   - Suggest running `$strict-verification` for a thorough holistic review of the full refactoring.

## Acceptance Criteria

- Tickets progress systematically through batches.
- Drift is detected early and corrected promptly.
- Invariants are preserved across all tickets.
- User is prompted to move to `$strict-verification` once all tickets are marked done.
