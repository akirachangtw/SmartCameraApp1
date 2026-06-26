const vscode = require('vscode');
const fs = require('fs');
const path = require('path');

class HeadroomViewProvider {
  constructor(extensionUri, options = {}) {
    this.extensionUri = extensionUri;
    this.workspaceRoot = options.workspaceRoot;
    this.output = options.output;
    this.onStatusChange = options.onStatusChange || (() => {});
    this.view = undefined;
    this.timer = undefined;
    this.config = this.loadPilotConfig();
    this.endpointBase = this.getConfigValue('headroom.proxy_url', process.env.HEADROOM_PROXY_URL || 'http://127.0.0.1:8787');
    this.healthPath = this.getConfigValue('headroom.health_path', '/health');
    this.statsPath = this.getConfigValue('headroom.stats_path', '/stats');
    this.pollIntervalMs = Number(this.getConfigValue('headroom.dashboard.poll_interval_ms', process.env.HEADROOM_POLL_INTERVAL_MS || 5000));
    this.requestTimeoutMs = Number(this.getConfigValue('headroom.dashboard.request_timeout_ms', process.env.HEADROOM_REQUEST_TIMEOUT_MS || 3000));
    this.showRawPayload = Boolean(this.getConfigValue('headroom.dashboard.show_raw_payload', true));
  }

  getConfigUri() {
    const file = this.configPath();
    return file && fs.existsSync(file) ? vscode.Uri.file(file) : undefined;
  }

  resolveWebviewView(webviewView) {
    this.view = webviewView;

    webviewView.webview.options = {
      enableScripts: true
    };

    webviewView.webview.html = this.getHtml(webviewView.webview);

    webviewView.onDidDispose(() => {
      this.stopPolling();
    });

    webviewView.webview.onDidReceiveMessage(async (message) => {
      if (message.type === 'refresh') {
        await this.refresh();
      }
    });

    this.startPolling();
    this.refresh();
  }

  startPolling() {
    this.stopPolling();
    this.timer = setInterval(() => {
      this.refresh();
    }, this.pollIntervalMs);
  }

  stopPolling() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = undefined;
    }
  }

  async manualRefresh() {
    await this.refresh();
  }

  async refresh() {
    if (!this.view) return;

    const payload = {
      type: 'state',
      endpointBase: this.endpointBase,
      checkedAt: new Date().toISOString(),
      latencyMs: null,
      mode: this.getConfigValue('pilot_flags.enable_headroom_dashboard', true) ? 'pilot' : 'native',
      showRawPayload: this.showRawPayload,
      configPath: this.configPath(),
      health: null,
      stats: null,
      error: null
    };

    const startedAt = Date.now();
    try {
      const healthRes = await this.fetchWithTimeout(`${this.endpointBase}${this.healthPath}`);
      payload.health = await this.safeJson(healthRes);
      payload.healthHttpStatus = healthRes.status;

      try {
        const statsRes = await this.fetchWithTimeout(`${this.endpointBase}${this.statsPath}`);
        payload.stats = await this.safeJson(statsRes);
        payload.statsHttpStatus = statsRes.status;
      } catch (statsError) {
        payload.stats = null;
        payload.statsError = String(statsError?.message || statsError);
      }
    } catch (error) {
      payload.error = String(error?.message || error);
    } finally {
      payload.latencyMs = Date.now() - startedAt;
    }

    const status = this.deriveStatus(payload);
    payload.summary = status;
    this.onStatusChange(status);
    this.output?.appendLine(`[${payload.checkedAt}] ${status.label} ${status.detail}`);
    this.view.webview.postMessage(payload);
  }

  async fetchWithTimeout(url) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), this.requestTimeoutMs);
    try {
      return await fetch(url, { signal: controller.signal });
    } finally {
      clearTimeout(timer);
    }
  }

  async safeJson(response) {
    const text = await response.text();
    try {
      return JSON.parse(text);
    } catch {
      return { raw: text };
    }
  }

  loadPilotConfig() {
    const file = this.configPath();
    if (!file || !fs.existsSync(file)) {
      return {};
    }
    try {
      return JSON.parse(fs.readFileSync(file, 'utf8'));
    } catch (error) {
      this.output?.appendLine(`Failed to read pilot config: ${String(error?.message || error)}`);
      return {};
    }
  }

  configPath() {
    if (!this.workspaceRoot) {
      return undefined;
    }
    return path.join(this.workspaceRoot, '.agents', 'pilot', 'pilot.config.json');
  }

  getConfigValue(dotPath, fallback) {
    const parts = dotPath.split('.');
    let current = this.config;
    for (const part of parts) {
      if (!current || typeof current !== 'object' || !(part in current)) {
        return fallback;
      }
      current = current[part];
    }
    return current ?? fallback;
  }

  deriveStatus(payload) {
    if (payload.error) {
      return {
        label: 'Offline',
        level: 'offline',
        detail: payload.error
      };
    }

    const health = payload.health || {};
    const healthStatus = typeof health === 'object' ? health.status : undefined;
    const ready = typeof health === 'object' ? health.ready : undefined;
    const healthy = healthStatus === 'ok' || healthStatus === 'healthy' || ready === true;

    if (healthy) {
      return {
        label: 'Healthy',
        level: 'healthy',
        detail: `${this.endpointBase} responded in ${payload.latencyMs ?? '-'}ms`
      };
    }

    return {
      label: 'Degraded',
      level: 'degraded',
      detail: `Health endpoint reachable, status=${healthStatus || 'unknown'}`
    };
  }

  getHtml(webview) {
    const nonce = String(Date.now());
    return `<!DOCTYPE html>
<html lang="zh-Hant">
<head>
  <meta charset="UTF-8" />
  <meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline'; script-src 'nonce-${nonce}';">
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Headroom Dashboard</title>
  <style>
    :root {
      color-scheme: light dark;
      --bg: var(--vscode-sideBar-background, #1e1e1e);
      --panel: var(--vscode-editor-background, #252526);
      --border: var(--vscode-panel-border, #3c3c3c);
      --text: var(--vscode-foreground, #cccccc);
      --muted: var(--vscode-descriptionForeground, #9da1a6);
      --ok: #3fb950;
      --warn: #d29922;
      --bad: #f85149;
      --accent: var(--vscode-button-background, #0e639c);
      --accentText: var(--vscode-button-foreground, #ffffff);
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      padding: 16px;
      background: var(--bg);
      color: var(--text);
      font: 13px/1.5 -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
    }
    .stack { display: grid; gap: 12px; }
    .summary {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 8px;
    }
    .card {
      background: var(--panel);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 12px;
    }
    .summary-card { min-height: 82px; display: grid; align-content: space-between; gap: 8px; }
    .row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }
    .title { font-size: 11px; color: var(--muted); text-transform: uppercase; }
    .value { font-size: 16px; font-weight: 600; overflow-wrap: anywhere; }
    .hint { color: var(--muted); font-size: 12px; overflow-wrap: anywhere; }
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 6px 10px;
      border-radius: 999px;
      border: 1px solid var(--border);
    }
    .dot { width: 10px; height: 10px; border-radius: 999px; background: var(--warn); }
    .dot.ok { background: var(--ok); }
    .dot.bad { background: var(--bad); }
    .kv { display: grid; gap: 6px; }
    .kv div { word-break: break-word; }
    button {
      background: var(--accent);
      color: var(--accentText);
      border: none;
      border-radius: 8px;
      padding: 8px 10px;
      cursor: pointer;
      font: inherit;
    }
    pre {
      margin: 0;
      max-height: 220px;
      overflow: auto;
      white-space: pre-wrap;
      overflow-wrap: anywhere;
      color: var(--muted);
    }
  </style>
</head>
<body>
  <div class="stack">
    <div class="row">
      <div>
        <div class="title">Headroom Sidecar</div>
        <div class="value">Local Dashboard</div>
      </div>
      <button id="refreshBtn">Refresh</button>
    </div>

    <div class="summary">
      <div class="card summary-card">
        <div class="title">Reachability</div>
        <div class="badge"><span id="healthDot" class="dot"></span><span id="healthLabel">Checking</span></div>
        <div id="reachHint" class="hint">-</div>
      </div>
      <div class="card summary-card">
        <div class="title">Mode</div>
        <div id="modeValue" class="value">-</div>
        <div class="hint">manual rollback remains external</div>
      </div>
      <div class="card summary-card">
        <div class="title">Latency</div>
        <div id="latencyValue" class="value">-</div>
        <div class="hint">last poll</div>
      </div>
      <div class="card summary-card">
        <div class="title">Stats</div>
        <div class="badge"><span id="statsDot" class="dot"></span><span id="statsLabel">Checking</span></div>
        <div id="statsHint" class="hint">-</div>
      </div>
    </div>

    <div class="card kv">
      <div><span class="title">Endpoint</span><br /><span id="endpoint">-</span></div>
      <div><span class="title">Config</span><br /><span id="configPath">-</span></div>
      <div><span class="title">Last Checked</span><br /><span id="checkedAt">-</span></div>
      <div><span class="title">/health HTTP</span><br /><span id="healthHttp">-</span></div>
      <div><span class="title">/stats HTTP</span><br /><span id="statsHttp">-</span></div>
    </div>

    <div class="card">
      <div class="title">Stats Summary</div>
      <pre id="statsSummary">-</pre>
    </div>

    <div class="card">
      <div class="title">Health Payload</div>
      <pre id="healthPayload">-</pre>
    </div>

    <div class="card">
      <div class="title">Stats Payload</div>
      <pre id="statsPayload">-</pre>
    </div>

    <div class="card">
      <div class="title">Error</div>
      <pre id="errorPayload">-</pre>
    </div>
  </div>

  <script nonce="${nonce}">
    const vscode = acquireVsCodeApi();
    const refreshBtn = document.getElementById('refreshBtn');
    const healthDot = document.getElementById('healthDot');
    const healthLabel = document.getElementById('healthLabel');
    const reachHint = document.getElementById('reachHint');
    const modeValue = document.getElementById('modeValue');
    const latencyValue = document.getElementById('latencyValue');
    const statsDot = document.getElementById('statsDot');
    const statsLabel = document.getElementById('statsLabel');
    const statsHint = document.getElementById('statsHint');
    const endpoint = document.getElementById('endpoint');
    const configPath = document.getElementById('configPath');
    const checkedAt = document.getElementById('checkedAt');
    const healthHttp = document.getElementById('healthHttp');
    const statsHttp = document.getElementById('statsHttp');
    const healthPayload = document.getElementById('healthPayload');
    const statsPayload = document.getElementById('statsPayload');
    const statsSummary = document.getElementById('statsSummary');
    const errorPayload = document.getElementById('errorPayload');

    refreshBtn.addEventListener('click', () => {
      vscode.postMessage({ type: 'refresh' });
    });

    window.addEventListener('message', (event) => {
      const data = event.data;
      endpoint.textContent = data.endpointBase || '-';
      configPath.textContent = data.configPath || '-';
      checkedAt.textContent = data.checkedAt ? new Date(data.checkedAt).toLocaleString() : '-';
      healthHttp.textContent = data.healthHttpStatus ?? '-';
      statsHttp.textContent = data.statsHttpStatus ?? '-';
      modeValue.textContent = data.mode || '-';
      latencyValue.textContent = data.latencyMs == null ? '-' : data.latencyMs + ' ms';
      healthPayload.textContent = data.showRawPayload ? JSON.stringify(data.health, null, 2) : 'hidden by config';
      statsPayload.textContent = data.showRawPayload ? JSON.stringify(data.stats, null, 2) : 'hidden by config';
      statsSummary.textContent = summarizeStats(data.stats);
      errorPayload.textContent = data.error || data.statsError || '-';

      const healthy = data.health && (data.health.status === 'ok' || data.health.ready === true || data.health.status === 'healthy');
      healthDot.className = healthy ? 'dot ok' : (data.error ? 'dot bad' : 'dot');
      healthLabel.textContent = healthy ? 'Healthy' : (data.error ? 'Unreachable' : 'Degraded');
      reachHint.textContent = data.summary?.detail || '-';
      const statsOk = Number(data.statsHttpStatus) >= 200 && Number(data.statsHttpStatus) < 300 && !data.statsError;
      statsDot.className = statsOk ? 'dot ok' : (data.error ? 'dot bad' : 'dot');
      statsLabel.textContent = statsOk ? 'Stats OK' : (data.error ? 'Not checked' : 'Stats degraded');
      statsHint.textContent = data.statsError || (data.statsHttpStatus ? 'GET /stats HTTP ' + data.statsHttpStatus : 'GET /stats');
    });

    function summarizeStats(stats) {
      if (!stats || typeof stats !== 'object') return '-';
      const keys = ['requests_total', 'request_count', 'compression_ratio', 'cache_hit_rate', 'fallback_rate'];
      const lines = keys
        .filter((key) => Object.prototype.hasOwnProperty.call(stats, key))
        .map((key) => key + ': ' + stats[key]);
      return lines.length ? lines.join('\\n') : JSON.stringify(stats, null, 2);
    }
  </script>
</body>
</html>`;
  }
}

module.exports = {
  HeadroomViewProvider
};
