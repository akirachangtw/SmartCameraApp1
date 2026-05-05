---
name: status
description: Quickly estimate current conversation token usage and session health. Use when the user invokes /status, asks for token monitoring, asks whether the session should continue, or wants a concise context-window health snapshot.
---

# Status

## Workflow

Produce a concise session status snapshot without exposing detailed token arithmetic.

1. Estimate the current conversation size from visible context and recent tool output.
2. Classify session health against an approximate 200K-token context window.
3. Give one concrete next action.

## Estimation Rules

- Chinese character: approximately 1.5 tokens.
- English word: approximately 1.3 tokens.
- Code or logs: approximately 10-15 tokens per line.
- Treat the usable context window as approximately 200K tokens unless the active model or environment states otherwise.

## Health Bands

- Green: under 50% usage. Continue normally.
- Yellow: 50-75% usage. Continue, but prepare a handoff after the current logical unit.
- Orange: 75-90% usage. Prefer finishing the current ticket and then use `$compact` or produce a handoff.
- Red: over 90% usage. Stop expanding scope and create a handoff immediately.

## Output Format

Use this format:

```markdown
/status: Token 使用率估算 [低/中/高/臨界]。
- 對話輪數: 約 [N] 輪
- 估算 Token: 約 [X]K / 250K
- 使用率: 約 [Y]%
- 狀態: [綠/黃/橙/紅] - [一句原因]
- 建議行動: [一句具體建議]
```

Keep the answer short. Do not run code unless the user asks for a persisted report or a repository-specific status check.
