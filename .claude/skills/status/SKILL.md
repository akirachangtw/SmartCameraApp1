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

**Session 健康度**：參照 CLAUDE.md §Session Health 規則。

**建議行動**：[根據狀態給出一句具體建議，例如：繼續工作 / 完成當前 Ticket 後執行 /compact / 立即 /compact]
