1. 目的 (Objective)
為避免觸發 Claude Pro 的 Session Limit (對話限制) 及 Token 消耗過度，並確保模型在長對話中保持邏輯清晰，開發者必須主動管理對話的生命週期 (Lifecycle)。「適時開啟新對話」 是保持高效率與準確度的關鍵。

2. 重置對話的觸發時機 (Triggers for New Chat)
當遇到以下任一情況時，強制執行「交接程序」並開啟新對話：
系統警告：Claude 介面顯示剩餘訊息提示 (例如 "10 messages remaining until...")。
任務轉換：當一個階段性任務完成（例如：功能開發完成，準備開始 Debug 或寫文件時）。
邏輯衰退：感覺 Claude 開始「變笨」、忘記前面的設定、或產生幻覺 (Hallucination) 時。
大型重構後：當程式碼發生大規模變動，舊的 Context 已不再準確時。 