> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# MCP Service Safety — Ownership 判定規約

事件支撐依據：2026-08-10 23:48:28+08:00 TKR-13 事故（見下方證據鏈）。
機械守門實作見 `.claude/hooks/mcp_service_pid_guard.py`（`MCP-PID-OWNERSHIP-GUARD-01`）。

## §1 Port Ownership ≠ Session Ownership

佔用測試埠（如 `netstat` 查得某 PID 監聽 9443）**MUST NOT** 被推定為「由本 session 啟動」。
`McpTicketPlatform` Windows Service 及其 child 程序（`api-server`／`mcp-http`／`web-gui`／`caddy`）
可能早於本 session 存在並持續佔用相同埠號；port 命中只是風險訊號，不是 kill 授權。

## §2 關閉 Browser Tab ≠ 授權停止 Port Listener

實機驗證收尾時關閉瀏覽器分頁，**MUST NOT** 被解讀為「該分頁背後的 port listener 可終止」。
只有本 session 明確建立、且可透過 pid-file／spawn 紀錄證明 ownership 的暫時程序才可清理。

## §3 Ownership 不明 → 停止，不得猜測

守門（`mcp_service_pid_guard.py`）判定 ownership 不明時 **MUST** deny 並 fail-closed；
執行者 **MUST** 停止回報，**MUST NOT** 改用其他 kill 語法繞過（如換用 `pkill`／`wmic`／
`-EncodedCommand` 規避字面偵測）。繞過偵測層即違反本條，不因守門本身有漏洞而免責。

## §4 事故四段證據鏈

TKR-13 收尾事故：`netstat :9443` 查得 PID `5044` → 誤判為本 session 佔用 → 執行
`taskkill //PID 5044 //F` → `scripts/windows_service.py` `monitor_loop()` 偵測 child exit
（`web-gui` pid=5044 code=1）即令整組 `McpTicketPlatform` service 收束（`MCP Ticket Platform
service stopped`）。四段：**netstat 誤判 ownership → 執行終止 → child exit 被 supervisor 偵測
→ 全組 service cascade 停機**。

## §5 翻盤條件

本規則（PLT 端由 mcp_service_pid_guard.py hook 機械強制；APP 端無此 hook，本檔全文屬行為契約層，無機械閘）僅封鎖「Claude Code 之 Bash 工具發出的終止命令」入口。
若未來事故來源轉為非 Claude Bash（人工 GUI 操作、其他 agent runtime、OS 層外部工具等），
**MUST** 另開 OS/service 層防護票處理；本規則 **MUST NOT** 宣稱覆蓋該域。
