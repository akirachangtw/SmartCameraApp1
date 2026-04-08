---
disable-model-invocation: false
---
# Description
快速顯示當前對話的 Token 使用估算與 Session 健康度。

# Prompt
請執行對話狀態快照評估，輸出以下格式：

## 📊 Session 狀態報告

| 指標 | 估算值 |
|------|--------|
| 對話輪數 | [N] 輪 |
| 估算 Token | ~[X]K / 200K |
| 使用率 | ~[Y]% |
| 狀態 | 🟢 正常 / 🟡 預警 / 🔴 需優化 |

**估算方式**（內部計算，不需顯示過程）：
- 中文字 ≈ 1.5 tokens
- 英文單詞 ≈ 1.3 tokens
- 代碼每行 ≈ 10-15 tokens
- 上下文窗口 ≈ 200K tokens

**Session 健康度**（對應 /sv /qa 標準）：

| 對話輪數 | 狀態 | 建議 |
|---------|------|------|
| ≤ 10 turns | 🟢 | 繼續工作 |
| 11-18 turns | 🟡 | 留意，密集 Coding 可考慮下個 Ticket 前 /compact |
| ≥ 19 turns | 🔴 | 建議立即 /compact 開新 Session |

「turns」= 本次對話中 user 訊息的總數（不含系統 Prompt）。  
若對話中有大量代碼貼入（單則 >200 行），每則代碼額外計為 +2 turns。

**建議行動**：[根據狀態給出一句具體建議，例如：繼續工作 / 完成當前 Ticket 後執行 /compact / 立即 /compact]
