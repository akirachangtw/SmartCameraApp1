const fs = require('fs');
const path = require('path');
const readline = require('readline');

const DB_FILE = path.join(__dirname, '..', 'events.jsonl');

async function validate() {
  console.log(`Starting validation of event store: ${DB_FILE}`);
  
  if (!fs.existsSync(DB_FILE)) {
    console.error(`Error: File does not exist: ${DB_FILE}`);
    process.exit(1);
  }

  const fileStream = fs.createReadStream(DB_FILE);
  const rl = readline.createInterface({
    input: fileStream,
    crlfDelay: Infinity
  });

  let lineCount = 0;
  let errorCount = 0;

  for await (const line of rl) {
    lineCount++;
    if (!line.trim()) {
      continue;
    }
    
    try {
      const event = JSON.parse(line);
      
      // Basic schema validations
      if (event.scope !== 'retrieval_compression_kpi_draft') {
        console.error(`Line ${lineCount}: Incorrect scope. Expected 'retrieval_compression_kpi_draft', got '${event.scope}'`);
        errorCount++;
      }
      if (!event.timestamp) {
        console.error(`Line ${lineCount}: Missing timestamp`);
        errorCount++;
      }
      if (!event.event_type) {
        console.error(`Line ${lineCount}: Missing event_type`);
        errorCount++;
      }
      if (!event.kpi) {
        console.error(`Line ${lineCount}: Missing kpi object`);
        errorCount++;
      }
    } catch (err) {
      console.error(`Line ${lineCount}: Invalid JSON - ${err.message}`);
      errorCount++;
    }
  }

  console.log(`\nValidation Summary:`);
  console.log(`- Total lines read: ${lineCount}`);
  console.log(`- Total schema/parse errors: ${errorCount}`);

  if (lineCount < 20) {
    console.error(`Error: Event count is below the minimum threshold of 20 (got ${lineCount}).`);
    process.exit(1);
  }

  if (errorCount > 0) {
    console.error(`Error: Validation failed due to ${errorCount} schema errors.`);
    process.exit(1);
  }

  console.log('SUCCESS: All validation checks passed successfully!');
  process.exit(0);
}

validate().catch(err => {
  console.error('Unhandled validation error:', err);
  process.exit(2);
});
