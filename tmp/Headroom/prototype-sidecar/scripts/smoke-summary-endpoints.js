const http = require('http');

function get(url) {
  return new Promise((resolve, reject) => {
    http.get(url, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        if (res.statusCode !== 200) {
          reject(new Error(`Status ${res.statusCode} from ${url}`));
        } else {
          try {
            resolve(JSON.parse(body));
          } catch (e) {
            reject(new Error(`Failed to parse JSON from ${url}: ${e.message}`));
          }
        }
      });
    }).on('error', reject);
  });
}

async function smokeTest() {
  console.log('Starting Sidecar endpoints smoke tests...');
  const baseUrl = 'http://127.0.0.1:8788';
  let failures = 0;

  try {
    console.log(`\nTesting GET ${baseUrl}/summary...`);
    const summary = await get(`${baseUrl}/summary`);
    console.log('Response:', JSON.stringify(summary, null, 2));
    
    // Check required fields
    if (summary.source_status !== 'prototype') {
      console.error('FAIL: Expected source_status to be "prototype"');
      failures++;
    }
    if (typeof summary.retrieval_queries_total !== 'number') {
      console.error('FAIL: Missing or invalid retrieval_queries_total');
      failures++;
    }
  } catch (err) {
    console.error('FAIL: /summary request failed:', err.message);
    failures++;
  }

  try {
    console.log(`\nTesting GET ${baseUrl}/events/recent...`);
    const recent = await get(`${baseUrl}/events/recent`);
    console.log(`Response contains ${recent.length} recent events.`);
    
    if (!Array.isArray(recent)) {
      console.error('FAIL: Expected response to be an array');
      failures++;
    }
  } catch (err) {
    console.error('FAIL: /events/recent request failed:', err.message);
    failures++;
  }

  if (failures > 0) {
    console.error(`\nSmoke tests FAILED with ${failures} errors.`);
    process.exit(1);
  }

  console.log('\nSUCCESS: All smoke tests passed successfully!');
  process.exit(0);
}

smokeTest();
