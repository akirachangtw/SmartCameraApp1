# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D15

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| GAP-11-PORTRAIT-EMA | ✅ **Done**（關票 · 已移 deliver/）· SSOT §2.5.1（:536-541）Portrait bbox EMA `last*0.7 + new*0.3` 補實作 · AI-1 常數家族 + AI-2 `PortraitEmaSmoother`（`smooth()`／`reset()`／companion `portraitEmaStep()` 單一真理源）+ AI-3 ContractTest C-ε1 · **A2**：Scope Ban BLOCKER→遷入 smoother 新檔，VM 淨新增函數=0 · **A5**：R2 domain 純度 BLOCKER→`ssot_context §1` 白名單收斂（RectF/PointF+Log）+ 移除 `@VisibleForTesting`（Postponed #11）· Cross-Review R3（腳本 R4）✅ PASS（REACHABILITY: NONE）· SC-01 血緣：對 `GuidanceStateConstants.kt`/`ContractTest.kt` 純新增（numstat 6/0、11/0，零碰撞）· 207/207 tests · `ab59067` · **兩顆 [實機] AC 併入 D19 實測腳本** |
| INFRA-SKILLS-UC02 | ✅ Done（碼閉環）· `uc/SKILL.md` Ticket DONE 改為 QA 交棒信號（AI-1）+ QA/SV Auto-Chain ownership 鎖定，`/uc` 不得直呼 `/sv`（AI-2）+ 非 DONE 動作停止語義保留（AI-3）· 25L diff（14+/11-）< MAX_LINES 45 · Codex Cross-Review R2 PASS（REACHABILITY: NONE，K5-Rule 29 誠實揭露 AC2-5 未實測）· `6b74eab` · 免 Traycer（純 prompt-layer 治理票，Codex PASS 即閉環）· **AC2-5[可達性/行為性] 待下次真實 `/uc` Ticket DONE 觸發實測後轉 PASS** |
| INFRA-HOOK-STOP-01 | ✅ Done（碼閉環）· `telegram_stop.sh` 消除 `set -euo pipefail` 靜默 `exit 1`：新增 `extract_ticket_id` 結構式 parser 取代舊 hardcoded family allowlist · 新增 `telegram_stop_test.sh` 4/4 PASS 無網路回歸測試 · 205/205 tests + 0 new lint + ELF ALIGNED · Codex Cross-Review R1 PASS（REACHABILITY: NONE）· `70a34ec`（code）+ `52e4b60`（PROGRESS_ARCHIVE.md S48）· **[實機] AC ✅ 已驗證（新 Session 確認 Stop hook error 未再出現，Akira 2026-07-14 回報）** |
| GAP-10-BREATH-RESTART | ✅ Done（碼閉環）· `HaloGuidanceLayer.kt` ptfBreathAlpha/normalBreathAlpha 兩處 `InfiniteRepeatableSpec` 補 `repeatMode = RepeatMode.Reverse`（比照既有 breathe-dot 範本）· 205/205 tests + 0 new lint + ELF ALIGNED · Codex Cross-Review R1 PASS（REACHABILITY: NONE）· `0acabd6`（code）+ `3e8d43e`（docs）· **[實機] AC 待 D17 Akira 呼吸真偽終審**（BREATH-KILL 預授權出口）· 關票待 Traycer |
| STATE-CONTRACT-05 | ✅ Done（碼閉環）· `HALO_RADIUS_SCALE` 0.6f→0.5f（SSOT §2.5.4，Traycer SC-04 R1 Fix 1 承接）· ContractTest C-ρ1 關係式（`2f*HALO_RADIUS_SCALE==1.0f`）取代裸數字快照 · K4-η 首輪 13 命中裁決終態（PTF ×3 `HaloGuidanceLayer.kt:264/276/291` register-out→已定性待 SC-06，Postponed #7）· `cad0ce6`（僅 `GuidanceStateConstants.kt` + `GuidanceStateContractTest.kt`）· 關票待 Traycer R2 |
| INFRA-AGENTS-CR-01 | ✅ Done（碼閉環）· `pre-modification-simulator` 重名 agent 退場（AI-1/AI-2 刪除 `-enhanced`/`-sonnet` 兩檔，消除 registry silent-collision）+ 新建 `cross-reviewer` R0 預審 subagent（AI-3，字面閘 5 職責，HC3 零 PASS 命中）+ `cross-review.md` R0 路由/三護欄補丁（AI-4）· assembleDebug PASS + 207/207 tests + 0 new lint + ELF ALIGNED · Codex Cross-Review R1 PASS（REACHABILITY: NONE）· `dd4c6ec` · **免 Traycer**（純 agent/rule 治理票，無 .kt/.xml，Codex PASS 即閉環，Akira 2026-07-15 裁決，比照 INFRA-SKILLS-UC02 前例）· **兩項[行為性] AC 已補測轉 PASS**（2026-07-15 同票內補測，Akira「授權即刻補測，免新票」裁決）：`Agent(subagent_type: "cross-reviewer")` 委派成功（agentId `a2728ada4acf22760`，registry 已熱重載）；GAP-11 R3 payload 回放（重建自 `bcf08c1`）3/3 domain Android import 命中字面閘（`RectF`/`Log`/`VisibleForTesting`），裁決 FAIL/BLOCKER，符合職責 4(a) 預期行為 → **8/8 AC 全數 PASS，完全關票** |

**Last Updated**: 2026-07-15

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S54** · GAP-06-TARGET-01-REVA 🟡 進行中（AI-1~5+A2-2 已實作：`RuleResult.targetHint`/`ThirdsRule` 幾何輸出/`CompositionResult` 聚合/`CameraScreen` 座標轉換/`HaloGuidanceLayer` GuidanceActive null-guard；AI-4 確認零改動——`CameraState.compositionResult` 既有 sibling 欄位已足；/qa PASS，待 /sv → Cross-Review → Traycer T2）· **S53** · INFRA-VERIFY-EXEMPT-01 ✅ Done（`verify_checklist.sh` PIPELINE_ARTIFACT 豁免補 PROGRESS_ARCHIVE.md · Codex R3 PASS · `e75d235`）· **S52** · INFRA-AGENTS-CR-01 ✅ Done（碼閉環，8/8 AC 全數 PASS，見 ARCHIVE）· **S51** · INFRA-63 ✅ Done（CLAUDE.md Rule 1 新增 AskUserQuestion 彈出視窗禁令 · `8328171`）· **S50** · GAP-11-PORTRAIT-EMA ✅ Done（`PortraitEmaSmoother` 單一真理源 · Cross-Review R3 PASS · `ab59067`）|
| Compile | ✅ PASS `assembleDebug`（GAP-06-TARGET-01-REVA · 2026-07-15，Device+Emulator 雙 flavor 皆過） |
| Tests | ✅ PASS `testDeviceDebugUnitTest` + `testEmulatorDebugUnitTest` 207/207（0 fail，雙 flavor） |
| Lint | ✅ `lintDeviceDebug` PASS，0 new warning（ALLOWED 檔案內 2 筆既有 Modifier-順序提示 `CameraScreen.kt:146`/`HaloGuidanceLayer.kt:154` 為 pre-existing，非本票 diff 觸及行）· static_blind_scan.sh 3 項警示同為 pre-existing 域外檔案 |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| GAP-06 | HaloTraction：GuidanceActive + Aligned 態圓點渲染已閉環（GAP-06-RENDER-DOT-01 ✅ Done）。**GAP-06-TARGET-01-REVA producer wiring 已實作**（`DIFF_REF` 校正 `873ef99`；ALLOWED 補 `ThirdsRule.kt`+`HaloGuidanceLayer.kt`，A2-2 授權；AI-1~5+A2-2 全數落地，working tree 待 commit）。/qa PASS（assembleDebug 雙 flavor + 207/207 tests），**待 /sv → Codex Cross-Review（R0 護欄二預授權）→ Traycer T2**。Normal vs Focus Ring 路由實機驗仍留 | 🟡 P1（producer 接線完成，剩驗收管線；GuidanceActive null-guard 已消除假對齊風險） | Aligned/GuidanceActive 消費斷鏈已修；剩：Cross-Review + 實機 AC（人臉偏離截圖 x2、假對齊風險驗證、topInstruction=HeadroomRule 無 targetHint 場景） |
| BUG-P08-F4-REAL-DEVICE | AC-4 🔴 橫向 detection 失效（BUG-LCSOURCE-HARDCODED-ROTATION）| 🔴 Hard Gate FAIL | Phase B 後開票 |
| BUG-LCSOURCE-HARDCODED-ROTATION | compensatedRotation 硬編碼 → 橫向偏差 | 🟡 P1 | 開 P08-F9 後處理 |

> **🟢 治理校正結案（2026-07-12）**：GAP-07 / AC-10（skip-when-focused / Guard 7a）**STATE-CONTRACT-03 已 Closed**（Traycer R4 Path A APPROVE · 錨 `7e99f28`）。詳見 `PROGRESS_ARCHIVE.md` S41。

## PROVISIONAL Ledger（防孤兒追蹤 · 2026-07-07 CSO 提案 Akira 授權）

| PROVISIONAL | Owner | 觸發事件 | 死線 | 孤兒警訊 / 原則 |
| ---- | ---- | ---- | ---- | ---- |
| §9 閾值三元組（SSOT v4.0.7 L1363） | Akira＋實測 | D15-16 餐廳實測 P25 校準（CR-7 腳本軌 A，純 IMU 遙測統計，Fable5 零介入） | D16 | **延測不硬測**：GAP-06/07 未閉 → Sprint 順延換乾淨數據，禁污染錨入檔（CSO 裁決 2026-07-07，Akira 核准） |
| GAP-06-TARGET（Rev A 進行中 · R-T21） | CSO | Rev A AI-1~5+A2-2 已實作：`CompositionResult.targetHint`（`ThirdsRule` 幾何來源）→ `CameraScreen` px 轉換 → `HaloGuidanceLayer.targetPosition` | Cross-Review + Traycer T2 通過後關票 | **A2-2 已消除舊風險**：`targetPosition==null` 時 GuidanceActive 圓點改為不渲染（非 falls-back 歸心），假對齊訊號已由 null-guard 阻斷；`targetPosition: Offset? = null` 仍為永久介面契約（SSOT L393） |
| Portrait composite target（SSOT L500） | CSO+Track B | A2-1 裁決：合成公式（thirds+headroom+gaze）判定規格缺口，MVP interim=thirds 單軌 | Track B 回收後 SSOT 修訂票 | 翻盤條件：感知樣本顯示單軌牽引方向誤導 |

## Next Actions

1. **🟡 引用修正支線**：使用者要求「`.claude/`、`plans/` 引用新版本路徑 v3.3」，已掃描分類（現行指標多數已於 GOV-03/PROTO-01 同步；`plans/cso_briefing/` 內 v3.2 引用屬 R-T5 化石溯源，Hard Constraint 3 **MUST** 不回改）→ **新 session 待使用者確認範圍或指定具體檔案後執行**
2. **✅ TRACTION-SSOT-01 → STATE-CONTRACT-04 ✅ Done**：SSOT §2.5/§2.5.1/§2.5.2 + Layout §3.3 規格已落地；ALIGNED 呼吸帶三常數家族已單次觸碰重錨（`ce9b232`，見上表）；GAP-06-TARGET producer wiring 已進行中（Rev A AI-1~5+A2-2 已實作，/qa PASS，見 Active Blockers）
3. **✅ STATE-CONTRACT-03（GAP-07）已 Closed**（Traycer R4 Path A APPROVE · 錨 `7e99f28`）；AC-10 轉 ✅；附帶發現之 verify_checklist glob bug 已由 INFRA-VERIFY-GLOB-01 修復（`db4106a`）
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