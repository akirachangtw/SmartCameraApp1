# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| TRACTION-SSOT-05 | ✅ Done · Codex 跨族窄審 PASS（手動窄審繞過 cross_review.sh `:!plans/` 排除 bug，真實 diff be90488..9af1223 注入；Guard Reachability=無新 Guard · Findings none）· Level-D9 語義清掃：§2.2 共存矛盾改寫（引 D-9 不重述）+ §4.3 scoring-only 疫苗（consumer=FoodCompositionScore + R-T5 鏡像）+ §8 詞族豁免註腳（不改名 over-sweep 防線）· 禁區零觸碰（§4.2 prioritize Kotlin / Layout / app/src）· Build/Test/Lint N/A（純 spec .md）· 9af1223 · ⚠️ 衍生 INFRA（Option B）：cross_review.sh `:!plans/` 排除使 plans/-scoped 票 diff 歸零 |
| TRACTION-SSOT-04 | ✅ Done · Codex Cross-Review PASS（複合路徑：R1[S-1/3/5/6] ⊕ R-T18[S-2 具名排除，L481 D-3 重錨敘述行 = 定義性 false-positive] ⊕ FIX-A b8rxia4a9[S-4 消除]）· 對齊態可見性修正（D-3 值重錨 0.25→0.40 + §2.0 alpha 組合語義條款 + §2.3/§2.5 圓點強存在 scale 1.1 統一 + V 曲線 0.35+x×0.5 收斂）· 母體 515d3cc+669cc4a、FIX-A 565e03e · 禁區零觸碰（未對齊 0.8 R-T2/R-T9 / L481 D-3 重錨行）· Build/Test/Lint N/A（純 spec .md）|
| INFRA-SV-STATE-RECEIPT-01 | ✅ Done · Codex Cross-Review PASS（R1）· 拆 STATE_FILE 語意超載（關切 A round 記帳 vs 關切 B PASS attestation）· cross_review.sh PASS/ADVISORY 於 `clear_budget_state` 前寫 durable range-bound receipt 至 `.tmp/cross_review_pass/${TICKET_ID}`（不受兩 janitor 影響）· structural_verify.sh Check E re-anchor：讀 receipt 驗 VERDICT∈{PASS,ADVISORY} **且** DIFF_SCOPE 匹配當前 --post scope（mismatch→BLOCKER false-open 防線）· **AC-1 真實閉環**：Step 5 --post Check E ✅ 綠（解 SV-CHECK-E-REGRESSION）· 禁區零改（AC-3）· Build/Test/Lint N/A（bash -n + Check E 自測 5/5）· 3eae2b5 |
| INFRA-M3-AUDIT-01 | ✅ Done · Codex Cross-Review PASS（R1）· family provenance 偵測控制 `m3_family_audit.sh`（唯讀+escalate，`ticket_pattern.md` 表 derive family 白名單，結構文法 drift-check 鏡像 `commit-hook=`）· 對帳 185 ID → 結構合法 158 / 已登記 158 / **escalate 0** · self-test 0 誤報（雙段家族+leading-only FP2 防禦）· `/dream` §0 stale-gate（>14 日 fail-loud）· `ticket_pattern.md` 第 6 行死指令消除（版本 B）· Build/Test/Lint N/A · 7025e39 · ⚠️ /qa Step 5 --post [E] waived-with-cause（Check E 對所有 PASS 票必 BLOCKER 之跨機制 regression；非 M3 缺陷，綁 INFRA-SV-STATE-RECEIPT-01 修票）|
| INFRA-SV-STATE-CLEANUP-01 | ✅ Done · Codex Cross-Review PASS（R1）· cross_review.sh 新增 `cleanup_terminal_success_states` 掃除歷史 terminal-success state 殘檔（round 計算前）· 實戰驗證 state 殘檔 34→5（PASS/ADVISORY 掃除，BLOCKER/REPORT_FIX/UNKNOWN/malformed 保留）· fixture 8 案全綠 · Build/Test/Lint N/A（純 .sh）· 07909b8 |
| KAIROS-BACKFILL-01-FIX-A | ✅ Traycer execute_review PASS · ticket_pattern.md 收斂 `KAIROS-[CODE]-[NN]`→`KAIROS-BACKFILL-[NN][...]` 專用格式（CSO 裁決）+ Registry 歷史稽核敘述改寫（AC-6 乾淨通過，0 殘留負面敘述）· 1F+registry |

**Last Updated**: 2026-07-05

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S31** · TRACTION-SSOT-05 ✅ Done（9af1223 · Codex 跨族窄審 PASS）· TRACTION-SSOT-04 ✅ Done（565e03e）· INFRA-SV-STATE-RECEIPT-01 ✅ Done（3eae2b5）· INFRA-M3-AUDIT-01 ✅ Done（7025e39）· INFRA-SV-STATE-CLEANUP-01 ✅ Done（07909b8）· GAP-06 仍 🔴 BLOCKED（待 HaloGuidanceLayer.kt 渲染實作）|
| Compile | ⚪ N/A (TRACTION-SSOT-05：純 spec .md 治理票，無 Gradle target) |
| Tests | ⚪ N/A (governance ticket · 字串錨 grep AC-1~5 全綠代替) |
| Lint | ⚪ N/A (governance ticket) |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| GAP-06 | HaloTraction 渲染缺牽引圓點：Normal 無 HaloTraction（出 Focus Ring）；PTF 圓點靜態不磁吸 | 🔴 P0 | SSOT 規格已修（TRACTION-SSOT-01 ✅：alpha 0.8 + 牽引圓點 + 三段 V 曲線）；待 HaloGuidanceLayer.kt 渲染實作票落地 |
| GAP-07 | afState 回授斷裂 → Guard 7a 死碼 → 瘋狂 AF（Focus Ring 閃動）（**=AC-10 假過根因**）| 🔴 P0 | CameraViewModel.kt L2117 result 被拋棄、afState 永 Idle（dead field）；需接線 |
| BUG-P08-F4-REAL-DEVICE | AC-4 🔴 橫向 detection 失效（BUG-LCSOURCE-HARDCODED-ROTATION）| 🔴 Hard Gate FAIL | Phase B 後開票 |
| BUG-LCSOURCE-HARDCODED-ROTATION | compensatedRotation 硬編碼 → 橫向偏差 | 🟡 P1 | 開 P08-F9 後處理 |

> **🟠 治理校正（2026-06-30）**：AC-10（skip-when-focused / Guard 7a）由 ✅ 改 **⚠️ PARTIAL** — unit logic 已實作測試，但 production input `AfState.Locked` 為 dead field（不可達），三道 gate 結構性假過。實際修復 = GAP-07；流程防護 = INFRA-REACH-01（已開票 Pending/）。

## Next Actions

> **S31（2026-07-05）：✅ TRACTION-SSOT-05 Done（Level-D9 語義清掃落地 · §2.2/§4.3/§8 三處 · AC-1~5 字串錨全綠 · Codex 跨族窄審 PASS 手動繞過 `:!plans/` 排除 bug · 9af1223）→ 開 Option B INFRA 票修 cross_review.sh plans/-scoped 空 diff（KAIROS K4 候選）**
>
> **S30（2026-07-05）：✅ TRACTION-SSOT-04 Done（對齊態可見性修正 · Codex Cross-Review PASS 複合路徑 R1⊕R-T18⊕FIX-A · S-4 REAL BLOCKER 消除 565e03e）· 續 HaloGuidanceLayer 渲染票（GAP-06）/ TRACTION-RENDER-01 開票**
>
> **S29（2026-07-04）：✅ INFRA-SV-STATE-RECEIPT-01 Done（全票 --post 阻斷已解 · Check E re-anchor durable receipt · M3 [E] waiver 解除）**

1. **🟡 引用修正支線**：使用者要求「`.claude/`、`plans/` 引用新版本路徑 v3.3」，已掃描分類（現行指標多數已於 GOV-03/PROTO-01 同步；`plans/cso_briefing/` 內 v3.2 引用屬 R-T5 化石溯源，Hard Constraint 3 **MUST** 不回改）→ **新 session 待使用者確認範圍或指定具體檔案後執行**
2. **✅ TRACTION-SSOT-01**（/sv + Codex R2 PASS · fc6ffa8）：SSOT §2.5/§2.5.1/§2.5.2 + Layout §3.3 規格已落地 → **下一步開 HaloGuidanceLayer.kt 渲染實作票**（GAP-06 解除前置）
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

**Canonical Registry**：`plans/cso_briefing/project/Pending/KAIROS4_Candidate_Compilation.md`（K4-1~45，共 45 項；本節僅作暫存區，新候選登記後即合併入該檔並移除本節條目）。

> INFRA-XX（S28）稽核：Compilation 檔內原標「✅ 收錄」的 K4-3/6/8/9 經 grep 核查目標檔案（`cross-review.md` / `kairos-lessons.md` / `camera-constraints.md`）均無對應條文，已改列 🔴 待補寫；`kairos-lessons.md` 確認為必要存在（唯一 path-scoped 自動載入的已落地規則本體，與 Compilation 的候選登記角色不重疊，見下方說明）。KAIROS-BACKFILL-01（S28）已補寫落地，假閉環佇列清空。

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_6.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
