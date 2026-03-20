# Handoff Template

Use this when a reset trigger fires. Keep it short and factual.

```md
## Restart Package

Objective:
[one sentence]

Completed:
- [verified completed item]

Current verified state:
- [facts grounded in files, logs, or test output]

Files / scope:
- [exact paths in scope]
- [explicit out-of-scope or scope bans]

Pending issue:
- [the exact unresolved problem]

Next step:
- [the first concrete action for the new chat]

Risks / assumptions:
- [only unresolved risks or assumptions]

Commands or tests already run:
- [command] -> [important result]
```

## Rules

- Prefer repository truth over memory.
- Do not mix guesses into the verified-state section.
- Keep the package dense enough that a fresh chat can reload context quickly.
