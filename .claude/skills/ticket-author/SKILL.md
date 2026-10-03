---
name: ticket-author
description: Diagnose an issue, turn an approved specification into a minimal Ticket Map, or author one evidence-backed formal Ticket. Manual modes: diag, plan, ticket.
argument-hint: "diag|plan|ticket [task text and @sources]"
disable-model-invocation: true
user-invocable: true
---

# Ticket Author P0

## Mission

Run Akira's staged workflow:

`Perplexity or Claude Desktop discussion → diagnosis/specification → Ticket Map → per-ticket live evidence → formal Ticket`

This skill performs diagnosis, planning, evidence collection, and Ticket authoring. It MUST NOT implement production code, execute a Ticket, or treat AI-generated material as sufficient live proof.

## Invocation

Invocation payload:

```text
$ARGUMENTS
```

Parse the first non-whitespace token as `MODE`. Accepted modes:

```text
/ticket-author diag
/ticket-author plan
/ticket-author ticket
```

The mode selects the workflow stage; it is not the task description. Treat the remaining arguments, current user message, `@file` references, and attachments as the task payload.

If mode is absent or unsupported, stop with `STOP_INVALID_MODE`. Never infer a mode silently.

## Required references

Read only what the selected mode requires:

- Always read `references/input-contract.md`.
- Always read `references/state-transitions.md`.
- Always read `references/stop-conditions.md`.
- In `ticket`, also fully read the latest `D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md` before creating, reviewing, or modifying a formal Ticket.
- If Craft synchronization is explicitly requested, fully read `Craft_MCP_Protocol.md` before any Craft operation.

A prior session summary does not substitute for a mandatory current read.

## Core rules

1. Deliver the smallest useful result for the selected mode.
2. Reuse existing project tools, ticket writers, templates, and evidence mechanisms.
3. Ask the user only for unresolved product semantics, compatibility policy, scope expansion, ownership, destructive action, or another non-derivable decision.
4. Automatically repair uniquely resolvable factual, formatting, consistency, and template defects, then rerun the complete applicable gate.
5. Label load-bearing claims as `VERIFIED`, `INFERRED`, `CONFLICTED`, or `UNRESOLVED`.
6. Local live evidence governs local implementation facts. Preserve conflicts with parent or external sources and surface semantic consequences.
7. Source diagnoses/specifications provide intent and prior analysis, not fresh proof of current repository state.
8. Never add a service, daemon, database, workflow engine, or governance layer for this P0 workflow.
9. Never modify production code in any mode.
10. Never create a formal Ticket in `diag` or `plan`.

## Source precedence

For implementation facts:

1. Current local repository, runtime, registry, and direct tool output.
2. Current project SSOT and contracts.
3. Version-pinned first-party history.
4. Parent diagnosis/specification.
5. External official source.
6. AI-generated analysis.

For product semantics:

1. Explicit current user decision.
2. Approved parent specification or decision record.
3. Current project SSOT.
4. Historical Ticket/discussion.
5. AI recommendation.

A semantic decision may choose desired behavior but cannot overwrite an observed baseline; record the gap.

## Mode: diag

Purpose: determine what is happening, where, why, and which repair directions remain credible.

Procedure:

1. Parse the symptom/question, sources, target, and constraints.
2. Read relevant logs, diagnosis material, SSOT, code, data, and historical Tickets.
3. Establish the current live baseline before accepting source conclusions.
4. Trace the smallest complete causal path: trigger/input → state/transformation → behavior → observer.
5. Separate facts, hypotheses, contradictions, and missing evidence.
6. Inspect the highest-value discriminating evidence first.
7. Compare credible repair directions by outcome, scope, compatibility, and verification path.
8. Do not choose between valid product semantics without an existing decision.

Gate PASS requires a precise symptom, bounded inspection scope, evidence-supported causal assessment or explicit insufficiency, distinguished credible alternatives, and explicit unknowns.

Output:

- Verdict: confirmed, partially confirmed, disproved, or insufficient evidence.
- Observed baseline and causal chain.
- Verified findings and weakened hypotheses.
- Repair directions and risks.
- Decision required, if any.
- Recommended next mode, normally `plan`.
- Final state.

Do not produce a Ticket Map unless the user starts `plan`.

## Mode: plan

Purpose: convert an approved diagnosis/specification into a bounded implementation plan and minimal ordered Ticket Map.

Procedure:

1. Extract and state the parent problem, singular observable goal, constraints, non-goals, and approved decisions.
2. Perform bounded live inspection to confirm the goal still fits the current baseline; do not perform full per-ticket evidence collection.
3. Identify independent delivery units.
4. Split only where work can be independently implemented, verified, reverted, sequenced, or requires a distinct semantic decision.
5. Keep tightly coupled edits together when splitting creates an invalid intermediate state or duplicate verification.
6. For each candidate, define one goal, estimated scope, exclusions, dependency triplet, acceptance intent, and evidence still needed.
7. Order by real execution dependency; detect overlap and cycles.

Each Ticket Map item MUST include:

- `item`: plan-local ID such as `T01`, not a formal Ticket ID.
- `title`, `goal`, and `why`.
- `estimated_scope`, explicitly not final ALLOWED.
- `non_goals`.
- `execution_dependency`, `evidence_dependency`, `coordination_dependency`.
- `acceptance_intent`.
- `ticket_evidence_needed`.
- `delivery_independence`: independent, coupled, or optional follow-up.

Gate PASS requires one observable parent goal, resolved plan-defining semantics, one primary outcome per candidate, no duplicated ownership, directional non-circular dependencies, no fabricated anchors/line numbers/formal IDs, preserved shared constraints, and explicit per-ticket evidence gaps.

Output the plan baseline, decisions, technical boundary, Ticket Map, dependency order, deferred scope, unresolved decisions, recommended first ticket invocation, and final state. Do not register or write a formal Ticket.

## Mode: ticket

Purpose: collect complete live evidence for one atomic plan item and create one formal Ticket only after both evidence and authoring gates pass.

Procedure:

1. Perform all mandatory governance reads and record actual filenames, versions, and dates.
2. Validate the selected plan item, parent source, target project, and single-ticket goal.
3. Search for an existing Ticket with the same ID, title, source, or equivalent outcome.
4. Pin repository/worktree, HEAD or equivalent revision, dirty/clean state, registry revision, and evidence time.
5. Verify parent decisions against live state. If live facts invalidate scope or semantics, stop with `STOP_REPLAN_REQUIRED`.
6. Bound relevant producers, readers, writers, consumers, entrypoints, populations, and observers.
7. Distinguish main, archive, fixture, and other populations where relevant.
8. For alias, schema, normalization, fallback, guard, migration, lifecycle, or zero-mutation work, build the smallest complete state matrix before ACs.
9. Ensure legacy fixtures bypass the behavior under test and assert their starting shape.
10. Classify every load-bearing claim; none may remain unresolved.
11. Derive candidate ALLOWED/FORBIDDEN from live ownership and approved scope.
12. Write behavioral ACs only after population/source, entrypoint, and observer are mutually reachable.
13. Apply all current template requirements, including dependency triplet, Verify Config, AI-0 live anchors, ownership/location, Error Handling, and AC types.
14. Run the evidence gate.
15. Compile the Ticket, scan repeated claims for consistency, then review the complete final revision.
16. Run the authoring gate.
17. Write/register only through the existing authorized mechanism and verify the write.

Evidence gate PASS requires:

- Traceable parent source/decision and unambiguous ownership.
- Recorded live revision and time.
- No sample presented as a complete population.
- Direct live support for load-bearing local claims.
- Source/date and required spot-checking for external claims.
- No AI source as sole load-bearing evidence.
- Visible disposition of source/live conflicts.
- Reachable population/source, entrypoint, and observer for every behavioral AC.
- Bounded applicable readers, writers, and consumers.
- Complete conditional state matrix and valid fixtures when triggered.
- Consistent scope and no hidden semantic decision.
- Fresh evidence receipts.

Authoring gate PASS requires:

- Each Action Item maps to an AC/receipt, and each AC maps to an in-scope path.
- No contradiction among Action Items, Hard Constraints, Error Handling, and ACs.
- Current template, naming, ownership, location, Verify Config, AI-0, dependency, and AC-type rules pass.
- Evidence-tier wording is not overstated.
- The complete Ticket was re-reviewed after final revision.

On PASS, return Ticket path/ID, one-sentence goal, evidence baseline, both gate results, governance filenames/versions/dates, non-blocking limitations, next action `RGR`, and `Final state: COMPLETE`.

On non-PASS, do not create or partially register a Ticket. Return the exact stop code and smallest next action.

## Conditional state matrix

When triggered, cover only reachable and contract-relevant combinations:

- Stored state: absent, present/null, present/value.
- Input: omitted, explicit null, value, dual-key equal, dual-key unequal.
- Operation: create, update, read/list, transition.
- Outcome: accept/reject, persisted shape, returned view, warning/error, revision/state mutation.

The matrix defines the reviewed state space; do not expand it speculatively.

## Autonomous repair

Auto-repair when one correct result follows from existing evidence without changing approved semantics, including derived counts, duplicated-claim consistency, template placement, readable source dates, re-probable anchors, and AC wording with already-established reachability.

Do not auto-repair product behavior, compatibility, migration/deletion, scope expansion, ownership, irreversible actions, or a fork with multiple valid semantics.

After changing any AC, Hard Constraint, scope, or semantic statement, rerun the complete applicable gate, not only the edited section.

## Loop limits

- Internal self-repair does not count as an external review round.
- If the same blocker survives two autonomous repair attempts, stop and report its root cause.
- If a formal parent Ticket later fails or remains PARTIAL for three external gate rounds, stop autonomous progression and escalate under the existing RGR policy.

## Response shape

Completion:

```text
Mode: diag | plan | ticket
Gate: PASS
Final state: COMPLETE
Output: <result or artifact>
Next: <one action>
```

Stop:

```text
Mode: diag | plan | ticket
Gate: STOP
Code: <exact stop code>
Completed evidence: <brief list>
Blocker: <one precise statement>
Decision or input needed: <smallest answer required>
Final state: DECISION_REQUIRED | BLOCKED
```

Never hide partial completion or claim PASS with a pending load-bearing condition.
