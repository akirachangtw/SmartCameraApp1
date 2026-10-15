# ADV-OWNER-01: 票務 Stop guard 僅對 active.json 登記的 owner session 作用（owner 寫入與保留、兩 hook 歸屬核對、session_start 非阻斷狀態提示），並移除跨 session／跨 hook marker 清理

**Revision (Rev A.1)**: 2026-10-15 派工前同步（審查核可派工；不回 plan、不新增 `### AI-` 標題〔仍 AI-0～AI-7〕）。變更：①External-review 6a 改記重用已執行研究 `rsch-20261015-4bf08abf30`＋官方來源抽驗，刪除「不送 PPSS」措辭，補「官方契約非本地品質 PASS／probe 待執行／MCP 子行程原始 ID 限制不套用 Bash·hook」；②`session_start.sh` 唯讀約束限縮於新增提示函式，驗收改為「新增提示未增加或改變既有合法副作用」，保留 `active.json`／ledger／Registry 保護斷言，不以 `tmp/` 全不變為新要求（AI-3、AI-6.4、Hard Constraints 3·7、對應 AC）；③AI-7 補 `test_preflight_adapter.sh` 執行端隔離：保存／trap 恢復原始 `active.json`（內容與存在狀態）並讀回確認，不以 `git status`／重跑 activation 代替。

**簽發日期**: 2026-10-15
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（ADV-LAYER-01 已 `CLOSED`，僅為問題來源，不再追加其紀錄）
- `evidence_dependency`: ADV-LAYER-01（`deliver/ADV-LAYER-01_budget.md` 取證附註，commit `9e9ca024`：NO_PROGRESS seq 9–14 來源與兩支 hook 無 owner 核對之取證；僅作問題來源，本票錨點皆 live 直查）
- `coordination_dependency`: 無（plan-local T02〔R0 回件承接，尚無正式票號〕與本票無檔案重疊，僅沿用改後 hook 測試 fixture；本票與任何編輯 `activate_ticket.sh`／`verify_checklist.sh`／`session_start.sh`／`gate_stop_guard.py`／`registry_review_stop_guard.py` 之票須串行，`concurrency.md` §2）

## 檔案治理／來源說明

- **Parent**：Plan-backed 單票。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261015-stopguard-owner-plan.md`（Rev B；`.tmp` 非長期載體，本節使本票獨立成立）；Parent artifact status＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`；plan-local item＝`PLAN-ITEM-01`；Parent read timestamp＝2026-10-15 13:53（+08:00）。
- **Template 實讀**：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，產出日期 2026-05-29，最新 Revision 日期 2026-09-14（全檔 322 行實讀）。
- **票號**：`ADV-OWNER-01`（OWNER＝ticket owner）。本機機械取號：registry（`TICKET_REGISTRY.json`／`TICKET_REGISTRY_ARCHIVE.json`）與 `Pending/`、`deliver/`、`void/` 對 `ADV-OWNER` 前綴 grep 零命中；`reports/` 目錄不存在；`ADV` family 已於 `ticket_pattern.md` 補登，topic `Advisor`（既有）。
- **Selected item 原文（PLAN-ITEM-01 goal）**：兩支票務 Stop guard 按 `active.json.owner_session_id` 作用，並不再跨 session／hook 刪標記。producer（owner 寫入／保留）、consumer（兩 hook 與 session_start 完整性校驗）、清理三者互鎖：單獨交付 owner 欄而不改 hook 無觀察效果，單獨改 hook 而無 owner 欄則全體 fail-open，故單票。
- **相關約束（取自 parent，Akira 2026-10-15 裁決）**：
  - **D1-1**：缺有效 owner、owner 為空、或無法確認當前 session 身分 → 兩 hook 放行（fail-open）：不阻擋、不累計 blocks、不寫 pending／ledger、不要求轉態、不自動認領。僅作用於票務 Stop guard，不放寬其他 permissions、品質或轉態授權。`verify_cmd.conf` 無 owner 同樣放行，不作歸屬退路。
  - **D1-2**：不預設 `/clear`／resume 必須重新 activation；`session_id` 未變且匹配 owner 持續有效，身分改變或 owner 缺失才需顯式 activation 恢復。
  - **D1-3**：claim 僅限明確派工／啟動入口；一般驗證、讀票、內部呼叫帶 ticket 不得覆寫 owner。
  - **D2**：移除 `evict_stale_markers` 及其呼叫，不新增 GC；非 owner 放行須在任何票務 ledger 或 marker 操作之前。
  - 非目標：不改 R0／Advisor Router、分層預算、T02、K 值、ledger 格式；**不改 `gate_ledger.py`**（其 `--ticket` 缺省解析以 `active.json` 為票號來源〔`gate_ledger.py:11,149-166`〕，非 owner session 顯式呼叫 helper 仍可寫該票 ledger——此為 Stop guard 以外的同根殘餘窗口，本票依裁決不處理，**承接者：本票收尾實錄（AI-7）如實揭露＋`/sv` Cross-Review 揭露；是否另立票由審查方裁量**）；不新增 coordinator／daemon／DB／第二套 state／人工認領流程；不清既有 ledger／pending；不重開 ADV-LAYER-01。

### 與 parent（Rev B）的差異（本票新增或變更，載於此處不以「衝突處以本段為準」概括）

| # | Rev B 原文 | 本票處置 | 依據 |
|---|---|---|---|
| 1 | 定向 live check L1（env SID＝Stop payload `session_id`）作為 ticket 階段 AI-0 驗收項，無直接證據則標 UNVERIFIED | owner 來源改記**官方契約已確認**（見證據分級 ①）；L1 marker 比對**降為補充**，不作強制 gate；**未做本地比對不記本地 PASS**；若出現具體矛盾（env 與 payload 實際不等）再定向處理 | Akira 2026-10-15 裁決；官方 `env-vars` 文件抽驗 |
| 2 | writer state matrix 僅涵蓋「新票 claim」與非 claim 各格 | **補一格同票 claim**：同票 owner A 經明確 handoff，由 SID B 以 `activate_ticket.sh` activation → owner 改為 B；B 受控、A 放行 | Akira 2026-10-15 裁決 |
| 3 | §4：`preflight_activation.sh` 為 caller，視訊號傳 claim 旗標 | 取證結論：repo 內**無**可機械識別的 preflight 派工／啟動訊號 → **不 claim**，`preflight_activation.sh` **不修改**（改列 FORBIDDEN）；`write_active_json` 恆為非 claim；claim 的唯一入口為 `activate_ticket.sh` | D1-3；本票 live 取證 |
| 4 | `session_start.sh` 僅列為「完整性校驗」consumer | **新增**最小非阻斷、唯讀 owner 狀態提示（reset／inject 共用同一函式），及 `activate_ticket.sh` 取不到 SID 時的警示；CLOSED 票不提示承接；當前 SID 不可得不得報為已確認非 owner | Akira 2026-10-15 補充裁決 |
| 5 | `session_start.sh` 的 matcher 覆蓋列為待查 | 已確認：`.claude/settings.json` 的 `SessionStart` 以 `startup\|clear` → `session_start.sh reset`、`resume\|compact\|fork` → `session_start.sh inject`；**不再待查，不修改 settings.json**（FORBIDDEN） | Akira 補充；本票 live 讀 `settings.json` L91–100 |
| 6 | 測試落點僅寫 `.claude/hooks/tests/*` | 明列兩支**新增**測試檔：`.claude/hooks/tests/session_start_owner_test.sh`、`.claude/scripts/tests/owner_writer_test.sh`（repo 內原無 `session_start.sh` 專屬測試） | 本票 live 取證 |
| 7 | UNKNOWN：`/clear` 後 session id 是否改變 | 官方 `env-vars` 文件載明 `CLAUDE_CODE_SESSION_ID` 於 Bash／hook 與 payload `session_id` 一致且 `/clear` 更新；`--resume <id>` 得到被 resume 的 ID。其餘（`--continue`／`--resume` 無明確 ID 可能得初始 ID）維持文件原述 | 官方契約 |
| 8 | （未規定）hint 在何種 lifecycle 靜默 | `REVIEW`／`CLOSED`／`VOID`（兩 hook 的 `TERMINAL`）與冊面查無／無法判定者不提示承接；此為由「CLOSED 票不提示」依兩 hook 既有 no-op 集合推導之一致延伸 | 本票 live 讀 `gate_stop_guard.py:33` 與 `registry_review_stop_guard.py:14` |

**證據分級**（evidence 2026-10-15 13:53；repo `D:\DevSources\AndroidProj\SmartCameraApp1`，main @ `37e2f37d`，工作樹 clean）：
- ① **CONFIRMED（官方契約；非本機實測，本票未做本地 env↔payload 比對，不記本地 PASS）**：`code.claude.com/docs/en/env-vars`（2026-10-15 抽驗）載明 `CLAUDE_CODE_SESSION_ID` 於 Bash／PowerShell 與 **hook command subprocess** 設為當前 session ID，「matches the `session_id` field in the hook JSON input and is updated on `/clear`」；`code.claude.com/docs/en/hooks` 載明 common input 含 `session_id`。（hooks 文件單獨不載 `CLAUDE_CODE_SESSION_ID`，以 env-vars 頁為準。）
- ② **CONFIRMED（本地原始碼，live 直讀）**：
  - `gate_stop_guard.py` `decide()` 取 `active.json` 的 `ticket_id` 後無任何 owner 核對（L438–442），之後進 `ledger_status`／`missed-check`／`persistent_block`（寫整票 `human NO_PROGRESS` 或 `blocks`）。
  - `registry_review_stop_guard.py` `main()` 無 owner 核對；`read_ticket_id()` 先讀 `active.json`、缺失時退讀 `verify_cmd.conf`；`evict_stale_markers()`（def L95）刪除 `tmp/.stop_guard/` 內不以本 session 開頭的全部檔案，呼叫於 L144。`gate_stop_guard.py` 無任何 unlink。
  - `active.json` 現為七欄；三個寫入者：`activate_ticket.sh` inline printf（L189）、`verify_checklist.sh::write_active_json`（L55–87；`/qa` Step 0 於 L283 呼叫）、`preflight_activation.sh`（以 sed 擷取 `get_conf`／`write_active_json` 後 eval，L48–58）。`write_active_json` read-back 僅比對 ticket_id／revision／repo_root／head_sha 四欄。
  - `session_start.sh::report_active_ticket` 要求 `active.json` 剛好 9 行、末行（第 9 行）`}`、七 key 各 1 次，否則回報「不完整或損毀」；reset／inject 兩分支皆呼叫它。
  - `.claude/settings.json` L91–100：`SessionStart` matcher 如差異表 #5。
  - `preflight_activation.sh` 呼叫者全集（repo 內 `.claude`／`docs` 外 grep）：僅 `session_start.sh` 的文字提示與 `qa/SKILL.md` 的人工時機（L64），**無腳本帶 ticket 呼叫**，故無可機械識別之派工／啟動訊號。
  - `.gitignore:81` 命中 `plans/cso_briefing/project/*`（Pending 票檔 untracked 為既定慣例）；對 ALLOWED 九檔 `git check-ignore` 零命中；既有七檔皆已追蹤。
  - 既有測試編碼了待改行為：`registry_review_stop_guard_test.sh` 案例 ⑤ 斷言「舊 session 標記被淘汰」、多數案例以 `write_conf`（`verify_cmd.conf`）提供票號、末案「verify_cmd.conf fallback when active.json absent → block」；`gate_stop_guard_test.py` L50 fixture 為 `{"ticket_id": TICKET}`，payload 預設 `session_id="s1"`（L112）；`test_gate_ledger.py` `CarrierSurvival`（L604 起）把真實 `session_start.sh` 複製到暫存 repo 以 `reset`／`inject` 執行。
- ③ **SUPPORTED（取證附註，未重驗）**：seq 9–14 對應 mod session `6dde7a0e-da23-4b73-ac49-eb502258d0ba`／prompt `fbb46f9f-7c5a-4a0f-8c74-71aea7f76b56`（`deliver/ADV-LAYER-01_budget.md` 取證附註；依指示不重查）。
- ④ **UNKNOWN（本票不依賴其結論）**：subagent、Codex worktree、手動終端機內 `CLAUDE_CODE_SESSION_ID` 是否存在 → 一律視同「取不到 SID」，走 fail-open／警示分支。
- **Baseline 測試**：簽發時（2026-10-15 約 14:00，main @ `37e2f37d`）已實測：`gate_stop_guard_test.py` 48 tests OK、`test_gate_ledger.py` 53 tests OK、`registry_review_stop_guard_test.sh` ALL PASS；`test_preflight_adapter.sh` 因會改寫真實 `tmp/verify/active.json` 未於簽發時執行。AI-0 仍須於開工時重測並以開工值為準。

**External-review decision**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` §6「實作級外審」，2026-10-15 讀）：
- **6a（成立）**：`INTRODUCED_CAPABILITY=feature:ticket-owner`≠`NONE`；corpus 核對（`search_decisions` "ticket-owner" 0 hits；`Pending/`／`deliver/`／`void/`／`Handoff/`／兩份 registry 對 `ticket-owner` 與 `owner_session_id` 字面 grep 0 命中）→ 無完整字面命中，保守判成立。**處置＝重用已執行之定向研究 `rsch-20261015-4bf08abf30`（審查核可時採納），本票另有官方來源抽驗（`env-vars` 頁，證據分級 ①），不重送同題**（防遞歸律）。官方契約僅為契約層確認，**不是本地品質 PASS**；bounded probe＝AI-6 的決定性測試，**狀態：待執行**（AI-6 執行後才記實際結果）。官方文件所載「MCP server 子行程保留其原始啟動 ID」之限制只適用於 stdio MCP server 子行程，**不套用到 Bash／hook**（本票 owner 來源與核對皆在 Bash／hook）。**EXECUTED 不代表品質驗收 PASS，不豁免 Cross-Review。**
- 6b①：`ALLOWED` 九檔首層 path segment 皆 `.claude`，distinct＝1 <2，不成立；6b②：`### AI-[1-9][0-9]*:` 標題數＝7 <8，不成立；6b③：`DEPENDENCY_DELTA=NONE`，不成立；6c：各標的檔檔頭無 `Revision` 欄修訂歷史，不納本款。開工後若 AI 標題增至 ≥8 或 ALLOWED 增跨首層路徑，MUST 重判。

## Verify Config
TICKET_ID=ADV-OWNER-01
CREATED=2026-10-15
PRESET=feature
ALLOWED=.claude/scripts/activate_ticket.sh,.claude/skills/qa/scripts/verify_checklist.sh,.claude/hooks/session_start.sh,.claude/hooks/gate_stop_guard.py,.claude/hooks/registry_review_stop_guard.py,.claude/hooks/tests/gate_stop_guard_test.py,.claude/hooks/tests/registry_review_stop_guard_test.sh,.claude/hooks/tests/session_start_owner_test.sh,.claude/scripts/tests/owner_writer_test.sh
FORBIDDEN=.claude/settings.json,.claude/settings.local.json,.claude/CLAUDE.md,CLAUDE.md,CLAUDE.local.md,.claude/skills/qa/scripts/preflight_activation.sh,.claude/skills/qa/scripts/preflight_core.py,.claude/skills/qa/tests/test_preflight_adapter.sh,.claude/scripts/gate_ledger.py,.claude/scripts/tests/test_gate_ledger.py,.claude/rules/ssot-governance.md,.claude/rules/cross-review.md,.claude/agents/cross-reviewer.md,.claude/skills/sv/scripts/cross_review.sh,.claude/skills/sv/CROSS_REVIEW.md,.claude/hooks/stop.sh,.claude/hooks/telegram_stop.sh,.claude/hooks/notify.sh,.claude/hooks/telegram_ask.sh,.claude/hooks/snapshot_cueshot_memory.py,plans/cso_briefing/project/deliver/ADV-LAYER-01_budget.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-15 13:53
BASE_COMMIT=37e2f37d
TARGET_BLOB=.claude/scripts/activate_ticket.sh:9875c67e;.claude/skills/qa/scripts/verify_checklist.sh:dce90ad8;.claude/hooks/session_start.sh:2dd7e58f;.claude/hooks/gate_stop_guard.py:c3769490;.claude/hooks/registry_review_stop_guard.py:e40cc044;.claude/hooks/tests/gate_stop_guard_test.py:fa16f9f7;.claude/hooks/tests/registry_review_stop_guard_test.sh:8053054d
INTRODUCED_CAPABILITY=feature:ticket-owner
DEPENDENCY_DELTA=NONE

> PRESET=feature（`.py`／`.sh` 行為變更＋測試；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。`MAX_LINES=N/A`：含 fail-closed／守門／授權判定類不變量，失敗形狀為審查發現式，不可先驗估算（TPL v3.5 R3）；MAX_LINES 非 execution gate。`TARGET_BLOB` 僅列既有七檔（`git hash-object` 前 8 碼，2026-10-15 13:53 live 取得）；兩支新增測試檔無既有 blob，不列。`BASE_COMMIT`／`ANCHOR_PROBED`／`TARGET_BLOB` 為資訊性診斷值（TPL v3.11），不作 gate，AI-0 仍逐檔讀現值。`FORBIDDEN` 使用字面路徑（避開 `verify_checklist.sh` 對含 `/` token 的 glob 字面比對缺陷）。
>
> **`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 命中 `/qa` Step 4／4.5 標準觸碰面**：`DEFERRED-TO-DOCSYNC`——Dashboard 同步、`PROGRESS_ARCHIVE.md` 輪替、票檔 Pending→deliver 搬移與 AC 勾選走下一輪 `/compact` 標準收尾，不另開 DOCSYNC 票（`ssot-governance.md` §Ticket Closure 紀律「FORBIDDEN 延後項免開票」）。`TICKET_REGISTRY.json` 僅得經 mcp-ticket-platform `registry_*` 寫入，MUST NOT 直改。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: 見下列各 AI
- **動作**: 以 live `grep`／`Read` 逐字確認各 AI 之「現值」命中數；記錄 `git rev-parse --short HEAD` 與 `git status --short`；記錄 baseline 測試結果（`python -I .claude/hooks/tests/gate_stop_guard_test.py`、`python -I .claude/scripts/tests/test_gate_ledger.py`、`bash .claude/hooks/tests/registry_review_stop_guard_test.sh`，各自 tests／案例數與 OK/FAIL）。**不得**在派工前跑會改寫真實 `tmp/verify/active.json` 的 `test_preflight_adapter.sh`（它執行真實腳本於真實 repo）。
- 錨點：①`activate_ticket.sh`：字串 `\"activation_source\": \"%s\"\n}\n'` 恰 1 次；`"activate_ticket.sh"` 作為 printf 末參數恰 1 次。②`verify_checklist.sh`：`local repo_root branch head_sha activated_at src` 恰 1 次；printf 內 `\"activation_source\": \"%s\"\n}\n'` 恰 1 次；`write_active_json` 於 L283 之呼叫恰 1 處。③`session_start.sh`：`[[ "$total" == "9" ]] || ok=0` 恰 1 次；`last="$(sed -n '9p' "$ACTIVE_JSON" 2>/dev/null)"` 恰 1 次；`for key in ticket_id ticket_revision repo_root branch head_sha activated_at activation_source; do` 恰 1 次；`echo "📍 Active ticket: ${ticket_id} | ${branch} @ ${head_sha:0:8} | activated ${activated_at}"` 恰 1 次。④`gate_stop_guard.py`：`    ctx.ticket = ticket` 恰 1 次且緊接於 `TICKET_RE.match(ticket)` 失敗分支之後。⑤`registry_review_stop_guard.py`：`evict_stale_markers` 全檔恰 2 次（def＋呼叫）。⑥`gate_stop_guard_test.py`：`write_text(json.dumps({"ticket_id": TICKET}), encoding="utf-8")` 恰 1 次。⑦`registry_review_stop_guard_test.sh`：`echo "{\"ticket_id\": \"$1\"}" > "$SCRATCH/tmp/verify/active.json"` 恰 1 次。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置（本票僅觸及 `.claude/` 可逆治理面：`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 記錄後以現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止），禁自行推定等價位置。**
- [x] 完成（2026-10-15；HEAD `37e2f37d`，工作樹開工前 clean。錨點②③④⑥⑦全命中；①票面 AI-0 錨點字串含多餘反斜線〔markdown 跳脫殘留，非檔內字面〕，live 實檔 `"activation_source": "%s"
}
'` 於 `activate_ticket.sh`／`verify_checklist.sh` 各恰 1 次，分類 `ANCHOR_STALE_SCOPE_INTACT`〔票面撰寫層，記錄後以現值續作〕；⑤`evict_stale_markers` 全檔 2 次。Baseline：`gate_stop_guard_test.py` 48 OK、`test_gate_ledger.py` 53 OK、`registry_review_stop_guard_test.sh` ALL PASS；`test_preflight_adapter.sh` 派工前未跑，見 AI-7）

### AI-1: `activate_ticket.sh` — 唯一 claim 入口：寫入 `owner_session_id`、移轉提示、取不到 SID 警示
- **檔案**: `.claude/scripts/activate_ticket.sh`
- **現值**: 步驟 5a 以 inline printf 寫入七欄 `active.json`（`... "activation_source": "%s"\n}\n'`，末參數 `"activate_ticket.sh"`）。
- **目標值**:
  1. printf 於 `activation_source` 之後增第八欄 `"owner_session_id": "%s"`（`activation_source` 行補逗號；欄位順序與縮排與 AI-2 完全一致）；值＝`${CLAUDE_CODE_SESSION_ID:-}`（取不到則空字串）。不得以 cwd／branch／票號／讀票／最近活動猜 owner。
  2. 寫入前（僅讀）取既有 `active.json` 的 `owner_session_id`；既有檔缺失或不可解析視為無既有 owner（不輸出移轉提示、不報錯）；若非空且不等於新值 → 輸出一行 owner 移轉提示（可含兩端前 8 碼；明確 handoff＝新 session 重跑 activation，不新增協調機制）。
  3. 若 `CLAUDE_CODE_SESSION_ID` 為空 → 輸出一行警示：owner 未登記、票務 Stop guard 對此票不生效、需於可取得 session ID 的環境重跑 activation。警示 MUST NOT 使 exit code 非 0、MUST NOT 阻斷 activation，亦不使 `✅ ACTIVATED` 行改變。
  4. 檔頭註解「七欄」更新為含 owner 之八欄敘述。
- [x] 完成（`activate_ticket.sh:190-196` printf 八欄＋`CLAUDE_CODE_SESSION_ID`；`:203-207` 取不到 SID 警示／owner 移轉提示；既有檔缺失或不可解析→無移轉提示；檔頭註解更新。`owner_writer_test.sh` claim 各格 PASS）

### AI-2: `write_active_json`（`verify_checklist.sh`）— 恆為非 claim：同票保留 owner、異票清空
- **檔案**: `.claude/skills/qa/scripts/verify_checklist.sh`（僅 `write_active_json` 函式本體）
- **現值**: 函式以 printf 寫七欄；不讀既有檔內容。
- **目標值**: 於**函式本體內**（preflight 以 sed 擷取 `^write_active_json()` 至 `^}` 後 eval，MUST NOT 拆成函式外 helper）寫入前讀既有 `$dest`（缺檔／不可讀＝無 owner）：若既有 `ticket_id`＝本次 `$TICKET_ID` → 沿用既有 `owner_session_id`（含原本即空者：維持空，不順手認領）；否則 → 空字串。**不讀、不使用 `CLAUDE_CODE_SESSION_ID`**（呼叫者 env 有無／等於誰皆不影響此格）。printf 增第八欄，格式與 AI-1 位元組同構。read-back 四欄比對維持不變；既有 `📌 active.json:` sentinel 輸出字面不變。
- [x] 完成（`verify_checklist.sh:58-65` 函式本體內：同票沿用既有 owner、異票／缺檔／不可解析→空，不讀 env SID；`:76-77` printf 八欄與 AI-1 同構。`owner_writer_test.sh` 非 claim 各格＋同構檢查 PASS）

### AI-3: `session_start.sh` — 完整性校驗接受 8 欄；reset／inject 共用的非阻斷 owner 狀態提示
- **檔案**: `.claude/hooks/session_start.sh`
- **現值**: 校驗要求總行數 `9`、第 9 行 `}`、七 key 各 1 次；`report_active_ticket` 在通過後輸出 `📍 Active ticket: ...` 一行。
- **目標值**:
  1. 校驗改為：總行數 ∈ {9（舊 7 欄）, 10（含 `owner_session_id`）}；末行（第 `total` 行）為 `}`；七個必要 key 各 1 次且值非空；`owner_session_id` 為**選用** key（出現 ≤1 次，值允許為空字串）；若出現則總行數須為 10。其餘判定維持。
  2. 在 `report_active_ticket` 通過校驗後呼叫**同一個**新增函式（單一實作，reset／inject 共用，不複製平行邏輯）輸出至多一行狀態提示（前綴例如 `👤 Ticket owner:`），語意為：
     - lifecycle ∈ {`REVIEW`,`CLOSED`,`VOID`}，或冊面查無／無法判定（含 `python` 不可用、registry 不可讀）→ **不輸出**（CLOSED 票不提示重新承接；無法判定 lifecycle 時不假設為活態）。
     - `owner_session_id` 缺失或空 → 「未登記」：票務 Stop guard 對此票不生效，需承接請顯式 activation（不自動認領）。
     - owner 非空但 `CLAUDE_CODE_SESSION_ID` 為空 → 「無法取得當前 session ID，歸屬未確認」（**不得**報為已確認非 owner）。
     - owner 與 `CLAUDE_CODE_SESSION_ID` 相等 → 「本 session 為 owner」。
     - 兩者皆非空且不等 → 「其他 session 為 owner」，本 session 不受該票票務 Stop guard 控制，需承接請顯式 activation。
  3. 新增的 owner 提示函式 MUST 唯讀、非阻斷：不寫任何檔、不改 `active.json`／ledger／registry、不自動認領、恆 `exit 0`（維持 HC-1）；整支腳本之 `reset`／`inject` 既有合法副作用原樣保留，不新增副作用；lifecycle 查詢使用既有 `python`（hooks 已以 `python` 執行），失敗靜默。不新增 UI／通知機制，沿用既有 stdout 路徑。
- [x] 完成（`session_start.sh:17-39` 共用 `report_owner_status`；`:50-58` 校驗接受 9／10 行、owner 選用；`:84` 於 reset／inject 共用的 `report_active_ticket` 內呼叫。`session_start_owner_test.sh` reset／inject 各 20 項 PASS）

### AI-4: `gate_stop_guard.py` — 先核對 owner，非 owner 在任何 ledger／marker 操作前放行
- **檔案**: `.claude/hooks/gate_stop_guard.py`
- **現值**: `decide()` 取得 `ticket` 並驗證 `TICKET_RE` 後設 `ctx.ticket = ticket`，隨即讀 registry lifecycle、`ledger_status`。
- **目標值**: 於 `ctx.ticket = ticket` 之前插入 owner 核對：`active.get("owner_session_id")` 非字串、為空，或不等於 `payload["session_id"]`（原值，非 `_safe` 後值）→ `return None`（放行）。此核對 MUST 早於 `registry_lifecycle`／`ledger_status`／`missed-check`／`persistent_block`／任何 marker 讀寫。匹配 owner 時行為與現況完全相同。
- [x] 完成（`gate_stop_guard.py:442-444` 於 `ctx.ticket = ticket` 前核對 owner，早於 registry_lifecycle／ledger_status／marker）

### AI-5: `registry_review_stop_guard.py` — owner 核對；移除跨 session 清理
- **檔案**: `.claude/hooks/registry_review_stop_guard.py`
- **現值**: `main()` 在取得 `session_id` 後直接 `read_ticket_id()`；`evict_stale_markers(session_id)` 於寫 marker 前呼叫。
- **目標值**:
  1. 在 `session_id` 取得後、`read_ticket_id()` 之前，自 `ACTIVE_JSON` 讀 `owner_session_id`：缺檔／解析失敗／缺 key／空／不等於 `session_id` → `fail_open()`。因此 `verify_cmd.conf` 退路對 guard 不再生效（該分支程式碼本票不刪，列後續清理觀察）。
  2. 刪除 `evict_stale_markers` 函式定義與其呼叫（D2：不新增 GC，也不改為「受限清理」）。其餘 marker 邏輯（`{session_id}_{ticket_id}.blocked` 一次性攔截）不變。
- [x] 完成（`registry_review_stop_guard.py:95-100` `read_owner()`、`:115` 於 `read_ticket_id()` 前核對；`evict_stale_markers` 定義與呼叫皆刪除）

### AI-6: 測試 — 更新既有兩套 fixture、反轉舊行為案例、新增兩支測試
- **檔案**: `.claude/hooks/tests/gate_stop_guard_test.py`、`.claude/hooks/tests/registry_review_stop_guard_test.sh`（更新）；`.claude/hooks/tests/session_start_owner_test.sh`、`.claude/scripts/tests/owner_writer_test.sh`（新增）
- **目標值**:
  1. `gate_stop_guard_test.py`：`Env` fixture 的 `active.json` 補 `"owner_session_id": "s1"`（等於 `payload()` 預設 `session_id`），使既有案例原樣成立；新增：①非 owner（owner 為他值）同票同 cwd → 放行且 ledger 檔前後位元組相同、無 `tmp/.stop_guard` 新檔；②owner 缺 key／空字串／payload 無 `session_id` → 放行且無任何寫入；③owner 匹配仍照常 block（至少一個既有 block 案例顯式斷言）。
  2. `registry_review_stop_guard_test.sh`：以含 `owner_session_id`（＝`PAYLOAD_SID` 的 `sid-A`）的 `active.json` 提供預期 block 案例的票號；案例 ⑤ 反轉為「sid-B（非 owner）放行，且預置的 `sid-A_*` 與他 hook 的 `.seq`／他 session `.blocked` 於 owner 觸發一次 block 後仍在」；「verify_cmd.conf fallback when active.json absent → block」反轉為放行；新增缺 owner／空 owner／僅 conf → 放行且 `tmp/.stop_guard` 內容前後相同。
  3. `owner_writer_test.sh`（新增）：在暫存 git repo 內以**真實** `activate_ticket.sh` 與 sed 擷取之**真實** `write_active_json`（同 preflight 擷取法）驗證 writer state matrix 全格（見 Hard Constraints 之後的矩陣），並含一條端到端：`activate_ticket.sh`（env SID＝`sid-A`）→ 真實 `registry_review_stop_guard.py` 以 payload `sid-A` 攔截、以 `sid-B` 放行無副作用；再以 env SID＝`sid-B` 重跑 `activate_ticket.sh`（handoff）→ `sid-B` 攔截、`sid-A` 放行。
  4. `session_start_owner_test.sh`（新增）：在暫存 repo 複製真實 `session_start.sh`，以 `reset` 與 `inject` 兩模式、設定／清空 `CLAUDE_CODE_SESSION_ID`，斷言提示的**狀態語義**（owner 本 session／其他 session／未登記／歸屬未確認／CLOSED 等 REVIEW,CLOSED,VOID 不提示／舊 7 欄 `active.json` 不報損毀／含第 8 欄不報損毀）與**新增提示無副作用**：完整 `reset`／`inject` 保留其既有合法副作用（`reset` 清 `tmp/verify_cmd.conf`／`.tmp/sv_report.md`／`tmp/handoff.md`、建立 `tmp/`），測試驗證新增 owner 提示**未增加或改變**這些副作用（既有合法變動集合之外，樹內無任何新增／修改／刪除）；`active.json`、票務 ledger、Registry 的前後位元組／雜湊相同；exit 0。**不**以「`tmp/` 全部檔案完全不變」作為整支腳本的新要求。**不要求完整提示句逐字一致**，僅斷言可穩定判定的狀態標記或關鍵語義（見 AC）。
- [x] 完成（`gate_stop_guard_test.py` fixture owner＝`s1`，新增 `OwnerGate` 6 例 → 54 OK；`registry_review_stop_guard_test.sh` 重寫 ⑤、conf-fallback 反轉、新增 ⑤b 缺 owner 四型 → ALL PASS；新增 `session_start_owner_test.sh`、`owner_writer_test.sh` 皆 ALL PASS）

### AI-7: 驗證、殘留掃描與收尾實錄（不新增實作）
- **檔案**: 無新增實作檔；收尾實錄寫入本票檔（Pending→deliver 搬移屬標準收尾）。
- **動作**: 重跑四套既有測試與兩支新增測試；`grep -rn "evict_stale_markers" .claude/hooks .claude/scripts .claude/skills` 零命中（`PROGRESS.md` 歷史敘述除外）；`grep -n "owner_session_id"` 於 ALLOWED 內的 writer／consumer／測試全集與 AI-1～AI-5 一致；確認 `git diff --stat` 僅涉 ALLOWED；於票檔記錄：官方契約僅為契約層確認、**未做本地 env↔payload 比對**；補充性 L1 marker 比對**未做**則照實記「未做」，不記 PASS。ADV-LAYER-01 票檔不追加任何紀錄。`test_preflight_adapter.sh`／`test_gate_ledger.py` 為 FORBIDDEN 檔，僅可**執行**不可修改。**`test_preflight_adapter.sh` 隔離（執行端包裹，不修改該檔）**：執行前保存真實 `tmp/verify/active.json` 的**原始內容與存在狀態**（副本置於 `tmp/ADV-OWNER-01/`，`concurrency.md` §1），以 trap 確保結束或失敗時**恢復原樣**（原本不存在則刪除）並**讀回逐位元組比對確認**；**不得**以 `git status` 或重跑 activation 代替原樣恢復；不擴張成全域備份流程。**偵測 observer**：執行前後各取 `tmp/verify/` 目錄清單（檔名＋雜湊）比對，僅偵測、不備份；若發現 `active.json` 以外之檔被改寫，如實記錄並停止回報，不自行擴大備份範圍。
- [x] 完成（見「收尾實錄」。四套既有測試與兩支新增測試結果、`test_preflight_adapter.sh` 隔離執行與恢復讀回、殘餘窗口承接均已記錄）

## Hard Constraints
1. `owner_session_id` 的值**只能**來自 `activate_ticket.sh` 內的 `CLAUDE_CODE_SESSION_ID`；任何 hook、`write_active_json`、`session_start.sh`、`preflight_activation.sh` MUST NOT 寫入或覆寫 owner 為「當前 session」；MUST NOT 以 cwd、branch、票號出現、讀票、最近活動、prompt 文字或 `verify_cmd.conf` 推定 owner。
2. `write_active_json` 恆為非 claim：同票保留既有 owner（不受呼叫者 env SID 有無／等於誰影響），異票清空；`preflight_activation.sh` 不修改，不得因「帶 ticket」或「顯式 ticket_id」覆寫 owner。
3. 缺有效 owner／owner 空／payload 缺 `session_id` → 兩 hook 放行（D1-1）；不得 fail-closed、不得自動認領、不得因此累計 blocks、寫 ledger／pending 或要求轉態。政策 MUST NOT 外溢至其他 permissions、品質或轉態授權。
4. owner 核對 MUST 在任何 ledger／pending／registry-lifecycle／marker 讀寫之前（gate hook 早於 `ctx.ticket` 賦值，registry hook 早於 `read_ticket_id`）。
5. 清理：`evict_stale_markers` 及其呼叫全數移除；MUST NOT 以任何形式新增 GC、age 判定或「非本 session 即過期」判斷；MUST NOT 刪除他 session／他 hook 的 marker。
6. `active.json` 八欄格式（欄位順序、縮排、換行）在 `activate_ticket.sh` 與 `write_active_json` 必須位元組同構；舊 7 欄檔必須被所有 consumer 當作「無 owner」正常讀取，不得報損毀或崩潰。
7. `session_start.sh` 新增之 owner 提示函式 MUST 唯讀、非阻斷、恆 `exit 0`；不自動認領；當前 SID 不可得 MUST NOT 輸出「其他 session 為 owner」；`REVIEW`／`CLOSED`／`VOID` 及無法判定 lifecycle 者 MUST NOT 提示承接。不新增 UI／通知機制、不改 `settings.json`。
8. 不新增 coordinator／daemon／DB／第二套 state／人工認領流程；不改 ledger 格式、K 值、R0／Advisor Router；不清既有 ledger／pending；不重開或追加 ADV-LAYER-01。
9. `TICKET_REGISTRY.json` 僅得經 `registry_*` MCP 工具寫入，MUST NOT 直改（本票實作期間通常無需寫入）。
10. 不宣稱 hook 真實鏈已驗：本票測試為 [stub]／決定性子行程測試；官方契約僅為契約層確認，MUST NOT 記為本地實測 PASS。**殘餘窗口與承接者**：①真實 Stop 鏈（桌面 Claude Code 實際觸發 hook）未驗——依 parent plan §6「不跑真實票務 probe」不在本票範圍，承接者＝本票收尾實錄（AI-7）與 `/sv` Cross-Review 揭露；②`CLAUDE_CODE_SESSION_ID`↔payload `session_id` 未做本地比對——承接者同上，若後續出現具體矛盾依 Error Handling 對應條處理；③`gate_ledger.py` 缺省票號解析（見來源說明「非目標」）——承接者同上。

### writer state matrix（`owner_writer_test.sh` 逐格涵蓋）

| 既有 `active.json` | 本次寫入 | 入口 | env SID | 預期 `owner_session_id` |
|---|---|---|---|---|
| 無檔 | 新票 | `activate_ticket.sh`（claim） | 有 `sid-A` | `sid-A` |
| 無檔 | 新票 | claim | 空 | 空，且輸出取不到 SID 警示、exit 0 |
| 有檔（舊票，owner `sid-A`） | 另一票 | claim | 有 `sid-B` | `sid-B` |
| 有檔（舊 7 欄無 owner） | 同票 | claim | 有 `sid-A` | `sid-A` |
| **有檔（同票，owner `sid-A`）** | **同票** | **claim（handoff）** | **有 `sid-B`** | **`sid-B`**（含移轉提示；`sid-B` 受控、`sid-A` 放行） |
| 有檔（同票，owner `sid-A`） | 同票 | claim | 空 | 空（含警示；不保留舊 owner） |
| 有檔（owner `sid-A`） | 異票 | `write_active_json`（非 claim） | 有／空 | 空 |
| 有檔（無 owner／舊 7 欄） | 異票 | 非 claim | 有／空 | 空 |
| 有檔（同票，owner `sid-A`） | 同票 | 非 claim | 有（等於 `sid-A`／異於 `sid-A`）／空 | `sid-A`（保留，三種 env 皆然） |
| 有檔（同票，無 owner／舊 7 欄） | 同票 | 非 claim | 有／空 | 空（不順手認領） |
| 無檔 | 任意 | 非 claim | 有／空 | 空 |
| 有檔但不可解析（損毀） | 新票或同票 | claim | 有 `sid-A` | `sid-A`（無移轉提示、不報錯） |
| 有檔但不可解析（損毀） | 任意 | 非 claim | 有／空 | 空 |

## Error Handling
- 任一 AI-0 錨點不符 → 依 TPL v3.11 三分流處置；`ALREADY_EXECUTED_PENDING_CLOSURE` 或需動 FORBIDDEN 檔才能達成 → 停止回報。
- 實作需要修改 FORBIDDEN 檔（含 `preflight_activation.sh`、`test_preflight_adapter.sh`、`test_gate_ledger.py`、`settings.json`）才能讓既有測試通過 → 停止回報並升報（`SCOPE`），禁自行改 FORBIDDEN。
- `test_preflight_adapter.sh` 或 `test_gate_ledger.py`（含 `CarrierSurvival` 之 reset／inject）因八欄格式失敗 → 先本地取證判定是 consumer 校驗缺口（在 ALLOWED 內修）或需動 FORBIDDEN；後者停止回報。
- 實測發現 `CLAUDE_CODE_SESSION_ID` 與 Stop payload `session_id` 具體矛盾（直接證據顯示不等）→ 停止實作 owner 來源，回報並回 `plan` 重議取得入口，不退為猜測；無直接證據則不視為矛盾。
- 發現 owner 核對需要新增 state／檔案／協調機制才能成立 → 停止回報。
- 同一 incident 修復受 Gate 約束：依 `ssot-governance.md` §Autonomous Engineering Gate 處置（本票不改該文）。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律）。本票 ALLOWED 九檔 live 檢查零命中；Pending 票檔本身命中 `.gitignore:81` 屬既定慣例，非 commit 標的。

## Acceptance Criteria
- [x] **[存在性]** `activate_ticket.sh` 與 `write_active_json` 皆輸出含 `owner_session_id` 的八欄 `active.json`，兩處 printf 格式位元組同構（`owner_writer_test.sh` 以同一組輸入比對兩者輸出）。
- [x] **[可達性]** owner 的 production producer＝`activate_ticket.sh`（`/uc`、`/normal` 帶票號呼叫）寫入 `owner_session_id`；consumer＝`gate_stop_guard.py`、`registry_review_stop_guard.py`（Stop hook）與 `session_start.sh`。producer 證據欄：`.claude/scripts/activate_ticket.sh:190-196`（owner 寫入）；consumer 證據欄：`.claude/hooks/gate_stop_guard.py:442-444`、`.claude/hooks/registry_review_stop_guard.py:95-100,115`、`.claude/hooks/session_start.sh:19-39,84`（執行後回填，2026-10-15）；非 claim 保留端：`.claude/skills/qa/scripts/verify_checklist.sh:58-65,76-77`。`evict_stale_markers` 殘留：`grep -rn "evict_stale_markers" .claude/hooks .claude/scripts .claude/skills` 零命中（`.claude/PROGRESS.md` 一處為歷史 Dashboard 敘述，屬 FORBIDDEN／DEFERRED-TO-DOCSYNC，不計；2026-10-15 開工時限縮 grep 範圍，非降低驗收：程式碼目錄仍為零命中）。
- [x] **[行為性]** given owner＝`s1` 的 `active.json` 與 payload `session_id=s1` → expect 兩 hook 行為與改前一致（既有 block 案例原樣成立）。經真實 hook 子行程驅動，禁 fixture 直注被測 state。
- [x] **[行為性]** given 同票同 cwd、`owner_session_id`＝他 session → expect `gate_stop_guard.py` 輸出為空且 ledger 檔前後位元組相同、`tmp/.stop_guard` 無新檔；`registry_review_stop_guard.py` 輸出為空且 `tmp/.stop_guard` 無新檔（不累計 blocks、不寫 pending、不要求轉態）。
- [x] **[行為性]** given owner 缺 key／空字串／payload 無 `session_id`／僅 `verify_cmd.conf` 有票號 → expect 兩 hook 放行，ledger 與 `tmp/.stop_guard` 前後相同，且 `active.json` 未被改寫（不自動認領）。
- [x] **[行為性]** given `tmp/.stop_guard` 預置 `othersession_X.seq`（他 session）、`othersession_Y.blocked`（他 session 他 hook）與 `sid-A_*`，when owner `sid-A` 觸發一次 registry hook block → expect 預置各檔仍在且內容不變。
- [x] **[行為性]** writer state matrix 全格（見上表）逐格通過；關鍵格：①同票 claim handoff（`sid-A`→`sid-B`）後 `sid-B` 受控、`sid-A` 放行（端到端經真實 `activate_ticket.sh` 與 `registry_review_stop_guard.py`）；②非 claim 同票更新在 env SID 為空、為 owner、為他 session 三種情況下 owner 皆不變；③非 claim 異票 owner 空；④claim 取不到 SID → owner 空且輸出警示、exit 0、`✅ ACTIVATED` 行照舊。
- [x] **[行為性]** `session_start_owner_test.sh`：`reset` 與 `inject` 兩模式下，狀態語義依下表且彼此一致；所有情況 exit 0；新增提示未增加或改變 `session_start.sh` 既有合法副作用（`reset` 之 `verify_cmd.conf`／`sv_report.md`／`handoff.md` 清除與 `tmp/` 建立照舊；其餘無任何新增／修改／刪除），且 `active.json`、票務 ledger、Registry 前後雜湊相同（唯讀、不認領）；`inject` 模式不清任何檔：

  | `active.json` owner | env SID | ticket lifecycle | 預期提示語義 |
  |---|---|---|---|
  | 缺／空 | 有／空 | 活態 | 未登記（Stop guard 不生效；顯式 activation 才承接） |
  | 非空 | 空 | 活態 | 歸屬未確認（**不是**「其他 session 為 owner」） |
  | 非空＝env | 有 | 活態 | 本 session 為 owner |
  | 非空≠env | 有 | 活態 | 其他 session 為 owner |
  | 任意 | 任意 | `REVIEW`／`CLOSED`／`VOID`／冊面查無 | 不輸出 owner 提示 |
  | 舊 7 欄檔／含第 8 欄檔 | — | — | 均不報「不完整或損毀」 |

  斷言採穩定狀態標記或關鍵語義，不要求提示整句逐字一致。
- [x] **[行為性]** 既有 `test_gate_ledger.py`（含 `CarrierSurvival` 對真實 `session_start.sh` 的 reset／inject 與 ledger 位元組不變斷言）、`gate_stop_guard_test.py`、`registry_review_stop_guard_test.sh` 全綠（baseline 於 AI-0 記錄；更新／新增後數量如實記錄）；`test_preflight_adapter.sh` 重跑結果如實記錄（其為 FORBIDDEN 檔，僅執行，不修改）。
- [x] **[存在性]** 收尾實錄載明：官方契約為契約層確認、未做本地 env↔payload 比對；補充性 L1 marker 比對若未做則記「未做」，不記 PASS；ADV-LAYER-01 票檔無任何新增變更（`git diff` 對 `deliver/ADV-LAYER-01_budget.md` 為空）。
- ~~`assembleDebug` PASS~~ (N/A: 無 Android 產物；本票僅 `.sh`／`.py` 與測試)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（7 個既有檔修改＋2 個新增測試檔，皆在 ALLOWED 內；`git status` 無其他變動）

## 收尾實錄（Agent，2026-10-15；QA／SV 前）

- **範圍**：HEAD `37e2f37d`；修改 7 檔（`activate_ticket.sh`、`verify_checklist.sh`〔僅 `write_active_json` 函式本體〕、`session_start.sh`、`gate_stop_guard.py`、`registry_review_stop_guard.py`、兩支既有測試）＋新增 2 支測試（`session_start_owner_test.sh`、`owner_writer_test.sh`）；`preflight_activation.sh`、`settings.json`、`gate_ledger.py`、`test_gate_ledger.py`、`test_preflight_adapter.sh` 未改。
- **測試結果（實測，[stub]／子行程決定性測試）**：
  - `python -I .claude/hooks/tests/gate_stop_guard_test.py`：48 → **54 OK**（新增 `OwnerGate` 6 例：owner 受控、非 owner 同票同 cwd 放行且 ledger／marker 前後相同、owner 缺／空／非字串、payload 無 `session_id`、handoff、非 owner 連續 8 次不累計 pending）。
  - `bash .claude/hooks/tests/registry_review_stop_guard_test.sh`：**ALL PASS**（含反轉的跨 session 案例、`verify_cmd.conf` 不再作歸屬退路、缺 owner 四型放行且 marker／active.json 不變、他 session／他 hook marker 於 owner 觸發 block 後仍在）。
  - `bash .claude/scripts/tests/owner_writer_test.sh`（新增）：**ALL PASS**（writer state matrix 全格含同票 claim handoff、claim 取不到 SID、非 claim 同票三種 env SID、損毀檔；兩 writer 位元組同構；端到端經真實 `activate_ticket.sh`＋真實 `registry_review_stop_guard.py` 的 handoff）。
  - `bash .claude/hooks/tests/session_start_owner_test.sh`（新增）：**ALL PASS**（reset／inject 各 20 項：狀態語義四態、SID 不可得不報非 owner、`REVIEW`／`CLOSED`／`VOID`／冊面查無／registry 損毀均不提示、舊 7 欄與 8 欄均不報損毀；`reset` 副作用僅限其既有合法刪除〔`tmp/verify_cmd.conf`、`.tmp/sv_report.md`、`tmp/handoff.md`〕、`inject` 零變動；`active.json`／ledger／Registry 前後雜湊相同；exit 0）。
  - `python -I .claude/scripts/tests/test_gate_ledger.py`：**53 OK**（含 `CarrierSurvival` 對真實 `session_start.sh` reset／inject 的 ledger 位元組不變斷言）。
  - `bash .claude/skills/qa/tests/test_preflight_adapter.sh`（FORBIDDEN 檔，僅執行）：**39 passed／80 failed，與基線完全相同**。基線方式：臨時將 `activate_ticket.sh`／`verify_checklist.sh` 換回 `HEAD` 版本重跑同一隔離包裹，逐項 ✅／❌ 序列與修改後 diff 為空（`ADAPTER_RESULTS_IDENTICAL_BASE_VS_AFTER`），隨後以 `cmp` 確認我的版本原樣恢復（`MY_VERSIONS_RESTORED_IDENTICAL`）。失敗為**既有、與本票無關**（`R7` 核心缺席類斷言、缺少 `deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md` 等環境前提）；本票未使其變好或變壞，亦不宣稱其通過。
  - **adapter 測試隔離**（Rev A.1 要求）：包裹腳本 `tmp/ADV-OWNER-01/run_adapter_isolated.sh`（不修改該測試檔）執行前保存真實 `tmp/verify/active.json` 的原始內容與存在狀態，trap 於結束恢復；兩次執行均讀回確認 `RESTORE_READBACK=IDENTICAL`，且 `tmp/verify/` 目錄清單＋雜湊前後 `TMP_VERIFY_LISTING=IDENTICAL`（無 active.json 以外之檔被改寫）；未以 `git status` 或重跑 activation 代替恢復。
- **殘餘掃描**：`grep -rn "evict_stale_markers" .claude/hooks .claude/scripts .claude/skills` 零命中；`.claude/PROGRESS.md` 一處為歷史 Dashboard 敘述（FORBIDDEN／DEFERRED-TO-DOCSYNC）。AC 的 grep 範圍因此於開工時限縮為程式碼目錄（見 AC 內註記），程式碼目錄仍零命中。
- **未做／限制（照實揭露）**：
  1. **官方契約僅契約層確認**（`env-vars` 文件：`CLAUDE_CODE_SESSION_ID` 於 Bash／hook 與 payload `session_id` 一致且 `/clear` 更新）；**未做本地 env↔payload 比對**，補充性 L1 marker 比對**未做**，不記本地 PASS。
  2. **真實 Stop 鏈未驗**：所有 hook 測試為子行程決定性測試（[stub]），未在桌面 Claude Code 實際 Stop 觸發下驗證，不宣稱 hook 真實鏈已驗；承接：本收尾實錄與 `/sv` 揭露。
  3. **`gate_ledger.py` 缺省票號解析**（以 `active.json` 為票號來源，未核對 owner）：Stop guard 以外的同根殘餘窗口，本票依裁決不處理；承接：本收尾實錄與 `/sv` 揭露，是否另立票由審查方裁量。
  4. 目前 `tmp/verify/active.json` 為派工前由舊版 `activate_ticket.sh` 之前流程寫入的七欄檔（無 owner），本 session 未自我重新 claim → 對本 session 兩 hook 為 fail-open（符合 D1-1）；要讓本 session 受控須顯式重跑 activation，本票未自動認領。
  5. 未做變異驗證（暫時移除 owner 核對看測試是否失敗）：執行環境拒絕該複製式驗證指令，未改道繞過；測試的 teeth 由「owner 受控」與「非 owner 放行」成對案例及 handoff 案例間接承載，非機械變異證明。
  7. **`tmp/verify_cmd.conf` 於測試期間消失**（實作中途發現）：adapter 測試與本票腳本皆不引用該檔，疑為同 cwd 他 session 的 `SessionStart reset`（其既有行為 `rm -f tmp/verify_cmd.conf`）所致，**未能確認來源**；已依 `ticket-run` 步驟 10.1 由票面 Verify Config 重生（`DIFF_REF=37e2f37d`），**未**重跑 activation（避免非預期 claim）。此為跨 session 共享暫存檔的既有風險，與本票 owner 範圍相鄰但不屬本票修復。
  6. 開工時 AI-0 錨點①文字含 markdown 跳脫殘留（票面撰寫層）；已記錄為 `ANCHOR_STALE_SCOPE_INTACT`，以實檔現值續作。
- **ADV-LAYER-01**：未追加任何紀錄（`deliver/ADV-LAYER-01_budget.md` 工作樹無變動）。
- **PROGRESS／ARCHIVE／票檔搬移／registry 收尾**：`DEFERRED-TO-DOCSYNC`，於下一輪 `/compact` 標準收尾處理。

## SV／Cross-Review 實錄（2026-10-15，commit `e556a341`，範圍 `37e2f37d..e556a341`）

- **SV**：Stage A–C 無阻斷；`structural_verify.sh --post` PASS [ADVISORY]（A/B/C/D/E/F/G）。Compile／Lint／16KB ELF 為 N/A／SKIP（無 `app/` 變更）。報告：`.tmp/sv_report.md`（歸檔 `.tmp/sv_report_20261015_144409.md`）。
- **R0（4 次，皆為預過濾，非放行；記錄 `.tmp/fail-advisor/R0_ADV-OWNER-01_*.md` 共 4 份，時戳 143259〔round1 FAIL〕／143453〔round2 FORWARD〕／144053〔round3 FORWARD〕／144218〔round4 FORWARD〕；`.tmp` 非版控，彙總已入本節）**：round 1 判 **FAIL**（K3-6：payload 的 Git Diff 區段為空）——本地取證確認為 payload 產生當下 `tmp/verify_cmd.conf` 被外力刪除致 `ALLOWED` 讀不到、`.claude/` 被排除；非程式碼缺陷，已重建 conf 並重產 payload（9 個 diff header）；round 2–4 皆 FORWARD。R0 攔截不計入 fail budget（護欄三）；無 Akira override。
- **Codex Cross-Review（正式審查）**：第 1 次 **REPORT_FIX**（`REPORT_FIX_SOURCE=codex`）——SV 報告 Stage B 的可達性主張缺可核對舉證；依 Gate 開 incident `INC-001`（executor 層 1 輪）：補入官方 `env-vars` 文件抽驗（capture `toolu_015wiwGDWXQvYKpE91omtEBF.txt:17`）、hooks §Common input fields、本地觀測 A（`ENV_SID` 非空）與 B（production Stop payload 命名的 marker `5692ff86-…_ADV-LAYER-01.blocked`），並如實維持「同 session 值比對未做」的 PARTIAL 揭露；R0 另提 MINOR（`:283`→`:290` 與改前版行號註記）一併 report-only 修正；第 2 次 **ADVISORY**（exit 0，`.tmp/cross_review_result_20261015_144219.md`）。`INC-001` 以 `round end --result pass` 結案（`rounds=1`）。REPORT_FIX_COUNT 用 1／上限 2；Codex 無 BLOCKER、無 FAIL finding。
- **Codex 殘餘觀察（ADVISORY，記錄不阻斷）**：①同 session env↔payload 值比對與真實 Stop 鏈端到端驗證未做（owner gate production-path 測試缺口，未構成已確認 regression）；②`gate_ledger.py` 以 `active.json` 取票號未核對 owner（殘餘風險）。承接者：本實錄與 SV 報告；是否另立票由審查方裁量。
- **reviews 回填**：`qa=PASS`、`sv=PASS`、`codex=PASS`〔依 Transition Table「ADVISORY 無 BLOCKER／FAIL finding 視同 PASS」映射；**實際 verdict＝ADVISORY**，記於 checkpoint `codex_verdict_actual`〕、`traycer=N_A`（`Traycer Workflow: N/A`）。
- **作業期間偏差（如實）**：①`tmp/verify_cmd.conf`／`.tmp/sv_report.md` 於 QA→SV 期間數度被外力刪除（與每次子 agent 結束時序吻合，來源未確認；`session_start.sh reset` 為唯一會同時刪此二檔之程式），以 per-ticket 快照與 `.tmp/sv_report_*.md` 歸檔重建，並將 stub 重產 payload／R0／送 Codex 置於同一命令鏈內以避免再被刪；②為先產生 payload 而不呼叫 Codex，使用 PATH 上的 stub `codex`（`tmp/ADV-OWNER-01/stubbin/codex`，僅 exit 1），未寫入 `.tmp/cross_review_state` 的 round／verdict；③`PROGRESS.md`／`PROGRESS_ARCHIVE.md` 同步、Done 標記為 `DEFERRED-TO-DOCSYNC`。
- **未做（不變）**：本地 env↔payload 值比對、真實 Stop 鏈驗證、變異驗證；`test_preflight_adapter.sh` 39/80 為與基線相同的既有失敗。
