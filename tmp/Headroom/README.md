# Headroom Pilot Workspace

This directory contains implementation and evidence artifacts. The tracked wiring SSOT is `.agents/pilot/pilot.config.json`.

## Current decision (2026-07-16)

- Headroom active integration (`http://127.0.0.1:8787/health` plus `/stats`) is decommissioned; binary/config remain in place for rollback.
- Supervisor `http://127.0.0.1:8788` exposes `/events/ingest`, `/summary`, and `/events/recent` as Preview.
- Preview remains unpromoted until at least 20 real Claude Canary events satisfy the gate. The 21 fixtures validate Schema/aggregation only and contribute zero promotion evidence.
- If 8788 is offline, KPI panels show Preview/Unavailable; Headroom's decommissioned state does not change this.
- CodeGraph is optional and disabled; it is not the producer of Supervisor endpoints.

## Artifacts

| Path | Authority |
|---|---|
| `pilot-docs/pilot-runbook.md` | Current operator commands and rollback behavior |
| `pilot-docs/kpi-promotion-gate.md` | Current real-evidence promotion thresholds |
| `pilot-docs/retrieval-compression-kpi-schema.json` | Preview event Schema v0.2.0 |
| `prototype-sidecar/` | Observability Supervisor implementation and HTTP smoke tests |
| `vscode-headroom-dashboard/` | Dashboard source, compiled runtime, and VSIX |
| `pilot-docs/pilot-integration.md` | Historical design reference; not runtime authority |
| `pilot-docs/MCP-TS-HEADROOM-IMPLEMENTATION.md` | Historical umbrella and ticket provenance |

`pilot-integration.md` remains present intentionally. Its historical ports and deployment sketches are subordinate to Config v2 and this current contract.

## Verification commands

Headroom active integration is decommissioned; the commands below read back the retained removal evidence instead of probing 8787.

```powershell
Get-Content tmp/Headroom/pilot-docs/removal-evidence/service-removal-after.txt
Get-Content tmp/Headroom/pilot-docs/removal-evidence/vscode-extensions-after.txt
npm.cmd --prefix tmp/Headroom/prototype-sidecar test
npm.cmd --prefix tmp/Headroom/vscode-headroom-dashboard run test:contracts
npm.cmd --prefix tmp/Headroom/vscode-headroom-dashboard run check
npm.cmd --prefix tmp/Headroom/vscode-headroom-dashboard run package
```

When the Supervisor is running, additionally verify `http://127.0.0.1:8788/health`, `/summary`, and `/events/recent`. A failed 8788 request is only a Preview gap; it does not reflect Headroom's already-decommissioned state.
