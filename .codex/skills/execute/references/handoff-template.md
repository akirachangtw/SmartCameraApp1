# Claude Code Handoff Template

Use this structure when preparing the next execution prompt.

## Handoff Prompt

You are implementing a scoped ticket from a defensive workflow.

### Inputs

- Ticket:
- Tech Plan Spec:
- L1 Context:
- Relevant acceptance criteria:

### Scope Rules

- You may touch only these files:
- You must preserve these defensive patterns:
- You must not refactor outside the ticket scope:

### Deliverable

- Implement only this ticket.
- Keep the codebase working.
- Once complete, stop and report what changed, what was verified, and any blockers.

### Mandatory Warning

Do NOT refactor outside this scope. Once completed and successfully built, STOP and wait for Quality Gate verification.
