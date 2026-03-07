# **AI 智能攝影師專案：終極防禦型混合工作流 (Ultimate Defensive Hybrid Workflow)**

## **1\. 核心理念：從「除錯」走向「防禦」與「閉環」**

傳統的 AI 開發流程（生成 \-\> 報錯 \-\> 修復）在複雜的 Android Camera / UI 架構中極易引發「狀態污染」與「硬體衝突」。

本工作流導入 **「左移（Shift-Left）」** 與 **「零信任驗證 (Zero-Trust Verification)」** 概念，將除錯心力前置於「系統設計階段」，並在實作後強制進行比對。透過 Traycer 與 Gemini 的雙重審查及最終驗收，形成完美的 **PDCA (Plan-Do-Check-Act) 防禦閉環**，確保交給 Claude Code 的每一行需求都是無懈可擊且如實執行的。

## **2\. 終極防禦四部曲 (The 4-Step Defensive Pipeline)**

### **Step 1: 宏觀防禦與邊界確立 (Traycer) \- \[Plan\]**

* **角色**：Technical Architect (技術架構師) / 系統導航員
* **使用模式**：
  * **新功能開發** \-\> 使用 **Epic Mode** (+ 預設 Agile Workflow)
  * **還技術債/解 Bug** \-\> 使用 **Refactoring Workflow**
* **核心動作**：
  1. **釐清現況**：Traycer 掃描專案，建立既有代碼的 Mental Model（例如尋找 CameraControlAdapter）。
  2. **驗證問題**：確認使用者的需求是否符合現實（例如：抓出「前鏡頭定焦卻要求實作 AF」的矛盾）。
  3. **確立邊界**：明確定義 Scope IN 與 Scope OUT，防止範圍蔓延 (Scope Creep)。
* **輸出產物**：一份經過驗證、邊界清晰的 **L1 需求與現況報告 (Validated L1 Context)**。

### **Step 2: 微觀防禦與底層架構審查 (Gemini 2.0/1.5 Pro) \- \[Design\]**

* **角色**：Principal Android Architect (矽谷頂尖 Android 首席架構師)
* **核心動作**：帶著 Traycer 產出的 L1 報告，進行「破壞性沙盤推演」。強制 Gemini 執行以下四項檢查：
  1. **軟硬體交叉矩陣**：過濾硬體限制（定焦鏡頭、無閃光燈、低階機型）。
  2. **狀態機防禦 (FSM)**：強制使用 Kotlin Sealed Class，消滅跨狀態的變數污染。
  3. **單向資料流 (UDF) 斷點檢查**：確保 UI 渲染與硬體 Callback 徹底解耦。
  4. **破壞性提問 (Edge Cases)**：模擬生命週期中斷、畫面狂點等極端情境。
* **輸出產物**：一份極度嚴謹的 **L2 實作指南 (Implementation Guide)**（包含具體架構、防坑點與數學演算法藍圖）。

### **Step 3: 終端落地與分步實作 (Claude Code) \- \[Do\]**

* **角色**：The Finisher / Senior Developer
* **核心動作**：
  * 讀取 Gemini 產出的 Implementation\_Guide\_L2.md。
  * **嚴格禁止大重寫 (Big-bang rewrite)**，強制要求 Claude Code 遵循文件中定義的 Step 1, Step 2 分階段實作。
  * 實作完成後，自動運行 ./gradlew build 確保無編譯錯誤。
  * **暫停等待驗收**：在通過 Step 4 驗證前，不隨便修改其他架構或合併代碼。

### **Step 4: 驗收閉環與知識沉澱 (Traycer Verify) \- \[Check & Act\]**

* **角色**：Quality Gate (品質守門員)
* **使用模式**：使用 **Verification Workflow**
* **核心動作**：
  * **雙向核對**：讀取 L1 (分析) 與 L2 (方法)，並檢視 Claude Code 剛才修改的程式碼。
  * **抓出偏移 (Implementation Drift)**：檢查目標狀態與防禦機制（如 Sealed Class）是否如實建立？是否有預期外的行為改變？
  * **提供最終裁決 (Three Paths)**：
    * **Path A (批准 Approve)**：實作完美符合計畫。確認完成。
    * **Path B (開立修復票據 Fix Tickets)**：發現偏移或遺漏，開立具體 Fix Ticket 交回給 Claude Code 修正，修正後再次驗證。
    * **Path C (升級重新規劃 Escalate to Re-plan)**：發現底層不可抗力限制（如 API 棄用），中止驗證並帶回 Step 2 重新規劃。
* **知識沉澱 (Knowledge Consolidation)**：若走向 Path A，由 Claude Code 或開發者將本次踩過的坑與重大決策，記錄至 **devspec/feature\_record\_Backlog.md** 的 **Critical Warnings** 段落，建立 Single Source of Truth (SSOT)。

## **3\. 優化提示詞生成器 (GUI Tool Updates)**

建議使用更新為四個階段的 tools/prompt\_generator\_gui.pyw 腳本，Prompt 生成邏輯如下：

**Prompt 1 \- 給 Traycer 的啟動指令 (宏觀防禦)**

我需要開發/重構 \[填入功能名稱，例如：混合對焦系統\]。
請啟動你的 \[Epic Mode / Refactoring Workflow\]。在我們規劃任何代碼前，請先探索專案中與此相關的現有邏輯，並與我確認：
1\. 你的理解與我的問題是否相符？
2\. 這次修改的 Scope 邊界（什麼做、什麼不做）？
確認共識後，請產出 L1 現況與需求報告。

**Prompt 2 \- 給 Gemini 的架構審查指令 (微觀防禦)**

你現在是一位擁有 10 年經驗的矽谷頂尖 Android 首席架構師。
以下是 Traycer 梳理出的 L1 需求與現況：
\[貼上 Traycer 的結論\]

請對此進行「嚴格的技術設計審查」。請根據：1. 軟硬體能力交叉矩陣 2\. 狀態機防禦 (Sealed Class) 3\. UDF 解耦 4\. 破壞性邊界條件，來拷問這個需求。
審查完畢後，請為我產出一份給 Claude Code 閱讀的英文 \`Claude\_Implementation\_Guide\_L2.md\`，內容必須包含防抖、解耦與狀態隔離的具體規範。

**Prompt 3 \- 給 Claude Code 的實作指令 (終端落地)**

請讀取 \`Claude\_Implementation\_Guide\_L2.md\`。
請嚴格遵守文件中的架構規範（包含 Sealed Class 設計與硬體 Bypass 邏輯）。
請以「小步快跑」的方式實作，執行完第一步後請暫停並向我總結，待我確認後再進入下一步。
完成所有實作後，先編譯專案確保無錯誤，並等待 Traycer 的最終驗收 (Quality Gate)，先不要急著將紀錄寫入 Log。

**Prompt 4 \- 給 Traycer 的驗收指令 (驗收閉環)**

請切換為 Quality Gate 驗證角色 (Verification Workflow)。
請讀取 L1 需求報告與 L2 架構指南，並檢視 Claude Code 剛才修改的程式碼。
請檢查：
1\. 目標狀態與防禦機制（如 Sealed Class）是否如實建立？
2\. 是否有發生 Implementation drift (代碼偏移) 或未預期的行為改變？
請提供你的最終裁決：Path A (批准), Path B (開立 Fix Tickets) 或 Path C (遇到死胡同需重新規劃)。
若為 Path A，請指示 Claude Code 將重大防禦決策寫入 \`devspec/feature\_record\_Backlog.md\` 的 Critical Warnings 中。

## **4\. 團隊紀律與最佳實踐**

1. **SSOT 絕對優先**：任何工具在修改相機核心代碼前，**必須**先閱讀 feature\_record\_Backlog.md 裡的 Critical Warnings。
2. **拒絕「差不多」**：在 Step 1 與 Step 2 中，如果 AI 沒有發現任何潛在風險就說「沒問題可以做」，請退回要求它重新執行「破壞性提問」。優秀的架構審查一定會找出漏洞。
3. **無縫對接與驗證**：這套流程將大幅減少「人工搬運的細節遺漏」，並透過最後一關 Traycer Verification 確保實作 100% 吻合架構，徹底消滅 AI 開發中的「盲目自信幻覺」。

## **5\. 實機驗測與異常處理迴圈 (Device Testing Feedback Loop)**

通過 Step 4 的靜態驗證後，代表代碼邏輯安全，可以打包至實機進行物理驗測。但在實機測試中若發現異常（Logcat 報錯、非預期的硬體行為或視覺卡頓），請嚴格遵守以下紀律：

1. **絕對禁止：直接讓 Claude Code 分析 Logcat 進行搶修**
   將報錯直接丟給 Claude Code 會導致其為了消滅 Bug 而隨意添加 try-catch 或繞過防禦機制，造成嚴重的**架構偏移 (Implementation Drift)**，並破壞好不容易建立的 SSOT。
2. **標準做法：退回 Step 1 重新啟動防禦閉環**
   將實機測試遇到的異常描述與 Logcat 錯誤訊息收集起來，**帶回給 Traycer (Step 1\)**，並告訴它：「實機測試發生異常，請扮演 Refactoring 角色幫我釐清這個 Edge Case。」這會觸發更新 L1，接著讓 Gemini (Step 2\) 產出包含新防禦策略的 L2 指南，最後再讓 Claude (Step 3\) 安全實作。
3. **例外情況**：僅有純粹的語法錯誤 (Syntax Error) 或與狀態機完全無關的微小 Typo，才允許 Claude Code 直接修復。任何牽涉到 Lifecycle、Camera Callback 或狀態轉換的異常，都必須走完整個 PDCA 閉環。
