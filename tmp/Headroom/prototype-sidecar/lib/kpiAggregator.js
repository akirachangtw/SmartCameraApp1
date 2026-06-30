function aggregateKPIEvents(events) {
  if (!Array.isArray(events)) {
    events = [];
  }

  // 1. Retrieval
  let retrievalQueriesTotal = 0;
  let retrievalHits = 0;
  let retrievalZeroResultCount = 0;
  let manualOverrideCount = 0;

  // 2. Compression
  let rawInputTokensEstTotal = 0;
  let compressedInputTokensEstTotal = 0;
  let compressionRatioSum = 0;
  let compressionRatioCount = 0;
  let compressionFallbackCount = 0;

  // 3. Quality
  let qualityScoreSum = 0;
  let qualityScoreCount = 0;
  let followupCountSum = 0;
  let followupCountCount = 0;
  let manualRetryCount = 0;

  // 4. Latency & Fallback
  const latencies = [];
  let fallbackRateEvents = 0;
  let fallbackActiveCount = 0;

  events.forEach(e => {
    if (!e || typeof e !== 'object') return;

    // Check event type
    const type = e.event_type;
    const kpi = e.kpi || {};

    // Latency extraction
    if (kpi.latency && typeof kpi.latency.end_to_end_latency_ms === 'number') {
      latencies.push(kpi.latency.end_to_end_latency_ms);
    }

    // Fallback extraction
    if (type === 'fallback_rate_snapshot_emitted' || kpi.fallback) {
      if (kpi.fallback) {
        fallbackRateEvents++;
        if (kpi.fallback.fallback_active === true) {
          fallbackActiveCount++;
        }
      }
    }

    // Retrieval
    if (type === 'retrieval_query_completed' || type === 'retrieval_query_failed' || kpi.retrieval) {
      if (kpi.retrieval) {
        retrievalQueriesTotal++;
        if (kpi.retrieval.retrieval_hit === true) {
          retrievalHits++;
        }
        if (kpi.retrieval.retrieved_items === 0) {
          retrievalZeroResultCount++;
        }
        if (kpi.retrieval.manual_override === true || typeof kpi.retrieval.manual_override_count === 'number') {
          manualOverrideCount += kpi.retrieval.manual_override_count || 1;
        }
      }
    }

    // Compression
    if (type === 'compression_completed' || type === 'compression_failed' || kpi.compression) {
      if (kpi.compression) {
        if (typeof kpi.compression.input_tokens_estimated === 'number') {
          rawInputTokensEstTotal += kpi.compression.input_tokens_estimated;
        }
        if (typeof kpi.compression.output_tokens_estimated === 'number') {
          compressedInputTokensEstTotal += kpi.compression.output_tokens_estimated;
        }
        if (typeof kpi.compression.compression_ratio === 'number') {
          compressionRatioSum += kpi.compression.compression_ratio;
          compressionRatioCount++;
        }
        if (kpi.compression.compression_fallback_level && kpi.compression.compression_fallback_level !== 'none') {
          compressionFallbackCount++;
        }
      }
    }

    // Quality
    if (kpi.quality || kpi.quality_proxy) {
      const q = kpi.quality || kpi.quality_proxy || {};
      if (typeof q.quality_score === 'number') {
        qualityScoreSum += q.quality_score;
        qualityScoreCount++;
      } else if (typeof q.quality_score_avg_1_5 === 'number') {
        qualityScoreSum += q.quality_score_avg_1_5;
        qualityScoreCount++;
      }

      if (typeof q.followup_count === 'number') {
        followupCountSum += q.followup_count;
        followupCountCount++;
      } else if (typeof q.followup_count_avg === 'number') {
        followupCountSum += q.followup_count_avg;
        followupCountCount++;
      }

      if (typeof q.manual_retry_count === 'number') {
        manualRetryCount += q.manual_retry_count;
      }
    }
  });

  // Calculate averages & percentiles
  const retrievalHitRate = retrievalQueriesTotal > 0 ? (retrievalHits / retrievalQueriesTotal) : null;
  
  // Calculate average compression ratio from sum or overall formula
  let compressionRatioAvg = null;
  if (compressionRatioCount > 0) {
    compressionRatioAvg = compressionRatioSum / compressionRatioCount;
  } else if (rawInputTokensEstTotal > 0) {
    compressionRatioAvg = (rawInputTokensEstTotal - compressedInputTokensEstTotal) / rawInputTokensEstTotal;
  }

  const qualityScoreAvg = qualityScoreCount > 0 ? (qualityScoreSum / qualityScoreCount) : null;
  const followupCountAvg = followupCountCount > 0 ? (followupCountSum / followupCountCount) : null;
  const fallbackRate = fallbackRateEvents > 0 ? (fallbackActiveCount / fallbackRateEvents) : null;

  // Calculate p95 latency
  let latencyP95 = null;
  if (latencies.length > 0) {
    latencies.sort((a, b) => a - b);
    const idx = Math.ceil(0.95 * latencies.length) - 1;
    latencyP95 = latencies[idx];
  }

  return {
    retrieval_queries_total: retrievalQueriesTotal,
    retrieval_hit_rate: retrievalHitRate,
    retrieval_zero_result_count: retrievalZeroResultCount,
    manual_override_count: manualOverrideCount,
    
    raw_input_tokens_est_total: rawInputTokensEstTotal,
    compressed_input_tokens_est_total: compressedInputTokensEstTotal,
    compression_ratio_avg: compressionRatioAvg,
    compression_fallback_count: compressionFallbackCount,
    
    quality_score_avg_1_5: qualityScoreAvg,
    followup_count_avg: followupCountAvg,
    manual_retry_count: manualRetryCount,
    
    end_to_end_p95_latency_ms: latencyP95,
    fallback_rate: fallbackRate
  };
}

module.exports = {
  aggregateKPIEvents
};
