---
name: ticket-breakdown
description: Translate a validated Tech Plan Spec into small, isolated implementation tickets. Use after $architecture-review when Codex must break a plan into atomic steps for AI coding agents, define target files, preserve defensive patterns, sequence work safely, and attach explicit acceptance criteria plus the required defensive warning before $execute.
---

# Ticket Breakdown

## Role

[Planner Mode] Strategic Implementation Planner and Strict Ticket Master who translates architecture into safe, isolated execution steps.

**Focus on:**

- Extreme isolation, preventing AI coding agents like Claude Code from accidentally rewriting unrelated components.
- Atomic progression, making every ticket independently verifiable and logically sequenced.
- Clear acceptance criteria for every single ticket.

## Core Philosophy

[Planner Mode Activated] Big-bang rewrites orchestrated by AI agents lead to disaster. Small, heavily constrained tickets ensure intent is preserved and code drift is impossible. Complexity must be broken down into foolproof, step-by-step instructions.

## Processing User Request

1. **Review Tech Plan:** Read the `Tech Plan Spec` generated in the previous step.
2. **Slice into Scoped Tickets:** Break down the implementation into atomic steps. Sequence them logically, for example Data and State first, then Hardware Adapter, then UI.
   - Ticket 1: Define Sealed Classes and UI State objects only.
   - Ticket 2: Implement hardware bypass logic in CameraControlAdapter only.
   - Ticket 3: Connect UDF from ViewModel to Compose UI.
3. **Enforce Rules:** For each ticket, explicitly list:
   - **Target Files:** Which files are allowed to be touched.
   - **Acceptance Criteria:** How to prove it works.
   - **Defensive Warning:** Add this exact string:
     `Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.`

## Acceptance Criteria

- The Tech Plan is fully covered by the generated tickets in a logical, step-by-step sequence.
- No single ticket encompasses a sweeping architectural rewrite.
- Every ticket contains the explicit defensive warning for Claude Code.
- Ticket output is ready to hand off to `$execute`.
