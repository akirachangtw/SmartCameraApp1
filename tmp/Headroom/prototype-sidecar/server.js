const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const { aggregateKPIEvents } = require('./lib/kpiAggregator');
const { validateEvent } = require('./scripts/validate-kpi-events');

const DEFAULT_PORT = Number(process.env.PORT || 8788);
const DEFAULT_DB_FILE = path.join(__dirname, 'events.jsonl');
const MAX_BODY_BYTES = 128 * 1024;

function ensureStore(dbFile) {
  fs.mkdirSync(path.dirname(dbFile), { recursive: true });
  if (!fs.existsSync(dbFile)) fs.writeFileSync(dbFile, '', 'utf8');
}

function readEventStore(dbFile) {
  ensureStore(dbFile);
  const events = [];
  const degradedEvidence = [];
  const lines = fs.readFileSync(dbFile, 'utf8').split(/\r?\n/);
  lines.forEach((line, index) => {
    if (!line.trim()) return;
    try {
      const event = JSON.parse(line);
      const validation = validateEvent(event);
      if (validation.valid) events.push(event);
      else degradedEvidence.push({ line: index + 1, kind: 'schema', errors: validation.errors });
    } catch (error) {
      degradedEvidence.push({ line: index + 1, kind: 'json', errors: [error.message] });
    }
  });
  return { events, degradedEvidence };
}

function recentMetadata(event) {
  return {
    timestamp: event.timestamp,
    event_type: event.event_type,
    source_kind: event.source_kind,
    request_id: event.request_context?.request_id || null,
    agent: event.request_context?.agent || 'unknown',
    task_type: event.request_context?.task_type || 'unknown',
    fallback_active: event.kpi?.fallback?.fallback_active === true,
    input_tokens_estimated: event.kpi?.compression?.input_tokens_estimated ?? null,
    output_tokens_estimated: event.kpi?.compression?.output_tokens_estimated ?? null
  };
}

function createApp(options = {}) {
  const dbFile = options.dbFile || process.env.HEADROOM_EVENT_STORE || DEFAULT_DB_FILE;
  ensureStore(dbFile);
  const app = express();
  app.use(cors());
  app.use(express.json({ limit: MAX_BODY_BYTES }));

  app.get('/health', (req, res) => {
    try {
      const { degradedEvidence } = readEventStore(dbFile);
      res.json({
        status: degradedEvidence.length === 0 ? 'healthy' : 'degraded',
        service: 'headroom-observability-supervisor',
        evidence_status: degradedEvidence.length === 0 ? 'complete' : 'degraded',
        invalid_event_lines: degradedEvidence.length
      });
    } catch (error) {
      res.status(503).json({ status: 'unhealthy', service: 'headroom-observability-supervisor', error: error.message });
    }
  });

  app.post('/events/ingest', (req, res) => {
    const event = { ...req.body, timestamp: new Date().toISOString() };
    const validation = validateEvent(event);
    if (!validation.valid) return res.status(422).json({ error: 'schema_validation_failed', details: validation.errors });
    try {
      fs.appendFileSync(dbFile, `${JSON.stringify(event)}\n`, 'utf8');
      return res.status(201).json({ status: 'accepted', event });
    } catch (error) {
      return res.status(500).json({ error: 'event_store_write_failed', details: error.message });
    }
  });

  app.get('/summary', (req, res) => {
    try {
      const { events, degradedEvidence } = readEventStore(dbFile);
      res.json({
        source_status: 'prototype',
        evidence_status: degradedEvidence.length === 0 ? 'complete' : 'degraded',
        invalid_event_lines: degradedEvidence.length,
        ...aggregateKPIEvents(events)
      });
    } catch (error) {
      res.status(503).json({ source_status: 'prototype', evidence_status: 'unavailable', error: error.message });
    }
  });

  app.get('/events/recent', (req, res) => {
    try {
      const { events, degradedEvidence } = readEventStore(dbFile);
      res.json({
        evidence_status: degradedEvidence.length === 0 ? 'complete' : 'degraded',
        invalid_event_lines: degradedEvidence.length,
        events: events.slice(-20).map(recentMetadata)
      });
    } catch (error) {
      res.status(503).json({ evidence_status: 'unavailable', events: [], error: error.message });
    }
  });

  app.use((error, req, res, next) => {
    if (error?.type === 'entity.too.large') return res.status(413).json({ error: 'payload_too_large', max_bytes: MAX_BODY_BYTES });
    if (error instanceof SyntaxError) return res.status(400).json({ error: 'invalid_json' });
    return next(error);
  });
  return app;
}

function startServer(port = DEFAULT_PORT, options = {}) {
  const server = createApp(options).listen(port, () => {
    console.log(`Headroom observability supervisor listening on ${server.address().port}`);
    console.log('Routes: GET /health, POST /events/ingest, GET /summary, GET /events/recent');
  });
  return server;
}

if (require.main === module) startServer();

module.exports = { createApp, startServer, readEventStore, recentMetadata, MAX_BODY_BYTES };
