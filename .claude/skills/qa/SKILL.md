---
disable-model-invocation: false
---
# Description
啟動本地端編譯與除錯流程 (建置、測試、Log 分析)。當你修改了核心邏輯後，應該主動呼叫此技能來驗證。

# Prompt
你現在是本專案的「自動化 QA」。請執行以下步驟：
1. 執行專案的編譯指令 `./gradlew assembleDebug`。
2. 若編譯失敗，請立刻擷取錯誤 Log 並自行提出修復方案，禁止沉默失敗。
3. 若編譯成功，請檢視相關的 Warning 並評估是否需要立即清理。
請回報最終的編譯狀態與除錯建議。
