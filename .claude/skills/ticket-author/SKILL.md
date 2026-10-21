---
name: ticket-author
description: Diagnose an issue, turn an approved specification into a minimal Ticket Map, author one evidence-backed formal Ticket, or close a REVIEW Ticket on mechanical evidence. Manual modes: diag, plan, ticket. close is called by the autonomous workflow's Agent through the Skill tool (args `close <TICKET_ID>`) or by a person.
argument-hint: "diag|plan|ticket [task text and @sources] | close <TICKET_ID>"
disable-model-invocation: false
---

# Ticket Author P0

## Mission

Run Akira's staged workflow:

`Perplexity or Claude Desktop discussion → diagnosis/specification → Ticket Map → per-ticket live evidence → formal Ticket`

This skill performs diagnosis, planning, evidence collection, Ticket authoring, and (in close mode) evidence-based closing of a REVIEW Ticket. It MUST NOT implement production code, execute a Ticket, or treat AI-generated material as sufficient live proof.

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
/ticket-author close <TICKET_ID>
```

The mode selects the workflow stage; it is not the task description. Treat the remaining arguments, current user message, `@file` references, and attachments as the task payload.

If mode is absent or unsupported, stop with `STOP_INVALID_MODE`. Never infer a mode silently.

## Reference routing

Load a reference only when its stage is reached:

- `references/input-contract.md`: parsing, mode inputs, fixture tightening, parent classification.
- `references/state-transitions.md`: states, receipts, recovery.
- `references/stop-conditions.md`: reason codes and repair limits.

In `ticket`, before creating a formal Ticket, fully read the only mandatory governance source: `D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`. Record the actual path read, template version, document date, and effective revision date. No other copy, summary, or model memory substitutes; missing, incompletely readable, or unidentifiable version → `STOP_GOVERNANCE_UNAVAILABLE`.

A prior session summary does not substitute for a mandatory current read.

## Evidence model

Three separate axes:

| Axis | Values | Applies to |
|---|---|---|
| Gate | `PASS` / `STOP` (STOP carries `Final state` and `Reason code`) | Whether the mode workflow completed |
| Diag Verdict | `CONFIRMED` / `PARTIALLY_CONFIRMED` / `DISPROVED` / `INSUFFICIENT_EVIDENCE` | `diag` overall conclusion only |
| Claim Status | `CONFIRMED` / `SUPPORTED` / `HYPOTHESIS` / `UNKNOWN` | Each load-bearing claim |

`PARTIALLY_CONFIRMED` belongs only to Diag Verdict, never Claim Status. Baseline types `HISTORICAL` / `CURRENT_LIVE` classify a claim; they are not statuses. Gate and Diag Verdict are this Skill's output contract; only the Claim Status definition below is a mirror.

Claim Status definition — verbatim local copy of the approved persistent protocol, not definitions created by this Skill. Source: `D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` (v3.1.5), section "證據分級與硬風險判準(本協議共同基底;2026-09-12 修訂裁決)", original line L18:

**證據四態**:`CONFIRMED`(官方契約、本地原始碼或可重現 provider 實測支持)／`SUPPORTED`(可信二手來源、SDK wrapper、issue、論壇或相鄰實作支持)／`HYPOTHESIS`(有合理技術路徑,尚未驗證)／`UNKNOWN`(訊號不足)。「公認」「業界常見」至多 `SUPPORTED`,MUST NOT 標 `CONFIRMED`。

`HYPOTHESIS` carries no added probe duty; probes follow the hard-risk rule independently. Update this copy whenever the persistent protocol changes (no automatic sync in P0).

## Core rules

1. Deliver the smallest useful result for the selected mode.
2. Reuse existing project tools, ticket writers, templates, and evidence mechanisms.
3. Ask the user only for unresolved product semantics, compatibility policy, scope expansion, ownership, destructive action, or another non-derivable decision.
4. Automatically repair uniquely resolvable factual, formatting, consistency, and template defects, then rerun the complete applicable gate.
5. Label every load-bearing claim with a Claim Status (see Evidence model); none may remain `UNKNOWN` without an AI-0 probe or explicit disposition.
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

Gate PASS requires a precise symptom, bounded inspection scope, evidence-supported causal assessment or explicit insufficiency, distinguished credible alternatives, and explicit unknowns. An unobservable symptom with no trustworthy trace completes with Verdict `INSUFFICIENT_EVIDENCE`, not a stop.

Output sections, fixed order: `Document / Historical baseline`, `Current-live baseline`, `Evidence gaps`, `External research`, `Evidence reconciliation`, `Verdict`.

- A complete historical document is never current-live `CONFIRMED`; without current repo/runtime/Registry/device evidence use `PARTIALLY_CONFIRMED` or `INSUFFICIENT_EVIDENCE`.
- `External research` may output `NOT_RUN` in P0.
- Include repair directions and risks, decision required (if any), and recommended next mode (normally `plan`).
- Saved artifact header MUST contain `Fixture:`, `Gate:`, `Verdict:` (see `input-contract.md`).

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

Load-bearing decisions have one of three states: `APPROVED` (may support a formal Ticket Map), `PENDING_DECISION` (blocks affected items), `RECOMMENDED_ONLY` (suggestion, never authorization). "Recommend/suggest/candidate" wording in a source defaults to non-`APPROVED`.

An unresolved fork may be saved as a provisional plan (header per `input-contract.md`); after the decision, resume from the analysis state and rerun the Plan Gate. A `plan` PASS writes an artifact by default as the `ticket` parent, with plan-local IDs and no formal Ticket ID.

Output the plan baseline, decisions, technical boundary, Ticket Map, dependency order, deferred scope, unresolved decisions, recommended first ticket invocation, and final state. Do not register or write a formal Ticket.

## Mode: ticket

Purpose: collect complete live evidence for one atomic plan item and create one formal Ticket only after both evidence and authoring gates pass. For a Plan-backed parent, several explicitly listed items may be handled in one call as separate serial single-item runs (see Selected items (batch form) below).

Procedure:

1. Classify the parent by header (Plan-backed → Diag-backed → Spec-backed, per `input-contract.md`) and apply its fixture and eligibility fail-closed checks. An ineligible parent — any fixture parent — returns `STOP_REPLAN_REQUIRED` immediately with zero formal writes: no template read, no Ticket ID, no `Pending/` write, no Registry write.
2. Eligible parent: fully read the Ticket template (see Reference routing) and record actual path, version, document date, and effective revision date.
3. Fully read the parent source. Then validate the selected item's atomicity; unprovable atomicity — multiple independent outcomes, or a fork the selected item cannot resolve on its own — stops with `STOP_REPLAN_REQUIRED`, with no Ticket ID, no `Pending/` write, and no Registry write issued.
4. Search for an existing Ticket with the same ID, title, source, or equivalent outcome.
5. Record repository/worktree, HEAD or equivalent revision, dirty/clean state, registry revision, and evidence time as diagnostics only; none may alone veto authoring. If content changes: re-read affected sources → update evidence → update the Ticket → rerun the complete gate.
6. Verify parent decisions against live state. If live facts invalidate scope or semantics, stop with `STOP_REPLAN_REQUIRED`.
7. Bound relevant producers, readers, writers, consumers, entrypoints, populations, and observers.
8. Distinguish main, archive, fixture, and other populations where relevant.
9. For alias, schema, normalization, fallback, guard, migration, lifecycle, or zero-mutation work, build the smallest complete state matrix before ACs.
10. Ensure legacy fixtures bypass the behavior under test and assert their starting shape.
11. Classify every load-bearing claim; none may remain unresolved.
12. Derive candidate ALLOWED/FORBIDDEN from live ownership and approved scope.
13. Write behavioral ACs only after population/source, entrypoint, and observer are mutually reachable.
14. Apply all current template requirements, including dependency triplet, Verify Config, AI-0 live anchors, ownership/location, Error Handling, and AC types.
15. Run the evidence gate.
16. Compile the Ticket, scan repeated claims for consistency, then review the complete final revision.
17. Run the authoring gate.
18. Write and register through this named sequence, then verify the final registry state and discoverability (acceptance looks at the final state, not call count):
    1. Duplicate check: target project `Pending/`, `deliver/`, `void/`, and the same-project Registry (`registry_list(project=<PROFILE.PROJECT>, ...)`).
    2. Create the Ticket file under `<PROFILE.TICKET_FILE_BASE>/Pending/` (file name per `input-contract.md`; embed parent source per `input-contract.md`).
    3. `registry_upsert_ticket(project=<PROFILE.PROJECT>, ticket_data={id, title, topic, file, lifecycle: "PENDING", ...}, allow_new_topic=false)` with project, ID, file, and lifecycle explicit; `ticket_data` MUST be a dict containing `id`. The `file` field uses the single convention `Pending/<basename>` (relative to `PROFILE.TICKET_FILE_BASE`): this matches every live PENDING entry and the `ticket-run` path join `TICKET_FILE_BASE + "/" + file`.
    4. Do not rely on an implicit lifecycle default. If the upsert already yields `lifecycle=PENDING`, do not re-call for form.
    5. Read back with `registry_list(project=<PROFILE.PROJECT>, ticket_id=<ID>, fields=["id","lifecycle","file","topic"])` and verify ID, project, lifecycle, and file.
    6. Verify the ticket is discoverable by the `ticket-run` equivalent query `registry_list(project=<PROFILE.PROJECT>, lifecycle="PENDING", fields=["id","title","file","lifecycle"], limit=50)`.
    A mismatch after the minimal safe retry → `STOP_WRITE_FAILED`.

### Selected items (batch form)

`ticket` also accepts `Selected items: <ID>, <ID>, ...` (syntax, raw token count, and set resolution: `input-contract.md`). It adds no mode, lifecycle, Gate, reason code, ledger, or persistent batch artifact. `Selected item: <ID>` and `Selected items: <ID>` with a raw token count of 1 run exactly as described above, with no batch rules, batch progress, or batch summary; `Selected items: X, X` has a raw token count of 2 and is never deduplicated or downgraded. The rules below apply to a raw token count of 2 or more, which requires one Plan-backed parent.

1. Shared preflight, once: steps 1 and 2, the full parent read of step 3, and set resolution. A STOP here is a shared STOP: zero formal writes and no item started.
2. Strict serial execution: each selected item then runs step 3's atomicity validation (the parent read is shared; re-read it only after compaction or resume, as below) through step 18.6 as its own single-item run, in the resolved order. Item N+1 starts only after item N has passed 18.6 or recorded its STOP receipt. Never run items in parallel and never fan out to subagents. Steps 4 and 18.1 and all live evidence are redone for every item and never reused from an earlier item, so a later item sees every Ticket the batch already wrote. After context compaction or resume, and before the next item, re-read the template and the parent.
3. STOP routing, using only the existing reason codes and their Final states:
   A STOP is shared only if it arises in the shared preflight or is named in the shared list below; every other STOP is item-local.
   - Shared STOP — stop every item not yet started: any STOP in the shared preflight; `STOP_WRITE_FAILED`; `STOP_GOVERNANCE_UNAVAILABLE`; `STOP_ACCESS_BLOCKED` when the blocked resource is the parent, the Ticket template, or the Registry.
   - Item-local STOP — record that item's STOP receipt and continue with the next item that does not depend on it: every other STOP, including `STOP_ACCESS_BLOCKED` on any resource other than the parent, the Ticket template, or the Registry (for example when only that item's own evidence target is blocked).
   - Dependency STOP — an item whose `execution_dependency` names a selected item that is not met is dependency-blocked and is not run: no live evidence, no Ticket ID, no write. A named item is met when (a) it passed 18.6 earlier in this invocation, or (b) a read-back lookup (for example `registry_list` by Ticket ID plus the Ticket file's embedded source section) verifies a traceable, non-`VOID` formal Ticket for the same parent artifact and the same plan-local item ID; its lifecycle may be `PENDING`, `IN_PROGRESS`, `REVIEW` or `CLOSED`. The lookup population is the Registry main and archive, `Pending/`, `deliver/` (including `deliver/archive/`), and `void/`; it is not claimed to equal the step 18.1 duplicate check. A later item references a met Ticket by its formal Ticket ID. A `STOP_DUPLICATE_TICKET` raised by the named item never blocks the dependent item by itself: look up the matching Ticket first, and only if none is found is the dependent item dependency-blocked. A named item that STOPped for any other reason, or was itself dependency-blocked, is not met unless such a Ticket exists. A dependency-blocked item outputs the existing `STOP_REPLAN_REQUIRED` stop response (`Decision or input needed`: resolve the named item's STOP, then rerun this item in a later call) and has its own progress run. `coordination_dependency` is informational only: it neither orders items nor propagates a STOP.
4. Completed Tickets stay: a Ticket whose write and read-back passed is never rolled back. A later item may reference an already created sibling by its formal Ticket ID; it MUST NOT edit any Ticket file or Registry entry that an earlier item wrote.
5. Progress: every item that starts has its own run with `runId: tka-<UTC timestamp>-<item>` and the events of the Progress contract below. Shared preflight events are reported on the first item's run (a shared preflight STOP is `run.failed` on that run, and no other run starts); a later item reports `templates-loaded` as reused from the shared preflight. Items not started after a shared STOP have no run.
6. Receipts: every item that started yields its own existing completion or stop response. After the last one, an informational summary of exactly five lines follows: `Selected:` (the resolved item order), `Completed:` (item → Ticket ID and path), `Stopped:` (item → stop code, dependency STOPs included), `Not started:` (item → reason), and `Shared stop:` (the shared STOP code and the item that raised it, or `none`). The summary is not a Gate, state, or reason code: it carries no `Gate:` or `Final state:` line and no batch-level Gate or Final state, is never written to a file, and cannot claim an overall PASS.

### Progress contract

`ticket` mode only; `diag` and `plan` emit nothing. Report each event with `mcp__context-bar__progress_emit` (`workflowId: ticket-author`), one call per event, never batched, never a percent. If the tool is absent or a call fails, skip it: progress never affects a gate, a stop, or the response.

| Event | When |
|---|---|
| `run.started` (`runId: tka-<UTC timestamp>`, `label: TKA ticket: <selected item>`) | Mode is `ticket`, before step 1 |
| `stage.started` `bootstrap` → `checkpoint.completed` `templates-loaded` | Before step 1 → after step 2 |
| `stage.started` `intake` → `parent-parsed` | Before step 3 → after step 4 |
| `stage.started` `evidence` → `sources-located` → `load-bearing-verified` | Before step 5 → after step 7 → after step 11 |
| `stage.started` `mapping` → `mapped` | Before step 12 → after step 14 |
| `conflicts-resolved` (stage `evidence`) | After step 15 evidence gate PASS |
| `stage.started` `draft` → `drafted` | Before step 16 → after compiling and the repeated-claim scan |
| `stage.started` `rgr` → `rgr-passed` | Before the final-revision review → after step 17 authoring gate PASS (this Skill's own review/repair/rerun, not `ticket-rigor-gate`) |
| `stage.started` `finalize` → `written` | Before step 18 → after step 18.6 passes |
| `run.completed` | On the PASS response |
| `run.failed` (`message: <Reason code>`) | On any STOP response |

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
- Consistent scope and no hidden semantic decision. For a load-bearing derivation that determines authorization, ordering, acceptance conditions, or a persistent side effect (for example an equivalence, compatibility, unchanged-behavior, or retry-outcome claim), name one concrete state that satisfies its premises but yields a different result or an unintended blocked path. If no state that satisfies its premises yields a different result, record the boundary states examined and why each yields the same result. If no approved decision settles the right result for that state, route it through `STOP_DECISION_REQUIRED` instead of choosing; a fail-closed STOP is not by itself a defect, only a block that contradicts the approved goal. Keep the counterexample or the states examined, with the disposition, in the evidence receipt, and let the applicable gate rerun after any repair recheck it.
- Fresh evidence receipts.

Authoring gate PASS requires:

- Each Action Item maps to an AC/receipt, and each AC maps to an in-scope path.
- No contradiction among Action Items, Hard Constraints, Error Handling, and ACs.
- Current template, naming, ownership, location, Verify Config, AI-0, dependency, and AC-type rules pass.
- Evidence-tier wording is not overstated.
- The complete Ticket was re-reviewed after final revision.

On PASS, return Ticket path/ID, one-sentence goal, evidence baseline, both gate results, template path/version/dates, non-blocking limitations, the next action (the project's existing review path; do not unconditionally chain a rigor review), and `Final state: COMPLETE`. In ticket mode this skill only authors new Tickets; closing an existing REVIEW Ticket is close mode.

On non-PASS, do not create or partially register a Ticket. Return the exact stop code and smallest next action. For a parent-eligibility stop, route the `Decision or input needed` by the parent problem:

| Parent problem | Decision or input needed |
|---|---|
| Non-atomic Spec | Run `/ticket-author plan` on that spec |
| Provisional plan, or Plan Gate not PASS | Resolve the pending decision, then rerun `plan` |
| `Fixture: true` | Supply a non-fixture real parent; do not rerun the same fixture |
| `Ticket mode eligibility: FORBIDDEN` | Resolve the blockers the artifact lists, producing a new eligible plan |
| Diag Gate not PASS, Verdict not on the whitelist, missing field, or invalid value | Supplement evidence or rerun `/ticket-author diag` |
| Partial artifact header | Regenerate the artifact with the complete required header |

A source document with no artifact header at all is still Spec-backed; missing Plan/Diag fields alone never reject it.

## Mode: close

Purpose: take one formal Ticket at `REVIEW` through deterministic local evidence, reviewer-backed AC verification and, once its delivery goal is established, an autonomous transition to `CLOSED` with Registry read-back. Invocation: `/ticket-author close <TICKET_ID>`. The autonomous workflow's Agent invokes it with the Skill tool (`skill` = `ticket-author`, `args` = `close <TICKET_ID>`) after Agent closeout reaches `REVIEW` (Transition Table), or hands it an upstream blocker with its evidence; no per-Ticket Owner approval is requested. Upstream steps MUST NOT replace this call by running the helper, the Registry tools or the procedure below by hand, MUST NOT fill verdicts themselves and write `CLOSED`, and MUST NOT declare the acceptance: verification and the closing decision belong to this mode. A Ticket at a lifecycle other than `REVIEW`/`CLOSED` gets a read-only judgement: evidence (and a reviewer reply if useful) may be collected and reported as `NOT_CLOSED(precondition)` or a blocker; nothing is written and the lifecycle never moves. `close` does not author Tickets, execute a Ticket, or modify production code.

Scratch directory `<dir>` = `.tmp/ticket-author/close/<TICKET_ID>/`. Everything in `<dir>` (preflight output, AC detail, command output, raw reviewer responses, retries) is scratch: kept 7 days (cleaned by the helper when the command runs, not on a timer), never committed, never registered, never a dependency. The helper `close_helper.py` (standard library only) never calls the Registry or a model; the Agent reads and writes the Registry through the Registry tools and hands observations to the helper as JSON files in `<dir>`. The helper prints the next action.

```close-dispatch
python .claude/skills/ticket-author/scripts/close_helper.py preflight --ticket <ID> --registry <dir>/registry.json --dependents <dir>/dependents.json
python .claude/skills/ticket-author/scripts/close_helper.py reviewer --ticket <ID>
python .claude/skills/ticket-author/scripts/close_helper.py decide --ticket <ID> --registry <dir>/registry.json --verdicts <dir>/verdicts.json --reply <dir>/reviewer_reply.txt --round <N> --fallback-tried <0|1>
python .claude/skills/ticket-author/scripts/close_helper.py readback --ticket <ID> --registry <dir>/registry_after.json
python .claude/skills/ticket-author/scripts/close_helper.py finalize --ticket <ID> --registry <dir>/registry_after.json --verdicts <dir>/verdicts.json --summary <dir>/summary.txt --commits <h1,h2>
```

Procedure:

1. Observe through the Registry tools (authoritative channel): `registry_list(project=SmartCameraApp1, ticket_id=<ID>, include_archive=true, fields=["id","lifecycle","file","reviews","checkpoint"])` → write the single ticket object to `<dir>/registry.json`; `registry_list` over the other non-closed tickets with fields `["id","lifecycle","file","depends_on","blocked_by"]` → `<dir>/dependents.json`. A Registry that cannot be read is `STOP_ACCESS_BLOCKED`; an unknown Ticket ID is `STOP_MISSING_SUBJECT`.
2. Run `preflight`. Read its compact output, not raw files.
3. Run `reviewer`. The reviewer is a real second call, never the implementing Agent answering inline. Codex is the primary reviewer for every Ticket type: `gpt-6-sol` for code, complex features, load-bearing logic, and any Skill or rule text that changes execution, authorization, acceptance or closing behavior (so a `.md` under `.claude/` is heavy); `gpt-6-luna` only for genuinely simple text, formatting or explicit metadata edits. The helper classifies by the files the Ticket changes and where they live, not by PRESET or extension alone (light = every ALLOWED file is `.md`/`.txt`/`.rst` and none lives under `.claude/`); the Agent may raise a Ticket to heavy, never lower it. Escalate Luna to Sol (`decide` returns `ESCALATE_REVIEWER`) when Luna cannot settle load-bearing semantics, finds a complex contradiction, or conflicts with the mechanical evidence; a Luna `FAIL` is not a reason to switch family. Fall back to Claude only for a Codex fault — authentication, connection, quota, service or model unavailable, or one bounded retry that still returns no usable answer — by passing `--codex-fault <auth|network|quota|unavailable|model_unavailable|recovery_exhausted>` to `reviewer`; the fallback is a separate Claude reviewer started with the Agent tool (`opus` for heavy, `sonnet` for light), never the implementing session answering inline. A `REPORT_FIX`, a substantive finding, a `FAIL` or a LIMIT is not a Codex fault. The actual model, tier, fault reason and independence are recorded; a Claude fallback is never reported as Codex or cross-model verification.
4. Verify each AC, evidence first. Build a compact evidence bundle from `preflight.json` and the Ticket (reuse valid existing evidence; never truncate AC text). Ask the reviewer for a judgement on EVERY AC in `preflight.json` — `N/A` ones included, where the reviewer checks that the stated reason holds and no inapplicable test is run — by writing the prompt to `<dir>/review_prompt.md` and requiring one line per AC in the form `AC<n>: PASS|FAIL|N/A|UNVERIFIED | <short evidence>`. Codex: `codex exec -c model="<gpt-6-sol|gpt-6-luna>" -c model_reasoning_effort="high" - < <dir>/review_prompt.md`, raw output saved verbatim to `<dir>/reviewer_reply.txt`; Claude fallback: the separate reviewer's returned text, saved verbatim to the same file. A reply from an escalation or a point re-check goes to `<dir>/reviewer_reply2.txt` and overrides only the ACs it covers. Status meanings: `PASS` (direct, traceable evidence), `FAIL` (direct evidence it is not met), `N/A` (the Ticket itself strikes it out and the reason checks out), `UNVERIFIED` (evidence insufficient; say so). An exit code supports only what the command checked; an implementation scope still `UNKNOWN` is not evidence that an AC about that scope passes; a checkbox, memory or an old prompt is never evidence; an AC that is hard to verify is not thereby non-core; Cross-Review findings recorded in the Ticket's checkpoint are evidence like any other. Where a reply contradicts direct mechanical evidence, evidence wins: re-ask only that point (or escalate) and record `override_evidence` for that AC in `verdicts.json`; free-text model output never overrides a direct fact. Write `<dir>/verdicts.json` with `reviewer` {`family`, `actual_model`, `tier`, `fallback_used`, `fallback_reason`, `independent_call` (true for a separate call), `inline` (must be false), `limitation`} and one entry per AC (by `n`) with `status`, `core` and a short evidence pointer. `decide` parses the reply files itself: it requires one-to-one coverage of the whole AC list with valid numbers and agreement with `verdicts.json` (a status difference needs `override_evidence`); it does not prove the quality of the reviewer's judgement. A point review covers only that point and is never reported as whole-Ticket verification. Core ACs and hard blockers are defined in `.claude/rules/ssot-governance.md` §Ticket Closure; this skill does not copy them.
5. Run `decide`. `--round` (supplement rounds so far) and `--fallback-tried` (1 once an escalation to the stronger reviewer has been used) are required; `decide` refuses to run without them.
6. Act on `next`:
   - `SUPPLEMENT` or `RECONCILE`: collect the missing evidence (missing or invalid verdicts, a contradiction between evidence and a reviewer, a core `UNVERIFIED` AC, a concrete dependency lead, an implementation scope that the helper reports as `UNKNOWN`), at most 2 rounds per Ticket in total, update `verdicts.json`, run `decide` again with the new `--round`. At the cap `decide` asks for nothing more: it returns `NOT_CLOSED`, except that an implementation scope still `UNKNOWN` adds no terminal state of its own (the AC verdicts decide, and an AC about that scope stays `UNVERIFIED`). Never repeat the same action beyond the cap, and never invoke `close` again from inside `close`.
   - `ESCALATE_REVIEWER`: ask the stronger Codex reviewer (`gpt-6-sol`) about the unresolved ACs, save its reply to `<dir>/reviewer_reply2.txt`, run `decide` again with both `--reply` files and `--fallback-tried 1` (a Codex fault is handled earlier by `reviewer --codex-fault`, not here).
   - `BLOCKED`: stop. Write nothing to the Ticket. Respond `STOP_DECISION_REQUIRED` naming the blocker and what must change.
   - `NOT_CLOSED`: the Ticket stays at `REVIEW`; write nothing to the Ticket; respond with the reason. Do not create a human-approval step.
   - `CLOSE`: when the decision says to resolve an open checkpoint, call `registry_set_checkpoint` with a `resolution` only because its matter is genuinely resolved; then `registry_set_status(lifecycle="CLOSED")` (plus the `reviews` patch from the decision; the patch fills only absent fields, with a result recorded in `verdicts.json` `reviews_observed` when mechanical evidence of that review exists and with `N_A` otherwise, and never fills an optional external review with `PASS` without evidence; the `N_A` fields are disclosed in the final result).
   - `ALREADY_CLOSED`: write nothing; go to step 7 for a read-back and respond.
   - `WRITE_FINAL_ONLY`: the Ticket is already `CLOSED` from an earlier run of this command; go to step 7 and step 8 only.
   - `PRECONDITION_FAILED`: this is the read-only judgement — write nothing, do not move the lifecycle; respond `NOT_CLOSED` with the precondition (lifecycle other than `REVIEW`/`CLOSED`, Ticket file missing, ambiguous or contradicting the Registry); contradictions are `STOP_EVIDENCE_CONFLICT`.
7. Read back: `registry_list` again → `<dir>/registry_after.json`, then run `readback`. A mismatch is reported as is, never as consistent, and is `STOP_WRITE_FAILED` only after one minimal safe retry.
8. Only for a Ticket that is `CLOSED` in the read-back: write the 3-line delivery summary to `<dir>/summary.txt` (when the Ticket's checkpoint recorded a Cross-Review verdict history or a LIMIT, one of the lines MUST state it, because `CLOSED` clears the checkpoint) and run `finalize`, which repeats the read-back check and the earlier-decision binding check itself and refuses to write when either fails. This minimal final result is the only thing kept on the original Ticket; a Ticket that was not closed gets nothing written.
9. Respond with the existing completion shape (`Mode: close`, `Gate: PASS`, `Final state: COMPLETE`) plus `Close result: CLOSED | ALREADY_CLOSED | NOT_CLOSED(<reason>)`, the actual reviewer(s) and limitations, and one line each for what actually happened: transition done or not, read-back ok or mismatch, final result written or skipped. Partial success is reported as partial. `STOP_*` is used only for the cases named above.

Re-entry is derived from observable state only (Registry lifecycle, the final-result section in the Ticket, and, when still present, an earlier `decision.json` in `<dir>` that is bound to this Ticket, its file and the implementation commit): a rerun after partial success updates the existing final-result section instead of appending; a `CLOSED` Ticket with no final-result section and no matching earlier close decision is left untouched and reported, so evidence lost after 7 days is never guessed back. Never batch-close or batch-edit closed Tickets. Losing `<dir>` after 7 days only means evidence is collected again.

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
Reason code: <exact stop code>
Completed evidence: <brief list>
Blocker: <one precise statement>
Decision or input needed: <smallest answer required>
Final state: DECISION_REQUIRED | CHANNEL_BLOCKED | REPAIR_EXHAUSTED
```

Terminal states (SSOT; the per-code mapping lives in `references/stop-conditions.md`):

- `DECISION_REQUIRED`: product semantics, compatibility policy, scope, ownership, or irreversible action needs a decision.
- `CHANNEL_BLOCKED`: the template, repo, Registry, or a required tool is inaccessible.
- `REPAIR_EXHAUSTED`: the same blocker survived two repairs. Format, template, AC, fixture, consumer, and state-space defects are first repaired or supplemented automatically, never ended immediately.

Never hide partial completion or claim PASS with a pending load-bearing condition.
