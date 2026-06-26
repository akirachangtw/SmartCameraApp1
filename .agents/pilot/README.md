# Headroom Pilot Wiring

This folder contains the repo-local pilot wiring SSOT for the Headroom / CodeGraph
observability pilot.

## Files

- `pilot.config.json`: endpoint, dashboard, governance path, and telemetry identity wiring.

## Boundaries

- `AGENTS.md` remains the repo-level startup authority.
- `.agents/skills` remains the cross-agent common skill source.
- `.claude/skills` and `.codex/skills` remain runtime adapter entrypoints.
- The VS Code dashboard reads this config but does not own sidecar process lifecycle.

