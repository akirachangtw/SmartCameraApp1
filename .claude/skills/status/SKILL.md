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

## Step 0：資料權威分層（MUST）

1. `/context` 為當前 session context 之**最終裁決視圖**；`/compact`／`/clear` 決策 MUST 以其為準。
2. `tmp/claude_status_cache.json` 為本 skill 之 telemetry SSOT，**不是** Claude Code 當前 context 真相。
3. 估算模式為最低層 fallback。

本 skill **無程式化通道**讀取 `/context` 輸出，discrepancy 之偵測與回報為**人工中介**，非自動比對；使用者回報二者不一致時，MUST 以 `/context` 為準並標記為 observability discrepancy，MUST NOT 隱性覆蓋或忽略。

## Step 1：嘗試讀取 statusline 真實快取

執行：
```
bash -c 'cat tmp/claude_status_cache.json 2>/dev/null'
```

若輸出為有效 JSON（非空），解析以下欄位：
- `context_window.total_input_tokens`（或 `current_usage` 各 token 欄位加總）→ 真實 Context **已用絕對量**（bug-immune，見下方註記）
- `rate_limits.five_hour.used_percentage` → 5h Rate Limit 使用%
- `rate_limits.seven_day.used_percentage` → 7d Rate Limit 使用%
- `cost.total_cost_usd` → 本 session 累計花費（USD）
- `model.display_name` → 當前模型名稱

**MUST** 不使用 `context_window_size` / `used_percentage` / `remaining_percentage` 計算 Context 佔比：這三個欄位對 1M 模型（Sonnet 5 / Opus 5 等）因 Claude Code upstream bug（GitHub anthropics/claude-code#63447）回傳被 clamp 的 200K 分母，佔比不可靠。分子（`total_input_tokens`，token 絕對量）不受此 bug 影響。

**改用固定常數分母繞開 bug**：目前主力模型（Sonnet 5 / Opus 5 等）之真實 context window 上限為已知常數 **1,000,000**。**MUST** 以 `total_input_tokens ÷ 1,000,000 × 100`（四捨五入至整數）手動計算佔比，**MUST NOT** 讀取上述三個受污染欄位。本規則僅適用於已知為 1M context window 之模型；非 1M 模型不適用，本 skill 不處理該情境（範圍外）。固定 1,000,000 常數分母所得之佔比為**顯示性 telemetry**，**MUST NOT** 作為 Session 健康度判準、**MUST NOT** 作為 `/compact`／`/clear` 之充分條件。健康度判定依既有「對話輪數」規則（見下方 Session 健康度段），本票不變更之。

快取時效判斷：若快取存在，以 `bash -c 'echo $(( $(date +%s) - $(stat -c %Y tmp/claude_status_cache.json) ))'` 直取快取齡（秒），標記為「🟢 真實數據（快取齡 [N]s）」。**MUST** 僅顯示原始齡值；**MUST NOT** 依該值進行分類、加註狀態或產生建議（`STALE_AFTER_SECONDS` 為 OPEN_PARAMETER，未定值）。

### Step 1.5：checkpoint 記錄（快取讀取成功時 MUST 執行）

以單行 JSON 追加至 `tmp/claude_status_checkpoint.jsonl`，欄位**僅限五欄**：`ts`（epoch 秒）、`session_id`、`total_input_tokens`、`model`（display_name）、`project_dir`。**MUST** 以 `json.dumps` 等安全序列化方式產生該行（**MUST NOT** 手動字串拼接，`project_dir` 於 Windows 路徑含反斜線，裸拼接會產生非法 JSON）；追加後以 `tail -n 20` 覆寫原檔實施輪替（上限 20 行＝載體衛生固定值，非判定門檻）。

讀取 checkpoint 檔**倒數第二**筆 entry；其 `session_id` 與本次相同 → 計算並顯示 `Δtoken = 本次 total_input_tokens − 前筆值`（原始數值，可為負）；不同或無前筆 → 顯示「Δtoken：基線重設（session 更替／首筆）」。**MUST NOT** 據 Δtoken 輸出任何建議或狀態詞（`TOKEN_DELTA_FOR_CHECKPOINT` 為 OPEN_PARAMETER）。

## Step 2：估算模式（快取不存在或解析失敗時 fallback）

**估算方式**（內部計算，不需顯示過程；僅估**已用絕對量**，不除以任何視窗上限）：
- 中文字 ≈ 1.5 tokens
- 英文單詞 ≈ 1.3 tokens
- 代碼每行 ≈ 10-15 tokens
- **MUST** 不估算或顯示 Context 佔比百分比：分母雖已知為 1,000,000（見 Step 1），但此模式下分子本身亦為估算值，兩者相除無意義，故仍只輸出 `~NNK tokens` 絕對值。

## 輸出格式

```
## 📊 Session 狀態報告

| 指標 | 值 | 來源 |
|------|-----|------|
| 模型 | [model name] | 🟢 真實 / 🟡 預設 |
| Context 已用 | ~[N]K tokens (~[P]%) | 🟢 真實 / 🟡 估算 |
| 5h Rate Limit 剩餘 | [N]% | 🟢 真實 / 🟡 N/A |
| Session 花費 | $[X.XXXX] USD | 🟢 真實 / 🟡 N/A |
| 快取齡 | [N]s | 🟢 真實 / 🟡 N/A |
| Δtoken（同 session） | [±N] / 基線重設 | 🟢 真實 / 🟡 N/A |
| 工作目錄一致性 | ✅ / ⚠️（payload.workspace.project_dir vs pwd） | 🟢 真實 / 🟡 N/A |
| 對話輪數 | [N] 輪 | 🟡 估算 |
| 狀態 | 🟢 正常 / 🟡 預警 / 🔴 需優化 | — |
```

> Context 佔比＝已用 token 絕對值 ÷ 1,000,000（1M 模型固定常數），繞開 upstream bug #63447 之 API 分母污染；估算模式（無快取時）因分子亦為估算值，仍不顯示佔比。

**Session 健康度**：**SHOULD** 參照 CLAUDE.md §Session Health 規則。健康度以**對話輪數**判定（≤10 🟢 / 11-18 🟡 / ≥19 🔴），**MUST** 不以 Context 佔比判定（佔比為顯示性 telemetry、非健康度判準；其數值本身經 1M 常數分母已可靠——見 Step 1 定位條款）。

**建議行動**：[**依對話輪數**給出一句建議，例如：繼續工作 / 完成當前 Ticket 後執行 /compact / 立即 /compact]

## 已知取證缺口（原樣登錄，禁升格為事實）

- #63447 之 open／closed 狀態、最後更新日期、maintainer 結論與修復版本**未驗證**；MUST NOT 宣稱該 bug 已修復或特定 Claude Code 版本安全。
- `total_input_tokens` 於受影響版本之端到端正確性無官方明文保證；其定位維持「絕對趨勢訊號」，MUST NOT 據以產生硬性 context 健康結論。
- `used_percentage` 官方定義僅計 input-side、不含 output tokens（本 skill 已禁用該欄位，此處僅為背景登錄）。

> 💡 statusline 快取由 IDE statusLine 命令刷新（含 context_window / rate_limits / cost）。腳本：`.claude/skills/status/scripts/statusline.py`（單一渲染器 + 唯一快取寫入者），快取：`tmp/claude_status_cache.json`。該腳本僅掛載於 `statusLine`，不掛 Stop hook（Stop payload 缺 rich fields）。
