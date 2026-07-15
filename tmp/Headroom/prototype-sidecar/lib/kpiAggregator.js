function percentile95(values) {
  if (values.length === 0) return null;
  const sorted = [...values].sort((a, b) => a - b);
  return sorted[Math.ceil(sorted.length * 0.95) - 1];
}

function aggregateMetricSet(events) {
  let retrievalQueriesTotal = 0;
  let retrievalHits = 0;
  let inputTokensTotal = 0;
  let outputTokensTotal = 0;
  let fallbackEvents = 0;
  let fallbackActive = 0;
  const latencies = [];

  for (const event of events) {
    const kpi = event.kpi || {};
    if (kpi.retrieval) {
      retrievalQueriesTotal += 1;
      if (kpi.retrieval.retrieval_hit === true) retrievalHits += 1;
    }
    if (kpi.compression) {
      inputTokensTotal += kpi.compression.input_tokens_estimated || 0;
      outputTokensTotal += kpi.compression.output_tokens_estimated || 0;
    }
    if (kpi.fallback) {
      fallbackEvents += 1;
      if (kpi.fallback.fallback_active === true) fallbackActive += 1;
    }
    if (Number.isFinite(kpi.latency?.end_to_end_latency_ms)) latencies.push(kpi.latency.end_to_end_latency_ms);
  }

  return {
    retrieval_queries_total: retrievalQueriesTotal,
    retrieval_hit_rate: retrievalQueriesTotal > 0 ? retrievalHits / retrievalQueriesTotal : null,
    raw_input_tokens_est_total: inputTokensTotal,
    compressed_input_tokens_est_total: outputTokensTotal,
    token_keep_ratio: inputTokensTotal > 0 ? outputTokensTotal / inputTokensTotal : null,
    token_savings_rate: inputTokensTotal > 0 ? (inputTokensTotal - outputTokensTotal) / inputTokensTotal : null,
    fallback_rate: fallbackEvents > 0 ? fallbackActive / fallbackEvents : null,
    end_to_end_p95_latency_ms: percentile95(latencies)
  };
}

function aggregateKPIEvents(events) {
  const safeEvents = Array.isArray(events) ? events.filter((event) => event && typeof event === 'object') : [];
  const realEvents = safeEvents.filter((event) => event.source_kind === 'real');
  const fixtureEvents = safeEvents.filter((event) => event.source_kind === 'fixture');
  const allMetrics = aggregateMetricSet(safeEvents);

  return {
    event_count: safeEvents.length,
    real_event_count: realEvents.length,
    fixture_event_count: fixtureEvents.length,
    promotion_real_event_count: realEvents.length,
    promoted: false,
    ...allMetrics,
    all_metrics: allMetrics,
    real_metrics: aggregateMetricSet(realEvents),
    fixture_metrics: aggregateMetricSet(fixtureEvents)
  };
}

module.exports = { aggregateKPIEvents, aggregateMetricSet };
