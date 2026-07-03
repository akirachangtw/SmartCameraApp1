# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| INFRA-M3-AUDIT-01 | 🟡 /qa PASS（待 /sv）· family provenance 偵測控制 `m3_family_audit.sh`（唯讀+escalate，`ticket_pattern.md` 表 derive family 白名單，結構文法 drift-check 鏡像 `commit-hook=`）· 對帳 185 ID → 結構合法 158 / 已登記 158 / **escalate 0** · self-test 0 誤報（雙段家族+leading-only FP2 防禦）· `/dream` §0 stale-gate（>14 日 fail-loud）· `ticket_pattern.md` 第 6 行死指令消除（版本 B）· Build/Test/Lint N/A（純 .sh/.md）· bash -n + --self-test 代替 |
| INFRA-SV-STATE-CLEANUP-01 | ✅ Done · Codex Cross-Review PASS（R1）· cross_review.sh 新增 `cleanup_terminal_success_states` 掃除歷史 terminal-success state 殘檔（round 計算前）· 實戰驗證 state 殘檔 34→5（PASS/ADVISORY 掃除，BLOCKER/REPORT_FIX/UNKNOWN/malformed 保留）· fixture 8 案全綠 · Build/Test/Lint N/A（純 .sh）· 07909b8 |
| INFRA-TICKETID-01 | ✅ Done · Codex Cross-Review PASS（R1）· commit-hook 由 13-family 列舉收斂為單一結構文法 `^\[[A-Z][A-Z0-9]*(-[A-Z0-9]+)+\]` + 表降 provenance · CLAUDE.md Rule 4a 補償控制單行 · regex 自測全綠（13 PASS/3 REJECT/新 family 免改）· Build/Test/Lint N/A（純治理票）· c7e3035 |
| KAIROS-BACKFILL-01-FIX-A | ✅ Traycer execute_review PASS · ticket_pattern.md 收斂 `KAIROS-[CODE]-[NN]`→`KAIROS-BACKFILL-[NN][...]` 專用格式（CSO 裁決）+ Registry 歷史稽核敘述改寫（AC-6 乾淨通過，0 殘留負面敘述）· 1F+registry |
| KAIROS-BACKFILL-01 | ✅ Codex end-state inspection PASS（含撞號檢查回答行）· 假閉環規則補寫 K4-3/6/8/9 + 併補 K4-27 · `cross-review.md` §「Scope Ban BLOCKER 處置」新增 + `kairos-lessons.md` K3-Rule 25 append + `camera-constraints.md` Rule 30/31/32 · Registry 回填 KAIROS4_Candidate_Compilation.md · 4F |

**Last Updated**: 2026-07-03

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S28** · INFRA-M3-AUDIT-01 🟡 /qa PASS 待 /sv（family provenance 偵測控制 · escalate 0 · /dream stale-gate）· INFRA-SV-STATE-CLEANUP-01 ✅ Done（07909b8）· INFRA-TICKETID-01 ✅ Done（c7e3035）· GAP-06 仍 🔴 BLOCKED（待 HaloGuidanceLayer.kt 渲染實作）|
| Compile | ⚪ N/A (INFRA-M3-AUDIT-01：純 .sh/.md 治理票，無 Gradle target) |
| Tests | ⚪ N/A (governance ticket · bash -n + --self-test 代替，0 誤報全綠) |
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

> **S28（2026-07-02）：PROTO-01 收尾 · 引用修正支線待範圍確認**

1. **🟡 引用修正支線**：使用者要求「`.claude/`、`plans/` 引用新版本路徑 v3.3」，已掃描分類（現行指標多數已於 GOV-03/PROTO-01 同步；`plans/cso_briefing/` 內 v3.2 引用屬 R-T5 化石溯源，Hard Constraint 3 **MUST** 不回改）→ **新 session 待使用者確認範圍或指定具體檔案後執行**
2. **✅ TRACTION-SSOT-01**（/sv + Codex R2 PASS · fc6ffa8）：SSOT §2.5/§2.5.1/§2.5.2 + Layout §3.3 規格已落地 → **下一步開 HaloGuidanceLayer.kt 渲染實作票**（GAP-06 解除前置）
3. **🔴 開票 GAP-07**：修 afState 回授斷裂（CameraViewModel.kt L2117 接線）
4. **🔴 實機重測**：TRACTION-SSOT-01 修復後，AC-6/8/9 實機重測 → Traycer 終審
5. **🟡 STATE-CONTRACT-01 Traycer 終審**：FIX-A + FIX-B + FIX-C 均 /sv PASS，送終審

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
