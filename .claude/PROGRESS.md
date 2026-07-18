# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D15

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| INFRA-CLOSURE-SOP-01 | ✅ Done（碼閉環）· `ssot-governance.md` 增列「Ticket Closure 紀律」節：勾選/搬移/修補票三條 MUST（AI-1）· +8L < MAX_LINES 40 · Codex Cross-Review R1/R2 REPORT_FIX（sv_report 措辭/token 修正）→R3 **PASS**（REACHABILITY: NONE）· `9032723` · 免 Traycer（純 .md 治理票）· **首次 dogfood**（INFRA-AGENTS-CR-02 收尾）AC 3/3 附證據 PASS，票檔已搬 `deliver/` |
| INFRA-AGENTS-CR-02 | ✅ Done（碼閉環，AC 5/5 附 read-back 證據）· `cross-review.md` R0 送審記錄落盤條款：`.tmp/fail-advisor/R0_[TICKET]_[YYYYMMDD_HHMMSS].md` 六欄位格式 + `RETAIN_N=20` FIFO housekeeping + `.tmp/` 非版控紀律（AI-1）· `d8ecabd` · Codex R2 PASS · **AC2/AC3[行為性] 已轉 PASS**（R0 委派記錄 + 21 筆佔位 housekeeping 實測，證據見票檔）· 免開修補票（Akira 2026-07-17 裁決 B，無未閉環 AC）· 票檔已搬 `deliver/` |
| INFRA-GSTACK-D15-SYNC-01-FIX-A | ✅ Done（治理同步，AC 7/7 附證據）· Headroom 退場閉環同步（2026-07-16 退場日期）：Batch 1 兩票 execute_review PASS——`INFRA-HEADROOM-EVIDENCE-CLOSURE-01`（R2 PASS）+ `INFRA-HEADROOM-DOCSYNC-02`（R1 PASS，`b6b199f`）· 後續 `FIX-A`（`a1843e2`）+ `FIX-A-CLEANUP`（`9a3efc4`）· 人工 readback **[實機] PASS** · KAIROS D15 回顧見 `KAIROS_D15_Headroom_Decommission_Retrospective.md`（gitignored）· `484fc11` · 票檔已搬 `deliver/` |
| INFRA-SKILLS-UC02 | ✅ Done（碼閉環）· `uc/SKILL.md` Ticket DONE 改為 QA 交棒信號（AI-1）+ QA/SV Auto-Chain ownership 鎖定，`/uc` 不得直呼 `/sv`（AI-2）+ 非 DONE 動作停止語義保留（AI-3）· 25L diff（14+/11-）< MAX_LINES 45 · Codex Cross-Review R2 PASS（REACHABILITY: NONE，K5-Rule 29 誠實揭露 AC2-5 未實測）· `6b74eab` · 免 Traycer（純 prompt-layer 治理票，Codex PASS 即閉環）· **AC2-5[可達性/行為性] 待下次真實 `/uc` Ticket DONE 觸發實測後轉 PASS** |
| GAP-10-BREATH-RESTART | ✅ Done（碼閉環）· `HaloGuidanceLayer.kt` ptfBreathAlpha/normalBreathAlpha 兩處 `InfiniteRepeatableSpec` 補 `repeatMode = RepeatMode.Reverse`（比照既有 breathe-dot 範本）· 205/205 tests + 0 new lint + ELF ALIGNED · Codex Cross-Review R1 PASS（REACHABILITY: NONE）· `0acabd6`（code）+ `3e8d43e`（docs）· **[實機] AC 待 D17 Akira 呼吸真偽終審**（BREATH-KILL 預授權出口）· 關票待 Traycer |

**Last Updated**: 2026-07-18

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S60** · INFRA-AGENTS-CR-04 ✅ 碼閉環（AI-1~3 全數落地：cross-reviewer.md 職責 1 條件豁免三要件 + FORWARD 附註分支、cross-review.md 增列 `TRUE-POSITIVE-RULE-GAP` 三分類定義 + `CLASSIFICATION` 記錄欄位；+7L < MAX_LINES 120；待 R0/Codex Cross-Review）· **S59** · INFRA-CLOSURE-SOP-01 ✅ Done（碼閉環，`ssot-governance.md` 增列 Ticket Closure 紀律，+8L，Codex R3 PASS，首次 dogfood 收尾 CR-02，免 Traycer）· **S58** · INFRA-AGENTS-CR-02 ✅ Done（AC 5/5 附證據，`cross-review.md` R0 記錄落盤條款，`d8ecabd`，Codex R2 PASS）· **S57** · INFRA-64 母票終審結案 ✅ Done（`INFRA-HEADROOM-DECOMMISSION-01` 第三次終審 PASS，`daeee26`）· **S56** · INFRA-PROGRESS-COMPRESS-01 票據收尾 ✅ Done（checkbox 全勾＋執行記錄補交＋5 探針彙整全 PASS）|
| Compile | ✅ PASS `assembleDebug`（INFRA-AGENTS-CR-04 · 2026-07-18，54s） |
| Tests | ✅ PASS `testDeviceDebugUnitTest` + `testEmulatorDebugUnitTest` 209/209（0 fail，雙 flavor；本票零 .kt 觸碰） |
| Lint | ✅ `lintDeviceDebug` PASS，0 new warning（純 .md 治理票，lint 不掃描）· static_blind_scan.sh 3 項警示同為 pre-existing 域外檔案，非本票 diff 觸及 |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| GAP-06 | HaloTraction：GuidanceActive + Aligned 態圓點渲染已閉環（GAP-06-RENDER-DOT-01 ✅ Done）。**GAP-06-TARGET-01-REVA producer wiring 碼閉環**（AI-1~5+A2-2 全數落地，`2f4a7c8`+`56982fd`；Codex Cross-Review ADVISORY PASS）+ **A6 修補碼閉環**（unit tests×2 PASS + KDoc comment-only），**待 R0/Codex 快審 → Traycer T2 回審**。Normal vs Focus Ring 路由實機驗仍留 | 🟡 P1（producer 接線+A6 修補完成，剩終審；GuidanceActive null-guard 已消除假對齊風險） | Aligned/GuidanceActive 消費斷鏈已修；A6 測試缺口已補（`HaloGuidanceLayerTest.kt` 三場景因無自動執行鏈未落地，映射至下列 [實機] AC）；剩：R0/Codex 快審 + Traycer T2 回審 + 實機 AC（人臉偏離截圖 x2、假對齊風險驗證、topInstruction=HeadroomRule 無 targetHint 場景） |
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
9. **⚠️ 待 Akira 核驗**：GAP-06-TARGET-01-REVA 曾於 2026-07-16 執行 Codex Cross-Review R1 PASS（`b6b199f..5d8cc36`），但該結果與 `.tmp/` 佐證檔現已不存在（疑遭 2026-07-17 後續治理票提交覆蓋/housekeeping 清空）；本表暫不回寫該 PASS 狀態，詳見 `SESSION_HANDOFF_20260718_1421.md`
10. **🟡 INFRA-AGENTS-CR-04**：AI-1~3 碼閉環（`DIFF_REF=a596df5`，`/qa` PASS：assembleDebug + 209/209 tests×2 + lintDeviceDebug 0 new warning），待 R0/Codex Cross-Review → `/sv`

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