const { aggregateKPIEvents } = require('../lib/kpiAggregator');
const { positiveFixtures } = require('../generate_samples');

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

function run() {
  const fixtureSummary = aggregateKPIEvents(positiveFixtures);
  assert(fixtureSummary.event_count === 21, 'fixture event count must be 21');
  assert(fixtureSummary.fixture_event_count === 21, 'all generated samples must remain fixtures');
  assert(fixtureSummary.real_event_count === 0, 'fixtures must not count as real');
  assert(fixtureSummary.promotion_real_event_count === 0, 'fixtures must not create promotion evidence');
  assert(fixtureSummary.token_savings_rate >= 0 && fixtureSummary.token_savings_rate <= 1, 'token savings must be bounded');

  const realEvent = JSON.parse(JSON.stringify(positiveFixtures[0]));
  realEvent.source_kind = 'real';
  const mixedSummary = aggregateKPIEvents([...positiveFixtures, realEvent]);
  assert(mixedSummary.real_event_count === 1, 'real source count must be isolated');
  assert(mixedSummary.fixture_event_count === 21, 'fixture count must remain isolated');
  assert(mixedSummary.promotion_real_event_count === 1, 'only real events may increment promotion evidence');
  console.log('PASS: aggregation isolates real and fixture evidence.');
  console.log(JSON.stringify(mixedSummary, null, 2));
}

try { run(); } catch (error) { console.error(`FAIL: ${error.message}`); process.exitCode = 1; }
