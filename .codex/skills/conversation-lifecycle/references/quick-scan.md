# Quick Scan

Use this before deep reasoning. The goal is to decide in under 15 seconds whether the current chat is still healthy.

## Four Questions

1. **Limit warning**
   Is the system showing a low-message warning or similar session-limit alert?

2. **Phase shift**
   Did the work just move into a different phase, such as implementation to debugging, debugging to documentation, or development to review?

3. **Reasoning drift**
   Is the model forgetting established constraints, contradicting prior decisions, or hallucinating details that are not grounded in the files?

4. **Stale context**
   Did a large refactor or broad file change make earlier discussion unreliable?

## Decision

- If all four answers are no: continue in the current chat.
- If any answer is yes: stop adding new work here, build a restart package, and continue in a fresh chat.
