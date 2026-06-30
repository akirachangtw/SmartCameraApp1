import type { DashboardSnapshot, DataSourceStatus, HealthState, MetricValue, PilotMode, FallbackLevel, SidecarSummary } from '../types/dashboard.types';
import type { PanelViewModel } from '../types/panel.types';

export interface RawDashboardInput {
  endpoint?: string | null;
  configPath?: string | null;
  httpStatus?: number | null;
  activeRepo?: string | null;
  activeAgentEntry?: string | null;
  currentMode?: PilotMode;
  healthState?: HealthState;
  fallbackLevel?: FallbackLevel;
  metrics?: Record<string, MetricValue>;
  dataSource?: DataSourceStatus;
  lastUpdatedAt?: string | null;
}

export function mapToDashboardSnapshot(input: RawDashboardInput): DashboardSnapshot {
  return {
    activeRepo: input.activeRepo ?? null,
    activeAgentEntry: input.activeAgentEntry ?? null,
    currentMode: input.currentMode ?? 'pilot',
    healthState: input.healthState ?? 'unknown',
    fallbackLevel: input.fallbackLevel ?? 'none',
    endpoint: input.endpoint ?? null,
    configPath: input.configPath ?? null,
    httpStatus: input.httpStatus ?? null,
    lastUpdatedAt: input.lastUpdatedAt ?? null,
    metrics: input.metrics ?? {}
  };
}

export function buildDefaultPanels(snapshot: DashboardSnapshot, source: DataSourceStatus): PanelViewModel[] {
  const makeMetric = (key: string, fallbackLabel: string): MetricValue => {
    return snapshot.metrics[key] ?? { label: fallbackLabel, value: null, source: source.contract, state: 'unknown' };
  };

  return [
    {
      id: 'session',
      title: 'Panel A · Session',
      renderState: 'active',
      promotionState: 'active',
      sourceLabel: source.contract,
      metrics: [
        makeMetric('activeRepo', 'Active Repo'),
        makeMetric('activeAgentEntry', 'Active Agent'),
        makeMetric('currentMode', 'Mode')
      ]
    },
    {
      id: 'retrieval',
      title: 'Panel B · Retrieval',
      renderState: source.promoted ? 'active' : 'prototype',
      promotionState: source.promoted ? 'active' : 'prototype',
      sourceLabel: source.contract,
      metrics: [makeMetric('retrievalHitRate', 'Retrieval Hit Rate')],
      blockers: source.promoted ? [] : ['Retrieval metrics not yet promoted']
    },
    {
      id: 'compression',
      title: 'Panel C · Compression',
      renderState: source.promoted ? 'active' : 'prototype',
      promotionState: source.promoted ? 'active' : 'prototype',
      sourceLabel: source.contract,
      metrics: [makeMetric('compressionRatioAvg', 'Compression Ratio Avg')],
      blockers: source.promoted ? [] : ['Compression schema not yet promoted']
    },
    {
      id: 'latency',
      title: 'Panel D · Latency',
      renderState: 'active',
      promotionState: 'active',
      sourceLabel: source.contract,
      metrics: [makeMetric('endToEndLatencyP95', 'End-to-End Latency P95')]
    },
    {
      id: 'quality',
      title: 'Panel E · Quality',
      renderState: source.available ? 'active' : 'unavailable',
      promotionState: source.promoted ? 'active' : 'prototype',
      sourceLabel: source.contract,
      metrics: [makeMetric('qualityScoreAvg', 'Quality Score Avg')]
    },
    {
      id: 'governance',
      title: 'Panel F · Governance',
      renderState: 'active',
      promotionState: source.promoted ? 'active' : 'blocked',
      sourceLabel: source.contract,
      metrics: [makeMetric('promotionState', 'Promotion State')],
      blockers: source.promoted ? [] : [source.reason ?? 'Promotion gate not passed']
    }
  ];
}

export function mapSummaryToPanels(
  healthOk: boolean,
  healthHttpStatus: number | null,
  healthError: string | null,
  statsOk: boolean,
  statsHttpStatus: number | null,
  statsError: string | null,
  summary: SidecarSummary | null,
  summaryError: string | null,
  latencyMs: number,
  gateState: string,
  codegraphEnabled?: boolean,
  codegraphOk?: boolean,
  codegraphError?: string | null
): PanelViewModel[] {
  const activeSource = 'activeHeadroomContract';
  const previewSource = summary ? 'futurePromotedContract' : 'jsonlPilotContract';

  const retrievalMetrics: MetricValue[] = [
    { label: 'Retrieval Hit Rate', value: summary && summary.retrieval_hit_rate !== null ? `${(summary.retrieval_hit_rate * 100).toFixed(1)}%` : 'Unavailable', source: previewSource },
    { label: 'Zero Result Count', value: summary ? summary.retrieval_zero_result_count : 'Unavailable', source: previewSource },
    { label: 'Manual Override Count', value: summary ? summary.manual_override_count : 'Unavailable', source: previewSource },
    { label: 'Total Queries', value: summary ? summary.retrieval_queries_total : 'Unavailable', source: previewSource }
  ];

  const retrievalBlockers = summaryError ? [summaryError] : [];

  if (codegraphEnabled) {
    retrievalMetrics.push({
      label: 'CodeGraph Integration',
      value: codegraphOk ? 'Active (Healthy)' : 'Degraded (Bypass)',
      source: previewSource,
      state: codegraphOk ? 'ok' : 'warn'
    });
    if (!codegraphOk) {
      retrievalBlockers.push(codegraphError || 'CodeGraph unreachable - using ripgrep fallback');
    }
  }

  return [
    {
      id: 'headroom-health',
      title: 'Headroom /health',
      renderState: healthOk ? 'active' : 'unavailable',
      promotionState: 'active',
      sourceLabel: activeSource,
      metrics: [
        { label: 'HTTP Status', value: healthHttpStatus ?? null, source: activeSource },
        { label: 'State', value: healthOk ? 'Healthy' : 'Unavailable', source: activeSource },
        { label: 'Latency', value: latencyMs, unit: 'ms', source: activeSource }
      ],
      blockers: healthError ? [healthError] : []
    },
    {
      id: 'headroom-stats',
      title: 'Headroom /stats',
      renderState: statsOk ? 'active' : 'unavailable',
      promotionState: 'active',
      sourceLabel: activeSource,
      metrics: [
        { label: 'HTTP Status', value: statsHttpStatus ?? null, source: activeSource },
        { label: 'State', value: statsOk ? 'Stats OK' : 'Stats unavailable', source: activeSource }
      ],
      blockers: statsError ? [statsError] : []
    },
    {
      id: 'retrieval',
      title: 'KPI Preview · Retrieval',
      renderState: summary ? 'active' : 'prototype',
      promotionState: 'prototype',
      sourceLabel: previewSource,
      metrics: retrievalMetrics,
      blockers: retrievalBlockers
    },
    {
      id: 'compression',
      title: 'KPI Preview · Compression',
      renderState: summary ? 'active' : 'prototype',
      promotionState: 'prototype',
      sourceLabel: previewSource,
      metrics: [
        { label: 'Compression Ratio', value: summary && summary.compression_ratio_avg !== null ? `${(summary.compression_ratio_avg * 100).toFixed(1)}%` : 'Unavailable', source: previewSource },
        { label: 'Token Saved Estimate', value: summary ? `${summary.raw_input_tokens_est_total - summary.compressed_input_tokens_est_total} tokens` : 'Unavailable', source: previewSource },
        { label: 'Fallback Count', value: summary ? summary.compression_fallback_count : 'Unavailable', source: previewSource }
      ],
      blockers: summaryError ? [summaryError] : []
    },
    {
      id: 'quality',
      title: 'KPI Preview · Quality',
      renderState: summary ? 'active' : 'prototype',
      promotionState: 'prototype',
      sourceLabel: previewSource,
      metrics: [
        { label: 'Quality Score', value: summary && summary.quality_score_avg_1_5 !== null ? `${summary.quality_score_avg_1_5.toFixed(2)}/5` : 'Unavailable', source: previewSource },
        { label: 'Manual Retry Count', value: summary ? summary.manual_retry_count : 'Unavailable', source: previewSource },
        { label: 'Follow-up Count Avg', value: summary && summary.followup_count_avg !== null ? summary.followup_count_avg.toFixed(1) : 'Unavailable', source: previewSource }
      ],
      blockers: summaryError ? [summaryError] : []
    },
    {
      id: 'governance',
      title: 'Governance · Active Contract',
      renderState: 'active',
      promotionState: 'active',
      sourceLabel: activeSource,
      metrics: [
        { label: 'Active Endpoints', value: '/health + /stats', source: activeSource },
        { label: 'Future KPI', value: `Preview (${gateState})`, source: previewSource }
      ],
      blockers: []
    }
  ];
}

