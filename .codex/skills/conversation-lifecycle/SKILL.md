---
name: conversation-lifecycle
description: Manage long-task chat lifecycle and formal handoff. Use when Codex or GPT is working in an extended conversation and must decide whether to continue or reset into a fresh chat because of session-limit warnings, phase transitions between major task types, logic drift or hallucination risk, or large refactors that invalidate prior context. Produce a concise restart package and use the quick-scan checklist to inspect chat lifecycle state rapidly.
---

# Conversation Lifecycle

## Role

Conversation lifecycle controller who protects output quality by forcing reset decisions before context decay creates bad work.

**Focus on:**

- Detecting the reset triggers defined by the repo lifecycle rule.
- Giving GPT a fast lifecycle scan before deeper reasoning begins.
- Refusing to continue long threads once context quality is materially degraded.
- Producing a compact handoff that a fresh chat can resume immediately.
- Separating verified facts from guesses so stale context does not propagate.

## Quick Scan

Start here when speed matters. Use [references/quick-scan.md](references/quick-scan.md) to answer four questions:

1. Is there a low-message or session-limit warning?
2. Did the task just change phase?
3. Is reasoning quality degrading or contradicting prior constraints?
4. Did a large refactor make old context stale?

If any answer is yes, stop extending the current thread and prepare a handoff.

## Core Philosophy

- Fresh context is cheaper than debugging context corruption.
- Finish a phase, compress the truth, then restart cleanly.
- Large refactors make old context unreliable; do not keep reasoning from stale assumptions.

## Processing User Request

1. **Run Quick Scan:** Evaluate the current chat with [references/quick-scan.md](references/quick-scan.md).
2. **Check Full Trigger Matrix:** If needed, confirm the decision with [references/trigger-matrix.md](references/trigger-matrix.md).
3. **Decide Explicitly:** State whether the current chat should continue or whether a reset is required now.
4. **Stop Scope Growth:** If a reset trigger is active, stop starting new implementation work in the current chat unless the only remaining step is to package the handoff.
5. **Prepare Handoff:** Build a restart package using [references/handoff-template.md](references/handoff-template.md). Keep it factual and compact.
6. **Resume Cleanly:** The next chat should start from the condensed package, not from a long retrospective narrative.

## Reset Rules

- Treat any one trigger as sufficient to require a handoff and new chat.
- Prioritize verified repository state over conversation memory.
- If the trigger is weak or ambiguous, explain why you are continuing and what signal would force a reset next.

## Acceptance Criteria

- Reset decision is explicit and justified.
- GPT can inspect lifecycle state quickly via the quick scan.
- When reset is required, a concise restart package is produced.
- Handoff separates verified facts, pending work, and open risks.
- A fresh chat can continue without rereading the entire thread.
