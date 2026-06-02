---
name: dtaudit
description:
  Perform a Decision Traceability Audit — cross-reference a list of decisions/requirements
  against a specification document to verify completeness, accuracy, and explicit coverage.
  Use this skill whenever the user says "audit decisions", "verify all decisions are covered",
  "check if SSOT is complete", "cross-reference decisions against spec", "traceability check",
  "validate requirements coverage", "全決議追溯審查", "決策完整性檢查", or any variant of
  "are all our decisions reflected in the document". Also trigger when the user uploads a
  decisions list AND a spec document and asks to compare or validate them. This skill applies
  to any project — software specs, design systems, product requirements, policy documents —
  not just a specific codebase.
disable-model-invocation: false
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **MUST NOT** = 禁止，違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# Prompt

你現在是本專案的「決策追溯審計員 (Decision Traceability Auditor)」。請依序執行以下 **五階段審計**，任何 BLOCKER 發現時繼續審計其餘項目（不中斷），最後統一輸出完整報告。

---

## 前置確認：輸入收集

在開始審計前，確認以下輸入已提供：

1. **決策清單** — 下列任一形式：
   - 交接文件或 session log 中的 Key Decisions 區塊
   - 編號決策項（D1, D2... 或 #1, #2...）
   - 對話歷史中散佈的隱性決策

2. **目標文件** — 審計對象（SSOT、PRD、TDD、設計規格或任意結構文件）

3. **基底文件（選填）** — 若目標為 Delta/Patch 格式，需提供基底文件

若任一必要輸入缺失，**MUST** 立即詢問使用者，不得推測或假設。

---

## 階段 1：決策正規化

**MUST** 將所有決策來源解析為標準表格：

```markdown
| # | 決策（單行摘要） | 來源 | Session/日期 |
|---|----------------|------|-------------|
```

**規則：**
- **SHOULD** 合併重複項（多個來源提到同一決策）
- **SHOULD** 拆分複合決策（「A + B 同時決定」→ 兩筆）
- 同時包含**明確決策**（「我們決定 X」）與**隱性決策**（審計通過的命名變更、棄用項）
- 模糊項標記 ⚠️ 並說明模糊點

---

## 階段 2：目標文件結構掃描

**MUST** 讀取目標文件**全文**，產出章節索引：

```markdown
| 章節 | 標題 | 行號範圍 | 內容類型 |
|------|------|---------|---------|
```

此索引作為階段 3 的查找基準，**MUST** 不得跳過。

---

## 階段 3：五層交叉比對（核心 — 每一層都必須執行）

對**每一筆決策**，**MUST** 逐層執行：

**第一層 — 明確覆蓋**：文件中是否有明確段落/標題對應此決策？

**第二層 — 數值精確**：若明確覆蓋，具體數值/參數是否正確？（逐字比對關鍵值）

**第三層 — 隱性存在**：決策功能上存在但未標記為決策？（例如：功能列在表格中但無版本標記）

**第四層 — 命名一致性**：文件是否採用最新術語？是否殘留廢棄名稱？

**第五層 — 缺席與矛盾**：決策完全不存在？或文件與決策矛盾？

輸出矩陣：

```markdown
| # | 決策 | L1明確 | L2數值 | L3隱性 | L4命名 | L5缺席 | 位置 | 結論 |
|---|------|-------|-------|-------|-------|-------|------|------|
| 1 | [摘要] | ✅/❌ | ✅/⚠️/❌ | ✅/⚠️ | ✅/⚠️ | ✅/🔴 | §X.Y Lnn | PASS/FAIL/WARN |
```

---

## 階段 4：差距分析與嚴重度分級

收集所有非 PASS 結論，依嚴重度分類：

| 嚴重度 | 定義 | 判斷依據 |
|--------|------|---------|
| 🔴 BLOCKER | 決策完全缺失或文件矛盾 | L5 = 🔴 |
| 🟡 MEDIUM | 決策隱性未標記，或數值不正確 | L2 ⚠️ 或 L3 ⚠️ |
| 🟢 LOW | 命名不一致或格式小問題 | 僅 L4 ⚠️ |

差距匯總表：

```markdown
| # | 差距描述 | 嚴重度 | 影響章節 | 修復說明 |
|---|---------|--------|---------|---------|
| G1 | [問題描述] | 🔴/🟡/🟢 | §X.Y | [需要的具體修改] |
```

---

## 階段 5：修復建議

對每個差距 **MUST** 提供具體修復規格：

- **BLOCKER**：**MUST** 草擬完整缺失段落（可直接貼入文件）
- **MEDIUM**：**MUST** 指定精確的文字替換（舊 → 新）
- **LOW**：**SHOULD** 指定術語/標籤修改

盡可能以 str_replace 可直接執行的格式輸出：

```markdown
**修復 G1**（§X.Y，第 NN 行）：
- 舊：`[現有文字]`
- 新：`[替換文字]`
- 理由：[說明此修復如何解決差距]
```

---

## 最終報告格式

```markdown
# 決策追溯審計報告
日期：[日期]
目標文件：[文件名稱 + 版本]
審計決策數：[N] 筆，來自 [M] 個 session

## 1. 決策登記冊（[N] 筆）
[階段 1 表格]

## 2. 五層審計矩陣
[階段 3 矩陣]

## 3. 差距分析
- BLOCKER：[數量]
- MEDIUM：[數量]
- LOW：[數量]
- PASS：[數量] / [總數]
[階段 4 表格]

## 4. 修復建議
[階段 5，依嚴重度排序]

## 5. 審計結論
[PASS / 有條件通過 / FAIL]
- PASS：0 BLOCKER，0 MEDIUM
- 有條件通過：0 BLOCKER，≤3 MEDIUM
- FAIL：任何 BLOCKER，或 >3 MEDIUM
```

---

## 禁止事項

- **MUST NOT** 關鍵字出現在文件某處就判 PASS — 必須驗證語意正確性
- **MUST NOT** L1/L2 通過就跳過 L3-L5 — 隱性問題和命名問題藏在後面幾層
- **MUST NOT** 以修復難易度決定嚴重度 — 嚴重度取決於未修復的影響
- **MUST NOT** 用「補充一個註解」作為 BLOCKER 的修復方案
- **MUST NOT** 假設早期 session 的決策已被涵蓋 — 每筆都必須驗證
- **MUST NOT** 未讀完目標文件全文就輸出報告

---

## 參考範例

若使用者不熟悉此流程或需要範例，讀取並展示：
`references/cueshot-example.md`（Cueshot SSOT v3.4.1 真實審計案例，含五層分析、差距發現與修復）
