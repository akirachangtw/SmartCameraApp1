export type PilotMode = 'native' | 'pilot' | 'degraded' | 'bypass';
export type HealthState = 'healthy' | 'degraded' | 'offline' | 'unknown';
export type CompressionFallbackLevel = 'none' | 'f1' | 'f2' | 'f3';
export type RollbackLevel = 'r0' | 'r1' | 'r2' | 'r3';
export type FallbackLevel = CompressionFallbackLevel;
export type PromotionState = 'active' | 'prototype' | 'blocked' | 'future';

export interface MetricValue {
  label: string;
  value: string | number | null;
  unit?: string;
  source?: string;
  state?: 'ok' | 'warn' | 'error' | 'unknown';
}

export interface DashboardSnapshot {
  activeRepo: string | null;
  activeAgentEntry: string | null;
  currentMode: PilotMode;
  healthState: HealthState;
  fallbackLevel: CompressionFallbackLevel;
  endpoint: string | null;
  configPath: string | null;
  httpStatus: number | null;
  lastUpdatedAt: string | null;
  metrics: Record<string, MetricValue>;
}

export interface DataSourceStatus {
  contract: 'activeHeadroomContract' | 'jsonlPilotContract' | 'futurePromotedContract';
  available: boolean;
  promoted: boolean;
  reason?: string;
}

export interface SidecarSummary {
  source_status: 'prototype' | 'promoted';
  retrieval_queries_total: number;
  retrieval_hit_rate: number | null;
  retrieval_zero_result_count: number;
  manual_override_count: number;
  raw_input_tokens_est_total: number;
  compressed_input_tokens_est_total: number;
  compression_ratio_avg: number | null;
  compression_fallback_count: number;
  quality_score_avg_1_5: number | null;
  followup_count_avg: number | null;
  manual_retry_count: number;
  end_to_end_p95_latency_ms: number | null;
  fallback_rate: number | null;
}

