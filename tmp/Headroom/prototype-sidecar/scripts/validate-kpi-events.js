const fs = require('fs');
const path = require('path');

const DEFAULT_EVENT_FILE = path.join(__dirname, '..', 'events.jsonl');
const SCHEMA_FILE = path.join(__dirname, '..', '..', 'pilot-docs', 'retrieval-compression-kpi-schema.json');
const schema = JSON.parse(fs.readFileSync(SCHEMA_FILE, 'utf8'));

function typeMatches(value, expected) {
  if (expected === 'null') return value === null;
  if (expected === 'array') return Array.isArray(value);
  if (expected === 'object') return value !== null && typeof value === 'object' && !Array.isArray(value);
  if (expected === 'integer') return Number.isInteger(value);
  if (expected === 'number') return typeof value === 'number' && Number.isFinite(value);
  return typeof value === expected;
}

function validateNode(value, rule, location, errors) {
  const expectedTypes = rule.type === undefined ? [] : (Array.isArray(rule.type) ? rule.type : [rule.type]);
  if (expectedTypes.length > 0 && !expectedTypes.some((type) => typeMatches(value, type))) {
    errors.push(`${location}: expected ${expectedTypes.join('|')}`);
    return;
  }
  if (Object.prototype.hasOwnProperty.call(rule, 'const') && value !== rule.const) {
    errors.push(`${location}: must equal ${JSON.stringify(rule.const)}`);
  }
  if (rule.enum && !rule.enum.some((candidate) => candidate === value)) {
    errors.push(`${location}: value is not in enum`);
  }
  if (typeof value === 'number') {
    if (rule.minimum !== undefined && value < rule.minimum) errors.push(`${location}: below minimum ${rule.minimum}`);
    if (rule.maximum !== undefined && value > rule.maximum) errors.push(`${location}: above maximum ${rule.maximum}`);
  }
  if (typeof value === 'string') {
    if (rule.minLength !== undefined && value.length < rule.minLength) errors.push(`${location}: shorter than ${rule.minLength}`);
    if (rule.format === 'date-time' && (!/^\d{4}-\d{2}-\d{2}T/.test(value) || Number.isNaN(Date.parse(value)))) {
      errors.push(`${location}: invalid date-time`);
    }
  }
  if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
    const properties = rule.properties || {};
    for (const required of rule.required || []) {
      if (!Object.prototype.hasOwnProperty.call(value, required)) errors.push(`${location}.${required}: required property missing`);
    }
    for (const [key, childValue] of Object.entries(value)) {
      if (properties[key]) validateNode(childValue, properties[key], `${location}.${key}`, errors);
      else if (rule.additionalProperties === false) errors.push(`${location}.${key}: additional property not allowed`);
    }
  }
  if (Array.isArray(value) && rule.items) {
    value.forEach((item, index) => validateNode(item, rule.items, `${location}[${index}]`, errors));
  }
}

function validateCompressionFormula(event, errors) {
  const compression = event?.kpi?.compression;
  if (!compression || !Number.isInteger(compression.input_tokens_estimated) || !Number.isInteger(compression.output_tokens_estimated)) return;
  const input = compression.input_tokens_estimated;
  const output = compression.output_tokens_estimated;
  if (output > input) errors.push('$.kpi.compression.output_tokens_estimated: cannot exceed input_tokens_estimated');
  const expectedKeep = input === 0 ? 1 : output / input;
  const expectedSavings = input === 0 ? 0 : (input - output) / input;
  if (Math.abs(compression.token_keep_ratio - expectedKeep) > 1e-9) {
    errors.push('$.kpi.compression.token_keep_ratio: does not match output/input formula');
  }
  if (Math.abs(compression.token_savings_rate - expectedSavings) > 1e-9) {
    errors.push('$.kpi.compression.token_savings_rate: does not match (input-output)/input formula');
  }
}

function validateEvent(event, schemaOverride = schema) {
  const errors = [];
  validateNode(event, schemaOverride, '$', errors);
  validateCompressionFormula(event, errors);
  return { valid: errors.length === 0, errors };
}

function validateJsonlText(text) {
  const results = [];
  text.split(/\r?\n/).forEach((line, index) => {
    if (!line.trim()) return;
    try {
      const event = JSON.parse(line);
      results.push({ line: index + 1, ...validateEvent(event) });
    } catch (error) {
      results.push({ line: index + 1, valid: false, errors: [`$: invalid JSON - ${error.message}`] });
    }
  });
  return results;
}

function runSelfTest() {
  const { positiveFixtures, negativeFixtures } = require('../generate_samples');
  const positiveFailures = positiveFixtures.filter((event) => !validateEvent(event).valid);
  const negativeFalsePasses = negativeFixtures.filter(({ event }) => validateEvent(event).valid);
  const legacyFailures = positiveFixtures
    .map((event) => { const legacy = JSON.parse(JSON.stringify(event)); delete legacy.source_kind; return legacy; })
    .filter((event) => !validateEvent(event).valid);
  console.log(`Self-test: positive=${positiveFixtures.length - positiveFailures.length}/${positiveFixtures.length} PASS`);
  console.log(`Self-test: negative=${negativeFixtures.length - negativeFalsePasses.length}/${negativeFixtures.length} correctly rejected`);
  console.log(`Self-test: legacy=${legacyFailures.length}/${positiveFixtures.length} correctly rejected`);
  return positiveFailures.length === 0 && negativeFalsePasses.length === 0 && legacyFailures.length === positiveFixtures.length;
}

function runCli() {
  if (process.argv.includes('--self-test')) process.exitCode = runSelfTest() ? 0 : 1;
  else {
    const fileIndex = process.argv.indexOf('--file');
    const eventFile = fileIndex >= 0 ? path.resolve(process.argv[fileIndex + 1]) : DEFAULT_EVENT_FILE;
    if (!fs.existsSync(eventFile)) throw new Error(`File does not exist: ${eventFile}`);
    const results = validateJsonlText(fs.readFileSync(eventFile, 'utf8'));
    const failures = results.filter((result) => !result.valid);
    failures.forEach((failure) => console.error(`Line ${failure.line}: ${failure.errors.join('; ')}`));
    console.log(`Validation Summary: ${results.length - failures.length}/${results.length} PASS`);
    process.exitCode = results.length >= 20 && failures.length === 0 ? 0 : 1;
  }
}

if (require.main === module) {
  try { runCli(); } catch (error) { console.error(error.message); process.exitCode = 2; }
}

module.exports = { schema, validateEvent, validateJsonlText };
