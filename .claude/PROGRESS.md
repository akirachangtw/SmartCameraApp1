# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D13

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| GAP-06-RENDER-DOT-01 | ✅ Done · FIX-A 四閘全綠 · AI-1 Aligned 分支疊加 dot Canvas（primaryFace + drawTractionDot L383 + `TAG_HALO_DOT` probe，綠環路徑零觸碰）· AI-2 androidTest +4（軸向四象限 4/4 + T-3 persistence 正負 + T-4 R-T13 V-3 解耦）· 本地 /qa 綠（assembleEmulatorDebug + 202 unit + lint 0 new + **instrumented 21/0/0**）· Codex R2 **PASS**（REACHABILITY: NONE · producer B `CameraScreen.kt:296` · receipt 194528）· structural_verify **ADVISORY**（A-G）· **Traycer R2 review-gate PASS**· DIFF `d04fe4a..504d49a`（恰 2 .kt · spawn R-T27 案 A · conf R-T28 重建）· targetPosition = GAP-06-TARGET placeholder（R-T21 nullable 永久契約 SSOT L393，未擴大；母議題渲染 scope 已閉，producer wiring 另案）· 已移 deliver/（Akira 2026-07-09 授權「GAP-06 CLOSURE 可關閉」）|
| SSOT-V407-SWEEP-01 | ✅ Done · CLAUDE.md 現行 SSOT 指針 v4_0_6→v4_0_7（恰 2 處：核心鐵律 1 + `## SSOT` 策略行）· Codex Cross-Review PASS（REACHABILITY NONE）· assembleDebug PASS · AI-3 `.claude/rules/` v4_0_6 零命中 · Layout v2_5_1 零觸碰 · 6a08bed · 執行軌開機契約除雷（GAP-06 渲染票前置）· 已移 deliver/ |
| SSOT-V407-01 | ✅ Done · SSOT v4.0.6→v4.0.7 七簇條文（§8 Freemium 通道切割+創始買斷 / §9 雙漏斗 / §2.5.4 鎖定態 Halo / §2.6 呼吸韻律 / §11 十六天 / B.15 / B.16 敘事鏈）· Codex Cross-Review ADVISORY→PASS（REACHABILITY NONE · receipt 5714078）· footer 化石 `End of SSOT v4.0.6`→v4.0.7 amend 修正（a881096，+1 行）· AC-5「GUIDANCE_ACTIVE 值在 α0.8、provenance 允留」+ receipt/HEAD 綁定 均 CSO 批准補證 · 唯二 PROVISIONAL（§2.5.4 alpha vaccine / §9 閾值三元組）待回填 · 已移 deliver/ |
| INFRA-REACH-02 | ✅ Done · 9/9 AC PASS · Codex Cross-Review R2 ADVISORY（`REACHABILITY: NONE` · 46b8e24）+ 補跑正式 /qa→/sv 後 R2 ADVISORY（99a81de，structural_verify A-G PASS）· AC「第二 state holder 正向命中」由 Akira 裁決選項 (C)：合成 `tmp/` 臨時 fixture 自測（不落地 `app/src/main/**`），正向命中 `fakeMode` + `normalMode` 不誤報，見 `reports/REACH02_AI2_SelfTest_SecondHolder_20260706.md`；fixture 已清理 · AI-1~AI-7 七項 Action Item 皆完成 · 已移 deliver/ |
| INFRA-SKILLS-UC01 | ✅ Done · Codex Cross-Review PASS（R1，完整 cross_review.sh · Guard Reachability 無新 Guard）· `/uc` 新增「## 驗證性動作豁免」段落：零廢話只約束解說性輸出，執行 `.claude/**/*.sh` MUST 用注入〔腳本現況〕canonical 格式 or 讀腳本頭 Usage，MUST NOT 憑記憶組裝參數 · 既有約束（零過渡/純 Diff/DONE 終止）保留 · AC-4 .sh 零觸碰 · +8L · 47fe32d |

**Last Updated**: 2026-07-09

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S40** · GAP-06-RENDER-DOT-01 ✅ Done（CLOSURE-01 三項全解：PROGRESS 同步 + D13 KAIROS /dream + deliver/ 移票）· **S39** · FIX-A 🟢 四閘全綠（/qa 21/0/0 → Codex R2 PASS → structural ADVISORY → Traycer R2 PASS）· **S38** · FIX-A spawn（母票 Traycer R1 STOP 修補）· **S37** · SSOT-V407-SWEEP-01 ✅（6a08bed）· **S36** · SSOT-V407-01 ✅（a881096）· GAP-06 母議題剩磁吸 producer wiring（GAP-06-TARGET）+ Normal 渲染路由實機驗 |
| Compile | ✅ PASS `assembleDebug`（53s · GAP-06 圓點渲染 · 2026-07-08 /qa Step 0 Scope Gate PASS 後補跑） |
| Tests | ✅ PASS 202 unit（0 fail）+ androidTest compile（20s · GAP-06 AI-3 computeTractionDot V-curve/幾何 5 條） |
| Lint | ✅ PASS `lint`（0 new warning；1 既存 baseline `ModifierParameter`@HaloGuidanceLayer.kt:151，isPtfMode/ptfAlignmentResult 於 2a5103b 已在 modifier 前，targetPosition 依既有簽名慣例插入，非新品類） |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| GAP-06 | HaloTraction：GuidanceActive + **Aligned 態圓點渲染已閉環（GAP-06-RENDER-DOT-01 ✅ Done，已移 deliver/）**；母議題後續仍剩 producer wiring + Normal vs Focus Ring 路由實機驗 | 🟡 P1 | Aligned 消費斷鏈已修；剩 producer = GAP-06-TARGET placeholder（磁吸接線）；路由待實機 |
| GAP-07 | afState 回授斷裂 → Guard 7a 死碼 → 瘋狂 AF（Focus Ring 閃動）（**=AC-10 假過根因**）| 🔴 P0 | CameraViewModel.kt L2117 result 被拋棄、afState 永 Idle（dead field）；需接線 |
| BUG-P08-F4-REAL-DEVICE | AC-4 🔴 橫向 detection 失效（BUG-LCSOURCE-HARDCODED-ROTATION）| 🔴 Hard Gate FAIL | Phase B 後開票 |
| BUG-LCSOURCE-HARDCODED-ROTATION | compensatedRotation 硬編碼 → 橫向偏差 | 🟡 P1 | 開 P08-F9 後處理 |

> **🟠 治理校正（2026-06-30）**：AC-10（skip-when-focused / Guard 7a）由 ✅ 改 **⚠️ PARTIAL** — unit logic 已實作測試，但 production input `AfState.Locked` 為 dead field（不可達），三道 gate 結構性假過。實際修復 = GAP-07；流程防護 = INFRA-REACH-01（已開票 Pending/）。

## PROVISIONAL Ledger（防孤兒追蹤 · 2026-07-07 CSO 提案 Akira 授權）

| PROVISIONAL | Owner | 觸發事件 | 死線 | 孤兒警訊 / 原則 |
| ---- | ---- | ---- | ---- | ---- |
| §2.5.4 alpha vaccine（SSOT v4.0.7 L678） | UITA | UITA 退回單返回（live 錨點 0.8 / LIMITED 0.35 / 呼吸帶 0.25→0.40 重推導）→ CSO 驗收回填；**回件後連動觸發 STATE-CONTRACT-04（placeholder · R-T20+R-T22 · 禁預賦 AC）：GuidanceStateConstants 三常數家族單次觸碰重錨 —— HALO_ALIGNED_ALPHA_MIN/MAX（0.10/0.18→呼吸帶）+ HALO_ACTIVE_ALPHA（0.25→未對齊 live 錨，SSOT L397-399/L548 規定 0.8，待 UITA 回件定值）+ ContractTest assert + SSOT 錨同步；盤點 HaloGuidanceLayer L283 `× 0.5f` 弱化環派生消費點（語意隨新錨重映射，禁遺孤）** | D14（Halo 教學 0.5 天前定稿） | UITA 逾 48h 無回件即升級 CSO；回件前任何票禁重錨呼吸帶（GAP-06 Rev B 已 FORBIDDEN 隔離） |
| §9 閾值三元組（SSOT v4.0.7 L1363） | Akira＋實測 | D15-16 餐廳實測 P25 校準（CR-7 腳本軌 A，純 IMU 遙測統計，Fable5 零介入） | D16 | **延測不硬測**：GAP-06/07 未閉 → Sprint 順延換乾淨數據，禁污染錨入檔（CSO 裁決 2026-07-07，Akira 核准） |
| GAP-06-TARGET（票 placeholder · R-T21 · 禁預賦 AC） | CSO | STATE-CONTRACT 系列收斂（SC-03/04 落地）後定 scope 開票；producer 接線：rule engine `CompositionAdvice.Adjust(dx,dy)` → guidance 管線 → `HaloGuidanceLayer.targetPosition`（必碰 CameraState/CameraScreen，SC 領土相鄰） | SC-04 閉環後下一收割節點 | producer 未接期間 null→歸心 dist=0 視覺近似「已對齊」假訊號 —— producer 票驗收**必引**此風險；`HaloGuidanceLayer.targetPosition: Offset? = null` 為永久介面契約（SSOT L393），後續票禁改簽名 |

## Next Actions

> **S40（2026-07-09）：✅ GAP-06-RENDER-DOT-01 全鏈結案 — Rule 4a 併入 Family Provenance Spawn-Time 登記規則（CLAUDE.md）+ 母票/FIX-A AC 全數打勾 + 兩票移 deliver/（Akira 授權「GAP-06 CLOSURE 可關閉」）· GAP-06 母議題（producer wiring + 路由實機驗）仍列 Active Blockers**

1. **🟡 引用修正支線**：使用者要求「`.claude/`、`plans/` 引用新版本路徑 v3.3」，已掃描分類（現行指標多數已於 GOV-03/PROTO-01 同步；`plans/cso_briefing/` 內 v3.2 引用屬 R-T5 化石溯源，Hard Constraint 3 **MUST** 不回改）→ **新 session 待使用者確認範圍或指定具體檔案後執行**
2. **✅ TRACTION-SSOT-01**（/sv + Codex R2 PASS · fc6ffa8）：SSOT §2.5/§2.5.1/§2.5.2 + Layout §3.3 規格已落地 → **GAP-06 票已 spawn（Rev D · R-T22）**：圓點實作進行中，ALIGNED 呼吸帶雙 SSOT 對撞讓渡 **STATE-CONTRACT-04（placeholder · 掛 UITA D14 回件前置 Gate · 禁預賦 AC）**；targetPosition UPSTREAM-GAP 判 stub-and-report（R-T21 · nullable 永久契約），上游接線登記 **GAP-06-TARGET placeholder（禁預賦 AC · 見 PROVISIONAL Ledger）**；AI-2 Rev D 二度縮編（R-T22）：HALO_UNALIGNED_ALPHA 撤票（消費點被 FORBIDDEN 領土雙點佔據 + 0.8 在 UITA D14 重推導清單），轉圓點域 HALO_DOT_* 常數；環域 0.25→0.8 併入 SC-04 三常數家族
3. **🔴 開票 GAP-07**：修 afState 回授斷裂（CameraViewModel.kt L2117 接線）
4. **🔴 實機重測**：TRACTION-SSOT-01 修復後，AC-6/8/9 實機重測 → Traycer 終審
5. **🟡 STATE-CONTRACT-01 Traycer 終審**：FIX-A + FIX-B + FIX-C 均 /sv PASS，送終審
6. **🟢 TRACTION-SSOT-04 CSO 閉環**（技術已閉，待 CSO Ticket）：母票 line-91 AC box 打勾 · `move_file` Pending→deliver · Postponed #6（R-T14）→DONE / #5（RENDER-01）→ready · 編號對帳 K4-45（R-T18 stamp 前釐清 R-T17：overruled-void vs consumed）

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）
- 📝 INFRA-34（待下輪）：/sv Step 10.5 Session Note 自動歸檔職責

## KAIROS Candidate Registries

- D10 #3：`plans/cso_briefing/project/Pending/KAIROS3_Candidate_Compilation.md`（K3-1~7）
- D13 #4：`plans/cso_briefing/project/Pending/KAIROS4_Candidate_Compilation.md`（K4-1~50，暫存區，新候選登記後合併入該檔）

> INFRA-XX（S28）稽核：K4-3/6/8/9 經 grep 核查目標檔案均無對應條文，已改列 🔴 待補寫；KAIROS-BACKFILL-01（S28）已補寫落地。

## KAIROS Findings — D13 (2026-07-08)

**Error Patterns**：🔴 Family provenance 應於 spawn 當下登記，禁事後靠 M3 稽核補（GAP-06-RENDER-DOT-01 + INFRA-TICKETID-SSOT-V407-01 二度重演，見下方建議條文）· 🟡 reachability 自報行號 off-by-one（K4-ξ 已收錄）· 🟢 capability-present/behavior-absent 假完成風險（K4-ο `TAG_HALO_DOT` probe 範式已建立防護）
**Decision Audit**：R-T27 spawn PROGRESS-only ✅ · R-T28 verify_cmd 重建 ✅ · K4-δ 行號 295→296 ⚠️ 需微調（R1→R2 才修正，非首發即中）· SSOT-V407 family 收斂 ✅（長效性待觀察）· **SSOT Drift**：無新增，既有 PROVISIONAL（§2.5.4/§9）持續追蹤中見 PROVISIONAL Ledger
**Skill 壓測**：/qa /sv ✅（本 Sprint tmp/ ≥5 次 sv_report/log 證據）· `m3_family_audit.sh` ✅（本次 self-test+2 refresh+1 stale-check）· `/dream` 本次為 D13 首跑 · `/uc`/`/compact`/`/mdlint` 執行次數缺 telemetry，資訊不足無法判定壓測狀態

**CLAUDE.md Addition — 已核准併入**：Family Provenance Spawn-Time 登記規則已由 `INFRA-CLAUDE-RULE-HARVEST-01`（CSO 2026-07-09 裁決）正式併入 Rule 4a 本文，非新增第 13 條核心規則。觸發來源：GAP-06-RENDER-DOT-01、INFRA-TICKETID-SSOT-V407-01。

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_7.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
