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
4. activation 成功時，**MUST** 依 `.claude/rules/ssot-governance.md` §Ticket Runtime Transition Table 判定該票下一步；若由 Header／Verify Config／Action Items／AC／gate receipt 唯一決定，**MUST** 立即接續（AUTO-CHAIN），**MUST NOT** 詢問是否要看票面內容或確認下一步。模式切換本身不改變該票 required next transition（不變量①）。票的 Action Items 完成並自驗後，實作提交、票檔搬 `deliver/`、冊面轉 `REVIEW` 與票檔單票提交（`ssot-governance.md` §Session Persistence 邊界第 5 點軌道 A，`[<TICKET_ID>]` 前綴）屬標準收尾，**MUST** 自主完成，**MUST NOT** 以 wrap_up 或任何形式詢問是否提交；自行選定軌道即可，提交前依 Gate 入口先跑 `check commit`。
5. 涉及實作或指定 Ticket 時，先讀該 Ticket、當前進度 SSOT 與 live diff；缺少必要依據時停止實作並以正文回報。
