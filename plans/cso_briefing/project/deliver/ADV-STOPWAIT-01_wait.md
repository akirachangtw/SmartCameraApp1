# ADV-STOPWAIT-01: Gate Stop guard 於主 agent 合法等待背景 subagent／workflow／teammate 時不累計 NO_PROGRESS（依 Stop hook 輸入 `background_tasks` 之類型篩選），無新增等待預算

**簽發日期**: 2026-10-24
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ADV-CLAUDECLI-01 —— 事件：R0 subagent 執行期間主 agent 之 Stop 被 guard 阻擋累計，seq 8–13 觸發 `human NO_PROGRESS`；ADV-ENTRY-01／INFRA 系 Gate Stop guard（`gate_stop_guard.py`）原立法
- `coordination_dependency`: 無

## 來源（Parent）
- Parent type：Plan-backed；路徑 `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261024-autonomy-closeloop-plan.md`（Revision R3；Fixture false／Plan Gate PASS／Ticket mode eligibility ALLOWED）；plan-local item `T06`；讀取時間 2026-10-24。
- Selected item：Stop guard 背景等待（diag C5）。
- 承重決策（Akira 2026-10-24 裁決）：①依「類型篩選」續行——僅 `background_tasks` 中屬 `subagent`／`workflow`／`teammate` 者視為合法等待；背景 `shell`、`monitor`、`cloud session`、`MCP task` 不算；②**不新增任意八次等待預算**（本票不引入任何自訂之連續暫停上限；平台自有之「連續 8 次 stop hook continuation 上限」`CLAUDE_CODE_STOP_HOOK_BLOCK_CAP` 為 Claude Code 平台行為，與本票無關、不改）。
- 票號歸屬：本票由「工程師自主閉環」議題（源自 ADV-CLAUDECLI-01 收束與 Advisor 成果表 P1 缺口）發起，依 Akira 2026-10-24 裁決與模板 v3.12「發起議題律」掛 `ADV` 前綴。觸及之 hook 區域屬 Gate 子系統（目前非「仍在開發中之他議題」；若 Akira 另行轉交原議題對話，由其決定）。

## 證據（開票時 live，2026-10-24）
- **官方契約（第一手，CONFIRMED）**：`https://code.claude.com/docs/en/hooks.md` §Stop → §Stop input（2026-10-24 以 WebFetch 讀取該頁 offset 199000 起之區段）：「Stop hooks receive `stop_hook_active`, `last_assistant_message`, `background_tasks`, and `session_crons`」；「The `background_tasks` and `session_crons` arrays let hooks distinguish "session is done" from "session is paused waiting for background work to wake it back up". Both arrays are present when the task registry is reachable and are empty when nothing is in flight or scheduled.」；「Each entry in `background_tasks` describes one in-flight task」，欄位 `id`／`type`（`shell`、`subagent`、`monitor`、`workflow`、`teammate`、`cloud session`、`MCP task`；「Falls back to the raw discriminant for unrecognized types」）／`status`／`description`／選填 `command`／`agent_type`／`server`／`tool`／`name`。SubagentStop 同含此二陣列但「scoped to the parent session」。
- **官方未載（UNKNOWN，不推定）**：`status` 之值域；Stop 是否在背景任務執行中被延後（文件僅暗示 Stop 可在背景工作進行時觸發）。
- **本地 guard（live @b20d0ba0 之最近修改提交）**：`.claude/hooks/gate_stop_guard.py` `decide()`（:431–486）於 `persistent`（open incident 或 missed gate）時呼叫 `persistent_block()`（:489–507）；`persistent_block` 以 `progress_vector` 與 `load_seq` 之 `(count, hw)` 判進度，無進度則 `count += 1`，`count >= K_NO_PROGRESS`（=6，:32）即 `ledger_write human NO_PROGRESS`（:501–502）並放行，否則寫 `blocks` 事件並回 block。全檔對 `background_tasks`／subagent 無任何引用。
- 測試：`.claude/hooks/tests/gate_stop_guard_test.py`（`Env.payload(**extra)` 可注入額外欄位；既有 `ProgressAndFailures` 類含 `test_sequence_counts_unchanged_blocks_and_ignores_noise`、`test_k_without_progress_writes_no_progress_and_allows`）。
- **反例檢查**：①`background_tasks` 為模型可間接觸發之狀態（啟動長時間 subagent）→ 模型可藉每回合開一個背景 subagent 規避 NO_PROGRESS 計數。此為使用者已裁決之類型篩選的**固有殘餘風險**，本票不以自訂預算封閉（Akira 明示不新增八次預算）；於票面與收尾實錄揭露。②欄位缺失／非 list／條目非 dict／`type` 非字串或大小寫異形 → 依 fail-closed 視為無合法等待，計數照常。③僅背景 shell 在跑（如 `tail -f`）→ 不算合法等待，計數照常。④暫停期間不得重置或增加既有 `seq` 計數（暫停結束後從原計數續算），不得寫 `blocks` 事件。
- **機械後盾揭露**：guard 既有覆蓋矩陣屬 Stop 事後防線；本票只加一條「不計數」分支，不改 incident／round／ledger 語義。

## Verify Config
TICKET_ID=ADV-STOPWAIT-01
CREATED=2026-10-24
PRESET=feature
ALLOWED=.claude/hooks/gate_stop_guard.py,.claude/hooks/tests/gate_stop_guard_test.py
FORBIDDEN=.claude/scripts/**,.claude/hooks/registry_review_stop_guard.py,.claude/hooks/session_start.sh,.claude/hooks/stop.sh,.claude/hooks/telegram_stop.sh,.claude/hooks/telegram_ask.sh,.claude/hooks/snapshot_cueshot_memory.py,.claude/hooks/notify.sh,.claude/hooks/tests/registry_review_stop_guard_test.sh,.claude/hooks/tests/session_start_owner_test.sh,.claude/hooks/tests/telegram_stop_test.sh,.claude/settings.json,.claude/settings.local.json,.claude/skills/**,.claude/rules/**,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,CLAUDE.md,app/**,plans/roadmap/**,plans/architecture/**,plans/cso_briefing/project/TICKET_REGISTRY.json,.gitignore
MAX_LINES=N/A（安全不變量類票：fail-closed／守門判定，失敗形狀為審查發現式，不可先驗估算；TPL R3）
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-24 16:00
BASE_COMMIT=306b5276
TARGET_BLOB=N/A（開票端未取 blob；AI-0 逐檔現值錨點替代）
INTRODUCED_CAPABILITY=feature:stop-guard-background-wait
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/hooks/gate_stop_guard.py`、`.claude/hooks/tests/gate_stop_guard_test.py`
- **動作**: 確認 `K_NO_PROGRESS = 6`（唯一）、`def persistent_block(` 唯一且內含 `count += 1` 與 `human`／`NO_PROGRESS` 寫入、`def decide(` 之 `persistent = bool(open_incs or missed)` 區段、全檔無 `background_tasks`；測試檔 `Env.payload(` 簽名含 `**extra`、`ProgressAndFailures` 類存在；`.claude/settings.json` 之 Stop 註冊指向 `gate_stop_guard.py`（唯讀確認，不改）。**live 取證**：本機以真實 Claude Code Stop hook 輸入確認 `background_tasks` 之實際形狀為 AC 前提——以一次性唯讀探測（例如以官方文件所載形狀之 fixture 對照；若無法於不改設定下取得真實輸入，標「僅文件契約，未取真實輸入」並於收尾揭露，不為此改 `settings.json`）。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置；`background_tasks` 之形狀與官方文件不符 → 停止回報。**
- [x] 完成（2026-10-24：`K_NO_PROGRESS = 6`〔gate_stop_guard.py:32〕唯一、`persistent_block` 唯一且含 `count += 1` 與 `human NO_PROGRESS` 寫入、全檔原無 `background_tasks`；測試檔 `Env.payload(` 簽名含 `**extra`、`ProgressAndFailures` 存在；`.claude/settings.json:150` Stop 註冊指向 `gate_stop_guard.py`（唯讀）；基線 `gate_stop_guard_test.py` 54 OK。**真實輸入**：僅取得官方文件契約（§Stop input 第一手引文，見〈證據〉），**未取得真實 Stop hook 輸入樣本**——不為此改 `settings.json`，於收尾揭露）

### AI-1: 合法等待判定與暫停分支
- **檔案**: `.claude/hooks/gate_stop_guard.py`
- **現值**: `persistent_block` 對每次無進度之 Stop 計數，無任何背景任務概念。
- **目標值**: 新增純函式 `waiting_on_agent_work(payload) -> bool`：僅當 `payload["background_tasks"]` 為 list 且至少一個條目為 dict、其 `type` 為字串且**恰等於**（大小寫敏感）`"subagent"`／`"workflow"`／`"teammate"` 之一時回 True；其餘一切（欄位缺失、非 list、條目非 dict、`type` 缺失／非字串／他值、空 list）回 False（fail-closed）。於 `persistent_block` 最前（讀 `load_seq`／計數之前）：`if waiting_on_agent_work(ctx.payload): return None`——放行該次 Stop，**不**寫 `blocks` 事件、**不**改 `seq` marker（計數與高水位保持不變）、**不**寫 `human`；其後 `count` 之計算與 `K_NO_PROGRESS` 判定不變。不新增任何自訂暫停次數上限或時間預算。
- [x] 完成（新增 `WAIT_TASK_TYPES`＋`waiting_on_agent_work(payload)`；`persistent_block` 最前 `if waiting_on_agent_work(ctx.payload): return None`；無新增次數／時間上限常數）

### AI-2: 測試
- **檔案**: `.claude/hooks/tests/gate_stop_guard_test.py`
- **目標值**: 新增測試類（沿用 `Base`／`Env` 與真實子程序 `hook()` 呼叫，非 mock）覆蓋下列 AC。
- [x] 完成（`BackgroundWait` 類 6 案，真實子程序呼叫 hook；全檔 60 OK〔基線 54＋6〕）

## Hard Constraints
1. 不改 `K_NO_PROGRESS`、ledger schema、incident／round／missed-check／single-check 語義，不改 `decide()` 其他分支。
2. 不新增任何自訂之連續暫停上限、重試或等待預算；不改 `settings.json` 之 hook 註冊；不改平台 `CLAUDE_CODE_STOP_HOOK_BLOCK_CAP`。
3. 判定只讀 Stop 輸入之 `background_tasks`（平台填寫），**MUST NOT** 讀 transcript 文字、模型自述或 `last_assistant_message` 推定「正在等待」。
4. `waiting_on_agent_work` 不得因任何輸入而拋出例外致 guard 失效（既有 fail-open 契約保留）；未知形狀一律回 False。

## Error Handling
- 官方文件之 `background_tasks` 形狀與實測 Stop 輸入不符 → 停止回報。
- 既有 `gate_stop_guard_test.py` 任一案因本改動失敗且無法在不弱化斷言下同步 → 停止回報。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** `gate_stop_guard.py` 含 `waiting_on_agent_work`，且僅被 `persistent_block` 在計數前呼叫；全檔無新增之次數／時間上限常數。（`gate_stop_guard.py:494` `waiting_on_agent_work`、`:504-505` 僅被 `persistent_block` 於計數前呼叫；全檔無新增次數／時間上限常數〔僅 `WAIT_TASK_TYPES` 型別集合 `:491`〕；提交 `4a33ca73`）
- [ ] **[可達性]** guard 之判定 input `background_tasks` 之 producer 為 Claude Code 平台之 Stop hook 輸入（官方文件 §Stop input，第一手引文見〈證據〉）；本票測試以文件所載形狀之真實子程序輸入驅動；producer 證據欄執行後回填（`檔案:行號` 或官方文件章節＋AI-0 取證結果；若僅文件契約則如實標「未取真實輸入」）。（**未勾，如實揭露**：`background_tasks` 之 producer 為 Claude Code 平台之 Stop hook 輸入，repo 內無 `檔案:行號` 可指認；**僅有官方文件契約〔第一手引文見〈證據〉〕＋文件形狀測試，未取得真實 Stop 輸入樣本**；SV 報告標 PARTIAL，Codex Cross-Review 判 ADVISORY 並明示取得可核驗之真實平台輸入證據前該 AC 不得勾選）
- [x] **[行為性]** given 有 open incident（`persistent` 為真）且既有 `seq` 計數＝n：(a) Stop 輸入含 `background_tasks:[{type:"subagent",...}]` 連續 10 次 → expect 每次放行（無 block 輸出）、`seq` marker 之 count／hw 不變、ledger 無新 `blocks`、無 `human NO_PROGRESS`；(b) `type` 為 `workflow`／`teammate` 同上；(c) 混合 `shell`＋`subagent` → 暫停；(d) 僅 `shell`／`monitor`／`cloud session`／`MCP task` → 計數照常（第 K 次起寫 `human NO_PROGRESS` 並放行，行為與修改前一致）；(e) 欄位缺失／`null`／非 list／空 list／條目非 dict／`type` 缺失、非字串或大小寫異形（`"Subagent"`）→ 計數照常（fail-closed）；(f) 暫停期間後恢復為無背景任務之 Stop → 自原 count 續算（不重置、不跳增）。（`BackgroundWait`〔`gate_stop_guard_test.py:630`〕：(a) 連 10 次 subagent 等待皆放行、seq／`blocks`／`pending_human` 不變、結束後自原計數續算；(b) workflow／teammate；(c) 混合 shell＋subagent；(d) 僅 shell／monitor／cloud session／MCP task 照常計數至 NO_PROGRESS；(e) 12 種異形輸入 fail-closed；(f) 續算；真實子程序呼叫 hook）
- [x] **[行為性]** 既有 `gate_stop_guard_test.py` 全數通過（不弱化既有斷言；基線通過數於 AI-0 記錄）。（全檔 `Ran 60 tests ... OK`，修改前基線 54 OK；未弱化既有斷言）
- [x] `python -I` 執行 `gate_stop_guard_test.py` PASS（取代 assembleDebug；本票無 Android 產物）。~~assembleDebug PASS~~ (N/A: 無 app/ 變更)（`python -I .claude/hooks/tests/gate_stop_guard_test.py` → 60 OK；~~assembleDebug PASS~~ 票面 N/A，本環境 Gradle 回 loopback IOException，未取得 Gradle 成功證據）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（實測 2 檔：`gate_stop_guard.py`、`gate_stop_guard_test.py`，皆 ALLOWED；校正後範圍 `326bea4c..4a33ca73`；`git diff --check` 無輸出）

## 收尾實錄（Agent closeout，2026-10-24；交 TKA close 判定，非自行終審）
- **實作提交**：`4a33ca73`（`gate_stop_guard.py` +16、`gate_stop_guard_test.py` +79）。
- **QA**：Step 0 PASS（DIFF_REF 因並行 session 之 `326bea4c` 提交交錯而依腳本建議校正為 `326bea4c`，已揭露於 SV 報告）；Gradle 因 `java.io.IOException: Unable to establish loopback connection` 未能執行，`reviews.qa=N_A`。
- **SV／Cross-Review**：R0 FORWARD（非正式 PASS）；Codex `gpt-5.6-sol` 終局 verdict＝**ADVISORY**（`codex_verdict_actual`；registry `reviews.codex` 依 Transition Table 映射為既有 enum `PASS`）：producer 判 (C)（平台寫入、無 repo 內 writer），因 SV 報告已據實標 PARTIAL 而不構成 REPORT_FIX，並明示取得可核驗之真實平台輸入證據前 `[可達性]` AC 不得勾選。
- **未勾 AC**：`[可達性]`（見該行揭露）。
- **已知限制**：真實 Stop 輸入樣本未取得；`status` 值域與 Stop 是否於背景任務期間被延後官方文件未載；殘餘規避風險（模型可藉開背景 subagent 規避計數）依 Akira 2026-10-24 裁決不以自訂預算封閉；Gradle 未驗。
- **PROGRESS.md Dashboard 同步**：FORBIDDEN 延後項，待 `/compact`。本票 close 之保存由 `persist` 執行。
- **指示式觀察**：本票上線後，live Stop guard（即本 session 所用 hook）於 subagent 等待期間不再累計 NO_PROGRESS——可於後續票之 R0／reviewer 等待期間再觀察（非盲測）。


<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: 交付：gate_stop_guard.py 新增 waiting_on_agent_work（僅 subagent／workflow／teammate 類型、fail-closed），於 persistent_block 計數前放行，不寫 blocks／不動 seq／無新增等待預算；實作提交 4a33ca73，gate_stop_guard_test.py 60 OK（基線 54）。 / 驗收：close reviewer gpt-6-sol 6 條 AC 全 PASS（AC2 [可達性] 為官方 Stop input 文件契約＋文件形狀測試，未取得真實 Stop 輸入樣本；票面該勾選框維持未勾）；Gradle 因 loopback IOException 未執行，reviews.qa=N_A。 / Cross-Review 實際 verdict 為 ADVISORY（reviews.codex=PASS 為 enum 映射）；殘餘規避風險（模型可藉開背景 subagent 規避計數）依 Akira 2026-10-24 裁決不以自訂預算封閉；PROGRESS Dashboard 同步待 /compact。
- 最終驗收結論: 核心 AC 3 條皆為 PASS 或有效 N/A；非核心 PASS 3／N/A 0／UNVERIFIED 0／FAIL 0（共 6 條 AC）
- 實作提交: 4a33ca73
- 實際收束結果: 2026-10-24 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: codex/gpt-6-sol（tier=heavy）；覆蓋 AC 6／6（全部），independent_call=True
- 披露: dependency contract: 未知（未宣稱已確認無缺件）；reviewer limitation: reviewer 無法讀檔，證據嵌入 prompt；AC2 僅文件契約，真實 Stop 輸入未驗
<!-- close-result:end -->
