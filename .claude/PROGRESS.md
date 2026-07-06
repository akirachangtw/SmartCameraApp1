# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| INFRA-REACH-02 | ⚠️ 8/9 AC PASS，1 項待 Akira 裁決 · Codex Cross-Review R2 PASS（ADVISORY · `REACHABILITY: NONE` 實測輸出 · 46b8e24）· CSO 審查 M-1 後修正：`structural_verify.sh` REACHABILITY 驗證由內嵌 Check E-2 重構為獨立 Check G（不複用 Check E receipt 語義）、R-1 迴歸防護重跑（INFRA-CR-PLANS-DIFF-01 carve-out 15/15 assertions PASS）、AI-4 雙自測重跑（負向 exit=1/正向 exit=0）· AI-1~AI-7 七項 Action Item 皆完成 · **待裁決**：AC「3.5.5 glob 化對第二 state holder 正向命中 dead-field 樣例」現僅有 1 個真實正向命中（`CameraState.kt`/`afState`），全碼庫無第二個真實 dead-field bug 可證——三選項見票內揭露（接受現況 / 標 PARTIAL 開後續票 / 合成自測 fixture）· 4f06a27（原始）→ 46b8e24（修正） |
| INFRA-SKILLS-UC01 | ✅ Done · Codex Cross-Review PASS（R1，完整 cross_review.sh · Guard Reachability 無新 Guard）· `/uc` 新增「## 驗證性動作豁免」段落：零廢話只約束解說性輸出，執行 `.claude/**/*.sh` MUST 用注入〔腳本現況〕canonical 格式 or 讀腳本頭 Usage，MUST NOT 憑記憶組裝參數 · 既有約束（零過渡/純 Diff/DONE 終止）保留 · AC-4 .sh 零觸碰 · +8L · 47fe32d |
| TRACTION-SSOT-05-FIX-A | ✅ Done · Codex Cross-Review PASS（R1，完整 cross_review.sh + ssot_context 注入，禁手動窄審 · Guard Reachability 無新 Guard · type-drift Findings none）· R-T19 Option C：§4.3 L865 幻名 `FoodCompositionScore`→`levelScore`（§7.1 評分→§7.2 EXIF 既有定義消費者）· 解 SSOT-05 母票 R3 type-drift BLOCKER（Codex 實質正確：全 SSOT 零定義空名）· L866 FoodCompositionRule/R-T5 鏡像零觸碰 · MAX_LINES 5（實 1 行）· 0757dfc · Check E receipt 合法自動寫入 · 母票+FIX-A 同移 deliver/ |
| INFRA-CR-PLANS-DIFF-01 | ✅ Done · Codex Cross-Review PASS（R1，dogfood：修好的腳本自審自身 diff）· cross_review.sh 粗排除 → ALLOWED-aware carve-out（`.claude//docs//plans/` 逐一，只在 ALLOWED 涵蓋時 carve-out）· 修 plans/-scoped 治理票被 `:!plans/` 餵空 diff bug（實證 0→2564 bytes）· verdict/prompt/fail-budget/exit code 零觸碰 · Fixture A/B/C+EXTRA_EXCLUDE+empty 14 assertions 全綠 · 43a35fb · Traycer execute_review pending |
| TRACTION-SSOT-05 | ✅ FIX-A 閉環（母票 type-drift 已由 FIX-A 0757dfc 修正）· Level-D9 語義清掃 §2.2/§4.3/§8 三處落地（9af1223）· 手動窄審 PASS 曾遮蔽 §4.3 幻名 `FoodCompositionScore`（無 ssot_context 未觸發 drift 檢查）· 完整 cross_review R3 BLOCKER 正確揪出 → R-T19 Option C 重錨為 levelScore → 母票+FIX-A 配對同移 deliver/（reflog 閉合）|

**Last Updated**: 2026-07-06

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S34** · INFRA-REACH-02 ⚠️ 8/9 AC（待裁決第二 state holder 正向命中，Codex R2 ADVISORY · 46b8e24）· INFRA-SKILLS-UC01 ✅ Done（Codex R1 PASS · 47fe32d）· TRACTION-SSOT-05-FIX-A ✅ Done（0757dfc）· TRACTION-SSOT-05 ✅ FIX-A 閉環（母票+FIX-A 同移 deliver/）· INFRA-CR-PLANS-DIFF-01 ✅ Done（43a35fb）· GAP-06 仍 🔴 BLOCKED（待 HaloGuidanceLayer.kt 渲染實作）|
| Compile | ⚪ N/A (INFRA-REACH-02：純 shell/prompt 治理票，無 `.kt`/`.xml` 變更，無 Gradle target) |
| Tests | ⚪ N/A (governance ticket · AI-4 REACHABILITY token 雙自測負向 exit=1/正向 exit=0 代替) |
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

> **S34（2026-07-06）：⚠️ INFRA-REACH-02 8/9 AC PASS（Reachability 防線 v2：AI-1~AI-7 七項全落地，CSO 審查 M-1 後 Check G 標準化重構 + R-1 迴歸防護重跑 + Codex R2 ADVISORY 46b8e24）→ 1 項 AC（第二 state holder 正向命中）待 Akira 裁決，票暫留 Pending/ 未移 deliver/ → KAIROS K4-50「AC 語義折損律」候選登記完成**
>
> **S33（2026-07-06）：✅ INFRA-SKILLS-UC01 Done（`/uc` 新增「## 驗證性動作豁免」：零廢話只約束解說性輸出，執行 `.claude/**/*.sh` MUST 用注入 canonical 格式，MUST NOT 憑記憶組裝參數）Codex Cross-Review PASS · 47fe32d**
>
> **S32（2026-07-06）：✅ TRACTION-SSOT-05-FIX-A Done（R-T19 Option C：§4.3 幻名 FoodCompositionScore→levelScore，解母票 R3 type-drift BLOCKER · Codex R1 PASS ssot_context 注入 · 0757dfc）→ 母票+FIX-A 同移 deliver/（reflog 閉合）→ SSOT-05 母票假 ✅ 回滾為「FIX-A 閉環」**

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

**Canonical Registry**：`plans/cso_briefing/project/Pending/KAIROS4_Candidate_Compilation.md`（K4-1~50，共 50 項；本節僅作暫存區，新候選登記後即合併入該檔並移除本節條目）。

> INFRA-XX（S28）稽核：Compilation 檔內原標「✅ 收錄」的 K4-3/6/8/9 經 grep 核查目標檔案（`cross-review.md` / `kairos-lessons.md` / `camera-constraints.md`）均無對應條文，已改列 🔴 待補寫；`kairos-lessons.md` 確認為必要存在（唯一 path-scoped 自動載入的已落地規則本體，與 Compilation 的候選登記角色不重疊，見下方說明）。KAIROS-BACKFILL-01（S28）已補寫落地，假閉環佇列清空。

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_6.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
