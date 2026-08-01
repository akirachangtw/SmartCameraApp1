# MEO Adjudication Artifacts — Schema (Rev F §C)

`meo/adjudications/` 承載 `MEO_ESCALATION_PENDING` 之外部裁決管道。所有 artifacts 一律
git-tracked（§C.8），為永久稽核軌跡。

## 雙檔規格（§C.2）

單一檔案無法內嵌其自身建立後才產生之 `audit_id`，故拆為兩個各自 immutable 之 artifact，
皆由 Akira 經 `mcp-ticket-platform:write_file_safe` 於 agent session **外**、以 `mode="create"`
建立（**MUST NOT** `overwrite`/`append`；重複裁決須以新 ID 另立兩檔）。

### (a) decision record — `meo/adjudications/<MEO_ESCALATION_ID>.md`

`key: value` 一行一欄，四必填欄：

| 欄位 | 說明 |
|---|---|
| `decision` | enum：`approve_upgrade` / `deny_retry_with_high_effort` / `defer_and_checkpoint` |
| `adjudicator` | 裁決人 |
| `timestamp` | 建立時間戳 |
| `signals_preserved` | 裁決時所見信號快照描述 |

此檔內 **不含** `audit_id`（機械上不可能——建立時尚未取得回傳 envelope）。

### (b) audit sidecar — `meo/adjudications/<MEO_ESCALATION_ID>.audit.md`

`key: value` 一行一欄，三必填欄：

| 欄位 | 說明 |
|---|---|
| `episode_id` | MUST 等於檔名 ID 與 decision record 檔名 ID |
| `decision_record_audit_id` | = 建立 (a) 時 MCP 回傳 envelope 之 `audit_id`，由 producer 轉錄 |
| `timestamp` | 建立時間戳 |

## release_pending() 五步 transition（§C.5）

`meo_pretooluse_gate.sh` 內固定函式 `release_pending()` 為 STATE release transition 之
**唯一可信執行體**。驗證（雙檔存在、ID 相符、enum 合法、必填欄非空）全數通過後，於與
signal producer（§A.1）**同一把** `mkdir "$STATE_FILE.lock"` 鎖之單一臨界區內，對
STATE 之 lock-scoped temp copy 依序執行，終驗通過後單次 atomic replace 原檔：

1. **重新 read-back**：鎖內重讀 active `MEO_ESCALATION_ID`、`MEO_ESCALATION_PENDING`，
   與 (a)(b) 兩 artifacts；與驗證時之值不一致 → 中止、零寫入。
2. **寫入 resolving decision**：(a) 之 `decision` 值寫入 `MEO_LAST_ESCALATION_DECISION`。
3. **寫入 watermark**：`MEO_WM_RETRY`／`MEO_WM_STREAK`／`MEO_WM_ERROR_CLASS`／
   `MEO_WM_REJECT` 快照為當前對應原始信號值。
4. **最後**才將 `MEO_ESCALATION_PENDING` 由 `true` → `false`。
5. **終驗 read-back**：確認步驟 2–4 之鍵於 temp copy 上全部符合預期值，方 `mv` 單次
   atomic replace 原 `STATE_FILE`；任一步不符 → 捨棄 temp copy，原檔不變。

步驟 2 **MUST** 先於步驟 4（record-before-clear，對應 `decision-tree.yaml` 之
`pending_release.signal_preservation`）。任一步失敗 → 中止並回退為「未 release」語義：
**MUST NOT** 只清 `PENDING`、**MUST NOT** 留部分 watermark、**MUST NOT** 保留 stale decision。
觸發本次驗證之原始 `Write/Edit/MultiEdit/Bash` 呼叫，**MUST 仍回 `deny`**。

## 誠實聲明（§C.4）

sidecar 驗證所證明者為**通道一致性**（兩檔經同一外部 MCP 通道循序建立），**非密碼學真實
性**；偽造之真正防線為 gate 對 `meo/adjudications/` 路徑寫入之 `deny`（AI-3 §3）。
**MUST NOT** 於任何文件、註解或 commit 訊息中宣稱此 receipt 機制可防偽。

## 處置規則（§C.8）

`meo/adjudications/` 下所有 artifacts（`.gitkeep`、`SCHEMA.md`、decision records、
audit sidecars）一律 git-tracked，**MUST NOT** 加入 `.gitignore`、**MUST NOT** 外部歸檔。
