# Ticket Author P0 — State Transitions

## States

```text
START
  → INTAKE_PARSED
  → INPUT_VALIDATED
  → SOURCE_BASELINED
  → LIVE_EVIDENCE
  → MODE_ANALYSIS
  → MODE_GATE
  → OUTPUT_READY
  → COMPLETE
```

Side states:

```text
ANY ACTIVE STATE → DECISION_REQUIRED
ANY ACTIVE STATE → CHANNEL_BLOCKED
LIVE_EVIDENCE | MODE_ANALYSIS | MODE_GATE | OUTPUT_READY → REPAIR_EXHAUSTED
DECISION_REQUIRED → INPUT_VALIDATED | MODE_ANALYSIS | CHANNEL_BLOCKED
CHANNEL_BLOCKED → end run
REPAIR_EXHAUSTED → end run
```

`CHANNEL_BLOCKED` is entered when a required source, governance file, repo, Registry, or formal write channel is unavailable. `REPAIR_EXHAUSTED` is entered only from the four named states above, when the same repairable or supplementable blocker has already failed twice after repair/evidence and re-verification. No other generic source state or new intermediate state is introduced; parent classification and eligibility checks stay under the existing `INPUT_VALIDATED`.

Never report a later state when an earlier state's exit conditions remain incomplete.

## Receipts

| State | Exit condition | Minimum receipt |
|---|---|---|
| `START` | Skill invoked | Raw invocation available |
| `INTAKE_PARSED` | Mode and payload parsed | Mode, subject, sources, requested output |
| `INPUT_VALIDATED` | Mode-specific required input satisfied | Goal/subject/item validation result |
| `SOURCE_BASELINED` | Required sources read or missing source stopped | Names, versions/dates when available, gaps |
| `LIVE_EVIDENCE` | Mode-appropriate inspection completed | Target revision/state, scope, tools/commands, conflicts |
| `MODE_ANALYSIS` | Diagnosis, plan, or Ticket evidence model complete | Mode-specific result |
| `MODE_GATE` | Applicable gate evaluated | PASS or exact stop code |
| `OUTPUT_READY` | Output complete and validated | Output type and destination |
| `COMPLETE` | Result delivered | Final response and next action |
| `DECISION_REQUIRED` | Non-derivable decision blocks progress | Fork, consequence, recommendation |
| `CHANNEL_BLOCKED` | Required source, governance file, repo, Registry, or formal write channel is unavailable | Unavailable channel, attempted recovery, Decision or input needed, resume point |
| `REPAIR_EXHAUSTED` | Same repairable/supplementable blocker failed twice after repair/evidence and re-verification | Repair attempt 1 and result, repair attempt 2 and result, remaining blocker, Decision or input needed, resume point |

P0 requires no database or persistent run ledger. Include receipts only where they improve traceability.

## Mode paths

### `diag`

```text
INTAKE_PARSED
→ symptom/question validated
→ source/live baseline
→ causal inspection
→ diagnostic gate
→ diagnostic report
→ COMPLETE
```

`diag` may end as a useful `insufficient evidence` report when the unavailable evidence cannot be recovered in the run. Use `STOP_PROBE_REQUIRED` only when one named probe can materially resolve the uncertainty.

### `plan`

```text
INTAKE_PARSED
→ parent goal validated
→ source/decision baseline
→ bounded live fit-check
→ decomposition
→ plan gate
→ Ticket Map
→ COMPLETE
```

A Ticket Map is not a group of formal Tickets. It contains plan-local IDs and unverified estimated scope.

After a pending decision is resolved, resume from the analysis state and rerun the Plan Gate; do not redo undisputed evidence collection.

### `ticket`

```text
INTAKE_PARSED
→ atomic item validated
→ mandatory governance reads
→ source baseline
→ live evidence
→ evidence gate
→ Ticket compile
→ full authoring gate
→ authorized write and read-back
→ COMPLETE
```

No formal Ticket may be written before the current Ticket template is fully read and the evidence gate passes. A failed write verification ends the run with `STOP_WRITE_FAILED` (`CHANNEL_BLOCKED` in `stop-conditions.md`), not `COMPLETE`.

Parent classification precedes `atomic item validated`: classify by header per `input-contract.md`; an ineligible parent ends with `STOP_REPLAN_REQUIRED`.

### `close`

```text
INTAKE_PARSED
→ registry observed
→ preflight
→ reviewer selected
→ AC verdicts
→ decision
→ [supplement | fallback reviewer]*
→ transition
→ read-back
→ final result
→ COMPLETE
```

Mapping to existing states and receipts only (no new state): the preflight output is `LIVE_EVIDENCE`; verdicts and the decision are `MODE_ANALYSIS`/`MODE_GATE`; the transition and read-back are `OUTPUT_READY`. Re-entry is derived from observable state (Registry lifecycle, the final-result section in the Ticket, and an earlier `decision.json` while it still exists in the scratch directory); no manifest or state file is created.

## Regression transitions

Return from `ticket` to `plan` when:

- Live evidence invalidates the parent scope or semantics.
- The selected item contains multiple independent outcomes.
- Ownership changes across repositories or projects.
- A newly discovered dependency changes Ticket ordering.

Return from `plan` to `diag` only when the problem or causal basis remains too uncertain to define an overall goal. Do not regress merely because file-level scope is unknown; that belongs to per-ticket evidence.
