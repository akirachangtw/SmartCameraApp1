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
ANY ACTIVE STATE → BLOCKED
DECISION_REQUIRED → INPUT_VALIDATED | MODE_ANALYSIS | BLOCKED
BLOCKED → end run
```

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
| `BLOCKED` | Access, governance, evidence, or write prevents progress | Completed work, blocker, smallest recovery action |

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

No formal Ticket may be written before both governance files are fully read and the evidence gate passes. A failed write verification transitions to `BLOCKED`, not `COMPLETE`.

## Regression transitions

Return from `ticket` to `plan` when:

- Live evidence invalidates the parent scope or semantics.
- The selected item contains multiple independent outcomes.
- Ownership changes across repositories or projects.
- A newly discovered dependency changes Ticket ordering.

Return from `plan` to `diag` only when the problem or causal basis remains too uncertain to define an overall goal. Do not regress merely because file-level scope is unknown; that belongs to per-ticket evidence.
