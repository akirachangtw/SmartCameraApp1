# Headroom Pilot Workspace

This folder contains the working artifacts for the Headroom / CodeGraph pilot. The active repo configuration is not here; it lives at `.agents/pilot/pilot.config.json`.

## Current Decision

- `AGENTS.md` remains the repo-level prompt contract.
- `.agents/skills` remains the cross-agent common skill source.
- `.claude/skills` and `.codex/skills` remain runtime adapter roots.
- `.agents/pilot/pilot.config.json` is the canonical pilot wiring config.
- The VS Code dashboard polls Headroom `/health` and `/stats`.
- `/summary` was an early draft endpoint and is no longer an acceptance target.
- CodeGraph is reserved in config and docs, but its dashboard card is not active in the current phase.

## Kept Artifacts

| Path | Role |
|---|---|
| `pilot-docs/pilot-runbook.md` | Daily checks, incident handling, rollout checklist, and active endpoint decision. |
| `pilot-docs/dashboard-metrics-schema.json` | Active Headroom-only `/health` and `/stats` telemetry schema. |
| `pilot-docs/retrieval-compression-kpi-schema.json` | Draft companion schema for the next KPI phase. Not an active gate. |
| `pilot-docs/pilot-ops-visualization.html` | Interactive ops view for dashboard blocks, status lamps, health flow, and rollout checks. |
| `vscode-headroom-dashboard/` | Local VS Code dashboard extension source plus packaged `.vsix`. |

## Removed During Consolidation

The following artifacts were removed because their durable content is now represented by the kept files above or by `.agents/pilot/pilot.config.json`:

- `pilot-integration.md`: early broad proposal with obsolete `/summary` references.
- `headroom-pilot-decision.md`: decision content consolidated into this README plus the runbook.
- `repo-decision/`: duplicate config bundle; both copies matched `.agents/pilot/pilot.config.json`.

## Verification Commands

```bash
# Validate JSON docs
powershell -NoProfile -Command "Get-Content tmp/Headroom/pilot-docs/dashboard-metrics-schema.json -Raw | ConvertFrom-Json | Out-Null; Get-Content tmp/Headroom/pilot-docs/retrieval-compression-kpi-schema.json -Raw | ConvertFrom-Json | Out-Null; Get-Content .agents/pilot/pilot.config.json -Raw | ConvertFrom-Json | Out-Null"

# Validate dashboard extension syntax
cd tmp/Headroom/vscode-headroom-dashboard
npm.cmd run check

# Rebuild local VSIX
npm.cmd run package
```
