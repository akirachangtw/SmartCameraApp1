import * as fs from 'fs';

export interface PilotConfig {
  mode?: 'native' | 'pilot' | 'degraded' | 'bypass';
  dashboardRefreshMs?: number;
  sidecarBaseUrl?: string;
  headroom?: {
    proxy_url?: string;
    health_path?: string;
    stats_path?: string;
    dashboard?: {
      poll_interval_ms?: number;
      request_timeout_ms?: number;
      show_raw_payload?: boolean;
    };
  };
  pilot_flags?: {
    enable_headroom_dashboard?: boolean;
  };
}

export function loadPilotConfig(configPath: string | undefined): PilotConfig {
  if (!configPath || !fs.existsSync(configPath)) return {};
  const raw = fs.readFileSync(configPath, 'utf-8');
  return JSON.parse(raw) as PilotConfig;
}

export function resolveHeadroomEndpoints(config: PilotConfig): { proxyUrl: string | null; healthPath: string; statsPath: string } {
  return {
    proxyUrl: config.headroom?.proxy_url ?? config.sidecarBaseUrl ?? null,
    healthPath: config.headroom?.health_path ?? '/health',
    statsPath: config.headroom?.stats_path ?? '/stats'
  };
}
