# Quality Gate Decision Template

## Strict Verification Result

### 1. Decision

- `Approve`
- `Fix Tickets`
- `Escalate`

### 2. Evidence

- Tech Plan intent checked:
- Full ticket set checked:
- Cross-ticket consistency checked:
- Full-scope reverse scans checked:
- Actual implementation findings:
- Runtime or Logcat findings:
- PROGRESS.md sync:
- KAIROS trigger:
- Candidate CLAUDE.md rules:

### 3. Reasoning

- Why this decision is correct

### 4. Learnings

- New constraints, edge cases, performance observations, or rule candidates:

### 5. Next Step

- If `Approve`: record closure requirements and observations
- If `Fix Tickets`: list the fix tickets or exact corrections, then direct the user to `$execute`
- If `Escalate`: state why the work must return to `$architecture-review`
