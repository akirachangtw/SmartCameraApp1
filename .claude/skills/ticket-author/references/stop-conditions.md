# Ticket Author P0 — Stop Conditions

## Response contract

Every stop must return:

```text
Mode: diag | plan | ticket
Gate: STOP
Code: <exact code>
Completed evidence: <brief list>
Blocker: <one precise statement>
Decision or input needed: <smallest answer required>
Final state: DECISION_REQUIRED | BLOCKED
```

Do not discard completed read-only evidence. Do not continue into a partial write.

## Global stops

| Code | Trigger | Final state |
|---|---|---|
| `STOP_INVALID_MODE` | Mode absent or not `diag`, `plan`, `ticket` | `BLOCKED` |
| `STOP_MISSING_SUBJECT` | No problem, goal, source, or selected item identifies work | `DECISION_REQUIRED` |
| `STOP_SOURCE_UNAVAILABLE` | Load-bearing referenced source cannot be read | `BLOCKED` |
| `STOP_TARGET_AMBIGUOUS` | Multiple projects/components plausibly own the work | `DECISION_REQUIRED` |
| `STOP_DECISION_REQUIRED` | Multiple materially different semantics/outcomes remain valid | `DECISION_REQUIRED` |
| `STOP_EVIDENCE_CONFLICT` | Load-bearing sources conflict and precedence cannot resolve semantics | `DECISION_REQUIRED` |
| `STOP_ACCESS_BLOCKED` | Required repository, registry, runtime, or tool inaccessible | `BLOCKED` |
| `STOP_IRREVERSIBLE_RISK` | Destructive, external, credentialed, or irreversible action lacks authorization | `DECISION_REQUIRED` |
| `STOP_SCOPE_EXPANSION` | Required work crosses approved parent boundary | `DECISION_REQUIRED` |

## `diag` stops

| Code | Trigger | Final state |
|---|---|---|
| `STOP_PROBE_REQUIRED` | One named runtime/device/external probe is necessary to distinguish causes | `DECISION_REQUIRED` |
| `STOP_SYMPTOM_NOT_OBSERVABLE` | Symptom cannot be observed and no trustworthy trace exists | `BLOCKED` |

A diagnostic run may complete with verdict `insufficient evidence` instead when that limitation is itself the useful answer.

## `plan` stops

| Code | Trigger | Final state |
|---|---|---|
| `STOP_GOAL_AMBIGUOUS` | Source supports multiple materially different parent goals | `DECISION_REQUIRED` |
| `STOP_PLAN_CONFLICT` | Approved decisions contradict each other | `DECISION_REQUIRED` |
| `STOP_NOT_DECOMPOSABLE` | Requested split creates invalid intermediate states or duplicate verification | `BLOCKED` |

`STOP_NOT_DECOMPOSABLE` is not a defect. Return one coupled candidate and explain why splitting is unsafe or wasteful.

## `ticket` stops

| Code | Trigger | Final state |
|---|---|---|
| `STOP_GOVERNANCE_UNAVAILABLE` | Evidence spec or current Ticket template cannot be fully read | `BLOCKED` |
| `STOP_REPLAN_REQUIRED` | Item not atomic, parent semantics changed, or live evidence invalidates plan | `DECISION_REQUIRED` |
| `STOP_DUPLICATE_TICKET` | Existing Ticket already owns equivalent outcome | `BLOCKED` |
| `STOP_ID_OWNERSHIP_UNRESOLVED` | Ticket ID, project ownership, or registry destination unsafe to determine | `DECISION_REQUIRED` |
| `STOP_BASELINE_CONTAMINATED` | Dirty/concurrent changes make target behavior or diff boundary unreliable | `BLOCKED` |
| `STOP_POPULATION_UNKNOWN` | Load-bearing data claim lacks bounded population | `BLOCKED` |
| `STOP_AC_UNREACHABLE` | Behavioral AC data cannot reach stated entrypoint or observer | `BLOCKED` |
| `STOP_STATE_SPACE_INCOMPLETE` | Triggered matrix omits reachable contract-relevant class | `BLOCKED` |
| `STOP_CONSUMER_UNKNOWN` | Load-bearing consumer set cannot be bounded | `BLOCKED` |
| `STOP_FIXTURE_INVALID` | Fixture traverses behavior under test or fails to establish claimed precondition | `BLOCKED` |
| `STOP_EVIDENCE_STALE` | Revision, runtime, registry, or source changed after collection | `BLOCKED` |
| `STOP_TEMPLATE_INVALID` | Compiled Ticket fails current template | `BLOCKED` |
| `STOP_WRITE_FAILED` | Authorized Ticket/registry write fails read-back verification | `BLOCKED` |

## Decision-fork discipline

When stopping for a decision:

- Present only materially different options.
- State observable consequence, compatibility effect, and recommended option.
- Do not ask the user to decide facts that live inspection can determine.
- After a decision, resume from the earliest invalidated state rather than restarting all work.

## Repair limits

- Retry a uniquely repairable blocker at most twice autonomously.
- The same surviving blocker after two repairs ends the run.
- External RGR rounds are separate from internal repair attempts.
- Three external FAIL/PARTIAL rounds on the same parent Ticket require escalation under the existing RGR policy.
