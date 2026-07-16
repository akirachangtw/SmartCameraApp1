# Headroom Pilot Wiring

This folder is the repo-local wiring SSOT for the Headroom observability pilot. `AGENTS.md` remains the startup authority; `pilot.config.json` owns endpoint wiring.

## Files

- `pilot.config.json`: endpoint, dashboard, governance path, and telemetry identity wiring.
- `codegraph-queries.md`: pilot CodeGraph query patterns, fallback reasons, and usage boundaries.
- `headroom-payload-rules.md`: Week 3 Headroom compression targets, bypass rules, and fallback levels.
- `headroom-sampling.md`: Week 3/4 Headroom quality sampling fields, scoring, and review workflow.

## Current runtime contract (2026-07-16)

- **Decommissioned**: Headroom active integration (`http://127.0.0.1:8787`, `GET /health` and `GET /stats`) is disabled; binary/config remain in place for rollback and it is no longer the active runtime.
- **Preview**: Observability Supervisor on `http://127.0.0.1:8788`, using `POST /events/ingest`, `GET /summary`, and `GET /events/recent`.
- `/summary` and `/events/recent` remain Preview until at least 20 `source_kind=real` canary events pass the gate and both config and runtime report promoted.
- `source_kind=fixture` is test data and never promotion evidence.
- Headroom's decommissioned state does not change Supervisor Preview status.
- CodeGraph remains optional and disabled; it is not the producer of Supervisor endpoints.

## Boundaries

- `AGENTS.md` remains the repo-level startup authority.
- `.agents/skills` remains the cross-agent common skill source.
- `.claude/skills` and `.codex/skills` remain runtime adapter entrypoints.
- The dashboard reads this config but does not own the lifecycle of the 8787 Windows Service or the 8788 Supervisor.
