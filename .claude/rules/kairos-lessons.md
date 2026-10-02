---
description: KAIROS 回顧產出的架構守則 — 經實戰驗證的規則
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# KAIROS Lessons (實戰萃取規則)

## K1-Rule 11 — 骨架 Ticket 未實作強制聲明
D1/骨架 Ticket 中的未實作函數，**MUST** 以 `error("Not implemented — will be wired in DX-TY")`
顯式拋出。**MUST** 嚴禁回傳合法型別空值（`null`、`emptyList()`）偽裝完成。
觸發來源：SV-D2-T4-FIX-01

## K1-Rule 12 — Rule 負向測試強制覆蓋
每個 `CompositionRule` 實作的測試套件，**MUST** 包含 ≥1 組負向測試
（空 faces 清單 + 座標超界 `[0,1]` 以外），驗證結果為 `score=0` / `isAligned=false`。
觸發來源：SV-D2-T1-FIX-01~03

## K2-Rule 13 — Android SDK 型別 JVM 測試環境規則
使用 `android.graphics.PointF`/`RectF` 的類別，JVM 單元測試 **MUST** 加上
`@RunWith(RobolectricTestRunner::class)` + `@Config(sdk = [28])`，
或將欄位型別改為純 Kotlin data class。
理由：stub 環境下 PointF/RectF fields 全回傳 0，導致測試假過。
觸發來源：D6-B2-T1, D6-B3

## K3-Rule 18 — Edit 前強制 Re-view
對 >200 行的 .kt/.xml，Edit (str_replace) 前 **MUST** 先 Read 操作區段（±10 行上下文）。
/compact 後視為強制觸發條件（無論檔案大小）。
（注意：此規則已內嵌至核心鐵律 5「寫之前先讀」中）

## K3-Rule 25 — Partial Fix 驗收紀律
Bug 修復 Ticket 若只覆蓋「觸發路徑」而非「所有消費路徑」，/sv **MUST**：
(a) 明列「已知未覆蓋路徑」於 sv_report.md；
(b) 開後續 Ticket 追蹤未覆蓋路徑；
(c) **MUST** 不得將僅覆蓋部分路徑的修復標記為「完整修復」。
範例：P05-R1 修 EyeAfController（AF 路徑），但 HaloGuidanceLayer（渲染路徑）
和 observePtfAlignment（PTF 路徑）未覆蓋 → 琥珀圓環持續亂竄（Ghost Face 壁癌）。
- **Bug 品類覆蓋**：修復 scope **MUST** 覆蓋品類邊界而非已知實例。反例：Ghost Face 三次壁癌（AF 越界→全路徑越界→形狀），每次只堵一個子類。（觸發來源：K4-6）
觸發來源：Session V — GazeAF Ghost Face 壁癌分析。

## K3-Rule 26 — 多消費路徑掃描

Ticket 動工前（Plan 階段），**MUST** 對 Ticket 要修改的「資料來源」執行：
`grep -rn "[資料來源名]" app/src/main/java --include=*.kt`
列出所有消費者路徑，每條路徑標註「是否在 ALLOWED 範圍」。
若存在消費者不在 ALLOWED 範圍 → **MUST** 回報 CSO，等待裁決是否擴充 ALLOWED 或開後續 Ticket。
**MUST** 嚴禁「只修觸手可及的路徑，忽略看不見的消費者」。
觸發來源：Session V — GazeAF Ghost Face 壁癌分析（三條消費路徑，P05-R1 僅覆蓋一條）。

## K4-Rule 28 — Guidance Gate Hotfix 架構邊界

任何在 detection/face 管線上加入 predicate filter 的 Hotfix，**MUST** 滿足三項架構邊界：

① **Branch 分離**：predicate **MUST** 只能落在 consumer 層（`observeCompositionAdvice` / `observePtfAlignment` 等），**MUST** 禁止在 shared raw state（`_state.detectionResult.faces`）上刪臉；Branch B（AF pipeline）依賴完整 raw list。

② **Branch B 存活驗證**：predicate = 0 eligible 幀時，AF pipeline **MUST** 仍可存活；需實機 Logcat 證明（對焦觸發 + guidance=0 同幀可觀測）。

③ **Magic Number 禁止**：filter threshold **MUST** 定義於 companion object 常數，禁止 inline 魔術數字；所有 consumer 層 inline lambda 引用常數名。

觸發來源：D12 HOTFIX-47（K380 Ghost Face guidance gate；GF-01~03 雙裝置實機驗收 PASS）。

## K5-Rule 29 — Prompt-Layer Governance Ticket 雙層驗證

Prompt-layer 治理 Ticket（只修改 `.sh` / `.md` skill 檔案的 wiring 票）**MUST** 驗證兩個產物均正確運作，僅完成文件修改不算閉環：

① **Payload Emitter（發送端）**：prompt 字串確實包含新問句（可透過 `grep` 或 heredoc diff 驗證）。

② **Reviewer Output Artifact（接收端）**：Cross-Review 實際執行後的 Codex 輸出 **MUST** 包含對應強制回答行（如 `Guard Input Reachability: 無新 Guard`）。

doc-only wiring（只更新 SKILL.md/CROSS_REVIEW.md 說明文字，但未在 `cross_review.sh` PROMPT_HEADER 中硬接線）= 文件層假閉環，不得標 Done。

**Why**：INFRA-REACH-01 原始 AI-3 實作將規則寫入 `sv/SKILL.md`（human reminder 層），但 `cross_review.sh` PROMPT_HEADER 未注入，Codex 在無 ssot_context 的環境中無法執行此檢查——即使本輪 PASS 也只是因 ssot_context 恰好可用。

**How to apply**：Prompt-layer 治理 Ticket /sv 時，Cross-Review 輸出中 **MUST** 出現被注入問句的對應回答行；缺失 → REPORT_FIX，要求補充 prompt wiring 後重跑。

觸發來源：D12 INFRA-REACH-01-FIX-AI3 Traycer Rule Harvest。

## 認知條款 — SessionStart 重入（候選 #18，`KAIROS_CaseC_Harvest_Round_Merged_Laws` KD-6）

- SessionStart hook 於 `--resume` 重入時再次觸發，係官方**設計行為**，非平台 bug。
- 故跨 session 狀態 **MUST NOT** 依賴 hook 重置語意作為真理來源；需跨 session 存續之狀態 **MUST** 外部化至獨立事件載體（append-only event log 類），由 reducer 導出當前態。
- 事件支撐：MEO v1 phantom-reset（STATE.md 10+ 處註記），為可預期生命週期後果；v2-lite 事件層即本條款之承載設計。
- 性質：認知約束（無機械驗收面）。翻盤條件：v2-lite 事件層若證實仍受 session 邊界污染，升格為行為規則並附機械檢核。
