const fs = require('fs');
const path = require('path');
const readline = require('readline');
const { aggregateKPIEvents } = require('../lib/kpiAggregator');

const DB_FILE = path.join(__dirname, '..', 'events.jsonl');

async function verify() {
  console.log(`Verifying aggregation against event store: ${DB_FILE}`);
  
  if (!fs.existsSync(DB_FILE)) {
    console.error(`Error: File does not exist: ${DB_FILE}`);
    process.exit(1);
  }

  const events = [];
  const fileStream = fs.createReadStream(DB_FILE);
  const rl = readline.createInterface({
    input: fileStream,
    crlfDelay: Infinity
  });

  for await (const line of rl) {
    if (line.trim()) {
      try {
        events.push(JSON.parse(line));
      } catch (e) {
        console.error(`Warning: Failed to parse line: ${line.trim()}`);
      }
    }
  }

  const summary = aggregateKPIEvents(events);
  console.log('\nAggregation Summary:');
  console.log(JSON.stringify(summary, null, 2));

  // Assertions
  let failures = 0;
  
  if (typeof summary.retrieval_queries_total !== 'number' || summary.retrieval_queries_total < 0) {
    console.error('FAIL: retrieval_queries_total is not a valid number');
    failures++;
  }
  
  if (summary.retrieval_hit_rate !== null && (typeof summary.retrieval_hit_rate !== 'number' || summary.retrieval_hit_rate < 0 || summary.retrieval_hit_rate > 1)) {
    console.error('FAIL: retrieval_hit_rate is out of bounds [0, 1]');
    failures++;
  }

  if (summary.compression_ratio_avg !== null && (typeof summary.compression_ratio_avg !== 'number' || summary.compression_ratio_avg < 0 || summary.compression_ratio_avg > 1)) {
    console.error('FAIL: compression_ratio_avg is out of bounds [0, 1]');
    failures++;
  }

  if (failures > 0) {
    console.error(`\nVerification FAILED with ${failures} assertion errors.`);
    process.exit(1);
  }

  console.log('\nSUCCESS: KPI aggregation verified successfully!');
  process.exit(0);
}

verify().catch(err => {
  console.error('Unexpected verification error:', err);
  process.exit(2);
});
