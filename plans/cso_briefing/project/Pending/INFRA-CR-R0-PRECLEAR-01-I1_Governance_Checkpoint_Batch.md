# INFRA-CR-R0-PRECLEAR-01-I1 — 治理 Checkpoint 三合一批次票

**Status**: PENDING（Checkpoint-gated；三件事一次送審）
**類型**: 實作票（治理文件觸碰）
**來源議題**: Advisor
**母票**: `INFRA-CR-R0-PRECLEAR-01`（APPROVED，E0/E1 結案）
**依據 SSOT**: `reports/INFRA-CR-R0-PRECLEAR-01_E1_Ruling_20260720.md`（裁決 2／裁決 3／裁決 5 行為層落點①＋AC-I5）；`Handoff_PRECLEAR01_IPhase_Ready_20260720.md` TODO#1

**佔用宣告（C5，執行 session 開場必貼）**:

```text
TICKET_ID=INFRA-CR-R0-PRECLEAR-01-I1
BRANCH=feature/cueshot-mvp
ALLOWED=.claude/rules/concurrency.md, .claude/CLAUDE.md
SCOPE=治理文件三合一註記/修改，Checkpoint 批次送審；不觸碰 .sh／sv/SKILL.md／PROGRESS.md
```

---

## 1. Outcome（成果定義）

| 項 | 內容 | 依據 |
|----|------|------|
| A | `concurrency.md` §4 待開票清單第 1 項加註：「由 `INFRA-CR-R0-PRECLEAR-01` P2 涵蓋／不另立票／**MUST** 不得重複開票」，註記格式比照 commit `[INFRA-CONCURRENCY-01-DOCSYNC-03]` 既決前例 | E1 裁決 2 |
| B | `concurrency.md` §5 條文旁新增註解（**不改本文**，C2 合規）：引導 agent 於 append 前現值直取時**亦執行 TICKET_ID 比對** | E1 裁決 5 行為層落點① |
| C | `.claude/CLAUDE.md` Boot Sequence 新增 grep-before-rm 步驟：`rm -f` 前先 grep `tmp/verify_cmd.conf` 內 TICKET_ID，非己票 → **整個 rm 步驟不執行（`verify_cmd.conf`／`sv_report.md`／`handoff.md` 三檔皆不清）＋宣告佔用衝突＋停止**（防半清殘局） | AC-I5／E1 裁決 3／P3(b) |
| D | 搭載 KAIROS #4/#5 合節：於 `.claude/CLAUDE.md` 新增「驗證與信任錨設計二紀律」節（節名逐字；主 session 2026-07-21 已 `search_decisions` 對決策檔覆核零 drift）；**內容與落點以 `2026-07-20_KAIROS_Intake_Round_20260720` 決策檔為 SSOT**（載體＝I 階段 CLAUDE.md Checkpoint 批次），寫入前 **MUST** 以 `search_decisions` 覆核原文，禁自行腦補措辭、**禁自行新增 ALLOWED 外第三檔落點** | Handoff TODO#1＋C4 |

## 2. Verification（驗收）

| # | AC | 驗證方式 |
|---|----|---------|
| I1-1 | A 註記字面含「P2 涵蓋／不另立票／不得重複開票」三要素 | 文字比對 |
| I1-2 | B 為旁註記；機械謂詞：§5 範圍 diff hunks 僅含 `+`（新增旁註）行、`-` 行數＝0；既有 §5 正文零改寫／零刪除 | `git diff` 機械檢視 |
| I1-3 | C 之 Boot Sequence 含 grep→分支邏輯；模擬非己票 conf 情境 → 不清除＋衝突宣告 | 行為測試 |
| I1-4 | 三件（A＋B＋C）**MUST** 同一次 Checkpoint 送審；**MUST NOT** inline 夾帶於其他 AC 交付或分批 | 送審紀錄 |
| I1-5 | D 節文字與 KAIROS 決策檔裁定內容一致（引用不複寫，僅摘錄裁定文字） | `search_decisions` 對照 |

## 3. Constraints（約束）

- C2：§5 已簽發條文不回改；PROGRESS.md 屬 FORBIDDEN，本票不觸碰
- C5：佔用宣告開場；C6：禁 agent 自動寫回圖譜
- **防順手實作**：AC-I4 三讀取點改造／AC-I2 word-boundary 等後續 AC **MUST NOT** 於本票夾帶執行
- 本票 **MUST NOT** 觸碰 `.sh` 腳本與 `sv/SKILL.md`（後者屬 I2 一般票流，裁決 5 明文不進 Checkpoint）

## 4. Iteration（迭代路徑）

1. Checkpoint 批次送審（A＋B＋C＋D 一次提交）
2. 通過 → commit → 回報主 session
3. 解鎖後續實作切分：依相依序 **P3(a)(b) 前置 P1**（AC-I4/I5 先於 AC-I1/I2），P4 可並行

## 5. Error Handling（錯誤處理）

| 情境 | 處置 |
|------|------|
| Checkpoint 未過 | 逐項退回修訂；禁部分提交（三合一整批進退） |
| KAIROS 決策檔 search 未命中 | 中止 D 項，回報主 session 覆核，禁憑記憶重建 |
| MCP 寫入/讀取逾時 | 依 userPreferences R5 SOP：read-back 驗證優先，無法確認改存 artifact，禁重複套 patch |
| 開場發現 `tmp/verify_cmd.conf` 已含非己票 TICKET_ID | 中止＋回報主 session 佔用衝突；禁清除、禁覆寫 conf |
