# Examples for `ticket-rigor-gate`

檔案治理／來源說明：本文件示範 `ticket-rigor-gate` 之預期使用方式與輸出風格。案例 1 依據 `TKR-02_Fix_Family_Ticket_Review_20260802.md` 所揭示的母票與 FIX 家族演化模式改寫；其餘案例為依同一審查框架抽象出的教學型示例，用途是校準 Skill 行為，而非作為真實事件記錄。

---

## Example 1 — Parent ticket produced a predictable FIX family

### Scenario

A feature started from one broad parent ticket that combined service logic, MCP tools, validation rules, lifecycle constraints, manifest behavior, and test obligations in one vertical slice. The implementation later required a long sequence of follow-up FIX tickets because important contracts were only stated in summary form and were not fully expanded before work began.

### Input signals

- The parent ticket mixes schema closure, persistence ordering, multi-project routing, warning behavior, and end-to-end adapter behavior in one unit of work.
- A key contract such as `checkpoint.options` is described at a summary level, but missing-field, missing-type, array-level, and partial-merge rules are not fully expanded up front.
- Several follow-up tickets rely on prior contract clarification or prior findings, yet the dependency field still claims `no dependency`.
- Some acceptance criteria prove helper existence or a narrow happy path, but only later tickets prove production producer reachability.
- A destructive lifecycle path clears state, while durable archive enforcement remains only partially closed.

### What the skill should notice

- Missing contract expansion.
- Overloaded parent-ticket scope.
- Hidden evidence dependencies.
- Acceptance criteria maturity arrives late instead of being present before decomposition.
- Late-stage fix quality is stronger than initial parent-ticket precision.

### Expected verdict

- Gate: FAIL, derived mechanically from 3 Critical findings.
- Findings: 3 Critical, 3 Major, 1 Minor.
- Rigor: 5 / 10 for the initial parent-ticket quality.
- Granularity: 7 / 10 across the family, but uneven because some tickets are well-scoped and others are omnibus fixes.
- Completeness: 8 / 10 for current family-level coverage, yet still not fully closed.
- Traceability: 6 / 10 because dependency declarations lag behind actual semantic reliance.

### Example output

```md
## Verdict
- Gate: FAIL
- Rigor: 5 / 10
- Granularity: 7 / 10
- Completeness: 8 / 10
- Traceability: 6 / 10
- Overall judgment: This parent ticket was not ready for decomposition; the eventual FIX family shows strong recovery quality, but it also proves that the original contract expansion was insufficient.

## Findings Summary

### Critical
- `Missing Canonical Source` — FIX-A signed `must`-level `OPTION_SELECT` semantics against a canonical source that was not repo-readable, and later retracted its own premise as mistaken.
- `Persistence Pairing Gap` — the R2 checkpoint-clearing path relies on a documented archival habit rather than a mechanically guaranteed durable receipt before destruction.
- `Propagation Gap` — the manifest out-of-bounds warning stops at the logger and never reaches the `registry_list` response the caller consumes.

### Major
- `Missing Contract Expansion` — `checkpoint.options` needed four separate follow-up tickets (A / D / E / I) to close one contract.
- `Hidden Dependency` — tickets D / E / I / C declare `no dependency` while relying on contract closure or findings established by earlier tickets.
- `Overloaded Ticket` — FIX-C bundles seven action items across four root causes; FIX-F mixes project routing with LF write normalization.

### Minor
- Family Sequence Pollution (anti-pattern; outside the nine-category taxonomy) — FIX-B is unrelated technical debt yet occupies a FIX-family sequence number, inflating the family's apparent size.

## Mechanical Verdict Derivation
- Critical count = 3 → any Critical exists → **FAIL**.
- Major count (3) and Minor count (1) are not consulted; the Critical branch already decides the gate.

## Family Map
| Ticket | Gap actually closed | Granularity verdict | Evidence maturity | Completeness verdict |
|---|---|---|---|---|
| FIX-A | `OPTION_SELECT` contract premise (later retracted) | atomic | existence only | not closed |
| FIX-C | durability + schema + docs, four root causes | overloaded | behavior | partially closed |
| FIX-F | project routing + LF normalization | overloaded | reachability | closed |
| FIX-B | unrelated technical debt | atomic | behavior | closed, but out of family scope |
| FIX-I | `checkpoint.options` array-level closure | atomic | behavior, negative cases | closed |

Classification: FIX-A / FIX-I are direct remediation of a parent-ticket shortfall; FIX-F is a gap exposed only after the parent shipped; FIX-C is a data-preservation and durability gap caused by a parent-ticket post-condition; FIX-B is unrelated technical debt and is therefore excluded from the family's coverage denominator.

## Key Risks
- The same contract is being closed across multiple follow-up tickets.
- Parent scope mixes schema, routing, durability, and adapter behavior.
- Dependency declarations are not honest enough for sequencing and review.
- Some destructive flows are not paired with a durable receipt.
- Warning/report behavior stops short of the consumer-facing response.

## Missing Contract Expansion
- `checkpoint.options` lacks a full truth table across missing type, missing options, empty array, and partial merge cases.
- Manifest warning behavior does not fully specify caller-visible propagation.
- Invalid dependency input behavior is not fully specified.

## Ticket Atomicity Review
- FIX-C is overloaded and should have been split into durability/order vs schema/integrity vs documentation correction.
- FIX-F mixes project routing and LF write normalization, which are distinct failure mechanisms.

## Dependency Review
- execution_dependency: some validation and state-machine closures depend on prior schema alignment.
- evidence_dependency: later tickets rely on prior contract findings despite declaring no dependency.
- coordination_dependency: several tickets could not safely run in parallel even though the ticket metadata implied otherwise.

## Evidence Review
- existence: early criteria prove helper existence and a narrow happy path.
- reachability: production-producer reachability appears only in later tickets, not in the parent.
- behavior: negative-case coverage arrives too late in the ticket family.
- on-device: not applicable to this service-layer family; no on-device obligation declared and none required.

## Persistence and Propagation Review
- Historical archive recovery is addressed.
- Future destructive transitions still lack full mechanical closure.
- Internal warnings do not always reach the consumer-facing response.

## Proposed Re-split
1. Derived contract and data preflight
2. Manifest and routing closure
3. Mutation durability and ordering guarantees
4. Lifecycle/checkpoint state-machine closure
5. Adapter and E2E response visibility

## Gate Decision
- Not safe to split in the original form.
- Rewrite the parent ticket first, then decompose.
```

### Why this is correct

The repeated closure of the same contract across multiple tickets shows that decomposition happened before the contract was fully expanded. The correct lesson is not merely that many fixes were needed, but that those fixes were structurally predictable from the original parent-ticket gaps.

### Suggested re-split

1. Derived contract + data preflight.
2. Manifest and multi-project routing.
3. Registry mutation durability.
4. Lifecycle and checkpoint state machine.
5. MCP adapters and end-to-end evidence closure.

---

## Example 2 — Parent ticket is close, but still missing contract closure before decomposition

### Scenario

A parent ticket is mostly well-structured and already distinguishes service logic, validation, and integration behavior. However, one validator rule still uses compressed prose such as “options must be a non-empty canonical string array when type is OPTION_SELECT,” without spelling out missing-field behavior, partial-merge behavior, or consumer-visible error behavior.

### Input signals

- The ticket already identifies the correct producer path.
- Acceptance criteria include both positive and negative tests.
- Child-ticket slicing is mostly clean.
- One rule with strong normative wording has not been expanded into a full truth table.
- Caller-visible warning or error details are underspecified.

### What the skill should notice

- The ticket is **not broadly broken**.
- The main problem is localized contract compression.
- This is a `PARTIAL`, not a `FAIL`.
- Decomposition can happen after targeted parent-ticket revision, not after a full rewrite.

### Expected verdict

- Gate: PARTIAL, derived mechanically from 0 Critical and exactly 2 Major findings.
- Findings: 0 Critical, 2 Major, 2 Minor.
- Rigor: 8 / 10.
- Granularity: 8 / 10.
- Completeness: 7 / 10.
- Traceability: 8 / 10.

### Example output

```md
## Verdict
- Gate: PARTIAL
- Rigor: 8 / 10
- Granularity: 8 / 10
- Completeness: 7 / 10
- Traceability: 8 / 10
- Overall judgment: The parent ticket is close to decomposition-ready, but one contract still needs explicit truth-table expansion and caller-visible error semantics.

## Findings Summary

### Critical
- None.

### Major
- `Missing Contract Expansion` — the `options` rule states strong normative wording but does not expand missing-field, missing-type, empty-array, or partial-merge behavior into a truth table.
- `Propagation Gap` — the ticket claims caller-visible error reporting, but does not specify the response shape through which the caller actually consumes those details.

### Minor
- `Missing Canonical Source` (honest-absence branch) — this feature has no independent SSOT and the ticket does not pretend otherwise; recorded as an honest gap, not escalated.
- Documentation-layer inconsistency — one child-ticket title still uses the pre-rename module name.

## Mechanical Verdict Derivation
- Critical count = 0 → the FAIL branch does not apply.
- Major count = 2 → 2 or more Major findings → **PARTIAL**.
- The 2 Minor findings are recorded for revision guidance and do not affect this verdict.

## Key Risks
- A compressed validator rule may produce predictable follow-up fix tickets.
- Consumer-facing error behavior is underspecified.

## Missing Contract Expansion
- The rule for `options` does not yet expand missing-type, empty-array, and partial-merge behavior.

## Ticket Atomicity Review
- Proposed child tickets are mostly clean and independently verifiable.

## Dependency Review
- execution_dependency: acceptable.
- evidence_dependency: acceptable.
- coordination_dependency: acceptable.

## Evidence Review
- existence: present for the validator and its call site.
- reachability: the production producer path is identified and covered.
- behavior: positive and negative cases are both present.
- on-device: no on-device obligation applies; the ticket does not claim runtime or device behavior.

## Persistence and Propagation Review
- No major destructive-flow risk found.
- Error propagation semantics should be made explicit before splitting.

## Proposed Re-split
1. Revise parent ticket contract section
2. Keep current child-ticket plan after revision

## Gate Decision
- Safe to split after targeted parent-ticket fixes.
```

### Why this is correct

The point of the gate is not to block all imperfect tickets. It is to identify whether the remaining defects are foundational enough to contaminate decomposition. In this example, the problem is narrow, explicit, and cheaply fixable before child tickets are created.

### Suggested re-split

1. Add one derived-contract appendix or truth-table section to the parent ticket.
2. Keep the existing child-ticket breakdown.
3. Add one consumer-visible error expectation to the acceptance criteria.

---

## Example 3 — Parent ticket is ready for decomposition

### Scenario

A parent ticket covers one bounded feature area, keeps routing concerns separate from durability concerns, points to repo-readable canonical schema definitions, and already includes truth-table expansion for the strongest contract clauses. The proposed child tickets each map to one failure mechanism and one independent acceptance path.

### Input signals

- Parent ticket scope is narrow and coherent.
- Canonical schema and enums are explicitly referenced and readable.
- Acceptance criteria distinguish existence, reachability, and behavior.
- Destructive flows are paired with durable retention requirements where needed.
- Proposed child tickets declare honest execution and evidence dependencies.

### What the skill should notice

- Contract expansion is already mature.
- Child-ticket slicing is atomic and reviewable.
- Evidence is designed around production paths.
- No obvious “future FIX chain” smell is present.

### Expected verdict

- Gate: PASS, derived mechanically from 0 Critical and 1 Major finding.
- Findings: 0 Critical, 1 Major, 3 Minor.
- Rigor: 9 / 10.
- Granularity: 9 / 10.
- Completeness: 9 / 10.
- Traceability: 9 / 10.

### Example output

```md
## Verdict
- Gate: PASS
- Rigor: 9 / 10
- Granularity: 9 / 10
- Completeness: 9 / 10
- Traceability: 9 / 10
- Overall judgment: The parent ticket is ready for decomposition; remaining risk is normal implementation uncertainty rather than preventable contract ambiguity.

## Findings Summary

### Critical
- None.

### Major
- `Evidence Gap` — one negative acceptance criterion proves validator-level rejection but not the consumer-visible rejection message.

### Minor
- `Missing Canonical Source` (honest-absence branch) — no independent feature SSOT exists; the ticket says so plainly and points to the shared schema document instead.
- Numbering inconsistency between the action-item list and the acceptance-criteria list.
- One missing cross-reference from the parent ticket to the glossary entry for a domain term.

## Mechanical Verdict Derivation
- Critical count = 0 → the FAIL branch does not apply.
- Major count = 1 → fewer than 2 Major findings → the PARTIAL branch does not apply → **PASS**.
- Three Minor findings are present. Minor findings never aggregate into PARTIAL or FAIL, so the count is irrelevant to this verdict; they are listed purely as revision guidance.

## Key Risks
- No critical pre-decomposition risks found.
- Normal implementation variance remains.

## Missing Contract Expansion
- No high-severity gaps found.

## Ticket Atomicity Review
- Proposed child tickets each map to a single failure mechanism.

## Dependency Review
- execution_dependency: clearly declared.
- evidence_dependency: clearly declared.
- coordination_dependency: clearly declared.

## Evidence Review
- existence: canonical schema and enum references are explicit and repo-readable.
- reachability: each child ticket names the production producer path it must constrain.
- behavior: positive and negative cases are declared per child ticket.
- on-device: explicitly waived with reason; no runtime, sensor, or device path is touched.

## Persistence and Propagation Review
- Destructive flows are paired correctly.
- Caller-visible warning/error behavior is specified where relevant.

## Proposed Re-split
1. Proceed with the proposed child-ticket breakdown

## Gate Decision
- Safe to split now.
```

### Why this is correct

This is the target state for the skill. The gate passes not because the ticket is long or polished, but because the contract has been expanded enough that decomposition no longer hides predictable structural gaps.

### Suggested re-split

Proceed with the existing child-ticket plan.

---

## How to use these examples

These examples are **worked verifications of the Mechanical Verdict Rule**, not score calibration. Each one exists to show that the gate state falls out of the Findings Summary by counting, with no narrative judgment in between.

Read each example by covering the `## Verdict` line, counting the Critical and Major findings, applying the rule from `SKILL.md`, and confirming that you land on the same gate state:

| Example | Critical | Major | Rule branch taken | Gate |
|---|---|---|---|---|
| 1 | 3 | 3 | any Critical exists | FAIL |
| 2 | 0 | 2 | no Critical, Major ≥ 2 | PARTIAL |
| 3 | 0 | 1 | no Critical, Major < 2 | PASS |

Example 3 also demonstrates the Minor rule: it carries three Minor findings and still passes. Minor counts never accumulate into a worse gate state.

If a future revision of an example ever produces a gate state that its own findings do not derive, the example is wrong — not the rule. `SKILL.md` is the SSOT; these examples are its arithmetic check.
