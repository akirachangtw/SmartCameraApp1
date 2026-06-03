---
name: telegram-notify
description: "從 Claude Code 發送 Telegram 通知。Usage: /telegram-notify <訊息>"
metadata:
  author: Akira Chang
  version: '2.0'
---

# Telegram Notify

Claude Code 任務完成或中斷時，推播通知到 Telegram（Session 關閉後仍有效）。

## 前置設定（一次性）

加入 `~/.bashrc` 或 `~/.zshrc`，重開 shell 後生效：

```bash
export TELEGRAM_BOT_TOKEN="123456:ABC-DEF..."  # @BotFather /newbot 取得
export TELEGRAM_CHAT_ID="數字ID"               # api.telegram.org/bot<TOKEN>/getUpdates 取得
```

## Prompt

使用者執行 `/telegram-notify [訊息]` 時：

1. 讀取訊息參數，若未提供則預設 `任務完成`
2. 判斷是否為成功通知（預設 exit_code=0）或失敗通知（exit_code=1）
3. 執行：
   ```bash
   bash .claude/skills/telegram-notify/scripts/notify.sh "[訊息]" [exit_code]
   ```
4. 回報：
   - 環境變數缺失 → `⚠️ 請先設定 TELEGRAM_BOT_TOKEN 與 TELEGRAM_CHAT_ID`
   - 送出成功 → `✅ Telegram 通知已送出：[訊息]`
