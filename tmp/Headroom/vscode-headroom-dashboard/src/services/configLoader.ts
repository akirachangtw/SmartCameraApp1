import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';

export interface PilotConfig {
  version?: number;
  mode?: 'native' | 'pilot' | 'degraded' | 'bypass';
  dashboardRefreshMs?: number;
  sidecarBaseUrl?: string;
  headroom?: {
    proxy_url?: string;
    health_path?: string;
    stats_path?: string;
    promotion_gate?: { has_schema_note?: boolean; has_fallback_mapping?: boolean; readme_aligned?: boolean };
    dashboard?: { poll_interval_ms?: number; request_timeout_ms?: number; show_raw_payload?: boolean };
  };
  supervisor?: {
    enabled?: boolean;
    base_url?: string;
    health_path?: string;
    ingest_path?: string;
    summary_path?: string;
    recent_path?: string;
    source_status?: 'prototype' | 'promoted';
    promoted?: boolean;
  };
  codegraph?: { enabled?: boolean; health_url?: string };
  pilot_flags?: { enable_headroom_dashboard?: boolean; enable_codegraph_health?: boolean };
}

export interface HeadroomEndpoints { proxyUrl: string | null; healthPath: string; statsPath: string }
export interface SupervisorEndpoints {
  enabled: boolean;
  baseUrl: string | null;
  healthPath: string;
  ingestPath: string;
  summaryPath: string;
  recentPath: string;
  sourceStatus: 'prototype' | 'promoted';
  promoted: boolean;
}

export function loadPilotConfig(configPath: string | undefined): PilotConfig {
  if (!configPath || !fs.existsSync(configPath)) return {};
  return JSON.parse(fs.readFileSync(configPath, 'utf-8')) as PilotConfig;
}

export function resolveHeadroomEndpoints(config: PilotConfig): HeadroomEndpoints {
  return {
    proxyUrl: config.headroom?.proxy_url ?? config.sidecarBaseUrl ?? null,
    healthPath: config.headroom?.health_path ?? '/health',
    statsPath: config.headroom?.stats_path ?? '/stats'
  };
}

export function resolveSupervisorEndpoints(config: PilotConfig): SupervisorEndpoints {
  return {
    enabled: config.supervisor?.enabled ?? false,
    baseUrl: config.supervisor?.base_url ?? null,
    healthPath: config.supervisor?.health_path ?? '/health',
    ingestPath: config.supervisor?.ingest_path ?? '/events/ingest',
    summaryPath: config.supervisor?.summary_path ?? '/summary',
    recentPath: config.supervisor?.recent_path ?? '/events/recent',
    sourceStatus: config.supervisor?.source_status ?? 'prototype',
    promoted: config.supervisor?.promoted ?? false
  };
}

export function joinEndpoint(baseUrl: string, endpointPath: string): string {
  return `${baseUrl.replace(/\/$/, '')}/${endpointPath.replace(/^\//, '')}`;
}

export function runConfigLoaderSelfTest(): void {
  const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'headroom-config-'));
  const configPath = path.join(tempDir, 'pilot.config.json');
  try {
    fs.writeFileSync(configPath, JSON.stringify({ version: 2, supervisor: { enabled: true, base_url: 'http://127.0.0.1:18788' } }));
    const first = resolveSupervisorEndpoints(loadPilotConfig(configPath));
    fs.writeFileSync(configPath, JSON.stringify({ version: 2, supervisor: { enabled: true, base_url: 'http://127.0.0.1:28788' } }));
    const second = resolveSupervisorEndpoints(loadPilotConfig(configPath));
    if (first.baseUrl === second.baseUrl || second.baseUrl !== 'http://127.0.0.1:28788') throw new Error('config hot reload failed');
    const isolated = resolveSupervisorEndpoints({ codegraph: { health_url: 'http://127.0.0.1:9999/health' } });
    if (isolated.baseUrl !== null) throw new Error('supervisor URL must not derive from codegraph.health_url');
  } finally {
    fs.rmSync(tempDir, { recursive: true, force: true });
  }
}
