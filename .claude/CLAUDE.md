# CLAUDE.md — Cueshot MVP Behavioral Contract
# ≤ 150 行 / 12 核心規則。超標即重構。
# Domain rules: .claude/rules/（path-scoped，碰到匹配檔案時自動載入）

## ⚡ Boot Sequence

Session 啟動 → 讀 PROGRESS.md → 輸出 Boot Report → **停止等 Ticket**。
```
📍 Ticket: [ID] / 無 → 停止
📊 CLAUDE.md [N]行 | PROGRESS.md [N]行
⚠️ Blockers: [列出或「無」]
```

---

## 核心鐵律（6 條）

### 1. 確認再動手
- 不假設。有歧義就問，不要靜默選擇。
- 如果存在多種解讀，全部列出——不要替 user 挑。
- 如果有更簡單的做法，推回來。推回是職責不是越權。
- 修改核心邏輯前，先查閱 SSOT（`plans/architecture/SSOT_v4_0.md`）。
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

### 4. 越權禁令
a) 無 Ticket 不動手。格式：`D[X]-T[Y]` / `P0-[NAME]` / `INFRA-[XX]` / `HOTFIX-[XX]` / `V16-[XX]`。
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
```
0.Resume → 1.Plan(Gemini) → 2.Code(/uc) → 3./qa → 4./sv
→ 4.5.cross_review(Codex) → 5.Traycer → 6.Git → 7.KAIROS(節點時)
```

## Session Health
≤10 turns 🟢 / 11-18 🟡 / ≥19 🔴 立即 /compact。單則 >200 行代碼 +2 turns。

## Skills
`/sv` 語意審查 · `/qa` Build+Test+靜態掃描 · `/uc` 臥底模式 · `/compact` 壓縮交接 · `/dream` KAIROS 回顧(D2/D6/D10/D13/D15)

## SSOT
- 策略: `plans/architecture/SSOT_v4_0.md` · 佈局: `plans/architecture/Cueshot_Layout_Spec_v2_2.md`
- 進度: `PROGRESS.md` · 歸檔: `PROGRESS_ARCHIVE.md`

## Cross-Review
diff 跨三層 or 涉及 CameraViewModel.kt → `xhigh`。純 domain 新建 → `high`（預設）。

## Path-Scoped Rules（匹配檔案時自動載入）
- `.claude/rules/android-conventions.md` — 錯誤防禦/UDF/i18n/Bash/命名/Git
- `.claude/rules/camera-constraints.md` — VCM/ML Kit 互斥/CameraX
- `.claude/rules/compose-ui.md` — Ambient UI/預設值陷阱/Insets
- `.claude/rules/decommission.md` — 四層退場/SSOT 遷移
- `.claude/rules/state-machine.md` — 狀態機時序/Cooldown
- `.claude/rules/critical-files.md` — 禁刪檔案
- `.claude/rules/kairos-lessons.md` — K1-K3 實戰規則

---
**工作指標**：diff 中不必要的變更更少、因過度複雜導致的重寫更少、釐清問題在實作前而非犯錯後。
