# Ticket Rigor Gate — Repair Routing

This file holds the Advisor Repair Mode's routing law: what may be repaired, by whom, under what boundary, and through which lane. It is one of three carriers of the `ticket-rigor-gate` skill, split out of `SKILL.md` by `RGR-RESTRUCT-01`. Core review law (finding taxonomy, severity, gate rules, the review algorithm) lives in `SKILL.md`; calibration accounting, the Repair Ledger, and Phase promotion criteria live in `OPS_CALIBRATION.md`. When a rule here needs a definition owned elsewhere, it points to that file rather than repeating the definition.

## Advisor Repair Mode

This mode lets an L1 advisor apply **fact-layer corrections** to a ticket automatically, instead of returning every Minor finding to a human. It exists because returning naming inconsistencies and missing cross-references to L2 adjudication costs more than the defect does.

The boundary is the same one stated in `SKILL.md`'s Non-Negotiable Principle 2: **facts may be repaired, decisions may not.** A repair is fact-layer only when the correct value is derivable from a repo-readable source and the repair is mechanically verifiable afterwards. Everything else is a decision and stays with the human.

### Repair Whitelist

Only the following are in range. Anything not listed is out of range by default; the list is closed, not illustrative.

| # | Finding class | Autonomy | Rule |
|---|---|---|---|
| A-1 | Documentation-layer inaccuracy; naming or numbering inconsistency; missing cross-reference (all `Minor`) | **Full** | Correct value must be read from registry, filesystem, or the referenced document. Never from memory. |
| A-2 | `Hidden Dependency` — adding the honest declaration | **Full** | Only when the dependency is self-evident from the ticket's own text (it cites a prior contract or verdict). Classify it as `execution_` / `evidence_` / `coordination_dependency` per `SKILL.md` Step 7. Adding an honest declaration does not change scope. |
| A-3 | `Missing Contract Expansion` — truth-table cell filling | **Partial** | Fill only cells whose answer is readable from a canonical source (schema, validator code, existing tests), citing file and line. Cells whose answer is a design choice (e.g. "what should happen on null") MUST be left empty and marked `L2-DECIDE`. Never infer a cell from an adjacent one. |
| A-4 | `Evidence Gap` — drafting an AC upgrade | **Partial** | Existence-to-reachability rewrites follow `SKILL.md`'s Acceptance-Criteria Typology as a template transformation. Any claim about a producer path MUST carry a Grep/Read citation with line numbers; without a citation the AC MUST NOT be written. |

### Out Of Range (always human)

`Overloaded Ticket` (scope decision) · `Persistence Pairing Gap` (adding a receipt is a design decision) · `Propagation Gap` (changes response shape) · `Incomplete Root-Cause Sweep` (expands scope) · `Missing On-Device Obligation` (declaring or waiving on-device validation is an authority call) · **every `Critical` finding** — a Critical is by definition a `FAIL`, and most Criticals arise from an unreadable canonical source, which is exactly the situation where no fact is available to repair from.

### Guardrails

All five apply. Failing any one voids the repair.

1. **Pre-signature only.** Repairs apply to drafts and unregistered tickets. Once a ticket is in the registry it leaves repair range; corrections then go through Addendum plus human adjudication, under the law in force at signing time. The single narrow exception is the **Post-Signature Erratum Lane** defined below, which does not repeal this guardrail: it keeps Addendum as the write channel and keeps the adjudication, moving it from before the write to after it.

   Direct interactive current-ticket exception: for the sole purpose of the General Autonomous Repair Default's direct interactive route, registry membership does not by itself place the attached ticket outside repair range while its lifecycle is exactly `TICKET_RIGOR`. This exception does not alter the meaning of "signed", "registered", "dispatched", or any authority rule outside that route.
2. **Per-class shadow calibration.** Each whitelist class accrues its own record of *repair proposed → accepted by the human without modification*. The promotion bar and its consequences (Phase 0 vs Phase 1, rejection reset) are legislated once, in `OPS_CALIBRATION.md`'s Phase Promotion Criteria — this guardrail enforces that bar, it does not redefine it. This mirrors the shadow-mode rule for gate verdicts (see `SKILL.md`'s Iteration And Escalation); the two counters are independent.
3. **Diff budget.** A single repair producing a diff of **more than 15 lines** stops and escalates. Repairing a Minor must not become a ticket rewrite.
4. **Normative sentences are frozen.** Sentences containing `must` / `only` / `required` / `reject` are not edited unless that sentence *is* the finding. This blocks semantic drift from entering under cover of a cosmetic fix.
5. **Post-repair diff verification.** Compare the working-tree diff line by line against the finding list. Any change not traceable to a listed finding → revert the whole repair.

Ledger format and required fields for each repair entry are legislated once, in `OPS_CALIBRATION.md`'s Repair Ledger section.

### Dispatch Authority — Preauthorized Autonomous Apply

**Definition.** Preauthorization is issued by the ticket at signing time, not by a post-signature repair. Its authority basis, target, and verification conditions are fixed at that moment and reviewed under L2 at signing. It is therefore not an exception to Guardrail 1's "Pre-signature only" — it falls outside that guardrail's range entirely. Guardrail 1 remains in full force against any post-signature repair that was not preauthorized.

**Six release conditions (all MUST hold).** A finding may be auto-applied only when all six are true:

1. An authority anchor exists and is locatable in a repo-readable source.
2. The target falls within the scope the ticket declared, and is never the body of another already-signed ticket — preauthorization scopes only the declaring ticket's own target, never a write into a different, already-registered ticket's text.
3. The preimage matches the declared value.
4. The proposed change's shape falls within the declared mutation shape.
5. Required verification is declared and executable.
6. No breaker or contract block is currently in effect.

**Consequence when all six hold.** The host MUST release the apply; the runner applies, verifies, persists the receipt, and continues to the next item in the batch. It MUST NOT ask a human about that finding case by case.

**Consequence when any condition fails.** The host MUST NOT guess and MUST NOT ask the human in free text "to be safe." It MUST emit an existing machine code (RTR / CB) or hand the finding back to the human under the existing Out Of Range / decision-layer rules.

**Self-report carries no authority.** `requires_human_decision` in the response is a model self-report; when preauthorization is met it is an audit-only field and MUST NOT by itself constitute a stop, on the same footing as `RGR-MODEL-CONTRACT-01`'s treatment of `run_summary`.

### Tooling Note

Two layers describe this mode's non-write posture.

**Procedural law** (unchanged, still binding): the proposal leg and the write leg are separated; writes are performed only by the host session (a `/uc` execution session, or a Codex leg it invokes) using that session's own tools; writes land only in the ticket's Addendum or an explicitly bounded section; every write is logged to the Repair Ledger.

**Technical enforcement**: this skill's `allowed-tools: Read, Grep, Glob` frontmatter grant does **not** restrict which tools are available — it is a pre-approval convenience only. Per Claude Code's skill documentation (live-verified 2026-08-19): "It does not restrict which tools are available: every tool remains callable, and your permission settings still govern tools that are not listed." Real technical isolation requires one of three mechanisms, all confirmed live against current Claude Code documentation: a skill-level `disallowed-tools` frontmatter field, which does remove the listed tools from Claude's pool while that skill is active; a subagent's `tools` field, a genuine hard allowlist; or a subagent's `disallowedTools` field, a genuine hard denylist ("If both are set, `disallowedTools` is applied first, then `tools` is resolved against the remaining pool"). A skill can also run its body as such a subagent's prompt via `context: fork`. None of these is set for this skill today — it runs inline, not via `context: fork`, so no subagent-level tool boundary applies to it either. Autonomous repair therefore has **no live technical block today**; the only boundary currently in force is the procedural law above, and it is enforced by discipline, not by tooling.

## Post-Signature Erratum Lane

Rev E. Standing L2 ruling, Akira, 2026-08-10.

The problem this closes: a `/uc` session executing a dispatched ticket can only touch tickets that are already registered, so every ticket it sees is out of Advisor repair range by guardrail 1. A factual error found mid-execution therefore had exactly two dispositions — break the chain and escalate, or say nothing. Both are wrong for a misnumbered cross-reference.

The lane's legal ancestor is the existing **signed-time law** carve-out for AC errata: an arithmetic correction is not a relaxation of the acceptance criteria, and correcting one does not reopen the ticket under later law.

### Erratum Range

A finding enters this lane only if **all four** hold. The conjunction is the whole design; any one of them alone is not sufficient.

1. It is class **A-1** (documentation-layer inaccuracy, naming or numbering inconsistency, missing cross-reference). Classes A-2 / A-3 / A-4 stay pre-signature only — each involves drafting new normative text, which is not erratum work.
2. Its severity is **Minor or Medium** (see `SKILL.md`'s Severity Ordering). Every `Critical` is out, as under Out Of Range.
3. The correct value is **read live** from the registry, the filesystem, or the referenced document, and the reading is cited. Never from memory, never from a prior session's output, never from an executor's report.
4. The sentence being corrected is **not** a normative sentence under guardrail 4 (`must` / `only` / `required` / `reject`). A normative sentence found to be wrong is a decision-layer finding and escalates, whatever its severity.

Severity is not itself a licence: a Medium finding that is decision-layer stays with the human. The severity bound only caps the fact-layer whitelist; it does not widen it.

### Lane Procedure

1. Do not break the `/uc` chain. Emit the one-line queue signal defined in the `uc` skill's exception list and continue.
2. Write the correction to the ticket's **Addendum** — append only. The original signed text is never edited in place; the Addendum carries old value, new value, and the citation from range condition 3.
3. Append one Repair Ledger line, marked `erratum`.
4. The human ratifies or rejects in batch, after the fact. Ratification is not assumed by silence: an unratified erratum stays queued and is reported at handoff.

Each ratification or rejection counts toward the A-1 class calibration counter — see `OPS_CALIBRATION.md`'s Calibration Coupling.

### Cross-Review Execution Leg

Detection and repair-proposal drafting for this lane may be delegated to Codex CLI, non-interactive `exec` form, so the diff is scriptable and auditable.

| Phase | Cross-review (detection) | Repair proposal | Invocation |
|---|---|---|---|
| Before any class passes calibration | `gpt-5.6-sol`, `model_reasoning_effort=high` | `gpt-5.6-sol`, `high` | `codex.cmd exec "<prompt>" -m gpt-5.6-sol -c model_reasoning_effort=high` |
| After a class passes calibration | `gpt-5.6-sol`, `high` | `gpt-5.6-terra`, `medium` | `codex.cmd exec "<prompt>" -m gpt-5.6-terra -c model_reasoning_effort=medium` |

Sol is assigned to proposal drafting during calibration because a rejection resets the counter to zero; the binding cost there is counter loss, not tokens. Once a class has passed, proposal drafting is mechanical and drops to Terra.

Codex output is an executor report and carries no evidential weight by itself. Verification sampling requirements, and how a failed sample counts against the calibration counter, are legislated once in `OPS_CALIBRATION.md`'s Verification Sampling section.

## General Autonomous Repair Default

Rev A. Established by `RGR-RESEARCH-SUBMIT-01`, Akira, 2026-09-22.

**Scope — an independent track, not an extension.** This default governs behavior only when this skill's finding review is executed live and directly — a Claude Code session (or an equivalent executor with direct tool access) running the review itself — as opposed to the `/ticket-rigor-gate sol|terra` runner-driven path described in `SKILL.md`'s Advisor Repair Mode section. `SKILL.md`'s "Eligible repair MUST be invoked only through the project-owned non-interactive repair runner" governs that separate runner path and is untouched by this section. This default does not read, extend, narrow to, or otherwise depend on the Repair Whitelist above, its `Autonomy: Full`/`Partial` labels, or `OPS_CALIBRATION.md`'s per-class calibration counters. The Repair Whitelist's "the list is closed, not illustrative" stays true of that list; this default is a second, separate judgment mechanism, not an entry added to it.

**The four-condition test.** When a finding satisfies all four, the executor MUST fix and verify it directly, in the same turn, without first proposing the fix and waiting for a per-finding "do you agree" confirmation:

1. The finding falls inside the current task's scope and the executor's already-granted modification permission (the ALLOWED scope already in force for the work at hand — this default never grants new permission, it only decides whether the executor stops to ask within permission it already has).

   **Attached-ticket authority.** In a direct interactive invocation `/ticket-rigor-gate @<ticket-path>`, the exact attached ticket file is the sole current-ticket mutation target and counts as already-granted scope under condition 1 — only while its registry lifecycle is exactly `TICKET_RIGOR`, and only for repairs that also satisfy conditions 2–4. It grants no authority over any other ticket, the registry, this skill, runner, schemas, fixtures, production code, lifecycle, or any public contract.

   Lifecycle states: `DRAFT_NEEDED` — read and pre-scan only, no autonomous mutation. `TICKET_RIGOR` — the only state in which this authority applies. `PENDING`, `IN_PROGRESS`, `EXECUTED`, `REVIEW`, `CLOSED`, `BLOCKED`, `VOID` — this authority does not apply; any mutation follows the existing ticket and dispatch authority law.

   **Concurrency.** At the start of the execution, record the attached ticket file's size and content hash; re-check both before every mutation. On any mismatch, ownership ambiguity, or target drift, stop before mutating and report `CONCURRENT_TICKET_WRITE`. No other writer may mutate the same ticket file while the execution is active; this does not otherwise redefine human or SKAD authority.
2. The correct fix can be reasonably determined — a fact-layer judgment under `SKILL.md`'s Non-Negotiable Principle 2, not a design choice.
3. The change is reversible, or its risk is otherwise bounded — a small, local, git-revertible edit, not a schema, contract, or behavior-changing mutation.
4. A proportionate verification method exists and is actually run before the fix is reported done. At minimum it replays the finding's original triggering scenario against the repaired result. Only when the repaired claim still governs an adjacent branch that was not changed, or a consumer already named by the ticket that was not changed, it also checks one minimal discriminating case that the recorded invariant still holds. The result is reported in the existing final report with no new field, and a failed replay or required discriminating case counts as a failure of that iteration under the session-local closure loop, whose iteration count and counting semantics are unchanged.

Success is reported only in the final report: what was fixed and what verification confirmed it. This default does not create a Repair Ledger obligation, a queue, a batch-approval step, a webhook, a poller, a notifier, a retry system, or an open-ended re-review — none of that exists for this track and none may be added under cover of it.

**When any condition fails.** The finding turns on a decision — a requirement, a policy call, an SSOT adjudication, a genuine choice among multiple non-interchangeable options — or the change is irreversible or high-risk, or no proportionate verification exists: stop and report it as an exception in the final report. The absence of an official or authoritative local source is not itself grounds to stop or to assert infeasibility — attempt Research Fallback below first. A finding that fails a condition does not stop processing of independently eligible current-ticket findings in the same cycle; escalation occurs only after every independently eligible repair has been attempted, verified, and reassessed, or when the three-cycle budget is exhausted.

A finding reported as an exception under this branch MUST be reported with `ROUTE=C_EXCEPTION` in the final report; this section's exception and `SKILL.md`'s disposition schema `C_EXCEPTION` are the same route, not two separate outcomes. Decision-Fork (`SKILL.md` → Decision-Fork Findings) is a decision-layer finding classification under the `C_EXCEPTION` route, not a substitute repair route; a Decision-Fork finding MUST likewise be reported with `ROUTE=C_EXCEPTION`, with its existing Decision-Fork record supplying the decision question and mechanical consequence.

**Session-local closure loop.** Within one interactive review execution, and only for findings that remain within the same ticket scope and satisfy the existing General Autonomous Repair Default eligibility, perform at most three autonomous repair iterations. Each iteration is repair → proportionate verification → rerun gate under the existing Mechanical Verdict Rule. PASS may be reported only from the rerun. If the third rerun is not PASS, stop autonomous repair and report all completed repairs, verification outcomes, remaining findings, the reason for non-convergence, and the final gate disposition. Do not void, reopen, or revise the ticket automatically. This loop is internal to a single review round as defined in `SKILL.md` → Iteration And Escalation; only the execution's final disposition counts toward the ticket-level three-round escalation.

**Calibration boundary.** Guardrail 2's per-class shadow calibration, and the `OPS_CALIBRATION.md` Phase Promotion Criteria it enforces, govern only the enumerated repair classes (A-1 through A-4) entering Advisor Repair Mode's non-interactive runner lane. Neither may be read to delay, gate, or otherwise limit the General Autonomous Repair Default for a finding already inside an interactive task's authorized write scope: the two mechanisms bind different lanes — runner-invoked versus live-session-invoked — and calibration in one is not a precondition the other's law imposes.

### Migration-Lite Family Closure

A finding family is the set of findings that share the same alias, state class, or guard contract. A bounded matrix is a table, indexed by the five facets in `SKILL.md`'s Migration-Lite Review, that lists the disposition of every applicable class in that family.

Within a `TICKET_RIGOR` execution, after repairing an active Migration-Lite finding, MUST rerun the complete active profile, and MUST scan for sibling Action Items, constraints, error rules, and Acceptance Criteria that share the affected field contract, population, entrypoint, or consumer.

An Acceptance Criterion that is removed, reduced, exempted, or amended MUST, before the verdict is recomputed, have every sibling Acceptance Criterion that shares the same population, source, entrypoint, or contract assumption reverified.

Counting: the initial Critical or Major finding is the first appearance of its family. On the first rerun after repair, any Critical or Major finding in the same family — including the original finding if it remains unresolved — counts as the second appearance. On the second appearance, the next repair MUST close the family with a bounded matrix; no further example-specific branches may be added. A matrix cell that requires a product or policy decision is marked `L2-DECIDE` and reported with `ROUTE=C_EXCEPTION`. If the third repair iteration is still not PASS, stop under the existing closure cap and report the unresolved classes and the Acceptance Criteria they affect.

None of the session-local repairs above determine whether SKAD opens a Revision, splits, supersedes, or voids this ticket.

## Research Fallback

Rev A. Established by `RGR-RESEARCH-SUBMIT-01`, Akira, 2026-09-22.

When local evidence — repo-readable source, this skill's own governing files, the ticket under review — is insufficient to resolve a genuine external-fact ambiguity (a public technical specification, an official API/SDK contract, a platform behavior, or vendor policy), call the already-registered MCP tool `research_submit` once, with the minimal, desensitized question and queries needed to resolve it. Never send secrets, tokens, credentials, private URLs, absolute local paths, a full diff, a full ticket body, customer data, or private source code. No fixed numeric query cap is legislated here beyond the platform's own `maxItems: 5`; "minimal" is a principle, not a countable rule this section can mechanically enforce. Any traceable public source may be used to resolve the ambiguity — official documentation, standards, public issue trackers, maintainer discussion, technical reports, or community experiments. Source strength affects only how strongly the conclusion is stated and how it is labeled in the final report (e.g. official vs. corroborated-but-unofficial vs. single-source-unverified); it is never a gate on whether the ambiguity may be researched at all.

The call is synchronous: `research_submit` returns `results[]` in the same response (verified against `core/services/research_service.py` in `mcp-ticket-platform`, which calls the search provider inline and returns the artifact before the tool call completes). There is no submission queue, no webhook, no completion notifier, and no poll loop to wait on — none may be built to simulate one. Feed the returned result back into the General Autonomous Repair Default's four-condition test above: if the fix is now reasonably determinable and still within existing scope and permission, fix and verify directly per that default; if it still turns on a decision, multiple non-interchangeable options, or an irreversible/high-risk change, report it as an exception.

A research result is admissible only as evidence for that four-condition reassessment. If, after reassessment, the finding still turns on a requirement, a policy call, an SSOT adjudication, or any other decision-layer matter (i.e. it is still `C_EXCEPTION`), the result MUST NOT be read as authorizing an autonomous fix.

Record in the final report, per call: the question asked and the `request_id`/`run_id` the tool returned, as the reference. A submit-call failure (validation, cap, or provider error) is recorded in the final report; it does not retry itself and does not block any other finding. This section does not add a dedup ledger, an allowlist, a DLP filter, or any other persistent state — repeat-submission discipline for the same ticket, finding, and question is the executor's own judgment, stated plainly in the report, not a mechanically enforced constraint.

Within one interactive execution, a materially equivalent external-fact question MUST NOT be resubmitted; any subsequent judgment MUST reuse the result already obtained in that execution and cite the same `request_id`/`run_id` in the final report. This is a session-local execution discipline, not a persistent deduplication mechanism.
