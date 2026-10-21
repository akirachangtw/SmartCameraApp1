# Ticket Author P0 — Input Contract

## Parsing

The first non-whitespace argument is `MODE`. The task payload is the union of:

- Remaining invocation text.
- Current user message.
- `@file` references.
- Attached files.
- Explicitly referenced existing Tickets, logs, URLs, or repository paths.

Natural language is the default. YAML/JSON is optional and must not be required for interactive use.

## Common fields

At least one of the following must identify the subject:

- Problem, symptom, question, or requested capability.
- Diagnosis, repair brief, technical specification, or Ticket Map.
- Selected Ticket Map item (or an explicit list of items; a raw token count of 2 or more requires a Plan-backed parent) with a traceable parent source.

Optional fields:

| Field | Meaning |
|---|---|
| `source` | Documents, attachments, Tickets, URLs, logs, screenshots, or repo paths |
| `target` | Repository, project, service, component, UI, or workflow |
| `goal` | Observable outcome |
| `constraints` | Compatibility, scope, cost, safety, and forbidden-change limits |
| `non_goals` | Explicit exclusions |
| `decisions` | Approved semantic/architectural choices |
| `requested_output` | Report, Ticket Map, or formal Ticket |
| `fixture_execution` | `true` or `false`; omitted means `false` only when no fixture signal exists (see Fixture tightening) |
| `Fixture decisions:` | Synthetic decision list supplied only for fixture runs; never a formal approval |

Do not stop for a missing optional field when bounded read-only inspection can establish it.

## Fixture tightening

Fixture signals (any one tightens; none can loosen):

1. Input `fixture_execution: true`.
2. Input contains `Fixture decisions:`.
3. Parent header has `Fixture: true`.
4. Input or parent self-labels as synthetic, fake-decision, or fixture execution (semantic safety net; tightens only).

Any signal present → effective `fixture_execution = true`; no other parameter overrides it, including `fixture_execution: false`. Do not stop; output `Input normalization: fixture_execution forced to true because <signal>`.

Fixture mode MUST NOT: allocate a formal Ticket ID, create a formal Ticket, write the Registry, change any lifecycle, serve as a `ticket` parent, or treat a synthetic decision as formal approval. Artifacts carry `Fixture: true` and `Ticket mode eligibility: FORBIDDEN`. Every `diag` and `plan` artifact outputs a `Fixture:` field.

| Artifact | `Fixture` | Formal Ticket eligibility |
|---|---|---|
| Normal diag | `false` | Still requires Gate, Verdict, atomicity |
| Fixture diag | `true` | Forbidden |
| Normal plan PASS | `false` | `Ticket mode eligibility: ALLOWED` |
| Provisional plan | `false` | `Ticket mode eligibility: FORBIDDEN` |
| Fixture plan | `true` | `Ticket mode eligibility: FORBIDDEN` |

## Cross-mode artifacts

Location: `<PROFILE.REPO_ROOT>/.tmp/ticket-author/`; names `YYYYMMDD-<slug>-diag.md` and `YYYYMMDD-<slug>-plan.md`. Short-term hand-off only, not a long-term SSOT. No ledger, database, or cross-session state.

`diag` and `plan` write an artifact by default, in addition to replying in conversation (a provisional plan included); a `plan` PASS artifact is the `ticket` parent. An invocation that says read-only or not to modify files means tracked repo files, the Registry, and Tickets, not this scratch artifact. The artifact is editable scratch: a rerun or a newly approved decision updates it in place. Artifact header (the leading `key: value` block before the first `#`/`##` heading) MUST contain `Fixture:`; `diag` also `Gate:` and `Verdict:`; `plan` also `Plan Gate:` and `Ticket mode eligibility:`. A provisional plan header: `Plan Gate: NOT PASSED`, `Status: PROVISIONAL`, `Formal Ticket IDs: FORBIDDEN`, `Ticket mode eligibility: FORBIDDEN`, `Fixture: <true|false>`.

## `diag`

Required:

- A problem, symptom, question, or source that contains one.

Not required:

- Chosen solution.
- Final implementation goal.
- Ticket ID.

Example:

```text
/ticket-author diag

Halo radius jitters while the face appears stationary.
Inspect @logcat.log, live implementation, and applicable SSOT.
Find the causal path and smallest credible repair directions. Do not open a Ticket.
```

## `plan`

Required:

- Diagnosis, repair brief, technical specification, or sufficiently clear feature request.
- One overall observable goal, stated explicitly or unambiguously extractable.

If one goal is unambiguous, state the extracted goal and proceed. If materially different goals remain plausible, stop with `STOP_GOAL_AMBIGUOUS`.

Example:

```text
/ticket-author plan

Use @technical-spec.md as the parent specification.
Goal: apply the approved EMA to the actual Halo input path without changing Eye AF.
Produce the smallest ordered Ticket Map. Do not create formal Tickets.
```

## `ticket`

Required:

- One selected Ticket Map item or equivalently atomic objective (`Selected item: <ID>`); or an explicit comma-separated list of Ticket Map item IDs (`Selected items: <ID>, <ID>, ...`; a raw token count of 2 or more requires a Plan-backed parent; see Selected items (batch form) below).
- Traceable parent diagnosis/specification/approved plan (a readable file).
- One observable Ticket goal (single item: provided by the invocation; in the `Selected items` form with a raw token count of 2 or more: one per selected item, as stated in its Ticket Map item).
- Target project or enough information to locate it uniquely.

Return to `plan` with `STOP_REPLAN_REQUIRED` when the request contains independently deployable outcomes, unrelated repositories, unresolved semantic decisions, atomicity cannot be proven, or the selected item still has multiple goals. In the `Selected items` form each condition of the preceding sentence applies to each selected item separately, never to the list as a whole; a list of atomic items is not by itself a request with independently deployable outcomes.

### Parent classification

Header = the leading `key: value` block of the artifact before the first `#`/`##` heading. Classify by header only (same-named text in the body does not count), in this priority, regardless of filename, directory, extension, description, or user-claimed parent type:

1. Header has `Plan Gate:` or `Ticket mode eligibility:` → Plan-backed.
2. Else header has `Verdict:` → Diag-backed.
3. Else → Spec-backed.

| Type | Required | Rejected (`STOP_REPLAN_REQUIRED`) |
|---|---|---|
| Plan-backed | `Fixture: false` + `Plan Gate: PASS` + `Ticket mode eligibility: ALLOWED` | Any field missing or different; `Fixture: true` |
| Diag-backed | `Fixture: false` + `Gate: PASS` + `Verdict` ∈ {`CONFIRMED`, `PARTIALLY_CONFIRMED`} | `Fixture` missing or true; `Gate` missing or not PASS; Verdict `DISPROVED`/`INSUFFICIENT_EVIDENCE`/missing/invalid |
| Spec-backed | Readable file; one atomic implementation outcome; problem and goal sufficient to start live evidence; no unresolved multi-option fork | Header `Fixture: true`; document self-labels as fixture/synthetic/fake decision |

Spec-backed `Fixture` is optional (absent = ordinary existing spec); only self-labeling tightens, and a test-related topic is not self-labeling. A bare prompt, session memory, or a provisional/blocked plan is never a parent.

`PARTIALLY_CONFIRMED` stops only for: unresolved product goal/semantics, unclear ALLOWED ownership/write responsibility, unauthorized irreversible/destructive/high-risk operation, or another persistent-protocol hard risk. A runtime/behavior unknown that matches no hard risk may proceed as `Claim status: UNKNOWN` + `AI-0 probe` (through the formal entrypoint) + `Failure branch` + `[行為性]` AC; `[實機]` is limited to real-device Logcat/screen/screenshot.

### Source embedding and naming

The formal Ticket source section MUST embed: Parent type, Parent absolute path, Parent artifact status, plan-local item ID (if applicable), selected item text or atomic objective, relevant constraints, Plan Gate/eligibility result (if applicable), and Parent read timestamp. A `.tmp` path alone is insufficient; the Ticket must stand alone after the `.tmp` file is gone. Do not copy the whole plan.

File name: `<ID>_<slug>.md`; slug lowercase ASCII kebab-case ≤12 characters; whole basename ≤40 characters. Check both limits before generating.

Example:

```text
/ticket-author ticket

Source: @technical-spec.md and @ticket-map.md
Selected item: T02 Halo input smoothing
Goal: apply approved EMA at the shared Halo input boundary.
Collect live evidence first; create the Ticket only after both gates pass.
```

### Selected items (batch form)

`Selected items: <ID>, <ID>, ...` is an additive input form of `ticket` mode. The raw token count is the number of comma-separated tokens exactly as written, counted before any normalization or deduplication. `Selected items: X` (raw token count 1) is routed in full to the existing `Selected item: X` single-item flow: no batch set resolution, no batch progress, and no batch summary, so the two forms are fully equivalent. The batch rules below apply only to a raw token count of 2 or more, which requires one Plan-backed parent. `Selected items: X, X` has a raw token count of 2: the duplicate rule below applies to it, and it is never deduplicated or downgraded to the single-item form. The form adds no mode, lifecycle, Gate, reason code, ledger, or persistent batch artifact.

- For a raw token count of 2 or more, only item IDs of the Ticket Map of one Plan-backed parent are accepted. A Diag-backed or Spec-backed parent with such a list returns `STOP_REPLAN_REQUIRED`. `all eligible` and any wildcard are not supported; a token that is not an item ID fails the Ticket Map check below.
- Set resolution belongs to the shared preflight and runs after parent classification and the full parent read. Every failure below is a shared STOP: zero formal writes, no Ticket ID, no `Pending/` write, no Registry write, and no item started.

| Condition | Reason code |
| --- | --- |
| `Selected item` and `Selected items` both given with different content | `STOP_DECISION_REQUIRED` |
| The raw tokens contain a duplicate ID (no deduplication and no downgrade to the single-item form; the user corrects the list) | `STOP_DECISION_REQUIRED` |
| A token is not an item of the parent Ticket Map | `STOP_MISSING_SUBJECT` |
| A selected item's `evidence_dependency` names a plan-local item that has not been executed (no formal Ticket for this parent and that item at lifecycle `REVIEW` or `CLOSED`; determined over the Registry main and archive, `Pending/`, `deliver/` including `deliver/archive/`, and `void/`, matching each Ticket's embedded source section by parent path and plan-local item ID), whether or not that item is in the list | `STOP_REPLAN_REQUIRED` (name the item and the sibling; move the item out of the list, or run it after the sibling is executed) |
| The `execution_dependency` references among the selected items form a cycle | `STOP_REPLAN_REQUIRED` |
| A free-text `Goal:` is supplied together with a raw token count of 2 or more (an invocation must not override a Ticket Map item's goal) | `STOP_DECISION_REQUIRED` |

- `Selected item` and `Selected items` given together with identical content are accepted as one list (no STOP).
- An `execution_dependency` that names another selected item is allowed. If an item names another selected item in its `execution_dependency`, the named item is processed first (topological order; ties keep the Ticket Map order). `coordination_dependency` is informational only: it does not order items, does not form cycles, and does not propagate a STOP. A reference to an item that is not selected behaves as in the single-item form. Whether an `execution_dependency` is satisfied is defined in `SKILL.md`, Mode: ticket, "Selected items (batch form)", Dependency STOP.
- Each selected item's goal is the one stated in its Ticket Map item; an invocation-level `Goal:` is not accepted for a raw token count of 2 or more (see the table above). Each item's Ticket embeds its own plan-local item ID and the parent read timestamp of the shared preflight.
- Execution (serial order, STOP routing, progress, receipts) is defined in `SKILL.md`, Mode: ticket, "Selected items (batch form)".

Example:

```text
/ticket-author ticket

Source: @.tmp/ticket-author/<YYYYMMDD-slug>-plan.md
Selected items: T01, T03
Collect live evidence for each item in turn; create each Ticket only after both of its gates pass.
```

## `close`

Required: one formal Ticket ID registered in the target project's Registry (main or archive). `close` reads no parent artifact, plan item or `Fixture` header and never consults `diag`/`plan` artifacts. A missing or malformed ID stops with `STOP_MISSING_SUBJECT`.

The caller may be an Agent in the autonomous workflow or a person; no per-Ticket Owner approval is part of the input. Scratch input and output live in `.tmp/ticket-author/close/<TICKET_ID>/` (see `SKILL.md`, Mode: close).

## Structured option

For saved handoffs or automation, this optional shape is accepted:

```yaml
mode: plan
source:
  - technical-spec.md
problem: Current behavior and impact.
goal: One observable overall outcome.
target: project or component
constraints:
  - required limit
non_goals:
  - excluded result
decisions:
  - approved choice
requested_output: ticket_map
```
