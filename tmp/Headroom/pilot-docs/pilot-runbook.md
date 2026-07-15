# Headroom Pilot Runbook

## Runtime contract

- Active: Headroom Windows Service at `127.0.0.1:8787`, endpoints `/health` and `/stats`.
- Preview: Observability Supervisor at `127.0.0.1:8788`, endpoints `/health`, `/events/ingest`, `/summary`, `/events/recent`.
- Promotion: at least 20 `source_kind=real` Claude Canary events plus all thresholds in `kpi-promotion-gate.md`.
- Fixtures validate mechanics only. They cannot increment `promotion_real_event_count`.

## Daily checks

```powershell
curl.exe --fail http://127.0.0.1:8787/health
curl.exe --fail http://127.0.0.1:8787/stats
npm.cmd --prefix tmp/Headroom/prototype-sidecar test
npm.cmd --prefix tmp/Headroom/vscode-headroom-dashboard run test:contracts
```

If the Supervisor is intentionally running:

```powershell
curl.exe --fail http://127.0.0.1:8788/health
curl.exe --fail http://127.0.0.1:8788/summary
curl.exe --fail http://127.0.0.1:8788/events/recent
```

## Interpretation

| Condition | Active lamp | Future KPI | Action |
|---|---|---|---|
| 8787 health/stats OK; 8788 offline | Healthy | Preview/Unavailable | Continue active use; investigate Preview separately |
| 8787 health or stats fails | Degraded/Offline | Unchanged | Follow Headroom service recovery |
| 8788 returns degraded evidence | Unchanged | HOLD/BLOCKED | Inspect invalid JSONL/schema lines |
| 21 fixtures, 0 real | Unchanged | BLOCKED | Run real Canary; do not promote |
| 20+ real but threshold fails | Unchanged | HOLD/BLOCKED | Restore route and analyze failed metric |

## Incident and rollback

1. Never restart or rewrite the existing 8787 Service as part of Supervisor recovery.
2. Stop only the transient Supervisor/Canary process.
3. Run the Canary stop script to unwrap and restore the saved Claude routing/config snapshot.
4. Verify 8787 `/health + /stats` again.
5. Keep Preview blocked until provenance, redaction, bypass, and restore evidence are complete.

## Packaging verification

```powershell
npm.cmd --prefix tmp/Headroom/vscode-headroom-dashboard run check
npm.cmd --prefix tmp/Headroom/vscode-headroom-dashboard run package
tar.exe -tf tmp/Headroom/vscode-headroom-dashboard/vscode-headroom-dashboard-0.0.1.vsix
```

The VSIX must contain `extension/out/headroomViewProvider.js` and the compiled config/gate services. Source-only success is insufficient.
