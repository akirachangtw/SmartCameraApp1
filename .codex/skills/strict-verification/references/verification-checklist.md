# Strict Verification Checklist

Use this checklist before issuing the final verdict.

## 1. Intent Match

- Does the code match the Tech Plan Spec?
- Does the code match the full executed ticket set rather than an improvised approach?
- Does the assembled structure match the intended target state?
- Do shared interfaces, DI bindings, ViewModel wiring, and UI consumers connect correctly across tickets?

## 2. Defensive Pattern Preservation

- Were state, UDF, and hardware invariants preserved?
- Did the implementation introduce hacks, bypasses, or silent semantic drift?
- Were request/session invalidation, lifecycle cancellation, and callback drop policies preserved?
- Are ML Kit mutual exclusion and device-tier degradation still enforced where relevant?

## 3. Full-Scope Reverse Scan

Run these scans across the entire implementation scope:

- Orphan Code: old logic, old UI, old state, or split-brain paths still reachable.
- Anti-Mock: TODO(), placeholders, fake nulls, disconnected dependencies, or hardcoded values without authority.
- Edge Case: null/empty ML Kit results, bounds checks, silent catch blocks, lifecycle races, and degradation fallbacks.
- State Mutex: duplicate UI renderers, missing None/null exits, simultaneous mutually exclusive guidance states, or detector overlap.

## 4. Evidence From Runtime

- Do logs or user tests reveal only local bugs, or do they invalidate the plan?
- Is the observed failure architectural or implementation-specific?
- If device evidence exists, does FULL/LIMITED/LEGACY behavior match the plan?

## 5. gstack Continuity

- Is PROGRESS.md updated with completed tickets, date, and blockers?
- Is the current Sprint Day a KAIROS node, and has that been flagged before closure?
- Are accumulated learnings captured as candidate CLAUDE.md rules when appropriate?

## 6. Verdict Discipline

- Approve only when intent and behavior both hold
- Create fix tickets when the plan survives but implementation missed
- Escalate when the plan itself no longer stands
- Do not approve when prior execution-review MINOR, DRIFT, or BLOCK findings remain unresolved
