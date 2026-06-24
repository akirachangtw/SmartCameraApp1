# Execution Review Checklist

Use this checklist after Claude Code returns work.

Review one ticket at a time. Do not merge several ticket diffs into one verdict unless the user explicitly asks for a batch-level interim review.

## 1. Ticket Alignment

- Did the implementation satisfy the ticket goal?
- Did it respect the allowed file list?
- Did it avoid every item in the absolute ban list?
- Were any extra refactors introduced?

## 2. Tech Plan Alignment

- Were the agreed architectural decisions preserved?
- Were state, UDF, and hardware defenses kept intact?
- Did the implementation preserve the named SSOT/contract authority instead of improvising behavior?

## 3. Four-Point Reverse Scan

Run all four scans even when the diff appears clean.

### Orphan Code Detection

- If this ticket refactors or replaces behavior, is the old logic unreachable or removed?
- Is there any split-brain path where old and new pipelines can both trigger?

### Anti-Mock And Lazy Implementation Check

- Any TODO(), empty body, placeholder return, or null passed where a real value is required?
- Any variable, dependency, or data source declared but not connected?
- Any magic number without SSOT or ticket justification?

### Edge Case And Error Handling Check

- Are null/empty ML Kit results, collection bounds, lifecycle cancellation, and device-tier degradation handled where relevant?
- Are catch blocks non-silent?
- Are failure paths observable enough for later verification?

### State Mutual Exclusion Check

- Can two UI components render the same target simultaneously?
- Are None/null states guarded with early exits where required?
- Can Edge Warmth, Breathing Ring, Face Detection, Object Detection, or other mutually exclusive states overlap without a gate?

## 4. Finding Classification

Classify every finding into exactly one level:

| Level | Name | Action |
|---|---|---|
| PASS | Well Implemented | Mark ticket done and proceed. |
| MINOR | Minor Issue | Issue a targeted fix instruction, then re-review. |
| SCOPE | Scope Creep | Warn, remove or revert out-of-scope changes, then reassess ticket status. |
| DRIFT | Approach Drift | Stop for user decision; do not update the Tech Plan autonomously. |
| BLOCK | Invariant Violation | Hard block; revert or retry from a clean ticket handoff. |

## 5. Codebase Health

- Does the result appear internally coherent?
- Is there evidence that the codebase remains buildable or logically consistent?
- Did the change leave partial work that blocks the next batch?

## 6. Learnings

- Record new constraints, edge cases, performance observations, or candidate CLAUDE.md rules discovered during review.
- If there are no learnings, state `No new learnings`.

## 7. Next Action

- Mark done
- Issue a fix prompt
- Warn and remove out-of-scope changes
- Stop for user alignment
- Hard block and retry
