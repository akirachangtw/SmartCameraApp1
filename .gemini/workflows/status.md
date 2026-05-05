# Status Workflow

When the user invokes `/status`, perform the following steps to evaluate the session health and token usage.

## Step 1: Calculate Estimates
Estimate the current context token usage internally (do not output the calculation process). Use the following heuristics:
- Chinese characters ≈ 1.5 tokens
- English words ≈ 1.3 tokens
- Code per line ≈ 10-15 tokens
- Context window limit ≈ 175K tokens (or maximum context limit)

## Step 2: Output Report
Generate and output the following snapshot report to the user:

## 📊 Session 狀態報告

| 指標 | 估算值 |
|------|--------|
| 對話輪數 | [N] 輪 |
| 估算 Token | ~[X]K / 175K |
| 使用率 | ~[Y]% |
| 狀態 | 🟢 正常 / 🟡 預警 / 🔴 需優化 |

**Session 健康度**：參照專案指引或根據經驗判斷當前上下文長度是否會導致幻覺或效能下降。

**建議行動**：[根據狀態給出一句具體建議，例如：繼續工作 / 完成當前 Ticket 後執行 /compact / 立即執行 /compact]
