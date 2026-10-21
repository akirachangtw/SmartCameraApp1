# Ticket Author P0 — Stop Conditions

Reason-code table and repair limits only. Response shape and the three terminal states are defined in `SKILL.md`.

Do not discard completed read-only evidence. Do not continue into a partial write.

## Global stops

| Code | Trigger | Final state |
|---|---|---|
| `STOP_INVALID_MODE` | Mode absent or not `diag`, `plan`, `ticket`, `close` | `DECISION_REQUIRED` |
| `STOP_MISSING_SUBJECT` | No problem, goal, source, or selected item identifies work | `DECISION_REQUIRED` |
| `STOP_SOURCE_UNAVAILABLE` | Load-bearing referenced source cannot be read | `CHANNEL_BLOCKED` |
| `STOP_TARGET_AMBIGUOUS` | Multiple projects/components plausibly own the work | `DECISION_REQUIRED` |
| `STOP_DECISION_REQUIRED` | Multiple materially different semantics/outcomes remain valid | `DECISION_REQUIRED` |
| `STOP_EVIDENCE_CONFLICT` | Load-bearing sources conflict and precedence cannot resolve semantics | `DECISION_REQUIRED` |
| `STOP_ACCESS_BLOCKED` | Required repository, registry, runtime, or tool inaccessible | `CHANNEL_BLOCKED` |
| `STOP_IRREVERSIBLE_RISK` | Destructive, external, credentialed, or irreversible action lacks authorization | `DECISION_REQUIRED` |
| `STOP_SCOPE_EXPANSION` | Required work crosses approved parent boundary | `DECISION_REQUIRED` |

## `diag` stops

| Code | Trigger | Final state |
|---|---|---|
| `STOP_PROBE_REQUIRED` | One named runtime/device/external probe is necessary to distinguish causes | `DECISION_REQUIRED` |

An unobservable symptom with no trustworthy trace is not a stop: complete with Diag Verdict `INSUFFICIENT_EVIDENCE`.

## `plan` stops

| Code | Trigger | Final state |
|---|---|---|
| `STOP_GOAL_AMBIGUOUS` | Source supports multiple materially different parent goals | `DECISION_REQUIRED` |
| `STOP_PLAN_CONFLICT` | Approved decisions contradict each other | `DECISION_REQUIRED` |

Work that cannot be split safely is not a stop: return one coupled candidate and explain why splitting is unsafe or wasteful, then complete.

## `ticket` stops

| Code | Trigger | Final state |
|---|---|---|
| `STOP_GOVERNANCE_UNAVAILABLE` | Current Ticket template cannot be fully read or its version is unidentifiable | `CHANNEL_BLOCKED` |
| `STOP_REPLAN_REQUIRED` | Parent ineligible (see parent eligibility in `input-contract.md`), item not atomic, parent semantics changed, or live evidence invalidates plan | `DECISION_REQUIRED` |
| `STOP_DUPLICATE_TICKET` | Existing Ticket already owns equivalent outcome | `DECISION_REQUIRED` |
| `STOP_ID_OWNERSHIP_UNRESOLVED` | Ticket ID, project ownership, or registry destination unsafe to determine | `DECISION_REQUIRED` |
| `STOP_POPULATION_UNKNOWN` | Load-bearing data claim lacks bounded population | `REPAIR_EXHAUSTED` (self-repair or supplement evidence and re-verify first; emit only after the same blocker fails twice) |
| `STOP_AC_UNREACHABLE` | Behavioral AC data cannot reach stated entrypoint or observer | `REPAIR_EXHAUSTED` (self-repair or supplement evidence and re-verify first; emit only after the same blocker fails twice) |
| `STOP_STATE_SPACE_INCOMPLETE` | Triggered matrix omits reachable contract-relevant class | `REPAIR_EXHAUSTED` (self-repair or supplement evidence and re-verify first; emit only after the same blocker fails twice) |
| `STOP_CONSUMER_UNKNOWN` | Load-bearing consumer set cannot be bounded | `REPAIR_EXHAUSTED` (self-repair or supplement evidence and re-verify first; emit only after the same blocker fails twice) |
| `STOP_FIXTURE_INVALID` | Fixture traverses behavior under test or fails to establish claimed precondition | `REPAIR_EXHAUSTED` (self-repair or supplement evidence and re-verify first; emit only after the same blocker fails twice) |
| `STOP_TEMPLATE_INVALID` | Compiled Ticket fails current template | `REPAIR_EXHAUSTED` (self-repair or supplement evidence and re-verify first; emit only after the same blocker fails twice) |
| `STOP_WRITE_FAILED` | Ticket/registry write already performed per contract still fails or read-back mismatches after the minimal safe retry (content or format defects take the matching repair code first) | `CHANNEL_BLOCKED` |

A runtime/behavior unknown that matches no hard risk and can be verified through an AI-0 probe plus a reachable observer is not a stop: record `Claim status: UNKNOWN` with the AI-0 probe, failure branch, and a `[行為性]` AC.

HEAD, worktree state, Registry revision, and evidence time are diagnostic records only and never a stop condition. When content changes: re-read affected sources → update evidence → update the Ticket → rerun the complete gate.

## `close` stops

`close` completes (Gate PASS) with `Close result: CLOSED | ALREADY_CLOSED | NOT_CLOSED(<reason>)` in every case except the stops below; it adds no reason code.

| Code | Trigger in `close` | Final state |
|---|---|---|
| `STOP_MISSING_SUBJECT` | Ticket ID missing, malformed, or not registered in the Registry (main or archive) | `DECISION_REQUIRED` |
| `STOP_ACCESS_BLOCKED` | The Registry cannot be read or written | `CHANNEL_BLOCKED` |
| `STOP_SOURCE_UNAVAILABLE` | A load-bearing source (the Ticket file) cannot be read | `CHANNEL_BLOCKED` |
| `STOP_EVIDENCE_CONFLICT` | Registry and Ticket file location contradict each other, or the Ticket file name is ambiguous | `DECISION_REQUIRED` |
| `STOP_DECISION_REQUIRED` | A hard blocker: a core AC is confirmed FAIL, a known security issue, irreversible damage or data-loss risk, or a contract that a dependent Ticket explicitly needs is not delivered | `DECISION_REQUIRED` |
| `STOP_WRITE_FAILED` | The transition or read-back still mismatches after one minimal safe retry | `CHANNEL_BLOCKED` |

A core AC that stays `UNVERIFIED` after supplementation and fallback, an unresolved checkpoint, and a lifecycle other than `REVIEW`/`CLOSED` are not stops: the Ticket stays as it is and the result is `NOT_CLOSED(<reason>)`.

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
