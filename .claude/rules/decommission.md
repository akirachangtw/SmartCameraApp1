---
description: 四層退場與 SSOT 遷移規則
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Decommission Rules

## 退場票型管線相容性清單（INFRA-SV-ZERODIFF-01 AI-2 掃描結論）

唯讀掃描（不夾帶修改，Hard Constraint 2）涵蓋零 diff／docs-only 票在管線各節點之「diff 必非空」隱含假設清點：

| 節點 | 空 diff／docs-only 行為 | 是否需修 | 承接票建議 |
|------|------------------------|---------|-----------|
| `cross_review.sh` payload 生成 | 已有 `DIFF_AVAILABLE=false` graceful fallback，不崩潰，仍可產生 payload 供 Codex 審查 | 不需修 | 無 |
| Codex 語意審查輸入（prompt template） | Guard Input Reachability 等問句已對 diff-less 票設計「無新 Guard」預期答案；docs-only 票已有實務 PASS 先例（`INFRA-AGENTS-CR-04`） | 不需修（待觀察） | 無；建議下次真實 docs-only 票時人工確認 Codex 實際回覆符合預期 |
| Traycer 回審 | 外部人工/工具流程，非本地腳本，無本地崩潰風險可查；docs-only／治理票豁免 Traycer 已有既定先例 | 不需修 | 無 |
| `/qa` Step 0（`verify_checklist.sh`） | `CHANGED` 為空時已有明確分支：`⚪ 無變更檔案（diff 為空）` → `✅ Step 0 PASS（無變更）`，`exit 0` | 不需修 | 無 |
| `/qa` Step 4（PROGRESS.md 更新） | 已有票面明文優先序例外（`INFRA-QA-PRECEDENCE-01`），FORBIDDEN 命中可 SKIP | 不需修 | 無 |
| `/qa` Step 4.5（Auto Commit） | 真零 diff（非 FORBIDDEN-hit SKIP）情境下「Commit」子步驟無 `--allow-empty` 或等效處理，`git commit` 對空 staging 會失敗，行為未定義 | **需修** | 建議開票（暫擬 `INFRA-QA-COMMIT-EMPTYDIFF-01`，待 CSO 簽發）：為真零 diff 情境明訂 Commit 子步驟行為 |
| `/qa` Step 5 / `structural_verify.sh --post`（Check B 零 diff 宣告制） | 本票 AI-1 已建三條件宣告制，並修正 POST_MODE 下零 diff PASS 分支正確 fall-through 至 Check D/E/F/G（不再略過） | 已由本票解決 | 無 |
| **附加問**：docs-only／治理票之 `/sv` 適用性 | `/sv` 本體（Stage A–D）無「必有語意可審之產物 diff」會崩潰的隱含假設；Stage D 自動路由 `cross_review.sh` 已優雅處理空 diff。唯一既有跳過先例僅止於 Traycer 層級（治理票豁免），`/sv` 本體未建任何明文跳過分支 | 不需修（假設缺口待明文化） | 建議登記追蹤票，將本結論寫入 `sv/SKILL.md`，避免未來稽核重新推導同一結論 |

## 零 diff 退場票操作要點

零 diff 是**被授權且被宣告**的狀態，不是默認狀態（與 `EXCLUDED_PAYLOAD` 立法目的同源：豁免的是明示對象，不是免檢權）。

### ZERO-DIFF-DECLARED 宣告格式

- 宣告 **MUST** 寫在 sv_report.md 的 `## Files Changed` 區段內，且 **MUST** 為純文字行，**MUST NOT** 寫成 `- ` bullet list item。
  - 原因：既有 `REPORTED_FILES` 解析邏輯會將任何 `- ` 開頭行的第一個 token 當作「已回報檔案」；若宣告寫成 bullet，會被誤判為非空 `REPORTED_FILES`，導致零 diff 分支永遠無法觸發。
- 宣告行 **MUST** 含 token 字面 `ZERO-DIFF-DECLARED`，且同行 **MUST** 附授權依據引用（CSO 裁決編號、Ticket 條款出處等）；裸宣告（無依據）**MUST FAIL**。
- 範例（合法）：
  ```text
  ## Files Changed
  ZERO-DIFF-DECLARED — 授權依據：INFRA-HEADROOM-DECOMMISSION-01 R1 STOP（2026-07-15）CSO 裁決條件3
  ```

### 三條件缺一即 FAIL

1. 機械前提：`DIFF_REF == DIFF_END` **且** `git diff --name-only` 實測為空。
2. 顯式宣告：`ZERO-DIFF-DECLARED` token + 授權依據同行存在。
3. 反向濫用防線：token 存在但實際 `git diff` 非空 → **MUST FAIL**（宣告不得掩蓋實際變更）。

## 退場理由四要件模板

退場（Decommission）票之理由陳述 **MUST** 覆蓋以下四要件（`INFRA-AGENTS-CR-01` 先例）：

1. **管線零引用**：`grep -rn` 確認目標模組/檔案已無任何呼叫點引用。
2. **結構性失效**：說明退場對象為何在現行架構下已無法正常運作或已被取代。
3. **職能覆蓋**：說明退場對象原本承擔之職能已由何者接手（或確認職能本身已不存在）。
4. **git 可回復**：確認退場動作透過 git 歷史可回溯還原，非破壞性刪除。

## 退役旁路律（候選 #19，`KAIROS_CaseC_Harvest_Round_Merged_Laws` KD-7）

- 退役／拆卸類票 **MUST** 於票面預先確立**不經被拆卸層**之合法執行旁路；令之執行路徑 **MUST NOT** 穿越自身標的（否則待拆之 gate 將擋下拆卸動作本身＝退役悖論）。
- 旁路 **MUST** 於開票時具名指明（工具鏈／通道），禁執行中臨時推定。
- 正向取證先例：MEO v1 執法層退役以 Desktop `mcp-ticket-platform` 寫入為旁路（該通道不經 Claude Code PreToolUse hooks，全 session 實證），Step A–E 全序執行完畢。
