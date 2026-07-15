import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import { joinEndpoint, loadPilotConfig, resolveHeadroomEndpoints, resolveSupervisorEndpoints } from './services/configLoader';
import { resolvePilotConfigPath } from './services/extensionPaths';
import { fetchSidecarSummary, fetchWithTimeout } from './services/dataFetcher';
import { evaluatePromotionGate } from './services/promotionGate';
import { mapSummaryToPanels } from './services/dashboardMapper';
import { evaluateRiskSignals } from './services/riskEvaluator';

export class HeadroomViewProvider implements vscode.WebviewViewProvider {
  private view?: vscode.WebviewView;
  private timer?: NodeJS.Timeout;
  private readonly workspaceRoot?: string;
  private readonly output?: vscode.OutputChannel;
  private readonly onStatusChange: (state: { label: string; level: string; detail: string; mode?: string }) => void;
  private readonly context?: vscode.ExtensionContext;

  private readonly pollIntervalMs: number;

  constructor(
    private readonly extensionUri: vscode.Uri,
    options: {
      workspaceRoot?: string;
      output?: vscode.OutputChannel;
      onStatusChange?: (state: { label: string; level: string; detail: string; mode?: string }) => void;
      context?: vscode.ExtensionContext;
    } = {}
  ) {
    this.workspaceRoot = options.workspaceRoot;
    this.output = options.output;
    this.onStatusChange = options.onStatusChange || (() => {});
    this.context = options.context;

    const config = loadPilotConfig(this.configPath());
    this.pollIntervalMs = Number(this.getConfigValue(config, 'headroom.dashboard.poll_interval_ms', process.env.HEADROOM_POLL_INTERVAL_MS || 5000));
  }

  public getConfigUri(): vscode.Uri | undefined {
    const file = this.configPath();
    return file && fs.existsSync(file) ? vscode.Uri.file(file) : undefined;
  }

  public resolveWebviewView(webviewView: vscode.WebviewView): void {
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
    void this.refresh();
  }

  public startPolling(): void {
    this.stopPolling();
    this.timer = setInterval(() => {
      void this.refresh();
    }, this.pollIntervalMs);
  }

  public stopPolling(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = undefined;
    }
  }

  public async manualRefresh(): Promise<void> {
    await this.refresh();
  }

  public async refresh(): Promise<void> {
    if (!this.view) {
      return;
    }

    const config = loadPilotConfig(this.configPath());
    const headroomEndpoints = resolveHeadroomEndpoints(config);
    const supervisorEndpoints = resolveSupervisorEndpoints(config);
    const endpointBase = headroomEndpoints.proxyUrl ?? process.env.HEADROOM_PROXY_URL ?? 'http://127.0.0.1:8787';
    const requestTimeoutMs = Number(this.getConfigValue(config, 'headroom.dashboard.request_timeout_ms', process.env.HEADROOM_REQUEST_TIMEOUT_MS || 3000));
    const showRawPayload = Boolean(this.getConfigValue(config, 'headroom.dashboard.show_raw_payload', true));
    const payload: {
      type: string;
      endpointBase: string;
      checkedAt: string;
      latencyMs: number | null;
      mode: string;
      showRawPayload: boolean;
      configPath: string | undefined;
      health: any;
      stats: any;
      error: string | null;
      healthHttpStatus?: number | null;
      statsHttpStatus?: number | null;
      statsError?: string | null;
      summary?: { label: string; level: string; detail: string };
      panels?: any[];
      risks?: any[];
      gate?: any;
    } = {
      type: 'state',
      endpointBase,
      checkedAt: new Date().toISOString(),
      latencyMs: null,
      mode: this.getConfigValue(config, 'pilot_flags.enable_headroom_dashboard', true) ? 'pilot' : 'native',
      showRawPayload,
      configPath: this.configPath(),
      health: null,
      stats: null,
      error: null
    };

    const startedAt = Date.now();
    const [healthResult, statsResult] = await Promise.all([
      fetchWithTimeout<any>(joinEndpoint(endpointBase, headroomEndpoints.healthPath), requestTimeoutMs),
      fetchWithTimeout<any>(joinEndpoint(endpointBase, headroomEndpoints.statsPath), requestTimeoutMs)
    ]);
    payload.health = healthResult.data;
    payload.healthHttpStatus = healthResult.status;
    payload.error = healthResult.ok ? null : healthResult.error ?? `HTTP ${healthResult.status}`;
    payload.stats = statsResult.data;
    payload.statsHttpStatus = statsResult.status;
    payload.statsError = statsResult.ok ? null : statsResult.error ?? `HTTP ${statsResult.status}`;
    payload.latencyMs = Date.now() - startedAt;

    const status = this.deriveStatus(payload);
    payload.summary = status;

    const healthOk = payload.healthHttpStatus !== undefined && payload.healthHttpStatus !== null && payload.healthHttpStatus >= 200 && payload.healthHttpStatus < 300 && !payload.error;
    const statsOk = payload.statsHttpStatus !== undefined && payload.statsHttpStatus !== null && payload.statsHttpStatus >= 200 && payload.statsHttpStatus < 300 && !payload.statsError;

    let summary: any = null;
    let summaryError: string | null = null;

    try {
      if (!supervisorEndpoints.enabled || !supervisorEndpoints.baseUrl) throw new Error('Supervisor preview is not configured');
      const summaryUrl = joinEndpoint(supervisorEndpoints.baseUrl, supervisorEndpoints.summaryPath);
      const summaryResult = await fetchSidecarSummary(summaryUrl, requestTimeoutMs);
      if (summaryResult.ok && summaryResult.data) {
        summary = summaryResult.data;
      } else {
        summaryError = summaryResult.error || `HTTP ${summaryResult.status}`;
      }
    } catch (e: any) {
      summaryError = e.message || String(e);
    }

    const hasSchemaNote = this.getConfigValue(config, 'headroom.promotion_gate.has_schema_note', true);
    const hasFallbackMapping = this.getConfigValue(config, 'headroom.promotion_gate.has_fallback_mapping', true);
    const activeReadmeAligned = this.getConfigValue(config, 'headroom.promotion_gate.readme_aligned', true);
    const codegraphEnabled = this.getConfigValue(config, 'codegraph.enabled', false);

    let codegraphOk = false;
    let codegraphHttpStatus: number | null = null;
    let codegraphError: string | null = null;

    const enableCodegraphHealth = this.getConfigValue(config, 'pilot_flags.enable_codegraph_health', false);
    if (codegraphEnabled && enableCodegraphHealth) {
      try {
        const cgHealthUrl = this.getConfigValue(config, 'codegraph.health_url', 'http://127.0.0.1:8788/health');
        const cgRes = await fetchWithTimeout<any>(cgHealthUrl, requestTimeoutMs);
        codegraphHttpStatus = cgRes.status;
        if (cgRes.ok) {
          codegraphOk = true;
        } else {
          codegraphError = `HTTP ${cgRes.status}`;
        }
      } catch (e: any) {
        codegraphError = e.message || String(e);
      }
    }

    const sourceDescriptor = {
      contract: 'futurePromotedContract' as const,
      label: 'Future Promoted Contract',
      available: summary !== null,
      promoted: supervisorEndpoints.promoted && summary?.promoted === true && summary?.source_status === 'promoted',
      sourceStatus: summary?.source_status ?? (summaryError ? 'unavailable' : supervisorEndpoints.sourceStatus)
    };

    const gateInput = {
      source: sourceDescriptor,
      hasSchemaNote,
      realEventCount: summary?.promotion_real_event_count ?? 0,
      hasFallbackMapping,
      activeReadmeAligned,
      codegraphEnabled,
      codegraphOk
    };

    const gateResult = evaluatePromotionGate(gateInput);

    payload.panels = mapSummaryToPanels(
      healthOk,
      payload.healthHttpStatus ?? null,
      payload.error,
      statsOk,
      payload.statsHttpStatus ?? null,
      payload.statsError ?? null,
      summary,
      summaryError,
      payload.latencyMs ?? 0,
      gateResult.state,
      codegraphEnabled,
      codegraphOk,
      codegraphError
    );

    const mockSnapshot = {
      activeRepo: this.workspaceRoot ? path.basename(this.workspaceRoot) : null,
      activeAgentEntry: 'claude',
      currentMode: payload.mode as any,
      healthState: (healthOk ? 'healthy' : 'offline') as any,
      fallbackLevel: 'none' as any,
      endpoint: endpointBase,
      configPath: this.configPath() ?? null,
      httpStatus: payload.healthHttpStatus ?? null,
      lastUpdatedAt: payload.checkedAt,
      metrics: {
        retrievalHitRate: { label: 'Hit Rate', value: summary ? summary.retrieval_hit_rate : null },
        compressionRatioAvg: { label: 'Token Savings Rate', value: summary ? summary.token_savings_rate : null },
        qualityScoreAvg: { label: 'Evidence Status', value: summary?.evidence_status === 'complete' ? 5 : null }
      }
    };

    payload.risks = evaluateRiskSignals(mockSnapshot, {
      contract: 'activeHeadroomContract',
      available: healthOk && statsOk,
      promoted: true
    });
    payload.gate = gateResult;

    this.onStatusChange({
      ...status,
      mode: payload.mode
    });
    this.output?.appendLine(`[${payload.checkedAt}] ${status.label} ${status.detail}`);
    void this.view.webview.postMessage(payload);
  }

  private configPath(): string | undefined {
    if (this.context) {
      return resolvePilotConfigPath(this.context, {
        workspaceRoot: this.workspaceRoot
      });
    }
    if (!this.workspaceRoot) {
      return undefined;
    }
    return path.join(this.workspaceRoot, '.agents', 'pilot', 'pilot.config.json');
  }

  private getConfigValue(config: any, dotPath: string, fallback: any): any {
    const parts = dotPath.split('.');
    let current = config;
    for (const part of parts) {
      if (!current || typeof current !== 'object' || !(part in current)) {
        return fallback;
      }
      current = current[part];
    }
    return current ?? fallback;
  }

  private deriveStatus(payload: any): { label: string; level: string; detail: string } {
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
        detail: `${payload.endpointBase} responded in ${payload.latencyMs ?? '-'}ms`
      };
    }

    return {
      label: 'Degraded',
      level: 'degraded',
      detail: `Health endpoint reachable, status=${healthStatus || 'unknown'}`
    };
  }

  private getHtml(webview: vscode.Webview): string {
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
      --bg: var(--vscode-sideBar-background, #0d1117);
      --panel: var(--vscode-editor-background, #161b22);
      --border: var(--vscode-panel-border, #30363d);
      --text: var(--vscode-foreground, #c9d1d9);
      --muted: var(--vscode-descriptionForeground, #8b949e);

      --ok: #238636;
      --ok-glow: rgba(35, 134, 54, 0.4);
      --warn: #d29922;
      --warn-glow: rgba(210, 153, 34, 0.4);
      --bad: #da3633;
      --bad-glow: rgba(218, 54, 51, 0.4);
      --info: #388bfd;
      --info-glow: rgba(56, 139, 253, 0.4);

      --accent: var(--vscode-button-background, #1f6feb);
      --accent-hover: var(--vscode-button-hoverBackground, #388bfd);
      --accentText: var(--vscode-button-foreground, #ffffff);
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      padding: 14px;
      background: var(--bg);
      color: var(--text);
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      font-size: 13px;
      line-height: 1.5;
    }
    .stack { display: grid; gap: 14px; }
    .row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }
    .title-area { display: flex; flex-direction: column; }
    .main-title { font-size: 16px; font-weight: 700; color: #f0f6fc; }
    .subtitle { font-size: 11px; color: var(--muted); text-transform: uppercase; letter-spacing: 0.5px; }

    /* Top Status Bar */
    .status-banner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: rgba(22, 27, 34, 0.8);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 10px 14px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      backdrop-filter: blur(8px);
    }
    .status-indicator { display: flex; align-items: center; gap: 8px; }
    .lamp {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: #8b949e;
      box-shadow: 0 0 0 0 rgba(139, 148, 158, 0.5);
    }
    .lamp.healthy {
      background: var(--ok);
      animation: pulse-healthy 2s infinite;
    }
    .lamp.degraded {
      background: var(--warn);
      animation: pulse-degraded 2s infinite;
    }
    .lamp.offline {
      background: var(--bad);
      animation: pulse-bad 2s infinite;
    }
    @keyframes pulse-healthy {
      0% { box-shadow: 0 0 0 0 var(--ok-glow); }
      70% { box-shadow: 0 0 0 6px rgba(35, 134, 54, 0); }
      100% { box-shadow: 0 0 0 0 rgba(35, 134, 54, 0); }
    }
    @keyframes pulse-degraded {
      0% { box-shadow: 0 0 0 0 var(--warn-glow); }
      70% { box-shadow: 0 0 0 6px rgba(210, 153, 34, 0); }
      100% { box-shadow: 0 0 0 0 rgba(210, 153, 34, 0); }
    }
    @keyframes pulse-bad {
      0% { box-shadow: 0 0 0 0 var(--bad-glow); }
      70% { box-shadow: 0 0 0 6px rgba(218, 54, 51, 0); }
      100% { box-shadow: 0 0 0 0 rgba(218, 54, 51, 0); }
    }
    .status-text { font-weight: 600; color: #f0f6fc; }

    button {
      background: var(--accent);
      color: var(--accentText);
      border: 1px solid rgba(240, 246, 252, 0.1);
      border-radius: 6px;
      padding: 6px 12px;
      cursor: pointer;
      font-size: 12px;
      font-weight: 600;
      transition: background 0.2s ease, transform 0.1s ease;
    }
    button:hover { background: var(--accent-hover); }
    button:active { transform: scale(0.98); }

    /* Risk Alerts */
    .alerts-section {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .no-alerts {
      background: rgba(35, 134, 54, 0.08);
      border: 1px dashed rgba(35, 134, 54, 0.3);
      color: #3fb950;
      border-radius: 8px;
      padding: 12px;
      text-align: center;
      font-weight: 500;
    }
    .risk-card {
      background: var(--panel);
      border-left: 4px solid var(--border);
      border-radius: 0 8px 8px 0;
      border-top: 1px solid var(--border);
      border-right: 1px solid var(--border);
      border-bottom: 1px solid var(--border);
      padding: 10px 12px;
      transition: transform 0.2s ease;
    }
    .risk-card:hover { transform: translateX(2px); }
    .risk-card.severity-info { border-left-color: var(--info); }
    .risk-card.severity-warn { border-left-color: var(--warn); }
    .risk-card.severity-critical { border-left-color: var(--bad); }
    .risk-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 6px;
    }
    .risk-title { font-weight: 700; color: #f0f6fc; }
    .risk-badge {
      font-size: 9px;
      font-weight: 700;
      padding: 2px 6px;
      border-radius: 4px;
    }
    .badge-info { background: rgba(56, 139, 253, 0.15); color: #58a6ff; border: 1px solid rgba(56, 139, 253, 0.3); }
    .badge-warn { background: rgba(210, 153, 34, 0.15); color: #f0883f; border: 1px solid rgba(210, 153, 34, 0.3); }
    .badge-critical { background: rgba(218, 54, 51, 0.15); color: #ff7b72; border: 1px solid rgba(218, 54, 51, 0.3); }
    .risk-body p { margin: 0 0 6px 0; color: #c9d1d9; font-size: 12px; }
    .risk-fallback {
      background: rgba(240, 246, 252, 0.05);
      border-radius: 4px;
      padding: 6px 8px;
      font-size: 11px;
      color: #8b949e;
    }

    /* Promotion Gate */
    .gate-section { display: flex; flex-direction: column; }
    .gate-card {
      background: var(--panel);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 12px;
    }
    .gate-card.state-pass { border-color: rgba(35, 134, 54, 0.5); }
    .gate-card.state-hold { border-color: rgba(210, 153, 34, 0.5); }
    .gate-card.state-blocked { border-color: rgba(218, 54, 51, 0.5); }
    .gate-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid var(--border);
      padding-bottom: 8px;
      margin-bottom: 10px;
    }
    .gate-title { font-weight: 700; color: #f0f6fc; }
    .gate-badge {
      font-size: 10px;
      font-weight: 700;
      padding: 2px 6px;
      border-radius: 99px;
      text-transform: uppercase;
    }
    .gate-badge.badge-pass { background: var(--ok); color: #ffffff; }
    .gate-badge.badge-hold { background: var(--warn); color: #0d1117; }
    .gate-badge.badge-blocked { background: var(--bad); color: #ffffff; }
    .gate-section-title { font-size: 11px; font-weight: bold; color: var(--muted); text-transform: uppercase; margin: 8px 0 4px 0; }
    .gate-card ul { margin: 0 0 10px 0; padding-left: 18px; }
    .gate-card li { margin-bottom: 4px; font-size: 12px; }
    .gate-blocker { color: #ff7b72; }
    .gate-warning { color: #f0883f; }
    .evidence-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 6px;
      margin-top: 6px;
    }
    .evidence-item {
      display: flex;
      align-items: center;
      gap: 6px;
      background: rgba(240, 246, 252, 0.03);
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 11px;
    }
    .evidence-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #8b949e;
    }
    .evidence-item.ok .evidence-dot { background: #3fb950; }
    .evidence-item.fail .evidence-dot { background: #f85149; }
    .evidence-key { color: var(--muted); }
    .evidence-val { font-weight: bold; color: #f0f6fc; }

    /* Panels Grid */
    .section-header { font-weight: 700; color: #f0f6fc; margin: 10px 0 4px 0; }
    .panels-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 12px;
    }
    .panel-card {
      background: var(--panel);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 12px;
      transition: box-shadow 0.2s ease, border-color 0.2s ease;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .panel-card.state-active {
      border-color: rgba(31, 111, 235, 0.4);
      box-shadow: 0 2px 8px rgba(0,0,0,0.15);
    }
    .panel-card.state-prototype {
      border-style: dashed;
      opacity: 0.85;
      background: rgba(22, 27, 34, 0.4);
    }
    .panel-card.state-blocked, .panel-card.state-unavailable {
      opacity: 0.65;
      background: rgba(22, 27, 34, 0.2);
    }
    .panel-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .panel-title { font-weight: 700; color: #f0f6fc; }
    .panel-badge {
      font-size: 9px;
      font-weight: bold;
      padding: 2px 6px;
      border-radius: 99px;
      text-transform: uppercase;
    }
    .panel-badge.badge-active { background: rgba(31, 111, 235, 0.15); color: #58a6ff; border: 1px solid rgba(31, 111, 235, 0.3); }
    .panel-badge.badge-prototype { background: rgba(210, 153, 34, 0.1); color: #f0883f; border: 1px solid rgba(210, 153, 34, 0.2); }
    .panel-badge.badge-blocked { background: rgba(218, 54, 51, 0.1); color: #ff7b72; border: 1px solid rgba(218, 54, 51, 0.2); }
    .panel-badge.badge-unavailable { background: rgba(139, 148, 158, 0.1); color: #8b949e; border: 1px solid rgba(139, 148, 158, 0.2); }
    .panel-meta { font-size: 11px; color: var(--muted); }
    .metric-list {
      list-style: none;
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .metric-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px dashed rgba(48, 54, 61, 0.5);
      padding-bottom: 4px;
    }
    .metric-item:last-child { border-bottom: none; }
    .metric-label { color: var(--muted); }
    .metric-value { font-weight: 600; }
    .metric-value.empty { color: var(--muted); font-weight: normal; }
    .metric-value.has-val { color: #f0f6fc; }
    .panel-blockers {
      background: rgba(218, 54, 51, 0.05);
      border: 1px solid rgba(218, 54, 51, 0.15);
      border-radius: 4px;
      padding: 6px;
      margin-top: 4px;
    }
    .panel-blocker-item {
      font-size: 11px;
      color: #ff7b72;
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .panel-blocker-item::before {
      content: "•";
      font-weight: bold;
    }

    /* Collapsible Raw telemetries */
    details {
      background: var(--panel);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 8px 12px;
    }
    summary {
      cursor: pointer;
      font-weight: 600;
      color: #f0f6fc;
      outline: none;
      user-select: none;
    }
    pre {
      margin: 8px 0 0 0;
      max-height: 180px;
      overflow: auto;
      background: #090c10;
      border-radius: 6px;
      padding: 8px;
      font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, Liberation Mono, monospace;
      font-size: 11px;
      color: #c9d1d9;
    }
  </style>
</head>
<body>
  <div class="stack">
    <div class="row">
      <div class="title-area">
        <span class="subtitle">Headroom Sidecar</span>
        <span class="main-title">Control Dashboard</span>
      </div>
      <button id="refreshBtn">Refresh</button>
    </div>

    <!-- Status Banner -->
    <div class="status-banner">
      <div class="status-indicator">
        <span id="statusLamp" class="lamp"></span>
        <span id="statusLabel" class="status-text">Checking</span>
      </div>
      <div id="latencyLabel" class="panel-meta">-- ms</div>
    </div>

    <div class="section-header">Runtime Status</div>
    <div class="alerts-section" id="risksContainer">
      <div class="no-alerts">Initializing...</div>
    </div>

    <div class="section-header">Endpoint Checks</div>
    <div class="panels-grid" id="endpointChecksContainer">
      <!-- /health and /stats status cards -->
    </div>

    <div class="section-header">Promotion Gate Checklist</div>
    <div class="gate-section" id="gateContainer">
      <!-- Promotion gate preview -->
    </div>

    <div class="section-header">Observability Panels</div>
    <div class="panels-grid" id="panelsContainer">
      <!-- Active status and future KPI previews -->
    </div>

    <div class="section-header">Raw Payloads</div>
    <details>
      <summary>Telemetry Context Details</summary>
      <div style="display:flex; flex-direction:column; gap:8px; margin-top:8px;">
        <div><span class="subtitle">Config Path:</span> <code id="configPath">--</code></div>
        <div><span class="subtitle">Endpoint:</span> <code id="endpoint">--</code></div>
        <div><span class="subtitle">Last Checked:</span> <span id="checkedAt">--</span></div>
      </div>

      <p class="subtitle" style="margin: 10px 0 4px 0;">/health payload</p>
      <pre id="healthPayload">--</pre>

      <p class="subtitle" style="margin: 10px 0 4px 0;">/stats payload</p>
      <pre id="statsPayload">--</pre>

      <p class="subtitle" style="margin: 10px 0 4px 0;">Connection Error Details</p>
      <pre id="errorPayload">--</pre>
    </details>
  </div>

  <script nonce="${nonce}">
    const vscode = acquireVsCodeApi();
    const refreshBtn = document.getElementById('refreshBtn');

    const statusLamp = document.getElementById('statusLamp');
    const statusLabel = document.getElementById('statusLabel');
    const latencyLabel = document.getElementById('latencyLabel');

    const configPath = document.getElementById('configPath');
    const endpoint = document.getElementById('endpoint');
    const checkedAt = document.getElementById('checkedAt');
    const healthPayload = document.getElementById('healthPayload');
    const statsPayload = document.getElementById('statsPayload');
    const errorPayload = document.getElementById('errorPayload');

    refreshBtn.addEventListener('click', () => {
      vscode.postMessage({ type: 'refresh' });
    });

    function escape(str) {
      if (!str) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
    }

    window.addEventListener('message', (event) => {
      const data = event.data;
      if (!data || data.type !== 'state') return;

      // Update basic details
      endpoint.textContent = data.endpointBase || '--';
      configPath.textContent = data.configPath || '--';
      checkedAt.textContent = data.checkedAt ? new Date(data.checkedAt).toLocaleString() : '--';
      latencyLabel.textContent = data.latencyMs != null ? data.latencyMs + ' ms' : '-- ms';

      // Update lamp
      const lvl = data.summary?.level || 'unknown';
      statusLamp.className = 'lamp ' + lvl;
      statusLabel.textContent = data.summary?.label || 'Checking';

      // Update debug payloads
      healthPayload.textContent = data.showRawPayload ? JSON.stringify(data.health, null, 2) : 'Hidden by config';
      statsPayload.textContent = data.showRawPayload ? JSON.stringify(data.stats, null, 2) : 'Hidden by config';
      errorPayload.textContent = data.error || data.statsError || 'None';

      // Risks Rendering
      const risksContainer = document.getElementById('risksContainer');
      risksContainer.innerHTML = '';
      const activeRisks = (data.risks || []).filter(r => r.triggered);
      if (activeRisks.length === 0) {
        risksContainer.innerHTML = '<div class="no-alerts">Active contract: /health + /stats only. No runtime alerts.</div>';
      } else {
        activeRisks.forEach(risk => {
          const card = document.createElement('div');
          card.className = 'risk-card severity-' + risk.severity;
          card.innerHTML = \`
            <div class="risk-header">
              <span class="risk-title">\${escape(risk.title)}</span>
              <span class="risk-badge badge-\${risk.severity}">\${risk.severity.toUpperCase()}</span>
            </div>
            <div class="risk-body">
              <p>\${risk.reasons.map(escape).join('<br/>')}</p>
              \${risk.fallbackHint ? \`<div class="risk-fallback"><strong>Fallback:</strong> \${escape(risk.fallbackHint)}</div>\` : ''}
            </div>
          \`;
          risksContainer.appendChild(card);
        });
      }

      // Endpoint checks rendering. This is the full active dashboard surface
      // until observability KPI panels are formally promoted.
      const endpointChecksContainer = document.getElementById('endpointChecksContainer');
      endpointChecksContainer.innerHTML = '';
      [
        {
          title: '/health',
          state: data.error ? 'blocked' : (Number(data.healthHttpStatus) >= 200 && Number(data.healthHttpStatus) < 300 ? 'active' : 'unavailable'),
          status: data.healthHttpStatus ?? '--',
          detail: data.error || 'Headroom sidecar reachability'
        },
        {
          title: '/stats',
          state: data.statsError ? 'blocked' : (Number(data.statsHttpStatus) >= 200 && Number(data.statsHttpStatus) < 300 ? 'active' : 'unavailable'),
          status: data.statsHttpStatus ?? '--',
          detail: data.statsError || 'Headroom sidecar stats'
        }
      ].forEach(check => {
        const card = document.createElement('div');
        card.className = 'panel-card state-' + check.state;
        card.innerHTML = \`
          <div class="panel-header">
            <span class="panel-title">\${escape(check.title)}</span>
            <span class="panel-badge badge-\${check.state}">\${escape(String(check.status))}</span>
          </div>
          <div class="panel-body">
            <div class="panel-meta">\${escape(check.detail)}</div>
          </div>
        \`;
        endpointChecksContainer.appendChild(card);
      });

      // Promotion gate rendering. Missing future KPI promotion is shown as a
      // neutral preview state, not as an operational blocker.
      const gateContainer = document.getElementById('gateContainer');
      gateContainer.innerHTML = '';
      if (data.gate) {
        const gate = data.gate;
        const gateCard = document.createElement('div');
        gateCard.className = 'gate-card state-' + gate.state;

        let blockersList = '';
        if (gate.blockers && gate.blockers.length > 0) {
          blockersList = '<div class="gate-section-title">Runtime blockers:</div><ul>' +
            gate.blockers.map(b => \`<li class="gate-blocker">\${escape(b)}</li>\`).join('') + '</ul>';
        }

        let warningsList = '';
        if (gate.warnings && gate.warnings.length > 0) {
          warningsList = '<div class="gate-section-title">Warnings:</div><ul>' +
            gate.warnings.map(w => \`<li class="gate-warning">\${escape(w)}</li>\`).join('') + '</ul>';
        }

        const statusNote = !blockersList && !warningsList
          ? '<div class="no-alerts">Future KPI panels are visible as previews. Active Headroom status remains /health + /stats.</div>'
          : '';

        let evidenceList = '<div class="gate-section-title">Evidence Checklist:</div><div class="evidence-grid">';
        if (gate.evidence) {
          gate.evidence.forEach(ev => {
            const isOk = ev.value !== false && ev.value !== null && ev.value !== 0 && ev.value !== 'blocked';
            evidenceList += \`
              <div class="evidence-item \${isOk ? 'ok' : 'fail'}">
                <span class="evidence-dot"></span>
                <span class="evidence-key">\${escape(ev.key)}:</span>
                <span class="evidence-val">\${escape(String(ev.value))}</span>
              </div>
            \`;
          });
        }
        evidenceList += '</div>';

        gateCard.innerHTML = \`
          <div class="gate-header">
            <span class="gate-title">Promotion Gate Checklist</span>
            <span class="gate-badge badge-\${gate.state}">\${gate.state.toUpperCase()}</span>
          </div>
          <div class="gate-body">
            \${statusNote}
            \${blockersList}
            \${warningsList}
            \${evidenceList}
          </div>
        \`;
        gateContainer.appendChild(gateCard);
      }

      // Observability panels rendering. Prototype KPI panels are retained as
      // interface previews with explicit source labels.
      const panelsContainer = document.getElementById('panelsContainer');
      panelsContainer.innerHTML = '';
      if (data.panels) {
        data.panels.forEach(panel => {
          const card = document.createElement('div');
          card.className = 'panel-card state-' + panel.renderState;

          let metricsList = '<ul class="metric-list">';
          panel.metrics.forEach(m => {
            const hasVal = m.value !== null && m.value !== undefined;
            const valStr = hasVal ? m.value + (m.unit ? ' ' + m.unit : '') : 'n/a';
            metricsList += \`
              <li class="metric-item">
                <span class="metric-label">\${escape(m.label)}</span>
                <span class="metric-value \${hasVal ? 'has-val' : 'empty'}">\${escape(String(valStr))}</span>
              </li>
            \`;
          });
          metricsList += '</ul>';

          let blockersHtml = '';
          if (panel.blockers && panel.blockers.length > 0) {
            blockersHtml = '<div class="panel-blockers">' + panel.blockers.map(b => \`<div class="panel-blocker-item">\${escape(b)}</div>\`).join('') + '</div>';
          }

          card.innerHTML = \`
            <div class="panel-header">
              <span class="panel-title">\${escape(panel.title)}</span>
              <span class="panel-badge badge-\${panel.renderState}">\${panel.renderState.toUpperCase()}</span>
            </div>
            <div class="panel-body">
              <div class="panel-meta">Source: <code>\${escape(panel.sourceLabel)}</code></div>
              \${metricsList}
              \${blockersHtml}
            </div>
          \`;
          panelsContainer.appendChild(card);
        });
      }
    });
  </script>
</body>
</html>`;
  }
}
