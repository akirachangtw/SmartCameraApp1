# Headroom Dashboard (Pilot Skeleton)

A minimal VS Code extension skeleton that opens a sidebar webview and polls a local Headroom sidecar.

## What it does

- Adds a **Headroom** activity bar icon.
- Opens a sidebar dashboard named **Local Sidecar**.
- Adds a status bar health summary.
- Reads repo pilot wiring from `.agents/pilot/pilot.config.json` when available.
- Polls:
  - `GET /health`
  - `GET /stats`
- Displays reachability, basic status, latency, last check time, raw payloads, and a small stats summary.

## Expected local runtime

- Headroom proxy already installed and runnable.
- Default endpoint: `http://127.0.0.1:8787`

## Start sidecar

```bash
bash scripts/start-headroom-sidecar.sh
```

The extension intentionally does not own sidecar process lifecycle in this pilot version.
Start and stop the sidecar outside VS Code so rollback stays simple.

## Environment variables

### Sidecar launcher

- `HEADROOM_PROXY_HOST` default `127.0.0.1`
- `HEADROOM_PROXY_PORT` default `8787`
- `HEADROOM_LOG_DIR` default `.headroom`

### VS Code extension host

- `HEADROOM_PROXY_URL` default `http://127.0.0.1:8787`
- `HEADROOM_POLL_INTERVAL_MS` default `5000`
- `HEADROOM_REQUEST_TIMEOUT_MS` default `3000`

If `.agents/pilot/pilot.config.json` exists in the workspace root, it takes precedence over these defaults.

## Notes

- This is a minimal skeleton, not a packaged production extension.
- `src/extension.js` and `src/headroomViewProvider.js` are plain CommonJS to minimize setup.
- This version follows the pilot decision patch: Headroom `/health` and `/stats` are enabled; CodeGraph fields are reserved in config but not rendered as an active card yet.
