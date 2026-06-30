const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const readline = require('readline');
const { aggregateKPIEvents } = require('./lib/kpiAggregator');

const app = express();
const PORT = process.env.PORT || 8788;
const DB_FILE = path.join(__dirname, 'events.jsonl');
const FIXTURES_FILE = path.join(__dirname, 'fixtures.jsonl');

app.use(cors());
app.use(express.json({ limit: '50mb' }));

// Init DB with fixtures if empty
if (!fs.existsSync(DB_FILE)) {
  if (fs.existsSync(FIXTURES_FILE)) {
    fs.copyFileSync(FIXTURES_FILE, DB_FILE);
    console.log('Initialized events.jsonl from fixtures.jsonl');
  } else {
    fs.writeFileSync(DB_FILE, '');
  }
}

app.post('/events/ingest', (req, res) => {
  const event = {
    timestamp: new Date().toISOString(),
    ...req.body
  };
  fs.appendFile(DB_FILE, JSON.stringify(event) + '\n', (err) => {
    if (err) return res.status(500).json({ error: 'Failed to write event' });
    res.status(201).json({ status: 'ok', event });
  });
});

app.get('/summary', async (req, res) => {
  const events = [];
  if (fs.existsSync(DB_FILE)) {
    const fileStream = fs.createReadStream(DB_FILE);
    const rl = readline.createInterface({ input: fileStream, crlfDelay: Infinity });

    for await (const line of rl) {
      if (line.trim()) {
        try {
          events.push(JSON.parse(line));
        } catch (e) {
          // ignore
        }
      }
    }
  }

  const kpis = aggregateKPIEvents(events);
  res.json({
    source_status: 'prototype',
    ...kpis
  });
});

app.get('/events/recent', async (req, res) => {
  const events = [];
  if (fs.existsSync(DB_FILE)) {
    const fileStream = fs.createReadStream(DB_FILE);
    const rl = readline.createInterface({ input: fileStream, crlfDelay: Infinity });

    for await (const line of rl) {
      if (line.trim()) {
        try {
          events.push(JSON.parse(line));
        } catch (e) {
          // ignore
        }
      }
    }
  }

  const recent = events.slice(-20).map(e => ({
    ts: e.timestamp,
    type: e.event_type && e.event_type.includes('fallback') ? 'fallback' : 'info',
    reason: e.event_type || 'unknown',
    level: e.kpi?.compression?.compression_fallback_level || (e.event_type && e.event_type.includes('fallback') ? 'R1' : 'none')
  }));

  res.json(recent);
});

// Mock Proxy Endpoint to simulate compression routing and fallback
app.post('/v1/proxy/completions', (req, res) => {
  const payload = req.body;
  const isLog = payload.type === 'log';
  const isTool = payload.type === 'tool_output';
  const isSearch = payload.type === 'search_result';
  const isCode = payload.type === 'code_context';

  let rawTokens = payload.content ? Math.ceil(payload.content.length / 4) : 0;
  let compTokens = rawTokens;
  let fallbackLevel = 'none';
  let fallbackReason = 'none';

  // Determine fallback level (Mock logic based on payload config or randomness)
  if (payload.force_f3 || isCode) {
    fallbackLevel = 'F3';
    fallbackReason = isCode ? 'full_text_required' : 'proxy_error';
  } else if (payload.force_f2 || (isSearch && Math.random() < 0.2)) {
    fallbackLevel = 'F2';
    fallbackReason = 'quality_drop';
  } else if (payload.force_f1 || Math.random() < 0.1) {
    fallbackLevel = 'F1';
    fallbackReason = 'quality_drop';
    compTokens = Math.ceil(rawTokens * 0.8); // Less aggressive compression
  } else if (isLog || isTool || isSearch) {
    compTokens = Math.ceil(rawTokens * 0.3); // Aggressive compression
  }

  const ratio = rawTokens > 0 ? (rawTokens - compTokens) / rawTokens : 0;

  const event = {
    scope: 'retrieval_compression_kpi_draft',
    schema_version: '0.1.0',
    event_type: 'compression_completed',
    timestamp: new Date().toISOString(),
    kpi: {
      compression: {
        compression_enabled: fallbackLevel !== 'F3',
        input_tokens_estimated: rawTokens,
        output_tokens_estimated: compTokens,
        compression_ratio: ratio,
        compression_fallback_level: fallbackLevel,
        compression_fallback_reason: fallbackReason
      }
    }
  };

  fs.appendFile(DB_FILE, JSON.stringify(event) + '\n', () => {
    res.status(200).json({
      status: 'ok',
      compressed: fallbackLevel !== 'F3',
      fallback_level: fallbackLevel,
      mock_response: "Simulated LLM Response"
    });
  });
});

app.get('/health', (req, res) => {
  res.json({ status: 'healthy', service: 'codegraph' });
});

app.get('/stats', (req, res) => {
  res.json({
    queries_executed: 42,
    index_status: 'up-to-date',
    symbols_indexed: 1200
  });
});

app.listen(PORT, () => {
  console.log(`Prototype Sidecar listening on port ${PORT}`);
  console.log(`- POST /events/ingest`);
  console.log(`- GET  /summary`);
  console.log(`- GET  /events/recent`);
  console.log(`- GET  /health (codegraph)`);
  console.log(`- GET  /stats (codegraph)`);
});
