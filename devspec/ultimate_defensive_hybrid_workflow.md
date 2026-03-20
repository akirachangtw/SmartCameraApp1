# **AI 智能攝影師專案：終極防禦型混合工作流 (Ultimate Defensive Hybrid Workflow)**

## **1\. 核心理念：規格驅動與多模型制衡 (Spec-Driven & AI Cross-Review)**

傳統的 AI 開發極易發生「意圖流失 (Losing Intent)」與「代碼偏移 (Code Drift)」。本工作流導入 YouTube 先進的 **「規格驅動開發 (Spec-Driven Development)」** 與 **「自訂工作流 (Custom Workflows)」** 概念。

我們透過不同 AI 模型的特性建立「互相審查矩陣」：

* **Traycer (產品/QA)**：定義意圖、拆解票據 (Tickets)、品質驗收。
* **Gemini (架構師)**：防禦性沙盤推演、定義 Sealed Class 狀態機、架構審查。
* **Claude Code (實作兵)**：嚴格限制範圍的終端落地。

形成完美的 **PDCA (Plan-Design-Code-Act) 防禦閉環**，徹底消滅大重寫 (Big-bang rewrite) 的風險。

## **2\. 終極防禦四部曲 (The 4-Step Defensive Pipeline)**

### **Step 1: 宏觀防禦與意圖收斂 (Traycer) \- \[Plan\]**

* **角色**：Technical Architect (技術架構師) / 系統導航員
* **使用模式**：使用 Traycer 的 **Epic Mode** 或 **Refactoring Workflow**
* **核心動作**：
  1. **釐清現況**：掃描專案，建立既有代碼的 Mental Model。
  2. **規格驅動 (Spec-Driven)**：確保將模糊的想法轉化為明確的約束條件與驗收標準。
  3. **確立邊界**：明確定義 Scope IN 與 Scope OUT，防止範圍蔓延。
* **輸出產物**：一份經過驗證、邊界清晰的 **L1 需求與現況報告 (Validated L1 Context)**。

### **Step 2: 微觀防禦與架構審查 (Gemini 2.0/1.5 Pro) \- \[Design\]**

* **角色**：Principal Android Architect (矽谷頂尖 Android 首席架構師)
* **核心動作**：帶著 L1 報告，對 Traycer 的計畫進行「破壞性沙盤推演」與架構制衡：
  1. **軟硬體交叉矩陣**：過濾底層硬體限制（定焦鏡頭、低階機型）。
  2. **狀態機防禦 (FSM)**：強制定義 Kotlin Sealed Class，消滅跨狀態變數污染。
  3. **單向資料流 (UDF) 檢查**：確保 UI 渲染與硬體 Callback 徹底解耦。
* **輸出產物**：一份極度嚴謹的 **L2 實作指南 (Implementation Guide)**（包含具體架構、防坑點與數學演算法藍圖）。

### **Step 3: 防禦型票據拆解與終端落地 (Traycer \+ Claude Code) \- \[Code\]**

* **角色**：Traycer (Ticket Master) \+ Claude Code (The Finisher)
* **核心動作**：
  1. **票據拆解 (Ticket Breakdown)**：**絕對禁止**讓 Claude 一次看完整份 L2 就開始狂寫。必須先在 Traycer 中根據 L2 指南，拆解出極小範圍的 **Scoped Tickets**（例如：Ticket A 只建 Sealed Class，Ticket B 只套用 Filter）。
  2. **小步落地**：Claude Code 每次只被允許執行「一張 Ticket」。
  3. **暫停等待**：Ticket 實作完成並編譯成功後，暫停動作等待驗收。

### **Step 4: 嚴格驗收閉環與知識沉澱 (Traycer Verify) \- \[Check & Act\]**

* **角色**：Quality Gate (品質守門員)
* **使用模式**：使用 Traycer **Verification Workflow**
* **核心動作**：
  * **雙向核對**：對照 L1、L2 以及剛才的 Ticket，檢視 Claude Code 的實作。
  * **提供最終裁決 (Three Paths)**：
    * **Path A (批准 Approve)**：實作完美，無 Code Drift。
    * **Path B (開立修復票據 Fix Tickets)**：發現偏移，開立具體 Fix Ticket 逼迫 Claude 修改。
    * **Path C (升級重新規劃 Escalate)**：遇到死胡同，退回 Step 2 讓 Gemini 重新規劃。
  * **知識沉澱 (SSOT)**：確認無誤後，寫入 devspec/feature\_record\_Backlog.md 的 Critical Warnings。

## **3\. 打造專屬 Traycer 自訂工作流 (Custom Workflow Setup)**

💡 *此為基於 Traycer 官方教學的進階優化，建議在 Traycer 中建立一個專屬的 **Ultimate Defensive Workflow**，將我們的防禦思維與 Agent Mode 切換直接「硬編碼」進系統中。*

在 Traycer 中建立自訂工作流，並設定以下五個 Command 節點：

1. **Trigger Workflow (入口點)**：收集需求，探索程式碼現況，驗證問題並產出 L1 報告。
2. **Command: architectural\_review \[Planner Mode\]**：扮演架構審查員，整合 Gemini 產出的 L2 Guide 進行防禦性架構對齊與壓力測試，並收斂產出 Tech Plan。
3. **Command: ticket\_breakdown \[Planner Mode\]**：扮演規劃者，強制將 L2 的架構拆解成帶有 Acceptance Criteria (驗收標準) 的極小範圍 Scoped Tickets。
4. **Command: execute \[Planner Mode\]**：扮演執行編排器，管理票據的實作順序、相依性，並自動產生給 Claude Code 的精確交接指令 (Handoff Instructions)。
5. **Command: strict\_verification \[Reviewer Mode\]**：扮演品質守門員，強制比對實作與 L1/L2 規格，並執行 Path A/B/C 驗收閉環邏輯。

## **4\. 優化提示詞生成器 (GUI Tool Updates)**

為了完美對接我們剛才建立的 **Ultimate Defensive Workflow (Traycer 自訂工作流)**，建議將 tools/prompt\_generator\_gui.pyw 產出的 4 步驟 Prompt 同步升級，直接呼叫對應的 Command：

**Prompt 1 \- 給 Traycer 的啟動指令 (Trigger Workflow)**

我需要開發/重構 \[填入功能\]。
請啟動 Ultimate Defensive Workflow 並執行 \`trigger-workflow\`。在規劃代碼前，請先探索專案現有邏輯，並進行 Spec-Driven 的釐清。請與我確認：
1\. 我的需求是否符合現實？
2\. 這次修改的 Scope IN 與 Scope OUT 邊界？
確認後，請產出 L1 現況與需求報告。

**Prompt 2 \- 給 Gemini 的架構審查指令 (微觀防禦)**

你現在是一位擁有 10 年經驗的頂尖 Android 架構師。
這是 Traycer 產出的 L1 需求：\[貼上 L1\]。
請對此進行「嚴格的技術設計審查」。請根據：1. 軟硬體能力矩陣 2\. Sealed Class 狀態機防禦 3\. UDF 解耦 4\. 破壞性邊界條件，來拷問這個需求。
審查完畢後，產出一份給 Claude Code 參考的英文 \`Claude\_Implementation\_Guide\_L2.md\` 防禦架構指南。

**Prompt 3 \- 給 Traycer 的架構匯入與排程指令 (Review & Breakdown)**

請執行 \`architectural\_review\`。這是我準備好的 L2 防禦架構指南：
\[貼上 Gemini 產出的 L2\]
請吸收這份指南並產出正式的 Tech Plan Spec。確認無誤後，請接續執行 \`ticket\_breakdown\` 將其拆解為 Scoped Tickets，最後啟動 \`execute\` 為我產出第一批要交接給 Claude Code 的【精確交接指令 (Handoff Instructions)】。

**Prompt 4 \- 給 Claude Code 實作與 Traycer 驗收 (Code & Verify)**

【第一步：貼給 Claude Code】
請根據以下 Traycer 的交接指令執行任務：
\[貼上 Traycer 產出的 Handoff Instructions\]
請嚴格遵守 Ticket 範圍與 Acceptance Criteria，絕不可進行大重寫。實作並編譯無誤後，請立即暫停並回報。

【第二步：Claude 完成後，貼回給 Traycer】
Claude Code 已完成實作。請執行 \`strict\_verification\`。
請嚴格比對 Tech Plan 與實際代碼，檢查狀態機制是否如實建立？是否有 Implementation drift (代碼偏移)？
請給出最終裁決：Path A (批准), Path B (開立 Fix Tickets) 或 Path C (升級重新規劃)。

## **5\. 實機驗測與異常處理迴圈 (Device Testing Feedback Loop)**

通過 Step 4 的靜態驗證後，打包至實機進行物理驗測。若實測發現異常（Logcat 報錯、非預期的視覺卡頓），請嚴格遵守紀律：

1. **絕對禁止：讓 Claude Code 直接分析 Logcat 搶修**
   這會破壞好不容易建立的 SSOT 與架構，導致意圖流失。
2. **標準做法：退回 Step 1 重新啟動防禦閉環**
   將 Logcat 與異常行為帶回給 Traycer (Step 1)，說明：「實測發生異常，請幫我釐清這個 Edge Case，並拆解修復 Ticket。」這將觸發更新 L1 \-\> Gemini 審查 L2 \-\> Claude 再次安全實作的完整 PDCA。
3. **例外情況**：僅有純粹的 Syntax Error 或與架構無關的微小 Typo，才允許 Claude 直接修復。

### **雙機測試交接流程 (Home NB ↔ Work NB)**

實機測試需跨機器執行時，使用以下標準交接工作流：

```
Home NB (開發)  →  GitHub  →  Work NB (實機測試)  →  GitHub  →  Home NB (查看結果)
  Step 1: Push           Step 2: Pull     (執行測試)  Step 3: Push    Step 4: Pull
```

**工具**：`python tools/test_handoff_gui.pyw`
**完整 SOP**：`devspec/99-test_handoff.md`

**結果判斷**：
| 結果 | 下一步 |
|------|--------|
| PASS | 繼續下一個 Ticket 或進行 Merge SOP |
| PARTIAL / FAIL | 帶 Logcat 回 Step 1 (Traycer `trigger-workflow`) 重啟 PDCA |
