# ADV-CR-R0-PRECLEAR-01-I2 — sv/SKILL.md 行為層提醒插入票 **(原Ticket_ID: INFRA-CR-R0-PRECLEAR-01-I2更名以符合議題)**

**Status**: PENDING（一般票流；**不進 Checkpoint**——`sv/SKILL.md` 不在 `cross-review.md` 敏感路徑清單，E1 裁決 5 明文）
**類型**: 實作票（skill 文件行為層提醒）
**來源議題**: Advisor
**母票**: `ADV-CR-R0-PRECLEAR-01`（APPROVED，E0/E1 結案）
**依據 SSOT**: `reports/ADV-CR-R0-PRECLEAR-01_E1_Ruling_20260720.md`（裁決 5 行為層落點②）；`Handoff_PRECLEAR01_IPhase_Ready_20260720.md` TODO#2

**佔用宣告（C5，執行 session 開場必貼）**:

```text
TICKET_ID=ADV-CR-R0-PRECLEAR-01-I2
BRANCH=feature/cueshot-mvp
ALLOWED=.claude/skills/sv/SKILL.md
SCOPE=兩個 PROGRESS.md 寫入流程步驟內插入提醒行＋KAIROS #3 紀律行；不觸碰 .sh／rules/*.md／CLAUDE.md／PROGRESS.md
```

---

## 1. Outcome（成果定義）

| 項 | 內容 | 依據 |
|----|------|------|
| A | `sv/SKILL.md` 內**兩個實際寫入 PROGRESS.md 的流程步驟**各新增一行提醒：append 前現場 `wc -l` 直取現值＋TICKET_ID 比對（「把提醒放在手所在之處，非規則檔深處」） | E1 裁決 5 行為層落點② |
| B | 插入 KAIROS #3「E 階段進場紀律」行（AC 落點存在性前置取證；載體＝本票 `sv/SKILL.md`，主 session 2026-07-21 已 `search_decisions` 對決策檔覆核確認）；**內容與落點以 `2026-07-20_KAIROS_Intake_Round_20260720` 決策檔為 SSOT**，寫入前 **MUST** 以 `search_decisions` 覆核原文；決策檔或落點錨點無法確認 → **中止回報，禁自行裁量** | Handoff TODO#2＋C4 |

## 2. Verification（驗收）

| # | AC | 驗證方式 |
|---|----|---------|
| I2-1 | 兩個寫入流程步驟落點各含一行提醒；判準＝「**實際寫入 PROGRESS.md 之流程步驟**」數＝2（步驟層級，非字串命中數——全檔 `PROGRESS.md` 字串命中 8 處，僅 2 處為寫入步驟）；預期錨點：`Step 10: Ticket Done 標記`／`Step 10.5: Session Note 清理`；功能判準為主、錨點交叉核對，**不一致（如已重編號）→ 中止回報，禁按編號盲插** | grep＋人工覆核 |
| I2-2 | 提醒文字含「`wc -l` 直取現值」＋「TICKET_ID 比對」二要素 | 文字比對 |
| I2-3 | KAIROS #3 行與決策檔裁定內容一致 | `search_decisions` 對照 |
| I2-4 | 既有步驟語意零改動（diff 僅新增行，無刪除/改寫） | `git diff` 機械檢視 |

## 3. Constraints（約束）

- C2：PROGRESS.md 屬 FORBIDDEN，本票僅改 `sv/SKILL.md`，不觸碰 PROGRESS.md 本體
- C5：佔用宣告開場；C6：禁 agent 自動寫回圖譜
- **不併入 Checkpoint 批次**（裁決 5 明文）；可與 I1 前後接續執行，但 **MUST** 各自獨立 TICKET_ID 宣告，禁共用宣告
- 防順手實作：AC-I8 機械層（AC-I3 測試案例連動）屬後續 AC-I3 票，**MUST NOT** 本票夾帶

## 4. Iteration（迭代路徑）

1. 一般票流執行 → commit → 回報
2. 本票完成後，AC-I8 行為層雙落點閉環達成（落點①由 I1 承載）；退役層隨 PROGRESS append 路徑退役自然閉環

## 5. Error Handling（錯誤處理）

| 情境 | 處置 |
|------|------|
| 寫入流程步驟落點數 ≠ 2 | 中止，回報實際落點清單供人工裁定，禁自行擴縮範圍 |
| KAIROS 決策檔 search 未命中 | 中止 B 項，回報主 session 覆核，禁憑記憶重建 |
| MCP 寫入/讀取逾時 | 依 userPreferences R5 SOP：read-back 驗證優先，無法確認改存 artifact，禁重複套 patch |
