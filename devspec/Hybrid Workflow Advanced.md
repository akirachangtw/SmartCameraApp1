# **AI 智能攝影師專案：混合工作流 (Hybrid Workflow) 優化建議**

## **1\. 目前流程評估 (Current Evaluation)**

* **優點**：具備多模型共識（Consensus），能有效減少單一 AI 的幻覺。  
* **痛點**：人工在 Traycer、Gemini、Claude Code 之間搬運內容容易遺漏細節，且 Claude Code 若不清楚決策背景，容易寫出衝突代碼。

## **2\. 進階優化策略：以 Traycer 為首的探索流**

建議將工作流調整為 **「探索 \-\> 規劃 \-\> 驗證 \-\> 執行」** 四部曲：

### **Step 0: 探索與定位 (Traycer)**

* **目的**：在寫 L1 之前，搞清楚「現有代碼長什麼樣」。  
* **動作**：使用 Traycer 搜索專案中所有與 CameraX、Focus 或 Lifecycle 相關的實作，並記錄關鍵的類別路徑（例如：MainActivity.kt 裡的 startCamera）。  
* **優化**：將 Traycer 找出的關鍵代碼段落作為輸入，餵給您的 Python 腳本。

### **Step 1: L1 規劃同步 (OpenAI/Gemini)**

* **動作**：基於 Traycer 的探索結果，生成 docs/plan/\<topic\>\_L1.md。

### **Step 2: 交叉 L2 可行性評估 (Gemini 2.0/1.5 Pro)**

* **優化**：讓 Gemini 扮演「安全審查員」。  
* **動作**：將 L1 規劃與 Traycer 發現的既有邏輯同時餵給 Gemini，詢問：「這份新計畫是否會破壞現有的 Lifecycle 管理？」

### **Step 3: Claude Code 終端落地 (The Finisher)**

* **關鍵指令**：  
  \# 利用 Claude Code 讀取所有上下文  
  claude "Read docs/plan/camera\_static\_mode\_L2.md and the code identified by Traycer.   
  Implement the focus locking logic following smart\_camera\_plan\_zh-TW.md."

## **3\. 具體工具分工 (Updated Role Assignment)**

| 工具 | 角色 | 核心產出 |
| :---- | :---- | :---- |
| **Traycer (VS Code)** | **導航員/雷達** | **定位關鍵代碼**、理解複雜依賴關係、提供精確的上下文路徑。 |
| **OpenAI (L1)** | **架構師** | 定義需求、定義介面協議、撰寫 PR Notes 範本。 |
| **Gemini (L2)** | **資深審查員** | 檢查 CameraX 安全性、效能評估、多模型邏輯校閱。 |
| **Claude Code (CLI)** | **執行長 (CEO)** | **執行修改**、合併代碼、運行 ./gradlew build 並修復報錯；**彙整測試結果**（撰寫 HANDOVER_SUMMARY / TEST_REPORT、更新 BRANCH_RELEASES、記錄 Critical Warnings）；**維護開發規範**（更新 feature_record_Backlog.md、devspec 文件、session handoff log）。 |

## **4\. 針對「對焦功能」的優化腳本建議**

使用 **`tools/prompt_generator_gui.pyw`**（Hybrid Workflow 提示詞生成器）自動產生四步驟提示詞。工具透過 Gemini API 根據問題描述生成 Step 0~3 的完整提示，每步驟輸出獨立分頁，可直接複製貼入對應工具。

**啟動方式：**

```bash
# 直接執行（無終端視窗）
pythonw tools/prompt_generator_gui.pyw

# 或透過 VSCode Task：
# Ctrl+Shift+P → Tasks: Run Task → 🤖 Workflow Prompt Generator
```

**優化後的 Prompts 邏輯（由 GUI 自動生成）：**

1. **Step 0 (Traycer)**：「定位與 Traycer 標註的 CameraProvider / Lifecycle 相關代碼。」
2. **Step 1 L1**：「參考 Traycer 探索結果，設計 AF/AE 狀態機規劃文件。」
3. **Step 2 L2**：「檢查此實作是否會與目前的 Lifecycle 綁定衝突。」
4. **Step 3 Claude Code**：「根據 L2 核准計畫，實作並 build 驗證。」

> **工具說明**：詳見 `tools/.gitkeep_critical_files`（Tier 2 — Active Development Tools）。

## **5\. 終極建議：使用 devspec/feature\_record\_Backlog.md 作為 Decision Log**

每次使用 Traycer 發現重大歷史代碼坑點時，請記錄在 **`devspec/feature_record_Backlog.md`** 的 **Critical Warnings** 段落（而非另建 `docs/decision_log.md`）。

**原因**：`feature_record_Backlog.md` 已是本專案的單一真實來源（SSOT），pre-commit hook 會強制要求修改關鍵檔案前先閱讀此文件，能有效防止 Claude Code 在不知情的情況下重寫掉原本為了修正 Bug 而存在的舊邏輯。

**記錄格式（加入 Critical Warnings 段落）：**

```markdown
### ⚠️ Critical Warning — <ComponentName>
- **坑點**：說明曾踩過的坑（例如：在 ON_STOP 呼叫 clearCaptureRequestOptions() 會被 HAL 忽略）
- **正確做法**：記錄修正後的方案（例如：改用 DefaultLifecycleObserver.onStart() + isLocked guard）
- **版本記錄**：v2.11.4-07（commit 5a73ec4）
```