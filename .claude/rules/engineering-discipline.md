---
description: 工程紀律 Rule 7–12 — Intent-Driven、確定性用代碼、測試驗 intent、Checkpoint、衝突不混搭、失敗要大聲（自 CLAUDE.md 移出，編號沿用）
---

# 工程紀律（Rule 7–12）

## 7. Intent-Driven

將任務轉化為可驗證目標，迭代直到成功：
- 「加驗證」→「寫 invalid input 測試，然後讓它通過」
- 「修 bug」→「寫重現測試，然後讓它通過」
- 「重構 X」→「確認重構前後測試都通過」

強成功標準讓你能獨立迭代。弱標準（「讓它動」）需要不斷確認。

## 8. 確定性工作用代碼

routing / retry / status-code / deterministic transform → 用 code。
只在 classification / drafting / extraction 場景用 model 判斷。
Status code 已能回答的問題，code 回答。

## 9. 測試驗 intent

測試編碼「為什麼」行為重要，不只驗「做了什麼」。
若 diff 含 return 值語義變更或 state 轉移路徑變更 → 測試必須覆蓋新舊值邊界。
無法在 business logic 變更時失敗的測試 = 錯誤的測試。

## 10. 每步 Checkpoint

多步驟任務每完成一步 → 回報 done / verified / remaining。
無法回述當前狀態 → 停下來重述。不從無法描述的狀態繼續前進。

## 11. 衝突不混搭

兩種 pattern 矛盾 → 選較新 / 較多測試覆蓋的那個，標記另一個待清。
混合兩種 pattern 的代碼 = 最差的代碼。發現矛盾 → 大聲說，不靜默各取一半。

## 12. 失敗要大聲

不確定就說。「完成」但跳過項目 = 沒完成。「測試通過」但跳了測試 = 沒通過。
預設：暴露不確定性，而非隱藏它。
