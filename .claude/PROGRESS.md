# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| TRACTION-PROTO-01 | ✅ Codex Cross-Review PASS · Prototype v3.3 Level dual-axis→roll-only reconcile（R-T5 downstream sweep 結案）· v3.2 凍結為歷史 baseline · Halo α=0.85 保留 · 1F +1123L 新檔 · 84d6cb1 |
| TRACTION-LAYOUT-01 | ✅ Codex Cross-Review PASS · Layout Spec 內文版號 bump v2.5.2→v2.5.3 + §16 修訂追蹤（Layer 5 metadata 閉環）· 零 rename · 1F +20/-8L · 2ae5bef |
| TRACTION-GOV-02 | ✅ /index-verify PASS · Layout Spec 內文版號 bump v2.5.1→v2.5.2（檔名穩定不變）+ §15 修訂追蹤 · 零 rename · Cross-Review N/A · 1F +19/-8L · ee3bf82 |
| TRACTION-GOV-03 | ✅ /index-verify PASS · 檔名版號慣例建立（活文件 slug / 快照版號入檔名，R-T12）· Naming_Version_Convention.md 入索引 + CSO_INDEX 指標 · 零 rename · 1F +37L · 36cfc36 |
| TRACTION-GOV-01 | ✅ /index-verify PASS · Layout Spec 版本漂移修正 SSOT v4.0.5→v4.0.6（16 處，殘留 0）· Cross-Review N/A · 1F +16/-16L · d1d9047 |
| TRACTION-SSOT-03 | ✅ /sv PASS · Level roll-only D-9 清理（§2.0.1 pitch 化石移除→abs(rollDiff)）+ §2.4 補全 + Layout Layer 5 表 · Codex R1 PASS · 2F +30/-3L · f87aae0 |

**Last Updated**: 2026-07-02

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S28** · PROTO-01 ✅ Codex Cross-Review PASS（原型 v3.3 Level roll-only；R-T5 downstream sweep 結案）· GAP-06 仍 🔴 BLOCKED（待 HaloGuidanceLayer.kt 渲染實作）· **→ 開渲染實作票** |
| Compile | ⚪ N/A (PROTO-01：prototype .html，無 Gradle target) |
| Tests | ⚪ N/A (PROTO-01 prototype) |
| Lint | ⚪ N/A (PROTO-01 prototype) |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| GAP-06 | HaloTraction 渲染缺牽引圓點：Normal 無 HaloTraction（出 Focus Ring）；PTF 圓點靜態不磁吸 | 🔴 P0 | SSOT 規格已修（TRACTION-SSOT-01 ✅：alpha 0.8 + 牽引圓點 + 三段 V 曲線）；待 HaloGuidanceLayer.kt 渲染實作票落地 |
| GAP-07 | afState 回授斷裂 → Guard 7a 死碼 → 瘋狂 AF（Focus Ring 閃動）（**=AC-10 假過根因**）| 🔴 P0 | CameraViewModel.kt L2117 result 被拋棄、afState 永 Idle（dead field）；需接線 |
| BUG-P08-F4-REAL-DEVICE | AC-4 🔴 橫向 detection 失效（BUG-LCSOURCE-HARDCODED-ROTATION）| 🔴 Hard Gate FAIL | Phase B 後開票 |
| BUG-LCSOURCE-HARDCODED-ROTATION | compensatedRotation 硬編碼 → 橫向偏差 | 🟡 P1 | 開 P08-F9 後處理 |

> **🟠 治理校正（2026-06-30）**：AC-10（skip-when-focused / Guard 7a）由 ✅ 改 **⚠️ PARTIAL** — unit logic 已實作測試，但 production input `AfState.Locked` 為 dead field（不可達），三道 gate 結構性假過。實際修復 = GAP-07；流程防護 = INFRA-REACH-01（已開票 Pending/）。

## Next Actions

> **S24（2026-06-30）：INFRA-REACH-01 + FIX-AI3 全鏈路閉環 · Guard Input Reachability gate 完整落地 · K5-Rule 29 收割**

1. **✅ TRACTION-SSOT-01**（/sv + Codex R2 PASS · fc6ffa8）：SSOT §2.5/§2.5.1/§2.5.2 + Layout §3.3 規格已落地 → **下一步開 HaloGuidanceLayer.kt 渲染實作票**（GAP-06 解除前置）
2. **🔴 開票 GAP-07**：修 afState 回授斷裂（CameraViewModel.kt L2117 接線）
3. **🔴 實機重測**：TRACTION-SSOT-01 修復後，AC-6/8/9 實機重測 → Traycer 終審
4. **🟡 STATE-CONTRACT-01 Traycer 終審**：FIX-A + FIX-B + FIX-C 均 /sv PASS，送終審
5. **✅ GOV-01/02/03 全閉環**（/index-verify PASS）：版本漂移修正 + Layout v2.5.2 內文 bump + 檔名版號慣例（活文件 slug / 快照版號入檔名，R-T12）→ 治理衛生軌清空

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

## D13 KAIROS #4 候選議程

**Canonical Registry**：`plans/cso_briefing/project/Pending/KAIROS4_Candidate_Compilation.md`（K4-1~28, K4-34, K4-35，共 30 項；本節僅作暫存區，新候選登記後即合併入該檔並移除本節條目）。

> INFRA-XX（S28）稽核：Compilation 檔內原標「✅ 收錄」的 K4-3/6/8/9 經 grep 核查目標檔案（`cross-review.md` / `kairos-lessons.md` / `camera-constraints.md`）均無對應條文，已改列 🔴 待補寫；`kairos-lessons.md` 確認為必要存在（唯一 path-scoped 自動載入的已落地規則本體，與 Compilation 的候選登記角色不重疊，見下方說明）。

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_6.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
