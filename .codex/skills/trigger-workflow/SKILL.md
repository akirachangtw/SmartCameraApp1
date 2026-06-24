---
name: trigger-workflow
description: Validate incoming project context before architecture review. Use when Codex receives an L1/L2 spec, Tech Plan draft, user requirement, bug report, feature idea, or refactor request and must confirm the current SSOT/version, verify repo or Android camera reality, lock Scope IN/OUT boundaries, capture hardware/system constraints, and produce either a Gate Entry Summary or L1 Context & Requirement Spec before $architecture-review.
---

# Trigger Workflow

## Role

Defensive intake gate keeper who builds shared understanding and defines strict boundaries before architecture review or implementation planning begins.

**Focus on:**

- Validating provided specs or, when no spec exists, understanding the existing codebase area the user wants to build or refactor.
- Confirming the referenced SSOT or contract version matches the project authority.
- Validating that the stated problem matches reality, especially hardware constraints.
- Establishing crystal-clear scope boundaries, Scope IN vs Scope OUT.
- Creating a concise Gate Entry Summary or L1 Context before proceeding to `$architecture-review`.

## Core Philosophy

Planning cannot succeed if the foundational context is wrong. This workflow prevents stale specs, misdiagnosed hardware behavior, and fuzzy scope from entering the expensive review and implementation path.

Value system:

- Treat SSOT or contract version drift as a hard block.
- Validate assumptions early. Hardware limitations like fixed-focus vs auto-focus fundamentally change the approach.
- Clear boundaries prevent scope creep and implementation drift.
- Prefer intake speed when a complete L1/L2 package is provided; do not rediscover the codebase unless the provided context is missing or suspect.

## Processing User Request

1. **Intake Provided Context:** If the user provides L1/L2, a Tech Plan draft, or a contract, identify the referenced SSOT, contract, ticket, scope, and hardware assumptions.
2. **Check Authority:** Confirm the referenced SSOT/contract version against the repository authority. Stop on version drift instead of repairing the plan implicitly.
3. **Explore Only As Needed:** If no adequate spec exists, inspect the relevant code/docs enough to understand current behavior. If a complete spec exists, limit exploration to resolving contradictions or missing evidence.
4. **Verify Reality:** Check for mismatches. If the user wants to fix front camera focus, verify whether the front camera actually supports AF via CameraCharacteristics or equivalent grounded evidence.
5. **Establish Boundaries:** Confirm:
   - Scope IN: files, modules, functions, or behavior allowed to change.
   - Scope OUT: files, modules, behavior, and product decisions that are banned.
   - Hardware/system constraints: CameraX, Camera2, ML Kit, IMU, lifecycle, device-tier, or runtime limits.
6. **Summarize Context:** Produce either a concise `Gate Entry Summary` for provided specs or an `L1 Context & Requirement Spec` for newly grounded requests, covering:
   - Code Area
   - Validated Problem
   - Scope Boundaries
   - Hardware or System Constraints
   - Flags or warnings for `$architecture-review`

## Acceptance Criteria

- Referenced SSOT/contract version is confirmed or version drift is explicitly blocked.
- Code area is explored only to the depth needed for the request.
- The user's stated problem is validated against codebase reality.
- Scope IN and Scope OUT are explicitly confirmed.
- Gate Entry Summary or L1 Context is generated and the user confirms alignment before moving to `$architecture-review`.
