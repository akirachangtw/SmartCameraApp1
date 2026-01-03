# 混合工作流自動觸發判斷清單（Checklist）

**適用對象**
- VSCode with Claude Code 為主要實作與檢視工具
- ChatGPT 作為規劃、彙整與風險掃描輔助
- 適用於 CameraX / AI / 即時影像 / 架構型專案

---

## Level 1｜規劃檢視自動啟動（Always-on）
> 每一次開發 / 修正規劃皆必須執行

### 觸發條件（任一即成立）
- ☐ 新功能構想
- ☐ Bug 修正討論
- ☐ 行為異常但尚未定位
- ☐ 架構、流程或資料流調整構想
- ☐ 「我想確認這樣改對不對」

### 必做事項
- ☐ 彙整問題背景（What / Why / Scope）
- ☐ 拆解變更影響範圍（功能、模組、層級）
- ☐ 列出潛在風險（至少 1–3 項）
- ☐ 產出結構化規劃摘要

### 產出物
- ☐ 規劃摘要（Markdown / Issue）

---

## Level 2｜可行性檢視自動啟動（Conditional）
> 中高風險改動必須啟動

### 自動升級條件（任一即成立）
- ☐ 涉及 CameraX / Preview / ImageAnalysis
- ☐ 涉及模擬器 vs 實體裝置差異
- ☐ 涉及 FPS / Memory / CPU / GPU
- ☐ 涉及模式切換（Live / Static / Debug）
- ☐ 涉及既有高風險模組
- ☐ 修改兩個以上檔案
- ☐ 非純 UI / 文案調整

### 必做事項
- ☐ 將規劃轉為交接格式
- ☐ Claude Code 檢視 repo 現況
- ☐ 評估 API、依賴與衝突
- ☐ 給出可行性結論（可行 / 不可行 / 需調整）

### 產出物
- ☐ 可行性結論與修正建議

---

## Level 3｜實作與驗證自動啟動（Strict）
> 高成本，僅在必要時啟動

### 啟動條件（需全部符合）
- ☐ Level 2 結論為可行
- ☐ 變更範圍明確、可最小 diff
- ☐ 回滾方案清楚
- ☐ 驗證方式已定義

### 必做事項
- ☐ 小步提交（atomic commits）
- ☐ Build / 測試 / 實機驗證
- ☐ 紀錄結果與差異

### 產出物
- ☐ Commit / PR
- ☐ 驗證紀錄或回滾說明

---

## 快速判斷速查
- 想改東西 → Level 1
- 碰相機 / 效能 / 模式 → Level 2
- 可行 + 有驗證 → Level 3

---

**建議存放路徑**
`docs/workflow/hybrid_workflow_checklist.md`
