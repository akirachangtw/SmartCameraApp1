# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D13

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| GAP-06-RENDER-DOT-01 | ✅ Done · HaloGuidanceLayer 牽引圓點渲染（AI-1 computeTractionDot atan2/hypot + 三段 V 曲線色/alpha + drawTractionDot radialGradient · AI-2 HALO_DOT_* 6 常數 · AI-3 androidTest 5 條）· /qa 綠（Build/202 test/Lint 0 new）· Codex Cross-Review PASS（Guard Reachability 有新 Guard→Producer B 可達，附行號）· DIFF `2a5103b..feb1732` · targetPosition non-null producer = GAP-06-TARGET placeholder（R-T21 stub-and-report，永久 nullable 契約 SSOT L393）· ticket_pattern.md 拆獨立 `[INFRA-TICKETID-01]` 4392c51 窗外 |
| SSOT-V407-SWEEP-01 | ✅ Done · CLAUDE.md 現行 SSOT 指針 v4_0_6→v4_0_7（恰 2 處：核心鐵律 1 + `## SSOT` 策略行）· Codex Cross-Review PASS（REACHABILITY NONE）· assembleDebug PASS · AI-3 `.claude/rules/` v4_0_6 零命中 · Layout v2_5_1 零觸碰 · 6a08bed · 執行軌開機契約除雷（GAP-06 渲染票前置）· 已移 deliver/ |
| SSOT-V407-01 | ✅ Done · SSOT v4.0.6→v4.0.7 七簇條文（§8 Freemium 通道切割+創始買斷 / §9 雙漏斗 / §2.5.4 鎖定態 Halo / §2.6 呼吸韻律 / §11 十六天 / B.15 / B.16 敘事鏈）· Codex Cross-Review ADVISORY→PASS（REACHABILITY NONE · receipt 5714078）· footer 化石 `End of SSOT v4.0.6`→v4.0.7 amend 修正（a881096，+1 行）· AC-5「GUIDANCE_ACTIVE 值在 α0.8、provenance 允留」+ receipt/HEAD 綁定 均 CSO 批准補證 · 唯二 PROVISIONAL（§2.5.4 alpha vaccine / §9 閾值三元組）待回填 · 已移 deliver/ |
| INFRA-REACH-02 | ✅ Done · 9/9 AC PASS · Codex Cross-Review R2 ADVISORY（`REACHABILITY: NONE` · 46b8e24）+ 補跑正式 /qa→/sv 後 R2 ADVISORY（99a81de，structural_verify A-G PASS）· AC「第二 state holder 正向命中」由 Akira 裁決選項 (C)：合成 `tmp/` 臨時 fixture 自測（不落地 `app/src/main/**`），正向命中 `fakeMode` + `normalMode` 不誤報，見 `reports/REACH02_AI2_SelfTest_SecondHolder_20260706.md`；fixture 已清理 · AI-1~AI-7 七項 Action Item 皆完成 · 已移 deliver/ |
| INFRA-SKILLS-UC01 | ✅ Done · Codex Cross-Review PASS（R1，完整 cross_review.sh · Guard Reachability 無新 Guard）· `/uc` 新增「## 驗證性動作豁免」段落：零廢話只約束解說性輸出，執行 `.claude/**/*.sh` MUST 用注入〔腳本現況〕canonical 格式 or 讀腳本頭 Usage，MUST NOT 憑記憶組裝參數 · 既有約束（零過渡/純 Diff/DONE 終止）保留 · AC-4 .sh 零觸碰 · +8L · 47fe32d |

**Last Updated**: 2026-07-08

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S38** · GAP-06-RENDER-DOT-01 ✅ Done（HaloTraction 牽引圓點渲染+磁吸幾何落地 · Codex Cross-Review PASS · feb1732）· **S37** · SSOT-V407-SWEEP-01 ✅（6a08bed）· **S36** · SSOT-V407-01 ✅（a881096）· **S35** · INFRA-REACH-02 ✅（9/9 AC）· GAP-06 母議題剩磁吸 producer wiring（GAP-06-TARGET）+ Normal 渲染路由實機驗 |
| Compile | ✅ PASS `assembleDebug`（53s · GAP-06 圓點渲染 · 2026-07-08 /qa Step 0 Scope Gate PASS 後補跑） |
| Tests | ✅ PASS 202 unit（0 fail）+ androidTest compile（20s · GAP-06 AI-3 computeTractionDot V-curve/幾何 5 條） |
| Lint | ✅ PASS `lint`（0 new warning；1 既存 baseline `ModifierParameter`@HaloGuidanceLayer.kt:151，isPtfMode/ptfAlignmentResult 於 2a5103b 已在 modifier 前，targetPosition 依既有簽名慣例插入，非新品類） |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| GAP-06 | HaloTraction：圓點渲染+磁吸幾何已落地（GAP-06-RENDER-DOT-01 ✅ feb1732）；剩 producer wiring（圓點恆歸心 dist=0）+ Normal vs Focus Ring 渲染路由實機驗 | 🔴 P0 | render/math 完成；producer = GAP-06-TARGET placeholder（SC-04 閉環後開票）；路由待實機 |
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

> **S37（2026-07-07）：✅ SSOT-V407-SWEEP-01 Done — CLAUDE.md 現行 SSOT 指針對齊 v4_0_6→v4_0_7（恰 2 處）· Codex Cross-Review PASS · assembleDebug PASS · AI-3 `.claude/rules/` 零命中 · Layout v2_5_1 零觸碰 · 6a08bed · 執行軌開機契約除雷 → 票移 deliver/（S32~S34 Session Notes 歸檔至 PROGRESS_ARCHIVE.md，PROGRESS.md 壓回 ≤80）**
>
> **S36（2026-07-07）：✅ SSOT-V407-01 Done — SSOT v4.0.6→v4.0.7 七簇條文入檔（歸檔 #154-160+#163）· Codex Cross-Review ADVISORY→PASS（footer 化石 amend 修正）· AC-5 provenance 判讀 + receipt(5714078)/HEAD(a881096) 綁定經 CSO 批准補證 · 唯二 PROVISIONAL 待回填（見 PROVISIONAL Ledger）→ 票移 deliver/**
>
> **S35（2026-07-06）：✅ INFRA-REACH-02 Done（9/9 AC）— structural_verify A-G PASS（ADVISORY）；Akira 裁決 AC「第二 state holder 正向命中」選項 (C) 合成 `tmp/` fixture 自測 · 票移 deliver/**

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

## D10 KAIROS #3 候選議程

**Canonical Registry**：`plans/cso_briefing/project/Pending/KAIROS3_Candidate_Compilation.md`（K3-1~7，共 7 項；本節僅作指標，全量候選表已搬移至該檔）。

## D13 KAIROS #4 候選議程

**Canonical Registry**：`plans/cso_briefing/project/Pending/KAIROS4_Candidate_Compilation.md`（K4-1~50，共 50 項；本節僅作暫存區，新候選登記後即合併入該檔並移除本節條目）。

> INFRA-XX（S28）稽核：Compilation 檔內原標「✅ 收錄」的 K4-3/6/8/9 經 grep 核查目標檔案（`cross-review.md` / `kairos-lessons.md` / `camera-constraints.md`）均無對應條文，已改列 🔴 待補寫；`kairos-lessons.md` 確認為必要存在（唯一 path-scoped 自動載入的已落地規則本體，與 Compilation 的候選登記角色不重疊，見下方說明）。KAIROS-BACKFILL-01（S28）已補寫落地，假閉環佇列清空。

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_7.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
