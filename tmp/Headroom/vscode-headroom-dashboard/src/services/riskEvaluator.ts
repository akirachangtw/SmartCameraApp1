import type { DashboardSnapshot, DataSourceStatus } from '../types/dashboard.types';
import type { RiskSignal } from '../types/risk.types';

export function evaluateRiskSignals(snapshot: DashboardSnapshot, source: DataSourceStatus): RiskSignal[] {
  const retrievalHitRate = Number(snapshot.metrics.retrievalHitRate?.value ?? 0);
  const compressionRatio = Number(snapshot.metrics.compressionRatioAvg?.value ?? 0);
  const qualityScore = Number(snapshot.metrics.qualityScoreAvg?.value ?? 0);

  return [
    {
      id: 'retrieval-drift',
      title: 'Retrieval Drift',
      severity: retrievalHitRate > 0 && retrievalHitRate < 0.5 ? 'warn' : 'info',
      triggered: retrievalHitRate > 0 && retrievalHitRate < 0.5,
      reasons: retrievalHitRate > 0 && retrievalHitRate < 0.5 ? ['Retrieval hit rate below expected threshold'] : [],
      affectedPanels: ['retrieval', 'governance'],
      fallbackHint: 'Hold CodeGraph-heavy flow or fall back to native search.',
      evidence: [{ key: 'retrievalHitRate', value: snapshot.metrics.retrievalHitRate?.value ?? null }]
    },
    {
      id: 'compression-harm',
      title: 'Compression Harm',
      severity: compressionRatio > 0 && compressionRatio < 0.35 && qualityScore > 0 && qualityScore < 3 ? 'critical' : 'info',
      triggered: compressionRatio > 0 && compressionRatio < 0.35 && qualityScore > 0 && qualityScore < 3,
      reasons: compressionRatio > 0 && compressionRatio < 0.35 && qualityScore > 0 && qualityScore < 3 ? ['Compression is aggressive while quality score is degraded'] : [],
      affectedPanels: ['compression', 'quality'],
      fallbackHint: 'Reduce compression or switch to F1/F2 fallback.',
      evidence: [
        { key: 'compressionRatioAvg', value: snapshot.metrics.compressionRatioAvg?.value ?? null },
        { key: 'qualityScoreAvg', value: snapshot.metrics.qualityScoreAvg?.value ?? null }
      ]
    },
    {
      id: 'observability-gap',
      title: 'Observability Gap',
      severity: !source.promoted ? 'warn' : 'info',
      triggered: !source.promoted,
      reasons: !source.promoted ? ['Metrics source is not yet promoted to active contract'] : [],
      affectedPanels: ['governance', 'retrieval', 'compression'],
      fallbackHint: 'Keep dashboard in prototype/health-only mode until promotion gate passes.',
      evidence: [{ key: 'contract', value: source.contract }, { key: 'promoted', value: source.promoted }]
    }
  ];
}
