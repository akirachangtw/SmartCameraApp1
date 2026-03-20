# Quality Gate Decision Template

## Strict Verification Result

### 1. Decision

- `Approve`
- `Fix Tickets`
- `Escalate`

### 2. Evidence

- Tech Plan intent checked:
- Ticket scope checked:
- Actual implementation findings:
- Runtime or Logcat findings:

### 3. Reasoning

- Why this decision is correct

### 4. Next Step

- If `Approve`: request learnings be added to `devspec/feature_record_Backlog.md`
- If `Fix Tickets`: list the fix tickets or exact corrections, then direct the user to `$execute`
- If `Escalate`: state why the work must return to `$architecture-review`
