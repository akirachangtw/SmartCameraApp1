# **獨立開發者：低成本高防禦 gstack 工作流 (Cost-Optimized AI Workflow)**

**版本**: v1.2 (Next-Gen AI Edition)

**適用對象**: 資源受限、需嚴格控管 API 成本與每日/每月模型配額的獨立開發者。

**核心理念**: 借鑑 Garry Tan 的 gstack 架構，透過「本地角色化指令 (Slash Commands)」與「免費大腦前置過濾」，將昂貴的 AI 呼叫次數降至最低。**「零重工 (Zero Rework) 就是最大的省錢。」**

## **⏳ 雙週配額生存指南 (Quota Survival Guide)**

**【當前約束現實】**

假設 Traycer 帳單重置日為下個月 3 號，若在月中僅剩 $20 美金（約 80 次 $0.25 執行），代表**每日僅有 4.4 次**的 Traycer 執行扣打。同時必須兼顧 Claude Pro / ChatGPT Plus 每 3\~5 小時的對話次數限制。

**【極限配速策略 (Pacing Strategy)】**

1. **Gemini Advanced (3.1 Pro) (防波堤)**：它擁有突破性的巨量 Token 與相對最寬鬆的配額。**任何報錯、Log 分析、幾千行的 Code 追蹤，第一時間全部丟給 Gemini。** 絕對不要用 Claude Pro 或 GPT Plus 去做「大海撈針」的 Debug，會瞬間耗盡對話次數。  
2. **ChatGPT Plus (精密計算機)**：利用其強大的 reasoning (GPT-4.5 / o3-mini / o1) 處理純邏輯與數學（如座標矩陣轉換）。**一次只問一個精確的數學/演算法問題**，拿完 Function 就走，不要跟它閒聊架構。  
3. **Claude Pro (Opus/Sonnet 4.6) / CLI (無情打字機)**：利用 .claude/commands/ 在本地端進行單一檔案的修改。**嚴格限制 Context 丟入量**，不要把整個專案餵給它，只餵 CameraViewModel.kt 和 CLAUDE.md，以保護其強大但有限的配額。  
4. **Traycer (最終法官)**：**每天只准呼叫 2\~4 次**。只在「大架構拆票 (Planner)」與「最終程式碼合併前的嚴格驗收 (Strict Verification)」時使用。禁止用 Traycer 來修編譯錯誤 (Syntax Error)。

## **💰 成本路由策略 (Cost Routing Strategy)**

為了極致壓縮成本，專案開發必須嚴格遵守以下 AI 分流策略：

1. **\[配額最寬鬆\] Gemini Advanced (大腦/架構師)**：  
   * **職責**：架構設計、Bug 根因分析、SSOT 真理文件撰寫、Prompt 極限壓縮。  
   * **原則**：**絕對不讓付費 API 去「猜」需求**。將所有上下文先交給大腦過濾，產出最精簡的「實作交接單」。  
2. **\[對話次數限制\] Claude Pro (Opus/Sonnet 4.6) / CLI (.claude/commands/)**：  
   * **職責**：苦力實作、本地端自我驗收 (/sv)、本地編譯與 Log 檢查 (/qa)。  
   * **原則**：利用本地終端機腳本進行自動化防禦，把低級錯誤（如忘記 import、變數拼錯）在本地端就打掉，避免浪費 Traycer 驗收次數。  
3. **\[硬性金錢限制\] Traycer ($0.25/run)**：  
   * **職責**：最終的架構級 Code Review、複雜邏輯的最終驗收 (Strict Verification)。  
   * **原則**：**法官不負責幫忙修 Bug**。只有當本地端所有腳本與 /sv 都通過、且 ./gradlew assembleDebug 編譯成功後，才能刷卡呼叫 Traycer 進行結案蓋章。

## **🛠️ gstack 本地化防禦矩陣設定 (Local Defense Matrix Setup)**

在任何新專案的根目錄，建立以下檔案結構以啟動本地防禦網：

### **1\. 專案最高憲法 (CLAUDE.md)**

Claude Code 啟動時會自動載入此檔案（免除每次貼 Prompt 的 Token 浪費）。

\# \[專案名稱\] 開發紀律與真理

\#\# 🛑 零容忍執行紀律 (Zero-Tolerance Constraints)  
1\. \*\*零偷懶政策\*\*：絕對禁止留下 \`// TODO\`、\`return null // implement later\`，必須真實接線。  
2\. \*\*焦土政策\*\*：重構時，必須主動搜尋並刪除舊管線的廢棄變數與 UI 元件。  
3\. \*\*錯誤防禦\*\*：必須處理可能導致 Crash 的 null 狀態與陣列越界。  
4\. \*\*架構純潔\*\*：嚴格遵守 UDF (Unidirectional Data Flow)，狀態層與 UI 層絕對分離。

### **2\. 本地無情法官 (.claude/commands/sv.md)**

將基本的殘骸掃描與驗證工作轉移到本地，節省 Traycer 費用。

\# Description  
執行本地端的無情法官初步驗收，掃描代碼殘骸與偷懶行為。

\# Prompt  
你現在是本專案的「本地 Reviewer」。請針對剛修改的範圍，強制執行反向驗證：  
1\. \*\*殘骸掃描 (Orphan Check)\*\*：確認舊邏輯、隱藏的 feature flag 或廢棄 import 是否已徹底刪除。  
2\. \*\*Mock 偵測 (Anti-Lazy Check)\*\*：檢查是否有塞入無意義的預設值或 \`TODO\`。  
3\. \*\*UI 互斥 (State Exclusive)\*\*：檢查 UI 渲染是否有雙重重疊風險，\`return\` 防線是否穩固。

若發現缺失，請條列並主動修復。若一切完美，請回報：「本地 SV 驗收通過，準備提交 Traycer 終審。」

### **3\. 本地自動化 QA 守護 (.claude/commands/qa.md)**

結合腳本自動化，讓 AI 自己編譯、自己看 Log，避免人類反覆測試的摩擦力。

\# Description  
啟動本地端編譯與除錯流程 (建置、測試、Log 分析)。

\# Prompt  
你現在是本專案的「自動化 QA」。請執行以下步驟：  
1\. 執行專案的編譯指令 (例如 \`./gradlew assembleDebug\` 或 \`npm run build\`)。  
2\. 若編譯失敗，請立刻擷取錯誤 Log 並自行提出修復方案，禁止沉默失敗。  
3\. 若編譯成功，請檢視相關的 Warning 並評估是否需要立即清理。  
請回報最終的編譯狀態與除錯建議。

## **🔄 標準作業流程 (SOP) 實戰範例**

1. **\[Plan\]** 將報錯或需求貼給 **Gemini Advanced**，取得壓縮後的 Prompt (不消耗嚴格配額)。  
2. **\[Code\]** 在終端機開啟 **Claude Code**，貼上 Prompt 讓其在本地實作。  
3. **\[Local Check\]** 實作完成後，在終端機輸入 /sv 讓 Claude 自我審查。  
4. **\[Local Build\]** 輸入 /qa 讓 Claude 確認編譯成功。  
5. **\[Final Gate\]** 只有當 /sv 和 /qa 都完美通過後，才開啟 **Traycer** 花費 $0.25 執行最終的 /strict\_verification (每日限 2\~4 次)。