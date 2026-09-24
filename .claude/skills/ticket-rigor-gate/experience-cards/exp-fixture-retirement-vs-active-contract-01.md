---
id: exp-fixture-retirement-vs-active-contract-01
status: active
topic: fixture-retirement
trigger: "A fixture group appears old, narrow, or associated with a frozen feature or a closed ticket, and deleting it is being proposed to cut token, file, or maintenance cost. Before deletion, determine whether it tests the frozen capability itself or a still-active lower-level component that current workflows depend on."
decision: "Retire fixtures only when no active consumer or safety contract depends on them. Establish the consumer by reading the harness itself (what binary or main loop does it actually drive?) and by checking which recent tickets used its PASS as acceptance evidence — not by the fixture's directory name or its producer ticket's status. Retain the smallest deterministic suite that covers active authorization, boundary, or fail-closed behavior. Within a retained suite, classify each subdirectory separately: inputs the harness mutates and restores (backup → mutate → hash-assert → restore) are part of the contract and stay tracked; only one-way, never-restored output is a relocation or deletion candidate."
why: "A fixture's producer ticket closing, or the feature it was first written for being frozen, says nothing about whether the component it exercises is still live. Deleting the only regression that drives a live runner's main loop removes the one mechanism that would reveal an authorization-boundary break on the next edit — the failure cost is silent unauthorized apply, not merely one fewer test."
reuse_when: "Reviewing fixture cleanup after ticket closure or feature freeze; comparing a regression suite's value against its token and maintenance cost; deciding whether a fixture subdirectory that changes during a run is pollution or a deliberate assertion target."
avoid_when: "Deleting fixtures solely because the producer ticket is closed or the originating feature is frozen; treating executable safety coverage as raw historical evidence; relocating a harness-restored mutable input to a gitignored path (that strips the versioned baseline its before/after hash assertions depend on)."
confidence: high
source_refs:
  - "RGR-P0A-02 (preauthorization regression matrix; fixtures/p0a02/ drives repair_runner.sh main loop via stubbed codex.cmd)"
  - "RGR-DISPATCH-AUTH-01 (preauthorization dispatch rule the c5/c5b boundary cases protect)"
  - "p0a02 consumer audit 2026-09-24 (SKAD Desktop, read-only; deletion proposal withdrawn by Akira after audit)"
last_confirmed: 2026-09-24
retire_when: "The active consumer and the safety contract it covers are removed or replaced, or equivalent current coverage exists elsewhere and has been verified — not when any single ticket closes."
---
