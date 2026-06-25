# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| STATE-CONTRACT-01 | ✅ AI-1～AI-6 實作完成 · assembleDeviceDebug PASS · 20+9 tests PASS |
| PHASE-B-01-R4 | ✅ Traycer T5 PASS · Consumer Map Q1 + Review Summary stale-pointer 修正完成 |
| PHASE-B-01-R3 | ✅ RC2 repo-grounding 三紅燈全清（getFocusState / FocusIndicatorOverlay / CompositionGuideOverlay） |
| INFRA-59 | ✅ CLAUDE.md + gstack hook R-suffix 白名單補齊 |

**Last Updated**: 2026-06-25

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S22** · STATE-CONTRACT-01 ✅ 實作完成 · **待 /sv + Traycer + commit** |
| Compile | 0 errors ✅ (assembleDeviceDebug PASS) |
| Tests | 391 passed ✅ (EyeAfControllerTest 20 + GuidanceStateContractTest 9 新增) |
| Lint | 0 errors ✅ (0 new warnings in ALLOWED files) |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| BHA-01 | T02 Hard Gate — 歸一化 ✅，視覺 🔴 | 🟡 部分通過 | 視覺 FAIL：BUG-HALO-NOPTF-DEGENERATE → 已由 STATE-CONTRACT-01 閉環（六態渲染 + Aligned breathing）。 |
| BUG-P08-F4-REAL-DEVICE | AC-4 🔴 橫向 detection 失效（BUG-LCSOURCE-HARDCODED-ROTATION）· AC-5~9 ✅ | 🔴 Hard Gate FAIL | Phase B 後開票 |
| BUG-LCSOURCE-HARDCODED-ROTATION | compensatedRotation 硬編碼 → 橫向偏差 | 🟡 P1 | 開 P08-F9 後處理 |
| BUG-PTF-HALO-SIZE | PTF 圓環過大且不跟隨 | 🔴 P0 | P08-F7 待實作 |
| BUG-MLKIT-PAREIDOLIA-FP | Ghost Face | ✅ HOTFIX-47 PASS | GF-01~03 PASS |

## Next Actions

> **S22（2026-06-25）：STATE-CONTRACT-01 AI-1～AI-6 ✅ · 待 /sv + commit + 真機驗收**

1. **🔴 /sv**：STATE-CONTRACT-01 語意審查 + 確定性驗收
2. **🔴 Commit**：兩份 commit（.kt + .md 分開）
3. **🟡 真機驗收**：Bug #1 瘋狂對焦 / #2 Halo 消失 / #3 閃爍 確認閉環（K4-23 規則）
4. **🟡 STATE-CONTRACT-02**：legacy cleanup（`_compositionState` 刪除 / CompositionGuideOverlay 路由）

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

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_6.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
