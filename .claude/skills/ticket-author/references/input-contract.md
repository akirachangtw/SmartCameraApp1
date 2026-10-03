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
- Selected Ticket Map item with a traceable parent source.

Optional fields:

| Field | Meaning |
|---|---|
| `source` | Documents, attachments, Tickets, URLs, logs, screenshots, or repo paths |
| `target` | Repository, project, service, component, UI, or workflow |
| `goal` | Observable outcome |
| `constraints` | Compatibility, scope, cost, safety, and forbidden-change limits |
| `non_goals` | Explicit exclusions |
| `decisions` | Approved semantic/architectural choices |
| `requested_output` | Report, Ticket Map, dry run, or formal Ticket |

Do not stop for a missing optional field when bounded read-only inspection can establish it.

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

- One selected Ticket Map item or equivalently atomic objective.
- Traceable parent diagnosis/specification/approved plan.
- One observable Ticket goal.
- Target project or enough information to locate it uniquely.

Return to `plan` with `STOP_REPLAN_REQUIRED` when the request contains independently deployable outcomes, unrelated repositories, or unresolved semantic decisions.

Example:

```text
/ticket-author ticket

Source: @technical-spec.md and @ticket-map.md
Selected item: T02 Halo input smoothing
Goal: apply approved EMA at the shared Halo input boundary.
Collect live evidence first; create the Ticket only after both gates pass.
```

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
