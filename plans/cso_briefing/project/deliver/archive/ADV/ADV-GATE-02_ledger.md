# ADV-GATE-02: 新增 gate_ledger.py——事件溯源、並行安全的 incident／三輪／PPSS 證據／入口 check ledger helper（含單元測試）（Rev A）

**Revision**: Rev A（2026-10-13；依審查方指示 A1–A5 就地修訂票面；未實作、未改 lifecycle、ALLOWED 不變、未重跑 plan／diag、未再次付費外審）。變更：A1 PPSS 不付費限制縮為開票階段與離線單元測試，新增 executor 驗收實跑 AC；A2 Windows `os.kill` 證據改記官方契約並刪除重現探針；A3 External-review decision 改 `EXECUTED`；A4 AI-0 取號改記歷史並於開工核對冊面；A5 helper 身分措辭改為「票檔落點保守判斷」並統一 `loc:*` 標記。

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T03`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261011-autogate-plan.md`；Parent artifact status＝`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`／`Fixture: false`（Revision C＋C.1，2026-10-11）；plan-local item ID＝`T03`；Parent read timestamp＝2026-10-12 22:10（全檔讀取）。支撐 diag：`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261012-t03-diag.md`（`Gate: PASS`／`Verdict: PARTIALLY_CONFIRMED`／`Fixture: false`，Akira 2026-10-12 接受並指定 Next A）。`.tmp` 檔日後可能被清理，故本票自帶下列摘錄，不依賴其存續。

**Selected item 原文（T03 goal，摘要不改義）**：實作 `gate_ledger.py`——事件溯源 ledger（載體不在 `tmp/`／`.tmp/`、不使 `git status` 變髒、跨 clear／compact 存活、worktree 間不分裂；路徑 `<git-common-dir>/gate/<TICKET_ID>.jsonl`）；incident 以 subject 為身分、跨 gate point 與 verdict 續算；`round start --expect` CAS 且須帶策略與授權；同 incident **最多 3 輪**、finding／腳本執行／step-limit 不計輪；提前升報須 `strategy_scan`；PPSS 三態＋支持斷言；`check <gate_point>` 供各入口呼叫並寫 receipt；`status` 供恢復義務讀取。Parent 約束：不新增資料庫／daemon／framework／平行 SSOT；helper 無任何 registry 呼叫；不改 hooks／settings／既有腳本；不宣稱「完整 Gate」。

**已裁決補充（Akira 2026-10-12 於開票指令中裁決，S1–S5，優先於 plan 對應措辭）**：
- **S1（lock 暫態重試）**：acquire 階段對 Windows 暫態 `PermissionError` 可**有限重試**，僅限已授權之 ledger 路徑（鎖檔）；必須有 timeout、**保留原始錯誤**，真實權限失敗不得無限吞掉；工具／classifier 拒絕**不套用**此重試。須有「持續拒絕」與「存活 owner 不被誤回收」負向測試。
- **S2（身分）**：`--ticket` 明示優先於 `active.json` 作**身分選擇**，**不構成 activation 或授權**。mutation 核對票存在與適用狀態；無 ticket 不得用 `unknown`；終態票不開輪；唯讀 `status` 可核驗歷史 ledger。（Rev A 對齊：此處「適用狀態」「終態」均指依票檔落點的**保守可用性判斷**〔`loc:*` 標記，見 AI-2〕，非權威 lifecycle。）
- **S3（common-dir／worktree）**：common-dir 解析為絕對路徑；跨 worktree 共用性為 T03 局部驗證；**不得改第二 worktree 的受版控檔案或 Git 設定**。
- **S4（審查）**：review 採完整 payload＋executor 測試結果，**不冒稱 Codex 獨立執行**；不再追加預設 model 探針；實際 review 到時依現役入口（`cross_review.sh`）驗證，**不預填 PASS**。
- **S5（PPSS，Rev A 修訂）**：**開票階段**與**離線單元測試**不付費、不連網，沿用既有 artifact；`evidence_ok` 僅表示「來源閱讀與支持聲明可追溯」，**不宣稱 helper 驗真**。helper 與單元測試**不呼叫 PPSS、不連網**（HC2／AI-4 邊界不變）；**executor 驗收階段**另有一次由 agent 發起的真實 PPSS 實跑（見 AC「PPSS 真實鏈實跑」，復原 plan T03 AC 之「實跑」項），額度與未驗收束規則見該 AC。**遺留 probe 檔 `.git/gate_probe_T03/` 明示保留**（未取得精確清理批准，不重試刪除；不阻擋本票）。

**證據分級**：①**CONFIRMED（2026-10-12 22:10 live 直讀／本地探針）**：HEAD `c3a71f16`、工作樹 clean；`.claude/scripts/` 僅 `activate_ticket.sh`、`m3_family_audit.sh`，`gate_ledger.py` 不存在；`.claude/scripts/gate_ledger.py`、`.claude/scripts/tests/test_gate_ledger.py` 未被 `.gitignore` 排除（`git check-ignore` 無輸出）；Python 3.14.5（win32）；環境無 pytest（`No module named pytest`），repo 既有 py 測試為 stdlib `unittest`（`.claude/skills/qa/tests/test_preflight_core.py`）；`.gitattributes` 僅鎖 `*.md`／`*.sh` LF，`.py` 無 eol 屬性（`core.autocrlf=true`，既有 `.py` 為 `i/lf w/crlf`）；`active.json` 七欄，writer＝`activate_ticket.sh` L184–196 與 `qa/scripts/verify_checklist.sh` L55–76（tmp 檔＋`mv` 原子替換），`session_start.sh reset` 只刪 `verify_cmd.conf`／`.tmp/sv_report.md`／`tmp/handoff.md`、不刪 `active.json`；`tools/cleanup_expired_tmp.py` L19–24 `TARGETS` 僅 `<APP>/tmp`、`<APP>/.tmp` 與 PLT 兩處，保留 7 天，**不含 `.git`**，`--target`／`--dry-run` 為其 CLI；Registry 讀值：`ADV-GATE-01`、`ADV-RULE4C-01` 皆 `CLOSED`（reviews 全 PASS／N_A）；**開票取號歷史（2026-10-12 22:10，登冊前）**：`ADV-GATE-02` 於 registry 主檔／archive／Pending／deliver／void／plans／.claude 全無出現，`ADV-GATE-\d+` 最大號＝01，`TICKET_REGISTRY.json` 主冊 ADV 前綴僅 `ADV-REPORTFIX-01`、`ADV-GATE-01`、`ADV-RULE4C-01`；其後本票自身已登冊（開工核對見 AI-0）。②**CONFIRMED（本地隔離 probe，N=3）**：於 `.git/gate_probe_T03/` 建立 jsonl 成功且 `git status --short` 為空；8 process 同時 `round_start expect=0` 並各 append 21 次 block → 恰 1 勝者、rounds=1、blocks 168／168、無遺失；過期 lock 可回收。③**CONFIRMED（本地首次 probe 暴露，N=1）**：Windows 上 `O_CREAT|O_EXCL` 搶鎖，遇別 process 剛 `unlink` 鎖的瞬間回 `PermissionError`（delete-pending；非 `FileExistsError`、非權限拒絕）。④**SUPPORTED〔relayed〕（官方契約，Rev A 改記）**：Python 3.14 官方文件 `os.kill`——Windows 上，非 `CTRL_C_EVENT`／`CTRL_BREAK_EVENT` 的 sig 以 `TerminateProcess` 終止目標程序；故 `os.kill(pid, 0)` **不可作存活查詢**；須改用 Win32 `OpenProcess`／`GetExitCodeProcess`（ctypes）。來源：https://docs.python.org/3.14/library/os.html#os.kill ；查核日 2026-10-12（**審查方查核，〔relayed〕**，本票作者未獨立重讀該頁；`research_submit` 結果 `rsch-20261012-9506410da5` 之 `results[]` 收錄 `docs.python.org/3/library/os.html`，僅佐證 URL 被命中，未驗頁面內文）；不記搜尋 metadata 的 `last_updated`。**本地觀測（不作依據、不重跑）**：Python 3.14.5 win32 一次探針（N=1）中 `os.kill(<已結束子程序 pid>, 0)` 未丟例外、對存活子程序亦未終止——與官方契約**不一致**，原因未查；本票決策**僅依官方契約**（禁用 `os.kill(pid, 0)`），此觀測不得被引為「可用」或「不可用」之證據。⑤**CONFIRMED**：`~/.codex/worktrees/4ba7/SmartCameraApp1` 為第二 worktree（detached），其 gitdir `commondir`＝`../..`（指向主 `.git`）；plan A11：worktree 內 `git rev-parse --git-common-dir` 回主 `.git`。⑥**SUPPORTED（尚未實測）**：從第二 worktree 實際執行 helper 之共用性——diag 時對該 worktree 的檢查被權限拒絕（未改道重試）；本票以暫存 repo＋暫存 linked worktree 做決定性驗證，真實第二 worktree 僅列選配唯讀檢查（AC 內有失敗分支）。⑦**UNKNOWN（不阻擋）**：`cross_review.sh` 實際 model（`gpt-5.6-sol`）下 Codex 是否仍受 sandbox 1312 影響；diag 唯一 bounded probe 用的是 `~/.codex/config.toml` 預設 model（`gpt-6-luna`），API 回 400（ChatGPT 帳號不支援），**非** 1312 亦未到讀檔階段。審查證據安排見 S4。

**External-review decision**: EXECUTED — `rsch-20261012-9506410da5`（觸發條款 6a，約 $0.005，2026-10-12；artifact 位於 PLT `data/research/2026-10/`，三題：`os.kill` Windows signal 0 行為、`O_EXCL` 暫態 `PermissionError`／delete-pending 競態、`msvcrt.locking` 對 `O_EXCL` 鎖檔與 stale lock）。**命中範圍如實限定**：僅 `os.kill` 題獲官方文件命中（證據④，〔relayed〕）；其餘兩題（`O_EXCL` 暫態 `PermissionError`、`msvcrt.locking`／stale lock）為 **UNKNOWN**，由本地 probe ②③ 以其限定強度（本機 Python 3.14.5 win32、N=1／N=3，非官方契約）承載，**不得寫成全項確認**。**不改為 WAIVED**；**不因未命中而對同題重觸發外審**。ADV-GATE-01／ADV-RULE4C-01 之 WAIVED 明載「不傳遞至 T03／T04」，不引為本票先例。

**簽發日期**: 2026-10-12
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: normal  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: ADV-GATE-01（已 CLOSED；Gate 契約節為本 helper 詞彙與規則之來源，AI-0 逐字核對）
- `evidence_dependency`: ADV-GATE-01（Gate 節詞彙：`finding`／`round`／`step_limit`／`consult`／`missed_check`、`HUMAN_REQUIRED` 類別、PPSS 三態）、ADV-REPORTFIX-01（`cross_review.sh` LIMIT 行為，僅供對照）——僅作字面判準來源，本票錨點皆為 live 直查
- `coordination_dependency`: 無（autogate plan T02 尚未開票；T02 須串行於本票之後，因其 `check` 呼叫須指向已存在之指令；`concurrency.md` §2）

## Verify Config
TICKET_ID=ADV-GATE-02
CREATED=2026-10-12
PRESET=feature
ALLOWED=.claude/scripts/gate_ledger.py,.claude/scripts/tests/test_gate_ledger.py
FORBIDDEN=.claude/hooks/**,.claude/settings.json,.claude/settings.local.json,.claude/agents/**,.claude/skills/**,.claude/rules/**,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,.claude/scripts/activate_ticket.sh,.claude/scripts/m3_family_audit.sh,tools/**,app/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json
MAX_LINES=N/A
DIFF_REF=c3a71f16
ANCHOR_PROBED=2026-10-12 22:10
BASE_COMMIT=c3a71f16
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=tech:gate-ledger-helper
DEPENDENCY_DELTA=NONE

> PRESET=feature（`.py` 新檔＋unittest；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。`MAX_LINES=N/A`：含並行安全／停損／升報類不變量，失敗形狀為審查發現式，不可先驗估算（TPL v3.5 R3）；MAX_LINES 非 execution gate。`TARGET_BLOB=N/A`：標的皆為**新增檔**，無既有 blob；`BASE_COMMIT` 必填、AI-0 逐檔確認不存在，`N/A` 不豁免 AI-0／scope／evidence 驗證。`INTRODUCED_CAPABILITY` 為顯式 canonical token（非自然語言）。`Traycer Workflow: N/A`：純 `.claude/` 內治理工具腳本，不觸 `app/`；審查走 SV＋Cross-Review payload（S4）。**DEFERRED-TO-DOCSYNC（不開票）**：FORBIDDEN 命中 `.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` → `/qa` Step 4 SKIP；Dashboard 同步、Done 標記、票檔 Pending→deliver 搬移與 checkbox 勾選留待下一輪 `/compact` 標準收尾，**不另開票**。`.claude/scripts/tests/` 為新目錄（沿用 repo 既有 `…/tests/` 慣例）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）＋取號紀錄
- **檔案**: `.claude/scripts/gate_ledger.py`（新增）、`.claude/scripts/tests/test_gate_ledger.py`（新增）；唯讀參照 `.claude/rules/ssot-governance.md`、`.claude/scripts/activate_ticket.sh`、`.claude/skills/qa/scripts/verify_checklist.sh`、`.claude/hooks/session_start.sh`、`tools/cleanup_expired_tmp.py`
- **動作**: 逐項確認（行號僅供定位；以字面搜尋為準）：
  - `git rev-parse --short HEAD`、`git status --short`（ALLOWED 兩檔此時**不存在**；若已存在 → 依 AI-0 三分流處置）；`git check-ignore -v` 對兩個 ALLOWED 路徑**無輸出**。
  - 取號：開票取號歷史（2026-10-12 22:10，登冊前：全冊 grep 無 `ADV-GATE-02`、`ADV-GATE-\d+` 最大號＝`01`）僅為歷史記錄，**開工時不再要求命中數為 0**。**開工核對**：`registry_list(project=SmartCameraApp1, ticket_id=ADV-GATE-02)` 冊面僅**一筆**，且其 `file` 指向本票檔（`Pending/ADV-GATE-02_ledger.md`，或依 lifecycle 已歸位之 `deliver/`）；票檔實際落點與冊面 `file` 一致。**本票自身存在不算占號。**
  - `ssot-governance.md` Gate 節仍在：`^## Autonomous Engineering Gate$` 恰 1 筆；詞彙 `finding|round|step_limit|consult|missed_check`、`NO_AUTHORIZED_STRATEGY`、`EXHAUSTED`、`RETRIEVED|SOURCE_READ|INVALID`、`evidence_ok` 於該節內命中（本 helper 詞彙與此一致；不一致 → 停止回報，不自行改契約）。
  - `active.json` writer／reader 字面仍在：`activate_ticket.sh` 之 `ACTIVE_JSON_DEST="tmp/verify/active.json"`、`verify_checklist.sh` 之 `write_active_json()`、`session_start.sh` 之 `ACTIVE_JSON=` 與七欄鍵（`ticket_id ticket_revision repo_root branch head_sha activated_at activation_source`）；`cleanup_expired_tmp.py` 之 `TARGETS` 不含 `.git`。
  - 環境：`python --version`；`python -c "import unittest"`；`python -m pytest` **不作為**依賴。
  - 載體路徑寫入能力：`git rev-parse --git-common-dir` 之絕對路徑下 `gate/` 於**正式使用時**由 helper 首次寫入建立；本 AI **不**在真實 `.git` 內建立／刪除任何探針檔。被權限拒絕 → 依 Error Handling 停止，不改道。
  - `.git/gate_probe_T03/` **只確認其存在並保持原狀**（不讀寫、不刪除、不納入任何測試）。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流分類處置**（`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 為資訊性，記錄後以現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止；`BLOCKED_UNATTRIBUTED_STATE` 因僅觸 `.claude/` 治理面，記錄後續作）；Gate 節詞彙不符，或 `ADV-GATE-02` 被**另一張不同票**重號、或冊面 `file` 與票檔實際落點不一致 → 停止回報。
- [x] 完成（2026-10-13，HEAD `c3a71f16`）：ALLOWED 兩檔當時不存在、`git check-ignore` 無輸出；冊面 `ADV-GATE-02` 僅一筆且 `file`＝`Pending/ADV-GATE-02_ledger.md`；Gate 節詞彙命中；writer／reader／`TARGETS` 字面仍在；`.git/gate_probe_T03/` 原狀保留

### AI-1: 核心——ledger 路徑、事件 append、lock、fold
- **檔案**: `.claude/scripts/gate_ledger.py`（新增；Python 3 stdlib only；檔頭 Usage 註解為 canonical，列出全部子命令、旗標與 exit code）
- **現值**: （新檔，無）
- **目標值**:
  - **路徑**：`Path(git rev-parse --git-common-dir)`（以呼叫時 cwd 為基準）`.resolve()` 為**絕對路徑**；ledger＝`<common-dir>/gate/<TICKET_ID>.jsonl`，鎖檔＝同名加 `.lock`。`<common-dir>/gate/` 以外路徑**一律不讀不寫**（含 `gate_probe_*`）。
  - **事件**：每行一個 UTF-8 JSON 物件（`newline='\n'`，`ensure_ascii=False`），欄位至少 `seq`（行序，由持鎖者以 fold 末值＋1 賦予，**僅為 ledger 內行序，非新事件 ID 機制**）、`ts`（UTC ISO）、`type`、`ticket`、其餘依型別。**append-only**，不就地改寫；所有計數／狀態一律由 fold 還原。stdout 以 `sys.stdout.reconfigure(encoding='utf-8')` 輸出，避免 cp1252 下中文 `UnicodeEncodeError`。
  - **lock（acquire 階段）**：`os.open(lock, O_CREAT|O_EXCL|O_WRONLY)`；鎖檔內容為 `{"pid":…,"ts":…}`。`FileExistsError` 與 Windows 暫態 `PermissionError` **僅在 acquire 內**同樣以 5–20ms 間隔重試，總時限 `LOCK_TIMEOUT`（預設 10s，函式參數可覆寫供測試）；逾時拋 `LockTimeout`，訊息含**最後一次原始錯誤**（類別、errno／winerror、路徑）並以 `raise … from` 保留；CLI 對應 exit 6。**`PermissionError` 發生於 acquire 以外之操作（建立 `gate/` 目錄、append、讀取）→ 立即失敗（exit 6，原始錯誤原樣輸出），不重試、不換路徑**。
  - **stale 回收（保守）**：僅當「鎖齡 > `STALE_SECONDS`（預設 30s）**且** owner pid 確認已不存活」才 `unlink` 回收；pid 缺失／無法解析且鎖齡 > `STALE_SECONDS` 才視為無主可回收；owner 存活（或存活無法判定）→ **不回收**，繼續等待至 timeout。Windows 存活判斷用 ctypes `OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION)`＋`GetExitCodeProcess`（`STILL_ACTIVE=259`）；POSIX 用 `os.kill(pid, 0)`；**Windows 禁用 `os.kill(pid, 0)`**（理由：Python 3.14 官方契約——Windows 上非 `CTRL_C_EVENT`／`CTRL_BREAK_EVENT` 的 sig 以 `TerminateProcess` 終止目標，不可作存活查詢；證據④〔relayed〕；實作註解記錄此理由）。已知限制（如實揭露）：pid 重用可能使死 owner 被誤判存活→只造成等待至 timeout、不會誤回收。
  - **寫入**：持鎖 → fold 取現態 → 驗證 → append → `flush`＋`fsync` → 釋放（`finally`）。CAS：mutation 可帶 `--expect <n>`，與持鎖後 fold 值不符→拒絕（exit 5）。
- [x] 完成：`gate_ledger.py` 核心（路徑／事件／lock／fold）；E1 Win32 判定依 `GetExitCodeProcess` 非零才看 code（259 存活、其他已死），回傳零＝不可判定、保存 last error、不回收；ctypes argtypes／restype 已宣告、handle `finally` 關閉

### AI-2: 身分解析與票適用性核對
- **檔案**: `.claude/scripts/gate_ledger.py`
- **現值**: （新檔，無）
- **目標值**:
  - **身分選擇**：`--ticket <ID>` 明示 > `tmp/verify/active.json` 之 `ticket_id`（主 checkout 根下，唯讀解析）> 無。ID 須符 `^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$` 且含至少一位數字（與 `activate_ticket.sh` 同規則；**同時防路徑穿越**，`..`、`/`、`\` 一律拒絕，exit 3）。**無任何來源 → exit 3，訊息指出「無 ticket」；不得使用 `unknown`／空字串作 key，不建立任何 ledger 檔。**
  - **`--ticket` 不是 activation／授權**：helper **不寫** `active.json`、`verify_cmd.conf`、任何 `tmp/` 狀態，不呼叫 `activate_ticket.sh`；receipt 記 `ticket_source: explicit|active_json`。
  - **票存在／可用性（mutation 核對；Rev A：依票檔落點的保守可用性判斷，非權威 lifecycle）**：以主 checkout 根（`<common-dir>` 之上層；linked worktree 亦解析至主 checkout，因票檔 gitignored 只存在於主 checkout）下 `plans/cso_briefing/project/{Pending,deliver,void}/<ID>_*.md`（亦含 `<ID>.md`）定位，落點標記固定為 `loc:pending`／`loc:deliver`／`loc:void`／`loc:none`／`loc:multi`：僅 `loc:pending` → 可用；`loc:deliver`／`loc:void` → 拒絕所有 mutation（exit 4）；`loc:none` → exit 4；`loc:multi`（多於一處）→ exit 4（矛盾停止，不猜）。落點與冊面 lifecycle 不一致（如已 `REVIEW` 而檔仍在 `Pending/`）helper 無從得知，屬 Closure 紀律「矛盾停止」，**不由 helper 判定**。**不讀、不寫任何 `TICKET_REGISTRY*.json`，不新增 Registry 讀寫或平台依賴**。
  - **唯讀 `status`**：不論落點為何，只要 ledger 存在即可核驗歷史（exit 0，輸出欄位 `ticket_location` 取上列 `loc:*` 之一）；ledger 與票檔皆無 → exit 4；`status` 不建立目錄或檔案。無 `--ticket` 時讀 `active.json`，若該票 `ticket_location` 非 `loc:pending` 則輸出標示 `stale`。
- [x] 完成：`resolve_ticket()`／`ticket_location()`（`loc:*` 標記）／唯讀 `status`

### AI-3: incident／round／step-limit／consult／strategy-scan／human／blocks／missed-check
- **檔案**: `.claude/scripts/gate_ledger.py`
- **現值**: （新檔，無）
- **目標值**（詞彙與 Gate 節一致；規則由 fold 判定，不假裝判斷根因、策略新穎性或產品語意）：
  - `incident open --subject <tok>… [--gate-point P] [--verdict V] [--same-root-cause INC] [--independent-evidence REF]`：寫 `finding`（`rounds` 不變）。subject token 正規化（trim＋小寫）；與同票既有 open incident 之 subject **有交集**或帶 `--same-root-cause` → attach（續算，不重置）；僅當附 `--independent-evidence`（非空）**且** subject 與所有 open incident 無交集 → 建新 incident；同票已有 open incident 且缺獨立證據 → 拒絕並改 attach（exit 5 附說明）。`gate_point`、`verdict` 僅為屬性，不入身分。
  - `round start --incident INC --expect N --strategy S --authority A`：須 `N==已開輪數`、無進行中輪、已開輪數 <3、incident open 且非 pending-human；`--strategy`、`--authority` 非空；策略（正規化後）不得與同 incident 先前各輪相同。`round end --incident INC --result pass|fail`：每輪只能結束一次；`pass`→incident 結案（不要求湊滿）；`fail` 計入 fail。**第 3 輪 fail 後禁止第 4 輪**。
  - `step-limit --incident INC --source NAME`：寫 `step_limit`，**不開輪、不重置、不改輪數、不觸發升報**（含 `CHECKPOINT_REPORT_FIX_LIMIT` 之記錄；rounds=2 時其後 `round start`（新策略＋授權）仍被接受）。
  - `consult --incident INC --kind local|advisor`；`strategy-scan --incident INC --item "<策略>::<排除依據>"`（可重複；每項兩段皆非空）。helper **只檢查非空**，不搜尋策略、不判斷真偽。
  - `human --category C [--incident INC] --basis B`：寫 `human` 事件，使該 incident 進入 pending-human，並印出恰一行 `HUMAN_REQUIRED: <類別> | <ticket> | <incident|none> | round=<n>/3 | <basis>`。前置：`EXHAUSTED` 須該 incident 已 3 輪全 fail 且 `--basis rounds_3`（rounds<3 一律拒絕，不論有無 strategy-scan）；`NO_AUTHORIZED_STRATEGY` 須 rounds<3、且該 incident 已有 `consult local`＋`consult advisor`＋至少 1 筆 strategy-scan；驗證先行類（`SSOT_CONFLICT`／`PRODUCT_DECISION`／`SCOPE`）須已有 `consult local`＋`consult advisor`；立即類（`PERMISSION`／`IRREVERSIBLE_SECURITY_COST`／`HUMAN_GATE`／`COST`）直接接受（無 incident 時 `incident` 欄為 `none`）；`NO_PROGRESS` 須 ledger 已有 ≥1 筆 `blocks`（連續門檻屬 T04 hook，helper 不判斷）。未列舉類別 → exit 2。
  - `human resolve --ref R`（`R` 非空，記 Akira／裁決引用）：清除 pending-human；**對 `EXHAUSTED` 拒絕**（exit 5；EXHAUSTED 後續跑屬 `COST` 恢復門，T03 不定義），且 resolve **不提高**任何 incident 的 3 輪上限。
  - `blocks --note N`、`missed-check --gate-point P`：僅寫事件；**不改輪數、不改 incident 狀態**（供 T04 hook）。
  - `ppss record` / `ppss read`：見 AI-4。
- [x] 完成：incident／round／step-limit／consult／strategy-scan／human（含 resolve）／blocks／missed-check

### AI-4: PPSS 證據記錄
- **檔案**: `.claude/scripts/gate_ledger.py`
- **現值**: （新檔，無）
- **目標值**（沿用既有 artifact 形狀 `{request_id, captured_at, queries, results[{title,url,snippet,date,last_updated}]}`；helper **不呼叫 PPSS、不連網**）：
  - `ppss record --incident INC --artifact PATH [--domains d1,d2] [--new-query-strategy S --reason R]`：讀 artifact JSON；缺檔／無法解析／`results` 為空／（有 `--domains` 時）無任何 `results[].url` 網域屬範圍 → `INVALID`；否則 `RETRIEVED`（僅代表取回，`evidence_ok=false`）。**提交窗**＝自該 incident 最近一次 `round end`（或 incident 開啟）起；窗內第 2 次 `ppss record` 若缺 `--new-query-strategy`（**查詢策略**，非修復策略）或 `--reason` → 拒絕；附齊則接受並記錄，**`rounds` 不變、不開新輪**。
  - `ppss read --incident INC --request-id RID --url U --source-file F --supports <claim_id|none> [--quote Q]`：`U` 須屬該 request 之 `results[].url`（否則拒絕）；記 `url`＋`sha256(source 文字)`（不存全文）；`--supports none` → `SOURCE_READ` 且 `evidence_ok=false`（**已讀但離題**）；`--supports <claim_id>` 須同時附 `--quote Q`，且 `Q` 須為該 source 文字之**子字串**，否則拒絕；通過 → `SOURCE_READ` 且 `evidence_ok=true`。
  - **語意界線（Hard Constraint 引用）**：`evidence_ok=true` 僅表「來源閱讀與支持聲明可追溯」；helper 不判斷聲明真偽；`SOURCE_READ` 單獨不得使 `evidence_ok=true`；不使用任何相關性分數；不沿用 `trigger_rules: ["6b1"]`（本 helper 不涉及 `trigger_rules`）。
- [x] 完成：`ppss record`／`ppss read`（離線，無網路／PPSS 呼叫）

### AI-5: check 入口檢查與 receipt
- **檔案**: `.claude/scripts/gate_ledger.py`
- **現值**: （新檔，無）
- **目標值**:
  - `check <gate_point> [--late]`，`gate_point ∈ {qa, commit, sv, closeout, done, resume}`（其餘 exit 2）。mutation 規則（AI-2 身分與適用性核對）適用——`ticket_location` 非 `loc:pending` 之票 `check` 被拒（exit 4）。
  - 結果：該票存在 pending-human incident（`EXHAUSTED`／`NO_AUTHORIZED_STRATEGY`／其他已 `human` 且未 resolve）→ **exit 1**，並印出待人工摘要；否則 **exit 0**。
  - 每次呼叫（含 exit 1）寫一筆 `check` receipt：`gate_point`、`ticket`、`ticket_source`、`head`（呼叫時 `git rev-parse HEAD` 全 sha，取不到記 `null`）、`ts`、`exit`、`late`（`--late` 時為 true）。**receipt 綁定欄位僅用既有識別（HEAD sha、時間），不新增事件 ID 機制**；`--late` 由呼叫者宣告，helper 不判斷新鮮度（新鮮度判準屬 T04）。
  - `status` 輸出（供 T02 `resume` 與 T04 hook 消費）為單一 JSON 物件（`--json`）或人類可讀摘要：ticket、`ticket_location`（`loc:*`，與 AI-2 同一組字面）、open／closed incidents（含輪數、pending-human 類別）、最近 receipts、blocks／missed_check 計數。
- [x] 完成：`check`＋receipt、`status --json`

### AI-6: 單元測試（stdlib unittest，隔離暫存環境）
- **檔案**: `.claude/scripts/tests/test_gate_ledger.py`（新增；`python -m unittest .claude/scripts/tests/test_gate_ledger.py` 或等效）
- **現值**: （新檔，無）
- **目標值**: 涵蓋 Acceptance Criteria 全部 `[行為性]` 案例。**所有 git 操作僅在 `tempfile` 建立之暫存 repo 內**（`git init`、`git worktree add` 亦只加在該暫存 repo）；**測試不得讀寫真實 repo 的 `.git`、不得觸碰真實第二 worktree 或任何 Git 設定、不得呼叫 `tools/cleanup_expired_tmp.py` 的預設 `TARGETS`**。並行案例用真實獨立 process（`subprocess`／`multiprocessing`）。PPSS **單元**案例離線執行（不呼叫 `research_submit`、不連網），使用內嵌之最小 fixture（形狀同既有 artifact）；另以 `skipUnless` 讀取 `D:\DevSources\workflow\mcp-ticket-platform\data\research\2026-10\rsch-20261011-66f685d4c6.json`（唯讀，離題負向樣本）——不存在則 skip 並於收尾實錄註明。
- [x] 完成：`test_gate_ledger.py` 33 案例全綠（`python -m unittest discover -s .claude/scripts/tests -p test_gate_ledger.py`）

### AI-7: 驗證與收尾證據
- **檔案**: （無修改；票檔收尾實錄）
- **動作**: 實跑測試並將 **完整輸出**（含通過數、skip 明細）貼入收尾實錄；回填 `[可達性]` AC 之 `檔案:行號`；填寫覆蓋矩陣中**與 T03 相關列**（check 程式結果、PPSS 使用、並行 session／worktree、提前升報）的實測現況；列出 `.git/gate_probe_T03/` 仍保留。**PPSS 真實鏈實跑**（AC 之 executor 驗收）：貼入實跑輸出（`research_submit` 回傳之 `request_id`／`artifact_path`、所讀引用來源 URL 與讀取方式、`ppss record`／`ppss read` 輸出、ledger 對應事件），並填**驗收／未驗狀態欄**（`PASS-chain` 或 `UNVERIFIED`＋原始失敗）；`UNVERIFIED` 時明示未驗範圍，不宣稱真實鏈已驗證。Cross-Review 走 `cross_review.sh` 現役入口，收尾實錄註明「payload 審查，非 reviewer 獨立執行測試」，並記錄實際 Codex 結果（含 sandbox／model 錯誤原文，若發生）；**不預填 PASS**。收尾**不得**宣稱「完整 Gate」或等義語。
- [x] 完成（見「收尾實錄」）

## Hard Constraints
1. **範圍**：僅 ALLOWED 兩檔；不改 hooks／settings／rules／skills／既有 scripts／`PROGRESS*`／registry；不實作 hook、不改 consumer（T02／T04 範圍）；`git diff --name-only` ⊆ ALLOWED。
2. **無 registry、無網路、無平行系統**：helper 不呼叫、不讀、不寫任何 registry 檔或 registry 工具，Gate 類別字面不得出現於任何 registry 寫入參數（D-ENUM 不外溢條件①）；不新增資料庫、daemon、通用事件平台、第三方依賴；**helper 與單元測試**不呼叫 PPSS／Advisor、不連網（executor 驗收階段的一次真實 PPSS 實跑由 agent 發起、不經 helper 呼叫 PPSS，見 AC）。
3. **身分非授權**：`--ticket` 僅選擇 ledger key；不寫 `active.json`／`verify_cmd.conf`；無 ticket 不得 fallback `unknown`；`ticket_location` 非 `loc:pending`（deliver／void）之票不得 mutation（保守可用性判斷，非權威 lifecycle）。
4. **載體邊界**：只讀寫 `<common-dir>/gate/<TICKET_ID>.jsonl(.lock)`；`.git/gate_probe_T03/` **原狀保留、不讀寫、不刪除**；測試只用暫存 repo；不改第二 worktree 受版控檔案或 Git 設定。
5. **重試邊界（S1）**：暫態 `PermissionError` 重試**僅限 acquire 階段的鎖檔 `os.open`**，必須有 timeout 並保留原始錯誤；其他位置的 `PermissionError`、以及工具／classifier／permission 層對 helper 呼叫的拒絕，**不重試、不換語法／路徑／工具**；被拒即依 Error Handling 停止。
6. **Windows 存活判斷**：禁用 `os.kill(pid, 0)`（Python 3.14 官方契約：Windows 上非 `CTRL_C_EVENT`／`CTRL_BREAK_EVENT` 的 sig 以 `TerminateProcess` 終止目標，不可作存活查詢）；存活判斷用 `OpenProcess`＋`GetExitCodeProcess`，POSIX 分支用 `os.kill(pid, 0)` 不變；回收鎖須「鎖齡逾時 **且** owner 確認不存活」，存活或不可判定一律不回收。
7. **append-only、fold 計數**：無就地改寫；輪數唯一依據為 `round` 事件；`step_limit`／`blocks`／`missed_check`／`finding`／`ppss` 不改輪數；hook 類事件不得增加解題輪。
8. **語意不越界**：helper 只檢查結構、紀錄與轉態，不判定根因、策略新穎性、授權真偽或 PPSS 聲明真偽（agent 自證、可稽核不可阻擋）；`evidence_ok` 文義限「可追溯」。
9. **exit code 固定並寫入 Usage**：0 成功；1 `check` 有待人工；2 用法錯誤；3 身分無法解析；4 票檔落點非 `loc:pending`（deliver／void）、不存在或矛盾；5 契約拒絕（CAS 不符、策略重複、第 4 輪、前置不足等）；6 ledger IO／lock 失敗。
10. **完整性宣告律**：收尾實錄只陳述本票實際交付與 T03 相關矩陣列實測現況；不得宣稱「完整 Gate」；未覆蓋項列明，揭露不構成已覆蓋。
11. **EOL／編碼**：新 `.py` 不得依賴 CRLF／LF 差異（`.py` 無 eol 屬性，工作樹可能為 CRLF）；stdout／檔案 I/O 明示 UTF-8。
12. **不取號、不開他票**：不新增、不修改其他票檔或冊面；遺留 `.git/gate_probe_T03/` 之清理屬 Akira 後續精確批准，不由本票處理。

## Error Handling
- AI-0 任一錨點不符 → 依 TPL v3.11 三分流分類處置（見 AI-0）；行號漂移、措辭微變 → 只重讀補證，不停止。
- Gate 節詞彙與本票規則不一致，或 `ADV-GATE-02` 被**另一張不同票**重號、或冊面 `file` 與票檔實際落點不一致 → 停止回報（本票自身存在不算占號）。
- PPSS 真實鏈實跑不可達（`research_submit` 失敗／無回傳／來源無法讀取等）→ 記原始失敗輸出，該 AC 標**未驗**、不勾 PASS；**不阻擋 helper 本體完成**；依 Akira 2026-10-07 裁決以「未驗」收束本票，不等待人工接受；收尾實錄明示未驗範圍，**不宣稱真實鏈已驗證，不自動另開修補票**。額度已用盡（見 AC）仍失敗 → 同樣收束，不發第三次。
- 寫入 `<common-dir>/gate/` 被權限／classifier 拒絕 → **停止**，記為 `HUMAN_REQUIRED(PERMISSION)`（若 helper 已可用則走其 `human` 子命令，否則文字回報）；**不改道、不換語法／路徑／工具重試同一被拒動作**；可繼續其他已授權工作（如暫存 repo 內測試）。
- 暫存 repo／`git worktree add` 在測試環境無法建立 → 記錄具體錯誤，該案例標未驗；**不得改用真實第二 worktree 寫入**。
- 真實第二 worktree 的選配唯讀檢查（`status` 於 `~/.codex/worktrees/…` 執行，不建立任何檔）被拒或不可達 → 記為「真實第二 worktree 未驗」，以暫存 linked worktree 測試為準；不重試、不阻擋本票。
- `cleanup_expired_tmp.py` 之 audit 輸出落點無法導向暫存目錄 → 該 AC 改為 `--dry-run`＋`TARGETS` 靜態斷言（不真跑刪除），並記錄；`session_start.sh` 複本無法於暫存 repo 執行 → 改以「刪除 `verify_cmd.conf`＋ledger 位元組不變」模擬並記錄。
- 實作期發現需要 registry 讀寫、hook 改動或 consumer 改動才能達成 AC → 停止回報（`STOP_REPLAN_REQUIRED` 性質），不擴 ALLOWED。
- 測試持續失敗且連續 2 次修復仍失敗 → 停止回報（步驟級上限；依 Gate 記為 `step_limit`，不自行升報）。
- `git check-ignore -v <ALLOWED 標的>` 命中（標的被 `.gitignore` 排除）→ 停止回報（gitignore 死結律）。

## Acceptance Criteria
- [ ] **[存在性]** 兩個 ALLOWED 檔存在；`python -m py_compile .claude/scripts/gate_ledger.py` 通過；檔頭 Usage 註解列出全部子命令（`status incident round step-limit consult strategy-scan ppss human blocks missed-check check`）、旗標與 exit code 0–6；僅 import stdlib；`grep -nE "registry|TICKET_REGISTRY|requests|urllib|http" gate_ledger.py` 於可執行程式碼（非 Usage／註解字面說明「不使用」）零命中；`git diff --name-only <DIFF_REF>` ⊆ ALLOWED；`.git/gate_probe_T03/` 仍存在且內容與開工時相同。 〔狀態：**未勾（豁免）**——除「`.git/gate_probe_T03/` 內容與開工時相同」外各子項已驗（`py_compile` 通過、Usage 完整、僅 stdlib、`git diff --name-only c3a71f16..0452a579` ⊆ ALLOWED）。該子項：**無開工基線、無法驗證**；Akira 2026-10-13 明示接受／豁免，不勾成實測 PASS，不以目前雜湊回填；僅確認該目錄與 `T03PROBE-ZZ-99.jsonl` 仍存在〕
- [x] **[可達性]** producer／consumer 證據欄（執行後回填 `檔案:行號`）：`active.json` writer＝`activate_ticket.sh`＿＿、`verify_checklist.sh write_active_json`＿＿；reader＝`session_start.sh`＿＿與 helper `resolve_ticket()`＿＿；`cleanup_expired_tmp.py TARGETS`（不含 `.git`）＿＿；ledger 消費面＝Usage 內 `status --json`／`check` 契約＿＿（T02／T04 之 consumer）。 〔證據：`activate_ticket.sh:187`／`verify_checklist.sh:55`（writer）；`session_start.sh:15`、`gate_ledger.py:137,148`（reader／`resolve_ticket()`）；`cleanup_expired_tmp.py:20-25` `TARGETS` 不含 `.git`；ledger 消費面＝`gate_ledger.py` 檔頭 Usage 與 `cmd_status`(:503)／`cmd_check`(:787)〕
- [x] **[行為性]** **輪的定義**：given 一個 incident 先記 `finding`、再記兩次 `step-limit`：expect `rounds=0`；given `round start` 缺 `--strategy` 或 `--authority`、或策略與先前輪（正規化後）相同：expect 被拒（exit 5）且無事件寫入。 〔證據：`test_round_definition`〕
- [x] **[行為性]** **跨 gate point／verdict 續算**：given 同票同 subject 在 `qa`、`sv` 兩個 gate point 各 fail，且 verdict 由 `REPORT_FIX` 變 `BLOCKER`：expect 視為同一 incident、輪數延續；第 3 輪 fail 後 `human --category EXHAUSTED --basis rounds_3` 被接受並印出恰一行 `HUMAN_REQUIRED: EXHAUSTED | …`，隨後第 4 次 `round start` 被拒。 〔證據：`test_cross_gate_point_and_exhausted`〕
- [x] **[行為性]** **分開需獨立證據**：given 已有 open incident，`incident open` 新 subject 且無 `--independent-evidence`：expect 被拒並改 attach；附證據且 subject 無交集：expect 建新 incident，且兩 incident 各自 3 輪獨立計。 〔證據：`test_independent_evidence`〕
- [x] **[行為性]** **第 3 輪不被截斷、不湊滿**：given rounds=2 且已記 `step-limit`（source＝`CHECKPOINT_REPORT_FIX_LIMIT`）：expect `round start`（新策略＋授權）仍被接受；given 第 1 輪 `pass`：expect incident 結案、不要求後續輪。 〔證據：`test_third_round_not_truncated_and_pass_closes`〕
- [x] **[行為性]** **EXHAUSTED 僅限三輪**：given rounds<3：expect `human --category EXHAUSTED` 被拒（不論有無 strategy-scan、不論 basis）；`human resolve` 對 `EXHAUSTED` 被拒，且不提高上限。 〔證據：`test_exhausted_only_after_three`〕
- [x] **[行為性]** **NO_AUTHORIZED_STRATEGY**：given rounds=2 fail、incident 已有 `consult local`＋`consult advisor` 與 ≥1 筆非空 strategy-scan：expect `human --category NO_AUTHORIZED_STRATEGY` 被接受；缺其中任一（或 strategy-scan 項任一段為空）：expect 被拒；helper 原始碼無策略搜尋／判斷邏輯。 〔證據：`test_no_authorized_strategy`〕
- [x] **[行為性]** **載體存活（暫存環境）**：given 暫存 repo 已有 ledger，且 `tmp/verify_cmd.conf` 被刪、`tmp/verify/active.json` 保留、以 `session_start.sh reset` 及 `inject`（腳本複本、cwd＝暫存 repo）各跑一次、`cleanup_expired_tmp.py` 複本（`TARGETS` 限定暫存 `tmp/.tmp`，含 mtime 逾 7 天舊檔）實跑一次：expect ledger 檔位元組不變、計數不變、舊 `tmp/` 檔被清而 ledger 未被觸及。 〔證據：`test_reset_inject_and_cleanup_leave_ledger`（暫存 repo；`TARGETS` 限定暫存目錄）〕
- [x] **[行為性]** **並行（真實多 process）**：given 8 個並行 process 同時 `round start --expect 0`（同 incident）與各 append 多次 `blocks`：expect 恰 1 勝者、其餘 exit 5；無事件遺失、`seq` 嚴格單調無重複；`blocks`／`missed-check` 不改輪數。 〔證據：`test_eight_processes_one_round_winner`〕
- [x] **[行為性]** **lock 暫態重試與負向（S1）**：given acquire 階段 `os.open(lock…)` 先拋若干次 `PermissionError` 後恢復：expect 重試後成功；given 同一 `PermissionError` **持續**（測試以 `LOCK_TIMEOUT=0.5s`）：expect 在約 timeout 內以 `LockTimeout`／exit 6 結束（**非無限等待**），訊息含原始錯誤類別與 errno／winerror，`__cause__` 保留，ledger 無部分事件；given `PermissionError` 發生於建立 `gate/` 或 append：expect 立即失敗、**不重試**。given 鎖檔 owner＝存活子程序且鎖齡逾 `STALE_SECONDS`：expect **不被回收**（鎖檔仍在、等待至 timeout 後失敗）；given owner pid 已結束且鎖齡逾期：expect 被回收並成功寫入；given 鎖齡未逾期的存活 owner：expect 不回收。 〔證據：`LockCases` test_1／2／2b／3／3b／4／ownerless＋E1 四案〕
- [x] **[行為性]** **身分（S2；落點標記 `loc:*` 與 AI-2／AI-5 同字面）**：given `active.json`＝票 A（Pending）且 `--ticket B`（Pending）：expect 事件記於 B、receipt `ticket_source=explicit`、`active.json`／`verify_cmd.conf` 位元組不變；given 無 `--ticket` 且無 `active.json`：expect mutation exit 3、`gate/` 下**不產生任何檔**、無 `unknown*`；given ticket 字串為 `../x`、`a/b`、小寫或無數字：expect exit 3；given 票檔在 `deliver/` 或 `void/`（`ticket_location`＝`loc:deliver`／`loc:void`）：expect 所有 mutation（含 `check`）exit 4 且無事件，而 `status --ticket` 於已有 ledger 時 exit 0 並輸出對應 `ticket_location`；given 票檔三處皆無且無 ledger：`status` exit 4（`loc:none`）；given 票檔同時存在於兩處（`loc:multi`）：mutation exit 4；given `active.json` 指向 `ticket_location` 非 `loc:pending` 之票且無 `--ticket`：mutation exit 4、`status` 標示 `stale`。上述為票檔落點之保守可用性判斷，測試不斷言權威 lifecycle，亦不讀 Registry。 〔證據：`Identity` test_explicit_over_active_and_no_activation／no_ticket_and_invalid_ids／locations／stale_active_json〕
- [x] **[行為性]** **common-dir／worktree（S3，暫存 repo）**：given 暫存 repo＋其 linked worktree，於主 checkout 與 linked worktree 各對同票 `incident open`／`blocks`：expect 兩處解析到**同一絕對 ledger 路徑**、`seq` 單調無分裂；票檔存在性由 linked worktree 亦能解析至主 checkout；`git status --short` 於兩處皆為空（ledger 在 `.git` 內）。選配（失敗分支見 Error Handling）：於真實第二 worktree 執行唯讀 `status`，expect 解析之 ledger 路徑與主 checkout 相同且不建立任何檔。 〔證據：`test_worktree_shares_ledger`（暫存 repo＋linked worktree）通過；選配「真實第二 worktree 唯讀 `status`」**未執行（未驗）**，不影響本 AC 主體〕
- [x] **[行為性]** **PPSS 每輪一次與證據（S5）**：given 同提交窗內第 2 次 `ppss record` 缺 `--new-query-strategy`／`--reason`：expect 被拒；附齊：expect 接受且 `rounds` 不變；given `results` 非空、網域屬範圍、無 `ppss read`：expect `RETRIEVED` 且 `evidence_ok=false`；given `ppss read --supports none`（已讀但離題）：expect `SOURCE_READ` 且 `evidence_ok=false`；given `--supports <claim_id> --quote Q` 且 `Q` 為 source 文字子字串：expect `evidence_ok=true`（測試**不**斷言語意為真）；given `Q` 非子字串或缺 `--quote`：expect 被拒；given 空／缺檔／網域不符：expect `INVALID`；**負向 fixture 二則**：①既有離題 artifact `rsch-20261011-66f685d4c6`（若存在）未讀 → `evidence_ok=false`；②同一結果某筆引用來源已讀但內容不支持所問問題（`--supports none`）→ `evidence_ok=false`。**此單元案例離線：不呼叫 `research_submit`、不連網。** 〔證據：`Ppss` 4 案（含既有離題 artifact 負向樣本）〕
- [x] **[行為性]** **PPSS 真實鏈實跑（executor 驗收；復原 plan T03 AC 之「實跑」項；Rev A）**：given 單元測試與 helper 本體完成後，由 **agent**（非 helper、非單元測試）以結構化 `filters.domains` 發起**一次**真實 `research_submit`（真實外部事實題，約 $0.005）→ 實際讀取其中**一個**引用來源（`WebFetch`／`Read`）→ 以 `ppss record`／`ppss read` 寫入 ledger → 核對可追溯記錄（`request_id`、來源 URL、段落 `sha256`、`RETRIEVED`→`SOURCE_READ`、`evidence_ok`）：expect 鏈走通且如實記錄即 PASS，`evidence_ok` 為 `true` 或 `false` 皆可 PASS；來源內容不支持所問問題時記 `--supports none`，**不得為湊 `evidence_ok=true` 而改題或改引文**。**額度與截斷序**：預設僅一次；第二次須附新查詢策略（`--new-query-strategy`）與理由（`--reason`）且不開新輪；**不發第三次**。**PPSS 不可達**：記原始失敗輸出，本 AC 標**未驗**、不勾 PASS，不阻擋 helper 本體完成；依 Akira 2026-10-07 裁決以「未驗」收束本票，不等待人工接受，收尾實錄（AI-7 狀態欄）明示未驗範圍、不宣稱真實鏈已驗證、不自動另開修補票。 〔驗收狀態：**PASS-chain**（`evidence_ok=false`，如實記錄）。`rsch-20261012-f471f5f0a2`（一次 `research_submit`，`filters.domains=["docs.python.org"]`，$0.005；首次呼叫因 `justification` 型別驗證錯誤於送出前被拒、未成立請求）→ 經 `WebFetch` 讀 `https://docs.python.org/3.14/library/os.html`（前 100000／255648 字元，為 WebFetch 處理後回答，非原始頁面）→ 本次取得的局部處理後文字（WebFetch 前 100000／255648 字元之處理後回答）未支持該問題，不斷言完整官方頁不存在該內容 → `ppss read --supports none`；ledger `INC-001` seq 3–4；詳見收尾實錄〕
- [x] **[行為性]** **`check` 程式結果與 receipt**：given 有 `EXHAUSTED`／`NO_AUTHORIZED_STRATEGY` 或其他未 resolve 之 pending-human incident：expect `check <gate_point>` exit 1 並印出摘要；否則 exit 0；每次呼叫寫一筆含 `gate_point`、`ticket`、`ticket_source`、`head`、`ts`、`exit`、`late` 的 receipt，`--late` 時 `late=true`；receipt 不含新增事件 ID 欄位；`gate_point` 不在六值內 → exit 2。 〔證據：`test_check_and_receipt`〕
- [x] **[行為性]** **輸出編碼**：given 以 cp1252 預設編碼環境（`PYTHONIOENCODING` 未設）執行含中文 `--strategy`／`--item` 的 `round start`／`status`：expect 不拋 `UnicodeEncodeError`，ledger 內為合法 UTF-8。 〔證據：`test_utf8_output_under_legacy_codepage`〕
- [x] **[存在性]** `python -m unittest` 全綠；完整輸出（通過數、skip 明細）貼入收尾實錄；收尾實錄含 Cross-Review 實際結果原文（payload 審查、非獨立執行；不預填 PASS）、`.git/gate_probe_T03/` 保留聲明、T03 相關覆蓋矩陣列實測現況與未覆蓋項，且不含「完整 Gate」宣稱。 〔證據：39 tests OK（`unittest discover`，約 31.9 s；完整輸出見收尾實錄）；Cross-Review 第 2 次 PASS（`.tmp/cross_review_result_20261013_021657.md`，payload 審查，非 reviewer 獨立執行）；收尾實錄無「完整 Gate」宣稱〕
- ~~`assembleDebug` PASS~~ (N/A: 無 `app/` 變更，僅 `.claude/scripts/` 新增檔)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 〔證據：`git diff --name-only c3a71f16..0452a579` 僅兩個 ALLOWED 檔〕


## 收尾實錄（Agent closeout，2026-10-13）

- **交付**：`cc541546`（初版）＋`0452a579`（fix-forward，G1／G2／尾行保護）；`.claude/scripts/gate_ledger.py`（925 行）、`.claude/scripts/tests/test_gate_ledger.py`（783 行）；相對 `c3a71f16`：2 files、1708 insertions。ALLOWED 未擴。
- **unittest**：`python -m unittest discover -s .claude/scripts/tests -p test_gate_ledger.py -v` → 39 tests OK。涵蓋：RoundsAndHuman 9、Identity 5、LedgerTail 2、Concurrency 1、CarrierSurvival 1、LockCases 15（含 E1 四案、G1 三案、G2 一案）、Ppss 4、StaticChecks 2。票面「`python -m unittest <path>`」寫法在本環境 `ValueError: Empty module name`，改用 `unittest discover`（票面「或等效」）。
- **E1**：`GetExitCodeProcess` 回傳非零才依 code 判斷（259 存活、其他已死）；回傳零＝不可判定、保存 last error、不回收；`OpenProcess`／`GetExitCodeProcess`／`CloseHandle` 皆宣告 argtypes／restype，handle 於 `finally` 關閉（`test_e1_*`）。
- **E2**：驗收 incident 開輪後未失敗，`round end --result pass` 結案；開輪後失敗分支**未被觸發、未實測**。
- **lock 四案**：①暫態 `PermissionError` 重試後成功；②持續拒絕於 timeout 內以 `LockTimeout` 結束、保留原始錯誤與 `__cause__`；③存活 owner 之過期鎖不被回收；④死 owner 之過期鎖被回收——皆 PASS。
- **G1／G2／尾行保護（fix-forward，依 Akira ack）**：非正 pid 不呼叫 `_pid_state`／`OpenProcess`／`os.kill`，逾期鎖依既有規則回收；`OpenProcess` NULL 映射 87→已死、5／其他→不可判定且無 handle 不呼叫 `CloseHandle`；`Ledger.read(strict)` 遇未以換行結尾或中段損毀的 ledger，mutation exit 6、檔案位元組不變（不 truncate、不恢復），`status` 仍可讀。
- **PPSS 真實鏈：PASS-chain，`evidence_ok=false`**：`rsch-20261012-f471f5f0a2`（一次 `research_submit`，`filters.domains=["docs.python.org"]`，約 $0.005；另有一次因 `justification` 型別驗證錯誤於送出前被拒，未成立請求）→ `WebFetch` 讀 `docs.python.org/3.14/library/os.html` 前 100000／255648 字元（處理後回答，非原始頁面）→ 本次取得的局部處理後文字（WebFetch 前 100000／255648 字元之處理後回答）未支持該問題，不斷言完整官方頁不存在該內容 → `--supports none`。**未驗範圍**：未讀該頁其餘 155648 字元，亦未讀其餘四筆引用；`evidence_ok=false` 僅表示本次局部處理後文字未支持該問題，不代表 `O_EXCL` 語意已查明，亦不代表完整官方頁不含該內容。
- **驗收 incident 最終狀態**（`status --json`，ledger `<common-dir>/gate/ADV-GATE-02.jsonl`，`last_seq=5`）：`INC-001` `status=closed`、`rounds_started=1`、`rounds_failed=0`、`round_open=false`、`pending_human=[]`、`blocks=0`、`missed_checks=0`、receipts 空；`ppss` 一筆 `SOURCE_READ`／`evidence_ok=false`。
- **Cross-Review**：第 1 次（`c3a71f16..cc541546`）Codex `REPORT_FIX`（`REPORT_FIX_SOURCE=codex`，exit 3；兩點皆為報告措辭，已修；該次 Codex shell 子程序 `setup refresh had errors`，依 payload 判讀）；Akira ack 後第 2 次（`c3a71f16..0452a579`，`R1`，`gpt-5.6-sol`）**PASS**（exit 0；`Guard Input Reachability: 有新 Guard — State: active.json — Producer: (B)`；`REACHABILITY: NONE`；結果檔 `.tmp/cross_review_result_20261013_021657.md`）。第 2 次日誌中未出現 `exec_command failed`。此為 payload 審查，**非 reviewer 獨立執行測試**。`structural_verify --post` PASS (A–G) [ADVISORY：`PROGRESS.md` 80 行，與本票無關]。
- **未勾項與限制**：AC1 子項「`.git/gate_probe_T03/` 內容與開工時相同」**無開工基線、無法驗證，Akira 2026-10-13 明示接受／豁免**（不勾 PASS、不以目前雜湊回填，G3）；選配「真實第二 worktree 唯讀 `status`」**未執行**；本 helper **尚無 consumer**（T02 入口 `check` 呼叫、T04 Stop hook 未實作），**不得宣稱「完整 Gate」**；T02／T04／T05 待後續。覆蓋矩陣中僅「check 程式結果」「PPSS 使用」「並行 session／worktree（暫存 repo）」「提前升報前置檢查」有本票測試證據。
- **審查邊界**：Cross-Review 為 payload 審查，非 reviewer 獨立執行測試；不再跑第三次 Cross-Review，不新增測試或修補票（Akira 2026-10-13）；executor 維持 `REVIEW`，`CLOSED` 由審查方依既有通道終審。
- **保留物**：`.git/gate_probe_T03/` 原狀保留（未取得精確清理批准）；真實 ledger `gate/ADV-GATE-02.jsonl` 保留。
- **DEFERRED-TO-DOCSYNC（不開票）**：`PROGRESS.md`／`PROGRESS_ARCHIVE.md` 同步與 Done 標記留待下一輪 `/compact`。
- **Traycer**：`Traycer Workflow: N/A` → `reviews.traycer=N_A`。External-review decision 維持 `EXECUTED`（`rsch-20261012-9506410da5`，僅 `os.kill` 題獲官方命中），本次收尾未再外審。
- **補記（SKAD 2026-10-13 02:00 findings G4／G5／G7，如實）**：G4 驗收 incident 的 subject 實為 `ppss:acceptance-run`（開工指示為 `ppss-chain-acceptance`；ledger append-only，不改寫），且實際順序為 `incident open`／`round start` 先於 `research_submit`（指示為先取得可讀來源再開 incident）；結果上 incident 已閉環、無殘留 open。G5 `ppss_read` 的 `source_sha256` 為 WebFetch **處理後文字**之雜湊（來源檔 sha256 `7f8a6396…`），不可重現原頁。G7 真實已死子程序的存活判斷路徑（本機 Python 3.14.5 win32 實測）：子程序物件仍持有 handle 時走 `GetExitCodeProcess` → `exit code 0` → 已死；handle 釋放後走 `OpenProcess` NULL → `last_error=87` → 已死；兩條路徑皆回 `False`。
