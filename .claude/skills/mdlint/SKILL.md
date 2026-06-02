---
name: mdlint
description: 掃描並自動修復專案 Markdown 文件的 linting warnings（MD022/025/026/029/032/036/040/055/060）。日常維運用，可指定單一檔案或預設掃描 .claude/ 目錄。
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Description

掃描並自動修復專案 Markdown 文件的 linting warnings。針對本專案常見的 10 類 MD 規則執行確定性修正，不改變文件語意內容。

# Prompt

你現在是本專案的「Markdown Linting 自動修復器」。請**嚴格依序**執行以下步驟。

## 使用方式

- `/mdlint` — 預設掃描 `.claude/CLAUDE.md`
- `/mdlint <檔案路徑>` — 掃描指定 Markdown 檔案（相對路徑）
- `/mdlint all` — 掃描 `.claude/` 目錄下所有 `.md` 檔案

---

## Step 1：確認目標檔案

依使用者參數決定掃描範圍：

```text
無參數       → 目標 = .claude/CLAUDE.md
<路徑>       → 目標 = 指定路徑
all          → 目標 = .claude/**/*.md（排除 skills/ 子目錄下的 SKILL.md）
```

**MUST** 用 Read 工具讀取目標檔案完整內容，記錄行號。

---

## Step 2：套用確定性修正規則

依下列優先順序逐一掃描並修正。每條規則若有命中，記錄「行號 + 修正類型 + before/after」。

### Rule A — MD040 fenced code block 缺語言標籤

- 掃描所有 ` ``` ` 開頭（無語言標籤）的 fence。
- 判斷內容類型，套用適當語言標籤：
  - 流程步驟 / 純文字說明 → `text`
  - Shell 指令 → `bash`
  - Kotlin 程式碼 → `kotlin`
  - JSON → `json`
  - 已有標籤者 → 跳過

### Rule B — MD025 多個 H1 (`#`) heading

- 第一個 `#` 保留為 H1。
- 後續的 `#` 標題改為 `##`（降一層）。
- 若原文件整體結構需要，對應子標題層級亦同步下移（`##`→`###`，`###`→`####`）。

### Rule C — MD026 heading 尾端標點符號

- 掃描所有 heading（`#` 開頭）。
- 若尾端為 `:`、`。`、`！`、`？` → 移除。
- 英文 `:` 在技術術語中（如 `Type: Foo`）為 heading 內容的一部分時保留。

### Rule D — MD022/MD032 heading 或 list 前後缺空行

- **heading 後缺空行**：若 `##`/`###` 標題的下一行直接是清單項目（`-` 或數字），插入一個空行。
- **list 前後缺空行**：若有序或無序清單前後緊接非清單段落，插入空行。
- 有序清單的 continuation 縮排（4 空格）視為清單的一部分，不插入空行。

### Rule E — MD029 有序清單非標準前綴

- 掃描獨立的有序清單區塊（與上方有序清單之間有空行或 `---` 分隔）。
- 若起始數字非 `1`（如 `11.`、`17.`），視為「跨區塊 Rule 編號」場景：
  - 將有序清單轉換為無序清單（`-`）。
  - 在 `**` bold 文字前插入 `**Rule N —**` 前綴（N = 原始數字），使 Rule 編號保留在文字中。
  - 範例：`11. **骨架 Ticket...**` → `- **Rule 11 — 骨架 Ticket...**`

### Rule F — MD036 粗體段落代替 heading

- 掃描獨立行且格式為 `**...**` 的段落（前後有空行，且內容語意為章節標題）。
- 轉換為適當層級的 heading（通常 `###`）。
- 若粗體段落夾在正文中（非獨立行）→ 跳過，不轉換。

### Rule G — MD060/MD055 table pipe 格式

- 掃描表格的 separator 行（如 `|---|---|`）。
- 每個 cell 補上空格：`|---|` → `| --- |`。
- 若 separator 行前後的表格 data 行已有空格 → 一致化。

### Rule H — MD056 table 後段落誤讀

- 若表格最後一行之後，緊接（無空行）一個不含 `|` 的段落，linter 可能誤判為損壞的表格列。
- 修復：在表格最後一行與後續段落之間插入空行。

---

## Step 3：執行修正

使用 Edit 工具逐一套用 Step 2 識別的修正。

**重要限制**：
- **MUST** 只修改格式，不修改任何文字內容、規則語意、連結路徑。
- 若某修正可能影響語意（如 MD029 Rule 編號轉換），**MUST** 先輸出預覽讓使用者確認。
- 每次 Edit 前，若檔案 > 200 行，**MUST** 先 Read 操作區段（±10 行）確認精確上下文（Rule 18）。

---

## Step 4：回報結果

```text
=== mdlint Report ===
目標: <檔案路徑>

修正項目:
  ✅ MD040 L37: ```  →  ```text
  ✅ MD025 L84: # 🛡️...  →  ## 🛡️...
  ✅ MD026 L115: ## 鐵律 4...:  →  ## 鐵律 4...
  ✅ MD029 L217-219: 11./12.  →  bullet Rule 11/12
  ... (逐條列出)

跳過項目（需人工判斷）:
  ⚠️ MD036 L60: **...** 夾在正文段落中，未自動轉換

剩餘 warnings（linter 設定差異或語意保留）:
  ℹ️ MD025 L1: 第一個 H1 — 正常，保留

→ 修正完成。建議 git diff 確認後提交。
```

---

## 注意事項

1. **MUST** 禁止越權：本 Skill 僅修復格式，**MUST** 不修復任何 `.kt`/`.xml` 程式碼。
2. **SHOULD** 原子化：修正完成後提示使用者執行獨立的 `[Docs]` commit（鐵律 2）。
3. **SHOULD** Session Health：參照 CLAUDE.md §Session Health 規則，在 Report 末尾附上 turns 狀態。
