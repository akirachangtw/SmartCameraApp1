"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.mapToDashboardSnapshot = mapToDashboardSnapshot;
exports.buildDefaultPanels = buildDefaultPanels;
exports.mapSummaryToPanels = mapSummaryToPanels;
function mapToDashboardSnapshot(input) {
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
function buildDefaultPanels(snapshot, source) {
    const metric = (key, label) => snapshot.metrics[key] ?? { label, value: null, source: source.contract, state: 'unknown' };
    const preview = source.promoted ? 'active' : 'prototype';
    return [
        { id: 'session', title: 'Panel A · Session', renderState: 'active', promotionState: 'active', sourceLabel: source.contract, metrics: [metric('activeRepo', 'Active Repo'), metric('activeAgentEntry', 'Active Agent'), metric('currentMode', 'Mode')] },
        { id: 'retrieval', title: 'Panel B · Retrieval', renderState: preview, promotionState: source.promoted ? 'active' : 'prototype', sourceLabel: source.contract, metrics: [metric('retrievalHitRate', 'Retrieval Hit Rate')] },
        { id: 'compression', title: 'Panel C · Compression', renderState: preview, promotionState: source.promoted ? 'active' : 'prototype', sourceLabel: source.contract, metrics: [metric('tokenSavingsRate', 'Token Savings Rate')] },
        { id: 'latency', title: 'Panel D · Latency', renderState: preview, promotionState: source.promoted ? 'active' : 'prototype', sourceLabel: source.contract, metrics: [metric('endToEndLatencyP95', 'End-to-End Latency P95')] },
        { id: 'quality', title: 'Panel E · Quality', renderState: preview, promotionState: source.promoted ? 'active' : 'prototype', sourceLabel: source.contract, metrics: [metric('qualityScoreAvg', 'Quality Score Avg')] },
        { id: 'governance', title: 'Panel F · Governance', renderState: 'active', promotionState: 'active', sourceLabel: 'activeHeadroomContract', metrics: [metric('promotionState', 'Promotion State')] }
    ];
}
function mapSummaryToPanels(healthOk, healthHttpStatus, healthError, statsOk, statsHttpStatus, statsError, summary, summaryError, latencyMs, gateState, codegraphEnabled, codegraphOk, codegraphError) {
    const activeSource = 'activeHeadroomContract';
    const previewSource = summary ? 'jsonlPilotContract' : 'futurePromotedContract';
    const promoted = summary?.source_status === 'promoted' && summary.promoted === true;
    const futureState = promoted ? 'active' : summary ? 'prototype' : 'unavailable';
    const futurePromotion = promoted ? 'active' : 'prototype';
    const unavailable = summaryError ? 'Unavailable' : null;
    const retrievalMetrics = [
        { label: 'Retrieval Hit Rate', value: summary?.retrieval_hit_rate != null ? `${(summary.retrieval_hit_rate * 100).toFixed(1)}%` : unavailable, source: previewSource },
        { label: 'Total Queries', value: summary?.retrieval_queries_total ?? unavailable, source: previewSource },
        { label: 'Real / Fixture Events', value: summary ? `${summary.real_event_count} / ${summary.fixture_event_count}` : unavailable, source: previewSource }
    ];
    if (codegraphEnabled)
        retrievalMetrics.push({ label: 'CodeGraph Integration', value: codegraphOk ? 'Healthy' : 'Bypass', source: previewSource, state: codegraphOk ? 'ok' : 'warn' });
    return [
        {
            id: 'headroom-health', title: 'Headroom /health', renderState: healthOk ? 'active' : 'unavailable', promotionState: 'active', sourceLabel: activeSource,
            metrics: [{ label: 'HTTP Status', value: healthHttpStatus, source: activeSource }, { label: 'State', value: healthOk ? 'Healthy' : 'Unavailable', source: activeSource }, { label: 'Latency', value: latencyMs, unit: 'ms', source: activeSource }], blockers: healthError ? [healthError] : []
        },
        {
            id: 'headroom-stats', title: 'Headroom /stats', renderState: statsOk ? 'active' : 'unavailable', promotionState: 'active', sourceLabel: activeSource,
            metrics: [{ label: 'HTTP Status', value: statsHttpStatus, source: activeSource }, { label: 'State', value: statsOk ? 'Stats OK' : 'Stats unavailable', source: activeSource }], blockers: statsError ? [statsError] : []
        },
        { id: 'retrieval', title: 'KPI Preview · Retrieval', renderState: futureState, promotionState: futurePromotion, sourceLabel: previewSource, metrics: retrievalMetrics },
        {
            id: 'compression', title: 'KPI Preview · Compression', renderState: futureState, promotionState: futurePromotion, sourceLabel: previewSource,
            metrics: [{ label: 'Token Keep Ratio', value: summary?.token_keep_ratio != null ? `${(summary.token_keep_ratio * 100).toFixed(1)}%` : unavailable, source: previewSource }, { label: 'Token Savings Rate', value: summary?.token_savings_rate != null ? `${(summary.token_savings_rate * 100).toFixed(1)}%` : unavailable, source: previewSource }, { label: 'Token Saved Estimate', value: summary ? summary.raw_input_tokens_est_total - summary.compressed_input_tokens_est_total : unavailable, unit: 'tokens', source: previewSource }]
        },
        {
            id: 'quality', title: 'KPI Preview · Quality / Latency', renderState: futureState, promotionState: futurePromotion, sourceLabel: previewSource,
            metrics: [{ label: 'P95 Latency', value: summary?.end_to_end_p95_latency_ms ?? unavailable, unit: 'ms', source: previewSource }, { label: 'Fallback Rate', value: summary?.fallback_rate != null ? `${(summary.fallback_rate * 100).toFixed(1)}%` : unavailable, source: previewSource }, { label: 'Evidence', value: summary?.evidence_status ?? unavailable, source: previewSource }]
        },
        {
            id: 'governance', title: 'Governance · Active Contract', renderState: 'active', promotionState: 'active', sourceLabel: activeSource,
            metrics: [{ label: 'Active Endpoints', value: '/health + /stats', source: activeSource }, { label: 'Future KPI', value: `Preview (${gateState})`, source: previewSource }, { label: 'Supervisor Error', value: summaryError ?? codegraphError ?? 'None', source: previewSource }]
        }
    ];
}
//# sourceMappingURL=dashboardMapper.js.map