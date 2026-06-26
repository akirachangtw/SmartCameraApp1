#!/usr/bin/env bash
set -euo pipefail

PORT="${HEADROOM_PROXY_PORT:-8787}"
HOST="${HEADROOM_PROXY_HOST:-127.0.0.1}"
LOG_DIR="${HEADROOM_LOG_DIR:-.headroom}"
mkdir -p "$LOG_DIR"

if ! command -v headroom >/dev/null 2>&1; then
  echo "headroom command not found. Install with: pip install 'headroom-ai[proxy]'" >&2
  exit 1
fi

echo "Starting Headroom proxy on ${HOST}:${PORT}"
exec headroom proxy --host "$HOST" --port "$PORT" 2>&1 | tee -a "$LOG_DIR/headroom-proxy.log"
