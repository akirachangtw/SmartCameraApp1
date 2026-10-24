# ADV-CLAUDECLI-01: 於 route.py 新增 `advisor.claude-cli` 通道——經共同入口以 Claude 訂閱 CLI（Opus 5.5／High）交付問題並取回答，支援有票（incident 路徑）與無票（顯式 `--no-ticket`），envelope 與記帳標明實際通道／模型／錯誤，失敗明確回報、無備援、無轉計費 API

**Revision**: Rev A.4（2026-10-24，TKA close 再審〔Codex `gpt-6-sol`〕判 BLOCKED〔核心 AC11、AC12 FAIL；AC4、AC15 UNVERIFIED；AC16 非核心 FAIL〕後，經正式契約〔`registry_set_status` REVIEW→IN_PROGRESS＋票檔回 `Pending/`＋`file` 指標以 `registry_upsert_ticket` 更新〕同票受控返回修復狀態；Akira 2026-10-24 裁決：①AC11 修最終 cwd 路徑解析、補 junction／symlink 反例；②AC12 補 `init.tools` 空陣列 guard，不刪 AC；③AC4 補 token 衝突測試；④AC15 尋回既有原始證據並建立可定位對應，不新增模型呼叫、不重造原件；⑤AC16 如實分開本票與他 session 提交並附 EOL 輸出；提交 `9a4fc213`；不重啟已達 LIMIT 之舊 Cross-Review，依既有 close 契約再審）。前版：Rev A.3（2026-10-23，原地修訂，4a 計數＝3：①套用 Akira F1 裁決〔採 A〕——HC9、AI-5、AI-7 與 do_native 回歸 AC 改為「既有 65 案除 test_S2／test_S3 兩處具名同步外不修改」，不改共用 `table_copy`、不改 `alternatives` 政策、不弱化斷言；②收準 F2：缺 `.git` 依原設計初始化，僅初始化失敗才拒絕；③收準 F3：中斷不自動續傳／補記帳／重播，不禁止其後依 Gate 合法發起之新諮詢；④4a 首次達門檻，已依 Instructions §4a／分流通道送單次 PPSS 外審查詢包〔`rsch-20261023-b0a2e27e49`〕並記入〈External-review decision〕，存廢三案建議＝續修。不重跑 diag、不新增模型 probe、不新增票）。前版：Rev A.2（2026-10-23，RGR Preflight Cycle 1 修補 F2–F7，4a 計數＝2：①cwd 準備失敗之零啟動處置〔`cli_unavailable` 加 detail，不新增 reason〕；②單次呼叫不可續作之聲明與承接者；③Advisor 條增 `advisor.claude-cli` 真實性依據句；④AC 15 真實全鏈之可追溯要求、觀察者與 Mechanism Bypass 揭露；⑤AI-8 ② 指令補全域 `--ticket`；⑥缺 `--incident` 判定優先序差異之揭露。F1〔新 enabled 來源使既有 test_S2／S3 必然失敗，與 HC9 互斥〕為 Decision-Fork，待 Akira 裁決，本 Rev 未改 HC9／AI-5／AC 12；報告 `.tmp/rigor-gate/RGR_ADV-CLAUDECLI-01_20261023.md`）。Rev A.1 見下方〈檔案治理／來源說明〉。

**簽發日期**: 2026-10-23
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（`ADV-ENTRY-01` 已限定 CLOSED，為基線，非本票開工前置）
- `evidence_dependency`: ADV-ENTRY-01 —— 引用其 `route.py`／接線表／Gate 條文為基線與測試慣例；ADV-APICALL-01、ADV-APIGATE-01 —— 引用 `advisor_call.py` 之 exit code 分類慣例與 API 路徑之 closed-after-call 語義（僅作對照，不修改）
- `coordination_dependency`: 無（`ssot-governance.md` 為共享檔，與其他 session 串行見 Error Handling；非同批收束之票）

## Verify Config
TICKET_ID=ADV-CLAUDECLI-01
CREATED=2026-10-23
PRESET=feature
ALLOWED=.claude/scripts/route.py,.claude/scripts/route_table.json,.claude/scripts/tests/test_route.py,.claude/rules/ssot-governance.md
FORBIDDEN=.claude/scripts/advisor_call.py,.claude/scripts/gate_ledger.py,.claude/scripts/activate_ticket.sh,.claude/scripts/m3_family_audit.sh,.claude/scripts/tests/test_advisor_call.py,.claude/scripts/tests/test_gate_ledger.py,.claude/scripts/tests/fixtures/advisor_seed_payload.md,.claude/hooks/gate_stop_guard.py,.claude/hooks/registry_review_stop_guard.py,.claude/hooks/session_start.sh,.claude/hooks/stop.sh,.claude/hooks/telegram_stop.sh,.claude/hooks/telegram_ask.sh,.claude/hooks/snapshot_cueshot_memory.py,.claude/hooks/notify.sh,.claude/hooks/tests/**,.claude/settings.json,.claude/settings.local.json,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,.claude/rules/cross-review.md,.claude/rules/engineering-discipline.md,.claude/rules/concurrency.md,.claude/rules/ticket_pattern.md,.claude/skills/**,.claude/agents/**,CLAUDE.md,CLAUDE.local.md,app/**,plans/roadmap/**,plans/architecture/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,tools/**,.codex/**,.agents/**,.gitignore
MAX_LINES=N/A
EXCLUDED_PAYLOAD=PROGRESS.md
DIFF_REF=bd8b532f
ANCHOR_PROBED=2026-10-23 15:47
BASE_COMMIT=bd8b532f
TARGET_BLOB=.claude/scripts/route.py:e7d50833;.claude/scripts/route_table.json:27842ac7;.claude/scripts/tests/test_route.py:f58f17ff;.claude/rules/ssot-governance.md:35835a77
INTRODUCED_CAPABILITY=feature:advisor-claude-cli
DEPENDENCY_DELTA=TOOL:claude@2.1.304

> PRESET=feature（擴充既有入口腳本與接線表、測試，並修訂一條治理條文；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。`MAX_LINES=N/A`：含授權／守門判定類 fail-closed 不變量（認證與模型判定、前置拒絕、記帳條件），失敗形狀為審查發現式，不可先驗估算（TPL v3.5 R3）；MAX_LINES 非 execution gate。`TARGET_BLOB` 為開票時 `git hash-object` 前 8 碼（四檔皆既有）；`BASE_COMMIT` 為開票時 HEAD；`DIFF_REF` 依 `ticket_pattern.md` 票據模板規範於開工回填。`INTRODUCED_CAPABILITY`／`DEPENDENCY_DELTA` 為顯式填寫（TPL v3.10）：`claude` CLI 為本票首次由腳本依賴之外部工具（版本以開票時實測 2.1.304）。`ALLOWED` 不含 `gate_stop_guard.py` 與 `cross-review.md`：live 取證其 `ROUTER` 字串與該檔皆無來源名列舉（見證據分級 C11），列入即違「新增條款 scope 一致性」。

## 檔案治理／來源說明
- **Parent type**：Plan-backed。**Parent 絕對路徑**：`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261023-advisor-routing-plan.md`（`.tmp` 暫存，七天內可能消失；本節即獨立可讀摘要）。**Parent artifact 狀態**：`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`（Rev B，2026-10-23）。**Rev A.1（2026-10-23，派工前原地修正，Owner 指示七項；不撤票、不重開，ID／ALLOWED／單票交付不變）**：①逾時為終局判定，殘留輸出僅供診斷，不判成功、不回 answer、不記帳；②成功須 `is_error` 明確布林 `false`、回答為非空白字串，欄位缺失／型別不符統一 `unparseable`，僅空白字串為 `empty_answer`；③副作用預期分「原 repo 零寫入」與「隔離案例 repo 預期 ledger 寫入」；④AI-0 漂移處置統一依 TPL v3.11 三分流（移除「任一不符即停」）；⑤執行記錄寫入失敗、唯一檔名、行程回收失敗有界處理；⑥ledger note 記實際 `apiKeySource`；⑦AC 12 分 (a) 成功實測／(b) 歷史失敗輸出／(c) stub 三種證據。**plan-local item**：`T01`（非票號）。**Parent read timestamp**：2026-10-23 15:3x（本 session 內撰寫該 plan Rev B，開票前核對全文）。上游：`.tmp/ticket-author/20261022-advisor-p1-diag.md`（含 Addendum、Addendum 2；`Fixture: false`／`Gate: PASS`／`PARTIALLY_CONFIRMED`）；需求基準 `plans/roadmap/advisor-outcomes.md`（`8fd0d367`；P1-A 5／P1-B 5／P1-C 5，約定合計 15，P2 已移出）。
- **Selected item 原文（T01 goal，摘要不改義）**：經 `route.py consult`，executor 以 Claude 訂閱 CLI（Opus 5.5／High）交付最小必要上下文、取得回答並返回，有票（incident 路徑）與無票（顯式 `--no-ticket`）皆可；envelope 與（有票時的）ledger note 標明實際通道、模型與錯誤；失敗明確回報，不改走其他通道。
- **約束（來自 plan §1／§3）**：重用 `route.py`／接線表／ledger／既有授權、成本、計輪與停損；沿用 ADV 定位（諮詢資源、不操作原任務——`--tools ""` 機械保證無工具執行，**不等於無副作用**）；不新增第二套 ledger／DB／queue／簽章；不改 `advisor_call.py`／`gate_ledger.py`／receipt schema／`gate_stop_guard.py`；不新增 ledger 事件型別；不使用 `--bare`；不更動全域認證；不繞過權限拒絕。
- **非目標（已移出成果表）**：完整自動備援與錯誤切換政策、訂閱轉計費 API、API／native 全通道必驗、各 Skill 機制調整與介接維護（含 TKA／RGR／uc／qa／sv 文字與「何時該求助」規則）、全域後繼政策與遷移、RGR 修復 runner、close reviewer、Codex 通道。
- **plan 決議（皆 APPROVED）**：D1 主通道＝Claude 訂閱 CLI、正式模型 Opus 5.5／High；D2 有票與無票皆明確支援；D3 無備援、無自動切換、無轉計費 API；D4 `claude_cli` script-call adapter（Router 內執行 subprocess）；D5 無票顯式 `--no-ticket`、不自動降級、無票零 ledger；D6 有票沿用 M1／M4／M5 與 `consult --kind advisor` 記帳（僅成功時）；D7 輸出用 `stream-json --verbose`；D8 模型判定嚴格＝`modelUsage` 鍵集合恰為 {請求模型}。

## 證據分級
證據時間：2026-10-22 ~ 2026-10-23 live；`main` @ `bd8b532f`（開票時 HEAD；較 plan 之 `8fd0d367` 多一個他 session 的 `[GOVERNANCE-SYNC]` protocol 提交，未觸 ALLOWED 檔）；工作樹乾淨；`tmp/verify/active.json` 指向已交付之 `TKA-TRUST-01`（`loc:deliver`，非活態，不阻擋）。

- **CONFIRMED（本地原始碼、工具輸出、可重現 provider 實測）**：
  - **C1 錨點**：`route.py` 之 `ADAPTERS`、`validate_table` 之 ledger_kind 判斷、`do_native` note（寫死 `source=advisor.native`）、`PHASES`、`cmd_consult`、`build_parser` 之 `--incident required=True`、`main` 簽章，各於開票時字面命中恰 1；`route_table.json` `"advisor.native":{"kind": "model-dispatch"` 命中 1。
  - **C2 可執行檔**：`shutil.which("claude")`＝`C:\Users\Administrator\.local\bin\claude.EXE`（原生 exe，`subprocess` 以 list 啟動即可，不需 shell）；`claude --version`＝2.1.304。
  - **C3 成功形狀**（2026-10-23 Opus 5.5／High，`claude -p --model claude-opus-5-5 --effort high --tools "" --strict-mcp-config --disable-slash-commands --no-session-persistence --output-format stream-json --verbose`，隔離 git-init cwd，三金鑰變數 `env -u`）：exit 0；`system/init`：`apiKeySource="none"`、`model="claude-opus-5-5"`、`tools=[]`、plugins 8、agents 15；`result`：`is_error=false`、`modelUsage` 僅 `claude-opus-5-5`（provider firstParty、costBasis list，CLI 報 0.0502）；2 個 `SessionStart` hook（`hook_response` exit 0、stdout 空）；13.6 s。`--output-format json` 與 stream 之 `result` 事件**無** `apiKeySource` 鍵（以保存輸出核對）。
  - **C4 `apiKeySource="none"` 的證明範圍**：以空 `CLAUDE_CONFIG_DIR`＋不可達 `ANTHROPIC_BASE_URL`（不產生任何模型呼叫）實測，**未登入**時 `system/init` 仍為 `apiKeySource="none"`。故它只證明「未使用 API key 來源」，**不證明**訂閱帳務。
  - **C5 未登入形狀**（同上實測）：exit 1（2 s）；`assistant` 事件帶 `error="authentication_failed"`、文字 `Not logged in · Please run /login`；`result`：`is_error=true` 但 `subtype="success"`、`terminal_reason="api_error"`、`modelUsage` 為空、`total_cost_usd=0`、`result` 為上述文字。→ 判定必須先看 `is_error`，不得以 `subtype` 判成功。
  - **C6 API 不可達形狀**（同上，`ANTHROPIC_BASE_URL=http://127.0.0.1:9`，不產生模型呼叫）：CLI **內部**重試——`system/api_retry` 事件 attempt 1..8（`max_retries=10`，延遲 0.6 s 起指數增至 38 s，`error="unknown"`），100 s 牆鐘逾時時仍未結束且無 `result` 事件（以外部 `timeout` 殺，exit 124）。→ adapter 自身只啟動一次，但**無法抑制 CLI 內部重試**；必須有牆鐘逾時，並計數回報 `api_retry`。
  - **C7 行程樹**：Windows 上 `Popen.kill()` 不終止孫行程（實測孫行程仍存活）；`taskkill /F /T /PID` 可用並終止之。
  - **C8 副作用快照**（2026-10-23 Opus 案例前後）：repo `git status`、`tmp/verify_cmd.conf`、`tmp/verify/active.json`、`.git/gate` 目錄、`~/.claude/settings.json` 不變；`~/.claude.json` 被 CLI 自行更新（mtime，+29 bytes）；`~/.claude/projects/` 新增一個空目錄（對應隔離 cwd，`--no-session-persistence` 下無 transcript）。
  - **C9 `--effort high` 被接受**（exit 0、有 thinking tokens）；CLI 輸出不回報實際生效 effort。
  - **C10 無票現況**：`route.py --ticket NOPE-01 consult …`→`REFUSED ledger_refused: no ledger and no ticket file (loc:none)`；無 `--ticket` 時解析到 `active.json` 之已交付票→`ticket_not_mutable loc:deliver`。
  - **C11 來源名消費者**：`.claude/` 內列舉來源名者僅 `route.py`（docstring、`:452`、`:501`、`:630`）、`route_table.json` 與 `ssot-governance.md:214`（Advisor 條，`<advisor.api｜advisor.native>`）；`gate_stop_guard.py:376-378` 之 `ROUTER` 字串**無**來源列舉（僅「依 Gate 條文…一律經入口 `route.py consult`」）；`cross-review.md`、`CLAUDE.md`、各 Skill 之 `SKILL.md` 皆無來源名。
  - **C12 EOL**：`route.py`、`route_table.json`、`test_route.py`、`ssot-governance.md` 皆 `i/lf w/lf`（`ssot-governance.md` 另 `attr text eol=lf`）。`git check-ignore -v` 對四檔無命中。
  - **C13 ledger**：`gate_ledger.py` `consult --incident --kind advisor [--note]`（`:664-671`）於寫鎖內要求 incident 為 open，僅存 `kind`／`note`；現有 `route.py` 之 `ledger_consult` 已支援 `closed_extra` 之 closed-after-call 報告。
  - **C14 目標檔名唯一**：registry（主＋archive）、`Pending/`、`deliver/`、`void/` 無同義或同號票；`ADV-CLAUDECLI` 前綴字面無命中；`ADV-` family 已於 `ticket_pattern.md` 補登 provenance。
- **SUPPORTED（外部，頁面級；PPSS `rsch-20261023-3908cdee2e`，2026-10-23，4 條查詢，est $0.005，artifact `D:\DevSources\workflow\mcp-ticket-platform\data\research\2026-10\rsch-20261023-3908cdee2e.json` 為 runtime 衍生物，**結論以本票為準**）**：①`https://platform.claude.com/docs/en/build-with-claude/effort`（last_updated 2026-10-22）：Opus 5.5 與 Haiku 5.5 之 effort 預設為 `medium`，設為預設值與省略該參數行為完全相同，其他模型預設 `high`；`max` 等級可用於 Opus 5.5；effort 為行為訊號而非嚴格 token 預算——故 `--effort high` 於 Opus 5.5 為**非預設**，顯式傳入才有別於省略；②`https://code.claude.com/docs/en/cli`（last_updated 2026-10-22）：`--effort` 接受 `low`／`medium`／`high`／`xhigh`／`max`／`ultracode`，可用等級依模型而異；`claude auth status` 之 `authMethod` 為 `none`／`claude.ai`／`oauth_token`／`api_key`／`api_key_helper`／`third_party`（此為 auth status 輸出，**非** stream init 之 `apiKeySource`）；`--output-format` 含 `stream-json`；③`https://code.claude.com/docs/es/headless`（last_updated 2026-10-22）：stream-json 須搭配 `--verbose`；`system/api_retry` 事件之 `error` 類別為 `authentication_failed`／`oauth_org_not_allowed`／`account_on_hold`／`billing_error`／`rate_limit`／`overloaded`／`invalid_request`／`model_not_found`／`server_error`／`max_output_tokens`／`cloud_credential_error`／`unknown`；SessionStart／Setup hook 會發出 `hook_started`／`hook_progress`／`hook_response` 事件；`--bare` 於 CI 不載入 hooks／plugins／memory／CLAUDE.md；④`https://code.claude.com/docs/en/hooks-guide`（2026-10-22）：SessionStart 於 session 開始時觸發；`disableAllHooks: true` 可停用 hooks（專案設定可覆寫，managed 設定之 hooks 仍執行）。→ 支持本票：以 `api_retry`／assistant `error` 類別分類失敗；顯式傳 effort；hooks 不可由本票以未驗證旗標關閉。
- **HYPOTHESIS／UNKNOWN（各附 bounded probe 或處置）**：
  - **U1** stream `system/init` 之 `apiKeySource` 完整值域：官方頁面未回傳；本地觀察僅 `"none"`。處置：只接受缺鍵以外之 `null`／`"none"`，**其餘值一律 fail-closed 拒絕並原樣（遮蔽）回報**（AC 6）。
  - **U2** effort 實際生效值：CLI 不回報；處置：envelope 只標「effort 旗標值」，不宣稱生效（AC 2 欄位、Hard Constraint 6）。
  - **U3** 非 git 目錄作 cwd 是否可行：探測命令被權限層拒絕，**未改道、未重試**；處置：adapter 只使用**已實測形狀**——固定於系統暫存目錄、已 `git init` 之空目錄、位於 repo 外（Hard Constraint 11）；非 git 形狀不使用。
  - **U4** plugin／hook 自身是否寫外部檔案或網路（含 telemetry 類）：UNKNOWN；處置：不宣稱無副作用，envelope 回報 hook 事件，AC 15 快照比對。
  - **U5** 其他失敗之 exit code：僅觀察到未登入 exit 1、逾時被殺；處置：分類以 stream 事件欄位為主，不依賴 exit code 區分原因（AC 5）。
  - **U7** `CLAUDE_CODE_SUBPROCESS_ENV_SCRUB=1`（若父 Claude Code session 設定）時子 CLI 之訂閱認證是否受影響：`HYPOTHESIS`（4a 外審 `rsch-20261023-b0a2e27e49`）；處置：不新增 probe，AI-8 真實呼叫失敗時依 `not_logged_in` 等既有分類如實記錄。
  - **U6** 無 External：PPSS 4 查詢中，nested session／exit code／`apiKeySource` 值域無相關頁面回傳；依防遞歸律，同 capability 完成一次外審＋一次 bounded probe 後**不因來源不足重送**（本票已完成多次本地 bounded probe：成功、未登入、不可達、行程樹）。

**External-review decision**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` §6 與「外審查詢包」「分流順序與發送通道」節，2026-10-23 live 直讀；分項記錄）：
- **6a（成立；取證 EXECUTED）**：`INTRODUCED_CAPABILITY=feature:advisor-claude-cli`；token 及其子字串（`advisor-claude-cli`、`claude-cli`、`advisor.claude-cli`）於固定 corpus（Smarter-Project vault 全目錄、本專案 `docs/session_handoff` 與 `Handoff`、`Pending/` `deliver/` `void/`、`TICKET_REGISTRY.json` 與 ARCHIVE）無完整字面命中 → 取證。**範圍**：單次 PPSS（觸發碼 `6a`／`6b2`／`6b3`），4 條查詢，限本 capability 之 CLI 契約面（effort、`apiKeySource`、hooks、失敗事件），**非架構選型之泛用外審**；綁定欄位隨 `metadata.binding` 送出。**6b**：①`ALLOWED` 首層 path segment 皆 `.claude`，distinct＝1 → 不成立；②`### AI-n:` 標題數＝8（AI-1..AI-8；`AI-0` 不計）→ **成立（≥8）**；③`DEPENDENCY_DELTA=TOOL:claude@2.1.304`≠`NONE` → 成立。**6c** 不成立（本票非受管制產物之反覆修訂計數）。
- **硬風險判準①–⑥皆未命中**：①官方 contract 明確禁止——未命中（`-p`／stream-json／`--effort` 皆為官方文件旗標）；②provider hard contract 未確認且 request 格式可能錯誤——未命中（不自組 API request，CLI 旗標已由官方頁面與本地實測雙重確認）；③新公開網路面或既有安全邊界——未命中（子行程經既有 Claude Code 通道，不新增監聽面；金鑰變數僅於子行程環境移除）；④不可逆資料寫入／刪除——未命中（ledger 僅追加既有事件型別；執行記錄在 git-ignored `tmp/`）；⑤實質費用且無成本上限——未命中（訂閱 CLI；本票真實呼叫合計 ≤2 次；adapter 單次啟動＋牆鐘逾時；CLI 內部重試受逾時界限）；⑥bounded probe 已明確失敗且無可行替代——未命中（成功、未登入、不可達、行程樹皆有可用結果；非 git cwd 探測被權限層拒絕，已以「用已實測形狀」處置）。
- **4a（成立；Rev A.3，2026-10-23）**：同票修訂計數首次達 3（A.1 Owner 七項、A.2 RGR 修補、A.3 F1 裁決套用；皆為派工前收準，非修補鏈）→ 依條件 4 輸出設計可行性外審查詢包，經 PPSS 單次送出（`research_submit`，觸發碼 `4a`，4 條查詢，綁定欄位隨 `metadata.binding`；`request_id=rsch-20261023-b0a2e27e49`、`run_id=run-a0c6f87222e2`、cache miss、估計 0.005 USD、5 筆結果；artifact 為 runtime 衍生物，結論以本票為準）。逐項回答：①**證據等級**——以 `claude -p` 搭配 `--output-format json`／`stream-json --verbose` 由腳本程式化整合為官方明列之用法（`https://code.claude.com/docs/en/best-practices`，2026-10-21：「Non-interactive mode is how you integrate Claude into CI pipelines, pre-commit hooks, or any automated workflow」；`https://code.claude.com/docs/en/headless`，2026-10-20），故「以子行程包裝 CLI 作諮詢通道」為反模式之證據等級＝無支持（官方正面列為整合方式，`SUPPORTED`：非反模式）；`--bare` 不使用訂閱登入（headless 頁，2026-10-20）與本票 HC2 不用 `--bare` 一致（`SUPPORTED`）；②**風險**——`CLAUDECODE`／`CLAUDE_CODE_CHILD_SESSION` 會設於 Claude Code 所啟子行程，巢狀非互動 `claude -p` session 預設仍持久化（`https://code.claude.com/docs/en/env-vars`，2026-10-23），本票已以 `--no-session-persistence` 處置（C8 實測僅新增空目錄）；`CLAUDE_CODE_SUBPROCESS_ENV_SCRUB=1` 時父 session 子行程環境之憑證會被清除（同頁），對子 CLI 之訂閱 OAuth 是否有影響＝`HYPOTHESIS`（OAuth 存於設定／鑰匙圈而非環境變數），列 U7，由 AI-8 真實呼叫觀察、失敗即如實記錄（不新增 probe）；硬風險判準①–⑥仍皆未命中；③**替代模式**——(i) Agent SDK（Python／TypeScript 套件，官方稱「full programmatic control」）：須新增第三方套件，違 HC13「僅標準庫」並新增依賴，且 SDK 之訂閱認證未見回件證據（`UNKNOWN`），適用於需要多輪 agent 迴圈或工具控制之場景，本票只要單次問答，不採；(ii) 計費 API client：違 D3（不轉計費 API），不採；(iii) MCP：本票為諮詢顧問而非工具暴露，且 `--strict-mcp-config` 已排除 MCP 載入，不適用；④**≤`HYPOTHESIS`**：U7（ENV_SCRUB 與訂閱認證之交互）、Agent SDK 訂閱認證——前者以 AI-8 既有 ≤2 次真實呼叫為觀察點，後者不在本票路徑，皆不新增 probe。**存廢三案建議**：續修（官方支持之整合路徑、三次修訂皆屬派工前收準、無硬風險命中）；退役（放棄訂閱 CLI 通道）與降級（改人工貼問）皆會使 P1-A①②／P1-C⑤ 落空，不建議。依條件 4「僅為健康訊號」，本項不構成凍結或派工阻擋；遲滯：存廢裁決落檔後 4a 計數重置。
- **bounded probe**：本地 probe 已於開票前執行（C3–C9）；實作期 probe＝AC 2–11 之離線案例與 AC 15 之 ≤2 次真實呼叫，狀態 **PLANNED**（執行於本票 AI-5、AI-8）。**Cross-Review**：不豁免。

## Action Items

### AI-0: 錨點復驗（派工後、修改前；唯讀）
- **檔案**: 見各 AI
- **動作**: 逐字確認各 AI「現值」於檔內命中數與票面相符（標示「唯一」者恰 1 次）；錨點不符一律依 TPL v3.11「基線漂移三分流」處置，**不另設「任一不符即停」**：①`BASE_COMMIT..HEAD` 間無觸及 ALLOWED 標的之提交→`ANCHOR_STALE_SCOPE_INTACT`（資訊性）；②觸及標的之提交含他票 ID→`CANDIDATE_SUPERSEDED`（資訊性）；③含本票 ID→`ALREADY_EXECUTED_PENDING_CLOSURE`（**停止**，直送收束審查，禁重複施作）；④觸及標的而無前綴之提交→`BLOCKED_UNATTRIBUTED_STATE`：停止之範圍限觸及 `app/` production code 者，本票 ALLOWED 全在 `.claude/`，故**降為記錄後續作**（僅唯讀取證並載明）。資訊性與降級者：記錄分類與歸因票 ID，以工作樹**現值**（字面重讀該符號當前文字）續作；僅當該符號於工作樹不存在或無法唯一定位時，該 AI 無法套用——記錄並停止該 AI 回報（此為字面定位失敗，非因分類而停），**禁自行推定等價位置**。並完成：
  1. `route.py` 錨點各唯一：`ADAPTERS = ("api_advisor", "native_advisor", "ppss")`；validate_table 內 `if s.get("adapter") in ("api_advisor", "native_advisor") and s.get("ledger_kind") != "advisor":`；`note = (f"source=advisor.native via={VIA} dispatch={a.dispatch_id} "`；`PHASES = {"api_advisor": (None, "call")`；`c.add_argument("--incident", required=True)`；`def main(argv=None, *, client_factory=None, env=None, now=None) -> int:`；`def cmd_consult(a, client_factory, env, now):`。`route_table.json` 含 `"advisor.native":{"kind": "model-dispatch"` 恰 1。`ssot-governance.md` 含 `<advisor.api｜advisor.native>` 恰 1（Advisor 條，現為第 214 行）、`不得直接以 \`gate_ledger.py consult --kind advisor\` 記帳` 恰 1。
  2. `git ls-files --eol` 四檔皆 `i/lf w/lf`；`git check-ignore -v` 四檔無命中；`git status --short` clean；`tmp/verify/active.json` 歸屬已辨識（非本票者保留不動）；registry 無 IN_PROGRESS 票觸及 ALLOWED 檔。
  3. `gate_ledger.py` 公開面（唯讀）：`status --json` 欄位（`ticket`／`ticket_location`／`incidents[].status|consults`）、`consult --incident --kind advisor --note` 與寫鎖內 open 檢查（`:664-671`）與本票所載一致；**不得**依賴 `status --json` 之 note 或時間戳。
  4. `claude` 可執行檔與版本：`shutil.which("claude")` 為 `.EXE`、`claude --version` 為 2.1.304（版本不同→記錄為資訊性漂移，續作並於收尾實錄標明）；`claude --help` 含 `--effort`、`--tools`、`--strict-mcp-config`、`--disable-slash-commands`、`--no-session-persistence`、`--output-format`、`--verbose`。
  5. 基線回歸（修改前）於 `python -I` 下全綠：`test_route.py`（65）、`test_gate_ledger.py`（59）、`test_advisor_call.py`（25）、`gate_stop_guard_test.py`（54，位於 `.claude/hooks/tests/`）；任一失敗 → 停止回報（非本票造成，須先處置）。
  6. 保存之實測 stream（未登入形狀、不可達形狀、成功形狀）若已消失（`tmp`／scratchpad 非耐久），**不得**以記憶重建為測試 fixture；改以 stub 依證據分級 C3／C5／C6 之欄位描述自造最小事件序列，並於收尾實錄標明「依票面描述自造，非原始輸出」。
- **停止條件僅限**：`ALREADY_EXECUTED_PENDING_CLOSURE`；字面錨點無法唯一定位（見上）；第 5 項基線回歸失敗（非漂移，須先處置）；第 4 項 `claude --help` 缺必要旗標或第 2 項各查驗落入 Error Handling 所列停止條（`ssot-governance.md` 他 session 未提交變更、`check-ignore` 命中等）。**其餘不符（含錨點命中數漂移、`claude` 版本不同、`BLOCKED_UNATTRIBUTED_STATE` 降級）皆為資訊性記錄，續作。**
- [x] 完成

### AI-1: 接線表新增 `advisor.claude-cli` 列，並使 `route.py` 表驗證接受 `claude_cli` adapter 及其 `cli` 區塊
- **檔案**: `.claude/scripts/route_table.json`、`.claude/scripts/route.py`
- **現值**: `route_table.json` 含 `"advisor.native":{"kind": "model-dispatch", …`（唯一）；`route.py` 之 `ADAPTERS = ("api_advisor", "native_advisor", "ppss")`（唯一）與 validate_table 之 `if s.get("adapter") in ("api_advisor", "native_advisor") and s.get("ledger_kind") != "advisor":`（唯一）
- **目標值**:
  1. `route_table.json` 於 `advisor.native` 與 `research.ppss` 之間新增一列（其餘列位元組不變）：`"advisor.claude-cli": {"kind": "script-call", "role": "advisor", "enabled": true, "adapter": "claude_cli", "ledger_kind": "advisor", "cli": {"bin": "claude", "prefix_args": [], "model": "claude-opus-5-5", "effort": "high", "timeout_s": 180}}`。
  2. `ADAPTERS` 加入 `"claude_cli"`；ledger_kind 判斷之 tuple 加入 `"claude_cli"`。
  3. validate_table 對 `adapter == "claude_cli"` 之列要求 `cli` 為 object，並檢查：`bin` 為非空字串；`prefix_args` 為字串 list（可空）；`model` 為非空字串；`effort` ∈ {`low`,`medium`,`high`,`xhigh`,`max`}；`timeout_s` 為 int 且 30 ≤ 值 ≤ 600；缺任一項或型別不符 → 表錯誤（沿用既有 `table_invalid` 與 `errors` 列表）。`bin`／`prefix_args` 為測試與部署可替換之接點（正式表為 `claude`／`[]`），不得由命令列傳入。
  4. `table check` 輸出之 `callable`／`enabled` 含 `advisor.claude-cli`；其餘既有鍵不變。
- [x] 完成

### AI-2: 模式與前置拒絕——`--no-ticket`、`--incident` 改為依來源必填、`claude_cli` 之 phase 與零啟動前置檢查
- **檔案**: `.claude/scripts/route.py`
- **現值**: `c.add_argument("--incident", required=True)`（唯一）；`PHASES = {"api_advisor": (None, "call"), "native_advisor": ("dispatch", "return"), "ppss": (…)}`（唯一）；`def cmd_consult(a, client_factory, env, now):` 內 `if adapter == "api_advisor": … elif adapter == "native_advisor": … else: …` 分派鏈（唯一）
- **目標值**:
  1. `--incident` 改為非必填（預設 None）；新增 `--no-ticket`（`store_true`）。非 `claude_cli` 之來源缺 `--incident` → `Stop(E_USAGE, "USAGE", "usage", "--incident is required for <source>")`（與既有「缺必要參數」同 exit 2、同 `reason=usage`）；非 `claude_cli` 之來源帶 `--no-ticket` → `usage`。**判定順序**：本項缺參數檢查位於 `load_table`／`resolve_source`／phase 檢查之後（來源類型須先由表決定）；故表無效、來源未知或停用時先回 `TABLE_INVALID`（3）／`UNKNOWN_SOURCE`／`SOURCE_DISABLED`（4），與修改前（argparse 於讀表前即報 `usage`）之優先序不同——「同 exit 2、同 `reason=usage`」僅於表有效且來源可呼叫時成立。此差異為已知，既有 65 案皆帶 `--incident`、無測試依賴舊優先序（RGR 2026-10-23 取證），於收尾實錄揭露。
  2. `PHASES["claude_cli"] = (None, "call")`；`cmd_consult` 分派鏈加 `claude_cli` 分支呼叫新函式 `do_claude_cli`（見 AI-3），其餘分支行為不變。
  3. `claude_cli` 之前置規則（皆於任何 subprocess 啟動**之前**判定，違反者 runner 零次啟動）：①有票（未帶 `--no-ticket`）須帶 `--incident` 與 `--payload`、`--evidence`，否則 `usage`；②無票（`--no-ticket`）須帶 `--payload`，且**不得**帶 `--incident`、`--evidence` 或全域 `--ticket`（避免有票與無票混用），否則 `usage`；無票亦須在 git repo 內執行（執行記錄落 repo 根 `tmp/`），否則沿用既有 `usage not_a_git_repo`（零啟動）；③有票：`precheck`（M1：incident open 且票為 `loc:pending`）與 `check_tokens`（M4／M5：恰一個 `incident=` 令牌且等於 `--incident`；恰一個 `failure_sha256=`／`uncertainty_sha256=` 令牌且等於非空 `--evidence` 檔之 sha256）——**沿用既有函式與拒絕 reason**；④payload 須可讀、UTF-8、≤200000 bytes（超過或不可讀→`REFUSED request_invalid`）；⑤無票不呼叫 `precheck`／ledger、不要求 ticket 解析成功，且**不得**因有票條件失敗而降級為無票（模式只由 `--no-ticket` 顯式決定）；⑥`cli.bin` 於 `PATH`（或為絕對路徑）解析不到→`CALL_FAILED cli_unavailable`（零啟動）。
  4. 模組 docstring 之 Usage／Sources／reason 列表同步新增 `advisor.claude-cli`、`--no-ticket` 與 AI-3 之新 reason。
- [x] 完成

### AI-3: `do_claude_cli`——固定 argv／環境／cwd、單次啟動與逾時回收、stream-json 解析、認證與模型判定、失敗分類、envelope、執行記錄、有票記帳
- **檔案**: `.claude/scripts/route.py`
- **現值**: 無（新增函式；插於 `do_api` 之後、`native_slot` 之前或同檔相鄰處）。既有 `precheck`、`check_tokens`、`ledger_consult`、`one_line`、`scrub`、`Stop`、`E_*` 為重用對象，不改動其行為。
- **目標值**:
  1. **argv**（唯一形狀，已實測）：`[<cli.bin>] + cli.prefix_args + ["-p", "--model", <cli.model>, "--effort", <cli.effort>, "--tools", "", "--strict-mcp-config", "--disable-slash-commands", "--no-session-persistence", "--output-format", "stream-json", "--verbose"]`；**不得**加入 `--bare`、`--setting-sources`、`--max-budget-usd` 或任何未實測旗標。prompt 由 stdin 以 UTF-8 送入：固定角色前言（諮詢顧問、只回答、不操作任務、不呼叫工具）＋空行＋payload 全文。
  2. **環境**：複製呼叫端 `env`（缺省 `os.environ`）後移除 `ANTHROPIC_API_KEY`、`ADVISOR_ANTHROPIC_API_KEY`、`ANTHROPIC_AUTH_TOKEN`（僅子行程；父環境與 `env` 物件不得被改動）；其餘不動。**cwd**＝`<系統暫存目錄>/cueshot_advisor_cli_cwd`（固定單一目錄，不存在則建立並以 `git init -q` 初始化；必須**不在**目前 repo 根之下——**Rev A.4：判定對象為 `resolve()` 後之最終路徑**〔含 junction／symlink 指向〕，建立前後各判一次，並以 resolve 後路徑作為子行程 cwd）。cwd 準備於 AI-2 前置全部通過後、CLI 啟動前完成，任何失敗皆零啟動並回 `CALL_FAILED cli_unavailable`（以 `detail` 區分，不新增 reason）：該路徑已存在而為一般檔案→`detail=cwd_not_directory`；目錄存在但無 `.git`→依原設計補執行 `git init -q`（不拒絕）；僅目錄建立或 `git init` 失敗（`OSError` 或 returncode≠0）→`detail=cwd_prepare_failed`；解析後之 cwd 位於 repo 根之下（例如系統暫存目錄被設在 repo 內）→`detail=cwd_inside_repo`。（RGR 2026-10-23 F2，推導修法）
  3. **生命週期**：通過 AI-2 前置後恰啟動 1 次（`Popen`，`stdin`／`stdout`／`stderr` 皆 pipe；Windows 以 `CREATE_NEW_PROCESS_GROUP`、非 Windows 以 `start_new_session`）；`communicate(timeout=<cli.timeout_s>)`；逾時→終止**整個行程樹**（Windows `taskkill /F /T /PID <pid>`、非 Windows `os.killpg`）→回收（見下「回收有界處理」）→回 `CALL_FAILED timeout`。**逾時為終局判定，不可被其後解析推翻**：回收後可**有界地**取出殘留 stdout／stderr 並解析，**僅作診斷**（`cli_internal_retries`、已見之 `session_id`／`usage`、hooks、warnings，envelope 標 `diagnostic:true`）；**不得**以殘留輸出判定成功、**不得**回傳 `answer`、**不得**記帳（不得呼叫 `ledger_consult`、不寫 consult 事件）——即使殘留輸出中已出現 `result` 事件亦然。任何失敗皆不重啟、不重試、不換參數。CLI 內部對 API 之重試（`system/api_retry`）不受控，只受逾時界限，入口計數並回報。
     **回收有界處理**：`taskkill`／`killpg` 呼叫本身失敗（`OSError`，或 returncode≠0 且主行程仍存活）→ 改以 `Popen.kill()` 直殺主行程**一次**；之後 `wait(timeout=10)`；仍未結束 → 不再嘗試、不迴圈、不阻塞：關閉管線句柄（不再讀取）、envelope `reaped:false`、`warnings` 加 `process_not_reaped pid=<pid>`、`detail` 含 pid；仍回 `CALL_FAILED timeout`（exit 6），殘留輸出視為不可得（`diagnostics_unavailable`）。成功回收則 `reaped:true`。所有等待皆有秒數上限（取殘留輸出之 `communicate` 亦 ≤5 s，逾時則同上關閉管線並標診斷不可得）。
  4. **解析**：stdout 以 UTF-8（`errors="replace"`）逐行 `json.loads`，非 JSON 行忽略；取第一個 `system/init`（`init`）、全部 `system/hook_started|hook_response`（`hooks`）、全部 `system/api_retry`（計數與最後 `error` 類別）、各 `assistant` 事件之 `error` 欄（首個非空）、最後一個 `result`。
  5. **判定順序**（第一個命中者即終止，皆不記帳）：①逾時→`timeout`（終局，見第 3 項）；②啟動丟 `OSError`→`cli_unavailable`；③無 `init`→`init_missing`；④認證三態（讀 `init`）：缺 `apiKeySource` 鍵→`auth_unreported`；值為 `null` 或字串 `"none"`→通過；其他任何值（含非字串）→`api_key_source`，原值經 `scrub` 後放 `detail`；**④b `init.tools` 須為空陣列（Rev A.4）**：缺鍵→`tools_unreported`（REFUSED）；非 list→`unparseable`（`detail=tools_invalid`）；非空 list→`tools_enabled`（REFUSED，`detail` 為遮蔽後之工具清單）；⑤無 `result` 事件→`nonzero_exit`（returncode≠0）或 `unparseable`；⑥`result.is_error` **須為布林**：非布林（缺、null、字串 `"false"`、數字等）→`unparseable`（`detail=is_error_invalid`，**不得**當成功）；為 `true`（**先於 `subtype` 判斷**）→依 assistant `error` 類別映射：`authentication_failed`／`oauth_org_not_allowed`／`cloud_credential_error`→`not_logged_in`；`model_not_found`→`model_unavailable`；其餘類別與 `result.terminal_reason`→`cli_error`（原類別放 `detail`）；⑦returncode≠0 而 `is_error` 為 `false`→`nonzero_exit`；⑧模型：`result.modelUsage` 存在而非 object→`unparseable`（`detail=model_usage_invalid`）；設請求模型 R、`modelUsage` 鍵集合 M、`init.model` 為 I——M 為空或缺→`model_unreported`；R∉M 或 I≠R→`model_mismatch`；R∈M 且 M 另含其他模型→`multiple_models`（envelope 帶全部模型與 token）；僅 M＝{R} 且 I＝R 通過；⑨回答（`result.result`）**須為字串**：缺、null、數字等非字串→`unparseable`（`detail=answer_invalid`）；為空或全空白字串→`empty_answer`；⑩成功＝⑥（`is_error` 明確為布林 `false`）、⑧、⑨皆通過。**無效輸出分類統一**：必要欄位缺失或型別不符一律 `unparseable`（CALL_FAILED、exit 6、`detail` 指出欄位）；僅「型別正確而內容為空白」為 `empty_answer`（REFUSED、exit 5）；二者皆不記帳。
  6. **exit／disposition**：⑩成功＝exit 0；有票 `disposition=RECORDED`、無票 `disposition=ANSWERED`。`REFUSED`（exit 5）：`auth_unreported`、`api_key_source`、`tools_unreported`、`tools_enabled`、`model_unreported`、`model_mismatch`、`multiple_models`、`empty_answer`、`incident_closed_after_call`；`CALL_FAILED`（exit 6）：`cli_unavailable`、`timeout`、`init_missing`、`nonzero_exit`、`unparseable`、`not_logged_in`、`model_unavailable`、`cli_error`。
  7. **envelope 欄位**（成功與已啟動之失敗盡量帶出）：`mode`（`ticketed`｜`ticketless`）、`channel`（`claude-subscription-cli`）、`model`（成功時＝R）、`models_used`（每模型 `inputTokens`／`outputTokens`／`cacheReadInputTokens`／`cacheCreationInputTokens`）、`effort_flag`（旗標值，**不宣稱生效**）、`api_key_source`（原樣；缺鍵以字串 `"<absent>"`、null 以 `"<null>"` 明示）、`session_id`、`usage`、`cost_reported_usd`（CLI 牌價換算，明標非實際計費）、`duration_ms`、`cli_internal_retries`、`hooks`（事件、名稱、outcome、exit_code）、`warnings`（hook 非零 exit 或 stdout 非空、`api_retry` 發生、`process_not_reaped`、`record_error`）、`reaped`（逾時路徑）、`diagnostic`（逾時路徑，恆 `true`）、`record`（執行記錄相對路徑，寫入失敗為 `null`）、`record_error`（寫入失敗時之錯誤類別名，遮蔽後）、`answer`（僅成功）。成本已發生之失敗仍帶 `session_id`／`usage`／`cost_reported_usd`（若已取得），不宣稱零成本。
  8. **執行記錄**：凡已啟動之呼叫（成功或失敗）皆於記帳**之前**嘗試寫入記錄檔——有票 `tmp/<ticket>/advisor_cli/`、無票 `tmp/_advisor_cli_ticketless/`（`tmp/` 為 git-ignored、非耐久），檔名 `advisor_cli_<UTC yyyymmddThhmmssZ>_<uuid4 前 8 碼十六進位>.json`（同秒亦互異），以 exclusive 建立（`open(..., "x")`）；`FileExistsError` 換新隨機後綴重試，**至多 3 次**；目錄以 `mkdir(parents=True, exist_ok=True)` 建立。內容為判定結果、envelope 欄位、`payload_sha256`、（遮蔽後之）stream 擷取；記錄為「本地執行記錄，不是計費或供應商端證明」。**寫入失敗（目錄無法建立、權限、磁碟、3 次皆撞名等 `OSError`）為有界非致命**：不超過上述次數、不重啟 CLI、**不改變任何判定**（成功仍成功、失敗仍為原分類）；envelope `record:null`、`record_error=<錯誤類別名>` 並進 `warnings`；有票成功者仍記帳，note 之 `record=none`（見第 9 項）。記錄是事後稽核輔助，不是記帳前置條件。
  9. **有票記帳**：僅 ⑩成功時以 `ledger_consult` 記一次 `consult --kind advisor`，`--note` 由入口組成並含 `source=advisor.claude-cli`、`via=route.py`、`channel=claude-subscription-cli`、`model=<R>`、`effort_flag=<E>`、`api_key_source=<實際值>`（依第 7 項呈現：`none` 或 `<null>`；成功路徑只可能此二者，**不得固定寫死**）、`session_id=<id>`、`payload_sha256=<h>`、`incident=<INC>`、`evidence=<kind>:<h>`、`record=<相對路徑｜none>`；若 ledger 因 incident 已不 open 而拒絕→`REFUSED incident_closed_after_call`（沿用 `ledger_consult` 之 `closed_extra`：回答、記錄檔路徑、session_id 保留於 envelope，`recorded:false`，**不**再呼叫 CLI、**不**重開 incident）。無票永不呼叫 ledger。
- [x] 完成

### AI-4: `do_native` 之 ledger note 改記實際來源
- **檔案**: `.claude/scripts/route.py`
- **現值**: `note = (f"source=advisor.native via={VIA} dispatch={a.dispatch_id} "`（唯一）
- **目標值**: `note = (f"source={a.source} via={VIA} dispatch={a.dispatch_id} "`（其餘字串不變）。表內以 `native_advisor` adapter 命名之別名來源（如測試用）於 note 內記其實際來源名。
- [x] 完成

### AI-5: 測試——經真實入口之離線案例（表驅動 stub，不新增 `main` 注入口）
- **檔案**: `.claude/scripts/tests/test_route.py`
- **現值**: 既有 65 案；`RouteCase` 基底（`run_main`、`cli`、`table_copy`、`ledger`、`events`）；測試只經 `route.main`／子行程 CLI，不直接呼叫內部 adapter；ledger 前置只用 ledger CLI
- **目標值**: 新增測試類（沿用 `RouteCase`），以 `table_copy` 產生 `advisor.claude-cli` 列的 `cli.bin = sys.executable`、`cli.prefix_args = [<stub.py>]`，使真實 `claude_cli` 路徑啟動**真實子行程**（stub）；stub 於每次啟動追加一行至計數檔（觀察「啟動次數」）、把收到之 argv／環境鍵名／cwd／stdin 摘要寫入記錄檔、再依場景輸出預設事件序列（stdout JSONL）或睡眠／生孫行程。覆蓋（對應 AC 2–11）：有票成功（含 `apiKeySource=null` 之 note 呈現）、無票成功、前置拒絕零啟動各類、失敗分類各一案、認證三態、`is_error` 非布林各型別與回答非字串各型別（`unparseable`）、模型判定各類、逾時與行程樹回收（含「已輸出完整成功序列後仍睡眠」→ 仍 `timeout`、無 answer、不記帳）、空回答、incident 關閉於呼叫中（stub 於回答前以真實 ledger CLI 關閉 incident）、hook／api_retry／warnings 回報、env／cwd／argv 精確、執行記錄唯一檔名與寫入失敗（以一般檔案占用 `advisor_cli` 路徑製造，判定與記帳不變）、`do_native` 來源標籤。回收失敗分支（主行程於升級後仍存活）無法在不新增注入口下機械重現，該分支僅以靜態審查驗證並於收尾實錄標「靜態、非行為驗證」。stream 事件序列依證據分級 C3／C5／C6 之欄位（若保存之原始輸出可得則以其為準，否則自造並標註）。既有 65 案除下列具名同步外不得修改（僅新增）。**F1 具名例外（Akira 2026-10-23 裁決，採 A）**：新列 `advisor.claude-cli` 為 enabled 之同 role 來源，必然改變兩案之觀察結果，故僅同步此兩處、各一行——①`test_S2_disabled_with_alternative_and_continuation`：`alternatives` 預期由 `["advisor.native"]` 改為 `["advisor.native", "advisor.claude-cli"]`（依表序），其後以 `alternatives[0]` 續作之斷言不變；②`test_S3_all_sources_of_role_disabled`：fixture 另停用 `advisor.claude-cli`，`MISSING_REQUIRED`／`required_capability_absent`／`alternatives==[]` 斷言不變。不改共用 `table_copy`、不改 `alternatives` 計算政策、不弱化任何斷言；其餘 63 案位元組不變，保持原驗證意義。
- [x] 完成

### AI-6: Gate 條文同步（僅 Advisor 條）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: Advisor 條（現第 214 行）：`…且一律經入口 \`python .claude/scripts/route.py consult --incident <INC> --source <advisor.api｜advisor.native>\`，不得直接以 \`gate_ledger.py consult --kind advisor\` 記帳…`（`<advisor.api｜advisor.native>` 唯一）；PPSS 條（第 215 行）不動
- **目標值**: 僅修訂 Advisor 條：①來源清單加入 `advisor.claude-cli`；②載明 `advisor.claude-cli`＝入口於同一次呼叫內先核對（有票：沿用 M1／M4／M5）、再單次啟動 `claude -p`（固定旗標、不帶工具與 MCP、子行程不含三個 API 金鑰變數、牆鐘逾時、不重試、**不轉其他通道或計費 API**），僅成功才記 `consult --kind advisor`，並於 envelope／note 標明實際通道、模型、effort 旗標與 `apiKeySource`（**僅證明未使用 API key 來源，不證明訂閱帳務；effort 實際生效值 CLI 不回報**）；③載明無票模式 `--no-ticket`：顯式、不寫 ledger、不改 Gate 適用範圍（Gate 仍只對 active 票適用）、不得因有票條件失敗而降級；④一句：啟動 hooks／plugins 仍會執行，`--tools ""` 只保證無工具執行；⑤既有 `advisor.api`／`advisor.native` 句與「Advisor 不可達」處置**逐字保留**；⑥一句真實性依據：`advisor.claude-cli` 之真實性與時序依入口執行記錄（git-ignored `tmp/`，非耐久）與 `session_id`（本地紀錄，非供應商端證明），不同於原生調用之 transcript 與 API 之 receipt；本句置於 Advisor 條內，不改 §Incident 與輪次之既有句（HC8；RGR 2026-10-23 F4）。檔案維持 LF，其餘節與 PPSS 條位元組不變。
- [x] 完成

### AI-7: 全量驗證與範圍核對（唯讀＋回歸）
- **檔案**: 見各 AC
- **動作**: 於 `python -I` 下重跑 `test_route.py`（既有 65 案〔含 F1 具名同步之 test_S2／test_S3〕＋新增全綠；`git diff` 對既有案僅見 test_S2、test_S3 各一行）、`test_gate_ledger.py`（59）、`test_advisor_call.py`（25）、`gate_stop_guard_test.py`（54）；`python .claude/scripts/route.py table check` 回 `ok:true` 且 callable 含三個加一個來源；`git diff --name-only <DIFF_REF>..HEAD` 僅四個 ALLOWED 檔；`git diff` 對 `advisor_call.py`／`gate_ledger.py`／`gate_stop_guard.py`／`.claude/skills/**` 為空；`ssot-governance.md` 之 diff 僅落在 Advisor 條；PPSS 條與其餘節位元組不變；四檔 EOL 仍為 `i/lf w/lf`；`git check-ignore -v` 無命中；離線測試前後，**原 repo** 之 `.git/gate`（事件數與檔案集合）與 `tmp/verify/*` 不變（測試只在暫存 repo 內寫 ledger）。
- [x] 完成

### AI-8: 有界真實驗證（合計 ≤2 次真實訂閱呼叫）與收尾實錄
- **檔案**: 隔離案例 repo（session scratchpad，非耐久）；票檔收尾實錄（`Pending/` 內本票檔）
- **動作**:
  1. **前置**（零模型呼叫）：建立隔離案例 repo 與案例票（`loc:pending`，如 `CASE-CLI-01`）、小型可重現缺陷（含失敗測試）、以 ledger CLI（顯式 `--ticket`）開 incident；前後副作用快照腳本備妥。
  2. **有票真實呼叫（1 次）**：payload 載明 `incident=<INC>` 與單一證據令牌，經 `route.py --ticket CASE-CLI-01 consult --source advisor.claude-cli --incident … --payload … --evidence …` 取得回答（隔離 repo 無 `tmp/verify/active.json`，須顯式全域 `--ticket`，否則 ledger 解析不到票而 `ledger_refused`、零啟動；RGR 2026-10-23 F6）；executor 消費（先本地驗證其說法）→採納並修復→測試轉綠→執行原流程下一必要步驟（例如執行該模組 CLI）；核對 ledger 恰新增 1 筆 `consult --kind advisor`、note 欄位完整。**消費之可追溯與觀察者**：修補 diff 或下一步 MUST 可追溯至回答中被引用之具體建議（收尾實錄引用回答片段）；觀察者＝本 session transcript 中該 `route.py` 呼叫之後、修補提交之前的 tool_use 序列（本地驗證命令與其輸出）與修補 diff。**Mechanism Bypass 揭露**：受控缺陷由 executor 於第 1 步預先設計，executor 本可不經顧問而修復；本證據證明「取得→消費→修驗→返回」經 Routing 走通，**不**證明顧問提供了 executor 未知之資訊（RGR 2026-10-23 F5）。
  3. **無票真實呼叫（1 次）**：`--no-ticket --payload …` 取得回答；核對無 ledger 檔／零事件、envelope 欄位完整、回答被 executor 消費於一個與有票案例不同的小問題。
  4. 每次呼叫前後做副作用快照，**分兩個對象，預期不同**：(a) **原 repo**（SmartCameraApp1；`route.py` 不在其內執行）：`git status`、`tmp/verify_cmd.conf`、`tmp/verify/active.json`、`.git/gate` 目錄、`~/.claude/settings.json` 須**完全不變**（零 ledger 寫入、零新檔）；(b) **隔離案例 repo**（`route.py` 實際執行處）：**預期有 ledger 寫入**——有票呼叫＝前置 `incident open`（ledger CLI，呼叫前）＋呼叫本身恰 +1 筆 `consult --kind advisor` 與 1 個執行記錄檔；無票呼叫＝`.git/gate` 事件數與檔案集合相對該呼叫前快照**不變**，僅新增 `tmp/_advisor_cli_ticketless/` 內 1 個執行記錄檔；以呼叫前快照為基準逐呼叫比對，非以「案例 repo 全程無寫入」為準。已知項另記：`~/.claude.json`（CLI 自行更新）與 `~/.claude/projects` 空目錄。
  5. **次數與失敗**：整張票的真實訂閱呼叫合計**上限 2 次**（有票 1、無票 1）；任何前置探測、旗標試驗、重送一律不得額外呼叫模型；呼叫一旦啟動即計入次數（含逾時、失敗、被拒），不為取得更好答案或修正參數重送；任一次失敗→如實記錄原文錯誤與判定類別，AC 15 記為未達成，其他 AC 不受影響。權限層拒絕→停止並如實回報，**不改道、不繞過、不重送同一被拒動作**；settings 權限變更屬另案授權。
  6. 收尾實錄：各 AC 證據、偏差、已知限制（含 U1–U6 之最終狀態）、DIFF_END；`projman align` 不在本票範圍（使用者依證據另行執行）。
- [x] 完成

## Hard Constraints
1. **無備援、無自動切換、無轉計費 API**：失敗只回明確失敗，不改走 `advisor.api`、`advisor.native`、Codex 或計費 API；`SOURCE_DISABLED` 時既有通用 `alternatives` 欄位為資訊性、不排序、不由入口執行，不得被實作成備援。
2. **argv 僅用已實測形狀**：不使用 `--bare`；不新增 `--setting-sources`、`--max-budget-usd` 或任何未實測旗標；模型與 effort 只來自接線表。
3. **生命週期**：前置拒絕零啟動；通過後恰啟動 1 次；任何失敗不重啟、不重試；逾時為終局判定：終止行程樹並有界回收，殘留輸出僅作診斷，不得判成功、不得回傳 `answer`、不得記帳；CLI 內部 `api_retry` 不受控且僅受牆鐘逾時界限，必須計數回報，不得宣稱「無重試」。**不可續作**：每次 consult 為單次、不可續作之操作——CLI 已回應而入口於寫執行記錄或記帳前中斷者，遺留之執行記錄只作稽核、不作狀態來源（HC8），入口不讀取它補記帳或回傳回答；之後是否再呼叫屬新的一次 consult（成本另計），由 executor 依 Gate「無新證據之相同命令、prompt…不重播」判斷並於報告揭露前次中斷（承接者＝該 Gate 條款）。入口不自動續傳、不補記帳、不自動重播；此限制只針對被中斷的那次呼叫，**不等於永久禁止**其後依 Gate 合法發起之新諮詢（Akira 2026-10-23 收準）。
4. **環境與密鑰**：子行程環境移除 `ANTHROPIC_API_KEY`／`ADVISOR_ANTHROPIC_API_KEY`／`ANTHROPIC_AUTH_TOKEN`；父環境與傳入之 `env` 物件不得被改動；任何輸出（envelope、note、執行記錄、stderr）不得含金鑰字樣（`sk-ant-` 一律遮蔽）。
5. **認證判定誠實**：`apiKeySource` 缺鍵、null／`none`、其他值三態分開；`none`／null 只證明「未使用 API key 來源」，**不得**寫成「已證訂閱」或「帳務為訂閱」；未知值 fail-closed。
6. **模型與 effort**：成功須 `modelUsage` 鍵集合恰為 {請求模型} 且 `init.model` 等於請求模型；`effort_flag` 為旗標值，**不得**宣稱實際生效。
7. **有票／無票界線**：有票沿用 M1／M4／M5 與單次 `consult --kind advisor` 記帳，僅成功才記；無票零 ledger、零事件、不呼叫 `precheck`；二者只由 `--no-ticket` 顯式區分，不得互相降級；不新增 ledger 事件型別。
8. **範圍**：不改 `advisor_call.py`／`gate_ledger.py`／`gate_stop_guard.py`／receipt schema／`.claude/skills/**`／`settings.json`；`ssot-governance.md` 只動 Advisor 條，PPSS 條與其餘節位元組不變；不改 PPSS 與 `native_advisor` 之行為（僅 AI-4 來源標籤）；不新增第二套 ledger／DB／queue／簽章／狀態檔／鎖（執行記錄檔屬 git-ignored 暫存，不作狀態來源）。
9. **測試紀律**：行為測試一律經 `route.main` 或子行程 CLI；禁直接呼叫內部 adapter／檢查函式；不新增 `main` 注入口（stub 經接線表 `cli.bin`／`cli.prefix_args`）；禁手寫 ledger 事件作為被測行為（前置用 ledger CLI）；既有 65 案除 F1 具名例外（test_S2 之 `alternatives` 預期、test_S3 之 fixture，各一行，見 AI-5）外不得修改，例外處不得弱化斷言、不得改共用 `table_copy` 或 `alternatives` 政策。
10. **真實呼叫**：整張票合計 ≤2 次；探測不得額外呼叫模型；呼叫一旦啟動即計次，不重送；不繞過權限拒絕。
11. **cwd**：固定於系統暫存目錄、已 `git init`、位於 repo 外；不使用 repo 內目錄；不使用非 git 形狀（未實測）。
12. **副作用不宣稱為零**：`--tools ""` 只保證無工具執行；hooks／plugins 仍會啟動，必須在 envelope 回報 hook 事件與 warnings，Gate 條文須載明此限定。
13. **依賴與檔案格式**：僅標準庫（`subprocess`、`tempfile`、`shutil`、`json`、`uuid`）；四個 ALLOWED 檔維持 LF／UTF-8；`route_table.json` 可被 `json.load` 讀取且無重複鍵。
14. **有界處理與輸出有效性**：成功須 `result.is_error` 為明確布林 `false` 且回答為非空白字串；必要欄位缺失／型別不符一律 `unparseable`，僅空白字串為 `empty_answer`。執行記錄寫入失敗（唯一檔名重試至多 3 次）與行程回收失敗（升級一次、等待皆有上限）皆為有界非致命：不重啟 CLI、不改判定、不迴圈、不阻塞，並在 envelope 如實揭露（`record_error`／`reaped:false`）；ledger note 之 `api_key_source` 記實際值，不得固定改寫。

## Error Handling
- AI-0 錨點命中數與票面不符 → 依 TPL v3.11「基線漂移三分流」分類記錄，資訊性分類（`ANCHOR_STALE_SCOPE_INTACT`、`CANDIDATE_SUPERSEDED`）與降級者（`BLOCKED_UNATTRIBUTED_STATE` 僅觸及 `.claude/` 面，本票 ALLOWED 全屬此面）以工作樹現值續作；僅 `ALREADY_EXECUTED_PENDING_CLOSURE`（或字面錨點無法唯一定位）→ 停止該項回報。
- 基線回歸（AI-0 第 5 項）任一失敗 → 停止回報。
- `claude` 不可解析、`claude --help` 缺必要旗標、或版本與 2.1.304 的行為差異使 C3／C5／C6 形狀不再成立 → 停止回報（不自行調整判定）。
- 實作期發現必須改 `advisor_call.py`／`gate_ledger.py`／`gate_stop_guard.py` 或任何 FORBIDDEN 檔才能達成 AC → 停止回報（不擴 scope）。
- `ssot-governance.md` 有他 session 的未提交變更或與 ALLOWED 重疊的 IN_PROGRESS 票 → 停止回報（串行，concurrency.md §2）。
- `git check-ignore -v` 對任一 ALLOWED 標的命中（被 `.gitignore` 排除）→ 停止回報（gitignore 死結律）。
- 離線測試連續 2 次修復仍編譯／斷言失敗（同一缺陷）→ 停止回報根因（Gate 逐層計輪語義不變）。
- 任一真實呼叫被權限層拒絕、逾時、未登入或回非預期形狀 → 如實記錄並停止該次，**不重送、不改道**；AC 15 記未達成；不以較弱的離線證據冒充。
- **原 repo** 之副作用快照出現本票未預期之差異（`git status`、`tmp/verify/*`、`.git/gate`、`settings.json`）→ 停止回報並保留現場，不還原他人變更；**隔離案例 repo** 之預期 ledger 寫入（有票呼叫 +1 consult、incident open；無票零新增）不算差異，與預期不符者才停止回報。
- 發現需要降低 hooks／plugins 影響（新旗標或設定）→ 記為資訊並停止擴張（另票，需先有實測）。

## Acceptance Criteria

> **AC 修訂閉環（Rev A.4）**：未移除、縮減或豁免任何 AC。AI-3 增列判定 ④b（`init.tools` 空陣列 guard）與 reason（`tools_unreported`／`tools_enabled`／`unparseable`〔`tools_invalid`〕）、cwd 判定改為最終路徑，使 AC 12 所列 `init.tools` 成為實際 guard 輸入、AC 11「cwd 不在 repo 根之下」涵蓋 junction／symlink；測試覆蓋增強（K7 增 3 案：`incident_token_ambiguous`／`evidence_token_ambiguous`／`evidence_token_missing`；K9b；K18b〔含合法外部連結仍可用〕）。共用同一母體之兄弟 AC（AC 4 前置拒絕、AC 5 失敗分類、AC 6 `apiKeySource`、AC 7 模型、AC 11 argv／env／cwd、AC 12 可達性）已重驗：route 87／ledger 59／advisor_call 25／stop_guard 54 全綠。
>
> **AC 修訂閉環（Rev A.3）**：do_native 回歸 AC 由「既有 65 案全數不修改」改為「除 F1 具名例外兩處外不修改、全數全綠」（Akira F1 裁決）。共用同一母體之兄弟 AC 已重驗：接線表存在性 AC（`table check` 含新列、`enabled` 含它）與新列 enabled 一致，判準不變；範圍 AC（`git diff --stat` 僅 ALLOWED 四檔）不變，S2／S3 同步位於 ALLOWED 之 `test_route.py`；前置拒絕與 cwd AC 之「啟動計數 0」不受影響；中斷不可續作之收準不改任何 AC 判準（僅限定被中斷之該次呼叫）。無 AC 被移除或豁免；「不修改」之限定縮為具名兩處屬 Akira 裁決，非無聲縮減。
>
> **AC 修訂閉環（Rev A.2，RGR）**：cwd 與 argv／env 之 AC（增 cwd 失敗分支之靜態驗證註記）、Advisor 條之存在性 AC（增真實性依據句）、真實呼叫 AC（增消費可追溯、觀察者與 Mechanism Bypass 揭露）經修訂。共用同一母體之兄弟 AC 已重驗：前置拒絕 AC 與 cwd 失敗分支同屬「零啟動」母體，其「啟動計數恆為 0」判準不變；可達性 AC 之 (a) 成功實測與真實呼叫 AC 同母體，(a) 之欄位支持範圍不因消費可追溯要求而改變；範圍 AC（`git diff` 僅 ALLOWED、Advisor 條以外位元組不變）不受真實性句影響（該句位於 Advisor 條內）。F1 涉及之「既有 65 案全數不修改」AC 本 Rev 未動，待裁決（已於 Rev A.3 依 Akira 2026-10-23 裁決處置，見上）。無 AC 被移除、縮減或豁免。

- [x] **[存在性]** 接線表含 `advisor.claude-cli` 列，`route.py table check` 回 `ok:true`、`callable`／`enabled` 含它；`validate_table` 對 `cli` 區塊缺失、`model` 空、`effort` 不在集合、`timeout_s` 非整數或超出 30–600、`bin` 空、`prefix_args` 非字串 list 皆回表錯誤。（AI-1）
- [x] **[行為性]** given 有票案例（隔離 repo、`loc:pending` 票、open incident、payload 含單一 `incident=` 與 `failure_sha256`／`uncertainty_sha256` 令牌）與 stub 輸出成功事件序列（init `apiKeySource="none"`、`model=R`；result `is_error=false`、`modelUsage={R}`、回答非空），expect exit 0、`disposition=RECORDED`、ledger 恰新增 1 筆 `consult --kind advisor`、note 含 `via=route.py`／`source=advisor.claude-cli`／`model=R`／`effort_flag`／`api_key_source=<實際值>`（此案 `none`）／`session_id`／`payload_sha256`／`incident`／`evidence`／`record=<相對路徑>`；另一案 stub `init.apiKeySource=null` 成功，note 為 `api_key_source=<null>`、envelope `api_key_source="<null>"`（note **不得**固定為 `none`）；envelope 含 AI-3 第 7 項全部欄位；`answer` 與 stub 回答一致；執行記錄檔存在於 `tmp/<ticket>/advisor_cli/`。（AI-2、AI-3、AI-5）
- [x] **[行為性]** given 無 ticket 的 repo（無 `Pending/` 票、無 ledger 檔）與 `--no-ticket --payload`，stub 輸出成功序列，expect exit 0、`disposition=ANSWERED`、`mode=ticketless`、`.git/gate` 下**無任何新檔**、ledger 事件數不變；`--no-ticket` 加 `--incident`、`--evidence` 或全域 `--ticket`→`usage` 且 stub 啟動計數為 0；非 git repo 內→`usage not_a_git_repo` 且啟動計數為 0；**無 `--no-ticket`** 且 ticket 解析失敗→維持 `REFUSED`／`ledger_refused`（不自動降級）且啟動計數為 0；非 `claude_cli` 來源帶 `--no-ticket`→`usage`。（AI-2、AI-3、AI-5）
- [x] **[行為性]** given 各前置拒絕情境：缺 `--payload`、有票缺 `--incident` 或 `--evidence`、令牌缺失／衝突／不符（沿用既有 reason）、incident 不存在或已關閉、票非 `loc:pending`、payload 不可讀／非 UTF-8／超過 200000 bytes、`cli.bin` 不可解析（`cli_unavailable`），expect 明確 reason／exit，且 stub **啟動計數恆為 0**、ledger 事件數不變。（AI-2、AI-5）
- [x] **[行為性]** given 啟動後之失敗 stub 場景，expect 明確 reason、不記帳、啟動計數恰 1、無重啟：未登入形狀（依 C5：assistant `error=authentication_failed`、`result.is_error=true`、`subtype="success"`、`modelUsage` 空）→`not_logged_in`（exit 6）；`model_not_found`→`model_unavailable`；其他 assistant error 類別→`cli_error` 且 `detail` 含類別；`init` 缺失→`init_missing`；無 `result` 且 returncode≠0→`nonzero_exit`；stdout 非 JSON→`unparseable`；returncode≠0 而 `is_error=false`→`nonzero_exit`；`result.is_error` 缺、`null`、字串 `"false"`、數字 `0`→`unparseable`（`detail=is_error_invalid`，**不得**判成功）；`modelUsage` 存在而非 object→`unparseable`（`model_usage_invalid`）。（AI-3、AI-5）
- [x] **[行為性]** given `system/init` 之 `apiKeySource` 為：缺鍵→`auth_unreported`；`null`→通過；`"none"`→通過；`"user"`、`"project"`、`"ANTHROPIC_API_KEY"`、`""`、數字、object 等其他值→`api_key_source` 且 `detail` 含（遮蔽後）原值；失敗者皆不記帳、啟動計數 1；envelope `api_key_source` 依規則呈現 `"<absent>"`／`"<null>"`／原值。（AI-3、AI-5）
- [x] **[行為性]** given `modelUsage` 鍵集合與 `init.model` 的組合：M＝{R}且 I＝R→成功；M＝{R, 他者}→`multiple_models`；M＝{他者}→`model_mismatch`；M 為空或缺→`model_unreported`；M＝{R} 但 I≠R→`model_mismatch`；失敗者皆不記帳，`multiple_models` 之 envelope 帶全部模型與 token。（AI-3、AI-5）
- [x] **[行為性]** given 會睡眠逾 `timeout_s` 之 stub（表中 `timeout_s` 於測試縮短至允許下限以上之最小值），expect `CALL_FAILED timeout`、`reaped:true`、不記帳、啟動計數 1、**stub 與其生出的孫行程皆已不存在**（真實子行程驗證，非 mock）。given stub 於逾時前已輸出 `api_retry` 事件，expect envelope `diagnostic:true`、`cli_internal_retries` 為該計數且仍為 `timeout`（殘留輸出僅供診斷）。given stub 先輸出**完整成功序列**（含非空回答與 `is_error=false`）再睡眠逾時，expect 仍為 `timeout`、envelope **無** `answer`、不記帳（ledger 零新增 consult）。回收失敗分支（升級 `Popen.kill()` 後仍存活→`reaped:false`＋`process_not_reaped` warning、仍為 `timeout`）無注入口可機械重現：以靜態審查驗證並標「靜態、非行為驗證」。（AI-3、AI-5）
- [x] **[行為性]** given exit 0、`is_error=false` 但回答為空字串或全空白字串→`empty_answer`（exit 5）、不記帳、envelope 仍帶 `session_id`／`usage`／`cost_reported_usd`；回答缺、`null`、數字等非字串→`unparseable`（`answer_invalid`，exit 6）、不記帳（空白字串與非字串分類不得混用）。given 有票呼叫中 incident 被（stub 於回答前經真實 ledger CLI）關閉→`incident_closed_after_call`（exit 5）：回答、記錄檔路徑與 `session_id` 保留於 envelope、`recorded:false`、ledger 零新增 consult、啟動計數恰 1、incident 保持已關閉（不重開）。（AI-3、AI-5）
- [x] **[行為性]** given stub 輸出 hook 事件（成功、非零 exit、stdout 非空）與 `api_retry` 事件，expect envelope `hooks` 列出事件／名稱／outcome／exit_code，非零 exit 或 stdout 非空與 `api_retry` 皆進 `warnings`，且不改變成功判定。（AI-3、AI-5）
- [x] **[行為性]** given 任一已啟動呼叫，stub 記錄顯示：argv 恰為 AI-3 第 1 項之固定形狀（無 `--bare`／`--setting-sources`／`--max-budget-usd`，`--model`／`--effort` 值取自表）；環境鍵名**不含**三個 API 金鑰變數（即使呼叫端 `env` 含之）且呼叫端 `env` 物件於呼叫後未被改動；cwd 位於系統暫存之固定目錄、含 `.git`、且不在 repo 根之下；stdin 以固定角色前言開頭並含 payload 全文；任何 stdout／stderr／envelope／記錄檔不含測試注入之假金鑰字串。**執行記錄**：連續兩次呼叫（含同一秒內）之記錄檔名互異；`advisor_cli` 路徑被一般檔案占用而無法寫入記錄時，判定與記帳**不變**（成功仍成功並記帳、失敗仍為原分類）、envelope `record:null` 與 `record_error`、`warnings` 含之，有票成功者 ledger note 為 `record=none`。cwd 準備失敗分支（AI-3 第 2 項之 `cwd_not_directory`／`cwd_prepare_failed`／`cwd_inside_repo`，皆零啟動）若無法不新增注入口而機械重現，以靜態審查驗證並標「靜態、非行為驗證」。（AI-3、AI-5）
- [x] **[可達性]** 判定所依 guard 輸入（`system/init.apiKeySource`／`init.model`／`init.tools`、`system/hook_*`、`system/api_retry`、assistant `error`、`result.is_error`／`terminal_reason`／`modelUsage`／`result`）在 production 路徑有 producer。**三種證據分開記錄、不互相升格、不合併計為「已證」**，每欄位逐一標明其實際只由哪一類證據支持：
  - **(a) 成功實測**〔`[已執行-真實]`〕：AI-8 真實呼叫之 stream 擷取（有票 1、無票 1）；僅能支持成功形狀會出現之欄位（`init.*`、`hook_*`、`result.is_error=false`／`modelUsage`／`result`）。真實呼叫未執行、失敗或被拒→(a) 記未達成，不以 (b)(c) 補足。
  - **(b) 歷史失敗輸出**〔`[歷史-不重現]`〕：2026-10-23 開票前之 C5（未登入：assistant `error`、`result.is_error=true`、`terminal_reason`）與 C6（不可達：`system/api_retry`）實測；本票不重做該呼叫（不新增探測）。須載明原始輸出是否仍可取得（路徑:行號）；已消失者只能引用證據分級 C5／C6 之欄位描述，標「歷史、描述性、不可重現」，**不得**重建為原始輸出。
  - **(c) stub 證據**〔`[stub]`〕：AI-5 離線案例（依 C3／C5／C6 描述自造事件序列）；只證明入口對該欄位之解析與判定行為，**不證明** production producer，標「僅 stub 層」。
  **producer 證據欄（執行後回填；每欄位一列，分 a／b／c 三欄）**：入口讀取該欄位處＝`.claude/scripts/route.py:<行號>`；(a)＝`<路徑>:<行號>`或「未取得」；(b)＝`<路徑>:<行號>`或「描述性」；(c)＝`test_route.py:<測試名>`。（AI-3、AI-5、AI-8）
- [x] **[行為性]** `do_native` 之 ledger note 記 `source=<a.source>`（表內以 `native_advisor` 命名之別名來源於 note 記其實際來源名）；既有 65 案除 F1 具名例外兩處（AI-5；test_S2 預期、test_S3 fixture，各一行、斷言強度不變）外全數不修改，且全數（含該兩案）全綠；`test_gate_ledger.py`（59）、`test_advisor_call.py`（25）、`gate_stop_guard_test.py`（54）全綠。（AI-4、AI-7）
- [x] **[存在性]** `ssot-governance.md` Advisor 條含：`advisor.claude-cli` 於來源清單；入口先核對再單次啟動、固定旗標、不帶工具與 MCP、子行程無三個 API 金鑰變數、牆鐘逾時、不重試、不轉其他通道或計費 API、僅成功才記帳；envelope／note 標明通道／模型／effort 旗標／`apiKeySource` 及其證明範圍限定；`--no-ticket` 無票模式（顯式、不寫 ledger、不改 Gate 範圍、不降級）；hooks／plugins 仍會啟動之限定；`advisor.claude-cli` 真實性依據句（執行記錄非耐久、`session_id` 非供應商端證明）；原 `advisor.api`／`advisor.native` 句與「Advisor 不可達」處置逐字保留；檔案 LF；PPSS 條與其餘節位元組不變；`gate_stop_guard.py`、`advisor_call.py`、`gate_ledger.py`、`.claude/skills/**` 之 `git diff` 為空。（AI-6、AI-7）
- [x] **[行為性]** **真實呼叫（整張票合計 ≤2 次，明標受控代表案例，Opus 5.5／High，訂閱通道，`ANTHROPIC_API_KEY` 未設）**：有票 1 次——經 Router 取得回答（`apiKeySource` 為 none、實際模型 `claude-opus-5-5`、ledger 恰 1 筆 consult、note 欄位完整）→ executor 先本地驗證回答說法→採納並修復→測試轉綠→執行原流程下一必要步驟（亦作為 P1-C⑤ 之經 Routing 全鏈證據；消費須可追溯：修補 diff 或下一步引用回答之具體建議，觀察者為 transcript tool_use 序列與修補 diff；不證明回答為解題所必需，受控缺陷為 executor 預知，見 AI-8 第 2 步）；無票 1 次——`--no-ticket` 取得回答、零 ledger 事件、envelope 欄位完整、回答被消費。每次呼叫前後副作用快照分兩對象：**原 repo**（SmartCameraApp1）之 `git status`、`tmp/verify_cmd.conf`、`tmp/verify/active.json`、`.git/gate`、`~/.claude/settings.json` 須**完全不變**；**隔離案例 repo**（`route.py` 實際執行處）**預期有 ledger 寫入**——有票呼叫恰 +1 筆 `consult --kind advisor`（另有呼叫前由 ledger CLI 開立之 incident），無票呼叫 `.git/gate` 事件數與檔案集合相對呼叫前快照不變；兩者皆新增 1 個執行記錄檔。`~/.claude.json` 與 `~/.claude/projects` 空目錄為已知項另列。呼叫失敗或被權限層拒絕者如實記錄、不重送，本 AC 記未達成。失敗類別案例以離線 stub 證明並標「僅 stub 層」。（AI-8）
- [x] **[存在性]** `git diff --stat` 僅涉及 ALLOWED 檔案（`.claude/scripts/route.py`、`.claude/scripts/route_table.json`、`.claude/scripts/tests/test_route.py`、`.claude/rules/ssot-governance.md`）；四檔 EOL 仍 `i/lf w/lf`。
- [ ] ~~`assembleDebug` PASS~~（N/A：無 `app/`／gradle 變更；feature preset 之純腳本與治理條文票）

## 收尾實錄（實作期，2026-10-23；DIFF_REF=bd8b532f，DIFF_END=9a4fc213〔Rev A.4 後〕）

- **AI-0**：錨點 10 項各命中恰 1；`BASE_COMMIT..HEAD` 無觸及 ALLOWED 之提交（HEAD 仍 `bd8b532f`）；四檔 `i/lf w/lf`、`check-ignore` 無命中、工作樹 clean、registry 無 IN_PROGRESS；`claude` 2.1.304、`--help` 含全部必要旗標；基線回歸 route 65／ledger 59／advisor_call 25／stop_guard 54 全綠。`DIFF_REF` 已回填。
- **AI-1～AI-4、AI-6**：`route_table.json` +2 行（新列）、`route.py` +377／−5（含 docstring）、`ssot-governance.md` 僅 Advisor 條單行（來源清單加 `advisor.claude-cli`＋一段通道說明）。`do_native` note 改 `source={a.source}`。
- **AI-5／AI-7**：`test_route.py` 新增 20 案（`ClaudeCliTests` K1–K19＋K15b），既有 65 案僅 F1 具名同步兩處（S2：`alternatives` 預期一行；S3：fixture 另停用 `advisor.claude-cli` 一行），`git diff -U0` 於既有區僅見該兩 hunk。全量 `python -I`：route 85／ledger 59／advisor_call 25／stop_guard 54 全綠；`table check` ok、callable 4；`git diff --name-only bd8b532f` 僅四檔；FORBIDDEN 檔 diff 為空；EOL 仍 `i/lf w/lf`。離線測試前後原 repo `.git/gate` 檔案集合不變。
- **突變驗證**（暫時改 `route.py`、跑對應測試、還原並 `cmp` 確認位元組相同）：is_error 型別守衛移除／note 固定 `none`／API 金鑰環境變數未移除／逾時改以殘留輸出判定／rc≠0 仍成功／multiple_models 放行／空白回答放行／`--no-ticket` 混用 `--ticket` 放行／記錄寫入失敗改變判定——9 項皆被對應測試殺死；`taskkill` 去掉 `/T` 之突變導致 K11 逾時掛起（孫行程殘留，已手動清除），視為被殺（非斷言訊息證據）。
- **AI-8（真實呼叫合計 2 次，皆成功，無補額度、無新增探測）**：隔離案例 repo＝session scratchpad `clicase`（非耐久）。
  - ①有票（`--ticket CASE-CLI-01`，`INC-001`）：exit 0、`RECORDED`、`api_key_source=none`、模型 `claude-opus-5-5`、`effort_flag=high`、`session_id=439f77ac-6a6a-4232-b247-aa16f1af44dd`、`cli_internal_retries=0`、hooks 2 個 SessionStart 皆 exit 0、14.8 s、CLI 牌價 0.0472 USD（非帳務）；ledger 恰 +1 筆 consult，note 欄位完整（`channel`／`model`／`effort_flag`／`api_key_source`／`session_id`／`payload_sha256`／`incident`／`evidence`／`record`）。**消費**：回答指「`return base * 2 ** attempt` 改 `min(base * 2 ** attempt, 30.0)`，並先確認 check.py 載入同一份 retry_delay.py」→ 本地驗證前提（`retry_delay.__file__` 即案例目錄那份）、驗證其附帶說法（attempt=1100 確實 `OverflowError`）→ 套用該一行 → `check.py` 由失敗轉 `ok` → 執行模組 CLI（`retry_delay.py 10`→`30.0`、`0`→`1.0`）。觀察者＝本 session transcript 中該 `route.py` 呼叫之後的驗證命令與修補 diff（`retry_delay.py` 1 行）。**Mechanism Bypass 揭露**：缺陷由 executor 預先設計，本證據證明「取得→消費→修驗→返回」經 Routing 走通，**不**證明顧問提供了 executor 未知之資訊。
  - ②無票（`--no-ticket`）：exit 0、`ANSWERED`、`mode=ticketless`、同模型／旗標／`api_key_source=none`、12.0 s、0.0311 USD（牌價）；案例 repo ledger 檔案集合與行數不變（2→2）、僅新增 `tmp/_advisor_cli_ticketless/` 內 1 個執行記錄檔。**消費**：回答指「改用整數 tuple key，並先確認標籤皆為 `v`＋純數字段」→ 本地驗證前提（4 個標籤皆符合 `v\d+\.\d+\.\d+`）與附帶說法（`v1.2.0-rc1` 會 `ValueError`）→ 套用 → `tags.py` 輸出符合預期順序。
  - **副作用快照**：原 repo（`git status`、`tmp/verify_cmd.conf`、`tmp/verify/active.json`、`.git/gate` 各檔雜湊、`~/.claude/settings.json`）兩次呼叫前後**完全相同**；案例 repo 依預期（有票 +1 consult／無票零新增）；已知項：`~/.claude.json` 101688→101717 bytes（CLI 自行更新）、`~/.claude/projects` 新增空目錄 `C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd`。
- ~~**AC 12 producer 證據欄**（入口讀取處 `route.py` 行號為提交前現值）~~（**已作廢**：`init.tools` 現已有 guard、行號已變；以文末〈AC12 原始事件嵌入〉為準）：
  | 欄位 | route.py | (a) 成功實測 | (b) 歷史失敗輸出 | (c) stub |
  | --- | --- | --- | --- | --- |
  | `init.apiKeySource` | :667–:691 | 兩次真實呼叫記錄（`clicase/tmp/**/advisor_cli_*.json` 之 `stream`）：`"none"` | `opuscase/probe_emptycfg.jsonl:1`（未登入時亦為 `"none"`） | K9、K3、K4 |
  | `init.model` | :712 | 同上：`claude-opus-5-5` | 同上 | K10 |
  | `init.tools` | 未讀取 | 同上：`[]`（僅記錄於 stream 擷取，**非判定輸入**，與票面 AC 12 列舉有出入，如實揭露） | — | — |
  | `system/hook_*` | :648 | 同上：2×SessionStart 皆 exit 0 | `opuscase/probe_unreach.jsonl`（hooks 事件） | K15 |
  | `system/api_retry` | :655 | 未取得（成功路徑不出現） | `opuscase/probe_unreach.jsonl:6–:13`（attempt 1..8） | K11、K15 |
  | assistant `error` | :659 | 未取得 | `opuscase/probe_emptycfg.jsonl:2`（`authentication_failed`） | K8 |
  | `result.is_error` | :693 | 同上：`false` | `opuscase/probe_emptycfg.jsonl:3`（`true`） | K8 |
  | `result.terminal_reason` | :698（僅入 detail） | 同上：`completed` | 同上：`api_error` | K8 |
  | `result.modelUsage` | :706 | 同上：僅 `claude-opus-5-5` | 同上：`{}` | K10 |
  | `result.result` | :716 | 同上：非空字串 | 同上（未登入文字） | K13 |
  （(b) 之 `opuscase/` 為 session scratchpad，非耐久；本表即其摘要。stub 事件序列依 C3／C5／C6 欄位自造，hook／result 之欄位名取自上列保存之原始輸出，**非**原始輸出本身。）
- **偏差／詮釋**：①判定序 ③ 之前新增「stdout 無任何可解析 JSON 事件→`nonzero_exit`／`unparseable`」，使 AC 5「stdout 非 JSON→`unparseable`」與「init 缺失→`init_missing`」可並存；②`api_key_source` 值為空字串時 envelope 照原值呈現（空字串）；③`init.tools` 未納入判定（見上）；④逾時測試因 `timeout_s` 下限 30 s，K11／K12 各耗約 30 s。
- **已知限制／UNVERIFIED**：回收失敗分支（`reaped:false`）與 `cwd_prepare_failed` 僅靜態審查（無注入口，不新增）；`effort` 實際生效值不可得；`apiKeySource=none` 不證訂閱帳務；U7（`CLAUDE_CODE_SUBPROCESS_ENV_SCRUB=1`）未被觀察到——本 session 該變數**未設定**，兩次真實呼叫在未設定下成功，故 U7 維持 `HYPOTHESIS`；U1 值域僅觀察 `"none"`；`ADVISOR_ANTHROPIC_API_KEY` 於父環境有設定而子行程未帶（`apiKeySource=none`），與預期一致；缺 `--incident` 之判定優先序變更已揭露（AI-2），既有 65 案無依賴；`projman align` 不在本票範圍。

- **提交與審查軌跡**：實作 `781dcd49`（4 檔）；`/qa` Step 0 PASS、Step 4 SKIPPED（FORBIDDEN 含 PROGRESS.md，同步債登記於 QA 報告）。`/sv` R0 第一次 FORWARD（無 ACTIONABLE／UNDECIDABLE／AUTHORITY 列）→ Codex R1 回 **REPORT_FIX**：RF-1（SV report 缺可核驗 producer 證據）與 RF-2（docstring「Every launched call leaves…」與非致命記錄寫入不一致）＋ADVISORY（回收失敗與 `cwd_prepare_failed` 僅靜態）。處置：RF-1 於 SV report 內嵌逐欄 producer 擷取（真實成功 stream 兩份＋歷史失敗輸出兩份，含行號）並標明 `api_retry`／assistant `error` 僅歷史證據；RF-2 判為措辭不一致而非行為缺陷（票面 AI-3 第 8 項／HC14 即為非致命 best-effort），提交 `22770829` 將 `route.py` docstring 改為「ATTEMPTS…非致命」並於 Advisor 條補「寫入失敗為有界非致命、不改判定」；ADVISORY 維持揭露（無注入口，不新增）。Gate：incident `cross-review:codex-report-fix-r1`（INC-001）executor 層 round 1。
- **Cross-Review 終局（如實，不是 PASS）**：Codex 三次 verdict 皆 **REPORT_FIX**，第三次後腳本輸出 `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2，步驟級上限，記 `step_limit`；非 BLOCKER、不再重跑）；**無終局 verdict**，`reviews.codex` 填 `N_A`（≠PASS）。R0 共五次（第一／二／三／五次 FORWARD，第四次 CHECKPOINT-REQUIRED〔UNDECIDABLE＋ACTIONABLE，已本地取證與修正，第五次確認消除〕），記錄於 `.tmp/fail-advisor/`（非耐久；第三次記錄為補記 `_LATE`，漏寫為執行端疏失）。Gate：incident INC-001（`cross-review:codex-report-fix-r1`）保持 open；executor 層 2 輪 fail、r0 層 1 輪 fail、advisor 層 0；`step_limit` 1；中途 seq 13 `NO_PROGRESS`（stop-guard 於等待 R0 subagent 期間連擋 5 次、第 6 次升報）經 Akira 對話授權 `human resolve`（seq 16），原事件與計數保留。**程序違反紀錄**：`check commit` 回 exit 1（因上述待人工）時，執行端把檢核與 `git commit` 串在同一指令且未以 `&&` 阻擋，導致提交 `8e4088b4` 在檢核失敗狀態下完成；Akira 裁決保留該提交、不還原；其後所有提交均以 `check commit` exit 0 為前提。
- **三次 Codex finding 與處置**：R1＝RF-1 SV 報告缺可核驗 producer 證據（已於 SV 報告內嵌逐欄擷取）、RF-2 docstring「Every launched call leaves…」與非致命記錄不一致（已改措辭，`22770829`）。R2＝RF-1 報告誤稱 `null` 被拒絕（實際被接受，已更正並標 stub-only）、RF-2 前置 guard 外部輸入漏列（已補四類並列 producer）、ADVISORY `modelUsage` 內層值非 object 會 `AttributeError`（真實缺陷，已修，`8e4088b4`）。R3（LIMIT，未解除）＝**RF-1** Stage B 對 `ledger_status`／payload／PATH／temp-CWD 僅寫類別未附 `檔案:行號`；**ADVISORY** temp-CWD 之 repo 外保證只 resolve `parent`、未再 resolve 組合後 `base`，固定路徑若被預先建為指向 repo 之 junction／symlink 則 `cwd_inside_repo` 判不到。R0 第三次另提示 `diagnostics_unavailable` 未實作，已補（`5abbd211`）。
- **R3 RF-1 之補證（LIMIT 後補入本票檔，Codex 未複審）**：guard 輸入之 consumer（`route.py` @HEAD `5abbd211`）＝`precheck` :378／`ledger_status` :366（讀 ledger `status --json`）、`Path(a.payload).read_bytes()` :794、`check_tokens` :450／呼叫 :806、`shutil.which(cli["bin"])` :807、`prepare_cli_cwd` :573／呼叫 :810、`communicate(timeout)` :825、`kill_cli_tree` :828。producer＝ledger 狀態由真實 `gate_ledger.py cmd_status`（:547，輸出 `ticket_location`／`incidents` :540）產生，測試與 AI-8 均經真實 ledger CLI（AI-8 案例 ledger `CASE-CLI-01.jsonl` seq 1 finding→seq 2 consult）；payload／evidence 為呼叫端寫入之檔案（測試 `good_api()` :170、`evidence()` :162，K7 逐案；AI-8 真實案例檔）；`claude` 由 OS PATH 解析（AI-8 解析到 `C:\Users\Administrator\.local\bin\claude.EXE`）；temp-CWD 由 OS 檔案系統提供（K16／K18、AI-8 真實）。逾時狀態之 producer＝真實子行程（K11／K12 以真實孫行程驗行程樹回收，`reaped` 為 `True`）；`reaped:false` 與 `diagnostics_unavailable:true` 分支無注入口，僅靜態。
- **未解除之已知限制（ADVISORY，本票不另修）**：temp-CWD 之 reparse-point 缺口——利用面需本機使用者預先把固定路徑 `<暫存目錄>/cueshot_advisor_cli_cwd` 建成指向 repo 之 junction／symlink；LIMIT 後再改碼將成為未經 Codex 複審之尾端提交，故不修，列後續修補候選（修法：對組合後 `base` 於 `exists()` 時再 `resolve()` 並重做 repo 內判斷，加 junction 測試）。
- **最終測試（HEAD `5abbd211`）**：`python -I`——`test_route.py` 85 OK（既有 65〔僅 S2／S3 具名同步〕＋新增 20）、`test_gate_ledger.py` 59 OK、`test_advisor_call.py` 25 OK、`gate_stop_guard_test.py` 54 OK（後三者於 `8e4088b4` 重跑，其後僅動 `route.py`／`test_route.py`）。真實呼叫共 2 次皆成功。DIFF_REF=bd8b532f，DIFF_END=5abbd211（範圍含他 session 之 `72352908` `[COMPACT-SYNC]`〔僅 PROGRESS.md，已揭露於 SV 報告 Excluded Payload〕）。`projman align` 未執行（票面載明由使用者依證據另行執行）。


## 證據附錄（Rev A.4；AC15／AC16 補件，2026-10-24）

> 本附錄**不重造原件、不新增模型呼叫**：逐字收錄 2026-10-23 AI-8 真實呼叫當時產生、仍存於 session scratchpad（非耐久）之原始輸出，並附大小與 sha256 以建立可定位對應。原件於 `scratchpad` 目錄 `D:\...\scratchpad\`（`...\d7f48063-6006-4d6e-a8e3-2f7fa366bc43\scratchpad`）。

### AC15 可定位對應（真實呼叫 → 原件 → 觀察者）
- **觀察者（transcript）**：session `d7f48063-6006-4d6e-a8e3-2f7fa366bc43` 之 `~/.claude/projects/<專案>/d7f48063-….jsonl`，`tool_use` 序列（UTC，id 為尾 8 碼）：10:27:07 `RJTa87Lq` 呼叫前快照（`snap_before1.json`）→ 10:27:21 `nQgpYUkN` **有票真實呼叫**（`route.py --ticket CASE-CLI-01 consult --source advisor.claude-cli --incident INC-001 …`，輸出 `real1_out.json`）→ 10:27:42 `PExfp9EJ` 讀取 envelope／回答／ledger consult → 10:27:50 `9Ga3DuEX` **本地驗證回答前提**（`retry_delay.__file__`、`OverflowError` 附帶說法）→套用修補→`check.py` 由失敗轉 `ok`→`retry_delay.py 10`／`0` → 10:27:59 `fPzkAkp2` 呼叫後快照（`snap_after1.json`）→ 10:28:08 `acF4MM9C` **無票真實呼叫**（`route.py consult --no-ticket …`，輸出 `real2_out.json`）→ 10:29:05 `Rd7PirWK` 本地驗證並套用 `tags.py` 修補、`snap_after2.json`。
- **原件清單**：
  - `real1_out.json`：2327 bytes, sha256=f3a760384d54aa6d24175725a6f9bd8a19bfb67231254b49bf79ef2396a91555；`real2_out.json`：2341 bytes, sha256=fd2a9b26a2b680a5b826d9905800707f8d0ac99d2d9e8751096836ff9dbd59d6
  - `snap_before1.json`：3166 bytes, sha256=7030b17b7d69b14790039024499b1ffdc49997d3bde2003047142db85a210d38；`snap_after1.json`：3323 bytes, sha256=59650f1d06b97d2c136353111367afd0bb74e3de56229da78ef6fb7775f89c3c；`snap_after2.json`：3403 bytes, sha256=c925142f169b905a01986ab86a72bc6ae31405abf0d7680b8ebb124d7c11d162
  - `case_payload.md`：1147 bytes, sha256=df76ab9f26569cd7814be2537a733d8fbf1436ca84af5ebb4879161ca2e201d0；`case_evidence.txt`：380 bytes, sha256=ec96c1449e9fb4081170017cd767d55e506d1e5a550f5f81a560d2727feb73f8；`case_payload_tl.md`：490 bytes, sha256=69288fd254a44fc9afa9211a928d39fc0f34b6f47d07808ec3651f2a5c16e763
  - 案例 ledger `clicase/.git/gate/CASE-CLI-01.jsonl`：857 bytes, sha256=710201990fa8c53f91d61bb68cfc678f4c88ba74b77548c156aed6b6815aa649
  - 入口執行記錄（含完整 stream）：
  - `clicase/tmp/_advisor_cli_ticketless/advisor_cli_20261023T102820Z_a6165187.json`：18702 bytes，sha256=b1019eb65b7b7b6dd2d3bcea56474bd385b2934aa0875a1cce5f665cdd79d190
  - `clicase/tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json`：19102 bytes，sha256=4cebc11a897226ba56713e3eef11e2897b92a3ff24558d1d62e97e6d52bf4a47
- **完整 envelope 原件（有票，`real1_out.json` 逐字）**：
```json
{"ok": true, "verb": "consult", "via": "route.py", "mode": "ticketed", "channel": "claude-subscription-cli", "requested_model": "claude-opus-5-5", "models_used": {"claude-opus-5-5": {"inputTokens": 2, "outputTokens": 603, "cacheReadInputTokens": 1559, "cacheCreationInputTokens": 4352}}, "effort_flag": "high", "api_key_source": "none", "session_id": "439f77ac-6a6a-4232-b247-aa16f1af44dd", "usage": {"input_tokens": 2, "cache_creation_input_tokens": 4352, "cache_read_input_tokens": 1559, "output_tokens": 603, "output_tokens_details": {"thinking_tokens": 294}, "server_tool_use": {"web_search_requests": 0, "web_fetch_requests": 0}, "service_tier": "standard", "cache_creation": {"ephemeral_1h_input_tokens": 4352, "ephemeral_5m_input_tokens": 0}, "inference_geo": "global", "iterations": [{"input_tokens": 2, "output_tokens": 603, "cache_read_input_tokens": 1559, "cache_creation_input_tokens": 4352, "cache_creation": {"ephemeral_5m_input_tokens": 0, "ephemeral_1h_input_tokens": 4352}, "type": "message"}], "speed": "standard", "fallback_credit": null}, "cost_reported_usd": 0.047195799999999996, "cost_note": "CLI list-price figure, not billing", "duration_ms": 14774, "cli_internal_retries": 0, "hooks": [{"event": "SessionStart", "name": "SessionStart:startup", "outcome": "success", "exit_code": 0}, {"event": "SessionStart", "name": "SessionStart:startup", "outcome": "success", "exit_code": 0}], "warnings": [], "record": "tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json", "model": "claude-opus-5-5", "answer": "1. **改哪行**：`retry_delay.py` 裡 `delay()` 的 `return base * 2 ** attempt`。\n2. **改成**：`return min(base * 2 ** attempt, 30.0)`。這樣 delay(0)=1.0、delay(3)=8.0 不變，delay(10) 被截到 30.0，回傳值仍是 float。\n3. **套用前先驗證**：確認 `check.py` 載入的就是你要改的那份 `retry_delay.py`。traceback 的路徑在 `...\\scratchpad\\clicase\\`，要排除 sys.path 上有別的同名副本，否則改了也不會生效。\n4. 附帶一提：如果 attempt 可能非常大（約 >1023），`base * 2 ** attempt` 在比較前就會拋 OverflowError。check.py 不測這種情況，但需要時可以先把 attempt 夾住。", "disposition": "RECORDED", "ledger_seq": 2, "detail": "evidence=failure", "source": "advisor.claude-cli"}
```
- **完整 envelope 原件（無票，`real2_out.json` 逐字）**：
```json
{"ok": true, "verb": "consult", "via": "route.py", "mode": "ticketless", "channel": "claude-subscription-cli", "requested_model": "claude-opus-5-5", "models_used": {"claude-opus-5-5": {"inputTokens": 2, "outputTokens": 555, "cacheReadInputTokens": 3189, "cacheCreationInputTokens": 2418}}, "effort_flag": "high", "api_key_source": "none", "session_id": "43f06819-833a-417f-984d-d393412b3836", "usage": {"input_tokens": 2, "cache_creation_input_tokens": 2418, "cache_read_input_tokens": 3189, "output_tokens": 555, "output_tokens_details": {"thinking_tokens": 226}, "server_tool_use": {"web_search_requests": 0, "web_fetch_requests": 0}, "service_tier": "standard", "cache_creation": {"ephemeral_1h_input_tokens": 2418, "ephemeral_5m_input_tokens": 0}, "inference_geo": "global", "iterations": [{"input_tokens": 2, "output_tokens": 555, "cache_read_input_tokens": 3189, "cache_creation_input_tokens": 2418, "cache_creation": {"ephemeral_5m_input_tokens": 0, "ephemeral_1h_input_tokens": 2418}, "type": "message"}], "speed": "standard", "fallback_credit": null}, "cost_reported_usd": 0.0310898, "cost_note": "CLI list-price figure, not billing", "duration_ms": 12021, "cli_internal_retries": 0, "hooks": [{"event": "SessionStart", "name": "SessionStart:startup", "outcome": "success", "exit_code": 0}, {"event": "SessionStart", "name": "SessionStart:startup", "outcome": "success", "exit_code": 0}], "warnings": [], "record": "tmp/_advisor_cli_ticketless/advisor_cli_20261023T102820Z_a6165187.json", "model": "claude-opus-5-5", "answer": "1. **最小修法**：把排序的 key 從字串改成整數 tuple，例如 `sorted(tags, key=lambda t: tuple(map(int, t.removeprefix('v').split('.'))), reverse=True)`。這樣 `10 > 9` 會按數值比較，而不是按字元比較。\n2. **套用前要確認**：所有標籤都是 `v` 加上純數字段，例如 `vX.Y.Z`，而且段數都一樣。\n3. 如果有 `v1.2.0-rc1` 這種預發布後綴，`int()` 會拋出 `ValueError`。如果有 `v1.2` 和 `v1.2.0` 混在一起，兩者的先後順序會出乎預期，需要先補齊段數或另外處理。\n4. `str.removeprefix` 要 Python 3.9 以上；版本較舊就改用 `t[1:]`，但前提是確定每個標籤都以 `v` 開頭。", "disposition": "ANSWERED", "detail": "ticketless: no ledger event was written", "source": "advisor.claude-cli"}
```
- **案例 ledger 原件（`CASE-CLI-01.jsonl` 逐字；seq 1 finding → seq 2 consult〔有票呼叫，note 欄位完整〕，無票呼叫未新增事件）**：
```json
{"seq": 1, "ts": "2026-10-23T10:27:08+00:00", "ticket": "CASE-CLI-01", "type": "finding", "incident": "INC-001", "new": true, "subjects": ["case:retry-delay-cap"], "gate_point": null, "verdict": null, "same_root_cause": null, "independent_evidence": null, "attach_reason": null}
{"seq": 2, "ts": "2026-10-23T10:27:37+00:00", "ticket": "CASE-CLI-01", "type": "consult", "incident": "INC-001", "kind": "advisor", "note": "source=advisor.claude-cli via=route.py channel=claude-subscription-cli model=claude-opus-5-5 effort_flag=high api_key_source=none session_id=439f77ac-6a6a-4232-b247-aa16f1af44dd payload_sha256=df76ab9f26569cd7814be2537a733d8fbf1436ca84af5ebb4879161ca2e201d0 incident=INC-001 evidence=failure:ec96c1449e9fb4081170017cd767d55e506d1e5a550f5f81a560d2727feb73f8 record=tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json"}
```
- **副作用快照原件（呼叫前 `snap_before1.json`／有票後 `snap_after1.json`／無票後 `snap_after2.json`，逐字）**：
```json
{
 "case": {
  "git_gate": {
   "CASE-CLI-01.jsonl": "7e6d7d05d20bdefc"
  },
  "ledger_lines": 1,
  "tmp_files": []
 },
 "known": {
  "claude_json": 101688,
  "claude_projects": [
   "C--Users-Administrator",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-183f1ec9-554f-4aa1-939c-98fef1fabb1c-scratchpad-stopprobe",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-d7f48063-6006-4d6e-a8e3-2f7fa366bc43-scratchpad-opuscase-cwd",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-d7f48063-6006-4d6e-a8e3-2f7fa366bc43-scratchpad-p1case",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-f553949a-6aa0-4325-ba8c-1cb99aa6df54-scratchpad-wire-low",
   "D--DevSources-AndroidProj-SmartCameraApp1--tmp-ticket-author-20261013-t04-probe-run-20261013-144805",
   "D--DevSources-AndroidProj-SmartCameraApp1--tmp-ticket-author-20261013-t04-probe-run-adv-20261013-165641",
   "D--DevSources-AndroidProj-SmartCameraApp1--tmp-ticket-author-wire-probe0",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain1",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain4a",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain4b",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain5",
   "D--DevSources-claude-mods",
   "d--DevSources-AndroidProj-SmartCameraApp1",
   "d--DevSources-ObsidianVault",
   "d--DevSources-workflow-mcp-ticket-platform"
  ]
 },
 "orig": {
  "active_json": "ef9d47b6b25c786e",
  "advisor_cli_dirs": [],
  "git_gate": {
   "ADV-APICALL-01.jsonl": "5e573ccce469355b",
   "ADV-APIGATE-01.jsonl": "ca8a52525d0a3d7e",
   "ADV-ENTRY-01.jsonl": "1d103cc2f6571f10",
   "ADV-GATE-02.jsonl": "02d5dc4dce38efd0",
   "ADV-GATE-03.jsonl": "6997029dc7693724",
   "ADV-GATE-04.jsonl": "c44fc652508d6463",
   "ADV-LAYER-01.jsonl": "9908a7654a4efab7",
   "ADV-OWNER-01.jsonl": "8818632534a30934",
   "ADV-R0ROUTE-01.jsonl": "da48ab85c9eb7906",
   "ADV-RESETISO-01.jsonl": "ab132664f6f940dd",
   "ADV-REVIEW-01.jsonl": "824389906a165b10",
   "INFRA-RULESLIM-01.jsonl": "1a65ec981a336316",
   "PJMAN-AUTOALIGN-01.jsonl": "115a9a456712dafe",
   "PJMAN-CLOSELOOP-01.jsonl": "751b6759135411f4",
   "PJMAN-OUTCOMES-01.jsonl": "a0454ca4f9ade3c4",
   "PJMAN-SKILL-01.jsonl": "98ffdac46f611dd8",
   "RGR-DERIV-01.jsonl": "f154de7bfccbfba5",
   "RGR-EQUIV-01.jsonl": "f3cc19a700e82999",
   "RGR-REPLAY-01.jsonl": "e56b04670712fe6f",
   "RGR-RERUN-01.jsonl": "c1b114fb8bef28dd",
   "TKA-BATCH-01.jsonl": "c3970455fcfad7b8",
   "TKA-CLOSE-01.jsonl": "6beed08d2eb7e006",
   "TKA-EVGATE-01.jsonl": "e26e1df44a9063e5",
   "TKA-TRUST-01.jsonl": "086813c639eacc7c",
   "TKA-WIRE-01.jsonl": "45d763d068bfea63"
  },
  "git_status": [
   " M .claude/rules/ssot-governance.md",
   " M .claude/scripts/route.py",
   " M .claude/scripts/route_table.json",
   " M .claude/scripts/tests/test_route.py"
  ],
  "settings_json": "061d178b73d911b0",
  "verify_cmd_conf": "ab2f52d88a4fa696"
 }
}
```
```json
{
 "case": {
  "git_gate": {
   "CASE-CLI-01.jsonl": "710201990fa8c53f"
  },
  "ledger_lines": 2,
  "tmp_files": [
   "tmp\\CASE-CLI-01\\advisor_cli\\advisor_cli_20261023T102737Z_b68340b6.json"
  ]
 },
 "known": {
  "claude_json": 101717,
  "claude_projects": [
   "C--Users-Administrator",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-183f1ec9-554f-4aa1-939c-98fef1fabb1c-scratchpad-stopprobe",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-d7f48063-6006-4d6e-a8e3-2f7fa366bc43-scratchpad-opuscase-cwd",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-d7f48063-6006-4d6e-a8e3-2f7fa366bc43-scratchpad-p1case",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-f553949a-6aa0-4325-ba8c-1cb99aa6df54-scratchpad-wire-low",
   "C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd",
   "D--DevSources-AndroidProj-SmartCameraApp1--tmp-ticket-author-20261013-t04-probe-run-20261013-144805",
   "D--DevSources-AndroidProj-SmartCameraApp1--tmp-ticket-author-20261013-t04-probe-run-adv-20261013-165641",
   "D--DevSources-AndroidProj-SmartCameraApp1--tmp-ticket-author-wire-probe0",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain1",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain4a",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain4b",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain5",
   "D--DevSources-claude-mods",
   "d--DevSources-AndroidProj-SmartCameraApp1",
   "d--DevSources-ObsidianVault",
   "d--DevSources-workflow-mcp-ticket-platform"
  ]
 },
 "orig": {
  "active_json": "ef9d47b6b25c786e",
  "advisor_cli_dirs": [],
  "git_gate": {
   "ADV-APICALL-01.jsonl": "5e573ccce469355b",
   "ADV-APIGATE-01.jsonl": "ca8a52525d0a3d7e",
   "ADV-ENTRY-01.jsonl": "1d103cc2f6571f10",
   "ADV-GATE-02.jsonl": "02d5dc4dce38efd0",
   "ADV-GATE-03.jsonl": "6997029dc7693724",
   "ADV-GATE-04.jsonl": "c44fc652508d6463",
   "ADV-LAYER-01.jsonl": "9908a7654a4efab7",
   "ADV-OWNER-01.jsonl": "8818632534a30934",
   "ADV-R0ROUTE-01.jsonl": "da48ab85c9eb7906",
   "ADV-RESETISO-01.jsonl": "ab132664f6f940dd",
   "ADV-REVIEW-01.jsonl": "824389906a165b10",
   "INFRA-RULESLIM-01.jsonl": "1a65ec981a336316",
   "PJMAN-AUTOALIGN-01.jsonl": "115a9a456712dafe",
   "PJMAN-CLOSELOOP-01.jsonl": "751b6759135411f4",
   "PJMAN-OUTCOMES-01.jsonl": "a0454ca4f9ade3c4",
   "PJMAN-SKILL-01.jsonl": "98ffdac46f611dd8",
   "RGR-DERIV-01.jsonl": "f154de7bfccbfba5",
   "RGR-EQUIV-01.jsonl": "f3cc19a700e82999",
   "RGR-REPLAY-01.jsonl": "e56b04670712fe6f",
   "RGR-RERUN-01.jsonl": "c1b114fb8bef28dd",
   "TKA-BATCH-01.jsonl": "c3970455fcfad7b8",
   "TKA-CLOSE-01.jsonl": "6beed08d2eb7e006",
   "TKA-EVGATE-01.jsonl": "e26e1df44a9063e5",
   "TKA-TRUST-01.jsonl": "086813c639eacc7c",
   "TKA-WIRE-01.jsonl": "45d763d068bfea63"
  },
  "git_status": [
   " M .claude/rules/ssot-governance.md",
   " M .claude/scripts/route.py",
   " M .claude/scripts/route_table.json",
   " M .claude/scripts/tests/test_route.py"
  ],
  "settings_json": "061d178b73d911b0",
  "verify_cmd_conf": "ab2f52d88a4fa696"
 }
}
```
```json
{
 "case": {
  "git_gate": {
   "CASE-CLI-01.jsonl": "710201990fa8c53f"
  },
  "ledger_lines": 2,
  "tmp_files": [
   "tmp\\CASE-CLI-01\\advisor_cli\\advisor_cli_20261023T102737Z_b68340b6.json",
   "tmp\\_advisor_cli_ticketless\\advisor_cli_20261023T102820Z_a6165187.json"
  ]
 },
 "known": {
  "claude_json": 101717,
  "claude_projects": [
   "C--Users-Administrator",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-183f1ec9-554f-4aa1-939c-98fef1fabb1c-scratchpad-stopprobe",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-d7f48063-6006-4d6e-a8e3-2f7fa366bc43-scratchpad-opuscase-cwd",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-d7f48063-6006-4d6e-a8e3-2f7fa366bc43-scratchpad-p1case",
   "C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-f553949a-6aa0-4325-ba8c-1cb99aa6df54-scratchpad-wire-low",
   "C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd",
   "D--DevSources-AndroidProj-SmartCameraApp1--tmp-ticket-author-20261013-t04-probe-run-20261013-144805",
   "D--DevSources-AndroidProj-SmartCameraApp1--tmp-ticket-author-20261013-t04-probe-run-adv-20261013-165641",
   "D--DevSources-AndroidProj-SmartCameraApp1--tmp-ticket-author-wire-probe0",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain1",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain4a",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain4b",
   "D--DevSources-AndroidProj-SmartCameraApp1-tmp-ADV-GATE-04-chain5",
   "D--DevSources-claude-mods",
   "d--DevSources-AndroidProj-SmartCameraApp1",
   "d--DevSources-ObsidianVault",
   "d--DevSources-workflow-mcp-ticket-platform"
  ]
 },
 "orig": {
  "active_json": "ef9d47b6b25c786e",
  "advisor_cli_dirs": [],
  "git_gate": {
   "ADV-APICALL-01.jsonl": "5e573ccce469355b",
   "ADV-APIGATE-01.jsonl": "ca8a52525d0a3d7e",
   "ADV-ENTRY-01.jsonl": "1d103cc2f6571f10",
   "ADV-GATE-02.jsonl": "02d5dc4dce38efd0",
   "ADV-GATE-03.jsonl": "6997029dc7693724",
   "ADV-GATE-04.jsonl": "c44fc652508d6463",
   "ADV-LAYER-01.jsonl": "9908a7654a4efab7",
   "ADV-OWNER-01.jsonl": "8818632534a30934",
   "ADV-R0ROUTE-01.jsonl": "da48ab85c9eb7906",
   "ADV-RESETISO-01.jsonl": "ab132664f6f940dd",
   "ADV-REVIEW-01.jsonl": "824389906a165b10",
   "INFRA-RULESLIM-01.jsonl": "1a65ec981a336316",
   "PJMAN-AUTOALIGN-01.jsonl": "115a9a456712dafe",
   "PJMAN-CLOSELOOP-01.jsonl": "751b6759135411f4",
   "PJMAN-OUTCOMES-01.jsonl": "a0454ca4f9ade3c4",
   "PJMAN-SKILL-01.jsonl": "98ffdac46f611dd8",
   "RGR-DERIV-01.jsonl": "f154de7bfccbfba5",
   "RGR-EQUIV-01.jsonl": "f3cc19a700e82999",
   "RGR-REPLAY-01.jsonl": "e56b04670712fe6f",
   "RGR-RERUN-01.jsonl": "c1b114fb8bef28dd",
   "TKA-BATCH-01.jsonl": "c3970455fcfad7b8",
   "TKA-CLOSE-01.jsonl": "6beed08d2eb7e006",
   "TKA-EVGATE-01.jsonl": "e26e1df44a9063e5",
   "TKA-TRUST-01.jsonl": "086813c639eacc7c",
   "TKA-WIRE-01.jsonl": "45d763d068bfea63"
  },
  "git_status": [
   " M .claude/rules/ssot-governance.md",
   " M .claude/scripts/route.py",
   " M .claude/scripts/route_table.json",
   " M .claude/scripts/tests/test_route.py"
  ],
  "settings_json": "061d178b73d911b0",
  "verify_cmd_conf": "ab2f52d88a4fa696"
 }
}
```
  比對結論（執行當時輸出）：原 repo（`git status`、`tmp/verify_cmd.conf`、`tmp/verify/active.json`、`.git/gate` 各檔雜湊、`~/.claude/settings.json`）兩次呼叫前後相同；案例 repo 有票呼叫 ledger 1→2 行（+1 consult）、無票呼叫 2→2；已知項 `~/.claude.json` 101688→101717 bytes、`~/.claude/projects` 新增空目錄 `C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd`。
- **回答被消費之修補 diff 原件（隔離案例 repo `git diff`，逐字）**：
```diff
diff --git a/retry_delay.py b/retry_delay.py
index db85500..a7dd007 100644
--- a/retry_delay.py
+++ b/retry_delay.py
@@ -2,7 +2,7 @@ import sys
 
 
 def delay(attempt, base=1.0):
-    return base * 2 ** attempt
+    return min(base * 2 ** attempt, 30.0)
 
 
 if __name__ == '__main__':
diff --git a/tags.py b/tags.py
index 894f14b..ab275c0 100644
--- a/tags.py
+++ b/tags.py
@@ -2,7 +2,7 @@ TAGS = ['v1.10.0', 'v1.2.0', 'v1.9.3', 'v1.2.10']
 
 
 def newest_first(tags):
-    return sorted(tags, reverse=True)
+    return sorted(tags, key=lambda t: tuple(map(int, t.removeprefix('v').split('.'))), reverse=True)
 
 
 if __name__ == '__main__':
```
  對應：有票回答「`return base * 2 ** attempt` 改 `min(base * 2 ** attempt, 30.0)`，並先確認 check.py 載入同一份 retry_delay.py」→ 本地驗證前提與附帶說法後 → `retry_delay.py` 修補（上列第一個 hunk）→ `check.py` 轉 `ok`；無票回答「整數 tuple key，先確認標籤皆為 `v`＋純數字段」→ `tags.py` 修補（上列第二個 hunk）→ 輸出 `['v1.10.0', 'v1.9.3', 'v1.2.10', 'v1.2.0']`。
- **Mechanism Bypass 揭露（沿用）**：受控缺陷由 executor 預先設計；本附錄證明「取得→消費→修驗→返回」經 Routing 走通，**不**證明顧問提供了 executor 未知之資訊。
- **producer 逐欄擷取**（`producer_excerpt.txt`，3516 bytes, sha256=177e1957ade5bf1abe4a014b78564903a34a0f25f01e9cae368f6bd74d092e94；內容已逐字收於 SV 報告〈Stage B〉）。

### AC16 範圍分離（本票提交 vs 他 session 提交）
- `git log --format='%h %an %s' bd8b532f..HEAD`：
```text
9a4fc213 Akira Chang [ADV-CLAUDECLI-01] fix(route): resolve the final cwd path (junction/symlink into repo refused), require init.tools == [], test token conflicts (TKA close core AC11/AC12/AC4)
5abbd211 Akira Chang [ADV-CLAUDECLI-01] fix(route): mark diagnostics_unavailable on the timeout path (AI-3 item 3), K11 asserts the normal case
72352908 Akira Chang [COMPACT-SYNC] PROGRESS checkpoint：RGR 四票 CLOSED 確認，更正「未逐一重驗」註記
8e4088b4 Akira Chang [ADV-CLAUDECLI-01] fix(route): reject non-object modelUsage values as unparseable; tolerate non-string hook_id (Codex R2 advisory)
22770829 Akira Chang [ADV-CLAUDECLI-01] docs(route): execution record is best-effort (non-fatal) in docstring and Advisor clause, matching AI-3 item 8
781dcd49 Akira Chang [ADV-CLAUDECLI-01] feat(route): advisor.claude-cli channel via Claude subscription CLI (ticketed + --no-ticket), strict success judgement, no fallback
```
- 逐提交檔案（`git show --stat`）：
- `781dcd49`：[ADV-CLAUDECLI-01] feat(route): advisor.claude-cli channel via Claude subscription CLI (ticketed + --no-ticket), strict success judgement, no fallback；； .claude/rules/ssot-governance.md  |  2 +-； .claude/scripts/route.py      | 382 +++++++++++++++++++++++++-； 
- `22770829`：[ADV-CLAUDECLI-01] docs(route): execution record is best-effort (non-fatal) in docstring and Advisor clause, matching AI-3 item 8；； .claude/rules/ssot-governance.md | 2 +-； .claude/scripts/route.py     | 6 ++++--； 2 files changed, 5 insertions(+), 3 deletions(
- `8e4088b4`：[ADV-CLAUDECLI-01] fix(route): reject non-object modelUsage values as unparseable; tolerate non-string hook_id (Codex R2 advisory)；； .claude/scripts/route.py      | 7 +++++--； .claude/scripts/tests/test_route.py | 17 +++++++++++++++++； 2 files changed, 22 inse
- `72352908`：[COMPACT-SYNC] PROGRESS checkpoint：RGR 四票 CLOSED 確認，更正「未逐一重驗」註記；； .claude/PROGRESS.md | 4 ++--； 1 file changed, 2 insertions(+), 2 deletions(-)
- `5abbd211`：[ADV-CLAUDECLI-01] fix(route): mark diagnostics_unavailable on the timeout path (AI-3 item 3), K11 asserts the normal case；； .claude/scripts/route.py      | 7 ++++++-； .claude/scripts/tests/test_route.py | 1 +； 2 files changed, 7 insertions(+), 1 deletion(-)
- `9a4fc213`：[ADV-CLAUDECLI-01] fix(route): resolve the final cwd path (junction/symlink into repo refused), require init.tools == [], test token conflicts (TKA close core AC11/AC12/AC4)；； .claude/rules/ssot-governance.md  | 2 +-； .claude/scripts/route.py      | 30 +++++++
- **分離結論**：本票實作提交（`781dcd49`／`22770829`／`8e4088b4`／`5abbd211`／`9a4fc213`）**只**觸及 4 個 ALLOWED 檔；範圍內唯一非 ALLOWED 檔為 `.claude/PROGRESS.md`，**僅**來自他 session 之 `72352908` `[COMPACT-SYNC]`（純 artifact 提交，不屬本票交付）。依 `ssot-governance.md` §Ticket Closure 紀律「Dashboard 慣例同步」（INFRA-GOVSYNC-01 定調：`[COMPACT-SYNC]` 獨立提交之 PROGRESS.md 例行更新不計入票面「git diff --stat 僅涉 ALLOWED」違規判定）。
- 本票實作範圍 `git diff --stat bd8b532f 9a4fc213 -- <4 個 ALLOWED 檔>`：
```text
 .claude/rules/ssot-governance.md    |   2 +-
 .claude/scripts/route.py            | 408 ++++++++++++++++++++++++-
 .claude/scripts/route_table.json    |   2 +
 .claude/scripts/tests/test_route.py | 586 +++++++++++++++++++++++++++++++++++-
 4 files changed, 991 insertions(+), 7 deletions(-)
```
- 含他 session 提交之全範圍 `git diff --name-only bd8b532f..9a4fc213`：
```text
.claude/PROGRESS.md
.claude/rules/ssot-governance.md
.claude/scripts/route.py
.claude/scripts/route_table.json
.claude/scripts/tests/test_route.py
```
- **EOL 原始輸出**（`git ls-files --eol`，四檔）：
```text
i/lf    w/lf    attr/text eol=lf      	.claude/rules/ssot-governance.md
i/lf    w/lf    attr/                 	.claude/scripts/route.py
i/lf    w/lf    attr/                 	.claude/scripts/route_table.json
i/lf    w/lf    attr/                 	.claude/scripts/tests/test_route.py
```

### Rev A.4 修復結果（2026-10-24）
- **提交**：`9a4fc213`（`route.py`／`test_route.py`／`ssot-governance.md` Advisor 條；`check commit` exit 0 後提交；DIFF_END=`9a4fc213`）。
- **AC11**：`prepare_cli_cwd` 對 `resolve()` 後最終路徑判 repo 內（建立前後各判），並以 resolve 後路徑啟動；K18b（junction 指向 repo 根與 repo 子目錄皆 `cwd_inside_repo`、零啟動；指向 repo 外之合法連結仍成功且 `.git` 於目標內初始化）。
- **AC12**：判定 ④b `init.tools` 須為空陣列（缺→`tools_unreported`／非 list→`unparseable:tools_invalid`／非空→`tools_enabled`）；K9b；真實 stream 之 `tools=[]`（證據附錄 `producer_excerpt`）。Advisor 條同步加「`init.tools` 為空陣列」。
- **AC4**：K7 增 `incident_token_ambiguous`／`evidence_token_ambiguous`／`evidence_token_missing` 三案。
- **AC15／AC16**：見上方證據附錄（原件逐字、sha256、transcript 對應、提交分離、EOL 輸出）。
- **回歸**（`python -I`）：`test_route.py` 87 OK（既有 65〔僅 S2／S3 具名同步〕＋新增 22）、`test_gate_ledger.py` 59 OK、`test_advisor_call.py` 25 OK、`gate_stop_guard_test.py` 54 OK。**突變**（暫改 `route.py`→跑對應測試→還原並逐位元組比對）：最終 cwd 未 resolve（舊行為）、tools guard 移除、`tools_unreported` 放行、incident／evidence 衝突檢查移除，共 5 項皆被殺。
- **Gate 與狀態**：INC-001 executor 層第 3 輪（本輪）進行中；原事件、`step_limit`、seq 13／16 與計數全數保留；lifecycle 經正式契約 REVIEW→IN_PROGRESS、票檔回 `Pending/`、`file` 指標已更新；不重啟已達 LIMIT 之舊 Cross-Review，改依既有 close 契約再審。

- **EXCLUDED_PAYLOAD 授權轉錄（Rev A.4，2026-10-24）**：`EXCLUDED_PAYLOAD=PROGRESS.md`（basename，避開 conf 含 `/` 之字面比對缺陷）僅排除他 session `72352908` `[COMPACT-SYNC]` 混入 `DIFF_REF..DIFF_END` 之 `.claude/PROGRESS.md`；授權依據＝`ssot-governance.md` §Ticket Closure 紀律「Dashboard 慣例同步」（INFRA-GOVSYNC-01 定調：`[COMPACT-SYNC]` 獨立提交之 PROGRESS.md 例行更新不計入票面 diff stat 違規判定）；該檔仍列於 SV 報告 Files Changed 與 Excluded Payload，不隱藏 payload；本票 PROGRESS.md 同步依 FORBIDDEN 延後。
- **Step 0 腳本限制揭露（Rev A.4）**：`verify_checklist.sh` 之 [0.2] 禁區檢查不採用 `EXCLUDED_PAYLOAD`（僅 [0.1] 採用），故他 session `72352908` 觸及本票 FORBIDDEN 之 `.claude/PROGRESS.md` 使該腳本判 `⛔ FORBIDDEN FILE`（`72352908` 進入範圍前之 Step 0 為 PASS）；腳本位於 `.claude/skills/**`（本票 FORBIDDEN，不得修改），與 `concurrency.md` 待開票清單第 5 項同型之「腳本行為 vs 文件文義」校準債；本票以 `close_helper.py preflight` 之 `scope`（只計實作提交、PASS）與上列提交分離證據作為範圍依據，**不宣稱 Step 0 腳本 PASS**。

### 補件 2（Rev A.4 再審 supplement round 1；2026-10-24）——僅補證據，不改程式

#### AC12 現版 producer 逐欄表（取代前次收尾實錄中標「未讀取」且用修改前行號之舊表；舊表已作廢）
| 欄位 | route.py 讀取處（HEAD 9a4fc213） | (a) 成功實測（`[已執行-真實]`） | (b) 歷史失敗輸出（`[歷史-不重現]`） | (c) stub（`[stub]`，僅 stub 層） |
| --- | --- | --- | --- | --- |
| `system/init.apiKeySource` | `route.py:700／:702` | 兩次真實呼叫 `"none"`（L5） | probe_emptycfg L1／probe_unreach L5 `"none"` | `test_route.py` K9／K4／K3 |
| `system/init.model` | `route.py:733` | 兩次真實呼叫 `claude-opus-5-5`（L5） | probe_emptycfg L1／probe_unreach L5 | `test_route.py` K10 |
| `system/init.tools` | `route.py:704／:708` | 兩次真實呼叫 `tools=[]`（L5） | probe_emptycfg L1／probe_unreach L5 皆 `tools=[]` | `test_route.py` K9b（缺→tools_unreported／非 list／非空→tools_enabled）；K3 成功案 tools=[] |
| `system/hook_started|hook_response` | `route.py:660` | 兩次真實呼叫各 2 個 SessionStart hook（L1–L4，exit 0） | probe_unreach L1–L4 | `test_route.py` K15／K15b |
| `system/api_retry` | `route.py:668` | （成功路徑不出現） | probe_unreach L6–L13（attempt 1..8，max_retries 10） | `test_route.py` K11／K15 |
| `assistant.error` | `route.py:672` | （成功路徑不出現） | probe_emptycfg L2 `authentication_failed` | `test_route.py` K8 |
| `result.is_error` | `route.py:712` | 兩次真實呼叫 `false`（L13） | probe_emptycfg L3 `true` | `test_route.py` K8 |
| `result.terminal_reason` | `route.py:717` | 兩次真實呼叫 `completed`（L13） | probe_emptycfg L3 `api_error` | `test_route.py` K8（僅入 detail） |
| `result.modelUsage` | `route.py:725` | 兩次真實呼叫僅 `claude-opus-5-5`（L13） | probe_emptycfg L3 `{}` | `test_route.py` K10／K8 |
| `result.result` | `route.py:737` | 兩次真實呼叫非空字串（L13） | probe_emptycfg L3 未登入文字 | `test_route.py` K13 |

(a)(b) 之行號 L 為各原件內 stream 行號（見證據附錄原件與 `producer_excerpt`）；(c) 僅證明入口解析與判定，**不是** production producer 證據。

#### AC2 AI-3 第 7 項欄位對照
| AI-3 第 7 項欄位 | 真實有票 envelope（`real1_out.json`）是否含 | K3 是否斷言 |
| --- | --- | --- |
| `mode` | 是（值見原件） | 是 |
| `channel` | 是（值見原件） | 是 |
| `model` | 是（值見原件） | 是 |
| `models_used` | 是（值見原件） | 是 |
| `effort_flag` | 是（值見原件） | 是 |
| `api_key_source` | 是（值見原件） | 是 |
| `session_id` | 是（值見原件） | 是 |
| `usage` | 是（值見原件） | 否（存在性見 AC2 舉證；K3 另斷言整體 envelope 結構） |
| `cost_reported_usd` | 是（值見原件） | 是 |
| `duration_ms` | 是（值見原件） | 否（存在性見 AC2 舉證；K3 另斷言整體 envelope 結構） |
| `cli_internal_retries` | 是（值見原件） | 是 |
| `hooks` | 是（值見原件） | 是 |
| `warnings` | 是（值見原件） | 是 |
| `answer` | 是（值見原件） | 是 |
| `record` | 是（值見原件） | 是 |
| 逾時路徑專屬 `reaped`, `diagnostic`, `diagnostics_unavailable`／失敗路徑 `record_error` | 成功路徑不適用（`record_error` 僅寫入失敗時出現） | K11／K12／K17 |

真實有票 envelope 之全部鍵：`ok, verb, via, mode, channel, requested_model, models_used, effort_flag, api_key_source, session_id, usage, cost_reported_usd, cost_note, duration_ms, cli_internal_retries, hooks, warnings, record, model, answer, disposition, ledger_seq, detail, source`。AI-3 第 7 項列舉欄位於成功路徑皆存在（`usage`、`duration_ms` 於真實 envelope 原件可見）。

#### AC15 transcript 實際 tool_use 命令與輸出（觀察者原文；session `d7f48063-…`）
#### tool_use …nQgpYUkN｜10:27:21 UTC｜Bash
命令（原文，路徑前綴縮寫為 `$S`＝session scratchpad）：
```bash
S="$S"; cd "$S/clicase" && python -I "D:/DevSources/AndroidProj/SmartCameraApp1/.claude/scripts/route.py" --ticket CASE-CLI-01 consult --source advisor.claude-cli --incident INC-001 --payload "$S/case_payload.md" --evidence "$S/case_evidence.txt" > "$S/real1_out.json" 2> "$S/real1_err.txt"; echo "exit=$?"; wc -c "$S/real1_out.json" "$S/real1_err.txt"
```
輸出（原文，截至 2200 字元）：
```text
exit=0
2327 C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/real1_out.json
   0 C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/real1_err.txt
2327 total
Shell cwd was reset to D:\DevSources\AndroidProj\SmartCameraApp1
```

#### tool_use …PExfp9EJ｜10:27:42 UTC｜Bash
命令（原文，路徑前綴縮寫為 `$S`＝session scratchpad）：
```bash
S="$S"; python -X utf8 - "$S" <<'EOF'
import json, sys, pathlib
S = pathlib.Path(sys.argv[1])
o = json.loads((S/"real1_out.json").read_text(encoding="utf-8"))
ans = o.pop("answer")
print(json.dumps(o, ensure_ascii=False, indent=1)[:2500])
print("---- ANSWER ----")
print(ans)
EOF
echo "---- ledger consult note"; tail -n 2 "$S/clicase/.git/gate/CASE-CLI-01.jsonl" | python -X utf8 -c "import sys,json; [print(json.dumps(json.loads(l),ensure_ascii=False)) for l in sys.stdin]"
```
輸出（原文，截至 2200 字元）：
```text
{

 "ok": true,

 "verb": "consult",

 "via": "route.py",

 "mode": "ticketed",

 "channel": "claude-subscription-cli",

 "requested_model": "claude-opus-5-5",

 "models_used": {

  "claude-opus-5-5": {

   "inputTokens": 2,

   "outputTokens": 603,

   "cacheReadInputTokens": 1559,

   "cacheCreationInputTokens": 4352

  }

 },

 "effort_flag": "high",

 "api_key_source": "none",

 "session_id": "439f77ac-6a6a-4232-b247-aa16f1af44dd",

 "usage": {

  "input_tokens": 2,

  "cache_creation_input_tokens": 4352,

  "cache_read_input_tokens": 1559,

  "output_tokens": 603,

  "output_tokens_details": {

   "thinking_tokens": 294

  },

  "server_tool_use": {

   "web_search_requests": 0,

   "web_fetch_requests": 0

  },

  "service_tier": "standard",

  "cache_creation": {

   "ephemeral_1h_input_tokens": 4352,

   "ephemeral_5m_input_tokens": 0

  },

  "inference_geo": "global",

  "iterations": [

   {

    "input_tokens": 2,

    "output_tokens": 603,

    "cache_read_input_tokens": 1559,

    "cache_creation_input_tokens": 4352,

    "cache_creation": {

     "ephemeral_5m_input_tokens": 0,

     "ephemeral_1h_input_tokens": 4352

    },

    "type": "message"

   }

  ],

  "speed": "standard",

  "fallback_credit": null

 },

 "cost_reported_usd": 0.047195799999999996,

 "cost_note": "CLI list-price figure, not billing",

 "duration_ms": 14774,

 "cli_internal_retries": 0,

 "hooks": [

  {

   "event": "SessionStart",

   "name": "SessionStart:startup",

   "outcome": "success",

   "exit_code": 0

  },

  {

   "event": "SessionStart",

   "name": "SessionStart:startup",

   "outcome": "success",

   "exit_code": 0

  }

 ],

 "warnings": [],

 "record": "tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json",

 "model": "claude-opus-5-5",

 "disposition": "RECORDED",

 "ledger_seq": 2,

 "detail": "evidence=failure",

 "source": "advisor.claude-cli"

}

---- ANSWER ----

1. **改哪行**：`retry_delay.py` 裡 `delay()` 的 `return base * 2 ** attempt`。

2. **改成**：`return min(base * 2 ** attempt, 30.0)`。這樣 delay(0)=1.0、delay(3)=8.0 不變，delay(10) 被截到 30.0，回傳值仍是 float。

3. **套用前先驗證**：確認 `check.py` 載入的就是你要改的那份 `retry_delay.py`。traceback 的路徑在 `...\scratch
```

#### tool_use …9Ga3DuEX｜10:27:50 UTC｜Bash
命令（原文，路徑前綴縮寫為 `$S`＝session scratchpad）：
```bash
cd /c/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase || exit 1; echo "== premise 1: which retry_delay.py does check.py import?"; python -c "import sys; sys.path.insert(0, '.'); import retry_delay; print(retry_delay.__file__)"; python - <<'EOF'
import runpy, sys
sys.argv=["check.py"]
import importlib.util, os
# emulate `python check.py`: sys.path[0] is the script dir
src = open("check.py", encoding="utf-8").read()
sys.path.insert(0, os.getcwd())
import retry_delay
print("check.py would import:", os.path.abspath(retry_delay.__file__))
EOF
echo "== premise 2 (advisor's side claim): huge attempt overflows before the cap?"; python -c "
from retry_delay import delay
try:
    print(delay(1100))
except Exception as e:
    print(type(e).__name__, e)"
echo "== apply the minimal fix (only retry_delay.py)"; python - <<'EOF'
import pathlib
p = pathlib.Path("retry_delay.py"); t = p.read_text(encoding="utf-8")
old = "return base * 2 ** attempt"; assert t.count(old) == 1
p.write_text(t.replace(old, "return min(base * 2 ** attempt, 30.0)"), encoding="utf-8")
EOF
git diff --stat; echo "== test"; python check.py; echo "== next necessary step of the original flow: run the module CLI"; python retry_delay.py 10; python retry_delay.py 0
```
輸出（原文，截至 2200 字元）：
```text
== premise 1: which retry_delay.py does check.py import?
C:\Users\Administrator\AppData\Local\Temp\claude\D--DevSources-AndroidProj-SmartCameraApp1\d7f48063-6006-4d6e-a8e3-2f7fa366bc43\scratchpad\clicase\retry_delay.py

check.py would import: C:\Users\Administrator\AppData\Local\Temp\claude\D--DevSources-AndroidProj-SmartCameraApp1\d7f48063-6006-4d6e-a8e3-2f7fa366bc43\scratchpad\clicase\retry_delay.py

== premise 2 (advisor's side claim): huge attempt overflows before the cap?
OverflowError int too large to convert to float

== apply the minimal fix (only retry_delay.py)
 retry_delay.py | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
== test
ok

== next necessary step of the original flow: run the module CLI
30.0

1.0
Shell cwd was reset to D:\DevSources\AndroidProj\SmartCameraApp1
```

#### tool_use …fPzkAkp2｜10:27:59 UTC｜Bash
命令（原文，路徑前綴縮寫為 `$S`＝session scratchpad）：
```bash
S="$S"; python -X utf8 -I "$S/snap.py" "$S/snap_after1.json" "$S/clicase" >/dev/null; python -X utf8 - "$S" <<'EOF'
import json, sys, pathlib
S = pathlib.Path(sys.argv[1])
b = json.loads((S/"snap_before1.json").read_text(encoding="utf-8")); a = json.loads((S/"snap_after1.json").read_text(encoding="utf-8"))
print("ORIG repo identical:", a["orig"] == b["orig"])
for k in b["orig"]:
    if a["orig"][k] != b["orig"][k]: print("  DIFF orig", k)
print("case ledger lines:", b["case"]["ledger_lines"], "->", a["case"]["ledger_lines"])
print("case tmp new:", sorted(set(a["case"]["tmp_files"]) - set(b["case"]["tmp_files"])))
print("known: claude.json size", b["known"]["claude_json"], "->", a["known"]["claude_json"], "| projects same:", a["known"]["claude_projects"] == b["known"]["claude_projects"],
      "| new project dirs:", sorted(set(a["known"]["claude_projects"]) - set(b["known"]["claude_projects"])))
EOF
grep -c '"type": "consult"' "$S/clicase/.git/gate/CASE-CLI-01.jsonl"; python -X utf8 -c "
import json,glob
d=json.load(open(glob.glob('$S/clicase/tmp/CASE-CLI-01/advisor_cli/*.json')[0],encoding='utf-8'))
print('record verdict:',d['verdict'],'| keys:',sorted(d)[:8])"
```
輸出（原文，截至 2200 字元）：
```text
ORIG repo identical: True

case ledger lines: 1 -> 2

case tmp new: ['tmp\\CASE-CLI-01\\advisor_cli\\advisor_cli_20261023T102737Z_b68340b6.json']

known: claude.json size 101688 -> 101717 | projects same: False | new project dirs: ['C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd']

1
record verdict: SUCCESS | keys: ['answer', 'envelope', 'note', 'payload_sha256', 'returncode', 'source', 'stderr', 'stream']
```

#### tool_use …acF4MM9C｜10:28:08 UTC｜Bash
命令（原文，路徑前綴縮寫為 `$S`＝session scratchpad）：
```bash
S="$S"; cd "$S/clicase" && python -I "D:/DevSources/AndroidProj/SmartCameraApp1/.claude/scripts/route.py" consult --no-ticket --source advisor.claude-cli --payload "$S/case_payload_tl.md" > "$S/real2_out.json" 2> "$S/real2_err.txt"; echo "exit=$?"; wc -c "$S/real2_out.json" "$S/real2_err.txt" | head -2; python -X utf8 - "$S" <<'EOF'
import json, sys, pathlib
S = pathlib.Path(sys.argv[1])
o = json.loads((S/"real2_out.json").read_text(encoding="utf-8"))
ans = o.pop("answer", None)
for k in ("ok","disposition","mode","channel","requested_model","model","effort_flag","api_key_source","session_id","cost_reported_usd","duration_ms","cli_internal_retries","warnings","record","detail","models_used"):
    print(k, "=", json.dumps(o.get(k), ensure_ascii=False))
print("---- ANSWER ----"); print(ans)
EOF
```
輸出（原文，截至 2200 字元）：
```text
exit=0
2341 C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/real2_out.json
   0 C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/real2_err.txt
ok = true

disposition = "ANSWERED"

mode = "ticketless"

channel = "claude-subscription-cli"

requested_model = "claude-opus-5-5"

model = "claude-opus-5-5"

effort_flag = "high"

api_key_source = "none"

session_id = "43f06819-833a-417f-984d-d393412b3836"

cost_reported_usd = 0.0310898

duration_ms = 12021

cli_internal_retries = 0

warnings = []

record = "tmp/_advisor_cli_ticketless/advisor_cli_20261023T102820Z_a6165187.json"

detail = "ticketless: no ledger event was written"

models_used = {"claude-opus-5-5": {"inputTokens": 2, "outputTokens": 555, "cacheReadInputTokens": 3189, "cacheCreationInputTokens": 2418}}

---- ANSWER ----

1. **最小修法**：把排序的 key 從字串改成整數 tuple，例如 `sorted(tags, key=lambda t: tuple(map(int, t.removeprefix('v').split('.'))), reverse=True)`。這樣 `10 > 9` 會按數值比較，而不是按字元比較。

2. **套用前要確認**：所有標籤都是 `v` 加上純數字段，例如 `vX.Y.Z`，而且段數都一樣。

3. 如果有 `v1.2.0-rc1` 這種預發布後綴，`int()` 會拋出 `ValueError`。如果有 `v1.2` 和 `v1.2.0` 混在一起，兩者的先後順序會出乎預期，需要先補齊段數或另外處理。

4. `str.removeprefix` 要 Python 3.9 以上；版本較舊就改用 `t[1:]`，但前提是確定每個標籤都以 `v` 開頭。
Shell cwd was reset to D:\DevSources\AndroidProj\SmartCameraApp1
```

#### tool_use …Rd7PirWK｜10:29:05 UTC｜Bash
命令（原文，路徑前綴縮寫為 `$S`＝session scratchpad）：
```bash
S="$S"; cd "$S/clicase" || exit 1; echo "== premise: every tag is 'v' + all-digit segments, same segment count?"; python -c "
from tags import TAGS
import re
print(all(re.fullmatch(r'v\d+\.\d+\.\d+', t) for t in TAGS), TAGS)"; python --version; echo "== side claim: pre-release suffix breaks int()"; python -c "
try: tuple(map(int,'v1.2.0-rc1'.removeprefix('v').split('.')))
except Exception as e: print(type(e).__name__)"; echo "== apply (only newest_first)"; python - <<'EOF'
import pathlib
p = pathlib.Path("tags.py"); t = p.read_text(encoding="utf-8")
old = "return sorted(tags, reverse=True)"; assert t.count(old) == 1
p.write_text(t.replace(old, "return sorted(tags, key=lambda t: tuple(map(int, t.removeprefix('v').split('.'))), reverse=True)"), encoding="utf-8")
EOF
echo "== result"; python tags.py; python -c "
from tags import newest_first, TAGS
assert newest_first(TAGS) == ['v1.10.0', 'v1.9.3', 'v1.2.10', 'v1.2.0']; print('matches expected')"
cd /d/DevSources/AndroidProj/SmartCameraApp1; python -X utf8 -I "$S/snap.py" "$S/snap_after2.json" "$S/clicase" >/dev/null; python -X utf8 - "$S" <<'EOF'
import json, sys, pathlib
S = pathlib.Path(sys.argv[1])
b = json.loads((S/"snap_after1.json").read_text(encoding="utf-8")); a = json.loads((S/"snap_after2.json").read_text(encoding="utf-8"))
b0 = json.loads((S/"snap_before1.json").read_text(encoding="utf-8"))
print("ORIG repo identical across both calls:", a["orig"] == b0["orig"])
print("case ledger unchanged by the ticketless call:", a["case"]["git_gate"] == b["case"]["git_gate"], a["case"]["ledger_lines"], "(was", b["case"]["ledger_lines"], ")")
print("case tmp new (ticketless call):", sorted(set(a["case"]["tmp_files"]) - set(b["case"]["tmp_files"])))
print("known claude.json size:", b0["known"]["claude_json"], "->", a["known"]["claude_json"])
EOF
grep -c '"type": "consult"' "$S/clicase/.git/gate/CASE-CLI-01.jsonl"
```
輸出（原文，截至 2200 字元）：
```text
== premise: every tag is 'v' + all-digit segments, same segment count?
True ['v1.10.0', 'v1.2.0', 'v1.9.3', 'v1.2.10']

Python 3.14.5

== side claim: pre-release suffix breaks int()
ValueError

== apply (only newest_first)
== result
['v1.10.0', 'v1.9.3', 'v1.2.10', 'v1.2.0']

matches expected

ORIG repo identical across both calls: True

case ledger unchanged by the ticketless call: True 2 (was 2 )

case tmp new (ticketless call): ['tmp\\_advisor_cli_ticketless\\advisor_cli_20261023T102820Z_a6165187.json']

known claude.json size: 101688 -> 101717

1
```

### 補件 3（Rev A.4 再審 supplement round 2；2026-10-24）——AC12 producer 原件可核對對應（僅補證據，不改程式）

#### 原件現況（逐檔：完整路徑、大小、mtime、sha256；執行端於 2026-10-24 以 `Path.stat()`／`hashlib` 實測）
- a1 `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json`｜19102 bytes｜mtime 2026-10-23T18:27:37｜sha256=4cebc11a897226ba56713e3eef11e2897b92a3ff24558d1d62e97e6d52bf4a47
- a2 `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/_advisor_cli_ticketless/advisor_cli_20261023T102820Z_a6165187.json`｜18702 bytes｜mtime 2026-10-23T18:28:20｜sha256=b1019eb65b7b7b6dd2d3bcea56474bd385b2934aa0875a1cce5f665cdd79d190
- b1 `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_emptycfg.jsonl`｜6691 bytes｜mtime 2026-10-23T15:45:00｜sha256=95490eb3fac9dd9a2fe04fc202f3d2bb5075c196a209726b9febcd05edd47c16
- b2 `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl`｜8768 bytes｜mtime 2026-10-23T15:44:30｜sha256=8313033535c8b35999f5b7f8853b012f85ba3606c87d84ea02f1e06086d59ab4
- a3 `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/real1_out.json`｜2327 bytes｜mtime 2026-10-23T18:27:37｜sha256=f3a760384d54aa6d24175725a6f9bd8a19bfb67231254b49bf79ef2396a91555
- a4 `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/real2_out.json`｜2341 bytes｜mtime 2026-10-23T18:28:20｜sha256=fd2a9b26a2b680a5b826d9905800707f8d0ac99d2d9e8751096836ff9dbd59d6

#### 逐欄 producer 對應（原件路徑＋stream 行號＋該行雜湊＋解析欄位值；行內容由 `json.loads` 取自上列原件，非重造）
| 原件 | 完整路徑（session scratchpad，非耐久） | stream 行 | 該行 sha256（前 16） | 欄位 | 該行解析出之欄位值 |
| --- | --- | --- | --- | --- | --- |
| a1 真實有票呼叫記錄 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json`（JSON 鍵 `stream`） | L3 | `d32ec6b766df82d5` | system/hook_response | `{"hook_event": "SessionStart", "outcome": "success", "exit_code": 0}` |
| a1 真實有票呼叫記錄 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json`（JSON 鍵 `stream`） | L4 | `fb9fef0e164f7a84` | system/hook_response | `{"hook_event": "SessionStart", "outcome": "success", "exit_code": 0}` |
| a1 真實有票呼叫記錄 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json`（JSON 鍵 `stream`） | L5 | `f38bfe763e64ffef` | init.apiKeySource／init.model／init.tools | `{"apiKeySource": "none", "model": "claude-opus-5-5", "tools": []}` |
| a1 真實有票呼叫記錄 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json`（JSON 鍵 `stream`） | L13 | `4987b7b75bc310ef` | result.is_error／terminal_reason／modelUsage／result | `{"is_error": false, "terminal_reason": "completed", "modelUsage_keys": ["claude-opus-5-5"], "result_head": "1. **改哪行**：`retry_de"}` |
| a2 真實無票呼叫記錄 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/_advisor_cli_ticketless/advisor_cli_20261023T102820Z_a6165187.json`（JSON 鍵 `stream`） | L3 | `74b7aa901c1dd8f7` | system/hook_response | `{"hook_event": "SessionStart", "outcome": "success", "exit_code": 0}` |
| a2 真實無票呼叫記錄 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/_advisor_cli_ticketless/advisor_cli_20261023T102820Z_a6165187.json`（JSON 鍵 `stream`） | L4 | `7b0f651fbf51b542` | system/hook_response | `{"hook_event": "SessionStart", "outcome": "success", "exit_code": 0}` |
| a2 真實無票呼叫記錄 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/_advisor_cli_ticketless/advisor_cli_20261023T102820Z_a6165187.json`（JSON 鍵 `stream`） | L5 | `4630d5ec9f6a27b5` | init.apiKeySource／init.model／init.tools | `{"apiKeySource": "none", "model": "claude-opus-5-5", "tools": []}` |
| a2 真實無票呼叫記錄 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/_advisor_cli_ticketless/advisor_cli_20261023T102820Z_a6165187.json`（JSON 鍵 `stream`） | L13 | `cddd4ab78e608642` | result.is_error／terminal_reason／modelUsage／result | `{"is_error": false, "terminal_reason": "completed", "modelUsage_keys": ["claude-opus-5-5"], "result_head": "1. **最小修法**：把排序的 key"}` |
| b1 歷史未登入原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_emptycfg.jsonl` | L1 | `2ccae4b24588cd6e` | init.apiKeySource／init.model／init.tools | `{"apiKeySource": "none", "model": "claude-opus-5-5", "tools": []}` |
| b1 歷史未登入原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_emptycfg.jsonl` | L2 | `6d5db249cca4b9ba` | assistant.error | `{"error": "authentication_failed"}` |
| b1 歷史未登入原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_emptycfg.jsonl` | L3 | `63fadb4b3d984c78` | result.is_error／terminal_reason／modelUsage／result | `{"is_error": true, "terminal_reason": "api_error", "modelUsage_keys": [], "result_head": "Not logged in · Plea"}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L3 | `9054f0607dbb9a9b` | system/hook_response | `{"hook_event": "SessionStart", "outcome": "success", "exit_code": 0}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L4 | `ccf3d253d217a226` | system/hook_response | `{"hook_event": "SessionStart", "outcome": "success", "exit_code": 0}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L5 | `91a298192bbcccf8` | init.apiKeySource／init.model／init.tools | `{"apiKeySource": "none", "model": "claude-opus-5-5", "tools": []}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L6 | `373957d3a2a42738` | system/api_retry | `{"attempt": 1, "max_retries": 10, "error": "unknown"}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L7 | `bc0f0578f86ad473` | system/api_retry | `{"attempt": 2, "max_retries": 10, "error": "unknown"}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L8 | `4fcabd5d6ac3c40a` | system/api_retry | `{"attempt": 3, "max_retries": 10, "error": "unknown"}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L9 | `137fe14e184ab7a7` | system/api_retry | `{"attempt": 4, "max_retries": 10, "error": "unknown"}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L10 | `5d39a69865c8a337` | system/api_retry | `{"attempt": 5, "max_retries": 10, "error": "unknown"}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L11 | `cebba265346f83bf` | system/api_retry | `{"attempt": 6, "max_retries": 10, "error": "unknown"}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L12 | `741d6e9d6a3a885e` | system/api_retry | `{"attempt": 7, "max_retries": 10, "error": "unknown"}` |
| b2 歷史不可達原始輸出 | `C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl` | L13 | `7c6e73081d52af76` | system/api_retry | `{"attempt": 8, "max_retries": 10, "error": "unknown"}` |

說明：(a)＝本票兩次真實成功呼叫（2026-10-23）之入口執行記錄內 `stream` 欄逐行；(b)＝開票前保存之歷史失敗原始輸出，**本票未重做該呼叫**；兩者皆為 session scratchpad 檔，非耐久，本表與原件雜湊為其耐久摘錄。`init.tools` 於 (a) 兩份與 (b) 兩份之 init 事件皆為 `[]`。

## AC12 原始事件嵌入（Rev A.4 補件 4；2026-10-24，Akira 裁決之限 AC12 證據可讀性具名續審用；不改程式、不新增模型呼叫）

> 前次（收尾實錄中標「`init.tools` 未讀取」、使用修改前行號之）producer 舊表與補件 2 之摘要表，自本節起**以本節為準**；舊表作廢但保留原文作歷史。

### 證明範圍三分（不得互相升格）
1. **歷史真實輸出**（a1、a2；`[已執行-真實]`，2026-10-23）：證明 `claude -p --output-format stream-json --verbose`（2.1.304）在 production 實際產生下列 guard 輸入欄位與成功形狀之值。producer 是 CLI，非 route.py；呼叫當時之 route.py 為修復前 `781dcd49`，其**判定結果**不代表現版判定（現版 `9a4fc213` 另加 `init.tools` 空陣列 guard 與最終 cwd 解析），但欄位本身的 producer 事實不受 route.py 版本影響。
2. **歷史失敗輸出**（b1、b2；`[歷史-不重現]`，開票前 bounded probe）：證明失敗形狀之欄位（assistant `error`、`result.is_error=true`、`terminal_reason=api_error`、空 `modelUsage`、`system/api_retry`）由真實 CLI 產生；本票未重做。
3. **最新程式／測試**（`[已執行]` 於 `9a4fc213`；stub 為 `[stub]`）：現版 route.py 讀取處＝`init.apiKeySource` :700／:702、`init.tools` :704／:708、`init.model` :733、`hook_*` :660、`api_retry` :668、assistant `error` :672、`result.is_error` :712、`terminal_reason` :717、`modelUsage` :725、`result.result` :737；`test_route.py` 87 OK（K3／K4／K8／K9／K9b／K10／K11／K13／K15）。stub 只證入口對欄位的解析與判定，**不是** producer 證據。

### 原始事件（逐字）
**a1｜真實有票呼叫（2026-10-23，route.py@781dcd49 啟動之 `claude -p` 2.1.304 輸出）**
原件：`C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/CASE-CLI-01/advisor_cli/advisor_cli_20261023T102737Z_b68340b6.json`（JSON 鍵 `stream`），整檔 sha256=`4cebc11a897226ba56713e3eef11e2897b92a3ff24558d1d62e97e6d52bf4a47`；以下為該原件中與 AC12 guard 輸入相關之事件行，**逐字**（僅以 `L<n>:` 標行號；`sk-ant-` 遮蔽規則未命中任何內容）：
```json
L3: {"type":"system","subtype":"hook_response","hook_id":"1ce7e29f-cc72-41e0-9405-9ac64eb516b2","hook_name":"SessionStart:startup","hook_event":"SessionStart","output":"","stdout":"","stderr":"","exit_code":0,"outcome":"success","uuid":"139bad2c-22c0-4561-890a-49d63f1a7717","session_id":"439f77ac-6a6a-4232-b247-aa16f1af44dd"}
L4: {"type":"system","subtype":"hook_response","hook_id":"5e7bb03b-415e-4b34-bee2-1b7a83c9a30c","hook_name":"SessionStart:startup","hook_event":"SessionStart","output":"","stdout":"","stderr":"","exit_code":0,"outcome":"success","uuid":"5bbb9a26-e9c0-41e1-9ead-7dcc69cf7e67","session_id":"439f77ac-6a6a-4232-b247-aa16f1af44dd"}
L5: {"type":"system","subtype":"init","cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\cueshot_advisor_cli_cwd","session_id":"439f77ac-6a6a-4232-b247-aa16f1af44dd","tools":[],"mcp_servers":[],"model":"claude-opus-5-5","permissionMode":"auto","slash_commands":[],"apiKeySource":"none","claude_code_version":"2.1.304","output_style":"default","agents":["claude","claude-code-guide","Explore","general-purpose","Plan","statusline-setup","understand-anything:architecture-analyzer","understand-anything:article-analyzer","understand-anything:assemble-reviewer","understand-anything:domain-analyzer","understand-anything:file-analyzer","understand-anything:graph-reviewer","understand-anything:knowledge-graph-guide","understand-anything:project-scanner","understand-anything:tour-builder"],"skills":[],"plugins":[{"name":"context-bar","path":"D:\\DevSources\\claude-mods\\context-bar","source":"context-bar@inline","version":"0.1.0"},{"name":"understand-anything","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\understand-anything\\understand-anything\\2.8.1","source":"understand-anything@understand-anything","version":"2.8.1"},{"name":"frontend-design","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\claude-plugins-official\\frontend-design\\465f09344424","source":"frontend-design@claude-plugins-official"},{"name":"cowork-plugin-management","path":"C:\\Users\\Administrator\\.claude\\plugins\\synced\\dcd7b68f-604c-422c-bf55-e2fb149f77bb_42153965-a23d-4904-83d9-b6fa38cdaff3\\cowork-plugin-management~g2","source":"cowork-plugin-management@synced","version":"0.2.2"},{"name":"cc-plugin-agents-md","path":"builtin","source":"cc-plugin-agents-md@builtin"},{"name":"cc-plugin-telemetry","path":"builtin","source":"cc-plugin-telemetry@builtin"},{"name":"cc-plugin-plugin-authoring","path":"builtin","source":"cc-plugin-plugin-authoring@builtin"},{"name":"cc-plugin-you-should-know","path":"builtin","source":"cc-plugin-you-should-know@builtin"}],"capabilities":["interrupt_receipt_v1","interrupt_cancel_queued_v1","interrupt_send_now_v1","msg_lifecycle_v1","request_marker_lists_v1","sdk_mcp_tools_list_changed","sdk_mcp_manifests","mcp_read_resource_v1","mcp_tool_ui_meta_v1","ui_surface_v1","prompts_screened_v1","mcp_call_result_not_for_model_v1"],"analytics_disabled":false,"product_feedback_disabled":false,"uuid":"e1172cf6-f816-4c30-bbb0-4ac8e65b0bb7","memory_paths":{"auto":"C:\\Users\\Administrator\\.claude\\projects\\C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd\\memory\\"},"scratchpad_path":"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\claude\\C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd\\439f77ac-6a6a-4232-b247-aa16f1af44dd\\scratchpad","original_cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\cueshot_advisor_cli_cwd","additional_directories":["C:\\Users\\Administrator\\.claude\\channels\\telegram","D:\\DevSources\\AndroidProj\\SmartCameraApp1\\.claude\\skills\\ticket-rigor-gate\\runner"],"messaging_socket_path":"\\\\.\\pipe\\LOCAL\\cc-msg-c44955eeda2713fbc735c17fccf61110","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","per_turn_effort_active":true,"view_mode":"default","powershell_path":"C:\\Users\\Administrator\\AppData\\Local\\Microsoft\\WindowsApps\\pwsh.exe","startup_timing":{"entrypoint":"claude-desktop","warm_spare_claimed":false,"plugin_full_loads":0,"user_context_builds":2,"sdk_mcp_dial_runs":0,"sdk_mcp_servers_dialed":0,"flag_fetch_kick":"not_eligible","flag_fetch_kick_judged_at_ms":275,"hooks_init_source":"startup","phases":{"node_boot_ms":138,"settings_load_ms":0,"pre_action_mdm_keychain_ms":8,"pre_action_init_ms":123,"pre_action_client_data_url_ms":0,"pre_action_migrations_ms":0,"pre_action_managed_settings_ms":7,"pre_action_ms":182,"permission_context_ms":344,"setup_import_ms":1,"setup_uds_dir_ms":0,"setup_uds_listen_ms":1,"setup_uds_publish_ms":50,"setup_uds_messaging_ms":69,"setup_hooks_snapshot_ms":0,"setup_file_watcher_ms":0,"setup_release_notes_ms":12,"setup_ms":76,"skills_load_ms":345,"tools_commands_load_ms":196,"model_catalog_decision_ms":71,"mcp_connect_ms":4,"print_import_ms":50,"headless_prologue_ms":464,"hooks_init_ms":854,"initial_messages_load_ms":297,"headless_post_load_ms":10,"input_ready_ms":2081,"first_message_read_ms":2097,"sdk_mcp_update_ms":3,"first_message_handle_ms":3,"first_message_handle_cpu_ms":0,"first_message_handle_loop_lag_ms":0,"mcp_prewait_ms":0,"qe_system_prompt_ms":25,"prompt_submit_hooks_ms":310,"qe_plugin_skills_load_ms":0,"init_emit_ms":1},"time_origin_ms":1792751242436.4846,"phase_start_ms":{"node_boot_ms":0,"settings_load_ms":139,"pre_action_mdm_keychain_ms":144,"pre_action_init_ms":151,"pre_action_client_data_url_ms":274,"pre_action_migrations_ms":278,"pre_action_managed_settings_ms":278,"pre_action_ms":144,"permission_context_ms":338,"setup_import_ms":711,"setup_uds_dir_ms":735,"setup_uds_listen_ms":735,"setup_uds_publish_ms":736,"setup_uds_messaging_ms":722,"setup_hooks_snapshot_ms":795,"setup_file_watcher_ms":795,"setup_release_notes_ms":797,"setup_ms":733,"skills_load_ms":727,"tools_commands_load_ms":897,"model_catalog_decision_ms":1095,"mcp_connect_ms":1219,"print_import_ms":1224,"headless_prologue_ms":1278,"hooks_init_ms":1185,"initial_messages_load_ms":1743,"headless_post_load_ms":2040,"input_ready_ms":0,"first_message_read_ms":0,"sdk_mcp_update_ms":2097,"first_message_handle_ms":2097,"mcp_prewait_ms":2201,"qe_system_prompt_ms":2213,"prompt_submit_hooks_ms":2246,"qe_plugin_skills_load_ms":2558,"init_emit_ms":2562}}}
L13: {"duration_api_ms":8166,"stop_reason":"end_turn","session_id":"439f77ac-6a6a-4232-b247-aa16f1af44dd","total_cost_usd":0.047195799999999996,"usage":{"input_tokens":2,"cache_creation_input_tokens":4352,"cache_read_input_tokens":1559,"output_tokens":603,"output_tokens_details":{"thinking_tokens":294},"server_tool_use":{"web_search_requests":0,"web_fetch_requests":0},"service_tier":"standard","cache_creation":{"ephemeral_1h_input_tokens":4352,"ephemeral_5m_input_tokens":0},"inference_geo":"global","iterations":[{"input_tokens":2,"output_tokens":603,"cache_read_input_tokens":1559,"cache_creation_input_tokens":4352,"cache_creation":{"ephemeral_5m_input_tokens":0,"ephemeral_1h_input_tokens":4352},"type":"message"}],"speed":"standard","fallback_credit":null},"modelUsage":{"claude-opus-5-5":{"inputTokens":2,"outputTokens":603,"cacheReadInputTokens":1559,"cacheCreationInputTokens":4352,"webSearchRequests":0,"costUSD":0.047195799999999996,"contextWindow":1000000,"maxOutputTokens":128000,"thinkingTokens":294,"canonicalModel":"claude-opus-5-5","provider":"firstParty","costBasis":"list"}},"permission_denials":[],"terminal_reason":"completed","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","subagent_stats":{"spawned":0,"requested":{"background":0,"foreground":0,"unset":0},"started_in_background":0,"max_depth":0,"spawned_by_subagents":0,"completed":0,"failed":0,"killed":{"parent":0,"user":0,"system":0},"refused":{"depth_limit":0,"concurrency_limit":0,"budget":0},"by_type":{}},"safety_stops":0,"is_error":false,"num_turns":1,"subtype":"success","api_error_status":null,"result":"1. **改哪行**：`retry_delay.py` 裡 `delay()` 的 `return base * 2 ** attempt`。\n2. **改成**：`return min(base * 2 ** attempt, 30.0)`。這樣 delay(0)=1.0、delay(3)=8.0 不變，delay(10) 被截到 30.0，回傳值仍是 float。\n3. **套用前先驗證**：確認 `check.py` 載入的就是你要改的那份 `retry_delay.py`。traceback 的路徑在 `...\\scratchpad\\clicase\\`，要排除 sys.path 上有別的同名副本，否則改了也不會生效。\n4. 附帶一提：如果 attempt 可能非常大（約 >1023），`base * 2 ** attempt` 在比較前就會拋 OverflowError。check.py 不測這種情況，但需要時可以先把 attempt 夾住。","ttft_ms":5752,"type":"result","duration_ms":11938,"uuid":"62bf5bc8-9d8f-42c1-845b-c36dd94288d2","ttft_stream_ms":2409,"time_to_request_ms":535,"time_to_request_phases_ms":{"other":30,"system_prompt":25,"resume_other":0,"process_user_input":4,"input_attachments":5,"input_hooks":310,"tool_pool_refresh":1,"init_message":5,"engine_pickup":2,"message_prep":1,"model_config_wait":1,"autocompact":2,"query_setup":46,"auto_mode_context_wait":89,"model_gates":1,"tool_schema_build":1,"message_normalization":1,"client_creation":5,"request_build":6},"turn_start_resume_kind":"none","process_turn_index":0,"time_to_request_loop_lag_ms":70,"time_to_request_cpu_ms":281,"time_to_request_major_faults":5818,"input_attachments_detail":{"slowest_producer":"queued_commands","slowest_producer_ms":3,"cpu_ms":0,"major_faults":275},"first_content_frame_ms":2410,"queued_turn_count":0,"result_index":0}
```

**a2｜真實無票呼叫（同上）**
原件：`C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/clicase/tmp/_advisor_cli_ticketless/advisor_cli_20261023T102820Z_a6165187.json`（JSON 鍵 `stream`），整檔 sha256=`b1019eb65b7b7b6dd2d3bcea56474bd385b2934aa0875a1cce5f665cdd79d190`；以下為該原件中與 AC12 guard 輸入相關之事件行，**逐字**（僅以 `L<n>:` 標行號；`sk-ant-` 遮蔽規則未命中任何內容）：
```json
L3: {"type":"system","subtype":"hook_response","hook_id":"2d3a4f2d-0b86-4595-8718-51209c82a20f","hook_name":"SessionStart:startup","hook_event":"SessionStart","output":"","stdout":"","stderr":"","exit_code":0,"outcome":"success","uuid":"f8343264-367a-4c70-b4ae-3e2ededda1e7","session_id":"43f06819-833a-417f-984d-d393412b3836"}
L4: {"type":"system","subtype":"hook_response","hook_id":"b9918389-49b4-4fd4-a97a-cd15e2276c2d","hook_name":"SessionStart:startup","hook_event":"SessionStart","output":"","stdout":"","stderr":"","exit_code":0,"outcome":"success","uuid":"4341afb4-e5c9-4604-a52c-12c46be21781","session_id":"43f06819-833a-417f-984d-d393412b3836"}
L5: {"type":"system","subtype":"init","cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\cueshot_advisor_cli_cwd","session_id":"43f06819-833a-417f-984d-d393412b3836","tools":[],"mcp_servers":[],"model":"claude-opus-5-5","permissionMode":"auto","slash_commands":[],"apiKeySource":"none","claude_code_version":"2.1.304","output_style":"default","agents":["claude","claude-code-guide","Explore","general-purpose","Plan","statusline-setup","understand-anything:architecture-analyzer","understand-anything:article-analyzer","understand-anything:assemble-reviewer","understand-anything:domain-analyzer","understand-anything:file-analyzer","understand-anything:graph-reviewer","understand-anything:knowledge-graph-guide","understand-anything:project-scanner","understand-anything:tour-builder"],"skills":[],"plugins":[{"name":"context-bar","path":"D:\\DevSources\\claude-mods\\context-bar","source":"context-bar@inline","version":"0.1.0"},{"name":"understand-anything","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\understand-anything\\understand-anything\\2.8.1","source":"understand-anything@understand-anything","version":"2.8.1"},{"name":"frontend-design","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\claude-plugins-official\\frontend-design\\465f09344424","source":"frontend-design@claude-plugins-official"},{"name":"cowork-plugin-management","path":"C:\\Users\\Administrator\\.claude\\plugins\\synced\\dcd7b68f-604c-422c-bf55-e2fb149f77bb_42153965-a23d-4904-83d9-b6fa38cdaff3\\cowork-plugin-management~g2","source":"cowork-plugin-management@synced","version":"0.2.2"},{"name":"cc-plugin-agents-md","path":"builtin","source":"cc-plugin-agents-md@builtin"},{"name":"cc-plugin-telemetry","path":"builtin","source":"cc-plugin-telemetry@builtin"},{"name":"cc-plugin-plugin-authoring","path":"builtin","source":"cc-plugin-plugin-authoring@builtin"},{"name":"cc-plugin-you-should-know","path":"builtin","source":"cc-plugin-you-should-know@builtin"}],"capabilities":["interrupt_receipt_v1","interrupt_cancel_queued_v1","interrupt_send_now_v1","msg_lifecycle_v1","request_marker_lists_v1","sdk_mcp_tools_list_changed","sdk_mcp_manifests","mcp_read_resource_v1","mcp_tool_ui_meta_v1","ui_surface_v1","prompts_screened_v1","mcp_call_result_not_for_model_v1"],"analytics_disabled":false,"product_feedback_disabled":false,"uuid":"c5d07b9a-e51a-49c3-92b7-9cb1438ec047","memory_paths":{"auto":"C:\\Users\\Administrator\\.claude\\projects\\C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd\\memory\\"},"scratchpad_path":"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\claude\\C--Users-Administrator-AppData-Local-Temp-cueshot-advisor-cli-cwd\\43f06819-833a-417f-984d-d393412b3836\\scratchpad","original_cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\cueshot_advisor_cli_cwd","additional_directories":["C:\\Users\\Administrator\\.claude\\channels\\telegram","D:\\DevSources\\AndroidProj\\SmartCameraApp1\\.claude\\skills\\ticket-rigor-gate\\runner"],"messaging_socket_path":"\\\\.\\pipe\\LOCAL\\cc-msg-7a0f25beed8dd9dd7a2bb5cbcbe029ae","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","per_turn_effort_active":true,"view_mode":"default","powershell_path":"C:\\Users\\Administrator\\AppData\\Local\\Microsoft\\WindowsApps\\pwsh.exe","startup_timing":{"entrypoint":"claude-desktop","warm_spare_claimed":false,"plugin_full_loads":0,"user_context_builds":2,"sdk_mcp_dial_runs":0,"sdk_mcp_servers_dialed":0,"flag_fetch_kick":"not_eligible","flag_fetch_kick_judged_at_ms":181,"hooks_init_source":"startup","phases":{"node_boot_ms":126,"settings_load_ms":0,"pre_action_mdm_keychain_ms":2,"pre_action_init_ms":50,"pre_action_client_data_url_ms":0,"pre_action_migrations_ms":0,"pre_action_managed_settings_ms":3,"pre_action_ms":63,"permission_context_ms":215,"setup_import_ms":0,"setup_uds_dir_ms":0,"setup_uds_listen_ms":1,"setup_uds_publish_ms":12,"setup_uds_messaging_ms":19,"setup_hooks_snapshot_ms":0,"setup_file_watcher_ms":0,"setup_release_notes_ms":3,"setup_ms":21,"skills_load_ms":143,"tools_commands_load_ms":91,"model_catalog_decision_ms":8,"mcp_connect_ms":3,"print_import_ms":28,"headless_prologue_ms":451,"hooks_init_ms":730,"initial_messages_load_ms":226,"headless_post_load_ms":2,"input_ready_ms":1366,"first_message_read_ms":1373,"sdk_mcp_update_ms":0,"first_message_handle_ms":1,"first_message_handle_cpu_ms":0,"first_message_handle_loop_lag_ms":0,"mcp_prewait_ms":0,"qe_system_prompt_ms":29,"prompt_submit_hooks_ms":339,"qe_plugin_skills_load_ms":0,"init_emit_ms":1},"time_origin_ms":1792751288989.9956,"phase_start_ms":{"node_boot_ms":0,"settings_load_ms":126,"pre_action_mdm_keychain_ms":129,"pre_action_init_ms":130,"pre_action_client_data_url_ms":180,"pre_action_migrations_ms":182,"pre_action_managed_settings_ms":182,"pre_action_ms":129,"permission_context_ms":194,"setup_import_ms":442,"setup_uds_dir_ms":449,"setup_uds_listen_ms":449,"setup_uds_publish_ms":450,"setup_uds_messaging_ms":444,"setup_hooks_snapshot_ms":465,"setup_file_watcher_ms":465,"setup_release_notes_ms":466,"setup_ms":449,"skills_load_ms":448,"tools_commands_load_ms":513,"model_catalog_decision_ms":605,"mcp_connect_ms":640,"print_import_ms":645,"headless_prologue_ms":677,"hooks_init_ms":624,"initial_messages_load_ms":1128,"headless_post_load_ms":1354,"input_ready_ms":0,"first_message_read_ms":0,"sdk_mcp_update_ms":1373,"first_message_handle_ms":1373,"mcp_prewait_ms":1438,"qe_system_prompt_ms":1443,"prompt_submit_hooks_ms":1480,"qe_plugin_skills_load_ms":1821,"init_emit_ms":1825}}}
L13: {"duration_api_ms":6321,"stop_reason":"end_turn","session_id":"43f06819-833a-417f-984d-d393412b3836","total_cost_usd":0.0310898,"usage":{"input_tokens":2,"cache_creation_input_tokens":2418,"cache_read_input_tokens":3189,"output_tokens":555,"output_tokens_details":{"thinking_tokens":226},"server_tool_use":{"web_search_requests":0,"web_fetch_requests":0},"service_tier":"standard","cache_creation":{"ephemeral_1h_input_tokens":2418,"ephemeral_5m_input_tokens":0},"inference_geo":"global","iterations":[{"input_tokens":2,"output_tokens":555,"cache_read_input_tokens":3189,"cache_creation_input_tokens":2418,"cache_creation":{"ephemeral_5m_input_tokens":0,"ephemeral_1h_input_tokens":2418},"type":"message"}],"speed":"standard","fallback_credit":null},"modelUsage":{"claude-opus-5-5":{"inputTokens":2,"outputTokens":555,"cacheReadInputTokens":3189,"cacheCreationInputTokens":2418,"webSearchRequests":0,"costUSD":0.0310898,"contextWindow":1000000,"maxOutputTokens":128000,"thinkingTokens":226,"canonicalModel":"claude-opus-5-5","provider":"firstParty","costBasis":"list"}},"permission_denials":[],"terminal_reason":"completed","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","subagent_stats":{"spawned":0,"requested":{"background":0,"foreground":0,"unset":0},"started_in_background":0,"max_depth":0,"spawned_by_subagents":0,"completed":0,"failed":0,"killed":{"parent":0,"user":0,"system":0},"refused":{"depth_limit":0,"concurrency_limit":0,"budget":0},"by_type":{}},"safety_stops":0,"is_error":false,"num_turns":1,"subtype":"success","api_error_status":null,"result":"1. **最小修法**：把排序的 key 從字串改成整數 tuple，例如 `sorted(tags, key=lambda t: tuple(map(int, t.removeprefix('v').split('.'))), reverse=True)`。這樣 `10 > 9` 會按數值比較，而不是按字元比較。\n2. **套用前要確認**：所有標籤都是 `v` 加上純數字段，例如 `vX.Y.Z`，而且段數都一樣。\n3. 如果有 `v1.2.0-rc1` 這種預發布後綴，`int()` 會拋出 `ValueError`。如果有 `v1.2` 和 `v1.2.0` 混在一起，兩者的先後順序會出乎預期，需要先補齊段數或另外處理。\n4. `str.removeprefix` 要 Python 3.9 以上；版本較舊就改用 `t[1:]`，但前提是確定每個標籤都以 `v` 開頭。","ttft_ms":3757,"type":"result","duration_ms":9990,"uuid":"782563a1-2f8d-4e7f-8214-b4a397ee19f3","ttft_stream_ms":1791,"time_to_request_ms":549,"time_to_request_phases_ms":{"other":15,"system_prompt":29,"resume_other":0,"process_user_input":2,"input_attachments":6,"input_hooks":339,"tool_pool_refresh":1,"init_message":5,"engine_pickup":1,"message_prep":1,"model_config_wait":1,"autocompact":2,"query_setup":46,"auto_mode_context_wait":90,"model_gates":0,"tool_schema_build":1,"message_normalization":2,"client_creation":2,"request_build":6},"turn_start_resume_kind":"none","process_turn_index":0,"time_to_request_loop_lag_ms":30,"time_to_request_cpu_ms":390,"time_to_request_major_faults":4916,"input_attachments_detail":{"slowest_producer":"queued_commands","slowest_producer_ms":3,"cpu_ms":0,"major_faults":189},"first_content_frame_ms":1791,"queued_turn_count":0,"result_index":0}
```

**b1｜歷史未登入原始輸出（開票前 2026-10-23 bounded probe，非本票重做）**
原件：`C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_emptycfg.jsonl`，整檔 sha256=`95490eb3fac9dd9a2fe04fc202f3d2bb5075c196a209726b9febcd05edd47c16`；以下為該原件中與 AC12 guard 輸入相關之事件行，**逐字**（僅以 `L<n>:` 標行號；`sk-ant-` 遮蔽規則未命中任何內容）：
```json
L1: {"type":"system","subtype":"init","cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\claude\\D--DevSources-AndroidProj-SmartCameraApp1\\d7f48063-6006-4d6e-a8e3-2f7fa366bc43\\scratchpad\\opuscase\\cwd","session_id":"9c2d8b3f-9b8c-4e64-8620-4f36b6f0dea6","tools":[],"mcp_servers":[],"model":"claude-opus-5-5","permissionMode":"default","slash_commands":[],"apiKeySource":"none","claude_code_version":"2.1.304","output_style":"default","agents":["claude","claude-code-guide","Explore","general-purpose","Plan","statusline-setup"],"skills":[],"plugins":[{"name":"context-bar","path":"D:\\DevSources\\claude-mods\\context-bar","source":"context-bar@inline","version":"0.1.0"},{"name":"cc-plugin-agents-md","path":"builtin","source":"cc-plugin-agents-md@builtin"},{"name":"cc-plugin-telemetry","path":"builtin","source":"cc-plugin-telemetry@builtin"},{"name":"cc-plugin-plugin-authoring","path":"builtin","source":"cc-plugin-plugin-authoring@builtin"}],"capabilities":["interrupt_receipt_v1","interrupt_cancel_queued_v1","interrupt_send_now_v1","msg_lifecycle_v1","request_marker_lists_v1","sdk_mcp_tools_list_changed","sdk_mcp_manifests","mcp_read_resource_v1","mcp_tool_ui_meta_v1","ui_surface_v1","prompts_screened_v1","mcp_call_result_not_for_model_v1"],"analytics_disabled":false,"product_feedback_disabled":false,"uuid":"ef636e4b-3712-4ccb-a499-96381d31bcfb","memory_paths":{"auto":"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\claude\\D--DevSources-AndroidProj-SmartCameraApp1\\d7f48063-6006-4d6e-a8e3-2f7fa366bc43\\scratchpad\\opuscase\\emptycfg\\projects\\C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-d7f48063-6006-4d6e-a8e3-2f7fa366bc43-scratchpad-opuscase-cwd\\memory\\"},"original_cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\claude\\D--DevSources-AndroidProj-SmartCameraApp1\\d7f48063-6006-4d6e-a8e3-2f7fa366bc43\\scratchpad\\opuscase\\cwd","additional_directories":[],"messaging_socket_path":"\\\\.\\pipe\\LOCAL\\cc-msg-e0a98190680a0081ed6e1f041c55e773","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","per_turn_effort_active":true,"view_mode":"default","powershell_path":"C:\\Users\\Administrator\\AppData\\Local\\Microsoft\\WindowsApps\\pwsh.exe","startup_timing":{"entrypoint":"claude-desktop","warm_spare_claimed":false,"plugin_full_loads":0,"user_context_builds":2,"sdk_mcp_dial_runs":0,"sdk_mcp_servers_dialed":0,"flag_fetch_kick":"started_after_init_empty","flag_fetch_kick_judged_at_ms":168,"hooks_init_source":"startup","phases":{"node_boot_ms":123,"settings_load_ms":0,"pre_action_mdm_keychain_ms":2,"pre_action_init_ms":41,"pre_action_client_data_url_ms":0,"pre_action_migrations_ms":119,"pre_action_managed_settings_ms":2,"pre_action_ms":174,"growthbook_init_ms":0,"permission_context_ms":140,"setup_import_ms":0,"setup_uds_dir_ms":0,"setup_uds_listen_ms":1,"setup_uds_publish_ms":21,"setup_uds_messaging_ms":30,"setup_hooks_snapshot_ms":0,"setup_file_watcher_ms":0,"skills_load_ms":38,"setup_release_notes_ms":12,"setup_ms":40,"tools_commands_load_ms":15,"model_catalog_decision_ms":0,"mcp_connect_ms":2,"print_import_ms":27,"headless_prologue_ms":2,"hooks_init_ms":414,"initial_messages_load_ms":334,"headless_post_load_ms":4,"input_ready_ms":1027,"first_message_read_ms":1031,"sdk_mcp_update_ms":0,"first_message_handle_ms":0,"first_message_handle_cpu_ms":0,"first_message_handle_loop_lag_ms":0,"mcp_prewait_ms":0,"qe_system_prompt_ms":4,"prompt_submit_hooks_ms":3,"qe_plugin_skills_load_ms":0,"init_emit_ms":1},"time_origin_ms":1792741499482.868,"phase_start_ms":{"node_boot_ms":0,"settings_load_ms":123,"pre_action_mdm_keychain_ms":126,"pre_action_init_ms":127,"pre_action_client_data_url_ms":168,"pre_action_migrations_ms":171,"pre_action_managed_settings_ms":290,"pre_action_ms":126,"growthbook_init_ms":301,"permission_context_ms":304,"setup_import_ms":457,"setup_uds_dir_ms":468,"setup_uds_listen_ms":468,"setup_uds_publish_ms":468,"setup_uds_messaging_ms":461,"setup_hooks_snapshot_ms":494,"setup_file_watcher_ms":494,"skills_load_ms":466,"setup_release_notes_ms":495,"setup_ms":467,"tools_commands_load_ms":564,"model_catalog_decision_ms":579,"mcp_connect_ms":623,"print_import_ms":640,"headless_prologue_ms":668,"hooks_init_ms":590,"initial_messages_load_ms":670,"headless_post_load_ms":1004,"input_ready_ms":0,"first_message_read_ms":0,"sdk_mcp_update_ms":1031,"first_message_handle_ms":1031,"mcp_prewait_ms":1094,"qe_system_prompt_ms":1100,"prompt_submit_hooks_ms":1111,"qe_plugin_skills_load_ms":1116,"init_emit_ms":1120}}}
L2: {"type":"assistant","message":{"diagnostics":null,"id":"8b6a380b-da7d-4a79-9cd7-ea65f1b61641","container":null,"model":"<synthetic>","role":"assistant","stop_details":null,"stop_reason":"stop_sequence","stop_sequence":"","type":"message","usage":{"output_tokens_details":null,"input_tokens":0,"output_tokens":0,"cache_creation_input_tokens":0,"cache_read_input_tokens":0,"server_tool_use":{"web_search_requests":0,"web_fetch_requests":0},"service_tier":null,"cache_creation":{"ephemeral_1h_input_tokens":0,"ephemeral_5m_input_tokens":0},"inference_geo":null,"iterations":null,"speed":null,"fallback_credit":null},"content":[{"type":"text","text":"Not logged in · Please run /login"}],"context_management":null},"parent_tool_use_id":null,"session_id":"9c2d8b3f-9b8c-4e64-8620-4f36b6f0dea6","uuid":"00d42a23-95ce-456f-beb2-b6f863824a7c","timestamp":"2026-10-23T07:45:00.629Z","error":"authentication_failed","is_api_error_message":true}
L3: {"duration_api_ms":0,"stop_reason":"stop_sequence","session_id":"9c2d8b3f-9b8c-4e64-8620-4f36b6f0dea6","total_cost_usd":0,"usage":{"output_tokens_details":{"thinking_tokens":0},"input_tokens":0,"cache_creation_input_tokens":0,"cache_read_input_tokens":0,"output_tokens":0,"server_tool_use":{"web_search_requests":0,"web_fetch_requests":0},"service_tier":"standard","cache_creation":{"ephemeral_1h_input_tokens":0,"ephemeral_5m_input_tokens":0},"inference_geo":"","iterations":[],"speed":"standard","fallback_credit":null},"modelUsage":{},"permission_denials":[],"terminal_reason":"api_error","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","subagent_stats":{"spawned":0,"requested":{"background":0,"foreground":0,"unset":0},"started_in_background":0,"max_depth":0,"spawned_by_subagents":0,"completed":0,"failed":0,"killed":{"parent":0,"user":0,"system":0},"refused":{"depth_limit":0,"concurrency_limit":0,"budget":0},"by_type":{}},"safety_stops":0,"is_error":true,"num_turns":1,"subtype":"success","api_error_status":null,"result":"Not logged in · Please run /login","type":"result","duration_ms":54,"uuid":"2015fd45-1b03-4898-86ce-7132d9fbd923","queued_turn_count":0,"result_index":0}
```

**b2｜歷史 API 不可達原始輸出（同上）**
原件：`C:/Users/ADMINI~1/AppData/Local/Temp/claude/D--DevSources-AndroidProj-SmartCameraApp1/d7f48063-6006-4d6e-a8e3-2f7fa366bc43/scratchpad/opuscase/probe_unreach.jsonl`，整檔 sha256=`8313033535c8b35999f5b7f8853b012f85ba3606c87d84ea02f1e06086d59ab4`；以下為該原件中與 AC12 guard 輸入相關之事件行，**逐字**（僅以 `L<n>:` 標行號；`sk-ant-` 遮蔽規則未命中任何內容）：
```json
L3: {"type":"system","subtype":"hook_response","hook_id":"df0ab282-7134-484d-8f7a-fee19451707c","hook_name":"SessionStart:startup","hook_event":"SessionStart","output":"","stdout":"","stderr":"","exit_code":0,"outcome":"success","uuid":"63e72a7e-c6cb-49ee-8cea-14d978280e3d","session_id":"463964d2-6379-40ea-bef0-abfb3433557f"}
L4: {"type":"system","subtype":"hook_response","hook_id":"5974de5e-3e97-4712-b968-fe9aaded7b24","hook_name":"SessionStart:startup","hook_event":"SessionStart","output":"","stdout":"","stderr":"","exit_code":0,"outcome":"success","uuid":"97100f96-faeb-4736-84a3-1c8eab66189b","session_id":"463964d2-6379-40ea-bef0-abfb3433557f"}
L5: {"type":"system","subtype":"init","cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\claude\\D--DevSources-AndroidProj-SmartCameraApp1\\d7f48063-6006-4d6e-a8e3-2f7fa366bc43\\scratchpad\\opuscase\\cwd","session_id":"463964d2-6379-40ea-bef0-abfb3433557f","tools":[],"mcp_servers":[],"model":"claude-opus-5-5","permissionMode":"auto","slash_commands":[],"apiKeySource":"none","claude_code_version":"2.1.304","output_style":"default","agents":["claude","claude-code-guide","Explore","general-purpose","Plan","statusline-setup","understand-anything:architecture-analyzer","understand-anything:article-analyzer","understand-anything:assemble-reviewer","understand-anything:domain-analyzer","understand-anything:file-analyzer","understand-anything:graph-reviewer","understand-anything:knowledge-graph-guide","understand-anything:project-scanner","understand-anything:tour-builder"],"skills":[],"plugins":[{"name":"context-bar","path":"D:\\DevSources\\claude-mods\\context-bar","source":"context-bar@inline","version":"0.1.0"},{"name":"understand-anything","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\understand-anything\\understand-anything\\2.8.1","source":"understand-anything@understand-anything","version":"2.8.1"},{"name":"frontend-design","path":"C:\\Users\\Administrator\\.claude\\plugins\\cache\\claude-plugins-official\\frontend-design\\465f09344424","source":"frontend-design@claude-plugins-official"},{"name":"cowork-plugin-management","path":"C:\\Users\\Administrator\\.claude\\plugins\\synced\\dcd7b68f-604c-422c-bf55-e2fb149f77bb_42153965-a23d-4904-83d9-b6fa38cdaff3\\cowork-plugin-management~g2","source":"cowork-plugin-management@synced","version":"0.2.2"},{"name":"cc-plugin-agents-md","path":"builtin","source":"cc-plugin-agents-md@builtin"},{"name":"cc-plugin-telemetry","path":"builtin","source":"cc-plugin-telemetry@builtin"},{"name":"cc-plugin-plugin-authoring","path":"builtin","source":"cc-plugin-plugin-authoring@builtin"}],"capabilities":["interrupt_receipt_v1","interrupt_cancel_queued_v1","interrupt_send_now_v1","msg_lifecycle_v1","request_marker_lists_v1","sdk_mcp_tools_list_changed","sdk_mcp_manifests","mcp_read_resource_v1","mcp_tool_ui_meta_v1","ui_surface_v1","prompts_screened_v1","mcp_call_result_not_for_model_v1"],"analytics_disabled":false,"product_feedback_disabled":false,"uuid":"00714021-9ab4-4266-a697-2eaeb031ab34","memory_paths":{"auto":"C:\\Users\\Administrator\\.claude\\projects\\C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-d7f48063-6006-4d6e-a8e3-2f7fa366bc43-scratchpad-opuscase-cwd\\memory\\"},"scratchpad_path":"C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\claude\\C--Users-Administrator-AppData-Local-Temp-claude-D--DevSources-AndroidProj-SmartCameraApp1-d7f48063-6006-4d6e-a8e3-2f7fa366bc43-scratchpad-opuscase-cwd\\463964d2-6379-40ea-bef0-abfb3433557f\\scratchpad","original_cwd":"C:\\Users\\Administrator\\AppData\\Local\\Temp\\claude\\D--DevSources-AndroidProj-SmartCameraApp1\\d7f48063-6006-4d6e-a8e3-2f7fa366bc43\\scratchpad\\opuscase\\cwd","additional_directories":["C:\\Users\\Administrator\\.claude\\channels\\telegram","D:\\DevSources\\AndroidProj\\SmartCameraApp1\\.claude\\skills\\ticket-rigor-gate\\runner"],"messaging_socket_path":"\\\\.\\pipe\\LOCAL\\cc-msg-cfecc917a32174fbf9023b9e5e301b05","fast_mode_state":"off","fast_mode_disabled_reason":"sdk_opt_in_required","per_turn_effort_active":true,"view_mode":"default","powershell_path":"C:\\Users\\Administrator\\AppData\\Local\\Microsoft\\WindowsApps\\pwsh.exe","startup_timing":{"entrypoint":"claude-desktop","warm_spare_claimed":false,"plugin_full_loads":0,"user_context_builds":2,"sdk_mcp_dial_runs":0,"sdk_mcp_servers_dialed":0,"flag_fetch_kick":"not_eligible","flag_fetch_kick_judged_at_ms":300,"hooks_init_source":"startup","phases":{"node_boot_ms":136,"settings_load_ms":0,"pre_action_mdm_keychain_ms":10,"pre_action_init_ms":147,"pre_action_client_data_url_ms":0,"pre_action_migrations_ms":0,"pre_action_managed_settings_ms":11,"pre_action_ms":230,"permission_context_ms":319,"setup_import_ms":1,"setup_uds_dir_ms":0,"setup_uds_listen_ms":1,"setup_uds_publish_ms":19,"setup_uds_messaging_ms":35,"setup_hooks_snapshot_ms":0,"setup_file_watcher_ms":0,"setup_release_notes_ms":6,"setup_ms":36,"skills_load_ms":222,"tools_commands_load_ms":140,"model_catalog_decision_ms":1,"mcp_connect_ms":3,"print_import_ms":57,"headless_prologue_ms":459,"hooks_init_ms":987,"initial_messages_load_ms":437,"headless_post_load_ms":7,"input_ready_ms":2048,"first_message_read_ms":2059,"sdk_mcp_update_ms":2,"first_message_handle_ms":2,"first_message_handle_cpu_ms":0,"first_message_handle_loop_lag_ms":0,"mcp_prewait_ms":0,"qe_system_prompt_ms":64,"prompt_submit_hooks_ms":178,"qe_plugin_skills_load_ms":0,"init_emit_ms":1},"time_origin_ms":1792741399660.0054,"phase_start_ms":{"node_boot_ms":0,"settings_load_ms":137,"pre_action_mdm_keychain_ms":142,"pre_action_init_ms":152,"pre_action_client_data_url_ms":299,"pre_action_migrations_ms":303,"pre_action_managed_settings_ms":304,"pre_action_ms":142,"permission_context_ms":386,"setup_import_ms":756,"setup_uds_dir_ms":778,"setup_uds_listen_ms":778,"setup_uds_publish_ms":779,"setup_uds_messaging_ms":765,"setup_hooks_snapshot_ms":803,"setup_file_watcher_ms":803,"setup_release_notes_ms":805,"setup_ms":776,"skills_load_ms":769,"tools_commands_load_ms":870,"model_catalog_decision_ms":1013,"mcp_connect_ms":1045,"print_import_ms":1062,"headless_prologue_ms":1121,"hooks_init_ms":1031,"initial_messages_load_ms":1580,"headless_post_load_ms":2017,"input_ready_ms":0,"first_message_read_ms":0,"sdk_mcp_update_ms":2059,"first_message_handle_ms":2059,"mcp_prewait_ms":2178,"qe_system_prompt_ms":2193,"prompt_submit_hooks_ms":2266,"qe_plugin_skills_load_ms":2447,"init_emit_ms":2451}}}
L6: {"type":"system","subtype":"api_retry","attempt":1,"max_retries":10,"retry_delay_ms":582,"error_status":null,"error":"unknown","session_id":"463964d2-6379-40ea-bef0-abfb3433557f","uuid":"d911ee4c-3511-4221-a369-3ac6e5a959b4"}
L7: {"type":"system","subtype":"api_retry","attempt":2,"max_retries":10,"retry_delay_ms":1106,"error_status":null,"error":"unknown","session_id":"463964d2-6379-40ea-bef0-abfb3433557f","uuid":"5a185d82-b427-4b8f-bfc9-7892a4e4ae5d"}
L13: {"type":"system","subtype":"api_retry","attempt":8,"max_retries":10,"retry_delay_ms":38405,"error_status":null,"error":"unknown","session_id":"463964d2-6379-40ea-bef0-abfb3433557f","uuid":"b16822b3-d969-4a4c-8c89-cc61c382f18f"}
```
（`api_retry` 共 8 行：L6、L7、L8、L9、L10、L11、L12、L13；前兩行逐字收錄如上，其餘行同型、attempt 遞增。）

### 逐欄結論（依上列原文）
| 欄位 | (a) 真實成功原文 | (b) 歷史失敗原文 | 現版讀取（9a4fc213） |
| --- | --- | --- | --- |
| `init.apiKeySource` | a1／a2 init 行 `"apiKeySource":"none"` | b1／b2 init 行 `"none"` | :700／:702 |
| `init.model` | a1／a2 `"model":"claude-opus-5-5"` | b1／b2 同 | :733 |
| `init.tools` | a1／a2 `"tools":[]` | b1／b2 `"tools":[]` | :704／:708（K9b） |
| `system/hook_response` | a1／a2 各 2 行 `exit_code:0` | b2 2 行 | :660 |
| `system/api_retry` | 成功路徑無 | b2（attempt 1..8） | :668 |
| assistant `error` | 成功路徑無此鍵 | b1 `"error":"authentication_failed"` | :672 |
| `result.is_error` | a1／a2 `false` | b1 `true` | :712 |
| `result.terminal_reason` | a1／a2 `"completed"` | b1 `"api_error"` | :717（僅入 detail） |
| `result.modelUsage` | a1／a2 僅 `claude-opus-5-5` | b1 `{}` | :725 |
| `result.result` | a1／a2 非空字串 | b1 未登入文字 | :737 |

### Step 0 與本票 scope 分開記錄
- **Step 0 腳本**（`verify_checklist.sh tmp/verify_cmd.conf`，`9a4fc213`）：**FAIL**——[0.1] 已採 `EXCLUDED_PAYLOAD=PROGRESS.md` 而通過，[0.2] 禁區檢查不採 `EXCLUDED_PAYLOAD`，因他 session `72352908` 觸及本票 FORBIDDEN 之 `.claude/PROGRESS.md` 而判 `⛔ FORBIDDEN FILE`。腳本在 `.claude/skills/**`（本票 FORBIDDEN），未修改。**不宣稱 Step 0 PASS。**
- **本票 scope 核對**（另一件事）：本票 5 個實作提交只觸及 4 個 ALLOWED 檔（見證據附錄 AC16 逐提交 stat）；`close_helper.py preflight` 之 scope＝PASS（只計實作提交）。

<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: 交付：route.py 新增 advisor.claude-cli 通道（Claude 訂閱 CLI Opus 5.5／High；有票經 incident 記帳、顯式 --no-ticket 零 ledger；init.tools 空陣列、is_error 布林、模型集合、最終 cwd 解析等嚴格判定；無備援、不轉計費 API），實作 781dcd49／22770829／8e4088b4／5abbd211／9a4fc213。 / 驗收：TKA close（Codex gpt-6-sol）第一輪 BLOCKED（AC11／AC12 FAIL）→ Rev A.4 修復與證據補件 → 第二輪 NOT_CLOSED（AC12 UNVERIFIED）→ Akira 具名批准之 AC12 證據可讀性獨立續審 PASS；17 條 AC 全 PASS／N/A；真實呼叫 2 次發生於修復前實作 781dcd49（成果表標待重驗）。 / 審查史：Codex Cross-Review 三次 REPORT_FIX → CHECKPOINT_REPORT_FIX_LIMIT，無終局 verdict（reviews.codex=N_A，非 PASS）；Step 0 verify_checklist [0.2] 因他 session 72352908（PROGRESS.md）判 FAIL，未宣稱 PASS，本票 scope 另以實作提交核對 PASS。
- 最終驗收結論: 核心 AC 12 條皆為 PASS 或有效 N/A；非核心 PASS 4／N/A 1／UNVERIFIED 0／FAIL 0（共 17 條 AC）
- 實作提交: 781dcd49, 22770829, 8e4088b4, 5abbd211, 9a4fc213
- 實際收束結果: 2026-10-24 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: codex/gpt-6-sol（tier=heavy）；覆蓋 AC 17／17（全部），independent_call=True
- 披露: dependency contract: 未知（未宣稱已確認無缺件）；reviewer limitation: reviewer 本地命令工具未能啟動；依 prompt 內嵌之 diff／測試／證據擷取判定
<!-- close-result:end -->
