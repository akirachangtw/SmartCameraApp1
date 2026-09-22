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

### Single-Attachment Live Lane Carve-out

This carve-out applies **only** to payloads carrying `lane: single_attachment_live_lane`. It does not generalize to any other lane, and it is not a scope compiler: it resolves condition ② below by definition, once, for this one lane — it does not introduce multi-root, glob, or cross-ticket target resolution of any kind.

**Provenance.** This carve-out, its fixed parameters, and the bootstrap basis for the two classes it releases are adjudicated in vault decision `wiki/decisions/SKAD/2026-09-21_RGR_Single_Attachment_Live_Lane_Authorization.md`, Key Decisions KD1 (authorization model) and KD5 (`PERMITTED_REPAIR_CLASSES`, `CLASS_BASIS`). Any later ticket that instantiates this lane inherits authority from this committed clause; it does not itself grant authority — a ticket's signing text, a ticket's fields, and a model-produced payload are **never** a write-authority source under this carve-out.

**Fixed lane parameters (committed policy literal, not read from any ticket or payload).**

- `LANE_WRITE_MODE = addendum_only`
- `PERMITTED_REPAIR_CLASSES = A-1, A-2` (the two `Autonomy: Full` rows of the Repair Whitelist above; `CLASS_BASIS = whitelist-autonomy-full`, a bootstrap exception to `OPS_CALIBRATION.md`'s Phase Promotion Criteria — see that file's Phase 0 bootstrap exception clause for the full reasoning and reversal condition)

**Condition ① (authority anchor) for this lane.** The anchor is this carve-out clause itself, as committed in this file — a repo-readable, version-controlled source, unlike any individual ticket's file under `plans/cso_briefing/project/Pending/`, which is gitignored and therefore not a repo-readable source in the sense condition ① requires. The runner's `PERMITTED_REPAIR_CLASSES` allowlist is a fixed constant in `repair_runner.sh`, sourced from this clause's literal values above, never from a ticket field or payload field.

**Condition ② (target scope) for this lane.** "The scope the ticket declared" is defined, for this lane only, as the single Pending ticket the user attached in that `/ticket-rigor-gate` invocation — a session-parametrized target, not a fixed path. The runner's mechanical proof of this definition is `live_binding_passed`: canonical(`attachment_target`) equals canonical(`target.file_path`), the target is contained under `plans/cso_briefing/project/Pending/` as a single real `.md` file (not a directory, glob, symlink, or path outside the repo), and `expected_old_text_hash` is present. This binding is anti-drift verification, not an independent grant of authority — the authority is condition ① above.

**Condition ⑥ (no breaker/contract block) for this lane.** Satisfied by `circuit_breaker_precheck()` having already run and passed for the current run, evaluated before any per-finding processing begins.

Conditions ③④⑤ are unchanged from the general six-condition list and are enforced by the runner's existing `validate_target()`, `validate_diff_budget()`/`validate_frozen_sentence()`, and `required_verification` checks respectively, each at its own existing position in the guard chain.

**Class-level gate (independent of the six conditions).** A finding whose class is not in the fixed `PERMITTED_REPAIR_CLASSES` set above is proposal-only under this lane, regardless of any other condition, and is never mutated. Within the permitted classes: an A-1 finding auto-applies only when its anchor falls outside the ticket's normative sections (title, Verify Config, 前置依賴, Action Items, Hard Constraints, Error Handling, Acceptance Criteria) **and** the response's `edit_intent` is `additive`; an anchor inside a normative section, or `edit_intent` of `supersede` or missing, is not eligible and stays proposal-only. An A-2 finding auto-applies only when `edit_intent` is `additive`.

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
