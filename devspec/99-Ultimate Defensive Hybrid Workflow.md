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

![][image1]給 Traycer 的啟動指令 (宏觀防禦)

我需要開發/重構 \[填入功能名稱，例如：混合對焦系統\]。  
請啟動你的 \[Epic Mode / Refactoring Workflow\]。在我們規劃任何代碼前，請先探索專案中與此相關的現有邏輯，並與我確認：  
1\. 你的理解與我的問題是否相符？  
2\. 這次修改的 Scope 邊界（什麼做、什麼不做）？  
確認共識後，請產出 L1 現況與需求報告。

![][image2]給 Gemini 的架構審查指令 (微觀防禦)

你現在是一位擁有 10 年經驗的矽谷頂尖 Android 首席架構師。  
以下是 Traycer 梳理出的 L1 需求與現況：  
\[貼上 Traycer 的結論\]

請對此進行「嚴格的技術設計審查」。請根據：1. 軟硬體能力交叉矩陣 2\. 狀態機防禦 (Sealed Class) 3\. UDF 解耦 4\. 破壞性邊界條件，來拷問這個需求。  
審查完畢後，請為我產出一份給 Claude Code 閱讀的英文 \`Claude\_Implementation\_Guide\_L2.md\`，內容必須包含防抖、解耦與狀態隔離的具體規範。

![][image3]給 Claude Code 的實作指令 (終端落地)

請讀取 \`Claude\_Implementation\_Guide\_L2.md\`。  
請嚴格遵守文件中的架構規範（包含 Sealed Class 設計與硬體 Bypass 邏輯）。  
請以「小步快跑」的方式實作，執行完第一步後請暫停並向我總結，待我確認後再進入下一步。  
完成所有實作後，先編譯專案確保無錯誤，並等待 Traycer 的最終驗收 (Quality Gate)，先不要急著將紀錄寫入 Log。

![][image4]給 Traycer 的驗收指令 (驗收閉環)

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

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAmwAAAArCAYAAADFV9TYAAAC1UlEQVR4Xu3cP4tUVxgH4FEX8X8QXBbXnR13WVjZQgIhIggWwrZWBkUhKVKESCwMJCGgEAJJJYJIQLBTsFAI6bawsFHYwiLlYmXlB/AL6O917sHJJaQKWszzwMt9zz3nzt3yxzkzOxgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP9heXn5k9Fo9DL1JrXV9S+PRn/txzIcDj/v3wMAmCrJZicT0l638dzc3N6MNyfXfCDb8t7ra2trO2uQ/mLqXupSfyEAwFRJILqZetK792Zy/CEkOB5bXFxcn7y3tLS0KrABAFMvgehVwtI33bB2uS5mfGFhYWF3AtSPGT9O/VDBKXW4W3Mma86nbtXcysrKbK6XU49SNyp45bqR6++pc+mfTrzv69T1msvzX+XZhfqsWpO6lv7btlZgAwAYjHfTUrcToH7K9UHqfm5vS1g6MRwOz2b8fHV1dX+uf+TeqMJcC2C1K5Z+s9ZNHK3OdJ/7d9bPVZ/PvlPX+s5c+oOpvxIIj8zPz+/Jc1e79Y/Gf9F7AhsAMPUSmA4lEN2t4NSfa/pBKmHrRYLU8a7/LPOvunV1tHqzrRtNHLOm32p9PZvnTnV9BcBfBuNdu2dtTSOwAQBTL8Hpyxae/k0Fqto9a+OEq121W1bHpd34YQLVjepH41+Xnqy+guDR7pi1dtQy933qcu241VFoez73fq3j1Np5y/qf0x/I/JX2PoENAJh6CUm3KpT17zeZO91205oEqD8rhKWdqcDWjj1rRy3hbr762nlr4S3XT7uduO8y3JHrRv0StXvm3dFq/Q1Zs576ot45ftM4sFWobGMAAHrav9jom52d3TfovqvW9Nb+Y6520FpfR5+1wzZ5r78GAICPY6Y74vwt/fb+JAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADwv3oLKHd/Mmvmrc0AAAAASUVORK5CYII=>

[image2]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAmwAAAArCAYAAADFV9TYAAADE0lEQVR4Xu3cz4uVVRgH8Ds1SD/MCBqGpjtzvcPAxCwiCCIIWghuXRWKQS2CoqEIgwpBQQRrI4JEELQMXCREC0GiRS0UXLhwJdLKlX+A/4B9H+85cH2JVjGB9/OBw/ucX3fe5Zdz7p3RCAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD4F5ubm89PJpO7aQ/S7rT67sEYrt1rebeNvMtuXuXYcA4AYKEkEL2ZYHS/91dXV59N/8b8mj2ylL97emdnZ191NjY2vh2Px1sZuzSdTt8eLgYAWBgJRBfS/hiMPZjv74UEx1cS0g5XnYC2nXe4kv4Lo1mQu1/BcrAFAGAxJAzdSxj6uHUrHB2va8jxePx0AtNX6f+e9mXae2kvtTWHsuZo2sWa29raWslzN+1y2vkKXnlezfObtHdSX5v7ex+mna657P8ge8f1WbUm7VTqT/rasr29/VzG/6wr0vlxAICFMZl9f+27BKiv87yU9lOGl6bT6Rvr6+tH0r/ZQtP3GZtUmOsBrE7FUt+odXNXq8vtc29l/WrV+ewf6lnfmatTs7RfEwhfXltbeyb7TrT1l2dv9KiMH0o7nnJpOAcA8NhLYHoxYejHCk7DuW4YpBK2/koQe7XVr2f+XltXV6sX+rrJ3DVr6ju9rr3Z91arKwCeGc1O7a73NV19ny5r3x2OAwAsjISh93t4+icVqOr0rPcTrp6q07K6Lm39nxO0zlc9mf269OH3zCoIHmzXrHWilrkv0nbrxK2uQvv+jJ2t69Q6ecv6k6kPZP6ztu/zafuxQeqP6jSvagCAhZIQdLFC2XC8q8DUT9O6hKxfKoSlXK7A1q8960Qt4W6t6jp56+Etz9faSdyn6T6Z59U6OWt7Hl6t1jtkzeE6TWshbTlzt9P/rVrt6Z8NAMCc/i82hlZWVvaP2nfVusHaR+bqBK3XdfVZJ2zzY8M1AAD8P5ans3/XcS71E8NJAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAOA/9Tfaq4mn3fCfhwAAAABJRU5ErkJggg==>

[image3]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAmwAAAArCAYAAADFV9TYAAADGklEQVR4Xu3cv2udVRgH8ESD+FtEQ2iSm9sbA5EMUvAHhUIHoauToiioICoWHRTaUrAggk6lUkSQdqw4WBAHoYKDi0IHB8filKl/gP9A/D7mHHh9CU6awfv5wMN7znue9953/HLOTRYWAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIB/sLm5+dB0Ot1N7aVutfHu0Rj3Hra1tbVH8hqvp14arwEAzJUEouMJaX/0+crKyn2Z3xz2HJLFfO+FnZ2du2qysbHx2WQyeSzXN2ez2clxMwDA3EhIupT6aXRvbzg/DAmOjyecnapxhba203dsa2trOePvs/bw+BkAgLmQMHQ7wejtNq1drpfrGHJ9ff2ehKSzmf+YOpN6JXWk9TybnhdTl2utharTqeupixW8cr2R66ep5zP+efB9b6Qu1Fqefy3PrtdnVU/qw4zf6b2t/0p9/vAeAMBcme7/fu3zBKhzuX6dupbbi7PZ7JnJZPJc5r9ub28/kOsXuTetMNcDWO2KZXyz+gZHq0vtc39L/0qN89lf1rV+M1c7ZanvEgjXVldX781z77f+6/tvtK/6cu9q6qtMF4drAABzI4Hp0QpFFZzGa90BQer3BLEn2vjJrN9ufXW0eqn3TQfHrBnf6uN6Ns+daOMKgB8t7O/a/dJ7hurdsrabUPjUeA0A4H8vwenVHp4OUoGqds/6POHq7totq+PSNv8mYepijStU1S5bjSsIHm3HrG2n7IPU6dpxq6PQ/nzufVzHnbXzlv7zGT+Y9ffqnVI/1F+KVm/69obvAQAwNxKSLlcoG9/vsnay76Z1CU/fVgjLcKkCWz/2rB21hKrVGtfOWw9vuR5rO3HvZnpnrjfqL1HbM38drdY7pOdU6oX2ndsZv1XfUUen9Z1+xwYAcID+LzbGlpeX719ov1XrRr1/W6sdtD6uo8/aNRveG/eUCnUJgE9XeBveBwDgv7NU4SuB7ZOM7xgvAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/qj8BRoSMrmLUz/wAAAAASUVORK5CYII=>

[image4]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAmwAAAArCAYAAADFV9TYAAAC8ElEQVR4Xu3cPYucVRgG4F2ziMaoBF1W92Nm32VhZQsRhBAQFIS0qRRFQQsLSUgKBaPCLohgqhgIQRAsBQsDYpfCwsZACgvLkCpVfoB/IN6Pcw6OL2IVkmKuCw7nOR/vzJQ357y7S0sAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwP/Y2dl5ejqd3km7l3ar1Xe2Y7z3YRmGYW0ymRwfzwMALIxks5MJaX/28dra2hMZ35zf84As53sP9/f3H52fzNzBxsbGM/NzAAALJYHoctqvo7l78+MHIcHxhclkcmo091x+y9cCGwCw0BKI7iYYfdiGdcr1TsZvb25uPp4AdSHjX9I+SXs37fm25/XseSvtSq3t7u6upj+bdi3tUgWv9NfTX0x7I/Vvc9/3QdphreX59/PsZn1W7Uk7SH2m9qV/rc0fCmwAwEKbzt5fu5oA9Wn6H9K+z/TyMAwntra2Tmf8+97e3pPpv8nctMJcD2B1Kpb6Zu3b/udqdaV97h/1/lnV+exvq6935up9tLSfEwg31tfXj+a5j9r+a7NfNJP5z9r1rMAGACyuBKZnE4i+q+A0XuvGQSph63aC2Iutfjnrd9u+ulq93PdN565ZU9/qdT2b515pdQXAL5Zmp3Y3+p62vly1wAYALLQEo/d6ePovFajq9KyPE64eq9Oyui5t4x8TqC5VPZ39denJqisIbrdr1jpRy9rHaWfbX3xe7M9n7su6Tq2Tt+z/PPVTWT/fv6/tEdgAgMWVkHSlQtl4vsvaq/00rUuA+qlCWMqVCmz92rNO1BLu1quuk7ce3tK/1E7izmV4JP31uupsz/x9tVq/IXtOpb1Z3zn7ppk6gatQNz8HAEAz/hcb3erq6rGl9q5aN9r7r7U6Qet1XX3WCdv83HgPAAAPx8owDHsJbF+lfmS8CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADcV38BHjGCAU+wSUcAAAAASUVORK5CYII=>