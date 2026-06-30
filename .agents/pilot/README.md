# Headroom Pilot Wiring

This folder contains the repo-local pilot wiring SSOT for the Headroom / CodeGraph
observability pilot.

## Files

- `pilot.config.json`: endpoint, dashboard, governance path, and telemetry identity wiring.
- `codegraph-queries.md`: pilot CodeGraph query patterns, fallback reasons, and usage boundaries.
- `headroom-payload-rules.md`: Week 3 Headroom compression targets, bypass rules, and fallback levels.
- `headroom-sampling.md`: Week 3/4 Headroom quality sampling fields, scoring, and review workflow.

## Boundaries

- `AGENTS.md` remains the repo-level startup authority.
- `.agents/skills` remains the cross-agent common skill source.
- `.claude/skills` and `.codex/skills` remain runtime adapter entrypoints.
- The VS Code dashboard reads this config but does not own sidecar process lifecycle.

## Telemetry Scope & Promotion

- **Active Telemetry Contract**: Restricted to Headroom `/health` and `/stats` endpoints.
- **Promoted Telemetry Contract**: Dynamic retrieval, compression, and quality KPIs fetched from Sidecar `/summary` and `/events/recent` endpoints.
- **Degraded/Rollback Behavior**: If the Sidecar `/summary` API is unreachable or returns errors, the dashboard safely degrades to display the future KPI panels as prototype/unavailable, preserving the core `/health` and `/stats` active monitoring status.

