const fs = require('fs');
const path = require('path');

const FIXTURE_FILE = path.join(__dirname, 'fixtures.jsonl');

function buildEvent(index) {
  const inputTokens = index % 5 === 0 ? 0 : 4000 + index * 500;
  const outputTokens = inputTokens === 0 ? 0 : Math.round(inputTokens * (0.24 + (index % 4) * 0.04));
  const tokenKeepRatio = inputTokens === 0 ? 1 : outputTokens / inputTokens;
  const tokenSavingsRate = inputTokens === 0 ? 0 : (inputTokens - outputTokens) / inputTokens;
  const fallbackActive = index % 7 === 0;

  return {
    scope: 'retrieval_compression_kpi_draft',
    schema_version: '0.2.0',
    event_type: index % 4 === 0 ? 'compression_completed' : 'retrieval_query_completed',
    source_kind: 'fixture',
    timestamp: new Date(Date.UTC(2026, 5, 28, 4, index)).toISOString(),
    repo_context: {
      prompt_contract_path: 'AGENTS.md',
      skill_source_root: '.agents/skills',
      runtime_adapter_root: index % 3 === 0 ? '.codex/skills' : '.claude/skills',
      pilot_config_path: '.agents/pilot/pilot.config.json'
    },
    dashboard_context: {
      extension_id: 'headroom-dashboard',
      sidebar_view_id: 'headroom-view',
      dashboard_view_id: 'main'
    },
    request_context: {
      request_id: `fixture-${String(index + 1).padStart(3, '0')}`,
      agent: index % 3 === 0 ? 'codex' : 'claude',
      task_type: index % 3 === 0 ? 'sv' : 'implementation'
    },
    kpi: {
      retrieval: {
        used_retrieval: index % 4 !== 0,
        retrieval_hit: index % 4 === 0 ? null : index % 6 !== 0,
        retrieval_hit_rate: 0.8
      },
      compression: {
        compression_enabled: inputTokens > 0,
        input_tokens_estimated: inputTokens,
        output_tokens_estimated: outputTokens,
        tokens_saved_estimated: inputTokens - outputTokens,
        token_keep_ratio: tokenKeepRatio,
        token_savings_rate: tokenSavingsRate,
        compression_duration_ms: inputTokens === 0 ? 0 : 40 + index
      },
      fallback: {
        fallback_active: fallbackActive,
        fallback_reason: fallbackActive ? 'fixture_forced_bypass' : null,
        fallback_count: fallbackActive ? 1 : 0,
        total_requests: 1,
        fallback_rate: fallbackActive ? 1 : 0
      },
      latency: {
        end_to_end_latency_ms: 500 + index * 25,
        end_to_end_p95_latency_ms: 1000,
        sidecar_overhead_ms: 30 + index
      }
    }
  };
}

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

function buildNegativeFixtures(base = buildEvent(0)) {
  const missingRequired = clone(base);
  delete missingRequired.source_kind;

  const invalidRatio = clone(base);
  invalidRatio.kpi.compression.token_keep_ratio = 1.2;

  const invalidEnum = clone(base);
  invalidEnum.source_kind = 'synthetic';

  const extraProperty = clone(base);
  extraProperty.unapproved_payload = true;

  return [
    { name: 'missing-required-field', event: missingRequired },
    { name: 'invalid-ratio', event: invalidRatio },
    { name: 'invalid-enum', event: invalidEnum },
    { name: 'additional-property', event: extraProperty }
  ];
}

const positiveFixtures = Array.from({ length: 21 }, (_, index) => buildEvent(index));
const negativeFixtures = buildNegativeFixtures();

function writeFixtures() {
  const jsonl = `${positiveFixtures.map((event) => JSON.stringify(event)).join('\n')}\n`;
  fs.writeFileSync(FIXTURE_FILE, jsonl, 'utf8');
  console.log(`Wrote ${positiveFixtures.length} source_kind=fixture events to ${FIXTURE_FILE}`);
  console.log(`Defined ${negativeFixtures.length} in-memory negative fixtures for validator self-test.`);
}

if (require.main === module) {
  writeFixtures();
}

module.exports = { buildEvent, buildNegativeFixtures, positiveFixtures, negativeFixtures, writeFixtures };
