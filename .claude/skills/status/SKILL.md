---
name: status
description: 快速顯示當前對話的 Token 使用估算與 Session 健康度。優先讀取 statusline hook 快取取得真實 API 數據，無快取則 fallback 至估算模式。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Description
快速顯示當前對話的 Token 使用估算與 Session 健康度。

# Prompt
請執行對話狀態快照評估，依以下優先順序取得資料：

## Step 1：嘗試讀取 statusline 真實快取

執行：
```
bash -c 'cat tmp/claude_status_cache.json 2>/dev/null'
```

若輸出為有效 JSON（非空），解析以下欄位：
- `context_window.remaining_percentage` → 真實 Context 剩餘%
- `rate_limits.five_hour.used_percentage` → 5h Rate Limit 使用%
- `rate_limits.seven_day.used_percentage` → 7d Rate Limit 使用%
- `cost.total_cost_usd` → 本 session 累計花費（USD）
- `model.display_name` → 當前模型名稱

快取時效判斷：若快取存在，標記為「🟢 真實數據（上次 statusLine 更新時刷新）」。

## Step 2：估算模式（快取不存在或解析失敗時 fallback）

**估算方式**（內部計算，不需顯示過程）：
- 中文字 ≈ 1.5 tokens
- 英文單詞 ≈ 1.3 tokens
- 代碼每行 ≈ 10-15 tokens
- 上下文窗口 ≈ 200K tokens

## 輸出格式

```
## 📊 Session 狀態報告

| 指標 | 值 | 來源 |
|------|-----|------|
| 模型 | [model name] | 🟢 真實 / 🟡 預設 |
| Context 剩餘 | [N]% ([bar]) | 🟢 真實 / 🟡 估算 |
| 5h Rate Limit 剩餘 | [N]% | 🟢 真實 / 🟡 N/A |
| Session 花費 | $[X.XXXX] USD | 🟢 真實 / 🟡 N/A |
| 對話輪數 | [N] 輪 | 🟡 估算 |
| 狀態 | 🟢 正常 / 🟡 預警 / 🔴 需優化 | — |
```

**Session 健康度**：**SHOULD** 參照 CLAUDE.md §Session Health 規則。

**建議行動**：[根據 Context 剩餘% 與輪數給出一句建議，例如：繼續工作 / 完成當前 Ticket 後執行 /compact / 立即 /compact]

> 💡 statusline 快取由 IDE statusLine 命令刷新（含 context_window / rate_limits / cost），Stop hook payload 因缺少 rich fields 不覆寫快取。腳本：`.claude\skills\status\scripts\statusline.sh`，快取：`tmp/claude_status_cache.json`。
