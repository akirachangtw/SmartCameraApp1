# Claude Code Handoff Template

Use this structure when preparing the next execution prompt.

## Handoff Prompt

You are implementing a scoped ticket from a defensive workflow.

### Inputs

- Ticket:
- Tech Plan Spec:
- L1 Context:
- SSOT/contract reference:
- Relevant acceptance criteria:

### Scope Rules

- You may touch only these files:
- You must not touch these files/modules/behaviors:
- You must preserve these defensive patterns:
- You must not refactor outside the ticket scope:

### Deliverable

- Implement only this ticket.
- Keep the codebase working.
- Once complete, stop and report what changed, what was verified, and any blockers.

### Mandatory Warning

Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.

### Execution Discipline

1. Zero TODO Policy: every function must be fully implemented; no TODO(), pass, placeholder returns, or fake fallbacks.
2. Edge Case And Error Defense: handle null states, empty collections, array bounds, and lifecycle invalidation paths required by the ticket.
3. No Oversimplification: do not hardcode values that should be computed or skip validation required by the spec.
4. Self-Cross-Check: re-read every acceptance criterion before reporting completion.
5. No Silent Failures: every catch block must log or surface enough information for review; every fallback must explain why it is valid.
