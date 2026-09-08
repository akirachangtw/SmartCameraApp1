---
name: normal
description: 退出臥底模式，恢復正常對話模式。可以解釋、討論架構、提供建議。
disable-model-invocation: false
---

# Prompt
退出臥底模式。恢復正常回應格式：可以解釋、討論架構、提供建議。

`🗣️ Normal mode restored.`

## Phase 0：Ticket Activation（帶參數呼叫時）

1. `$ARGUMENTS` 非空 → **MUST** 先執行 `bash .claude/scripts/activate_ticket.sh "$ARGUMENTS"`；非零 exit → 回報 `⛔ Normal Activation STOPPED: <腳本 stderr 首行>`，但**仍繼續**退出臥底模式（不宣告 activation 成功，但模式切換本身 MUST NOT 因此被阻斷）。
2. `$ARGUMENTS` 為空 → **MUST** 維持現行行為（直接退出臥底模式），MUST NOT 因缺參數報錯。
3. activation 成功（exit 0）時，`activate_ticket.sh` 自身之 `✅ ACTIVATED` stdout **MUST** 原樣透出，`/normal` **MUST NOT** 額外吞掉或改寫該行。
