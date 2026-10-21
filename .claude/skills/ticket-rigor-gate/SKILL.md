---
name: ticket-rigor-gate
description: Review a parent ticket before decomposition, strengthen missing acceptance criteria and contract detail, and reduce predictable fix-ticket chains before implementation begins.
allowed-tools: Read, Grep, Glob
---

# Ticket Rigor Gate

檔案治理／來源說明：本文件為 `ticket-rigor-gate` 的正式 SSOT。其規格源自 TKR-02 母票至 FIX 家族的審查經驗、既有開票治理規範，以及對 Matt Pocock workflow 類技能的流程取經；本檔優先於所有 draft、chat 摘要與臨時說明。若其他文件與本檔衝突，以本檔為準。

This skill is carried across three files, split by `RGR-RESTRUCT-01`. This file (`SKILL.md`) holds core review law: finding taxonomy, severity, gate rules, and the review algorithm. `REPAIR_ROUTING.md` holds Advisor Repair Mode's routing law and the Post-Signature Erratum Lane. `OPS_CALIBRATION.md` holds calibration accounting: the Repair Ledger, the Phase promotion bar, and verification sampling. Each file legislates its own concerns once; where one file needs a definition owned by another, it points there rather than repeating it.

## Purpose

This skill is a **pre-implementation ticket review and correction skill**.

Its purpose is to review a ticket **before implementation and before large-scale decomposition begins**, then strengthen the ticket’s quality, rigor, and completeness while the cost of change is still low.

This skill assumes that a newly created ticket may not yet have a stable SSOT, and may never grow into a visible FIX family. That is acceptable. The goal is not to require a perfect SSOT before work starts; the goal is to identify what the ticket is still missing, especially in acceptance criteria, contract detail, boundary handling, data semantics, dependency honesty, and evidence design, then propose concrete corrections before those gaps leak into implementation.

In practice, this skill acts like a **ticket review and correction gate**:

- it checks whether the ticket has the acceptance criteria it should already have;
- it checks whether the ticket is missing contract clauses, truth tables, data constraints, dependency declarations, or evidence obligations that past experience says should exist;
- it proposes what must be added or revised before the ticket is safe to split or implement.

This skill does **not** promise that no follow-up fix tickets will ever exist. Its purpose is to reduce the number of **predictable** repair tickets that come from weak initial ticket quality.

## Relationship To SSOT

A ticket may enter this gate with no independent SSOT, with only a partial parent spec, or with nothing more than a draft ticket body. This is not a reason to skip the gate.

Instead, this skill should:

- identify which parts of the ticket already behave like SSOT;
- identify which parts still need explicit contract closure;
- require missing constraints and criteria to be written back into the ticket or an approved durable document before implementation begins.

If no canonical source exists, the skill must say so explicitly. It must never silently invent one.

## When To Use

Use this skill when **any** of the following mechanical triggers is true:

- normative clauses in the ticket body are **5 or more** (counted as distinct sentences matching the Step 2 keyword list; repeated occurrences of the same keyword within one sentence count once);
- the ticket is expected to be split into **2 or more** child tickets;
- the ticket touches **3 or more** allowed files, modules, or bounded areas;
- the ticket modifies validators, schemas, state machines, persistence rules, routing rules, or adapter response behavior;
- the ticket is already part of a parent-ticket plus FIX-family review.

For very small work items that do **not** meet any trigger above, the gate may be skipped, but the ticket should record a one-line waiver such as:

`Rigor Gate Waiver: below trigger threshold; single failure mechanism; no decomposition planned.`

## Modes

### 1. Preflight Mode

Use when a parent ticket or spec exists, but child tickets have not yet been created.

Goal: determine whether the parent ticket is ready for decomposition or direct implementation.

### 2. Family Review Mode

Use when a parent ticket has already generated follow-up fixes.

Goal: determine which fixes were structurally predictable from weak parent-ticket precision, missing contract expansion, hidden dependencies, or weak evidence design.

## Inputs

Gather as many of the following as are available:

- parent ticket or feature spec;
- existing child tickets or FIX tickets;
- ADRs, glossary, schema documents, interface contracts, validation rules;
- current production call path or tool/service adapter path;
- existing tests and acceptance criteria;
- team ticket governance conventions, if documented.

`/ticket-rigor-gate @<ticket-path>` 之附掛票檔，於其 lifecycle 為 `TICKET_RIGOR` 時，為本次 execution 之唯一 mutation target；授權條件與邊界見 `REPAIR_ROUTING.md` → General Autonomous Repair Default → Attached-ticket authority。

If canonical sources for enums, schemas, state vocabularies, or contracts are missing or not repo-readable, call that out explicitly as a high-risk finding rather than guessing.

## Non-Negotiable Principles

1. **Review before decomposition.** Do not assume a parent ticket is ready to split just because it reads clearly at a high level.
2. **Separate facts from decisions.** Facts are discoverable from code, docs, schema, tests, and current behavior. Decisions require explicit human choice and must not be hallucinated as facts.
3. **One child ticket = one failure mechanism + one independently verifiable outcome.** Shared files or shared review timing are not enough reason to merge work into one ticket.
4. **Acceptance criteria must prove reachability, not only existence.** Helper-level proof is weaker than production-path proof.
5. **Strong normative language must be expanded.** Terms like `required`, `non-empty`, `must`, `reject`, `warn`, `canonical`, `fallback`, `archive`, and `clear` imply truth tables or state transitions, not prose-only summaries.
6. **Durable records beat chat memory.** Any meaningful gate result must be written to an approved durable document path.

## Primary Questions

For every parent ticket or ticket family, answer these questions:

1. What contracts are asserted but not fully expanded?
2. Which required truth tables are missing?
3. Which tickets combine multiple failure mechanisms?
4. Which dependencies exist in reality but are not honestly declared?
5. Which acceptance criteria prove only existence instead of production-path behavior?
6. Which destructive actions lack a durable receipt or persistence pairing?
7. Which warnings or errors stop inside logs instead of reaching the consumer-facing response?
8. If this parent ticket were rewritten today, how should it be re-split?

## Evaluation Dimensions

Record findings first. Scores are optional presentation aids, not the primary decision mechanism.

### Rigor

Check whether the ticket moves from abstract intent to concrete, falsifiable behavior: validator rules, ordering guarantees, mutation guarantees, stop conditions, negative cases, and explicit failure semantics.

### Granularity

Check whether each ticket handles one root cause. If one ticket mixes durability, schema integrity, routing, docs correction, and multiple unrelated test obligations, treat it as overloaded.

### Completeness

Check whether the parent ticket expands schema rules, invalid-input behavior, warning propagation, persistence/deletion pairing, multi-project or multi-path behavior, and boundary cases instead of deferring them into later fixes.

### Traceability

Check whether dependency declarations match the real semantic chain. Distinguish execution order, evidence order, and coordination order rather than collapsing them into a vague “no dependency” claim.

## Finding Taxonomy

Use the following machine-oriented finding categories:

- Missing Contract Expansion
- Missing Canonical Source
- Hidden Dependency
- Evidence Gap
- Overloaded Ticket
- Persistence Pairing Gap
- Propagation Gap
- Incomplete Root-Cause Sweep
- Missing On-Device Obligation
- Decision-Fork

Each finding must be classified as one of:

- `Critical`
- `Major`
- `Medium`
- `Minor`

### Severity Ordering

The full severity scale, most to least severe, is: `Critical > Major > Medium > Minor`. This is the one place this scale is legislated; `REPAIR_ROUTING.md` and `OPS_CALIBRATION.md` point here rather than restating it.

Gate verdict determination (see Mechanical Verdict Rule below) draws only on `Critical` and `Major` — the two levels below them never affect a gate verdict. Repair-routing law draws on the full four-level scale: the Post-Signature Erratum Lane's severity bound (`REPAIR_ROUTING.md`) is stated as "Minor or Medium", which needs a level between `Major` and `Minor` to express. Gate decision law and repair law share this one ordering; neither may redefine it.

### Severity Assignment Rules

Severity is assigned by the following table, not by narrative impression. When a finding matches more than one row, take the highest severity.

| Severity | Criteria |
|---|---|
| `Critical` | A normative clause in the ticket asserts `must` / `only` / `required` over a canonical source that is not repo-readable; a destructive flow (clear / close / unlink / null / archive) has no durable receipt; a contract explicitly named in the ticket's own AC or error handling is unreachable through any declared production path. |
| `Major` | Hidden dependency (declared "no dependency" while relying on prior contract or evidence); evidence proves existence only where reachability is required; overloaded ticket mixing independent failure mechanisms; incomplete root-cause sweep; propagation gap; undeclared on-device obligation. |
| `Minor` | Documentation-layer inaccuracy; naming or numbering inconsistency; missing cross-reference; and — see the dual-state rule below — honest absence of a canonical source. |

### Decision-Fork Findings

A finding is a `Decision-Fork` when its nature is a **decision request**, not a defect report: the ticket contains multiple mutually exclusive viable paths that require human judgment. Format requirements:

- every option MUST state its **mechanical consequence** (which HC/AC in the ticket changes if this option is taken);
- **two-question mechanical pre-screen** (mechanical judgment only, no semantic discretion):
  1. Does this option violate one of this ticket's own Hard Constraints?
  2. Does this option zero out this ticket's own reason for existing (a self-defeating option)?

  If either answer is "yes" → mark `DISQUALIFIED(reason)`; the option is still listed but is not treated as a live option.
- severity: rated Major by blocking force (the ticket cannot be dispatched until resolved), but **the gate MUST NOT be scored FAIL merely because the decision has not yet returned** — a decision request is not itself a ticket defect.
- historical grounding (recorded so future readers understand the legislative rationale): `RGR-REVB-01-FIX-A` was once scored Critical-2 for "no own AI/AC, is a decision request not a dispatchable ticket"; TKR-11 M-1 is a second occurrence of the same shape, whose options (b) and (c) respectively trip pre-screen ② and ①.

**Dual-state rule for `Missing Canonical Source`.** Absence of an independent feature SSOT is normal for small tickets and is **not** by itself a defect.

- `Critical`: the ticket signs strong normative language that depends on a canonical source which cannot be read.
- `Minor`: the ticket has no independent SSOT and does not pretend otherwise. Record it as an honest gap. Do **not** invent a path that does not exist, and do not escalate severity to force the ticket to produce one.

## Gate Rules

Gate verdict must be derived from findings, not from narrative impression.

### Mechanical Verdict Rule

- If **any Critical finding** exists → `FAIL`
- Else if **Major findings are 2 or more** → `PARTIAL`
- Else → `PASS`

`Minor` findings do **not** affect the gate verdict, regardless of how many are recorded. They are listed in the report for revision guidance only. Do not aggregate, weight, or promote Minor findings into a PARTIAL or FAIL verdict.

`STRONG PASS` is an optional commendation label for exceptionally mature tickets, but it is **not** a separate gate state. The only formal gate states are:

- `PASS`
- `PARTIAL`
- `FAIL`

### Optional Score Display

If scores are shown, use only whole numbers from 0 to 10 for the four evaluation dimensions.

Scores are explanatory only. They do **not** determine the gate verdict.

Do not use decimal scoring.

### Advisory Status Does Not Waive Disposition

Advisory status or a RAPID_MODE exemption waives the gate's **blocking force**, not the **disposition obligation** for findings already detected. Once detected, disposition still routes by severity — functional Majors still get fixed, governance-layer and Minor findings still get logged. "Not enforceable" and "a detected real defect may be pretended unseen" are two different things.

## Acceptance-Criteria Typology

When reviewing evidence obligations, use the following mapping:

| English label | 中文對照 | Meaning |
|---|---|---|
| existence | 存在性 | proves a thing exists in code or config |
| reachability | 可達性 | proves a production runtime or workflow-control path can reach the guarded behavior (see the rule below) |
| behavior | 行為性 | proves the system behaves correctly under positive or negative conditions |
| on-device | 實機 | proves runtime behavior on the real target environment or device |

A `[可達性]` AC MUST exercise or trace a production runtime or workflow-control path from a stated source or precondition through guarded behavior to a named observer. A trace MUST show the path being taken — an execution result, emitted state, receipt, or a non-default production write point; listing where a producer and a consumer are defined does not by itself constitute a trace.

An AC that only asserts the presence or location of a document, contract, section, symbol, producer, consumer, or `file:line` reference is `[存在性]`, even if that material describes a runtime guard, and even if the cited file is itself an executable instruction artifact such as a skill. A docs-only ticket MAY use `[可達性]` only when it actually verifies a reachable production or workflow-control path.

A mislabeled AC MUST be recorded as a finding. Severity and disposition remain governed by the existing severity table and Mechanical Verdict Rule.

### On-Device Requirement Rule

If a ticket touches runtime state, guard logic, gate logic, platform device behavior, sensor behavior, camera behavior, or any path where real-environment execution matters, then:

- at least one acceptance criterion must be **reachability or stronger**; and
- any required **on-device** obligation must be declared explicitly, or explicitly waived with reason.

A ticket must not pretend that existence-only or behavior-only desktop evidence is enough when the real requirement is on-device validation.

## Vacuous Verification

Three previously separate variants collapse into one family so the reviewer can apply a pattern library instead of three scattered rules. `Unobserved Procedure Claim` was added later and belongs to the same family.

| Variant | Symptom | Detection Question |
|---|---|---|
| Self-Satisfying AC | The AC's truth value already holds at sign-off time, with no causal link to this ticket's action | Would this AC also be true if the ticket's action were **never executed**? |
| Mechanism Bypass | The test passes, but it proves a different mechanism than the one under review | **Which mechanism makes this observation true?** Is there a closer mechanism that would satisfy the observation without going through the mechanism under test? |
| Degradable Guard | A guard parameter's default value makes the assertion vacuously true / a no-op | With this parameter omitted, does the guard degrade to always-true? |
| Unobserved Procedure Claim | An AC asserts an internal step, ordering, file-read, or skipped action, but names only the final outcome and no observer for the claimed procedure | Is the procedure itself acceptance scope, and if so, **which observer records that it happened** (or happened in this order)? |

**Mandatory disposition**: when `Mechanism Bypass` is hit, require an isolation design that makes the mechanism under test the **sole load-bearing path** (independent instance, independent process, fault injection); otherwise the evidence is invalid.

For **document-layer evidence** (analyses, walkthroughs, review write-ups) there is no isolation to impose — a narrative cannot be run in a separate process. The equivalent disposition is **per-claim line-number citation**: every substantive claim MUST carry a locatable source (`file` + line range), marked `[first-hand]` or `[relayed]`. A walkthrough with no citable source counts as `Mechanism Bypass` and its evidence is invalid — because a fluent, self-consistent narrative can be produced without ever reading the source, which is exactly the closer mechanism this family is about.

**Mandatory disposition (Unobserved Procedure Claim)**: decide whether the procedure itself is required. If not, rewrite the AC around the observable outcome and state that internal ordering is not acceptance scope. If required, provide a separate observable clause or AC naming its observer and evidence. Without an observer, the procedure claim MUST NOT count as PASS and may only be disclosed.

A tool-call transcript is one possible observer, not a mandatory evidence format. Existing logs, receipts, emitted state, or other durable evidence MAY be used when they directly observe the claimed procedure.

## Mandatory Runtime Load

Before executing the Review Algorithm below, every gate execution MUST fully read `REPAIR_ROUTING.md`; if the selected lane uses calibration, `OPS_CALIBRATION.md` MUST also be read for that lane. Routing decisions MUST NOT be inferred from memory, a prior session, or a conversation summary. If `REPAIR_ROUTING.md` cannot be read, routing stops and the final report records `ROUTING_LAW_UNAVAILABLE`.

**Routing selector** (Research is a fact-gathering action, not a terminal route):
- Finding resolvable with local evidence → run the existing four-condition test.
- Genuine external factual ambiguity, local evidence insufficient → dispatch one minimally-redacted Research call, then rerun the four-condition test with the returned evidence.
- Decision, policy, SSOT adjudication, or a non-interchangeable choice → `ROUTE=C_EXCEPTION`; Research MUST NOT substitute for human judgment.
- Research fails, or remains unresolved after Research → do not retry; `ROUTE=C_EXCEPTION`.

## Migration-Lite Review

Trigger: a ticket that introduces or changes field aliasing, deprecation, normalization, migration, legacy-record preservation, or create/update compatibility, or that promises zero mutation for a rejection guard governing persisted or shared mutable data. A purely presentational change, or a hardware guard that persists no data, does not trigger this review.

Token: record `MIGRATION_LITE=ACTIVE` or `MIGRATION_LITE=NOT_TRIGGERED` on the line immediately after the §1 Verdict.

When active, check five facets: (1) distinguish the stored-state and input classes that materially differ, and where key presence affects behavior, distinguish absent, present-with-null, and present-with-value; (2) for each applicable class, the create/update accept-or-reject outcome, the persisted shape, retained values, and side effects; (3) whether each production reader, writer, serializer, archive path, API, and UI has been changed, is protected by an explicit compatibility behavior, or is disclosed as an open gap; (4) that every real-data behavioral AC names a reachable population, source, entrypoint, and observer; (5) that any legacy fixture that cannot be produced through the normalized writer is seeded below that boundary and its shape is asserted before the operation under test.

Disposition: all identified Migration-Lite completeness gaps MUST be recorded as findings and MUST NOT be omitted merely because their expected impact appears limited. Severity and final disposition remain governed by the existing severity table and Mechanical Verdict Rule.

Equivalent classes may be merged; this review does not require a Cartesian-product artifact. A Preflight PASS means the specification is dispatchable, not that its future implementation has been proven.

## Review Algorithm

Follow this sequence strictly.

### Step 1: Restate the claimed scope

Summarize what the parent ticket claims to achieve, in plain language, without expanding or improving it yet.

### Step 2: Extract normative clauses

List every clause that implies a rule or contract, especially any use of:

- required / optional
- must / must not
- only / canonical / valid
- non-empty / reject / warn
- merge / overwrite / append
- archive / clear / close / unlink
- fallback / ignore / skip

### Step 3: Expand hidden truth tables

For each normative clause, ask whether the ticket explicitly covers:

- missing field
- null value
- empty string
- empty array
- wrong type
- unknown enum
- partial merge
- sibling path differences
- producer path differences
- legal-but-borderline values

If not, mark `Missing Contract Expansion`.

For a load-bearing derivation in the ticket that determines authorization, ordering, acceptance conditions, or a persistent side effect (for example an equivalence, compatibility, unchanged-behavior, or retry-outcome claim), also check that the ticket records either one concrete state that satisfies the derivation's premises but yields a different result or an unintended blocked path, or the boundary states examined and why each yields the same result. If neither record is present, mark `Missing Contract Expansion`; if no approved decision settles the right result for the recorded state, treat it as a `Decision-Fork` finding. The reviewer is not required to produce the counterexample, and a fail-closed block is not by itself a defect, only a block that contradicts the approved goal.

### Step 4: Identify canonical sources

Locate the authoritative source for enums, schema versions, state vocabularies, and contract definitions.

If the review depends on a canonical source that is missing, unreadable, ambiguous, or inferred indirectly, mark `Missing Canonical Source`. Do not guess.

### Step 5: Trace the producer path

For every critical guard, identify:

- the production producer,
- the path into service or validator logic,
- the user-visible or consumer-visible outcome,
- the tests that prove that path is actually constrained.

If the evidence only covers helper code or grep-level existence, mark `Evidence Gap`.

When the ticket claims that two variants (for example old and new logic, or two forms of the same input) are equivalent, compatible, or behave unchanged, and the claim is not covered by Migration-Lite Review, choose, within the entry-to-observer range the ticket already declares, one condition that would change a branch outcome, and confirm that both variants still yield the promised observable result, using the trace standard defined under Acceptance-Criteria Typology. The reviewer need not compare every variant one by one, trace undeclared paths, consumers, metadata, or internal implementation details; a comparison limited to the changed point is an `Evidence Gap`, and nothing beyond the ticket's declared range is traced.

### Step 6: Check ticket atomicity

For each child or proposed child ticket, ask:

- Does it represent one failure mechanism?
- Does it have one coherent acceptance story?
- Would it still be valid if another root cause were removed?
- Does it require a distinct rollback or proof strategy?

If the answer suggests mixed scope, mark `Overloaded Ticket` and propose a split.

### Step 7: Check dependency honesty

For every claimed dependency, classify it explicitly:

- `execution_dependency`: code or behavior must exist first.
- `evidence_dependency`: contract, verdict, or prior result must be readable first.
- `coordination_dependency`: merge order, branch order, or worktree logistics require sequencing.

If a ticket says “no dependency” but clearly relies on prior contract closure or evidence, mark `Hidden Dependency`.

### Step 8: Check destructive-state pairing

For every path that clears, nulls, closes, deletes, unlinks, or archives state, verify whether there is a durable receipt or equivalent guaranteed preservation step before destruction.

If the process relies only on operator discipline, docs, or convention, mark `Persistence Pairing Gap`.

### Step 9: Check propagation to consumers

If the ticket claims a behavior such as “reject and report”, “warn caller”, or “return structured error details”, verify that the warning or error reaches the actual consumer-facing response shape rather than stopping in logs or internal traces.

If the internal system knows about the issue but the caller cannot consume it, mark `Propagation Gap`.

### Step 10: Perform reverse scan

If a fix addresses a fallback path, validation pattern, or contract mismatch, search for all sibling instances of the same root cause.

If the ticket fixes only the first discovered instance and leaves side doors behind, mark `Incomplete Root-Cause Sweep`.

### Step 11: Check on-device obligations

If the ticket touches runtime, guard, gate, sensor, device, camera, or platform-state behavior, verify whether on-device validation is required.

If such an obligation exists but is neither declared nor explicitly waived, mark `Missing On-Device Obligation`.

## Advisor Repair Mode And Calibration

Repair routing law — the fact-layer whitelist, the guardrails, the Post-Signature Erratum Lane, the General Autonomous Repair Default, and Research Fallback — is legislated in `REPAIR_ROUTING.md`. Calibration accounting — the Repair Ledger, the Phase promotion bar, and verification sampling — is legislated in `OPS_CALIBRATION.md`. Both files are installed alongside this one at `.claude/skills/ticket-rigor-gate/`; this file does not restate their content.

Eligible repair under Advisor Repair Mode's fact-layer whitelist MUST be invoked only through the project-owned non-interactive repair runner defined by `ticket-quality-skill-codex-runner-contract.md`; this runner-only mandate binds Advisor Repair Mode's non-interactive runner lane alone. A finding reviewed interactively in a live Claude Code session, already inside the current task's effective write scope, is governed instead by `REPAIR_ROUTING.md`'s General Autonomous Repair Default, not by this paragraph. The runner assembles the versioned prompt, invokes Codex CLI with the model profile resolved from `runner/profiles/model-profiles.v1.json`, requires a versioned JSON response schema, validates the result before write, and appends the execution record to the ticket quality report.

Within that same lane, the skill MUST NOT rely on manual freeform prompt invocation as the production repair path.

Repair invocation accepts a `sol|terra` profile argument (`/ticket-rigor-gate sol|terra`), mapped to `runner/profiles/model-profiles.v1.json`. The default profile is `terra`. Free-form model strings are rejected.

## Experience Card Lookup

Experience cards live at `experience-cards/*.md` (no index file — grep the directory). ① On a stall, a scope dispute, a verification misjudgment, a non-unique fix, a repeated failure, or a direction already rejected, grep `experience-cards/` by problem, file, and error type first. ② Only when a card's `confidence: high` AND its `trigger` AND `reuse_when` both match the current situation may its `decision` be used as the preferred course of action. ③ A hit on `avoid_when`, stale evidence, or a not-quite-matching situation means do not apply the card — fall back to normal local evidence and exception handling. ④ At task end, propose a candidate card only when the work produced a genuinely reusable decision, misjudgment pattern, effective fix, or a clearly non-retryable path — not a one-off event record. ⑤ A candidate card has no rule force until confirmed. Raw evidence belongs only in `source_refs`; never load a card's underlying reports/logs wholesale into skill context.

## Iteration And Escalation

This gate must not loop forever.

A review round is one completed gate execution that reaches a final PASS, PARTIAL, or FAIL disposition for the parent ticket. Internal repair, verification, and rerun activity performed within that same interactive execution does not create additional review rounds; only the final disposition of that execution is counted.

- If a ticket receives `FAIL`, revise the parent ticket and rerun the gate.
- If the same parent ticket fails or remains partial for **3 review rounds**, freeze autonomous progression and escalate to L2 / designated human adjudication.
- Before granting this skill full veto authority in a repo, run it in **shadow mode** against historical ticket families: **N = 10 cases at 90% or higher verdict consistency** with prior human review outcomes. Consistency is measured on the gate state (PASS / PARTIAL / FAIL) and on the Critical-finding set, not on optional scores. Any case below the bar resets the count. Until the bar is met, gate output is advisory and must not block dispatch.

## Durable Output Requirement

Every gate result must be written to a durable report file.

### Default Report Path

Write the report to:

`.tmp/rigor-gate/RGR_<ticket-id>_<YYYYMMDD>.md`

If that path does not exist, the operator must either:

- create the directory, or
- declare an approved equivalent durable path before using the skill.

### Registry Write-Back

After the report is written, the operator must perform the registry transition defined by `.claude/rules/ssot-governance.md`'s five-stage ticket lifecycle, stage ① (`TICKET_RIGOR`) — that document is the single source of truth for the `rigor` field's shape and the transition's trigger condition; this skill does not restate it. Skip the transition only if the ticket's current lifecycle is already past `PENDING`.

### Evidence Dependency Rule

If the gate result is `FAIL` or `PARTIAL`, the generated report becomes an `evidence_dependency` for the next parent-ticket revision or remediation ticket.

## Required Output

**Output language**: the final report MUST be written in Traditional Chinese (繁體中文) with English technical terms kept as-is (class/function/file names, status codes, severity labels). This mirrors CLAUDE.md Rule 6 (語言鎖定) and applies to this skill's own deliverable, not only to code comments. (免票授權：CSO 2026-08-28 對話裁決)

### 1. Verdict

- Gate: PASS / PARTIAL / FAIL
- Optional whole-number scores for Rigor / Granularity / Completeness / Traceability
- One-sentence overall judgment

### 2. Findings Summary

List findings grouped by severity:

- Critical
- Major
- Medium
- Minor

`Medium` findings never affect the Mechanical Verdict Rule (gate verdicts draw only on `Critical`/`Major`); the tier exists in this summary so repair routing (`REPAIR_ROUTING.md`, Erratum Lane) has an unambiguous bucket to consume.

Every finding MUST carry a ten-field disposition:

```text
FINDING_ID=
SEVERITY=
ROUTE=A_AUTO_REPAIR|C_EXCEPTION
AUTO_REPAIR_ATTEMPTED=true|false
RUNNER_DISPATCHED=true|false
RESEARCH_DISPATCHED=true|false
RESEARCH_REF=
REASON_CODES=
DECISION_QUESTION=
NEXT_ACTION=
```

Consistency rules: `AUTO_REPAIR_ATTEMPTED=false` requires non-empty `REASON_CODES`; a non-empty `DECISION_QUESTION` MUST be a specific, answerable question (a bare "please ask Akira to decide" is invalid); a direct interactive repair sets `RUNNER_DISPATCHED=false`, which MUST NOT be phrased as a runner or runtime failure. `RESEARCH_DISPATCHED=true` does not mean the repair succeeded, nor does it substitute for `ROUTE`. When `RESEARCH_DISPATCHED=true`, `RESEARCH_REF` MUST be written as `question=<minimal sanitized external-fact question>; request_id=<request identifier>; run_id=<run identifier>; sources=<returned evidence sources>`, one such group per Research call; the `question` segment MUST NOT contain the full ticket body, secrets, credentials, personal data, internal file paths, project or ticket identifiers, or context beyond what the question needs. All four segments MUST be present; a segment with no value is written as `none` (e.g. `sources=none` when the call returned no evidence). A group with a missing segment is a non-conforming report record; it does not change what `RESEARCH_DISPATCHED` means. A finding that reuses a result already obtained in the same execution cites that call's group unchanged. When `RESEARCH_DISPATCHED=false`, `RESEARCH_REF` stays empty. When `RESEARCH_DISPATCHED=false` for a finding that is an external-fact candidate, `REASON_CODES` MUST use one of: `LOCAL_EVIDENCE_SUFFICIENT`, `DECISION_REQUIRED`, `RESEARCH_UNAVAILABLE`, `RESEARCH_FAILED`, `UNRESOLVED_AFTER_RESEARCH`.

Within a session-local closure loop (see Iteration And Escalation), each cycle's findings are grouped under a `## Session-local Closure Cycle N of 3` heading; this does not add an eleventh field.

### 3. Key Risks

List the top 3–5 risks that make the ticket likely to spawn predictable fix tickets.

### 4. Missing Contract Expansion

List every rule that is stated but not fully expanded into testable conditions or truth tables.

### 5. Family Map (Family Review mode only)

Produce a per-ticket table:

| Ticket | Gap actually closed | Granularity verdict | Evidence maturity | Completeness verdict |
|---|---|---|---|---|

Then classify every ticket in the family into exactly one of:

- direct remediation of a parent-ticket shortfall;
- gap exposed only after the parent shipped (multi-project, split-brain, environment);
- data-preservation gap caused by a parent-ticket post-condition;
- unrelated technical debt discovered during the work.

Tickets in the fourth class must be excluded from the family's coverage denominator. Counting them inflates apparent coverage.

### 6. Ticket Atomicity Review

Identify overloaded tickets, mixed root causes, or tickets that should be split.

### 7. Dependency Review

Separate:

- execution dependencies
- evidence dependencies
- coordination dependencies

Call out dishonest “no dependency” claims explicitly.

### 8. Evidence Review

State which acceptance criteria prove:

- existence
- reachability
- behavior
- on-device

Then identify gaps and undeclared obligations.

Every residual-risk window MUST have a **named assignee** (a protocol clause, a monitoring facility, or explicit acceptance). Disclosed but no assignee → `Minor`. Undisclosed → `Major`.

### 9. Persistence and Propagation Review

State whether destructive flows are paired with durable retention, and whether warnings/errors reach the consumer-facing response.

### 10. Proposed Re-split Or Revision

If the ticket is not safe as written, propose a better decomposition or revision plan.

### 11. Gate Decision

State clearly:

- safe to implement now,
- safe to split now,
- safe only after targeted revision,
- not safe to proceed.

## Re-splitting Rules

When proposing a rewrite or decomposition, use these rules:

- derived contracts and data preflight should come before validator implementation;
- manifest/routing/multi-project issues should be isolated from schema closure when they involve distinct failure mechanics;
- durability, ordering, and persistence guarantees should not be bundled with docs cleanup or unrelated adapter work;
- consumer-facing adapter behavior, response envelopes, and warning visibility often belong in a later integration-focused ticket.

If useful, propose vertical slices in the pattern:

1. derived contract / data preflight
2. routing / manifest / producer-path closure
3. mutation durability / ordering guarantees
4. lifecycle or state-machine guard closure
5. consumer adapter / end-to-end evidence closure

## Stop Conditions

Pause and explicitly ask for clarification or missing inputs when:

- the canonical schema or enum source cannot be found,
- the current production path is unknown,
- a key term is ambiguous and could change ticket structure,
- a claimed dependency model cannot be verified from available materials,
- the approved durable report path is unknown.

Do not silently guess around missing canonical definitions.

## Tone And Behavior

- Be strict, but not theatrical.
- Prefer concrete clauses over generic advice.
- Do not praise a ticket for being long; praise it only when it is precise.
- Do not confuse many tickets with good decomposition.
- Distinguish good recovery quality from good initial ticket quality.
- If a fix family shows strong late-stage rigor, say so, but still state whether that rigor should have existed before decomposition.

## Anti-Patterns To Flag

Always flag these patterns when present:

- parent ticket is broad but contract-light;
- same contract is patched across multiple follow-up tickets;
- ticket depends on a prior conclusion but declares no dependency;
- acceptance criteria verify helper existence but not producer reachability;
- destructive lifecycle transition has no durable archive receipt;
- warning is logged internally but not surfaced to the caller;
- a fix ticket mixes multiple independent failure mechanisms;
- a child ticket’s closing notes quietly rewrite the contract instead of updating a durable SSOT location.
- an unrelated technical-debt ticket occupies a FIX-family sequence number, inflating the family's apparent size while adding no coverage to the parent contract; such work belongs in its own series with a `discovered_by` link.

## Suggested Metadata For Ticket Systems

If the team is willing to standardize ticket fields, recommend these:

- `ticket_type`
- `failure_mechanism`
- `producer_path`
- `acceptance_mode`
- `execution_dependency`
- `evidence_dependency`
- `coordination_dependency`
- `truth_table_refs`
- `legacy_scan_required`
- `durable_receipt_required`
- `superseded_by`
- `retired_clauses`

## Relationship To Other Gates And Skills

This skill is best used **after** a parent ticket or spec exists, but **before** child tickets are created.

Recommended flow:

1. clarify intent and unresolved decisions;
2. produce parent ticket or spec;
3. run `ticket-rigor-gate`;
4. revise parent ticket if needed;
5. split into child tickets if approved;
6. run any project-specific pre-dispatch anchor check per child ticket;
7. implement and review each child ticket separately.

This skill reviews **ticket structure and ticket completeness**.

Traycer-style review or implementation review tools examine **implementation-layer defects and runtime findings**.

These are complementary. They must not be treated as interchangeable.

## Installation Location

To be usable, the skill must be installed in an actual skill-discovery path, not left only as a deliverable artifact.

This skill is maintained as a **single carrier**. The installed path is the SSOT:

`.claude/skills/ticket-rigor-gate/SKILL.md` in the `SmartCameraApp1` repository.

There is no derived copy. Any file elsewhere describing this skill is historical and must carry a superseded pointer to the path above. Do not create a second live copy in another repository or in a deliverables folder; dual carriers drift because later writers follow historical path habits rather than the declared SSOT.

Deployment to additional projects is deferred until the skill is stable and shadow-mode calibration has passed; the mechanism for that will be decided at that time and is deliberately not pre-specified here.

## References

Primary internal basis:

- `mcp-ticket-platform/docs/deliver/TKR-02_...`（PLT 端歷史檔，非本 repo 路徑） — TKR-02 parent-to-FIX-family review; its evidence index (§10) resolves to paths inside this repository.
- Existing ticket template and acceptance-criteria governance conventions, including existence / reachability / behavior / on-device differentiation where applicable.
- Historical review findings on predictable fix-family emergence.

External workflow inspiration:

- Matt Pocock skills workflow patterns such as spec-to-tickets decomposition, issue-map style planning, and review-stage evidence discipline.

## One-Sentence Positioning

`ticket-rigor-gate` is a pre-implementation ticket review and correction skill: it inspects a ticket’s acceptance criteria, contract closure, dependency honesty, data semantics, and evidence obligations, then strengthens what is missing before implementation or large-scale decomposition begins.
