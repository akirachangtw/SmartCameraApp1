# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D15

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| INFRA-HEADROOM-DOCSYNC-02-FIX-A | ✅ Done（碼閉環）· `tmp/Headroom/README.md` Verification commands 移除兩個 8787 active probe，改為讀取既有 `service-removal-after.txt`/`vscode-extensions-after.txt` decommission readback + 8788 說明句移除「active Headroom failure」措辭（承接 `INFRA-HEADROOM-DOCSYNC-02` commit `b6b199f` execute_review Minor finding）· 8L diff < MAX_LINES 20 · Codex Cross-Review R1 PASS（REACHABILITY: NONE）· `a1843e2` · 免 Traycer（Ticket 標註 Traycer Workflow: N/A，純文件治理票） |
| INFRA-SKILLS-UC02 | ✅ Done（碼閉環）· `uc/SKILL.md` Ticket DONE 改為 QA 交棒信號（AI-1）+ QA/SV Auto-Chain ownership 鎖定，`/uc` 不得直呼 `/sv`（AI-2）+ 非 DONE 動作停止語義保留（AI-3）· 25L diff（14+/11-）< MAX_LINES 45 · Codex Cross-Review R2 PASS（REACHABILITY: NONE，K5-Rule 29 誠實揭露 AC2-5 未實測）· `6b74eab` · 免 Traycer（純 prompt-layer 治理票，Codex PASS 即閉環）· **AC2-5[可達性/行為性] 待下次真實 `/uc` Ticket DONE 觸發實測後轉 PASS** |
| GAP-10-BREATH-RESTART | ✅ Done（碼閉環）· `HaloGuidanceLayer.kt` ptfBreathAlpha/normalBreathAlpha 兩處 `InfiniteRepeatableSpec` 補 `repeatMode = RepeatMode.Reverse`（比照既有 breathe-dot 範本）· 205/205 tests + 0 new lint + ELF ALIGNED · Codex Cross-Review R1 PASS（REACHABILITY: NONE）· `0acabd6`（code）+ `3e8d43e`（docs）· **[實機] AC 待 D17 Akira 呼吸真偽終審**（BREATH-KILL 預授權出口）· 關票待 Traycer |
| STATE-CONTRACT-05 | ✅ Done（碼閉環）· `HALO_RADIUS_SCALE` 0.6f→0.5f（SSOT §2.5.4，Traycer SC-04 R1 Fix 1 承接）· ContractTest C-ρ1 關係式（`2f*HALO_RADIUS_SCALE==1.0f`）取代裸數字快照 · K4-η 首輪 13 命中裁決終態（PTF ×3 `HaloGuidanceLayer.kt:264/276/291` register-out→已定性待 SC-06，Postponed #7）· `cad0ce6`（僅 `GuidanceStateConstants.kt` + `GuidanceStateContractTest.kt`）· 關票待 Traycer R2 |
| GAP-06-TARGET-01-REVA | ✅ Done（碼閉環）· 牽引點 targetPosition producer 接線：AI-1 `RuleResult.targetHint: PointF?`（CSO Amendment A5 型別裁決）+ AI-2 `ThirdsRule` 最近三分點幾何計算 + AI-3 `CompositionResult.targetHint` 聚合 + AI-4 `CameraViewModel` 零改動（既有 `analyzeCompositionUseCase` 呼叫鏈已足）+ AI-5 `CameraScreen` normalized→px 轉換 + A2-2 `HaloGuidanceLayer` GuidanceActive `targetPosition==null` null-guard（Aligned 分支零觸碰，單行 diff 機械驗證）· assembleDebug 雙 flavor PASS + 207/207 tests + 0 new lint · STALE_DIFF_REF 校正 `873ef99`→`058ca0c`（排除並行 INFRA-HEADROOM-* 提交汙染）· R0 FORWARD → Codex Cross-Review **ADVISORY**（`REACHABILITY: targetPosition:CameraViewModel.kt:2108`；`ThirdsRuleTest.kt`/`HaloGuidanceLayerTest.kt` 測試覆蓋缺口記錄為已知未覆蓋，非 BLOCKER）· `2f4a7c8`+`56982fd` · **A6 修補已完成**（unit tests×2 `AnalyzeCompositionUseCaseTest` PASS + `HaloGuidanceLayer.kt` KDoc comment-only，`git diff` 僅含註解行；`HaloGuidanceLayerTest.kt` 因無自動執行鏈觸發 A6-1 停止條件，未落地，已映射至既有 [實機] AC）· R0 CHECKPOINT（`.claude/PROGRESS.md` token 命中，Akira 裁決 TRUE-POSITIVE-RULE-GAP，Option A 放行，校準承接 `INFRA-AGENTS-CR-04`）→ Codex Cross-Review R1 PASS（effort=high，REACHABILITY: NONE）· 待 Traycer T2 回審（錨修補 commit，A6-4）· **關票前置**：血緣封口未解除（A6-5，`058ca0c..HEAD` 五筆無票 `INFRA-HEADROOM-*` commit 待裁決） |

**Last Updated**: 2026-07-16

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S56** · INFRA-HEADROOM-DOCSYNC-02-FIX-A ✅ Done（碼閉環，Verification commands decommission 對齊，Codex Cross-Review R1 PASS，免 Traycer，見上表）· **S55** · GAP-06-TARGET-01-REVA A6 修補 ✅ 碼閉環（unit tests×2 + KDoc comment-only，Codex Cross-Review R1 PASS，待 Traycer T2 回審）· **S54** · GAP-06-TARGET-01-REVA ✅ Done（碼閉環，Codex Cross-Review ADVISORY PASS，待 Traycer T2，見上表）· **S53** · INFRA-VERIFY-EXEMPT-01 ✅ Done（`verify_checklist.sh` PIPELINE_ARTIFACT 豁免補 PROGRESS_ARCHIVE.md · Codex R3 PASS · `e75d235`）· **S52** · INFRA-AGENTS-CR-01 ✅ Done（碼閉環，8/8 AC 全數 PASS，見 ARCHIVE）|
| Compile | ✅ PASS `assembleDebug`（INFRA-HEADROOM-DOCSYNC-02-FIX-A · 2026-07-16，doc-only diff，1s） |
| Tests | ✅ PASS `testEmulatorDebugUnitTest`（0 fail；doc-only diff 無新增測試） |
| Lint | ✅ 0 new warning · static_blind_scan.sh 4 項警示同為 pre-existing 域外檔案（`TapFocusRing.kt`/`HDRWarningOverlay.kt`/`CameraPreview.kt`/`SettingsScreen.kt`），非本票 diff 觸及 |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| GAP-06 | HaloTraction：GuidanceActive + Aligned 態圓點渲染已閉環（GAP-06-RENDER-DOT-01 ✅ Done）。**GAP-06-TARGET-01-REVA producer wiring 碼閉環**（AI-1~5+A2-2 全數落地，`2f4a7c8`+`56982fd`；Codex Cross-Review ADVISORY PASS）+ **A6 修補碼閉環**（unit tests×2 PASS + KDoc comment-only，Codex Cross-Review R1 PASS），**待 Traycer T2 回審**。Normal vs Focus Ring 路由實機驗仍留 | 🟡 P1（producer 接線+A6 修補完成，剩終審；GuidanceActive null-guard 已消除假對齊風險） | Aligned/GuidanceActive 消費斷鏈已修；A6 測試缺口已補（`HaloGuidanceLayerTest.kt` 三場景因無自動執行鏈未落地，映射至下列 [實機] AC）；剩：Traycer T2 回審 + 實機 AC（人臉偏離截圖 x2、假對齊風險驗證、topInstruction=HeadroomRule 無 targetHint 場景） |
| BUG-P08-F4-REAL-DEVICE | AC-4 🔴 橫向 detection 失效（BUG-LCSOURCE-HARDCODED-ROTATION）| 🔴 Hard Gate FAIL | Phase B 後開票 |
| BUG-LCSOURCE-HARDCODED-ROTATION | compensatedRotation 硬編碼 → 橫向偏差 | 🟡 P1 | 開 P08-F9 後處理 |

## PROVISIONAL Ledger（防孤兒追蹤 · 2026-07-07 CSO 提案 Akira 授權）

| PROVISIONAL | Owner | 觸發事件 | 死線 | 孤兒警訊 / 原則 |
| ---- | ---- | ---- | ---- | ---- |
| §9 閾值三元組（SSOT v4.0.7 L1363） | Akira＋實測 | D15-16 餐廳實測 P25 校準（CR-7 腳本軌 A，純 IMU 遙測統計，Fable5 零介入） | D16 | **延測不硬測**：GAP-06/07 未閉 → Sprint 順延換乾淨數據，禁污染錨入檔（CSO 裁決 2026-07-07，Akira 核准） |
| GAP-06-TARGET（Rev A 碼閉環 · R-T21） | CSO | Rev A AI-1~5+A2-2 已實作+Cross-Review ADVISORY PASS：`CompositionResult.targetHint`（`ThirdsRule` 幾何來源）→ `CameraScreen` px 轉換 → `HaloGuidanceLayer.targetPosition` | Traycer T2 通過後關票 | **A2-2 已消除舊風險**：`targetPosition==null` 時 GuidanceActive 圓點改為不渲染（非 falls-back 歸心），假對齊訊號已由 null-guard 阻斷；`targetPosition: Offset? = null` 仍為永久介面契約（SSOT L393） |
| Portrait composite target（SSOT L500） | CSO+Track B | A2-1 裁決：合成公式（thirds+headroom+gaze）判定規格缺口，MVP interim=thirds 單軌 | Track B 回收後 SSOT 修訂票 | 翻盤條件：感知樣本顯示單軌牽引方向誤導 |

## Next Actions

1. **🟡 引用修正支線**：使用者要求「`.claude/`、`plans/` 引用新版本路徑 v3.3」，已掃描分類（現行指標多數已於 GOV-03/PROTO-01 同步；`plans/cso_briefing/` 內 v3.2 引用屬 R-T5 化石溯源，Hard Constraint 3 **MUST** 不回改）→ **新 session 待使用者確認範圍或指定具體檔案後執行**
2. **✅ TRACTION-SSOT-01 → STATE-CONTRACT-04 ✅ Done**：SSOT §2.5/§2.5.1/§2.5.2 + Layout §3.3 規格已落地；ALIGNED 呼吸帶三常數家族已單次觸碰重錨（`ce9b232`，見上表）；GAP-06-TARGET-01-REVA 碼閉環，待 Traycer T2（見 Active Blockers）
4. **🔴 待 CSO 裁決（治理驗證器）**：L-1 `[0.3]` 行數 awk 字面比對 → `MAX_LINES` 靜默失效（建議開後續票）· L-2 裸目錄前綴 FORBIDDEN token 語義（加分支 vs 判 malformed）· D-1/D-2/C-1（INFRA-EOL-01 遺留）· 本輪微票是否入 PROGRESS.md
5. **🔴 實機重測**：TRACTION-SSOT-01 修復後，AC-6/8/9 實機重測 → Traycer 終審
6. **🟡 STATE-CONTRACT-01 Traycer 終審**：FIX-A + FIX-B + FIX-C 均 /sv PASS，送終審
7. **🟢 TRACTION-SSOT-04 CSO 閉環**（技術已閉，待 CSO Ticket）：母票 line-91 AC box 打勾 · `move_file` Pending→deliver · Postponed #6（R-T14）→DONE / #5（RENDER-01）→ready · 編號對帳 K4-45（R-T18 stamp 前釐清 R-T17：overruled-void vs consumed）
8. **🔴 D15-16 實測報告 §6 剩餘待 CSO 裁決**（`plans/cso_briefing/reports/D15-16/D15-16_Field_Test_Findings_20260713.md`）：Food Halo 三層斷鏈（候選 GAP-08-FOOD-HALO）· EyeAF Failed 無 backoff 2s 重對焦迴圈（候選 GAP-09-EYEAF-BACKOFF）· Track B 採樣暫停 + Postponed #8 延後 · **GAP-10-BREATH-RESTART ✅ Done**（/qa+/sv+Codex Cross-Review 全 PASS，待 D17 Akira 呼吸真偽終審，見 Ticket 檔尾 BREATH-KILL 預授權出口；關票待 Traycer）

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）
- 📝 INFRA-34（待下輪）：/sv Step 10.5 Session Note 自動歸檔職責

## KAIROS Candidate Registries

- D10 #3：`plans/cso_briefing/project/Pending/KAIROS3_Candidate_Compilation.md`（K3-1~7）
- D13 #4：`plans/cso_briefing/project/Pending/KAIROS4_Candidate_Compilation.md`（K4-1~50，暫存區，新候選登記後合併入該檔）

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_7.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`