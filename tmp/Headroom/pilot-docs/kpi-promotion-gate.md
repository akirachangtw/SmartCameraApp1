# KPI Promotion Gate

## Current ruling

KPI status is **Preview / BLOCKED**. Active observability remains Headroom `8787 /health + /stats`. Supervisor `8788 /summary + /events/recent` does not become active merely because it responds.

## Required promotion evidence

| Gate | PASS condition | Failure result |
|---|---|---|
| Source provenance | `source_kind=real`; fixture/replay/injected state excluded | BLOCKED |
| Real event count | `promotion_real_event_count >= 20` | BLOCKED |
| Source status | config and runtime both explicitly `promoted=true`; runtime `source_status=promoted` | BLOCKED |
| Schema | Every event passes KPI Schema v0.2.0 and ratio formula checks | BLOCKED |
| Reachability | Real HTTP ingest reaches JSONL store and `/summary` | BLOCKED |
| Degraded evidence | Invalid JSONL/schema lines are zero | HOLD/BLOCKED |
| Safety bypass | Complete code, Diff/Patch, conversation, Read/Grep source, line-numbered CodeGraph source, fragile errors bypass 100% | BLOCKED |
| Savings | Eligible payload median token savings `>=30%` | HOLD |
| Quality | Average quality `>=4/5` | HOLD |
| Fallback | `<5%` | HOLD |
| Overhead | p95 `<=1000 ms` | HOLD |
| Restore | Canary stop/error paths restore original routing/config | BLOCKED |
| Docs/config/runtime | Active/Preview and 8787/8788 descriptions agree | HOLD |

Fixtures may make Schema, validator, aggregation, and HTTP smoke tests PASS, but can never satisfy source provenance or real event count.

## State semantics

- **PASS**: every required gate above passes; explicit promotion still requires config/runtime promotion flags.
- **HOLD**: production path exists, but a quality/performance/governance threshold is incomplete.
- **BLOCKED**: source is prototype, unpromoted, unreachable, unsafe, fixture-only, or evidence is invalid.

## Verification

```powershell
npm.cmd --prefix tmp/Headroom/prototype-sidecar test
npm.cmd --prefix tmp/Headroom/vscode-headroom-dashboard run test:contracts
curl.exe --fail http://127.0.0.1:8787/health
curl.exe --fail http://127.0.0.1:8787/stats
```

Supervisor smoke uses an isolated temporary store. Live promotion requires the Canary evidence workflow; fixture smoke output is not a promotion claim.
