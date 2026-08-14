# KAIROS #3 教訓候選彙整 — 完整版（K3-1 ~ K3-7）

**本檔為 KAIROS #3 候選登記唯一真理來源 (Canonical Registry)**。`PROGRESS.md` §D10 候選議程僅作暫存區指標，合併後即從 PROGRESS 移除。

**彙整日期**: 2026-07-03（自 PROGRESS.md §D10「KAIROS #3 候選議程（Pre-W1-B 登記）」搬移）
**來源**: D8 FIX-01~03 + FIX-INFRA-02-D + TICKET-01 + cross_review forensic + K3-7
**候選數量**: 7 項（K3-1~7 連續；無空號）
**觸發時機**: KAIROS #3（Pre-W1-B 登記，D10 節點）— 部分項目已落地（見下方收錄狀態）

---

## 全量候選表

| ID | 候選項目 | 來源 | 類型 |
|----|---------|------|------|
| K3-1 | Debug Overlay Zero Tolerance | D8 FIX-01~03 | CLAUDE.md 規則 |
| K3-2 | Hardcoded Text Scanning | D8 FIX-01~03 | /sv gate 擴充 |
| K3-3 | KAIROS Real-Device Screenshot Requirements | D8 FIX-01~03 | KAIROS 節點流程 |
| K3-4 | Decommission 票刪除驗收必須用 tracked filename glob 驗證 | FIX-INFRA-02-D | CLAUDE.md 規則 |
| K3-5 | Ticket 驗收清單與 SSOT 衝突時，以合併規格源為裁決依據 | TICKET-01 | CLAUDE.md 規則 |
| K3-6 | `cross_review.sh` payload 驗證 SV scope ↔ diff scope 一致性 | cross_review forensic | 腳本改進 |
| K3-7 | Edit 前強制 Re-view（>200 行目標檔 / /compact 後無條件觸發） | K3-7 | CLAUDE.md 規則 ✅ Rule 18 |

## 收錄狀態備註

- **K3-7** 已落地為 CLAUDE.md 核心鐵律 5「寫之前先讀」+ `kairos-lessons.md` K3-Rule 18。
- **K3-4** 已落地為 `phase0-lessons.md` Rule 19 追加（K3-4，tracked filename glob 驗證）。
- **K3-5** 已落地為 `phase0-lessons.md` Rule 27（SSOT 優先裁決，TICKET-01）。
- **K3-1** ✅ 已落地為 `phase0-lessons.md` Rule 24 追加（K3-1，CameraScreen 可視區 debug 文字一律 /sv 退件）〔R-b，2026-07-03 grep 核實——registry 原標「待裁決」屬錯帳，規則早已物理存在〕。
- **K3-2** ⏳ 待核 `/sv` skill 是否已含 hardcoded-text scan；若已由 Rule 24（K3-1）涵蓋則標已落地，否則補寫為 /sv gate 擴充〔R-c 殘留，唯一未結項〕。
- **K3-3** → 併入 KAIROS #4 **K4-34**（真機渲染/證據族；KAIROS 節點 real-device screenshot 要求由 K4-34 統一承載）〔R-c〕。
- **K3-6** → 併入 KAIROS #4 cross-review 群 **K4-3**（`cross-review.md` payload/scope 紀律叢集）〔R-c〕。
