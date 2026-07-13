# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D15

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| STATE-CONTRACT-05 | ✅ Done（碼閉環）· `HALO_RADIUS_SCALE` 0.6f→0.5f（SSOT §2.5.4，Traycer SC-04 R1 Fix 1 承接）· ContractTest C-ρ1 關係式（`2f*HALO_RADIUS_SCALE==1.0f`）取代裸數字快照 · K4-η 首輪 13 命中裁決終態（PTF ×3 `HaloGuidanceLayer.kt:264/276/291` register-out→已定性待 SC-06，Postponed #7）· `cad0ce6`（僅 `GuidanceStateConstants.kt` + `GuidanceStateContractTest.kt`）· 關票待 Traycer R2 |
| STATE-CONTRACT-04 | ✅ Done（碼閉環）· Halo alpha 三常數家族單次觸碰重錨：`HALO_ACTIVE_ALPHA` 0.25→0.8（R-T2）+ `HALO_ALIGNED_ALPHA_MIN/MAX` 0.10/0.18→0.25/0.40（R-T13 V-1）· ContractTest C-α1/α2/α3 關係式斷言取代裸數字快照（K4-54）· K4-η sweep 攔截清單外命中 ×2（`HaloGuidanceLayer.kt:96-97` PTF_BREATH / `CameraScreen.kt:580-581` 快門環）→ CSO 裁決域外零觸碰（Postponed #7 / #8+SC02-5）· /qa Step0 PASS + `testDeviceDebugUnitTest` 8/8 綠 + `assembleDeviceDebug` PASS · `ce9b232`（僅 `GuidanceStateConstants.kt` + `GuidanceStateContractTest.kt`）· 關票待 Traycer |
| SSOT-V407-LHVAC-01 | ✅ Done · LH-VAC-02 §2.5.4 alpha vaccine 回填（canonical 檔逐字取回，3 hunks · +15L）· V-1~V-4 + 白空間 + diff 口徑全綠 · `a0f6f13`（僅 `SSOT_v4_0_7.md`）· PROVISIONAL §2.5.4 切片歸零，觸發 SC-04 開工前置解禁 |
| SSOT-V407-SWEEP-01 | ✅ Done · CLAUDE.md 現行 SSOT 指針 v4_0_6→v4_0_7 · Codex Cross-Review PASS · `6a08bed` · 已移 deliver/ |
| SSOT-V407-01 | ✅ Done · SSOT v4.0.6→v4.0.7 七簇條文落地 · Codex Cross-Review PASS · `a881096` · 已移 deliver/ |
| INFRA-EOL-01 + INFRA-VERIFY-GLOB-01 | ✅ Done（治理域雙微票）· `.gitattributes` `*.md`/`*.sh eol=lf`（`f58ad29`）+ `verify_checklist.sh` glob 恢復 + `--self-test` 8/8（`db4106a`）· /qa + Codex R1 PASS · 遺留 L-1/L-2/D-1/D-2/C-1 待 CSO 裁（見 Next Actions） |

**Last Updated**: 2026-07-13

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S47** · GAP-10-BREATH-RESTART /qa PASS（uc AI-1+AI-2 · `HaloGuidanceLayer.kt` ptfBreathAlpha/normalBreathAlpha 補 `repeatMode = RepeatMode.Reverse` · +2L · 待 /sv）· **S46** · D15-16 實測診斷（無票 · Rule 4c 僅回報）→ 報告 `plans/cso_briefing/reports/D15-16/` · Track B 採樣暫停建議 + 6 項待裁決 · **S45** · STATE-CONTRACT-05 ✅ Done（`cad0ce6` · Halo 半徑重錨 + K4-η 終態 13 命中 · ledger `2ba2b83`）· **S44** · STATE-CONTRACT-04 ✅ Done（`ce9b232` · K4-η 域外裁決 Postponed #7/#8）· **S43** · SSOT-V407-LHVAC-01 ✅（`a0f6f13` · LH-VAC-02 回填）|
| Compile | ✅ PASS `assembleDebug`（GAP-10-BREATH-RESTART · 2026-07-13） |
| Tests | ✅ PASS `testDeviceDebugUnitTest` 205/205（0 fail · GAP-10-BREATH-RESTART，unrelated to breathe animation scope） |
| Lint | ✅ PASS `lintDeviceDebug`（0 new warning；1 既存 baseline `ModifierParameter`@HaloGuidanceLayer.kt:154） |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| GAP-06 | HaloTraction：GuidanceActive + **Aligned 態圓點渲染已閉環（GAP-06-RENDER-DOT-01 ✅ Done，已移 deliver/）**；母議題後續仍剩 producer wiring + Normal vs Focus Ring 路由實機驗 | 🔴 P0（D15-16 實測證實：producer 缺席 → 牽引點永釘中央 → 引導迴路死鎖，Track B 硬阻） | Aligned 消費斷鏈已修；剩 producer = GAP-06-TARGET placeholder（SC-04 閉環後下一收割節點）；路由待實機 |
| BUG-P08-F4-REAL-DEVICE | AC-4 🔴 橫向 detection 失效（BUG-LCSOURCE-HARDCODED-ROTATION）| 🔴 Hard Gate FAIL | Phase B 後開票 |
| BUG-LCSOURCE-HARDCODED-ROTATION | compensatedRotation 硬編碼 → 橫向偏差 | 🟡 P1 | 開 P08-F9 後處理 |

> **🟢 治理校正結案（2026-07-12）**：GAP-07 / AC-10（skip-when-focused / Guard 7a）**STATE-CONTRACT-03 已 Closed**（Traycer R4 Path A APPROVE · 錨 `7e99f28`）。詳見 `PROGRESS_ARCHIVE.md` S41。

## PROVISIONAL Ledger（防孤兒追蹤 · 2026-07-07 CSO 提案 Akira 授權）

| PROVISIONAL | Owner | 觸發事件 | 死線 | 孤兒警訊 / 原則 |
| ---- | ---- | ---- | ---- | ---- |
| §9 閾值三元組（SSOT v4.0.7 L1363） | Akira＋實測 | D15-16 餐廳實測 P25 校準（CR-7 腳本軌 A，純 IMU 遙測統計，Fable5 零介入） | D16 | **延測不硬測**：GAP-06/07 未閉 → Sprint 順延換乾淨數據，禁污染錨入檔（CSO 裁決 2026-07-07，Akira 核准） |
| GAP-06-TARGET（票 placeholder · R-T21 · 禁預賦 AC） | CSO | STATE-CONTRACT 系列收斂（SC-03/04 落地）後定 scope 開票；producer 接線：rule engine `CompositionAdvice.Adjust(dx,dy)` → guidance 管線 → `HaloGuidanceLayer.targetPosition`（必碰 CameraState/CameraScreen，SC 領土相鄰） | SC-04 閉環後下一收割節點 | producer 未接期間 null→歸心 dist=0 視覺近似「已對齊」假訊號 —— producer 票驗收**必引**此風險；`HaloGuidanceLayer.targetPosition: Offset? = null` 為永久介面契約（SSOT L393），後續票禁改簽名 |

## Next Actions

1. **🟡 引用修正支線**：使用者要求「`.claude/`、`plans/` 引用新版本路徑 v3.3」，已掃描分類（現行指標多數已於 GOV-03/PROTO-01 同步；`plans/cso_briefing/` 內 v3.2 引用屬 R-T5 化石溯源，Hard Constraint 3 **MUST** 不回改）→ **新 session 待使用者確認範圍或指定具體檔案後執行**
2. **✅ TRACTION-SSOT-01 → STATE-CONTRACT-04 ✅ Done**：SSOT §2.5/§2.5.1/§2.5.2 + Layout §3.3 規格已落地；ALIGNED 呼吸帶三常數家族已單次觸碰重錨（`ce9b232`，見上表）；GAP-06-TARGET producer wiring 為下一收割節點（見 Active Blockers）
3. **✅ STATE-CONTRACT-03（GAP-07）已 Closed**（Traycer R4 Path A APPROVE · 錨 `7e99f28`）；AC-10 轉 ✅；附帶發現之 verify_checklist glob bug 已由 INFRA-VERIFY-GLOB-01 修復（`db4106a`）
4. **🔴 待 CSO 裁決（治理驗證器）**：L-1 `[0.3]` 行數 awk 字面比對 → `MAX_LINES` 靜默失效（建議開後續票）· L-2 裸目錄前綴 FORBIDDEN token 語義（加分支 vs 判 malformed）· D-1/D-2/C-1（INFRA-EOL-01 遺留）· 本輪微票是否入 PROGRESS.md
5. **🔴 實機重測**：TRACTION-SSOT-01 修復後，AC-6/8/9 實機重測 → Traycer 終審
6. **🟡 STATE-CONTRACT-01 Traycer 終審**：FIX-A + FIX-B + FIX-C 均 /sv PASS，送終審
7. **🟢 TRACTION-SSOT-04 CSO 閉環**（技術已閉，待 CSO Ticket）：母票 line-91 AC box 打勾 · `move_file` Pending→deliver · Postponed #6（R-T14）→DONE / #5（RENDER-01）→ready · 編號對帳 K4-45（R-T18 stamp 前釐清 R-T17：overruled-void vs consumed）
8. **🔴 D15-16 實測報告 §6 剩餘待 CSO 裁決**（`plans/cso_briefing/reports/D15-16/D15-16_Field_Test_Findings_20260713.md`）：Food Halo 三層斷鏈（候選 GAP-08-FOOD-HALO）· EyeAF Failed 無 backoff 2s 重對焦迴圈（候選 GAP-09-EYEAF-BACKOFF）· Track B 採樣暫停 + Postponed #8 延後 · **GAP-10-BREATH-RESTART 已開票落地**（/qa PASS，待 /sv → D17 呼吸真偽終審，見 Ticket 檔尾 BREATH-KILL 預授權出口）

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）
- 📝 INFRA-34（待下輪）：/sv Step 10.5 Session Note 自動歸檔職責

## KAIROS Candidate Registries

- D10 #3：`plans/cso_briefing/project/Pending/KAIROS3_Candidate_Compilation.md`（K3-1~7）
- D13 #4：`plans/cso_briefing/project/Pending/KAIROS4_Candidate_Compilation.md`（K4-1~50，暫存區，新候選登記後合併入該檔）

## KAIROS Findings — D13 (2026-07-08)

**Error Patterns**：🔴 Family provenance 應於 spawn 當下登記，禁事後靠 M3 稽核補（GAP-06-RENDER-DOT-01 + INFRA-TICKETID-SSOT-V407-01 二度重演，見下方建議條文）· 🟡 reachability 自報行號 off-by-one（K4-ξ 已收錄）· 🟢 capability-present/behavior-absent 假完成風險（K4-ο `TAG_HALO_DOT` probe 範式已建立防護）
**Decision Audit**：R-T27 spawn PROGRESS-only ✅ · R-T28 verify_cmd 重建 ✅ · K4-δ 行號 295→296 ⚠️ 需微調（R1→R2 才修正，非首發即中）· SSOT-V407 family 收斂 ✅（長效性待觀察）· **SSOT Drift**：無新增，既有 PROVISIONAL（§2.5.4/§9）持續追蹤中見 PROVISIONAL Ledger
**Skill 壓測**：/qa /sv ✅（本 Sprint tmp/ ≥5 次 sv_report/log 證據）· `m3_family_audit.sh` ✅（本次 self-test+2 refresh+1 stale-check）· `/dream` 本次為 D13 首跑 · `/uc`/`/compact`/`/mdlint` 執行次數缺 telemetry，資訊不足無法判定壓測狀態

**CLAUDE.md Addition — 已核准併入**：Family Provenance Spawn-Time 登記規則已由 `INFRA-CLAUDE-RULE-HARVEST-01`（CSO 2026-07-09 裁決）正式併入 Rule 4a 本文，非新增第 13 條核心規則。觸發來源：GAP-06-RENDER-DOT-01、INFRA-TICKETID-SSOT-V407-01。

**Traycer strict_verification 最終閉環重跑（2026-07-09）**：Path A — APPROVE。GAP-06-RENDER-DOT-01 全 scope（實作 + closure + governance cleanup）確認可關閉，無新增修正項。**Governance Learnings**：①closure 票涉治理憲法層（CLAUDE.md）應即時獨立票化，不與 feature closure 綁定 ②KAIROS candidate 與 landed state 在 PROGRESS.md MUST 單向明確，禁止「待人工確認」與「已落地」雙真理並存 ③`INFRA-CLAUDE-RULE-HARVEST-01` 剝離 governance cleanup 之範式驗證有效，可作後續範本。

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_7.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`