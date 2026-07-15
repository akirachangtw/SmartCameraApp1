const fs = require('fs');
const os = require('os');
const path = require('path');
const http = require('http');
const { createApp, MAX_BODY_BYTES } = require('../server');
const { positiveFixtures } = require('../generate_samples');

function request(port, method, route, body) {
  return new Promise((resolve, reject) => {
    const payload = body === undefined ? null : JSON.stringify(body);
    const req = http.request({ hostname: '127.0.0.1', port, method, path: route, headers: payload ? { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(payload) } : {} }, (res) => {
      let text = '';
      res.on('data', (chunk) => { text += chunk; });
      res.on('end', () => {
        let json = null;
        try { json = text ? JSON.parse(text) : null; } catch (error) { json = null; }
        resolve({ status: res.statusCode, json, text });
      });
    });
    req.on('error', reject);
    if (payload) req.write(payload);
    req.end();
  });
}

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

async function run() {
  const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'headroom-supervisor-'));
  const dbFile = path.join(tempDir, 'events.jsonl');
  fs.writeFileSync(dbFile, `${positiveFixtures.map(JSON.stringify).join('\n')}\n`, 'utf8');
  const server = createApp({ dbFile }).listen(0);
  await new Promise((resolve) => server.once('listening', resolve));
  const port = server.address().port;
  try {
    const health = await request(port, 'GET', '/health');
    assert(health.status === 200 && health.json.status === 'healthy', 'health must be healthy for valid fixtures');

    const fixtureSummary = await request(port, 'GET', '/summary');
    assert(fixtureSummary.json.fixture_event_count === 21, 'summary must expose fixture count');
    assert(fixtureSummary.json.promotion_real_event_count === 0, 'fixtures must not promote');

    const real = JSON.parse(JSON.stringify(positiveFixtures[0]));
    real.source_kind = 'real';
    real.request_context.request_id = 'real-http-001';
    real.timestamp = '2000-01-01T00:00:00.000Z';
    const ingest = await request(port, 'POST', '/events/ingest', real);
    assert(ingest.status === 201, 'valid real event must be accepted');
    assert(ingest.json.event.timestamp !== real.timestamp, 'server timestamp must override client timestamp');
    const afterIngest = await request(port, 'GET', '/summary');
    assert(afterIngest.json.promotion_real_event_count === 1, 'HTTP ingest must reach store and summary');

    const invalid = JSON.parse(JSON.stringify(real));
    delete invalid.source_kind;
    const bytesBefore = fs.statSync(dbFile).size;
    const rejected = await request(port, 'POST', '/events/ingest', invalid);
    assert(rejected.status === 422 && fs.statSync(dbFile).size === bytesBefore, 'invalid event must be 4xx with zero append');

    const oversized = JSON.parse(JSON.stringify(real));
    oversized.raw_payload = { padding: 'x'.repeat(MAX_BODY_BYTES) };
    assert((await request(port, 'POST', '/events/ingest', oversized)).status === 413, 'oversized body must be rejected');
    assert((await request(port, 'POST', '/v1/proxy/completions', real)).status === 404, 'mock proxy route must not exist');
    assert((await request(port, 'GET', '/stats')).status === 404, 'fake stats route must not exist');

    fs.appendFileSync(dbFile, '{broken-json\n', 'utf8');
    const degraded = await request(port, 'GET', '/summary');
    assert(degraded.status === 200 && degraded.json.evidence_status === 'degraded', 'bad JSONL must produce degraded evidence');
    const recent = await request(port, 'GET', '/events/recent');
    assert(recent.status === 200 && recent.json.evidence_status === 'degraded', 'recent must remain available and degraded');
    const forbiddenKeys = ['prompt', 'content', 'code', 'api_key', 'messages'];
    assert(recent.json.events.every((event) => forbiddenKeys.every((key) => !(key in event))), 'recent must be metadata-only');
    console.log('PASS: HTTP ingest, aggregation, rejection, degradation, and redaction smoke tests.');
  } finally {
    await new Promise((resolve) => server.close(resolve));
    fs.rmSync(tempDir, { recursive: true, force: true });
  }
}

run().catch((error) => { console.error(`FAIL: ${error.message}`); process.exitCode = 1; });
