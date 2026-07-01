# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| TRACTION-GOV-01 | 🟡 /qa PASS · Layout Spec 版本漂移修正 SSOT v4.0.5→v4.0.6（16 處，殘留 0；含 L120 downstream sweep）· Cross-Review N/A · 待 /index-verify · 1F +16/-16L |
| TRACTION-SSOT-03 | ✅ /sv PASS · Level roll-only D-9 清理（§2.0.1 pitch 化石移除→abs(rollDiff)）+ §2.4 補全 + Layout Layer 5 表 · Codex R1 PASS · 2F +30/-3L · f87aae0 |
| TRACTION-SSOT-01 | ✅ /sv PASS · Halo 三層規格重寫（外環 alpha 0.8 + 牽引圓點 atan2/三段V曲線）· Codex R2 PASS · 2F +80/-16L · fc6ffa8 |
| TRACTION-SSOT-02 | ✅ /sv PASS · D-11 加亮弧→牽引圓點（atan2 歸屬保留）· Codex R2 PASS · 1F +2/-7L · 0a336ca |
| INFRA-REACH-01-FIX-AI3 | ✅ /sv PASS · cross_review.sh §3.5 + CROSS_REVIEW.md wiring · 2F +38L · 3028bd1 |

**Last Updated**: 2026-07-01

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S26** · TRACTION-GOV-01 Layout 版本漂移 v4.0.5→v4.0.6 🟡 /qa PASS 待 /index-verify · SSOT-03 ✅ /sv PASS · GAP-06 仍 🔴 BLOCKED（待 HaloGuidanceLayer.kt 渲染實作）· **→ 開渲染實作票** |
| Compile | 0 errors ✅ (assembleDeviceDebug PASS) |
| Tests | 400 passed ✅ (FIX-B +4 reducer tests；androidTest 編譯 PASS) |
| Lint | 0 errors ✅ (0 new warnings in ALLOWED files) |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| GAP-06 | HaloTraction 渲染缺牽引圓點：Normal 無 HaloTraction（出 Focus Ring）；PTF 圓點靜態不磁吸 | 🔴 P0 | SSOT 規格已修（TRACTION-SSOT-01 ✅：alpha 0.8 + 牽引圓點 + 三段 V 曲線）；待 HaloGuidanceLayer.kt 渲染實作票落地 |
| GAP-07 | afState 回授斷裂 → Guard 7a 死碼 → 瘋狂 AF（Focus Ring 閃動）（**=AC-10 假過根因**）| 🔴 P0 | CameraViewModel.kt L2117 result 被拋棄、afState 永 Idle（dead field）；需接線 |
| BUG-P08-F4-REAL-DEVICE | AC-4 🔴 橫向 detection 失效（BUG-LCSOURCE-HARDCODED-ROTATION）| 🔴 Hard Gate FAIL | Phase B 後開票 |
| BUG-LCSOURCE-HARDCODED-ROTATION | compensatedRotation 硬編碼 → 橫向偏差 | 🟡 P1 | 開 P08-F9 後處理 |

> **🟠 治理校正（2026-06-30）**：AC-10（skip-when-focused / Guard 7a）由 ✅ 改 **⚠️ PARTIAL** — unit logic 已實作測試，但 production input `AfState.Locked` 為 dead field（不可達），三道 gate 結構性假過。實際修復 = GAP-07；流程防護 = INFRA-REACH-01（已開票 Pending/）。

## Next Actions

> **S24（2026-06-30）：INFRA-REACH-01 + FIX-AI3 全鏈路閉環 · Guard Input Reachability gate 完整落地 · K5-Rule 29 收割**

1. **✅ TRACTION-SSOT-01**（/sv + Codex R2 PASS · fc6ffa8）：SSOT §2.5/§2.5.1/§2.5.2 + Layout §3.3 規格已落地 → **下一步開 HaloGuidanceLayer.kt 渲染實作票**（GAP-06 解除前置）
2. **🔴 開票 GAP-07**：修 afState 回授斷裂（CameraViewModel.kt L2117 接線）
3. **🔴 實機重測**：TRACTION-SSOT-01 修復後，AC-6/8/9 實機重測 → Traycer 終審
4. **🟡 STATE-CONTRACT-01 Traycer 終審**：FIX-A + FIX-B + FIX-C 均 /sv PASS，送終審
5. **🟡 TRACTION-GOV-01**：Layout Spec v4.0.5 → v4.0.6 版本漂移修正

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）
- 📝 INFRA-34（待下輪）：/sv Step 10.5 Session Note 自動歸檔職責

## D10 KAIROS #3 候選議程（Pre-W1-B 登記）

| ID | 候選項目 | 來源 | 類型 |
| ---- | --------- | ------ | ------ |
| K3-1 | Debug Overlay Zero Tolerance | D8 FIX-01~03 | CLAUDE.md 規則 |
| K3-2 | Hardcoded Text Scanning | D8 FIX-01~03 | /sv gate 擴充 |
| K3-3 | KAIROS Real-Device Screenshot Requirements | D8 FIX-01~03 | KAIROS 節點流程 |
| K3-4 | Decommission 票刪除驗收必須用 tracked filename glob 驗證 | FIX-INFRA-02-D | CLAUDE.md 規則 |
| K3-5 | Ticket 驗收清單與 SSOT 衝突時，以合併規格源為裁決依據 | TICKET-01 | CLAUDE.md 規則 |
| K3-6 | `cross_review.sh` payload 驗證 SV scope ↔ diff scope 一致性 | cross_review forensic | 腳本改進 |
| K3-7 | Edit 前強制 Re-view（>200 行目標檔 / /compact 後無條件觸發） | K3-7 | CLAUDE.md 規則 ✅ Rule 18 |

## D13 KAIROS #4 候選議程（Pre-D13 登記）

| ID | 候選項目 | 來源 | 類型 |
| ---- | --------- | ------ | ------ |
| K4-21 | Scope Ban Wording Guideline → `.claude/rules/scope-ban-governance.md`（Perplexity 6 點框架：禁絕對語氣 / Net-New vs Name-New / 行內例外具名化 / 指標對應） | P08-F4-FIX BLOCKER + CSO Override | rules 文件 |
| K4-22 | ssot_context.md 事實數據（行數/倍率）隨 Ticket 閉環自動更新機制 | CameraViewModel 2457→2344 漂移 | 自動化 |
| K4-23 | 靜態驗收 + 模擬器 QA 無法捕捉真機專屬 Camera 後端分流 bug → execute_review 後仍 **MUST** 真機驗收方可標 DONE | P08-F4-FIX S15 真機 FAIL | CLAUDE.md 規則 |
| K4-24 | Discovery Report 已記載「Path A 從不執行」，FIX 卻扶正 Path A 未修根因 → FIX Ticket **MUST** 對 Discovery 前提缺陷逐項閉環 | P08-F4 Discovery vs P08-F4-FIX 矛盾 | CLAUDE.md 規則 |
| K4-34 | 真機渲染缺陷無法被靜態/模擬器捕捉 — Halo 牽引圓點在 SSOT「加亮弧」空實作下，靜態+單元全綠但真機完全不渲染 → 視覺渲染類 AC **MUST** 真機 gate（K4-23 由「Camera 後端」擴展至「渲染」維度）| STATE-CONTRACT-01 AC-6/8 靜態 PASS 真機 FAIL（GAP-06）| CLAUDE.md 規則 / camera-constraints |
| K4-35 | Guard 依賴的 input 若無 production producer（dead field），單元測試注入該 state 造成 false pass → reachability 維度 **MUST** 入 /qa+/sv | AC-10 假過 / afState 永 Idle / Guard 7a 死碼（GAP-07）| /qa+/sv gate 擴充（✅ 已轉 INFRA-REACH-01 票，本候選即其防護）|

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_6.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
