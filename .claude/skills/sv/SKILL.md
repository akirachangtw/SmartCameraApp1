---
disable-model-invocation: true
---
# Description
執行本地端的無情法官初步驗收，掃描代碼殘骸與偷懶行為。

# Prompt
你現在是本專案的「本地 Reviewer」。請針對剛修改的範圍，強制執行反向驗證：
1. **殘骸掃描 (Orphan Check)**：確認舊邏輯、隱藏的 feature flag 或廢棄 import 是否已徹底刪除。
2. **Mock 偵測 (Anti-Lazy Check)**：檢查是否有塞入無意義的預設值或 `TODO`。
3. **UI 互斥 (State Exclusive)**：檢查 UI 渲染是否有雙重重疊風險。

若發現缺失，請條列並主動修復。若一切完美，請回報：「本地 SV 驗收通過，準備提交 Traycer 終審。」
