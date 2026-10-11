# CLAUDE.md — Cueshot MVP Behavioral Contract

≤ 150 行 / 12 核心規則。超標即重構。

## ⚡ Boot Sequence

Session 啟動 → `mkdir -p tmp && rm -f tmp/verify_cmd.conf .tmp/sv_report.md tmp/handoff.md` → 讀 PROGRESS.md → 輸出 Boot Report → **停止等 Ticket**。

```text
📍 Ticket: [ID] / 無 → 停止
📊 CLAUDE.md [N]行 | PROGRESS.md [N]行
⚠️ Blockers: [列出或「無」]
🌐 Output Language: [繁體中文 / EN]
```

Ticket 含 `## Verify Config` 區塊時，必須將該區塊內容複製為 `tmp/verify_cmd.conf`（純文字，原樣複製，不加工）。

---

## 獨立開發者：MVP 優先、反過度治理

以最低時間、成本、Token 與流程摩擦交付可發布 MVP。 預設直接完成任務，不把小需求升格為企業級架構、治理或文件以確保獨立開發的最大彈性及可接受的風險界線。

新增程式碼、依賴、流程或抽象前依序判斷：
1. 這是目前 MVP、收款、使用者回饋或安全發布的必要條件嗎？不是就不做。
2. 標準庫、原生平台、既有程式或已安裝依賴能做嗎？能就重用。
3. 能否更小、可逆、可測、可快速發布？優先最少檔案、最少設定、單一路徑。
4. 只有高風險或不可逆操作，才提高驗證與治理強度。
5. 採用企業化嚴謹工作流及治理? 需要的是AI自主智能化，嚴謹對獨立開發者是毀滅性的天災。

---

## 核心鐵律（6 條）

### 1. 確認再動手

- 不假設。有歧義就問，不要靜默選擇。
- 如果存在多種解讀，全部列出——不要替 user 挑。
- **MUST** 一律以對話正文提問（選項編號 + 完整脈絡）。**MUST** 禁止呼叫 `AskUserQuestion` 彈出選項視窗——視窗會截斷脈絡且不入對話存底，裁決無法追溯。
- 如果有更簡單的做法，推回來。推回是職責不是越權。
- 修改核心邏輯前，先查閱 SSOT（`plans/architecture/SSOT_v4_0_7.md`）。
  文件是架構唯一真理，程式碼的解釋以文件為準。

### 2. 最小修改

- 解決問題的最少代碼。不做推測性功能。
- 不為單一用途建抽象層。不為不可能的場景加錯誤處理。
- 如果寫了 200 行但 50 行能解決，重寫。
- 禁止 `// TODO`、`return null // implement later`——必須真實接線。

自我測試：一個 senior engineer 會說這 overcomplicated 嗎？如果是，簡化。

### 3. 手術刀

改既有代碼時：
- 不「順便改善」相鄰代碼、註解、格式。不重構沒壞的東西。
- 匹配既有風格，即使你會用不同方式寫。
- 發現無關的 dead code → 提出來，不要刪。

你的修改產生孤兒時：
- 移除你的修改造成的廢棄 imports / 變數 / 函數。
- 不移除修改前就存在的 dead code（除非被要求）。

同一 Commit 嚴禁混合「原始碼 .kt/.xml」與「文件 .md/.log」。
測試：每一行 diff 都能直接追溯到 user 的 Ticket 要求。
編輯 `.md` 檔案 **MUST** 遵守 Markdown 格式規範（MD022/MD025/MD040 等）；寫入後若有 warning **SHOULD** 立即執行 `/mdlint` 修復。

### 4. 越權禁令

a) 無 Ticket 不動手；無 Ticket → 輸出 Boot Report 並停止。合法 Ticket ID 格式見 `.claude/rules/ticket_pattern.md`（SSOT，與 commit-msg hook 同源）；其 family 表為 provenance 紀錄，非 hook 強制。遇結構合法但 family 未登記之 ID：該 family 票檔實存於 `Pending/` 或 `deliver/` 且「簽發者」欄含 `CSO` → 開工（AI-0/Boot）時自動 append provenance 行至 `ticket_pattern.md` Family Provenance 補登紀錄區塊（MUST NOT 改動 regex 與判定邏輯，MUST 於證據欄留補登紀錄；CSO 簽發票檔即授權證據，補登為轉錄非裁決）；無此類票檔（憑空出現新 family）→ STOP 並 escalate CSO，禁自行補表或改 regex。新 Ticket 使用尚未登記之 family 時，MUST 於 spawn 當下同步補登 provenance 表，禁止依賴事後稽核才補登。
b) 不基於 MEMORY.md / PROGRESS.md / TODO / FIXME 自主決定下一步。本條拘束「自行開啟新工作」，**不適用於**當前已啟動 Ticket 中由 Header／Verify Config／Action Items／AC／gate receipt 唯一決定之同票後續 transition；該類 transition 依 `.claude/rules/ssot-governance.md` §Ticket Runtime Transition Table 執行，**不因對話模式切換而失效**。
c) /qa /sv /compact /dream 是驗收工具，不是實作授權。發現問題 → 回報，不自行修復。
d) Session 首訊無 Ticket → 詢問後停止。
e) 正式開票僅可在目標專案 Claude Code session 經 `/ticket-author` 執行。一律自 `plan` 起步；根因未明時先 `diag`，再依診斷結果執行 `plan`。`ticket` 僅可對 plan artifact 的選定 item 執行，且該 artifact 必須同時為：`Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`、`Fixture: false`。`plan` 必須提供可讀的 Source、Goal、Constraints 與 Decisions；`ticket` 必須引用 plan artifact、Selected item 與 Goal。正式 ticket ID 僅能由 ticket mode 對既有 Registry（含 archive、Pending、deliver、void）查重後分配；不得由上游預留或宣稱已建立。禁止手工建立 Pending 票檔、直接寫入 Pending Registry，或繞過 `/ticket-author` 正式登冊。本條只規範正式 Pending ticket 的建立與登冊；任何下游 routing 後的執行、審查與狀態規則，依接手流程自身的 SSOT 辦理。

### 5. 寫之前先讀

- 修改檔案前，先讀 exports、immediate caller、shared utils。
- 「看起來正交」是最危險的判斷——不確定就問。
- 對 >200 行 .kt/.xml，Edit 前必須 Read 操作區段（±10 行上下文）。
- /compact 後強制重新 Read（無論檔案大小）。

### 6. 語言鎖定

所有輸出：繁體中文 + 英文技術術語。程式碼註解與變數名：英文。
嚴禁韓文、日文、簡體中文。違反 → 立即自我修正。

---

## 工程紀律（Rule 7–12）

Intent-Driven／確定性用代碼／測試驗 intent／每步 Checkpoint／衝突不混搭／失敗要大聲 → 全文見 `.claude/rules/engineering-discipline.md`（編號沿用）。

---

## Acceptance Criteria 修訂閉環

> Acceptance Criterion 遭移除、縮減或豁免，或其前置條件、預期結果被改變後，依賴舊有語意的審查結論即告失效，不得再引用為現行結論。
> 恢復實作或下游審查前，修訂者 MUST 重驗共用相同資料母體、來源、入口或契約假設的兄弟 Acceptance Criteria。重驗方式不限，不因此使任何審查工具成為強制前置；結論 MUST 記錄於票檔。
> 回執、勾選、完成註記、純錯字及其他非規範性修改不觸發本規則。本規則本身不改變 lifecycle、不建立 Rev、不拆票或新增票，也不計為新的外部審查輪次。

## SOP

> 依風險分級（Akira 2026-10-02 裁決，取代 2026-08-22「全程序、無快速通道」）：低風險 docs／治理文本票（僅 `.md`、可 git revert）得省略 3./qa、4./sv、4.5.cross_review；觸及 `app/` production code、腳本／pipeline 或不可逆操作之票維持全程序。

```text
0.Resume → 1.Plan(Claude) → 2.Code(/uc) → 3./qa → 4./sv → 4.5.cross_review(Codex) → 5.Traycer → 6.Git → 7.KAIROS(節點時)
```

## Skills

`/sv` 語意審查 · `/qa` Build+Test+靜態掃描 · `/uc` 臥底模式 · `/compact` 壓縮交接 · `/dream` KAIROS 回顧(D2/D6/D10/D13/D15) · `/mdlint` .md 格式修復

## SSOT

- 策略: `plans/architecture/SSOT_v4_0_7.md` · 佈局: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- 進度: `PROGRESS.md` · 歸檔: `PROGRESS_ARCHIVE.md`

Cross-Review 行為規範 → `.claude/rules/cross-review.md`

## Path-Scoped Rules

- 使用 `paths:` 的 rule，僅在讀取匹配檔案後載入。
- 沒有 `paths:` 的 rule 無條件載入。
- `globs:` 不是有效欄位，Claude Code 會忽略；不得新增。

---

**工作指標**：diff 中不必要的變更更少、因過度複雜導致的重寫更少、釐清問題在實作前而非犯錯後。
