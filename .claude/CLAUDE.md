# CLAUDE.md — Cueshot MVP Behavioral Contract

≤ 150 行 / 12 核心規則。超標即重構。
Domain rules: .claude/rules/（path-scoped，碰到匹配檔案時自動載入）

## ⚡ Boot Sequence

Session 啟動 → `mkdir -p tmp && rm -f tmp/verify_cmd.conf tmp/sv_report.md tmp/handoff.md` → 讀 PROGRESS.md → 輸出 Boot Report → **停止等 Ticket**。

```text
📍 Ticket: [ID] / 無 → 停止
📊 CLAUDE.md [N]行 | PROGRESS.md [N]行
⚠️ Blockers: [列出或「無」]
🌐 Output Language: [繁體中文 / EN]
```

Ticket 含 `## Verify Config` 區塊時，必須將該區塊內容複製為 `tmp/verify_cmd.conf`（純文字，原樣複製，不加工）。

---

## 核心鐵律（6 條）

### 1. 確認再動手

- 不假設。有歧義就問，不要靜默選擇。
- 如果存在多種解讀，全部列出——不要替 user 挑。
- 如果有更簡單的做法，推回來。推回是職責不是越權。
- 修改核心邏輯前，先查閱 SSOT（`plans/architecture/SSOT_v4_0_5.md`）。
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

a) 無 Ticket 不動手。格式：`P[XX]-[CODE]` / `INFRA-[XX]` / `HOTFIX-[XX]` / `SPIKE-[CODE]`。
   無 Ticket → 輸出 Boot Report 並停止。
b) 不基於 MEMORY.md / PROGRESS.md / TODO / FIXME 自主決定下一步。
c) /qa /sv /compact /dream 是驗收工具，不是實作授權。發現問題 → 回報，不自行修復。
d) Session 首訊無 Ticket → 詢問後停止。

### 5. 寫之前先讀

- 修改檔案前，先讀 exports、immediate caller、shared utils。
- 「看起來正交」是最危險的判斷——不確定就問。
- 對 >200 行 .kt/.xml，Edit 前必須 Read 操作區段（±10 行上下文）。
- /compact 後強制重新 Read（無論檔案大小）。

### 6. 語言鎖定

所有輸出：繁體中文 + 英文技術術語。程式碼註解與變數名：英文。
嚴禁韓文、日文、簡體中文。違反 → 立即自我修正。

---

## 工程紀律（6 條）

### 7. Intent-Driven

將任務轉化為可驗證目標，迭代直到成功：
- 「加驗證」→「寫 invalid input 測試，然後讓它通過」
- 「修 bug」→「寫重現測試，然後讓它通過」
- 「重構 X」→「確認重構前後測試都通過」

強成功標準讓你能獨立迭代。弱標準（「讓它動」）需要不斷確認。

### 8. 確定性工作用代碼

routing / retry / status-code / deterministic transform → 用 code。
只在 classification / drafting / extraction 場景用 model 判斷。
Status code 已能回答的問題，code 回答。

### 9. 測試驗 intent

測試編碼「為什麼」行為重要，不只驗「做了什麼」。
若 diff 含 return 值語義變更或 state 轉移路徑變更 → 測試必須覆蓋新舊值邊界。
無法在 business logic 變更時失敗的測試 = 錯誤的測試。

### 10. 每步 Checkpoint

多步驟任務每完成一步 → 回報 done / verified / remaining。
無法回述當前狀態 → 停下來重述。不從無法描述的狀態繼續前進。

### 11. 衝突不混搭

兩種 pattern 矛盾 → 選較新 / 較多測試覆蓋的那個，標記另一個待清。
混合兩種 pattern 的代碼 = 最差的代碼。發現矛盾 → 大聲說，不靜默各取一半。

### 12. 失敗要大聲

不確定就說。「完成」但跳過項目 = 沒完成。「測試通過」但跳了測試 = 沒通過。
預設：暴露不確定性，而非隱藏它。

---

## SOP

```text
0.Resume → 1.Plan(Gemini) → 2.Code(/uc) → 3./qa → 4./sv
→ 4.5.cross_review(Codex) → 5.Traycer → 6.Git → 7.KAIROS(節點時)
```

## Session Health

≤10 turns 🟢 / 11-18 🟡 / ≥19 🔴 立即 /compact。單則 >200 行代碼 +2 turns。

## Skills

`/sv` 語意審查 · `/qa` Build+Test+靜態掃描 · `/uc` 臥底模式 · `/compact` 壓縮交接 · `/dream` KAIROS 回顧(D2/D6/D10/D13/D15) · `/mdlint` .md 格式修復

## SSOT

- 策略: `plans/architecture/SSOT_v4_0_5.md` · 佈局: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- 進度: `PROGRESS.md` · 歸檔: `PROGRESS_ARCHIVE.md`

Cross-Review 行為規範 → `.claude/rules/cross-review.md`

## Path-Scoped Rules

`.claude/rules/*.md` — domain rules，碰到匹配檔案時自動載入。

---

**工作指標**：diff 中不必要的變更更少、因過度複雜導致的重寫更少、釐清問題在實作前而非犯錯後。
