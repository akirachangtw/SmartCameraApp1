# Trigger Matrix

Canonical source: `devspec/99-lifecycle.md`

## Objective

Avoid session-limit collisions, excess token burn, and long-thread reasoning decay. When any trigger fires, execute a handoff and continue in a new chat.

## Forced Triggers

- **System warning**
  Signal: the chat UI shows a low remaining-message warning.
  Action: stop extending the current thread and prepare handoff immediately.

- **Task phase transition**
  Signal: one stage ends and the next stage changes work type, such as feature work to debugging, debugging to documentation, or implementation to review.
  Action: summarize completed state, then restart in a fresh chat for the next phase.

- **Logic decay**
  Signal: the model starts forgetting prior constraints, contradicting established decisions, or hallucinating details.
  Action: stop trusting conversation memory and rebuild from repository truth in a new chat.

- **Large refactor**
  Signal: code changed enough that earlier context is no longer a reliable model of the codebase.
  Action: create a fresh handoff based on the current files and restart from that new baseline.

## Decision Rule

- Any one trigger is enough.
- Do not wait for multiple symptoms.
- Prefer an early reset over recovering from a bad context state later.
